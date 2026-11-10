package ru.ivansuper.jasmin.jabber;

import java.util.Vector;
import ru.ivansuper.jasmin.ContactlistItem;

public class RosterComparator {
    private RosterComparator() {
    }

    private final ContactlistItem getByHash(Vector<ContactlistItem> vector, ContactlistItem contactlistItem) {
        int hash = contactlistItem.getHash();
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            ContactlistItem contactlistItem2 = vector.get(i);
            if (contactlistItem2.getHash() == hash) {
                vector.remove(i);
                return contactlistItem2;
            }
        }
        return null;
    }

    public static RosterComparator getInstance() {
        RosterComparator rosterComparator;
        synchronized (RosterComparator.class) {
            try {
                rosterComparator = new RosterComparator();
            } catch (Throwable th) {
                throw th;
            }
        }
        return rosterComparator;
    }

    public final Vector<ContactlistItem> compare(Vector<ContactlistItem> vector, Vector<ContactlistItem> vector2) {
        Vector<ContactlistItem> vector3 = new Vector<>();
        Vector<ContactlistItem> vector4 = (Vector) vector.clone();
        for (ContactlistItem contactlistItem : vector2) {
            ContactlistItem byHash = getByHash(vector4, contactlistItem);
            if (byHash != null) {
                byHash.update(contactlistItem);
                vector3.add(byHash);
            } else {
                vector3.add(contactlistItem);
            }
        }
        return vector3;
    }
}
