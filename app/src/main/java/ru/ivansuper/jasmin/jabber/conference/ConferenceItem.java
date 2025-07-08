package ru.ivansuper.jasmin.jabber.conference;

import ru.ivansuper.jasmin.ContactlistItem;

public class ConferenceItem extends ContactlistItem {
    public Conference conference;
    public boolean hasUnreadMessages = false;
    private int unread_count;

    public ConferenceItem() {
        this.itemType = 10;
    }

    public void setHasUnreadMessages() {
        this.unread_count++;
        this.hasUnreadMessages = true;
    }

    public void setHasNoUnreadMessages() {
        this.hasUnreadMessages = false;
        this.unread_count = 0;
    }

    public int getUnreadCount() {
        return this.unread_count;
    }
}
