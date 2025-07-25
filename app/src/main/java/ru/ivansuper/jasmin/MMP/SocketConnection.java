package ru.ivansuper.jasmin.MMP;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import ru.ivansuper.jasmin.LogW;
import ru.ivansuper.jasmin.popup_log_adapter;

/**
 * Abstract class representing a socket connection.
 * This class provides the basic framework for establishing, maintaining, and managing
 * a network connection using sockets. It handles connection states, error reporting,
 * and data transmission (read/write).
 *
 * <p>Subclasses must implement abstract methods to define specific behaviors
 * for connection events (connect, connecting, disconnect, error, lost connection)
 * and raw data processing.
 *
 * <p>The connection process involves a dedicated {@link connectThread} for establishing
 * the connection asynchronously. Once connected, a {@link connectedThread} is started
 * to read incoming data, and a {@link writeThread} is started to handle outgoing data.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *     <li>Asynchronous connection establishment.</li>
 *     <li>Separate threads for reading and writing data.</li>
 *     <li>Callback mechanism for connection events and data reception.</li>
 *     <li>Error handling and reporting.</li>
 *     <li>Manages connection state (connected, connecting).</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * <ol>
 *     <li>Extend this class.</li>
 *     <li>Implement the abstract methods ({@code onConnect}, {@code onConnecting},
 *         {@code onDisconnect}, {@code onError}, {@code onLostConnection}, {@code onRawData}).</li>
 *     <li>Instantiate the subclass.</li>
 *     <li>Call the {@link #connect(String, int)} or {@link #connect(String)} method to initiate a connection.</li>
 *     <li>Use the {@link #write(ByteBuffer)} method to send data.</li>
 *     <li>Call the {@link #disconnect()} method to close the connection.</li>
 * </ol>
 *
 * @see Socket
 * @see InputStream
 * @see OutputStream
 * @see Thread
 * @see ByteBuffer
 */
public abstract class SocketConnection {
    public boolean connected = false;
    public boolean connecting = false;
    public int lastErrorCode = -1;
    public String lastServer = "none";
    public int lastPort = 0;
    private InetSocketAddress addr;
    private connectedThread connectedThrd;
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    private writeThread writeThrd;

    public abstract void onConnect();

    public abstract void onConnecting();

    public abstract void onDisconnect();

    public abstract void onError(int i);

    /** @noinspection unused*/
    public abstract void onLostConnection();

    public abstract void onRawData(ByteBuffer byteBuffer);

    private void errorOccured() {
        if (this.socket != null && this.connected) {
            try {
                this.socket.close();
            } catch (Exception ignored) {
            }
        }
        this.connecting = false;
        this.connected = false;
        if (this.writeThrd != null) {
            this.writeThrd.close();
        }
        try {
            this.socketIn.close();
            this.socketOut.close();
        } catch (Exception ignored) {
        }
        onError(this.lastErrorCode);
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
        if (this.writeThrd != null) {
            this.writeThrd.close();
        }
        try {
            this.socketIn.close();
        } catch (Exception ignored) {
        }
        try {
            this.socketOut.close();
        } catch (Exception ignored) {
        }
        onDisconnect();
    }

    public final void write(ByteBuffer source) {
        if (this.connected) {
            this.writeThrd.put(source);
        }
    }

    public void connect(String server, int port) {
        if (!this.connected) {
            this.lastServer = server;
            this.lastPort = port;
            this.socket = new Socket();
            connectThread cnt = new connectThread();
            cnt.setName("Socket connect thread");
            cnt.start();
        }
    }

    public void connect(String fullServerName) {
        if (!this.connected) {
            String[] server = fullServerName.split(":");
            this.lastServer = server[0];
            this.lastPort = Integer.parseInt(server[1]);
            this.socket = new Socket();
            connectThread cnt = new connectThread();
            cnt.setName("Socket connect thread");
            cnt.start();
        }
    }

