package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Process;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class debug {
    public static boolean initialized;

    public static void init() {
        exception_hdl hdl = new exception_hdl();
        Thread.setDefaultUncaughtExceptionHandler(hdl);
    }

    private static class exception_hdl implements Thread.UncaughtExceptionHandler {

        /** @noinspection NullableProblems*/
        @SuppressLint("LongLogTag")
        public void uncaughtException(Thread thread, Throwable throwable) {
            try {
                Log.e("JasmineIM:stack_dump", "Exception handled! Saving stack trace ...");
                Log.e("JasmineIM:stack_dump:trace", "Details", throwable);
                if (!resources.sd_mounted()) {
                    return;
                }

                String currentTimeMillisString = String.valueOf(System.currentTimeMillis());
                currentTimeMillisString = currentTimeMillisString.substring(currentTimeMillisString.length() - 7);
                File file1 = new File(resources.dataPath + "stack_trace_" + currentTimeMillisString + ".st");
                if (!file1.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file1.createNewFile();
                }

                FileOutputStream fileOutputStream = new FileOutputStream(file1);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                PrintStream printStream = new PrintStream(bufferedOutputStream);
                printStream.print("=== Jasmine Stack Dump File ===\n");
                StringBuilder stringBuilder = new StringBuilder("BOARD: ");
                printStream.print(stringBuilder.append(Build.BOARD).append("\n"));
                stringBuilder = new StringBuilder("BRAND: ");
                printStream.print(stringBuilder.append(Build.BRAND).append("\n"));
                stringBuilder = new StringBuilder("FINGERPRINT: ");
                printStream.print(stringBuilder.append(Build.FINGERPRINT).append("\n"));
                stringBuilder = new StringBuilder("ID: ");
                printStream.print(stringBuilder.append(Build.ID).append("\n"));
                stringBuilder = new StringBuilder("MANUFACTURER: ");
                printStream.print(stringBuilder.append(Build.MANUFACTURER).append("\n"));
                stringBuilder = new StringBuilder("MODEL: ");
                printStream.print(stringBuilder.append(Build.MODEL).append("\n"));
                stringBuilder = new StringBuilder("PRODUCT: ");
                printStream.print(stringBuilder.append(Build.PRODUCT).append("\n"));
                stringBuilder = new StringBuilder("TAGS: ");
                printStream.print(stringBuilder.append(Build.TAGS).append("\n"));
                stringBuilder = new StringBuilder("TYPE: ");
                printStream.print(stringBuilder.append(Build.TYPE).append("\n"));
                stringBuilder = new StringBuilder("USER: ");
                printStream.print(stringBuilder.append(Build.USER).append("\n\n"));
                stringBuilder = new StringBuilder("OS Version: ");
                printStream.print(stringBuilder.append(VERSION.SDK_INT).append(" ").append(VERSION.RELEASE).append("\n\n"));
                stringBuilder = new StringBuilder("Available memory/Heap size: ");
                printStream.print(stringBuilder.append(resources.DEVICE_HEAP_SIZE).append(" MB\n\n"));
                stringBuilder = new StringBuilder("Used memory: ");
                printStream.print(stringBuilder.append(resources.DEVICE_HEAP_USED_SIZE).append(" MB\n\n"));
                stringBuilder = new StringBuilder("Jasmine IM Version: ");
                printStream.print(stringBuilder.append(resources.VERSION));
                throwable.printStackTrace(printStream);
                printStream.close();
                File file2 = new File(resources.dataPath + "ForceClosed.marker");
                //noinspection ResultOfMethodCallIgnored
                file2.createNewFile();
            } catch (Exception var6) {
                //noinspection CallToPrintStackTrace
                var6.printStackTrace();
            }

            Process.killProcess(Process.myPid());
        }
    }
}