package ru.ivansuper.jasmin.jabber.commands;

import java.util.Vector;

public interface Callback {
    void onListLoaded(Vector<CommandItem> vector);
}
