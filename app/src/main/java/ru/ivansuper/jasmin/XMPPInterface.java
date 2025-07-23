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

    /**
     * A thread-safe collection of {@link OnXMLListener} instances.
     * These listeners are notified when new XMPP XML data is received or needs to be processed.
     * Use {@link #addPacketsListener(OnXMLListener)} to add a listener and
     * {@link #removePacketsListener(OnXMLListener)} to remove one.
     */
    public static final Vector<OnXMLListener> packet_listeners = new Vector<>();

    /**
     * Interface definition for a callback to be invoked when XML data is received
     * or needs to be processed. Implementers of this interface can register themselves
     * with the {@link XMPPInterface} to listen for incoming XMPP stanzas.
     */
    public interface OnXMLListener {
        boolean OnXMLData(JProfile jProfile, Node node);
    }

    /**
     * Adds a listener to the list of packet listeners.
     *
     * <p>The listener will be notified when new XML data is received or needs to be processed.
     * If the listener is already present in the list, it will not be added again.
     *
     * <p>This method is synchronized to ensure thread safety when modifying the list of listeners.
     *
     * @param listener The listener to add.
     * @noinspection unused
     */
    public static synchronized void addPacketsListener(OnXMLListener listener) {
        synchronized (XMPPInterface.class) {
            synchronized (packet_listeners) {
                if (!packet_listeners.contains(listener)) {
                    packet_listeners.add(listener);
                }
            }
        }
    }

    /**
     * Removes a listener from the list of XMPP packet listeners.
     *
     * <p>If the provided listener is currently registered, it will be removed and will no longer
     * receive notifications for new XMPP data. If the listener is not registered, this method
     * has no effect.
     *
     * @param listener The {@link OnXMLListener} to remove.
     * @noinspection unused
     */
    public static synchronized void removePacketsListener(OnXMLListener listener) {
        synchronized (XMPPInterface.class) {
            synchronized (packet_listeners) {
                packet_listeners.remove(listener);
            }
        }
    }

    /**
     * Dispatches an incoming XML packet event to all registered listeners.
     *
     * <p>This method iterates through the list of {@link OnXMLListener} instances and calls their
     * {@link OnXMLListener#OnXMLData(JProfile, Node)} method. If any listener returns {@code true}
     * (indicating it has processed the event), the iteration stops, and the method returns
     * {@code true}. Otherwise, it continues to the next listener.
     *
     * @param profile The {@link JProfile} associated with the XMPP connection from which the
     *     packet originated.
     * @param node The {@link Node} representing the received XML packet.
     * @return {@code true} if any listener processed the event (returned {@code true} from its
     *     {@code OnXMLData} method), {@code false} otherwise.
     */
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
