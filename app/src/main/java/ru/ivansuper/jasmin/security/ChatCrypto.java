package ru.ivansuper.jasmin.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ru.ivansuper.jasmin.resources;

/**
 * App-side orchestration around {@link E2ECrypto} for ICQ chats: one shared passphrase per
 * (profile, contact) pair, stored in a private SharedPreferences file, and the Argon2id key
 * derived from it cached in memory for the life of the process.
 * <p>
 * Argon2id is deliberately expensive (64 MiB, seconds on a phone), so derivations are
 * serialized process-wide (never two 64 MiB blocks at once) and every caller waiting for
 * the same conversation joins the one derivation in flight instead of starting its own.
 */
public final class ChatCrypto {
    private static final String TAG = "ChatCrypto";
    private static final String PREFS = "icq_e2e";

    // conversation id -> derived 32-byte key
    private static final Map<String, byte[]> keys = new HashMap<>();
    // conversation ids whose derivation is running right now
    private static final HashSet<String> deriving = new HashSet<>();
    private static final Object lock = new Object();
    // serializes the Argon2id runs themselves
    private static final Object deriveLock = new Object();

    private ChatCrypto() {
    }

    public static String conversationId(String profileId, String uin) {
        return profileId + "/" + uin;
    }

    private static SharedPreferences prefs() {
        return resources.ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // -- passphrase storage -----------------------------------------------------

    public static String getPassphrase(String conversationId) {
        return prefs().getString(conversationId, null);
    }

    /** Encryption is switched on for this conversation, whether or not the key is derived yet. */
    public static boolean isEnabled(String conversationId) {
        String p = getPassphrase(conversationId);
        return p != null && !p.isEmpty();
    }

    /** Stores a new passphrase and forgets any cached key. Call {@link #unlockAsync} afterwards. */
    public static void setPassphrase(String conversationId, String passphrase) {
        synchronized (lock) {
            keys.remove(conversationId);
        }
        prefs().edit().putString(conversationId, passphrase).commit();
    }

    /** Forgets the cached key and the stored passphrase: turns encryption off. */
    public static void disable(String conversationId) {
        synchronized (lock) {
            keys.remove(conversationId);
        }
        prefs().edit().remove(conversationId).commit();
    }

    // -- key cache --------------------------------------------------------------

    public static boolean isUnlocked(String conversationId) {
        synchronized (lock) {
            return keys.containsKey(conversationId);
        }
    }

    public static byte[] getKey(String conversationId) {
        synchronized (lock) {
            return keys.get(conversationId);
        }
    }

    /**
     * Derives (once) and caches the key for the stored passphrase, blocking the calling
     * thread for the duration. Returns the key, or null when no passphrase is set or the
     * derivation failed. Safe to call from any background thread; never call it on the UI thread.
     */
    public static byte[] unlock(String conversationId) {
        byte[] cached = getKey(conversationId);
        if (cached != null) return cached;
        String pass = getPassphrase(conversationId);
        if (pass == null || pass.isEmpty()) return null;
        synchronized (deriveLock) {
            // Someone else may have finished it while we waited for the lock.
            cached = getKey(conversationId);
            if (cached != null) return cached;
            synchronized (lock) {
                deriving.add(conversationId);
            }
            try {
                long t = System.currentTimeMillis();
                byte[] key = E2ECrypto.deriveKey(pass);
                Log.i(TAG, "key derived for " + conversationId + " in " + (System.currentTimeMillis() - t) + " ms");
                // Commit only if the passphrase was not changed out from under this derivation.
                if (pass.equals(getPassphrase(conversationId))) {
                    synchronized (lock) {
                        keys.put(conversationId, key);
                    }
                }
                return key;
            } catch (Throwable e) {
                Log.e(TAG, "key derivation failed for " + conversationId + ": " + e);
                return null;
            } finally {
                synchronized (lock) {
                    deriving.remove(conversationId);
                }
            }
        }
    }

    public static boolean isDeriving(String conversationId) {
        synchronized (lock) {
            return deriving.contains(conversationId);
        }
    }

    /** Runs {@link #unlock} on a background thread; {@code onDone} (may be null) runs on that thread afterwards. */
    public static void unlockAsync(final String conversationId, final Runnable onDone) {
        if (!isEnabled(conversationId) || isUnlocked(conversationId)) {
            if (onDone != null) onDone.run();
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                unlock(conversationId);
                if (onDone != null) onDone.run();
            }
        }, "e2e-derive");
        t.setPriority(Thread.NORM_PRIORITY - 1);
        t.start();
    }

    // -- message paths ----------------------------------------------------------

    /**
     * Encrypts outgoing text when encryption is on for this conversation and the key is
     * cached; passes the text through unchanged when encryption is off. When it is on but
     * the key is not ready this NEVER returns plaintext: it throws so the caller can fail
     * the send instead of leaking the message in the clear.
     */
    public static String encryptOutgoing(String conversationId, String text) throws Exception {
        if (text == null || text.isEmpty() || !isEnabled(conversationId)) return text;
        byte[] key = getKey(conversationId);
        if (key == null) throw new IllegalStateException("encryption key is not ready");
        return E2ECrypto.encrypt(text, key);
    }

    /**
     * Tries to read an incoming body for this conversation: returns the plaintext when it is
     * one of ours, or null when it is ordinary text (or the passphrase does not match). Derives
     * the key first if needed, so call it from the network thread only.
     */
    public static String decryptIncoming(String conversationId, String body) {
        if (body == null || body.isEmpty() || !isEnabled(conversationId)) return null;
        if (!E2ECrypto.looksEncrypted(body)) return null;
        byte[] key = unlock(conversationId);
        if (key == null) return null;
        return E2ECrypto.tryDecrypt(body, key);
    }
}
