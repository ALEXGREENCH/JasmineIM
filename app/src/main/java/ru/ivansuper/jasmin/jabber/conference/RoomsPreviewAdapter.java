package ru.ivansuper.jasmin.jabber.conference;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/* loaded from: classes.dex */
public class RoomsPreviewAdapter extends BaseAdapter {
    public boolean init;
    private Vector<Item> list = new Vector<>();
    private Vector<Item> display = new Vector<>();

    /* loaded from: classes.dex */
    public static class Item {
        public String desc;
        public String label;
    }

    public RoomsPreviewAdapter() {
        Item item = new Item();
        item.label = resources.getString("s_conference_rooms_loading_1");
        item.desc = resources.getString("s_conference_rooms_loading_2");
        this.list.add(item);
        refresh();
    }

    private void refresh() {
        this.display.clear();
        this.display.addAll(this.list);
        notifyDataSetInvalidated();
    }

    public void clear() {
        this.list.clear();
    }

    public void fill(Vector<Item> list) {
        this.list.clear();
        this.list.addAll(list);
        this.init = true;
        refresh();
    }

    public void error() {
        this.list.clear();
        Item item = new Item();
        item.label = resources.getString("s_conference_rooms_loading_error_1");
        item.desc = resources.getString("s_conference_rooms_loading_error_2");
        this.list.add(item);
        refresh();
    }

    public void applyFilter(String expr) {
        if (this.init) {
            this.display.clear();
            for (int i = 0; i < this.list.size(); i++) {
                Item item = this.list.get(i);
                boolean a = false;
                boolean b = false;
                try {
                    a = item.label.toLowerCase().indexOf(expr.toLowerCase()) >= 0;
                } catch (Exception e) {
                }
                try {
                    b = item.desc.toLowerCase().indexOf(expr.toLowerCase()) >= 0;
                } catch (Exception e2) {
                }
                if (a || b) {
                    this.display.add(item);
                }
            }
            notifyDataSetInvalidated();
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.display.size();
    }

    @Override // android.widget.Adapter
    public Item getItem(int arg0) {
        return this.display.get(arg0);
    }

    @Override // android.widget.Adapter
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override // android.widget.Adapter
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout item;
        if (arg1 == null) {
            item = (LinearLayout) View.inflate(resources.ctx, R.layout.room_preview_item, null);
        } else {
            item = (LinearLayout) arg1;
        }
        TextView label = (TextView) item.findViewById(R.id.room_preview_item_label);
        TextView desc = (TextView) item.findViewById(R.id.room_preview_item_desc);
        Item i = getItem(arg0);
        label.setText(i.label);
        desc.setText(i.desc);
        return item;
    }
}