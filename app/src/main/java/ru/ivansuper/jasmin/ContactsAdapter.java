package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Vector;

import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPGroup;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnAdapter;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQGroup;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JGroup;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.conference.ConferenceItem;
import ru.ivansuper.jasmin.protocols.IMProfile;

public class ContactsAdapter extends MultiColumnAdapter {
    public static final Object locker = new Object();
    /** @noinspection FieldCanBeLocal, unused */
    private final Context ctx;
    private boolean only_chats;
    private boolean only_conferences;
    private String pattern;
    private final jasminSvc svc;
    private final Vector<ContactlistItem> list = new Vector<>();
    private final Vector<ContactlistItem> displayList = new Vector<>();
    private boolean separated_chats = false;
    private boolean filtered = false;

    public ContactsAdapter(Context ctxParam, jasminSvc svc, boolean only_chats, boolean only_conferences) {
        //noinspection UnusedAssignment
        this.only_chats = false;
        //noinspection UnusedAssignment
        this.only_conferences = false;
        this.ctx = ctxParam;
        this.svc = svc;
        this.only_chats = only_chats;
        this.only_conferences = only_conferences;
    }

    public void createFromProfileManager(ProfilesManager manager) {
        synchronized (locker) {
            if (!this.only_chats && !this.only_conferences && this.filtered) {
                fill_filtered(manager);
            } else {
                this.separated_chats = PreferenceTable.ms_two_screens_mode;
                if (this.only_chats && !this.separated_chats) {
                    this.displayList.clear();
                    return;
                } else if (this.only_conferences && !this.separated_chats) {
                    this.displayList.clear();
                    return;
                } else if (this.only_chats) {
                    fillChats(manager);
                } else if (this.only_conferences) {
                    fillConferences(manager);
                } else if (!PreferenceTable.simple_cl) {
                    if (!PreferenceTable.showGroups) {
                        fillWithoutGroups(manager);
                    } else {
                        fillWithGroups(manager);
                    }
                } else {
                    fill_without_all(manager);
                }
            }
            notifyDataSetChanged();
        }
    }

    public void setFilter(String pattern) {
        this.pattern = pattern;
        if (pattern == null) {
            this.filtered = false;
            this.svc.handleContactlistNeedRemake();
        } else if (pattern.isEmpty()) {
            this.filtered = false;
            this.svc.handleContactlistNeedRemake();
        } else {
            this.pattern = this.pattern.toLowerCase();
            this.filtered = true;
            this.svc.handleContactlistNeedRemake();
        }
    }

