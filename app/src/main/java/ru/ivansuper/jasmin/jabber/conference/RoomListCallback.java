package ru.ivansuper.jasmin.jabber.conference;

import java.util.Vector;

/**
 * Callback interface for receiving results of room list loading operations.
 */
public interface RoomListCallback {
    void error();

    void roomsLoaded(Vector<RoomsPreviewAdapter.Item> vector);
}
