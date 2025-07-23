package ru.ivansuper.jasmin.Clients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ru.ivansuper.jasmin.resources;

/**
 * Provides functionality for handling ICQ capability GUIDs.
 * This class includes methods for initializing a list of GUIDs from a resource file,
 * checking for short capabilities, converting short capabilities to strings, and
 * translating GUIDs to their descriptions.
 */
public class IcqCapsBase {
    private static final ArrayList<GUID> guids = new ArrayList<>();

    /**
     * Initializes the ICQ capabilities database.
     * Reads capability data from the "icq/caps" resource file.
     * Each line in the file represents a capability and should be tab-separated with the following format:
     * {@code <name>\t<guid>\t<description>}
     * The GUID will be converted to uppercase.
     * If an error occurs during initialization (e.g., file not found or format error),
     * the stack trace will be printed, and the capabilities list might be empty or incomplete.
     */
    public static void init() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(resources.am.open("icq/caps")));
            while (r.ready()) {
                String line = r.readLine();
                String[] params = line.split("\t");
                GUID guid = new GUID(null);
                guid.name = params[0];
                guid.guid = params[1].toUpperCase();
                guid.desc = params[2];
                guids.add(guid);
            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * Converts a short capability word (integer) into a full GUID string.
     *
     * <p>This method takes a short capability word (an integer), converts it to a 4-character
     * hexadecimal string (padded with leading zeros if necessary), and then searches for this
     * short capability within a predefined list of GUIDs. If a match is found where the short
     * capability appears at a specific position (index 4) within a GUID, that full GUID is returned.
     * Otherwise, if no match is found, null is returned.
     *
     * @param word The short capability word (integer) to be converted and looked up.
     * @return The full GUID string corresponding to the short capability, or null if not found.
     */
    public static String checkoutShortCapability(int word) {
        StringBuilder cap = new StringBuilder(Integer.toHexString(word).toUpperCase());
        int j = 4 - cap.length();
        for (int i = 0; i < j; i++) {
            cap.insert(0, "0");
        }
        for (GUID guid : guids) {
            int idx = guid.guid.indexOf(cap.toString());
            if (idx == 4) {
                return guid.guid;
            }
        }
        return null;
    }

    /**
     * Converts an integer word to a 4-character uppercase hexadecimal string,
     * padding with leading zeros if necessary.
     *
     * @param word The integer to convert.
     * @return The 4-character uppercase hexadecimal string representation of the word.
     * @noinspection unused
     */
    public static String shortCapability(int word) {
        StringBuilder cap = new StringBuilder(Integer.toHexString(word).toUpperCase());
        int j = 4 - cap.length();
        for (int i = 0; i < j; i++) {
            cap.insert(0, "0");
        }
        return cap.toString();
    }

    /**
     * Translates a GUID (Globally Unique Identifier) to its corresponding description.
     * <p>
     * This method searches for a GUID within a predefined list of GUIDs. If a match is found,
     * it returns the associated description. Otherwise, it returns the original GUID in uppercase.
     *
     * @param guid_ The GUID string to be translated.
     * @return The description of the GUID if found, or the original GUID in uppercase if not found.
     */
    public static String translateGuid(String guid_) {
        String guid_2 = guid_.toUpperCase();
        for (GUID guid : guids) {
            if (guid_2.contains(guid.guid)) {
                return guid.desc;
            }
        }
        return guid_2;
    }

    private static class GUID {
        public String desc;
        public String guid;
        public String name;

        private GUID() {
        }

        /** @noinspection unused, CopyConstructorMissesField */
        GUID(GUID guid) {
            this();
        }
    }
}