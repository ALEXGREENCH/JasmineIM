package ru.ivansuper.jasmin.color_editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import ru.ivansuper.jasmin.R;

public class ColorsAdapter extends BaseAdapter {
    private ArrayList<Integer> colors;
    private final Context ctx;
    private ArrayList<String> names;

    public ColorsAdapter(Context ctx) {
        this.names = new ArrayList<>();
        //noinspection UnusedAssignment
        this.colors = new ArrayList<>();
        this.ctx = ctx;
        this.colors = ColorScheme.colors;
        this.names = ColorScheme.names;
    }

    @Override
    public int getCount() {
        return this.colors.size();
    }

    @Override
    public Integer getItem(int arg0) {
        return this.colors.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout lay;
        if (arg1 == null) {
            lay = (LinearLayout) LayoutInflater.from(this.ctx).inflate(R.layout.color_editor_list_item, null);
        } else {
            lay = (LinearLayout) arg1;
        }
        ImageView preview = lay.findViewById(R.id.color_editor_item_preview);
        TextView name = lay.findViewById(R.id.color_editor_item_name);
        preview.setBackgroundColor(this.colors.get(arg0));
        name.setText(this.names.get(arg0));
        return lay;
    }
}