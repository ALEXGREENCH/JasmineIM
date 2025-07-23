package ru.ivansuper.jasmin.plugins._interface;

import android.app.Dialog;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.chats.JChatActivity;

/**
 * Provides an interface for plugins to interact with the JChatActivity window and its components.
 * This class allows plugins to:
 * <ul>
 *   <li>Listen to window lifecycle events (started, paused, resumed, stopped).</li>
 *   <li>Handle key events within the chat window.</li>
 *   <li>Create custom dialogs.</li>
 *   <li>Process results from activities started for a result.</li>
 *   <li>Modify the options menu.</li>
 *   <li>Add custom menu items to the main and message context menus.</li>
 *   <li>Listen to click events on messages in the message list.</li>
 * </ul>
 * <p>
 * Plugins can register listeners for various events. When an event occurs, the corresponding
 * listener methods will be invoked.
 * </p>
 * <p>
 * All methods that add, remove, or dispatch events are synchronized to ensure thread safety.
 * </p>
 *
 * @noinspection unused
 */
public class JChatWindowInterface {
    public static final Vector<OnWindowListener> wnd_listeners = new Vector<>();
    public static final Vector<OnBindMenuAction> menu_items_bind_listeners = new Vector<>();
    public static final Vector<OnItemClickListener> messagelist_listeners = new Vector<>();

    public interface OnBindMenuAction {
        int JCHAT_MAIN_MENU = 10;
        int JCHAT_MESSAGE_MENU = 11;

        IdentificatedRunnable OnAddMenuItem(JChatActivity jChatActivity, int i, int i2, MenuItemWrapper menuItemWrapper);
    }

    public interface OnItemClickListener {
        boolean OnItemClick(AdapterView<?> adapterView, View view, int i, long j);
    }

    public interface OnWindowListener {
        int WND_PAUSED = 1;
        int WND_RESUMED = 2;
        int WND_STARTED = 0;
        int WND_STOPPED = 3;

        Dialog OnWndCreateDialog(JChatActivity jChatActivity, int i);

        boolean OnWndCreateOptionsMenu(JChatActivity jChatActivity, Menu menu);

        boolean OnWndKeyDown(JChatActivity jChatActivity, int i, KeyEvent keyEvent);

        void OnWndPaused(JChatActivity jChatActivity);

        boolean OnWndResult(JChatActivity jChatActivity, int i, int i2, Intent intent);

        void OnWndResumed(JChatActivity jChatActivity);

        void OnWndStarted(JChatActivity jChatActivity);

        void OnWndStopped(JChatActivity jChatActivity);
    }

    public static synchronized void addWindowListener(OnWindowListener listener) {
        synchronized (JChatWindowInterface.class) {
            if (!wnd_listeners.contains(listener)) {
                wnd_listeners.add(listener);
            }
        }
    }

    public static synchronized void removeWindowListener(OnWindowListener listener) {
        synchronized (JChatWindowInterface.class) {
            wnd_listeners.remove(listener);
        }
    }

    public static synchronized void addMenuItemAddListener(OnBindMenuAction listener) {
        synchronized (JChatWindowInterface.class) {
            if (!menu_items_bind_listeners.contains(listener)) {
                menu_items_bind_listeners.add(listener);
            }
        }
    }

    public static synchronized void removeWindowListener(OnBindMenuAction listener) {
        synchronized (JChatWindowInterface.class) {
            menu_items_bind_listeners.remove(listener);
        }
    }

    public static synchronized void addMessagesClickListener(OnItemClickListener listener) {
        synchronized (JChatWindowInterface.class) {
            if (!messagelist_listeners.contains(listener)) {
                messagelist_listeners.add(listener);
            }
        }
    }

    public static synchronized void removeMessagesClickListener(OnItemClickListener listener) {
        synchronized (JChatWindowInterface.class) {
            messagelist_listeners.remove(listener);
        }
    }

    public static synchronized boolean dispatchMessagesClickEvent(AdapterView<?> av, View view, int pos, long id) {
        boolean z;
        synchronized (JChatWindowInterface.class) {
            if (messagelist_listeners.isEmpty()) {
                z = false;
            } else {
                Iterator<OnItemClickListener> it = messagelist_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        z = false;
                        break;
                    }
                    OnItemClickListener listener = it.next();
                    boolean catched = listener.OnItemClick(av, view, pos, id);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    public static synchronized void dispatchWindowEvent(JChatActivity chat, int event) {
        synchronized (JChatWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                for (OnWindowListener listener : wnd_listeners) {
                    switch (event) {
                        case 0:
                            listener.OnWndStarted(chat);
                            break;
                        case 1:
                            listener.OnWndPaused(chat);
                            break;
                        case 2:
                            listener.OnWndResumed(chat);
                            break;
                        case 3:
                            listener.OnWndStopped(chat);
                            break;
                    }
                }
            }
        }
    }

    public static synchronized boolean dispatchWindowKeyEvent(JChatActivity chat, int code, KeyEvent event) {
        boolean z = false;
        synchronized (JChatWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    OnWindowListener listener = it.next();
                    boolean catched = listener.OnWndKeyDown(chat, code, event);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    public static synchronized Dialog dispatchWindowCreateDialogEvent(JChatActivity chat, int id) {
        Dialog dialog = null;
        synchronized (JChatWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                if (it.hasNext()) {
                    OnWindowListener listener = it.next();
                    dialog = listener.OnWndCreateDialog(chat, id);
                }
            }
        }
        return dialog;
    }

    public static synchronized boolean dispatchWindowResultEvent(JChatActivity chat, int request_code, int result_code, Intent data) {
        boolean z = false;
        synchronized (JChatWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    OnWindowListener listener = it.next();
                    boolean catched = listener.OnWndResult(chat, request_code, result_code, data);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    public static synchronized boolean dispatchWindowCreateOptionsMenuEvent(JChatActivity chat, Menu menu) {
        boolean z = false;
        synchronized (JChatWindowInterface.class) {
            if (!wnd_listeners.isEmpty()) {
                Iterator<OnWindowListener> it = wnd_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    OnWindowListener listener = it.next();
                    boolean catched = listener.OnWndCreateOptionsMenu(chat, menu);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }

    public static synchronized IdentificatedRunnable dispatchBindMenuItem(JChatActivity chat, int menu, int idx, MenuItemWrapper wrapper) {
        IdentificatedRunnable identificatedRunnable = null;
        synchronized (JChatWindowInterface.class) {
            if (!menu_items_bind_listeners.isEmpty()) {
                Iterator<OnBindMenuAction> it = menu_items_bind_listeners.iterator();
                if (it.hasNext()) {
                    OnBindMenuAction listener = it.next();
                    identificatedRunnable = listener.OnAddMenuItem(chat, menu, idx, wrapper);
                }
            }
        }
        return identificatedRunnable;
    }
}