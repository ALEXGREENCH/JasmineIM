package ru.ivansuper.jasmin.jabber;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import ru.ivansuper.jasmin.Base64Coder;
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
import ru.ivansuper.jasmin.XMPPInterface;
import ru.ivansuper.jasmin.chats.JChatActivity;
import ru.ivansuper.jasmin.jabber.FileTransfer.SIFileReceiver;
import ru.ivansuper.jasmin.jabber.FileTransfer.TransferController;
import ru.ivansuper.jasmin.jabber.GMail.GMailListener;
import ru.ivansuper.jasmin.jabber.GMail.GoogleMail;
import ru.ivansuper.jasmin.jabber.XMLConsole.Stanzas;
import ru.ivansuper.jasmin.jabber.XMLConsole.XMLConsoleActivity;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.bookmarks.BookmarkList;
import ru.ivansuper.jasmin.jabber.bytestreams.SOCKS5Controller;
import ru.ivansuper.jasmin.jabber.commands.Callback;
import ru.ivansuper.jasmin.jabber.commands.Command;
import ru.ivansuper.jasmin.jabber.commands.CommandItem;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.conference.ConferenceItem;
import ru.ivansuper.jasmin.jabber.conference.JoinRequest;
import ru.ivansuper.jasmin.jabber.conference.RoomListCallback;
import ru.ivansuper.jasmin.jabber.conference.RoomsPreviewAdapter;
import ru.ivansuper.jasmin.jabber.forms.Operation;
import ru.ivansuper.jasmin.jabber.registration.ClassicForm;
import ru.ivansuper.jasmin.jabber.vcard.Avatar;
import ru.ivansuper.jasmin.jabber.vcard.VCardDecoder;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/* loaded from: classes.dex */
public class JProfile extends IMProfile {
    /** @noinspection unused*/
    public static final int TYPE_GTALK = 3;
    /** @noinspection unused*/
    public static final int TYPE_QIP = 4;
    /** @noinspection unused*/
    public static final int TYPE_VK = 1;
    /** @noinspection unused*/
    public static final int TYPE_XMPP = 0;
    /** @noinspection unused*/
    public static final int TYPE_YANDEX = 2;
    private static final String VERSION_SUFFIX = "-1";
    public String PASS;
    private PendingIntentHandler PING_TASK;
    private boolean auth_chlng_received;
    private boolean authorized;
    public boolean compressed;
    public GMailListener gmail_listener;
    public String host;
    public int mail_notify_id;
    /** @noinspection unused*/
    private ping_thread pinger;
    public int port;
    public int priority;
    private RoomListCallback room_list_callback;
    public String server;
    public String status_desc;
    public XMLStream stream;
    public boolean tls_enabled;
    public int type;
    public boolean use_compression;
    public boolean use_sasl;
    public boolean use_tls;
    private int messages_seq = 0;
    public String resource = "JasmineIM";
    public Vector<ContactlistItem> contacts = new Vector<>();
    private final reconnector rcn = new reconnector();
    public Vector<GoogleMail.Mail> google_mail = new Vector<>();
    public final Vector<HistoryItem> messages_for_confirm = new Vector<>();
    public final Vector<String> conferences = new Vector<>();
    public final Vector<Conference> conference_rooms = new Vector<>();
    public final Vector<ConferenceItem> conference_items = new Vector<>();
    /** @noinspection unused*/
    private final Vector<JoinRequest> conference_join_requests = new Vector<>();
    private final Vector<Node> buffered_messages = new Vector<>();
    private final Vector<Node> buffered_presences = new Vector<>();
    private final screen_controller screen_ctrlr = new screen_controller(this, null);
    public boolean CONSOLE_ENABLED = false;
    public final Vector<Stanzas> CONSOLE = new Vector<>();
    private boolean ping_answer_received = true;
    private final ArrayList<PacketHandler> handlers = new ArrayList<>();
    private int AUTH_METHOD = 1;
    private final JProtocol.SCRAM SCRAM = new JProtocol.SCRAM();
    public final BookmarkList bookmarks = new BookmarkList(this);
    public final ru.ivansuper.jasmin.jabber.vcard.VCard my_vcard = ru.ivansuper.jasmin.jabber.vcard.VCard.getInstance();
    public final ServerList server_list = new ServerList();
    private boolean roster_received = false;

    private void putMessage(Node node) {
        if (this.roster_received) {
            handleStreamMessage(node);
            return;
        }
        synchronized (this.buffered_messages) {
            this.buffered_messages.add(node);
        }
    }

    private void checkMessagesBuffer() {
        synchronized (this.buffered_messages) {
            while (!this.buffered_messages.isEmpty()) {
                Node node = this.buffered_messages.remove(0);
                handleStreamMessage(node);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void putPresence(Node node) {
        if (this.roster_received) {
            handleStreamPresence(node);
            return;
        }
        synchronized (this.buffered_presences) {
            this.buffered_presences.add(node);
        }
    }

    private void checkPresencesBuffer() {
        synchronized (this.buffered_presences) {
            while (!this.buffered_presences.isEmpty()) {
                Node node = this.buffered_presences.remove(0);
                handleStreamPresence(node);
            }
        }
    }

    public final synchronized void putPacketHandler(PacketHandler handler) {
        this.handlers.add(handler);
    }

    private synchronized boolean checkHandler(String id, Node stanzas) {
        boolean found;
        found = false;
        PacketHandler handler = null;
        int i = 0;
        while (true) {
            if (i >= this.handlers.size()) {
                break;
            }
            PacketHandler h = this.handlers.get(i);
            if (!h.getID().equals(id)) {
                i++;
            } else {
                handler = h;
                handler.slot = stanzas;
                this.handlers.remove(i);
                found = true;
                break;
            }
        }
        if (handler != null) {
            if (handler.runOnUi) {
                this.svc.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        handler.execute();
                    }
                });
            } else {
                handler.execute();
            }
        }
        return found;
    }

    public final void putIntoConsole(final String xml, final int direction) {
        if (this.CONSOLE_ENABLED) {
            this.svc.runOnUi(new Runnable() {
                @Override
                public void run() {
                    JProfile.this.CONSOLE.add(new Stanzas(xml, direction));
                    XMLConsoleActivity.update(JProfile.this);
                }
            });
        }
    }

    public final void clearConsole() {
        this.svc.runOnUi(new Runnable() {
            @Override
            public void run() {
                JProfile.this.CONSOLE.clear();
                XMLConsoleActivity.update(JProfile.this);
            }
        });
    }

