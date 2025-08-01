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
import ru.ivansuper.jasmin.Preferences.PreferenceTable;

/**
 * UAdapter is a custom adapter class that extends BaseAdapter.
 * It is used to display a list of items, each with an optional icon, label, and ID.
 * The adapter supports filtering, selection, and customization of item appearance.
 *
 * <p>
 * The adapter provides several modes for displaying items:
 * <ul>
 *     <li>{@link #SHOW_ALL}: Shows both icon and label.</li>
 *     <li>{@link #FORCE_HIDE_ICON}: Hides the icon.</li>
 *     <li>{@link #FORCE_HIDE_LABEL}: Hides the label.</li>
 *     <li>{@link #FORCE_HIDE_LABEL_AND_ICON}: Hides both icon and label.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The adapter also supports adding separators to the list.
 * Separators can be simple lines or have a custom label.
 * </p>
 *
 * <p>
 * Items in the list can be selected, and the adapter provides methods for managing selection.
 * The appearance of items, such as padding, text color, and text size, can be customized.
 * </p>
 *
 * <p>
 * The adapter supports filtering of items based on a search expression.
 * When a filter is applied, only items whose labels start with the filter expression (case-insensitive) are displayed.
 * </p>
 *
 * <p>
 * The adapter uses a custom layout (R.layout.list_item) for displaying items.
 * The layout includes an ImageView for the icon, a TextView for the label, and a LinearLayout for the separator.
 * </p>
 */
public class UAdapter extends BaseAdapter {
    /**
     * Constant indicating that the icon should be hidden.
     * When this mode is set, only the label will be displayed for each item in the list.
     *
     * @see #setMode(int)
     * @see #SHOW_ALL
     * @see #FORCE_HIDE_LABEL
     * @see #FORCE_HIDE_LABEL_AND_ICON
     * @noinspection unused
     */
    public static final int FORCE_HIDE_ICON = 2;
    /** @noinspection unused*/
    public static final int FORCE_HIDE_LABEL = 1;
    /** @noinspection unused*/
    public static final int FORCE_HIDE_LABEL_AND_ICON = 3;
    /** @noinspection unused*/
    public static final int SHOW_ALL = 0;

    /**
     * A vector of Drawable objects representing the icons for each item in the list.
     * If an item does not have an icon, the corresponding element in this vector will be null.
     */
    private final Vector<Drawable> icons = new Vector<>();
    /**
     * A vector that stores the labels for each item in the list.
     * The labels are displayed in the TextView of each list item.
     * When filtering is enabled, only items whose labels match the filter expression are shown.
     */
    private final Vector<String> labels = new Vector<>();
    /**
     * A list of integer IDs associated with each item in the adapter.
     * These IDs are used to uniquely identify items and can be retrieved using {@link #getItemId(int)}.
     * When an item is a separator, its corresponding ID in this list will be null.
     */
    private final Vector<Integer> ids = new Vector<>();
    /**
     * A vector of integers representing the gravity of each item.
     * Gravity determines the alignment of the item within its parent layout.
     * If an item's gravity is null, it defaults to {@link android.view.Gravity#CENTER_VERTICAL} | {@link android.view.Gravity#START}.
     */
    private final Vector<Integer> gravs = new Vector<>();
    /**
     * A vector that stores the selection state for each item.
     * If an item is selected, the corresponding element in this vector will be non-null.
     * Otherwise, it will be null.
     */
    private final Vector<Integer> select = new Vector<>();
    /**
     * A list of objects representing separators.
     * If an element at a certain index is not null, it indicates that the item at that index is a separator.
     * The actual object stored in this vector for separators is a new {@code Object()},
     * while for regular items, it's {@code null}.
     * This vector is used in conjunction with {@link #icons}, {@link #labels}, {@link #ids},
     * {@link #gravs}, and {@link #select} to store item data.
     */
    private final Vector<Object> separators = new Vector<>();
    /**
     * A vector that stores the filtered items.
     * Each item in this vector is a {@link filtered_item} object,
     * which contains the label and ID of an item that matches the current filter.
     * This vector is populated by the {@link #doFilter()} method when a filter is applied.
     * If no filter is applied, this vector is empty.
     */
    private final Vector<filtered_item> filtered = new Vector<>();
    /**
     * The current display mode for items.
     * This determines whether icons, labels, or both are shown.
     * Possible values are:
     * <ul>
     *     <li>{@link #SHOW_ALL}: Shows both icon and label.</li>
     *     <li>{@link #FORCE_HIDE_ICON}: Hides the icon.</li>
     *     <li>{@link #FORCE_HIDE_LABEL}: Hides the label.</li>
     *     <li>{@link #FORCE_HIDE_LABEL_AND_ICON}: Hides both icon and label.</li>
     * </ul>
     * Defaults to {@link #SHOW_ALL}.
     */
    private int mode = 0;
    /**
     * The padding around each item in the list.
     * The actual padding applied is 70% of this value.
     */
    private int padding = 0;
    /**
     * The color to be used for the text in the list items.
     * A value of -1 indicates that the default text color should be used.
     */
    private int text_color = -1;
    /**
     * The size of the text in the list items.
     * The default value is 16.
     */
    private int text_size = PreferenceTable.clTextSize;
    /**
     * The current filter string used to filter the list items.
     * If empty, all items are displayed. Otherwise, only items whose labels
     * start with the filter string (case-insensitive) are displayed.
     */
    private String filter = "";
    /**
     * Indicates whether a shadow effect should be applied to the text of the list items.
     * When set to true, a shadow is added to the text, enhancing its visibility.
     * The default value is false.
     * @noinspection FieldCanBeLocal, unused
     */
    private boolean use_shadow = false;

