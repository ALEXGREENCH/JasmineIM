package ru.ivansuper.jasmin.icq;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Vector;

import ru.ivansuper.jasmin.AntispamBot;
import ru.ivansuper.jasmin.Clients.IcqCapsBase;
import ru.ivansuper.jasmin.Clients.IcqClientDetector;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.ContactsAdapter;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.LowLevelAntispam;
import ru.ivansuper.jasmin.MessagesDump;
import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.ProfilesAdapterItem;
import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.Service.PendingIntentHandler;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.SmileysManager;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.chats.ICQChatActivity;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.icq.FileTransfer.FileReceiver;
import ru.ivansuper.jasmin.icq.FileTransfer.FileSender;
import ru.ivansuper.jasmin.icq.FileTransfer.FileTransfer;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class ICQProfile extends IMProfile {

    /**
     * @noinspection unused
     */
    public static final int VISIBILITY_FOR_ALL = 1;
    /**
     * @noinspection unused
     */
    public static final int VISIBILITY_FOR_ALL_EX_INVISIBLE = 4;
    /**
     * @noinspection unused
     */
    public static final int VISIBILITY_FOR_CONTACTS = 5;
    /**
     * @noinspection unused
     */
    public static final int VISIBILITY_FOR_VISIBLE = 3;
    /**
     * @noinspection unused
     */
    public static final int VISIBILITY_INV_FOR_ALL = 2;
    private PendingIntentHandler PING_TASK;
    private final PendingIntentHandler ach_task_1;
    private String bos_server;
    public byte[] buddy_hash;
    private byte[] cookies;
    private boolean http_auth_used;
    private AvatarProtocol icon_proto;
    private SSIOperation lastAdd;
    private SSIOperation lastDelete;
    private SSIOperation lastRename;
    public String password;
    /**
     * @noinspection unused
     */
    private ping_thread pinger;
    private SocketConnection socket;
    public SharedPreferences sp;
    public String xdesc;
    public int xsts;
    public String xtitle;
    public String qip_status = null;
    public int visibilityId = GifDecoder.MaxStackSize;
    public int visibilityStatus = 1;
    public int sequence = 1;
    public final Contactlist contactlist = new Contactlist(this);
    public boolean authorized = false;
    public boolean authFirstStageCompleted = false;
    public boolean connectedToBOS = false;
    /**
     * @noinspection unused
     */
    public boolean connectionLosted = false;
    private boolean jumpingToBOS = false;
    private final Vector<HistoryItem> messagesForConfurming = new Vector<>();
    public final ArrayList<ssi_item> phantom_list = new ArrayList<>();
    public final ArrayList<ssi_item> visible_list = new ArrayList<>();
    public final ArrayList<ssi_item> invisible_list = new ArrayList<>();
    public final ArrayList<ssi_item> ignore_list = new ArrayList<>();
    private final reconnector rcn = new reconnector();

    //private boolean useMD5Login = true; // todo;...
    private boolean useMD5Login = false;
    private screen_controller screen_ctrlr = new screen_controller(this, null);
    private final ArrayList<String> offlineMessages = new ArrayList<>();
    private final ArrayList<FileTransfer> transfers = new ArrayList<>();
    private final ArrayList<InfoOperation> info_requests = new ArrayList<>();
    private final ICQContact temp_info_container = new ICQContact();
    private boolean ping_answer_received = true;
    public String buddy_name = "";
    public int buddy_group = 0;
    public int buddy_id = 0;
    public String away_text = "";
    public String away_text_backup = "";
    public final InfoContainer info_container = new InfoContainer();
    private ByteBuffer BUFFER = new ByteBuffer(0);

    private InfoOperation getInfoOperation(int id) {
        for (int i = 0; i < this.info_requests.size(); i++) {
            InfoOperation operation = this.info_requests.get(i);
            if (operation.id == id) {
                return operation;
            }
        }
        return null;
    }

    private void removeOperation(int id) {
        for (int i = 0; i < this.info_requests.size(); i++) {
            InfoOperation operation = this.info_requests.get(i);
            if (operation.id == id) {
                this.info_requests.remove(i);
                return;
            }
        }
    }

    private void putInfoOperation(String uin, int type, int id) {
        this.info_requests.add(new InfoOperation(uin, type, id));
    }

    public ICQProfile(String uin, String pass, jasminSvc svcParam, boolean autoconnect, boolean enabled) {
        this.password = "";
        this.xsts = -1;
        this.xtitle = " ";
        this.xdesc = " ";
        this.ach_task_1 = new PendingIntentHandler(("ach_task_" + this.ID + this.password).hashCode()) { // from class: ru.ivansuper.jasmin.icq.ICQProfile.1
            @Override
            public void run() {
            }
        };

        this.profile_type = 0;
        this.svc = svcParam;
        this.ID = uin;
        this.nickname = this.ID;
        this.password = pass;
        this.enabled = enabled;
        this.info_container.nickname = this.nickname;
        this.autoconnect = autoconnect;
        this.sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
        this.xsts = this.sp.getInt("xsts" + this.ID + "prf", -1);
        this.xtitle = getSavedXTitle(this.xsts);
        this.xdesc = getSavedXDesc(this.xsts);
        getSavedVisibility();
        this.openedInContactList = this.sp.getBoolean("pg" + this.ID, true);
        File profileDirectory = new File(resources.dataPath + this.ID);
        if (!profileDirectory.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            profileDirectory.mkdirs();
        }
        File avatars_dir = new File(resources.dataPath + this.ID + "/avatars/");
        if (!avatars_dir.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            avatars_dir.mkdirs();
        }
        File historyDirectory = new File(resources.dataPath + this.ID + "/history");
        if (!historyDirectory.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            historyDirectory.mkdirs();
        }
        File roster = new File(resources.dataPath + this.ID + "/roster.bin");
        if (!roster.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                roster.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (roster.length() > 0) {
            this.contactlist.loadFromLocalStorage(roster, this);
        }
        if (autoconnect && enabled) {
            this.status = Manager.getInt(this.ID + "status");
            setStatus(this.status);
            startConnectingChosed();
        }
    }

    private void initSocket() {
        this.socket = new SocketConnection(this.svc) {
            @Override
            public void onRawData(ByteBuffer data) {
                if (!this.connected && !this.connecting) {
                    return;
                }
                ICQProfile.this.proceedFlapPacket(data);
            }

            @Override
            public void onConnect() {
                Log.e("SOCKET", "Connected");
            }

            @Override
            public void onConnecting() {
                Log.e("SOCKET", "Connecting");
            }

            @Override
            public void onDisconnect() {
                Log.e("SOCKET", "Disconnected!");
                ICQProfile.this.handleProfileDisconnected();
            }

            @Override
            public void onLostConnection() {
                Log.e("SOCKET", "Connection losted");
                ICQProfile.this.handleProfileConnectionLost();
            }

            @Override
            public void onError(int errorCode) {
                Log.e("SOCKET", "ERROR = " + errorCode);
            }
        };
    }

    public final ssi_item isInVisible(String uin) {
        synchronized (this.visible_list) {
            for (int i = 0; i < this.visible_list.size(); i++) {
                ssi_item vi = this.visible_list.get(i);
                if (vi.uin.equals(uin)) {
                    return vi;
                }
            }
            return null;
        }
    }

    public final ssi_item isInInvisible(String uin) {
        synchronized (this.invisible_list) {
            for (int i = 0; i < this.invisible_list.size(); i++) {
                ssi_item vi = this.invisible_list.get(i);
                if (vi.uin.equals(uin)) {
                    return vi;
                }
            }
            return null;
        }
    }

    public final ssi_item isInIgnore(String uin) {
        synchronized (this.ignore_list) {
            for (int i = 0; i < this.ignore_list.size(); i++) {
                ssi_item vi = this.ignore_list.get(i);
                if (vi.uin.equals(uin)) {
                    return vi;
                }
            }
            return null;
        }
    }

    public final ssi_item isInPhantom(String uin) {
        synchronized (this.phantom_list) {
            int sz = this.phantom_list.size();
            for (int i = 0; i < sz; i++) {
                ssi_item p = this.phantom_list.get(i);
                if (p.uin.equals(uin)) {
                    return p;
                }
            }
            return null;
        }
    }

    public final ssi_item deletePhantom(String uin) {
        synchronized (this.phantom_list) {
            int sz = this.phantom_list.size();
            for (int i = 0; i < sz; i++) {
                ssi_item p = this.phantom_list.get(i);
                if (p.uin.equals(uin)) {
                    this.phantom_list.remove(i);
                    return p;
                }
            }
            return null;
        }
    }

    public final void addPhantom(String uin, int id, int type) {
        synchronized (this.phantom_list) {
            if (isInPhantom(uin) == null) {
                ssi_item p = new ssi_item();
                p.uin = uin;
                p.id = id;
                p.listType = type;
                this.phantom_list.add(p);
            }
        }
    }

    public final void saveVisibility() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
        sp.edit().putInt("vis" + this.ID + "prf", this.visibilityStatus).commit();
    }

    public final void getSavedVisibility() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
        this.visibilityStatus = sp.getInt("vis" + this.ID + "prf", 1);
    }

    public final void saveXStatus() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
        String x = String.valueOf(this.xsts);
        sp.edit().putInt("xsts" + this.ID + "prf", this.xsts).putString("x" + x + this.ID + "prftitle", this.xtitle).putString("x" + x + this.ID + "prfdesc", this.xdesc).commit();
    }

    public final String getSavedXTitle(int xs) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
        String x = String.valueOf(xs);
        return sp.getString("x" + x + this.ID + "prftitle", "");
    }

    public final String getSavedXDesc(int xs) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
        String x = String.valueOf(xs);
        return sp.getString("x" + x + this.ID + "prfdesc", "");
    }

    public final void makeToast(String text) {
        this.svc.showToast(text, 1);
    }

    /**
     * @noinspection unused
     */
    public final void makeShortToast(String text) {
        this.svc.showToast(text, 0);
    }

    public final void proceedFlapPacket(ByteBuffer buffer) {
        FLAP flp = new FLAP(buffer);
        Log.v("ICQProfile", "FLAP channel=" + flp.getChannel());
        switch (flp.getChannel()) {
            case 1:
                if (this.authFirstStageCompleted) {
                    handleBOSServerHello();
                    setConnectionStatus(35);
                } else {
                    handleServerAuthHello();
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

    private void handleServerAuthHello() {
        jasminSvc.pla.put(this.nickname, resources.getString("s_icq_authentification"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_authentification"));
        setConnectionStatus(19);

        try {
            if (this.useMD5Login) {
                Log.v("ICQProfile", "handleServerAuthHello: sending MD5 login");
                this.BUFFER = ICQProtocol.createHelloReply(this.sequence);
                send();
                this.BUFFER = ICQProtocol.createLoginAuthorizationRequest(this.sequence, this.ID);
                send();
            } else {
                Log.v("ICQProfile", "handleServerAuthHello: sending XOR login");
                this.BUFFER = ICQProtocol.createXORLogin(this.sequence, this.ID, this.password);
                send();
            }
        } catch (Exception e) {
            makeToast("error at createXORLogin()");
            disconnect();
        }
    }

    private void handleServerXORReply(TLV server, TLV cookie) {
        setConnectionStatus(25);
        // читаем адрес BOS-сервера
        String bos = server.getData().readStringAscii(server.length);
        // сохраняем его в поле, чтобы потом на него коннектиться
        this.bos_server = bos;
        Log.v("ICQProfile", "handleServerXORReply: bos=" + bos + ", cookie length=" + cookie.length);

        // логируем и показываем в списке контактов
        String msg = utilities.match(resources.getString("s_icq_connecting_to_BOS"), new String[]{bos});
        jasminSvc.pla.put(this.nickname, msg, null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ": " + msg);
        this.svc.showMessageInContactList(resources.getString("s_information"), msg);

        this.cookies = cookie.getData().readBytes(cookie.length);
        ByteBuffer buffer = ICQProtocol.createGoodbye(this.sequence);
        send(buffer);
        this.authFirstStageCompleted = true;
        this.jumpingToBOS = true;

        this.socket.disconnect();
        // теперь bos_server точно не null
        this.socket.connect(this.bos_server);
    }

    private void handleBOSServerHello() {
        jasminSvc.pla.put(this.nickname, resources.getString("s_icq_sending_cookies"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_sending_cookies"));
        Log.v("ICQProfile", "handleBOSServerHello: sending cookies length=" + this.cookies.length);
        ByteBuffer buffer = ICQProtocol.createSendCookies(this.cookies, this.ID, this.sequence);
        send(buffer);
    }

    private void handleDisconnectFlapData(ByteBuffer data) {
        TLVList list = new TLVList(data, data.getBytesCountAvailableToRead(), true);

        // 1) Сначала — если это нормальный ответ на XOR-логин (server+cookie)
        TLV server = list.getTLV(5);
        TLV cookie = list.getTLV(6);
        Log.v("ICQProfile", "handleDisconnectFlapData: serverTLV=" + (server != null) + ", cookieTLV=" + (cookie != null));
        if (server != null && cookie != null) {
            handleServerXORReply(server, cookie);
            list.recycle();
            return;
        }

        // 2) Только теперь — если пришёл TLV 1, то это именно "вход по e-mail"
        TLV uin = list.getTLV(1);
        if (uin != null) {
            ByteBuffer uin_data = uin.getData();
            this.ID = uin_data.readStringAscii(uin.length);
            this.svc.showMessageInContactList(
                    resources.getString("s_information"),
                    utilities.match(resources.getString("s_email_replaced"), new String[]{this.ID})
            );
            this.svc.profiles.writeProfilesToFile();
        }

        // 3) Дальше — ошибки и всё остальное
        TLV error = list.getTLV(8);
        if (error != null) {
            int code = error.getData().readWord();
            Log.e("ICQProfile", "Disconnect FLAP error code=" + code);
            proceedLoginError(code);
            list.recycle();
            return;
        }

        TLV tlv9 = list.getTLV(9);
        if (tlv9 != null) {
            ByteBuffer buf = tlv9.getData();
            if (buf.readWord() == 1) {
                this.svc.showMessageInContactList(
                        this.ID,
                        resources.getString("s_icq_used_on_another_device")
                );
            }
        }

        // если это не авторизация — просто скажем «пока» и отключимся
        ByteBuffer goodbye = ICQProtocol.createGoodbye(this.sequence);
        send(goodbye);
        disconnect();
        list.recycle();
    }

    private void handleSnacData(ByteBuffer data) {
        this.ping_answer_received = true;
        if (this.pinger != null) {
            this.pinger.resetTimer();
        }
        SNAC snc = new SNAC(data);
        Log.v("ICQProfile", "SNAC type=" + snc.getType() + " subtype=" + snc.getSubtype());
        switch (snc.getType()) {
            case 1:
                switch (snc.getSubtype()) {
                    case 3:
                        handleServerFamiliesList(snc.getData());
                        break;
                    case 5:
                        handleServerServiceRedirect(snc.getData(), snc.getFlags());
                        break;
                    case 7:
                        handleServerRates(snc.getData());
                        break;
                    case 15:
                        handleServerOnlineInfo(snc.getData(), snc.getFlags());
                        break;
                    case 19:
                        handleServerMOTD();
                        break;
                    case 21:
                        handleServerLinks();
                        break;
                    case 24:
                        handleServerFamiliesVersions();
                        break;
                    case 33:
                        handleServerExtStatus(snc.getData(), snc.getFlags());
                        break;
                }
            case 3:
                switch (snc.getSubtype()) {
                    case 11:
                        handleServerUserOnline(snc.getData());
                        break;
                    case 12:
                        handleServerUserOffline(snc.getData());
                        break;
                }
            case 4:
                switch (snc.getSubtype()) {
                    case 1:
                        handleServerMessageError(snc.getData());
                        break;
                    case 7:
                        handleServerMessageReceived(snc.getData(), snc.getFlags(), snc.getId());
                        break;
                    case 11:
                        handleServerMessageAck(snc.getData(), snc.getFlags());
                        break;
                    case 20:
                        handleTypingNotification(snc.getData());
                        break;
                }
            case 19:
                switch (snc.getSubtype()) {
                    case 6:
                        handleServerRoster(snc.getData(), snc.getFlags());
                        break;
                    case 8:
                        handleServerAddBuddy(snc.getData(), snc.getFlags());
                    case 9:
                        try {
                            handleServerUpdateGroup(snc.getData(), snc.getFlags());
                            break;
                        } catch (Exception e) {
                            break;
                        }
                    case 10:
                        handleServerDeleteBuddy(snc.getData(), snc.getFlags());
                        break;
                    case 14:
                        handleServerSSIResult(snc.getData(), snc.getFlags(), snc.getId());
                        break;
                    case 25:
                        handleAuthorizationRequest(snc.getData(), snc.getFlags());
                        break;
                    case 27:
                        handleAuthorizationReply(snc.getData(), snc.getFlags());
                        break;
                    case 28:
                        handleYouWereAdded(snc.getData(), snc.getFlags());
                        break;
                }
                break;
            case 21:
                if (snc.getSubtype() == 3) {
                    if (snc.getId() == 1023) {
                        handleInfoUpdateResult(snc.getData());
                    } else if (snc.getId() == 64017) {
                        handleServerOfflineMessage(snc.getData());
                    } else if (snc.getId() == 10) {
                        handleSearchResult(snc.getData(), snc.getFlags());
                    } else {
                        handleServerContactInfo(snc.getData(), snc.getFlags(), snc.getId());
                    }
                }
            case 23:
                switch (snc.getSubtype()) {
                    case 3:
                        handleServerLoginReply(snc.getData(), snc.getFlags());
                        break;
                    case 7:
                        handleServerAuthKeyResponse(snc.getData());
                        break;
                }
        }
    }

    private void handleServerRates(ByteBuffer buffer) {
        setConnectionStatus(99);
        jasminSvc.pla.put(this.nickname, resources.getString("s_icq_session_setup"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_session_setup"));
        if (this.rcn.is_active) {
            this.rcn.stop();
        }
        int groups = buffer.readWord();
        this.BUFFER = ICQProtocol.createAckRates(this.sequence, groups);
        send();
        this.BUFFER = ICQProtocol.createReqInfo(this.sequence);
        send();
        this.BUFFER = ICQProtocol.createReqLists(this.sequence);
        send();
        this.BUFFER = ICQProtocol.createReqLocation(this.sequence);
        send();
        this.BUFFER = ICQProtocol.createReqBuddy(this.sequence);
        send();
        this.BUFFER = ICQProtocol.createReqICBM(this.sequence);
        send();
        this.BUFFER = ICQProtocol.createReqBOS(this.sequence);
        send();
        File roster = new File(resources.dataPath + this.ID + "/roster.bin");
        //noinspection ResultOfMethodCallIgnored
        roster.delete();
        synchronized (this.phantom_list) {
            this.phantom_list.clear();
        }
        synchronized (this.visible_list) {
            this.visible_list.clear();
        }
        synchronized (this.invisible_list) {
            this.invisible_list.clear();
        }
        synchronized (this.ignore_list) {
            this.ignore_list.clear();
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            roster.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.contactlist.clear();
        ICQGroup not_in_list = new ICQGroup();
        not_in_list.id = -1;
        not_in_list.opened = true;
        not_in_list.name = resources.getString("s_icq_temp_group");
        not_in_list.itemType = 2;
        not_in_list.profile = this;
        this.contactlist.put(not_in_list);
        jasminSvc.pla.put(this.nickname, resources.getString("s_icq_roster_request"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_roster_request"));
        this.BUFFER = ICQProtocol.createRequestRoster(this.sequence);
        send();
    }

    private void handleServerLinks() {
        this.BUFFER = ICQProtocol.createClientFamilies(this.sequence);
        send();
        setConnectionStatus(60);
    }

    private void handleServerFamiliesList(ByteBuffer data) {
        // Server has sent the list of supported service families. According to
        // the OSCAR specification we must respond with SNAC(01,17) containing
        // the versions we support. Just send the request without parsing
        // the family list, as older code never handled this packet at all.
        this.BUFFER = ICQProtocol.createClientFamilies(this.sequence);
        send();
    }

    private void handleServerMOTD() {
    }

    private void handleServerFamiliesVersions() {
        this.BUFFER = ICQProtocol.createRatesRequest(this.sequence);
        send();
        setConnectionStatus(75);
    }

    private void handleServerRoster(ByteBuffer buffer, int flags) {
        IncomingRosterParser parser = new IncomingRosterParser();
        if ((flags & 32768) == 32768) {
            int len = buffer.readWord();
            buffer.skip(len);
        }
        synchronized (ContactsAdapter.locker) {
            parser.parse(buffer, this);
        }
        if (flags == 0) {
            setConnectionStatus(100);
            this.BUFFER = ICQProtocol.createSetUserInfo(this.sequence, this.xsts, this.qip_status);
            send();
            this.BUFFER = ICQProtocol.createSetICBM(this.sequence);
            send();
            this.BUFFER = ICQProtocol.createClientReady(this.sequence);
            send();
            if (this.qip_status != null) {
                this.status = 0;
            }
            this.BUFFER = ICQProtocol.createSetDCInfo(this.sequence, this.status, 256, 11);
            send();
            int id = (((int) System.currentTimeMillis()) << 32) & 16777215;
            this.BUFFER = ICQProtocol.createContactInfoRequest(this.sequence, this.ID, String.valueOf(Integer.parseInt(this.ID)), id);
            putInfoOperation(this.ID, 2, id);
            send();
            setVisibilityS(this.visibilityStatus);
            jasminSvc.pla.put(this.nickname, resources.getString("s_icq_starting_session"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_starting_session"));
            this.BUFFER = ICQProtocol.createRosterAck(this.sequence);
            send();
            jasminSvc.pla.put(this.nickname, resources.getString("s_icq_offline_msgs_req"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_offline_msgs_req"));
            this.BUFFER = ICQProtocol.createOfflineMsgsRequest(this.sequence, this.ID);
            send();
            this.contactlist.saveToLocalStorage();
            this.contactlist.sort();
            handleProfileConnected();
        }
    }

    @SuppressLint("LongLogTag")
    private final void handleServerLoginReply(ByteBuffer buffer, int flags) {
        setConnectionStatus(25);
        if (flags == 32768) {
            int len = buffer.readWord();
            buffer.skip(len);
        }
        TLVList list = new TLVList(buffer, buffer.getBytesCountAvailableToRead(), true);
        TLV server = list.getTLV(5);
        TLV cookie = list.getTLV(6);
        Log.v("ICQProfile", "handleServerLoginReply: serverTLV=" + (server != null) + ", cookieTLV=" + (cookie != null));
        if (server == null || cookie == null) {
            TLV error_url = list.getTLV(4);
            if (error_url != null) {
                String url = error_url.getData().readStringAscii(error_url.length);
                Log.e("JasmineIM:icqlogin:error:url", url);
            }
            TLV error = list.getTLV(8);
            int code = error == null ? -1 : error.getData().readWord();
            Log.e("ICQProfile", "Login reply error code=" + code);
            proceedLoginError(code);
            list.recycle();
            return;
        }
        this.bos_server = server.getData().readStringAscii(server.length);
        this.cookies = cookie.getData().readBytes(cookie.length);
        this.BUFFER = ICQProtocol.createGoodbye(this.sequence);
        send();
        this.authFirstStageCompleted = true;
        this.jumpingToBOS = true;
        this.socket.disconnect();
        this.socket.connect(this.bos_server);
        list.recycle();
    }

    public final void proceedLoginError(int error) {
        Log.e("ICQProfile", "proceedLoginError code=" + error +
                " authFirstStageCompleted=" + this.authFirstStageCompleted +
                " jumpingToBOS=" + this.jumpingToBOS +
                " connectedToBOS=" + this.connectedToBOS +
                " connectionStatus=" + this.connection_status);
        if (error == -1) {
            makeToast("Authorization error");
        } else {
            switch (error) {
                case 1:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_1"));
                    break;
                case 2:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_2"));
                    break;
                case 4:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_3"));
                    break;
                case 5:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_4"));
                    break;
                case 7:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_5"));
                    break;
                case 22:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_6"));
                    break;
                case 23:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_7"));
                    break;
                case 24:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_8"));
                    break;
                case 29:
                    this.svc.showMessageInContactList(this.ID, resources.getString("s_icq_login_error_9"));
                    break;
                default:
                    this.svc.showMessageInContactList(this.ID, utilities.match(resources.getString("s_icq_login_error_10"), new String[]{String.valueOf(error)}));
                    break;
            }
            jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_icq_login_error_log"), new String[]{String.valueOf(error)}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ": " + utilities.match(resources.getString("s_icq_login_error_log"), new String[]{String.valueOf(error)}));
        }
        disconnect();
    }

    private final void handleServerAuthKeyResponse(ByteBuffer buffer) {
        int len = buffer.readWord();
        byte[] key = buffer.readBytes(len);
        proceedMD5Login(key);
    }

    private void proceedMD5Login(byte[] key) {
        try {
            this.BUFFER = ICQProtocol.createMD5Login(key, this.sequence, this.ID, ICQProtocol.preparePassword(this.password));
            send();
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }

    private void handleAuthorizationRequest(ByteBuffer data, int flags) {
        String reason;
        this.svc.playEvent(3);
        if (flags == 32768) {
            data.skip(data.readWord());
        }
        String sUIN = data.readStringAscii(data.readByte());
        int len = data.readWord();
        int backup = data.readPos;
        try {
            reason = data.readStringUTF8(len);
        } catch (IOException e) {
            data.readPos = backup;
            reason = data.readString1251(len);
            e.printStackTrace();
        }
        ICQContact contact = this.contactlist.getContactByUIN(sUIN);
        if (contact != null) {
            if (!ICQChatActivity.VISIBLE || ICQChatActivity.contact != contact) {
                contact.setHasUnreadMessages();
                this.svc.updateNotify();
            }
            HistoryItem hst = new HistoryItem();
            hst.authType = 2;
            hst.isAuthMessage = true;
            hst.contact = contact;
            hst.message = reason;
            contact.hasUnreadedAuthRequest = true;
            contact.loadLastHistory();
            contact.history.add(hst);
            this.svc.handleIncomingMessage(hst);
            if (!contact.isChating) {
                putIntoOpenedChats(contact);
            }
            remakeContactList();
            return;
        }
        jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_auth_req_from_unknown"), new String[]{sUIN, reason}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_auth_req_from_unknown"), new String[]{sUIN, reason}));
    }

    private final void handleAuthorizationReply(ByteBuffer data, int flags) {
        if (flags == 32768) {
            int len = data.readWord();
            data.skip(len);
        }
        int len2 = data.readByte();
        String sUIN = data.readStringAscii(len2);
        int reply = data.readByte();
        ICQContact contact = this.contactlist.getContactByUIN(sUIN);
        if (contact != null) {
            if (!ICQChatActivity.VISIBLE || ICQChatActivity.contact != contact) {
                contact.setHasUnreadMessages();
                this.svc.updateNotify();
            }
            HistoryItem hst = new HistoryItem();
            hst.authType = reply;
            if (reply == 1) {
                contact.authorized = true;
                this.contactlist.saveToLocalStorage();
                this.svc.playEvent(1);
            } else {
                this.svc.playEvent(2);
            }
            hst.isAuthMessage = true;
            hst.contact = contact;
            contact.loadLastHistory();
            contact.history.add(hst);
            this.svc.handleIncomingMessage(hst);
            if (!contact.isChating) {
                putIntoOpenedChats(contact);
            }
            remakeContactList();
        }
    }

    private final void handleTypingNotification(ByteBuffer data) {
        data.skip(8);
        data.skip(2);
        int len = data.readByte();
        String sUIN = data.readStringAscii(len);
        int msg = data.readWord();
        ICQContact contact = this.contactlist.getContactByUIN(sUIN);
        if (contact != null) {
            if (msg == 2) {
                contact.typing = true;
            } else if (msg == 0) {
                contact.typing = false;
            }
            refreshContactList();
        }
        if (ICQChatActivity.VISIBLE && ICQChatActivity.contact == contact) {
            this.svc.handleChatUpdateInfo();
        }
    }

    private void handleServerExtStatus(ByteBuffer data, int flags) {
        if (flags == 32768) {
            int len = data.readWord();
            data.skip(len);
        }
        data.readStringAscii(data.getBytesCountAvailableToRead());
    }

    private void handleServerOnlineInfo(ByteBuffer data, int flags) {
        String away;
        if (flags == 32768) {
            int len = data.readWord();
            data.skip(len);
        }
        int len2 = data.readByte();
        String sUIN = data.readStringAscii(len2);
        if (sUIN.equals(this.ID)) {
            data.skip(2);
            int len3 = data.readWord();
            TLVList list = new TLVList(data, len3);
            TLV tlv = list.getTLV(29);
            if (tlv != null) {
                TLVList sublist = new TLVList(tlv.getData(), tlv.length, true, true);
                TLV tlv2 = sublist.getTLV(2);
                if (tlv2 != null) {
                    ByteBuffer buf = tlv2.getData();
                    int length = buf.readWord();
                    int backup = buf.readPos;
                    try {
                        away = buf.readStringUTF8(length);
                    } catch (IOException e) {
                        buf.readPos = backup;
                        away = buf.readStringAscii(length);
                    }
                    this.away_text = away;
                }
                sublist.recycle();
            }
            list.recycle();
        }
    }

    private void handleServerServiceRedirect(ByteBuffer data, int flags) {
        if (flags == 32768) {
            int len = data.readWord();
            data.skip(len);
        }
        TLVList list = new TLVList(data, data.getBytesCountAvailableToRead(), true);
        TLV server = list.getTLV(5);
        TLV cookie = list.getTLV(6);
        Log.v("ICQProfile", "handleServerServiceRedirect: serverTLV=" + (server != null) + ", cookieTLV=" + (cookie != null));
        if (server == null || cookie == null) {
            TLV error = list.getTLV(8);
            int code = error == null ? -1 : error.getData().readWord();
            Log.e("ICQProfile", "Service redirect error code=" + code);
            proceedLoginError(code);
        }
        if (this.icon_proto == null) {
            this.icon_proto = new AvatarProtocol(this, String.valueOf(server.getData().readStringAscii(server.length)) + ":5190", cookie.getData().readBytes(cookie.length));
        } else {
            this.icon_proto.restart(this, String.valueOf(server.getData().readStringAscii(server.length)) + ":5190", cookie.getData().readBytes(cookie.length));
        }
        list.recycle();
    }

    private void handleServerUserOnline(ByteBuffer buffer) {
        String away_status;
        int sts;
        String sUIN = buffer.readStringAscii(buffer.readByte());
        ICQContact contact = this.contactlist.getContactByUIN(sUIN);
        if (contact != null) {
            buffer.skip(2);
            buffer.skip(2);
            TLVList list = new TLVList(buffer, buffer.getBytesCountAvailableToRead(), true);
            TLV tlv = list.getTLV(6);
            contact.away_status = null;
            boolean become_online = false;
            if (contact.status == -1) {
                if (contact.presence_initialized) {
                    contact.requestBlink();
                    if (PreferenceTable.log_online) {
                        this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_contact_online"), new String[]{contact.name}));
                    }
                    this.svc.playEvent(4);
                }
                become_online = true;
            }
            if (tlv != null) {
                ByteBuffer tlvData = tlv.getData();
                tlvData.skip(2);
                contact.status = tlvData.readWord();
            } else {
                contact.status = 0;
            }
            TLV tlv2 = list.getTLV(12);
            contact.dc_info.reset();
            contact.protoVersion = 0;
            if (tlv2 != null) {
                ByteBuffer tlvData2 = tlv2.getData();
                contact.dc_info.ip = tlvData2.readBytes(4);
                contact.dc_info.port = tlvData2.readDWord();
                contact.dc_info.dc_type = tlvData2.readByte();
                contact.protoVersion = tlvData2.readWord();
                tlvData2.skip(12);
                contact.dc_info.dc1 = tlvData2.readDWord();
                contact.dc_info.dc2 = tlvData2.readDWord();
                contact.dc_info.dc3 = tlvData2.readDWord();
            }
            TLV tlv3 = list.getTLV(3);
            if (tlv3 != null) {
                long signOnTime = tlv3.getData().readDWord() << 32;
                contact.signOnTime = 1000 * signOnTime;
            }
            TLV tlv4 = list.getTLV(13);
            if (tlv4 != null) {
                ByteBuffer tlvData3 = tlv4.getData();
                int count = tlv4.length / 16;
                boolean qip_sts_found = false;
                boolean xfound = false;
                Capabilities caps = contact.capabilities;
                caps.clear();
                for (int i = 0; i < count; i++) {
                    byte[] cap = tlvData3.readBytes(16);
                    String capHex = utilities.convertToHex(cap).toUpperCase();
                    if (!qip_sts_found && (sts = qip_statuses.fromGuid(capHex)) != 0) {
                        contact.status = sts;
                        qip_sts_found = true;
                    }
                    if (!xfound) {
                        contact.xstatus = xstatus.getIcon(capHex);
                        if (contact.xstatus != null) {
                            xfound = true;
                        }
                    }
                    caps.add(capHex);
                }
                if (!xfound) {
                    contact.xtraz_text = null;
                }
            }
            TLV tlv5 = list.getTLV(25);
            if (tlv5 != null) {
                Capabilities caps2 = contact.capabilities;
                ByteBuffer tlv_data = tlv5.getData();
                for (int i2 = 0; i2 < tlv5.length; i2 += 2) {
                    int short_cap = tlv_data.readWord();
                    String full_cap = IcqCapsBase.checkoutShortCapability(short_cap);
                    if (full_cap != null) {
                        caps2.add(full_cap);
                    }
                }
            }
            TLV tlv6 = list.getTLV(29);
            if (tlv6 != null) {
                try {
                    TLVList lst = new TLVList(tlv6.getData(), 10, true, 0);
                    TLV tlv7 = lst.getTLV(2);
                    if (tlv7 != null) {
                        ByteBuffer tlvData4 = tlv7.getData();
                        int len = tlvData4.readWord();
                        int backup = tlvData4.readPos;
                        try {
                            away_status = tlvData4.readStringUTF8(len);
                        } catch (Exception e) {
                            tlvData4.readPos = backup;
                            away_status = tlvData4.readString1251(len);
                        }
                        if (away_status.length() > 0) {
                            contact.away_status = away_status;
                            if (utilities.isEmptyForDisplay(contact.away_status)) {
                                contact.away_status = null;
                            }
                        }
                        lst.recycle();
                    }
                } catch (Exception ignored) {
                }
            }
            if (contact.presence_initialized && PreferenceTable.log_online && become_online) {
                jasminSvc.pla.put(utilities.match(resources.getString("s_icq_contact_online"), new String[]{contact.name}), "", resources.getICQStatusIconFull(contact), null, popup_log_adapter.PRESENSE_DISPLAY_TIME, null);
            }
            if (contact.presence_initialized && PreferenceTable.auto_xtraz && contact.xstatus != null && isInInvisible(contact.ID) == null && isInIgnore(contact.ID) == null && this.visibilityStatus != 2) {
                sendXtrazRequest(contact.ID, 0);
            }
            if (!contact.presence_initialized) {
                contact.presence_initialized = true;
            }
            list.recycle();
            IcqClientDetector.instance.execVM(contact);
            if (become_online) {
                remakeContactList();
            } else {
                refreshContactList();
            }
            if (ICQChatActivity.VISIBLE && ICQChatActivity.contact == contact) {
                this.svc.handleChatUpdateInfo();
            }
        }
    }

    private void handleServerUserOffline(ByteBuffer buffer) {
        int len = buffer.readByte();
        String sUIN = buffer.readStringAscii(len);
        ICQContact contact = this.contactlist.getContactByUIN(sUIN);
        if (contact != null) {
            contact.resetBlink();
            contact.presence_initialized = true;
            if (contact.status != -1) {
                if (PreferenceTable.auto_close_chat && contact.isChating && !contact.hasUnreadMessages) {
                    closeChat(contact);
                }
                contact.status = -1;
                contact.xstatus = null;
                contact.away_status = null;
                contact.xtraz_text = null;
                contact.typing = false;
                if (PreferenceTable.log_offline) {
                    jasminSvc.pla.put(utilities.match(resources.getString("s_icq_contact_offline"), new String[]{contact.name}), "", resources.offline, null, popup_log_adapter.PRESENSE_DISPLAY_TIME, null);
                    this.svc.put_log(this.nickname + ": " + utilities.match(resources.getString("s_icq_contact_offline"), new String[]{contact.name}));
                }
                this.svc.playEvent(5);
                remakeContactList();
            }
            if (ICQChatActivity.VISIBLE && ICQChatActivity.contact == contact) {
                this.svc.handleChatUpdateInfo();
            }
        }
    }

    private final void handleServerMessageError(ByteBuffer buffer) {
        int error = buffer.readWord();
        switch (error) {
            case 4:
                Log.v("Jasmine:Message Error!", "you are trying to send message to offline client");
                return;
            case 9:
                Log.v("Jasmine:Message Error!", "message not supported by client");
                return;
            case 24:
                return;
            case 16:
                Log.v("Jasmine:Message Error!", "receiver/sender blocked");
                return;
            default:
                Log.v("Jasmine:Message Error!", "Unknown error");
        }
    }

    private final void handleServerDeleteBuddy(ByteBuffer data, int flags) {
        String sUIN;
        if (flags == 32768) {
            data.skip(data.readWord());
        }
        Log.e("PROFILE", "Delete buddy received!");
        int len = data.readWord();
        int backup = data.readPos;
        try {
            sUIN = data.readStringUTF8(len);
        } catch (Exception e) {
            data.readPos = backup;
            sUIN = data.readString1251(len);
        }
        int group = data.readWord();
        int id = data.readWord();
        int type = data.readWord();
        Log.e("PROFILE", "UIN: " + sUIN + " Group: " + String.valueOf(group) + " ID: " + String.valueOf(id) + " Type:" + String.valueOf(type));
        if (type == 0) {
            ICQContact contact = this.contactlist.getContactByUIN(sUIN);
            if (contact != null && !this.contactlist.getGroupById(group).isNotIntList) {
                this.svc.showMessageInContactList(Locale.getString("s_information"), Locale.getString("s_icq_contact_removed"));
                proceedLocalDeleteContact(contact);
            }
        } else if (type == 25) {
            deletePhantom(sUIN);
        }
        remakeContactList();
    }

    private void handleServerAddBuddy(ByteBuffer data, int flags) {
        String sUIN;
        if (flags == 32768) {
            data.skip(data.readWord());
        }
        int len = data.readWord();
        int backup = data.readPos;
        try {
            sUIN = data.readStringUTF8(len);
        } catch (Exception e) {
            data.readPos = backup;
            sUIN = data.readString1251(len);
            e.printStackTrace();
        }
        int group = data.readWord();
        int id = data.readWord();
        int type = data.readWord();
        TLVList list = new TLVList(data, data.readWord(), true);
        if (type == 0) {
            TLV tlv = list.getTLV(102);
            ICQContact contact = this.contactlist.getContactByUIN(sUIN);
            ICQGroup grp = this.contactlist.getGroupById(group);
            if (contact == null) {
                ICQContact contact2 = new ICQContact();
                contact2.ID = sUIN;
                contact2.name = sUIN;
                contact2.profile = this;
                contact2.group = group;
                contact2.id = id;
                if (grp.isNotIntList) {
                    contact2.added = false;
                    contact2.as_accepted = false;
                }
                contact2.authorized = tlv == null;
                contact2.init();
                this.contactlist.put(contact2);
                this.contactlist.sort();
            } else {
                contact.profile = this;
                contact.group = group;
                contact.id = id;
                if (grp.isNotIntList) {
                    contact.added = false;
                }
                contact.authorized = tlv == null;
            }
        } else if (type == 1) {
            ICQGroup grp2 = this.contactlist.getGroupById(group);
            if (group != 0 && grp2 == null) {
                ICQGroup grp3 = new ICQGroup();
                grp3.id = group;
                grp3.name = sUIN;
                grp3.profile = this;
                this.contactlist.put(grp3);
                this.contactlist.sort();
                if (list.getTLV(106) != null) {
                    grp3.isNotIntList = true;
                }
            }
        } else if (type == 25) {
            addPhantom(sUIN, id, type);
        }
        list.recycle();
        remakeContactList();
    }

    private void handleServerUpdateGroup(ByteBuffer data, int flags) throws Exception {
        String sUIN;
        if (flags == 32768) {
            data.skip(data.readWord());
        }
        int len = data.readWord();
        int backup = data.readPos;
        try {
            sUIN = data.readStringUTF8(len);
        } catch (Exception e) {
            data.readPos = backup;
            sUIN = data.readString1251(len);
            e.printStackTrace();
        }
        Log.v("PROFILE", "Update group received!");
        int group = data.readWord();
        int id = data.readWord();
        int type = data.readWord();
        Log.v("PROFILE", "Group: " + group + " ID: " + id + " Type:" + type);
        if (type == 0) {
            Log.v("PROFILE", "Contact will be changed");
            ICQContact contact = this.contactlist.getContactByUIN(sUIN);
            if (contact != null && !this.contactlist.getGroupById(group).isNotIntList && type == 0) {
                TLVList list = new TLVList(data, data.readWord(), true);
                TLV tlv = list.getTLV(102);
                contact.id = id;
                contact.authorized = tlv == null;
                list.recycle();
            }
        } else if (type == 1) {
            Log.v("PROFILE", "Group will be changed");
            ICQGroup grp = this.contactlist.getGroupById(group);
            if (grp != null) {
                grp.name = sUIN;
            }
        }
        refreshContactList();
    }

    private void handleServerMessageAck(ByteBuffer data, int flags) {
        int len;
        if (flags == 32768) {
            int len2 = data.readWord();
            data.skip(len2);
        }
        byte[] cookie = data.readBytes(8);
        if (data.writePos >= 50) {
            data.skip(2);
            int len3 = data.readByte();
            String sUIN = data.readStringAscii(len3);
            ICQContact contact = this.contactlist.getContactByUIN(sUIN);
            ackMessage(cookie, contact);
            if (data.getBytesCountAvailableToRead() >= 47) {
                data.skip(47);
                int type = data.readByte();
                if (type == 26) {
                    data.skip(8);
                    if (data.getBytesCountAvailableToRead() >= 2) {
                        int len4 = data.readWordLE();
                        data.skip(len4 + 4);
                        if (data.getBytesCountAvailableToRead() >= 4 && data.getBytesCountAvailableToRead() >= (len = data.readDWordLE())) {
                            if (len < 0 || len > data.getBytesCountAvailableToRead()) {
                                // Avoid crashes on malformed packets
                                len = Math.max(len, 0);
                                if (len > data.getBytesCountAvailableToRead()) {
                                    len = data.getBytesCountAvailableToRead();
                                }
                            }
                            String pluginData = "";
                            if (len > 2) {
                                int backup = data.readPos;
                                try {
                                    pluginData = data.readStringUTF8(len - 2);
                                } catch (IOException e) {
                                    data.readPos = backup;
                                    pluginData = data.readString1251(len - 2);
                                    e.printStackTrace();
                                }
                            }
                            String message = xstatus.getText(pluginData);
                            String title = xstatus.getTagContent(message, "title");
                            String desc = xstatus.getTagContent(message, "desc");
                            if (contact != null) {
                                contact.xtraz_text = title + " " + desc;
                                if (!utilities.isEmptyForDisplay(title + desc)) {
                                    if (!PreferenceTable.preloadHistory || contact.historyPreLoaded) {
                                        HistoryItem hst = new HistoryItem();
                                        hst.message = title + "\n" + desc;
                                        hst.direction = 1;
                                        hst.isXtrazMessage = true;
                                        hst.xTrazIcon = contact.xstatus.mutate();
                                        hst.contact = contact;
                                        contact.history.add(hst);
                                        this.svc.rebuildChat(this, contact, hst);
                                    }
                                } else {
                                    contact.xtraz_text = null;
                                }
                                if (utilities.isEmptyForDisplay(contact.xtraz_text)) {
                                    contact.xtraz_text = null;
                                }
                                refreshContactList();
                            }
                        }
                    }
                }
            }
        }
    }

    private void ackMessage(byte[] cookie, ICQContact contact) {
        int sz = this.messagesForConfurming.size();
        for (int i = 0; i < sz; i++) {
            HistoryItem item = this.messagesForConfurming.get(i);
            if (utilities.arrayEquals(item.cookie, cookie)) {
                item.confirmed = true;
                this.messagesForConfurming.remove(i);
                this.svc.handleChatNeedRefresh(contact);
                return;
            }
        }
    }

    private void handleYouWereAdded(ByteBuffer data, int flags) {
        if (flags == 32768) {
            int extraLen = data.readWord();
            data.skip(extraLen);
        }
        int len = data.readByte();
        String sUIN = data.readStringAscii(len);
        this.svc.showMessageInContactList(this.nickname, utilities.match(resources.getString("s_icq_you_were_added"), new String[]{sUIN}));
        this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_you_were_added"), new String[]{sUIN}));
    }

    private void handleServerContactInfo(ByteBuffer data, int flags, int id) {
        if (flags == 32768) {
            int extraLen = data.readWord();
            data.skip(extraLen);
        }
        InfoOperation operation = getInfoOperation(id);
        if (operation != null) {
            ICQContact contact = this.contactlist.getContactByUIN(operation.uin);
            if (contact == null) {
                contact = this.temp_info_container;
                contact.ID = operation.uin;
            }
            contact.inf.initAvatar();
            data.skip(14);
            int type = data.readWordLE();
            if (type == 200) {
                int res = data.readByte();
                if (res == 10) {
                    String name = null;
                    String surname = null;
                    String email = null;
                    String city = null;
                    String nick = data.readString1251(data.readWordLE() - 1);
                    data.skip(1);
                    int length = data.readWordLE();
                    if (length > 1) {
                        name = data.readString1251(length - 1);
                    }
                    data.skip(1);
                    int length2 = data.readWordLE();
                    if (length2 > 1) {
                        surname = data.readString1251(length2 - 1);
                    }
                    data.skip(1);
                    int length3 = data.readWordLE();
                    if (length3 > 1) {
                        email = data.readString1251(length3 - 1);
                    }
                    data.skip(1);
                    int length4 = data.readWordLE();
                    if (length4 > 1) {
                        city = data.readString1251(length4 - 1);
                    }
                    if (nick != null) {
                        contact.inf.nickname = nick;
                        if (operation.type == 3) {
                            contact.name = nick;
                            this.contactlist.saveToLocalStorage();
                            remakeContactList();
                            makeToast(resources.getString("s_icq_nick_updated"));
                            doRenameContact(contact, nick);
                        }
                        if (operation.type == 1) {
                            contact.name = nick;
                        }
                        if (operation.type == 2) {
                            if (nick != null && nick.trim().length() > 0) {
                                this.nickname = nick;
                            }
                            this.info_container.nickname = this.nickname;
                            this.svc.profiles.writeProfilesToFile();
                        }
                    }
                    if (name != null) {
                        contact.inf.name = name;
                    } else {
                        contact.inf.name = "-";
                    }
                    if (surname != null) {
                        contact.inf.surname = surname;
                    } else {
                        contact.inf.surname = "-";
                    }
                    if (email != null) {
                        contact.inf.email = email;
                    } else {
                        contact.inf.email = "-";
                    }
                    if (city != null) {
                        contact.inf.city = city;
                    } else {
                        contact.inf.city = "-";
                    }
                    if (operation.type == 2) {
                        if (name != null) {
                            this.info_container.name = name;
                        } else {
                            this.info_container.name = "";
                        }
                        if (surname != null) {
                            this.info_container.surname = surname;
                        } else {
                            this.info_container.surname = "";
                        }
                        if (email != null) {
                            this.info_container.email = email;
                        } else {
                            this.info_container.email = "";
                        }
                        if (city != null) {
                            this.info_container.city = city;
                        } else {
                            this.info_container.city = "";
                        }
                    }
                }
            } else if (type == 220) {
                int res2 = data.readByte();
                if (res2 == 10) {
                    int age = data.readWordLE();
                    int sex = data.readByte();
                    String homepage = null;
                    int length5 = data.readWordLE();
                    if (length5 > 1) {
                        homepage = data.readString1251(length5 - 1);
                    }
                    data.skip(1);
                    int birthyear = data.readWordLE();
                    int birthmonth = data.readByte();
                    int birthday = data.readByte();
                    if (homepage != null) {
                        contact.inf.homepage = homepage;
                    } else {
                        contact.inf.homepage = "-";
                    }
                    contact.inf.birthday = birthday;
                    contact.inf.birthmonth = birthmonth;
                    contact.inf.birthyear = birthyear;
                    contact.inf.age = age;
                    if (sex == 2) {
                        contact.inf.sex = resources.getString("s_icq_gender_man");
                    } else if (sex == 1) {
                        contact.inf.sex = resources.getString("s_icq_gender_woman");
                    } else {
                        contact.inf.sex = resources.getString("s_icq_gender_none");
                    }
                    if (operation.type == 2) {
                        this.info_container.birthday = birthday;
                        this.info_container.birthmonth = birthmonth;
                        this.info_container.birthyear = birthyear;
                        if (homepage != null) {
                            this.info_container.homepage = homepage;
                        } else {
                            this.info_container.homepage = "";
                        }
                        this.info_container.sex_ = sex;
                    }
                }
            } else if (type == 230) {
                int res3 = data.readByte();
                if (res3 == 10) {
                    int len = data.readWordLE();
                    String about = null;
                    if (len > 1) {
                        about = data.readString1251(len).trim();
                    }
                    if (about != null) {
                        contact.inf.about = about;
                    } else {
                        contact.inf.about = "-";
                    }
                    if (operation.type == 2) {
                        if (about != null) {
                            this.info_container.about = about;
                        } else {
                            this.info_container.about = "";
                        }
                        removeOperation(operation.id);
                    }
                }
            }
            refreshContactList();
            if (operation.type == 0 && type == 250) {
                removeOperation(operation.id);
                ICQContact.getAvatar(contact.inf.callback, contact.ID, this.ID, this.svc);
                this.svc.displayContactInfo(contact.getInfo());
            }
            if (operation.type == 4 && type == 250) {
                removeOperation(operation.id);
                ICQContact.getAvatar(contact.inf.callback, contact.ID, this.ID, this.svc);
                this.svc.displayContactInfoInSearch(contact.getInfo());
            }
        }
    }

    private final void handleServerOfflineMessage(ByteBuffer data) {
        data.skip(10);
        int type = data.readWord();
        if (type == 16640) {
            data.skip(2);
            String sUIN = String.valueOf(data.readDWordLE());
            int year = data.readWordLE();
            int month = data.readByte();
            int day = data.readByte();
            int hour = data.readByte();
            int minute = data.readByte();
            data.skip(2);
            int msgLength = data.readWordLE();
            int i = data.readPos;
            byte[] utf = new byte[msgLength];
            System.arraycopy(data.bytes, data.readPos, utf, 0, msgLength);
            String text = StringConvertor.byteArrayToString(utf, 0, msgLength);
            Log.e("OFFLINE MESSAGES", text);
            if (!this.offlineMessages.contains(text)) {
                this.offlineMessages.add(text);
                ICQContact contact = this.contactlist.getContactByUIN(sUIN);
                ICQMessage msg = new ICQMessage();
                msg.sender = sUIN;
                msg.timestamp = utilities.createLongTime(year, month, day, hour, minute, 0);
                msg.message = text;
                handleMessage(contact, msg, false);
            }
        } else if (type == 16896) {
            this.BUFFER = ICQProtocol.createDeleteOfflineMsgsRequest(this.sequence, this.ID);
            send();
            this.BUFFER = ICQProtocol.createAnotherOfflineMsgsRequest(this.sequence);
            send();
        }
    }

    private void handleSearchResult(ByteBuffer data, int flags) {
        SearchResultItem result = new SearchResultItem();
        data.skip(16);
        int result_code = data.readByte();
        if (result_code == 10) {
            data.skip(25);
            result.found_in_database = data.readWord();
            if (result.found_in_database == 0) {
                this.svc.handleSearchResult(result);
                return;
            }
            result.pages_available = data.readWord();
            data.skip(4);
            TLVList list = new TLVList(data, 49);
            TLV uin = list.getTLV(50);
            if (uin != null) {
                try {
                    result.uin = uin.getData().readStringUTF8(uin.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            TLV status = list.getTLV(JConference.BANNED_LIST_RECEIVED);
            if (status != null) {
                result.status = status.getData().readWord();
            }
            TLV auth = list.getTLV(410);
            if (auth != null) {
                result.need_auth = auth.getData().readWord() == 0;
            }
            TLV nick = list.getTLV(120);
            if (nick != null) {
                try {
                    result.nick = nick.getData().readStringUTF8(nick.length);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            TLV name = list.getTLV(100);
            if (name != null) {
                try {
                    result.firstname = name.getData().readStringUTF8(name.length);
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            TLV lastname = list.getTLV(110);
            if (lastname != null) {
                try {
                    result.lastname = lastname.getData().readStringUTF8(lastname.length);
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            TLV gender = list.getTLV(130);
            if (gender != null) {
                result.gender = gender.getData().readByte();
            }
            TLV age = list.getTLV(340);
            if (age != null) {
                result.age = age.getData().readWord();
            }
            if (flags == 0) {
                result.isLast = true;
            }
            list.recycle();
            this.svc.handleSearchResult(result);
        }
    }

    public final void sendSearchRequest(SearchCriteries criteries) {
        this.BUFFER = ICQProtocol.createSearchRequest(this.ID, criteries, this.sequence);
        userSend();
    }

    public final FileTransfer getTransfer(byte[] cookie) {
        for (int i = 0; i < this.transfers.size(); i++) {
            FileTransfer t = this.transfers.get(i);
            if (utilities.arrayEquals(t.cookie, cookie)) {
                return t;
            }
        }
        return null;
    }

    public final void createOutgoingFile(File file, ICQContact contact) {
        FileSender s = new FileSender();
        s.contact = contact;
        s.file_name = file.getName();
        s.file_size = (int) file.length();
        s.file = file;
        s.createCookie();
        contact.transfer_cookie = s.cookie;
        this.transfers.add(s);
        if (!contact.isChating) {
            putIntoOpenedChats(contact);
        }
        s.init();
    }

    public final void sendTransferRequest(String uin, byte[] cookie, byte[] ip, int port, File file) {
        try {
            this.BUFFER = ICQProtocol.createFileTransferSendRequest(cookie, uin, ip, port, file, this.sequence);
            send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void sendTransferRedirectToInverseProxy(String uin, byte[] cookie, byte[] ip, int port) {
        this.BUFFER = ICQProtocol.createRedirectFromLocalToInverseProxy(cookie, uin, ip, port, this.sequence);
        send();
    }

    public final void sendTransferAccept(String uin, byte[] cookie) {
        this.BUFFER = ICQProtocol.createTransferAccept(cookie, uin, this.sequence);
        send();
    }

    public final void sendTransferCancel(String uin, byte[] cookie) {
        this.BUFFER = ICQProtocol.createTransferCancel(cookie, uin, this.sequence);
        send();
    }

    public final void cancelAndRemoveTransfer(byte[] cookie) {
        synchronized (this.transfers) {
            for (int i = 0; i < this.transfers.size(); i++) {
                FileTransfer t = this.transfers.get(i);
                if (utilities.arrayEquals(t.cookie, cookie)) {
                    this.svc.cancelTransferNotify(Integer.parseInt(this.ID) + Integer.parseInt(t.contact.ID));
                    t.shutDown();
                    this.transfers.remove(i);
                    this.svc.handleChatTransferNeedRebuild();
                }
            }
        }
    }

    public final void cancelTransferAndSendRejection(byte[] cookie) {
        synchronized (this.transfers) {
            for (int i = 0; i < this.transfers.size(); i++) {
                FileTransfer t = this.transfers.get(i);
                if (utilities.arrayEquals(t.cookie, cookie)) {
                    this.svc.cancelTransferNotify(Integer.parseInt(this.ID) + Integer.parseInt(t.contact.ID));
                    t.cancel();
                    this.transfers.remove(i);
                    this.svc.handleChatTransferNeedRebuild();
                }
            }
        }
    }

    private final void handleCancelFile(ICQMessage msg) {
        cancelAndRemoveTransfer(msg.cookie);
        this.svc.playEvent(8);
    }

    private final void handleFileAccept(ICQMessage msg) {
        FileTransfer t = getTransfer(msg.cookie);
        if (t != null && t.direction == 0) {
            FileSender s = (FileSender) t;
            if (!s.initialized) {
                s.init();
            }
        }
    }

    private final void handleIncomingFile(ICQMessage msg) {
        FileReceiver receiver;
        ICQContact contact = this.contactlist.getContactByUIN(msg.sender);
        if (contact != null) {
            if (msg.msg_type == 1) {
                handleCancelFile(msg);
            } else if (msg.msg_type == 2) {
                handleFileAccept(msg);
            } else {
                if (contact.transfer_cookie != null && msg.transfer_step < 2) {
                    sendTransferCancel(contact.ID, msg.cookie);
                }
                contact.loadLastHistory();
                if (!contact.isChating) {
                    putIntoOpenedChats(contact);
                }
                FileTransfer t = getTransfer(msg.cookie);
                if (t == null) {
                    receiver = new FileReceiver();
                } else if (t instanceof FileReceiver) {
                    receiver = (FileReceiver) t;
                } else {
                    FileSender sender = (FileSender) t;
                    sender.cancel();
                    return;
                }
                receiver.contact = contact;
                receiver.cookie = msg.cookie;
                receiver.file_name = msg.file_name;
                receiver.file_size = msg.file_size;
                receiver.files_count = msg.files_count;
                receiver.verified_ip = msg.verified_ip;
                receiver.proxy_ip = msg.proxy_ip;
                receiver.ip = msg.client_ip;
                receiver.port = msg.port;
                receiver.use_proxy = msg.use_proxy;
                contact.transfer_cookie = receiver.cookie;
                if (t == null) {
                    this.transfers.add(receiver);
                    if (!ICQChatActivity.VISIBLE || ICQChatActivity.contact != contact) {
                        contact.hasUnreadedFileRequest = true;
                        contact.setHasUnreadMessages();
                        remakeContactList();
                        this.svc.showTransferNotify(Integer.parseInt(this.ID) + Integer.parseInt(contact.ID), utilities.match(resources.getString("s_icq_incoming_files"), new String[]{String.valueOf(receiver.files_count), contact.name}), IMProfile.getSchema(contact));
                    }
                    this.svc.handleIncomingFile();
                }
                this.svc.handleChatTransferNeedRebuild();
            }
        }
    }

    private final void handleServerMessageReceived(ByteBuffer buffer, int flags, int id) {
        ICQMessage msg = new ICQMessage(buffer);
        if (msg.is_file) {
            handleIncomingFile(msg);
            return;
        }
        ICQContact contact = this.contactlist.getContactByUIN(msg.sender);
        switch (msg.type) {
            case 1:
                if (flags == 262431) {
                    if (buffer.writePos < 10) {
                        this.BUFFER = ICQProtocol.createDeleteOfflineMsgsRequest(this.sequence, this.ID);
                        send();
                    } else if (this.offlineMessages.contains(msg.message)) {
                        return;
                    }
                    handleMessage(contact, msg, true);
                    return;
                }
                msg.timestamp = System.currentTimeMillis();
                if ((contact != null && contact.as_accepted) || LowLevelAntispam.proceedMessage(msg.message, 255)) {
                    handleMessage(contact, msg, true);
                    return;
                }
                return;
            case 26:
                if (contact != null) {
                    if (PreferenceTable.log_xtraz_reading) {
                        if (contact.status != -1) {
                            this.svc.forcePopUp(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_contact_read_status_1"), new String[]{contact.name}));
                            this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_contact_read_status_1"), new String[]{contact.name}));
                        } else {
                            this.svc.forcePopUp(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_contact_read_status_1"), new String[]{contact.name}));
                            this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_contact_read_status_2"), new String[]{contact.name}));
                        }
                    }
                } else if (PreferenceTable.log_xtraz_reading) {
                    this.svc.forcePopUp(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_contact_read_status_1"), new String[]{msg.sender}));
                    this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_icq_contact_read_status_2"), new String[]{msg.sender}));
                }
                handleXtrazRequest(msg.sender, msg.cookie);
                return;
            default:
                return;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private final void handleMessage(ICQContact contact, ICQMessage msg, boolean sendConfirm) {
        String preview;
        if (msg.message.length() > 64) {
            preview = String.valueOf(msg.message.substring(0, 64)) + "...";
        } else {
            preview = msg.message;
        }
        if (contact == null) {
            if (PreferenceTable.as_only_roster) {
                if (PreferenceTable.as_enable_notify) {
                    this.svc.showAntispamNotify(this.ID, utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}));
                }
                jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}), resources.msg_in, null, popup_log_adapter.MESSAGE_DISPLAY_TIME, null);
                this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}));
                return;
            } else if (PreferenceTable.as_qestion_enabled) {
                String account = msg.sender;
                int res = AntispamBot.checkQuestion(account, msg.message, this);
                switch (res) {
                    case 0:
                        return;
                    case 1:
                        ICQMessageChannel1 message = new ICQMessageChannel1(this.sequence, msg.sender, this.svc.getAntispamQuestion(), false, msg.cookie);
                        userSend(message.data);
                        jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}), resources.msg_in, null, popup_log_adapter.MESSAGE_DISPLAY_TIME, null);
                        this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}));
                        return;
                    case 2:
                        ICQMessageChannel1 message2 = new ICQMessageChannel1(this.sequence, msg.sender, this.svc.getAntispamAllowed(), false, msg.cookie);
                        userSend(message2.data);
                        msg.message = resources.getString("s_contact_allowed");
                        break;
                }
            }
        } else if (!contact.as_accepted) {
            if (PreferenceTable.as_only_roster) {
                if (PreferenceTable.as_enable_notify) {
                    this.svc.showAntispamNotify(this.ID, utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}));
                }
                jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}), resources.msg_in, null, popup_log_adapter.MESSAGE_DISPLAY_TIME, null);
                this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}));
                return;
            } else if (PreferenceTable.as_qestion_enabled) {
                String account2 = msg.sender;
                int res2 = AntispamBot.checkQuestion(account2, msg.message, this);
                switch (res2) {
                    case 0:
                        return;
                    case 1:
                        ICQMessageChannel1 message3 = new ICQMessageChannel1(this.sequence, msg.sender, this.svc.getAntispamQuestion(), false, msg.cookie);
                        userSend(message3.data);
                        jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}), resources.msg_in, null, popup_log_adapter.MESSAGE_DISPLAY_TIME, null);
                        this.svc.put_log(String.valueOf(this.nickname) + ": " + utilities.match(resources.getString("s_message_locked"), new String[]{this.ID, msg.sender, preview}));
                        return;
                    case 2:
                        ICQMessageChannel1 message4 = new ICQMessageChannel1(this.sequence, msg.sender, this.svc.getAntispamAllowed(), false, msg.cookie);
                        userSend(message4.data);
                        contact.as_accepted = true;
                        msg.message = resources.getString("s_contact_allowed");
                        break;
                }
            }
        }
        if (contact == null) {
            ICQContact newContact = new ICQContact();
            newContact.ID = msg.sender;
            newContact.name = msg.sender;
            newContact.profile = this;
            newContact.group = -1;
            newContact.id = 0;
            newContact.status = 0;
            newContact.init();
            newContact.added = false;
            this.contactlist.put(newContact);
            this.contactlist.sort();
            contact = newContact;
            int id = (((int) System.currentTimeMillis()) << 32) & 16777215;
            this.BUFFER = ICQProtocol.createContactInfoRequest(this.sequence, this.ID, contact.ID, id);
            putInfoOperation(contact.ID, 1, id);
            userSend();
        }
        if (!ICQChatActivity.VISIBLE || ICQChatActivity.contact != contact) {
            contact.setHasUnreadMessages();
            final ICQContact wrap = contact;
            jasminSvc.pla.put(contact.name, SmileysManager.getSmiledText((CharSequence) preview, 0, false), resources.msg_in, contact.avatar, popup_log_adapter.MESSAGE_DISPLAY_TIME, new Runnable() { // from class: ru.ivansuper.jasmin.icq.ICQProfile.3
                @Override // java.lang.Runnable
                public void run() {
                    Intent i = new Intent(ICQProfile.this.svc, ContactListActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction("ICQITEM" + ICQProfile.this.ID + "***$$$SEPARATOR$$$***" + wrap.ID);
                    ICQProfile.this.svc.startActivity(i);
                }
            });
            if (PreferenceTable.multi_notify) {
                this.svc.showPersonalMessageNotify(contact.name + "/" + this.nickname, preview, true, utilities.getHash(contact), contact);
            } else {
                this.svc.putMessageNotify(contact, contact.name, preview);
            }
            this.svc.lastContactForNonMultiNotify = contact;
            remakeContactList();
        }
        HistoryItem hst = new HistoryItem(msg.timestamp);
        hst.message = msg.message;
        hst.direction = 1;
        hst.contact = contact;
        contact.loadLastHistory();
        contact.history.add(hst);
        contact.writeMessageToHistory(hst);
        this.svc.handleIncomingMessage(hst);
        if (!contact.isChating) {
            putIntoOpenedChats(contact);
        }
        if (this.visibilityStatus != 2 && sendConfirm) {
            try {
                this.BUFFER = ICQProtocol.createMsgAck(this.sequence, msg.cookie, msg.sender);
                send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final void handleXtrazRequest(String sender, byte[] cookie) {
        try {
            this.BUFFER = ICQProtocol.createXtrazAnswer(this.sequence, cookie, sender, this);
            userSend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final void handleServerSSIResult(ByteBuffer data, int flags, int id) {
        if (flags == 32768) {
            int len = data.readWord();
            data.skip(len);
        }
        int result = data.readWord();
        switch (id) {
            case 131073:
                if (result == 0) {
                    makeToast(resources.getString("s_icq_visibility_changed"));
                    return;
                } else {
                    makeToast(String.valueOf(resources.getString("s_icq_visibility_change_error")) + " #" + String.valueOf(result));
                    return;
                }
            case 131075:
                if (result == 0 && this.lastRename != null) {
                    proceedLocalContactRename();
                    makeToast(resources.getString("s_icq_contact_renamed"));
                } else {
                    makeToast(resources.getString("s_icq_contact_rename_error"));
                }
                this.svc.cancelProgress();
                return;
            case 131080:
                if (result == 0 && this.lastAdd != null) {
                    proceedLocalAddContact((ICQContact) this.lastAdd.object);
                    if (((ICQContact) this.lastAdd.object).authorized) {
                        makeToast(resources.getString("s_icq_contact_added"));
                    } else {
                        makeToast(resources.getString("s_icq_auth_contact_added"));
                    }
                } else {
                    switch (result) {
                        case 14:
                            ((ICQContact) this.lastAdd.object).authorized = false;
                            proceedAddNotAuthContact();
                            break;
                        default:
                            makeToast(resources.getString("s_icq_contact_add_error"));
                            this.svc.cancelProgress();
                            break;
                    }
                }
                this.svc.cancelProgress();
                return;
            case 131082:
                if (result == 0 && this.lastDelete != null) {
                    proceedLocalDeleteContact((ICQContact) this.lastDelete.object);
                    makeToast(resources.getString("s_icq_contact_deleted"));
                } else {
                    makeToast(resources.getString("s_icq_contact_delete_error"));
                }
                this.svc.cancelProgress();
                return;
            case 131089:
                if (result == 0) {
                    makeToast(resources.getString("s_icq_visibility_changed"));
                    return;
                } else {
                    makeToast(String.valueOf(resources.getString("s_icq_visibility_change_error")) + " #" + String.valueOf(result));
                    return;
                }
            case 131104:
                if (result == 0) {
                    proceedLocalGroupRename();
                    makeToast(resources.getString("s_icq_group_renamed"));
                } else {
                    makeToast(resources.getString("s_icq_group_rename_error"));
                }
                this.svc.cancelProgress();
                return;
            case 131105:
                if (result == 0) {
                    proceedLocalDeleteGroup((ICQGroup) this.lastDelete.object);
                    makeToast(resources.getString("s_icq_group_deleted_successful"));
                } else {
                    makeToast(resources.getString("s_icq_group_delete_error"));
                }
                this.svc.cancelProgress();
                return;
            case 131106:
                if (result == 0) {
                    proceedLocalAddGroup((ICQGroup) this.lastAdd.object);
                    makeToast(resources.getString("s_icq_group_add_success"));
                } else {
                    makeToast(resources.getString("s_icq_group_add_error"));
                }
                this.svc.cancelProgress();
                return;
            default:
                return;
        }
    }

    private final void handleInfoUpdateResult(ByteBuffer data) {
        this.svc.cancelProgress();
        data.skip(16);
        int result = data.readByte();
        if (result == 10) {
            makeToast(resources.getString("s_icq_info_updated"));
        } else {
            this.svc.showMessageInContactList(resources.getString("s_icq_change_info_error_header"), resources.getString("s_icq_change_info_error_text"));
        }
    }

    public final void doUpdateInfo() {
        this.BUFFER = ICQProtocol.createInfoChange(this.sequence, this.ID, this.info_container);
        userSend();
        this.svc.displayProgress(resources.getString("s_icq_change_info_in_progress"));
    }

    public final void doRequestAvatarService() {
        this.BUFFER = ICQProtocol.createAvatarServiceRequest(this.sequence);
        send();
    }

    public final void sendAuthorizationReply(String uin, int reply) {
        this.BUFFER = ICQProtocol.createAuthReply(this.sequence, uin, reply);
        userSend();
    }

    public final void sendAuthorizationRequest(String uin) {
        this.BUFFER = ICQProtocol.createFutureAuthGrand(this.sequence, uin);
        userSend();
        this.BUFFER = ICQProtocol.createAuthorizationRequest(this.sequence, uin);
        userSend();
    }

    public final void updateUserInfo() {
        this.BUFFER = ICQProtocol.createSetUserInfo(this.sequence, this.xsts, this.qip_status);
        userSend();
    }

    public final void putIntoOpenedChats(ICQContact contact) {
        this.svc.opened_chats.add(contact);
        contact.isChating = true;
        remakeContactList();
        this.svc.rebuildChatMarkers();
    }

    public final void closeChat(ICQContact contact) {
        this.svc.opened_chats.remove(contact);
        contact.isChating = false;
        contact.clearPreloadedHistory();
        remakeContactList();
        this.svc.rebuildChatMarkers();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void closeAllChats() {
        int i = 0;
        while (i < this.svc.opened_chats.size()) {
            ContactlistItem contact = this.svc.opened_chats.get(i);
            if (contact.itemType == 1 && ((ICQContact) contact).profile.equals(this)) {
                closeChat((ICQContact) contact);
                i--;
            }
            i++;
        }
    }

    public final void setVisibility(int visParam) {
        if (visParam == 2) {
            this.svc.attachTimedTask(this.ach_task_1, 21600000L);
        } else {
            this.svc.removeTimedTask(this.ach_task_1);
        }
        this.visibilityStatus = visParam;
        this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
        userSend();
        this.BUFFER = ICQProtocol.createUpdateSSIInfo(this.sequence, this.visibilityId, this.visibilityStatus, true);
        userSend();
        this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
        userSend();
        saveVisibility();
    }

    public final void setVisibilityS(int visParam) {
        this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
        send();
        this.BUFFER = ICQProtocol.createUpdateSSIInfo(this.sequence, this.visibilityId, this.visibilityStatus, true);
        send();
        this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
        send();
    }

    public final void setAwayText(String text) {
        this.away_text = text;
        this.BUFFER = ICQProtocol.createSetAwayText(this.sequence, this.away_text);
        userSend();
    }

    public final void setAwayTextA(String text) {
        this.BUFFER = ICQProtocol.createSetAwayText(this.sequence, text);
        userSend();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void setStatusText(String text) {
        setAwayText(text);
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final String getStatusText() {
        return this.away_text;
    }

    public final void setStatus(int statusParam) {
        if (statusParam != -1) {
            Manager.putInt(String.valueOf(this.ID) + "status", statusParam);
        }
        this.status = statusParam;
        this.qip_status = qip_statuses.toGuid(statusParam);
        if (this.qip_status == null) {
            this.BUFFER = ICQProtocol.createSetStatus(this.sequence, this.status, 256);
            userSend();
            setAwayText(this.away_text);
            updateUserInfo();
        } else {
            this.BUFFER = ICQProtocol.createSetStatus(this.sequence, 0, 256);
            userSend();
            setAwayText(this.away_text);
            updateUserInfo();
        }
        notifyStatusIcon();
        EventTranslator.sendProfilePresence(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setTempStatus(int statusParam) {
        this.BUFFER = ICQProtocol.createSetStatus(this.sequence, statusParam, 256);
        userSend();
        setAwayTextA(this.away_text);
        this.qip_status = null;
        updateUserInfo();
    }

    public final void sendTypingNotify(String uin, int type) {
        if (PreferenceTable.send_typing_notify && this.visibilityStatus != 2) {
            this.BUFFER = ICQProtocol.createTypingNotify(this.sequence, uin, type);
            userSend();
        }
    }

    public final void sendXtrazRequest(String receiver, int idx) {
        ICQXtrazRequestMessage request = new ICQXtrazRequestMessage(this.sequence, this.ID, receiver, idx);
        userSend(request.data);
    }

    public final void sendMessage(String receiverUIN, String text, HistoryItem hst) {
        ICQContact contact = this.contactlist.getContactByUIN(receiverUIN);
        if (contact != null) {
            ByteBuffer cookie = new ByteBuffer(8);
            long id = System.currentTimeMillis();
            try {
                cookie.writeLong(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            hst.confirmed = false;
            System.arraycopy(cookie.bytes, 0, hst.cookie, 0, 8);
            this.messagesForConfurming.add(hst);
            contact.writeMessageToHistory(hst);
            if (!contact.isChating) {
                putIntoOpenedChats(contact);
            }
            boolean relay = contact.capabilities.list.contains("094613494C7F11D18222444553540000");
            boolean internal_utf8 = contact.capabilities.list.contains("0946134E4C7F11D18222444553540000");
            if (utilities.isUIN(receiverUIN)) {
                if (contact.added & contact.authorized) {
                    if (contact.status != -1) {
                        if (relay) {
                            send2(this.sequence, receiverUIN, text, contact.currentEncoding, internal_utf8, hst.cookie);
                        } else {
                            send1(this.sequence, receiverUIN, text, true, hst.cookie);
                        }
                    } else {
                        send1(this.sequence, receiverUIN, text, true, hst.cookie);
                    }
                } else {
                    send1(this.sequence, receiverUIN, text, true, hst.cookie);
                }
            } else {
                send1(this.sequence, receiverUIN, text, true, hst.cookie);
            }
            this.svc.playEvent(7);
        }
    }

    private final void send1(int a, String b, String c, boolean d, byte[] cookie) {
        Log.i("ICQProfile:send_msg", "Sending message on channel 1");
        ICQMessageChannel1 message = new ICQMessageChannel1(a, b, c, d, cookie);
        userSend(message.data);
    }

    private final void send2(int a, String b, String c, int d, boolean e, byte[] cookie) {
        Log.i("ICQProfile:send_msg", "Sending message on channel 2");
        ICQMessageChannel2 message = new ICQMessageChannel2(a, b, c, d, e, cookie);
        userSend(message.data);
    }

    public final void sendDeleteYourself(String uin) {
        this.BUFFER = ICQProtocol.createDelYourself(this.sequence, uin);
        userSend();
    }

    public final void checkRosterRecord() {
        if (this.buddy_id == 0) {
            this.buddy_name = "1";
            this.buddy_group = 0;
            this.buddy_id = utilities.getRandomSSIId();
            this.BUFFER = ICQProtocol.createAddRosterIconRecord(this.buddy_name, this.buddy_group, this.buddy_id, this.sequence);
            userSend();
        }
    }

    public final void doChangeAvatar(File file) {
        if (this.icon_proto != null && this.icon_proto.connected) {
            this.icon_proto.uploadAvatar(file);
            return;
        }
        this.svc.showMessageInContactList(this.nickname, resources.getString("s_icq_avatar_service_notify"));
        doRequestAvatarService();
    }

    public final void updateIconHash() {
        this.BUFFER = ICQProtocol.createUpdateIconHash(this.buddy_hash, this.buddy_name, this.buddy_group, this.buddy_id, this.sequence);
        userSend();
    }

    public final void doRequestContactInfoForDisplayInSearch(String uin) {
        int id = (((int) System.currentTimeMillis()) << 32) & 16777215;
        this.BUFFER = ICQProtocol.createContactInfoRequest(this.sequence, this.ID, uin, id);
        putInfoOperation(uin, 4, id);
        userSend();
    }

    public final void doRequestContactInfoForDisplay(String uin) {
        int id = (((int) System.currentTimeMillis()) << 32) & 16777215;
        this.BUFFER = ICQProtocol.createContactInfoRequest(this.sequence, this.ID, uin, id);
        putInfoOperation(uin, 0, id);
        userSend();
    }

    public final void doRequestContactInfoForNickRefresh(String uin) {
        int id = (((int) System.currentTimeMillis()) << 32) & 16777215;
        this.BUFFER = ICQProtocol.createContactInfoRequest(this.sequence, this.ID, uin, id);
        putInfoOperation(uin, 3, id);
        userSend();
    }

    public final void doAddToLists(ssi_item item, int listType) {
        switch (listType) {
            case 2:
                synchronized (this.visible_list) {
                    this.visible_list.add(item);
                }
                break;
            case 3:
                synchronized (this.invisible_list) {
                    this.invisible_list.add(item);
                }
                break;
            case 14:
                synchronized (this.ignore_list) {
                    this.ignore_list.add(item);
                }
                break;
        }
        refreshContactList();
        this.BUFFER = ICQProtocol.createAddToLists(this.sequence, item, listType);
        userSend();
    }

    public final void doRemoveFromLists(ssi_item item, int listType) {
        switch (listType) {
            case 2:
                synchronized (this.visible_list) {
                    this.visible_list.remove(item);
                }
                break;
            case 3:
                synchronized (this.invisible_list) {
                    this.invisible_list.remove(item);
                }
                break;
            case 14:
                synchronized (this.ignore_list) {
                    this.ignore_list.remove(item);
                }
                break;
        }
        refreshContactList();
        this.BUFFER = ICQProtocol.createRemoveFromLists(this.sequence, item, listType);
        userSend();
    }

    public final void doDeleteContact(ICQContact contact) {
        if (this.connected) {
            if (contact.group != -1) {
                this.svc.displayProgress(resources.getString("s_deleting") + " '" + contact.name + "' ...");
                this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
                userSend();
                SSIOperation ssi = new SSIOperation(1, contact);
                this.lastDelete = ssi;
                this.BUFFER = ICQProtocol.createContactDelete(this.sequence, contact);
                userSend();
                this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
                userSend();
                return;
            }
            Log.e("ICQProfile", "Group is -1, deleting in local mode");
            proceedLocalDeleteContact(contact);
            return;
        }
        Toast.makeText(this.svc, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_SHORT).show();
    }

    public final void doDeleteGroup(ICQGroup group) {
        if (this.connected) {
            if (!group.isNotIntList) {
                if (this.contactlist.getContactsByGroupId(group.id).size() > 0) {
                    Toast.makeText(this.svc, resources.getString("s_deleting_not_empty_group_error"), Toast.LENGTH_SHORT).show();
                    return;
                }
                this.svc.displayProgress(resources.getString("s_deleting") + " ...");
                this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
                userSend();
                SSIOperation ssi = new SSIOperation(1, group);
                this.lastDelete = ssi;
                this.BUFFER = ICQProtocol.createGroupDelete(this.sequence, group, this);
                userSend();
                this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
                userSend();
                return;
            }
            Toast.makeText(this.svc, resources.getString("s_you_cant_delete_this_group"), Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this.svc, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_SHORT).show();
    }

    public final void doAddContact(ICQContact contact, int flags) throws Exception {
        ICQContact cnt = this.contactlist.getContactByUIN(contact.ID);
        if (cnt != null && cnt.added) {
            makeToast(resources.getString("s_contact_already_exist"));
            return;
        }
        this.svc.displayProgress(String.valueOf(resources.getString("s_adding")) + " '" + contact.name + "' ...");
        this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
        userSend();
        SSIOperation ssi = new SSIOperation(0, contact);
        this.lastAdd = ssi;
        this.BUFFER = ICQProtocol.createAddContact(this.sequence, contact);
        userSend();
        this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
        userSend();
    }

    public final void doAddGroup(ICQGroup group) {
        ICQGroup grp = this.contactlist.getGroupByName(group.name);
        if (grp != null) {
            makeToast(resources.getString("s_group_already_exist"));
            return;
        }
        this.svc.displayProgress(resources.getString("s_adding") + " ...");
        this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
        userSend();
        SSIOperation ssi = new SSIOperation(0, group);
        this.lastAdd = ssi;
        this.BUFFER = ICQProtocol.createAddGroup(this.sequence, group);
        userSend();
        this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
        userSend();
    }

    public final void doRenameContact(ICQContact contact, String newNick) {
        this.svc.displayProgress(resources.getString("s_renaming") + " '" + contact.name + "' ...");
        this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
        userSend();
        SSIOperation ssi = new SSIOperation(4, contact);
        ssi.objectA = newNick;
        this.lastRename = ssi;
        this.BUFFER = ICQProtocol.createContactRename(this.sequence, contact.ID, newNick, contact.group, contact.id, !contact.authorized);
        userSend();
        this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
        userSend();
    }

    public final void doRenameGroup(ICQGroup group, String new_name) {
        this.svc.displayProgress(resources.getString("s_renaming") + " ...");
        this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
        userSend();
        SSIOperation ssi = new SSIOperation(4, group);
        ssi.objectA = new_name;
        this.lastRename = ssi;
        this.BUFFER = ICQProtocol.createGroupRename(this.sequence, group, this, new_name);
        userSend();
        this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
        userSend();
    }

    @SuppressLint("LongLogTag")
    private void proceedLocalDeleteContact(ICQContact contact) {
        Log.e("ICQProfile:proceedLocalDeleteContact", "Removing " + contact.ID);
        if (ICQChatActivity.VISIBLE && ICQChatActivity.contact.equals(contact)) {
            this.svc.closeChatIfShown();
        }
        this.svc.removeFromOpenedChats(contact.ID);
        this.contactlist.removeContact(contact.ID);
        this.svc.removeMessageNotify(contact);
        remakeContactList();
        this.contactlist.saveToLocalStorage();
    }

    private void proceedLocalDeleteGroup(ICQGroup group) {
        this.contactlist.removeGroup(group.id);
        remakeContactList();
        this.contactlist.saveToLocalStorage();
    }

    private void proceedLocalAddContact(ICQContact contact) {
        Log.e("ICQProfile", "Removing " + contact.ID);
        this.svc.removeFromOpenedChats(contact.ID);
        this.contactlist.removeContact(contact.ID);
        contact.added = true;
        Log.e("ICQProfile", "Adding " + contact.ID);
        this.contactlist.put(contact);
        this.contactlist.sort();
        remakeContactList();
        this.contactlist.saveToLocalStorage();
    }

    private void proceedLocalAddGroup(ICQGroup group) {
        this.contactlist.put(group);
        this.contactlist.sort();
        remakeContactList();
        this.contactlist.saveToLocalStorage();
    }

    private void proceedAddNotAuthContact() {
        try {
            ICQContact contact = (ICQContact) this.lastAdd.object;
            this.BUFFER = ICQProtocol.createSSIEditStart(this.sequence);
            userSend();
            this.BUFFER = ICQProtocol.createAddNotAuthContact(this.sequence, contact);
            userSend();
            this.BUFFER = ICQProtocol.createSSIEditEnd(this.sequence);
            userSend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proceedLocalContactRename() {
        ICQContact contact = (ICQContact) this.lastRename.object;
        //noinspection UnnecessaryLocalVariable
        String newNick = (String) this.lastRename.objectA;
        contact.name = newNick;
        remakeContactList();
        this.contactlist.saveToLocalStorage();
    }

    private final void proceedLocalGroupRename() {
        ICQGroup group = (ICQGroup) this.lastRename.object;
        //noinspection UnnecessaryLocalVariable
        String new_name = (String) this.lastRename.objectA;
        group.name = new_name;
        remakeContactList();
        this.contactlist.saveToLocalStorage();
    }

    private final void startPingTask() {
        this.PING_TASK = new PendingIntentHandler() { // from class: ru.ivansuper.jasmin.icq.ICQProfile.4
            @Override // ru.ivansuper.jasmin.Service.PendingIntentHandler
            public void run() {
                if (ICQProfile.this.ping_answer_received) {
                    ICQProfile.this.ping_answer_received = false;
                    ICQProfile.this.svc.removeTimedTask(ICQProfile.this.PING_TASK);
                    ICQProfile.this.svc.attachTimedTask(ICQProfile.this.PING_TASK, PreferenceTable.ping_freq * 1000);
                    ICQProfile.this.sendPingPacket();
                    return;
                }
                ICQProfile.this.disconnect();
                ICQProfile.this.handleProfileConnectionLost();
            }
        };
        if (PreferenceTable.use_ping) {
            this.PING_TASK.run();
        }
    }

    private void handleProfileStatusChanged() {
        notifyStatusIcon();
        refreshContactList();
    }

    /** @noinspection unused*/
    public final void handleNetworkStateChanged() {
    }

    /** @noinspection unused*/
    public final void handleServerNotResponding() {
        if (!this.rcn.is_active) {
            disconnect();
            handleProfileConnectionLost();
        }
    }

    private void performCleaning() {
        this.cookies = null;
        this.messagesForConfurming.clear();
        this.lastAdd = null;
        this.lastDelete = null;
        this.lastRename = null;
        this.phantom_list.clear();
        this.visible_list.clear();
        this.invisible_list.clear();
        this.ignore_list.clear();
        this.icon_proto = null;
        this.offlineMessages.clear();
        this.transfers.clear();
        this.info_requests.clear();
    }

    private void handleProfileConnected() {
        this.connected = true;
        this.connecting = false;
        startPingTask();
        this.svc.handleChatNeedRefreshContact();
        remakeContactList();
        notifyStatusIcon();
        this.svc.updateNotify();
        EventTranslator.sendProfilePresence(this);
    }

    public final void handleProfileDisconnected() {
        if (!this.jumpingToBOS) {
            this.authorized = false;
            this.connected = false;
            this.connecting = false;
            this.connectedToBOS = false;
            setConnectionStatus(0);
            this.contactlist.setAllContactsOffline();
            this.svc.cancelProgress();
            this.svc.removeTimedTask(this.PING_TASK);
            this.svc.removeTimedTask(this.ach_task_1);
            performCleaning();
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            notifyStatusIcon();
            remakeContactList();
            this.svc.updateNotify();
            EventTranslator.sendProfilePresence(this);
            return;
        }
        setConnectionStatus(30);
        this.jumpingToBOS = false;
    }

    public final void handleProfileConnectionLost() {
        jasminSvc.pla.put(this.nickname, resources.getString("s_icq_connection_losted"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_connection_losted"));
        handleProfileDisconnected();
        if (!this.rcn.is_active) {
            if (PreferenceTable.triple_vibro) {
                long[] pattern = {100, 50, 100, 50, 100, 50};
                this.svc.doVibrate(pattern);
            }
            this.rcn.start();
        }
    }

    @Override
    public final void disconnect() {
        if (this.rcn.is_active) {
            this.rcn.stop();
        }
        if (this.connectedToBOS) {
            this.BUFFER = ICQProtocol.createGoodbye(this.sequence);
            send();
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ignored) {
            }
        }
        if (this.socket != null) {
            this.socket.disconnect();
        }
        if (this.http_auth_used & this.connecting) {
            handleProfileDisconnected();
        } else if (this.connected || this.connecting) {
            jasminSvc.pla.put(this.nickname, resources.getString("s_icq_disconnected"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ": " + resources.getString("s_icq_disconnected"));
            handleProfileDisconnected();
        }
    }

    public final void reconnectorDisconnect() {
        if (this.connectedToBOS) {
            this.BUFFER = ICQProtocol.createGoodbye(this.sequence);
            send();
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
            }
        }
        if (this.socket != null) {
            this.socket.disconnect();
        }
        if (this.http_auth_used & this.connecting) {
            handleProfileDisconnected();
        }
    }

    public final void startConnectingChosed() {
        if (this.rcn.is_active) {
            this.rcn.stop();
        }
        startConnecting();
    }

    @Override
    public final void startConnecting() {
        if (!this.connected && !this.connecting) {
            initSocket();
            this.authorized = false;
            this.connected = false;
            this.connecting = true;
            this.connectedToBOS = false;
            this.authFirstStageCompleted = false;
            this.jumpingToBOS = false;
            handleProfileStatusChanged();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
            int method = Integer.parseInt(sp.getString("ms_auth_method", "0"));
            Log.v("ICQProfile", "startConnecting method=" + method);
            switch (method) {
                case 0: // XOR authentication
                    this.http_auth_used = false;
                    String srv = sp.getString("ms_server", "195.66.114.37");
                    String prt = sp.getString("ms_port", "5190");
                    Log.v("ICQProfile", "Connecting via XOR to " + srv + ":" + prt);
                    setConnectionStatus(10);
                    jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_icq_start_connecting_xor"), new String[]{srv, prt}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                    this.svc.put_log(this.nickname + ": " + utilities.match(resources.getString("s_icq_start_connecting_xor"), new String[]{srv, prt}));
                    this.socket.connect(srv + ":" + prt);
                    break;
                case 1: // MD5 authentication
                    this.http_auth_used = false;
                    String srv2 = sp.getString("ms_server", "195.66.114.37");
                    String prt2 = sp.getString("ms_port", "5190");
                    this.useMD5Login = true;
                    Log.v("ICQProfile", "Connecting via MD5 to " + srv2 + ":" + prt2);
                    setConnectionStatus(10);
                    jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_icq_start_connecting_md5"), new String[]{srv2, prt2}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                    this.svc.put_log(this.nickname + ": " + utilities.match(resources.getString("s_icq_start_connecting_md5"), new String[]{srv2, prt2}));
                    this.socket.connect(srv2 + ":" + prt2);
                    break;
                case 2: // HTTP authentication
                    this.http_auth_used = true;
                    HTTPAuthorizer authorizer = new HTTPAuthorizer(this, new http_auth_listener(this, null));
                    Log.v("ICQProfile", "Connecting via HTTP authorizer");
                    authorizer.performAuthorization();
                    break;
                default:
                    this.http_auth_used = false;
                    String srvD = sp.getString("ms_server", "195.66.114.37");
                    String prtD = sp.getString("ms_port", "5190");
                    Log.v("ICQProfile", "Connecting via default XOR to " + srvD + ":" + prtD);
                    setConnectionStatus(10);
                    jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_icq_start_connecting_xor"), new String[]{srvD, prtD}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                    this.svc.put_log(this.nickname + ": " + utilities.match(resources.getString("s_icq_start_connecting_xor"), new String[]{srvD, prtD}));
                    this.socket.connect(srvD + ":" + prtD);
                    break;
            }

            notifyStatusIcon();
            refreshContactList();
        }
    }


    /**
     * @noinspection unused
     */
    public final class http_auth_listener implements HTTPAuthorizer.HTTPAuthListener {
        private http_auth_listener() {
        }

        /**
         * @noinspection unused
         */ /* synthetic */ http_auth_listener(ICQProfile iCQProfile, http_auth_listener http_auth_listenerVar) {
            this();
        }

        @Override
        public void onSuccess(String bos, byte[] cookie) {
            ICQProfile.this.bos_server = bos;
            ICQProfile.this.cookies = cookie;
            ICQProfile.this.jumpingToBOS = false;
            jasminSvc.pla.put(ICQProfile.this.nickname, utilities.match(resources.getString("s_icq_connecting_to_BOS"), new String[]{bos}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            ICQProfile.this.svc.put_log(ICQProfile.this.nickname + ": " + utilities.match(resources.getString("s_icq_connecting_to_BOS"), new String[]{bos}));
            ICQProfile.this.authFirstStageCompleted = true;
            ICQProfile.this.socket.connect(ICQProfile.this.bos_server);
        }

        @Override
        public void onError(int code) {
            Log.e("ICQProfile", "HTTP authorization error code=" + code);
            ICQProfile.this.proceedLoginError(code);
        }

        @Override
        public void onProgress(int state) {
            ICQProfile.this.setConnectionStatus((state * 3) + 10);
            switch (state) {
                case 1:
                    jasminSvc.pla.put(ICQProfile.this.nickname, resources.getString("s_icq_http_connecting_1"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                    ICQProfile.this.svc.put_log(ICQProfile.this.nickname + ": " + resources.getString("s_icq_http_connecting_1"));
                    return;
                case 2:
                    jasminSvc.pla.put(ICQProfile.this.nickname, resources.getString("s_icq_http_connecting_2"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                    ICQProfile.this.svc.put_log(ICQProfile.this.nickname + ": " + resources.getString("s_icq_http_connecting_2"));
                    return;
                default:
            }
        }
    }

    private void userSend() {
        if (this.connected) {
            send();
        }
    }

    private void send() {
        if (this.socket.connected) {
            this.socket.write(this.BUFFER);
            this.sequence++;
            if (this.sequence > 65535) {
                this.sequence = 0;
            }
        }
    }

    public final void userSend(ByteBuffer buffer) {
        if (this.connected) {
            send(buffer);
        }
    }

    public final void send(ByteBuffer buffer) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.BUFFER) {
            if (this.socket != null) {
                if (this.socket.connected) {
                    this.socket.write(buffer);
                    this.sequence++;
                    if (this.sequence > 65535) {
                        this.sequence = 0;
                    }
                }
            }
        }
    }

    public final void getUnreadMessagesDump(MessagesDump dump) {
        Vector<ICQContact> list = this.contactlist.getContacts();
        for (ICQContact contact : list) {
            if (contact.hasUnreadMessages) {
                dump.simple_messages = true;
                dump.from_contacts++;
                dump.total_messages += contact.getUnreadCount();
            }
        }
    }

    public final class reconnector {
        private volatile reconnect_timer rt;
        public boolean is_active = false;
        public boolean enabled = false;
        public int limit = -1;
        private int tryes = 0;

        public reconnector() {
        }

        public void start() {
            if (!this.is_active) {
                this.enabled = true;
                this.is_active = true;
                this.limit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(ICQProfile.this.svc).getString("ms_reconnection_count", "15"));
                this.tryes = 0;
                this.rt = new reconnect_timer(this, null);
                this.rt.start();
                ICQProfile.this.svc.addWakeLock(ICQProfile.this.ID + ICQProfile.this.password);
                jasminSvc.pla.put(ICQProfile.this.nickname, resources.getString("s_reconnection_start"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                ICQProfile.this.svc.put_log(ICQProfile.this.nickname + ": " + resources.getString("s_reconnection_start"));
            }
        }

        public void stop() {
            if (this.is_active) {
                jasminSvc.pla.put(ICQProfile.this.nickname, resources.getString("s_reconnection_stop"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                ICQProfile.this.svc.put_log(ICQProfile.this.ID + ": " + resources.getString("s_reconnection_stop"));
                this.enabled = false;
                this.is_active = false;
                ICQProfile.this.svc.removeWakeLock(ICQProfile.this.ID + ICQProfile.this.password);
            }
        }

        public final class reconnect_timer extends Thread {
            private reconnect_timer() {
            }

            /** @noinspection unused*/reconnect_timer(reconnector reconnectorVar, reconnect_timer reconnect_timerVar) {
                this();
            }

            @Override
            public void run() {
                reconnector.this.is_active = true;
                int i = 0;
                while (reconnector.this.enabled) {
                    try {
                        //noinspection BusyWait
                        sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                    if (reconnector.this.enabled) {
                        if (i >= 20) {
                            i = 0;
                            if (reconnector.this.tryes < reconnector.this.limit) {
                                ICQProfile.this.reconnectorDisconnect();
                                try {
                                    //noinspection BusyWait
                                    sleep(1000L);
                                } catch (InterruptedException e2) {
                                    e2.printStackTrace();
                                }
                                if (ICQProfile.this.svc.isNetworkAvailable()) {
                                    jasminSvc.pla.put(ICQProfile.this.nickname, utilities.match(resources.getString("s_try_to_reconnect"), new String[]{String.valueOf(reconnector.this.tryes + 1)}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                                    ICQProfile.this.svc.put_log(ICQProfile.this.nickname + ": " + utilities.match(resources.getString("s_try_to_reconnect"), new String[]{String.valueOf(reconnector.this.tryes + 1)}));
                                    ICQProfile.this.startConnecting();
                                    reconnector.this.tryes++;
                                }
                            } else {
                                jasminSvc.pla.put(ICQProfile.this.nickname, resources.getString("s_reconnection_limit_exceed"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                                ICQProfile.this.svc.put_log(ICQProfile.this.nickname + ": " + resources.getString("s_reconnection_limit_exceed"));
                                reconnector.this.stop();
                                ICQProfile.this.svc.runOnUi(ICQProfile.this::disconnect, 150L);
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public final void handleScreenTurnedOff() {
        if (!this.screen_ctrlr.is_active && PreferenceTable.auto_change_status) {
            this.screen_ctrlr.start();
        }
    }

    @Override
    public final void handleScreenTurnedOn() {
        if (this.screen_ctrlr.status_changed) {
            Log.e("Auto-Away", "Main status recovered");
            this.away_text = this.away_text_backup;
            setStatus(this.status);
        }
        this.screen_ctrlr.stop();
    }

    private final class screen_controller {
        private PendingIntentHandler away_task;
        public boolean is_active;
        public boolean status_changed;

        private screen_controller() {
            this.is_active = false;
            this.status_changed = false;
            this.away_task = new PendingIntentHandler() {
                @Override
                public void run() {
                    ICQProfile.this.setTempStatus(1);
                    screen_controller.this.status_changed = true;
                    screen_controller.this.is_active = false;
                }
            };
        }

        /** @noinspection unused*/
        screen_controller(ICQProfile iCQProfile, screen_controller screen_controllerVar) {
            this();
        }

        public void start() {
            if (!this.is_active) {
                this.is_active = true;
                this.status_changed = false;
                ICQProfile.this.svc.attachTimedTask(this.away_task, PreferenceTable.auto_change_status_timeout * 1000L);
            }
        }

        public void stop() {
            this.is_active = false;
            this.status_changed = false;
            ICQProfile.this.svc.removeTimedTask(this.away_task);
        }
    }

    public final void sendPingPacket() {
        ByteBuffer buffer = ICQProtocol.createInvalidPacket(this.sequence);
        send(buffer);
    }

    public final class ping_thread extends Thread {
        private int counter = 0;

        private ping_thread() {
        }

        public void resetTimer() {
            this.counter = 0;
            ICQProfile.this.ping_answer_received = true;
        }

        @Override
        public void run() {
            setPriority(1);
            int period = PreferenceTable.ping_freq;
            setName(ICQProfile.this.ID + " ping thread");
            if (PreferenceTable.use_ping) {
                ByteBuffer buffer = ICQProtocol.createInvalidPacket(ICQProfile.this.sequence);
                ICQProfile.this.send(buffer);
                while (ICQProfile.this.connected) {
                    try {
                        if (this.counter > period) {
                            if (ICQProfile.this.ping_answer_received) {
                                ICQProfile.this.ping_answer_received = false;
                                ByteBuffer buffer2 = ICQProtocol.createInvalidPacket(ICQProfile.this.sequence);
                                ICQProfile.this.send(buffer2);
                                this.counter = 0;
                            } else {
                                ICQProfile.this.disconnect();
                                ICQProfile.this.handleProfileConnectionLost();
                                return;
                            }
                        }
                        //noinspection BusyWait
                        sleep(1000L);
                        this.counter++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public final void reinitParams(ProfilesAdapterItem pdata) {
        this.ID = pdata.id;
        this.password = pdata.pass;
        this.autoconnect = pdata.autoconnect;
        this.enabled = pdata.enabled;
        if (!this.enabled && this.connected) {
            disconnect();
        }
    }
}