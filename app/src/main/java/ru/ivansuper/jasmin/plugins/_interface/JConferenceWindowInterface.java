package ru.ivansuper.jasmin.plugins._interface;

import android.app.Dialog;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;

import java.util.Iterator;
import java.util.Vector;

import ru.ivansuper.jasmin.chats.JConference;

/**
 * Provides an interface for plugins to interact with the JConference window.
 * This class allows plugins to listen for window lifecycle events, handle key events,
 * create dialogs, modify options menus, and add custom menu items.
 *
 * <p><b>Usage:</b>
 * <br> - Plugins can register {@link OnWindowListener} to receive notifications about window state changes
 *   (started, paused, resumed, stopped) and to intercept events like key presses or menu creation.
 * <br> - Plugins can register {@link OnBindMenuAction} to add custom items to specific menus within
 *   the conference window (main menu, message menu, user list menu).
 *
 * <p>All listener registration and event dispatching methods are synchronized to ensure thread safety.
 *
 * <p><b>Event Dispatching:</b>
 * <br> - Window lifecycle events ({@code OnWndStarted}, {@code OnWndPaused}, etc.) are dispatched to all registered {@link OnWindowListener}s.
 * <br> - For events that can be "handled" or "intercepted" (e.g., {@code OnWndKeyDown}, {@code OnWndCreateDialog}, {@code OnWndCreateOptionsMenu}, {@code OnWndResult}),
 *   the dispatching stops as soon as one listener returns {@code true} (for boolean methods) or a non-null dialog (for {@code OnWndCreateDialog}).
 *   This means that the first listener to handle the event effectively consumes it.
 * <br> - For menu item binding ({@code dispatchBindMenuItem}), the event is dispatched to the first registered {@link OnBindMenuAction} listener.
 *
 * <p><b>Note:</b> The {@code @noinspection unused} annotation is used for constants and methods that are
 * intended to be used by plugins and might not be directly referenced within the core application code,
 * thus appearing as "unused" to static analysis tools.
 */
public class JConferenceWindowInterface {
    public static final Vector<OnWindowListener> wnd_listeners = new Vector<>();
    public static final Vector<OnBindMenuAction> menu_items_bind_listeners = new Vector<>();

    public interface OnBindMenuAction {
        /** @noinspection unused*/
        int CONFERENCE_MAIN_MENU = 0;
        /** @noinspection unused*/
        int CONFERENCE_MESSAGE_MENU = 2;
        /** @noinspection unused*/
        int CONFERENCE_USERLIST_MENU = 1;

        IdentificatedRunnable OnAddMenuItem(JConference jConference, int i, int i2, MenuItemWrapper menuItemWrapper);
    }

    public interface OnWindowListener {
        /** @noinspection unused*/
        int WND_PAUSED = 1;
        /** @noinspection unused*/
        int WND_RESUMED = 2;
        /** @noinspection unused*/
        int WND_STARTED = 0;
        /** @noinspection unused*/
        int WND_STOPPED = 3;

        Dialog OnWndCreateDialog(JConference jConference, int i);

        boolean OnWndCreateOptionsMenu(JConference jConference, Menu menu);

        boolean OnWndKeyDown(JConference jConference, int i, KeyEvent keyEvent);

        void OnWndPaused(JConference jConference);

        boolean OnWndResult(JConference jConference, int i, int i2, Intent intent);

        void OnWndResumed(JConference jConference);

        void OnWndStarted(JConference jConference);

        void OnWndStopped(JConference jConference);
    }

    /** @noinspection unused*/
    public static synchronized void addWindowListener(OnWindowListener listener) {
        synchronized (JConferenceWindowInterface.class) {
            if (!wnd_listeners.contains(listener)) {
                wnd_listeners.add(listener);
            }
        }
    }

    /** @noinspection unused*/
    public static synchronized void removeWindowListener(OnWindowListener listener) {
        synchronized (JConferenceWindowInterface.class) {
            wnd_listeners.remove(listener);
        }
    }

    /** @noinspection unused*/
    public static synchronized void addMenuItemAddListener(OnBindMenuAction listener) {
        synchronized (JConferenceWindowInterface.class) {
            if (!menu_items_bind_listeners.contains(listener)) {
                menu_items_bind_listeners.add(listener);
            }
        }
    }

    /** @noinspection unused*/
    public static synchronized void removeWindowListener(OnBindMenuAction listener) {
        synchronized (JConferenceWindowInterface.class) {
            menu_items_bind_listeners.remove(listener);
        }
    }

    public static synchronized void dispatchWindowEvent(JConference conference, int event) {
        synchronized (JConferenceWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                for (OnWindowListener listener : wnd_listeners) {
                    switch (event) {
                        case 0:
                            listener.OnWndStarted(conference);
                            break;
                        case 1:
                            listener.OnWndPaused(conference);
                            break;
                        case 2:
                            listener.OnWndResumed(conference);
                            break;
                        case 3:
                            listener.OnWndStopped(conference);
                            break;
                    }
                }
            }
        }
    }

    /** @noinspection unused*/
    public static synchronized boolean dispatchWindowKeyEvent(JConference conference, int code, KeyEvent event) {
        boolean z = false;
        synchronized (JConferenceWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    OnWindowListener listener = it.next();
                    boolean catched = listener.OnWndKeyDown(conference, code, event);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    public static synchronized Dialog dispatchWindowCreateDialogEvent(JConference conference, int id) {
        Dialog dialog = null;
        synchronized (JConferenceWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                if (it.hasNext()) {
                    OnWindowListener listener = it.next();
                    dialog = listener.OnWndCreateDialog(conference, id);
                }
            }
        }
        return dialog;
    }

    public static synchronized boolean dispatchWindowResultEvent(JConference conference, int request_code, int result_code, Intent data) {
        boolean z = false;
        synchronized (JConferenceWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    OnWindowListener listener = it.next();
                    boolean catched = listener.OnWndResult(conference, request_code, result_code, data);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    /** @noinspection unused*/
    public static synchronized boolean dispatchWindowCreateOptionsMenuEvent(JConference conference, Menu menu) {
        boolean z = false;
        synchronized (JConferenceWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    OnWindowListener listener = it.next();
                    boolean catched = listener.OnWndCreateOptionsMenu(conference, menu);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    public static synchronized IdentificatedRunnable dispatchBindMenuItem(JConference conference, int menu, int idx, MenuItemWrapper wrapper) {
        IdentificatedRunnable identificatedRunnable = null;
        synchronized (JConferenceWindowInterface.class) {
            if (!menu_items_bind_listeners.isEmpty()) {
                Iterator<OnBindMenuAction> it = menu_items_bind_listeners.iterator();
                if (it.hasNext()) {
                    OnBindMenuAction listener = it.next();
                    identificatedRunnable = listener.OnAddMenuItem(conference, menu, idx, wrapper);
                }
            }
        }
        return identificatedRunnable;
    }
}
