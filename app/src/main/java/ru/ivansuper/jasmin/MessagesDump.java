package ru.ivansuper.jasmin;

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
