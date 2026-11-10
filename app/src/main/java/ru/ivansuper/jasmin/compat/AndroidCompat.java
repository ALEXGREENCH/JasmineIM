package ru.ivansuper.jasmin.compat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewConfiguration;
import java.io.File;

/** Keep post-Donut framework references in separately loaded classes for Dalvik 1.6. */
public final class AndroidCompat {
    private AndroidCompat() {}

    public static void startForeground(Service service, int id, Notification notification) {
        if (Build.VERSION.SDK_INT >= 5) {
            Api5.startForeground(service, id, notification);
        } else {
            legacyForeground(service, true);
            ((NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, notification);
        }
    }

    public static void stopForeground(Service service, int id) {
        if (Build.VERSION.SDK_INT >= 5) {
            Api5.stopForeground(service);
        } else {
            ((NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
            legacyForeground(service, false);
        }
    }

    private static void legacyForeground(Service service, boolean foreground) {
        try {
            // Public in API 4; removed from the modern compile SDK.
            Service.class.getMethod("setForeground", boolean.class).invoke(service, foreground);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot change legacy service foreground state", e);
        }
    }

    public static void startService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) Api26.startService(context, intent);
        else context.startService(intent);
    }

    public static File externalFilesDir(Context context) {
        if (Build.VERSION.SDK_INT >= 8) return Api8.externalFilesDir(context);
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) return null;
        File dir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + context.getPackageName() + "/files");
        return dir.isDirectory() || dir.mkdirs() ? dir : null;
    }

    public static void apply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= 9) Api9.apply(editor);
        else editor.commit();
    }

    public static int checkSelfPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT >= 23 ? Api23.checkSelfPermission(context, permission)
                : context.checkCallingOrSelfPermission(permission);
    }

    public static void requestPermissions(Activity activity, String[] permissions, int code) {
        if (Build.VERSION.SDK_INT >= 23) Api23.requestPermissions(activity, permissions, code);
    }

    public static void copyText(Context context, String label, String text) {
        if (Build.VERSION.SDK_INT >= 11) Api11.copyText(context, label, text);
        else ((android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setText(text);
    }

    public static boolean hasHardwareMenuKey(Context context) {
        return Build.VERSION.SDK_INT < 14 || Api14.hasHardwareMenuKey(context);
    }

    public static void invalidateOnAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 16) Api16.invalidateOnAnimation(view);
        else view.postInvalidate();
    }

    public static void vibrate(Vibrator vibrator, long duration) {
        if (Build.VERSION.SDK_INT >= 26) Api26.vibrate(vibrator, duration);
        else vibrator.vibrate(duration);
    }

    public static void vibrate(Vibrator vibrator, long[] pattern) {
        if (Build.VERSION.SDK_INT >= 26) Api26.vibrate(vibrator, pattern);
        else vibrator.vibrate(pattern, -1);
    }

    @TargetApi(5)
    private static class Api5 {
        static void startForeground(Service service, int id, Notification notification) { service.startForeground(id, notification); }
        static void stopForeground(Service service) { service.stopForeground(true); }
    }
    @TargetApi(8)
    private static class Api8 {
        static File externalFilesDir(Context context) { return context.getExternalFilesDir(null); }
    }
    @TargetApi(9)
    private static class Api9 {
        static void apply(SharedPreferences.Editor editor) { editor.apply(); }
    }
    @TargetApi(11)
    private static class Api11 {
        static void copyText(Context context, String label, String text) {
            ((android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(android.content.ClipData.newPlainText(label, text));
        }
    }
    @TargetApi(14)
    private static class Api14 {
        static boolean hasHardwareMenuKey(Context context) { return ViewConfiguration.get(context).hasPermanentMenuKey(); }
    }
    @TargetApi(16)
    private static class Api16 {
        static void invalidateOnAnimation(View view) { view.postInvalidateOnAnimation(); }
    }
    @TargetApi(23)
    private static class Api23 {
        static int checkSelfPermission(Context context, String permission) { return context.checkSelfPermission(permission); }
        static void requestPermissions(Activity activity, String[] permissions, int code) { activity.requestPermissions(permissions, code); }
    }
    @TargetApi(26)
    private static class Api26 {
        static void startService(Context context, Intent intent) { context.startForegroundService(intent); }
        static void vibrate(Vibrator vibrator, long duration) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
        }
        static void vibrate(Vibrator vibrator, long[] pattern) {
            vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
        }
    }
}
