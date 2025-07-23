package ru.ivansuper.jasmin.jabber.conference;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Vector;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * Adapter for displaying a preview of conference rooms.
 * This adapter manages a list of {@link Item} objects, each representing a room with a label and description.
 * It supports filtering the displayed rooms based on a search expression.
 *
 * <p>Initially, the adapter displays a loading message. The actual room data can be populated using the
 * {@link #fill(Vector)} method. If an error occurs while fetching data, the {@link #error()} method can be
 * called to display an error message.
 *
 * <p>The {@link #applyFilter(String)} method allows filtering the displayed rooms based on whether the
 * provided expression matches the room's label or description (case-insensitive).
 *
 * <p>This adapter is designed to be used with a ListView or similar AdapterView.
 */
public class RoomsPreviewAdapter extends BaseAdapter {
    public boolean init;
    private final Vector<Item> list = new Vector<>();
    private final Vector<Item> display = new Vector<>();

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

    @Override
    public int getCount() {
        return this.display.size();
    }

    @Override
    public Item getItem(int arg0) {
        return this.display.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout item;
        if (arg1 == null) {
            item = (LinearLayout) View.inflate(resources.ctx, R.layout.room_preview_item, null);
        } else {
            item = (LinearLayout) arg1;
        }
        TextView label = item.findViewById(R.id.room_preview_item_label);
        TextView desc = item.findViewById(R.id.room_preview_item_desc);
        Item i = getItem(arg0);
        label.setText(i.label);
        desc.setText(i.desc);
        return item;
    }

    public static class Item {
        public String desc;
        public String label;
    }
}