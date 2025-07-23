package ru.ivansuper.jasmin.protocols.utils;

import android.graphics.drawable.Drawable;

/**
 * Represents client information, including an icon and a description.
 * This class is used to store and manage information about a client.
 */
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