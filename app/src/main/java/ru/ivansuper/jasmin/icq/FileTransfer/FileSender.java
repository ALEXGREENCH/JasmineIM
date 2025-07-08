package ru.ivansuper.jasmin.icq.FileTransfer;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.icq.ByteBuffer;
import ru.ivansuper.jasmin.icq.ByteCache;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class FileSender extends FileTransfer {
    public File file;
    private FileInputStream in;
    public ProxySocketConnection socket;
    public int sended = 0;
    /** @noinspection unused*/
    public int checksum = 0;
    public boolean accepted = false;
    public boolean sending = false;
    private boolean file_sended = false;
    public boolean initialized = false;

    public FileSender() {
        this.direction = 0;
    }

    public void createCookie() {
        ByteBuffer cookies_a = new ByteBuffer();
        try {
            cookies_a.writeLong(System.currentTimeMillis());
        } catch (Exception ignored) {
        }
        this.cookie = new byte[8];
        System.arraycopy(cookies_a.bytes, 0, this.cookie, 0, 8);
    }

    public void init() {
        this.initialized = true;
        this.contact.profile.svc.handleChatTransferNeedRebuild();
        int mode = 1;
        try {
            this.in = new FileInputStream(this.file);
        } catch (FileNotFoundException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        this.socket = new ProxySocketConnection(mode) {
            @Override
            public void onRawFileData(byte[] data, int length) {
                if (!FileSender.this.initialized) {
                    return;
                }
                FileSender.this.onRaw(data);
            }

            @Override
            public void onOFTHeader(ByteBuffer data) {
                if (!FileSender.this.initialized) {
                    return;
                }
                FileSender.this.onOFT(data);
            }

            @Override
            public void onProxyPacket(ByteBuffer data) {
                if (FileSender.this.initialized) {
                    Log.e("FileSender", "onProxyPacket()");
                    FileSender.this.onProxy(data);
                }
            }

            @Override
            public void onConnect() {
                if (FileSender.this.initialized) {
                    Log.e("FileSender", "Proxy connected");
                    FileSender.this.handleConnected();
                }
            }

            @Override
            public void onConnecting() {
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onLostConnection() {
                if (FileSender.this.initialized) {
                    Log.e("FileSender", "Connection losted");
                    FileSender.this.onLost();
                }
            }

            @Override
            public void onError(int errorCode) {
                if (FileSender.this.initialized) {
                    Log.e("FileSender", "Connection error: " + errorCode);
                    FileSender.this.onErrorA(errorCode);
                }
            }
        };
        Log.e("FileSender", "Request connecting to proxy");
        this.socket.connect("ars.icq.com", 5190);
    }

    /** @noinspection unused*/
    private void onErrorA(int error) {
        this.contact.profile.cancelAndRemoveTransfer(this.cookie);
        HistoryItem hst = new HistoryItem();
        hst.message = resources.getString("s_send_error_1");
        hst.direction = 1;
        hst.contact = this.contact;
        hst.isFileMessage = true;
        this.contact.history.add(hst);
        this.contact.profile.svc.handleChatNeedRebuild(this.contact);
    }

    private void onLost() {
    }

    private void handleConnected() {
        this.contact.profile.svc.handleChatTransferNeedRebuild();
        send(createProxySendInit());
    }

    private void onProxyReady() {
        Log.e("FileSender", "PROXY READY!");
        this.socket.mode = 2;
        send(createOFT2ForSend());
    }

    private void onProxy(ByteBuffer data) {
        Log.e("ProxyPacketRaw", utilities.convertToHex(data.getBytes()));
        data.skip(2);
        int command = data.readWord();
        if (command == 5) {
            onProxyReady();
        }
        if (command == 1) {
            Log.e("Proxy", "Proxy error");
            this.contact.profile.cancelAndRemoveTransfer(this.cookie);
        }
        if (command == 3) {
            data.skip(6);
            int port_a = data.readWord();
            byte a1 = data.readByte();
            byte a2 = data.readByte();
            byte a3 = data.readByte();
            byte a4 = data.readByte();
            byte[] ip_addr = {a1, a2, a3, a4};
            this.contact.profile.sendTransferRequest(this.contact.ID, this.cookie, ip_addr, port_a, this.file);
        }
    }

    private void onOFT(ByteBuffer data) {
        data.skip(6);
        int command = data.readWord();
        if (command == 514) {
            this.accepted = true;
            this.contact.profile.svc.handleChatTransferNeedRebuild();
            sending_thread t = new sending_thread();
            t.start();
            return;
        }
        if (command == 516) {
            handleSendingSuccess();
        }
    }

    /** @noinspection unused*/
    private void onRaw(byte[] raw_data) {
    }

    private void send(ByteBuffer source) {
        this.socket.write(source);
    }

    private void handleSendingSuccess() {
        Log.e("FileSender", "Sending success!");
        this.file_sended = true;
        this.contact.profile.cancelAndRemoveTransfer(this.cookie);
        this.contact.transfer_cookie = null;
        this.contact.profile.svc.handleChatTransferNeedRebuild();
        HistoryItem hst = new HistoryItem();
        hst.message = resources.getString("s_file_sended_successful");
        hst.direction = 1;
        hst.contact = this.contact;
        hst.isFileMessage = true;
        this.contact.history.add(hst);
        this.contact.profile.svc.handleChatNeedRebuild(this.contact);
    }

    private void handleFileTransferCanceled() {
        Log.e("FileSender", "Sending canceled!");
        this.contact.transfer_cookie = null;
        this.contact.profile.svc.handleChatTransferNeedRebuild();
        HistoryItem hst = new HistoryItem();
        hst.message = resources.getString("s_send_canceled");
        hst.direction = 1;
        hst.contact = this.contact;
        hst.isFileMessage = true;
        this.contact.history.add(hst);
        this.contact.profile.svc.handleChatNeedRebuild(this.contact);
    }

    @Override
    public void cancel() {
        if (!this.file_sended) {
            this.contact.profile.sendTransferCancel(this.contact.ID, this.cookie);
        }
        shutDown();
    }

    private class sending_thread extends Thread {

        @Override
        public void run() {
            while (FileSender.this.sended < FileSender.this.file_size && FileSender.this.socket.connected) {
                try {
                    byte[] raw = ByteCache.getByteArray(16384);
                    int readed = FileSender.this.in.read(raw, 0, 16384);
                    if (readed > 0) {
                        FileSender.this.socket.writeA(raw, 0, readed);
                        FileSender.this.sended += readed;
                        FileSender.this.contact.profile.svc.handleChatTransferRefreshProgress();
                    } else {
                        Log.e("FileSender", "Readed <= 0!");
                        //noinspection BusyWait
                        Thread.sleep(1000L);
                    }
                } catch (Exception e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    @Override
    public void shutDown() {
        this.initialized = false;
        try {
            this.socket.disconnect();
        } catch (Exception ignored) {
        }
        try {
            this.in.close();
        } catch (Exception ignored) {
        }
        if (!this.file_sended) {
            handleFileTransferCanceled();
        }
    }
}
