package ru.ivansuper.jasmin.Service;

import android.app.PendingIntent;

public abstract class PendingIntentHandler {
    public long id;
    public PendingIntent intent;

    public abstract void run();

    public PendingIntentHandler() {
        this.id = System.currentTimeMillis();
    }

    public PendingIntentHandler(long id) {
        this.id = id;
    }
}
