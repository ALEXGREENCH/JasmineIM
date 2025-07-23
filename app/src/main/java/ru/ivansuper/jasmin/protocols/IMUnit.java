package ru.ivansuper.jasmin.protocols;

import ru.ivansuper.jasmin.protocols.utils.Serializable;

/**
 * Represents an abstract unit in an instant messaging (IM) system.
 * This class serves as a base for different types of IM entities like contacts, groups, conferences, and profiles.
 * It defines constants for these unit types and an abstract method to retrieve the specific type of a unit.
 *
 * <p>Implementations of this class should provide a concrete way to determine the unit type.
 * This class implements {@link Serializable} to allow its instances to be serialized.
 */
public abstract class IMUnit implements Serializable {
    /** @noinspection unused*/
    public static final int TYPE_CONFERENCE = 2;
    /** @noinspection unused*/
    public static final int TYPE_CONTACT = 0;
    /** @noinspection unused*/
    public static final int TYPE_GROUP = 1;
    /** @noinspection unused*/
    public static final int TYPE_PROFILE = 3;

    /** @noinspection unused*/
    public abstract int getUnitType();
}