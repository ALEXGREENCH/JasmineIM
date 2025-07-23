package ru.ivansuper.jasmin.plugins._interface;

import android.content.Context;
import android.content.Intent;

import java.util.Iterator;
import java.util.Vector;

/**
 * A utility class for managing broadcast intents within the application's service.
 * <p>
 * This class provides a centralized mechanism for components to listen for and react to
 * specific intents broadcasted by the service. It allows for adding and removing listeners,
 * and it dispatches incoming intents to registered listeners.
 * </p>
 * <p>
 * Note: The {@code @noinspection unused} annotation suggests that this class might be part of a
 * larger system where its usage is not directly apparent in the immediate context, or it's
 * intended for use by external modules or plugins.
 * </p>
 */
public class ServiceBroadcastReceiver {
    public static final Vector<OnBroadcastListener> intent_listeners = new Vector<>();

    public interface OnBroadcastListener {
        boolean OnIntent(Context context, Intent intent);
    }

    public static synchronized void addBroadcastListener(OnBroadcastListener listener) {
        synchronized (ServiceBroadcastReceiver.class) {
            if (!intent_listeners.contains(listener)) {
                intent_listeners.add(listener);
            }
        }
    }

    public static synchronized void removeBroadcastListener(OnBroadcastListener listener) {
        synchronized (ServiceBroadcastReceiver.class) {
            //noinspection RedundantCollectionOperation
            if (intent_listeners.contains(listener)) {
                intent_listeners.remove(listener);
            }
        }
    }

    public static synchronized boolean OnIntent(Context context, Intent intent) {
        boolean z = false;
        synchronized (ServiceBroadcastReceiver.class) {
            if (!intent_listeners.isEmpty()) {
                Iterator<OnBroadcastListener> it = intent_listeners.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    OnBroadcastListener listener = it.next();
                    boolean catched = listener.OnIntent(context, intent);
                    if (catched) {
                        z = true;
                        break;
                    }
                }
            }
        }
        return z;
    }
}