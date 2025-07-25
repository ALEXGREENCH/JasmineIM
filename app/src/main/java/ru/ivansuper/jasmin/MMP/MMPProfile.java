package ru.ivansuper.jasmin.MMP;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.ContactsAdapter;
import ru.ivansuper.jasmin.GroupPresenceInfo;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.MessagesDump;
import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.ProfilesAdapterItem;
import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.Service.PendingIntentHandler;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.SmileysManager;
import ru.ivansuper.jasmin.chats.MMPChatActivity;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * Represents a user profile for the Mail.ru Agent (MMP) protocol.
 * This class handles the connection, authentication, contact list management,
 * message sending/receiving, and status updates for an MMP account.
 * It extends {@link IMProfile} to provide a common interface for different
 * instant messaging protocols.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Connecting to the MMP server and handling authentication.</li>
 *     <li>Managing the contact list (roster), including groups and individual contacts.</li>
 *     <li>Sending and receiving messages, including message confirmations.</li>
 *     <li>Handling user status updates (online, offline, away, etc.).</li>
 *     <li>Managing chat sessions with contacts.</li>
 *     <li>Implementing periodic pinging to maintain the connection.</li>
 *     <li>Handling connection loss and automatic reconnection attempts.</li>
 *     <li>Persisting the contact list to a local file.</li>
 *     <li>Interacting with the application's service ({@link jasminSvc}) for UI updates and notifications.</li>
 *     <li>Managing screen on/off events to potentially change user status (e.g., to "away").</li>
 * </ul>
 * </p>
 *
 * <p>The class utilizes a {@link SocketConnection} for network communication and
 * {@link ByteBuffer} for handling packet data. It defines various packet handling
 * methods (e.g., {@code handleServerHelloAck}, {@code handleServerMessage}) to process
 * responses from the MMP server.
 * </p>
 *
 * <p>It also includes inner classes for managing specific tasks:
 * <ul>
 *     <li>{@code screen_controller}: Handles automatic status changes based on screen activity.</li>
 *     <li>{@code reconnector}: Manages automatic reconnection attempts after connection loss.</li>
 * </ul>
 * </p>
 *
 * @see IMProfile
 * @see SocketConnection
 * @see MMPContact
 * @see MMPGroup
 * @see MMPProtocol
 * @see jasminSvc
 */
public class MMPProfile extends IMProfile {
    public String PASS;
    private PendingIntentHandler PING_TASK;
    private final File roster;
    private final SocketConnection socket;
    /** @noinspection unused*/
    private String status_desc;
    /** @noinspection unused*/
    private String status_title;
    private String status_uri;
    private int seq = 0;
    public Vector<ContactlistItem> contacts = new Vector<>();
    public Vector<HistoryItem> messages_for_confirming = new Vector<>();
    private int ping_period = -1;
    private final screen_controller screen_ctrlr = new screen_controller(this, null);
    private final reconnector rcn = new reconnector();

