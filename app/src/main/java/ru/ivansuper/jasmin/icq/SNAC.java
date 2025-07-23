package ru.ivansuper.jasmin.icq;

/**
 * Represents a SNAC (Simple Network Access Control) packet in the ICQ protocol.
 * SNACs are used to exchange information between the client and the server.
 * Each SNAC has a type, subtype, flags, and an ID, along with a data payload.
 */
public class SNAC {
    private final int flags;
    private final int id;
    private final ByteBuffer localBuffer;
    private final int subtype;
    private final int type;

    public SNAC(ByteBuffer var1) {
        this.localBuffer = var1;
        this.type = this.localBuffer.previewWord(0);
        this.subtype = this.localBuffer.previewWord(2);
        this.flags = this.localBuffer.previewWord(4);
        this.id = this.localBuffer.previewDWord(6);
        this.localBuffer.readPos = 16;
    }

    /** @noinspection unused*/
    public static ByteBuffer createSnac(int var0, int var1, int var2, int var3, ByteBuffer var4) {
        ByteBuffer var5 = new ByteBuffer(var4.writePos + 10);
        var5.writeWord(var0);
        var5.writeWord(var1);
        var5.writeWord(var2);
        var5.writeDWord(var3);
        var5.writeByteBuffer(var4);
        return var5;
    }

    /** @noinspection unused*/
    public final ByteBuffer getData() {
        return this.localBuffer;
    }

    /** @noinspection unused*/
    public final int getFlags() {
        return this.flags;
    }

    public final int getId() {
        return this.id;
    }

    /** @noinspection unused*/
    public final int getSubtype() {
        return this.subtype;
    }

    public final int getType() {
        return this.type;
    }
}

