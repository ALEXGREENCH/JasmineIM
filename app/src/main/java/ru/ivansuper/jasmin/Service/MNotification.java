package ru.ivansuper.jasmin.Service;

import android.content.Intent;

public class MNotification {
    public Intent intent;
    public String nick;
    public String schema;
    public String text;

    public MNotification() {}

    public MNotification(String nick, String text, Intent intent, String scheme) {
        this.nick = nick;
        this.text = text;
        this.intent = intent;
        this.schema = scheme;
    }
}