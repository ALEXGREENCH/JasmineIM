package ru.ivansuper.jasmin.jabber;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class xml_utils {
    public static final String VERIFYER = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz@.";
    private static final String amp___ = "&";
    private static final Pattern amp__ = Pattern.compile(amp___, 16);
    private static final String lt___ = "<";
    private static final Pattern lt__ = Pattern.compile(lt___, 16);
    private static final String gt___ = ">";
    private static final Pattern gt__ = Pattern.compile(gt___, 16);
    private static final String quot___ = "\"";
    private static final Pattern quot__ = Pattern.compile(quot___, 16);
    private static final String apos___ = "'";
    private static final Pattern apos__ = Pattern.compile(apos___, 16);
    private static final String amp____ = "&amp;";
    private static final Pattern amp_ = Pattern.compile(amp____, 16);
    private static final String lt____ = "&lt;";
    private static final Pattern lt_ = Pattern.compile(lt____, 16);
    private static final String gt____ = "&gt;";
    private static final Pattern gt_ = Pattern.compile(gt____, 16);
    private static final String quot____ = "&quot;";
    private static final Pattern quot_ = Pattern.compile(quot____, 16);
    private static final String apos____ = "&apos;";
    private static final Pattern apos_ = Pattern.compile(apos____, 16);

    public static String convertAsciiToUtf8(String str) {
        try {
            return new String(str.getBytes("ascii"), "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }

    public static String decodeString(String str) {
        if (str == null) {
            return null;
        }
        return replace(replace(replace(replace(replace(str, lt_, lt___), gt_, gt___), quot_, quot___), apos_, apos___), amp_, amp___);
    }

    public static String encodeString(String str) {
        if (str == null) {
            return null;
        }
        return replace(replace(replace(replace(replace(str, amp__, amp____), lt__, lt____), gt__, gt____), quot__, quot____), apos__, apos____);
    }

    public static byte[] getRawMD5Hash(String str) {
        try {
            return str.getBytes("utf8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final String getTagContent(String str, String str2) {
        String strSubstring;
        int iIndexOf;
        int iIndexOf2;
        if (str != null) {
            String lowerCase = new String(str).toLowerCase();
            String lowerCase2 = str2.toLowerCase();
            int iIndexOf3 = lowerCase.indexOf(lt___ + lowerCase2);
            if (iIndexOf3 != -1 && (iIndexOf = (strSubstring = lowerCase.substring(iIndexOf3)).indexOf(gt___, 0)) != -1 && (iIndexOf2 = strSubstring.indexOf("</" + lowerCase2 + gt___)) != -1) {
                return str.substring(iIndexOf + 1, iIndexOf2);
            }
        }
        return null;
    }

    public static HashMap<String, String> parseParams(String str) {
        HashMap<String, String> map = new HashMap<>();
        if (str != null) {
            String strReplaceAll = str.replaceAll(quot___, apos___);
            int i = 0;
            while (true) {
                int iIndexOf = strReplaceAll.indexOf("='", i);
                if (iIndexOf == -1) {
                    break;
                }
                String strSubstring = strReplaceAll.substring(i, iIndexOf);
                int i2 = iIndexOf + 2;
                int iIndexOf2 = strReplaceAll.indexOf(apos___, i2);
                map.put(strSubstring.trim(), strReplaceAll.substring(i2, iIndexOf2));
                i = iIndexOf2 + 2;
            }
        }
        return map;
    }

    public static final String replace(String str, String str2, String str3) {
        return Pattern.compile(str2, 16).matcher(str).replaceAll(str3);
    }

    public static final String replace(String str, Pattern pattern, String str2) {
        return pattern.matcher(str).replaceAll(str2);
    }

    public static String resolveHeader(String str) {
        String str2;
        int iIndexOf;
        if (str == null || str.length() < 3 || (iIndexOf = (str2 = new String(str)).indexOf(" ")) == -1) {
            return null;
        }
        String strSubstring = str2.substring(iIndexOf);
        int length = strSubstring.length();
        int iIndexOf2 = strSubstring.indexOf("/>");
        int iIndexOf3 = strSubstring.indexOf(gt___);
        if (iIndexOf2 != -1 && iIndexOf2 < length) {
            length = iIndexOf2;
        }
        if (iIndexOf3 == -1 || iIndexOf3 >= length) {
            iIndexOf3 = length;
        }
        return strSubstring.substring(1, iIndexOf3);
    }

    public static String resolveHeaderName(String str) {
        if (str.length() < 3) {
            return null;
        }
        int length = str.length();
        int iIndexOf = str.indexOf("/>");
        int iIndexOf2 = str.indexOf(gt___);
        int iIndexOf3 = str.indexOf(" ");
        if (iIndexOf != -1 && iIndexOf < length) {
            length = iIndexOf;
        }
        if (iIndexOf2 == -1 || iIndexOf2 >= length) {
            iIndexOf2 = length;
        }
        if (iIndexOf3 == -1 || iIndexOf3 >= iIndexOf2) {
            iIndexOf3 = iIndexOf2;
        }
        return str.substring(1, iIndexOf3);
    }

    public static boolean verifyStringAsJID(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (VERIFYER.indexOf(str.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }
}
