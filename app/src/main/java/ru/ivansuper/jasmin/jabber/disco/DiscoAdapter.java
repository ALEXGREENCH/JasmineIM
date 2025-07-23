package ru.ivansuper.jasmin.jabber.disco;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * Adapter for displaying service discovery items in a list.
 * This adapter handles the hierarchical structure of disco items
 * and their visual representation, including indentation for levels,
 * icons based on status and type, and open/closed indicators for nodes.
 */
public class DiscoAdapter extends BaseAdapter {
    private static final int LEVEL_SHIFT = (int) (14.0f * resources.dm.density);
    private Vector<Item> mDisplay;
    private final Item root;

    public DiscoAdapter(Item item) {
        this.root = item;
        build();
    }

    public final void build() {
        if (this.mDisplay == null) {
            this.mDisplay = new Vector<>();
        } else {
            this.mDisplay.clear();
        }
        this.mDisplay.addAll(this.root.toList(-1));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.mDisplay.size();
    }

    @Override
    public Item getItem(int position) {
        return this.mDisplay.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout lay;
        if (convertView == null) {
            lay = (LinearLayout) View.inflate(resources.ctx, R.layout.disco_item, null);
            lay.findViewById(R.id.item_back).setBackgroundDrawable(resources.getListSelector());
        } else {
            lay = (LinearLayout) convertView;
        }
        Item item = getItem(position);
        ImageView opened = lay.findViewById(R.id.item_open_indicator);
        ImageView icon = lay.findViewById(R.id.item_icon);
        TextView label = lay.findViewById(R.id.item_label);
        if (item.childs_loaded) {
            opened.setImageDrawable(item.opened ? resources.node_opened : resources.node_closed);
            opened.setVisibility(item.isOpenable() ? View.VISIBLE : View.INVISIBLE);
        } else {
            opened.setImageDrawable(resources.node_closed);
            opened.setVisibility(View.VISIBLE);
        }
        switch (item.status) {
            case 0:
                icon.setImageDrawable(resources.jabber_offline);
                break;
            case 1:
                switch (item.type) {
                    case 0:
                        icon.setImageDrawable(resources.jabber_online);
                        break;
                    case 1:
                        icon.setImageDrawable(resources.jabber_server);
                        break;
                    case 2:
                        icon.setImageDrawable(resources.jabber_command);
                        break;
                }
            case 2:
                icon.setImageDrawable(resources.jabber_waiting);
                break;
            case 3:
                icon.setImageDrawable(resources.jabber_error);
                break;
        }
        String name = item.NODE;
        if (name.length() > 64) {
            name = name.substring(0, name.length() - 4) + " ...";
        }
        label.setText(name);
        lay.setPadding((LEVEL_SHIFT * item.level) + 10, 10, 10, 10);
        return lay;
    }
}
