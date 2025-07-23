package ru.ivansuper.jasmin.icq;

/**
 * Represents an operation to retrieve or update information for an ICQ contact.
 *
 * <p>This class encapsulates the details of an information-related operation,
 * including the contact's UIN, the type of operation, and an identifier.
 *
 * <p>The class defines several constants to represent different types of
 * information operations:
 * <ul>
 *   <li>{@link #INFO_FOR_DISPLAY}: Retrieve information for display.
 *   <li>{@link #INFO_FOR_DISPLAY_IN_SEARCH}: Retrieve information for display in search results.
 *   <li>{@link #NOT_IN_LIST_REFRESH}: Refresh information for contacts not in the current list.
 *   <li>{@link #PROFILE_UPDATE}: Update the contact's profile information.
 *   <li>{@link #UPDATE_CONTACT}: Update the contact's information.
 * </ul>
 */
public class InfoOperation {
    /** @noinspection unused*/
    public static final int INFO_FOR_DISPLAY = 0;
    /** @noinspection unused*/
    public static final int INFO_FOR_DISPLAY_IN_SEARCH = 4;
    /** @noinspection unused*/
    public static final int NOT_IN_LIST_REFRESH = 1;
    /** @noinspection unused*/
    public static final int PROFILE_UPDATE = 2;
    /** @noinspection unused*/
    public static final int UPDATE_CONTACT = 3;
    public int id;
    public int type;
    public String uin;

    public InfoOperation(String uin, int type, int id) {
        this.uin = "";
        this.type = -1;
        this.id = 0;
        this.uin = uin;
        this.type = type;
        this.id = id;
    }
}