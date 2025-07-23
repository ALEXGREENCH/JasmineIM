package ru.ivansuper.jasmin;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;

import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.jabber.JProfile;

/**
 * Adapter for displaying a list of profiles in a ListView.
 * Each item in the list represents a user profile and shows an icon based on the profile type
 * and a label with the profile ID.
 */
public class ProfilesAdapter extends BaseAdapter {
    public Vector<ProfilesAdapterItem> profiles = new Vector<>();
    /** @noinspection unused*/
    public int profilesCount;

    public void setProfilesAdapter(Vector<ProfilesAdapterItem> profiles) {
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.profiles.size();
    }

    @Override
    public ProfilesAdapterItem getItem(int arg0) {
        return this.profiles.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public void remove(int idx) {
        this.profiles.removeElementAt(idx);
        notifyDataSetChanged();
    }

    public void add(ProfilesAdapterItem item) {
        this.profiles.add(item);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout item = new LinearLayout(resources.ctx);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(16);
        item.setPadding(5, 10, 5, 10);
        TextView label = new TextView(resources.ctx);
        label.setText(this.profiles.get(arg0).id);
        label.setTextColor(-1);
        label.setTextSize(20.0f);
        label.setPadding(10, 0, 0, 0);
        ImageView icon = new ImageView(resources.ctx);
        switch (this.profiles.get(arg0).profile_type) {
            case EventTranslator.PROFILE_TYPE_ICQ:
                icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.icq_status_online));
                break;
            case EventTranslator.PROFILE_TYPE_JABBER:
                switch (this.profiles.get(arg0).proto_type) {
                    case JProfile.TYPE_XMPP:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.xmpp));
                        break;
                    case JProfile.TYPE_VK:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.vk_online));
                        break;
                    case JProfile.TYPE_YANDEX:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.ya_online));
                        break;
                    case JProfile.TYPE_GTALK:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.gtalk_online));
                        break;
                    case JProfile.TYPE_QIP:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.qip_online));
                        break;
                }
            case EventTranslator.PROFILE_TYPE_MRIM:
                icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.mrim_contact_status_online));
                break;
            case 3:
                icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.xmpp_online));
                break;
        }
        item.addView(icon);
        item.addView(label);
        return item;
    }
}