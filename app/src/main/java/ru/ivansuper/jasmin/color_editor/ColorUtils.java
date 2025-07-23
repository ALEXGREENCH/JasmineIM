package ru.ivansuper.jasmin.color_editor;

/** Utility wrapper for {@link ColorScheme#getColor(int)} using {@link ColorKey}. */
public final class ColorUtils {
    private ColorUtils() {}

    /**
     * Retrieve a color from {@link ColorScheme} using a typed key.
     *
     * @param key the color identifier
     * @return the ARGB color value
     */
    public static int getColor(ColorKey key) {
        return ColorScheme.getColor(key.ordinal());
    }
}
