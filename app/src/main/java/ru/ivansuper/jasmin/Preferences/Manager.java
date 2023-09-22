package ru.ivansuper.jasmin.Preferences;

import android.preference.PreferenceManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/* loaded from: classes.dex */
public class Manager {
    public static final void checkFirstStartAndReset() {
        File marker = new File(String.valueOf(utilities.normalizePath(resources.dataPath)) + "settings_initialized");
        if (!marker.exists()) {
            try {
                marker.createNewFile();
            } catch (IOException e) {
            }
            String dir = String.valueOf(utilities.normalizePath(resources.dataPath)) + "shared_prefs";
            File d = new File(dir);
            d.mkdirs();
            String dest = String.valueOf(utilities.normalizePath(resources.dataPath)) + "shared_prefs/ru.ivansuper.jasmin_preferences.xml";
            Log.e("Manager", dest);
            File f = new File(dest);
            if (f.exists()) {
                f.delete();
            }
            resources.copyAssetToSD("defaults/prefs_map.xml", dest);
        }
    }

    public static final synchronized void putString(String key, String value) {
        synchronized (Manager.class) {
            PreferenceManager.getDefaultSharedPreferences(resources.ctx).edit().putString(key, value).commit();
        }
    }

    public static final synchronized void putInt(String key, int value) {
        synchronized (Manager.class) {
            PreferenceManager.getDefaultSharedPreferences(resources.ctx).edit().putInt(key, value).commit();
        }
    }

    public static final synchronized void putBoolean(String key, boolean value) {
        synchronized (Manager.class) {
            PreferenceManager.getDefaultSharedPreferences(resources.ctx).edit().putBoolean(key, value).commit();
        }
    }

    public static final synchronized String getString(String key) {
        String string;
        synchronized (Manager.class) {
            string = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getString(key, "");
        }
        return string;
    }

    public static final synchronized int getInt(String key) {
        int i;
        synchronized (Manager.class) {
            i = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getInt(key, 0);
        }
        return i;
    }

    public static final synchronized int getInt(String key, int default_) {
        int i;
        synchronized (Manager.class) {
            i = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getInt(key, default_);
        }
        return i;
    }

    public static final synchronized boolean getBoolean(String key) {
        boolean z;
        synchronized (Manager.class) {
            z = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getBoolean(key, false);
        }
        return z;
    }

    public static final synchronized boolean getBoolean(String key, boolean default_) {
        boolean z;
        synchronized (Manager.class) {
            z = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getBoolean(key, default_);
        }
        return z;
    }

    public static final synchronized int getStringInt(String key) {
        int val_;
        synchronized (Manager.class) {
            String value = PreferenceManager.getDefaultSharedPreferences(resources.ctx).getString(key, "");
            val_ = 0;
            try {
                val_ = Integer.parseInt(value);
            } catch (Exception e) {
            }
        }
        return val_;
    }
}