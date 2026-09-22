package ru.ivansuper.jasmin.jabber;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import ru.ivansuper.jasmin.resources;

/**
 * Finds where to connect for an XMPP domain the way RFC 6120 §3.2 says: the
 * {@code _xmpp-client._tcp.<domain>} SRV record (host and port), or, when the domain publishes
 * none, the domain itself on the default port.
 * <p>
 * Android has no public SRV API before 10, so this sends a plain UDP DNS query itself - to the
 * DNS servers of the active network first, then to public resolvers as a fallback - and parses
 * the SRV answers (with name compression). The whole lookup is bounded by a few seconds.
 */
public final class XmppSrvResolver {
    private static final String TAG = "XmppSrv";
    private static final int TYPE_SRV = 33;
    private static final int TIMEOUT_MS = 3000;
    private static final String[] PUBLIC_RESOLVERS = {"1.1.1.1", "8.8.8.8", "9.9.9.9"};
    private static final Random RANDOM = new Random();

    public static final class Target {
        public final String host;
        public final int port;
        /** true when the answer came from an SRV record, false for the plain-domain fallback */
        public final boolean fromSrv;
        /** true when no DNS server could be reached at all (as opposed to a definite "no record") */
        public final boolean lookupFailed;

        Target(String host, int port, boolean fromSrv, boolean lookupFailed) {
            this.host = host;
            this.port = port;
            this.fromSrv = fromSrv;
            this.lookupFailed = lookupFailed;
        }

        @Override
        public String toString() {
            return host + ":" + port + (fromSrv ? " (SRV)" : lookupFailed ? " (DNS lookup failed, domain itself)" : " (no SRV, domain itself)");
        }
    }

    public interface Callback {
        void onResult(Target target);
    }

    private XmppSrvResolver() {
    }

    /** Blocking; never call on the UI thread. Never returns null: falls back to {@code domain:fallbackPort}. */
    public static Target resolve(String domain, int fallbackPort) {
        domain = domain == null ? "" : domain.trim().toLowerCase();
        try {
            List<SrvRecord> records = querySrv("_xmpp-client._tcp." + domain);
            if (records != null && !records.isEmpty()) {
                Collections.sort(records, new Comparator<SrvRecord>() {
                    @Override
                    public int compare(SrvRecord a, SrvRecord b) {
                        if (a.priority != b.priority) return a.priority - b.priority;
                        return b.weight - a.weight;   // higher weight first within a priority
                    }
                });
                SrvRecord best = records.get(0);
                if (!".".equals(best.target) && !best.target.isEmpty()) {
                    Log.i(TAG, domain + " -> " + best.target + ":" + best.port + " (SRV, " + records.size() + " record(s))");
                    return new Target(best.target, best.port, true, false);
                }
                Log.i(TAG, domain + " publishes SRV '.' - service explicitly not available; using the domain");
            } else if (records == null) {
                Log.w(TAG, "SRV lookup for " + domain + " failed (no DNS answer); using the domain itself");
                return new Target(domain, fallbackPort, false, true);
            } else {
                Log.i(TAG, domain + " has no SRV record; using the domain itself");
            }
        } catch (Exception e) {
            Log.w(TAG, "SRV lookup for " + domain + " failed: " + e);
            return new Target(domain, fallbackPort, false, true);
        }
        return new Target(domain, fallbackPort, false, false);
    }

