package ru.ivansuper.jasmin.icq.FileTransfer;

import android.annotation.SuppressLint;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import ru.ivansuper.jasmin.icq.ByteBuffer;
import ru.ivansuper.jasmin.icq.ByteCache;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.utilities;

/**
 * Represents an abstract base class for managing a socket connection,
 * potentially through a proxy, for file transfer purposes.
 *
 * This class handles the low-level details of establishing, maintaining,
 * and closing a socket connection. It supports different modes of operation,
 * including proxy authentication, OFT (presumably a specific file transfer protocol)
 * header handling, and raw data transfer.
 *
 * Subclasses must implement the abstract callback methods to handle
 * connection events (connect, connecting, disconnect, error, lost connection)
 * and data reception events (OFT header, proxy packet, raw file data).
 *
 * The class uses separate threads for connecting and for handling
 * incoming data once the connection is established. It also includes
 * basic error handling and logging.
 *
 * Key functionalities include:
 * - Connecting to a specified server and port.
 * - Disconnecting from the server.
 * - Writing data (as {@link ByteBuffer} or byte arrays) to the socket.
 * - Potentially upgrading the connection to SSL/TLS using a {@link NaiveTrustManager}.
 * - Managing connection state (connected, connecting).
 * - Storing information about the last connection attempt (server, port, error code).
 *
 * The class defines constants for different operational modes:
 * - {@link #PROXY_AUTH_MODE}: For handling proxy authentication.
 * - {@link #OFT_AUTH_MODE}: For handling OFT protocol headers.
 * - {@link #TRANSFERING_MODE}: For transferring raw file data.
 */
public abstract class ProxySocketConnection {
    /** @noinspection unused*/
    public static final int OFT_AUTH_MODE = 2;
    /** @noinspection unused*/
    public static final int PROXY_AUTH_MODE = 1;
    /** @noinspection unused*/
    public static final int TRANSFERING_MODE = 3;
    private connectedThread connectedThrd;
    public int mode;
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    public boolean connected = false;
    public boolean connecting = false;
    public int lastErrorCode = -1;
    public String lastServer = "none";
    public int lastPort = 0;
    byte[] oft_header_ethalon = {79, 70, 84, 50};

    public abstract void onConnect();

    public abstract void onConnecting();

    public abstract void onDisconnect();

    public abstract void onError(int i);

    public abstract void onLostConnection();

    public abstract void onOFTHeader(ByteBuffer byteBuffer);

    public abstract void onProxyPacket(ByteBuffer byteBuffer);

    public abstract void onRawFileData(byte[] bArr, int i);

    public ProxySocketConnection(int m) {
        this.mode = m;
        Log.v("ProxySocket", "Initialized with " + this.mode);
    }

    private void errorOccured() {
        if (this.socket != null && this.connected) {
            try {
                this.socket.close();
            } catch (Exception ignored) {
            }
        }
        this.connecting = false;
        this.connected = false;
        try {
            this.socketIn.close();
            this.socketOut.close();
        } catch (Exception ignored) {
        }
        onError(this.lastErrorCode);
        onLostConnection();
    }

    private void errorOccuredA() {
        if (this.socket != null && this.connected) {
            try {
                this.socket.close();
            } catch (Exception ignored) {
            }
        }
        this.connecting = false;
        this.connected = false;
        try {
            this.socketIn.close();
        } catch (Exception ignored) {
        }
        try {
            this.socketOut.close();
        } catch (Exception ignored) {
        }
        onError(this.lastErrorCode);
        onDisconnect();
    }

    public void write(ByteBuffer source) {
        if (this.connected) {
            writeA(source);
        }
    }

    /** @noinspection unused*/
    public void writeB(ByteBuffer source) {
        if (this.connected) {
            writeB(source);
        }
    }

    public void connect(String server, int port) {
        if (!this.connected) {
            this.lastServer = server;
            this.lastPort = port;
            Log.e("FT:SOCKET", "Connecting called (" + server + ":" + port + ")");
            connectThread cnt = new connectThread();
            cnt.start();
        }
    }

    /** @noinspection unused*/
    public void connect(String fullServerName) {
        if (!this.connected) {
            String[] server = fullServerName.split(":");
            this.lastServer = server[0];
            this.lastPort = Integer.parseInt(server[1]);
            Log.e("FT:SOCKET", "Connecting called");
            connectThread cnt = new connectThread();
            cnt.start();
        }
    }

