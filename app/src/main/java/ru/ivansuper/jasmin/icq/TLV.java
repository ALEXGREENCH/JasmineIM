package ru.ivansuper.jasmin.icq;

/**
 * Represents a Type-Length-Value (TLV) structure.
 * TLV is a common encoding scheme used in communication protocols
 * where data is organized into a sequence of records. Each record
 * consists of a type identifier, a length field, and a value field.
 */
public class TLV {
    public byte[] data;
    public int length;
    public int type;

    public TLV(byte[] buffer, int TLVType, int TLVLength) {
        this.type = TLVType;
        this.length = TLVLength;
        this.data = buffer;
        if (this.data == null) {
            this.data = new byte[0];
        }
    }

    public final ByteBuffer getData() {
        return new ByteBuffer(this.data);
    }

    public final void recycle() {
        if (this.data != null) {
            ByteCache.recycle(this.data);
        }
        this.data = null;
    }
}
