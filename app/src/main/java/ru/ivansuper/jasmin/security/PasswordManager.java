package ru.ivansuper.jasmin.security;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * The PasswordManager class provides methods for saving and verifying passwords.
 * Passwords are stored in a file named "security0.bin" in the application's data directory.
 * The class uses a custom encoding/decoding scheme for password storage.
 * <p>
 * Note: The encoding scheme used in this class is not cryptographically secure and should not be used for sensitive data.
 * It is provided as an example only.
 */
public class PasswordManager {
    public static boolean TYPED = false;

    public static void savePassword(String password) throws Exception {
        File pass = new File(utilities.normalizePath(resources.dataPath) + "security0.bin");
        if (!pass.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                pass.createNewFile();
            } catch (Exception ignored) {
            }
        }
        //noinspection IOStreamConstructor
        OutputStream os = new FileOutputStream(pass);
        byte[] b = a(password);
        os.write(b.length);
        os.write(b);
        try {
            os.close();
        } catch (Exception ignored) {
        }
    }

    public static boolean verifyPassword(String password) {
        InputStream is = null;
        String pwd = "";
        File pass = new File(utilities.normalizePath(resources.dataPath) + "security0.bin");
        if (!pass.exists()) {
            return false;
        }
        InputStream is2;
        try {
            //noinspection IOStreamConstructor
            is = new FileInputStream(pass);
            try {
                int len = is.read();
                byte[] b = new byte[len];
                //noinspection ResultOfMethodCallIgnored
                is.read(b, 0, len);
                pwd = b(b);
                Log.e("Verifier", pwd);
            } catch (Exception e) {
                //noinspection UnusedAssignment
                is2 = is;
            }
        } catch (Exception ignored) {
        }
        if (pwd.equals(password)) {
            return true;
        }
        is2 = is;
        try {
            assert is2 != null;
            is2.close();
            return false;
        } catch (Exception e3) {
            return false;
        }
    }

    public static byte[] a(String source) throws Exception {
        //noinspection InjectedReferences
        byte[] stepA = source.getBytes("windows1251");
        String stepB = utilities.convertToHex(stepA);
        //noinspection InjectedReferences
        byte[] stepC = stepB.getBytes("windows1251");
        byte j = 1;
        for (int i = 0; i < stepC.length; i++) {
            stepC[i] = (byte) (stepC[i] - j);
            j = (byte) (j + 1);
        }
        return stepC;
    }

    public static String b(byte[] source) throws Exception {
        byte j = 1;
        for (int i = 0; i < source.length; i++) {
            source[i] = (byte) (source[i] + j);
            j = (byte) (j + 1);
        }
        //noinspection InjectedReferences
        String stepA = new String(source, "windows1251");
        byte[] stepB = utilities.hexStringToBytesArray(stepA);
        //noinspection InjectedReferences
        return new String(stepB, "windows1251");
    }
}