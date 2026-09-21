package ru.ivansuper.jasmin.security;

import java.math.BigInteger;
import java.security.GeneralSecurityException;

/**
 * ChaCha20-Poly1305 AEAD (RFC 8439, IETF variant: 256-bit key, 96-bit nonce, 128-bit tag)
 * in pure Java. Poly1305 is evaluated with BigInteger: messages here are a few KB at most,
 * so simplicity wins over a limb-based implementation.
 */
public final class ChaCha20Poly1305 {
    public static final int KEY_LEN = 32;
    public static final int NONCE_LEN = 12;
    public static final int TAG_LEN = 16;

    private static final BigInteger P = BigInteger.ONE.shiftLeft(130).subtract(BigInteger.valueOf(5));

    private ChaCha20Poly1305() {
    }

    /** Returns ciphertext ‖ tag. */
    public static byte[] seal(byte[] key, byte[] nonce, byte[] aad, byte[] plaintext) {
        checkParams(key, nonce);
        byte[] out = new byte[plaintext.length + TAG_LEN];
        xorStream(key, nonce, 1, plaintext, out, 0);
        byte[] tag = tag(polyKey(key, nonce), aad, out, plaintext.length);
        System.arraycopy(tag, 0, out, plaintext.length, TAG_LEN);
        return out;
    }

    /** Verifies the tag and returns the plaintext; throws when the tag does not match. */
    public static byte[] open(byte[] key, byte[] nonce, byte[] aad, byte[] sealed) throws GeneralSecurityException {
        checkParams(key, nonce);
        if (sealed == null || sealed.length < TAG_LEN) throw new GeneralSecurityException("too short");
        int ctLen = sealed.length - TAG_LEN;
        byte[] expected = tag(polyKey(key, nonce), aad, sealed, ctLen);
        int diff = 0;
        for (int i = 0; i < TAG_LEN; i++) diff |= expected[i] ^ sealed[ctLen + i];
        if (diff != 0) throw new GeneralSecurityException("tag mismatch");
        byte[] out = new byte[ctLen];
        xorStream(key, nonce, 1, sealed, out, 0);
        return out;
    }

    private static void checkParams(byte[] key, byte[] nonce) {
        if (key == null || key.length != KEY_LEN) throw new IllegalArgumentException("key");
        if (nonce == null || nonce.length != NONCE_LEN) throw new IllegalArgumentException("nonce");
    }

    // -- ChaCha20 --------------------------------------------------------------

    private static int[] initialState(byte[] key, byte[] nonce, int counter) {
        int[] s = new int[16];
        s[0] = 0x61707865;
        s[1] = 0x3320646e;
        s[2] = 0x79622d32;
        s[3] = 0x6b206574;
        for (int i = 0; i < 8; i++) s[4 + i] = readIntLE(key, i * 4);
        s[12] = counter;
        s[13] = readIntLE(nonce, 0);
        s[14] = readIntLE(nonce, 4);
        s[15] = readIntLE(nonce, 8);
        return s;
    }

    private static void block(int[] in, byte[] out) {
        int[] x = new int[16];
        System.arraycopy(in, 0, x, 0, 16);
        for (int i = 0; i < 10; i++) {
            quarterRound(x, 0, 4, 8, 12);
            quarterRound(x, 1, 5, 9, 13);
            quarterRound(x, 2, 6, 10, 14);
            quarterRound(x, 3, 7, 11, 15);
            quarterRound(x, 0, 5, 10, 15);
            quarterRound(x, 1, 6, 11, 12);
            quarterRound(x, 2, 7, 8, 13);
            quarterRound(x, 3, 4, 9, 14);
        }
        for (int i = 0; i < 16; i++) writeIntLE(out, i * 4, x[i] + in[i]);
    }

    private static void quarterRound(int[] x, int a, int b, int c, int d) {
        x[a] += x[b]; x[d] = Integer.rotateLeft(x[d] ^ x[a], 16);
        x[c] += x[d]; x[b] = Integer.rotateLeft(x[b] ^ x[c], 12);
        x[a] += x[b]; x[d] = Integer.rotateLeft(x[d] ^ x[a], 8);
        x[c] += x[d]; x[b] = Integer.rotateLeft(x[b] ^ x[c], 7);
    }

    private static void xorStream(byte[] key, byte[] nonce, int counter, byte[] in, byte[] out, int outOff) {
        int[] state = initialState(key, nonce, counter);
        byte[] ks = new byte[64];
        int len = Math.min(in.length, out.length - outOff);
        for (int pos = 0; pos < len; pos += 64) {
            block(state, ks);
            state[12]++;
            int n = Math.min(64, len - pos);
            for (int i = 0; i < n; i++) out[outOff + pos + i] = (byte) (in[pos + i] ^ ks[i]);
        }
    }

    private static byte[] polyKey(byte[] key, byte[] nonce) {
        byte[] block = new byte[64];
        block(initialState(key, nonce, 0), block);
        byte[] pk = new byte[32];
        System.arraycopy(block, 0, pk, 0, 32);
        return pk;
    }

    // -- Poly1305 --------------------------------------------------------------

    private static byte[] tag(byte[] otk, byte[] aad, byte[] ct, int ctLen) {
        byte[] rBytes = new byte[16];
        System.arraycopy(otk, 0, rBytes, 0, 16);
        rBytes[3] &= 15; rBytes[7] &= 15; rBytes[11] &= 15; rBytes[15] &= 15;
        rBytes[4] &= 252; rBytes[8] &= 252; rBytes[12] &= 252;
        BigInteger r = leToBig(rBytes, 0, 16);
        BigInteger s = leToBig(otk, 16, 16);

        int aadLen = aad == null ? 0 : aad.length;
        int aadPad = (16 - aadLen % 16) % 16;
        int ctPad = (16 - ctLen % 16) % 16;
        byte[] mac = new byte[aadLen + aadPad + ctLen + ctPad + 16];
        int p = 0;
        if (aadLen > 0) System.arraycopy(aad, 0, mac, 0, aadLen);
        p += aadLen + aadPad;
        System.arraycopy(ct, 0, mac, p, ctLen);
        p += ctLen + ctPad;
        writeLongLE(mac, p, aadLen);
        writeLongLE(mac, p + 8, ctLen);

        BigInteger acc = BigInteger.ZERO;
        for (int off = 0; off < mac.length; off += 16) {
            int n = Math.min(16, mac.length - off);
            BigInteger block = leToBig(mac, off, n).add(BigInteger.ONE.shiftLeft(8 * n));
            acc = acc.add(block).multiply(r).mod(P);
        }
        acc = acc.add(s);
        byte[] out = new byte[16];
        byte[] be = acc.toByteArray();   // big-endian, possibly with sign/overflow bytes
        for (int i = 0; i < 16 && i < be.length; i++) out[i] = be[be.length - 1 - i];
        return out;
    }

    private static BigInteger leToBig(byte[] b, int off, int len) {
        byte[] be = new byte[len + 1];   // leading zero keeps it positive
        for (int i = 0; i < len; i++) be[len - i] = b[off + i];
        return new BigInteger(be);
    }

    private static int readIntLE(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }

    private static void writeIntLE(byte[] b, int off, int x) {
        b[off] = (byte) x;
        b[off + 1] = (byte) (x >>> 8);
        b[off + 2] = (byte) (x >>> 16);
        b[off + 3] = (byte) (x >>> 24);
    }

    private static void writeLongLE(byte[] b, int off, long x) {
        for (int i = 0; i < 8; i++) b[off + i] = (byte) (x >>> (8 * i));
    }
}
