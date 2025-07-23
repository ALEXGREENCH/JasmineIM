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
    /**
     * A list of Drawable objects representing XStatus icons.
     * This list is populated with icons from {@link xstatus#icons}
     * and an additional cross icon for clearing the status.
     */
    private final Vector<Drawable> list = new Vector<>();

    /**
     * Constructs a new XStatusAdapter.
     * Initializes the list of drawables with XStatus icons and adds a default "cross" icon.
     */
    public XStatusAdapter() {
        Drawable[] items = xstatus.icons;
        Collections.addAll(list, items);
        list.add(resources.cross);
    }

    /**
     * Returns the total number of XStatus icons in the adapter.
     *
     * @return The number of items in the adapter.
     */
    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * Retrieves the XStatus icon (Drawable) at the specified position in the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The Drawable representing the XStatus icon at the specified position.
     */
    @Override
    public Drawable getItem(int position) {
        return list.get(position);
    }

    /**
     * Gets the ID of the item at the specified position.
     * In this adapter, the item ID is simply the position itself.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The ID of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * This method creates and configures an ImageView to display an XStatus icon.
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
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
