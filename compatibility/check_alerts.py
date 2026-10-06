"""Exercise real alert code with recording platform boundaries for SDK 4 through 36.

This checks API selection, preferences and player lifecycle, not a device's audio
driver, speaker or vibration motor. Device smoke checks complement this suite.
"""
from pathlib import Path
import subprocess
from check_ui import java_tool

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'build/compatibility/alerts'
STUBS = {
'android/annotation/TargetApi.java': '''package android.annotation; public @interface TargetApi { int value(); }''',
'android/os/Build.java': '''package android.os; public class Build { public static class VERSION { public static int SDK_INT = 4; } }''',
'android/os/Looper.java': '''package android.os; public class Looper { public static Looper getMainLooper() { return new Looper(); } }''',
'android/os/Handler.java': '''package android.os; import java.util.ArrayDeque;
public class Handler {
    static final ArrayDeque<Runnable> queue = new ArrayDeque<Runnable>();
    public Handler(Looper looper) {}
    public boolean post(Runnable runnable) { queue.add(runnable); return true; }
    public static void flush() { while (!queue.isEmpty()) queue.remove().run(); }
}''',
'android/util/Log.java': '''package android.util; public class Log {
    public static int w(String tag, String message) { return 0; }
    public static int w(String tag, String message, Throwable error) { return 0; }
}''',
'android/net/Uri.java': '''package android.net; import java.io.File; public class Uri {
    public final String value; private Uri(String value) { this.value=value; }
    public static Uri parse(String value) { return new Uri(value); }
    public static Uri fromFile(File file) { return new Uri("file://" + file.getPath()); }
}''',
'android/content/Context.java': '''package android.content;
public class Context {
    public static final String AUDIO_SERVICE="audio", NOTIFICATION_SERVICE="notification";
    public final android.media.AudioManager audio = new android.media.AudioManager();
    public final android.app.NotificationManager notifications = new android.app.NotificationManager();
    public final android.content.res.Resources resources = new android.content.res.Resources();
    public Object getSystemService(String name) { return AUDIO_SERVICE.equals(name) ? audio : notifications; }
    public Context getApplicationContext() { return this; }
    public android.content.res.Resources getResources() { return resources; }
}''',
'android/content/res/Resources.java': '''package android.content.res; public class Resources {
    public int lastRaw; public int opened, closed;
    public AssetFileDescriptor openRawResourceFd(int id) { lastRaw=id; opened++; return new AssetFileDescriptor(this); }
}''',
'android/content/res/AssetFileDescriptor.java': '''package android.content.res; import java.io.FileDescriptor;
public class AssetFileDescriptor {
    final Resources owner; public AssetFileDescriptor(Resources owner) { this.owner=owner; }
    public FileDescriptor getFileDescriptor() { return new FileDescriptor(); }
    public long getStartOffset() { return 0; } public long getLength() { return 100; }
    public void close() { owner.closed++; }
}''',
'android/media/AudioManager.java': '''package android.media; public class AudioManager {
    public static final int STREAM_NOTIFICATION=5, MODE_NORMAL=0;
    public int ringer=2, mode=0, volume=5;
    public int getRingerMode() { return ringer; } public int getMode() { return mode; }
    public int getStreamVolume(int stream) { if (stream != 5) throw new AssertionError("Wrong volume stream"); return volume; }
}''',
'android/media/AudioAttributes.java': '''package android.media; public class AudioAttributes {
    public static final int USAGE_NOTIFICATION=5, CONTENT_TYPE_SONIFICATION=4;
    public int usage, content;
    public static class Builder {
        final AudioAttributes value=new AudioAttributes();
        public Builder setUsage(int usage) { value.usage=usage; return this; }
        public Builder setContentType(int content) { value.content=content; return this; }
        public AudioAttributes build() { return value; }
    }
}''',
'android/media/MediaPlayer.java': '''package android.media;
import android.content.Context; import android.net.Uri; import java.io.FileDescriptor; import java.io.IOException;
import java.util.ArrayList;
public class MediaPlayer {
    public static final ArrayList<MediaPlayer> instances = new ArrayList<MediaPlayer>();
    public boolean started, released, async; public int stream=3, releases; public AudioAttributes attributes;
    public Uri uri;
    public interface OnPreparedListener { void onPrepared(MediaPlayer player); }
    public interface OnCompletionListener { void onCompletion(MediaPlayer player); }
    public interface OnErrorListener { boolean onError(MediaPlayer player, int what, int extra); }
    public OnPreparedListener prepared; public OnCompletionListener completed; public OnErrorListener error;
    public MediaPlayer() { instances.add(this); }
    public static MediaPlayer last() { return instances.get(instances.size()-1); }
    public void setAudioStreamType(int stream) { this.stream=stream; }
    public void setAudioAttributes(AudioAttributes attributes) { this.attributes=attributes; }
    public void setDataSource(FileDescriptor fd, long offset, long length) {}
    public void setDataSource(Context ctx, Uri uri) throws IOException { this.uri=uri; if (uri.value.contains("missing")) throw new IOException("Missing test file"); }
    public void setLooping(boolean looping) { if (looping) throw new AssertionError("Looping notification"); }
    public void setOnPreparedListener(OnPreparedListener listener) { prepared=listener; }
    public void setOnCompletionListener(OnCompletionListener listener) { completed=listener; }
    public void setOnErrorListener(OnErrorListener listener) { error=listener; }
    public void prepareAsync() { async=true; }
    public void start() { if (released || !async) throw new AssertionError("Invalid player state"); started=true; }
    public void release() { releases++; if (released) throw new AssertionError("Double release"); released=true; }
}''',
'android/os/VibrationEffect.java': '''package android.os; public class VibrationEffect {
    public static final int DEFAULT_AMPLITUDE=-1;
    public long duration; public long[] pattern; public int repeat;
    public static VibrationEffect createOneShot(long duration, int amplitude) {
        if (duration <= 0) throw new IllegalArgumentException();
        VibrationEffect result=new VibrationEffect(); result.duration=duration; return result;
    }
    public static VibrationEffect createWaveform(long[] pattern, int repeat) {
        VibrationEffect result=new VibrationEffect(); result.pattern=pattern; result.repeat=repeat; return result;
    }
}''',
'android/os/Vibrator.java': '''package android.os; import android.media.AudioAttributes;
public class Vibrator {
    public int calls, repeat; public long duration; public long[] pattern; public AudioAttributes attributes;
    public boolean effect, deny;
    private void record() { calls++; if (deny) throw new SecurityException("Denied test vibration"); }
    public void vibrate(long duration) { record(); this.duration=duration; }
    public void vibrate(long[] pattern, int repeat) { record(); this.pattern=pattern; this.repeat=repeat; }
    public void vibrate(long duration, AudioAttributes attributes) { vibrate(duration); this.attributes=attributes; }
    public void vibrate(long[] pattern, int repeat, AudioAttributes attributes) { vibrate(pattern,repeat); this.attributes=attributes; }
    public void vibrate(VibrationEffect effect, AudioAttributes attributes) {
        record(); this.effect=true; duration=effect.duration; pattern=effect.pattern; repeat=effect.repeat; this.attributes=attributes;
    }
}''',
'android/app/PendingIntent.java': '''package android.app; public class PendingIntent {}''',
'android/app/Notification.java': '''package android.app; import android.content.Context;
public class Notification {
    public static final int PRIORITY_HIGH=1;
    public int priority; public long[] vibrate;
    public static class Builder {
        private final Notification value=new Notification();
        public Builder(Context context) {} public Builder(Context context,String channel) {}
        public Builder setSmallIcon(int v) { return this; } public Builder setContentTitle(CharSequence v) { return this; }
        public Builder setContentText(CharSequence v) { return this; } public Builder setContentIntent(PendingIntent v) { return this; }
        public Builder setAutoCancel(boolean v) { return this; } public Builder setOngoing(boolean v) { return this; }
        public Builder setLights(int a,int b,int c) { return this; } public Builder setDefaults(int v) { return this; }
        public Builder setNumber(int v) { return this; } public Builder setTicker(CharSequence v) { return this; }
        public Builder setPriority(int v) { value.priority=v; return this; } public Builder setCategory(String v) { return this; }
        public Builder setVibrate(long[] v) { value.vibrate=v; return this; }
        public Builder setChannelId(String v) { return this; } public Builder setStyle(BigTextStyle v) { return this; }
        public Notification build() { return value; } public Notification getNotification() { return build(); }
    }
    public static class BigTextStyle { public BigTextStyle bigText(CharSequence v) { return this; } }
}''',
'android/app/NotificationChannel.java': '''package android.app; public class NotificationChannel {
    public final String id; public int importance; public boolean sound=true, vibration=true, lights;
    public NotificationChannel(String id,String name,int importance) { this.id=id; this.importance=importance; }
    public int getImportance() { return importance; }
    public void setSound(android.net.Uri sound, android.media.AudioAttributes attributes) { this.sound=sound != null; }
    public void enableVibration(boolean v) { vibration=v; } public void enableLights(boolean v) { lights=v; }
    public boolean shouldShowLights() { return lights; }
}''',
'android/app/NotificationManager.java': '''package android.app; import java.util.HashMap; public class NotificationManager {
    public static final int IMPORTANCE_NONE=0, IMPORTANCE_DEFAULT=3;
    public boolean enabled=true;
    public boolean areNotificationsEnabled() { return enabled; }
    public final HashMap<String,NotificationChannel> channels=new HashMap<String,NotificationChannel>();
    public NotificationChannel getNotificationChannel(String id) { return channels.get(id); }
    public void createNotificationChannel(NotificationChannel channel) { channels.put(channel.id,channel); }
}''',
'ru/ivansuper/jasmin/R.java': '''package ru.ivansuper.jasmin; public class R { public static class raw {
    public static final int inc_msg=1,auth_accepted=2,auth_denied=3,auth_req=4,contact_in=5,contact_out=6,inc_file=7,out_msg=8,transfer_rejected=9;
} }''',
'ru/ivansuper/jasmin/Preferences/PreferenceTable.java': '''package ru.ivansuper.jasmin.Preferences; public class PreferenceTable { public static boolean soundEnabled=true, heads_up_notify=false; }''',
'ru/ivansuper/jasmin/MediaTable.java': '''package ru.ivansuper.jasmin; public class MediaTable {
    public static String inc_msg="$*INTERNAL*$",auth_accepted=inc_msg,auth_denied=inc_msg,auth_req=inc_msg,
        contact_in=inc_msg,contact_out=inc_msg,inc_file=inc_msg,out_msg=inc_msg,transfer_rejected=inc_msg;
    public static boolean inc_msg_e=true,auth_accepted_e=true,auth_denied_e=true,auth_req_e=true,
        contact_in_e=true,contact_out_e=true,inc_file_e=true,out_msg_e=true,transfer_rejected_e=true;
}''',
}

if __name__ == '__main__':
    sources = []
    for name, source in STUBS.items():
        path = OUTPUT / 'stubs' / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(source, encoding='utf-8')
        sources.append(str(path))
    classes = OUTPUT / 'classes'
    classes.mkdir(parents=True, exist_ok=True)
    for name in ('Media.java', 'compat/AlertPolicy.java', 'compat/AlertCompat.java', 'compat/NotificationBuilder.java'):
        sources.append(str(ROOT / 'app/src/main/java/ru/ivansuper/jasmin' / name))
    sources.append(str(ROOT / 'compatibility/alerts/AlertsTest.java'))
    subprocess.run([java_tool('javac'), '-encoding', 'UTF-8', '-d', str(classes), *sources], check=True)
    subprocess.run([java_tool('java'), '-cp', str(classes), 'AlertsTest'], check=True)
