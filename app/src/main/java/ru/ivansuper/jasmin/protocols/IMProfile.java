package ru.ivansuper.jasmin.protocols;

import android.graphics.drawable.Drawable;

import java.util.Vector;

import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.icq.qip_statuses;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.conference.ConferenceItem;
import ru.ivansuper.jasmin.protocols.utils.Openable;
import ru.ivansuper.jasmin.resources;

public abstract class IMProfile implements Openable {

    // Типы протоколов
    public static final int OSCAR = 0;
    public static final int JABBER = 1;
    public static final int MMP = 2;
    /** IRC protocol */
    public static final int IRC = 3;

    // Абстрактные статусы
    public static final int STATUS_OFFLINE = -1;
    public static final int STATUS_ONLINE = 0;
    public static final int STATUS_FFC = 1;
    public static final int STATUS_ANGRY = 2;
    public static final int STATUS_DEPRESS = 3;
    public static final int STATUS_HOME = 4;
    public static final int STATUS_WORK = 5;
    public static final int STATUS_LUNCH = 6;
    public static final int STATUS_AWAY = 7;
    public static final int STATUS_NA = 8;
    public static final int STATUS_OC = 9;
    public static final int STATUS_DND = 10;

    // Поля профиля
    public boolean autoconnect;
    public boolean enabled;
    public boolean connected = false;
    public boolean connecting = false;
    public boolean openedInContactList = true;
    public int profile_type = -1;
    public String nickname = "";
    public String ID = "";
    public int status = STATUS_OFFLINE;
    public int connection_status = 0;

    public jasminSvc svc;
    private BottomPanelNotifier notifier;
    /** @noinspection unused*/
    public final BanList banlist = new BanList();

    // Интерфейс для обновления UI
    public interface BottomPanelNotifier {
        void onConnectionStatusChanged();
        void onStatusChanged();
    }

    // Методы жизненного цикла соединения
    public abstract void startConnecting();
    public abstract void disconnect();
    public abstract void closeAllChats();
    public abstract void handleScreenTurnedOn();
    public abstract void handleScreenTurnedOff();
    /** @noinspection unused*/
    public abstract String getStatusText();
    /** @noinspection unused*/
    public abstract void setStatusText(String str);

    // Openable интерфейс
    @Override
    public final boolean isOpened() {
        return openedInContactList;
    }

    @Override
    public final void setOpened(boolean opened) {
        openedInContactList = opened;
    }

    @Override
    public final void toggleOpened() {
        openedInContactList = !openedInContactList;
    }

    public final void setNotifier(BottomPanelNotifier notifier) {
        this.notifier = notifier;
        notifyConnectionStatus();
        notifyStatusIcon();
    }

    public final void notifyConnectionStatus() {
        if (notifier != null && svc != null) {
            svc.runOnUi(new Runnable() {
                @Override
                public void run() {
                    notifier.onConnectionStatusChanged();
                }
            });
        }
    }

    public final void notifyStatusIcon() {
        if (notifier != null && svc != null) {
            svc.runOnUi(new Runnable() {
                @Override
                public void run() {
                    notifier.onStatusChanged();
                }
            });
        }
    }

    public void setConnectionStatus(int status) {
        this.connection_status = status;
        notifyConnectionStatus();
    }

    public void remakeContactList() {
        if (ContactListActivity.CURRENT_IS_CONTACTS && !ContactListActivity.HIDDEN) {
            svc.handleContactlistNeedRemake();
        }
    }

    public void refreshContactList() {
        remakeContactList();
    }

    // Ban-лист
    public static final class BanList {
        /** @noinspection unused*/
        public static final int TRYES_LIMIT = 5;
        final Vector<Item> list = new Vector<>();

        public synchronized void clear() {
            list.clear();
        }

        /** @noinspection unused*/
        public synchronized int increase(String id) {
            Item item = get(id);
            if (item == null) {
                item = new Item();
                item.identifier = id;
                list.add(item);
            } else {
                item.tryes++;
            }
            return item.tryes;
        }

        public synchronized void remove(String id) {
            for (int i = 0; i < list.size(); i++) {
                Item item = list.get(i);
                if (item.identifier.equals(id)) {
                    list.remove(i);
                    break;
                }
            }
        }

        public synchronized Item get(String id) {
            for (Item item : list) {
                if (item.identifier.equals(id)) {
                    return item;
                }
            }
            return null;
        }

        public static final class Item {
            public String identifier;
            public int tryes = 0;
        }
    }

