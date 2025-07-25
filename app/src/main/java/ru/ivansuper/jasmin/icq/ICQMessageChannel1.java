package ru.ivansuper.jasmin.icq;

/**
 * Represents an ICQ message channel, version 1.
 * This class is responsible for constructing the binary data payload for sending an ICQ message.
 * It encapsulates the message content, recipient information, and protocol-specific formatting.
 */
public class ICQMessageChannel1 {
    private final int sequence;
    public ByteBuffer data;
    public String receiver;
    public String text;

    public ICQMessageChannel1(int seq, String Receiver, String MessageText, boolean check_enabled, byte[] cookie) {
        this.receiver = Receiver;
        this.text = MessageText;
        this.sequence = seq;
        createMessage(check_enabled, cookie);
    }

    private void createMessage(boolean check_enabled, byte[] cookie) {
        ByteBuffer main = null;
        try {
            main = new ByteBuffer();
        } catch (Exception e) {
            ////e = e;
        }
        try {
            main.write(cookie);
            main.writeWord(1);
            main.writeByte((byte) this.receiver.length());
            main.writeStringAscii(this.receiver);
            ByteBuffer tlv2 = new ByteBuffer();
            tlv2.writeByte((byte) 5);
            tlv2.writeByte((byte) 1);
            tlv2.writeWord(2);
            tlv2.writeWord(262);
            tlv2.writeByte((byte) 1);
            tlv2.writeByte((byte) 1);
            ByteBuffer msg = new ByteBuffer();
            msg.writeStringUnicode(this.text);
            tlv2.writeWord(msg.writePos + 4);
            tlv2.writeWord(2);
            tlv2.writeWord(0);
            tlv2.writeStringUnicode(this.text);
            main.writeIcqTLV(tlv2, 2);
            main.writeWord(6);
            main.writeWord(0);
            if (check_enabled) {
                main.writeWord(3);
                main.writeWord(0);
            }
            ByteBuffer snc = SNAC.createSnac(4, 6, 0, 6, main);
            this.data = FLAP.createFlap((byte) 2, this.sequence, snc);
        } catch (Exception e2) {
            ////e = e2;
            ////e.printStackTrace();
        }
    }
}