package ru.ivansuper.jasmin.icq;

import ru.ivansuper.jasmin.ContactlistItem;

/**
 * Represents an item in the Server-Side Information (SSI) list.
 * SSI items are used by ICQ to store contact list information on the server.
 * This class extends {@link ContactlistItem} and adds specific properties for SSI items.
 */
public class ssi_item extends ContactlistItem {
    public int id = 0;
    public int listType = 0;
    public String uin;

    public ssi_item() {
        this.itemType = 5;
    }
}
