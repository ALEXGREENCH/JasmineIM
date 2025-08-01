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

/**
 * The debug class provides a global uncaught exception handler for the application.
 * When an unhandled exception occurs, this class logs the exception details and
 * relevant system information to a file on the SD card (if available).
 * This helps in debugging and identifying issues in the application.
 *
 * <p>The class also creates a marker file ("ForceClosed.marker") to indicate
 * that the application was force-closed due to an unhandled exception.
 * This marker file can be checked on application startup to perform any
 * necessary cleanup or recovery operations.
 *
 * <p>To use this class, call the {@link #init()} method once during application
 * initialization, typically in the {@code onCreate()} method of the Application class.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class MyApplication extends Application {
 *     @Override
 *     public void onCreate() {
 *         super.onCreate();
 *         debug.init();
 *     }
 * }
 * }</pre>
 *
 * <p>The stack dump file includes the following information:
 * <ul>
 *     <li>Device information (Board, Brand, Fingerprint, ID, Manufacturer, Model, Product, Tags, Type, User)
 *     <li>OS version (SDK integer and release string)
 *     <li>Available memory/Heap size
 *     <li>Used memory
 *     <li>Jasmine IM version
 *     <li>The stack trace of the unhandled exception
 * </ul>
 *
 * <p><b>Note:</b> This class requires the {@code android.permission.WRITE_EXTERNAL_STORAGE}
 * permission to save the stack dump file to the SD card.
 */
public class debug {
    public static boolean initialized;

    public static void init() {
        exception_hdl hdl = new exception_hdl();
        Thread.setDefaultUncaughtExceptionHandler(hdl);
    }

    private static class exception_hdl implements Thread.UncaughtExceptionHandler {

        /** @noinspection NullableProblems*/
        @SuppressLint("LongLogTag")
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            try {
                Log.e("JasmineIM:stack_dump", "Exception handled! Saving stack trace ...");
                Log.e("JasmineIM:stack_dump:trace", "Details", ex);
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
            if (resources.sd_mounted()) {
                String unique_id = String.valueOf(System.currentTimeMillis());
                File dump = new File(resources.JASMINE_LOG_PATH + "stack_trace_" + unique_id.substring(unique_id.length() - 7) + ".st");
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
                File marker = new File(resources.JASMINE_LOG_PATH + "ForceClosed.marker");
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