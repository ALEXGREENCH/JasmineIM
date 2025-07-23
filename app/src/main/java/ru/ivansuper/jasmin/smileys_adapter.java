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
    private Vector<Movie> smileys;
    private Vector<String> tags;

    public smileys_adapter() {
        this.tags = new Vector<>();
        this.smileys = new Vector<>();
        this.tags = SmileysManager.selector_tags;
        this.smileys = SmileysManager.selector_smileys;
        notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return this.smileys.size();
    }

    @Override
    public Movie getItem(int position) {
        return this.smileys.get(position);
    }

    public String getTag(int position) {
        return this.tags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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