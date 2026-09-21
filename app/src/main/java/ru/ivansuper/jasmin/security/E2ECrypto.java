package ru.ivansuper.jasmin.security;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * End-to-end message encryption, wire-compatible with SimpleOKM (Android) and Komet: two
 * clients sharing a chat passphrase exchange ciphertext over the ordinary OSCAR text channel,
 * so the server only ever relays Russian-looking gibberish.
 * <pre>
 *   key    = Argon2id(pw, salt = SHA256("komet-enc-v1" || pw)[:16], v=0x13, m=65536 KiB, t=3, p=1, 32 bytes)
 *   blob   = nonce(12) || ChaCha20-Poly1305(plaintext, aad=[0x53,0x02])
 *   wire   = blob Base32-packed, each 5-bit symbol mapped to a Russian letter, spaced into 4-8 letter "words"
 * </pre>
 * There is no header on the wire: whether a message is one of ours is decided by trying to
 * open it, a valid Poly1305 tag (false-positive odds ~2^-128) meaning yes.
 */
public final class E2ECrypto {
    public static final int KEY_LEN = 32;
    private static final int NONCE_LEN = ChaCha20Poly1305.NONCE_LEN;
    private static final int TAG_LEN = ChaCha20Poly1305.TAG_LEN;
    private static final int MIN_BLOB = NONCE_LEN + TAG_LEN;

    // Scheme-version constant bound as AEAD associated data: authenticated but never sent.
    private static final byte[] AAD = {0x53, 0x02};   // 'S', scheme v2

    private static final byte[] SALT_CONTEXT = "komet-enc-v1".getBytes();
    private static final int SALT_LEN = 16;
    private static final int ARGON_MEM_KIB = 65536;
    private static final int ARGON_ITERS = 3;
    private static final int ARGON_PARALLELISM = 1;

    // Russian lowercase, ё removed: exactly 32 symbols, matching Komet's alphabet.
    private static final char[] RU_ALPHABET = "абвгдежзийклмнопрстуфхцчшщъыьэюя".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private E2ECrypto() {
    }

    /** Argon2id key from a chat passphrase. Slow by design (~64 MB, seconds on a phone): derive once per chat and cache. */
    public static byte[] deriveKey(String passphrase) throws Exception {
        if (passphrase == null || passphrase.isEmpty()) throw new IllegalArgumentException("empty passphrase");
        byte[] pw = passphrase.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(SALT_CONTEXT);
        sha.update(pw);
        byte[] digest = sha.digest();
        byte[] salt = new byte[SALT_LEN];
        System.arraycopy(digest, 0, salt, 0, SALT_LEN);
        return Argon2.hash(Argon2.TYPE_ID, pw, salt, null, null, ARGON_MEM_KIB, ARGON_ITERS, ARGON_PARALLELISM, KEY_LEN);
    }

    /** Encrypts plaintext to the Russian-letter wire form: nonce ‖ ciphertext ‖ tag, alphabet-encoded. */
    public static String encrypt(String plaintext, byte[] key) throws Exception {
        if (key == null || key.length != KEY_LEN) throw new IllegalArgumentException("bad key");
        byte[] nonce = new byte[NONCE_LEN];
        RANDOM.nextBytes(nonce);
        byte[] msg = (plaintext == null ? "" : plaintext).getBytes("UTF-8");
        byte[] sealed = ChaCha20Poly1305.seal(key, nonce, AAD, msg);
        byte[] blob = new byte[NONCE_LEN + sealed.length];
        System.arraycopy(nonce, 0, blob, 0, NONCE_LEN);
        System.arraycopy(sealed, 0, blob, NONCE_LEN, sealed.length);
        return alphabetEncode(blob);
    }

    /**
     * Tries to decrypt one message with this key. Returns the plaintext, or null for ordinary
     * text, a message under another passphrase, or anything tampered with.
     */
    public static String tryDecrypt(String text, byte[] key) {
        if (key == null || key.length != KEY_LEN) return null;
        byte[] blob;
        try {
            blob = alphabetDecode(text);
        } catch (Exception e) {
            return null;
        }
        if (blob == null || blob.length < MIN_BLOB) return null;
        byte[] nonce = new byte[NONCE_LEN];
        System.arraycopy(blob, 0, nonce, 0, NONCE_LEN);
        byte[] sealed = new byte[blob.length - NONCE_LEN];
        System.arraycopy(blob, NONCE_LEN, sealed, 0, sealed.length);
        try {
            byte[] plain = ChaCha20Poly1305.open(key, nonce, AAD, sealed);
            return new String(plain, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /** Cheap pre-check: true when the text consists only of alphabet letters and whitespace and is long enough to be a blob. */
    public static boolean looksEncrypted(String text) {
        if (text == null) return false;
        int symbols = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) continue;
            if (indexOf(Character.toLowerCase(c)) < 0) return false;
            symbols++;
        }
        return symbols * 5 / 8 >= MIN_BLOB;
    }

    // -- Russian-letter Base32 (matches komet alphabet.rs) ---------------------

    static String alphabetEncode(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        int run = 0, wordLen = 4;
        int bits = 0, value = 0;
        for (byte b : bytes) {
            value = (value << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                int index = (value >>> bits) & 31;
                if (run == wordLen) {
                    sb.append(' ');
                    run = 0;
                    wordLen = 4 + index % 5;
                }
                sb.append(RU_ALPHABET[index]);
                run++;
            }
            value &= 0xFF;   // keep only the bits still pending; avoids int overflow on long inputs
        }
        if (bits > 0) {
            int index = (value << (5 - bits)) & 31;
            if (run == wordLen) sb.append(' ');
            sb.append(RU_ALPHABET[index]);
        }
        return sb.toString();
    }

    static byte[] alphabetDecode(String text) {
        if (text == null || text.isEmpty()) return null;
        int bits = 0, value = 0, symbols = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(text.length());
        for (int i = 0; i < text.length(); i++) {
            char raw = text.charAt(i);
            if (Character.isWhitespace(raw)) continue;
            int index = indexOf(Character.toLowerCase(raw));
            if (index < 0) return null;   // not our alphabet
            symbols++;
            value = (value << 5) | index;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                out.write((value >>> bits) & 0xFF);
            }
            value &= 0xFF;
        }
        if (symbols == 0) return null;
        return out.toByteArray();
    }

    private static int indexOf(char c) {
        for (int i = 0; i < RU_ALPHABET.length; i++) if (RU_ALPHABET[i] == c) return i;
        return -1;
    }
}
