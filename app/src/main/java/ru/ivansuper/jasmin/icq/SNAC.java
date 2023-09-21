package ru.ivansuper.jasmin.icq;
public class SNAC {
    private int flags;
    private int id;
    private final ByteBuffer localBuffer;
    private int subtype;
    private int type;

    public SNAC(ByteBuffer var1) {
        this.localBuffer = var1;
        this.type = this.localBuffer.previewWord(0);
        this.subtype = this.localBuffer.previewWord(2);
        this.flags = this.localBuffer.previewWord(4);
        this.id = this.localBuffer.previewDWord(6);
        this.localBuffer.readPos = 16;
    }

    /** @noinspection unused*/
    public static final ByteBuffer createSnac(int var0, int var1, int var2, int var3, ByteBuffer var4) {
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

