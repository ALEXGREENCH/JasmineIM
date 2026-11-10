package ru.ivansuper.jasmin.locale;

/** Keep translations deterministic without starting the application UI. */
public final class Locale {
    public static String getString(String key) { return key; }
    public static String getCurrentLangCode() { return "en"; }
}
