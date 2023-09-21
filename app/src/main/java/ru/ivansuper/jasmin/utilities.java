package ru.ivansuper.jasmin;

import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.icq.ByteBuffer;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.jzlib.JZlib;

public class utilities {
    private static final String ReservedChars = "|\\?*<\":>+[]/'";
    private static MessageDigest digest;
    /**
     * @noinspection unused
     */
    public static long used_memory;
    public static final Random RANDOM = new Random(System.currentTimeMillis());
    /**
     * @noinspection unused
     */
    public static String chars = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZzАаБбВвГгДдЕеЁёЖжЗзИиЙйКкЛлМмНнОоПпРрСсТтУуФфХхЦцЧчШшЩщЪъЫыЬьЭэЮюЯя";
    /**
     * @noinspection unused
     */
    public static String latin_chars = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
    /**
     * @noinspection unused
     */
    public static String randomized = "308201e53082014ea00302010202044ec17a4d300d06092a864886f70d01010505003037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f6964204465627567301e170d3131313131343230333030355a170d3431313130363230333030355a3037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f696420446562756730819f300d06092a864886f70d010101050003818d00308189028181009dfd6f277a0fbd2d93bf0527405d02d31091d7bd8f8ec1d3ec62919bab815bc728d91a62863141ddc8f38cc6dee5f1d6a8bbbc6c920030843aee75f625d65fb92eff7c5ce7643c453736ed485aa404094f171c32b9397bf0c75f489a17e78e22e0fe608018c4eb8b9a03966e9285b1b2e73e61d0903faa4a87f3b847f71de40d0203010001300d06092a864886f70d01010505000381810093628f179da1c93f779a9773acf24f291fedbb1edab059cba66c678840c31a9bac02a7f64351cd41c058224cfcaba669c1bd507cbaf7a082691d9180048345eb764ae60d9de2d6f16573b95bf46dfc21a1e2cb6f160e997b9d7e048a88e4b1f18a984e72eb183fa50da20287f1386f9a7166e70525eb17744cbcb0961831b3c5";
    /**
     * @noinspection unused
     */
    public static String randomized2 = "308201eb30820154a00302010202044e887026300d06092a864886f70d01010505003039310f300d06035504061306313535393030310d300b060355040a1304486f6d65311730150603550403130e4976616e204b75647279616b6f763020170d3131313030323134303733345a180f32303631303931393134303733345a3039310f300d06035504061306313535393030310d300b060355040a1304486f6d65311730150603550403130e4976616e204b75647279616b6f7630819f300d06092a864886f70d010101050003818d00308189028181009721a5313ace678280700530dfafa15f563fd37cb6a10107b0be2b7de8c27eac2c5c1b950f21fb52ac9cd7ac3ab10a99eb60dbf9dc23b5207eef1614b01d8ba1c08e45f94335c4cd7c10daa0b4ccd8fe48dece1c73f3a9f66292ac9cca06eb1f6e199bc31c6f47cc0d0fce1faad2a33013f9e0c51a0308271591aad1dc03a8350203010001300d06092a864886f70d0101050500038181000b72002e821026583a19faca0ddf2ecb559a44549dcb7fc310b492921704838566e5e2beb68e6ffd2e931113b9e6cf2ad80c942022bb0a32509a9bc3e89fe082c7a554c66d1592ba0d0b3a92c00313141469aabd115a54e8b7eb684ec4982e48a34b45a91d6a9f4173e6d17c33cc36ae1bdfcd73a0de6fa22d981ba2e52c999c";
    /**
     * @noinspection unused
     */
    public static String randomized3 = "f6d653117301506035c17a4d300d06092a864886f70d01010505003037310b3009060355040613050403130e4976616ec17a4d300d06092a864886f70d01010505003037310b30c17a4d300d06092a864886f70d01010505003037310b30090603550406130090603550406130204b75647279616b6f763020170d3131313030323134303733345a180f32303631303931393134303733345a3039310f300d06035504061306313535393030310d300b060355040a1304486f6d65311730150603550403130e4976616e204b75647279616b6f7630819f300d06092a864886f70d010101050003818d00308189028181009721a5313ace678280700530dfafa15f563fd37cb6a10107b0be2b7de8c27eac2c5c1b950f21fb52ac9cd7ac3ab10a99eb60dbf9dc23b5207eef1614b01d8ba1c08e45f94335c4cd7c10daa0b4ccd8fe48dece1c73f3a9f66292ac9cca06eb1f6e199bc31c6f47cc0d0fce1faad2a33013f9e0c51a0308271591aad1dc03a8350203010001300d06092a864886f70d0101050500038181000b72002e821026583a19faca0ddf2ecb559a44549dcb7fc310b492921704838566e5e2beb68e6ffd2e931113b9e6cf2ad80c942022bb0a32509a9bc3e89fe082c7a554c66dc17a4d300d06092a864886f70d01010505003037310b3009060355040613000313141469aabd115981ba2e52c999c";
    /**
     * @noinspection unused
     */
    public static String randomized4 = "505003037310b3009060355040613050403130e4976616ec17a4d300d06092af6d653117301506035c17a4d300d06092a864886f70d010100b30c17a4d300d06092a864886f70d01010505003037310b30090603550406130090603550406130204b75647279616b6f763020170d3131313030323134303733345a180f323036313039313931343037331304486f6d65311730150603550403130e4976616e345a3039310f300d0603550406345a3039310f300d06035504061306313535393030310d300b060355040a30310d300b060355040a79616b6f7630819f300d06092a864886f70d010101050003818d00308189028181009721a5313ace678280700530dfafa15f563fd37cb6a10107b0be2b7de8c27eac2c5c1b95e48dece1c73f3a9f66292ac9cca06eb1f6e199bc3ce1faad2a33013f9e0c51a3013f9e0c51a0308271591aad1dc03a8350203010001300d06092a864886f70d0101050500038181000b72002e821026583a19faca0ddf2ecb559a44549dcb7fc310b492921704838566e5e2beb68e6ffd2e931113b9e6cf2ad80c942022bb0a32509a9bc3e89fe082c7a554c66dc17a4d300d060ce1faad2a33013f9e0c51a981ba2e52cce1faad2fe082c7a554c66dc17a4d300d060ce1faae6d17c33cc36ae1003037310b30090603550406139c";
    /**
     * @noinspection FieldMayBeFinal
     */
    private static int[] xorArray = {243, 38, 129, 196, 57, 134, 219, 146, 113, 163, 185, 230, 83, 122, 149, 124};
    private static final Random random = new Random();

