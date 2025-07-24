package ru.ivansuper.jasmin.icq;

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
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.popup_log_adapter;

/**
 * Abstract class representing a socket connection.
 * This class handles the low-level details of establishing and maintaining a socket connection,
 * including connecting, disconnecting, reading, and writing data.
 * <p>
 * Subclasses must implement the abstract callback methods to handle connection events and received data.
 * <p>
 * The connection process involves:
 * 1. Calling {@link #connect(String, int)} or {@link #connect(String)} to initiate a connection.
 * 2. A separate {@link connectThread} is started to handle the connection attempt in the background.
 * 3. During connection, {@link #onConnecting()} is called.
 * 4. If the connection is successful:
 *    - {@link #socketIn} and {@link #socketOut} are initialized.
 *    - A {@link connectedThread} (reader) and a {@link writeThread} (writer) are started.
 *    - {@link #onConnect()} is called (from the {@link writeThread}).
 * 5. If an error occurs during connection or while connected:
 *    - {@link #onError(int)} is called with an error code.
 *    - {@link #onLostConnection()} or {@link #onDisconnect()} is called depending on the error context.
 * <p>
 * Data is sent using the {@link #write(ByteBuffer)} method, which queues the data to be sent by the {@link writeThread}.
 * Incoming data is read by the {@link connectedThread} and passed to {@link #onRawData(ByteBuffer)} for processing.
 * <p>
 * The class manages the connection state ({@link #connected}, {@link #connecting}) and stores information
 * about the last connection attempt ({@link #lastServer}, {@link #lastPort}, {@link #lastErrorCode}).
 */
public abstract class SocketConnection {

    /**
     * @noinspection FieldCanBeLocal
     */
    private InetSocketAddress addr;
    /**
     * @noinspection FieldCanBeLocal
     */
    private connectedThread connectedThrd;
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    /**
     * @noinspection FieldCanBeLocal, unused
     */
    private final jasminSvc svc;
    private writeThread writeThrd;
    public boolean connected = false;
    public boolean connecting = false;
    public int lastErrorCode = -1;
    public String lastServer = "none";
    public int lastPort = 0;

    public abstract void onConnect();

    public abstract void onConnecting();

    public abstract void onDisconnect();

    public abstract void onError(int i);

    public abstract void onLostConnection();

    public abstract void onRawData(ByteBuffer byteBuffer);

    public SocketConnection(jasminSvc param) {
        this.svc = param;
    }

    private synchronized void errorOccured() {
        if (this.socket != null && this.connected) {
            try {
                this.socket.close();
            } catch (Exception ignored) {
            }
        }
        Log.e("SOCKET", "errorOccured, code=" + this.lastErrorCode);
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
        onLostConnection();
    }

    public synchronized void errorOccuredA() {
        if (this.socket != null && this.connected) {
            try {
                this.socket.close();
            } catch (Exception ignored) {
            }
        }
        Log.e("SOCKET", "errorOccuredA, code=" + this.lastErrorCode);
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
        onError(this.lastErrorCode);
        onDisconnect();
    }

    public void write(ByteBuffer source) {
        if (this.connected) {
            Log.v("SOCKET", "Queue write: " + source.writePos + " bytes");
            this.writeThrd.put(source);
        }
    }

    /**
     * @noinspection unused
     */
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

