package ru.ivansuper.jasmin.icq;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

public class SearchResultsAdapter extends BaseAdapter {
    private final Vector<SearchResultItem> list = new Vector<>();

    public void clear() {
        this.list.clear();
        notifyDataSetChanged();
    }

    public void put(SearchResultItem item) {
        this.list.add(item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public SearchResultItem getItem(int pos) {
        return this.list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int pos, View view, ViewGroup view_group) {
        LinearLayout lay;
        if (view == null) {
            lay = (LinearLayout) View.inflate(resources.ctx, R.layout.search_result_item, null);
        } else {
            lay = (LinearLayout) view;
        }
        ImageView status = lay.findViewById(R.id.search_item_sts);
        TextView uin = lay.findViewById(R.id.search_item_uin);
        TextView nick = lay.findViewById(R.id.search_item_nick);
        TextView gender_age = lay.findViewById(R.id.search_item_gender_age);
        TextView name = lay.findViewById(R.id.search_item_name);
        TextView lastname = lay.findViewById(R.id.search_item_lastname);
        SearchResultItem item = getItem(pos);
        status.setImageDrawable(item.status == 0 ? resources.offline : resources.online);
        if (item.status == 2) {
            status.setImageDrawable(resources.unauthorized);
        }
        if (item.need_auth) {
            status.setImageDrawable(resources.unauthorized_icon);
        }
        uin.setText(item.uin);
        nick.setText(resources.getString("s_user_nick") + ": " + item.nick);
        String gender = item.gender == 1 ? resources.getString("s_icq_gender_woman") : "-";
        if (item.gender == 2) {
            gender = resources.getString("s_icq_gender_man");
        }
        gender_age.setText(resources.getString("s_icqs_result_item_gender") + ": " + gender);
        name.setText(resources.getString("s_icqs_result_item_name") + ": " + item.firstname);
        lastname.setText(resources.getString("s_icqs_result_item_surname") + ": " + item.lastname);
        resources.attachContactlistItemBack(lay);
        return lay;
    }
}