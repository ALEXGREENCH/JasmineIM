package ru.ivansuper.jasmin.protocols.utils;

import android.graphics.drawable.Drawable;

public class ClientInfo {
    private String mDescription;
    private Drawable mIcon;

    public ClientInfo() {
    }

    /** @noinspection unused*/
    public ClientInfo(Drawable icon, String description) {
        setInfo(icon, description);
    }

    /** @noinspection unused*/
    public final void reset() {
        this.mIcon = null;
        this.mDescription = null;
    }

    public final void setInfo(Drawable icon, String description) {
        this.mIcon = icon;
        this.mDescription = description;
    }

    /** @noinspection unused*/
    public final boolean infoPresent() {
        return this.mIcon != null && this.mDescription != null;
    }

    public final Drawable getIcon() {
        return this.mIcon;
    }

    /** @noinspection unused*/
    public final String getDescription() {
        return this.mDescription;
    }
}