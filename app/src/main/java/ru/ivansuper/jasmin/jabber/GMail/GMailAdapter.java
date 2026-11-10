package ru.ivansuper.jasmin.jabber.GMail;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.resources;

public class GMailAdapter extends BaseAdapter {
    private Vector<GoogleMail.Mail> list;

    public GMailAdapter(Vector<GoogleMail.Mail> vector) {
        this.list = vector;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public GoogleMail.Mail getItem(int i) {
        return this.list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = View.inflate(resources.ctx, 2130903068, null);
        }
        LinearLayout linearLayout = (LinearLayout) view;
        TextView textView = (TextView) linearLayout.findViewById(2131427508);
        TextView textView2 = (TextView) linearLayout.findViewById(2131427509);
        TextView textView3 = (TextView) linearLayout.findViewById(2131427510);
        GoogleMail.Mail item = getItem(i);
        textView.setText(item.theme);
        textView2.setText(String.valueOf(item.sender_name) + " [" + item.sender_address + "]");
        textView3.setText(item.preview);
        return linearLayout;
    }

    public void init(Vector<GoogleMail.Mail> vector) {
        this.list = vector;
        notifyDataSetChanged();
    }
}
