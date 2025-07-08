package ru.ivansuper.jasmin.jabber.conference;

import java.util.Vector;

public interface RoomListCallback {
    void error();

    void roomsLoaded(Vector<RoomsPreviewAdapter.Item> vector);
}
