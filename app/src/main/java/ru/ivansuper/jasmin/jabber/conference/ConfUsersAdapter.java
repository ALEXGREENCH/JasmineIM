package ru.ivansuper.jasmin.jabber.conference;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Vector;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.jabber.Clients;
import ru.ivansuper.jasmin.resources;

public class ConfUsersAdapter extends BaseAdapter {
    private final Context ctx;
    private Vector<Conference.User> users;

    public ConfUsersAdapter(Context ctx, Vector<Conference.User> users) {
        //noinspection UnusedAssignment
        this.users = new Vector<>();
        this.ctx = ctx;
        this.users = users;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.users.size();
    }

    @Override
    public Conference.User getItem(int position) {
        return this.users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout lay;
        if (convertView == null) {
            lay = (LinearLayout) View.inflate(this.ctx, R.layout.conf_user_item, null);
        } else {
            lay = (LinearLayout) convertView;
        }
        ImageView sts = lay.findViewById(R.id.user_item_sts);
        ImageView affiliation = lay.findViewById(R.id.user_item_affiliation);
        ImageView client = lay.findViewById(R.id.user_item_client);
        TextView status = lay.findViewById(R.id.user_item_status_text);
        affiliation.setImageDrawable(null);
        TextView info = lay.findViewById(R.id.user_item_info);
        Conference.User user = getItem(position);
        if (user.status_text == null || user.status_text.trim().isEmpty()) {
            status.setVisibility(View.GONE);
        } else {
            status.setText(user.status_text);
            status.setVisibility(View.VISIBLE);
        }
        client.setImageDrawable(Clients.getIcon(user.client));
        switch (user.status) {
            case 0:
                sts.setImageDrawable(resources.jabber_chat);
                break;
            case 1:
                sts.setImageDrawable(resources.jabber_online);
                break;
            case 2:
                sts.setImageDrawable(resources.jabber_away);
                break;
            case 3:
                sts.setImageDrawable(resources.jabber_dnd);
                break;
            case 4:
                sts.setImageDrawable(resources.jabber_na);
                break;
        }
        if (user.affiliation.equals("owner")) {
            affiliation.setImageDrawable(resources.jabber_conf_owner);
        } else if (user.affiliation.equals("admin")) {
            affiliation.setImageDrawable(resources.jabber_conf_admin);
        } else {
            if (user.affiliation.equals("member")) {
                affiliation.setImageDrawable(resources.jabber_conf_member);
            }
            if (user.affiliation.equals("outcast")) {
                affiliation.setImageDrawable(resources.jabber_conf_outcast);
            }
            if (user.role.equals("moderator")) {
                affiliation.setImageDrawable(resources.jabber_conf_moderator);
            }
            if (user.role.equals("visitor")) {
                affiliation.setImageDrawable(resources.jabber_conf_visitor);
            }
        }
        info.setText(user.nick);
        return lay;
    }
}