    public void disconnect() {
        if (this.connected || this.connecting) {
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

    /**
     * @noinspection unused
     */
    public String getIp() {
        InetAddress addr = this.socket.getLocalAddress();
        return addr.getHostAddress();
    }

    public class connectThread extends Thread {
        public connectThread() {
        }

        @Override
        public void run() {
            connecting = true;
            try {
                Log.v("SOCKET", "Connect thread started");
                onConnecting();
                Log.v("SOCKET", "onConnecting() callback fired");
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                Log.v("SOCKET", "Socket options set: keepAlive, tcpNoDelay");
                addr = new InetSocketAddress(lastServer, lastPort);
                Log.v("SOCKET", "Connecting to " + lastServer + ":" + lastPort);
                socket.connect(addr, popup_log_adapter.INFO_DISPLAY_TIME);
                Log.v("SOCKET", "Socket connected");
                socket.setSoTimeout(0);
                socketIn = socket.getInputStream();
                socketOut = socket.getOutputStream();
                Log.v("SOCKET", "I/O streams opened");
                connecting = false;
                connected = true;
                Log.v("SOCKET", "Launching reader and writer threads");
                connectedThrd = new connectedThread(SocketConnection.this, null);
                connectedThrd.setName("Socket reader thread");
                connectedThrd.start();
                writeThrd = new writeThread(SocketConnection.this, null);
                writeThrd.setName("Socket write thread");
                writeThrd.start();
            } catch (UnknownHostException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                Log.e("SOCKET", "Unknown host: " + e.getMessage());
                lastErrorCode = 1;
                errorOccuredA();
            } catch (IOException e2) {
                //noinspection CallToPrintStackTrace
                e2.printStackTrace();
                Log.e("SOCKET", "I/O exception: " + e2.getMessage());
                lastErrorCode = 2;
                errorOccuredA();
            } catch (Exception e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
                Log.e("SOCKET", "Unexpected error: " + e3.getMessage());
                lastErrorCode = 255;
                errorOccuredA();
            }
        }
    }


    public final class writeThread extends Thread {
        public Vector<ByteBuffer> queue;

        private writeThread() {
            this.queue = new Vector<>();
        }

        /**
         * @noinspection unused, unused
         */
        writeThread(SocketConnection socketConnection, writeThread writethread) {
            this();
        }

        public void put(ByteBuffer buffer) {
            try {
                this.queue.add(buffer);
                synchronized (this) {
                    notify();
                }
            } catch (Exception e) {
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
            Log.v("SOCKET", "writeThread started");
            onConnect();
            while (connected) {
                try {
                    synchronized (this) {
                        ByteBuffer buffer = get();
                        if (buffer != null) {
                            socketOut.write(ByteBuffer.normalizeBytes(buffer.bytes, buffer.writePos));
                            Log.v("SOCKET", "Written " + buffer.writePos + " bytes");
                        } else {
                            wait();
                        }
                    }
                } catch (Exception e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    lastErrorCode = 5;
                    errorOccured();
                }
            }
        }
    }

    private class connectedThread extends Thread {
        private final ByteBuffer flap;

        private connectedThread() {
            this.flap = new ByteBuffer(16384);
        }

        /**
         * @noinspection unused
         */
        connectedThread(SocketConnection socketConnection, connectedThread connectedthread) {
            this();
        }

        @Override
        public void run() {
            Log.v("SOCKET", "connectedThread started");
            while (connected) {
                int realyReaded = 0;
                while (realyReaded < 6) {
                    int readed = -1;
                    try {
                        readed = socketIn.read(this.flap.bytes, realyReaded, 6 - realyReaded);
                    } catch (IOException e) {
                        //throw new RuntimeException(e); todo;
                        Log.e("eee,", e.getLocalizedMessage());
                    }
                    if (readed == -1) {
                        lastErrorCode = 8;
                        errorOccured();
                        return;
                    }
                    realyReaded += readed;
                }
                try {
                    int data_length = (this.flap.bytes[5] & 255) | ((this.flap.bytes[4] & 255) << 8);
                    Log.v("SOCKET", "Incoming packet length=" + data_length);
                    int realyReaded2 = 0;
                    while (realyReaded2 < data_length) {
                        int readed2 = socketIn.read(this.flap.bytes, realyReaded2 + 6, data_length - realyReaded2);
                        if (readed2 == -1) {
                            lastErrorCode = 9;
                            errorOccured();
                            return;
                        }
                        realyReaded2 += readed2;
                    }
                    Log.v("SOCKET", "Packet received, total=" + (data_length + 6) + " bytes");
                    this.flap.readPos = 0;
                    this.flap.writePos = data_length + 6;
                    if (this.flap.previewByte(0) != 42) {
                        throw new IOException("Invalid data (not a FLAP packet)");
                    }
                } catch (Exception e) {
                    if (connected) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        lastErrorCode = 4;
                        errorOccured();
                        return;
                    }
                }
                try {
                    onRawData(this.flap);
                } catch (Exception e2) {
                    LogW.trw("ICQSocket", e2);
                    Log.e("Oscar_protocol_error", "An exception handled while parsing incoming data. Trying to keep connection");
                    e2.printStackTrace();
                }
            }
        }
    }
}