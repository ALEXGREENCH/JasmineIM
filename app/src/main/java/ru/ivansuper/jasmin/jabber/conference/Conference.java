package ru.ivansuper.jasmin.jabber.conference;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.SmileysManager;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.jabber.Clients;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.XMLPacket;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.xml_utils;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/* loaded from: classes.dex */
public class Conference {
    public static final String BANNED_LIST_OPERATION = "banned_list_changed";
    public static final String BANNED_LIST_REQUEST = "banned_list_request";
    public static final String ROOM_CONFIG_FORM = "room_config_form";
    public final Vector<User> users = new Vector<>();
    private final Vector<JContact> pm_contacts = new Vector<>();
    public String JID;
    public RefreshCallback callback;
    public ConferenceItem item;
    public BannedAdapter mBannedList;
    public String nick;
    public String pass;
    public JProfile profile;
    /** @noinspection unused*/
    private final String SCROLL_STATE_HASH;
    public String theme = "";
    public ArrayList<HistoryItem> history = new ArrayList<>();
    public String typedText = "";
    public int unreaded = 0;
    private boolean online;
    private boolean turned_on;
    private boolean normal_shutdown = true;

    public Conference(String jid, String nick, String pass, JProfile profile) {
        this.JID = jid;
        this.nick = nick;
        this.pass = pass;
        this.profile = profile;
        this.SCROLL_STATE_HASH = Integer.toHexString(utilities.getHash(this));
    }

    public final void registerPMContact(JContact contact) {
        unregisterPMContact(contact);
        this.pm_contacts.add(contact);
        getInfoForPM(contact);
    }

    public final void unregisterPMContact(JContact contact) {
        if (this.pm_contacts.contains(contact)) {
            this.pm_contacts.remove(contact);
            contact.ext_status = resources.jabber_offline;
            this.profile.svc.handleContactlistDatasetChanged();
            this.profile.svc.handleChatUpdateInfo();
        }
    }

    private void getInfoForPM(JContact contact) {
        User user = getUser(JProtocol.getResourceFromFullID(contact.ID));
        if (user != null) {
            contact.ext_status = resources.getXMPPStatusIcon(user.status);
            this.profile.svc.handleContactlistDatasetChanged();
            this.profile.svc.handleChatUpdateInfo();
        }
    }

    private void setAllPMsOffline() {
        for (int i = 0; i < this.pm_contacts.size(); i++) {
            JContact c = this.pm_contacts.get(i);
            c.ext_status = resources.jabber_offline;
        }
        this.profile.svc.handleContactlistDatasetChanged();
        this.profile.svc.handleChatUpdateInfo();
    }

    private void notifyPMContact(String nick, String status) {
        JContact contact = null;
        int i = 0;
        while (true) {
            if (i >= this.pm_contacts.size()) {
                break;
            }
            JContact c = this.pm_contacts.get(i);
            if (!c.ID.endsWith(nick)) {
                i++;
            } else {
                contact = c;
                break;
            }
        }
        if (contact != null) {
            if (status != null && status.equals("offline")) {
                contact.ext_status = null;
            } else {
                contact.ext_status = resources.getXMPPStatusIcon(JProtocol.parseStatus(status));
            }
            this.profile.svc.handleContactlistDatasetChanged();
            this.profile.svc.handleChatUpdateInfo();
        }
    }

    public final boolean isOnline() {
        return this.online;
    }

    public final boolean isTurnedOn() {
        return this.turned_on;
    }

    public final boolean isNormalShutdown() {
        return this.normal_shutdown;
    }

    public final void setIsNormalShutdown(boolean normal) {
        this.normal_shutdown = normal;
    }

    public final void setAsOffline(final boolean lost) {
        resources.service.runOnUi(new Runnable() {
            @Override
            public void run() {
                boolean z = false;
                Conference.this.online = false;
                Conference conference = Conference.this;
                if (lost && !Conference.this.normal_shutdown) {
                    z = true;
                }
                conference.turned_on = z;
                Conference.this.clearUsers();
                Conference.this.setAllPMsOffline();
                Conference.this.sendConferenceOfflinePresence();
            }
        });
    }

