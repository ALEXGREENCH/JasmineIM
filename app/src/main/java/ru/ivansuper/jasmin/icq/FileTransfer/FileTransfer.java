package ru.ivansuper.jasmin.icq.FileTransfer;

import android.util.Log;
import java.math.BigDecimal;
import java.math.RoundingMode;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.icq.ByteBuffer;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.utilities;

/**
 * Abstract class representing a file transfer operation.
 * This class provides common functionalities and properties for file transfers,
 * such as contact information, cookie, direction, file name, file size, and port.
 * It also defines abstract methods for canceling and shutting down the transfer,
 * and concrete methods for creating proxy initialization packets and OFT2 packets.
 */
public abstract class FileTransfer {
    public ICQContact contact;
    public byte[] cookie;
    public int direction;
    public String file_name;
    public long file_size;
    public int port;

    public abstract void cancel();

    public abstract void shutDown();

    public ByteBuffer createProxyReceiveInit() {
        ByteBuffer data = new ByteBuffer();
        data.writeWord(1098);
        data.writeWord(4);
        data.writeDWord(0);
        data.writeWord(0);
        data.writeByte((byte) this.contact.profile.ID.length());
        data.writeStringAscii(this.contact.profile.ID);
        data.writeWord(this.port);
        data.write(this.cookie);
        data.writeWord(1);
        data.writeWord(16);
        data.write(utilities.hexStringToBytesArray("094613434C7F11D18222444553540000"));
        ByteBuffer proxy_packet = new ByteBuffer();
        proxy_packet.writeWord(data.writePos);
        proxy_packet.write(ByteBuffer.normalizeBytes(data.bytes, data.writePos));
        Log.d("ProxyReceiveInit", utilities.convertToHex(ByteBuffer.normalizeBytes(proxy_packet.bytes, proxy_packet.writePos)));
        return proxy_packet;
    }

    public ByteBuffer createProxySendInit() {
        ByteBuffer data = new ByteBuffer();
        data.writeWord(1098);
        data.writeWord(2);
        data.writeDWord(0);
        data.writeWord(0);
        data.writeByte((byte) this.contact.profile.ID.length());
        data.writeStringAscii(this.contact.profile.ID);
        data.write(this.cookie);
        data.writeWord(1);
        data.writeWord(16);
        data.write(utilities.hexStringToBytesArray("094613434C7F11D18222444553540000"));
        ByteBuffer proxy_packet = new ByteBuffer();
        proxy_packet.writeWord(data.writePos);
        proxy_packet.write(ByteBuffer.normalizeBytes(data.bytes, data.writePos));
        Log.e("ProxySendInit", utilities.convertToHex(ByteBuffer.normalizeBytes(proxy_packet.bytes, proxy_packet.writePos)));
        return proxy_packet;
    }

    public ByteBuffer createOFT2ForSend() {
        ByteBuffer data = new ByteBuffer();
        byte[] oft_header = {79, 70, 84, 50};
        data.write(oft_header);
        ByteBuffer oft2_packet = new ByteBuffer();
        oft2_packet.writeWord(ContactListActivity.SHOW_JABBER_CMD_FORM);
        oft2_packet.write(this.cookie);
        oft2_packet.writeDWord(0);
        oft2_packet.writeWord(1);
        oft2_packet.writeWord(1);
        oft2_packet.writeWord(1);
        oft2_packet.writeWord(1);
        oft2_packet.writeDWord((int) this.file_size);
        oft2_packet.writeDWord((int) this.file_size);
        oft2_packet.writeDWord(0);
        oft2_packet.writeDWord(0);
        oft2_packet.writeDWord(-65536);
        oft2_packet.writeDWord(0);
        oft2_packet.writeDWord(0);
        oft2_packet.writeDWord(-65536);
        oft2_packet.writeDWord(0);
        oft2_packet.writeDWord(-65536);
        oft2_packet.write(utilities.hexStringToBytesArray("436F6F6C2046696C655866657200000000000000000000000000000000000000201C1100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        oft2_packet.writeWord(0);
        oft2_packet.writeWord(0);
        if (this.file_name.length() + 1 >= 64) {
            oft2_packet.writeStringAscii(this.file_name);
            oft2_packet.writeByte((byte) 0);
        } else {
            oft2_packet.writeStringAscii(this.file_name);
            oft2_packet.writeByte((byte) 0);
            int count = 64 - (this.file_name.length() + 1);
            for (int i = 0; i < count; i++) {
                oft2_packet.writeByte((byte) 0);
            }
        }
        data.writeWord(oft2_packet.writePos + 6);
        data.write(ByteBuffer.normalizeBytes(oft2_packet.bytes, oft2_packet.writePos));
        return data;
    }

    public static String getSizeLabel(long size) {
        if ((double) size < 1024.0d) {
            return (double) size + " b";
        }
        if (size >= 1024 && size < 1048576) {
            return BigDecimal.valueOf((double) size / 1024.0d).setScale(2, RoundingMode.UP).doubleValue() + " KB";
        }
        if (size < 1048576) {
            return "[]";
        }
        return BigDecimal.valueOf(((double) size / 1024.0d) / 1024.0d).setScale(2, RoundingMode.UP).doubleValue() + " MB";
    }
}
