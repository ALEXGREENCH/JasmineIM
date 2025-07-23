package ru.ivansuper.jasmin.jabber.disco;

/**
 * Interface definition for a callback to be invoked when a refresh is needed.
 * This is typically used in scenarios where data or state needs to be updated
 * or reloaded.
 *
 * @noinspection unused
 */
public interface RefreshCallback {
    void handleNeedRefresh();
}
