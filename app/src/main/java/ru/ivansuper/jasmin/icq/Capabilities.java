package ru.ivansuper.jasmin.icq;

import java.util.Vector;

import ru.ivansuper.jasmin.utilities;

public class Capabilities {
    public final Vector<String> list = new Vector<>();

    public final void clear() {
        this.list.clear();
    }

    public final void add(String capability) {
        if (!this.list.contains(capability.toUpperCase())) {
            this.list.add(capability);
        }
    }

    /** @noinspection unused*/
    public final void del(String capability) {
        if (this.list.contains(capability.toUpperCase())) {
            this.list.remove(capability);
        }
    }

    public final byte[] toArray() {
        byte[] guids = new byte[this.list.size() * 16];
        int offset = 0;
        for (String guid : this.list) {
            byte[] guid_ = utilities.hexStringToBytesArray(guid);
            System.arraycopy(guid_, 0, guids, offset, guid_.length);
            offset += 16;
        }
        return guids;
    }
}
