package ru.ivansuper.jasmin.icq;

/**
 * Represents a FLAP (Framed Layer Application Protocol) packet.
 * This class provides methods to create, parse, and access data within a FLAP packet.
 * FLAP is used in the OSCAR protocol, which is the underlying protocol for AIM and ICQ.
 * <p>
 * A FLAP packet has the following structure:
 * <pre>
 * +-------------------+-------------------+-------------------+-------------------+
 * | Magic (1 byte)    | Channel (1 byte)  | Sequence (2 bytes)| Data Length (2 bytes)|
 * +-------------------+-------------------+-------------------+-------------------+
 * | Data (variable length)                                                       |
 * +------------------------------------------------------------------------------+
 * </pre>
 * - Magic: Always 0x2A (*)
 * - Channel: Identifies the type of data in the packet (e.g., login, chat, etc.)
 * - Sequence: A sequence number for the packet
 * - Data Length: The length of the data portion of the packet
 * - Data: The actual payload of the packet
 */
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
}
