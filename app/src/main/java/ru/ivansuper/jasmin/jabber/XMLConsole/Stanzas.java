package ru.ivansuper.jasmin.jabber.XMLConsole;

import ru.ivansuper.jasmin.jabber.XMLFormat;

public class Stanzas {
    public static final int INCOMING = 0;
    public static final int OUTGOING = 1;
    public int direction;
    public CharSequence xml;

    public Stanzas(String str, int i) {
        this.xml = "";
        this.xml = XMLFormat.format(str);
        this.direction = i;
    }
}
