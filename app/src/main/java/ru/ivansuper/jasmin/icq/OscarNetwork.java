package ru.ivansuper.jasmin.icq;

import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;

/**
 * Per-profile OSCAR network: which server to log in to, how, which ARS proxy to use for files and
 * where avatars are downloaded from.
 *
 * <p>Two known networks are presets (like the sign-in page of JasmineKICQ for Symbian). A preset
 * only fills in the values ({@link #preset(int)}); every value stays editable, so a profile keeps
 * working when a network moves its servers.</p>
 *
 * <p>Stored in the 127 reserved bytes after each ICQ profile in profiles.cfg, so older builds still
 * read the file. A record without the marker (a profile saved by an older build) takes the old
 * system-wide values from the preferences.</p>
 */
public final class OscarNetwork {
    public static final int KICQ = 0;
    public static final int MANDARIN = 1;
    public static final int CUSTOM = 2;

    /** The values of R.array.auth_methods_values. */
    public static final int AUTH_XOR = 0;
    public static final int AUTH_MD5_OLD = 1;
    public static final int AUTH_HTTP = 2;
    public static final int AUTH_MD5 = 3;
    /** What the presets log in with. */
    public static final int PRESET_AUTH = AUTH_MD5_OLD;

    public static final String KICQ_SERVER = "195.66.114.37";
    public static final String KICQ_AVATAR_URL = "http://217.147.15.238";
    /** Mandarin has no avatar server of its own; it uses the same HTTP one as KICQ. */
    public static final String MANDARIN_AVATAR_URL = KICQ_AVATAR_URL;
    public static final String MANDARIN_SERVER = "oscar.mandarin.im";
    public static final String MANDARIN_FT_SERVER = "ars.mandarin.im";
    public static final int DEFAULT_PORT = 5190;
    public static final int DEFAULT_FT_PORT = 5191;

    public static final int RESERVED_SIZE = 127;
    private static final int MARKER = 0x4E; // 'N'
    /** 1: values of presets not stored; 2: + avatar server; 3: every value stored;
     *  4: Mandarin preset got an avatar server. */
    private static final int VERSION = 4;
    private static final int HEADER = 8;
    /** Room left for the three strings and their length bytes. */
    public static final int MAX_TEXT = RESERVED_SIZE - HEADER - 3;

    /** Which preset the values came from; CUSTOM when none. */
    public int network = KICQ;
    public int authMethod = PRESET_AUTH;
    public String server = KICQ_SERVER;
    public int port = DEFAULT_PORT;
    /** Empty means files can be received through the sender's proxy but not sent. */
    public String ftServer = "";
    public int ftPort = DEFAULT_FT_PORT;
    /** Base URL, avatars are read from base + "/avatar/" + uin; empty = no avatars over HTTP. */
    public String avatarUrl = KICQ_AVATAR_URL;

    public OscarNetwork copy() {
        OscarNetwork n = new OscarNetwork();
        n.network = network;
        n.authMethod = authMethod;
        n.server = server;
        n.port = port;
        n.ftServer = ftServer;
        n.ftPort = ftPort;
        n.avatarUrl = avatarUrl;
        return n;
    }

    /** The values a preset fills in; for CUSTOM, empty server fields. */
    public static OscarNetwork preset(int network) {
        OscarNetwork n = new OscarNetwork();
        n.network = network;
        n.authMethod = PRESET_AUTH;
        n.port = DEFAULT_PORT;
        n.ftPort = DEFAULT_FT_PORT;
        switch (network) {
            case KICQ:
                n.server = KICQ_SERVER;
                n.ftServer = "";
                n.avatarUrl = KICQ_AVATAR_URL;
                break;
            case MANDARIN:
                n.server = MANDARIN_SERVER;
                n.ftServer = MANDARIN_FT_SERVER;
                n.avatarUrl = MANDARIN_AVATAR_URL;
                break;
            default:
                n.network = CUSTOM;
                n.server = "";
                n.ftServer = "";
                n.avatarUrl = "";
                break;
        }
        return n;
    }

    public String getServer() {
        String s = server == null ? "" : server.trim();
        if (s.length() == 0) {
            s = preset(network).server;
        }
        return s.length() == 0 ? KICQ_SERVER : s;
    }

    public int getPort() {
        return validPort(port) ? port : DEFAULT_PORT;
    }

    public int getAuthMethod() {
        return authMethod;
    }

    /** Empty when this profile has no file transfer proxy. */
    public String getFtServer() {
        return ftServer == null ? "" : ftServer.trim();
    }

    public int getFtPort() {
        return validPort(ftPort) ? ftPort : DEFAULT_FT_PORT;
    }

