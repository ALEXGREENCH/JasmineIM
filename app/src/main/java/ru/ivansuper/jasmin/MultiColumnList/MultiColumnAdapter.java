package ru.ivansuper.jasmin.MultiColumnList;

import android.widget.BaseAdapter;

/**
 * An abstract adapter class for use with {@link MultiColumnList}.
 * This adapter is responsible for providing data to the list view,
 * including defining the type of each item (group or item).
 */
public abstract class MultiColumnAdapter extends BaseAdapter {
    /** @noinspection unused*/
    public static final int ITEM_TYPE_GROUP = 0;
    /** @noinspection unused*/
    public static final int ITEM_TYPE_ITEM = 1;

    public abstract int getItemType(int i);
}
