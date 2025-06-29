package ru.ivansuper.jasmin.icq;

import android.util.Log;
import java.io.IOException;
import ru.ivansuper.jasmin.jabber.xml_utils;
import ru.ivansuper.jasmin.utilities;

public class ICQMessage {
    public int channel;
    public String client_ip;
    public byte[] cookie;
    public String file_name;
    public int file_size;
    public int files_count;
    public boolean is_file;
    public String message;
    public boolean more_than_one_file;
    public int msg_type;
    public int port;
    public String proxy_ip;
    public String sender;
    long timestamp;
    public int transfer_step;
    public int type;
    public boolean use_proxy;
    public String verified_ip;

    public ICQMessage(ByteBuffer buffer) {
        byte[] bArr = new byte[8];
        bArr[0] = 8;
        bArr[1] = 15;
        bArr[2] = 15;
        this.cookie = bArr;
        this.message = "";
        this.more_than_one_file = false;
        this.cookie = buffer.readBytes(8);
        this.channel = buffer.readWord();
        switch (this.channel) {
            case 1:
                parseChannel1(buffer);
                break;
            case 2:
                parseChannel2(buffer);
                break;
        }
    }

    public ICQMessage() {
        byte[] bArr = new byte[8];
        bArr[0] = 8;
        bArr[1] = 15;
        bArr[2] = 15;
        this.cookie = bArr;
        this.message = "";
        this.more_than_one_file = false;
    }

    private void parseChannel2(ByteBuffer buffer) {
        int len = buffer.readByte();
        this.sender = buffer.readStringAscii(len);
        buffer.skip(2);
        int tlvCount = buffer.readWord();
        buffer.skip(2);
        int len2 = buffer.readWord();
        buffer.skip(len2);
        buffer.skip(2);
        int len3 = buffer.readWord();
        buffer.skip(len3);
        buffer.skip(2);
        int len4 = buffer.readWord();
        buffer.skip(len4);
        TLVList list = new TLVList(buffer, tlvCount + 10);
        int count = list.getTLVCount(5);
        if (count > 0) {
            TLV tlv = list.getTLV(5, count);
            if (tlv != null) {
                ByteBuffer tlvData = tlv.getData();
                this.msg_type = tlvData.readWord();
                tlvData.skip(8);
                String guid = utilities.convertToHex(tlvData.readBytes(16));
                if (guid.equalsIgnoreCase("094613434C7F11D18222444553540000")) {
                    this.is_file = true;
                    if (this.msg_type == 0) {
                        TLVList tlv_list = new TLVList(tlvData, tlvData.writePos - tlvData.readPos, true);
                        TLV t = tlv_list.getTLV(16);
                        if (t != null) {
                            this.use_proxy = true;
                        }
                        TLV t2 = tlv_list.getTLV(10);
                        //noinspection DataFlowIssue
                        this.transfer_step = t2.getData().readWord();
                        TLV t3 = tlv_list.getTLV(2);
                        if (t3 != null) {
                            this.proxy_ip = t3.getData().readIPA();
                        }
                        TLV t4 = tlv_list.getTLV(3);
                        if (t4 != null) {
                            this.client_ip = t4.getData().readIPA();
                        }
                        TLV t5 = tlv_list.getTLV(4);
                        if (t5 != null) {
                            this.verified_ip = t5.getData().readIPA();
                        }
                        TLV t6 = tlv_list.getTLV(5);
                        if (t6 != null) {
                            this.port = t6.getData().readWord();
                        }
                        TLV t7 = tlv_list.getTLV(10001);
                        if (t7 != null) {
                            ByteBuffer tlvData2 = t7.getData();
                            int multiple_flag = tlvData2.readWord();
                            if (multiple_flag == 2) {
                                this.more_than_one_file = true;
                            }
                            this.files_count = tlvData2.readWord();
                            this.file_size = tlvData2.readDWord();
                            int len5 = (tlvData2.writePos - tlvData2.readPos) - 1;
                            this.file_name = null;
                            int backup = tlvData2.readPos;
                            try {
                                this.file_name = tlvData2.readStringUTF8(len5);
                            } catch (IOException e) {
                                tlvData2.readPos = backup;
                                this.file_name = tlvData2.readStringAscii(len5);
                            }
                            tlv_list.recycle();
                            return;
                        }
                        return;
                    }
                    return;
                }
                tlvData.skip(10);
                tlvData.skip(49);
                this.type = tlvData.readByte();
                tlvData.skip(5);
                int len6 = tlvData.readWordLE();
                int backup2 = tlvData.readPos;
                if (this.type == 1) {
                    try {
                        this.message = tlvData.readStringUTF8(len6 - 1);
                    } catch (Exception e2) {
                        tlvData.readPos = backup2;
                        this.message = tlvData.readString1251(len6 - 1);
                    }
                    this.message = xml_utils.decodeString(this.message);
                }
            }
            this.timestamp = System.currentTimeMillis();
            list.recycle();
        }
    }

    private void parseChannel1(ByteBuffer buffer) {
        int len = buffer.readByte();
        this.sender = buffer.readStringAscii(len);
        buffer.skip(2);
        int tlvCount = buffer.readWord() + 5;
        TLVList list = new TLVList(buffer, tlvCount);
        TLV tlv = list.getTLV(2);
        if (tlv != null) {
            this.type = 1;
            ByteBuffer tlvData = tlv.getData();
            tlvData.skip(2);
            int len2 = tlvData.readWord();
            tlvData.skip(len2 + 2);
            int len3 = tlvData.readWord() - 4;
            int chartype = tlvData.readWord();
            int charset = tlvData.readWord();
            Log.e("parseChannel1", "Chartype = " + chartype);
            Log.e("parseChannel1", "Charset = " + charset);
            byte[] raw = tlvData.readBytes(len3);
            this.message = StringConvertor.byteArrayToString(raw, 0, len3);
            Log.e("OFFLINE MESSAGES 2", this.message);
            this.message = xml_utils.decodeString(parseHtml(this.message));
            TLV time = list.getTLV(22);
            if (time != null) {
                this.timestamp = time.getData().readDWord();
                this.timestamp *= 1000;
            } else {
                this.timestamp = System.currentTimeMillis();
            }
        }
        list.recycle();
    }

    private String parseHtml(String source) {
        if (!source.startsWith("<HTML>") || !source.endsWith("</HTML>")) {
            return source;
        }
        StringBuilder res = new StringBuilder();
        int sz = source.length();
        boolean tag = false;
        for (int i = 0; i < sz; i++) {
            char chr = source.charAt(i);
            if (chr == '<') {
                tag = true;
            } else if (chr == '>') {
                tag = false;
            }
            if (!tag) {
                res.append(chr);
            }
        }
        return res.toString();
    }
}
