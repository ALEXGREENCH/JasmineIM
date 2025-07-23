package ru.ivansuper.jasmin;

/**
 * Represents a buffered dialog with a header and text content.
 * This class is used to store information for dialogs that are displayed
 * to the user, allowing for the separation of dialog data from its presentation.
 */
public class BufferedDialog {
    public String header;
    public String text;

    public BufferedDialog(String header, String text) {
        this.header = header;
        this.text = text;
    }
}