    public void disconnect() {
        if (this.connected) {
            this.connected = false;
            this.connectedThrd.cancel();
            try {
                this.socketIn.close();
                this.socketOut.close();
                this.socket.close();
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
            this.connecting = false;
            onDisconnect();
        }
    }

    public void writeA(ByteBuffer buffer) {
        if (this.connected) {
            try {
                this.socketOut.write(ByteBuffer.normalizeBytes(buffer.bytes, buffer.writePos));
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                this.lastErrorCode = 5;
                errorOccured();
            }
        }
    }

    /** @noinspection unused*/
    public void writeB(byte[] buffer) {
        if (this.connected) {
            try {
                this.socketOut.write(buffer);
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                this.lastErrorCode = 5;
                errorOccured();
            }
        }
    }

    public void writeA(byte[] buffer, int offset, int length) {
        if (this.connected) {
            try {
                this.socketOut.write(buffer, offset, length);
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                this.lastErrorCode = 5;
                errorOccured();
            }
        }
    }

    /** @noinspection unused*/
    public void jumpToSSL(String server, int port) {
        try {
            TrustManager[] tm = {new NaiveTrustManager()};
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(new KeyManager[0], tm, new SecureRandom());
            System.setProperty("javax.net.ssl.trustStore", "com.mycompany.mypackage.NaiveTrustManager");
            SSLSocketFactory factory = context.getSocketFactory();
            this.socket = factory.createSocket(this.socket, this.lastServer, this.lastPort, true);
            this.socketIn = this.socket.getInputStream();
            this.socketOut = this.socket.getOutputStream();
            onConnect();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            this.lastErrorCode = 0;
            errorOccured();
        }
    }

    @SuppressLint("CustomX509TrustManager")
    public static final class NaiveTrustManager implements X509TrustManager {

        /** @noinspection RedundantThrows*/
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        /** @noinspection RedundantThrows*/
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public class connectThread extends Thread {
        public connectThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Log.v("SOCKET", "Connect thread started! (" + ProxySocketConnection.this.lastServer + ":" + ProxySocketConnection.this.lastPort + ")");
            ProxySocketConnection.this.connecting = true;
            try {
                ProxySocketConnection.this.onConnecting();
                InetSocketAddress addr = new InetSocketAddress(ProxySocketConnection.this.lastServer, ProxySocketConnection.this.lastPort);
                ProxySocketConnection.this.socket = new Socket();
                ProxySocketConnection.this.socket.connect(addr, popup_log_adapter.DEFAULT_DISPLAY_TIME);
                ProxySocketConnection.this.socketIn = ProxySocketConnection.this.socket.getInputStream();
                ProxySocketConnection.this.socketOut = ProxySocketConnection.this.socket.getOutputStream();
                ProxySocketConnection.this.connecting = false;
                ProxySocketConnection.this.connected = true;
                ProxySocketConnection.this.connectedThrd = new connectedThread();
                ProxySocketConnection.this.connectedThrd.setName("FileTransferSocket");
                ProxySocketConnection.this.connectedThrd.start();
                ProxySocketConnection.this.onConnect();
            } catch (SocketTimeoutException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                ProxySocketConnection.this.lastErrorCode = 254;
                ProxySocketConnection.this.errorOccuredA();
            } catch (UnknownHostException e2) {
                //noinspection CallToPrintStackTrace
                e2.printStackTrace();
                ProxySocketConnection.this.lastErrorCode = 1;
                ProxySocketConnection.this.errorOccuredA();
            } catch (IOException e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
                ProxySocketConnection.this.lastErrorCode = 2;
                ProxySocketConnection.this.errorOccuredA();
            } catch (Exception e4) {
                //noinspection CallToPrintStackTrace
                e4.printStackTrace();
                ProxySocketConnection.this.lastErrorCode = 255;
                ProxySocketConnection.this.errorOccuredA();
            }
        }
    }

    private class connectedThread extends Thread {
        private boolean enabled = true;

        public void cancel() {
            this.enabled = false;
        }

        @Override
        public void run() {
            byte[] raw = new byte[32768];
            Log.v("SOCKET", "Connected thread started!!!");

            while (enabled) {
                if (!ProxySocketConnection.this.socket.isConnected()) {
                    ProxySocketConnection.this.lastErrorCode = 7;
                    ProxySocketConnection.this.errorOccured();
                    return;
                }

                try {
                    switch (ProxySocketConnection.this.mode) {
                        case 1: {
                            byte[] len = new byte[2];
                            int offset = 0;
                            while (offset < len.length) {
                                int read = ProxySocketConnection.this.socketIn.read(len, offset, len.length - offset);
                                if (read == -1 || !ProxySocketConnection.this.socket.isConnected()) {
                                    ProxySocketConnection.this.lastErrorCode = 4;
                                    ProxySocketConnection.this.errorOccured();
                                    enabled = false;
                                    return;
                                }
                                offset += read;
                            }

                            ByteBuffer temp = new ByteBuffer();
                            temp.write(len);
                            int packet_len = temp.readWord();

                            byte[] packet = new byte[packet_len];
                            offset = 0;
                            while (offset < packet.length) {
                                int read = ProxySocketConnection.this.socketIn.read(packet, offset, packet.length - offset);
                                if (read == -1 || !ProxySocketConnection.this.socket.isConnected()) {
                                    ProxySocketConnection.this.lastErrorCode = 4;
                                    ProxySocketConnection.this.errorOccured();
                                    enabled = false;
                                    return;
                                }
                                offset += read;
                            }

                            ByteBuffer proxy_packet = new ByteBuffer();
                            proxy_packet.write(packet);
                            ProxySocketConnection.this.onProxyPacket(proxy_packet);
                            break;
                        }

                        case 2: {
                            ByteBuffer oft2_packet = new ByteBuffer();
                            byte[] oft_header = new byte[4];
                            int offset = 0;
                            while (offset < oft_header.length) {
                                int read = ProxySocketConnection.this.socketIn.read(oft_header, offset, oft_header.length - offset);
                                if (read == -1 || !ProxySocketConnection.this.socket.isConnected()) {
                                    ProxySocketConnection.this.lastErrorCode = 4;
                                    ProxySocketConnection.this.errorOccured();
                                    enabled = false;
                                    return;
                                }
                                offset += read;
                            }

                            if (utilities.arrayEquals(oft_header, ProxySocketConnection.this.oft_header_ethalon)) {
                                oft2_packet.write(oft_header);

                                byte[] oft_len = new byte[2];
                                offset = 0;
                                while (offset < oft_len.length) {
                                    int read = ProxySocketConnection.this.socketIn.read(oft_len, offset, oft_len.length - offset);
                                    if (read == -1 || !ProxySocketConnection.this.socket.isConnected()) {
                                        ProxySocketConnection.this.lastErrorCode = 4;
                                        ProxySocketConnection.this.errorOccured();
                                        enabled = false;
                                        return;
                                    }
                                    offset += read;
                                }

                                oft2_packet.write(oft_len);
                                ByteBuffer oft2_len = new ByteBuffer();
                                oft2_len.write(oft_len);
                                int body_len = oft2_len.readWord() - 6;

                                byte[] oft_packet = new byte[body_len];
                                offset = 0;
                                while (offset < oft_packet.length) {
                                    int read = ProxySocketConnection.this.socketIn.read(oft_packet, offset, oft_packet.length - offset);
                                    if (read == -1 || !ProxySocketConnection.this.socket.isConnected()) {
                                        ProxySocketConnection.this.lastErrorCode = 4;
                                        ProxySocketConnection.this.errorOccured();
                                        enabled = false;
                                        return;
                                    }
                                    offset += read;
                                }

                                oft2_packet.write(oft_packet);
                                ProxySocketConnection.this.onOFTHeader(oft2_packet);
                            } else {
                                throw new IOException("OFT header mismatch");
                            }
                            break;
                        }

                        case 3: {
                            int read = ProxySocketConnection.this.socketIn.read(raw, 0, raw.length);
                            if (read > 0) {
                                byte[] raw_data = ByteCache.getByteArray(read);
                                System.arraycopy(raw, 0, raw_data, 0, read);
                                ProxySocketConnection.this.onRawFileData(raw_data, read);
                            } else {
                                //noinspection BusyWait
                                Thread.sleep(1000);
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    if (ProxySocketConnection.this.connected) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        ProxySocketConnection.this.lastErrorCode = 3;
                        ProxySocketConnection.this.errorOccured();
                        enabled = false;
                    }
                } catch (Exception e) {
                    if (ProxySocketConnection.this.connected) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        ProxySocketConnection.this.lastErrorCode = 4;
                        ProxySocketConnection.this.errorOccured();
                        enabled = false;
                    }
                }
            }
        }
    }
}
