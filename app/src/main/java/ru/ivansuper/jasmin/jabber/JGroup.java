package ru.ivansuper.jasmin.jabber;

import android.preference.PreferenceManager;
import ru.ivansuper.jasmin.ContactlistItem;

public class JGroup extends ContactlistItem {
    public int id;
    public JProfile jprofile;
    public boolean opened;
    public boolean hideOffline = false;
    public int online = 0;
    public int total = 0;

    public JGroup(JProfile jProfile, String str) {
        this.opened = true;
        this.itemType = 6;
        this.jprofile = jProfile;
        this.name = str;
        this.opened = PreferenceManager.getDefaultSharedPreferences(jProfile.svc).getBoolean("jg" + str, true);
    }

    @Override
    public int getHash() {
        return (String.valueOf(this.name) + this.id).hashCode();
    }

    @Override
    public void update(ContactlistItem contactlistItem) {
    }
}
