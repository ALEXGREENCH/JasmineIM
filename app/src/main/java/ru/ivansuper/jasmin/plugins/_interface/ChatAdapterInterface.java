package ru.ivansuper.jasmin.plugins._interface;

import android.view.View;
import android.view.ViewGroup;

import java.util.Vector;
import ru.ivansuper.jasmin.ChatAdapter;

/**
 * Provides an interface for plugins to interact with the {@link ChatAdapter}.
 * <p>
 * This class allows plugins to:
 * <ul>
 *     <li>Listen for and potentially override the creation of views for chat items ({@link OnGetViewListener}).</li>
 *     <li>Listen for and modify chat item views after they have been configured by the adapter ({@link OnConfigureItemListener}).</li>
 *     <li>Listen for events after a chat item view has been configured ({@link OnAfterConfigureItemListener}).</li>
 * </ul>
 * <p>
 * Listeners can be added and removed dynamically. Event dispatching ensures that all registered listeners
 * are notified in the order they were added.
 * <p>
 * Note: The {@code @noinspection unused} annotation indicates that some elements might appear unused
 * in the current context but are intended for use by external plugins.
 */
public class ChatAdapterInterface {
    public static final Vector<OnGetViewListener> get_view_listeners = new Vector<>();
    public static final Vector<OnConfigureItemListener> configure_item_listeners = new Vector<>();
    public static final Vector<OnGetViewListener> after_configure_item_listeners = new Vector<>();

    public interface OnAfterConfigureItemListener {
        boolean OnAfterConfigureItem(ChatAdapter chatAdapter, int i, View view);
    }

    public interface OnConfigureItemListener {
        boolean OnConfigureItem(ChatAdapter chatAdapter, int i, View view);
    }

    public interface OnGetViewListener {
        View OnGetView(ChatAdapter chatAdapter, int i, View view, ViewGroup viewGroup);
    }

    public static synchronized void addGetViewListener(OnGetViewListener listener) {
        synchronized (ChatAdapterInterface.class) {
            if (!get_view_listeners.contains(listener)) {
                get_view_listeners.add(listener);
            }
        }
    }

    public static synchronized void removeGetViewListener(OnGetViewListener listener) {
        synchronized (ChatAdapterInterface.class) {
            //noinspection RedundantCollectionOperation
            if (get_view_listeners.contains(listener)) {
                get_view_listeners.remove(listener);
            }
        }
    }

    public static synchronized void addConfigureItemListener(OnConfigureItemListener listener) {
        synchronized (ChatAdapterInterface.class) {
            if (!configure_item_listeners.contains(listener)) {
                configure_item_listeners.add(listener);
            }
        }
    }

    public static synchronized void removeConfigureItemListener(OnConfigureItemListener listener) {
        synchronized (ChatAdapterInterface.class) {
            //noinspection RedundantCollectionOperation
            if (configure_item_listeners.contains(listener)) {
                configure_item_listeners.remove(listener);
            }
        }
    }

    public static View dispatchGetViewEvent(ChatAdapter adapter, int pos, View buffered, ViewGroup root) {
        if (get_view_listeners.isEmpty()) {
            return null;
        }
        for (OnGetViewListener listener : get_view_listeners) {
            View result = listener.OnGetView(adapter, pos, buffered, root);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static boolean dispatchConfigureItemEvent(ChatAdapter adapter, int pos, View item) {
        if (configure_item_listeners.isEmpty()) {
            return false;
        }
        for (OnConfigureItemListener listener : configure_item_listeners) {
            boolean catched = listener.OnConfigureItem(adapter, pos, item);
            if (catched) {
                return true;
            }
        }
        return false;
    }
}