package ru.ivansuper.jasmin.irc;

import ru.ivansuper.jasmin.ContactlistItem;

/**
 * Basic IRC contact representation.
 */
public class IRCContact extends ContactlistItem {

    public IRCProfile profile;

    public IRCContact(IRCProfile profile, String nick) {
        this.profile = profile;
        this.ID = nick;
        this.name = nick;
        this.itemType = ContactlistItem.IRC_CONTACT;
    }
}
