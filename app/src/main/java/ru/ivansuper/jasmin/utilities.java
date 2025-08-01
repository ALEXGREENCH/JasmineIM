package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.os.Build;
import android.content.Context;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.jzlib.JZlib;

/**
 * This class provides a collection of utility methods for various purposes,
 * including string manipulation, hashing, date/time conversions, and Android-specific helpers.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Password "roasting" (XOR encryption).
 *     <li>Hexadecimal string conversion.
 *     <li>MD5 hash calculation (including legacy OSCAR versions).
 *     <li>UTF-8 data validation and preparation.
 *     <li>String splitting and replacement.
 *     <li>Date and time formatting and conversion (GMT to local).
 *     <li>Validation for UINs, MRIM addresses, and email addresses.
 *     <li>Stack trace retrieval.
 *     <li>Android ListView height adjustment.
 *     <li>File path normalization and filename validation.
 *     <li>SHA-1 hash generation.
 *     <li>Checking for hardware menu key presence.
 *     <li>Reflection-based method invocation.
 * </ul>
 *
 * <p>The class also contains several constants for character sets,
 * predefined randomized strings (purpose unclear from context), and reserved characters for filenames.
 *
 * <p><b>Note:</b> Some methods are marked with {@code @noinspection unused}. This might indicate
 * that they are intended for future use, are part of a library where not all methods are
 * used by every consumer, or are accessed via reflection.
 */
public class utilities {
    private static final String ReservedChars = "|\\?*<\":>+[]/'";
    private static MessageDigest digest;
    /**
     * Stores the amount of memory used by the application, in bytes.
     * This field is likely updated dynamically to reflect current memory consumption.
     *
     * @noinspection unused - This field might be accessed via reflection or used by parts of the codebase not included in this snippet.
     */
    public static long used_memory;
    /**
     * A pseudo-random number generator initialized with the current system time.
     * This can be used for various tasks requiring random number generation,
     * such as generating unique IDs, randomizing data, or for cryptographic purposes
     * (though for security-sensitive applications, {@link java.security.SecureRandom} is preferred).
     */
    public static final Random RANDOM = new Random(System.currentTimeMillis());

