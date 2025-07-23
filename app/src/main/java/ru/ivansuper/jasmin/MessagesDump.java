package ru.ivansuper.jasmin;

/**
 * Represents a summary of messages, typically used for displaying statistics or filtering.
 * This class stores information about the total number of messages, messages from contacts,
 * whether simple messages are included, and whether conference messages are included.
 */
public class MessagesDump {
    public boolean conferences;
    public int from_contacts;
    public boolean simple_messages;
    public int total_messages;

    public void erase() {
        this.total_messages = 0;
        this.from_contacts = 0;
        this.simple_messages = false;
        this.conferences = false;
    }
}