    public void fillWithGroups(ProfilesManager manager) {
        this.displayList.clear();
        Vector<IMProfile> profiles = manager.getProfiles();
        int sz = profiles.size();
        int chat_idx = 0;
        for (int i = 0; i < sz; i++) {
            IMProfile im_profile = profiles.get(i);
            if ((im_profile.connected || !PreferenceTable.ms_hide_not_connected_profiles) && im_profile.enabled) {
                switch (im_profile.profile_type) {
                    case 0:
                        ICQProfile profile = (ICQProfile) im_profile;
                        ICQGroup grp = new ICQGroup();
                        grp.name = profile.ID;
                        grp.itemType = 3;
                        grp.profile = profile;
                        this.displayList.add(grp);
                        if (!PreferenceTable.ms_chats_at_top) {
                            chat_idx = this.displayList.size();
                        }
                        boolean vis = grp.profile.openedInContactList;
                        Vector<ContactlistItem> contacts = profile.contactlist.getAllForDisplay();
                        int online_idx = this.displayList.size();
                        boolean skip = false;
                        for (int ii = 0; ii < contacts.size(); ii++) {
                            ContactlistItem item = contacts.get(ii);
                            switch (item.itemType) {
                                case 1:
                                    ICQContact contact = (ICQContact) item;
                                    grp.total++;
                                    if (contact.status > -1) {
                                        grp.online++;
                                    }
                                    if (contact.isChating) {
                                        if (this.separated_chats) {
                                            if (vis && !skip) {
                                                if (contact.status > -1) {
                                                    this.displayList.insertElementAt(contact, online_idx);
                                                    online_idx++;
                                                    break;
                                                } else if (PreferenceTable.hideOffline) {
                                                    break;
                                                } else {
                                                    this.displayList.addElement(contact);
                                                    break;
                                                }
                                            }
                                        } else if (!PreferenceTable.ms_chats_at_top && !vis) {
                                            break;
                                        } else {
                                            this.displayList.insertElementAt(contact, chat_idx);
                                            chat_idx++;
                                            online_idx++;
                                            break;
                                        }
                                    } else if (!skip && vis) {
                                        if (contact.status > -1) {
                                            this.displayList.insertElementAt(contact, online_idx);
                                            online_idx++;
                                            break;
                                        } else if (PreferenceTable.hideOffline) {
                                            break;
                                        } else {
                                            this.displayList.addElement(contact);
                                            break;
                                        }
                                    }
                                    break;
                                case 2:
                                    ICQGroup group = (ICQGroup) item;
                                    if (vis) {
                                        if (skip) {
                                            skip = false;
                                        }
                                        GroupPresenceInfo gpi = profile.contactlist.getGroupPresenceInfo(group.id);
                                        if (!gpi.empty_for_display) {
                                            group.online = gpi.online;
                                            group.total = gpi.total;
                                            if (!group.opened) {
                                                skip = true;
                                            }
                                            this.displayList.addElement(group);
                                            online_idx = this.displayList.size();
                                        }
                                    }
                                    break;
                            }
                        }
                        continue;
                    case 1:
                        JProfile jprofile = (JProfile) profiles.get(i);
                        ICQGroup grp2 = new ICQGroup();
                        grp2.id = jprofile.ID.hashCode();
                        grp2.name = jprofile.ID;
                        grp2.itemType = 5;
                        grp2.jprofile = jprofile;
                        this.displayList.add(grp2);
                        int profile_idx = this.displayList.size();
                        if (!PreferenceTable.ms_chats_at_top) {
                            chat_idx = this.displayList.size();
                        }
                        boolean vis2 = grp2.jprofile.openedInContactList;
                        Vector<ContactlistItem> jcontacts = jprofile.contacts;
                        int online_idx2 = this.displayList.size();
                        boolean skip2 = false;
                        String current_group = "";
                        int current_group_id = 0;
                        for (ContactlistItem item2 : jcontacts) {
                            switch (item2.itemType) {
                                case 4:
                                    JContact jcontact = (JContact) item2;
                                    if (skip2 && !jcontact.group.equals(current_group) && (!jcontact.group.isEmpty() || current_group_id != -1)) {
                                        skip2 = false;
                                    }
                                    if (!current_group.equals(jcontact.group)) {
                                        current_group = jcontact.group;
                                        online_idx2 = this.displayList.size();
                                    }
                                    grp2.total++;
                                    if (jcontact.isOnline()) {
                                        grp2.online++;
                                    }
                                    if (jcontact.isChating) {
                                        if (this.separated_chats) {
                                            if (vis2 && !skip2) {
                                                if (jcontact.isOnline()) {
                                                    this.displayList.insertElementAt(jcontact, online_idx2);
                                                    online_idx2++;
                                                    break;
                                                } else if (PreferenceTable.hideOffline) {
                                                    break;
                                                } else {
                                                    this.displayList.addElement(jcontact);
                                                    break;
                                                }
                                            }
                                        } else if (!PreferenceTable.ms_chats_at_top && !vis2) {
                                            break;
                                        } else {
                                            this.displayList.insertElementAt(jcontact, chat_idx);
                                            chat_idx++;
                                            online_idx2++;
                                            break;
                                        }
                                    } else if (!skip2 && vis2) {
                                        if (jcontact.isOnline()) {
                                            this.displayList.insertElementAt(jcontact, online_idx2);
                                            online_idx2++;
                                            break;
                                        } else if (PreferenceTable.hideOffline) {
                                            break;
                                        } else {
                                            this.displayList.addElement(jcontact);
                                            break;
                                        }
                                    }
                                    break;
                                case 6:
                                    JGroup jgroup = (JGroup) item2;
                                    current_group = jgroup.name;
                                    current_group_id = jgroup.id;
                                    if (vis2) {
                                        if (skip2) {
                                            skip2 = false;
                                        }
                                        GroupPresenceInfo gpi2 = jprofile.getGroupPresenceInfo(jgroup);
                                        if (!gpi2.empty_for_display) {
                                            jgroup.online = gpi2.online;
                                            jgroup.total = gpi2.total;
                                            if (!jgroup.opened) {
                                                skip2 = true;
                                            }
                                            this.displayList.addElement(jgroup);
                                            online_idx2 = this.displayList.size();
                                        }
                                    }
                                    break;
                                case 10:
                                    Conference conference = ((ConferenceItem) item2).conference;
                                    if (conference.isOnline()) {
                                        grp2.online++;
                                    }
                                    grp2.total++;
                                    if (vis2 && !this.separated_chats) {
                                        this.displayList.insertElementAt(item2, profile_idx);
                                        online_idx2++;
                                        if (!PreferenceTable.ms_chats_at_top) {
                                            chat_idx++;
                                        }
                                        break;
                                    }
                                    break;
                            }
                        }
                        continue;
                    case 2:
                        MMPProfile mmp_profile = (MMPProfile) im_profile;
                        MMPGroup mmp_grp = new MMPGroup(mmp_profile.ID, mmp_profile, 0, -1);
                        mmp_grp.itemType = 8;
                        this.displayList.add(mmp_grp);
                        if (!PreferenceTable.ms_chats_at_top) {
                            chat_idx = this.displayList.size();
                        }
                        boolean vis3 = mmp_grp.profile.openedInContactList;
                        Vector<ContactlistItem> mmp_contacts = mmp_profile.contacts;
                        int online_idx3 = this.displayList.size();
                        boolean skip3 = false;
                        for (int ii2 = 0; ii2 < mmp_contacts.size(); ii2++) {
                            ContactlistItem item3 = mmp_contacts.get(ii2);
                            switch (item3.itemType) {
                                case 7:
                                    MMPContact mmp_contact = (MMPContact) item3;
                                    if (mmp_contact.status != 0) {
                                        mmp_grp.online++;
                                    }
                                    mmp_grp.total++;
                                    if (mmp_contact.isChating) {
                                        if (this.separated_chats) {
                                            if (vis3 && !skip3) {
                                                if (mmp_contact.status > 0) {
                                                    this.displayList.insertElementAt(mmp_contact, online_idx3);
                                                    online_idx3++;
                                                    break;
                                                } else if (PreferenceTable.hideOffline) {
                                                    break;
                                                } else {
                                                    this.displayList.addElement(mmp_contact);
                                                    break;
                                                }
                                            }
                                        } else if (!PreferenceTable.ms_chats_at_top && !vis3) {
                                            break;
                                        } else {
                                            this.displayList.insertElementAt(mmp_contact, chat_idx);
                                            chat_idx++;
                                            online_idx3++;
                                            break;
                                        }
                                    } else if (vis3 && !skip3) {
                                        if (mmp_contact.status > 0) {
                                            this.displayList.insertElementAt(mmp_contact, online_idx3);
                                            online_idx3++;
                                            break;
                                        } else if (PreferenceTable.hideOffline) {
                                            break;
                                        } else {
                                            this.displayList.addElement(mmp_contact);
                                            break;
                                        }
                                    }
                                    break;
                                case 9:
                                    MMPGroup mmp_group = (MMPGroup) item3;
                                    if (vis3) {
                                        if (skip3) {
                                            skip3 = false;
                                        }
                                        GroupPresenceInfo gpi3 = mmp_profile.getGroupPresenceInfo(mmp_group.id);
                                        if (!gpi3.empty_for_display) {
                                            mmp_group.online = gpi3.online;
                                            mmp_group.total = gpi3.total;
                                            if (!mmp_group.opened) {
                                                skip3 = true;
                                            }
                                            this.displayList.addElement(mmp_group);
                                            online_idx3 = this.displayList.size();
                                        }
                                    }
                                    break;
                            }
                        }
                }
            }
        }
    }

