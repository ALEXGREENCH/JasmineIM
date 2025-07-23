package ru.ivansuper.jasmin.chats;

/**
 * Callback interface for chat initialization events.
 * Implement this interface to receive a notification when a chat session has been successfully initialized.
 */
public interface ChatInitCallback {
    void chatInitialized();
}