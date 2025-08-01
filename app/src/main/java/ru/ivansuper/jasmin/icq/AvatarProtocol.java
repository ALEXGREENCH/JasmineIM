package ru.ivansuper.jasmin.icq;

import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.File;

import ru.ivansuper.jasmin.resources;

import ru.ivansuper.jasmin.locale.Locale;
/**
 * Handles the ICQ avatar protocol for uploading and retrieving user avatars.
 * This class manages the connection to the avatar server, sends and receives
 * FLAP (Frame Layer Protocol) and SNAC (Simple Network Access Control) packets
 * to interact with the ICQ avatar service.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Establishing a connection to the avatar server.</li>
 *     <li>Handling server hello and cookie authentication.</li>
 *     <li>Processing server family information.</li>
 *     <li>Uploading new avatar images.</li>
 *     <li>Receiving and processing server replies for avatar uploads.</li>
 *     <li>Handling server icon replies (though currently, the retrieved icon data is not directly used beyond parsing).</li>
 *     <li>Managing connection state and automatic reconnection attempts upon disconnection.</li>
 * </ul>
 *
 * <p>The protocol involves a sequence of FLAP and SNAC messages exchanged between the client
 * and the server. This class encapsulates the logic for constructing these messages and
 * interpreting the server's responses.
 *
 * <p>Note: Some parts of the icon reply handling might be incomplete or not fully utilized
 * in the current implementation (e.g., {@code last_contact_for_result}).
 */
public class AvatarProtocol {
    public boolean connected = false;
    private byte[] cookie;
    /** @noinspection FieldCanBeLocal, unused */
    private ICQContact last_contact_for_result;
    private boolean operation_success;
    private ICQProfile profile;
    private SocketConnection socket;
    private int sequence = 255;

    public AvatarProtocol(ICQProfile profile, String address, byte[] cookies) {
        restart(profile, address, cookies);
    }

    public void restart(ICQProfile profile, String address, byte[] cookies) {
        this.profile = profile;
        this.cookie = cookies;
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
                AvatarProtocol.this.handleSocketConnected();
            }

            @Override
            public void onConnecting() {
            }

            @Override
            public void onDisconnect() {
                AvatarProtocol.this.connected = false;
                AvatarProtocol.this.handleDisconnect();
            }

            @Override
            public void onLostConnection() {
                AvatarProtocol.this.connected = false;
            }

            @Override
            public void onError(int errorCode) {
            }
        };
        this.socket.connect(address);
    }

    private void handleDisconnect() {
        this.profile.doRequestAvatarService();
    }

    private void handleIncomingFlap(ByteBuffer data) {
        FLAP flp = new FLAP(data);
        switch (flp.getChannel()) {
            case 1:
                ByteBuffer dataA = flp.getData();
                if (dataA.previewDWord(0) == 1) {
                    handleServerHello();
                    break;
                }
                break;
            case 2:
                handleSnacData(flp.getData());
                break;
            case 4:
                handleDisconnectFlapData(flp.getData());
                break;
        }
    }

    /** @noinspection unused*/
    public void uploadAvatar(final File file) {
        this.operation_success = false;
        Thread t = new Thread() {
            @Override
            public void run() {
                AvatarProtocol.this.profile.svc.displayProgress(resources.getString("s_changing_avatar"));
                AvatarProtocol.this.profile.svc.showAvatarProgress(Locale.getString("s_changing_avatar"));
                try {
                    AvatarProtocol.this.send(ICQProtocol.createAvatarUpload(file, AvatarProtocol.this.sequence));
                    sleep(7000L);
                    if (!AvatarProtocol.this.operation_success) {
                        AvatarProtocol.this.profile.svc.showMessageInContactList(AvatarProtocol.this.profile.nickname, resources.getString("s_change_avatar_error_1"));
                        throw new Exception();
                    }
                } catch (Exception e) {
                    AvatarProtocol.this.profile.svc.cancelProgress();
                    AvatarProtocol.this.profile.svc.cancelAvatarProgress();
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void handleSocketConnected() {

    }

    private void handleServerHello() {
        ByteBuffer cks = ICQProtocol.createSendCookies(this.cookie, this.profile.ID, this.sequence);
        send(cks);
    }

    /** @noinspection unused*/
    private void handleDisconnectFlapData(ByteBuffer data) {
        this.socket.disconnect();
    }

    private void handleSnacData(ByteBuffer data) {
        SNAC snc = new SNAC(data);
        switch (snc.getType()) {
            case 1:
                //noinspection SwitchStatementWithTooFewBranches
                switch (snc.getSubtype()) {
                    case 3:
                        handleServerFamilies(snc.getData(), snc.getFlags());
                        break;
                }
            case 16:
                switch (snc.getSubtype()) {
                    case 3:
                        handleServerUploadReply(snc.getData(), snc.getFlags());
                        break;
                    case 7:
                        handleServerIconReply(snc.getData(), snc.getFlags());
                        break;
                }
        }
    }

    /** @noinspection unused*/
    private void handleServerUploadReply(ByteBuffer data, int flags) {
        this.profile.svc.cancelProgress();
        this.profile.svc.cancelAvatarProgress();
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
        this.socket.disconnect();
    }

    /** @noinspection unused*/
    private void handleServerFamilies(ByteBuffer data, int flags) {
        ByteBuffer cks = ICQProtocol.createClientFamiliesAvatar(this.sequence);
        send(cks);
        ByteBuffer cks2 = ICQProtocol.createClientReadyAvatar(this.sequence);
        send(cks2);
    }

    /** @noinspection unused, CallToPrintStackTrace */
    private void handleServerIconReply(ByteBuffer data, int flags) {
        int len = data.readByte();
        data.readStringAscii(len);
        data.skip(3);
        int len2 = data.readByte();
        data.skip(len2 + 4);
        int len3 = data.readByte();
        data.skip(len3);
        int len4 = data.readWord();
        if (len4 >= 128) {
            byte[] icon_data = data.readBytes(len4);
            try {
                ByteArrayInputStream is = new ByteArrayInputStream(icon_data);
                Drawable.createFromStream(is, "avatar");
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.last_contact_for_result = null;
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