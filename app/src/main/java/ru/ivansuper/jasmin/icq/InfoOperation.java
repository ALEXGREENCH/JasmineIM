package ru.ivansuper.jasmin.icq;

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
        //noinspection UnusedAssignment
        this.uin = "";
        //noinspection UnusedAssignment
        this.type = -1;
        //noinspection UnusedAssignment
        this.id = 0;
        this.uin = uin;
        this.type = type;
        this.id = id;
    }
}