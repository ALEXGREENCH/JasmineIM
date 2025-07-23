package ru.ivansuper.jasmin;

import java.security.MessageDigest;

/** @noinspection ALL*/
public class MD5 {
    public static final byte[] AIM_MD5_STRING = "AOL Instant Messenger (SM)".getBytes();
    private static MessageDigest md5;
    public static byte[] calculateMD5(byte[] bytesToHash) {
        return md5.digest(bytesToHash);
    }

    public static void init() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}