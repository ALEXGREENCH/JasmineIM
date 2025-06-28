package ru.ivansuper.jasmin.protocols.utils.roster;

import java.util.Vector;

import ru.ivansuper.jasmin.protocols.IMContact;
import ru.ivansuper.jasmin.protocols.IMProfile;

/** @noinspection unused*/
public abstract class Contactlist<PacketObject> {
    /** @noinspection FieldCanBeLocal*/
    private final IMProfile mProfile;
    private final Vector<IMContact> mRoster = new Vector<>();

    public abstract boolean contactExists(String str);

    public abstract IMContact getContact(String str);

    public abstract boolean parse(PacketObject packetobject);

    public abstract void putContact(IMContact iMContact);

    public abstract IMContact removeContact(String str);

    public Contactlist(IMProfile profile) {
        this.mProfile = profile;
    }
}
