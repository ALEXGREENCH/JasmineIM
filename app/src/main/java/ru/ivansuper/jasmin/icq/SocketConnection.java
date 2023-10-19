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

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void errorOccured() {
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
        onLostConnection();
    }

    public synchronized void errorOccuredA() {
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
        onError(this.lastErrorCode);
        onDisconnect();
    }

    public void write(ByteBuffer source) {
        if (this.connected) {
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
                onConnecting();
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                addr = new InetSocketAddress(lastServer, lastPort);
                socket.connect(addr, popup_log_adapter.INFO_DISPLAY_TIME);
                socket.setSoTimeout(0);
                socketIn = socket.getInputStream();
                socketOut = socket.getOutputStream();
                connecting = false;
                connected = true;
                connectedThrd = new connectedThread(SocketConnection.this, null);
                connectedThrd.setName("Socket reader thread");
                connectedThrd.start();
                writeThrd = new writeThread(SocketConnection.this, null);
                writeThrd.setName("Socket write thread");
                writeThrd.start();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                lastErrorCode = 1;
                errorOccuredA();
            } catch (IOException e2) {
                e2.printStackTrace();
                lastErrorCode = 2;
                errorOccuredA();
            } catch (Exception e3) {
                e3.printStackTrace();
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
            if (this.queue.size() > 0) {
                return this.queue.remove(0);
            }
            return null;
        }

        @Override
        public void run() {
            onConnect();
            while (connected) {
                try {
                    synchronized (this) {
                        ByteBuffer buffer = get();
                        if (buffer != null) {
                            socketOut.write(ByteBuffer.normalizeBytes(buffer.bytes, buffer.writePos));
                        } else {
                            wait();
                        }
                    }
                } catch (Exception e) {
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
                    this.flap.readPos = 0;
                    this.flap.writePos = data_length + 6;
                    if (this.flap.previewByte(0) != 42) {
                        throw new IOException("Invalid data (not a FLAP packet)");
                    }
                } catch (Exception e) {
                    if (connected) {
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