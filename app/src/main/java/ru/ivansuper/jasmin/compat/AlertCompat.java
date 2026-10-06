package ru.ivansuper.jasmin.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

/** Notification audio/vibration classification, with API-4-safe entry points. */
public final class AlertCompat {
    private AlertCompat() {}

    public static boolean soundAllowed(Context context, boolean enabled, int phoneMode) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audio != null && AlertPolicy.soundAllowed(enabled, audio.getRingerMode(),
                phoneMode != 0 || audio.getMode() != AudioManager.MODE_NORMAL)
                && audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION) > 0;
    }

    public static boolean vibrationAllowed(Context context, boolean enabled, int phoneMode) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audio != null && AlertPolicy.vibrationAllowed(enabled, audio.getRingerMode(),
                phoneMode != 0 || audio.getMode() != AudioManager.MODE_NORMAL);
    }

    public static void configurePlayer(MediaPlayer player) {
        if (Build.VERSION.SDK_INT >= 21) Api21.configurePlayer(player);
        else player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
    }

    public static void vibrate(Vibrator vibrator, long duration) {
        if (vibrator == null || duration <= 0) return;
        try {
            if (Build.VERSION.SDK_INT >= 26) Api26.vibrate(vibrator, duration);
            else if (Build.VERSION.SDK_INT >= 21) Api21.vibrate(vibrator, duration);
            else vibrator.vibrate(duration);
        } catch (RuntimeException unavailable) { Log.w("JasmineAlerts", "Vibration unavailable", unavailable); }
    }

    public static void vibrate(Vibrator vibrator, long[] pattern) {
        if (vibrator == null || !AlertPolicy.validPattern(pattern)) return;
        try {
            if (Build.VERSION.SDK_INT >= 26) Api26.vibrate(vibrator, pattern);
            else if (Build.VERSION.SDK_INT >= 21) Api21.vibrate(vibrator, pattern);
            else vibrator.vibrate(pattern, -1);
        } catch (RuntimeException unavailable) { Log.w("JasmineAlerts", "Vibration unavailable", unavailable); }
    }

    @TargetApi(21)
    private static class Api21 {
        static android.media.AudioAttributes attributes() {
            return new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        }
        static void configurePlayer(MediaPlayer player) { player.setAudioAttributes(attributes()); }
        static void vibrate(Vibrator vibrator, long duration) { vibrator.vibrate(duration, attributes()); }
        static void vibrate(Vibrator vibrator, long[] pattern) { vibrator.vibrate(pattern, -1, attributes()); }
    }

    @TargetApi(26)
    private static class Api26 {
        static void vibrate(Vibrator vibrator, long duration) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE), Api21.attributes());
        }
        static void vibrate(Vibrator vibrator, long[] pattern) {
            vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1), Api21.attributes());
        }
    }
}
