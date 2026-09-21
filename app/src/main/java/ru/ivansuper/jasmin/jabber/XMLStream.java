package ru.ivansuper.jasmin.jabber;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLSocket;

import ru.ivansuper.jasmin.LogW;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Decompiler;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.jzlib.ZInputStream;
import ru.ivansuper.jasmin.jabber.jzlib.ZOutputStream;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.security.TlsSupport;

/**
 * The XMPP socket layer: a plain TCP stream with reader/writer threads, optional STARTTLS
 * ({@link #jumpToSSL}) or direct TLS (port 5223), and optional zlib stream compression.
 * <p>
 * This is a source re-creation of the class that used to live in {@code Jasmine_BLOB.jar}; the
 * public surface ({@link JProfile} subclasses it anonymously and the jar's own classes call
 * {@link #write(Node, JProfile)}) is unchanged. What changed is the TLS handling, which now goes
 * through {@link TlsSupport}: all TLS versions enabled, SNI = XMPP domain, eager handshake,
 * bundled roots and real hostname verification. See {@code ENCRYPTION.md} / the git history.
 */
public abstract class XMLStream {
    private static final String TAG = "XMLStream";
    private static final int CONNECT_TIMEOUT = 4500;
    /** Mirrors the XML console into logcat (debug builds only); SASL payloads are hidden. */
    private static final boolean DEBUG_XML = ru.ivansuper.jasmin.BuildConfig.DEBUG;

    // error codes reported through lastErrorCode (kept from the original)
    private static final int ERR_GENERIC = 0;
    private static final int ERR_UNKNOWN_HOST = 1;
    private static final int ERR_IO = 2;
    private static final int ERR_READ = 4;
    private static final int ERR_WRITE = 5;
    private static final int ERR_CLOSED = 8;
    private static final int ERR_OTHER = 255;

    public boolean connected;
    public boolean connecting;
    /** Unused, kept for API compatibility with the jar version. */
    @SuppressWarnings("unused")
    public Handler flapHandler;
    public int lastErrorCode = -1;
    public int lastPort;
    public String lastServer = "none";

    private final JProfile profile;
    private final boolean force_tls;

    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    private connectedThread connectedThrd;
    private writeThread writeThrd;

