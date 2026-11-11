package ru.ivansuper.jasmin.icq;

import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import ru.ivansuper.jasmin.resources;

/**
 * The SSBI (family 0x10) connection of an ICQ profile: downloads contacts' avatars by their
 * BART id and uploads our own.
 *
 * <p>The BOS server hands it out with SNAC 01/05 after we ask for family 0x10 (01/04). After the
 * cookie and 01/02 it is ready; requests (10/06) go one at a time from the profile's queue and
 * each 10/07 reply carries the picture, which is stored in avatars/&lt;uin&gt; with its BART id in
 * avatars/&lt;uin&gt;.bart. When the server closes the connection it is asked for again only while
 * avatars are still waiting, a few times at most.
 */
public class AvatarProtocol {
    private static final String TAG = "SSBI";
    private static final int MAX_RESTARTS = 3;

    public boolean connected = false;
    /** Cookie sent and 01/02 done: requests can go. */
    private boolean ready = false;
    private byte[] cookie;
    private boolean operation_success;
    private ICQProfile profile;
    private SocketConnection socket;
    private int sequence = 255;
    /** UIN whose 10/06 is out; null when none. */
    private String pending;
    private int restarts;
    private boolean closed;
    private boolean downHandled;

    public AvatarProtocol(ICQProfile profile, String address, byte[] cookies) {
        restart(profile, address, cookies);
    }

    public void restart(ICQProfile profile, String address, byte[] cookies) {
        if (this.socket != null && this.socket.connected) {
            try {
                this.socket.disconnect();
            } catch (Exception ignored) {
            }
        }
        this.profile = profile;
        this.cookie = cookies;
        this.ready = false;
        this.pending = null;
        this.closed = false;
        this.downHandled = false;
        Log.i(TAG, profile.ID + " connecting to " + address);
        this.socket = new SocketConnection(profile.svc) {
            @Override
            public void onRawData(ByteBuffer data) {
                if (!FLAP.itIsFlapPacket(data)) {
                    return;
                }
                AvatarProtocol.this.handleIncomingFlap(data);
            }

            @Override
            public void onConnect() {
                AvatarProtocol.this.connected = true;
            }

            @Override
            public void onConnecting() {
            }

            @Override
            public void onDisconnect() {
                AvatarProtocol.this.handleDisconnect(this);
            }

            @Override
            public void onLostConnection() {
                AvatarProtocol.this.handleDisconnect(this);
            }

            @Override
            public void onError(int errorCode) {
                AvatarProtocol.this.handleDisconnect(this);
            }
        };
        this.socket.connect(address);
    }

    /** Closes the connection for good, e.g. when the profile goes offline. */
    public void close() {
        this.closed = true;
        try {
            this.socket.disconnect();
        } catch (Exception ignored) {
        }
    }

    public boolean isReady() {
        return this.connected && this.ready;
    }

    private void handleDisconnect(SocketConnection which) {
        synchronized (this) {
            if (which != this.socket) {
                return; // an old socket of a restart
            }
            if (this.downHandled) {
                return; // a socket can report its end more than once
            }
            this.downHandled = true;
            this.connected = false;
            this.ready = false;
            this.pending = null;
            this.restarts++;
        }
        Log.i(TAG, this.profile.ID + " closed");
        this.profile.onAvatarServiceClosed(!this.closed && this.restarts <= MAX_RESTARTS);
    }

    private void handleIncomingFlap(ByteBuffer data) {
        FLAP flp = new FLAP(data);
        switch (flp.getChannel()) {
            case 1:
                ByteBuffer dataA = flp.getData();
                if (dataA.previewDWord(0) == 1) {
                    handleServerHello();
                }
                break;
            case 2:
                handleSnacData(flp.getData());
                break;
            case 4:
                this.socket.disconnect();
                break;
        }
    }

