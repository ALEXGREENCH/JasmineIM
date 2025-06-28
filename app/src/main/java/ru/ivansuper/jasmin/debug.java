package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class debug {
    public static boolean initialized;

    public static void init() {
        exception_hdl hdl = new exception_hdl();
        Thread.setDefaultUncaughtExceptionHandler(hdl);
    }

    private static class exception_hdl implements Thread.UncaughtExceptionHandler {

        @SuppressLint("LongLogTag")
        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable ex) {
            try {
                Log.e("JasmineIM:stack_dump", "Exception handled! Saving stack trace ...");
                Log.e("JasmineIM:stack_dump:trace", "Details", ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (resources.sd_mounted()) {
                String unique_id = String.valueOf(System.currentTimeMillis());
                File dump = new File(resources.dataPath + "stack_trace_" + unique_id.substring(unique_id.length() - 7) + ".st");
                if (!dump.exists()) {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        dump.createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                PrintStream out;
                try {
                    out = new PrintStream(new BufferedOutputStream(new FileOutputStream(dump)));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                out.print("=== Jasmine Stack Dump File ===\n");
                out.print("BOARD: " + Build.BOARD + "\n");
                out.print("BRAND: " + Build.BRAND + "\n");
                out.print("FINGERPRINT: " + Build.FINGERPRINT + "\n");
                out.print("ID: " + Build.ID + "\n");
                out.print("MANUFACTURER: " + Build.MANUFACTURER + "\n");
                out.print("MODEL: " + Build.MODEL + "\n");
                out.print("PRODUCT: " + Build.PRODUCT + "\n");
                out.print("TAGS: " + Build.TAGS + "\n");
                out.print("TYPE: " + Build.TYPE + "\n");
                out.print("USER: " + Build.USER + "\n\n");
                out.print("OS Version: " + Build.VERSION.SDK_INT + " " + Build.VERSION.RELEASE + "\n\n");
                out.print("Available memory/Heap size: " + resources.DEVICE_HEAP_SIZE + " MB\n\n");
                out.print("Used memory: " + resources.DEVICE_HEAP_USED_SIZE + " MB\n\n");
                out.print("Jasmine IM Version: " + resources.VERSION);
                ex.printStackTrace(out);
                out.close();
                File marker = new File(resources.dataPath + "ForceClosed.marker");
                try {
                    //noinspection ResultOfMethodCallIgnored
                    marker.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Process.killProcess(Process.myPid());
            }
        }
    }
}