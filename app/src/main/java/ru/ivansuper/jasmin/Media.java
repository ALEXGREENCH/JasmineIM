package ru.ivansuper.jasmin;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.compat.AlertCompat;
import ru.ivansuper.jasmin.compat.NotificationBuilder;

/** One notification sound at a time; player operations stay on the main looper. */
public class Media {
    public static final int INC_MSG = 0, AUTH_ACCEPTED = 1, AUTH_DENIED = 2,
            AUTH_REQUEST = 3, CONTACT_IN = 4, CONTACT_OUT = 5, INC_FILE = 6,
            OUT_MSG = 7, TRANSFER_REJECTED = 8;
    public static int ring_mode = 0;
    public static int phone_mode = 0;
    private static final String INTERNAL = "$*INTERNAL*$";
    private static final int[] SOUNDS = {R.raw.inc_msg, R.raw.auth_accepted, R.raw.auth_denied,
            R.raw.auth_req, R.raw.contact_in, R.raw.contact_out, R.raw.inc_file,
            R.raw.out_msg, R.raw.transfer_rejected};
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private volatile boolean closed;

    public Media(Context context) { this.context = context.getApplicationContext(); }

    public void playEvent(int event) { enqueue(event, false); }
    public void previewEvent(int event) { enqueue(event, true); }

    private void enqueue(final int event, final boolean preview) {
        if (event < 0 || event >= SOUNDS.length || closed) return;
        handler.post(new Runnable() {
            public void run() {
                if (!allowed(event, preview)) return;
                dispose(player);
                String source = source(event);
                try { prepare(event, preview, source); }
                catch (Exception error) {
                    dispose(player);
                    Log.w("JasmineMedia", "Cannot open event sound; using built-in sound", error);
                    if (source != null && !INTERNAL.equals(source)) {
                        try { prepare(event, preview, INTERNAL); }
                        catch (Exception fallbackError) {
                            dispose(player);
                            Log.w("JasmineMedia", "Cannot play built-in event sound", fallbackError);
                        }
                    }
                }
            }
        });
    }

    private boolean allowed(int event, boolean preview) {
        return !closed && (preview || NotificationBuilder.alertsAllowed(context, PreferenceTable.heads_up_notify))
                && AlertCompat.soundAllowed(context,
                preview || (PreferenceTable.soundEnabled && enabled(event)), phone_mode);
    }

    private void prepare(final int event, final boolean preview, String source) throws Exception {
        final MediaPlayer next = new MediaPlayer();
        player = next;
        AlertCompat.configurePlayer(next);
        if (source == null || INTERNAL.equals(source)) {
            AssetFileDescriptor descriptor = context.getResources().openRawResourceFd(SOUNDS[event]);
            try { next.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength()); }
            finally { descriptor.close(); }
        } else {
            Uri uri = source.indexOf("://") >= 0 ? Uri.parse(source) : Uri.fromFile(new File(source));
            next.setDataSource(context, uri);
        }
        next.setLooping(false);
        next.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer prepared) {
                if (player != prepared) return;
                if (!allowed(event, preview)) { dispose(prepared); return; }
                try { prepared.start(); }
                catch (RuntimeException error) { dispose(prepared); Log.w("JasmineMedia", "Cannot start event sound", error); }
            }
        });
        next.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer completed) { dispose(completed); }
        });
        next.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer failed, int what, int extra) {
                dispose(failed);
                Log.w("JasmineMedia", "Event sound decoder error: " + what + "/" + extra);
                return true;
            }
        });
        next.prepareAsync();
    }

    private void dispose(MediaPlayer old) {
        if (old == null || player != old) return;
        player = null;
        old.release();
    }

    public void release() {
        closed = true;
        handler.post(new Runnable() { public void run() { dispose(player); } });
    }

    private static boolean enabled(int event) {
        switch (event) {
            case INC_MSG: return MediaTable.inc_msg_e;
            case AUTH_ACCEPTED: return MediaTable.auth_accepted_e;
            case AUTH_DENIED: return MediaTable.auth_denied_e;
            case AUTH_REQUEST: return MediaTable.auth_req_e;
            case CONTACT_IN: return MediaTable.contact_in_e;
            case CONTACT_OUT: return MediaTable.contact_out_e;
            case INC_FILE: return MediaTable.inc_file_e;
            case OUT_MSG: return MediaTable.out_msg_e;
            case TRANSFER_REJECTED: return MediaTable.transfer_rejected_e;
            default: return false;
        }
    }

    private static String source(int event) {
        switch (event) {
            case INC_MSG: return MediaTable.inc_msg;
            case AUTH_ACCEPTED: return MediaTable.auth_accepted;
            case AUTH_DENIED: return MediaTable.auth_denied;
            case AUTH_REQUEST: return MediaTable.auth_req;
            case CONTACT_IN: return MediaTable.contact_in;
            case CONTACT_OUT: return MediaTable.contact_out;
            case INC_FILE: return MediaTable.inc_file;
            case OUT_MSG: return MediaTable.out_msg;
            case TRANSFER_REJECTED: return MediaTable.transfer_rejected;
            default: return INTERNAL;
        }
    }
}
