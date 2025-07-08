package ru.ivansuper.jasmin;

import android.graphics.drawable.Drawable;

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