    public JProfile(jasminSvc service, String ID, String host, String server, int port, String password, String nick, boolean use_compression, boolean use_tls, boolean use_sasl, boolean autoconnect, boolean enabled, int type) {
        this.PASS = "";
        this.server = "";
        this.host = "";
        this.port = 5222;
        this.status_desc = "";
        this.priority = 30;
        this.type = 0;
        this.profile_type = 1;
        this.svc = service;
        this.ID = ID.toLowerCase();
        this.PASS = password;
        this.server = server.toLowerCase();
        this.host = host.toLowerCase();
        this.port = port;
        if (nick != null) {
            this.nickname = nick;
        } else {
            this.nickname = ID + "@" + host;
        }
        this.enabled = enabled;
        if (this.port == 0) {
            this.port = 5222;
        }
        this.use_tls = use_tls;
        this.use_sasl = use_sasl;
        this.use_compression = use_compression;
        this.autoconnect = autoconnect;
        this.type = type;
        this.mail_notify_id = (ID + "@" + host).hashCode();
        //noinspection deprecation
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.svc);
        this.status_desc = sp.getString(ID + "@" + host + "desc", "");
        this.openedInContactList = sp.getBoolean("pg" + ID, true);
        this.priority = sp.getInt(ID + host + "priority", 30);
        initConnection();
        File avatars_dir = new File(resources.dataPath + getFullJID() + "/avatars/");
        if (!avatars_dir.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            avatars_dir.mkdirs();
        }
        File historyDirectory = new File(resources.dataPath + getFullJID() + "/history");
        if (!historyDirectory.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            historyDirectory.mkdirs();
        }
        File roster = new File(resources.dataPath + getFullJID() + "/roster.bin");
        if (!roster.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                roster.createNewFile();
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        } else if (roster.length() > 0) {
            loadRoster();
        }
        if (autoconnect && enabled) {
            this.status = Manager.getInt(getFullJID() + "status");
            startConnecting();
        }
    }

    private void initConnection() {
        this.stream = new XMLStream(this, this.port == 5223) {
            @Override
            public void onPacket(Node node) {
                String name = node.getName();
                JProfile.this.putIntoConsole(node.compile(), 0);
                JProfile.this.ping_answer_received = true;
                if (JProfile.this.pinger != null) {
                    JProfile.this.pinger.resetTimer();
                }
                boolean catched = XMPPInterface.dispatchOnXMLPacketEvent(JProfile.this, node);
                if (!catched) {
                    if (name.equals("stream:stream")) {
                        if (!JProfile.this.authorized) {
                            JProfile.this.handleStreamOpened();
                        } else {
                            JProfile.this.handleAuthorizedStreamOpened();
                        }
                        return;
                    }
                    if (name.equals("stream:features")) {
                        if (!JProfile.this.authorized) {
                            JProfile.this.handleStreamFeatures(node);
                        } else {
                            JProfile.this.handleAuthorizedStreamFeatures(node);
                        }
                        return;
                    }
                    if (!name.equals("success")) {
                        if (!name.equals("proceed")) {
                            if (!name.equals("compressed")) {
                                if (name.equals("challenge")) {
                                    if (!JProfile.this.auth_chlng_received) {
                                        JProfile.this.handleStreamAuthChallenge(node);
                                        JProfile.this.auth_chlng_received = true;
                                    } else {
                                        JProfile.this.handleStreamChallenge(node);
                                    }
                                    return;
                                }
                                if (!name.equals("iq")) {
                                    if (name.equals("message")) {
                                        JProfile.this.putMessage(node);
                                        return;
                                    }
                                    if (name.equals("presence")) {
                                        JProfile.this.putPresence(node);
                                    } else {
                                        if (!name.equals("stream:error")) {
                                            if (!name.endsWith("failure")) {
                                                return;
                                            }
                                            JProfile.this.handleStreamFailure(node);
                                            return;
                                        }
                                        JProfile.this.handleStreamError(node);
                                    }
                                    return;
                                }
                                JProfile.this.handleStreamIQ(node);
                                return;
                            }
                            JProfile.this.handleStreamCompressed(node);
                            return;
                        }
                        JProfile.this.handleStreamProceed(node);
                        return;
                    }
                    JProfile.this.handleStreamAuthSuccess();
                }
            }

            @Override
            public void onConnect() {
                Log.e("JABBER", "Connected!!!");
                JProfile.this.handleConnected();
            }

            @Override
            public void onConnecting() {
                Log.e("JABBER", "Connecting");
            }

            @Override
            public void onDisconnect() {
                Log.e("JABBER", "Disconnected!");
                JProfile.this.handleDisconnected(false, false);
            }

            @Override
            public void onLostConnection() {
                Log.e("JABBER", "Connection losted!");
                JProfile.this.handleConnectionLosted();
            }

            @Override
            public void onError(int errorCode) {
                JProfile.this.handleConnectionLosted();
            }
        };
    }

    private void handleConnected() {
        setConnectionStatus(32);
        XMLPacket packet = new XMLPacket("<?xml version='1.0' encoding='UTF-8'?><stream:stream to='" + this.host + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' xml:lang='" + Locale.getCurrentLangCode() + "' version='1.0'>", null);
        this.stream.write(packet, this);
    }

    private void handleStreamOpened() {
    }

    private void handleStreamFeatures(Node node) {
        Node starttls = node.findFirstLocalNodeByNameAndNamespace("starttls", "urn:ietf:params:xml:ns:xmpp-tls");
        if (starttls != null && !this.tls_enabled && this.use_tls) {
            jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_encrypting"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_encrypting"));
            this.stream.write(new Node("starttls", "", "urn:ietf:params:xml:ns:xmpp-tls"), this);
            return;
        }
        Node compression = node.findFirstLocalNodeByNameAndNamespace("compression", "http://jabber.org/features/compress");
        if (compression != null && !this.compressed && this.use_compression && compression.getNodeContainValue("zlib") != null) {
            jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_compressing"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_compressing"));
            Node stanzas = new Node("compress", "", "http://jabber.org/protocol/compress");
            Node method = new Node("method", "");
            method.setValue("zlib");
            stanzas.putChild(method);
            this.stream.write(stanzas, this);
            return;
        }
        setConnectionStatus(45);
        Node mechanisms = node.findFirstLocalNodeByNameAndNamespace("mechanisms", "urn:ietf:params:xml:ns:xmpp-sasl");
        if (mechanisms.getNodeContainValue("DIGEST-MD5") != null) {
            this.AUTH_METHOD = 1;
            Node auth = new Node("auth", "", "urn:ietf:params:xml:ns:xmpp-sasl");
            auth.putParameter("mechanism", "DIGEST-MD5");
            this.stream.write(auth, this);
            jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_authentification_digest"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_authentification_digest"));
            return;
        }
        if (mechanisms.getNodeContainValue("SCRAM-SHA-1") != null) {
            this.AUTH_METHOD = 2;
            Node auth2 = new Node("auth", this.SCRAM.getFirstMessageBase64(this.ID), "urn:ietf:params:xml:ns:xmpp-sasl");
            auth2.putParameter("mechanism", "SCRAM-SHA-1");
            this.stream.write(auth2, this);
            jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_authentification_scram"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_authentification_scram"));
            return;
        }
        if (mechanisms.getNodeContainValue("PLAIN") != null) {
            try {
                Node auth3 = new Node("auth", new String(Base64Coder.encode(JProtocol.getPlainArray(this.ID, this.PASS, this.host))), "urn:ietf:params:xml:ns:xmpp-sasl");
                auth3.putParameter("mechanism", "PLAIN");
                this.stream.write(auth3, this);
                jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_authentification_plain"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_authentification_plain"));
                return;
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return;
            }
        }
        if (mechanisms.getNodeContainValue("X-GOOGLE-TOKEN") != null) {
            try {
                Node auth4 = new Node("auth", new String(Base64Coder.encode(JProtocol.getXGoogleToken(this.ID, this.PASS))), "urn:ietf:params:xml:ns:xmpp-sasl");
                auth4.putParameter("mechanism", "X-GOOGLE-TOKEN");
                this.stream.write(auth4, this);
                jasminSvc.pla.put(getFullJID(), resources.getString("s_jabber_authentification_google"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_authentification_google"));
                return;
            } catch (Exception e2) {
                this.svc.showMessageInContactList(getFullJID(), resources.getString("s_jabber_google_auth_error"));
                jasminSvc.pla.put(getFullJID(), resources.getString("s_jabber_google_auth_error"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_google_auth_error"));
                disconnectInternal(false, false);
                return;
            }
        }
        jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_no_supported_auth_mechanism"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_no_supported_auth_mechanism"));
        Log.e("JABBER", "There is no supported auth mechanism");
        this.stream.disconnect();
    }

    private void handleStreamProceed(Node node) {
        if (node.getName().equalsIgnoreCase("proceed") && node.getNamespace().equalsIgnoreCase("urn:ietf:params:xml:ns:xmpp-tls")) {
            Log.e("JABBER", "SSL/TLS enabled!");
            this.tls_enabled = true;
            this.stream.jumpToSSL(this.server, 5222);
        }
    }

    private void handleStreamCompressed(Node node) {
        if (node.getName().equalsIgnoreCase("compressed") && node.getNamespace().equalsIgnoreCase("http://jabber.org/protocol/compress")) {
            Log.e("JABBER", "Compression enabled!");
            this.compressed = true;
            this.stream.compressStreams();
        }
    }

    private void handleStreamAuthChallenge(Node packet) {
        setConnectionStatus(55);
        String callenge = Base64Coder.decodeString(packet.getValue());
        Log.e("JABBER", "Auth challenge received: " + callenge);
        if (this.AUTH_METHOD == 1) {
            HashMap<String, String> values = xml_utils.parseParams(callenge);
            try {
                String realm = values.get("realm");
                if (realm == null) {
                    realm = "";
                }
                Node node = new Node("response", Base64Coder.encodeString(JProtocol.getResponse(this.ID, this.PASS, realm, "xmpp/" + this.host, values.get("nonce"), "5534491fa36be80ffbade139ea1a48ac")), "urn:ietf:params:xml:ns:xmpp-sasl");
                this.stream.write(node, this);
                return;
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return;
            }
        }
        if (this.AUTH_METHOD == 2) {
            Node node2 = new Node("response", this.SCRAM.getAnswerBase64(Base64Coder.decodeString(packet.getValue()), this.PASS), "urn:ietf:params:xml:ns:xmpp-sasl");
            this.stream.write(node2, this);
        }
    }

    /** @noinspection unused*/
    private void handleStreamChallenge(Node packet) {
        setConnectionStatus(70);
        Node node = new Node("response", "", "urn:ietf:params:xml:ns:xmpp-sasl");
        this.stream.write(node, this);
    }

    private void handleStreamAuthSuccess() {
        setConnectionStatus(80);
        jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_authentification_success"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_authentification_success"));
        this.authorized = true;
        XMLPacket packet = new XMLPacket("<?xml version='1.0' encoding='UTF-8'?><stream:stream to='" + this.host + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' xml:lang='" + Locale.getCurrentLangCode() + "' version='1.0'>", null);
        this.stream.write(packet, this);
    }

    private void handleAuthorizedStreamOpened() {
    }

    private void handleAuthorizedStreamFeatures(Node data) {
        setConnectionStatus(85);
        if (data.findFirstLocalNodeByName("bind") != null) {
            jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_resource_setup"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_resource_setup"));
            PacketHandler h = new AnonymousClass5(false);
            Node iq = new Node("iq", "");
            iq.putParameter("type", "set");
            iq.putParameter("id", h.getID());
            Node bind = new Node("bind", "", "urn:ietf:params:xml:ns:xmpp-bind");
            Node resource = new Node("resource", this.resource);
            bind.putChild(resource);
            iq.putChild(bind);
            putPacketHandler(h);
            this.stream.write(iq, this);
            return;
        }
        this.stream.disconnect();
    }

    class AnonymousClass5 extends PacketHandler {
        AnonymousClass5(boolean $anonymous0) {
            super($anonymous0);
        }

        @Override
        public void execute() {
            String JID;
            boolean z = false;
            Node stanzas = this.slot;
            Node jid = stanzas.findFirstNodeByName("jid");
            if (jid != null && (JID = jid.getValue()) != null) {
                JProfile.this.resource = JProtocol.getResourceFromFullID(JID);
            }
            JProfile.this.setConnectionStatus(99);
            jasminSvc.pla.put(JProfile.this.nickname, resources.getString("s_jabber_session_setup"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            JProfile.this.svc.put_log(JProfile.this.nickname + ":\n" + resources.getString("s_jabber_session_setup"));
            PacketHandler h = new PacketHandler(z) {
                @Override
                public void execute() {
                    JProfile.this.setConnectionStatus(100);
                    jasminSvc.pla.put(JProfile.this.nickname, resources.getString("s_jabber_roster_request"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                    JProfile.this.svc.put_log(JProfile.this.nickname + ":\n" + resources.getString("s_jabber_roster_request"));
                    JProfile.this.handleProfileConnected();
                    PacketHandler h2 = new PacketHandler(false) {
                        @Override
                        public void execute() {
                            Node stanzas2 = this.slot;
                            jasminSvc.pla.put(JProfile.this.nickname, resources.getString("s_jabber_starting_session"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                            JProfile.this.svc.put_log(JProfile.this.nickname + ":\n" + resources.getString("s_jabber_starting_session"));
                            if (JProfile.this.rcn.is_active) {
                                JProfile.this.rcn.stop();
                            }
                            JProfile.this.parseRoster(stanzas2);
                            JProfile.this.svc.handleChatNeedRefreshContact();
                            JProfile.this.sendPresence();
                        }
                    };
                    Node iq = new Node("iq");
                    iq.putParameter("type", "get");
                    iq.putParameter("id", h2.getID());
                    Node query = new Node("query", "", "jabber:iq:roster");
                    iq.putChild(query);
                    JProfile.this.putPacketHandler(h2);
                    JProfile.this.stream.write(iq, JProfile.this);
                    if (JProfile.this.type == 3) {
                        Node iq2 = new Node("iq");
                        iq2.putParameter("type", "get");
                        iq2.putParameter("to", JProfile.this.getFullJID());
                        iq2.putParameter("id", "google_mail_notify");
                        iq2.putParameter("from", JProfile.this.getFullJIDWithResource());
                        Node query2 = new Node("query", "", "google:mail:notify");
                        iq2.putChild(query2);
                        JProfile.this.stream.write(iq2, JProfile.this);
                    }
                }
            };
            Node iq = new Node("iq");
            iq.putParameter("type", "set");
            iq.putParameter("id", h.getID());
            Node session = new Node("session", "", "urn:ietf:params:xml:ns:xmpp-session");
            iq.putChild(session);
            JProfile.this.putPacketHandler(h);
            JProfile.this.stream.write(iq, JProfile.this);
        }
    }

    private void handleStreamIQ(Node stanzas) {
        String PID = stanzas.getParameter("id");
        if (PID == null) {
            PID = "";
        }
        String FROM = JProtocol.lowerCaseFullJID(stanzas.getParameter("from"));
        Node disco = stanzas.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/disco#info");
        if (disco != null && stanzas.getParameter("type").equals("get") && (disco.getParameter("node") == null || disco.getParameter("node").startsWith("http://jasmineicq.ru/caps"))) {
            this.stream.write(JProtocol.createDiscoInfo(FROM, PID, disco.params), this);
            return;
        }
        Node disco2 = stanzas.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/disco#items");
        if (disco2 != null && stanzas.getParameter("type").equals("get")) {
            String node = disco2.getParameter("node");
            Node iq = new Node("iq");
            iq.putParameter("type", "result");
            iq.putParameter("to", FROM);
            iq.putParameter("xml:lang", Locale.getCurrentLangCode());
            iq.putParameter("id", PID);
            Node query = new Node("query", "", "http://jabber.org/protocol/disco#items");
            if (node != null) {
                query.putParameter("node", node);
            }
            iq.putChild(query);
            this.stream.write(iq, this);
            return;
        }
        if (!checkHandler(PID, stanzas)) {
            if (stanzas.getParameter("type").equals("error") && !PID.equals("self_ping_thread")) {
                handleErrorMessage(stanzas, FROM);
            }
            Node si = stanzas.findFirstLocalNodeByNameAndNamespace("si", "http://jabber.org/protocol/si");
            if (si != null) {
                proceedIncomingFile(stanzas);
            }
            Node command = stanzas.findFirstLocalNodeByNameAndNamespace("command", "http://jabber.org/protocol/commands");
            if (command != null) {
                Node note = command.findFirstLocalNodeByName("note");
                if (note != null && command.getParameter("status").equals("completed")) {
                    this.svc.showMessageInContactList(Locale.getString("s_information"), note.getValue());
                } else {
                    Node x = command.findFirstLocalNodeByNameAndNamespace("x", "jabber:x:data");
                    if (x != null) {
                        proceedCommand(FROM, PID, command);
                    }
                }
            }
            if (stanzas.hasChilds()) {
                if (stanzas.findFirstLocalNodeByNameAndNamespace("time", "urn:xmpp:time") != null && stanzas.getParameter("type").equals("get")) {
                    Node iq2 = new Node("iq", "");
                    iq2.putParameter("type", "result");
                    iq2.putParameter("to", FROM);
                    iq2.putParameter("id", PID);
                    Node time = new Node("time", "", "urn:xmpp:time");
                    Node utc = new Node("utc", JProtocol.createDateTimeString(System.currentTimeMillis()));
                    Node tzo = new Node("tzo", JProtocol.createTimeZonePattern());
                    time.putChild(utc);
                    time.putChild(tzo);
                    iq2.putChild(time);
                    this.stream.write(iq2, this);
                }
                if (stanzas.findFirstLocalNodeByNameAndNamespace("ping", "urn:xmpp:ping") != null && stanzas.getParameter("type").equals("get") && !PID.equals("self_ping_thread")) {
                    Node iq3 = new Node("iq", "");
                    iq3.putParameter("type", "result");
                    iq3.putParameter("to", FROM);
                    iq3.putParameter("id", PID);
                    this.stream.write(iq3, this);
                }
            }
            Conference conference = getConference(JProtocol.getJIDFromFullID(FROM));
            if (conference != null) {
                conference.proceedPacket(stanzas);
                return;
            }
            if (this.type == 3) {
                switch (PID) {
                    case "google_mail_notify":
                        handleStreamGoogleMail(stanzas);
                        return;
                    case "google_mail_preview":
                        handleStreamGoogleMailPreview(stanzas);
                        return;
                    case "google_mail_new_message_notify":
                        handleStreamGoogleMailNewMessage(stanzas);
                        return;
                }
            }
            if (stanzas.findFirstLocalNodeByNameAndNamespace("mail", "google:mail:notify") != null) {
                GoogleMail.Mail mail = this.google_mail.get(0);
                Node iq4 = new Node("iq");
                iq4.putParameter("type", "get");
                iq4.putParameter("to", getFullJID());
                iq4.putParameter("id", "google_mail_new_message_notify");
                iq4.putParameter("from", getFullJIDWithResource());
                Node query2 = new Node("query", "", "google:mail:notify");
                if (!this.google_mail.isEmpty()) {
                    query2.putParameter("newer-than-tid", mail.tid);
                }
                iq4.putChild(query2);
                this.stream.write(iq4, this);
            }
            if (stanzas.findFirstLocalNodeByNameAndNamespace("query", "jabber:iq:version") != null && stanzas.getParameter("type").equals("get")) {
                Node iq5 = new Node("iq");
                iq5.putParameter("type", "result");
                iq5.putParameter("to", FROM);
                iq5.putParameter("id", PID);
                Node query3 = new Node("query", "", "jabber:iq:version");
                Node name = new Node("name", "Jasmine IM");
                Node version = new Node("version", resources.VERSION + VERSION_SUFFIX);
                Node os = new Node("os", "Android " + resources.OS_VERSION_STR + " (" + resources.SOFTWARE_STR + ")[" + resources.DEVICE_STR + "]");
                query3.putChild(name);
                query3.putChild(version);
                query3.putChild(os);
                iq5.putChild(query3);
                this.stream.write(iq5, this);
            }
            if (stanzas.findFirstLocalNodeByNameAndNamespace("query", "jabber:iq:roster") != null && stanzas.getParameter("type").equals("set")) {
                synchronized (ContactsAdapter.locker) {
                    proceedRosterCommand(stanzas);
                }
                Node iq6 = new Node("iq");
                iq6.putParameter("type", "result");
                iq6.putParameter("to", FROM);
                iq6.putParameter("id", PID);
                this.stream.write(iq6, this);
            }
        }
    }

    private void proceedIncomingFile(Node node) {
        SIFileReceiver receiver = new SIFileReceiver(this, node);
        TransferController.putTransfer(receiver);
        final HistoryItem hst = new HistoryItem(System.currentTimeMillis());
        hst.jtransfer = receiver;
        hst.direction = 1;
        hst.jcontact = getContact(JProtocol.getJIDFromFullID(receiver.partner_jid));
        //noinspection DataFlowIssue
        hst.jcontact.loadLastHistory();
        hst.jcontact.history.add(hst);
        if (!JChatActivity.is_any_chat_opened || JChatActivity.contact != hst.jcontact) {
            hst.jcontact.setHasUnreadMessages();
            jasminSvc.pla.put(hst.jcontact.name, receiver.file_name, resources.file_for_chat, hst.jcontact.avatar, popup_log_adapter.MESSAGE_DISPLAY_TIME, new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(JProfile.this.svc, ContactListActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction("JBRITEM" + JProfile.this.ID + "@" + JProfile.this.host + "***$$$SEPARATOR$$$***" + hst.jcontact.ID);
                    JProfile.this.svc.startActivity(i);
                }
            });
            if (PreferenceTable.multi_notify) {
                this.svc.showPersonalMessageNotify(hst.jcontact.name + "/" + this.ID + "@" + this.host, receiver.file_name, true, utilities.getHash(hst.jcontact), hst.jcontact);
            } else {
                this.svc.putMessageNotify(hst.jcontact, hst.jcontact.name, receiver.file_name);
            }
            this.svc.lastContactForNonMultiNotify = hst.jcontact;
        }
        this.svc.handleIncomingMessage(hst);
        if (!hst.jcontact.isChating) {
            openChat(hst.jcontact);
            hst.jcontact.isChating = true;
        }
        remakeContactList();
    }

    /** @noinspection unused*/
    private void handleErrorMessage(Node xml, String from) {
    }

    private synchronized void confirmAndDeleteMessage(String cookie, JContact contact) {
        for (int i = 0; i < this.messages_for_confirm.size(); i++) {
            HistoryItem hst = this.messages_for_confirm.get(i);
            if (hst.jabber_cookie.equals(cookie)) {
                hst.confirmed = true;
                this.messages_for_confirm.remove(i);
                //noinspection UnusedAssignment
                i--; // так как после remove элементы сдвигаются влево
                Log.e("JProfile", "Equals");
                this.svc.handleChatNeedRefresh(contact);
                break; // если нужно удалить только первое совпадение
            }
        }
    }

    public final void proceedCommand(final String from, final String id, final Node base) {
        this.svc.runOnUi(new Runnable() {
            @Override
            public void run() {
                if (!ContactListActivity.HIDDEN) {
                    JProfile.this.svc.showCommandFormInContactList(new Command(from, id, base, JProfile.this));
                }
            }
        });
    }

    /** @noinspection unused*/
    public final void proceedXForm(String from, final String id, final Node base) {
        this.svc.runOnUi(new Runnable() {
            @Override
            public void run() {
                Operation op = new Operation();
                op.profile = JProfile.this;
                op.prepareForm(base, id);
                op.form.build();
                op.to_type = 3;
                if (!ContactListActivity.HIDDEN) {
                    JProfile.this.svc.showXFormInContactList(op.form);
                }
            }
        });
    }

    /** @noinspection unused*/
    private void proceedCaptcha(String from, final String id, final Node base) {
        this.svc.runOnUi(new Runnable() {
            @Override
            public void run() {
                Operation op = new Operation();
                op.profile = JProfile.this;
                op.prepareForm(base, id);
                op.form.build();
                op.to_type = 3;
                if (!ContactListActivity.HIDDEN) {
                    JProfile.this.svc.showXFormInContactList(op.form);
                }
            }
        });
    }

    private void handleStreamMessage(Node node) {
        JContact contact;
        JContact contact2;
        String id;
        String id2;
        if (node.hasChilds()) {
            final String from = JProtocol.lowerCaseFullJID(node.getParameter("from"));
            String type = node.getParameter("type");
            if (type == null) {
                type = "normal";
            }
            if (type.equals("error")) {
                handleErrorMessage(node, from);
                return;
            }
            Node delay = node.findFirstLocalNodeByNameAndNamespace("delay", "urn:xmpp:delay");
            long timestamp = delay != null ? JProtocol.parseTimeStamp(delay.getParameter("stamp")) : 0L;
            Node captcha = node.findFirstLocalNodeByName("captcha");
            if (captcha != null) {
                proceedCaptcha(from, node.getParameter("id"), captcha);
                return;
            }
            final Conference conference = getConference(JProtocol.getJIDFromFullID(from));
            boolean private_ = false;
            if (conference != null) {
                if (type.equals("groupchat")) {
                    Node BODY = node.findFirstLocalNodeByName("body");
                    String body = BODY != null ? BODY.getValue() : null;
                    Node SUBJECT = node.findFirstLocalNodeByName("subject");
                    String subject = SUBJECT != null ? SUBJECT.getValue() : null;
                    if (subject == null) {
                        subject = "";
                    }
                    String message = !subject.isEmpty() ? resources.getString("s_jabber_message_theme") + ": " + subject + "\n\n" : "";
                    if (body != null) {
                        message = message + body;
                    }
                    //noinspection ConstantValue
                    if (!message.isEmpty() || !subject.isEmpty()) {
                        final String subject_ = subject;
                        final String message_ = message;
                        final long timestamp_ = timestamp;
                        this.svc.runOnUi(new Runnable() {
                            @Override
                            public void run() {
                                conference.incomingMessage(from, JProtocol.getResourceFromFullID(from), subject_, message_, timestamp_);
                            }
                        });
                        return;
                    }
                } else {
                    private_ = true;
                }
            }
            if (private_) {
                Log.e("JABBER", "PRIVATE MESSAGE: " + from);
                JContact contact3 = getContactByJID(from);
                if (contact3 == null) {
                    contact = createPMContainer(from, conference);
                } else {
                    contact = contact3;
                }
            } else {
                contact = getContactByJID(JProtocol.getJIDFromFullID(from));
            }
            if (contact != null) {
                if (node.findFirstNodeByName("composing") != null) {
                    contact.typing = true;
                }
                if (node.findFirstNodeByName("paused") != null) {
                    contact.typing = false;
                }
                if (node.findFirstNodeByName("active") != null) {
                    contact.typing = false;
                }
            }
            if (node.findFirstLocalNodeByNameAndNamespace("received", "urn:xmpp:receipts") != null && (id2 = node.getParameter("id")) != null) {
                confirmAndDeleteMessage(id2, contact);
            }
            if (node.findFirstLocalNodeByNameAndNamespace("request", "urn:xmpp:receipts") != null && (id = node.getParameter("id")) != null) {
                Node mess = new Node("message");
                mess.putParameter("to", from);
                mess.putParameter("id", id);
                Node received = new Node("received", "", "urn:xmpp:receipts");
                mess.putChild(received);
                this.stream.write(mess, this);
            }
            refreshContactList();
            this.svc.handleChatUpdateInfo();
            Node BODY2 = node.findFirstLocalNodeByName("body");
            String body2 = BODY2 != null ? BODY2.getValue() : null;
            Node SUBJECT2 = node.findFirstLocalNodeByName("subject");
            String subject2 = SUBJECT2 != null ? SUBJECT2.getValue() : null;
            if (subject2 == null) {
                subject2 = "";
            }
            String message2 = !subject2.isEmpty() ? resources.getString("s_jabber_message_theme") + ": " + subject2 + "\n\n" : "";
            if (body2 != null) {
                message2 = message2 + body2;
            }
            if (!type.equals("chat")) {
                if (!message2.trim().isEmpty()) {
                    this.svc.showMessageInContactList(from, xml_utils.decodeString(message2));
                }
                return;
            }
            if (!message2.isEmpty()) {
                if (contact == null) {
                    synchronized (ContactsAdapter.locker) {
                        try {
                            //noinspection CaughtExceptionImmediatelyRethrown
                            try {
                                contact2 = new JContact(this, JProtocol.getJIDFromFullID(from));
                                contact2.name = JProtocol.getJIDFromFullID(from);
                                doAddContact(contact2);
                                this.contacts.add(contact2);
                                sortContactList();
                                saveRoster();
                            } catch (Throwable th) {
                                ////th = th;
                                throw th;
                            }
                        } catch (Throwable th2) {
                            /////th = th2;
                            try {
                                throw th2;
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } else {
                    contact2 = contact;
                }
                contact2.typing = false;
                final JContact contact_ = contact2;
                final String message_2 = message2;
                final long timestamp_2 = timestamp;
                this.svc.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        JProfile.this.handleMessage(contact_, message_2, -1, timestamp_2);
                    }
                });
            } else {
                contact2 = contact;
            }
            //noinspection StatementWithEmptyBody
            if (contact2 == null) {

            }
        }
    }

    public final JContact createPMContainer(String FullJID, Conference conference) {
        synchronized (ContactsAdapter.locker) {
            JContact contact = getContactByJID(FullJID);
            if (contact != null) {
                return contact;
            }
            JContact contact2 = new JContact(this, FullJID);
            String res = JProtocol.getResourceFromFullID(FullJID);
            //noinspection NonStrictComparisonCanBeEquality
            if (res.length() <= 0) {
                res = FullJID;
            }
            contact2.name = res;
            contact2.conf_pm = true;
            contact2.setResource("PM", 1, 0, null, JProtocol.getJIDFromFullID(FullJID));
            conference.registerPMContact(contact2);
            this.contacts.add(contact2);
            sortContactList();
            return contact2;
        }
    }

    /** @noinspection unused*/
    private void handleMessage(final JContact from, String body, int auth, long timestamp) {
        String body2 = xml_utils.decodeString(body);
        HistoryItem hst = new HistoryItem(System.currentTimeMillis());
        hst.message = body2;
        hst.direction = 1;
        hst.jcontact = from;
        if (auth != -1) {
            hst.isAuthMessage = true;
            hst.authType = auth;
        }
        from.loadLastHistory();
        from.history.add(hst);
        from.writeMessageToHistory(hst);
        if (!JChatActivity.is_any_chat_opened || JChatActivity.contact != from) {
            from.setHasUnreadMessages();
            jasminSvc.pla.put(from.name, SmileysManager.getSmiledText(body2, 0, false), resources.msg_in, from.avatar, popup_log_adapter.MESSAGE_DISPLAY_TIME, new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(JProfile.this.svc, ContactListActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction("JBRITEM" + JProfile.this.ID + "@" + JProfile.this.host + "***$$$SEPARATOR$$$***" + from.ID);
                    JProfile.this.svc.startActivity(i);
                }
            });
            if (PreferenceTable.multi_notify) {
                this.svc.showPersonalMessageNotify(from.name + "/" + this.ID + "@" + this.host, body2, true, utilities.getHash(from), from);
            } else {
                this.svc.putMessageNotify(from, from.name, body2);
            }
            this.svc.lastContactForNonMultiNotify = from;
            remakeContactList();
        }
        this.svc.handleIncomingMessage(hst);
        if (!from.isChating) {
            openChat(from);
        }
    }

    private void handleStreamPresence(Node data) {
        final String from = JProtocol.lowerCaseFullJID(data.getParameter("from"));
        String type = data.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            type = "available";
        }
        int priority = 0;
        Node sts = data.findFirstLocalNodeByName("show");
        String status = sts != null ? sts.getValue() : null;
        Node dsc = data.findFirstLocalNodeByName("status");
        String desc = dsc != null ? dsc.getValue() : null;
        Node pr = data.findFirstLocalNodeByName("priority");
        String prior = pr != null ? pr.getValue() : null;
        if (prior != null) {
            try {
                priority = Integer.parseInt(prior);
            } catch (Exception e) {
                //noinspection DataFlowIssue
                priority = 0;
            }
        }
        Node c = data.findFirstLocalNodeByNameAndNamespace("c", "http://jabber.org/protocol/caps");
        String client = c != null ? c.getParameter("node") : null;
        final Conference conference = getConference(JProtocol.getJIDFromFullID(from));
        if (conference != null) {
            if (type.equals("error")) {
                handleErrorMessage(data, from);
                return;
            }
            if (type.equals("available") || type.equals("unavailable")) {
                String reason = "";
                int status_code = 0;
                boolean sys = false;
                Node X = data.findFirstLocalNodeByNameAndNamespace("x", "http://jabber.org/protocol/muc#admin");
                if (X == null) {
                    X = data.findFirstLocalNodeByNameAndNamespace("x", "http://jabber.org/protocol/muc#user");
                }
                if (X != null) {
                    Node reason_ = X.findFirstNodeByName("reason");
                    if (reason_ != null) {
                        reason = reason_.getValue();
                    }
                    if (reason == null) {
                        reason = "";
                    }
                    //noinspection CatchMayIgnoreException
                    try {
                        status_code = Integer.parseInt(X.findFirstLocalNodeByName("status").getParameter("code"));
                    } catch (Exception e2) {
                    }
                    if (status_code == 307 || status_code == 301) {
                        sys = true;
                    }
                }
                //noinspection DataFlowIssue
                final Node item = X.findFirstLocalNodeByName("item");
                final String reason_2 = reason;
                final String status_ = status;
                final String desc_ = desc;
                final String client_ = client;
                final int status_code_ = status_code;
                if (sys) {
                    this.svc.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            conference.incomingSysMessage(from, "(" + reason_2 + ")", status_code_);
                        }
                    });
                }
                if (type.equals("available")) {
                    this.svc.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            conference.userOnline(JProtocol.getResourceFromFullID(from), JProtocol.lowerCaseFullJID(item.getParameter("jid")), item.getParameter("affiliation"), item.getParameter("role"), status_, JProtocol.lowerCaseFullJID(from), reason_2, desc_, client_);
                        }
                    });
                }
                if (type.equals("unavailable")) {
                    this.svc.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            conference.userOffline(JProtocol.getResourceFromFullID(from), item.getParameter("affiliation"), item.getParameter("role"), item.getParameter("nick"), JProtocol.lowerCaseFullJID(from), reason_2, status_code_);
                        }
                    });
                    return;
                }
                return;
            }
        }
        switch (type) {
            case "available":
                userOnline(from, status, desc, priority, client);
                return;
            case "subscribe":
                handleAuthAsk(from);
                return;
            case "subscribed":
                handleAuthAllowed(from);
                break;
            case "ensubscribed":
                handleAuthDenied(from);
                break;
            case "unavailable":
                userOffline(from);
                break;
        }
    }

    /** @noinspection unused*/
    public final void sendMessage(JContact to, String body, HistoryItem hst, String resource) {
        String id = "msg_" + this.messages_seq;
        hst.jabber_cookie = id;
        this.messages_for_confirm.add(hst);
        if (resource == null) {
            if (this.type == 3) {
                Vector<JContact.Resource> list = to.getResources();
                for (int i = 0; i < list.size(); i++) {
                    Node mess = new Node("message");
                    mess.putParameter("type", "chat");
                    mess.putParameter("to", to.ID + "/" + list.get(i).name);
                    mess.putParameter("id", id);
                    Node BODY = new Node("body", body);
                    mess.putChild(BODY);
                    if (to.isOnline()) {
                        Node request = new Node("request", "", "urn:xmpp:receipts");
                        mess.putChild(request);
                    }
                    this.stream.write(mess, this);
                }
            } else {
                Node mess2 = new Node("message");
                mess2.putParameter("type", "chat");
                mess2.putParameter("to", to.ID);
                mess2.putParameter("id", id);
                Node BODY2 = new Node("body", body);
                mess2.putChild(BODY2);
                if (to.isOnline()) {
                    Node request2 = new Node("request", "", "urn:xmpp:receipts");
                    mess2.putChild(request2);
                }
                this.stream.write(mess2, this);
            }
        } else {
            Node mess3 = new Node("message");
            mess3.putParameter("type", "chat");
            mess3.putParameter("to", to.ID + "/" + resource);
            mess3.putParameter("id", id);
            Node BODY3 = new Node("body", body);
            mess3.putChild(BODY3);
            if (to.isOnline()) {
                Node request3 = new Node("request", "", "urn:xmpp:receipts");
                mess3.putChild(request3);
            }
            this.stream.write(mess3, this);
        }
        this.messages_seq++;
        if (!to.isChating) {
            openChat(to);
            to.isChating = true;
            remakeContactList();
        }
    }

    /** @noinspection unused*/
    public final void doRequestMailBoxInfo() {
        XMLPacket packet = new XMLPacket("<iq type='get' to='" + this.ID + "@" + this.host + "' id='google_mail_notify' from='" + this.ID + "@" + this.host + "/" + this.resource + "'><query xmlns='google:mail:notify'/></iq>", null);
        this.stream.write(packet, this);
    }

    /** @noinspection unused*/
    public final void doRequestMailBoxPreviewInfo() {
        XMLPacket packet = new XMLPacket("<iq type='get' to='" + this.ID + "@" + this.host + "' id='google_mail_preview' from='" + this.ID + "@" + this.host + "/" + this.resource + "'><query xmlns='google:mail:notify'/></iq>", null);
        this.stream.write(packet, this);
    }

    private void showGMailNotify(String title, String desc, int count) {
        this.svc.cancelMessageNotification(this.mail_notify_id);
        this.svc.showMailMessageNotify(title, desc, true, this.mail_notify_id, count, this.ID + "@" + this.host);
    }

    private void handleStreamGoogleMail(Node node) {
        this.google_mail = GoogleMail.parseXml(node);
        if (!this.google_mail.isEmpty()) {
            String to_display = utilities.match(resources.getString("s_mail_unread_count_1"), new String[]{String.valueOf(this.google_mail.size())});
            showGMailNotify(this.ID + "@" + this.host, utilities.match(resources.getString("s_mail_unread_count_1"), new String[]{String.valueOf(this.google_mail.size())}), this.google_mail.size());
            this.svc.showMessageInContactList(this.ID + "@" + this.host, to_display);
        }
    }

    private void handleStreamGoogleMailNewMessage(Node node) {
        Vector<GoogleMail.Mail> list = GoogleMail.parseXml(node);
        if (!list.isEmpty()) {
            GoogleMail.Mail mail = list.get(0);
            this.google_mail.insertElementAt(mail, 0);
            showGMailNotify(this.ID + "@" + this.host, utilities.match(resources.getString("s_mail_unread_count_1"), new String[]{String.valueOf(this.google_mail.size())}), this.google_mail.size());
            this.svc.runOnUi(new Runnable() {
                @Override
                public void run() {
                    if (JProfile.this.gmail_listener != null) {
                        JProfile.this.gmail_listener.onListChanged();
                    }
                }
            });
        }
    }

    private void handleStreamGoogleMailPreview(Node node) {
        this.google_mail = GoogleMail.parseXml(node);
        if (this.gmail_listener != null) {
            this.gmail_listener.onListChanged();
        }
    }

    private void proceedRosterCommand(Node node) {
        synchronized (ContactsAdapter.locker) {
            Node query = node.findFirstLocalNodeByName("query");
            for (Node n : query.childs) {
                String name = n.getParameter("name");
                String jid = JProtocol.lowerCaseFullJID(n.getParameter("jid"));
                Node GROUP = n.findFirstLocalNodeByName("group");
                String group = GROUP != null ? GROUP.getValue() : null;
                String subscript = n.getParameter("subscription");
                if (subscript.equals("remove")) {
                    removeContactByJID(jid);
                } else {
                    if (group != null && getGroup(group) == null && !group.isEmpty()) {
                        JGroup jgroup = new JGroup(this, group);
                        this.contacts.add(jgroup);
                    }
                    if (name == null) {
                        name = jid;
                    }
                    JContact contact = getContactByJID(jid);
                    boolean exist = true;
                    if (contact == null) {
                        contact = new JContact(this, jid);
                        exist = false;
                    }
                    contact.name = name;
                    if (group == null) {
                        group = "";
                    }
                    contact.group = group;
                    contact.subscription = 0;
                    if (subscript.equals("none")) {
                        //noinspection DataFlowIssue
                        contact.subscription = 0;
                    }
                    if (subscript.equals("from")) {
                        contact.subscription = 1;
                    }
                    if (subscript.equals("to")) {
                        contact.subscription = 2;
                    }
                    if (subscript.equals("both")) {
                        contact.subscription = 3;
                    }
                    if (jid.equals(this.ID + "@" + this.host)) {
                        contact.subscription = 3;
                    }
                    if (!exist) {
                        this.contacts.add(contact);
                    }
                }
            }
            sortContactList();
            remakeContactList();
            saveRoster();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void handleStreamRoomsList(Node node) {
        if (this.room_list_callback != null) {
            if (node == null) {
                this.room_list_callback.error();
                return;
            }
            Vector<RoomsPreviewAdapter.Item> rooms = new Vector<>();
            for (Node n : node.childs) {
                RoomsPreviewAdapter.Item i = new RoomsPreviewAdapter.Item();
                i.label = n.getParameter("name");
                i.desc = JProtocol.lowerCaseFullJID(n.getParameter("jid"));
                rooms.add(i);
            }
            if (this.room_list_callback != null) {
                this.room_list_callback.roomsLoaded(rooms);
                this.room_list_callback = null;
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    public final void setStatusDescription(String description) {
        this.status_desc = description;
        this.svc.sharedPreferences.edit().putString(getFullJID() + "desc", this.status_desc).commit();
        sendPresence();
    }

    public final String getStatusDescription() {
        return this.status_desc;
    }

    /** @noinspection unused*/
    public final void setPriority(int priority) {
        this.priority = priority;
        sendPresence();
    }

    public final void joinConference(final String JID, final String nick, final String pass) {
        boolean z = false;
        Log.e(getClass().getSimpleName(), "Joining conference " + JID);
        Conference conference = getConference(JID);
        if (conference != null) {
            Log.e(getClass().getSimpleName(), "Conference found. Updating status " + JID);
            conference.nick = nick;
            saveRoster();
            if (!conference.isTurnedOn()) {
                conference.updatePresence();
            }
            Log.e(getClass().getSimpleName(), "Conference found. Status updated " + JID);
            return;
        }
        Log.e(getClass().getSimpleName(), "Conference not found. Creating handler, sending request " + JID);
        PacketHandler h = new PacketHandler(z) { // from class: ru.ivansuper.jasmin.jabber.JProfile.17
            @Override // ru.ivansuper.jasmin.jabber.PacketHandler
            public void execute() {
                Node stanzas = this.slot;
                Node query = stanzas.findFirstLocalNodeByName("query");
                Node muc_feature = query.findFirstLocalNodeByNameAndParameter("feature", "var", "http://jabber.org/protocol/muc");
                if (muc_feature == null) {
                    JProfile.this.svc.showMessageInContactList(resources.getString("s_information"), resources.getString("s_jabber_server_in_not_for_conf_error"));
                } else {
                    JProfile.this.addConference(JID, JProtocol.getNameFromFullID(JID), nick, pass, true);
                }
            }
        };
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", JProtocol.getServerFromFullID(JID)).putParameter("id", h.getID());
        Node query = new Node("query");
        query.putParameter("xmlns", "http://jabber.org/protocol/disco#info");
        iq.putChild(query);
        putPacketHandler(h);
        this.stream.write(iq, this);
    }

    public final void doRequestInfoForDisplayRaw(final String full_jid) {
        PacketHandler h = new PacketHandler(false) { // from class: ru.ivansuper.jasmin.jabber.JProfile.18
            @Override // ru.ivansuper.jasmin.jabber.PacketHandler
            public void execute() {
                Node stanzas = this.slot;
                if (stanzas != null) {
                    if (stanzas.getParameter("type").equals("error")) {
                        JProfile.this.svc.showMessageInContactList(Locale.getString("s_information"), utilities.match(Locale.getString("s_contact_info_showing_error"), new String[]{full_jid}));
                        return;
                    }
                    Node vCard = stanzas.findFirstLocalNodeByNameAndNamespace("vCard", "vcard-temp");
                    if (vCard != null) {
                        VCard vcard_ = new VCard();
                        vcard_.avatar = Avatar.getAvatar(vCard);
                        vcard_.desc = VCardDecoder.decode(vCard);
                        JContact contact = JProfile.this.getContactByJID(JProtocol.lowerCaseFullJID(stanzas.getParameter("from")));
                        if (contact != null) {
                            contact.saveAvatar(vcard_.avatar);
                            contact.readLocalAvatar();
                        }
                        JProfile.this.svc.showVCardInContactList(vcard_);
                    }
                }
            }
        };
        putPacketHandler(h);
        sendVCardRequest(h.getID(), full_jid);
    }

    public final void sendVCardRequest(String pid, String to) {
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", to).putParameter("id", pid);
        Node vCard = new Node("vCard", "", "vcard-temp");
        iq.putChild(vCard);
        this.stream.write(iq, this);
    }

    public final void logoutConference(String JID) {
        Conference conference = getConference(JID);
        if (conference != null) {
            conference.setAsOffline(true);
            this.svc.handleChatUpdateInfo();
            remakeContactList();
            sendConferenceOfflinePresence(JID, conference.nick);
        }
    }

    public final void removeConference(String JID) {
        ConferenceItem item_ = null;
        synchronized (ContactsAdapter.locker) {
            int i = 0;
            while (true) {
                if (i >= this.conference_rooms.size()) {
                    break;
                }
                Conference conference = this.conference_rooms.get(i);
                if (!conference.JID.equals(JID)) {
                    i++;
                } else {
                    this.conference_rooms.remove(i);
                    break;
                }
            }
            int i2 = 0;
            while (true) {
                if (i2 >= this.conference_items.size()) {
                    break;
                }
                ConferenceItem item = this.conference_items.get(i2);
                if (!item.conference.JID.equals(JID)) {
                    i2++;
                } else {
                    item_ = this.conference_items.remove(i2);
                    break;
                }
            }
            sortContactList();
            saveRoster();
        }
        if (item_ != null) {
            this.svc.removeMessageNotify(item_);
        }
        this.svc.handleContactlistCheckConferences();
        remakeContactList();
    }

    private void sendConferenceOfflinePresence(String JID, String nick) {
        XMLPacket packet = new XMLPacket("<presence to='" + JID + "/" + nick + "' type='unavailable'/>", null);
        this.stream.write(packet, this);
    }

    @SuppressLint("ApplySharedPref")
    public final void changePriority(int priority) {
        this.priority = priority;
        //noinspection deprecation
        PreferenceManager.getDefaultSharedPreferences(resources.ctx).edit().putInt(this.ID + this.host + "priority", priority).commit();
        sendPresence();
    }

    public final void setStatus(int status) {
        if (status != -1) {
            Manager.putInt(getFullJID() + "status", status);
        }
        this.status = status;
        sendPresence();
        notifyStatusIcon();
        EventTranslator.sendProfilePresence(this);
    }

    private void sendPresence() {
        sendPresence(null, true);
    }

    public final void sendPresence(String to, boolean available) {
        Node presence = new Node("presence");
        if (to != null) {
            presence.putParameter("to", to);
        }
        if (!available) {
            presence.putParameter("type", "unavailable");
        }
        Node priority = new Node("priority");
        priority.setValue(String.valueOf(this.priority));
        presence.putChild(priority);
        String status_ = JProtocol.parseStatus(this.status);
        if (!status_.isEmpty()) {
            Node show = new Node("show");
            show.setValue(JProtocol.parseStatus(this.status));
            presence.putChild(show);
        }
        if (!this.status_desc.isEmpty()) {
            Node status = new Node("status");
            status.setValue(this.status_desc);
            presence.putChild(status);
        }
        Node c = new Node("c", "", "http://jabber.org/protocol/caps");
        c.putParameter("node", "http://jasmineicq.ru/caps");
        c.putParameter("ver", resources.VERSION + VERSION_SUFFIX);
        presence.putChild(c);
        this.stream.write(presence, this);
        for (int i = 0; i < this.conference_rooms.size(); i++) {
            Conference conf = this.conference_rooms.get(i);
            if (conf.isTurnedOn()) {
                conf.updatePresence();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void sendPresence(int temp_status) {
        Node presence = new Node("presence");
        Node priority = new Node("priority");
        priority.setValue(String.valueOf(this.priority));
        presence.putChild(priority);
        Node show = new Node("show");
        show.setValue(JProtocol.parseStatus(temp_status));
        presence.putChild(show);
        Node status = new Node("status");
        status.setValue(this.status_desc);
        presence.putChild(status);
        Node c = new Node("c", "", "http://jabber.org/protocol/caps");
        c.putParameter("node", "http://jasmineicq.ru/caps");
        c.putParameter("ver", resources.VERSION);
        presence.putChild(c);
        this.stream.write(presence, this);
        for (int i = 0; i < this.conference_rooms.size(); i++) {
            Conference conf = this.conference_rooms.get(i);
            if (conf.isTurnedOn()) {
                conf.updatePresence(temp_status);
            }
        }
    }

    public final void requestRoomsList(String server, RoomListCallback callback) {
        this.room_list_callback = callback;
        PacketHandler h = new PacketHandler(false) { // from class: ru.ivansuper.jasmin.jabber.JProfile.19
            @Override // ru.ivansuper.jasmin.jabber.PacketHandler
            public void execute() {
                Node stanzas = this.slot;
                if (stanzas.getParameter("type").equals("result")) {
                    JProfile.this.handleStreamRoomsList(stanzas.findFirstLocalNodeByName("query"));
                } else if (stanzas.getParameter("type").equals("error") && JProfile.this.room_list_callback != null) {
                    JProfile.this.room_list_callback.error();
                }
            }
        };
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", server).putParameter("id", h.getID());
        Node query = new Node("query", "", "http://jabber.org/protocol/disco#items");
        iq.putChild(query);
        putPacketHandler(h);
        this.stream.write(iq, this);
    }

    public final void doAddContact(JContact contact) {
        if (getContactByJID(contact.ID) == null) {
            String params = !contact.name.isEmpty() ? "name='" + xml_utils.encodeString(contact.name) + "'" : "";
            String group = !contact.group.isEmpty() ? "<group>" + xml_utils.encodeString(contact.group) + "</group>" : "";
            XMLPacket packet = new XMLPacket("<iq type='set' id='roster_add'><query xmlns='jabber:iq:roster'><item jid='" + xml_utils.encodeString(contact.ID) + "' " + params + " subscription='none'>" + group + "</item></query></iq>", null);
            this.stream.write(packet, this);
        }
    }

    /** @noinspection unused*/
    public final void doModifyContact(JContact contact) {
        if (getContactByJID(contact.ID) != null) {
            String subscription = contact.subscription == 3 ? "both" : "";
            if (contact.subscription == 1) {
                subscription = "from";
            }
            if (contact.subscription == 2) {
                subscription = "to";
            }
            if (contact.subscription == 0) {
                subscription = "none";
            }
            String params = !contact.name.isEmpty() ? "name='" + xml_utils.encodeString(contact.name) + "'" : "";
            String group = !contact.group.isEmpty() ? "<group>" + xml_utils.encodeString(contact.group) + "</group>" : "";
            XMLPacket packet = new XMLPacket("<iq type='set' id='roster_modify'><query xmlns='jabber:iq:roster'><item jid='" + xml_utils.encodeString(contact.ID) + "' " + params + " subscription='" + subscription + "'>" + group + "</item></query></iq>", null);
            this.stream.write(packet, this);
        }
    }

    public final void doModifyContactRaw(int subscription_, String ID_, String name_, String group_) {
        String subscription = subscription_ == 3 ? "both" : "";
        if (subscription_ == 1) {
            subscription = "from";
        }
        if (subscription_ == 2) {
            subscription = "to";
        }
        if (subscription_ == 0) {
            subscription = "none";
        }
        String params = !name_.isEmpty() ? "name='" + xml_utils.encodeString(name_) + "'" : "";
        String group = !group_.isEmpty() ? "<group>" + xml_utils.encodeString(group_) + "</group>" : "";
        XMLPacket packet = new XMLPacket("<iq type='set' id='roster_modify'><query xmlns='jabber:iq:roster'><item jid='" + xml_utils.encodeString(ID_) + "' " + params + " subscription='" + subscription + "'>" + group + "</item></query></iq>", null);
        this.stream.write(packet, this);
    }

    public final void doDeleteContact(JContact contact) {
        XMLPacket packet;
        if (contact.name != null && !contact.name.isEmpty()) {
            packet = new XMLPacket("<iq type='set' id='roster_modify'><query xmlns='jabber:iq:roster'><item jid='" + xml_utils.encodeString(contact.ID) + "' name='" + xml_utils.encodeString(contact.name) + "' subscription='remove'><group>" + contact.group + "</group></item></query></iq>", null);
        } else {
            packet = new XMLPacket("<iq type='set' id='roster_modify'><query xmlns='jabber:iq:roster'><item jid='" + xml_utils.encodeString(contact.ID) + "' subscription='remove'><group>" + xml_utils.encodeString(contact.group) + "</group></item></query></iq>", null);
        }
        this.stream.write(packet, this);
    }

    public final void doLocalDeleteContact(JContact contact) {
        closeChat(contact);
        Conference conf = getConference(JProtocol.getJIDFromFullID(contact.ID));
        if (conf != null) {
            conf.unregisterPMContact(contact);
        }
        removeContactByJID(contact.ID);
        this.svc.removeMessageNotify(contact);
        sortContactList();
        remakeContactList();
    }

    public final void doAddGroup(JGroup group) {
        synchronized (ContactsAdapter.locker) {
            if (getGroup(group.name) == null) {
                this.contacts.add(group);
                sortContactList();
                saveRoster();
                remakeContactList();
            }
        }
    }

    private void startPingTask() {
        this.PING_TASK = new PendingIntentHandler() {
            @Override
            public void run() {
                JProfile.this.svc.removeTimedTask(JProfile.this.PING_TASK);
                if (JProfile.this.ping_answer_received) {
                    if (JProfile.this.connected) {
                        JProfile.this.svc.attachTimedTask(JProfile.this.PING_TASK, PreferenceTable.ping_freq * 1000L);
                        JProfile.this.sendPingPacket();
                    }
                    JProfile.this.ping_answer_received = false;
                    return;
                }
                JProfile.this.handleConnectionLosted();
            }
        };
        if (PreferenceTable.use_ping) {
            this.PING_TASK.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void parseRoster(Node node) {
        Log.e(getClass().getSimpleName(), "Parsing start (Thread: " + Thread.currentThread().getName() + ")");
        Vector<ContactlistItem> updated = new Vector<>();
        Vector<String> groups = new Vector<>();
        updated.add(JProtocol.makeWOGroup(this));
        updated.addAll(getPMContacts());
        Node query = node.findFirstLocalNodeByName("query");
        String group_ = null;
        for (Node n : query.childs) {
            Node group = n.findFirstLocalNodeByName("group");
            if (group != null) {
                group_ = group.getValue();
                JGroup jgroup = getGroup(group_);
                if (jgroup == null) {
                    if (!group_.isEmpty()) {
                        JGroup jgroup2 = new JGroup(this, group_);
                        if (!groups.contains(group_)) {
                            groups.add(group_);
                            updated.add(jgroup2);
                        }
                    }
                } else if (!groups.contains(group_)) {
                    groups.add(group_);
                    updated.add(jgroup);
                }
            }
            String name = n.getParameterWODecode("name");
            String jid = JProtocol.lowerCaseFullJID(n.getParameterWODecode("jid"));
            Log.e("Roster", jid);
            String subscript = n.getParameterWODecode("subscription");
            if (name == null) {
                name = jid;
            }
            JContact contact = getContactByJID(jid);
            if (contact == null) {
                contact = new JContact(this, jid);
            }
            contact.subscription = 0;
            boolean subscription_detected = false;
            if (jid.equals(this.ID + "@" + this.host)) {
                contact.subscription = 3;
                subscription_detected = true;
            }
            if (!subscription_detected && subscript.equals("none")) {
                //noinspection DataFlowIssue
                contact.subscription = 0;
                subscription_detected = true;
            }
            if (!subscription_detected && subscript.equals("from")) {
                contact.subscription = 1;
                subscription_detected = true;
            }
            if (!subscription_detected && subscript.equals("to")) {
                contact.subscription = 2;
                subscription_detected = true;
            }
            if (!subscription_detected && subscript.equals("both")) {
                contact.subscription = 3;
            }
            contact.name = name;
            contact.group = group == null ? "" : group_;
            updated.add(contact);
        }
        synchronized (ContactsAdapter.locker) {
            this.contacts = updated;
            Log.e(getClass().getSimpleName(), "Parsing end");
            sortContactList();
            saveRoster();
            Log.e(getClass().getSimpleName(), "Sorting end");
        }
        this.roster_received = true;
        checkMessagesBuffer();
        checkPresencesBuffer();
    }

    private void handleAuthAsk(String user) {
        JContact contact = null;
        String jid = JProtocol.getJIDFromFullID(user);
        JContact contact2 = getContactByJID(jid);
        synchronized (ContactsAdapter.locker) {
            if (contact2 == null) {
                //noinspection CatchMayIgnoreException
                try {
                    Log.e("JABBER", "Adding " + jid);
                    contact = new JContact(this, jid);
                } catch (Throwable th) {
                    //noinspection AssignmentToCatchBlockParameter,UnusedAssignment,SillyAssignment,DataFlowIssue
                    th = th;
                }
                try {
                    //noinspection DataFlowIssue
                    contact.name = jid;
                    doAddContact(contact);
                    this.contacts.add(contact);
                    sortContactList();
                    saveRoster();
                    contact2 = contact;
                } catch (Throwable th2) {
                    ///th = th2;
                    try {
                        throw th2;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (contact2.isChating) {
                handleMessage(contact2, Locale.getString("s_jabber_inconing_accepted_auth"), 1, System.currentTimeMillis());
            }
            XMLPacket packet = new XMLPacket("<presence type='subscribed' to='" + user + "'/>", null);
            this.stream.write(packet, this);
            doRequestAuth(contact2);
        }
    }

    private void handleAuthAllowed(String user) {
        JContact contact = getContactByJID(JProtocol.getJIDFromFullID(user));
        if (contact != null) {
            if (contact.isChating) {
                handleMessage(contact, Locale.getString("s_jabber_authorization_accepted"), 1, System.currentTimeMillis());
            }
            this.svc.playEvent(1);
        }
    }

    private void handleAuthDenied(String user) {
        JContact contact = getContactByJID(JProtocol.getJIDFromFullID(user));
        if (contact != null) {
            if (contact.isChating) {
                handleMessage(contact, Locale.getString("s_jabber_authorization_rejected"), 0, System.currentTimeMillis());
            }
            contact.subscription = 0;
            this.svc.playEvent(2);
        }
    }

    public final void doRequestAuth(JContact contact) {
        XMLPacket packet = new XMLPacket("<presence type='subscribe' to='" + xml_utils.encodeString(contact.ID) + "'/>", null);
        this.stream.write(packet, this);
    }

    private void userOnline(String user, String status, String desc, int priority, String client) {
        JContact contact = getContactByJID(JProtocol.getJIDFromFullID(user));
        String resource = JProtocol.getResourceFromFullID(user);
        //noinspection ConstantValue
        if (resource == null) {
            resource = resources.getString("s_jabber_no_resource");
        }
        if (contact != null) {
            contact.mNeedLastOnlineTime = false;
            boolean is_online = contact.isOnline();
            int sts = JProtocol.parseStatus(status);
            contact.setResource(resource, sts, priority, client, xml_utils.decodeString(desc));
            boolean now_is_online = contact.isOnline();
            if (!is_online && now_is_online) {
                if (!contact.presence_initialized) {
                    contact.presence_initialized = true;
                } else {
                    contact.requestBlink();
                    if (PreferenceTable.log_online) {
                        jasminSvc.pla.put(utilities.match(resources.getString("s_icq_contact_online"), new String[]{contact.name}), null, resources.getXMPPStatusIconFull(contact), null, popup_log_adapter.PRESENSE_DISPLAY_TIME, null);
                        this.svc.put_log(this.nickname + ":\n" + utilities.match(resources.getString("s_icq_contact_online"), new String[]{contact.name}));
                    }
                    this.svc.playEvent(4);
                }
            }
            if (JChatActivity.is_any_chat_opened && JChatActivity.contact == contact) {
                this.svc.handleChatUpdateInfo();
            }
            remakeContactList();
        }
    }

    private void userOffline(String user) {
        JContact contact = getContactByJID(JProtocol.getJIDFromFullID(user));
        String resource = JProtocol.getResourceFromFullID(user);
        //noinspection ConstantValue
        if (resource == null) {
            resource = resources.getString("s_jabber_no_resource");
        }
        if (contact != null) {
            contact.resetBlink();
            contact.mNeedLastOnlineTime = true;
            boolean is_online = contact.isOnline();
            contact.deleteResource(resource);
            boolean now_is_online = contact.isOnline();
            if (is_online && !now_is_online) {
                if (PreferenceTable.auto_close_chat && contact.isChating && !contact.hasUnreadMessages) {
                    closeChat(contact);
                }
                if (PreferenceTable.log_offline) {
                    jasminSvc.pla.put(utilities.match(resources.getString("s_icq_contact_offline"), new String[]{contact.name}), null, resources.getXMPPStatusIconFull(contact), null, popup_log_adapter.PRESENSE_DISPLAY_TIME, null);
                    this.svc.put_log(this.nickname + ":\n" + utilities.match(resources.getString("s_icq_contact_offline"), new String[]{contact.name}));
                }
                this.svc.playEvent(5);
            }
            if (JChatActivity.is_any_chat_opened && JChatActivity.contact == contact) {
                this.svc.handleChatUpdateInfo();
            }
            remakeContactList();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void handleStreamFailure(Node data) {
        String description = resources.getString("s_jabber_connection_error");
        String content = xml_utils.getTagContent(data.compile(), "failure");
        if (content != null) {
            if (content.indexOf("temporary-auth-failure") > 0) {
                description = resources.getString("s_jabber_stream_error_1");
            }
            if (content.indexOf("not-authorized") > 0) {
                description = resources.getString("s_jabber_stream_error_2");
            }
            if (content.indexOf("bad-format") > 0) {
                description = resources.getString("s_jabber_stream_error_3");
            }
            if (content.contains("bad-namespace-prefix")) {
                description = resources.getString("s_jabber_stream_error_4");
            }
            if (content.indexOf("conflict") > 0) {
                description = resources.getString("s_jabber_stream_error_5");
            }
            if (content.indexOf("connection-timeout") > 0) {
                description = resources.getString("s_jabber_stream_error_6");
            }
            if (content.indexOf("host-gone") > 0) {
                description = resources.getString("s_jabber_stream_error_7");
            }
            if (content.indexOf("host-unknown") > 0) {
                description = resources.getString("s_jabber_stream_error_8");
            }
            if (content.indexOf("improper-addressing") > 0) {
                description = resources.getString("s_jabber_stream_error_9");
            }
            if (content.indexOf("internal-server-error") > 0) {
                description = resources.getString("s_jabber_stream_error_10");
            }
            if (content.indexOf("invalid-from") > 0) {
                description = resources.getString("s_jabber_stream_error_11");
            }
            if (content.indexOf("invalid-id") > 0) {
                description = resources.getString("s_jabber_stream_error_12");
            }
            if (content.indexOf("invalid-namespace") > 0) {
                description = resources.getString("s_jabber_stream_error_13");
            }
            if (content.indexOf("invalid-xml") > 0) {
                description = resources.getString("s_jabber_stream_error_14");
            }
            if (content.indexOf("policy-violation") > 0) {
                description = resources.getString("s_jabber_stream_error_15");
            }
            if (content.indexOf("remote-connection-failed") > 0) {
                description = resources.getString("s_jabber_stream_error_16");
            }
            if (content.indexOf("resource-constraint") > 0) {
                description = resources.getString("s_jabber_stream_error_17");
            }
            if (content.indexOf("restricted-xml") > 0) {
                description = resources.getString("s_jabber_stream_error_18");
            }
            if (content.indexOf("see-other-host") > 0) {
                description = resources.getString("s_jabber_stream_error_19");
            }
            if (content.indexOf("system-shutdown") > 0) {
                description = resources.getString("s_jabber_stream_error_20");
            }
            if (content.indexOf("undefined-condition") > 0) {
                description = resources.getString("s_jabber_stream_error_21");
            }
            if (content.indexOf("unsupported-encoding") > 0) {
                description = resources.getString("s_jabber_stream_error_22");
            }
            if (content.indexOf("unsupported-stanza-type") > 0) {
                description = resources.getString("s_jabber_stream_error_23");
            }
            if (content.indexOf("unsupported-version") > 0) {
                description = resources.getString("s_jabber_stream_error_24");
            }
            if (content.indexOf("xml-not-well-formed") > 0) {
                description = resources.getString("s_jabber_stream_error_25");
            }
        }
        jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_xml_stream_error"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_xml_stream_error") + ":\n" + description);
        this.svc.showMessageInContactList(this.ID + "@" + this.host, resources.getString("s_jabber_xml_stream_error") + ":\n" + description);
        this.stream.disconnect();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void handleStreamError(Node data) {
        String description = resources.getString("s_jabber_connection_error");
        Node text = data.findFirstNodeByName("text");
        if (data.findFirstNodeByName("temporary-auth-failure") != null) {
            description = resources.getString("s_jabber_stream_error_1");
        }
        if (data.findFirstNodeByName("not-authorized") != null) {
            description = resources.getString("s_jabber_stream_error_2");
        }
        if (data.findFirstNodeByName("bad-format") != null) {
            description = resources.getString("s_jabber_stream_error_3");
        }
        if (data.findFirstNodeByName("bad-namespace-prefix") != null) {
            description = resources.getString("s_jabber_stream_error_4");
        }
        if (data.findFirstNodeByName("conflict") != null) {
            description = resources.getString("s_jabber_stream_error_5");
        }
        if (data.findFirstNodeByName("connection-timeout") != null) {
            description = resources.getString("s_jabber_stream_error_6");
        }
        if (data.findFirstNodeByName("host-gone") != null) {
            description = resources.getString("s_jabber_stream_error_7");
        }
        if (data.findFirstNodeByName("host-unknown") != null) {
            description = resources.getString("s_jabber_stream_error_8");
        }
        if (data.findFirstNodeByName("improper-addressing") != null) {
            description = resources.getString("s_jabber_stream_error_9");
        }
        if (data.findFirstNodeByName("internal-server-error") != null) {
            description = resources.getString("s_jabber_stream_error_10");
        }
        if (data.findFirstNodeByName("invalid-from") != null) {
            description = resources.getString("s_jabber_stream_error_11");
        }
        if (data.findFirstNodeByName("invalid-id") != null) {
            description = resources.getString("s_jabber_stream_error_12");
        }
        if (data.findFirstNodeByName("invalid-namespace") != null) {
            description = resources.getString("s_jabber_stream_error_13");
        }
        if (data.findFirstNodeByName("invalid-xml") != null) {
            description = resources.getString("s_jabber_stream_error_14");
        }
        if (data.findFirstNodeByName("policy-violation") != null) {
            description = resources.getString("s_jabber_stream_error_15");
        }
        if (data.findFirstNodeByName("remote-connection-failed") != null) {
            description = resources.getString("s_jabber_stream_error_16");
        }
        if (data.findFirstNodeByName("resource-constraint") != null) {
            description = resources.getString("s_jabber_stream_error_17");
        }
        if (data.findFirstNodeByName("restricted-xml") != null) {
            description = resources.getString("s_jabber_stream_error_18");
        }
        if (data.findFirstNodeByName("see-other-host") != null) {
            description = resources.getString("s_jabber_stream_error_19");
        }
        if (data.findFirstNodeByName("system-shutdown") != null) {
            description = resources.getString("s_jabber_stream_error_20");
        }
        if (data.findFirstNodeByName("undefined-condition") != null) {
            description = resources.getString("s_jabber_stream_error_21");
        }
        if (data.findFirstNodeByName("unsupported-encoding") != null) {
            description = resources.getString("s_jabber_stream_error_22");
        }
        if (data.findFirstNodeByName("unsupported-stanza-type") != null) {
            description = resources.getString("s_jabber_stream_error_23");
        }
        if (data.findFirstNodeByName("unsupported-version") != null) {
            description = resources.getString("s_jabber_stream_error_24");
        }
        if (data.findFirstNodeByName("xml-not-well-formed") != null) {
            description = resources.getString("s_jabber_stream_error_25");
        }
        if (text != null && !text.getValue().isEmpty()) {
            description = description + "\n\n" + text.getValue();
        }
        jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_xml_stream_error"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_xml_stream_error") + ":\n" + description);
        this.svc.showMessageInContactList(this.ID + "@" + this.host, resources.getString("s_jabber_xml_stream_error") + ":\n" + description);
        this.stream.disconnect();
    }

    public final void getCommandList(String server, final Callback callback) {
        PacketHandler handler = new PacketHandler(true) { // from class: ru.ivansuper.jasmin.jabber.JProfile.21
            @Override // ru.ivansuper.jasmin.jabber.PacketHandler
            public void execute() {
                Node stanzas = this.slot;
                Node query = stanzas.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/disco#items");
                if (query == null) {
                    callback.onListLoaded(new Vector<>());
                    return;
                }
                Vector<Node> items = query.findLocalNodesByName("item");
                if (items.isEmpty()) {
                    callback.onListLoaded(new Vector<>());
                    return;
                }
                Vector<CommandItem> list = new Vector<>();
                for (Node n : items) {
                    String jid = JProtocol.lowerCaseFullJID(n.getParameter("jid"));
                    String node = n.getParameter("node");
                    String name = n.getParameter("name");
                    if (name == null) {
                        name = jid;
                    }
                    list.add(new CommandItem(jid, node, name));
                }
                callback.onListLoaded(list);
            }
        };
        putPacketHandler(handler);
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", server).putParameter("id", handler.getID());
        Node query = new Node("query", "", "http://jabber.org/protocol/disco#items");
        query.putParameter("node", "http://jabber.org/protocol/commands");
        iq.putChild(query);
        this.stream.write(iq, this);
    }

    public final void executeCommand(String server, String command_node) {
        Node iq = new Node("iq");
        iq.putParameter("type", "set").putParameter("to", server).putParameter("id", "cmd" + System.currentTimeMillis()).putParameter("xml:lang", Locale.getCurrentLangCode());
        Node command = new Node("command", "", "http://jabber.org/protocol/commands");
        command.putParameter("node", command_node);
        command.putParameter("action", "execute");
        iq.putChild(command);
        this.stream.write(iq, this);
    }

    public final void launchRegistration(String server) {
        PacketHandler h = new PacketHandler(false) {
            @Override
            public void execute() {
                Node stanzas = this.slot;
                final String FROM = JProtocol.lowerCaseFullJID(stanzas.getParameter("from"));
                final String PID = stanzas.getParameter("id");
                final Node reg_query = stanzas.findFirstLocalNodeByNameAndNamespace("query", "jabber:iq:register");
                if (reg_query != null) {
                    Node xdata = reg_query.findFirstLocalNodeByNameAndNamespace("x", "jabber:x:data");
                    if (xdata != null) {
                        JProfile.this.proceedXForm(FROM, PID, reg_query);
                        return;
                    }
                    final ClassicForm cform = new ClassicForm();
                    JProfile.this.svc.runOnUi(() -> {
                        cform.build(reg_query, FROM, PID, Locale.getString("s_do_register"), JProfile.this);
                        JProfile.this.svc.showXFormInContactList(cform);
                    });
                    PacketHandler res_h = new PacketHandler(PID, false) {
                        @Override
                        public void execute() {
                            Node stanzas2 = this.slot;
                            String type = stanzas2.getParameter("type");
                            if (type == null || type.equals("result")) {
                                JProfile.this.svc.showMessageInContactList(FROM, Locale.getString("s_registration_success"));
                            } else {
                                JProfile.this.svc.showMessageInContactList(FROM, Locale.getString("s_registration_error"));
                            }
                        }
                    };
                    JProfile.this.putPacketHandler(res_h);
                }
            }
        };
        putPacketHandler(h);
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", server).putParameter("id", h.getID());
        Node query = new Node("query", "", "jabber:iq:register");
        iq.putChild(query);
        this.stream.write(iq, this);
    }

    public final void cancelRegistration(final String server) {
        PacketHandler res_h = new PacketHandler(false) { // from class: ru.ivansuper.jasmin.jabber.JProfile.23
            @Override // ru.ivansuper.jasmin.jabber.PacketHandler
            public void execute() {
                Node stanzas = this.slot;
                String type = stanzas.getParameter("type");
                if (type == null || type.equals("result")) {
                    JProfile.this.svc.showMessageInContactList(server, Locale.getString("s_unregistration_success"));
                } else {
                    JProfile.this.svc.showMessageInContactList(server, Locale.getString("s_unregistration_error"));
                }
            }
        };
        putPacketHandler(res_h);
        Node iq = new Node("iq");
        iq.putParameter("type", "set").putParameter("from", getFullJIDWithResource()).putParameter("to", server).putParameter("id", res_h.getID());
        Node query = new Node("query", "", "jabber:iq:register");
        Node remove = new Node("remove");
        query.putChild(remove);
        iq.putChild(query);
        this.stream.write(iq, this);
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void setStatusText(String text) {
        setStatusDescription(text);
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final String getStatusText() {
        return getStatusDescription();
    }

    public final void connect() {
        jasminSvc.pla.put(this.nickname, utilities.match(resources.getString("s_jabber_connecting"), new String[]{this.server, String.valueOf(this.port)}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
        this.svc.put_log(this.nickname + ":\n" + utilities.match(resources.getString("s_jabber_connecting"), new String[]{this.server, String.valueOf(this.port)}));
        setConnectionStatus(15);
        PacketHandler.task_id = 0L;
        this.messages_seq = 0;
        this.authorized = false;
        this.auth_chlng_received = false;
        this.tls_enabled = false;
        this.compressed = false;
        this.stream.connect(this.server, this.port);
    }

    public final void addConference(String jid, String name, String nick, String pass, boolean connect) {
        Conference conference = new Conference(jid, nick, pass, this);
        this.conference_rooms.add(conference);
        ConferenceItem item = new ConferenceItem();
        item.conference = conference;
        item.ID = conference.JID;
        if (name == null) {
            item.name = JProtocol.getNameFromFullID(item.ID);
        } else {
            item.name = name;
        }
        conference.item = item;
        this.conference_items.add(item);
        if (connect) {
            conference.updatePresence();
        }
        sortContactList();
        saveRoster();
        this.svc.handleContactlistCheckConferences();
        remakeContactList();
    }

    public final Conference getConference(String JID) {
        if (JID == null) {
            return null;
        }
        String JID_ = JProtocol.lowerCaseFullJID(JID);
        for (int i = 0; i < this.conference_rooms.size(); i++) {
            Conference conference = this.conference_rooms.get(i);
            if (conference.JID.equals(JID_)) {
                return conference;
            }
        }
        return null;
    }

    private void sortContactList() {
        Vector<ContactlistItem> temporary = new Vector<>();
        Collections.sort(this.contacts);
        Vector<JGroup> groups = getGroups();
        int i = 0;
        while (!groups.isEmpty()) {
            JGroup group = groups.remove(0);
            temporary.addElement(group);
            Vector<JContact> list = getContactsByGroup(group.name, group.id);
            temporary.addAll(temporary.indexOf(group) + 1, list);
            i = (i - 1) + 1;
        }
        this.contacts.addAll(0, temporary);
        removeConferenceItems();
        Collections.sort(this.conference_items);
        this.contacts.addAll(0, this.conference_items);
    }

    private void removeConferenceItems() {
        synchronized (ContactsAdapter.locker) {
            int i = 0;
            while (i < this.contacts.size()) {
                ContactlistItem item = this.contacts.get(i);
                if (item.itemType == 10) {
                    this.contacts.remove(i);
                    i--;
                }
                i++;
            }
        }
    }

    private void setConferenceItemsOffline(boolean lost) {
        synchronized (ContactsAdapter.locker) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem item = this.contacts.get(i);
                if (item != null && item.itemType == 10) {
                    Conference conference = ((ConferenceItem) item).conference;
                    if (lost && conference.isTurnedOn()) {
                        conference.setIsNormalShutdown(false);
                        //noinspection ConstantValue
                        Log.e("JProfile", "Conference lost (" + lost + ")");
                    } else {
                        conference.setIsNormalShutdown(true);
                        Log.e("JProfile", "Conference deactivated (" + lost + ")");
                    }
                    conference.setAsOffline(lost);
                }
            }
        }
    }

    public final JGroup getGroup(String name) {
        synchronized (ContactsAdapter.locker) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 6 && it.name.equals(name)) {
                    return (JGroup) it;
                }
            }
            return null;
        }
    }

    public final Vector<JGroup> getGroups() {
        Vector<JGroup> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            JGroup without_group = null;
            int i = 0;
            while (i < this.contacts.size()) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 6) {
                    JGroup group = (JGroup) this.contacts.remove(i);
                    if (group.id == -1) {
                        without_group = group;
                    } else {
                        list.add(group);
                    }
                    i--;
                }
                i++;
            }
            Collections.sort(list);
            list.add(without_group);
        }
        return list;
    }

    public final Vector<JGroup> getGroupsA() {
        Vector<JGroup> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 6) {
                    JGroup group = (JGroup) this.contacts.get(i);
                    if (group.id != -1) {
                        list.add(group);
                    }
                }
            }
            Collections.sort(list);
        }
        return list;
    }

    public final JContact getContactByJID(String JID) {
        JContact jContact;
        if (JID == null) {
            return null;
        }
        String JID_ = JProtocol.lowerCaseFullJID(JID);
        synchronized (ContactsAdapter.locker) {
            int i = 0;
            while (true) {
                if (i < this.contacts.size()) {
                    ContactlistItem it = this.contacts.get(i);
                    if (it.itemType == 4 && it.ID.equals(JID_)) {
                        jContact = (JContact) it;
                        break;
                    }
                    i++;
                } else {
                    jContact = null;
                    break;
                }
            }
        }
        return jContact;
    }

    public final void removeContactByJID(String JID) {
        if (JID != null) {
            String JID_ = JProtocol.lowerCaseFullJID(JID);
            synchronized (ContactsAdapter.locker) {
                int i = 0;
                while (true) {
                    if (i < this.contacts.size()) {
                        ContactlistItem it = this.contacts.get(i);
                        if (it.itemType != 4 || !it.ID.equals(JID_)) {
                            i++;
                        } else {
                            this.svc.removeMessageNotify(this.contacts.remove(i));
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public final Vector<JContact> getContactsByGroup(String name, int id) {
        Vector<JContact> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            int i = 0;
            while (i < this.contacts.size()) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 4) {
                    JContact contact = (JContact) it;
                    if (contact.group.equals(name)) {
                        this.contacts.remove(i);
                        i--;
                        list.addElement(contact);
                    } else if (contact.group.isEmpty() && id == -1) {
                        this.contacts.remove(i);
                        i--;
                        list.addElement(contact);
                    }
                }
                i++;
            }
        }
        return list;
    }

    public final void startConnectingChosed() {
        if (this.rcn.is_active) {
            this.rcn.stop();
        }
        startConnecting();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void startConnecting() {
        if (!this.connected && !this.connecting) {
            connect();
            this.connecting = true;
            notifyStatusIcon();
            refreshContactList();
        }
    }

    public final void openChat(JContact jcontact) {
        this.svc.opened_chats.add(jcontact);
        jcontact.isChating = true;
        remakeContactList();
        this.svc.rebuildChatMarkers();
    }

    public final void closeChat(JContact jcontact) {
        this.svc.removeFromOpenedChats(jcontact.ID);
        jcontact.isChating = false;
        jcontact.clearPreloadedHistory();
        remakeContactList();
        this.svc.rebuildChatMarkers();
    }

    /** @noinspection unused*/
    public final void sendTypingNotify(String ID, int i) {
        XMLPacket packet = null;
        switch (i) {
            case 0:
                packet = new XMLPacket("<message type='chat' to='" + xml_utils.encodeString(ID) + "' id='typing_" + System.currentTimeMillis() + "'><paused xmlns='http://jabber.org/protocol/chatstates'/></message>", null);
                break;
            case 1:
                packet = new XMLPacket("<message type='chat' to='" + xml_utils.encodeString(ID) + "' id='typing_" + System.currentTimeMillis() + "'><composing xmlns='http://jabber.org/protocol/chatstates'/></message>", null);
                break;
        }
        this.stream.write(packet, this);
    }

    public final void sendDiscoRequest(String server, String xml_node, PacketHandler handler, boolean info) {
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", server).putParameter("id", handler.getID()).putParameter("xml:lang", Locale.getCurrentLangCode());
        Node query = new Node("query", "", "http://jabber.org/protocol/disco#" + (info ? "info" : "items"));
        if (xml_node != null && !xml_node.trim().isEmpty()) {
            query.putParameter("node", xml_node);
        }
        iq.putChild(query);
        this.handlers.add(handler);
        this.stream.write(iq, this);
    }

    @Override
    public final void closeAllChats() {
        int i = 0;
        while (i < this.svc.opened_chats.size()) {
            ContactlistItem contact = this.svc.opened_chats.get(i);
            if (contact.itemType == 4 && ((JContact) contact).profile.equals(this)) {
                closeChat((JContact) contact);
                i--;
            }
            i++;
        }
    }

    /** @noinspection unused*/
    private void setAllContactsOffline(boolean user, boolean lost) {
        synchronized (ContactsAdapter.locker) {
            setConferenceItemsOffline(lost);
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem item = this.contacts.get(i);
                if (item.itemType == 4) {
                    JContact contact = (JContact) item;
                    contact.presence_initialized = false;
                    contact.clearResources();
                    contact.typing = false;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setAllContactsPresenceInitialized() {
        synchronized (ContactsAdapter.locker) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem item = this.contacts.get(i);
                if (item.itemType == 4) {
                    JContact contact = (JContact) item;
                    contact.presence_initialized = true;
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    private void checkConferencesForReconnect() {
        if (PreferenceTable.ms_rejoin_to_conferences) {
            for (int i = 0; i < this.conference_rooms.size(); i++) {
                Conference conf = this.conference_rooms.get(i);
                Log.e("checkConferencesForReconnect", "NormalShutdown: " + conf.isNormalShutdown() + "     TurnedOn: " + conf.isTurnedOn());
                if (!conf.isNormalShutdown() && conf.isTurnedOn()) {
                    conf.updatePresence();
                }
            }
        }
    }

    public final void updateAvatar(File file, final Dialog progress) {
        boolean z = false;
        Bitmap avatar = Avatar.normalizeAvatar(file);
        if (avatar == null) {
            this.svc.showToast(Locale.getString("s_change_avatar_invalid_image"), 1);
            return;
        }
        PacketHandler h = new PacketHandler(z) { // from class: ru.ivansuper.jasmin.jabber.JProfile.24
            @Override // ru.ivansuper.jasmin.jabber.PacketHandler
            public void execute() {
                Node stanzas = this.slot;
                String type = stanzas.getParameter("type");
                if (type == null) {
                    type = "";
                }
                if (type.equals("result")) {
                    JProfile.this.svc.showToast(Locale.getString("s_change_avatar_success"), 1);
                } else {
                    JProfile.this.svc.showToast(Locale.getString("s_change_avatar_error_2"), 1);
                }
                progress.dismiss();
            }
        };
        putPacketHandler(h);
        Node iq = new Node("iq");
        iq.putParameter("type", "set").putParameter("id", h.getID());
        iq.putChild(this.my_vcard.compile(avatar));
        this.stream.write(iq, this);
    }

    public final void updateVCard(final Dialog progress) {
        PacketHandler h = new PacketHandler(false) { // from class: ru.ivansuper.jasmin.jabber.JProfile.25
            @Override // ru.ivansuper.jasmin.jabber.PacketHandler
            public void execute() {
                Node stanzas = this.slot;
                String type = stanzas.getParameter("type");
                if (type == null) {
                    type = "";
                }
                if (type.equals("result")) {
                    JProfile.this.svc.showToast(Locale.getString("s_edit_vcard_success"), 1);
                    JProfile.this.my_vcard.fillFromTemp();
                } else {
                    JProfile.this.svc.showToast(Locale.getString("s_edit_vcard_error"), 1);
                }
                progress.dismiss();
            }
        };
        putPacketHandler(h);
        Node iq = new Node("iq");
        iq.putParameter("type", "set").putParameter("id", h.getID());
        iq.putChild(this.my_vcard.compileTemp(this.my_vcard.avatar));
        this.stream.write(iq, this);
    }

    private void doRequestOwnVCard() {
        PacketHandler h = new PacketHandler(false) {
            @Override
            public void execute() {
                Node vcard;
                Node stanzas = this.slot;
                String type = stanzas.getParameter("type");
                if (type == null) {
                    type = "";
                }
                if (!type.equals("error") && (vcard = stanzas.findFirstLocalNodeByNameAndNamespace("vCard", "vcard-temp")) != null) {
                    Bitmap avatar = Avatar.getAvatarHQ(vcard);
                    VCardDecoder.decode(vcard, JProfile.this.my_vcard);
                    JProfile.this.my_vcard.avatar = avatar;
                    String nick = JProfile.this.my_vcard.getEntry(ru.ivansuper.jasmin.jabber.vcard.VCard.Entry.Type.NICKNAME);
                    if (nick != null && !nick.trim().isEmpty()) {
                        JProfile.this.nickname = nick;
                        JProfile.this.svc.profiles.writeProfilesToFile();
                    }
                }
            }
        };
        putPacketHandler(h);
        sendVCardRequest(h.getID(), getFullJID());
    }

    private void handleProfileConnected() {
        //noinspection Convert2MethodRef
        this.svc.runOnUi(() -> JProfile.this.setAllContactsPresenceInitialized(), 5000L);
        this.connecting = false;
        this.connected = true;
        dumpServerList();
        checkConferencesForReconnect();
        this.bookmarks.performRequest();
        doRequestOwnVCard();
        startPingTask();
        this.svc.updateNotify();
        this.svc.handleChatUpdateInfo();
        notifyStatusIcon();
        remakeContactList();
        EventTranslator.sendProfilePresence(this);
    }

    /* renamed from: ru.ivansuper.jasmin.jabber.JProfile$28, reason: invalid class name */
    class AnonymousClass28 extends PacketHandler {
        AnonymousClass28(boolean $anonymous0) {
            super($anonymous0);
        }

        @Override
        public void execute() {
            boolean z = false;
            Node query_ = this.slot.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/disco#items");
            Vector<Node> servers = query_.findLocalNodesByName("item");
            for (Node n : servers) {
                PacketHandler h = new PacketHandler(z) {
                    @Override
                    public void execute() {
                        Node query = this.slot.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/disco#info");
                        final String srv_name = JProtocol.lowerCaseFullJID(this.slot.getParameter("from"));
                        if (query != null) {
                            Vector<Node> identities = query.findLocalNodesByName("identity");
                            for (Node n2 : identities) {
                                try {
                                    if (n2.getParameter("category").equals("proxy") && n2.getParameter("type").equals("bytestreams")) {
                                        PacketHandler h2 = new PacketHandler(false) {
                                            @Override
                                            public void execute() {
                                                Node q = this.slot.findFirstLocalNodeByNameAndNamespace("query", SOCKS5Controller.NAMESPACE);
                                                try {
                                                    Node stream = q.findFirstLocalNodeByName("streamhost");
                                                    JProfile.this.server_list.put(srv_name, ServerList.Type.PROXY, stream.getParameter("host"), Integer.parseInt(stream.getParameter("port")));
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        };
                                        JProfile.this.putPacketHandler(h2);
                                        Node iq = new Node("iq");
                                        iq.putParameter("from", JProfile.this.getFullJIDWithResource()).putParameter("to", srv_name).putParameter("type", "get").putParameter("id", h2.getID());
                                        Node q = new Node("query", "", SOCKS5Controller.NAMESPACE);
                                        iq.putChild(q);
                                        JProfile.this.stream.write(iq, JProfile.this);
                                        return;
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            JProfile.this.server_list.put(JProtocol.lowerCaseFullJID(this.slot.getParameter("from")), ServerList.Type.OTHER);
                        }
                    }
                };
                if (JProtocol.lowerCaseFullJID(n.getParameter("jid")).contains("proxy")) {
                    PacketHandler h1 = new PacketHandler(z) {
                        @Override
                        public void execute() {
                            if (!this.slot.getParameterSafe("type").equals("error")) {
                                String srv_name = JProtocol.lowerCaseFullJID(this.slot.getParameter("from"));
                                Node q = this.slot.findFirstLocalNodeByNameAndNamespace("query", SOCKS5Controller.NAMESPACE);
                                Node stream = q.findFirstLocalNodeByName("streamhost");
                                Log.e("JProfile", "PROXY INFO FOUND! " + srv_name);
                                if (stream != null) {
                                    JProfile.this.server_list.put(srv_name, ServerList.Type.PROXY, stream.getParameter("host"), Integer.parseInt(stream.getParameter("port")));
                                }
                            }
                        }
                    };
                    JProfile.this.putPacketHandler(h1);
                    Node iq = new Node("iq");
                    iq.putParameter("from", JProfile.this.getFullJIDWithResource()).putParameter("to", JProtocol.lowerCaseFullJID(n.getParameter("jid"))).putParameter("type", "get").putParameter("id", h1.getID());
                    Node q = new Node("query", "", SOCKS5Controller.NAMESPACE);
                    iq.putChild(q);
                    JProfile.this.stream.write(iq, JProfile.this);
                } else {
                    JProfile.this.putPacketHandler(h);
                    Node iq2 = new Node("iq");
                    iq2.putParameter("to", JProtocol.lowerCaseFullJID(n.getParameter("jid"))).putParameter("type", "get").putParameter("id", h.getID());
                    Node query = new Node("query", "", "http://jabber.org/protocol/disco#info");
                    iq2.putChild(query);
                    JProfile.this.stream.write(iq2, JProfile.this);
                }
            }
        }
    }

    private void dumpServerList() {
        PacketHandler h = new AnonymousClass28(false);
        putPacketHandler(h);
        Node iq = new Node("iq");
        iq.putParameter("to", this.host).putParameter("type", "get").putParameter("id", h.getID());
        Node query = new Node("query", "", "http://jabber.org/protocol/disco#items");
        iq.putChild(query);
        this.stream.write(iq, this);
    }

    private void handleConnectionLosted() {
        this.stream.disconnect();
        if (this.connecting || this.connected) {
            jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_connection_losted"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_connection_losted"));
        }
        if (!this.rcn.is_active) {
            this.rcn.start();
        }
        handleDisconnected(false, true);
        this.svc.updateNotify();
        this.svc.handleChatUpdateInfo();
        notifyStatusIcon();
        remakeContactList();
    }

    private void handleDisconnected(boolean user, boolean lost) {
        setConnectionStatus(0);
        if (this.connecting || this.connected) {
            jasminSvc.pla.put(this.nickname, resources.getString("s_jabber_disconnected"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
            this.svc.put_log(this.nickname + ":\n" + resources.getString("s_jabber_disconnected"));
        }
        this.connecting = false;
        this.connected = false;
        this.roster_received = false;
        this.server_list.clear();
        this.handlers.clear();
        this.conferences.clear();
        this.buffered_messages.clear();
        this.buffered_presences.clear();
        this.svc.removeTimedTask(this.PING_TASK);
        EventTranslator.sendProfilePresence(this);
        setAllContactsOffline(user, lost);
        this.svc.updateNotify();
        this.svc.handleChatUpdateInfo();
        notifyStatusIcon();
        remakeContactList();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void disconnect() {
        disconnectInternal(true, false);
    }

    public final void disconnectInternal(boolean user, boolean lost) {
        this.status = -1;
        if (this.rcn.is_active) {
            this.rcn.stop();
            handleDisconnected(lost, user);
        }
        this.stream.disconnect();
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void handleScreenTurnedOff() {
        if (!this.screen_ctrlr.is_active && PreferenceTable.auto_change_status && this.type == 0) {
            this.screen_ctrlr.start();
        }
    }

    @Override // ru.ivansuper.jasmin.protocols.IMProfile
    public final void handleScreenTurnedOn() {
        if (this.screen_ctrlr.status_changed) {
            sendPresence();
        }
        this.screen_ctrlr.stop();
    }

    public final JContact getContact(String JID) {
        synchronized (ContactsAdapter.locker) {
            for (ContactlistItem item : this.contacts) {
                if (item.itemType == 4 && item.ID.equals(JID)) {
                    return (JContact) item;
                }
            }
            return null;
        }
    }

    public final Vector<ContactlistItem> getContacts() {
        Vector<ContactlistItem> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem item : this.contacts) {
                if (item.itemType == 4) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    public final Vector<JContact> getPMContacts() {
        Vector<JContact> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem item : this.contacts) {
                if (item.itemType == 4 && ((JContact) item).conf_pm) {
                    list.add((JContact) item);
                }
            }
        }
        return list;
    }

    /** @noinspection unused*/
    public final Vector<JContact> getContactsCasted() {
        Vector<JContact> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem item : this.contacts) {
                if (item.itemType == 4) {
                    list.add((JContact) item);
                }
            }
        }
        return list;
    }

    public final boolean isAnyChatOpened() {
        synchronized (ContactsAdapter.locker) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem item = this.contacts.get(i);
                if (item.itemType == 4 && ((JContact) item).isChating) {
                    return true;
                }
            }
            return false;
        }
    }

    /** @noinspection unused*/
    public final boolean isAnyConferenceOpened() {
        synchronized (ContactsAdapter.locker) {
            for (int i = 0; i < this.conference_items.size(); i++) {
                ConferenceItem item = this.conference_items.get(i);
                if (item.conference.isOnline()) {
                    return true;
                }
            }
            return false;
        }
    }

    public final Vector<JContact> getOnlyContacts() {
        Vector<JContact> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem item : this.contacts) {
                if (item.itemType == 4) {
                    list.add((JContact) item);
                }
            }
        }
        return list;
    }

    public final void saveRoster() {
        synchronized (ContactsAdapter.locker) {
            DataOutputStream dos = null;
            try {
                try {
                    //noinspection IOStreamConstructor
                    DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(resources.dataPath + this.ID + "@" + this.host + "/roster.bin"));
                    try {
                        for (ContactlistItem item : this.contacts) {
                            switch (item.itemType) {
                                case 4:
                                    JContact contact = (JContact) item;
                                    if (!contact.conf_pm) {
                                        dos2.write(4);
                                        dos2.writeUTF(contact.ID);
                                        dos2.writeUTF(contact.name);
                                        dos2.writeUTF(contact.group);
                                    }
                                    break;
                                case 6:
                                    JGroup jgroup = (JGroup) item;
                                    if (jgroup.id != -1) {
                                        dos2.write(6);
                                        dos2.writeUTF(jgroup.name);
                                    }
                                    break;
                            }
                        }
                        for (ConferenceItem conf : this.conference_items) {
                            dos2.write(10);
                            dos2.writeUTF(conf.conference.JID);
                            dos2.writeUTF(conf.conference.nick);
                            dos2.writeUTF(conf.conference.pass);
                        }
                        dos = dos2;
                    } catch (Exception e) {
                        //noinspection AssignmentToCatchBlockParameter,SillyAssignment,DataFlowIssue
                        e = e;
                        dos = dos2;
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        dos.close();
                    } catch (Throwable th) {
                        //noinspection AssignmentToCatchBlockParameter,SillyAssignment,DataFlowIssue
                        th = th;
                        throw th;
                    }
                } catch (Exception e2) {
                    ///e = e2;
                }
                try {
                    //noinspection DataFlowIssue
                    dos.close();
                } catch (Exception e3) {
                    //noinspection CallToPrintStackTrace
                    e3.printStackTrace();
                }
            } catch (Throwable th2) {
                ///th = th2;
                try {
                    throw th2;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void loadRoster() {
        synchronized (ContactsAdapter.locker) {
            DataInputStream dis = null;
            try {
                try {
                    //noinspection IOStreamConstructor
                    DataInputStream dis2 = new DataInputStream(new FileInputStream(resources.dataPath + this.ID + "@" + this.host + "/roster.bin"));
                    try {
                        this.contacts.add(JProtocol.makeWOGroup(this));
                        while (dis2.available() > 0) {
                            int item_type = dis2.read();
                            switch (item_type) {
                                case 4:
                                    String ID = dis2.readUTF();
                                    String nickname = dis2.readUTF();
                                    String group = dis2.readUTF();
                                    JContact contact = new JContact(this, ID);
                                    contact.name = nickname;
                                    contact.group = group;
                                    contact.subscription = 3;
                                    this.contacts.add(contact);
                                    break;
                                case 6:
                                    String name = dis2.readUTF();
                                    JGroup jgroup = new JGroup(this, name);
                                    this.contacts.add(jgroup);
                                    break;
                                case 10:
                                    String JID = dis2.readUTF();
                                    String nick = dis2.readUTF();
                                    String pass = dis2.readUTF();
                                    Conference conference = new Conference(JID, nick, pass, this);
                                    this.conference_rooms.add(conference);
                                    ConferenceItem item = new ConferenceItem();
                                    item.conference = conference;
                                    item.ID = JID;
                                    item.name = JProtocol.getNameFromFullID(JID);
                                    conference.item = item;
                                    this.conference_items.add(item);
                                    break;
                            }
                        }
                        sortContactList();
                        dis = dis2;
                    } catch (Exception e) {
                        //noinspection AssignmentToCatchBlockParameter,SillyAssignment,DataFlowIssue
                        e = e;
                        dis = dis2;
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        dis.close();
                    } catch (Throwable th) {
                        //noinspection AssignmentToCatchBlockParameter,SillyAssignment,DataFlowIssue
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    ///th = th2;
                    try {
                        throw th2;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Exception e2) {
                //e = e2;
            }
            try {
                //noinspection DataFlowIssue
                dis.close();
            } catch (Exception e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
            }
        }
    }

    public final void reinitParams(ProfilesAdapterItem pdata) {
        this.ID = pdata.id.toLowerCase();
        this.host = pdata.host.toLowerCase();
        this.PASS = pdata.pass;
        this.server = pdata.server.toLowerCase();
        this.port = pdata.port;
        if (this.port == 0) {
            this.port = 5222;
        }
        this.autoconnect = pdata.autoconnect;
        this.enabled = pdata.enabled;
        this.use_tls = pdata.tls;
        this.use_sasl = pdata.sasl;
        this.use_compression = pdata.compression;
        if (!this.enabled && this.connected) {
            disconnectInternal(false, false);
        }
    }

    public final void getUnreadMessagesDump(MessagesDump dump) {
        synchronized (ContactsAdapter.locker) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it != null) {
                    if (it.itemType == 4) {
                        JContact contact = (JContact) it;
                        if (contact.hasUnreadMessages) {
                            dump.simple_messages = true;
                            dump.from_contacts++;
                            dump.total_messages += contact.getUnreadCount();
                        }
                    }
                }
            }
            for (Conference conference : this.conference_rooms) {
                if (conference.item.hasUnreadMessages) {
                    dump.conferences = true;
                    dump.from_contacts++;
                    dump.total_messages += conference.item.getUnreadCount();
                }
            }
        }
    }

    public final GroupPresenceInfo getGroupPresenceInfo(JGroup jgroup) {
        GroupPresenceInfo gpi;
        synchronized (ContactsAdapter.locker) {
            gpi = new GroupPresenceInfo();
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it != null) {
                    if (it.itemType == 4) {
                        JContact contact = (JContact) it;
                        if (contact.group.equals(jgroup.name) || (contact.group.isEmpty() && jgroup.id == -1)) {
                            if (!contact.conf_pm) {
                                gpi.total++;
                                if (contact.isOnline()) {
                                    gpi.online++;
                                }
                            }
                            if (PreferenceTable.hideEmptyGroups) {
                                if (PreferenceTable.hideOffline) {
                                    if (contact.isOnline()) {
                                        gpi.empty_for_display = false;
                                    }
                                } else {
                                    gpi.empty_for_display = false;
                                }
                            }
                        }
                    }
                }
            }
            if (!PreferenceTable.hideEmptyGroups) {
                gpi.empty_for_display = false;
            }
        }
        return gpi;
    }

    public final String getFullJID() {
        return this.ID + "@" + this.host;
    }

    public final String getFullJIDWithResource() {
        return this.ID + "@" + this.host + "/" + this.resource;
    }

    private final class screen_controller {
        private final PendingIntentHandler away_task;
        public boolean is_active;
        public boolean status_changed;

        private screen_controller() {
            this.is_active = false;
            this.status_changed = false;
            this.away_task = new PendingIntentHandler() { // from class: ru.ivansuper.jasmin.jabber.JProfile.screen_controller.1
                @Override // ru.ivansuper.jasmin.Service.PendingIntentHandler
                public void run() {
                    JProfile.this.sendPresence(2);
                    screen_controller.this.status_changed = true;
                    screen_controller.this.is_active = false;
                }
            };
        }

        /** @noinspection unused*/ /* synthetic */ screen_controller(JProfile jProfile, screen_controller screen_controllerVar) {
            this();
        }

        public void start() {
            if (!this.is_active) {
                this.is_active = true;
                this.status_changed = false;
                JProfile.this.svc.attachTimedTask(this.away_task, PreferenceTable.auto_change_status_timeout * 1000L);
            }
        }

        public void stop() {
            this.is_active = false;
            this.status_changed = false;
            JProfile.this.svc.removeTimedTask(this.away_task);
        }
    }

    private final class reconnector {
        private volatile reconnect_timer rt;
        public boolean is_active = false;
        public boolean enabled = false;
        private int limit = -1;
        private int tryes = 0;

        public reconnector() {
        }

        public void start() {
            reconnect_timer reconnect_timerVar = null;
            if (!this.is_active) {
                this.enabled = true;
                this.is_active = true;
                //noinspection DataFlowIssue,deprecation
                this.limit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(JProfile.this.svc).getString("ms_reconnection_count", "15"));
                this.tryes = 0;
                //noinspection ConstantValue
                this.rt = new reconnect_timer(this, reconnect_timerVar);
                this.rt.start();
                JProfile.this.svc.addWakeLock(JProfile.this.ID + JProfile.this.PASS);
                jasminSvc.pla.put(JProfile.this.nickname, resources.getString("s_reconnection_start"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                JProfile.this.svc.put_log(JProfile.this.nickname + ": " + resources.getString("s_reconnection_start"));
            }
        }

        public void stop() {
            if (this.is_active) {
                jasminSvc.pla.put(JProfile.this.nickname, resources.getString("s_reconnection_stop"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                JProfile.this.svc.put_log(JProfile.this.nickname + ": " + resources.getString("s_reconnection_stop"));
                this.enabled = false;
                this.is_active = false;
                JProfile.this.svc.removeWakeLock(JProfile.this.ID + JProfile.this.PASS);
            }
        }

        private final class reconnect_timer extends Thread {
            private reconnect_timer() {
            }

            /** @noinspection unused*/ /* synthetic */ reconnect_timer(reconnector reconnectorVar, reconnect_timer reconnect_timerVar) {
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
                            if (reconnector.this.tryes >= reconnector.this.limit) {
                                jasminSvc.pla.put(JProfile.this.nickname, resources.getString("s_reconnection_limit_exceed"), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                                JProfile.this.svc.put_log(JProfile.this.nickname + ": " + resources.getString("s_reconnection_limit_exceed"));
                                reconnector.this.stop();
                                JProfile.this.svc.runOnUi(() -> JProfile.this.disconnectInternal(false, false), 150L);
                                return;
                            }
                            JProfile.this.stream.disconnect();
                            try {
                                //noinspection BusyWait
                                sleep(1000L);
                            } catch (InterruptedException e2) {
                                //noinspection CallToPrintStackTrace
                                e2.printStackTrace();
                            }
                            if (JProfile.this.svc.isNetworkAvailable()) {
                                jasminSvc.pla.put(JProfile.this.nickname, utilities.match(resources.getString("s_try_to_reconnect"), new String[]{String.valueOf(reconnector.this.tryes + 1)}), null, null, popup_log_adapter.INFO_DISPLAY_TIME, null);
                                JProfile.this.svc.put_log(JProfile.this.nickname + ": " + utilities.match(resources.getString("s_try_to_reconnect"), new String[]{String.valueOf(reconnector.this.tryes + 1)}));
                                JProfile.this.startConnecting();
                                reconnector.this.tryes++;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void sendPingPacket() {
        if (this.connected) {
            Node iq = new Node("iq");
            iq.putParameter("to", getFullJIDWithResource()).putParameter("type", "get").putParameter("id", "self_ping_thread");
            Node ping = new Node("ping", "", "urn:xmpp:ping");
            iq.putChild(ping);
            this.stream.write(iq, this);
        }
    }

    private final class ping_thread extends Thread {
        private int counter = 0;

        private ping_thread() {
        }

        public void resetTimer() {
            this.counter = 0;
            JProfile.this.ping_answer_received = true;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            setPriority(1);
            int period = PreferenceTable.ping_freq;
            setName(JProfile.this.ID + " ping thread");
            if (PreferenceTable.use_ping) {
                while (JProfile.this.connected) {
                    try {
                        if (this.counter > period) {
                            if (JProfile.this.ping_answer_received) {
                                JProfile.this.ping_answer_received = false;
                                //noinspection ConstantValue
                                if (JProfile.this.connected) {
                                    XMLPacket packet = new XMLPacket("<iq to='" + JProfile.this.ID + "@" + JProfile.this.host + "/" + JProfile.this.resource + "' type='get' id='self_ping_thread'><ping xmlns='urn:xmpp:ping'/></iq>", null);
                                    JProfile.this.stream.write(packet, JProfile.this);
                                }
                                this.counter = 0;
                            } else {
                                JProfile.this.handleConnectionLosted();
                                return;
                            }
                        }
                        //noinspection BusyWait
                        sleep(1000L);
                        this.counter++;
                    } catch (Exception e) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}