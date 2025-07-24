package ru.ivansuper.jasmin.Preferences;

import android.annotation.SuppressLint;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * The Manager class provides a set of static methods for managing application preferences.
 * It handles the initialization of settings on the first start, and provides methods
 * for storing and retrieving various data types (String, int, boolean) from SharedPreferences.
 * All methods are synchronized to ensure thread safety when accessing preferences.
 */
public class Manager {
    public static void checkFirstStartAndReset() {
        File marker = new File(utilities.normalizePath(resources.dataPath) + "settings_initialized");
        if (!marker.exists()) {
            //noinspection CatchMayIgnoreException
            try {
                //noinspection ResultOfMethodCallIgnored
                marker.createNewFile();
            } catch (IOException e) {
            }
            String dir = utilities.normalizePath(resources.dataPath) + "shared_prefs";
            File d = new File(dir);
            //noinspection ResultOfMethodCallIgnored
            d.mkdirs();
            String dest = utilities.normalizePath(resources.dataPath) + "shared_prefs/ru.ivansuper.jasmin_preferences.xml";
            Log.e("Manager", dest);
            File f = new File(dest);
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            resources.copyAssetToSD("defaults/prefs_map.xml", dest);

            // Apply density-based defaults for fonts and smileys
            DisplayMetrics metrics = resources.ctx.getResources().getDisplayMetrics();
            int density = metrics.densityDpi;
            int fontSize;
            int smileScale;
            int avatarSize;
            if (density >= DisplayMetrics.DENSITY_XXHIGH) {
                fontSize = 24;
                smileScale = 320;
                avatarSize = 48;
            } else if (density >= DisplayMetrics.DENSITY_XHIGH) {
                fontSize = 20;
                smileScale = 240;
                avatarSize = 44;
            } else if (density >= DisplayMetrics.DENSITY_HIGH) {
                fontSize = 18;
                smileScale = 180;
                avatarSize = 40;
            } else {
                fontSize = 15;
                smileScale = 160;
                avatarSize = 36;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(resources.ctx);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ms_cl_font_size", String.valueOf(fontSize));
            editor.putString("ms_chat_text_size", String.valueOf(fontSize));
            editor.putString("ms_chat_time_size", String.valueOf(fontSize));
            editor.putString("ms_smileys_scale", String.valueOf(smileScale));
            editor.putString("ms_cl_avatar_size", String.valueOf(avatarSize));
            editor.putString("ms_ui_font_scale", "100");
            editor.apply();
        }
    }

    @SuppressLint("ApplySharedPref")
    public static synchronized void putString(String key, String value) {
        synchronized (Manager.class) {
            //noinspection deprecation
            PreferenceManager.getDefaultSharedPreferences(resources.ctx).edit().putString(key, value).commit();
        }
    }

    @SuppressLint("ApplySharedPref")
    public static synchronized void putInt(String key, int value) {
        synchronized (Manager.class) {
            //noinspection deprecation
            PreferenceManager.getDefaultSharedPreferences(resources.ctx).edit().putInt(key, value).commit();
        }
    }

    @SuppressLint("ApplySharedPref")
    public static synchronized void putBoolean(String key, boolean value) {
        synchronized (Manager.class) {
            //noinspection deprecation
            PreferenceManager.getDefaultSharedPreferences(resources.ctx).edit().putBoolean(key, value).commit();
        }
    }

    public static synchronized String getString(String key) {
        String string;
        synchronized (Manager.class) {
            //noinspection deprecation
            string = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getString(key, "");
        }
        return string;
    }

    public static synchronized int getInt(String key) {
        int i;
        synchronized (Manager.class) {
            //noinspection deprecation
            i = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getInt(key, 0);
        }
        return i;
    }

    public static synchronized int getInt(String key, int default_) {
        int i;
        synchronized (Manager.class) {
            //noinspection deprecation
            i = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getInt(key, default_);
        }
        return i;
    }

    public static synchronized boolean getBoolean(String key) {
        boolean z;
        synchronized (Manager.class) {
            //noinspection deprecation
            z = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getBoolean(key, false);
        }
        return z;
    }

    public static synchronized boolean getBoolean(String key, boolean default_) {
        boolean z;
        synchronized (Manager.class) {
            //noinspection deprecation
            z = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getBoolean(key, default_);
        }
        return z;
    }

    public static synchronized int getStringInt(String key) {
        int val_;
        synchronized (Manager.class) {
            //noinspection deprecation
            String value = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getString(key, "");
            val_ = 0;
            try {
                //noinspection DataFlowIssue
                val_ = Integer.parseInt(value);
            } catch (Exception ignored) {
            }
        }
        return val_;
    }
}