    /** @noinspection ResultOfMethodCallIgnored*/
    public MMPProfile(jasminSvc jasminsvc, String str, String str2, boolean z, boolean z2) {
        this.PASS = "";
        this.status = 0;
        this.profile_type = 2;
        this.svc = jasminsvc;
        this.ID = str;
        this.PASS = str2;
        this.autoconnect = z;
        this.enabled = z2;
        //noinspection deprecation
        this.openedInContactList = PreferenceManager.getDefaultSharedPreferences(this.svc).getBoolean("mmpg" + str, true);
        this.socket = new SocketConnection() {
            @Override
            public void onRawData(ByteBuffer data) {
                MMPProfile.this.handlePacket(data);
            }

            @Override
            public void onConnect() {
                MMPProfile.this.handleConnected();
            }

            @Override
            public void onConnecting() {
            }

            @Override
            public void onDisconnect() {
                MMPProfile.this.handleDisconnected();
            }

            @Override
            public void onLostConnection() {
                MMPProfile.this.handleConnectionLost();
            }

            @Override
            public void onError(int errorCode) {
                MMPProfile.this.handleConnectionLost();
            }
        };
        File file = new File(resources.dataPath + str);
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        File file2 = new File(resources.dataPath + str + "/history");
        if (!file2.isDirectory()) {
            file2.mkdirs();
        }
        File file3 = new File(resources.dataPath + str + "/avatars");
        if (!file3.isDirectory()) {
            file3.mkdirs();
        }
        this.roster = new File(resources.dataPath + str + "/roster.bin");
        if (!this.roster.exists()) {
            try {
                this.roster.createNewFile();
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        } else if (this.roster.length() > 0) {
            loadRoster();
        }
        if (this.autoconnect && z2) {
            this.status = Manager.getInt(str + "status");
            startConnecting();
        }
    }

    public final MMPContact getContactByID(String ID) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 7) {
                    MMPContact contact = (MMPContact) it;
                    if (contact.ID.equals(ID)) {
                        return contact;
                    }
                }
            }
            return null;
        }
    }

    public final Vector<MMPContact> getContactsByGroupId(int id) {
        Vector<MMPContact> list;
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            list = new Vector<>();
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 7) {
                    MMPContact contact = (MMPContact) it;
                    if (contact.group == id) {
                        list.add(contact);
                    }
                }
            }
        }
        return list;
    }

    public final Vector<MMPGroup> getGroups() {
        Vector<MMPGroup> list;
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            list = new Vector<>();
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 9) {
                    MMPGroup group = (MMPGroup) it;
                    list.add(group);
                }
            }
        }
        return list;
    }

    public final Vector<MMPContact> getContacts() {
        Vector<MMPContact> list;
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            list = new Vector<>();
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 7) {
                    MMPContact contact = (MMPContact) it;
                    list.add(contact);
                }
            }
        }
        return list;
    }

    public final Vector<ContactlistItem> getContactsA() {
        Vector<ContactlistItem> list;
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            list = new Vector<>();
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 7) {
                    MMPContact contact = (MMPContact) it;
                    list.add(contact);
                }
            }
        }
        return list;
    }

    /** @noinspection unused*/
    public final Vector<ContactlistItem> getAllContacts() {
        Vector<ContactlistItem> vector;
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            vector = this.contacts;
        }
        return vector;
    }

    public final boolean isAnyChatOpened() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 7) {
                    MMPContact contact = (MMPContact) it;
                    if (contact.isChating) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void setAllContactsOffline() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 7) {
                    MMPContact contact = (MMPContact) it;
                    contact.presence_initialized = false;
                    contact.status = 0;
                }
            }
        }
    }

    private void setAllContactsPresenceInitialized() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.contacts) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem item = this.contacts.get(i);
                if (item.itemType == 7) {
                    item.presence_initialized = true;
                }
            }
        }
    }

    private void sortContactList() {
        Vector<MMPGroup> groups = getGroups();
        Vector<ContactlistItem> sorted = new Vector<>();
        Collections.sort(groups);
        for (int i = 0; i < groups.size(); i++) {
            MMPGroup group = groups.get(i);
            sorted.add(group);
            Vector<MMPContact> cnts = getContactsByGroupId(group.id);
            Collections.sort(cnts);
            sorted.addAll(cnts);
        }
        this.contacts.clear();
        this.contacts.addAll(sorted);
        sorted.clear();
        groups.clear();
    }

    public final GroupPresenceInfo getGroupPresenceInfo(int group_id) {
        GroupPresenceInfo info = new GroupPresenceInfo();
        Vector<MMPContact> list = getContactsByGroupId(group_id);
        info.total = list.size();
        info.empty_for_display = true;
        for (int i = 0; i < list.size(); i++) {
            MMPContact contact = list.get(i);
            if (contact.status != 0) {
                info.empty_for_display = false;
                info.online++;
            }
        }
        if (!PreferenceTable.hideOffline && info.total != 0) {
            info.empty_for_display = false;
        }
        if (!PreferenceTable.hideEmptyGroups) {
            info.empty_for_display = false;
        }
        return info;
    }

    private void handleConnected() {
        setConnectionStatus(45);
        ByteBuffer hello = Packet.createPacket(this.seq, 4097, new ByteBuffer(0));
        send(hello);
    }

    private void handlePacket(ByteBuffer packet) {
        Packet pkg = new Packet(packet);
        switch (pkg.command) {
            case 4098:
                handleServerHelloAck(pkg);
                break;
            case 4100:
                handleAuthSuccess(pkg);
                break;
            case 4101:
                handleAuthFailed(pkg);
                break;
            case 4105:
                try {
                    handleServerMessage(pkg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 4111:
                handleServerUserStatus(pkg);
                break;
            case 4114:
                handleServerMessageConfirm(pkg);
                break;
            case 4151:
                handleServerContactList(pkg);
                break;
        }
    }

    private void handleServerUserStatus(Packet packet) {
        ByteBuffer data = packet.getData();
        int raw_sts = data.readDWordLE();
        int status = raw_sts & 65535;
        String spec_status_uri = data.readLPSA();
        String status_title = data.readLPSULE();
        String status_desc = data.readLPSULE();
        String ID = data.readLPSA();
        MMPContact contact = getContactByID(ID);
        if (contact != null) {
            Log.e("MRIM:" + this.ID, ID + "@spec_status_uri: '" + spec_status_uri + "'     (int: " + status + ")");
            int now = contact.status;
            if (status == 4) {
                status = MMPProtocol.translateStatus(spec_status_uri);
            }
            contact.status = status;
            contact.status_text = status_title + " " + status_desc;
            if (now != 0 && status == 0) {
                handleUserOffline(contact);
            } else {
                handleUserOnline(contact);
            }
            this.svc.handleChatUpdateInfo();
            remakeContactList();
        }
    }

    private void handleUserOnline(MMPContact contact) {
        if (!contact.presence_initialized) {
            contact.presence_initialized = true;
        } else {
            contact.requestBlink();
            if (PreferenceTable.log_online) {
                jasminSvc.pla.put(utilities.match(resources.getString("s_icq_contact_offline"), new String[]{contact.name}), "", resources.getMMPStatusIconFull(contact), null, popup_log_adapter.PRESENSE_DISPLAY_TIME, null);
                this.svc.put_log(this.ID + ": " + utilities.match(resources.getString("s_icq_contact_online"), new String[]{contact.name}));
            }
            this.svc.playEvent(4);
        }
        if (MMPChatActivity.is_any_chat_opened && MMPChatActivity.contact == contact) {
            this.svc.handleChatUpdateInfo();
        }
    }

    private void handleUserOffline(MMPContact contact) {
        if (PreferenceTable.auto_close_chat && contact.isChating && !contact.hasUnreadMessages) {
            closeChat(contact);
        }
        if (PreferenceTable.log_offline) {
            jasminSvc.pla.put(utilities.match(resources.getString("s_icq_contact_offline"), new String[]{contact.name}), "", resources.getMMPStatusIconFull(contact), null, popup_log_adapter.PRESENSE_DISPLAY_TIME, null);
            this.svc.put_log(this.ID + ": " + utilities.match(resources.getString("s_icq_contact_offline"), new String[]{contact.name}));
        }
        this.svc.playEvent(5);
        if (MMPChatActivity.is_any_chat_opened && MMPChatActivity.contact == contact) {
            this.svc.handleChatUpdateInfo();
        }
    }

    private void clearContactList() {
        int i = 0;
        while (i < this.contacts.size()) {
            ContactlistItem item = this.contacts.get(i);
            if (item.itemType != 7 || !((MMPContact) item).isChating) {
                this.contacts.remove(i);
                i--;
            }
            i++;
        }
    }

    private void handleServerContactList(Packet packet) {
        ByteBuffer data = packet.getData();
        int status = data.readDWordLE();
        if (status == 0) {
            handleProfileConnected();
            int groups = data.readDWordLE();
            String group_mask = data.readLPSA();
            if (!group_mask.equals("us")) {
                Log.e("MRIM", "Group mask is not correct. Aborting");
                return;
            }
            String contact_mask = data.readLPSA();
            if (!contact_mask.equals("uussuussssusuuusssss")) {
                Log.e("MRIM", "Contact mask is not correct. Aborting");
                return;
            }
            synchronized (ContactsAdapter.locker) {
                clearContactList();
                for (int i = 0; i < groups; i++) {
                    int flags = data.readDWordLE();
                    String name = data.readLPSULE();
                    this.contacts.add(new MMPGroup(name, this, flags, i));
                }
                while (data.getBytesCountAvailableToRead() > 0) {
                    data.readDWordLE();
                    int group = data.readDWordLE();
                    String email = data.readLPSA();
                    String nick = data.readLPSULE();
                    data.readDWordLE();
                    int raw_sts = data.readDWordLE();
                    int sts = raw_sts & 65535;
                    data.readLPSA();
                    String spec_status_uri = data.readLPSA();
                    String x_title = data.readLPSULE();
                    String x_desc = data.readLPSULE();
                    data.readDWordLE();
                    data.readLPSA();
                    data.readDWordLE();
                    data.readDWordLE();
                    data.readDWordLE();
                    data.readLPSA();
                    data.readLPSA();
                    data.readLPSA();
                    data.readLPSA();
                    data.readLPSA();
                    MMPContact contact = getContactByID(email);
                    if (sts == 4) {
                        sts = MMPProtocol.translateStatus(spec_status_uri);
                    }
                    if (contact == null) {
                        contact = new MMPContact(email, nick, group, this);
                        contact.status = sts;
                        this.contacts.add(contact);
                    } else {
                        contact.status = sts;
                        contact.name = nick;
                        contact.group = group;
                    }
                    contact.status_text = x_title + " " + x_desc;
                }
                sortContactList();
                try {
                    saveRoster();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            setConnectionStatus(100);
            this.svc.handleChatNeedRefreshContact();
            this.svc.handleChatUpdateInfo();
            remakeContactList();
            updateStatus();
        }
    }

    /** @noinspection unused*/
    private void handleAuthSuccess(Packet packet) {
        setConnectionStatus(99);
        Log.e("MRIM", "Authorization success!");
    }

    private void handleAuthFailed(Packet packet) {
        String error = packet.getData().readLPSA();
        Log.e("MRIM", "Authorization error: " + error);
        this.svc.showMessageInContactList(this.ID, error);
        this.socket.disconnect();
    }

    private void handleServerHelloAck(Packet packet) {
        setConnectionStatus(65);
        this.ping_period = packet.getData().readDWordLE();
        send(MMPProtocol.createCsLogin3(this.seq, this.ID, this.PASS));
    }

    public final int getTranslatedStatus() {
        return this.status < 4 ? this.status : MMPProtocol.translateStatus(this.status_uri);
    }

    public final void setStatus(int status) {
        //noinspection StatementWithEmptyBody
        if (status != 0) {

        }
        Manager.putInt(this.ID + "status", status);
        this.status = status;
        this.status_uri = "";
        if (status > 4) {
            this.status = 4;
            this.status_uri = MMPProtocol.translateStatus(status);
        }
        updateStatus();
        notifyStatusIcon();
        remakeContactList();
        EventTranslator.sendProfilePresence(this);
    }

    private void updateStatus() {
        userSend(MMPProtocol.createChangeStatus(this.seq, this.status, this.status_uri, this.status_title, this.status_desc, this.ID));
    }

    /** @noinspection SameParameterValue*/
    private void updateStatus(int temp_status) {
        String uri = this.status_uri;
        if (temp_status > 4) {
            this.status = 4;
            uri = MMPProtocol.translateStatus(temp_status);
        }
        userSend(MMPProtocol.createChangeStatus(this.seq, temp_status, uri, this.status_title, this.status_desc, this.ID));
    }

    private void handleServerMessageConfirm(Packet packet) {
        ByteBuffer data = packet.getData();
        int status = data.readDWordLE();
        if (status == 0) {
            confirmMessage(packet.id);
        }
    }

    private void confirmMessage(int id) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.messages_for_confirming) {
            for (int i = 0; i < this.messages_for_confirming.size(); i++) {
                HistoryItem hst = this.messages_for_confirming.get(i);
                if (hst.mmp_cookie == id) {
                    hst.confirmed = true;
                    this.messages_for_confirming.remove(i);
                    this.svc.handleChatNeedRefresh(this);
                    return;
                }
            }
        }
    }

    private void handleServerMessage(Packet packet) throws IOException {
        String message;
        ByteBuffer data = packet.getData();
        int msg_id = data.readDWordLE();
        int flags = data.readDWordLE();
        String email = data.readLPSA();
        final MMPContact contact = getContactByID(email);
        if (contact != null) {
            if ((2097152 & flags) == 2097152) {
                message = data.readLPS();
            } else {
                message = data.readLPSULE();
            }
            if ((flags & 4) != 4) {
                send(MMPProtocol.createMessageConfirm(this.seq, email, msg_id));
            }
            boolean wakeup_alarm = false;
            if ((32768 & flags) != 32768 && (flags & 1024) != 1024 && (flags & 8) != 8 && (flags & 8192) != 8192 && (4194304 & flags) != 4194304) {
                if ((flags & 16384) == 16384) {
                    wakeup_alarm = true;
                }
                HistoryItem hst = new HistoryItem(System.currentTimeMillis());
                if (wakeup_alarm) {
                    hst.message = resources.getString("s_mrim_wakeup_alarm");
                } else {
                    hst.message = message;
                }
                hst.direction = 1;
                hst.mcontact = contact;
                hst.wakeup_alarm = wakeup_alarm;
                contact.loadLastHistory();
                contact.history.add(hst);
                if (!wakeup_alarm) {
                    contact.writeMessageToHistory(hst);
                }
                if (!MMPChatActivity.is_any_chat_opened || MMPChatActivity.contact != contact) {
                    if (wakeup_alarm) {
                        contact.setHasUnreadedAlarm();
                    }
                    contact.setHasUnreadMessages();
                    jasminSvc.pla.put(contact.name, SmileysManager.getSmiledText(hst.message, 0, false), resources.msg_in, contact.avatar, popup_log_adapter.MESSAGE_DISPLAY_TIME, new Runnable() { // from class: ru.ivansuper.jasmin.MMP.MMPProfile.2
                        @Override // java.lang.Runnable
                        public void run() {
                            Intent i = new Intent(MMPProfile.this.svc, ContactListActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setAction("MMPITEM" + MMPProfile.this.ID + "***$$$SEPARATOR$$$***" + contact.ID);
                            MMPProfile.this.svc.startActivity(i);
                        }
                    });
                    if (PreferenceTable.multi_notify) {
                        this.svc.showPersonalMessageNotify(contact.name + "/" + this.ID, hst.message, true, utilities.getHash(contact), contact);
                    } else {
                        this.svc.putMessageNotify(contact, contact.name, hst.message);
                    }
                    this.svc.last_contact_for_non_multi_notify = contact;
                    this.svc.updateNotify();
                }
                this.svc.handleIncomingMessage(this, contact, hst);
                if (!contact.isChating) {
                    openChat(contact);
                    contact.isChating = true;
                }
                remakeContactList();
            }
        }
    }

    public final void sendMessage(MMPContact contact, HistoryItem hst) {
        if (!contact.isChating) {
            openChat(contact);
            contact.isChating = true;
            remakeContactList();
        }
        hst.mmp_cookie = this.seq;
        send(MMPProtocol.createMessage(this.seq, contact.ID, hst.message));
        this.messages_for_confirming.add(hst);
    }

    public final void doSendSMS(String phone, String text) {
        send(MMPProtocol.createSmsMessage(this.seq, phone, text));
    }

    public final void openChat(MMPContact contact) {
        this.svc.opened_chats.add(contact);
        contact.isChating = true;
        remakeContactList();
    }

    public final void closeChat(MMPContact contact) {
        this.svc.removeFromOpenedChats(contact.ID);
        contact.isChating = false;
        contact.clearPreloadedHistory();
        remakeContactList();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void closeAllChats() {
        int i = 0;
        while (i < this.svc.opened_chats.size()) {
            ContactlistItem contact = this.svc.opened_chats.get(i);
            if (contact.itemType == 7 && ((MMPContact) contact).profile.equals(this)) {
                closeChat((MMPContact) contact);
                Log.e("MRIM", i + ": " + contact.name + " closing chat");
                i--;
            }
            i++;
        }
    }

    private void userSend(ByteBuffer data) {
        if (this.connected) {
            send(data);
        }
    }

    private void send(ByteBuffer data) {
        this.socket.write(data);
        this.seq++;
    }

    public final void connect() {
        setConnectionStatus(15);
        MMPProtocol.getAddress(this.svc, new MMPProtocol.AuthListener() {
            @Override
            public void onResult(String server) {
                if (MMPProfile.this.connecting && server != null) {
                    MMPProfile.this.socket.connect(server);
                }
            }
        });
        setConnectionStatus(25);
        this.seq = 0;
        this.ping_period = -1;
        this.connected = false;
        this.connecting = true;
        notifyStatusIcon();
    }

    private void handleProfileConnected() {
        this.svc.runOnUi(new Runnable() {
            @Override
            public void run() {
                MMPProfile.this.setAllContactsPresenceInitialized();
            }
        }, 5000L);
        this.connecting = false;
        this.connected = true;
        this.PING_TASK = new PendingIntentHandler() {
            @Override
            public void run() {
                MMPProfile.this.svc.removeTimedTask(MMPProfile.this.PING_TASK);
                if (MMPProfile.this.connected) {
                    MMPProfile.this.svc.attachTimedTask(MMPProfile.this.PING_TASK, ((int) (MMPProfile.this.ping_period * 0.8d)) * 1000);
                    MMPProfile.this.sendPingPacket();
                }
            }
        };
        this.PING_TASK.run();
        notifyStatusIcon();
        this.svc.addWakeLock("MRIM_WAKE_LOCK_" + this.ID);
        remakeContactList();
        this.svc.updateNotify();
        EventTranslator.sendProfilePresence(this);
        if (this.rcn.is_active) {
            this.rcn.stop();
        }
        jasminSvc.pla.put(this.ID, resources.getString("s_mrim_connected"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.ID + ": " + resources.getString("s_mrim_connected"));
    }

    private void handleConnectionLost() {
        handleDisconnected();
        if (!this.rcn.is_active) {
            this.rcn.start();
        }
    }

    private void handleDisconnected() {
        this.svc.removeTimedTask(this.PING_TASK);
        this.connecting = false;
        this.connected = false;
        this.messages_for_confirming.clear();
        setConnectionStatus(0);
        setAllContactsOffline();
        this.svc.handleChatUpdateInfo();
        this.svc.removeWakeLock("MRIM_WAKE_LOCK_" + this.ID);
        notifyStatusIcon();
        remakeContactList();
        this.svc.updateNotify();
        EventTranslator.sendProfilePresence(this);
        jasminSvc.pla.put(this.ID, resources.getString("s_mrim_disconnected"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.ID + ": " + resources.getString("s_mrim_disconnected"));
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void setStatusText(String text) {
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final String getStatusText() {
        return "";
    }

    public void startConnectingChosed() {
        startConnecting();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void startConnecting() {
        connect();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void disconnect() {
        if (this.rcn.is_active) {
            this.rcn.stop();
        }
        this.status = 0;
        this.socket.disconnect();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void handleScreenTurnedOff() {
        if (!this.screen_ctrlr.is_active && PreferenceTable.auto_change_status) {
            this.screen_ctrlr.start();
        }
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void handleScreenTurnedOn() {
        if (this.screen_ctrlr.status_changed) {
            updateStatus();
        }
        this.screen_ctrlr.stop();
    }

    private void sendPingPacket() {
        ByteBuffer ping = Packet.createPacket(this.seq, 4102, new ByteBuffer(0));
        send(ping);
    }

    public final void getUnreadMessagesDump(MessagesDump dump) {
        for (int i = 0; i < this.contacts.size(); i++) {
            ContactlistItem item = this.contacts.get(i);
            if (item.itemType == 7) {
                MMPContact contact = (MMPContact) item;
                if (contact.hasUnreadMessages) {
                    dump.simple_messages = true;
                    dump.from_contacts++;
                    dump.total_messages += contact.getUnreadCount();
                }
            }
        }
    }

    private synchronized void saveRoster() throws Throwable {
        DataOutputStream dos = null;
        try {
            //noinspection CaughtExceptionImmediatelyRethrown
            try {
                //noinspection IOStreamConstructor
                DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(this.roster));
                //noinspection CaughtExceptionImmediatelyRethrown
                try {
                    for (ContactlistItem item : this.contacts) {
                        switch (item.itemType) {
                            case 7:
                                MMPContact contact = (MMPContact) item;
                                dos2.write(7);
                                dos2.writeUTF(contact.ID);
                                dos2.writeUTF(contact.name);
                                dos2.writeInt(contact.group);
                                break;
                            case 9:
                                MMPGroup mgroup = (MMPGroup) item;
                                dos2.write(9);
                                dos2.writeUTF(mgroup.name);
                                dos2.writeInt(mgroup.flags);
                                dos2.writeInt(mgroup.id);
                                break;
                        }
                    }
                    dos = dos2;
                } catch (Exception e) {
                    dos = dos2;
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    dos.close();
                } catch (Throwable th) {
                    throw th;
                }
            } catch (Throwable th2) {
                throw th2;
            }
        } catch (Exception ignored) {

        }
        try {
            //noinspection DataFlowIssue
            dos.close();
        } catch (Exception e3) {
            //noinspection CallToPrintStackTrace
            e3.printStackTrace();
        }
    }

    private synchronized void loadRoster() {
        DataInputStream dis = null;
        try {
            try {
                //noinspection IOStreamConstructor
                DataInputStream dis2 = new DataInputStream(new FileInputStream(this.roster));
                while (dis2.available() > 0) {
                    //noinspection CaughtExceptionImmediatelyRethrown
                    try {
                        int item_type = dis2.read();
                        switch (item_type) {
                            case 7:
                                String ID = dis2.readUTF();
                                String nickname = dis2.readUTF();
                                int group = dis2.readInt();
                                MMPContact contact = new MMPContact(ID, nickname, group, this);
                                contact.name = nickname;
                                contact.group = group;
                                this.contacts.add(contact);
                                break;
                            case 9:
                                String name = dis2.readUTF();
                                int flags = dis2.readInt();
                                int id = dis2.readInt();
                                MMPGroup mgroup = new MMPGroup(name, this, flags, id);
                                this.contacts.add(mgroup);
                                break;
                        }
                    } catch (Exception e) {
                        dis = dis2;
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        dis.close();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                sortContactList();
                dis = dis2;
            } catch (Exception ignored) {

            }
            try {
                //noinspection DataFlowIssue
                dis.close();
            } catch (Exception e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
            }
        } catch (Throwable ignored) {

        }
    }

    public final void reinitParams(ProfilesAdapterItem pdata) {
        this.ID = pdata.id;
        this.PASS = pdata.pass;
        this.autoconnect = pdata.autoconnect;
        this.enabled = pdata.enabled;
        if (!this.enabled && this.connected) {
            disconnect();
        }
    }

    private final class screen_controller {
        private final PendingIntentHandler away_task;
        public boolean is_active;
        public boolean status_changed;

        private screen_controller() {
            this.is_active = false;
            this.status_changed = false;
            this.away_task = new PendingIntentHandler() {
                @Override
                public void run() {
                    MMPProfile.this.updateStatus(2);
                    screen_controller.this.status_changed = true;
                    screen_controller.this.is_active = false;
                }
            };
        }

        /** @noinspection unused*/
        screen_controller(MMPProfile mMPProfile, screen_controller screen_controllerVar) {
            this();
        }

        public void start() {
            if (!this.is_active) {
                this.is_active = true;
                this.status_changed = false;
                MMPProfile.this.svc.attachTimedTask(this.away_task, PreferenceTable.auto_change_status_timeout * 1000L);
            }
        }

        public void stop() {
            this.is_active = false;
            this.status_changed = false;
            MMPProfile.this.svc.removeTimedTask(this.away_task);
        }
    }

    private final class reconnector {
        public boolean enabled;
        public boolean is_active;
        private int limit;
        private volatile reconnect_timer rt;
        private int tries;

        private reconnector() {
            this.is_active = false;
            this.enabled = false;
            this.limit = -1;
            this.tries = 0;
        }

        /** @noinspection unused*/ /* synthetic */ reconnector(MMPProfile mMPProfile, reconnector reconnectorVar) {
            this();
        }

        public void start() {
            reconnect_timer reconnect_timerVar = null;
            if (!this.is_active) {
                this.enabled = true;
                this.is_active = true;
                //noinspection DataFlowIssue,deprecation
                this.limit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(MMPProfile.this.svc).getString("ms_reconnection_count", "15"));
                this.tries = 0;
                //noinspection ConstantValue
                this.rt = new reconnect_timer(this, reconnect_timerVar);
                this.rt.setDaemon(true);
                this.rt.start();
                MMPProfile.this.svc.addWakeLock(MMPProfile.this.ID + MMPProfile.this.PASS);
                jasminSvc.pla.put(MMPProfile.this.ID, resources.getString("s_reconnection_start"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                MMPProfile.this.svc.put_log(MMPProfile.this.ID + ": " + resources.getString("s_reconnection_start"));
            }
        }

        public void stop() {
            if (this.is_active) {
                jasminSvc.pla.put(MMPProfile.this.ID, resources.getString("s_reconnection_stop"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                MMPProfile.this.svc.put_log(MMPProfile.this.ID + ": " + resources.getString("s_reconnection_stop"));
                this.enabled = false;
                this.is_active = false;
                MMPProfile.this.svc.removeWakeLock(MMPProfile.this.ID + MMPProfile.this.PASS);
            }
        }

        private final class reconnect_timer extends Thread {
            private reconnect_timer() {
            }

            /** @noinspection unused*/
            reconnect_timer(reconnector reconnectorVar, reconnect_timer reconnect_timerVar) {
                this();
            }

            @Override
            public void run() {
                int i = 0;
                while (reconnector.this.enabled) {
                    try {
                        //noinspection BusyWait
                        sleep(1000L);
                    } catch (InterruptedException e) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                    }
                    i++;
                    if (reconnector.this.enabled) {
                        if (i >= 20) {
                            i = 0;
                            if (reconnector.this.tries < reconnector.this.limit) {
                                MMPProfile.this.handleDisconnected();
                                try {
                                    //noinspection BusyWait
                                    sleep(1000L);
                                } catch (InterruptedException e2) {
                                    //noinspection CallToPrintStackTrace
                                    e2.printStackTrace();
                                }
                                if (MMPProfile.this.svc.isNetworkAvailable()) {
                                    jasminSvc.pla.put(MMPProfile.this.ID, utilities.match(resources.getString("s_try_to_reconnect"), new String[]{String.valueOf(reconnector.this.tries + 1)}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                                    MMPProfile.this.svc.put_log(MMPProfile.this.ID + ": " + utilities.match(resources.getString("s_try_to_reconnect"), new String[]{String.valueOf(reconnector.this.tries + 1)}));
                                    MMPProfile.this.startConnecting();
                                    reconnector.this.tries++;
                                }
                            } else {
                                jasminSvc.pla.put(MMPProfile.this.ID, resources.getString("s_reconnection_limit_exceed"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                                MMPProfile.this.svc.put_log(MMPProfile.this.ID + ": " + resources.getString("s_reconnection_limit_exceed"));
                                reconnector.this.stop();
                                MMPProfile.this.svc.runOnUi(new Runnable() { // from class: ru.ivansuper.jasmin.MMP.MMPProfile.reconnector.reconnect_timer.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        MMPProfile.this.disconnect();
                                    }
                                }, 150L);
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
}
