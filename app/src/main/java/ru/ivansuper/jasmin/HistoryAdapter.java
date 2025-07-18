package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.ui.MyTextView;

public class HistoryAdapter extends BaseAdapter {
    private final Context ctx;
    private boolean filtered;
    public Vector<HistoryItem> filtered_list = new Vector<>();
    public Vector<HistoryItem> list;
    private String pattern;

    public HistoryAdapter(Context ctxParam, Vector<HistoryItem> history) {
        this.ctx = ctxParam;
        this.list = history;
    }

    public void clear() {
        this.list.clear();
        notifyDataSetChanged();
    }

    public void setFilter(String pattern) {
        this.pattern = pattern;
        if (pattern == null) {
            this.filtered = false;
            notifyDataSetChanged();
        } else if (pattern.isEmpty()) {
            this.filtered = false;
            notifyDataSetChanged();
        } else {
            this.pattern = this.pattern.toLowerCase();
            this.filtered = true;
            doFilter();
            notifyDataSetChanged();
        }
    }

    private void doFilter() {
        this.filtered_list.clear();
        for (int i = 0; i < this.list.size(); i++) {
            HistoryItem item = this.list.get(i);
            String a = item.message != null ? item.message.toLowerCase() : "";
            if (a.contains(this.pattern)) {
                this.filtered_list.add(item);
            }
        }
    }

    @Override
    public int getCount() {
        return this.filtered ? this.filtered_list.size() : this.list.size();
    }

    @Override
    public HistoryItem getItem(int position) {
        return this.filtered ? this.filtered_list.get(position) : this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout msg;
        if (convertView == null) {
            msg = (LinearLayout) LayoutInflater.from(this.ctx).inflate(R.layout.chat_item, null);
        } else {
            msg = (LinearLayout) convertView;
        }
        ImageView dragdrop = msg.findViewById(R.id.quote_button);
        dragdrop.setVisibility(View.GONE);
        TextView nick = msg.findViewById(R.id.msg_nick);
        if (!PreferenceTable.nickInChat) {
            nick.setVisibility(View.GONE);
        }
        TextView time = msg.findViewById(R.id.msg_time);
        MyTextView message = msg.findViewById(R.id.msg_text);
        CheckBox selector = msg.findViewById(R.id.chat_item_checkbox);
        LinearLayout status = msg.findViewById(R.id.msg_status);
        ImageView sts = msg.findViewById(R.id.msg_sts_icon);
        HistoryItem hst = getItem(position);
        time.setText(hst.formattedDate);
        nick.setTextSize(PreferenceTable.chatTextSize - 2);
        time.setTextSize(PreferenceTable.chatTextSize);
        time.setTextColor(ColorScheme.getColor(10));
        if (ContactHistoryActivity.multiquoting) {
            selector.setVisibility(View.VISIBLE);
            selector.setChecked(hst.selected);
        } else {
            selector.setVisibility(View.GONE);
        }
        message.setText(hst.message);
        message.selectMatches(this.pattern);
        message.setTextSize(PreferenceTable.chatTextSize);
        if (PreferenceTable.ms_chat_style == 1) {
            status.setVisibility(View.GONE);
            if (hst.direction == 1) {
                sts.setImageDrawable(resources.msg_in);
            } else if (hst.confirmed) {
                sts.setImageDrawable(resources.msg_out_c);
            } else {
                sts.setImageDrawable(resources.msg_out);
            }
        } else {
            status.setVisibility(View.VISIBLE);
            sts.setImageDrawable(null);
        }
        if (hst.direction == 1) {
            if (hst.contact != null) {
                nick.setText(hst.contact.name);
            }
            if (hst.jcontact != null) {
                nick.setText(hst.jcontact.name);
            }
            if (hst.mcontact != null) {
                nick.setText(hst.mcontact.name);
            }
            nick.setTextColor(ColorScheme.getColor(17));
            status.setBackgroundColor(ColorScheme.getColor(15));
            message.setTextColor(ColorScheme.getColor(16));
            message.setLinkTextColor(ColorScheme.getColor(16));
            msg.setBackgroundColor(ColorScheme.getColor(14));
            resources.attachIngMsg(msg);
        } else {
            if (hst.contact != null) {
                nick.setText(hst.contact.profile.nickname);
            }
            if (hst.jcontact != null) {
                nick.setText(hst.jcontact.profile.ID);
            }
            if (hst.mcontact != null) {
                nick.setText(hst.mcontact.profile.ID);
            }
            nick.setTextColor(ColorScheme.getColor(22));
            if (hst.confirmed) {
                status.setBackgroundColor(ColorScheme.getColor(19));
                message.setTextColor(ColorScheme.getColor(21));
                message.setLinkTextColor(ColorScheme.getColor(21));
            } else {
                status.setBackgroundColor(ColorScheme.getColor(20));
                message.setTextColor(ColorScheme.getColor(23));
                message.setLinkTextColor(ColorScheme.getColor(23));
            }
            msg.setBackgroundColor(ColorScheme.getColor(18));
            resources.attachOutMsg(msg);
        }
        message.relayout();
        return msg;
    }
}