package ru.ivansuper.jasmin.compat;

/** App preferences combined with Android's normal/vibrate/silent modes. */
public final class AlertPolicy {
    private AlertPolicy() {}

    public static boolean soundAllowed(boolean enabled, int ringerMode, boolean inCall) {
        return enabled && !inCall && ringerMode == 2;
    }

    public static boolean vibrationAllowed(boolean enabled, int ringerMode, boolean inCall) {
        return enabled && !inCall && (ringerMode == 1 || ringerMode == 2);
    }

    public static long vibrationDuration(String value) {
        try { return Math.max(0L, Long.parseLong(value)); }
        catch (RuntimeException invalid) { return 200L; }
    }

    public static boolean validPattern(long[] pattern) {
        if (pattern == null || pattern.length == 0) return false;
        boolean on = false;
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] < 0) return false;
            if ((i & 1) != 0 && pattern[i] > 0) on = true;
        }
        return on;
    }
}