    /** @noinspection unused*/
    public void uploadAvatar(final File file) {
        this.operation_success = false;
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    AvatarProtocol.this.send(ICQProtocol.createAvatarUpload(file, AvatarProtocol.this.sequence));
                    sleep(7000L);
                    if (!AvatarProtocol.this.operation_success) {
                        AvatarProtocol.this.profile.svc.showMessageInContactList(AvatarProtocol.this.profile.nickname, resources.getString("s_change_avatar_error_1"));
                        throw new Exception();
                    }
                } catch (Exception e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void handleServerHello() {
        send(ICQProtocol.createSendCookies(this.cookie, this.profile.ID, this.sequence));
    }

    private void handleSnacData(ByteBuffer data) {
        SNAC snc = new SNAC(data);
        ByteBuffer body = snc.getData();
        if ((snc.getFlags() & 0x8000) != 0) {
            int len = body.readWord();
            body.skip(len);
        }
        if (snc.getType() == 1) {
            if (snc.getSubtype() == 3) {
                handleServerFamilies();
            }
        } else if (snc.getType() == 16) {
            switch (snc.getSubtype()) {
                case 1:
                    handleServerError(body);
                    break;
                case 3:
                    handleServerUploadReply(body);
                    break;
                case 7:
                    handleServerIconReply(body);
                    break;
            }
        }
    }

    private void handleServerUploadReply(ByteBuffer data) {
        int unknown = data.readDWord();
        if (unknown == 257) {
            if (data.writePos - data.readPos > 4) {
                int len = data.readByte();
                this.profile.buddy_hash = data.readBytes(len);
                this.profile.updateIconHash();
                this.operation_success = true;
                this.profile.makeShortToast(resources.getString("s_change_avatar_success"));
            } else {
                this.profile.makeShortToast(resources.getString("s_change_avatar_error_2"));
            }
        } else {
            this.profile.makeShortToast(resources.getString("s_change_avatar_error_2"));
        }
    }

    private void handleServerFamilies() {
        send(ICQProtocol.createClientFamiliesAvatar(this.sequence));
        send(ICQProtocol.createClientReadyAvatar(this.sequence));
        this.ready = true;
        this.restarts = 0;
        Log.i(TAG, this.profile.ID + " ready");
        sendNext();
    }

    /** Sends the next queued avatar request unless one is out already. */
    public void sendNext() {
        ICQProfile.AvatarRequest r;
        synchronized (this) {
            if (!isReady() || this.pending != null) {
                return;
            }
            r = this.profile.nextAvatarRequest();
            if (r == null) {
                return;
            }
            this.pending = r.uin;
        }
        send(ICQProtocol.createAvatarRequest(r.hash, r.type, r.flags, r.uin, this.sequence));
    }

    private void handleServerError(ByteBuffer data) {
        int code = data.getBytesCountAvailableToRead() >= 2 ? data.readWord() : -1;
        String uin;
        synchronized (this) {
            uin = this.pending;
            this.pending = null;
        }
        Log.w(TAG, this.profile.ID + " error " + code + " for " + uin);
        if (uin != null) {
            this.profile.avatarRequestDone(uin);
        }
        sendNext();
    }

    /**
     * 10/07: uin, the requested BART id, a result byte, the BART id found (type word, flags byte,
     * length byte, hash), then the picture with a word length.
     */
    private void handleServerIconReply(ByteBuffer data) {
        String uin = null;
        try {
            uin = data.readStringAscii(data.readByte() & 0xFF);
            data.skip(3);
            data.skip(data.readByte() & 0xFF);
            data.skip(1); // result
            int type = data.readWord();
            int flags = data.readByte() & 0xFF;
            byte[] hash = data.readBytes(data.readByte() & 0xFF);
            int size = data.readWord();
            byte[] icon = size > 0 ? data.readBytes(size) : null;
            Log.i(TAG, this.profile.ID + " avatar of " + uin + ": " + size + " bytes");
            if (icon != null && isPicture(icon)) {
                store(uin, icon, ICQProfile.bartId(type, flags, hash));
            }
        } catch (Exception e) {
            Log.w(TAG, "bad 10/07", e);
        }
        synchronized (this) {
            this.pending = null;
        }
        if (uin != null) {
            this.profile.avatarRequestDone(uin);
        }
        sendNext();
    }

    private static boolean isPicture(byte[] data) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, o);
        return o.outWidth > 0 && o.outHeight > 0;
    }

    private void store(final String uin, byte[] icon, byte[] bart) {
        String dir = resources.dataPath + this.profile.ID + "/avatars/";
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(dir).mkdirs();
            write(new File(dir + uin), icon);
            write(new File(dir + uin + ".bart"), bart);
        } catch (Exception e) {
            Log.w(TAG, "cannot save avatar of " + uin, e);
            return;
        }
        final ICQContact contact = this.profile.contactlist.getContactByUIN(uin);
        if (contact == null) {
            return;
        }
        this.profile.svc.runOnUi(new Runnable() {
            @Override
            public void run() {
                try {
                    contact.readLocalAvatar();
                } catch (Throwable ignored) { // a picture Android cannot decode, or out of memory
                }
                AvatarProtocol.this.profile.svc.handleContactlistDatasetChanged();
            }
        });
    }

    private static void write(File f, byte[] data) throws Exception {
        FileOutputStream fos = new FileOutputStream(f);
        try {
            fos.write(data);
        } finally {
            fos.close();
        }
    }

    private void send(ByteBuffer buffer) {
        if (this.socket.connected) {
            this.socket.write(buffer);
            this.sequence++;
            if (this.sequence > 65535) {
                this.sequence = 0;
            }
        }
    }
}
