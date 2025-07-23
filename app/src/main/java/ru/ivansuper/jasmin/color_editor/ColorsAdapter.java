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

/**
 * Adapter for displaying a list of colors in the color editor.
 * This adapter is used to populate a ListView or GridView with color items,
 * where each item consists of a color preview and its name.
 */
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
    public Integer getItem(int position) {
        return this.colors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LinearLayout lay;
        if (view == null) {
            lay = (LinearLayout) LayoutInflater.from(this.ctx).inflate(R.layout.color_editor_list_item, null);
        } else {
            lay = (LinearLayout) view;
        }
        ImageView preview = lay.findViewById(R.id.color_editor_item_preview);
        TextView name = lay.findViewById(R.id.color_editor_item_name);
        preview.setBackgroundColor(this.colors.get(position));
        name.setText(this.names.get(position));
        return lay;
    }
}