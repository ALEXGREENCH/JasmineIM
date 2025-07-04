package ru.ivansuper.jasmin.ui;

import java.util.Vector;

public class ExFragmentManager {
    private static final Vector<ExFragment> mList = new Vector<>();

    public static synchronized void addFragment(ExFragment fragment) {
        synchronized (ExFragmentManager.class) {
            if (!mList.contains(fragment)) {
                mList.add(fragment);
            }
        }
    }

    public static synchronized void removeFragment(ExFragment fragment) {
        synchronized (ExFragmentManager.class) {
            mList.remove(fragment);
        }
    }

    public static synchronized void executeEvent(ExRunnable event) {
        synchronized (ExFragmentManager.class) {
            event.setExFragment(null);
            for (ExFragment f : mList) {
                event.setExFragment(f);
                event.run();
            }
            event.setExFragment(null);
        }
    }

    public static abstract class ExRunnable implements Runnable {
        protected ExFragment fragment;

        public void setExFragment(ExFragment fragment) {
            this.fragment = fragment;
        }
    }
}