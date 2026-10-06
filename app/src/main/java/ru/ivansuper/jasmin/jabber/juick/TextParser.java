package ru.ivansuper.jasmin.jabber.juick;

import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.StyleSpan;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.jabber.xmpp_utils;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.ui.MyTextView;
import ru.ivansuper.jasmin.utilities;

public class TextParser {
    private static final int STATE_MSG_ID = 3;
    private static final int STATE_SPACE = 1;
    private static final int STATE_TEXT = 0;
    private static final int STATE_USER = 2;
    private OnIDClickedListener listener;
    private MyTextView tv;

    public interface OnIDClickedListener {
        void OnMessageClicked(String str);

        void OnUserClicked(String str);
    }

    private TextParser(OnIDClickedListener onIDClickedListener, MyTextView myTextView) {
        this.listener = onIDClickedListener;
        this.tv = myTextView;
    }

    private final int getDogsCount(String str) {
        int i = 0;
        int i2 = 0;
        while (true) {
            int iIndexOf = str.indexOf("@", i);
            if (iIndexOf == -1) {
                return i2;
            }
            i = iIndexOf + 1;
            i2++;
        }
    }

    public static final TextParser getInstance(OnIDClickedListener onIDClickedListener, MyTextView myTextView) {
        TextParser textParser;
        synchronized (TextParser.class) {
            try {
                textParser = new TextParser(onIDClickedListener, myTextView);
            } catch (Throwable th) {
                throw th;
            }
        }
        return textParser;
    }

    private final boolean isLatinOrDigit(char c) {
        return utilities.latin_chars.indexOf(c) >= 0;
    }

    private final boolean isTab(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f' || c == '\b';
    }

    private final void proceedMessage(int i, int i2, String str, SpannableStringBuilder spannableStringBuilder) {
        spannableStringBuilder.setSpan(new StyleSpan(-1, ColorScheme.getColor(51), true), i, i2, 33);
        spannableStringBuilder.setSpan(new JuickIDSpan("#" + str, this.listener, JuickIDSpan.Type.Message), i, i2, 33);
        spannableStringBuilder.insert(i2, "  ");
        int i3 = i2 + 1;
        int i4 = i2 + 2;
        spannableStringBuilder.setSpan(new ImageSpan(resources.link_icon), i3, i4, 33);
        spannableStringBuilder.setSpan(new URLSpan("http://juick.com/" + str.replaceAll("/", "#")), i3, i4, 33);
    }

    private final void proceedUser(int i, int i2, String str, SpannableStringBuilder spannableStringBuilder) {
        spannableStringBuilder.setSpan(new StyleSpan(-1, ColorScheme.getColor(50), true), i, i2, 33);
        spannableStringBuilder.setSpan(new JuickIDSpan("@" + str, this.listener, JuickIDSpan.Type.User), i, i2, 33);
        spannableStringBuilder.insert(i2, "  ");
        int i3 = i2 + 1;
        int i4 = i2 + 2;
        spannableStringBuilder.setSpan(new ImageSpan(resources.link_icon), i3, i4, 33);
        spannableStringBuilder.setSpan(new URLSpan("http://juick.com/" + str), i3, i4, 33);
    }

    private final void removeNearestMessageLink(int i, String str, SpannableStringBuilder spannableStringBuilder) {
        String string = spannableStringBuilder.toString();
        String str2 = "http://juick.com/" + str.replaceAll("/", "#");
        int iIndexOf = string.indexOf(str2);
        if (iIndexOf >= i + ContactListActivity.UPDATE_BLINK_STATE || iIndexOf < 0) {
            return;
        }
        spannableStringBuilder.delete(iIndexOf, str2.length() + iIndexOf);
    }

    private final boolean verifyMessage(StringBuilder sb) {
        int length = sb.length();
        if (length > 24 || length < 2) {
            return false;
        }
        for (int i = 0; i < sb.length(); i++) {
            char cCharAt = sb.charAt(i);
            if (!Character.isDigit(cCharAt) && cCharAt != '/') {
                return false;
            }
        }
        return true;
    }

    private final boolean verifyUser(StringBuilder sb) {
        int length = sb.length();
        return length <= 64 && length >= 2 && getDogsCount(sb.toString()) <= 1;
    }

    public final SpannableStringBuilder parse(SpannableStringBuilder spannableStringBuilder) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int i2 = 0;
        char c = 1;
        while (i2 < spannableStringBuilder.length() + 1) {
            char cCharAt = i2 != spannableStringBuilder.length() ? spannableStringBuilder.charAt(i2) : ' ';
            switch (c) {
                case 0:
                    if (isTab(cCharAt)) {
                        c = 1;
                    }
                    break;
                case 1:
                    if (cCharAt == '@') {
                        c = 2;
                    } else if (cCharAt == '#') {
                        c = 3;
                    } else if (!isTab(cCharAt)) {
                        c = 0;
                    }
                    i = i2;
                    break;
                case 2:
                    if (!isTab(cCharAt) && xmpp_utils.isJIDSymbolAllowedWODog(cCharAt)) {
                        sb.append(cCharAt);
                    } else {
                        if (verifyUser(sb)) {
                            proceedUser(i, i2, sb.toString(), spannableStringBuilder);
                            Log.e("TextParser[" + i + ":" + i2 + "]", "User: '@" + sb.toString() + "'");
                        }
                        sb.setLength(0);
                        c = 1;
                    }
                    break;
                case 3:
                    if (Character.isDigit(cCharAt) || cCharAt == '/') {
                        sb.append(cCharAt);
                    } else {
                        if (verifyMessage(sb)) {
                            String string = sb.toString();
                            proceedMessage(i, i2, string, spannableStringBuilder);
                            removeNearestMessageLink(i2, string, spannableStringBuilder);
                            Log.e("TextParser[" + i + ":" + i2 + "]", "MessageID: '#" + string + "'");
                        }
                        sb.setLength(0);
                        c = 1;
                    }
                    break;
            }
            i2++;
        }
        return spannableStringBuilder;
    }

    public final SpannableStringBuilder parse(String str) {
        return parse(new SpannableStringBuilder(str));
    }
}
