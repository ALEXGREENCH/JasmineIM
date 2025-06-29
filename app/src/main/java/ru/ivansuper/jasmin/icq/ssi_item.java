package ru.ivansuper.jasmin.icq;

import ru.ivansuper.jasmin.ContactlistItem;

public class ssi_item extends ContactlistItem {
    public int id = 0;
    public int listType = 0;
    public String uin;

    public ssi_item() {
        this.itemType = 5;
    }
}
