package ru.ivansuper.jasmin.plugins._interface;

import android.graphics.drawable.Drawable;

/**
 * A wrapper class for menu items, containing an icon, label, and ID.
 * This class is used to represent menu items in a consistent way across different parts of the application.
 */
public class MenuItemWrapper {
    public Drawable icon;
    public int id;
    public String label;

    public MenuItemWrapper(Drawable icon, String label, int id) {
        this.icon = icon;
        this.label = label;
        this.id = id;
    }
}
