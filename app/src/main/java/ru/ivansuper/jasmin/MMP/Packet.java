package ru.ivansuper.jasmin.MMP;

/**
 * Represents a packet in the MMP protocol.
 * <p>
 * This class encapsulates the structure of a packet, including its command,
 * ID, length, and data payload. It provides methods for creating and parsing
 * packets from byte buffers.
 */
public class Packet {
    public int command;
    private final byte[] data;
    public int id;
    public int length;

    public Packet(ByteBuffer data) {
        data.readPos = 0;
        data.skip(8);
        this.id = data.readDWordLE();
        this.command = data.readDWordLE();
        this.length = data.readDWordLE();
        data.skip(24);
        this.data = data.readBytes(this.length);
    }

    public ByteBuffer getData() {
        return new ByteBuffer(this.data);
    }

    public static ByteBuffer createPacket(int seq, int command, ByteBuffer data) {
        ByteBuffer packet = new ByteBuffer();
        packet.writeDWordLE(-559038737);
        packet.writeDWordLE(65557);
        packet.writeDWordLE(seq);
        packet.writeDWordLE(command);
        packet.writeDWordLE(data.writePos);
        packet.writeDWordLE(0);
        packet.writeDWordLE(0);
        packet.writeLong(0L);
        packet.writeLong(0L);
        packet.writeByteBuffer(data);
        return packet;
    }
}
