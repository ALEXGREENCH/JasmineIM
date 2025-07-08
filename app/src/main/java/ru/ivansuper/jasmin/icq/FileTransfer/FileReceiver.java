package ru.ivansuper.jasmin.icq.FileTransfer;

import android.annotation.SuppressLint;
import android.text.SpannableStringBuilder;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.icq.ByteBuffer;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class FileReceiver extends FileTransfer {
    private File file;
    public String ip;
    private ByteBuffer oft;
    private FileOutputStream out;
    public String proxy_ip;
    public ProxySocketConnection socket;
    public boolean use_proxy;
    public String verified_ip;
    public long received = 0;
    public int files_count = 0;
    public int files_received = 0;
    public boolean redirected = false;
    public boolean accepted = false;
    private boolean files_received_b = false;
    private String save_path = "";
    public Vector<File> received_files = new Vector<>();

    public FileReceiver() {
        this.direction = 1;
    }

    public void init() {
        int mode;
        this.accepted = true;
        this.contact.profile.svc.handleChatTransferNeedRebuild();
        if (this.use_proxy) {
            mode = 1;
            Log.e("FileReceiver", "Proxy is used");
        } else {
            mode = 2;
        }
        Log.e("FileReceiver", "Client " + this.ip);
        Log.e("FileReceiver", "Proxy " + this.proxy_ip);
        Log.e("FileReceiver", "Verified " + this.verified_ip);
        Log.e("FileReceiver", "Port " + this.port);
        this.save_path = resources.JASMINE_INCOMING_FILES_PATH + this.contact.profile.ID + "/from_" + this.contact.ID + "/";
        this.socket = new ProxySocketConnection(mode) {
            @Override
            public void onRawFileData(byte[] data, int length) {
                FileReceiver.this.onRaw(data, length);
            }

            @Override
            public void onOFTHeader(ByteBuffer data) {
                Log.e("FileReceiver", "onOFTHeader()");
                FileReceiver.this.onOFT(data);
            }

            @Override
            public void onProxyPacket(ByteBuffer data) {
                Log.e("FileReceiver", "onProxyPacket()");
                FileReceiver.this.onProxy(data);
            }

            @Override
            public void onConnect() {
                FileReceiver.this.handleConnected();
            }

            @Override
            public void onConnecting() {
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onLostConnection() {
                FileReceiver.this.onLost();
            }

            @Override
            public void onError(int errorCode) {
                Log.e("FileReceiver", "Error: " + errorCode);
                FileReceiver.this.onErrorA(errorCode);
            }
        };
        if (this.use_proxy) {
            this.socket.connect(this.proxy_ip, 443);
        } else {
            this.socket.connect(this.ip, this.port);
        }
    }

    private void prepareFile() {
        try {
            this.out.close();
        } catch (Exception ignored) {
        }
        this.file = new File(this.save_path);
        if (!this.file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            this.file.mkdirs();
        }
        this.file = new File(this.save_path + this.file_name);
        if (this.file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            this.file.delete();
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            this.file.createNewFile();
        } catch (Exception ignored) {
        }
        try {
            this.out = new FileOutputStream(this.file);
        } catch (Exception ignored) {
        }
        this.received = 0L;
    }

    /** @noinspection unused*/
    private void onErrorA(int error) {
        if (!this.redirected) {
            this.redirected = true;
            this.socket.mode = 1;
            this.socket.connect("ars.icq.com", 5190);
            return;
        }
        this.contact.profile.cancelAndRemoveTransfer(this.cookie);
        HistoryItem hst = new HistoryItem();
        hst.message = resources.getString("s_recv_error_1");
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
        if (!this.redirected) {
            if (this.use_proxy) {
                send(createProxyReceiveInit());
            } else {
                this.contact.profile.sendTransferAccept(this.contact.ID, this.cookie);
            }
            return;
        }
        send(createProxySendInit());
    }

    private void onProxyReady() {
        Log.e("FileReceiver", "PROXY READY!");
        this.socket.mode = 2;
        if (!this.redirected) {
            this.contact.profile.sendTransferAccept(this.contact.ID, this.cookie);
        }
    }

    @SuppressLint("LongLogTag")
    private void onProxy(ByteBuffer data) {
        Log.e("FileReceiver:ProxyPacket", utilities.convertToHex(ByteBuffer.normalizeBytes(data.bytes, data.writePos)));
        data.skip(2);
        int command = data.readWord();
        if (command == 5) {
            onProxyReady();
        }
        if (command == 1) {
            Log.e("FileReceiver:Proxy", "Proxy error");
            this.contact.profile.cancelAndRemoveTransfer(this.cookie);
        }
        if (command == 3) {
            data.skip(6);
            int port_a = data.readWord();
            byte[] ip_addr = data.readBytes(4);
            Log.e(getClass().getSimpleName(), "Redirect: " + ip_addr[0] + ip_addr[1] + ip_addr[2] + ip_addr[3] + ":" + port_a);
            this.contact.profile.sendTransferRedirectToInverseProxy(this.contact.ID, this.cookie, ip_addr, port_a);
        }
    }

    private void onOFT(ByteBuffer data) {
        this.oft = new ByteBuffer(data.bytes);
        this.oft.writePos = data.writePos;
        this.socket.mode = 3;
        OFTParser op = new OFTParser(data);
        this.file_name = op.file_name;
        this.file_size = op.file_size;
        Log.e("FileReceiver:onOFT()", this.file_name + "(" + this.file_size + ")");
        prepareFile();
        sendAckOFT(data);
    }

    private void sendAckOFT(ByteBuffer data) {
        int size = data.writePos;
        data.writePos = 0;
        data.writePos += 6;
        data.writeWord(514);
        data.write(this.cookie);
        data.writePos = size;
        send(data);
    }

    private void onRaw(byte[] raw_data, int length) {
        this.received += length;
        try {
            this.out.write(raw_data, 0, length);
            this.contact.profile.svc.handleChatTransferRefreshProgress();
            if (this.received >= this.file_size) {
                this.received_files.addElement(this.file);
                this.socket.mode = 2;
                this.files_received++;
                ByteBuffer data = this.oft;
                int size = data.writePos;
                data.writePos = 0;
                data.writePos += 6;
                data.writeWord(516);
                data.writePos += 52;
                data.writeDWord((int) this.received);
                data.writePos = size;
                send(data);
                try {
                    this.out.close();
                } catch (IOException e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
                if (this.files_received < this.files_count) {
                    this.contact.profile.svc.handleChatTransferNeedRebuild();
                } else {
                    this.socket.disconnect();
                    handleFileReceived();
                }
            }
        } catch (Exception e2) {
            this.contact.profile.cancelAndRemoveTransfer(this.cookie);
            //noinspection CallToPrintStackTrace
            e2.printStackTrace();
        }
    }

    private void handleFileReceived() {
        Log.e("FileReceiver", "File successfuly received!");
        this.files_received_b = true;
        this.contact.profile.cancelAndRemoveTransfer(this.cookie);
        this.contact.transfer_cookie = null;
        this.contact.profile.svc.handleChatTransferNeedRebuild();
        HistoryItem hst = new HistoryItem();
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (this.files_count > 1) {
            ssb.append(resources.getString("s_files_successful_received")).append(":\n");
            for (int i = 0; i < this.files_count; i++) {
                File file = this.received_files.get(i);
                String name = file.getName();
                IntentSpan span = new IntentSpan(file.getAbsolutePath());
                int start = ssb.length();
                ssb.append(name).append("\n");
                ssb.setSpan(span, start, name.length() + start, 17);
            }
        } else {
            ssb.append(resources.getString("s_file_successful_received")).append(":\n");
            File file2 = this.received_files.get(0);
            String name2 = file2.getName();
            IntentSpan span2 = new IntentSpan(file2.getAbsolutePath());
            int start2 = ssb.length();
            ssb.append(name2);
            ssb.setSpan(span2, start2, name2.length() + start2, 17);
        }
        hst.messageS = ssb;
        hst.message = ssb.toString();
        hst.direction = 1;
        hst.contact = this.contact;
        hst.isFileMessage = true;
        this.contact.history.add(hst);
        this.contact.profile.svc.handleChatNeedRebuild(this.contact);
    }

    private void handleFileTransferCanceled() {
        Log.e("FileReceiver", "Receiving canceled!");
        this.contact.transfer_cookie = null;
        this.contact.profile.svc.handleChatTransferNeedRebuild();
        HistoryItem hst = new HistoryItem();
        hst.message = resources.getString("s_recv_canceled");
        hst.direction = 1;
        hst.contact = this.contact;
        hst.isFileMessage = true;
        this.contact.history.add(hst);
        this.contact.profile.svc.handleChatNeedRebuild(this.contact);
    }

    private void send(ByteBuffer source) {
        this.socket.write(source);
    }

    @Override
    public void cancel() {
        this.contact.profile.sendTransferCancel(this.contact.ID, this.cookie);
        shutDown();
    }

    @Override
    public void shutDown() {
        if (!this.files_received_b) {
            try {
                //noinspection ResultOfMethodCallIgnored
                this.file.delete();
            } catch (Exception ignored) {
            }
        }
        try {
            this.socket.disconnect();
        } catch (Exception ignored) {
        }
        try {
            this.out.close();
        } catch (Exception ignored) {
        }
        if (!this.files_received_b) {
            handleFileTransferCanceled();
        }
    }
}
