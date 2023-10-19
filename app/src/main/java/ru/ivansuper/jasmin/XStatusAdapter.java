package ru.ivansuper.jasmin;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.Vector;
import ru.ivansuper.jasmin.icq.xstatus;

/* loaded from: classes.dex */
public class XStatusAdapter extends BaseAdapter {
    private Vector<Drawable> list = new Vector<>();

    public XStatusAdapter() {
        Drawable[] items = xstatus.icons;
        for (Drawable drawable : items) {
            this.list.add(drawable);
        }
        this.list.add(resources.cross);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.list.size();
    }

    @Override // android.widget.Adapter
    public Drawable getItem(int position) {
        return this.list.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return position;
    }

    @Override // android.widget.Adapter
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