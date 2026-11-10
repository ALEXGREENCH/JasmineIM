package ru.ivansuper.jasmin.jabber;

import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;

public abstract class PacketHandler {
    public static long task_id = 0;
    private String ID;
    public boolean runOnUi;
    public Node slot;
    public String type;

    public PacketHandler() {
        generateID();
        this.runOnUi = true;
    }

    public PacketHandler(String str) {
        this.ID = str;
    }

    public PacketHandler(String str, boolean z) {
        this.ID = str;
        this.runOnUi = z;
    }

    public PacketHandler(boolean z) {
        generateID();
        this.runOnUi = z;
    }

    private final void generateID() {
        synchronized (this) {
            this.ID = "jasmine_task_" + String.valueOf(task_id);
            task_id++;
        }
    }

    public abstract void execute();

    public final String getID() {
        return this.ID;
    }
}
