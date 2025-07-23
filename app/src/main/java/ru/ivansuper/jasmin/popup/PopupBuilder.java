package ru.ivansuper.jasmin.popup;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import ru.ivansuper.jasmin.resources;

/**
 * Utility class for building QuickAction popups.
 * This class provides static methods to create different types of QuickAction popups,
 * such as simple popups, grid popups, and list popups.
 */
public class PopupBuilder {
    public PopupBuilder() {
    }

    /**
     * @noinspection unused
     */
    public static QuickAction build(View view, View var1, String var2) {
        QuickAction quickAction = new QuickAction(var1, var2);
        quickAction.setCustomView(view);
        return quickAction;
    }

    public static QuickAction buildGrid(BaseAdapter baseAdapter, View view, String s, int columns, int w, int h, AdapterView.OnItemClickListener onItemClickListener) {
        GridView gridView = new GridView(resources.ctx);
        gridView.setAdapter(baseAdapter);
        gridView.setDrawingCacheEnabled(false);
        gridView.setWillNotCacheDrawing(true);
        gridView.setNumColumns(columns);
        gridView.setBackgroundColor(0);
        gridView.setVerticalSpacing(0);
        gridView.setHorizontalSpacing(0);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setOnItemClickListener(onItemClickListener);
        gridView.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        gridView.setSelector(resources.getListSelector());
        QuickAction quickAction = new QuickAction(view, s);
        quickAction.setCustomView(gridView);
        return quickAction;
    }

    public static QuickAction buildList(BaseAdapter var0, View var1, String var2, int var3, int var4, AdapterView.OnItemClickListener var5) {
        ListView listView = new ListView(resources.ctx);
        listView.setAdapter(var0);
        listView.setDivider((Drawable) null);
        listView.setDividerHeight(0);
        listView.setAlwaysDrawnWithCacheEnabled(false);
        listView.setBackgroundColor(0);
        listView.setCacheColorHint(0);
        listView.setOnItemClickListener(var5);
        listView.setLayoutParams(new LinearLayout.LayoutParams(var3, var4));
        listView.setSelector(resources.getListSelector());
        QuickAction quickAction = new QuickAction(var1, var2);
        quickAction.setCustomView(listView);
        return quickAction;
    }
}
