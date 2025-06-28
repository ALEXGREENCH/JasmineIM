package ru.ivansuper.jasmin.MultiColumnList;

import android.widget.BaseAdapter;

public abstract class MultiColumnAdapter extends BaseAdapter {
    /** @noinspection unused*/
    public static final int ITEM_TYPE_GROUP = 0;
    /** @noinspection unused*/
    public static final int ITEM_TYPE_ITEM = 1;

    public abstract int getItemType(int i);
}
