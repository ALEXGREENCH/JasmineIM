package ru.ivansuper.jasmin.plugins._interface;

import android.content.Context;
import android.content.Intent;

import java.util.Iterator;
import java.util.Vector;

/** @noinspection unused*/
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