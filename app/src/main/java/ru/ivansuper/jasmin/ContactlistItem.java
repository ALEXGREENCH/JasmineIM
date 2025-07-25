package ru.ivansuper.jasmin;

/**
 * Represents an abstract item in a contact list.
 * This class provides a common base for different types of contact list entries,
 * such as individual contacts, groups, or special items like splitters.
 * It implements {@link Comparable} to allow sorting of contact list items,
 * primarily based on their names.
 *
 * <p>The class defines several constants to represent different item types:
 * <ul>
 *   <li>{@link #CONTACT}
 *   <li>{@link #GROUP}
 *   <li>{@link #PROFILE_GROUP}
 *   <li>{@link #JABBER_CONTACT}
 *   <li>{@link #JABBER_PROFILE_GROUP}
 *   <li>{@link #JABBER_GROUP}
 *   <li>{@link #MMP_CONTACT}
 *   <li>{@link #MMP_PROFILE_GROUP}
 *   <li>{@link #MMP_GROUP}
 *   <li>{@link #JABBER_CONFERENCE}
 *   <li>{@link #SPLITTER}
 * </ul>
 *
 * <p>Each item has an {@code itemType}, a flag {@code presence_initialized} to indicate
 * if presence information has been loaded, a {@code presense_timestamp} often used for
 * visual cues like blinking, a unique {@code ID}, and a display {@code name}.
 *
 * <p>The {@link #compareTo(ContactlistItem)} method provides a custom sorting logic
 * that considers character order based on a predefined character set (presumably in
 * {@code utilities.chars}) and then by string length.
 *
 * <p>The {@link #requestBlink()} and {@link #resetBlink()} methods manage the
 * {@code presense_timestamp}, typically used to indicate activity or new events
 * related to the contact item.
 */
public abstract class ContactlistItem implements Comparable<ContactlistItem> {

    public static final int CONTACT = 1;
    public static final int GROUP = 2;
    public static final int PROFILE_GROUP = 3;
    public static final int JABBER_CONTACT = 4;
    public static final int JABBER_PROFILE_GROUP = 5;
    public static final int JABBER_GROUP = 6;
    public static final int MMP_CONTACT = 7;
    public static final int MMP_PROFILE_GROUP = 8;
    public static final int MMP_GROUP = 9;
    public static final int JABBER_CONFERENCE = 10;
    public static final int SPLITTER = 11;

    public int itemType;
    public boolean presence_initialized;
    public long presense_timestamp;
    public String ID = "";
    public String name = "";

    public int getHash() {
        return this.ID.hashCode();
    }

    /** @noinspection unused*/
    public void update(ContactlistItem item) {

    }

    @Override
    public final int compareTo(ContactlistItem contact) {
        try {
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

            String nameA = this.name;
            String nameB = contact.name;

            int minLen = Math.min(nameA.length(), nameB.length());
            int lvl = 0;

            while (lvl < minLen) {
                int a = utilities.chars.indexOf(nameA.charAt(lvl));
                int b = utilities.chars.indexOf(nameB.charAt(lvl));

                a = (a >= 0 ? a : nameA.charAt(lvl)) + 256;
                b = (b >= 0 ? b : nameB.charAt(lvl)) + 256;

                if (a == b) {
                    lvl++;
                } else {
                    return IntegerCompat.compare(a, b);
                }
            }

            int lenA = nameA.length();
            int lenB = nameB.length();
            return IntegerCompat.compare(lenA, lenB);

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return 0;
        }
    }

    public final void requestBlink() {
        this.presense_timestamp = System.currentTimeMillis();
    }

    public final void resetBlink() {
        this.presense_timestamp = 0L;
    }
}