    // Утилиты
    public static String getSchema(ContactlistItem contact) {
        switch (contact.itemType) {
            case 1:
                ICQContact icq = (ICQContact) contact;
                return "ICQITEM" + icq.profile.ID + "***$$$SEPARATOR$$$***" + icq.ID;
            case 4:
                JContact jab = (JContact) contact;
                return "JBRITEM" + jab.profile.getFullJID() + "***$$$SEPARATOR$$$***" + jab.ID;
            case 7:
                MMPContact mmp = (MMPContact) contact;
                return "MMPITEM" + mmp.profile.ID + "***$$$SEPARATOR$$$***" + mmp.ID;
            case 12:
                ru.ivansuper.jasmin.irc.IRCContact irc = (ru.ivansuper.jasmin.irc.IRCContact) contact;
                return "IRCITEM" + irc.profile.ID + "***$$$SEPARATOR$$$***" + irc.ID;
            case 10:
                ConferenceItem conf = (ConferenceItem) contact;
                return "JCFITEM" + conf.conference.profile.getFullJID() + "***$$$SEPARATOR$$$***" + conf.conference.JID;
            default:
                return "";
        }
    }

    public static String getProfileID(IMProfile profile) {
        if (profile instanceof ICQProfile) return profile.ID;
        if (profile instanceof JProfile) return profile.ID;
        if (profile instanceof MMPProfile) return profile.ID;
        if (profile instanceof ru.ivansuper.jasmin.irc.IRCProfile) return profile.ID;
        return "";
    }

    public static String getProfileFullID(IMProfile profile) {
        if (profile instanceof ICQProfile) return profile.ID;
        if (profile instanceof JProfile) return ((JProfile) profile).getFullJID();
        if (profile instanceof MMPProfile) return profile.ID;
        if (profile instanceof ru.ivansuper.jasmin.irc.IRCProfile) return profile.ID;
        return "";
    }

    public static Drawable getProfileIcon(IMProfile profile) {
        if (profile instanceof JProfile) {
            switch (((JProfile) profile).type) {
                case 0: return resources.jabber_online;
                case 1: return resources.vk_online;
                case 2: return resources.yandex_online;
                case 3: return resources.gtalk_online;
                case 4: return resources.qip_online;
            }
        } else if (profile instanceof MMPProfile) {
            return resources.mrim_online;
        } else if (profile instanceof ru.ivansuper.jasmin.irc.IRCProfile) {
            return resources.online;
        }
        return resources.online;
    }

    public static String getProfileNick(IMProfile profile) {
        if (profile instanceof ICQProfile) return profile.nickname;
        if (profile instanceof JProfile) return ((JProfile) profile).getFullJID();
        if (profile instanceof MMPProfile) return profile.ID;
        if (profile instanceof ru.ivansuper.jasmin.irc.IRCProfile) return profile.ID;
        return "";
    }

    public static int getAbstractedStatus(IMProfile profile) {
        if (!profile.connected) return STATUS_OFFLINE;

        switch (profile.profile_type) {
            case OSCAR: {
                ICQProfile ip = (ICQProfile) profile;
                if (ip.qip_status != null) return qip_statuses.getId(ip.qip_status);
                return resources.getIcqAbstractStatus(profile.status);
            }
            case JABBER: {
                JProfile jp = (JProfile) profile;
                switch (jp.status) {
                    case 0: return STATUS_FFC;
                    case 1: return STATUS_ONLINE;
                    case 2: return STATUS_AWAY;
                    case 3: return STATUS_DND;
                    case 4: return STATUS_NA;
                    default: return STATUS_OFFLINE;
                }
            }
            case MMP: {
                MMPProfile mp = (MMPProfile) profile;
                //noinspection ConstantValue
                if (!mp.connected) return STATUS_OFFLINE;
                switch (mp.getTranslatedStatus()) {
                    case 2:  return STATUS_AWAY;
                    case 5:  return STATUS_DND;
                    case 6:  return STATUS_OC;
                    case 7:  return STATUS_NA;
                    case 8:  return STATUS_LUNCH;
                    case 9:  return STATUS_WORK;
                    case 10: return STATUS_HOME;
                    case 11: return STATUS_DEPRESS;
                    case 12: return STATUS_ANGRY;
                    case 13: return STATUS_FFC;
                    default: return STATUS_ONLINE;
                }
            }
            case IRC: {
                return STATUS_ONLINE;
            }
            default:
                return STATUS_ONLINE;
        }
    }
}
