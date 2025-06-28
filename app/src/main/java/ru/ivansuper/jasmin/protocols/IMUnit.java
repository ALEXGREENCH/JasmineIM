package ru.ivansuper.jasmin.protocols;

import ru.ivansuper.jasmin.protocols.utils.Serializable;

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