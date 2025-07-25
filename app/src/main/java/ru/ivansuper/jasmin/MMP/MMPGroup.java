package ru.ivansuper.jasmin.MMP;

import android.preference.PreferenceManager;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.resources;

/**
 * Represents a group of contacts in the MMP protocol.
 * Extends ContactlistItem to be displayed in a contact list.
 */
public class MMPGroup extends ContactlistItem {
    public int flags;
    public int id;
    public int online;
    public boolean opened;
    public MMPProfile profile;
    public int total;

    public MMPGroup(String NAME, MMPProfile profile, int flags, int id) {
        this.opened = true;
        this.itemType = 9;
        this.name = NAME;
        this.profile = profile;
        this.flags = flags;
        this.id = id;
        this.opened = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getBoolean("mmpg" + id, true);
    }
}
