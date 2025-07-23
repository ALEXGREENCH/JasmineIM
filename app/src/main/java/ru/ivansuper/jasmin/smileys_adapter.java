package ru.ivansuper.jasmin;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import java.util.Vector;
import ru.ivansuper.jasmin.animate_tools.Movie;
import ru.ivansuper.jasmin.animate_tools.SmileView;

/**
 * Adapter for displaying smileys in a list or grid.
 * This adapter is responsible for providing the data and views for each smiley item.
 * It uses a {@link Vector} of {@link Movie} objects to store the smileys and a
 * {@link Vector} of {@link String} objects to store their corresponding tags.
 * The smileys and tags are obtained from the {@link SmileysManager}.
 */
public class smileys_adapter extends BaseAdapter {
    /**
     * A vector of {@link Movie} objects representing the smileys to be displayed.
     * Each {@link Movie} object contains the animation or image data for a smiley.
     */
    private Vector<Movie> smileys;
    /**
     * A vector of strings representing the tags associated with each smiley.
     * Each tag corresponds to a smiley in the {@link #smileys} vector.
     * These tags are used for identifying and categorizing smileys.
     * The tags are obtained from the {@link SmileysManager#selector_tags} field.
     */
    private Vector<String> tags;

    /**
     * Constructs a new smileys_adapter.
     * Initializes the tags and smileys vectors with data from {@link SmileysManager}.
     * Notifies the adapter that the underlying data has changed and any View reflecting the data set should refresh itself.
     */
    public smileys_adapter() {
        this.tags = new Vector<>();
        this.smileys = new Vector<>();
        this.tags = SmileysManager.selector_tags;
        this.smileys = SmileysManager.selector_smileys;
        notifyDataSetInvalidated();
    }

    /**
     * Returns the number of smileys in the adapter.
     *
     * @return The number of smileys.
     */
    @Override
    public int getCount() {
        return this.smileys.size();
    }

    /**
     * Returns the Movie object at the specified position.
     *
     * @param position The position of the item in the data set.
     * @return The Movie object at the specified position.
     */
    @Override
    public Movie getItem(int position) {
        return this.smileys.get(position);
    }

    /**
     * Returns the tag associated with the smiley at the specified position.
     *
     * @param position The position of the smiley in the adapter.
     * @return The tag of the smiley at the specified position.
     */
    public String getTag(int position) {
        return this.tags.get(position);
    }

    /**
     * Returns the row ID of the item at the specified position.
     * In this implementation, the position itself is used as the row ID.
     *
     * @param position The position of the item within the adapter's data set whose row ID we want.
     * @return The ID of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * <p>This method is called by the {@link android.widget.AdapterView} to get the view
     * for each item in the list or grid. It either creates a new view or recycles an
     * existing one (convertView).
     *
     * <p>The view is inflated from the {@code R.layout.smile_item} layout resource and
     * displays a single smiley. The smiley is obtained from the {@link #getItem(int)}
     * method and set to the {@link SmileView} within the layout.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout lay;
        if (convertView == null) {
            lay = (LinearLayout) View.inflate(resources.ctx, R.layout.smile_item, null);
        } else {
            lay = (LinearLayout) convertView;
        }
        lay.setMinimumHeight(SmileysManager.max_height + 4);
        SmileView smile = lay.findViewById(R.id.smile_item);
        smile.setMovie(getItem(position));
        return lay;
    }
}