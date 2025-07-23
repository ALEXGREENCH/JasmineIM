package ru.ivansuper.jasmin;

/**
 * Represents a splitter item in a contact list.
 * This class extends {@link ContactlistItem} and sets the itemType to 11,
 * indicating that it is a splitter.
 */
public class ContactsSplitter extends ContactlistItem {
    public ContactsSplitter() {
        this.itemType = 11;
    }
}
