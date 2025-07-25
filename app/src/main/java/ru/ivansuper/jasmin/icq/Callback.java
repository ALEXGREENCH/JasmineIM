package ru.ivansuper.jasmin.icq;

/**
 * Represents a callback interface for asynchronous operations.
 * Implementations of this interface can be used to receive notifications
 * when an operation completes or encounters an event.
 */
public interface Callback {
    void notify(Object obj, int i);
}