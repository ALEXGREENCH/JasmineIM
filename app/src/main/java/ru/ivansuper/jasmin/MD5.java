package ru.ivansuper.jasmin;

import java.security.MessageDigest;

/** @noinspection ALL*/
public class MD5 {
    public static final byte[] AIM_MD5_STRING = "AOL Instant Messenger (SM)".getBytes();

    public static byte[] calculateMD5(byte[] bytesToHash) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(bytesToHash);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }

    public static void init() {
        // No initialization required anymore
    }
}