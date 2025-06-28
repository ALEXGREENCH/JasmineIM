package ru.ivansuper.jasmin;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MediaTable {
    public static String auth_accepted;
    public static boolean auth_accepted_e;
    public static String auth_denied;
    public static boolean auth_denied_e;
    public static String auth_req;
    public static boolean auth_req_e;
    public static String contact_in;
    public static boolean contact_in_e;
    public static String contact_out;
    public static boolean contact_out_e;
    public static String inc_file;
    public static boolean inc_file_e;
    public static String inc_msg;
    public static boolean inc_msg_e;
    /** @noinspection FieldCanBeLocal*/
    private static boolean initialized = false;
    public static String out_msg;
    public static boolean out_msg_e;
    public static String transfer_rejected;
    public static boolean transfer_rejected_e;

    public static void init() {
        if (!initialized) {
            forceUpdate();
        }
    }

    public static void forceUpdate() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(resources.ctx);
        auth_accepted = sp.getString("aa_snd", "$*INTERNAL*$");
        auth_denied = sp.getString("ad_snd", "$*INTERNAL*$");
        auth_req = sp.getString("ar_snd", "$*INTERNAL*$");
        contact_in = sp.getString("ci_snd", "$*INTERNAL*$");
        contact_out = sp.getString("co_snd", "$*INTERNAL*$");
        inc_file = sp.getString("if_snd", "$*INTERNAL*$");
        inc_msg = sp.getString("im_snd", "$*INTERNAL*$");
        out_msg = sp.getString("om_snd", "$*INTERNAL*$");
        transfer_rejected = sp.getString("tr_snd", "$*INTERNAL*$");
        auth_accepted_e = sp.getBoolean("aa_snd_e", true);
        auth_denied_e = sp.getBoolean("ad_snd_e", true);
        auth_req_e = sp.getBoolean("ar_snd_e", true);
        contact_in_e = sp.getBoolean("ci_snd_e", true);
        contact_out_e = sp.getBoolean("co_snd_e", true);
        inc_file_e = sp.getBoolean("if_snd_e", true);
        inc_msg_e = sp.getBoolean("im_snd_e", true);
        out_msg_e = sp.getBoolean("om_snd_e", true);
        transfer_rejected_e = sp.getBoolean("tr_snd_e", true);
    }
}
