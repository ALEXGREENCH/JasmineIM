package ru.ivansuper.jasmin.Clients;

import android.graphics.drawable.Drawable;

/**
 * Represents information about a client, such as their icon, name, and index.
 * This class is used to store and manage client-specific data.
 */
public class ClientInfo {
    /**
     * Constant representing no specific client.
     * @noinspection unused
     */
    public static final int NONE = -1;
    /**
     * The icon associated with the client.
     */
    public Drawable icon;
    /**
     * Index of this client's information within a larger collection or array.
     * A value of -1 typically indicates that the client information is not
     * currently associated with an index or is in an uninitialized state.
     */
    public int info_index = -1;
    /**
     * The name of the client.
     */
    public String name;

    /**
     * Resets the client information to its default state.
     * Sets the info_index to -1, icon to null, and name to null.
     */
    public final void reset() {
        this.info_index = -1;
        this.icon = null;
        this.name = null;
    }
}