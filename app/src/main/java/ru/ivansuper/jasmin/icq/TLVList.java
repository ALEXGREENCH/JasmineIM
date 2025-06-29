package ru.ivansuper.jasmin.icq;

import android.util.Log;
import java.util.Vector;

public class TLVList {
    public int TLVCount;
    private final Vector<TLV> list = new Vector<>();

    public TLVList(ByteBuffer buffer, int count) {
        //noinspection UnusedAssignment
        this.TLVCount = 0;
        this.TLVCount = count;
        int i = 0;
        while (i < count && buffer.getBytesCountAvailableToRead() >= 4) {
            int dataType = buffer.readWord();
            int size = buffer.readWord();
            if (size >= 0) {
                if (size == 0) {
                    TLV tlv = new TLV(null, dataType, size);
                    this.list.add(tlv);
                    i++;
                } else if (buffer.getBytesCountAvailableToRead() >= size) {
                    byte[] datachunk = ByteCache.getByteArray(size);
                    buffer.readBytes(size, datachunk);
                    TLV tlv2 = new TLV(datachunk, dataType, size);
                    this.list.add(tlv2);
                    i++;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    /** @noinspection unused*/
    public TLVList(ByteBuffer buffer, int count, boolean cut_off, int empty) {
        //noinspection UnusedAssignment
        this.TLVCount = 0;
        this.TLVCount = count;
        int i = 0;
        while (i < count && buffer.available()) {
            int dataType = buffer.readWord();
            buffer.skip(1);
            int size = buffer.readByte();
            if (size >= 0) {
                if (size == 0) {
                    TLV tlv = new TLV(new byte[0], dataType, size);
                    this.list.add(tlv);
                } else {
                    byte[] datachunk = ByteCache.getByteArray(size);
                    buffer.readBytes(size, datachunk);
                    TLV tlv2 = new TLV(datachunk, dataType, size);
                    this.list.add(tlv2);
                }
                i++;
            } else {
                return;
            }
        }
    }

    /** @noinspection unused*/
    public TLVList(ByteBuffer buffer, int length, boolean flag) {
        this.TLVCount = 0;
        int i = 0;
        int destPos = buffer.readPos + length;
        while (buffer.readPos < destPos && buffer.getBytesCountAvailableToRead() >= 4) {
            int dataType = buffer.readWord();
            int size = buffer.readWord();
            if (size >= 0) {
                if (size == 0) {
                    TLV tlv = new TLV(null, dataType, size);
                    this.list.add(tlv);
                } else {
                    if (buffer.getBytesCountAvailableToRead() < size) {
                        break;
                    }
                    byte[] datachunk = ByteCache.getByteArray(size);
                    buffer.readBytes(size, datachunk);
                    TLV tlv2 = new TLV(datachunk, dataType, size);
                    this.list.add(tlv2);
                }
                i++;
            } else {
                break;
            }
        }
        this.TLVCount = i;
    }

    /** @noinspection unused*/
    public TLVList(ByteBuffer buffer, int length, boolean cut_off, boolean empty) {
        this.TLVCount = 0;
        int i = 0;
        int destPos = buffer.readPos + length;
        while (buffer.readPos < destPos && buffer.getBytesCountAvailableToRead() >= 4) {
            int dataType = buffer.readWord();
            buffer.skip(1);
            int size = buffer.readByte();
            if (size >= 0) {
                if (size == 0) {
                    TLV tlv = new TLV(null, dataType, size);
                    this.list.add(tlv);
                } else {
                    if (buffer.getBytesCountAvailableToRead() < size) {
                        break;
                    }
                    byte[] datachunk = ByteCache.getByteArray(size);
                    buffer.readBytes(size, datachunk);
                    TLV tlv2 = new TLV(datachunk, dataType, size);
                    this.list.add(tlv2);
                }
                i++;
            } else {
                break;
            }
        }
        this.TLVCount = i;
    }

    public final TLV getTLV(int TLVType) {
        for (int i = 0; i < this.list.size(); i++) {
            TLV tlvA = this.list.get(i);
            if (tlvA.type == TLVType) {
                return tlvA;
            }
        }
        return null;
    }

    /** @noinspection unused*/
    public final TLV getTLV(int TLVType, int number) {
        int num = number - 1;
        for (int i = 0; i < this.list.size(); i++) {
            TLV tlvA = this.list.get(i);
            if (tlvA.type == TLVType) {
                if (num == 0) {
                    return tlvA;
                }
                num--;
            }
        }
        return null;
    }

    /** @noinspection unused*/
    public final int getTLVCount(int TLVType) {
        int count = 0;
        for (int i = 0; i < this.list.size(); i++) {
            TLV tlvA = this.list.get(i);
            if (tlvA.type == TLVType) {
                count++;
            }
        }
        return count;
    }

    /** @noinspection unused*/
    public final void logoutReadedTLVs() {
        for (int i = 0; i < this.list.size(); i++) {
            TLV tlvA = this.list.get(i);
            Log.i("TLVList dump", "0x" + Integer.toHexString(tlvA.type));
        }
    }

    public final void recycle() {
        for (int i = 0; i < this.list.size(); i++) {
            this.list.get(i).recycle();
        }
    }
}