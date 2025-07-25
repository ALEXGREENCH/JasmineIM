package ru.ivansuper.jasmin;

/**
 * Minimal compatibility helper for integer comparisons on older API levels.
 * Mirrors the behavior of {@code Integer.compare} introduced in later Java versions.
 */
public final class IntegerCompat {
    private IntegerCompat() {}

    public static int compare(int a, int b) {
        return a < b ? -1 : (a == b ? 0 : 1);
    }
}
