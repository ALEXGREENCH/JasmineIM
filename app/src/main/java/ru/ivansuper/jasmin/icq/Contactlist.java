package ru.ivansuper.jasmin.icq;

import android.preference.PreferenceManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.ContactsAdapter;
import ru.ivansuper.jasmin.GroupPresenceInfo;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.resources;

/**
 * Represents a list of contacts for an ICQ profile.
 * This class manages a collection of {@link ContactlistItem} objects,
 * which can be either {@link ICQContact} or {@link ICQGroup}.
 * It provides methods for adding, removing, retrieving, and organizing contacts and groups.
 * The class also handles saving and loading the contact list to/from local storage.
 * All operations that modify the contact list are synchronized on {@code ContactsAdapter.locker}.
 */
public class Contactlist {
    private final ArrayList<ContactlistItem> contacts = new ArrayList<>();
    private final ICQProfile profile;

    public Contactlist(ICQProfile profile) {
        this.profile = profile;
    }

    public final void clear() {
        synchronized (ContactsAdapter.locker) {
            int i = 0;
            while (i < this.contacts.size()) {
                ContactlistItem it = this.contacts.get(i);
                if (it != null) {
                    switch (it.itemType) {
                        case 1:
                            ICQContact contact = (ICQContact) it;
                            //noinspection IfStatementWithIdenticalBranches
                            if (!contact.isChating) {
                                this.contacts.remove(i);
                                i--;
                                break;
                            } else {
                                break;
                            }
                        case 2:
                            this.contacts.remove(i);
                            i--;
                            break;
                    }
                }
                i++;
            }
        }
    }

    public final void put(ContactlistItem item) {
        synchronized (ContactsAdapter.locker) {
            if (!this.contacts.contains(item)) {
                this.contacts.add(item);
            }
        }
    }

    public final void remove(ContactlistItem item) {
        synchronized (ContactsAdapter.locker) {
            if (this.contacts.contains(item)) {
                this.contacts.remove(item);
                sort();
            }
        }
    }

    /** @noinspection unused*/
    public final void putListOfContacts(Vector<ContactlistItem> list) {
        synchronized (ContactsAdapter.locker) {
            int count = list.size();
            if (count > 0) {
                this.contacts.addAll(list);
                sort();
            }
        }
    }