    /** Runs {@link #resolve} on a background thread and delivers the result on the UI thread. */
    public static void resolveAsync(final String domain, final int fallbackPort, final Callback callback) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final Target target = resolve(domain, fallbackPort);
                resources.service.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(target);
                    }
                });
            }
        }, "xmpp-srv");
        t.start();
    }

    // -- DNS ---------------------------------------------------------------------------------

    private static final class SrvRecord {
        int priority, weight, port;
        String target;
    }

    /**
     * Returns the SRV records, an empty list for a definite "none", or null when no resolver
     * answered. A "none" from the network's own resolvers is double-checked with a public one:
     * carrier and home-router resolvers are known to filter SRV or to keep a stale negative
     * answer cached for hours after a record is created.
     */
    private static List<SrvRecord> querySrv(String name) {
        List<String> system = systemResolvers();
        List<String> pub = new ArrayList<>();
        for (String r : PUBLIC_RESOLVERS) if (!system.contains(r)) pub.add(r);

        boolean anyAnswered = false;
        for (List<String> group : new List[]{system, pub}) {
            for (String resolver : group) {
                try {
                    List<SrvRecord> records = querySrv(name, resolver);
                    anyAnswered = true;
                    if (!records.isEmpty()) return records;
                    Log.i(TAG, resolver + " says " + name + " has no SRV record" + (group == system ? "; asking a public resolver too" : ""));
                    break;   // this group's verdict is "none": move on to the next group
                } catch (Exception e) {
                    Log.w(TAG, "resolver " + resolver + " failed: " + e);
                }
            }
        }
        if (!anyAnswered) {
            Log.w(TAG, "no resolver answered for " + name);
            return null;
        }
        return new ArrayList<>();
    }

    private static List<SrvRecord> querySrv(String name, String resolver) throws Exception {
        int id = RANDOM.nextInt(0xFFFF);
        byte[] query = buildQuery(id, name);
        DatagramSocket socket = new DatagramSocket();
        try {
            socket.setSoTimeout(TIMEOUT_MS);
            socket.send(new DatagramPacket(query, query.length, new InetSocketAddress(InetAddress.getByName(resolver), 53)));
            byte[] buf = new byte[4096];
            DatagramPacket reply = new DatagramPacket(buf, buf.length);
            socket.receive(reply);
            return parseSrvAnswers(buf, reply.getLength(), id);
        } finally {
            socket.close();
        }
    }

    private static byte[] buildQuery(int id, String name) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(id >>> 8);
        out.write(id);
        out.write(0x01);   // RD
        out.write(0x00);
        out.write(0); out.write(1);   // QDCOUNT
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        for (String label : name.split("\\.")) {
            if (label.isEmpty()) continue;
            byte[] b = label.getBytes("UTF-8");
            if (b.length > 63) throw new IllegalArgumentException("label too long");
            out.write(b.length);
            out.write(b);
        }
        out.write(0);
        out.write(0); out.write(TYPE_SRV);
        out.write(0); out.write(1);   // IN
        return out.toByteArray();
    }

    private static List<SrvRecord> parseSrvAnswers(byte[] b, int len, int expectedId) throws Exception {
        if (len < 12) throw new Exception("short reply");
        int id = ((b[0] & 0xFF) << 8) | (b[1] & 0xFF);
        if (id != expectedId) throw new Exception("id mismatch");
        int rcode = b[3] & 0x0F;
        if ((b[2] & 0x02) != 0) throw new Exception("truncated reply");
        if (rcode == 3) return new ArrayList<>();          // NXDOMAIN: definitely no record
        if (rcode != 0) throw new Exception("rcode " + rcode);
        int qd = ((b[4] & 0xFF) << 8) | (b[5] & 0xFF);
        int an = ((b[6] & 0xFF) << 8) | (b[7] & 0xFF);
        int[] pos = {12};
        for (int i = 0; i < qd; i++) {
            readName(b, pos);
            pos[0] += 4;
        }
        List<SrvRecord> out = new ArrayList<>();
        for (int i = 0; i < an; i++) {
            readName(b, pos);
            int type = ((b[pos[0]] & 0xFF) << 8) | (b[pos[0] + 1] & 0xFF);
            int rdlen = ((b[pos[0] + 8] & 0xFF) << 8) | (b[pos[0] + 9] & 0xFF);
            pos[0] += 10;
            int rdStart = pos[0];
            if (type == TYPE_SRV && rdlen >= 7) {
                SrvRecord r = new SrvRecord();
                r.priority = ((b[rdStart] & 0xFF) << 8) | (b[rdStart + 1] & 0xFF);
                r.weight = ((b[rdStart + 2] & 0xFF) << 8) | (b[rdStart + 3] & 0xFF);
                r.port = ((b[rdStart + 4] & 0xFF) << 8) | (b[rdStart + 5] & 0xFF);
                int[] p = {rdStart + 6};
                r.target = readName(b, p);
                out.add(r);
            }
            pos[0] = rdStart + rdlen;
        }
        return out;
    }

    /** Reads a (possibly compressed) domain name at pos[0], advancing pos[0] past it. */
    private static String readName(byte[] b, int[] pos) throws Exception {
        StringBuilder sb = new StringBuilder();
        int p = pos[0];
        int jumped = -1;
        int hops = 0;
        while (true) {
            if (p >= b.length) throw new Exception("bad name");
            int l = b[p] & 0xFF;
            if (l == 0) {
                p++;
                break;
            }
            if ((l & 0xC0) == 0xC0) {
                int ptr = ((l & 0x3F) << 8) | (b[p + 1] & 0xFF);
                if (jumped < 0) jumped = p + 2;
                p = ptr;
                if (++hops > 20) throw new Exception("compression loop");
                continue;
            }
            p++;
            if (sb.length() > 0) sb.append('.');
            sb.append(new String(b, p, l, "UTF-8"));
            p += l;
        }
        pos[0] = jumped >= 0 ? jumped : p;
        return sb.toString().toLowerCase();
    }

    // -- which resolvers to ask -----------------------------------------------------------

    private static List<String> systemResolvers() {
        List<String> out = new ArrayList<>();
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                ConnectivityManager cm = (ConnectivityManager) resources.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    List<Network> networks = new ArrayList<>();
                    if (Build.VERSION.SDK_INT >= 23 && cm.getActiveNetwork() != null) networks.add(cm.getActiveNetwork());
                    Collections.addAll(networks, cm.getAllNetworks());
                    for (Network n : networks) {
                        LinkProperties lp = cm.getLinkProperties(n);
                        if (lp == null) continue;
                        for (InetAddress a : lp.getDnsServers()) {
                            String s = a.getHostAddress();
                            if (!out.contains(s)) out.add(s);
                        }
                    }
                }
            }
            if (out.isEmpty() && Build.VERSION.SDK_INT < 26) {
                // old Android: the resolvers are exposed as system properties
                Class<?> sp = Class.forName("android.os.SystemProperties");
                java.lang.reflect.Method get = sp.getMethod("get", String.class);
                for (String key : new String[]{"net.dns1", "net.dns2"}) {
                    String v = (String) get.invoke(null, key);
                    if (v != null && !v.isEmpty() && !out.contains(v)) out.add(v);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot read system resolvers: " + e);
        }
        return out;
    }
}
