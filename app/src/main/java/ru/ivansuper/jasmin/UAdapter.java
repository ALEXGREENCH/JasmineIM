package ru.ivansuper.jasmin;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.color_editor.ColorScheme;

public class UAdapter extends BaseAdapter {
    /** @noinspection unused*/
    public static final int FORCE_HIDE_ICON = 2;
    /** @noinspection unused*/
    public static final int FORCE_HIDE_LABEL = 1;
    /** @noinspection unused*/
    public static final int FORCE_HIDE_LABEL_AND_ICON = 3;
    /** @noinspection unused*/
    public static final int SHOW_ALL = 0;
    private final Vector<Drawable> icons = new Vector<>();
    private final Vector<String> labels = new Vector<>();
    private final Vector<Integer> ids = new Vector<>();
    private final Vector<Integer> gravs = new Vector<>();
    private final Vector<Integer> select = new Vector<>();
    private final Vector<Object> separators = new Vector<>();
    private final Vector<filtered_item> filtered = new Vector<>();
    private int mode = 0;
    private int padding = 0;
    private int text_color = -1;
    private int text_size = 16;
    private String filter = "";
    /** @noinspection FieldCanBeLocal, unused */
    private boolean use_shadow = false;

    @Override
    public int getCount() {
        return this.filter.isEmpty() ? this.labels.size() : this.filtered.size();
    }

    public int getLastIndex() {
        return this.filter.isEmpty() ? this.labels.size() - 1 : this.filtered.size() - 1;
    }

    @Override
    public String getItem(int arg0) {
        return this.labels.get(arg0);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int idx) {
        return this.separators.get(idx) == null;
    }

    @Override
    public long getItemId(int arg0) {
        return this.filter.isEmpty() ? this.ids.get(arg0) : this.filtered.get(arg0).id;
    }