    // incoming bytes -> chars: an incremental UTF-8 decoder keeps a split multi-byte sequence
    // between reads instead of the hand-rolled decoder the jar had
    private final ByteBuffer pending = ByteBuffer.allocate(64 * 1024);
    private final CharBuffer chars = CharBuffer.allocate(64 * 1024);
    private final CharsetDecoder utf8 = Charset.forName("UTF-8").newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);
    private final StringBuffer c_buffer = new StringBuffer();

    public XMLStream(JProfile profile, boolean force_tls) {
        this.profile = profile;
        this.force_tls = force_tls;
    }

    public abstract void onConnect();

    public abstract void onConnecting();

    public abstract void onDisconnect();

    /** @noinspection unused*/
    public abstract void onError(int errorCode);

    public abstract void onLostConnection();

    public abstract void onPacket(Node node);

    // -- connection lifecycle ------------------------------------------------------

    public void connect(String server, int port) {
        if (connected) return;
        pending.clear();
        chars.clear();
        utf8.reset();
        c_buffer.setLength(0);
        lastServer = server;
        lastPort = port;
        socket = new Socket();
        connectThread t = new connectThread();
        t.setName("Socket connect thread");
        t.start();
    }

    public void disconnect() {
        if (!connected && !connecting) return;
        Log.v("SOCKET", "Disconnecting called");
        c_buffer.setLength(0);
        connected = false;
        if (writeThrd != null) writeThrd.close();
        closeQuietly();
        socketIn = null;
        socketOut = null;
        socket = null;
        connecting = false;
        onDisconnect();
    }

    public String getIp() {
        return socket.getLocalAddress().getHostAddress();
    }

    /**
     * STARTTLS: called by {@link JProfile} from the reader thread when the server answered
     * {@code <proceed/>}. The parameters are ignored, as in the original: the socket we already
     * have is upgraded in place. The handshake completes before this returns, so once
     * {@link #onConnect()} re-sends the stream header everything travels encrypted.
     */
    public void jumpToSSL(String server, int port) {
        try {
            SSLSocket ssl = upgradeToTls(socket);
            socket = ssl;
            socketIn = ssl.getInputStream();
            socketOut = ssl.getOutputStream();
            onConnect();
        } catch (TlsSupport.CertificateRejectedException e) {
            Log.e(TAG, "STARTTLS certificate rejected: " + e.getMessage());
            reportCertificateFailure(e);
        } catch (Exception e) {
            LogW.trw(TAG, e);
            lastErrorCode = ERR_GENERIC;
            errorOccured();
        }
    }

    public void compressStreams() {
        Log.e(TAG, "Compressing streams ...");
        socketIn = new ZInputStream(socketIn);
        ZOutputStream out = new ZOutputStream(socketOut, 9);
        out.setFlushMode(3);   // Z_FULL_FLUSH after every stanza
        socketOut = out;
        Log.e(TAG, "Streams compressed");
        onConnect();
    }

    public void write(XMLPacket packet, JProfile profile) {
        if (!connected) return;
        writeThrd.put(packet);
    }

    public void write(Node node, JProfile profile) {
        if (!connected) return;
        writeThrd.put(node);
    }

    // -- TLS --------------------------------------------------------------------------

    private SSLSocket upgradeToTls(Socket plain) throws IOException {
        // The certificate must be for the XMPP domain; a certificate for the host the user
        // typed as "server" is accepted too, since they chose it explicitly.
        List<String> names = new ArrayList<>();
        names.add(profile.host);
        if (lastServer != null && !lastServer.equalsIgnoreCase(profile.host)) names.add(lastServer);
        return TlsSupport.upgrade(plain, profile.host, lastPort, names, PreferenceTable.ms_check_tls_certificate);
    }

    /** Same user-visible outcome as the jar's NaiveTrustManager: log, drop the profile, tell the user. */
    private void reportCertificateFailure(final Exception cause) {
        final String jid = profile.ID + "@" + profile.host;
        resources.service.svcHdl.postDelayed(new Runnable() {
            @Override
            public void run() {
                resources.service.put_log(jid + ": TLS:" + resources.getString("s_jabber_tls_error_1") + " (" + cause.getMessage() + ")");
                profile.disconnect();
                resources.service.showMessageInContactList(jid, resources.getString("s_jabber_tls_error_2"));
            }
        }, 300L);
        lastErrorCode = ERR_GENERIC;
        errorOccuredA();
    }

    // -- failure paths (names kept from the original) ----------------------------------

    /** Connection dropped while connected: close and report {@link #onLostConnection()}. */
    private void errorOccured() {
        boolean wasConnected = connected;
        connecting = false;
        connected = false;
        if (wasConnected) {
            if (writeThrd != null) writeThrd.close();
            closeQuietly();
        }
        onLostConnection();
    }

    /** Could not connect (or was told to stop): close and report {@link #onDisconnect()}. */
    private void errorOccuredA() {
        boolean wasConnected = connected;
        connecting = false;
        connected = false;
        if (wasConnected) {
            if (writeThrd != null) writeThrd.close();
            closeQuietly();
        }
        onDisconnect();
    }

    private void closeQuietly() {
        try {
            if (socketIn != null) socketIn.close();
        } catch (Exception ignored) {
        }
        try {
            if (socketOut != null) socketOut.close();
        } catch (Exception ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {
        }
    }

    // -- incoming bytes -> nodes ---------------------------------------------------------

    /** Appends {@code len} bytes to the pending buffer and decodes every complete UTF-8 sequence into {@link #c_buffer}. */
    private void decodeRaw(byte[] data, int len) {
        int off = 0;
        while (off < len) {
            int n = Math.min(len - off, pending.remaining());
            pending.put(data, off, n);
            off += n;
            pending.flip();
            utf8.decode(pending, chars, false);
            pending.compact();          // keeps an incomplete trailing sequence for the next read
            chars.flip();
            c_buffer.append(chars);
            chars.clear();
        }
    }

    // -- threads ---------------------------------------------------------------------------

    private final class connectThread extends Thread {
        @Override
        public void run() {
            connecting = true;
            onConnecting();
            try {
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(lastServer, lastPort), CONNECT_TIMEOUT);
                socket.setSoTimeout(0);
                if (force_tls) {
                    // "legacy SSL" port: TLS from the first byte, handshake before the stream opens
                    socket = upgradeToTls(socket);
                }
                socketIn = socket.getInputStream();
                socketOut = socket.getOutputStream();
                connecting = false;
                connected = true;
                connectedThrd = new connectedThread();
                connectedThrd.setName("XML stream reader thread");
                connectedThrd.setPriority(Thread.MIN_PRIORITY);
                connectedThrd.start();
                writeThrd = new writeThread();
                writeThrd.setName("XML stream writer thread");
                writeThrd.setPriority(Thread.MIN_PRIORITY);
                writeThrd.start();
            } catch (TlsSupport.CertificateRejectedException e) {
                Log.e(TAG, "TLS certificate rejected: " + e.getMessage());
                reportCertificateFailure(e);
            } catch (UnknownHostException e) {
                LogW.trw(TAG, e);
                lastErrorCode = ERR_UNKNOWN_HOST;
                errorOccuredA();
            } catch (IOException e) {
                LogW.trw(TAG, e);
                lastErrorCode = ERR_IO;
                errorOccuredA();
            } catch (Exception e) {
                LogW.trw(TAG, e);
                lastErrorCode = ERR_OTHER;
                errorOccuredA();
            }
        }
    }

    private final class connectedThread extends Thread {
        private final byte[] buffer = new byte[16384];
        private final Decompiler dec = Decompiler.getInstance();

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
            while (connected) {
                try {
                    InputStream in = socketIn;   // replaced under us by STARTTLS / compression
                    int n = in.read(buffer, 0, buffer.length);
                    if (n < 0) {
                        // end of stream: the server closed the connection
                        lastErrorCode = ERR_CLOSED;
                        errorOccured();
                        return;
                    }
                    if (n == 0) {
                        Thread.sleep(1000L);
                        continue;
                    }
                    decodeRaw(buffer, n);
                    if (DEBUG_XML) Log.d(TAG, "<< " + c_buffer);
                    Node node;
                    while ((node = dec.Decompile(c_buffer)) != null) {
                        try {
                            onPacket(node);
                        } catch (Exception e) {
                            // A bug in a stanza handler is not a reason to drop the session: log
                            // it (logcat + the app log) and keep reading.
                            Log.e(TAG, "packet handler failed on <" + node.getName() + ">", e);
                            LogW.trw("JABBERSocket", e);
                        }
                    }
                } catch (Exception e) {
                    if (!connected) return;   // closed on purpose while we were blocked in read()
                    Log.e(TAG, "read failed", e);
                    LogW.trw("JABBERSocket", e);
                    lastErrorCode = ERR_READ;
                    errorOccured();
                    return;
                }
            }
        }
    }

    private final class writeThread extends Thread {
        public final Vector<String> queue = new Vector<>();

        private String get() {
            return queue.size() > 0 ? queue.remove(0) : null;
        }

        public void close() {
            synchronized (this) {
                notify();
            }
        }

        public void put(XMLPacket packet) {
            enqueue(packet.getXML());
        }

        public void put(Node node) {
            enqueue(node.compile());
        }

        private void enqueue(String xml) {
            queue.add(xml);
            synchronized (this) {
                notify();
            }
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
            onConnect();
            while (connected) {
                try {
                    synchronized (this) {
                        String xml = get();
                        if (xml == null) {
                            wait();
                            continue;
                        }
                        profile.putIntoConsole(xml, 1);
                        if (DEBUG_XML) Log.d(TAG, ">> " + (xml.contains("<auth") || xml.contains("<response") ? "[SASL data hidden]" : xml));
                        OutputStream out = socketOut;   // replaced under us by STARTTLS / compression
                        out.write(xml.getBytes("UTF-8"));
                        out.flush();
                    }
                } catch (Exception e) {
                    if (!connected) return;
                    LogW.trw(TAG, e);
                    lastErrorCode = ERR_WRITE;
                    errorOccured();
                    return;
                }
            }
        }
    }
}
