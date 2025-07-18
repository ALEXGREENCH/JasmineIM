package ru.ivansuper.jasmin.Preferences;

import android.annotation.SuppressLint;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

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