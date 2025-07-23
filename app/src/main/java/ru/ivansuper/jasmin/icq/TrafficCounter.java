package ru.ivansuper.jasmin.icq;

/**
 * Utility class for tracking network traffic.
 * This class provides static methods to count incoming (IN) and outgoing (OUT) data in bytes.
 * It also includes a method to reset the counters.
 *
 * @noinspection unused
 */
public class TrafficCounter {
    public static long IN = 0;
    public static long OUT = 0;

    public static void reset() {
        IN = 0L;
        OUT = 0L;
    }

    public static void in(int length) {
        IN += length;
    }

    public static void out(int length) {
        OUT += length;
    }
}