    /**
     * A string containing a comprehensive set of characters, including:
     * <ul>
     *     <li>Digits (0-9)</li>
     *     <li>Uppercase and lowercase Latin letters (A-Z, a-z)</li>
     *     <li>Uppercase and lowercase Cyrillic letters (А-Я, а-я, including Ё/ё)</li>
     * </ul>
     * This string might be used for generating random strings, validating input,
     * or other character-related operations.
     */
    public static String chars = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZzАаБбВвГгДдЕеЁёЖжЗзИиЙйКкЛлМмНнОоПпРрСсТтУуФфХхЦцЧчШшЩщЪъЫыЬьЭэЮюЯя";
    /**
     * A string containing all digits (0-9) and uppercase/lowercase Latin characters (A-Z, a-z).
     * This can be used for generating random strings, validating input, or other string manipulation tasks
     * where a specific set of Latin characters is required.
     *
     * @noinspection unused - This field might be used in parts of the application not shown or for future features.
     */
    public static String latin_chars = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
    /**
     * A predefined, seemingly random hexadecimal string.
     * Its specific purpose within the application is not immediately clear from the context.
     * It might be used as a constant for cryptographic operations, unique identifiers,
     * or other purposes where a fixed, unpredictable string is required.
     *
     * @noinspection unused - This field might be accessed via reflection or used by parts of the codebase not included in this snippet.
     */
    public static String randomized = "308201e53082014ea00302010202044ec17a4d300d06092a864886f70d01010505003037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f6964204465627567301e170d3131313131343230333030355a170d3431313130363230333030355a3037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f696420446562756730819f300d06092a864886f70d010101050003818d00308189028181009dfd6f277a0fbd2d93bf0527405d02d31091d7bd8f8ec1d3ec62919bab815bc728d91a62863141ddc8f38cc6dee5f1d6a8bbbc6c920030843aee75f625d65fb92eff7c5ce7643c453736ed485aa404094f171c32b9397bf0c75f489a17e78e22e0fe608018c4eb8b9a03966e9285b1b2e73e61d0903faa4a87f3b847f71de40d0203010001300d06092a864886f70d01010505000381810093628f179da1c93f779a9773acf24f291fedbb1edab059cba66c678840c31a9bac02a7f64351cd41c058224cfcaba669c1bd507cbaf7a082691d9180048345eb764ae60d9de2d6f16573b95bf46dfc21a1e2cb6f160e997b9d7e048a88e4b1f18a984e72eb183fa50da20287f1386f9a7166e70525eb17744cbcb0961831b3c5";
    /**
     * A predefined, apparently randomized hexadecimal string.
     * The specific purpose or origin of this string is not clear from the context.
     * It might be used for cryptographic purposes (e.g., as a salt or part of a key),
     * as a unique identifier, or for testing/mocking data.
     *
     * <p>Similar to {@link #randomized}, {@link #randomized3}, and {@link #randomized4}.
     *
     * @noinspection unused - This field might be accessed via reflection or used by parts of the codebase not included in this snippet.
     */
    public static String randomized2 = "308201eb30820154a00302010202044e887026300d06092a864886f70d01010505003039310f300d06035504061306313535393030310d300b060355040a1304486f6d65311730150603550403130e4976616e204b75647279616b6f763020170d3131313030323134303733345a180f32303631303931393134303733345a3039310f300d06035504061306313535393030310d300b060355040a1304486f6d65311730150603550403130e4976616e204b75647279616b6f7630819f300d06092a864886f70d010101050003818d00308189028181009721a5313ace678280700530dfafa15f563fd37cb6a10107b0be2b7de8c27eac2c5c1b950f21fb52ac9cd7ac3ab10a99eb60dbf9dc23b5207eef1614b01d8ba1c08e45f94335c4cd7c10daa0b4ccd8fe48dece1c73f3a9f66292ac9cca06eb1f6e199bc31c6f47cc0d0fce1faad2a33013f9e0c51a0308271591aad1dc03a8350203010001300d06092a864886f70d0101050500038181000b72002e821026583a19faca0ddf2ecb559a44549dcb7fc310b492921704838566e5e2beb68e6ffd2e931113b9e6cf2ad80c942022bb0a32509a9bc3e89fe082c7a554c66d1592ba0d0b3a92c00313141469aabd115a54e8b7eb684ec4982e48a34b45a91d6a9f4173e6d17c33cc36ae1bdfcd73a0de6fa22d981ba2e52c999c";
    /**
     * A predefined, seemingly random hexadecimal string.
     * Its specific purpose within the application is not immediately clear from the context.
     * It might be used for cryptographic purposes, unique identifiers, or other internal operations.
     *
     * @noinspection unused - This field might be accessed via reflection or used by parts of the codebase not included in this snippet.
     */
    public static String randomized3 = "f6d653117301506035c17a4d300d06092a864886f70d01010505003037310b3009060355040613050403130e4976616ec17a4d300d06092a864886f70d01010505003037310b30c17a4d300d06092a864886f70d01010505003037310b30090603550406130090603550406130204b75647279616b6f763020170d3131313030323134303733345a180f32303631303931393134303733345a3039310f300d06035504061306313535393030310d300b060355040a1304486f6d65311730150603550403130e4976616e204b75647279616b6f7630819f300d06092a864886f70d010101050003818d00308189028181009721a5313ace678280700530dfafa15f563fd37cb6a10107b0be2b7de8c27eac2c5c1b950f21fb52ac9cd7ac3ab10a99eb60dbf9dc23b5207eef1614b01d8ba1c08e45f94335c4cd7c10daa0b4ccd8fe48dece1c73f3a9f66292ac9cca06eb1f6e199bc31c6f47cc0d0fce1faad2a33013f9e0c51a0308271591aad1dc03a8350203010001300d06092a864886f70d0101050500038181000b72002e821026583a19faca0ddf2ecb559a44549dcb7fc310b492921704838566e5e2beb68e6ffd2e931113b9e6cf2ad80c942022bb0a32509a9bc3e89fe082c7a554c66dc17a4d300d06092a864886f70d01010505003037310b3009060355040613000313141469aabd115981ba2e52c999c";
    /**
     * Another predefined, likely randomized, hexadecimal string.
     * The purpose of this string is not clear from the context. It might be used for
     * cryptographic purposes, as a unique identifier, or for some internal data representation.
     * The {@code @noinspection unused} annotation suggests it might be accessed via reflection
     * or is part of a larger system where its usage is not immediately apparent.
     *
     * @noinspection unused
     */
    public static String randomized4 = "505003037310b3009060355040613050403130e4976616ec17a4d300d06092af6d653117301506035c17a4d300d06092a864886f70d010100b30c17a4d300d06092a864886f70d01010505003037310b30090603550406130090603550406130204b75647279616b6f763020170d3131313030323134303733345a180f323036313039313931343037331304486f6d65311730150603550403130e4976616e345a3039310f300d0603550406345a3039310f300d06035504061306313535393030310d300b060355040a30310d300b060355040a79616b6f7630819f300d06092a864886f70d010101050003818d00308189028181009721a5313ace678280700530dfafa15f563fd37cb6a10107b0be2b7de8c27eac2c5c1b95e48dece1c73f3a9f66292ac9cca06eb1f6e199bc3ce1faad2a33013f9e0c51a3013f9e0c51a0308271591aad1dc03a8350203010001300d06092a864886f70d0101050500038181000b72002e821026583a19faca0ddf2ecb559a44549dcb7fc310b492921704838566e5e2beb68e6ffd2e931113b9e6cf2ad80c942022bb0a32509a9bc3e89fe082c7a554c66dc17a4d300d060ce1faad2a33013f9e0c51a981ba2e52cce1faad2fe082c7a554c66dc17a4d300d060ce1faae6d17c33cc36ae1003037310b30090603550406139c";
    /**
     * An array of bytes used for "roasting" (XORing) passwords.
     * The password bytes are XORed with these characters cyclically.
     * This is a simple obfuscation technique, not a strong encryption method.
     *
     * @noinspection FieldMayBeFinal - This field might be modified by other parts of the codebase not shown,
     * or its non-final status might be intended for future flexibility, although it's initialized as a constant.
     */
    private static final byte[] ROAST_CHARS = new byte[]{
            (byte)0xF3, 0x26, (byte)0x81, (byte)0xC4, 0x39, (byte)0x86, (byte)0xDB, (byte)0x92,
            0x71, (byte)0xA3, (byte)0xB9, (byte)0xE6, 0x53, 0x7A, (byte)0x95, 0x7C
    };

    private static final Random random = new Random();

