package ru.ivansuper.jasmin.icq;

public class FLAP {
    private final byte channel;
    private final int dataSize;
    private final byte[] localBuffer;
    private final int seqDatagram;

    public FLAP(ByteBuffer source) {
        this.localBuffer = source.bytes;
        this.channel = ByteBuffer.previewByte(1, source.bytes);
        this.seqDatagram = ByteBuffer.previewWord(2, source.bytes);
        this.dataSize = ByteBuffer.previewWord(4, source.bytes);
    }

    public final int getChannel() {
        return this.channel;
    }

    /** @noinspection unused*/
    public final int getSeqDatagram() {
        return this.seqDatagram;
    }

    /** @noinspection unused*/
    public final int getDataSize() {
        return this.dataSize;
    }

    public final ByteBuffer getData() {
        ByteBuffer buffer = new ByteBuffer(this.localBuffer);
        buffer.readPos = 6;
        buffer.writePos = this.dataSize + 6;
        return buffer;
    }

    /** @noinspection unused*/
    public static boolean itIsFlapPacket(ByteBuffer buffer) {
        return buffer.previewByte(0) == 42;
    }

    /** @noinspection unused*/
    public static int allFlapSize(ByteBuffer buffer) {
        return buffer.previewWord(4) + 6;
    }

    public static ByteBuffer createFlap(byte channel, int seq, ByteBuffer source) {
        ByteBuffer buffer = new ByteBuffer(source.writePos + 6);
        buffer.writeByte((byte) 42);
        buffer.writeByte(channel);
        buffer.writeWord(seq);
        buffer.writeWord(source.writePos);
        buffer.writeByteBuffer(source);
        return buffer;
    }
}
