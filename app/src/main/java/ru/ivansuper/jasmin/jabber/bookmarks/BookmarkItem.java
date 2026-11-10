package ru.ivansuper.jasmin.jabber.bookmarks;

public class BookmarkItem implements Comparable<BookmarkItem> {
    public static final int TYPE_CONFERENCE = 0;
    public static final int TYPE_SEPARATOR = 2;
    public static final int TYPE_URL = 1;
    public String JID_OR_URL;
    public String NAME;
    public boolean autojoin;
    public String nick;
    public String password;
    public int type;

    @Override
    public int compareTo(BookmarkItem bookmarkItem) {
        if (this.type < bookmarkItem.type) {
            return -1;
        }
        if (this.type > bookmarkItem.type) {
            return 1;
        }
        int iCompareTo = (this.NAME == null || bookmarkItem.NAME == null) ? 0 : this.NAME.compareTo(bookmarkItem.NAME);
        return (iCompareTo != 0 || this.JID_OR_URL == null || bookmarkItem.JID_OR_URL == null) ? iCompareTo : this.JID_OR_URL.compareTo(bookmarkItem.JID_OR_URL);
    }
}
