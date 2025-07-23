package ru.ivansuper.jasmin.Service;

import android.content.Intent;

/**
 * Represents a notification to be displayed to the user.
 *
 * This class encapsulates the data required to create and display a notification,
 * including the sender's nickname, the notification text, an intent to be launched
 * when the notification is clicked, and a schema.
 */
public class MNotification {
    public Intent intent;
    public String nick;
    public String schema;
    public String text;

    public MNotification() {}

    /** @noinspection unused*/
    public MNotification(String nick, String text, Intent intent, String scheme) {
        this.nick = nick;
        this.text = text;
        this.intent = intent;
        this.schema = scheme;
    }
}