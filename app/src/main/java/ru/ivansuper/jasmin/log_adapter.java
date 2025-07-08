package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class log_adapter extends BaseAdapter {
    private final Vector<String> list = resources.log;

    public void put(String variable) {
        Date dt = new Date(System.currentTimeMillis());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String item = "[" + sdf.format(dt) + "]: " + variable;
        this.list.add(item);
        notifyDataSetChanged();
    }

    public void clear() {
        this.list.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public String getItem(int arg0) {
        return this.list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        TextView label;
        if (arg1 == null) {
            label = new TextView(resources.ctx);
            label.setTextSize(14.0f);
            label.setTextColor(-1);
            label.setShadowLayer(3.0f, 1.0f, 1.0f, -16777216);
            label.setPadding(5, 10, 5, 10);
        } else {
            label = (TextView) arg1;
        }
        if (arg0 % 2 > 0) {
            label.setBackgroundColor(1140850688);
        } else {
            label.setBackgroundColor(0);
        }
        label.setText(getItem(arg0));
        return label;
    }
}
