package ru.ivansuper.jasmin.compat;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import ru.ivansuper.jasmin.R;

/** Real decoder and vibration API smoke tests; does not claim physical output. */
public final class AlertDeviceProbe extends Instrumentation {
    private volatile Throwable failure;
    private volatile int started, completed;
    private MediaPlayer current;

    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }

    @Override public void onStart() {
        int[] sounds = {R.raw.inc_msg, R.raw.auth_accepted, R.raw.auth_denied,
                R.raw.auth_req, R.raw.contact_in, R.raw.contact_out, R.raw.inc_file,
                R.raw.out_msg, R.raw.transfer_rejected};
        for (final int sound : sounds) {
            final CountDownLatch done = new CountDownLatch(1);
            runOnMainSync(new Runnable() {
                public void run() {
                    try {
                        final MediaPlayer player = new MediaPlayer();
                        current = player;
                        AlertCompat.configurePlayer(player);
                        AssetFileDescriptor descriptor = getTargetContext().getResources().openRawResourceFd(sound);
                        try { player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength()); }
                        finally { descriptor.close(); }
                        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            public void onPrepared(MediaPlayer value) {
                                try { value.start(); started++; }
                                catch (Throwable error) { failure = error; done.countDown(); }
                            }
                        });
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer value) { completed++; done.countDown(); }
                        });
                        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            public boolean onError(MediaPlayer value, int what, int extra) {
                                failure = new AssertionError("Decoder error " + what + "/" + extra); done.countDown(); return true;
                            }
                        });
                        player.prepareAsync();
                    } catch (Throwable error) { failure = error; done.countDown(); }
                }
            });
            try { if (!done.await(10, TimeUnit.SECONDS)) failure = new AssertionError("Sound timeout: " + sound); }
            catch (InterruptedException error) { failure = error; }
            runOnMainSync(new Runnable() { public void run() { if (current != null) { current.release(); current = null; } } });
            if (failure != null) break;
        }
        runOnMainSync(new Runnable() {
            public void run() {
                try {
                    Vibrator vibrator = (Vibrator) getTargetContext().getSystemService(Context.VIBRATOR_SERVICE);
                    AlertCompat.vibrate(vibrator, 30L);
                    AlertCompat.vibrate(vibrator, new long[] {0, 20, 20, 20});
                    AlertCompat.vibrate(vibrator, 0L);
                    AlertCompat.vibrate(vibrator, new long[] {0, -1});
                    if (vibrator != null) vibrator.cancel();
                } catch (Throwable error) { failure = error; }
            }
        });
        if (failure == null && (started != 9 || completed != 9)) failure = new AssertionError("Incomplete playback");
        Bundle result = new Bundle();
        result.putString("stream", failure == null ? "PASS: 9 built-in sounds started/completed; vibration APIs smoke-tested\n"
                : "FAIL: " + android.util.Log.getStackTraceString(failure));
        finish(failure == null ? -1 : 0, result);
    }
}
