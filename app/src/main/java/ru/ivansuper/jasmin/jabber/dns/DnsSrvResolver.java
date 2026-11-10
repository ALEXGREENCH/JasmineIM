package ru.ivansuper.jasmin.jabber.dns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Vector;

public class DnsSrvResolver {
    public static final int XMPP_HTTPBIND = 3;
    public static final int XMPP_HTTPPOLL = 2;
    public static final int XMPP_TCP = 1;
    private static final String _bind = "_xmpp-client-xbosh";
    private static final String _poll = "_xmpp-client-httppoll";
    private static final String _srv = "_xmpp-client._tcp.";
    private static final String _txt = "_xmppconnect.";
    private String resolvedHost;
    private int resolvedPort = 5222;
    private String server;
    private long ttl;

    class SrvRdata {
        String host;
        int port;
        int ttl;

        SrvRdata() {
        }
    }

    private boolean askInetSrv(int i) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("8.8.8.8", 53));
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            byte[] bArrEncode = encode(this.server, i);
            byte[] bArr = new byte[bArrEncode.length + 2];
            System.arraycopy(bArrEncode, 0, bArr, 2, bArrEncode.length);
            bArr[0] = (byte) (bArrEncode.length >> 8);
            bArr[1] = (byte) bArrEncode.length;
            dataOutputStream.write(bArr);
            dataOutputStream.flush();
            byte[] bArr2 = new byte[2];
            dataInputStream.readFully(bArr2);
            byte[] bArr3 = new byte[(bArr2[1] & 255) | ((bArr2[0] << 8) & 255)];
            dataInputStream.readFully(bArr3);
            Vector vectorDecode = decode(bArr3);
            if (!vectorDecode.isEmpty() && vectorDecode.elementAt(0) != null) {
                if (i == 1) {
                    this.resolvedHost = ((SrvRdata) vectorDecode.elementAt(0)).host;
                    this.resolvedPort = ((SrvRdata) vectorDecode.elementAt(0)).port;
                    this.ttl = ((long) ((SrvRdata) vectorDecode.elementAt(0)).ttl) + System.currentTimeMillis();
                } else {
                    String str = i == 3 ? _bind : _poll;
                    for (int i2 = 0; i2 < vectorDecode.size(); i2++) {
                        SrvRdata srvRdata = (SrvRdata) vectorDecode.elementAt(i2);
                        if (srvRdata.host.startsWith(str)) {
                            this.resolvedHost = srvRdata.host.substring(srvRdata.host.indexOf("=") + 1, srvRdata.host.length());
                        }
                    }
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private Vector decode(byte[] bArr) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
            dataInputStream.readShort();
            dataInputStream.readShort();
            short s = dataInputStream.readShort();
            short s2 = dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();
            for (int i = 0; i < s; i++) {
                while (true) {
                    int unsignedByte = dataInputStream.readUnsignedByte();
                    if (unsignedByte == 0) {
                        break;
                    }
                    for (int i2 = 0; i2 < unsignedByte; i2++) {
                        dataInputStream.readUnsignedByte();
                    }
                }
                dataInputStream.readShort();
                dataInputStream.readShort();
            }
            Vector vector = new Vector();
            for (int i3 = 0; i3 < s2; i3++) {
                dataInputStream.readUnsignedShort();
                int unsignedShort = dataInputStream.readUnsignedShort();
                dataInputStream.readUnsignedShort();
                int i4 = dataInputStream.readInt();
                int unsignedShort2 = dataInputStream.readUnsignedShort();
                if (unsignedShort == 33) {
                    dataInputStream.readUnsignedShort();
                    dataInputStream.readUnsignedShort();
                    int unsignedShort3 = dataInputStream.readUnsignedShort();
                    StringBuffer stringBuffer = new StringBuffer();
                    while (true) {
                        int unsignedByte2 = dataInputStream.readUnsignedByte();
                        if (unsignedByte2 == 0) {
                            break;
                        }
                        for (int i5 = 0; i5 < unsignedByte2; i5++) {
                            stringBuffer.append((char) dataInputStream.readUnsignedByte());
                        }
                        stringBuffer.append('.');
                    }
                    if (443 == unsignedShort3) {
                        unsignedShort3 = 5222;
                    }
                    SrvRdata srvRdata = new SrvRdata();
                    srvRdata.host = stringBuffer.toString().substring(0, stringBuffer.length() - 1);
                    srvRdata.port = unsignedShort3;
                    srvRdata.ttl = i4;
                    vector.addElement(srvRdata);
                } else {
                    StringBuffer stringBuffer2 = new StringBuffer();
                    for (int i6 = 0; i6 < unsignedShort2; i6++) {
                        stringBuffer2.append((char) dataInputStream.readUnsignedByte());
                    }
                    SrvRdata srvRdata2 = new SrvRdata();
                    srvRdata2.host = stringBuffer2.toString().substring(1);
                    vector.addElement(srvRdata2);
                }
            }
            return vector;
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] encode(String str, int i) {
        String str2;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeShort(1638);
            dataOutputStream.writeByte(1);
            dataOutputStream.writeByte(0);
            dataOutputStream.writeShort(1);
            dataOutputStream.writeShort(0);
            dataOutputStream.writeShort(0);
            dataOutputStream.writeShort(0);
            StringBuffer stringBuffer = new StringBuffer();
            switch (i) {
                case 1:
                    str2 = _srv;
                    stringBuffer.append(str2);
                    break;
                case 2:
                case 3:
                    str2 = _txt;
                    stringBuffer.append(str2);
                    break;
            }
            stringBuffer.append(str);
            String[] strArrSplit = stringBuffer.toString().split("\\.");
            for (String str3 : strArrSplit) {
                byte[] bytes = str3.getBytes();
                dataOutputStream.writeByte(bytes.length);
                dataOutputStream.write(bytes);
            }
            dataOutputStream.writeByte(0);
            switch (i) {
                case 1:
                    dataOutputStream.writeShort(33);
                    break;
                case 2:
                case 3:
                    dataOutputStream.writeShort(16);
                    break;
            }
            dataOutputStream.writeShort(1);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    private String getSrvRecordName() {
        String str = "srv#" + this.server;
        return str.length() > 32 ? str.substring(0, 31) : str;
    }

    public String getHost() {
        return this.resolvedHost;
    }

    public int getPort() {
        return this.resolvedPort;
    }

    public boolean getSrv(String str, int i) {
        this.server = str;
        return askInetSrv(i);
    }
}
