package ru.ivansuper.jasmin.plugins._interface;

import android.graphics.drawable.Drawable;

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
