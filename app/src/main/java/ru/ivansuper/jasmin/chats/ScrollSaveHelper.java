package ru.ivansuper.jasmin.chats;

import android.os.Parcelable;
import android.util.Log;

import java.util.Vector;

/**
 * Helper class for saving and restoring scroll positions of views.
 * This class uses a static Vector to store {@link ScrollState} objects,
 * allowing scroll positions to be persisted across activity/fragment lifecycles or configuration changes.
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * // To save a scroll state:
 * String uniqueId = "myListView"; // A unique identifier for the view
 * Parcelable scrollState = listView.onSaveInstanceState();
 * ScrollSaveHelper.putState(uniqueId, scrollState);
 *
 * // To restore a scroll state:
 * Parcelable savedState = ScrollSaveHelper.getState(uniqueId);
 * if (savedState != null) {
 *     listView.onRestoreInstanceState(savedState);
 * }
 * </pre>
 *
 * <p><b>Note:</b> This class uses synchronized methods to ensure thread safety when accessing the shared stack.
 * However, it's important to manage the lifecycle of the saved states appropriately to avoid memory leaks,
 * especially if the IDs are tied to specific instances that might be destroyed and recreated.</p>
 */
public class ScrollSaveHelper {
    private static final Vector<ScrollState> mStack = new Vector<>();

    public static class ScrollState {
        public String id;
        public Parcelable state;
    }

    public static synchronized void putState(String id, Parcelable state_) {
        synchronized (ScrollSaveHelper.class) {
            Log.e("ScrollSaveHelper", "Saving (" + id + ")");
            ScrollState state = null;
            for (ScrollState s : mStack) {
                if (s.id.equals(id)) {
                    state = s;
                    break;
                }
            }
            if (state == null) {
                ScrollState state2 = new ScrollState();
                state2.id = id;
                state2.state = state_;
                mStack.add(state2);
            } else {
                state.state = state_;
            }
        }
    }

    public static synchronized Parcelable getState(String id) {
        synchronized (ScrollSaveHelper.class) {
            Log.e("ScrollSaveHelper", "GetOffset (" + id + ")");
            for (ScrollState s : mStack) {
                if (s.id.equals(id)) {
                    return s.state;
                }
            }
            return null;
        }
    }
}