    /** @noinspection unused*/
    public static byte[] xorPass(String source) throws Exception {
        byte[] res = new byte[source.length()];
        //noinspection InjectedReferences
        byte[] src = source.getBytes("windows1251");
        for (int i = 0; i < src.length; i++) {
            res[i] = (byte) (src[i] ^ xorArray[i]);
        }
        return ByteBuffer.normalizeBytes(res, source.length());
    }

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
     * @noinspection unused
     */
    public static byte[] getHashArray(byte[] key, String password) throws Exception {
        //noinspection InjectedReferences
        byte[] passwordRaw = password.getBytes("windows1251");
        byte[] md5buf = new byte[key.length + passwordRaw.length + MD5.AIM_MD5_STRING.length];
        System.arraycopy(key, 0, md5buf, 0, key.length);
        int md5marker = key.length;
        System.arraycopy(passwordRaw, 0, md5buf, md5marker, passwordRaw.length);
        System.arraycopy(MD5.AIM_MD5_STRING, 0, md5buf, md5marker + passwordRaw.length, MD5.AIM_MD5_STRING.length);
        return MD5.calculateMD5(md5buf);
    }

    /**
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
     * @noinspection unused
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
     * @noinspection unused
     */
    public static boolean isUnicode(byte a, byte b, byte c) {
        return a <= 10 && b > 10 && c <= 10;
    }

    public static String longitudeToString(long seconds) {
        int days = (int) (seconds / 86400);
        long seconds2 = seconds % 86400;
        int hours = (int) (seconds2 / 3600);
        long seconds3 = seconds2 % 3600;
        int minutes = (int) (seconds3 / 60);
        return match(resources.getString("s_date_string"), new String[]{String.valueOf(days), String.valueOf(hours), String.valueOf(minutes), String.valueOf(seconds3 % 60)});
    }

    /**
     * @noinspection unused
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
                continue;
            }
        }
        return true;
    }

    public static int getRandomSSIId() {
        Random rnd = new Random(System.currentTimeMillis());
        int a = rnd.nextInt() & 28671;
        return a + GifDecoder.MaxStackSize;
    }

    /** @noinspection unused*/
    public static long getRandom() {
        return random.nextLong();
    }

    /**
     * @noinspection unused
     */
    public static boolean isEmptyForDisplay(String source) {
        return source == null || source.trim().length() == 0;
    }

    /**
     * @noinspection unused
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
     * @noinspection unused
     */
    public static int getHash(Conference conference) {
        return (conference.JID + conference.nick + conference.profile.ID + conference.profile.host + conference.profile.PASS).hashCode() - 65535;
    }