    public final GroupPresenceInfo getGroupPresenceInfo(int group_id) {
        GroupPresenceInfo gpi;
        synchronized (ContactsAdapter.locker) {
            gpi = new GroupPresenceInfo();
            for (ContactlistItem it2 : this.contacts) {
                if (it2 != null) {
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (it2.itemType) {
                        case 1:
                            ICQContact contact = (ICQContact) it2;
                            if (contact.group != group_id) {
                                break;
                            } else {
                                gpi.total++;
                                if (contact.status > -1) {
                                    gpi.online++;
                                }
                                if (!PreferenceTable.hideEmptyGroups) {
                                    break;
                                } else if (PreferenceTable.hideOffline) {
                                    //noinspection IfStatementWithIdenticalBranches
                                    if (contact.status <= -1) {
                                        break;
                                    } else {
                                        gpi.empty_for_display = false;
                                        break;
                                    }
                                } else {
                                    gpi.empty_for_display = false;
                                    break;
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

    public final ICQContact getContactByUIN(String uin) {
        synchronized (ContactsAdapter.locker) {
            for (ContactlistItem it2 : this.contacts) {
                if (it2 != null) {
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (it2.itemType) {
                        case 1:
                            ICQContact contact = (ICQContact) it2;
                            if (!contact.ID.equals(uin)) {
                                break;
                            } else {
                                return contact;
                            }
                    }
                }
            }
            return null;
        }
    }

    public final ICQGroup getGroupByName(String name) {
        synchronized (ContactsAdapter.locker) {
            for (ContactlistItem it2 : this.contacts) {
                if (it2 != null) {
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (it2.itemType) {
                        case 2:
                            ICQGroup group = (ICQGroup) it2;
                            if (!group.name.trim().equals(name.trim())) {
                                break;
                            } else {
                                return group;
                            }
                    }
                }
            }
            return null;
        }
    }

    public final void removeContact(String uin) {
        synchronized (ContactsAdapter.locker) {
            for (ContactlistItem it2 : this.contacts) {
                if (it2 != null && it2.itemType != 4) {
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (it2.itemType) {
                        case 1:
                            ICQContact contact = (ICQContact) it2;
                            if (!contact.ID.equals(uin)) {
                                break;
                            } else {
                                this.contacts.remove(contact);
                                Thread.dumpStack();
                                sort();
                                return;
                            }
                    }
                }
            }
        }
    }

    public final void removeGroup(int id) {
        synchronized (ContactsAdapter.locker) {
            for (ContactlistItem it2 : this.contacts) {
                if (it2 != null && it2.itemType != 4) {
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (it2.itemType) {
                        case 2:
                            ICQGroup group = (ICQGroup) it2;
                            if (group.id != id) {
                                break;
                            } else {
                                this.contacts.remove(group);
                                sort();
                                return;
                            }
                    }
                }
            }
        }
    }

    public final ArrayList<ContactlistItem> getAll() {
        ArrayList<ContactlistItem> arrayList;
        synchronized (ContactsAdapter.locker) {
            arrayList = this.contacts;
        }
        return arrayList;
    }

    public final Vector<ContactlistItem> getAllForDisplay() {
        Vector<ContactlistItem> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem it2 : this.contacts) {
                if (it2 != null && it2.itemType != 4) {
                    switch (it2.itemType) {
                        case 1:
                            list.add(it2);
                            break;
                        case 2:
                            ICQGroup group = (ICQGroup) it2;
                            //noinspection IfStatementWithIdenticalBranches
                            if (group.id == -1) {
                                break;
                            } else {
                                list.add(it2);
                                break;
                            }
                    }
                }
            }
        }
        return list;
    }

    public final void saveToLocalStorage() {
        synchronized (ContactsAdapter.locker) {
            final File roster = new File(resources.dataPath + this.profile.ID + "/roster.bin");
            sort();
            this.profile.svc.handleContactlistNeedRemake();
            Thread save_thread = new Thread() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(1);
                    Contactlist.this.saveToLocalStorageA(roster);
                }
            };
            save_thread.start();
        }
    }

    private void saveToLocalStorageA(File roster) {
        synchronized (ContactsAdapter.locker) {
            DataOutputStream dos = null;
            try {
                try {
                    //noinspection IOStreamConstructor
                    DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(roster));
                    //noinspection CaughtExceptionImmediatelyRethrown
                    try {
                        int count = this.contacts.size();
                        if (count > 0) {
                            for (int i = 0; i < count; i++) {
                                ContactlistItem it = this.contacts.get(i);
                                switch (it.itemType) {
                                    case 1:
                                        ICQContact contact = (ICQContact) it;
                                        dos2.writeByte(1);
                                        write1251ToStream(dos2, contact.ID);
                                        write1251ToStream(dos2, contact.name);
                                        dos2.writeInt(contact.group);
                                        dos2.writeBoolean(contact.authorized);
                                        break;
                                    case 2:
                                        ICQGroup item = (ICQGroup) it;
                                        dos2.writeByte(2);
                                        write1251ToStream(dos2, item.name);
                                        dos2.writeInt(item.id);
                                        break;
                                }
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
                } catch (Exception ignored) {

                }
                try {
                    //noinspection DataFlowIssue
                    dos.close();
                } catch (Exception ignored) {
                }
            } catch (Throwable ignored) {

            }
        }
    }

    private void write1251ToStream(DataOutputStream stream, String what) throws Exception {
        byte[] name = what.getBytes("unicode");
        stream.writeShort(name.length);
        stream.write(name);
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private String read1251FromStream(DataInputStream stream) throws Exception {
        int len = stream.readShort();
        byte[] name = new byte[len];
        stream.read(name);
        //noinspection UnnecessaryLocalVariable
        String result = new String(name, "unicode");
        return result;
    }

    public void loadFromLocalStorage(File roster, ICQProfile profile) {
        synchronized (ContactsAdapter.locker) {
            DataInputStream dis = null;
            try {
                try {
                    //noinspection IOStreamConstructor
                    DataInputStream dis2 = new DataInputStream(new FileInputStream(roster));
                    while (dis2.available() > 0) {
                        //noinspection CaughtExceptionImmediatelyRethrown
                        try {
                            int itemType = dis2.readByte();
                            switch (itemType) {
                                case 1:
                                    ICQContact contact = new ICQContact();
                                    contact.ID = read1251FromStream(dis2);
                                    contact.name = read1251FromStream(dis2);
                                    contact.profile = profile;
                                    contact.group = dis2.readInt();
                                    contact.authorized = dis2.readBoolean();
                                    contact.init();
                                    this.contacts.add(contact);
                                    break;
                                case 2:
                                    ICQGroup group = new ICQGroup();
                                    group.name = read1251FromStream(dis2);
                                    group.id = dis2.readInt();
                                    //noinspection deprecation
                                    group.opened = PreferenceManager.getDefaultSharedPreferences(profile.svc).getBoolean("g" + group.id, true);
                                    group.profile = profile;
                                    this.contacts.add(group);
                                    break;
                            }
                        } catch (Exception e) {
                            dis = dis2;
                            //noinspection CallToPrintStackTrace
                            e.printStackTrace();
                            dis.close();
                            sort();
                            profile.svc.handleContactlistNeedRemake();
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                    dis = dis2;
                } catch (Exception ignored) {

                }
                try {
                    //noinspection DataFlowIssue
                    dis.close();
                } catch (Exception ignored) {
                }
                sort();
                profile.svc.handleContactlistNeedRemake();
            } catch (Throwable ignored) {

            }
        }
    }

    public final ICQGroup getGroupById(int id) {
        synchronized (ContactsAdapter.locker) {
            for (ContactlistItem it2 : this.contacts) {
                if (it2.itemType == 2) {
                    ICQGroup item = (ICQGroup) it2;
                    if (item.id == id) {
                        return item;
                    }
                }
            }
            return null;
        }
    }

    public final Vector<ICQContact> getContacts() {
        Vector<ICQContact> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem it2 : this.contacts) {
                if (it2.itemType == 1) {
                    list.add((ICQContact) it2);
                }
            }
        }
        return list;
    }

    public final Vector<ICQContact> getContactsByGroupId(int id) {
        Vector<ICQContact> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem it2 : this.contacts) {
                if (it2.itemType == 1 && ((ICQContact) it2).group == id) {
                    list.add((ICQContact) it2);
                }
            }
        }
        return list;
    }

    public final Vector<ContactlistItem> getContactsA() {
        Vector<ContactlistItem> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem it2 : this.contacts) {
                if (it2.itemType == 1) {
                    list.add(it2);
                }
            }
        }
        return list;
    }

    public final Vector<ICQGroup> getGroups(boolean with_not_in_list) {
        Vector<ICQGroup> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            for (ContactlistItem it2 : this.contacts) {
                if (it2.itemType == 2) {
                    if (with_not_in_list) {
                        list.add((ICQGroup) it2);
                    } else if (!((ICQGroup) it2).isNotIntList && ((ICQGroup) it2).id != -1) {
                        list.add((ICQGroup) it2);
                    }
                }
            }
        }
        return list;
    }

    public final void sort() {
        synchronized (ContactsAdapter.locker) {
            this.contacts.trimToSize();
            Vector<ICQContact> cnts = new Vector<>();
            int i = 0;
            while (i < this.contacts.size()) {
                if (this.contacts.get(i).itemType == 1) {
                    cnts.addElement((ICQContact) this.contacts.remove(i));
                    i--;
                }
                i++;
            }
            Collections.sort(cnts);
            Collections.sort(this.contacts);
            int i2 = 0;
            while (i2 < this.contacts.size()) {
                ContactlistItem it = this.contacts.get(i2);
                if (it.itemType == 2) {
                    Vector<ICQContact> cnts_ = getGroupContacts(cnts, ((ICQGroup) it).id);
                    this.contacts.addAll(i2 + 1, cnts_);
                    i2 += cnts_.size();
                }
                i2++;
            }
        }
    }

    private Vector<ICQContact> getGroupContacts(Vector<ICQContact> contacts, int group_id) {
        Vector<ICQContact> list;
        synchronized (ContactsAdapter.locker) {
            list = new Vector<>();
            int i = 0;
            while (i < contacts.size()) {
                ContactlistItem it = contacts.get(i);
                //noinspection CastCanBeRemovedNarrowingVariableType
                if (it.itemType == 1 && ((ICQContact) it).group == group_id) {
                    list.addElement(contacts.remove(i));
                    i--;
                }
                i++;
            }
        }
        return list;
    }

    public final void setAllContactsOffline() {
        synchronized (ContactsAdapter.locker) {
            for (ContactlistItem it2 : this.contacts) {
                if (it2.itemType == 1) {
                    ICQContact contact = (ICQContact) it2;
                    contact.xstatus = null;
                    contact.away_status = "";
                    contact.status = -1;
                    contact.typing = false;
                    contact.presence_initialized = false;
                }
            }
        }
    }

    public final boolean isAnyChatOpened() {
        synchronized (ContactsAdapter.locker) {
            for (int i = 0; i < this.contacts.size(); i++) {
                ContactlistItem it = this.contacts.get(i);
                if (it.itemType == 1 && ((ICQContact) it).isChating) {
                    return true;
                }
            }
            return false;
        }
    }
}