    /**
     * Encrypts a password using a cyclic XOR operation with a predefined set of characters.
     * The Russian comment "«Жарим» пароль по циклическому XOR’у" translates to
     * "Roast" the password using cyclic XOR.
     *
     * @param password The password string to be encrypted.
     * @return A byte array representing the XOR-encrypted password.
     * @throws UnsupportedEncodingException if the "ASCII" encoding is not supported.
     */
    public static byte[] xorPass(String password) throws UnsupportedEncodingException {
        //noinspection CharsetObjectCanBeUsed
        byte[] pwd = password.getBytes("ASCII");
        byte[] out = new byte[pwd.length];
        for (int i = 0; i < pwd.length; i++) {
            out[i] = (byte)(pwd[i] ^ ROAST_CHARS[i % ROAST_CHARS.length]);
        }
        return out;
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     * Each byte is converted into two hexadecimal characters (0-9, a-f).
     *
     * @param data The byte array to convert.
     * @return The hexadecimal string representation of the byte array.
     */
    public static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte datum : data) {
            int halfbyte = (datum >>> 4) & 15;
            int two_halfs = 0;
            while (true) {
                if (halfbyte <= 9) {
                    buf.append((char) (halfbyte + 48));
                } else {
                    buf.append((char) ((halfbyte - 10) + 97));
                }
                halfbyte = datum & 15;
                int two_halfs2 = two_halfs + 1;
                if (two_halfs >= 1) {
                    break;
                }
                two_halfs = two_halfs2;
            }
        }
        return buf.toString();
    }

    /**
     * Generates a hash array used for authentication.
     * This method combines a key, a password (after MD5 hashing with "windows1251" encoding),
     * and a predefined AIM-specific MD5 string ({@link MD5#AIM_MD5_STRING}).
     * The combined byte array is then MD5 hashed.
     *
     * @param key A byte array representing a key.
     * @param password The user's password as a String.
     * @return A byte array representing the final MD5 hash.
     * @throws Exception If "windows1251" encoding is not supported or if MD5 hashing fails.
     */
    public static byte[] getHashArray(byte[] key, String password) throws Exception {
        //noinspection InjectedReferences
        byte[] passDigest = MD5.calculateMD5(password.getBytes("windows1251"));
        byte[] md5buf = new byte[key.length + passDigest.length + MD5.AIM_MD5_STRING.length];
        System.arraycopy(key, 0, md5buf, 0, key.length);
        int md5marker = key.length;
        System.arraycopy(passDigest, 0, md5buf, md5marker, passDigest.length);
        System.arraycopy(MD5.AIM_MD5_STRING, 0, md5buf, md5marker + passDigest.length, MD5.AIM_MD5_STRING.length);
        return MD5.calculateMD5(md5buf);
    }

    /**
     * Calculates a legacy MD5 hash used by older OSCAR (Open System for CommunicAtion in Realtime)
     * implementations. This method concatenates a key, a password (encoded in "windows-1251"),
     * and a predefined AIM_MD5_STRING, then computes the MD5 hash of the resulting byte array.
     *
     * <p>This method handles different Android SDK versions for "windows-1251" encoding:
     * <ul>
     *   <li>For Android API level 19 (KitKat) and above, it uses {@link Charset#forName(String)}.
     *   <li>For older Android versions, it falls back to {@link String#getBytes(String)},
     *       catching {@link UnsupportedEncodingException} if the encoding is not supported.
     *       If the encoding is unsupported, the stack trace is printed, and {@code null} is returned.
     * </ul>
     *
     * @param key A byte array representing the key.
     * @param password The password string to be included in the hash.
     * @return A byte array representing the MD5 hash, or {@code null} if "windows-1251" encoding
     *         is not supported on older Android versions.
     */
    public static byte[] getOldHashArray(byte[] key, String password) {
        byte[] passwordRaw;

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            Charset win1251 = Charset.forName("windows-1251");
            passwordRaw = password.getBytes(win1251);
        } else {
            try {
                passwordRaw = password.getBytes("windows-1251");
            } catch (java.io.UnsupportedEncodingException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return null;
            }
        }

        byte[] md5buf = new byte[key.length + passwordRaw.length + MD5.AIM_MD5_STRING.length];
        System.arraycopy(key, 0, md5buf, 0, key.length);
        int md5marker = key.length;
        System.arraycopy(passwordRaw, 0, md5buf, md5marker, passwordRaw.length);
        System.arraycopy(MD5.AIM_MD5_STRING, 0, md5buf, md5marker + passwordRaw.length, MD5.AIM_MD5_STRING.length);
        return MD5.calculateMD5(md5buf);
    }


    /**
     * Converts a hexadecimal string to a byte array.
     * Each pair of characters in the input string is interpreted as a hexadecimal representation of a byte.
     *
     * @param paramString The hexadecimal string to convert. Must contain an even number of characters.
     * @return A byte array representing the hexadecimal string.
     * @throws IllegalArgumentException If the input string has an odd number of characters or contains non-hexadecimal characters.
     * @noinspection unused
     */
    public static byte[] hexStringToBytesArray(String paramString) {
        int i = paramString.length();
        if (i % 2 != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }
        byte[] arrayOfByte = new byte[paramString.length() / 2];
        for (int j = 0; j < i; j += 2) {
            int k = j / 2;
            int m = j + 2;
            byte n = (byte) Integer.parseInt(paramString.substring(j, m), 16);
            arrayOfByte[k] = n;
        }
        return arrayOfByte;
    }

    /**
     * Compares two byte arrays for equality.
     * Two arrays are considered equal if they have the same length and all
     * corresponding elements are equal.
     *
     * @param what The first byte array.
     * @param with The second byte array.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static boolean arrayEquals(byte[] what, byte[] with) {
        if (what.length != with.length) {
            return false;
        }
        int len = what.length;
        for (int i = 0; i < len; i++) {
            if (what[i] != with[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given three bytes represent a specific Unicode pattern.
     * This method seems to implement a custom, narrow check for a particular byte sequence,
     * rather than a general Unicode validation.
     * The logic `a <= 10 && b > 10 && c <= 10` suggests it's looking for a pattern where
     * the first and third bytes are within a low range (0-10) and the middle byte is
     * outside that range (greater than 10). This is not a standard Unicode validation method.
     *
     * @param a The first byte.
     * @param b The second byte.
     * @param c The third byte.
     * @return {@code true} if the bytes match the specific pattern, {@code false} otherwise.
     * @noinspection unused
     */
    public static boolean isUnicode(byte a, byte b, byte c) {
        return a <= 10 && b > 10 && c <= 10;
    }

    /**
     * Converts a duration in seconds into a string representation of days, hours, minutes, and seconds.
     * The format of the output string is determined by the resource string "s_date_string",
     * which should contain placeholders like "%%%1", "%%%2", "%%%3", and "%%%4" for days,
     * hours, minutes, and seconds respectively.
     *
     * @param seconds The total duration in seconds.
     * @return A formatted string representing the duration.
     */
    public static String longitudeToString(long seconds) {
        int days = (int) (seconds / 86400);
        long seconds2 = seconds % 86400;
        int hours = (int) (seconds2 / 3600);
        long seconds3 = seconds2 % 3600;
        int minutes = (int) (seconds3 / 60);
        return match(resources.getString("s_date_string"), new String[]{String.valueOf(days), String.valueOf(hours), String.valueOf(minutes), String.valueOf(seconds3 % 60)});
    }

    /**
     * Checks if a byte array segment contains valid UTF-8 encoded data.
     *
     * <p>This method iterates through the specified portion of the byte array
     * and validates the UTF-8 sequence rules.
     *
     * <ul>
     *   <li>Single-byte characters (ASCII) must have the most significant bit as 0.
     *   <li>Multi-byte sequences start with a byte indicating the number of subsequent bytes.
     *   <li>Continuation bytes in a multi-byte sequence must start with "10" in binary.
     * </ul>
     *
     * @param array The byte array to check.
     * @param start The starting index in the array (inclusive).
     * @param lenght The number of bytes to check from the start index.
     * @return {@code true} if the specified segment of the array is valid UTF-8,
     *         {@code false} otherwise. Returns {@code false} if {@code lenght} is 0.
     * @noinspection unused - This method might be used by other parts of the application or library.
     */
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

    /**
     * Generates a random integer ID within a specific range.
     * The generated ID is a random integer between {@link GifDecoder#MaxStackSize} (inclusive)
     * and {@code GifDecoder.MaxStackSize + 28671} (inclusive).
     *
     * <p>The method uses {@link System#currentTimeMillis()} to seed the {@link Random}
     * number generator, ensuring a different sequence of random numbers for each invocation
     * if called with sufficient time separation.
     *
     * <p>The random number generation involves taking a random integer, applying a bitwise AND
     * operation with the mask {@code 28671} (which is {@code 0x6FFF} or {@code 0b110111111111111}),
     * and then adding {@link GifDecoder#MaxStackSize} to the result.
     * The bitwise AND operation effectively limits the random part to the range [0, 28671].
     *
     * @return A randomly generated integer ID.
     */
    public static int getRandomSSIId() {
        Random rnd = new Random(System.currentTimeMillis());
        int a = rnd.nextInt() & 28671;
        return a + GifDecoder.MaxStackSize;
    }

    /**
     * Generates a pseudo-random long value.
     * This method utilizes a shared {@link Random} instance initialized with the current system time.
     *
     * @return A pseudo-randomly generated long.
     * @noinspection unused - This method might be accessed via reflection or used by parts of the codebase not included in this snippet.
     */
    public static long getRandom() {
        return random.nextLong();
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace characters.
     * This is typically used to determine if a string has meaningful content for display purposes.
     *
     * @param source The string to check.
     * @return {@code true} if the string is null, empty, or consists only of whitespace; {@code false} otherwise.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static boolean isEmptyForDisplay(String source) {
        return source == null || source.trim().isEmpty();
    }

    /**
     * Converts a Java String to a byte array using Windows-1251 encoding.
     *
     * <p>This method manually maps specific Cyrillic characters (like Ё, Ї, etc.)
     * from their Unicode code points to their corresponding Windows-1251 byte values.
     * Other Cyrillic characters in the range U+0410 to U+044F (А-я) are converted
     * by a direct offset calculation. Characters outside these ranges are truncated
     * to their lower 8 bits (effectively treating them as ISO-8859-1 or similar).
     *
     * <p><b>Warning:</b> This method does not handle all Unicode characters correctly
     * and is specific to Windows-1251. For general-purpose string-to-byte conversion,
     * use {@link String#getBytes(java.nio.charset.Charset)} with the appropriate charset.
     *
     * @param s The string to convert.
     * @return A byte array representing the string in Windows-1251 encoding.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for specific legacy compatibility.
     */
    public static byte[] stringToByteArray1251(String s) {
        byte[] buf = new byte[s.length()];
        int size = s.length();
        for (int i = 0; i < size; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case 1025:
                    buf[i] = -88;
                    break;
                case 1028:
                    buf[i] = -86;
                    break;
                case 1030:
                    buf[i] = -78;
                    break;
                case 1031:
                    buf[i] = -81;
                    break;
                case 1105:
                    buf[i] = -72;
                    break;
                case 1108:
                    buf[i] = -70;
                    break;
                case 1110:
                    buf[i] = -77;
                    break;
                case 1111:
                    buf[i] = -65;
                    break;
                case 1168:
                    buf[i] = -91;
                    break;
                case 1169:
                    buf[i] = -76;
                    break;
                default:
                    if (ch >= 1040 && ch <= 1103) {
                        buf[i] = (byte) ((ch - 1040) + 192);
                    } else {
                        buf[i] = (byte) (ch & 255);
                    }
                    break;
            }
        }
        return buf;
    }

    /**
     * Calculates a hash code for a {@link Conference} object.
     * The hash is based on the conference's JID, nickname, profile ID, profile host, and profile password.
     * A constant value (65535) is subtracted from the initial hash code.
     *
     * @param conference The Conference object to hash.
     * @return An integer hash code for the conference.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static int getHash(Conference conference) {
        return (conference.JID + conference.nick + conference.profile.ID + conference.profile.host + conference.profile.PASS).hashCode() - 65535;
    }

    /**
     * Generates a hash code for an ICQ contact.
     * The hash is based on the contact's ID, profile ID, and profile password.
     * A constant value (65535) is subtracted from the standard hashCode result.
     *
     * @param contact The ICQContact object to generate the hash for.
     * @return An integer hash code for the contact.
     * @noinspection unused - This method might be used by parts of the codebase not shown or intended for future use.
     */
    public static int getHash(ICQContact contact) {
        return (contact.ID + contact.profile.ID + contact.profile.password).hashCode() - 65535;
    }

    /**
     * Generates a hash code for a {@link JContact} object.
     * The hash code is calculated based on the contact's ID, profile ID, profile host, and profile password.
     * A constant value (65535) is subtracted from the result.
     *
     * @param contact The JContact object for which to generate the hash.
     * @return An integer representing the hash code of the contact.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet,
     * or it could be intended for future use or accessed via reflection.
     */
    public static int getHash(JContact contact) {
        return (contact.ID + contact.profile.ID + contact.profile.host + contact.profile.PASS).hashCode() - 65535;
    }

    /**
     * Calculates a hash code for an MMPContact.
     * The hash is based on the contact's ID, profile ID, and profile password.
     * A constant value (65535) is subtracted from the standard hashCode result.
     *
     * @param contact The MMPContact object to hash.
     * @return An integer hash code for the contact.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet,
     * or it could be intended for future use or accessed via reflection.
     */
    public static int getHash(MMPContact contact) {
        return (contact.ID + contact.profile.ID + contact.profile.PASS).hashCode() - 65535;
    }

    /**
     * Converts a Java String to a UTF-8 encoded byte array.
     *
     * <p>This method handles different Android SDK versions for optimal UTF-8 encoding:
     * <ul>
     *   <li>For Android API level 19 (KitKat) and above, it uses {@link StandardCharsets#UTF_8}.
     *   <li>For older Android versions, it falls back to specifying "UTF-8" as a string,
     *       suppressing the {@code CharsetObjectCanBeUsed} lint warning.
     * </ul>
     * If the input string is {@code null}, this method returns {@code null}.
     *
     * <p>If an {@link UnsupportedEncodingException} occurs (which is highly unlikely for UTF-8
     * on standard Java/Android platforms), the stack trace is printed to standard error,
     * and {@code null} is returned.
     *
     * @param source The String to be encoded into UTF-8 bytes.
     * @return A byte array representing the UTF-8 encoded string, or {@code null}
     *         if the input string is null or if UTF-8 encoding is not supported.
     */
    public static byte[] prepareUTF8(String source) {
        if (source == null) return null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                return source.getBytes(StandardCharsets.UTF_8);
            } else {
                //noinspection CharsetObjectCanBeUsed
                return source.getBytes("UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Checks if the given string represents a valid UIN (User Identification Number).
     * A string is considered a valid UIN if it can be parsed as an integer
     * and its length is less than or equal to 9 characters.
     *
     * @param source The string to validate.
     * @return {@code true} if the string is a valid UIN, {@code false} otherwise.
     */
    public static boolean isUIN(String source) {
        try {
            Integer.parseInt(source);
            return source.length() <= 9;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a given string is a valid MRIM (Mail.Ru Instant Messenger) address.
     * An MRIM address is considered valid if:
     * <ul>
     *     <li>It is not null.</li>
     *     <li>It contains exactly one "@" symbol.</li>
     *     <li>The part before the "@" symbol is not empty.</li>
     *     <li>The part after the "@" symbol (the domain) is one of "list.ru", "mail.ru", "bk.ru", or "inbox.ru".</li>
     * </ul>
     *
     * @param source The string to check.
     * @return {@code true} if the string is a valid MRIM address, {@code false} otherwise.
     * @noinspection BooleanMethodIsAlwaysInverted
     */
    public static boolean isMrim(String source) {
        if (source == null) {
            return false;
        }
        String[] parts = source.split("@");
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        return parts[1].equals("list.ru") || parts[1].equals("mail.ru") || parts[1].equals("bk.ru") || parts[1].equals("inbox.ru");
    }

    /**
     * Checks if a given string is a valid email address.
     * An email address is considered valid if:
     * <ul>
     *     <li>It contains exactly one "@" symbol.</li>
     *     <li>The part after the "@" symbol (domain) contains at least one "." (dot).</li>
     * </ul>
     * This is a basic validation and does not cover all RFC email address standards.
     *
     * @param source The string to validate.
     * @return {@code true} if the string is a valid email address according to the basic rules, {@code false} otherwise.
     */
    public static boolean isEmail(String source) {
        String[] parts = source.split("@");
        if (parts.length != 2) {
            return false;
        }
        String[] parts1 = parts[1].split("\\.");
        return parts1.length >= 2;
    }

    /**
     * Creates a long representing the number of milliseconds since the Unix epoch (January 1, 1970, 00:00:00 GMT)
     * for the given date and time, adjusted to the local time zone.
     *
     * <p>This method calculates the total number of days from 1970 to the given year,
     * accounting for leap years (with a specific adjustment for the year 2000).
     * It then adds the days of the preceding months in the given year and the specified day of the month.
     * Finally, it converts the total days, hours, minutes, and seconds into milliseconds and
     * adjusts this GMT time to the local time zone using {@link #GMTToLocal(long)}.
     *
     * <p><b>Note:</b> The leap year calculation and the special handling for the year 2000
     * might have specific historical or system-dependent reasons not immediately obvious
     * from the code. For modern, robust date-time manipulation, consider using
     * {@link Calendar} or the {@code java.time} package (for Java 8+).
     *
     * @param year The year (e.g., 2023).
     * @param mon The month (1-12).
     * @param day The day of the month (1-31).
     * @param hour The hour of the day (0-23).
     * @param min The minute of the hour (0-59).
     * @param sec The second of the minute (0-59).
     * @return A long value representing the date and time in milliseconds since the epoch, adjusted to local time.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static long createLongTime(int year, int mon, int day, int hour, int min, int sec) {
        int febCount;
        byte[] dayCounts = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int day_count = ((year - 1970) * 365) + day + ((year - 1968) / 4);
        if (year >= 2000) {
            day_count--;
        }
        if (year % 4 == 0 && year != 2000) {
            day_count--;
            febCount = 29;
        } else {
            febCount = 28;
        }
        int i = 0;
        while (i < mon - 1) {
            day_count += i == 1 ? febCount : dayCounts[i];
            i++;
        }
        return GMTToLocal((((long) day_count * 24 * 3600) + (hour * 3600L) + (min * 60L) + sec) * 1000);
    }

    /**
     * Converts a given time from GMT (Greenwich Mean Time) to the local time zone.
     *
     * <p>This method calculates the offset between GMT and the local time zone,
     * considering daylight saving time if applicable. It then adds this offset
     * to the input GMT time to get the local time.
     *
     * <p>The offset calculation has some specific adjustments:
     * <ul>
     *   <li>It adds 3,600,000 milliseconds (1 hour) if daylight saving is not in effect.
     *       This seems counterintuitive and might be a bug or specific to a particular use case.
     *   <li>It subtracts 10,800,000 milliseconds (3 hours) from the current time zone offset.
     *       This could be an adjustment for a specific target time zone or a fixed base offset.
     *   <li>It subtracts 60,000 milliseconds (1 minute) from the total calculated offset.
     *       The reason for this adjustment is unclear from the code itself.
     * </ul>
     *
     * <p><b>Note:</b> The logic for offset calculation, particularly the fixed subtractions
     * and the conditional addition for daylight saving, might lead to incorrect conversions
     * if not carefully managed or if the underlying assumptions change.
     * Standard Java time API (java.time package) is generally recommended for more robust
     * time zone conversions.
     *
     * @param time The time in milliseconds since the epoch, in GMT.
     * @return The corresponding time in milliseconds since the epoch, adjusted to the local time zone.
     */
    public static long GMTToLocal(long time) {
        Calendar c = Calendar.getInstance();
        TimeZone tz = c.getTimeZone();
        long offset = ((tz.inDaylightTime(new Date()) ? 0 : 3600000) + (tz.getOffset(System.currentTimeMillis()) - 10800000)) - 60000;
        return time + offset;
    }

    /**
     * Retrieves the current system time formatted as a string in "HH:mm:ss" format.
     * This method uses the default system timezone.
     *
     * @return A string representing the current time (e.g., "14:35:02").
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static String getCurrentTimeString() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Retrieves the current date as a string formatted as "dd.MM.yyyy".
     *
     * <p>This method uses the system's current time to create a {@link Date} object
     * and then formats it using a {@link SimpleDateFormat} with the pattern "dd.MM.yyyy".
     * For example, if the current date is January 23, 2024, this method will return "23.01.2024".
     *
     * <p>The {@code @SuppressLint("SimpleDateFormat")} annotation is used to suppress warnings
     * about the potential for locale-specific formatting issues if the default locale
     * is not desired. In this specific case, the format string "dd.MM.yyyy" is
     * generally consistent across locales, but it's a good practice to be aware of this
     * when using {@link SimpleDateFormat} without explicitly setting a locale.
     *
     * @return A string representing the current date in "dd.MM.yyyy" format.
     */
    public static String getCurrentDateTimeString() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Computes a string based on the manufacturer and model name.
     * It trims and converts both strings to lowercase.
     * It then calculates the number of matching characters at the beginning of both strings,
     * up to the length of the shorter string.
     * If the percentage of matching characters is greater than 20%, it returns the model name.
     * Otherwise, it returns the manufacturer and model name concatenated with a space.
     *
     * @param manufact The manufacturer name.
     * @param model    The model name.
     * @return The model name if the match percentage is > 20%, otherwise "manufact model".
     */
    public static String compute(String manufact, String model) {
        String manufact_ = manufact.trim().toLowerCase();
        String model_ = model.trim().toLowerCase();
        int total = manufact_.length();
        if (model_.length() < total) {
            total = model_.length();
        }
        int matches = 0;
        for (int i = 0; i < total; i++) {
            if (manufact_.charAt(i) == model_.charAt(i)) {
                matches++;
            }
        }
        int percentage = (matches * 100) / total;
        if (percentage > 20) {
            return model;
        }
        return manufact + " " + model;
    }

    /**
     * Replaces placeholders in a source string with values from an array.
     * Placeholders are expected in the format "%%%N", where N is a 1-based index
     * corresponding to the position in the {@code parts} array.
     * For example, "%%%1" will be replaced by {@code parts[0]}, "%%%2" by {@code parts[1]}, and so on.
     *
     * @param source The string containing placeholders to be replaced.
     * @param parts An array of strings to substitute into the placeholders.
     * @return The source string with all placeholders replaced by the corresponding parts.
     */
    public static String match(String source, String[] parts) {
        String result = source;
        for (int i = 0; i < parts.length; i++) {
            result = replace(result, "%%%" + (i + 1), parts[i]);
        }
        return result;
    }

    /**
     * Replaces all occurrences of a specified part within a source string with a new part.
     * This method uses regular expressions for replacement and is case-sensitive by default,
     * but the flag `16` (Pattern.LITERAL) is used, meaning the `part` is treated as a literal string.
     *
     * @param source The original string.
     * @param part The substring to be replaced. This is treated as a literal string.
     * @param new_part The string to replace all occurrences of `part`.
     * @return A new string with all occurrences of `part` replaced by `new_part`.
     */
    public static String replace(String source, String part, String new_part) {
        Pattern pattern = Pattern.compile(part, 16);
        Matcher matcher = pattern.matcher(source);
        return matcher.replaceAll(new_part);
    }

    /**
     * Splits a string by a given delimiter using regular expressions.
     * The delimiter is treated as a regular expression, and the matching is case-insensitive.
     *
     * @param source The string to be split. If null, an empty array is returned.
     * @param determiner The regular expression to use as a delimiter. If null, an empty array is returned.
     * @return An array of strings resulting from the split. Returns an empty array if either the source or determiner is null.
     */
    public static String[] split(String source, String determiner) {
        if (source == null) {
            return new String[0];
        }
        if (determiner == null) {
            return new String[0];
        }
        Pattern p = Pattern.compile(determiner, 16);
        return p.split(source);
    }

    /**
     * Retrieves a formatted string representation of an exception's stack trace,
     * including the full stack trace and details of the first stack trace element.
     *
     * <p>The output format is:
     * <pre>
     * [Full Stack Trace from Throwable.printStackTrace()]
     * [NATIVE]: (if applicable)
     * Class: [ClassName of the first stack element]
     * Method: [MethodName of the first stack element]:[LineNumber of the first stack element]
     * </pre>
     *
     * @param e The Throwable object for which to get the stack trace.
     * @return A string containing the formatted stack trace information.
     *         Returns an empty string followed by the first stack element's details
     *         if {@link #getStackTraceString(Throwable)} returns an empty string for the throwable.
     * @noinspection unused - This method might be used for debugging or logging in specific contexts
     * not immediately visible, or it could be intended for future use.
     */
    public static String getStack(Throwable e) {
        StackTraceElement[] list = e.getStackTrace();
        String tr = getStackTraceString(e);
        String result = tr + "\n";
        StackTraceElement item = list[0];
        String line = (item.isNativeMethod() ? "[NATIVE]:\n" : "") + "Class: " + item.getClassName() + "\nMethod: " + item.getMethodName() + ":" + item.getLineNumber();
        return result + line;
    }

    /**
     * Converts a Throwable's stack trace into a string.
     * If the provided Throwable is null, an empty string is returned.
     * Otherwise, the stack trace is written to a StringWriter and then
     * returned as a trimmed string.
     *
     * @param tr The throwable whose stack trace is to be converted.
     * @return A string representation of the stack trace, or an empty string if {@code tr} is null.
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        String result = sw.toString();
        return result.trim();
    }

    /**
     * Reads the content of the "changelog.txt" file located in the "info" assets folder.
     * This method attempts to read the file character by character and append it to a StringBuilder.
     * It includes basic error handling for file operations, attempting to close the reader
     * even if exceptions occur during reading. Any exceptions encountered during reading or
     * closing the stream are silently ignored, and an empty string might be returned in case of failure.
     *
     * @return A String containing the content of the changelog file, or an empty string if
     *         the file cannot be read or is empty.
     */
    public static String readChangeLog() {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(resources.am.open("info/changelog.txt")));
            while (reader2.ready()) {
                try {
                    result.append((char) reader2.read());
                } catch (Exception e) {
                    reader = reader2;
                }
            }
            reader = reader2;
        } catch (Exception ignored) {
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception ignored) {
            }
        }
        return result.toString();
    }

    /**
     * Dynamically sets the height of a ListView based on the total height of its children.
     * This is useful when you want the ListView to expand to show all its items
     * without scrolling, for example, when the ListView is inside a ScrollView.
     *
     * <p><b>Warning:</b> Using this method can be inefficient for ListViews with a large
     * number of items, as it requires measuring each child view. Consider alternative
     * layouts or approaches if performance is critical for very long lists.
     *
     * @param listView The ListView whose height needs to be adjusted.
     * @noinspection unused - This method might be used in specific layout scenarios where dynamic height adjustment is required.
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + totalHeight;
            listView.setLayoutParams(params);
        }
    }

    /**
     * Copies a specified range of a byte array into a new array.
     * The new array will have a length of {@code end - start}.
     *
     * <p>If {@code end} is greater than the length of the original array,
     * the elements from {@code start} to the end of the original array are copied,
     * and the remaining elements in the new array are filled with the default byte value (0).
     *
     * @param original The byte array from which a range is to be copied.
     * @param start The initial index of the range to be copied, inclusive.
     * @param end The final index of the range to be copied, exclusive.
     *            This may be greater than {@code original.length}, in which case
     *            0s are padded at the end of the copy.
     * @return A new byte array containing the specified range from the original array.
     * @throws IllegalArgumentException if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0} or {@code start > original.length}.
     * @noinspection unused
     */
    public static byte[] copyOfRange(byte[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        byte[] result = new byte[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Reads a String from a DataInputStream, assuming UTF-16 Big Endian encoding.
     * Each character is represented by two bytes, with the most significant byte read first.
     *
     * @param stream The DataInputStream to read from.
     * @param length The number of bytes to read from the stream. This should be an even number,
     *               as each character is two bytes. If an odd length is provided, the behavior
     *               might be unpredictable or lead to an EOFException depending on the stream's state.
     * @return The String read from the stream.
     * @throws Exception If an I/O error occurs while reading from the stream.
     * @noinspection unused
     */
    public static String readStringUnicodeBE(DataInputStream stream, int length) throws Exception {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < length; i += 2) {
            int a_ = stream.readByte();
            int b_ = stream.readByte();
            int pair = (a_ << 8) | b_;
            b.append((char) pair);
        }
        return b.toString();
    }

    /**
     * Writes a string to a DataOutputStream in Unicode Big Endian format.
     * Each character of the string is written as two bytes:
     * the higher 8 bits (big endian) followed by the lower 8 bits.
     *
     * @param string The string to write.
     * @param stream The DataOutputStream to write to.
     * @throws Exception If an I/O error occurs.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static void writeStringUnicodeBE(String string, DataOutputStream stream) throws Exception {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            int c = string.charAt(i);
            int a = (c >> 8) & 255;
            int b = c & 255;
            stream.write(a);
            stream.write(b);
        }
    }

    /**
     * Reads a Unicode string from a {@link DataInputStream} that is prefixed with its length.
     * The method first reads an integer representing the total number of bytes for the string characters.
     * Then, it reads pairs of bytes, interpreting each pair as a big-endian Unicode character (UTF-16BE),
     * and appends these characters to a {@link StringBuilder}.
     *
     * <p>The length read from the stream is the number of bytes, so the number of characters
     * will be {@code length / 2}.
     *
     * @param stream The DataInputStream to read from.
     * @return The string read from the stream.
     * @throws Exception If an I/O error occurs or if the stream ends prematurely.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static String readPreLengthStringUnicodeBE(DataInputStream stream) throws Exception {
        StringBuilder b = new StringBuilder();
        int length = stream.readInt();
        for (int i = 0; i < length; i += 2) {
            int a_ = stream.readByte();
            int b_ = stream.readByte();
            int pair = (a_ << 8) | b_;
            b.append((char) pair);
        }
        return b.toString();
    }

    /**
     * Writes a string to the specified DataOutputStream using Unicode Big Endian encoding,
     * prefixed with its length in bytes.
     *
     * <p>First, the total length of the string in bytes (number of characters * 2) is written as an integer.
     * Then, each character of the string is written as two bytes in Big Endian order
     * (most significant byte first).
     *
     * @param string The string to write.
     * @param stream The DataOutputStream to write to.
     * @throws Exception if an I/O error occurs.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static void writePreLengthStringUnicodeBE(String string, DataOutputStream stream) throws Exception {
        int length = string.length();
        stream.writeInt(length * 2);
        for (int i = 0; i < length; i++) {
            int c = string.charAt(i);
            int a = (c >> 8) & 255;
            int b = c & 255;
            stream.write(a);
            stream.write(b);
        }
    }

    /**
     * Normalizes a file path by ensuring it ends with a file separator.
     * If the given path string does not already end with the system-dependent
     * file separator character (e.g., "/" on Unix-like systems, "\" on Windows),
     * this method appends the separator to the path.
     *
     * @param path The file path string to normalize.
     * @return The normalized file path string, guaranteed to end with a file separator.
     */
    public static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }
        return path;
    }

    /**
     * Verifies if a given filename contains any reserved characters.
     * The set of reserved characters is defined in the {@link #ReservedChars} constant.
     *
     * @param name The filename string to verify.
     * @return {@code true} if the filename does not contain any reserved characters,
     *         {@code false} otherwise.
     */
    public static boolean verifyFileName(String name) {
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (ReservedChars.indexOf(c) >= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the SHA-1 hash of a given string and returns it as a hexadecimal string.
     *
     * <p>This method is synchronized on the {@code utilities.class} object to ensure
     * thread-safe initialization and use of the static {@link MessageDigest} instance
     * for SHA-1.
     *
     * <p>The input string is converted to bytes using UTF-8 encoding.
     * <ul>
     *   <li>On Android API level 19 (KitKat) and above, {@link StandardCharsets#UTF_8} is used.
     *   <li>On older versions, "UTF-8" is specified directly.
     * </ul>
     *
     * <p>If the "SHA-1" algorithm is not available (which is highly unlikely on standard
     * Android/Java platforms), an error message is printed to {@code System.err}, and
     * the method will return {@code null}.
     *
     * <p>If an {@link UnsupportedEncodingException} occurs during UTF-8 byte conversion
     * (also highly unlikely), the stack trace is printed, and {@code null} is returned.
     *
     * @param data The string to hash.
     * @return The hexadecimal representation of the SHA-1 hash of the input string.
     *         Returns {@code null} if the SHA-1 algorithm is not found or if
     *         UTF-8 encoding is not supported.
     * @noinspection unused
     */
    public static synchronized String getHexSha1Hash(String data) {
        String convertToHex;
        synchronized (utilities.class) {
            if (digest == null) {
                try {
                    digest = java.security.MessageDigest.getInstance("SHA-1");
                } catch (java.security.NoSuchAlgorithmException e) {
                    System.err.println("Failed to load the SHA-1 MessageDigest");
                    return null;
                }
            }
            try {
                byte[] utf8Bytes;
                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    utf8Bytes = data.getBytes(StandardCharsets.UTF_8);
                } else {
                    //noinspection CharsetObjectCanBeUsed
                    utf8Bytes = data.getBytes("UTF-8");
                }
                digest.update(utf8Bytes);
                convertToHex = convertToHex(digest.digest());
            } catch (UnsupportedEncodingException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return null;
            }
        }
        return convertToHex;
    }

    /**
     * Converts an integer value to its 4-byte (8-character) hexadecimal string representation.
     * The resulting string is padded with leading zeros if necessary to ensure it is 8 characters long.
     * For example, the integer value 255 (0xFF) would be converted to "000000ff".
     *
     * @param value The integer value to convert.
     * @return The 4-byte hexadecimal string representation of the integer.
     */
    public static String to4ByteHEX(int value) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(value));
        int count = 8 - hex.length();
        for (int i = 0; i < count; i++) {
            hex.insert(0, "0");
        }
        return hex.toString();
    }

    /**
     * Checks if any of the specified character indices within a Spannable fall within a ClickableSpan.
     * This is useful for determining if a click or touch event at a particular character position
     * should trigger a link action.
     *
     * @param spn The Spannable object containing the text and ClickableSpans.
     * @param index A varargs array of integer indices to check. These are typically character offsets
     *              within the Spannable.
     * @return {@code true} if at least one of the provided indices falls within the start (exclusive)
     *         and end (exclusive) of any ClickableSpan in the Spannable. Returns {@code false}
     *         if no such index is found or if there are no ClickableSpans.
     */
    public static boolean isThereLinks(Spannable spn, int... index) {
        ClickableSpan[] spans = spn.getSpans(0, spn.length(), ClickableSpan.class);
        for (ClickableSpan s : spans) {
            int start = spn.getSpanStart(s);
            int end = spn.getSpanEnd(s);
            for (int idx : index) {
                if (start < idx && idx < end) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Converts a duration in seconds to a string formatted as "MM:SS" (minutes:seconds).
     * Both minutes and seconds are zero-padded to two digits. For example, 90 seconds
     * would be formatted as "01:30".
     *
     * @param seconds The total number of seconds.
     * @return A string representing the time in MM:SS format.
     * @noinspection unused - This method might be used by parts of the codebase not included in this snippet or intended for future use.
     */
    public static String toMMHHTimeString(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return normalizeStringLength(String.valueOf(minutes), "0", 2) + ":" + normalizeStringLength(String.valueOf(secs), "0", 2);
    }

    /**
     * Check if device has a hardware menu key.
     * For pre-ICS devices we assume menu key is present.
     */
    public static boolean hasHardwareMenuKey(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewConfiguration config = ViewConfiguration.get(context);
            return config.hasPermanentMenuKey();
        }
        return true;
    }

    /**
     * Normalizes the length of a string by prepending a specified character
     * until the string reaches the desired length.
     *
     * <p>If the source string's length is already equal to or greater than the
     * target length, the original string is returned unchanged. Otherwise,
     * the {@code char_} string is inserted at the beginning of the source string
     * repeatedly until the total length equals {@code length}.
     *
     * @param src The source string to normalize.
     * @param char_ The character (as a String, typically a single character) to prepend.
     * @param length The desired final length of the string.
     * @return The normalized string. If {@code src} is already {@code length} or longer,
     *         {@code src} is returned as is.
     */
    public static String normalizeStringLength(String src, String char_, int length) {
        StringBuilder srcBuilder = new StringBuilder(src);
        while (srcBuilder.length() < length) {
            srcBuilder.insert(0, char_);
        }
        src = srcBuilder.toString();
        return src;
    }

    /**
     * Invokes a method on a given object using reflection.
     * <p>
     * This method attempts to find a declared method with the specified name and argument types
     * within the provided class. It then makes the method accessible (if it's not already)
     * and invokes it on the receiver object with the given arguments.
     * <p>
     * Errors during method lookup or invocation (e.g., NoSuchMethodException, IllegalAccessException,
     * InvocationTargetException) are caught and logged.
     *
     * @param cls      The class in which the method is declared.
     * @param method   The name of the method to invoke.
     * @param receiver The object instance on which to invoke the method.
     *                 For static methods, this can be {@code null}.
     * @param args     The arguments to pass to the method. The types of these arguments
     *                 are used to find the matching method signature.
     * @noinspection unused - This method is likely used for dynamic method calls where the specific method is not known at compile time.
     */
    public static void invokeMethod(Class<?> cls, String method, Object receiver, Object... args) {
        try {
            Log.e("utilities", cls.getSimpleName() + ".invokeMethod( " + method + " )");
            //noinspection rawtypes
            Class[] arg_types = new Class[args.length];
            for (int i = 0; i < arg_types.length; i++) {
                arg_types[i] = args[i].getClass();
            }
            Method m = cls.getDeclaredMethod(method, arg_types);
            m.setAccessible(true);
            m.invoke(receiver, args);
            Log.e("utilities", cls.getSimpleName() + ".invokeMethod( " + method + " ) -- success");
        } catch (Exception e) {
            Log.e("utilities", "invokeMethod()", e);
        }
    }
}