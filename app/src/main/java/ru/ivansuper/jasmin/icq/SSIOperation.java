package ru.ivansuper.jasmin.icq;

/**
 * Represents an operation to be performed on the Server-Side Information (SSI) list.
 * This class encapsulates the type of operation and the associated data objects.
 * It is used to manage changes to the contact list, such as adding, removing, or modifying contacts.
 */
public class SSIOperation {
    public Object object;
    public Object objectA;
    /** @noinspection unused*/
    public Object objectB;
    public int operationType;

    public SSIOperation(int type, Object obj) {
        this.operationType = type;
        this.object = obj;
    }
}