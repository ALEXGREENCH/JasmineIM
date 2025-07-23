package ru.ivansuper.jasmin.icq;

import ru.ivansuper.jasmin.utilities;

/**
 * Represents an ICQ message channel, version 2. This class is responsible for
 * constructing the binary data packet for sending an ICQ message.
 * It handles different text encodings and incorporates necessary protocol-specific
 * TLV (Type-Length-Value) structures.
 */
public class ICQMessageChannel2 {
    public ByteBuffer data;
    public String receiver;
    private final int sequence;
    public String text;

    public ICQMessageChannel2(int seq, String Receiver, String MessageText, int encoding, boolean internalUTF8, byte[] cookie) {
        this.receiver = Receiver;
        this.text = MessageText;
        this.sequence = seq;
        createMessage(encoding, internalUTF8, cookie);
    }

    private void createMessage(int encoding, boolean internalUTF8, byte[] cookie) {
        try {
            ByteBuffer main = new ByteBuffer();
            try {
                main.write(cookie);
                main.writeWord(2);
                main.writeByte((byte) this.receiver.length());
                main.writeStringAscii(this.receiver);
                ByteBuffer tlv5 = new ByteBuffer();
                tlv5.writeWord(0);
                tlv5.write(cookie);
                tlv5.write(utilities.hexStringToBytesArray("094613494C7F11D1822244455354000000"));
                tlv5.write(utilities.hexStringToBytesArray("0A00020001000F0000"));
                ByteBuffer tlv2711 = new ByteBuffer();
                tlv2711.write(utilities.hexStringToBytesArray("1B000A00000000000000000000000000000000000000030000000000000E000000000000000000000000000000010000000100"));
                ByteBuffer msg = new ByteBuffer();
                boolean utf8used = false;
                switch (encoding) {
                    case -1:
                        if (internalUTF8) {
                            msg.writeStringUTF8(this.text);
                            utf8used = true;
                        } else {
                            msg.writeString1251(this.text);
                        }
                        break;
                    case 0:
                        msg.writeStringUTF8(this.text);
                        utf8used = true;
                        break;
                    case 1:
                        msg.writeString1251A(this.text);
                        break;
                    case 2:
                        msg.writeStringUnicode(this.text);
                        break;
                    case 3:
                        msg.writeString1251(this.text);
                        break;
                }
                tlv2711.writeWordLE(msg.writePos + 1);
                tlv2711.write(msg.readBytes(msg.writePos));
                tlv2711.writeByte((byte) 0);
                tlv2711.writeDWord(0);
                tlv2711.writeDWord(-256);
                if (utf8used) {
                    tlv2711.writeDWordLE(38);
                    tlv2711.writeStringAscii("{0946134E-4C7F-11D1-8222-444553540000}");
                }
                tlv5.writeIcqTLV(tlv2711, 10001);
                main.writeIcqTLV(tlv5, 5);
                main.writeWord(3);
                main.writeWord(0);
                ByteBuffer snc = SNAC.createSnac(4, 6, 0, 6, main);
                this.data = FLAP.createFlap((byte) 2, this.sequence, snc);
            } catch (Exception e) {
                ////e = e;
                ////e.printStackTrace();
            }
        } catch (Exception e2) {
            ////e = e2;
        }
    }
}