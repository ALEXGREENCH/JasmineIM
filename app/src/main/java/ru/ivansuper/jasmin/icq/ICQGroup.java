package ru.ivansuper.jasmin.icq;

import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.jabber.JProfile;

public class ICQGroup extends ContactlistItem {
    public int id;
    public JProfile jprofile;
    public ICQProfile profile;
    public boolean opened = true;
    /** @noinspection unused*/
    public boolean hideOffline = false;
    public boolean isNotIntList = false;
    public int online = 0;
    public int total = 0;

    public ICQGroup() {
        this.itemType = 2;
    }

    /** @noinspection unused*/
    public boolean isEmptyForScreen() {
        return false;
    }
}