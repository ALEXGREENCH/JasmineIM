package ru.ivansuper.jasmin.jabber.bookmarks;

import android.app.Dialog;
import android.util.Log;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.PacketHandler;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.resources;

public class BookmarkList {
    public OnResultListener listener;
    public final BookmarksAdapter mAdapter = new BookmarksAdapter();
    public final Vector<BookmarkItem> mList = new Vector<>();
    private JProfile profile;

    public interface OnResultListener {
        void OnResult();
    }

    public BookmarkList(JProfile jProfile) {
        this.profile = jProfile;
    }

    private final void readFromQuery(Node node) {
        Node nodeFindFirstLocalNodeByNameAndNamespace = node.findFirstLocalNodeByNameAndNamespace("storage", "storage:bookmarks");
        if (nodeFindFirstLocalNodeByNameAndNamespace == null) {
            return;
        }
        this.mList.clear();
        for (Node node2 : nodeFindFirstLocalNodeByNameAndNamespace.childs) {
            String name = node2.getName();
            BookmarkItem bookmarkItem = new BookmarkItem();
            bookmarkItem.NAME = node2.getParameter("name");
            Node nodeFindFirstLocalNodeByName = node2.findFirstLocalNodeByName("nick");
            if (nodeFindFirstLocalNodeByName != null) {
                bookmarkItem.nick = nodeFindFirstLocalNodeByName.getValue();
            }
            Node nodeFindFirstLocalNodeByName2 = node2.findFirstLocalNodeByName("password");
            if (nodeFindFirstLocalNodeByName2 != null) {
                bookmarkItem.password = nodeFindFirstLocalNodeByName2.getValue();
            }
            if (name.equals("url")) {
                bookmarkItem.type = 1;
                bookmarkItem.JID_OR_URL = node2.getParameter("url").trim().toLowerCase();
                this.mList.add(bookmarkItem);
            } else if (name.equals("conference")) {
                bookmarkItem.type = 0;
                bookmarkItem.JID_OR_URL = node2.getParameter("jid").trim().toLowerCase();
                String parameter = node2.getParameter("autojoin");
                bookmarkItem.autojoin = Boolean.parseBoolean(parameter);
                if (!bookmarkItem.autojoin && parameter != null && parameter.equals("1")) {
                    bookmarkItem.autojoin = true;
                }
                this.mList.add(bookmarkItem);
                if (bookmarkItem.autojoin && PreferenceTable.ms_use_bookmark_autojoin && bookmarkItem.JID_OR_URL.split("@").length == 2) {
                    String str = bookmarkItem.nick;
                    if (str == null) {
                        str = this.profile.ID;
                    }
                    if (str.length() == 0) {
                        str = this.profile.ID;
                    }
                    String str2 = bookmarkItem.password;
                    if (str2 == null) {
                        str2 = "";
                    }
                    this.profile.joinConference(bookmarkItem.JID_OR_URL, str, str2);
                }
            }
        }
        Collections.sort(this.mList);
        this.mAdapter.fill(this.mList);
        Log.e(getClass().getSimpleName(), "Bookmarks loaded");
    }

    public final void add(BookmarkItem bookmarkItem) {
        add(bookmarkItem, null);
    }

    public final void add(BookmarkItem bookmarkItem, Dialog dialog) {
        synchronized (this) {
            this.mList.add(bookmarkItem);
            update(dialog);
        }
    }

    public final boolean itIsExist(String str) {
        boolean z;
        synchronized (this) {
            if (str == null) {
                z = true;
            } else {
                Iterator<BookmarkItem> it = this.mList.iterator();
                do {
                    if (!it.hasNext()) {
                        return false;
                    }
                } while (!it.next().JID_OR_URL.equalsIgnoreCase(str));
                z = true;
            }
        }
        return z;
    }

