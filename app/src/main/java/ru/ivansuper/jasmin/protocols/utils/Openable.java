package ru.ivansuper.jasmin.protocols.utils;

/**
 * Represents an object that can be opened or closed.
 * This interface provides methods to check the opened state,
 * set the opened state, and toggle the opened state.
 */
public interface Openable {
    /** @noinspection unused*/
    boolean isOpened();

    /** @noinspection unused*/
    void setOpened(boolean z);

    /** @noinspection unused*/
    void toggleOpened();
}