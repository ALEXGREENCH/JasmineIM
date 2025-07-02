package ru.ivansuper.jasmin.base;

import android.os.Process;

public class destroyer extends Thread {
    @Override
    public void run() {
        try {
            Thread.sleep(700L);
        } catch (InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        Process.sendSignal(Process.myPid(), 9);
    }
}
