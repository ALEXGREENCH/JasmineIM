package ru.ivansuper.jasmin;

import android.graphics.drawable.Drawable;

/**
 * Represents a task to be displayed in a popup menu.
 * Each task has an icon, a runnable action, and a variable string.
 */
public class PopupTask {
    public Drawable icon;
    public Runnable task;
    public String variable;

    public PopupTask(String var, Runnable task, Drawable icon) {
        this.variable = var;
        this.task = task;
        this.icon = icon;
    }
}
