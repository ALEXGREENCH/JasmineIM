package ru.ivansuper.jasmin.compat;

/** Scroll metrics use the same units as View.computeVerticalScroll*(). */
public final class ScrollIndicatorGeometry {
    private ScrollIndicatorGeometry() {}

    public static int thumbHeight(int track, int extent, int range, int minimum) {
        if (track <= 0 || extent <= 0 || range <= extent) return 0;
        return Math.min(track, Math.max(minimum, (int) ((long) track * extent / range)));
    }

    public static int thumbTop(int track, int thumb, int offset, int extent, int range) {
        if (range <= extent || track <= thumb) return 0;
        int clamped = Math.max(0, Math.min(offset, range - extent));
        return (int) ((long) (track - thumb) * clamped / (range - extent));
    }

    public static int alpha(long now, long visibleUntil, int fadeDuration) {
        if (now <= visibleUntil) return 255;
        long elapsed = now - visibleUntil;
        return elapsed >= fadeDuration ? 0 : 255 - (int) (elapsed * 255 / fadeDuration);
    }
}