    /** Creates a new adapter with text size taken from preferences */
    public UAdapter() {
        this.text_size = PreferenceTable.clTextSize;
    }

    /**
     * Returns the number of items in the adapter.
     * If a filter is applied, it returns the number of filtered items.
     * Otherwise, it returns the total number of items.
     *
     * @return The number of items in the adapter.
     */
    @Override
    public int getCount() {
        return this.filter.isEmpty() ? this.labels.size() : this.filtered.size();
    }

    /**
     * Returns the index of the last item in the adapter.
     *
     * <p>
     * If a filter is applied, the method returns the index of the last item in the filtered list.
     * Otherwise, it returns the index of the last item in the original list.
     * </p>
     *
     * @return The index of the last item, or -1 if the list is empty.
     */
    public int getLastIndex() {
        return this.filter.isEmpty() ? this.labels.size() - 1 : this.filtered.size() - 1;
    }

    /**
     * Gets the data item associated with the specified position in the data set.
     * <p>
     * If a filter is applied, this method returns the label of the filtered item at the specified position.
     * Otherwise, it returns the label of the item at the specified position in the original data set.
     * </p>
     *
     * @param arg0 The position of the item within the adapter's data set whose data we want.
     * @return The data at the specified position.
     */
    @Override
    public String getItem(int arg0) {
        return this.labels.get(arg0);
    }

