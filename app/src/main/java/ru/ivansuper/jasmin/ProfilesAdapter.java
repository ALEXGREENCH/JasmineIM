package ru.ivansuper.jasmin;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;

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
            case 0:
                icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.icq_status_online));
                break;
            case 1:
                switch (this.profiles.get(arg0).proto_type) {
                    case 0:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.xmpp));
                        break;
                    case 1:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.vk_online));
                        break;
                    case 2:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.ya_online));
                        break;
                    case 3:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.gtalk_online));
                        break;
                    case 4:
                        icon.setImageDrawable(resources.ctx.getResources().getDrawable(R.drawable.qip_online));
                        break;
                }
            case 2:
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