    public void fillWithoutGroups(ProfilesManager manager) {
        this.displayList.clear();
        Vector<IMProfile> profiles = manager.getProfiles();
        int sz = profiles.size();
        int chat_idx = 0;
        for (int i = 0; i < sz; i++) {
            IMProfile im_profile = profiles.get(i);
            if ((im_profile.connected || !PreferenceTable.ms_hide_not_connected_profiles) && im_profile.enabled) {
                switch (im_profile.profile_type) {
                    case 0:
                        ICQProfile profile = (ICQProfile) profiles.get(i);
                        ICQGroup grp = new ICQGroup();
                        grp.name = profile.ID;
                        grp.itemType = 3;
                        grp.profile = profile;
                        this.displayList.add(grp);
                        if (!PreferenceTable.ms_chats_at_top) {
                            chat_idx = this.displayList.size();
                        }
                        boolean vis = grp.profile.openedInContactList;
                        Vector<ICQContact> contacts = profile.contactlist.getContacts();
                        Collections.sort(contacts);
                        int online_idx = this.displayList.size();
                        for (int ii = 0; ii < contacts.size(); ii++) {
                            ICQContact contact = contacts.get(ii);
                            if (contact.status > -1) {
                                grp.online++;
                            }
                            grp.total++;
                            if (contact.isChating) {
                                if (this.separated_chats) {
                                    if (vis) {
                                        if (contact.status > -1) {
                                            this.displayList.insertElementAt(contact, online_idx);
                                            online_idx++;
                                        } else if (!PreferenceTable.hideOffline) {
                                            this.displayList.addElement(contact);
                                        }
                                    }
                                } else if (PreferenceTable.ms_chats_at_top || vis) {
                                    this.displayList.insertElementAt(contact, chat_idx);
                                    chat_idx++;
                                    online_idx++;
                                }
                            } else if (vis) {
                                if (contact.status > -1) {
                                    this.displayList.insertElementAt(contact, online_idx);
                                    online_idx++;
                                } else if (!PreferenceTable.hideOffline) {
                                    this.displayList.addElement(contact);
                                }
                            }
                        }
                        continue;
                    case 1:
                        JProfile vprofile = (JProfile) profiles.get(i);
                        ICQGroup grp2 = new ICQGroup();
                        grp2.id = vprofile.ID.hashCode();
                        grp2.name = vprofile.ID;
                        grp2.itemType = 5;
                        grp2.jprofile = vprofile;
                        this.displayList.add(grp2);
                        int profile_idx = this.displayList.size();
                        if (!PreferenceTable.ms_chats_at_top) {
                            chat_idx = this.displayList.size();
                        }
                        boolean vis2 = grp2.jprofile.openedInContactList;
                        Vector<ContactlistItem> jcontacts = vprofile.contacts;
                        Collections.sort(jcontacts);
                        int online_idx2 = this.displayList.size();
                        for (int ii2 = 0; ii2 < jcontacts.size(); ii2++) {
                            ContactlistItem item = jcontacts.get(ii2);
                            switch (item.itemType) {
                                case 4:
                                    JContact jcontact = (JContact) item;
                                    if (jcontact.isOnline()) {
                                        grp2.online++;
                                    }
                                    grp2.total++;
                                    if (jcontact.isChating) {
                                        if (this.separated_chats) {
                                            if (vis2) {
                                                if (jcontact.isOnline()) {
                                                    this.displayList.insertElementAt(jcontact, online_idx2);
                                                    online_idx2++;
                                                    break;
                                                } else if (PreferenceTable.hideOffline) {
                                                    break;
                                                } else {
                                                    this.displayList.addElement(jcontact);
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        } else if (!PreferenceTable.ms_chats_at_top && !vis2) {
                                            break;
                                        } else {
                                            this.displayList.insertElementAt(jcontact, chat_idx);
                                            chat_idx++;
                                            online_idx2++;
                                            break;
                                        }
                                    } else if (vis2) {
                                        if (jcontact.isOnline()) {
                                            this.displayList.insertElementAt(jcontact, online_idx2);
                                            online_idx2++;
                                            break;
                                        } else if (PreferenceTable.hideOffline) {
                                            break;
                                        } else {
                                            this.displayList.addElement(jcontact);
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                case 10:
                                    Conference conference = ((ConferenceItem) item).conference;
                                    if (conference.isOnline()) {
                                        grp2.online++;
                                    }
                                    grp2.total++;
                                    if (vis2 && !this.separated_chats) {
                                        if (conference.isOnline()) {
                                            this.displayList.insertElementAt(item, chat_idx);
                                            profile_idx++;
                                            chat_idx++;
                                        } else {
                                            this.displayList.insertElementAt(item, profile_idx);
                                            profile_idx++;
                                        }
                                        online_idx2++;
                                        break;
                                    }
                                    break;
                            }
                        }
                        continue;
                    case 2:
                        MMPProfile mmp_profile = (MMPProfile) im_profile;
                        MMPGroup mmp_grp = new MMPGroup(mmp_profile.ID, mmp_profile, 0, -1);
                        mmp_grp.itemType = 8;
                        this.displayList.add(mmp_grp);
                        if (!PreferenceTable.ms_chats_at_top) {
                            chat_idx = this.displayList.size();
                        }
                        boolean vis3 = mmp_grp.profile.openedInContactList;
                        Vector<MMPContact> mmp_contacts = mmp_profile.getContacts();
                        Collections.sort(mmp_contacts);
                        int online_idx3 = this.displayList.size();
                        for (int ii3 = 0; ii3 < mmp_contacts.size(); ii3++) {
                            MMPContact mmp_contact = mmp_contacts.get(ii3);
                            if (mmp_contact.status > 0) {
                                mmp_grp.online++;
                            }
                            mmp_grp.total++;
                            if (mmp_contact.isChating) {
                                if (this.separated_chats) {
                                    if (vis3) {
                                        if (mmp_contact.status > 0) {
                                            this.displayList.insertElementAt(mmp_contact, online_idx3);
                                            online_idx3++;
                                        } else if (!PreferenceTable.hideOffline) {
                                            this.displayList.addElement(mmp_contact);
                                        }
                                    }
                                } else if (PreferenceTable.ms_chats_at_top || vis3) {
                                    this.displayList.insertElementAt(mmp_contact, chat_idx);
                                    chat_idx++;
                                    online_idx3++;
                                }
                            } else if (vis3) {
                                if (mmp_contact.status > 0) {
                                    this.displayList.insertElementAt(mmp_contact, online_idx3);
                                    online_idx3++;
                                } else if (!PreferenceTable.hideOffline) {
                                    this.displayList.addElement(mmp_contact);
                                }
                            }
                        }
                }
            }
        }
    }

    public void fill_without_all(ProfilesManager manager) {
        this.displayList.clear();
        Vector<IMProfile> profiles = manager.getProfiles();
        int sz = profiles.size();
        Vector<ContactlistItem> list = new Vector<>();
        Vector<ContactlistItem> confs = new Vector<>();
        for (int i = 0; i < sz; i++) {
            IMProfile improfile = profiles.get(i);
            if ((improfile.connected || !PreferenceTable.ms_hide_not_connected_profiles) && improfile.enabled) {
                switch (improfile.profile_type) {
                    case 0:
                        ICQProfile profile = (ICQProfile) improfile;
                        Vector<ContactlistItem> contacts = profile.contactlist.getContactsA();
                        list.addAll(contacts);
                        continue;
                    case 1:
                        JProfile jprofile = (JProfile) improfile;
                        Vector<ContactlistItem> jcontacts = jprofile.getContacts();
                        list.addAll(jcontacts);
                        confs.addAll(jprofile.conference_items);
                        continue;
                    case 2:
                        MMPProfile mmp_profile = (MMPProfile) improfile;
                        Vector<ContactlistItem> mmp_contacts = mmp_profile.getContactsA();
                        list.addAll(mmp_contacts);
                }
            }
        }
        Collections.sort(list);
        Collections.sort(confs);
        list.addAll(confs);
        int chat_idx = 0;
        int online_idx = this.displayList.size();
        for (ContactlistItem item : list) {
            switch (item.itemType) {
                case 1:
                    ICQContact icontact = (ICQContact) item;
                    if (icontact.isChating) {
                        if (this.separated_chats) {
                            if (icontact.status > -1) {
                                this.displayList.insertElementAt(icontact, online_idx);
                                online_idx++;
                                break;
                            } else if (PreferenceTable.hideOffline) {
                                break;
                            } else {
                                this.displayList.addElement(icontact);
                                break;
                            }
                        } else {
                            this.displayList.insertElementAt(icontact, chat_idx);
                            chat_idx++;
                            online_idx++;
                            break;
                        }
                    } else if (icontact.status > -1) {
                        this.displayList.insertElementAt(icontact, online_idx);
                        online_idx++;
                        break;
                    } else if (PreferenceTable.hideOffline) {
                        break;
                    } else {
                        this.displayList.addElement(icontact);
                        break;
                    }
                case 4:
                    JContact jcontact = (JContact) item;
                    if (jcontact.isChating) {
                        if (this.separated_chats) {
                            if (jcontact.isOnline()) {
                                this.displayList.insertElementAt(jcontact, online_idx);
                                online_idx++;
                                break;
                            } else if (PreferenceTable.hideOffline) {
                                break;
                            } else {
                                this.displayList.addElement(jcontact);
                                break;
                            }
                        } else {
                            this.displayList.insertElementAt(jcontact, chat_idx);
                            chat_idx++;
                            online_idx++;
                            break;
                        }
                    } else if (jcontact.isOnline()) {
                        this.displayList.insertElementAt(jcontact, online_idx);
                        online_idx++;
                        break;
                    } else if (PreferenceTable.hideOffline) {
                        break;
                    } else {
                        this.displayList.addElement(jcontact);
                        break;
                    }
                case 7:
                    MMPContact mmp_contact = (MMPContact) item;
                    if (mmp_contact.isChating) {
                        if (this.separated_chats) {
                            if (mmp_contact.status > 0) {
                                this.displayList.insertElementAt(mmp_contact, online_idx);
                                online_idx++;
                                break;
                            } else if (PreferenceTable.hideOffline) {
                                break;
                            } else {
                                this.displayList.addElement(mmp_contact);
                                break;
                            }
                        } else {
                            this.displayList.insertElementAt(mmp_contact, chat_idx);
                            chat_idx++;
                            online_idx++;
                            break;
                        }
                    } else if (mmp_contact.status > 0) {
                        this.displayList.insertElementAt(mmp_contact, online_idx);
                        online_idx++;
                        break;
                    } else if (PreferenceTable.hideOffline) {
                        break;
                    } else {
                        this.displayList.addElement(mmp_contact);
                        break;
                    }
                case 10:
                    ConferenceItem conf = (ConferenceItem) item;
                    if (!this.separated_chats) {
                        this.displayList.insertElementAt(conf, online_idx);
                        chat_idx++;
                        online_idx++;
                    }
                    break;
            }
        }
    }

    public void fill_filtered(ProfilesManager manager) {
        //noinspection deprecation
        new FillFilteredTask(manager).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class FillFilteredTask extends AsyncTask<Void, Void, Void> {

        private final ProfilesManager manager;

        /** @noinspection deprecation*/
        public FillFilteredTask(ProfilesManager manager) {
            this.manager = manager;
        }

        /** @noinspection deprecation*/
        @Override
        protected Void doInBackground(Void... params) {
            displayList.clear();
            Vector<IMProfile> profiles = manager.getProfiles();
            Vector<ContactlistItem> list = new Vector<>();
            Vector<ContactlistItem> confs = new Vector<>();

            for (IMProfile improfile : profiles) {
                if ((improfile.connected || !PreferenceTable.ms_hide_not_connected_profiles) && improfile.enabled) {
                    switch (improfile.profile_type) {
                        case 0:
                            ICQProfile profile = (ICQProfile) improfile;
                            Vector<ContactlistItem> contacts = profile.contactlist.getContactsA();
                            list.addAll(contacts);
                            break;
                        case 1:
                            JProfile jprofile = (JProfile) improfile;
                            Vector<ContactlistItem> jcontacts = jprofile.getContacts();
                            list.addAll(jcontacts);
                            confs.addAll(jprofile.conference_items);
                            break;
                        case 2:
                            MMPProfile mmp_profile = (MMPProfile) improfile;
                            Vector<ContactlistItem> mmp_contacts = mmp_profile.getContactsA();
                            list.addAll(mmp_contacts);
                            break;
                    }
                }
            }

            Collections.sort(list);
            Collections.sort(confs);
            list.addAll(confs);

            int online_idx = displayList.size();

            for (ContactlistItem item : list) {
                if (shouldAddItem(item)) {
                    if (item.itemType == 10) {
                        displayList.insertElementAt(item, online_idx);
                    } else {
                        displayList.addElement(item);
                    }
                    online_idx++;
                }
            }

            if (displayList.size() == 1) {
                ADB.setActivated(6);
            }

            return null;
        }

        private boolean shouldAddItem(ContactlistItem item) {
            return item.name.toLowerCase().contains(pattern) || item.ID.toLowerCase().contains(pattern);
        }

        /** @noinspection deprecation*/
        @Override
        protected void onPostExecute(Void result) {
            // notify adapter about changed data after background fill
            notifyDataSetChanged();
        }
    }

    public void fillChats(ProfilesManager manager) {
        //noinspection deprecation
        new FillChatsTask().execute(manager);
    }

    /** @noinspection deprecation*/
    @SuppressLint("StaticFieldLeak")
    private class FillChatsTask extends AsyncTask<ProfilesManager, Void, Void> {

        @Override
        protected Void doInBackground(ProfilesManager... params) {
            ProfilesManager manager = params[0];
            displayList.clear();
            list.clear();
            Vector<IMProfile> profiles = manager.getProfiles();
            int sz = profiles.size();
            Vector<ContactlistItem> list = new Vector<>();
            int active_profiles_count = manager.getActiveProfilesCount();
            for (int i = 0; i < sz; i++) {
                IMProfile improfile = profiles.get(i);
                switch (improfile.profile_type) {
                    case 0:
                        ICQProfile profile = (ICQProfile) improfile;
                        Vector<ContactlistItem> contacts = profile.contactlist.getContactsA();
                        if (profile.contactlist.isAnyChatOpened()) {
                            ContactsSplitter splitter = new ContactsSplitter();
                            splitter.name = improfile.ID;
                            list.add(splitter);
                            Collections.sort(contacts);
                            list.addAll(contacts);
                        }
                        break;
                    case 1:
                        JProfile jprofile = (JProfile) improfile;
                        Vector<ContactlistItem> jcontacts = jprofile.getContacts();
                        if (jprofile.isAnyChatOpened()) {
                            ContactsSplitter splitter2 = new ContactsSplitter();
                            splitter2.name = improfile.ID;
                            list.add(splitter2);
                            Collections.sort(jcontacts);
                            list.addAll(jcontacts);
                        }
                        break;
                    case 2:
                        MMPProfile mmp_profile = (MMPProfile) improfile;
                        Vector<ContactlistItem> mmp_contacts = mmp_profile.getContactsA();
                        if (mmp_profile.isAnyChatOpened()) {
                            ContactsSplitter splitter3 = new ContactsSplitter();
                            splitter3.name = improfile.ID;
                            list.add(splitter3);
                            Collections.sort(mmp_contacts);
                            list.addAll(mmp_contacts);
                        }
                        break;
                }
            }
            if (active_profiles_count < 2 || PreferenceTable.simple_cl) {
                Collections.sort(list);
            }
            int chat_idx = 0;
            //noinspection unused
            int online_idx = displayList.size();
            for (ContactlistItem item : list) {
                switch (item.itemType) {
                    case 1:
                        ICQContact icontact = (ICQContact) item;
                        if (icontact.isChating && separated_chats) {
                            displayList.insertElementAt(icontact, chat_idx);
                            chat_idx++;
                            online_idx++;
                            break;
                        }
                        break;
                    case 4:
                        JContact jcontact = (JContact) item;
                        if (jcontact.isChating && separated_chats) {
                            displayList.insertElementAt(jcontact, chat_idx);
                            chat_idx++;
                            online_idx++;
                            break;
                        }
                        break;
                    case 7:
                        MMPContact mmp_contact = (MMPContact) item;
                        if (mmp_contact.isChating && separated_chats) {
                            displayList.insertElementAt(mmp_contact, chat_idx);
                            chat_idx++;
                            online_idx++;
                            break;
                        }
                        break;
                    case 11:
                        if (active_profiles_count >= 2 && !PreferenceTable.simple_cl) {
                            displayList.add(item);
                            chat_idx++;
                            online_idx++;
                            break;
                        }
                        break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // notify adapter about changed data after background fill
            notifyDataSetChanged();
        }
    }

    public void fillConferences(ProfilesManager manager) {
        //noinspection deprecation
        new FillConferencesTask().execute(manager);
    }

    /** @noinspection deprecation*/
    @SuppressLint("StaticFieldLeak")
    private class FillConferencesTask extends AsyncTask<ProfilesManager, Void, Void> {

        @Override
        protected Void doInBackground(ProfilesManager... params) {
            ProfilesManager manager = params[0];
            displayList.clear();
            list.clear();
            Vector<IMProfile> profiles = manager.getProfiles();
            int sz = profiles.size();
            Vector<ContactlistItem> confs = new Vector<>();
            int active_profiles_count = manager.getConferencedProfilesCount();
            for (int i = 0; i < sz; i++) {
                IMProfile improfile = profiles.get(i);
                if (improfile.connected || !PreferenceTable.ms_hide_not_connected_profiles) {
                    if (improfile.profile_type == 1) {
                        JProfile jprofile = (JProfile) improfile;
                        if (!jprofile.conference_items.isEmpty()) {
                            Collections.sort(jprofile.conference_items);
                            ContactsSplitter splitter = new ContactsSplitter();
                            splitter.name = improfile.ID;
                            confs.add(splitter);
                            confs.addAll(jprofile.conference_items);
                        } else {
                            //noinspection UnnecessaryContinue
                            continue;
                        }
                    }
                }
            }
            if (active_profiles_count < 2 || PreferenceTable.simple_cl) {
                Collections.sort(confs);
            }
            list.addAll(confs);
            int online_idx = displayList.size();
            for (ContactlistItem item : list) {
                switch (item.itemType) {
                    case 10:
                        if (separated_chats) {
                            ConferenceItem conf = (ConferenceItem) item;
                            Conference conf_ = conf.conference;
                            if (conf_.isOnline()) {
                                displayList.insertElementAt(conf, online_idx);
                                online_idx++;
                            } else {
                                displayList.add(conf);
                            }
                        }
                        break;
                    case 11:
                        if (active_profiles_count >= 2 && !PreferenceTable.simple_cl) {
                            displayList.add(item);
                            online_idx = displayList.size();
                            break;
                        }
                        break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // notify adapter about changed data after background fill
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return this.displayList.size();
    }

    /** @noinspection unused*/
    public int getCountA() {
        return this.list.size();
    }

    @Override
    public ContactlistItem getItem(int arg0) {
        return this.displayList.get(arg0);
    }

    /** @noinspection unused*/
    public ContactlistItem getItemA(int arg0) {
        return this.list.get(arg0);
    }

    @Override
    public int getItemType(int idx) {
        if (idx < getCount() && idx >= 0) {
            switch (getItem(idx).itemType) {
                case 2:
                case 3:
                case 5:
                case 6:
                case 8:
                case 9:
                case 11:
                    return 0;
                case 4:
                case 7:
                case 10:
                default:
                    return 1;
            }
        }
        return -1;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int pos) {
        if (pos < getCount() && pos >= 0) {
            return getItem(pos).itemType != 11;
        }
        return false;
    }

    @Override // android.widget.Adapter
    public long getItemId(int arg0) {
        return this.displayList.get(arg0).itemType;
    }

    /** @noinspection unused*/
    public long getItemIdA(int arg0) {
        return this.list.get(arg0).itemType;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        RosterItemView item;
        if (arg1 == null) {
            item = new RosterItemView(resources.ctx);
        } else {
            item = (RosterItemView) arg1;
        }
        ContactlistItem ci = getItem(arg0);
        item.update(ci);
        item.requestLayout();
        return item;
    }

    protected void finalize() throws Throwable {
        Log.e(getClass().getSimpleName(), "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
        super.finalize();
    }
}