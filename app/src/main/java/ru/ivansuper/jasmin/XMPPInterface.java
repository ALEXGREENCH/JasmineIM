package ru.ivansuper.jasmin;

import java.util.Vector;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;

/**
 * Provides a global interface for interacting with XMPP (Extensible Messaging and Presence Protocol)
 * data. This class manages a list of listeners that can be notified when new XML data is received
 * or needs to be processed.
 *
 * <p>It allows different parts of the application to subscribe to XMPP events and react to
 * specific XML stanzas.
 */
public class XMPPInterface {
    public static final Vector<OnXMLListener> packet_listeners = new Vector<>();

    public interface OnXMLListener {
        boolean OnXMLData(JProfile jProfile, Node node);
    }

    /** @noinspection unused*/
    public static synchronized void addPacketsListener(OnXMLListener listener) {
        synchronized (XMPPInterface.class) {
            synchronized (packet_listeners) {
                if (!packet_listeners.contains(listener)) {
                    packet_listeners.add(listener);
                }
            }
        }
    }

    /** @noinspection unused*/
    public static synchronized void removePacketsListener(OnXMLListener listener) {
        synchronized (XMPPInterface.class) {
            synchronized (packet_listeners) {
                if (packet_listeners.contains(listener)) {
                    packet_listeners.remove(listener);
                }
            }
        }
    }

    public static boolean dispatchOnXMLPacketEvent(JProfile profile, Node node) {
        synchronized (packet_listeners) {
            if (packet_listeners.isEmpty()) {
                return false;
            }
            for (OnXMLListener listener : packet_listeners) {
                boolean catched = listener.OnXMLData(profile, node);
                if (catched) {
                    return true;
                }
            }
            return false;
        }
    }
}
