package ru.ivansuper.jasmin.Service;

import android.app.PendingIntent;

/**
 * Represents a handler for a PendingIntent.
 * <p>
 * This abstract class provides a base for handling PendingIntents.
 * Subclasses should implement the {@link #run()} method to define the action
 * to be performed when the PendingIntent is triggered.
 * </p>
 * <p>
 * Each handler is assigned a unique ID, which can be used to identify it.
 * The ID is generated using {@link System#currentTimeMillis()} by default,
 * but can also be specified in the constructor.
 * </p>
 */
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
