package ru.ivansuper.jasmin;

import java.util.Arrays;

public class Base64Coder {

    private static final char[] map1 = new char[64];
    private static final byte[] map2 = new byte[128];
    /** @noinspection SystemGetProperty*/
    private static final String systemLineSeparator = System.getProperty("line.separator");

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) map1[i++] = c;
        for (char c = 'a'; c <= 'z'; c++) map1[i++] = c;
        for (char c = '0'; c <= '9'; c++) map1[i++] = c;
        map1[i++] = '+';
        map1[i] = '/';

        Arrays.fill(map2, (byte) -1);
        for (int j = 0; j < map1.length; j++) map2[map1[j]] = (byte) j;
    }

    // ----------------- ENCODE -----------------

    public static String encodeString(String s) {
        return new String(encode(s.getBytes()));
    }

    public static String encodeLines(byte[] in) {
        return encodeLines(in, 0, in.length, 76, systemLineSeparator);
    }

    public static String encodeLines(byte[] in, int iOff, int iLen, int lineLen, String lineSeparator) {
        int blockLen = (lineLen * 3) / 4;
        if (blockLen <= 0) throw new IllegalArgumentException();

        int lines = (iLen + blockLen - 1) / blockLen;
        int bufLen = ((iLen + 2) / 3) * 4 + lines * lineSeparator.length();

        StringBuilder buf = new StringBuilder(bufLen);
        int ip = 0;
        while (ip < iLen) {
            int l = Math.min(iLen - ip, blockLen);
            buf.append(encode(in, iOff + ip, l));
            buf.append(lineSeparator);
            ip += l;
        }
        return buf.toString();
    }

    public static char[] encode(byte[] in) {
        return encode(in, 0, in.length);
    }

    /** @noinspection unused*/
    public static char[] encode(byte[] in, int iLen) {
        return encode(in, 0, iLen);
    }

    public static char[] encode(byte[] in, int iOff, int iLen) {
        int oDataLen = (iLen * 4 + 2) / 3;
        int oLen = ((iLen + 2) / 3) * 4;
        char[] out = new char[oLen];

        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;

        while (ip < iEnd) {
            int i0 = in[ip++] & 0xFF;
            int i1 = ip < iEnd ? in[ip++] & 0xFF : 0;
            int i2 = ip < iEnd ? in[ip++] & 0xFF : 0;

            int o0 = i0 >>> 2;
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 15) << 2) | (i2 >>> 6);
            int o3 = i2 & 63;

            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op++] = op < oDataLen ? map1[o2] : '=';
            out[op++] = op < oDataLen ? map1[o3] : '=';
        }

        return out;
    }

    // ----------------- DECODE -----------------

    public static String decodeString(String s) {
        return new String(decode(s));
    }

    /** @noinspection unused*/
    public static byte[] decodeLines(String s) {
        char[] buf = new char[s.length()];
        int p = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
                buf[p++] = c;
            }
        }
        return decode(buf, 0, p);
    }

    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }

    public static byte[] decode(char[] in) {
        return decode(in, 0, in.length);
    }

    public static byte[] decode(char[] in, int iOff, int iLen) {
        if (iLen % 4 != 0)
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");

        while (iLen > 0 && in[iOff + iLen - 1] == '=') iLen--;

        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];

        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;

        while (ip < iEnd) {
            char c0 = in[ip++];
            char c1 = in[ip++];
            char c2 = ip < iEnd ? in[ip++] : 'A';
            char c3 = ip < iEnd ? in[ip++] : 'A';

            if (c0 > 127 || c1 > 127 || c2 > 127 || c3 > 127)
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");

            int b0 = map2[c0];
            int b1 = map2[c1];
            int b2 = map2[c2];
            int b3 = map2[c3];

            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");

            out[op++] = (byte) ((b0 << 2) | (b1 >>> 4));
            if (op < oLen) out[op++] = (byte) ((b1 << 4) | (b2 >>> 2));
            if (op < oLen) out[op++] = (byte) ((b2 << 6) | b3);
        }

        return out;
    }

    // Private constructor to prevent instantiation
    private Base64Coder() {}
}