    /** Without a trailing slash; empty when avatars are not downloaded. */
    public String getAvatarUrl() {
        String s = avatarUrl == null ? "" : avatarUrl.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** The avatar server a new profile of this network starts with. */
    public static String defaultAvatarUrl(int network) {
        return preset(network).avatarUrl;
    }

    public static boolean validPort(int p) {
        return p > 0 && p <= 65535;
    }

    /** Recognises a known network by its server, the way JasmineKICQ does for old accounts. */
    public static int networkOf(String host) {
        String s = host == null ? "" : host.trim().toLowerCase();
        if (s.equals("kicq.ru") || s.equals("195.66.114.37")) {
            return KICQ;
        }
        if (s.equals("oscar.mandarin.im") || s.equals("mandarin.im")) {
            return MANDARIN;
        }
        return CUSTOM;
    }

    /** A profile saved by an older build: the system-wide settings it used to connect with. */
    public static OscarNetwork fromLegacyPrefs(SharedPreferences sp) {
        OscarNetwork n = new OscarNetwork();
        String srv = "195.66.114.37";
        String prt = "5190";
        String auth = "0";
        String ft = "";
        String ftPort = String.valueOf(DEFAULT_FT_PORT);
        String avatar = KICQ_AVATAR_URL;
        if (sp != null) {
            try {
                srv = sp.getString("ms_server", srv);
                prt = sp.getString("ms_port", prt);
                auth = sp.getString("ms_auth_method", auth);
                ft = sp.getString("ms_ft_server", ft);
                ftPort = sp.getString("ms_ft_port", ftPort);
                avatar = sp.getString("ms_avatar_base_url", avatar);
            } catch (Exception ignored) {
            }
        }
        int known = networkOf(srv);
        int oldPort = parsePort(prt, DEFAULT_PORT);
        if (known != CUSTOM && oldPort == DEFAULT_PORT) {
            n = preset(known);
        } else {
            n.network = CUSTOM;
            n.server = srv == null ? "" : srv.trim();
            n.port = oldPort;
            n.authMethod = parseAuth(auth);
            // ars.icq.com is gone; start without a proxy unless another one was set
            n.ftServer = ft == null || ft.trim().equalsIgnoreCase("ars.icq.com") ? "" : ft.trim();
            n.ftPort = parsePort(ftPort, DEFAULT_FT_PORT);
        }
        n.avatarUrl = avatar == null ? "" : avatar.trim();
        return n;
    }

    public static int parsePort(String s, int fallback) {
        try {
            int p = Integer.parseInt(s.trim());
            return validPort(p) ? p : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    public static int parseAuth(String s) {
        try {
            int a = Integer.parseInt(s.trim());
            return a >= 0 && a <= 3 ? a : AUTH_XOR;
        } catch (Exception e) {
            return AUTH_XOR;
        }
    }

    /** Bytes the three strings take in the record; at most MAX_TEXT. */
    public int textBytes() {
        return utf8(server).length + utf8(ftServer).length + utf8(avatarUrl).length;
    }

    /** Fills the reserved block of a profile record; strings beyond the room are cut. */
    public byte[] toReserved() {
        byte[] out = new byte[RESERVED_SIZE];
        int i = 0;
        out[i++] = (byte) MARKER;
        out[i++] = (byte) VERSION;
        out[i++] = (byte) network;
        out[i++] = (byte) authMethod;
        out[i++] = (byte) (port >> 8);
        out[i++] = (byte) port;
        out[i++] = (byte) (ftPort >> 8);
        out[i++] = (byte) ftPort;
        i = putString(out, i, server);
        i = putString(out, i, ftServer);
        putString(out, i, avatarUrl);
        return out;
    }

    /** null when the block holds no network (a profile from an older build). */
    public static OscarNetwork fromReserved(byte[] b) {
        if (b == null || b.length < HEADER + 2 || (b[0] & 0xFF) != MARKER || (b[1] & 0xFF) < 1) {
            return null;
        }
        int version = b[1] & 0xFF;
        OscarNetwork n = new OscarNetwork();
        int i = 2;
        n.network = b[i++] & 0xFF;
        if (n.network > CUSTOM) {
            n.network = CUSTOM;
        }
        n.authMethod = b[i++] & 0xFF;
        if (n.authMethod > 3) {
            n.authMethod = AUTH_XOR;
        }
        n.port = ((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF);
        i += 2;
        n.ftPort = ((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF);
        i += 2;
        int[] pos = {i};
        n.server = getString(b, pos);
        n.ftServer = getString(b, pos);
        // version 1 had no avatar server; it was system-wide then
        n.avatarUrl = version >= 2 ? getString(b, pos) : defaultAvatarUrl(n.network);
        if (version < 4 && n.network == MANDARIN && n.avatarUrl.trim().length() == 0) {
            // the Mandarin preset had no avatar server before; an empty one was that default
            n.avatarUrl = MANDARIN_AVATAR_URL;
        }
        if (version < 3 && n.network != CUSTOM) {
            // a preset's server, ports, proxy and login were fixed then; take them now
            OscarNetwork p = preset(n.network);
            p.avatarUrl = n.avatarUrl;
            return p;
        }
        return n;
    }

    private static byte[] utf8(String s) {
        try {
            return (s == null ? "" : s.trim()).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

    private static int putString(byte[] out, int i, String s) {
        byte[] v = utf8(s);
        int len = Math.max(0, Math.min(v.length, Math.min(255, out.length - i - 1)));
        if (i >= out.length) {
            return i;
        }
        out[i++] = (byte) len;
        System.arraycopy(v, 0, out, i, len);
        return i + len;
    }

    private static String getString(byte[] b, int[] pos) {
        int i = pos[0];
        if (i >= b.length) {
            return "";
        }
        int len = b[i++] & 0xFF;
        if (len > b.length - i) {
            len = b.length - i;
        }
        String s;
        try {
            s = new String(b, i, len, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            s = "";
        }
        pos[0] = i + len;
        return s;
    }
}
