package ru.ivansuper.jasmin;

import android.util.Log;
import ru.ivansuper.jasmin.Service.jasminSvc;

public class LogW {
    /** @noinspection unused*/
    public static void trw(String TAG, Throwable trw) {
        jasminSvc svc = resources.service;
        if (svc != null) {
            svc.put_log("\n***********\n" + utilities.getStackTraceString(trw) + "\n***********");
        }
    }

    public static void e(String TAG, String message) {
        Log.e(TAG, message);
    }

    public static void v(String TAG, String message) {
        Log.v(TAG, message);
    }

    public static void i(String TAG, String message) {
        Log.i(TAG, message);
    }

    public static void d(String TAG, String message) {
        Log.d(TAG, message);
    }
}