    /**
     * Indicates whether all the items in this adapter are enabled.
     * If this method returns true, it means all items are selectable and clickable.
     * This implementation always returns false, meaning that there might be disabled items in the list.
     *
     * @return false, indicating that not all items are enabled.
     * @see #isEnabled(int)
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int idx) {
        return this.separators.get(idx) == null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * <p>If a filter is applied, the id of the filtered item at the given position is returned.
     * Otherwise, the id of the item at the given position in the original list is returned.
     *
     * @param arg0 The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int arg0) {
        return this.filter.isEmpty() ? this.ids.get(arg0) : this.filtered.get(arg0).id;
    }

    /**
     * Adds a new item to the adapter with the given label and ID.
     * The item will not have an icon, and its gravity will be set to the default value.
     *
     * @param label The label for the new item.
     * @param id The ID for the new item.
     */
    public void put(String label, int id) {
        this.icons.addElement(null);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    /**
     * Adds an item with an icon, label, and ID to the adapter.
     *
     * @param icon  The drawable resource for the icon.
     * @param label The text to display as the label.
     * @param id    The unique identifier for the item.
     */
    public void put(Drawable icon, String label, int id) {
        this.icons.addElement(icon);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    /**
     * Adds an item to the adapter with the given label, ID, and gravity.
     * The icon for this item will be null.
     *
     * @param label The label of the item.
     * @param id The ID of the item.
     * @param gravity The gravity of the item.
     */
    public void put(String label, int id, int gravity) {
        this.icons.addElement(null);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(gravity);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    /**
     * Adds a new item to the adapter with an icon, label, ID, and gravity.
     *
     * @param icon The drawable icon for the item.
     * @param label The text label for the item.
     * @param id The integer ID for the item.
     * @param gravity The gravity for the item's layout.
     */
    public void put(Drawable icon, String label, int id, int gravity) {
        this.icons.addElement(icon);
        this.labels.addElement(label);
        this.ids.addElement(id);
        this.gravs.addElement(gravity);
        this.select.addElement(null);
        this.separators.addElement(null);
    }

    /**
     * Adds an array of labels to the adapter.
     * Each label will be added as a new item with a null icon and an ID corresponding to its index in the array.
     *
     * @param array The array of labels to add.
     * @noinspection unused
     */
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

    /**
     * Adds an array of icons to the adapter.
     * Each icon will be associated with an empty label and an ID corresponding to its index in the array.
     *
     * @param array The array of Drawables to add as icons.
     * @noinspection unused
     */
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

    /**
     * Adds a separator to the list.
     * The separator will be displayed as a horizontal line.
     */
    public void put_separator() {
        this.icons.addElement(null);
        this.labels.addElement("---");
        this.ids.addElement(null);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(new Object());
    }

    /**
     * Adds a separator item with a custom label to the list.
     *
     * @param name The label for the separator.
     */
    public void put_separator(String name) {
        this.icons.addElement(null);
        this.labels.addElement(name);
        this.ids.addElement(null);
        this.gravs.addElement(null);
        this.select.addElement(null);
        this.separators.addElement(new Object());
    }

    /**
     * Sets the display mode for the adapter.
     * The mode determines whether to show icons, labels, both, or neither.
     *
     * @param mode The display mode to set. Must be one of:
     *             <ul>
     *                 <li>{@link #SHOW_ALL}</li>
     *                 <li>{@link #FORCE_HIDE_ICON}</li>
     *                 <li>{@link #FORCE_HIDE_LABEL}</li>
     *                 <li>{@link #FORCE_HIDE_LABEL_AND_ICON}</li>
     *             </ul>
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Toggles the selection state of the item at the specified position.
     * If the item is already selected, it will be unselected.
     * If the item is not selected, it will be selected.
     * After toggling the selection, this method notifies the adapter that the data set has changed.
     *
     * @param pos The position of the item whose selection state is to be toggled.
     */
    public void toggleSelection(int pos) {
        if (this.select.get(pos) != null) {
            this.select.set(pos, null);
        } else {
            this.select.set(pos, 1);
        }
        notifyDataSetChanged();
    }

    /**
     * Sets the item at the specified position as selected.
     * All other items will be unselected.
     *
     * @param pos The position of the item to select.
     */
    public void setSelected(int pos) {
        unselectAll();
        this.select.set(pos, 1);
        notifyDataSetChanged();
    }

    /**
     * Unselects all items and then unselects the item at the specified position.
     *
     * @param pos The position of the item to unselect.
     * @noinspection unused
     */
    public void setUnselected(int pos) {
        unselectAll();
        this.select.set(pos, null);
        notifyDataSetChanged();
    }

    /**
     * Unselects all items in the list.
     * This method iterates through the selection list and sets each element to null,
     * effectively clearing any previous selections.
     * Note: This method does not call notifyDataSetChanged(), so the UI will not update
     * until it is called explicitly.
     */
    public void unselectAll() {
        for (int i = 0; i < this.select.size(); i++) {
            this.select.set(i, null);
        }
    }

    /**
     * Returns the index of the first selected item in the list.
     *
     * @return The index of the first selected item, or -1 if no item is selected.
     * @noinspection unused
     */
    public int getSelectedIdx() {
        for (int i = 0; i < this.select.size(); i++) {
            if (this.select.get(i) != null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the padding for the items in the list.
     * The actual padding applied is 70% of the specified value.
     *
     * @param padding The desired padding value.
     */
    public void setPadding(int padding) {
        this.padding = (int) (padding * 0.7f);
        notifyDataSetChanged();
    }

    /**
     * Sets the text color for the items in the list.
     *
     * @param color The color to set for the text.
     */
    public void setTextColor(int color) {
        this.text_color = color;
        notifyDataSetChanged();
    }

    /**
     * Sets the text size for the items in the list.
     *
     * @param size The new text size.
     */
    public void setTextSize(int size) {
        this.text_size = size;
        notifyDataSetChanged();
    }

    /**
     * Clears all items from the adapter.
     * This method removes all icons, labels, IDs, gravities, selections, and separators.
     * After clearing the data, it notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void clear() {
        this.icons.clear();
        this.labels.clear();
        this.ids.clear();
        this.gravs.clear();
        this.select.clear();
        this.separators.clear();
        notifyDataSetChanged();
    }

    /**
     * Sets the filter expression for the adapter.
     * When a filter is applied, only items whose labels start with the filter expression (case-insensitive) are displayed.
     *
     * @param expression The filter expression.
     * @noinspection unused
     */
    public void setFilter(String expression) {
        this.filter = expression;
        doFilter();
        notifyDataSetChanged();
    }

    /**
     * Sets whether to use a shadow effect for the text in the list items.
     *
     * @param use_shadow {@code true} to use shadow, {@code false} otherwise.
     */
    public void setUseShadow(boolean use_shadow) {
        this.use_shadow = use_shadow;
        notifyDataSetChanged();
    }

    /**
     * Filters the items in the adapter based on the current filter expression.
     * This method clears the existing filtered list and repopulates it with items
     * whose labels start with the filter expression (case-insensitive).
     * This method is called internally when the filter is set or updated.
     */
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

    /**
     * Returns the view for a specific item in the list.
     *
     * <p>
     * This method is called by the ListView to get the view for each item.
     * It inflates the layout for the item if it's not already inflated,
     * and then sets the icon, label, and other properties of the item
     * based on its position and the current state of the adapter.
     * </p>
     *
     * <p>
     * If the item is disabled (i.e., it's a separator), the method
     * configures the view to display the separator. Otherwise, it
     * configures the view to display the item's icon and label.
     * </p>
     *
     * <p>
     * The appearance of the item, such as padding, text color, and text size,
     * is determined by the adapter's properties. The selection state of the
     * item is also reflected in its appearance.
     * </p>
     *
     * <p>
     * If a filter is applied, the method displays the filtered items.
     * Otherwise, it displays all items.
     * </p>
     *
     * @param position The position of the item in the list.
     * @param view The old view to reuse, if possible.
     * @param viewGroup The parent view that this view will eventually be attached to.
     * @return The view for the item at the specified position.
     */
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LinearLayout layout;
        if (view == null) {
            layout = (LinearLayout) View.inflate(resources.ctx, R.layout.list_item, null);
        } else {
            layout = (LinearLayout) view;
        }
        LinearLayout separator = layout.findViewById(R.id.list_item_separator);
        separator.setVisibility(View.GONE);
        LinearLayout lay = layout.findViewById(R.id.list_item_back);
        ImageView icon = layout.findViewById(R.id.list_item_icon);
        icon.setVisibility(View.VISIBLE);
        icon.setBackgroundDrawable(resources.ctx.getResources().getDrawable(R.drawable.smiley_and_send_btn));
        TextView label = layout.findViewById(R.id.list_item_label);
        lay.setPadding(this.padding, this.padding, this.padding, this.padding);
        if (!isEnabled(position)) {
            lay.setPadding(2, this.padding, 2, this.padding);
            separator.setVisibility(View.VISIBLE);
            separator.setBackgroundColor(ColorScheme.getColor(44));
            icon.setVisibility(View.GONE);
            String lbl = this.labels.get(position);
            if (!lbl.equals("---")) {
                separator.setVisibility(View.GONE);
                label.setGravity(17);
                label.setTextSize((int) (this.text_size / 1.15d));
                label.setTextColor(this.text_color);
                label.setText(lbl);
                label.setBackgroundColor(ColorScheme.getColor(44));
                label.setShadowLayer(1.0f, 0.0f, 0.0f, 0xFF000000);
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
            if (this.select.get(position) != null) {
                lay.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(47), 2));
            } else {
                lay.setBackgroundColor(0);
            }
            if (this.filter.isEmpty()) {
                if (this.gravs.get(position) == null) {
                    lay.setGravity(19);
                } else {
                    lay.setGravity(this.gravs.get(position));
                }
                Drawable icn = this.icons.get(position);
                if (icn != null) {
                    icon.setImageDrawable(icn);
                }
                label.setTextSize(this.text_size);
                label.setTextColor(this.text_color);
                label.setShadowLayer(1.0f, 0.0f, 0.0f, 0xFF000000);
                label.setText(this.labels.get(position));
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
                label.setShadowLayer(3.0f, 0.0f, 0.0f, 0xFFB1B1B1);
                label.setText(this.filtered.get(position).label);
            }
        }
        return layout;
    }

    /**
     * Represents an item that has been filtered.
     * This class is used when the adapter's filter is active.
     * It stores the label and the original ID of the item.
     */
    public static class filtered_item {
        public String label = "";
        public int id = 0;
    }
}