    public final boolean itIsExist(String str, String str2) {
        boolean z;
        synchronized (this) {
            if (str != null && str2 != null) {
                Iterator<BookmarkItem> it = this.mList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        z = false;
                        break;
                    }
                    BookmarkItem next = it.next();
                    if (next.NAME.equalsIgnoreCase(str) && next.JID_OR_URL.equalsIgnoreCase(str2)) {
                        z = true;
                        break;
                    }
                }
            } else {
                z = true;
            }
        }
        return z;
    }

    public final boolean itIsExist(BookmarkItem bookmarkItem) {
        boolean z;
        synchronized (this) {
            for (BookmarkItem bookmarkItem2 : this.mList) {
                if (bookmarkItem2.NAME.equalsIgnoreCase(bookmarkItem.NAME) && bookmarkItem2.JID_OR_URL.equalsIgnoreCase(bookmarkItem.JID_OR_URL)) {
                    return true;
                }
            }
            z = false;
        }
        return z;
    }

    public final void performRequest() {
        PacketHandler packetHandler = new PacketHandler() {
            @Override
            public void execute() {
                Node node = this.slot;
                if (node.getParameter("type").equals("result")) {
                    final Node nodeFindFirstLocalNodeByNameAndNamespace = node.findFirstLocalNodeByNameAndNamespace("query", "jabber:iq:private");
                    if (nodeFindFirstLocalNodeByNameAndNamespace != null) {
                        BookmarkList.this.profile.svc.runOnUi(new Runnable() {
                            @Override
                            public void run() {
                                BookmarkList.this.readFromQuery(nodeFindFirstLocalNodeByNameAndNamespace);
                            }
                        });
                    }
                    if (BookmarkList.this.listener == null) {
                        return;
                    }
                } else if (BookmarkList.this.listener == null) {
                    return;
                }
                BookmarkList.this.listener.OnResult();
            }
        };
        this.profile.putPacketHandler(packetHandler);
        Node node = new Node("iq");
        node.putParameter("type", "get").putParameter("id", packetHandler.getID());
        Node node2 = new Node("query", "", "jabber:iq:private");
        node2.putChild(new Node("storage", "", "storage:bookmarks"));
        node.putChild(node2);
        this.profile.stream.write(node, this.profile);
    }

    public final void remove(BookmarkItem bookmarkItem) {
        remove(bookmarkItem, null);
    }

    public final void remove(BookmarkItem bookmarkItem, Dialog dialog) {
        synchronized (this) {
            this.mList.remove(bookmarkItem);
            update(dialog);
        }
    }

    public final void update() {
        update(null);
    }

    public final void update(final Dialog dialog) {
        synchronized (this) {
            resources.service.runOnUi(new Runnable() {
                @Override
                public void run() {
                    Collections.sort(BookmarkList.this.mList);
                    BookmarkList.this.mAdapter.fill(BookmarkList.this.mList);
                    final Dialog dialog2 = dialog;
                    PacketHandler packetHandler = new PacketHandler() {
                        @Override
                        public void execute() {
                            if (!this.slot.getParameter("type").equals("result")) {
                                if (BookmarkList.this.listener != null) {
                                    BookmarkList.this.listener.OnResult();
                                }
                            } else {
                                if (BookmarkList.this.listener != null) {
                                    BookmarkList.this.listener.OnResult();
                                }
                                if (dialog2 != null) {
                                    dialog2.dismiss();
                                }
                            }
                        }
                    };
                    BookmarkList.this.profile.putPacketHandler(packetHandler);
                    Node node = new Node("iq");
                    node.putParameter("type", "set").putParameter("id", packetHandler.getID());
                    Node node2 = new Node("query", "", "jabber:iq:private");
                    Node node3 = new Node("storage", "", "storage:bookmarks");
                    for (BookmarkItem bookmarkItem : BookmarkList.this.mList) {
                        switch (bookmarkItem.type) {
                            case 0:
                                Node node4 = new Node("conference");
                                node4.putParameter("name", bookmarkItem.NAME).putParameter("jid", bookmarkItem.JID_OR_URL).putParameter("autojoin", String.valueOf(bookmarkItem.autojoin));
                                if (bookmarkItem.nick != null) {
                                    node4.putChild(new Node("nick", bookmarkItem.nick));
                                }
                                if (bookmarkItem.password != null) {
                                    node4.putChild(new Node("password", bookmarkItem.password));
                                }
                                node3.putChild(node4);
                                break;
                            case 1:
                                Node node5 = new Node("url");
                                node5.putParameter("name", bookmarkItem.NAME).putParameter("url", bookmarkItem.JID_OR_URL);
                                node3.putChild(node5);
                                break;
                        }
                    }
                    node2.putChild(node3);
                    node.putChild(node2);
                    BookmarkList.this.profile.stream.write(node, BookmarkList.this.profile);
                }
            });
        }
    }
}
