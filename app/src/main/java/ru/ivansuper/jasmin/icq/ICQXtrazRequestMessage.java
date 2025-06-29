package ru.ivansuper.jasmin.icq;

import ru.ivansuper.jasmin.utilities;

public class ICQXtrazRequestMessage {
    public ByteBuffer data;
    public String receiver;
    public String sender;
    private final int sequence;

    public ICQXtrazRequestMessage(int seq, String Sender, String Receiver, int xsts) {
        this.sender = Sender;
        this.receiver = Receiver;
        this.sequence = seq;
        createMessage(xsts);
    }

    private void createMessage(int xsts) {
        ByteBuffer main = null;
        try {
            main = new ByteBuffer();
        } catch (Exception e) {
            ////e = e;
        }
        try {
            long cookie = System.currentTimeMillis();
            main.writeLong(cookie);
            main.writeWord(2);
            main.writeByte((byte) this.receiver.length());
            main.writeStringAscii(this.receiver);
            ByteBuffer tlv5 = new ByteBuffer();
            tlv5.writeWord(0);
            tlv5.writeLong(cookie);
            tlv5.write(utilities.hexStringToBytesArray("094613494C7F11D18222444553540000"));
            tlv5.write(utilities.hexStringToBytesArray("000A00020001000F0000"));
            ByteBuffer tlv2711 = new ByteBuffer();
            tlv2711.write(utilities.hexStringToBytesArray("1B000800000000000000000000000000000000000000030000000000000E0000000000000000000000000000001A0000000100"));
            tlv2711.writeWord(256);
            tlv2711.writeByte((byte) 0);
            tlv2711.writeWord(20224);
            tlv2711.write(utilities.hexStringToBytesArray("3B60B3EFD82A6C45A4E09C5A5E67E865"));
            tlv2711.writeWord(2048);
            tlv2711.writeWord(10752);
            tlv2711.writeWord(0);
            tlv2711.writeStringAscii("Script Plug-in: Remote Notification Arrive");
            tlv2711.write(utilities.hexStringToBytesArray("000001000000000000000000000000"));
            StringBuilder xTraz = new StringBuilder();
            xTraz.append("<N><QUERY>");
            xTraz.append(xstatus.makeXPromt("<Q><PluginID>srvMng</PluginID></Q>"));
            xTraz.append("</QUERY><NOTIFY>");
            xTraz.append(xstatus.makeXPromt("<srv><id>cAwaySrv</id><req><id>AwayStat</id><trans>" + xsts + "</trans><senderId>" + this.sender + "</senderId></req></srv>"));
            xTraz.append("</NOTIFY></N>");
            String plugin = xTraz.toString();
            tlv2711.writeDWordLE(plugin.length() + 4);
            tlv2711.writeDWordLE(plugin.length());
            tlv2711.writeStringAscii(xTraz.toString());
            tlv2711.writeWord(3338);
            tlv5.writeIcqTLV(tlv2711, 10001);
            main.writeIcqTLV(tlv5, 5);
            ByteBuffer snc = SNAC.createSnac(4, 6, 0, 6, main);
            this.data = FLAP.createFlap((byte) 2, this.sequence, snc);
        } catch (Exception e2) {
            ////e = e2;
            ////e.printStackTrace();
        }
    }
}