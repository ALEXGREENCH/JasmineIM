package ru.ivansuper.jasmin.locale;

/**
 * Represents a language pack.
 */
public class Language {
    public String AUTHOR;
    public String LANGUAGE;
    public String NAME;
    public boolean internal;
    public String path;

    public Language(String name, String language, String author, String path, boolean internal) {
        this.NAME = name;
        this.LANGUAGE = language;
        this.AUTHOR = author;
        this.path = path;
        this.internal = internal;
    }
}