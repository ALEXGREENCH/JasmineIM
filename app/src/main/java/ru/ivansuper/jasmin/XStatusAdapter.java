package ru.ivansuper.jasmin;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.Collections;
import java.util.Vector;

import ru.ivansuper.jasmin.icq.xstatus;

/**
 * Adapter for displaying XStatus icons in a list or grid.
 * XStatus icons represent various user moods or activities.
 * This adapter populates a view (e.g., ListView, GridView) with these icons.
 */
public class XStatusAdapter extends BaseAdapter {
    private final Vector<Drawable> list = new Vector<>();

    public XStatusAdapter() {
        Drawable[] items = xstatus.icons;
        Collections.addAll(this.list, items);
        this.list.add(resources.cross);
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Drawable getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView sts = new ImageView(resources.ctx);
        sts.setBackgroundDrawable(resources.ctx.getResources().getDrawable(R.drawable.smiley_and_send_btn));
        sts.setImageDrawable(getItem(position));
        sts.setPadding(10, 10, 10, 10);
        sts.measure(0, 0);
        sts.setScaleType(ImageView.ScaleType.CENTER);
        return sts;
    }
}