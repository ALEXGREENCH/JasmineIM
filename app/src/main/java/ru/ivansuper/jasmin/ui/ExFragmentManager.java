package ru.ivansuper.jasmin.ui;

import java.util.Vector;

/**
 * Manages a list of {@link ExFragment} instances and provides a mechanism to execute
 * an {@link ExRunnable} on each of them. This class is designed to handle events
 * or operations that need to be performed across multiple fragments.
 *
 * <p>All methods in this class are synchronized to ensure thread safety when
 * adding, removing, or executing events on fragments.
 */
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