    public void put(String label, int id) {
        this.icons.addElement(null);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    public void put(Drawable icon, String label, int id) {
        this.icons.addElement(icon);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    public void put(String label, int id, int gravity) {
        this.icons.addElement(null);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(gravity);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    public void put(Drawable icon, String label, int id, int gravity) {
        this.icons.addElement(icon);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(gravity);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    /** @noinspection unused*/
    public void put_labels(String[] array) {
        for (int i = 0; i < array.length; i++) {
            this.icons.addElement(null);
            this.labels.addElement(array[i]);
            this.ids.addElement(i);
            this.gravs.addElement(null);
            this.select.addElement(null);
            this.separators.addElement(null);
        }
    }

    /** @noinspection unused*/
    public void put_icons(Drawable[] array) {
        for (int i = 0; i < array.length; i++) {
            this.icons.addElement(array[i]);
            this.labels.addElement("");
            this.ids.addElement(i);
            this.gravs.addElement(null);
            this.select.addElement(null);
            this.separators.addElement(null);
        }
    }

    public void put_separator() {
        this.icons.addElement(null);
        this.labels.addElement("---");
        this.ids.addElement(null);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(new Object());
    }

    public void put_separator(String name) {
        this.icons.addElement(null);
        this.labels.addElement(name);
        this.ids.addElement(null);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(new Object());
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void toggleSelection(int pos) {
        if (this.select.get(pos) != null) {
            this.select.set(pos, null);
        } else {
            this.select.set(pos, 1);
        }
        notifyDataSetChanged();
    }

    public void setSelected(int pos) {
        unselectAll();
        this.select.set(pos, 1);
        notifyDataSetChanged();
    }

    /** @noinspection unused*/
    public void setUnselected(int pos) {
        unselectAll();
        this.select.set(pos, null);
        notifyDataSetChanged();
    }

    public void unselectAll() {
        for (int i = 0; i < this.select.size(); i++) {
            this.select.set(i, null);
        }
    }

    /** @noinspection unused*/
    public int getSelectedIdx() {
        for (int i = 0; i < this.select.size(); i++) {
            if (this.select.get(i) != null) {
                return i;
            }
        }
        return -1;
    }

    public void setPadding(int padding) {
        this.padding = (int) (padding * 0.7f);
        notifyDataSetChanged();
    }

    public void setTextColor(int color) {
        this.text_color = color;
        notifyDataSetChanged();
    }

    public void setTextSize(int size) {
        this.text_size = size;
        notifyDataSetChanged();
    }

    public void clear() {
        this.icons.clear();
        this.labels.clear();
        this.ids.clear();
        this.gravs.clear();
        this.select.clear();
        this.separators.clear();
        notifyDataSetChanged();
    }

    /** @noinspection unused*/
    public void setFilter(String expression) {
        this.filter = expression;
        doFilter();
        notifyDataSetChanged();
    }

    public void setUseShadow(boolean use_shadow) {
        this.use_shadow = use_shadow;
        notifyDataSetChanged();
    }

    private void doFilter() {
        this.filtered.clear();
        for (int i = 0; i < this.labels.size(); i++) {
            String label = this.labels.get(i);
            if (label.toLowerCase().startsWith(this.filter.toLowerCase())) {
                filtered_item item = new filtered_item();
                item.label = label;
                item.id = this.ids.get(i);
                this.filtered.add(item);
            }
        }
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout layout;
        if (arg1 == null) {
            layout = (LinearLayout) View.inflate(resources.ctx, R.layout.list_item, null);
        } else {
            layout = (LinearLayout) arg1;
        }
        LinearLayout separator = layout.findViewById(R.id.list_item_separator);
        separator.setVisibility(View.GONE);
        LinearLayout lay = layout.findViewById(R.id.list_item_back);
        ImageView icon = layout.findViewById(R.id.list_item_icon);
        icon.setVisibility(View.VISIBLE);
        icon.setBackgroundDrawable(resources.ctx.getResources().getDrawable(R.drawable.smiley_and_send_btn));
        TextView label = layout.findViewById(R.id.list_item_label);
        lay.setPadding(this.padding, this.padding, this.padding, this.padding);
        if (!isEnabled(arg0)) {
            lay.setPadding(2, this.padding, 2, this.padding);
            separator.setVisibility(View.VISIBLE);
            separator.setBackgroundColor(ColorScheme.getColor(44));
            icon.setVisibility(View.GONE);
            String lbl = this.labels.get(arg0);
            if (!lbl.equals("---")) {
                separator.setVisibility(View.GONE);
                label.setGravity(17);
                label.setTextSize((int) (this.text_size / 1.15d));
                label.setTextColor(this.text_color);
                label.setText(lbl);
                label.setBackgroundColor(ColorScheme.getColor(44));
                label.setShadowLayer(1.0f, 0.0f, 0.0f, -16777216);
            } else {
                label.setVisibility(View.GONE);
            }
            label.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            label.setPadding(0, 0, 0, 0);
        } else {
            label.setGravity(3);
            label.setBackgroundColor(0);
            label.setVisibility(View.VISIBLE);
            label.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            label.setPadding(this.padding, 0, 0, 0);
            if (this.select.get(arg0) != null) {
                lay.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(47), 2));
            } else {
                lay.setBackgroundColor(0);
            }
            if (this.filter.isEmpty()) {
                if (this.gravs.get(arg0) == null) {
                    lay.setGravity(19);
                } else {
                    lay.setGravity(this.gravs.get(arg0));
                }
                Drawable icn = this.icons.get(arg0);
                if (icn != null) {
                    icon.setImageDrawable(icn);
                }
                label.setTextSize(this.text_size);
                label.setTextColor(this.text_color);
                label.setShadowLayer(1.0f, 0.0f, 0.0f, -16777216);
                label.setText(this.labels.get(arg0));
                if (this.mode != 0) {
                    if (this.mode == 2) {
                        icon.setVisibility(View.GONE);
                    }
                    if (this.mode == 3) {
                        icon.setVisibility(View.GONE);
                        label.setVisibility(View.GONE);
                    }
                    if (this.mode == 1) {
                        label.setVisibility(View.GONE);
                    }
                }
            } else {
                label.setTextSize(this.text_size);
                label.setTextColor(this.text_color);
                label.setShadowLayer(3.0f, 0.0f, 0.0f, -4988581);
                label.setText(this.filtered.get(arg0).label);
            }
        }
        return layout;
    }

    public static class filtered_item {
        public String label = "";
        public int id = 0;
    }
}