package ru.ivansuper.jasmin.compat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

/** API 4-safe signatures; the framework Builder is loaded only on API 11+. */
public final class NotificationBuilder {
    private final Object builder;

    public NotificationBuilder(Context context) {
        builder = Api11.create(context);
    }

    public NotificationBuilder(Context context, String channel) {
        builder = Api26.create(context, channel);
    }

    public static void createChannel(Context context, String id, String name, int importance) {
        if (Build.VERSION.SDK_INT >= 26) Api26.createChannel(context, id, name, importance);
    }

    public NotificationBuilder setSmallIcon(int icon) {
        Api11.setSmallIcon(builder, icon);
        return this;
    }

    public NotificationBuilder setContentTitle(CharSequence title) {
        Api11.setContentTitle(builder, title);
        return this;
    }

    public NotificationBuilder setContentText(CharSequence text) {
        Api11.setContentText(builder, text);
        return this;
    }

    public NotificationBuilder setContentIntent(PendingIntent intent) {
        Api11.setContentIntent(builder, intent);
        return this;
    }

    public NotificationBuilder setAutoCancel(boolean value) {
        Api11.setAutoCancel(builder, value);
        return this;
    }

    public NotificationBuilder setOngoing(boolean value) {
        Api11.setOngoing(builder, value);
        return this;
    }

    public NotificationBuilder setLights(int color, int on, int off) {
        Api11.setLights(builder, color, on, off);
        return this;
    }

    public NotificationBuilder setDefaults(int defaults) {
        Api11.setDefaults(builder, defaults);
        return this;
    }

    public NotificationBuilder setNumber(int count) {
        Api11.setNumber(builder, count);
        return this;
    }

    public NotificationBuilder setTicker(CharSequence ticker) {
        Api11.setTicker(builder, ticker);
        return this;
    }

    public NotificationBuilder setPriority(int priority) {
        if (Build.VERSION.SDK_INT >= 16) Api16.setPriority(builder, priority);
        return this;
    }

    public NotificationBuilder setCategory(String category) {
        if (Build.VERSION.SDK_INT >= 21) Api21.setCategory(builder, category);
        return this;
    }

    public NotificationBuilder setChannelId(String channel) {
        if (Build.VERSION.SDK_INT >= 26) Api26.setChannelId(builder, channel);
        return this;
    }

    public Notification build() {
        return Build.VERSION.SDK_INT >= 16 ? Api16.build(builder) : Api11.getNotification(builder);
    }
    public NotificationBuilder setBigText(CharSequence text) {
        if (Build.VERSION.SDK_INT >= 16) Api16.setBigText(builder, text);
        return this;
    }
    public Notification getNotification() { return Api11.getNotification(builder); }

    @TargetApi(11)
    private static class Api11 {
        static Object create(Context context) { return new Notification.Builder(context); }
        static Notification getNotification(Object builder) { return ((Notification.Builder) builder).getNotification(); }
        static void setSmallIcon(Object builder, int icon) { ((Notification.Builder) builder).setSmallIcon(icon); }
        static void setContentTitle(Object builder, CharSequence title) { ((Notification.Builder) builder).setContentTitle(title); }
        static void setContentText(Object builder, CharSequence text) { ((Notification.Builder) builder).setContentText(text); }
        static void setContentIntent(Object builder, PendingIntent intent) { ((Notification.Builder) builder).setContentIntent(intent); }
        static void setAutoCancel(Object builder, boolean value) { ((Notification.Builder) builder).setAutoCancel(value); }
        static void setOngoing(Object builder, boolean value) { ((Notification.Builder) builder).setOngoing(value); }
        static void setLights(Object builder, int color, int on, int off) { ((Notification.Builder) builder).setLights(color, on, off); }
        static void setDefaults(Object builder, int defaults) { ((Notification.Builder) builder).setDefaults(defaults); }
        static void setNumber(Object builder, int count) { ((Notification.Builder) builder).setNumber(count); }
        static void setTicker(Object builder, CharSequence ticker) { ((Notification.Builder) builder).setTicker(ticker); }
    }

    @TargetApi(16)
    private static class Api16 {
        static void setBigText(Object builder, CharSequence text) {
            ((Notification.Builder) builder).setStyle(new Notification.BigTextStyle().bigText(text));
        }
        static Notification build(Object builder) { return ((Notification.Builder) builder).build(); }
        static void setPriority(Object builder, int priority) { ((Notification.Builder) builder).setPriority(priority); }
    }

    @TargetApi(21)
    private static class Api21 {
        static void setCategory(Object builder, String category) { ((Notification.Builder) builder).setCategory(category); }
    }

    @TargetApi(26)
    private static class Api26 {
        static Object create(Context context, String channel) { return new Notification.Builder(context, channel); }
        static void createChannel(Context context, String id, String name, int importance) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(new android.app.NotificationChannel(id, name, importance));
        }
        static void setChannelId(Object builder, String channel) { ((Notification.Builder) builder).setChannelId(channel); }
    }
}