    /**
     * @noinspection unused
     */
    public static int getHash(ICQContact contact) {
        return (contact.ID + contact.profile.ID + contact.profile.password).hashCode() - 65535;
    }

    /**
     * @noinspection unused
     */
    public static int getHash(JContact contact) {
        return (contact.ID + contact.profile.ID + contact.profile.host + contact.profile.PASS).hashCode() - 65535;
    }

    /**
     * @noinspection unused
     */
    public static int getHash(MMPContact contact) {
        return (contact.ID + contact.profile.ID + contact.profile.PASS).hashCode() - 65535;
    }

    /**
     * @noinspection unused
     */
    public static byte[] prepareUTF8(String source) {
        try {
            return source.getBytes("utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isUIN(String source) {
        try {
            Integer.parseInt(source);
            return source.length() <= 9;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isMrim(String source) {
        if (source == null) {
            return false;
        }
        String[] parts = source.split("@");
        if (parts.length != 2 || parts[0].length() == 0) {
            return false;
        }
        return parts[1].equals("list.ru") || parts[1].equals("mail.ru") || parts[1].equals("bk.ru") || parts[1].equals("inbox.ru");
    }

    public static boolean isEmail(String source) {
        String[] parts = source.split("@");
        if (parts.length != 2) {
            return false;
        }
        String[] parts1 = parts[1].split("\\.");
        return parts1.length >= 2;
    }

    /**
     * @noinspection unused
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

    public static long GMTToLocal(long time) {
        Calendar c = Calendar.getInstance();
        TimeZone tz = c.getTimeZone();
        long offset = ((tz.inDaylightTime(new Date()) ? 0 : 3600000) + (tz.getOffset(System.currentTimeMillis()) - 10800000)) - 60000;
        return time + offset;
    }

    /** @noinspection unused*/
    public static String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String fullFormattedDate = sdf.format(new Date(System.currentTimeMillis()));
        return fullFormattedDate;
    }

    public static String getCurrentDateTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String fullFormattedDate = sdf.format(new Date(System.currentTimeMillis()));
        return fullFormattedDate;
    }

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

    public static String match(String source, String[] parts) {
        String result = source;
        for (int i = 0; i < parts.length; i++) {
            result = replace(result, "%%%" + (i + 1), parts[i]);
        }
        return result;
    }

    public static String replace(String source, String part, String new_part) {
        Pattern pattern = Pattern.compile(part, 16);
        Matcher matcher = pattern.matcher(source);
        return matcher.replaceAll(new_part);
    }

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
     * @noinspection unused
     */
    public static String getStack(Throwable e) {
        StackTraceElement[] list = e.getStackTrace();
        String tr = getStackTraceString(e);
        String result = "" + tr + "\n";
        StackTraceElement item = list[0];
        String line = (item.isNativeMethod() ? "[NATIVE]:\n" : "") + "Class: " + item.getClassName() + "\nMethod: " + item.getMethodName() + ":" + item.getLineNumber();
        return result + line;
    }

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
     * @noinspection unused
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
     * @noinspection unused
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
     * @noinspection unused
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
     * @noinspection unused
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

    public static String normalizePath(String path) {
        if (!path.endsWith(File.separator)) {
            return path + File.separator;
        }
        return path;
    }

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
     * @noinspection unused
     */
    public static synchronized String getHexSha1Hash(String data) {
        String convertToHex;
        synchronized (utilities.class) {
            if (digest == null) {
                try {
                    digest = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("Failed to load the SHA-1 MessageDigest");
                }
            }
            try {
                digest.update(data.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e2) {
                System.err.println(e2);
            }
            convertToHex = convertToHex(digest.digest());
        }
        return convertToHex;
    }

    public static String to4ByteHEX(int value) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(value));
        int count = 8 - hex.length();
        for (int i = 0; i < count; i++) {
            hex.insert(0, "0");
        }
        return hex.toString();
    }

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
     * @noinspection unused
     */
    public static String toMMHHTimeString(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return normalizeStringLength(String.valueOf(minutes), "0", 2) + ":" + normalizeStringLength(String.valueOf(secs), "0", 2);
    }

    public static String normalizeStringLength(String src, String char_, int length) {
        StringBuilder srcBuilder = new StringBuilder(src);
        while (srcBuilder.length() < length) {
            srcBuilder.insert(0, char_);
        }
        src = srcBuilder.toString();
        return src;
    }

    /**
     * @noinspection unused
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