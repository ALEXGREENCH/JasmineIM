package ru.ivansuper.jasmin.icq;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import ru.ivansuper.jasmin.jabber.jzlib.JZlib;

/* loaded from: classes.dex */
public class StringConvertor {
    /** @noinspection unused*/
    public static final byte ENCODING_AUTO = 4;
    /** @noinspection unused*/
    public static final byte ENCODING_STD = 3;
    /** @noinspection unused*/
    public static final byte ENCODING_UCS2 = 2;
    /** @noinspection unused*/
    public static final byte ENCODING_UTF8 = 1;
    private static final char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    static boolean systemWin1251 = true;

    /** @noinspection unused*/
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 240) >>> 4]);
            sb.append(hexChar[b[i] & 15]);
            sb.append(" ");
            if (i != 0 && i % 15 == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /** @noinspection unused*/
    public static String byteArrayToHexString(byte[] buf) {
        StringBuilder hexString = new StringBuilder(buf.length);
        for (byte b : buf) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() < 2) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /** @noinspection unused*/
    public static boolean isDataCP1251(byte[] array, int start, int lenght) {
        int end = start + lenght;
        for (int i = start; i < end; i++) {
            if ((array[i] & 192) == 192) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDataUCS2(byte[] array, int start, int lenght) {
        if ((lenght & 1) != 0) {
            return false;
        }
        int end = start + lenght;
        boolean result = true;
        for (int i = start; i < end; i += 2) {
            byte b = array[i];
            if (b > 0 && b < 9) {
                return true;
            }
            if (b == 0 && array[i + 1] != 0) {
                return true;
            }
            if (b > 32 || b < 0) {
                result = false;
            }
        }
        return result;
    }

    public static boolean isDataUTF8(byte[] array, int start, int lenght) {
        if (lenght == 0) {
            return false;
        }
        int len = lenght;
        int i = start;
        while (len > 0) {
            int i2 = i + 1;
            byte bt = array[i];
            len--;
            int seqLen = 0;
            if ((bt & 224) == 192) {
                seqLen = 1;
            } else if ((bt & 240) == 224) {
                seqLen = 2;
            } else if ((bt & 248) == 240) {
                seqLen = 3;
            } else //noinspection ConstantValue
                if ((bt & JZlib.Z_MEM_ERROR) == 248) {
                seqLen = 4;
            } else //noinspection ConstantValue
                    if ((bt & JZlib.Z_STREAM_ERROR) == 252) {
                seqLen = 5;
            }
            if (seqLen == 0) {
                if ((bt & 128) == 128) {
                    return false;
                }
                i = i2;
            } else {
                int j = 0;
                i = i2;
                while (j < seqLen) {
                    if (len == 0) {
                        return false;
                    }
                    int i3 = i + 1;
                    if ((array[i] & 192) != 128) {
                        return false;
                    }
                    len--;
                    j++;
                    i = i3;
                }
            }
        }
        return true;
    }

    /** @noinspection unused*/
    public static byte[] stringToByteArray1251(String s) {
        if (systemWin1251) {
            try {
                return s.getBytes("Windows-1251");
            } catch (Exception e) {
                systemWin1251 = false;
            }
        }
        byte[] buf = new byte[s.length()];
        int size = s.length();
        for (int i = 0; i < size; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case 'Ё':
                    buf[i] = -88;
                    break;
                case 'Є':
                    buf[i] = -86;
                    break;
                case 'І':
                    buf[i] = -78;
                    break;
                case 'Ї':
                    buf[i] = -81;
                    break;
                case 'ё':
                    buf[i] = -72;
                    break;
                case 'є':
                    buf[i] = -70;
                    break;
                case 'і':
                    buf[i] = -77;
                    break;
                case 'ї':
                    buf[i] = -65;
                    break;
                case 'Ґ':
                    buf[i] = -91;
                    break;
                case 'ґ':
                    buf[i] = -76;
                    break;
                default:
                    if (ch >= 'А' && ch <= 'я') {
                        buf[i] = (byte) ((ch - 'А') + 192);
                    } else {
                        buf[i] = (byte) (ch & 'ÿ');
                    }
                    break;
            }
        }
        return buf;
    }

    public static String byteArray1251ToString(byte[] buf, int pos, int len) {
        if (systemWin1251) {
            try {
                return new String(buf, pos, len, "Windows-1251");
            } catch (Exception e) {
                systemWin1251 = false;
            }
        }
        int end = pos + len;
        StringBuilder stringbuffer = new StringBuilder(len);
        for (int i = pos; i < end; i++) {
            int ch = buf[i] & 255;
            switch (ch) {
                case 165:
                    stringbuffer.append('Ґ');
                    break;
                case 168:
                    stringbuffer.append('Ё');
                    break;
                case 170:
                    stringbuffer.append('Є');
                    break;
                case 175:
                    stringbuffer.append('Ї');
                    break;
                case 178:
                    stringbuffer.append('І');
                    break;
                case 179:
                    stringbuffer.append('і');
                    break;
                case 180:
                    stringbuffer.append('ґ');
                    break;
                case 184:
                    stringbuffer.append('ё');
                    break;
                case 186:
                    stringbuffer.append('є');
                    break;
                case 191:
                    stringbuffer.append('ї');
                    break;
                default:
                    if (ch >= 192) {
                        stringbuffer.append((char) ((ch + 1040) - 192));
                    } else {
                        stringbuffer.append((char) ch);
                    }
                    break;
            }
        }
        return stringbuffer.toString();
    }

    public static String removeCr(String val) {
        if (val.indexOf(13) >= 0) {
            StringBuilder result = new StringBuilder();
            int size = val.length();
            for (int i = 0; i < size; i++) {
                char chr = val.charAt(i);
                if (chr != 0 && chr != '\r') {
                    result.append(chr);
                }
            }
            return result.toString();
        }
        return val;
    }

    /** @noinspection unused*/
    public static String restoreCrLf(String val) {
        StringBuilder result = new StringBuilder();
        int size = val.length();
        for (int i = 0; i < size; i++) {
            char chr = val.charAt(i);
            if (chr != '\r') {
                if (chr == '\n') {
                    result.append("\r\n");
                } else {
                    result.append(chr);
                }
            }
        }
        return result.toString();
    }

    /** @noinspection unused*/
    public static boolean stringEquals(String s1, String s2) {
        if (s1.length() != s2.length()) {
            return false;
        }
        if (s1.equals(s2)) {
            return true;
        }
        int size = s1.length();
        for (int i = 0; i < size; i++) {
            if (toLowerCase(s1.charAt(i)) != toLowerCase(s2.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** @noinspection unused*/
    public static int stringCompare(String s1, String s2) {
        if (s1 == null) {
            return (s2 == null) ? 0 : -1;
        }
        if (s2 == null) {
            return 1;
        }
        if (s1.equals(s2)) {
            return 0;
        }
        int size = Math.min(s1.length(), s2.length());
        for (int i = 0; i < size; i++) {
            int result = toLowerCase(s1.charAt(i)) - toLowerCase(s2.charAt(i));
            if (result != 0) {
                return result;
            }
        }
        return s1.length() - s2.length();
    }

    public static String toLowerCase(String s) {
        char[] chars = s.toCharArray();
        for (int i = s.length() - 1; i >= 0; i--) {
            chars[i] = toLowerCase(chars[i]);
        }
        String res = new String(chars);
        return res.equals(s) ? s : res;
    }

    public static String toUpperCase(String s) {
        char[] chars = s.toCharArray();
        for (int i = s.length() - 1; i >= 0; i--) {
            chars[i] = toUpperCase(chars[i]);
        }
        String res = new String(chars);
        return res.equals(s) ? s : res;
    }

    private static char toLowerCase(char c) {
        char c2 = Character.toLowerCase(c);
        if ((c2 >= 'A' && c2 <= 'Z') || ((c2 >= 'À' && c2 <= 'Ö') || ((c2 >= 'Ø' && c2 <= 'Þ') || (c2 >= 'Ѐ' && c2 <= 'Я')))) {
            //noinspection ConstantValue
            if (c2 <= 'Z' || (c2 >= 'А' && c2 <= 'Я')) {
                return (char) (c2 + ' ');
            }
            //noinspection ConstantValue
            if (c2 < 'А') {
                return (char) (c2 + 'P');
            }
            return (char) (c2 + ' ');
        }
        return c2;
    }

    private static char toUpperCase(char c) {
        char c2 = Character.toUpperCase(c);
        if ((c2 >= 'a' && c2 <= 'z') || ((c2 >= 'ß' && c2 <= 'ö') || ((c2 >= 'ø' && c2 <= 'ÿ') || (c2 >= 'а' && c2 <= 'џ')))) {
            if (c2 <= 'z' || (c2 >= 'а' && c2 <= 'я')) {
                return (char) (c2 - ' ');
            }
            if (c2 > 'Я') {
                return (char) (c2 - 'P');
            }
            return (char) (c2 - ' ');
        }
        return c2;
    }

    private static String convertChar(String str, String[] src, String[] dest) {
        for (int i = src.length - 1; i >= 0; i--) {
            if (src[i].equals(str)) {
                return dest[i];
            }
            if (src[i].equals(toLowerCase(str))) {
                return toUpperCase(dest[i]);
            }
        }
        return null;
    }

    /** @noinspection unused*/
    private static String convertText(String str, String[] src, String[] dest) {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            String ch;
            int endPos = Math.min(src[0].length() + i, str.length());
            while (true) {
                if (endPos > i) {
                    ch = str.substring(i, endPos);
                    String trans = convertChar(ch, src, dest);
                    if (trans != null) {
                        buf.append(trans);
                        break;
                    }
                    if (ch.length() == 1) {
                        buf.append(ch);
                        break;
                    }
                    endPos--;
                }
            }
            i += ch.length();
        }
        return buf.toString();
    }

    public static String byteArrayToString(byte[] buf, int off, int len) {
        if (buf.length < off + len) {
            return "";
        }
        while (len > 0 && buf[(off + len) - 1] == 0) {
            len--;
        }
        if (len == 0) {
            return "";
        }
        if (isDataUCS2(buf, off, len)) {
            return ucs2beByteArrayToString(buf, off, len);
        }
        if (isDataUTF8(buf, off, len)) {
            return utf8beByteArrayToString(buf, off, len);
        }
        return byteArray1251ToString(buf, off, len);
    }

    public static String ucs2beByteArrayToString(byte[] buf, int off, int len) {
        if (off + len > buf.length || len % 2 != 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int end = off + len;
        for (int i = off; i < end; i += 2) {
            sb.append((char) getWordBE(buf, i));
        }
        return removeCr(sb.toString());
    }

    public static int getWordBE(byte[] buf, int off) {
        int val = (buf[off] << 8) & 65280;
        return (buf[off + 1] & 255) | val;
    }

    /** @noinspection unused*/
    public static String byteArrayToWinString(byte[] buf, int off, int len) {
        if (buf.length < off + len) {
            return "";
        }
        if (len > 0 && buf[(off + len) - 1] == 0) {
            len--;
        }
        if (len == 0) {
            return "";
        }
        return new String(buf, off, len);
    }

    public static String utf8beByteArrayToString(byte[] buf, int off, int len) {
        if (len > 0) {
            try {
                if (buf[(off + len) - 1] == 0) {
                    len--;
                }
            } catch (Exception e) {
                return "";
            }
        }
        if (len == 0) {
            return "";
        }
        byte[] buf2 = new byte[len + 2];
        putWordBE(buf2, 0, len);
        System.arraycopy(buf, off, buf2, 2, len);
        ByteArrayInputStream bais = new ByteArrayInputStream(buf2);
        DataInputStream dis = new DataInputStream(bais);
        try {
            return removeCr(dis.readUTF());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void putWordBE(byte[] buf, int off, int val) {
        buf[off] = (byte) ((val >> 8) & 255);
        buf[off + 1] = (byte) (val & 255);
    }
}