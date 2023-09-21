package ru.ivansuper.jasmin;

import java.security.MessageDigest;
import ru.ivansuper.jasmin.icq.SNAC;

public class MD5 {
    public static final byte[] AIM_MD5_STRING = "AOL Instant Messenger (SM)".getBytes();
    private static MessageDigest md5;
    public static byte[] calculateMD5(byte[] var0) {
        return md5.digest(var0);
    }

    public static void init() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception var1) {
            var1.printStackTrace();
        }

    }
}