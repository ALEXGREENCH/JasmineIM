package ru.ivansuper.jasmin.jabber.disco;

import java.util.Vector;

public class Item {
    /** @noinspection unused*/
    public static final int START_ROOT_LEVEL = -1;
    /** @noinspection unused*/
    public static final int STATUS_ERROR = 3;
    /** @noinspection unused*/
    public static final int STATUS_LOADING = 2;
    /** @noinspection unused*/
    public static final int STATUS_NORMAL = 1;
    /** @noinspection unused*/
    public static final int STATUS_NOT_LOADED = 0;
    /** @noinspection unused*/
    public static final int TYPE_COMMAND = 2;
    /** @noinspection unused*/
    public static final int TYPE_NORMAL = 0;
    /** @noinspection unused*/
    public static final int TYPE_SERVER = 1;
    public String JID;
    public String NODE;
    public Item PARENT;
    public String XML_NODE;
    private final Vector<Item> items = new Vector<>();
    public int level = 0;
    public int status = 0;
    public int type = 0;
    public final Vector<Identity> identities = new Vector<>();
    public final Vector<String> features = new Vector<>();
    public boolean opened = true;
    public boolean childs_loaded = false;

    public static class Identity {
        public String category;
        public String name;
        public String type;
    }

    public Item(String node, Item parent, String xml_node, String jid) {
        this.PARENT = parent;
        this.NODE = node;
        this.XML_NODE = xml_node;
        this.JID = jid;
    }

    public final void detectType() {
        if (isCategoryPresent("server")) {
            this.type = 1;
        } else if (isTypePresent("command-node")) {
            this.type = 2;
        } else {
            this.type = 0;
        }
    }

    public boolean isTypePresent(String type) {
        for (int i = 0; i < this.identities.size(); i++) {
            Identity idn = this.identities.get(i);
            if (idn.type != null && idn.type.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCategoryPresent(String category) {
        for (int i = 0; i < this.identities.size(); i++) {
            Identity idn = this.identities.get(i);
            if (idn.type != null && idn.category.equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

    public final void append(Item item) {
        this.items.add(item);
    }

    public final void append(Vector<Item> list) {
        this.items.addAll(list);
    }

    public final void clear() {
        this.items.clear();
    }

    public boolean isOpenable() {
        return !this.items.isEmpty();
    }

    public final Vector<Item> toList(int start_level) {
        this.level = start_level + 1;
        Vector<Item> items_ = new Vector<>();
        items_.add(this);
        if (this.opened) {
            for (int i = 0; i < this.items.size(); i++) {
                Item item = this.items.get(i);
                items_.addAll(item.toList(this.level));
            }
        }
        return items_;
    }
}
