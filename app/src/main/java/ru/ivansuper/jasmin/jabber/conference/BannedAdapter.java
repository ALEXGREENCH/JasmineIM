package ru.ivansuper.jasmin.jabber.conference;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;

/**
 * An adapter for displaying a list of banned users in a conference.
 * It extends {@link BaseAdapter} and uses a custom layout for each item.
 */
public class BannedAdapter extends BaseAdapter {
    private ArrayList<BannedItem> mList;

    public BannedAdapter(ArrayList<BannedItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
    }

    public void put(BannedAdapter list) {
        this.mList = list.getItems();
    }

    public void put(ArrayList<BannedItem> list) {
        this.mList = list;
    }

    public ArrayList<BannedItem> getItems() {
        return this.mList;
    }

    @Override
    public int getCount() {
        return this.mList.size();
    }

    @Override
    public BannedItem getItem(int position) {
        return this.mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout lay;
        if (convertView == null) {
            lay = (LinearLayout) View.inflate(resources.ctx, R.layout.banned_list_item, null);
            LinearLayout lay1 = lay.findViewById(R.id.lay1);
            lay1.setBackgroundDrawable(resources.getListSelector());
            LinearLayout lay2 = lay.findViewById(R.id.lay2);
            lay2.setBackgroundColor(ColorScheme.getColor(44));
        } else {
            lay = (LinearLayout) convertView;
        }
        BannedItem item = getItem(position);
        TextView title = lay.findViewById(R.id.title);
        TextView desc = lay.findViewById(R.id.desc);
        Button remove = lay.findViewById(R.id.btn1);
        remove.setText(Locale.getString("s_do_delete"));
        title.setText(item.JID == null ? "No jid" : item.JID);
        desc.setText(item.reason == null ? "" : item.reason);
        remove.setOnClickListener(item.task);
        lay.setDescendantFocusability(393216);
        return lay;
    }

    public static class BannedItem {
        public String JID;
        public String reason;
        public View.OnClickListener task;

        public BannedItem(String jid, String reason, View.OnClickListener task) {
            this.JID = jid;
            this.reason = reason;
            this.task = task;
        }
    }
}
