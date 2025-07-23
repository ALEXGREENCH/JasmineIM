package ru.ivansuper.jasmin.locale;

import android.preference.PreferenceManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * Provides access to localized strings and manages language settings.
 * This class handles loading language files, retrieving strings by key,
 * and determining the current language.
 */
public class Locale {
    public static int DEFAULT = 0;
    private static final HashMap<String, String> strings = new HashMap<>();

    /** @noinspection CallToPrintStackTrace*/
    public static void prepareLocale(BufferedReader reader) {
        int idx;
        try {
            strings.clear();
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith("s") && (idx = line.indexOf(":=")) > 3) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 2).trim();
                    strings.put(key, value);
                }
            }
        } catch (IOException e) {
            strings.clear();
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /**
     * Prepares the locale by loading the selected language.
     * It retrieves the currently selected language from shared preferences.
     * If the selected language index is invalid, it defaults to the first available language.
     * If the selected language is internal, it attempts to load it from the assets.
     * If loading the selected language fails or if it's not an internal language,
     * it falls back to loading the internal Russian (RU) localization.
     */
    public static void prepare() {
        //noinspection DataFlowIssue,deprecation
        int current = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(resources.ctx).getString("ms_select_language", "0"));
        ArrayList<Language> list = getAvailable();
        if (current > list.size()) {
            current = DEFAULT;
        }
        Language language = list.get(current);
        if (language.internal) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(resources.am.open(language.path)));
                prepareLocale(reader);
                return;
            } catch (IOException e) {
                prepareInternalRU();
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return;
            }
        }
        prepareInternalRU();
    }

    /**
     * Prepares the internal Russian locale.
     * This method attempts to load the Russian localization strings from the "locale/RU.txt" file.
     * If an {@link IOException} occurs during file reading, the stack trace is printed.
     */
    public static void prepareInternalRU() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resources.am.open("locale/RU.txt")));
            prepareLocale(reader);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of available languages.
     *
     * <p>This method returns a predefined list of languages that are supported by the application.
     * Each language is represented by a {@link Language} object, which contains information such as
     * the language name, localized name, author, path to the localization file, and whether it's
     * an internal resource.
     *
     * @return An {@link ArrayList} of {@link Language} objects representing the available languages.
     *         The list currently includes:
     *         <ul>
     *           <li>Russian localization
     *           <li>English localization
     *           <li>Ukrainian localization
     *         </ul>
     */
    public static ArrayList<Language> getAvailable() {
        ArrayList<Language> list = new ArrayList<>();
        Language language = new Language("Русская локализация", "Русский", "Ivansuper", "locale/RU.txt", true);
        list.add(language);
        Language language2 = new Language("English localization", "English", "Sergey Predko", "locale/EN.txt", true);
        list.add(language2);
        Language language3 = new Language("Українська локалізація", "Українська", "WJFSDisaster", "locale/UA.txt", true);
        list.add(language3);
        return list;
    }

    public static String getCurrentLangCode() {
        //noinspection DataFlowIssue,deprecation
        int current = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(resources.ctx).getString("ms_select_language", "0"));
        if (current == 0) {
            return "ru";
        }
        return "en";
    }

    public static String getString(String key) {
        String result = strings.get(key);
        if (result == null) {
            result = "null";
        }
        utilities.replace(result, "[NL]", "\n");
        return result;
    }
}