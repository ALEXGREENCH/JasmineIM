package ru.ivansuper.jasmin.jabber;

import android.view.View;
import android.widget.ScrollView;

public abstract class AbstractForm {
    public String TITLE;
    public ScrollView form;

    public abstract void cancel();

    public final View getContent() {
        return this.form;
    }

    public abstract void send();
}
