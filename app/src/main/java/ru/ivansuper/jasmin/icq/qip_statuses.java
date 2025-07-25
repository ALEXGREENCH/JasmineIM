package ru.ivansuper.jasmin.icq;

import android.graphics.drawable.Drawable;

import ru.ivansuper.jasmin.resources;

public class qip_statuses {
    public static String[] guids = {"B7074378F50C777797775778502D0570", "B7074378F50C777797775778502D0575", "B7074378F50C777797775778502D0576", "B7074378F50C777797775778502D0577", "B7074378F50C777797775778502D0578", "B7074378F50C777797775778502D0579"};
    /** @noinspection unused*/
    public static int[] ints = {16384, 32, 20480, 24576, 8193, 12288};
    public static String[] names = {"[QIP_Depression]", "[QIP_FreeForChat]", "[QIP_AtHome]", "[QIP_AtWork]", "[QIP_Lunch]", "[QIP_Evil]"};
    public static int[] ids = {3, 1, 4, 5, 6, 2};
    public static Drawable[] icons = {resources.depress, resources.chat, resources.home, resources.work, resources.eat, resources.evil};

    /** @noinspection unused*/
    public static String translate(String guid) {
        for (int i = 0; i < guids.length; i++) {
            if (guid.toUpperCase().equals(guids[i])) {
                return names[i];
            }
        }
        return guid;
    }

    public static Drawable getIcon(String guid) {
        if (guid == null) {
            return null;
        }
        for (int i = 0; i < guids.length; i++) {
            if (guid.toUpperCase().equals(guids[i])) {
                return icons[i];
            }
        }
        return null;
    }

    public static int getId(String guid) {
        if (guid == null) {
            return 0;
        }
        for (int i = 0; i < ids.length; i++) {
            if (guid.toUpperCase().equals(guids[i])) {
                return ids[i];
            }
        }
        return 0;
    }

    public static String toGuid(int status) {
        switch (status) {
            case 32:
                return guids[1];
            case 8193:
                return guids[4];
            case 12288:
                return guids[5];
            case 16384:
                return guids[0];
            case 20480:
                return guids[2];
            case 24576:
                return guids[3];
            default:
                return null;
        }
    }

    public static int fromGuid(String guid) {
        int res = 0;
        if (guid.toUpperCase().equals(guids[1])) {
            res = 32;
        }
        if (guid.toUpperCase().equals(guids[4])) {
            res = 8193;
        }
        if (guid.toUpperCase().equals(guids[5])) {
            res = 12288;
        }
        if (guid.toUpperCase().equals(guids[0])) {
            res = 16384;
        }
        if (guid.toUpperCase().equals(guids[2])) {
            res = 20480;
        }
        if (guid.toUpperCase().equals(guids[3])) {
            return 24576;
        }
        return res;
    }
}