package ru.ivansuper.jasmin.jabber;

import android.text.SpannableStringBuilder;
import ru.ivansuper.jasmin.StyleSpan;

public class XMLFormat {
    public static final CharSequence format(String str) {
        String str2 = new String(str);
        int i = 0;
        String strSubstring = str2.substring(0, str2.indexOf(">", 0) + 1);
        boolean z = strSubstring.charAt(strSubstring.length() - 1) == "/".charAt(0);
        String strResolveHeaderName = xml_utils.resolveHeaderName(strSubstring);
        String tagContent = getTagContent(str2, strResolveHeaderName, strSubstring);
        String str3 = "";
        if (tagContent == null) {
            tagContent = "";
        }
        if (!z && str2.substring(str2.length() - (strResolveHeaderName.length() + 3), str2.length()).equals("</" + strResolveHeaderName + ">")) {
            str3 = "</" + strResolveHeaderName + ">";
        }
        String strReplace = xml_utils.replace(xml_utils.replace(String.valueOf(strSubstring.trim()) + "\n" + tagContent.trim() + "\n" + str3.trim(), "'", "\""), "><", ">\n<");
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(strReplace.trim());
        int i2 = 0;
        while (true) {
            int iIndexOf = strReplace.indexOf("<", i2);
            if (iIndexOf != -1) {
                int i3 = iIndexOf + 1;
                if (strReplace.charAt(i3) == "/".charAt(i)) {
                    i3++;
                }
                int nearestScobe = getNearestScobe(strReplace, i3);
                int iIndexOf2 = strReplace.indexOf(">", i3);
                if (iIndexOf2 > nearestScobe) {
                    int iIndexOf3 = nearestScobe;
                    do {
                        int iIndexOf4 = strReplace.indexOf("=\"", iIndexOf3);
                        if (iIndexOf4 == -1 || iIndexOf4 > iIndexOf2) {
                            break;
                        }
                        spannableStringBuilder.setSpan(new StyleSpan(12, -16767616, true), iIndexOf3, iIndexOf4 + 1, 33);
                        int i4 = iIndexOf4 + 2;
                        iIndexOf3 = strReplace.indexOf("\"", i4);
                        spannableStringBuilder.setSpan(new StyleSpan(12, -16755456, true), i4, iIndexOf3, 33);
                        if (iIndexOf3 == -1) {
                            break;
                        }
                    } while (iIndexOf3 <= iIndexOf2);
                }
                if (nearestScobe == -1) {
                    break;
                }
                spannableStringBuilder.setSpan(new StyleSpan(12, -8388608, true), i3, nearestScobe, 33);
                i2 = nearestScobe;
                i = 0;
            } else {
                break;
            }
        }
        return spannableStringBuilder;
    }

    private static final int getNearestScobe(String str, int i) {
        int length = str.length();
        int iIndexOf = str.indexOf("/>", i);
        int iIndexOf2 = str.indexOf(">", i);
        int iIndexOf3 = str.indexOf(" ", i);
        if (iIndexOf != -1 && iIndexOf < length) {
            length = iIndexOf;
        }
        if (iIndexOf2 == -1 || iIndexOf2 >= length) {
            iIndexOf2 = length;
        }
        return (iIndexOf3 == -1 || iIndexOf3 >= iIndexOf2) ? iIndexOf2 : iIndexOf3;
    }

    private static final String getTagContent(String str, String str2, String str3) {
        if (str.length() <= (str2.length() * 2) + 5 || !str.endsWith("</" + str2 + ">")) {
            return null;
        }
        return str.substring(str3.length(), str.length() - (str2.length() + 3));
    }
}
