import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import ru.ivansuper.jasmin.Media;
import ru.ivansuper.jasmin.MediaTable;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.compat.AlertCompat;
import ru.ivansuper.jasmin.compat.AlertPolicy;
import ru.ivansuper.jasmin.compat.NotificationBuilder;

public final class AlertsTest {
    private static int checks;
    private static void expect(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError("SDK " + Build.VERSION.SDK_INT + ": " + message);
    }
    public static void main(String[] args) throws Exception {
        for (int sdk = 4; sdk <= 36; sdk++) {
            Build.VERSION.SDK_INT = sdk;
            verifyModes();
            verifyVibration();
            verifySounds();
            verifyChannels();
        }
        expect(AlertPolicy.vibrationDuration("0") == 0, "zero duration");
        expect(AlertPolicy.vibrationDuration("-100") == 0, "negative duration");
        expect(AlertPolicy.vibrationDuration("oops") == 200, "invalid duration fallback");
        expect(AlertPolicy.vibrationDuration("350") == 350, "configured duration");
        System.out.println("PASS: " + checks + " alert checks across SDK 4 through 36 (recording platform boundaries)");
    }
    private static void verifyModes() {
        Context context = new Context();
        for (int mode = 0; mode <= 2; mode++) {
            context.audio.ringer = mode;
            for (int enabled = 0; enabled <= 1; enabled++) {
                for (int call = 0; call <= 1; call++) {
                    expect(AlertCompat.soundAllowed(context, enabled == 1, call) == (enabled == 1 && call == 0 && mode == 2), "sound preferences/ringer/call");
                    expect(AlertCompat.vibrationAllowed(context, enabled == 1, call) == (enabled == 1 && call == 0 && mode > 0), "vibration preferences/ringer/call");
                }
            }
        }
        context.audio.volume = 0;
        expect(!AlertCompat.soundAllowed(context, true, 0), "notification volume zero");
        context.audio.mode = 3;
        expect(!AlertCompat.vibrationAllowed(context, true, 0), "VoIP call suppresses vibration");
    }
    private static void verifyVibration() {
        Vibrator vibrator = new Vibrator();
        AlertCompat.vibrate(vibrator, 0L);
        AlertCompat.vibrate(vibrator, -1L);
        AlertCompat.vibrate(vibrator, (long[]) null);
        AlertCompat.vibrate(vibrator, new long[] {});
        AlertCompat.vibrate(vibrator, new long[] {0, 0});
        AlertCompat.vibrate(vibrator, new long[] {0, -2});
        expect(vibrator.calls == 0, "invalid vibrations are no-ops");
        AlertCompat.vibrate(vibrator, 200L);
        expect(vibrator.calls == 1 && vibrator.duration == 200, "one-shot vibration");
        expect(vibrator.effect == (Build.VERSION.SDK_INT >= 26), "effect API selection");
        expect(Build.VERSION.SDK_INT < 21 ? vibrator.attributes == null : vibrator.attributes.usage == 5, "notification usage permits background vibration");
        long[] pattern = {0, 50, 30, 50};
        AlertCompat.vibrate(vibrator, pattern);
        expect(vibrator.calls == 2 && vibrator.pattern == pattern && vibrator.repeat == -1, "non-repeating vibration pattern");
        vibrator.deny = true;
        AlertCompat.vibrate(vibrator, 200L); // No crash on permission/device rejection.
        AlertCompat.vibrate((Vibrator) null, 200L);
    }
    private static void verifySounds() throws Exception {
        Context context = new Context();
        PreferenceTable.soundEnabled = true;
        Media.phone_mode = 0;
        Media media = new Media(context);
        for (int event = 0; event < 9; event++) {
            int before = MediaPlayer.instances.size();
            media.playEvent(event);
            expect(MediaPlayer.instances.size() == before, "player creation must be queued");
            Handler.flush();
            MediaPlayer player = MediaPlayer.last();
            expect(context.resources.lastRaw == event + 1, "event maps to its correct built-in sound");
            expect(context.resources.opened == context.resources.closed, "raw file descriptors are closed");
            expect(Build.VERSION.SDK_INT < 21 ? player.stream == 5 : player.attributes.usage == 5 && player.attributes.content == 4, "notification stream/attributes");
            player.prepared.onPrepared(player);
            expect(player.started && player.async, "sound starts after async preparation");
            player.completed.onCompletion(player);
            expect(player.releases == 1, "completed player is released once");
        }
        int before = MediaPlayer.instances.size();
        context.audio.ringer = 0;
        media.playEvent(0); Handler.flush();
        expect(MediaPlayer.instances.size() == before, "already silent at startup");
        context.audio.ringer = 2;
        PreferenceTable.soundEnabled = false;
        media.playEvent(0); Handler.flush();
        expect(MediaPlayer.instances.size() == before, "global sound toggle");
        MediaTable.inc_msg_e = false;
        media.previewEvent(0); Handler.flush();
        MediaPlayer preview = MediaPlayer.last();
        preview.prepared.onPrepared(preview);
        expect(preview.started, "explicit preview works while app/event sounds are disabled");
        preview.completed.onCompletion(preview);
        PreferenceTable.soundEnabled = true;
        before = MediaPlayer.instances.size();
        media.playEvent(0); Handler.flush();
        expect(MediaPlayer.instances.size() == before, "per-event sound toggle");
        MediaTable.inc_msg_e = true;
        context.notifications.enabled = false;
        media.playEvent(0); Handler.flush();
        expect((MediaPlayer.instances.size() == before) == (Build.VERSION.SDK_INT >= 24), "system notification switch");
        context.notifications.enabled = true;
        if (Build.VERSION.SDK_INT >= 26) {
            context.notifications.createNotificationChannel(new android.app.NotificationChannel(
                    NotificationBuilder.MESSAGE_CHANNEL_DEFAULT_ID, "Messages", 0));
            before = MediaPlayer.instances.size();
            media.playEvent(0); Handler.flush();
            expect(MediaPlayer.instances.size() == before, "blocked message channel suppresses sound");
            media.previewEvent(0); Handler.flush();
            MediaPlayer explicit = MediaPlayer.last();
            explicit.prepared.onPrepared(explicit);
            expect(explicit.started, "explicit preview bypasses notification blocking");
            explicit.completed.onCompletion(explicit);
            context.notifications.channels.clear();
        }
        MediaTable.inc_msg = "/missing/test.ogg";
        media.playEvent(0); Handler.flush();
        MediaPlayer fallback = MediaPlayer.last();
        fallback.prepared.onPrepared(fallback);
        expect(fallback.started && fallback.uri == null, "missing custom file falls back to built-in");
        fallback.completed.onCompletion(fallback);
        MediaTable.inc_msg = "content://sounds/test";
        media.playEvent(0); Handler.flush();
        expect(MediaPlayer.last().uri.value.equals(MediaTable.inc_msg), "content URI support");
        MediaPlayer.last().error.onError(MediaPlayer.last(), 1, 0);
        expect(MediaPlayer.last().released, "decoder error releases player");
        MediaTable.inc_msg = "$*INTERNAL*$";
        media.playEvent(0); Handler.flush();
        MediaPlayer pending = MediaPlayer.last();
        PreferenceTable.soundEnabled = false;
        pending.prepared.onPrepared(pending);
        expect(!pending.started && pending.released, "turning sound off during preparation");
        PreferenceTable.soundEnabled = true;
        media.playEvent(0); Handler.flush();
        MediaPlayer first = MediaPlayer.last();
        media.playEvent(1); Handler.flush();
        MediaPlayer second = MediaPlayer.last();
        first.prepared.onPrepared(first);
        first.completed.onCompletion(first);
        second.prepared.onPrepared(second);
        expect(first.releases == 1 && !first.started && second.started, "rapid events/stale callbacks");
        media.playEvent(2);
        media.release(); Handler.flush();
        expect(second.released, "shutdown releases active player and cancels pending sound");
        before = MediaPlayer.instances.size();
        media.playEvent(0); Handler.flush();
        expect(MediaPlayer.instances.size() == before, "closed media cannot restart");
    }
    private static void verifyChannels() {
        Context context = new Context();
        if (Build.VERSION.SDK_INT >= 11) {
            android.app.Notification notice = new NotificationBuilder(context).setHeadsUpPriority().build();
            expect(notice.priority == (Build.VERSION.SDK_INT >= 16 ? 1 : 0), "heads-up priority API selection");
            boolean legacyHeadsUp = Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 26;
            expect((notice.vibrate != null) == legacyHeadsUp, "silent heads-up marker only on Android 5-7");
            if (legacyHeadsUp) expect(notice.vibrate.length == 1 && notice.vibrate[0] == 0, "heads-up does not cause a second vibration");
        }
        context.notifications.enabled = false;
        expect(NotificationBuilder.alertsAllowed(context, false) == (Build.VERSION.SDK_INT < 24), "system notification policy API selection");
        context.notifications.enabled = true;
        if (Build.VERSION.SDK_INT >= 26) {
            for (int importance = 0; importance <= 4; importance++) {
                context.notifications.createNotificationChannel(new android.app.NotificationChannel(
                        NotificationBuilder.MESSAGE_CHANNEL_DEFAULT_ID, "Messages", importance));
                expect(NotificationBuilder.alertsAllowed(context, false) == (importance >= 3), "channel blocked/silent/alerting importance");
            }
            context.notifications.getNotificationChannel(NotificationBuilder.MESSAGE_CHANNEL_DEFAULT_ID).importance = 0;
            expect(NotificationBuilder.alertsAllowed(context, true), "independent heads-up channel policy");
        }
        NotificationBuilder.createSilentChannel(context, "messages-v2", "messages", "Messages", 4);
        if (Build.VERSION.SDK_INT < 26) {
            expect(context.notifications.channels.isEmpty(), "no channel access on pre-26 Android"); return;
        }
        android.app.NotificationChannel channel = context.notifications.getNotificationChannel("messages-v2");
        expect(channel.importance == 4 && !channel.sound && !channel.vibration, "heads-up channel has no duplicate system alert");
        channel.importance = 0;
        NotificationBuilder.createSilentChannel(context, "messages-v2", "messages", "Messages", 4);
        expect(channel == context.notifications.getNotificationChannel("messages-v2") && channel.importance == 0, "existing channel/user choices preserved");
        context.notifications.createNotificationChannel(new android.app.NotificationChannel("old-disabled", "Old", 0));
        NotificationBuilder.createSilentChannel(context, "new-disabled", "old-disabled", "New", 4);
        expect(context.notifications.getNotificationChannel("new-disabled").importance == 0, "migration preserves blocked channel");
    }
}
