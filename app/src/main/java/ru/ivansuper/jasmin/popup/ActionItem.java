package ru.ivansuper.jasmin.popup;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Represents an item in a popup menu.
 *
 * <p>Each action item has an icon, a title, and an optional click listener.
 * This class provides methods to set and get these properties.
 *
 * @noinspection unused
 */
public class ActionItem {
    private Drawable icon;
    private View.OnClickListener listener;
    private String title;

    public ActionItem() {
    }

    public ActionItem(Drawable icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public View.OnClickListener getListener() {
        return this.listener;
    }
}