    /** @noinspection CallToPrintStackTrace*/
    public void disconnect() {
        if (this.connected) {
            Log.v("SOCKET", "Disconnecting called");
            this.connected = false;
            if (this.writeThrd != null) {
                this.writeThrd.close();
            }
            try {
                this.socketIn.close();
                this.socketOut.close();
                this.socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.socketIn = null;
            this.socketOut = null;
            this.socket = null;
            System.gc();
            this.connecting = false;
            onDisconnect();
        }
    }

    public String getIp() {
        InetAddress addr = this.socket.getLocalAddress();
        return addr.getHostAddress();
    }

    public class connectThread extends Thread {
        public connectThread() {
        }

        @Override
        public void run() {
            SocketConnection.this.connecting = true;
            try {
                SocketConnection.this.onConnecting();
                SocketConnection.this.socket.setKeepAlive(true);
                SocketConnection.this.socket.setTcpNoDelay(true);
                SocketConnection.this.addr = new InetSocketAddress(SocketConnection.this.lastServer, SocketConnection.this.lastPort);
                SocketConnection.this.socket.connect(SocketConnection.this.addr, popup_log_adapter.DEFAULT_DISPLAY_TIME);
                SocketConnection.this.socket.setSoTimeout(0);
                SocketConnection.this.socketIn = SocketConnection.this.socket.getInputStream();
                SocketConnection.this.socketOut = SocketConnection.this.socket.getOutputStream();
                SocketConnection.this.connecting = false;
                SocketConnection.this.connected = true;
                SocketConnection.this.connectedThrd = new connectedThread();
                SocketConnection.this.connectedThrd.setDaemon(true);
                SocketConnection.this.connectedThrd.setName("Socket reader thread");
                SocketConnection.this.connectedThrd.start();
                SocketConnection.this.writeThrd = new writeThread();
                SocketConnection.this.writeThrd.setName("Socket write thread");
                SocketConnection.this.writeThrd.start();
            } catch (UnknownHostException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                SocketConnection.this.lastErrorCode = 1;
                SocketConnection.this.errorOccuredA();
            } catch (IOException e2) {
                //noinspection CallToPrintStackTrace
                e2.printStackTrace();
                SocketConnection.this.lastErrorCode = 2;
                SocketConnection.this.errorOccuredA();
            } catch (Exception e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
                SocketConnection.this.lastErrorCode = 255;
                SocketConnection.this.errorOccuredA();
            }
        }
    }

    private final class writeThread extends Thread {
        public Vector<ByteBuffer> queue;

        private writeThread() {
            this.queue = new Vector<>();
        }

        public void put(ByteBuffer buffer) {
            try {
                this.queue.add(buffer);
                synchronized (this) {
                    notify();
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        public void close() {
            synchronized (this) {
                notify();
            }
        }

        private ByteBuffer get() {
            if (!this.queue.isEmpty()) {
                return this.queue.remove(0);
            }
            return null;
        }

        @Override
        public void run() {
            SocketConnection.this.onConnect();
            while (SocketConnection.this.connected) {
                try {
                    synchronized (this) {
                        ByteBuffer buffer = get();
                        if (buffer != null) {
                            SocketConnection.this.socketOut.write(ByteBuffer.normalizeBytes(buffer.bytes, buffer.writePos));
                        } else {
                            wait();
                        }
                    }
                } catch (Exception e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    SocketConnection.this.lastErrorCode = 5;
                    SocketConnection.this.errorOccured();
                }
            }
        }
    }

    private class connectedThread extends Thread {
        /** @noinspection FieldCanBeLocal, unused */
        private final Object locker;
        private final ByteBuffer packet;

        private connectedThread() {
            this.packet = new ByteBuffer(65535);
            this.locker = new Object();
        }

        @Override
        public void run() {
            while (SocketConnection.this.connected) {
                try {
                    this.packet.readPos = 0;
                    int realyReaded = 0;
                    while (realyReaded < 44) {
                        int readed = SocketConnection.this.socketIn.read(this.packet.bytes, realyReaded, 44 - realyReaded);
                        if (readed == -1) {
                            SocketConnection.this.lastErrorCode = 8;
                            SocketConnection.this.errorOccured();
                            return;
                        }
                        realyReaded += readed;
                    }
                    int data_length = this.packet.previewDWordLE(16);
                    int realyReaded2 = 0;
                    while (realyReaded2 < data_length) {
                        int readed2 = SocketConnection.this.socketIn.read(this.packet.bytes, realyReaded2 + 44, data_length - realyReaded2);
                        if (readed2 == -1) {
                            SocketConnection.this.lastErrorCode = 9;
                            SocketConnection.this.errorOccured();
                            return;
                        }
                        realyReaded2 += readed2;
                    }
                    this.packet.readPos = 0;
                    this.packet.writePos = data_length + 44;
                    if (this.packet.previewDWordLE(0) != -559038737) {
                        throw new IOException("Invalid data (not a MRIM packet)");
                    }
                } catch (Exception e) {
                    if (SocketConnection.this.connected) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        SocketConnection.this.lastErrorCode = 4;
                        SocketConnection.this.errorOccured();
                        return;
                    }
                }
                try {
                    SocketConnection.this.onRawData(this.packet);
                } catch (Exception e2) {
                    LogW.trw("MRIMSocket", e2);
                    //noinspection CallToPrintStackTrace
                    e2.printStackTrace();
                }
            }
        }
    }
}
