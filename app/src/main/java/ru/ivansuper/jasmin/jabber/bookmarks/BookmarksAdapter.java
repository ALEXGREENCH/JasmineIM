package ru.ivansuper.jasmin.jabber.bookmarks;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;

public class BookmarksAdapter extends BaseAdapter {
    private final Vector<BookmarkItem> mList = new Vector<>();

    private final void computeSeparators() {
        int iIndexOf;
        Iterator<BookmarkItem> it = this.mList.iterator();
        boolean z = false;
        boolean z2 = false;
        while (true) {
            iIndexOf = 1;
            if (!it.hasNext()) {
                break;
            }
            BookmarkItem next = it.next();
            if (next.type == 0) {
                z = true;
            }
            if (next.type == 1) {
                z2 = true;
            }
            if (z && z2) {
                break;
            }
        }
        if (z && z2) {
            int i = this.mList.get(0).type;
            for (BookmarkItem bookmarkItem : this.mList) {
                if (i != bookmarkItem.type) {
                    iIndexOf = this.mList.indexOf(bookmarkItem);
                    break;
                }
                i = bookmarkItem.type;
            }
            BookmarkItem bookmarkItem2 = new BookmarkItem();
            bookmarkItem2.NAME = Locale.getString("s_urls_separator");
            bookmarkItem2.type = 2;
            this.mList.insertElementAt(bookmarkItem2, iIndexOf);
            BookmarkItem bookmarkItem3 = new BookmarkItem();
            bookmarkItem3.NAME = Locale.getString("s_conferences_separator");
            bookmarkItem3.type = 2;
            this.mList.insertElementAt(bookmarkItem3, 0);
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public final void fill(Vector<BookmarkItem> vector) {
        this.mList.clear();
        this.mList.addAll(vector);
        computeSeparators();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.mList.size();
    }

    @Override
    public BookmarkItem getItem(int i) {
        return this.mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Drawable drawable;
        if (view == null) {
            view = View.inflate(resources.ctx, 2130903075, null);
        }
        LinearLayout linearLayout = (LinearLayout) view;
        ImageView imageView = (ImageView) linearLayout.findViewById(2131427568);
        TextView textView = (TextView) linearLayout.findViewById(2131427569);
        BookmarkItem item = getItem(i);
        if (item.type == 2) {
            imageView.setVisibility(8);
            textView.setShadowLayer(1.0f, 0.0f, 0.0f, -16777216);
            linearLayout.setPadding(0, 3, 0, 3);
            textView.setPadding(3, 3, 3, 3);
            textView.setGravity(17);
            textView.setBackgroundColor(1996488704);
        } else {
            imageView.setVisibility(0);
            linearLayout.setPadding(0, 0, 0, 0);
            textView.setPadding(0, 0, 0, 0);
            textView.setShadowLayer(1.0f, 1.0f, 1.0f, -16777216);
            textView.setGravity(3);
            textView.setBackgroundColor(0);
        }
        textView.setTextColor(-1);
        textView.setText(item.NAME == null ? item.JID_OR_URL : item.NAME);
        switch (item.type) {
            case 0:
                drawable = resources.jabber_conference;
                break;
            case 1:
                drawable = resources.url_icon;
                break;
            default:
                return linearLayout;
        }
        imageView.setImageDrawable(drawable);
        return linearLayout;
    }

    @Override
    public boolean isEnabled(int i) {
        return getItem(i).type != 2;
    }
}
