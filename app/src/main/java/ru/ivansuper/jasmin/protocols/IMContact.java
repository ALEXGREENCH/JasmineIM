package ru.ivansuper.jasmin.protocols;

import ru.ivansuper.jasmin.protocols.utils.ClientInfo;
import ru.ivansuper.jasmin.protocols.utils.Flags;

/**
 * Represents an IM contact.
 * This class extends {@link IMUnit} and implements the {@link Flags} interface.
 * It stores information about a contact, such as their profile, ID, nickname, group, and flags.
 *
 */
public class IMContact extends IMUnit implements Flags {
    /** @noinspection unused*/
    public static final long FLAG_ADDED = 2;
    /** @noinspection unused*/
    public static final long FLAG_AUTHORIZED = 1;
    private final ClientInfo mClientInfo = new ClientInfo();
    private long mFlags;
    private final IMGroup mGroup;
    private final String mID;
    private final String mNickname;
    private final IMProfile mProfile;

    public IMContact(IMProfile profile, String ID, String nickname, IMGroup group, long flags) {
        this.mProfile = profile;
        this.mID = ID;
        this.mNickname = nickname;
        this.mGroup = group;
    }

    public IMProfile getProfile() {
        return this.mProfile;
    }

    public String getID() {
        return this.mID;
    }

    public String getNickname() {
        return this.mNickname;
    }

    public IMGroup getGroup() {
        return this.mGroup;
    }

    public ClientInfo getClientInfo() {
        return this.mClientInfo;
    }

    @Override
    public int getUnitType() {
        return 0;
    }

    @Override
    public byte[] serialize() {
        return null;
    }

    @Override
    public void deserialize(byte[] data) {
    }

    @Override
    public boolean checkFlag(long flag) {
        return (this.mFlags & flag) == flag;
    }

    @Override
    public void addFlag(long flag) {
        this.mFlags |= flag;
    }

    @Override
    public void removeFlag(long flag) {
        this.mFlags &= ~flag;
    }

    @Override
    public void clearFlags() {
        this.mFlags = 0L;
    }
}