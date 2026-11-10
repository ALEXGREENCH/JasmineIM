package ru.ivansuper.jasmin.jabber.commands;

public class CommandItem {
    public String jid;
    public String name;
    public String node;

    public CommandItem(String str, String str2, String str3) {
        this.jid = str;
        this.node = str2;
        this.name = str3;
    }
}
