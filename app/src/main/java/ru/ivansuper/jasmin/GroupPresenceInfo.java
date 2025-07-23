package ru.ivansuper.jasmin;

/**
 * Represents the presence information for a group of contacts.
 * This class stores the number of online contacts, the total number of contacts,
 * and a flag indicating whether the group should be considered empty for display purposes.
 */
public class GroupPresenceInfo {
    public int online = 0;
    public int total = 0;
    public boolean empty_for_display = true;
}
