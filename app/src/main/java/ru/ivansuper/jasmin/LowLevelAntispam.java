package ru.ivansuper.jasmin;

import android.text.Spannable;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;

/**
 * The LowLevelAntispam class provides methods to check if a given text message is likely to be spam.
 * It uses a set of rules to determine if a message is spam, including checking for incorrect words,
 * character count limits, links, long words, abnormal word/space ratios, and excessive use of
 * uppercase letters or non-letter characters.
 *
 * The `proceedMessage` method takes a text message and a depth level as input. The depth level
 * determines how many rules are applied to the message. A higher depth level means more rules are
 * applied, making the spam detection more strict.
 *
 * The class uses a predefined list of incorrect words to check for offensive language. It also
 * uses Android's Linkify class to detect links in the message.
 *
 * The class logs messages to the Android Logcat system when it detects potential spam.
 */
public class LowLevelAntispam {
    private static final String[] incorrect_words = {"пизд", "хуй", "хуи", "хуе", "хуё", "бляд", "еба", "ёба", "еби", "ёби", "пёзд", "fuck", "bastard"};

    private static boolean checkIncorrectWords(String text) {
        String temporary = new String(text.getBytes()).toLowerCase();
        for (String word : incorrect_words) {
            if (temporary.contains(word)) {
                return false;
            }
        }
        return true;
    }

    public static boolean proceedMessage(String text, int depth) {
        if (depth <= 0) {
            return true;
        }
        int length = text.length();
        if (length > 160) {
            Log.e("LowLevelAntispam", "Characters count limit (max: 160)");
            return false;
        }
        if (!checkIncorrectWords(text)) {
            Log.e("LowLevelAntispam", "Incorrect words found");
            return false;
        }
        Spannable s_text = Spannable.Factory.getInstance().newSpannable(text);
        Linkify.addLinks(s_text, Linkify.WEB_URLS);
        URLSpan[] spans = s_text.getSpans(0, s_text.length(), URLSpan.class);
        int links = spans.length;
        if (links > 0) {
            Log.e("LowLevelAntispam", "Links found");
            return false;
        }
        if (text.contains("http://")) {
            Log.e("LowLevelAntispam", "Links found");
            return false;
        }
        if (depth == 1) {
            return true;
        }
        int words = utilities.split(text, " ").length;
        if (length > 24 && words == 1) {
            Log.e("LowLevelAntispam", "Longword");
            return false;
        }
        int spaces_percent = ((words - 1) * 100) / length;
        if ((spaces_percent < 5 || spaces_percent > 30) && words > 1) {
            Log.e("LowLevelAntispam", "Not normal words/spaces factor (words: " + words + "; spaces_percent: " + spaces_percent + ")");
            return false;
        }
        if (depth == 2) {
            return true;
        }
        int lower = 0;
        int upper = 0;
        int not_letters = 0;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (!Character.isLetter(c)) {
                not_letters++;
            } else if (Character.isLowerCase(c)) {
                lower++;
            } else {
                upper++;
            }
        }
        if (lower == 0) {
            lower = 1;
        }
        float factor = (float) upper / lower;
        if (factor >= 0.3f) {
            Log.e("LowLevelAntispam", "Multicase");
            return false;
        }
        if (depth == 3) {
            return true;
        }
        float factor2 = (float) not_letters / length;
        if (factor2 < 0.5f || length <= 10) {
            //noinspection ConditionalExpressionWithIdenticalBranches
            return depth == 4 ? true : true;
        }
        Log.e("LowLevelAntispam", "Too many symbols");
        return false;
    }
}