    private void sendConferenceOfflinePresence() {
        XMLPacket packet = new XMLPacket("<presence to='" + this.JID + "/" + this.nick + "' type='unavailable'/>", null);
        this.profile.stream.write(packet, this.profile);
    }

    public User getUser(String nick) {
        synchronized (this.users) {
            for (int i = 0; i < this.users.size(); i++) {
                User user = this.users.get(i);
                if (user.nick.equals(nick)) {
                    return user;
                }
            }
            return null;
        }
    }

    public boolean containUser(String nick) {
        synchronized (this.users) {
            for (int i = 0; i < this.users.size(); i++) {
                User user = this.users.get(i);
                if (user.nick.equals(nick)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void clearUsers() {
        synchronized (this.users) {
            this.users.clear();
        }
    }

    public void updatePresence() {
        updatePresence(this.profile.status);
    }

    public void updatePresence(int status_param) {
        Node presence = new Node("presence");
        presence.putParameter("to", this.JID + "/" + this.nick);
        Node priority = new Node("priority");
        priority.setValue(String.valueOf(this.profile.priority));
        presence.putChild(priority);
        String status_ = JProtocol.parseStatus(status_param);
        if (!status_.isEmpty()) {
            Node show = new Node("show");
            show.setValue(JProtocol.parseStatus(status_param));
            presence.putChild(show);
        }
        if (!this.profile.status_desc.isEmpty()) {
            Node status = new Node("status");
            status.setValue(this.profile.status_desc);
            presence.putChild(status);
        }
        if (!this.pass.isEmpty()) {
            Node x = new Node("x", "", "http://jabber.org/protocol/muc");
            Node pass = new Node("password");
            pass.setValue(this.pass);
            x.putChild(pass);
            presence.putChild(x);
        }
        Node c = new Node("c", "", "http://jabber.org/protocol/caps");
        c.putParameter("node", "http://jasmineicq.ru/caps").putParameter("ver", resources.VERSION);
        presence.putChild(c);
        this.profile.stream.write(presence, this.profile);
        this.turned_on = true;
    }

    public void kickUser(User user, String reason) {
        XMLPacket packet = new XMLPacket("<iq type='set' to='" + this.JID + "'><query xmlns='http://jabber.org/protocol/muc#admin'><item nick='" + xml_utils.encodeString(user.nick) + "' role='none'><reason>" + reason + "</reason></item></query></iq>", null);
        this.profile.stream.write(packet, this.profile);
    }

    public void banUser(String user, String reason) {
        Node iq = new Node("iq");
        iq.putParameter("to", this.JID).putParameter("type", "set").putParameter("id", BANNED_LIST_OPERATION);
        Node query = new Node("query", "", "http://jabber.org/protocol/muc#admin");
        Node item = new Node("item");
        item.putParameter("jid", user).putParameter("affiliation", "outcast");
        if (reason != null && !reason.trim().isEmpty()) {
            Node reason_ = new Node("reason", reason);
            item.putChild(reason_);
        }
        query.putChild(item);
        iq.putChild(query);
        this.profile.stream.write(iq, this.profile);
    }

    public void banUsers(String[] users) {
        for (String user : users) {
            banUser(user, null);
        }
    }

    public void banUser(User user, String reason) {
        banUser(user.nick, reason);
    }

    public final void showRoomPreferences() {
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", this.JID).putParameter("id", ROOM_CONFIG_FORM);
        Node query = new Node("query", "", "http://jabber.org/protocol/muc#owner");
        iq.putChild(query);
        this.profile.stream.write(iq, this.profile);
    }

    public boolean isMeAOwner() {
        return isHeAOwner(this.nick);
    }

    public boolean isMeAModerator() {
        return isHeAModerator(this.nick);
    }

    /** @noinspection unused*/
    public boolean isMeAPoweredModerator() {
        return isMeAreAdmin() || isMeAOwner();
    }

    public boolean isMeAreAdmin() {
        return isHeAreAdmin(this.nick);
    }

    public boolean isHeAOwner(String nick) {
        User user;
        return this.online && (user = getUser(nick)) != null && user.affiliation.equals("owner");
    }

    public boolean isHeAModerator(String nick) {
        User user;
        return this.online && (user = getUser(nick)) != null && user.role.equals("moderator");
    }

    public boolean isHeAreAdmin(String nick) {
        User user;
        return this.online && (user = getUser(nick)) != null && user.affiliation.equals("admin");
    }

    /** @noinspection unused*/
    public boolean isHeWithoutRights(String nick) {
        User user;
        return !this.online || (user = getUser(nick)) == null || user.affiliation.equals("none");
    }

    /** @noinspection unused*/
    public boolean isHeASimpleUser(String nick) {
        return !isHeAModerator(nick) && !isHeAreAdmin(nick) && !isHeAOwner(nick);
    }

    public int getUserStrength(String nick) {
        User user = getUser(nick);
        if (user == null) {
            return 0;
        }
        String a = user.affiliation;
        String r = user.role;
        if (r.equals("visitor") && a.equals("none")) {
            return 1;
        }
        if (r.equals("visitor") && a.equals("member")) {
            return 2;
        }
        if (r.equals("participant") && a.equals("none")) {
            return 3;
        }
        if (r.equals("participant") && a.equals("member")) {
            return 4;
        }
        if (r.equals("moderator") && a.equals("none")) {
            return 5;
        }
        if (r.equals("moderator") && a.equals("member")) {
            return 6;
        }
        if (r.equals("moderator") && a.equals("admin")) {
            return 7;
        }
        return (r.equals("moderator") && a.equals("owner")) ? 8 : 0;
    }

    public void setUserRole(String nick, String role) {
        Node iq = new Node("iq");
        iq.putParameter("type", "set").putParameter("to", this.JID);
        Node query = new Node("query", "", "http://jabber.org/protocol/muc#admin");
        Node item = new Node("item");
        item.putParameter("nick", nick).putParameter("role", role);
        query.putChild(item);
        iq.putChild(query);
        this.profile.stream.write(iq, this.profile);
    }

    public void setUserAffiliation(String nick, String affiliation) {
        Node iq = new Node("iq");
        iq.putParameter("type", "set").putParameter("to", this.JID);
        Node query = new Node("query", "", "http://jabber.org/protocol/muc#admin");
        Node item = new Node("item");
        item.putParameter("nick", nick).putParameter("affiliation", affiliation);
        query.putChild(item);
        iq.putChild(query);
        this.profile.stream.write(iq, this.profile);
    }

    public void updateNickname(String nick) {
        if (this.online) {
            Node presence = new Node("presence");
            presence.putParameter("to", this.JID + "/" + nick);
            Node priority = new Node("priority");
            priority.setValue(String.valueOf(this.profile.priority));
            presence.putChild(priority);
            Node show = new Node("show");
            show.setValue(JProtocol.parseStatus(this.profile.status));
            presence.putChild(show);
            Node status = new Node("status");
            status.setValue(this.profile.status_desc);
            presence.putChild(status);
            Node x = new Node("x", "", "http://jabber.org/protocol/muc");
            Node pass = new Node("password");
            pass.setValue(this.pass);
            x.putChild(pass);
            presence.putChild(x);
            Node c = new Node("c", "", "http://jabber.org/protocol/caps");
            c.putParameter("node", "http://jasmineicq.ru/caps");
            c.putParameter("ver", resources.VERSION);
            presence.putChild(c);
            this.profile.stream.write(presence, this.profile);
            return;
        }
        this.nick = nick;
        this.profile.saveRoster();
    }

    public void userOnline(String nick, String jid, String affilation, String role, String status, String FullJID, String reason, String sts, String client) {
        String old = "";
        boolean online_ = false;
        if (containUser(nick)) {
            online_ = true;
            User user = getUser(nick);
            user.status_text = sts;
            user.jid = jid;
            user.status = JProtocol.parseStatus(status);
            user.nick = nick;
            user.affiliation = affilation;
            old = user.role;
            user.role = role;
            user.client = Clients.foundCap(client);
        } else {
            User user2 = new User();
            user2.status_text = sts;
            user2.jid = jid;
            user2.status = JProtocol.parseStatus(status);
            user2.nick = nick;
            user2.affiliation = affilation;
            user2.role = role;
            user2.client = Clients.foundCap(client);
            this.users.add(user2);
            if (this.online) {
                incomingSysMessage("/" + nick, jid == null ? "" : "(" + jid + ")", 5);
            }
        }
        notifyPMContact(nick, status);
        if (online_) {
            if (!old.equals("visitor") && role.equals("visitor")) {
                incomingSysMessage(FullJID, "(" + reason + ")", 2);
            }
            if (old.equals("visitor") && !role.equals("visitor")) {
                incomingSysMessage(FullJID, "(" + reason + ")", 3);
            }
        }
        Collections.sort(this.users);
        if (nick.equals(this.nick)) {
            this.online = true;
        }
        if (this.callback != null) {
            this.callback.update();
        }
        this.profile.svc.handleChatUpdateInfo();
        this.profile.svc.handleContactlistNeedRemake();
    }

    private void replaceUserNick(String old_, String new_) {
        for (int i = 0; i < this.users.size(); i++) {
            User user = this.users.get(i);
            if (user.nick.equals(old_)) {
                user.nick = new_;
                Collections.sort(this.users);
                return;
            }
        }
    }

    private User removeUser(String nick) {
        for (int i = 0; i < this.users.size(); i++) {
            User user = this.users.get(i);
            if (user.nick.equals(nick)) {
                return this.users.remove(i);
            }
        }
        return null;
    }

    /** @noinspection unused*/
    public void userOffline(String nick, String affilation, String role, String new_nick, String FullJID, String reason, int status_code) {
        User user;
        synchronized (this.users) {
            if (new_nick != null && status_code == 303) {
                incomingSysMessage("/" + nick, new_nick, 4);
                if (getUser(new_nick) != null) {
                    removeUser(nick);
                } else {
                    replaceUserNick(nick, new_nick);
                }
                this.profile.saveRoster();
            }
            if (status_code == 0 && (user = removeUser(nick)) != null) {
                incomingSysMessage("/" + nick, user.jid == null ? "" : "(" + user.jid + ")", 6);
            }
            if (nick.equals(this.nick) && new_nick != null) {
                this.nick = new_nick;
                this.profile.saveRoster();
            }
            if (nick.equals(this.nick) && new_nick == null) {
                this.online = false;
                this.turned_on = false;
                this.normal_shutdown = true;
                clearUsers();
            }
            notifyPMContact(nick, "offline");
            if (this.callback != null) {
                this.callback.update();
            }
        }
        this.profile.svc.handleChatUpdateInfo();
        this.profile.svc.handleContactlistNeedRemake();
    }

    public void incomingSysMessage(String FullJID, String reason, int status_code) {
        if (reason != null && reason.endsWith("()")) {
            reason = reason.substring(0, reason.length() - 2);
        }
        HistoryItem hst = new HistoryItem(System.currentTimeMillis());
        hst.conf = this;
        hst.me = this.nick;
        hst.conf_nick = JProtocol.getResourceFromFullID(FullJID);
        hst.conf_profile = this.profile;
        hst.confirmed = true;
        hst.conf_warn = status_code;
        hst.direction = 1;
        String msg = "";
        switch (status_code) {
            case 2:
                msg = resources.getString("s_pres_desc_3");
                break;
            case 3:
                msg = resources.getString("s_pres_desc_4");
                break;
            case 4:
                msg = resources.getString("s_pres_desc_5");
                break;
            case 5:
                msg = resources.getString("s_pres_desc_6");
                break;
            case 6:
                msg = resources.getString("s_pres_desc_7");
                break;
            case 301:
                msg = resources.getString("s_pres_desc_2");
                break;
            case 307:
                msg = resources.getString("s_pres_desc_1");
                break;
        }
        //noinspection DataFlowIssue
        if (!reason.isEmpty()) {
            msg = msg + " " + reason;
        }
        hst.message = msg;
        this.history.add(hst);
        if ((!this.profile.svc.isAnyChatOpened || !isNowThisOpened()) && hst.conf_nick.equals(this.nick)) {
            this.item.setHasUnreadMessages();
            jasminSvc.pla.put(hst.conf_nick, SmileysManager.getSmiledText(hst.message, 0, false), resources.msg_in, null, popup_log_adapter.MESSAGE_DISPLAY_TIME, new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Conference.this.profile.svc, ContactListActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction("JCFITEM" + Conference.this.profile.ID + "@" + Conference.this.profile.host + "***$$$SEPARATOR$$$***" + Conference.this.JID);
                    Conference.this.profile.svc.startActivity(i);
                }
            });
            if (PreferenceTable.multi_notify) {
                this.profile.svc.showPersonalMessageNotify(hst.conf_nick, hst.message, true, utilities.getHash(this), this.item);
            } else {
                this.profile.svc.putMessageNotify(this.item, hst.conf_nick, hst.message);
            }
            this.profile.svc.handleIncomingMessage();
        } else {
            this.profile.svc.handleChatNeedRebuild(this);
        }
        this.profile.svc.handleContactlistNeedRemake();
    }

    private boolean checkMessageInLast(HistoryItem hst) {
        int bottom = this.history.size() - 150;
        if (bottom < 0) {
            bottom = 0;
        }
        for (int i = this.history.size() - 1; i >= bottom; i--) {
            HistoryItem hst_ = this.history.get(i);
            if (hst_.message.equals(hst.message) && hst_.isTheme == hst.isTheme && hst_.conf_warn == hst.conf_warn) {
                return true;
            }
        }
        return false;
    }

    public void incomingMessage(String FullJID, String nick, String subject, String message, long timestamp) {
        if (containUser(nick) || this.JID.equals(JProtocol.getJIDFromFullID(FullJID))) {
            HistoryItem hst = new HistoryItem(timestamp);
            hst.conf = this;
            hst.me = this.nick;
            hst.conf_nick = nick;
            hst.conf_profile = this.profile;
            hst.confirmed = true;
            if (this.nick.equals(nick)) {
                hst.direction = 0;
            } else {
                hst.direction = 1;
            }
            hst.message = message;
            if (hst.message.startsWith("/me")) {
                hst.message = hst.message.substring(3).trim();
                hst.isMe = true;
                hst.addTwoPoints = false;
            } else if (hst.conf_nick.isEmpty()) {
                hst.isMe = true;
                hst.addTwoPoints = false;
            } else {
                hst.addTwoPoints = true;
            }
            if (!subject.isEmpty()) {
                hst.isTheme = true;
                hst.conf_nick = resources.getString("s_conference_theme");
                if (nick == null) {
                    nick = "";
                }
                hst.message = subject + (nick.isEmpty() ? "" : "\n(" + utilities.match(resources.getString("s_who_made_the_theme"), new String[]{nick}) + ")");
                this.theme = subject;
                this.profile.svc.handleChatUpdateInfo();
            }
            if (timestamp == 0 || !checkMessageInLast(hst)) {
                this.history.add(hst);
                Pattern p = Pattern.compile("(\\A|\\W+)" + Pattern.quote(hst.me) + "(\\W+|\\z)", 34);
                Matcher m = p.matcher(hst.message);
                hst.itIsForMe = m.find();
                if (!JConference.is_any_chat_opened || !isNowThisOpened()) {
                    if (hst.itIsForMe) {
                        this.item.setHasUnreadMessages();
                        jasminSvc.pla.put(hst.conf_nick, SmileysManager.getSmiledText(hst.message, 0, false), resources.msg_in, null, popup_log_adapter.MESSAGE_DISPLAY_TIME, new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(Conference.this.profile.svc, ContactListActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.setAction("JCFITEM" + Conference.this.profile.ID + "@" + Conference.this.profile.host + "***$$$SEPARATOR$$$***" + Conference.this.JID);
                                Conference.this.profile.svc.startActivity(i);
                            }
                        });
                        if (PreferenceTable.multi_notify) {
                            this.profile.svc.showPersonalMessageNotify(hst.conf_nick, hst.message, true, utilities.getHash(this), this.item);
                        } else {
                            this.profile.svc.putMessageNotify(this.item, hst.conf_nick, hst.message);
                        }
                        this.profile.svc.handleIncomingMessage();
                    }
                    this.unreaded++;
                } else {
                    this.profile.svc.handleChatNeedRebuild(this);
                }
                this.profile.svc.handleContactlistNeedRemake();
            }
        }
    }

    public void setTheme(String theme) {
        String text = xml_utils.encodeString(theme);
        XMLPacket packet = new XMLPacket("<message type='groupchat' to='" + this.JID + "'><subject>" + text + "</subject></message>", null);
        this.profile.stream.write(packet, this.profile);
    }

    public void sendMessage(String message) {
        String text = xml_utils.encodeString(message);
        XMLPacket packet = new XMLPacket("<message type='groupchat' to='" + this.JID + "'><body>" + text + "</body></message>", null);
        this.profile.stream.write(packet, this.profile);
    }

    public final void doRequestBannedList() {
        Node iq = new Node("iq");
        iq.putParameter("type", "get").putParameter("to", this.JID).putParameter("id", BANNED_LIST_REQUEST);
        Node query = new Node("query", "", "http://jabber.org/protocol/muc#admin");
        Node item = new Node("item");
        item.putParameter("affiliation", "outcast");
        query.putChild(item);
        iq.putChild(query);
        this.profile.stream.write(iq, this.profile);
    }

    public void proceedPacket(Node xml) {
        Node item;
        Node query;
        Node query2;
        String PID = xml.getParameter("id");
        String type = xml.getParameter("type");
        final String from = xml.getParameter("from");
        if (PID == null) {
            PID = "";
        }
        if (type == null) {
            type = "";
        }
        if (type.equals("result") && PID.equals(ROOM_CONFIG_FORM) && (query2 = xml.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/muc#owner")) != null) {
            this.profile.proceedXForm(xml.getParameter("from"), PID, query2);
        }
        if (xml.findFirstLocalNodeByNameAndNamespace("query", "jabber:iq:version") != null && xml.getParameter("type").equals("get")) {
            Node iq = new Node("iq");
            iq.putParameter("type", "result");
            iq.putParameter("to", xml.getParameter("from"));
            iq.putParameter("id", PID);
            Node query3 = new Node("query", "", "jabber:iq:version");
            Node name = new Node("name", "Jasmine IM");
            Node version = new Node("version", resources.VERSION);
            Node os = new Node("os", "Android " + resources.OS_VERSION_STR + " (" + resources.SOFTWARE_STR + ")[" + resources.DEVICE_STR + "]");
            query3.putChild(name);
            query3.putChild(version);
            query3.putChild(os);
            iq.putChild(query3);
            this.profile.stream.write(iq, this.profile);
        }
        if (xml.getParameter("type").equals("result") && PID.equals(BANNED_LIST_OPERATION)) {
            if (BannedListActivity.ACTIVE) {
                doRequestBannedList();
                return;
            }
            return;
        }
        if (xml.getParameter("type").equals("result") && PID.equals(BANNED_LIST_REQUEST) && (query = xml.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/muc#admin")) != null) {
            Vector<Node> mItems = query.findLocalNodesByName("item");
            ArrayList<BannedAdapter.BannedItem> banned = new ArrayList<>();
            for (Node n : mItems) {
                final String jid = n.getParameter("jid");
                Node reason_ = n.findFirstLocalNodeByName("reason");
                String reason = null;
                if (reason_ != null) {
                    reason = reason_.getValue();
                }
                banned.add(new BannedAdapter.BannedItem(jid, reason, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Node iq2 = new Node("iq");
                        iq2.putParameter("type", "set").putParameter("to", Conference.this.JID).putParameter("id", Conference.BANNED_LIST_OPERATION);
                        Node query4 = new Node("query", "", "http://jabber.org/protocol/muc#admin");
                        Node item2 = new Node("item", "", "");
                        item2.putParameter("jid", jid).putParameter("affiliation", "none");
                        query4.putChild(item2);
                        iq2.putChild(query4);
                        Conference.this.profile.stream.write(iq2, Conference.this.profile);
                    }
                }));
            }
            if (!banned.isEmpty()) {
                this.mBannedList = new BannedAdapter(banned);
                this.profile.svc.handleConferenceBannedListReceived(this);
            }
        }
        Node query4 = xml.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/muc#admin");
        if (query4 == null) {
            query4 = xml.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/muc#user");
        }
        if (query4 != null && (item = query4.findFirstLocalNodeByName("item")) != null && query4.childs.size() == 1) {
            final String nick = item.getParameter("nick");
            String affiliation = item.getParameter("affiliation");
            String role = item.getParameter("role");
            Node reason_2 = item.findFirstLocalNodeByName("reason");
            String reason2 = "";
            if (reason_2 != null) {
                reason2 = reason_2.getValue();
            }
            final String REASON = reason2;
            if (affiliation.equals("outcast")) {
                resources.service.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        Conference.this.incomingSysMessage(from + "/" + nick, "(" + REASON + ")", 307);
                    }
                });
            } else //noinspection ConstantValue
                if (role.equals("none") && !affiliation.equals("outcast")) {
                    resources.service.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            Conference.this.incomingSysMessage(from + "/" + nick, "(" + REASON + ")", 301);
                        }
                    });
                }
        }
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    private boolean isNowThisOpened() {
        return JConference.conference.equals(this);
    }

    public interface RefreshCallback {
        void update();
    }

    public class User implements Comparable<User> {
        public String affiliation;
        public int client = -1;
        public String jid;
        public String nick;
        public String role;
        public int status;
        public String status_text;

        public User() {
        }

        @Override
        public int compareTo(User user) {
            try {
                int us1 = Conference.this.getUserStrength(this.nick);
                int us2 = Conference.this.getUserStrength(user.nick);
                if (us1 > us2) {
                    return -1;
                }
                if (us1 < us2) {
                    return 1;
                }
                String nameA = this.nick;
                String nameB = user.nick;
                int minLen = nameA.length();
                if (nameB.length() < minLen) {
                    minLen = nameB.length();
                }
                int lvl = 0;
                while (true) {
                    int a = utilities.chars.indexOf(nameA.charAt(lvl)) + 256;
                    if (a == 255) {
                        a = nameA.charAt(lvl);
                    }
                    int b = utilities.chars.indexOf(nameB.charAt(lvl)) + 256;
                    if (b == 255) {
                        b = nameB.charAt(lvl);
                    }
                    if (a == b) {
                        lvl++;
                        if (lvl >= minLen) {
                            return 0;
                        }
                    } else {
                        if (a < b) {
                            return -1;
                        }
                        //noinspection ConstantValue
                        if (a > b) {
                            return 1;
                        }
                    }
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return 0;
            }
        }

        /** @noinspection unused*/
        public Drawable getAffiliationIcon() {
            if (this.affiliation == null) {
                return resources.jabber_online;
            }
            switch (this.affiliation) {
                case "owner":
                    return resources.jabber_conf_owner;
                case "admin":
                    return resources.jabber_conf_admin;
                case "member":
                    return resources.jabber_conf_member;
                case "outcast":
                    return resources.jabber_conf_outcast;
            }
            return resources.jabber_online;
        }

        /** @noinspection unused*/
        public int getAffiliationInt() {
            if (this.affiliation == null) {
                return 1;
            }
            switch (this.affiliation) {
                case "owner":
                    return 4;
                case "admin":
                    return 3;
                case "member":
                    return 2;
            }
            return this.affiliation.equals("outcast") ? 0 : 1;
        }

        /** @noinspection unused*/
        public String getAffiliation() {
            if (this.affiliation == null) {
                return resources.getString("s_affiliation_none");
            }
            switch (this.affiliation) {
                case "owner":
                    return resources.getString("s_affiliation_owner");
                case "admin":
                    return resources.getString("s_affiliation_admin");
                case "member":
                    return resources.getString("s_affiliation_member");
                case "outcast":
                    return resources.getString("s_affiliation_outcast");
            }
            return resources.getString("s_affiliation_none");
        }

        /** @noinspection unused*/
        public String getRole() {
            if (this.role == null) {
                return resources.getString("s_role_none");
            }
            switch (this.role) {
                case "moderator":
                    return resources.getString("s_role_moderator");
                case "participant":
                    return resources.getString("s_role_participant");
                case "visitor":
                    return resources.getString("s_role_visitor");
            }
            return resources.getString("s_role_participant");
        }
    }
}
