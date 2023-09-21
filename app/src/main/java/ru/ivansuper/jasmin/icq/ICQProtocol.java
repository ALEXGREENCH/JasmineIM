package ru.ivansuper.jasmin.icq;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/* loaded from: classes.dex */
public class ICQProtocol {
    public static ByteBuffer createHelloReply(int seq) {
        ByteBuffer buffer = new ByteBuffer(48);
        buffer.writeDWord(1);
        buffer.writeWord(32771);
        buffer.writeWord(4);
        buffer.writeWord(16);
        buffer.writeWord(0);
        ByteBuffer flp = FLAP.createFlap((byte) 1, seq, buffer);
        return flp;
    }

    public static ByteBuffer createGoodbye(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer flp = FLAP.createFlap((byte) 4, seq, buffer);
        return flp;
    }

    public static ByteBuffer createPing(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer flp = FLAP.createFlap((byte) 5, seq, buffer);
        return flp;
    }

    public static ByteBuffer createTypingNotify(int seq, String uin, int type) {
        ByteBuffer buffer = new ByteBuffer(48);
        buffer.writeDWord(0);
        buffer.writeDWord(0);
        buffer.writeWord(1);
        buffer.writeByte((byte) uin.length());
        buffer.writeStringAscii(uin);
        buffer.writeWord(type);
        ByteBuffer snc = SNAC.createSnac(4, 20, 0, 20, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createOfflineMsgsRequest(int seq, String uin) {
        ByteBuffer buffer = new ByteBuffer(64);
        ByteBuffer buf = new ByteBuffer(64);
        buf.writeWord(2048);
        buf.writeDWordLE(Integer.parseInt(uin));
        buf.writeWord(15360);
        buf.writeWord(seq + 1);
        buffer.writeIcqTLV(buf, 1);
        ByteBuffer snc = SNAC.createSnac(21, 2, 0, 64017, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createDeleteOfflineMsgsRequest(int seq, String uin) {
        ByteBuffer buffer = new ByteBuffer(64);
        ByteBuffer buf = new ByteBuffer(64);
        buf.writeWord(2048);
        buf.writeDWordLE(Integer.parseInt(uin));
        buf.writeWord(15872);
        buf.writeWord(seq + 1);
        buffer.writeIcqTLV(buf, 1);
        ByteBuffer snc = SNAC.createSnac(21, 2, 0, 0, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createMD5Login(byte[] key, int seq, String uin, String password) throws Exception {
        ByteBuffer buffer = new ByteBuffer(512);
        buffer.writeWord(1);
        buffer.writePreLengthStringAscii(uin);
        buffer.writeWord(37);
        byte[] md5 = utilities.getHashArray(key, password);
        buffer.writeWord(md5.length);
        buffer.write(md5);
        ByteBuffer snc = SNAC.createSnac(23, 2, 0, 0, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createXORLogin(int seq, String uin, String password) throws Exception {
        String pass = password.length() > 8 ? password.substring(0, 8) : password;
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeDWord(1);
        buffer.writeWord(1);
        buffer.writePreLengthStringAscii(uin);
        buffer.writeWord(2);
        byte[] xor = utilities.xorPass(pass);
        buffer.writeWord(xor.length);
        buffer.write(xor);
        ByteBuffer flp = FLAP.createFlap((byte) 1, seq, buffer);
        return flp;
    }

    public static ByteBuffer createEMAILLogin(int seq, String email, String password) throws Exception {
        String pass = password.length() > 8 ? password.substring(0, 8) : password;
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeDWord(1);
        buffer.writeWord(86);
        buffer.writeWord(0);
        buffer.writeWord(1);
        buffer.writePreLengthStringAscii(email);
        buffer.writeWord(2);
        byte[] xor = utilities.xorPass(pass);
        buffer.writeWord(xor.length);
        buffer.write(xor);
        ByteBuffer flp = FLAP.createFlap((byte) 1, seq, buffer);
        return flp;
    }

    public static ByteBuffer createSendCookies(byte[] cookies, String uin, int seq) {
        ByteBuffer buffer = new ByteBuffer(cookies.length + ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeDWord(1);
        buffer.writeWord(6);
        buffer.writeWord(cookies.length);
        buffer.write(cookies);
        ByteBuffer flp = FLAP.createFlap((byte) 1, seq, buffer);
        return flp;
    }

    public static ByteBuffer createRequestRoster(int seq) {
        ByteBuffer buffer = new ByteBuffer(16);
        ByteBuffer snc = SNAC.createSnac(19, 4, 0, 0, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createCheckRoster(int seq) {
        ByteBuffer buffer = new ByteBuffer(16);
        buffer.writeDWord(1271737088);
        buffer.writeWord(22);
        ByteBuffer snc = SNAC.createSnac(19, 5, 0, 65541, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSetStatus(int seq, int status, int flags) {
        ByteBuffer data = new ByteBuffer(1024);
        data.writeWord(6);
        data.writeWord(4);
        data.writeWord(flags);
        data.writeWord(status);
        ByteBuffer snc = SNAC.createSnac(1, 30, 0, 30, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSetAwayText(int seq, String text) {
        String away = text.length() > 253 ? String.valueOf(text.substring(0, 249)) + " ..." : text;
        ByteBuffer data = new ByteBuffer(512);
        ByteBuffer buffer = new ByteBuffer(512);
        if (text.length() > 0) {
            buffer.writeWord(2);
            buffer.writeByte((byte) 4);
            byte[] raw = null;
            try {
                raw = away.getBytes("utf8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            buffer.writeByte((byte) (raw.length + 2));
            buffer.writeWord(raw.length);
            try {
                buffer.write(raw);
            } catch (Exception e) {
                e.printStackTrace();
            }
            buffer.writeWord(0);
            buffer.writeWord(14);
            buffer.writeWord(8);
            buffer.writeStringAscii("icqmood5");
        } else {
            buffer.writeWord(2);
            buffer.writeByte((byte) 0);
            buffer.writeByte((byte) 0);
            buffer.writeWord(14);
            buffer.writeWord(0);
        }
        data.writeIcqTLV(buffer, 29);
        ByteBuffer snc = SNAC.createSnac(1, 30, 0, 30, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSetDCInfo(int seq, int status, int flags, int protoVer) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        ByteBuffer buf = new ByteBuffer(16);
        buf.writeWord(flags);
        buf.writeWord(status);
        buffer.writeIcqTLV(buf, 6);
        ByteBuffer buf2 = new ByteBuffer(16);
        buf2.writeWord(0);
        buffer.writeIcqTLV(buf2, 8);
        ByteBuffer buf3 = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buf3.writeWord(0);
        buf3.writeWord(0);
        buf3.writeDWord(0);
        buf3.writeByte((byte) 4);
        buf3.writeWord(protoVer);
        buf3.writeWord(0);
        buf3.writeWord(0);
        buf3.writeDWord(0);
        buf3.writeDWord(0);
        buf3.writeDWord(0);
        buf3.writeDWord(0);
        buf3.writeDWord(0);
        buf3.writeWord(0);
        buffer.writeIcqTLV(buf3, 12);
        ByteBuffer buf4 = new ByteBuffer();
        buf4.writeWord(0);
        buffer.writeIcqTLV(buf4, 31);
        ByteBuffer snc = SNAC.createSnac(1, 30, 0, 30, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createClientFamilies(int seq) {
        ByteBuffer buffer = new ByteBuffer(64);
        buffer.writeDWord(2228225);
        buffer.writeDWord(65540);
        buffer.writeDWord(1245188);
        buffer.writeDWord(131073);
        buffer.writeDWord(196609);
        buffer.writeDWord(1376257);
        buffer.writeDWord(262145);
        buffer.writeDWord(393217);
        buffer.writeDWord(589825);
        buffer.writeDWord(655361);
        buffer.writeDWord(720897);
        ByteBuffer snc = SNAC.createSnac(1, 23, 0, 23, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createClientFamiliesAvatar(int seq) {
        ByteBuffer buffer = new ByteBuffer(16);
        buffer.writeWord(1);
        buffer.writeWord(4);
        buffer.writeWord(16);
        buffer.writeWord(1);
        ByteBuffer snc = SNAC.createSnac(1, 23, 0, 23, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createClientReady(int seq) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeDWord(2228225);
        buffer.writeDWord(17831503);
        buffer.writeDWord(65540);
        buffer.writeDWord(17831503);
        buffer.writeDWord(1245188);
        buffer.writeDWord(17831503);
        buffer.writeDWord(131073);
        buffer.writeDWord(17831503);
        buffer.writeDWord(196609);
        buffer.writeDWord(17831503);
        buffer.writeDWord(1376257);
        buffer.writeDWord(17831503);
        buffer.writeDWord(262145);
        buffer.writeDWord(17831503);
        buffer.writeDWord(393217);
        buffer.writeDWord(17831503);
        buffer.writeDWord(589825);
        buffer.writeDWord(17831503);
        buffer.writeDWord(655361);
        buffer.writeDWord(17831503);
        buffer.writeDWord(720897);
        buffer.writeDWord(17831503);
        ByteBuffer snc = SNAC.createSnac(1, 2, 0, 23, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createClientReadyAvatar(int seq) {
        ByteBuffer buffer = new ByteBuffer(64);
        buffer.writeDWord(65540);
        buffer.writeDWord(1050852);
        buffer.writeDWord(1048577);
        buffer.writeDWord(1050852);
        ByteBuffer snc = SNAC.createSnac(1, 2, 0, 23, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSetICBM(int seq) {
        ByteBuffer buffer = new ByteBuffer(64);
        buffer.writeWord(0);
        buffer.writeDWord(1803);
        buffer.writeWord(8000);
        buffer.writeWord(999);
        buffer.writeWord(999);
        buffer.writeWord(0);
        buffer.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(4, 2, 0, 2, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSetUserInfo(int seq, int x, String qip_status) {
        ByteBuffer buffer = new ByteBuffer(256);
        buffer.write(utilities.hexStringToBytesArray("4a61736d696e65204943512023232323"));
        buffer.write(utilities.hexStringToBytesArray("4a61736d696e6520766572ff"));
        String[] ver = resources.VERSION.split("\\.");
        buffer.writeByte(Byte.parseByte(ver[0]));
        buffer.writeByte(Byte.parseByte(ver[1]));
        buffer.writeByte(Byte.parseByte(ver[2]));
        buffer.writeByte((byte) 0);
        buffer.write(utilities.hexStringToBytesArray("094600004C7F11D18222444553540000"));
        buffer.write(utilities.hexStringToBytesArray("094613494C7F11D18222444553540000"));
        buffer.write(utilities.hexStringToBytesArray("0946134E4C7F11D18222444553540000"));
        buffer.write(utilities.hexStringToBytesArray("094613434C7F11D18222444553540000"));
        buffer.write(utilities.hexStringToBytesArray("563FC8090B6F41BD9F79422609DFA2F3"));
        buffer.write(utilities.hexStringToBytesArray("1A093C6CD7FD4EC59D51A6474E34F5A0"));
        buffer.write(utilities.hexStringToBytesArray("094600004C7F11D18222444553540000"));
        buffer.write(utilities.hexStringToBytesArray("0946134D4C7F11D18222444553540000"));
        if (x != -1) {
            buffer.write(utilities.hexStringToBytesArray(xstatus.guids[x]));
        }
        if (qip_status != null) {
            buffer.write(utilities.hexStringToBytesArray(qip_status));
        }
        ByteBuffer buf = new ByteBuffer(256);
        buf.writeIcqTLV(buffer, 5);
        ByteBuffer snc = SNAC.createSnac(2, 4, 0, 4, buf);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createUpdateSSIInfo(int seq, int id, int param, boolean my_info) {
        int s;
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeDWord(0);
        buffer.writeWord(id);
        buffer.writeWord(4);
        buffer.writeWord(5);
        buffer.writeWord(202);
        buffer.writeWord(1);
        buffer.writeByte((byte) param);
        if (my_info) {
            s = 135185;
        } else {
            s = 131089;
        }
        ByteBuffer snc = SNAC.createSnac(19, 9, 0, s, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createContactRename(int seq, String uin, String newNick, int group, int id, boolean have_auth_flag) {
        ByteBuffer buffer = new ByteBuffer(512);
        buffer.writeWord(uin.length());
        buffer.writeStringAscii(uin);
        buffer.writeWord(group);
        buffer.writeWord(id);
        buffer.writeWord(0);
        ByteBuffer buf = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        try {
            buf.writeStringUTF8(newNick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!have_auth_flag) {
            buffer.writeWord(buf.writePos + 4);
        } else {
            buffer.writeWord(buf.writePos + 8);
            buffer.writeWord(102);
            buffer.writeWord(0);
        }
        buffer.writeIcqTLV(buf, 305);
        ByteBuffer snc = SNAC.createSnac(19, 9, 0, 131075, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createGroupRename(int seq, ICQGroup group, ICQProfile profile, String new_name) {
        ByteBuffer buffer = new ByteBuffer(512);
        try {
            byte[] raw = group.name.getBytes("utf8");
            buffer.writeWord(raw.length);
            buffer.write(raw);
            buffer.writeWord(group.id);
            buffer.writeWord(0);
            buffer.writeWord(1);
            Vector<ICQContact> list = profile.contactlist.getContactsByGroupId(group.id);
            if (list.size() != 0) {
                buffer.writeWord((list.size() * 2) + 4);
                buffer.writeWord(200);
                buffer.writeWord(list.size() * 2);
                Iterator<ICQContact> it = list.iterator();
                while (it.hasNext()) {
                    ICQContact contact = it.next();
                    buffer.writeWord(contact.id);
                }
            } else {
                buffer.writeWord(0);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ByteBuffer snc = SNAC.createSnac(19, 9, 0, 131104, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSSIEditStart(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(19, 17, 0, 17, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSSIEditEnd(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(19, 18, 0, 18, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAddToLists(int seq, ssi_item item, int listType) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        String sUIN = item.uin;
        buffer.writeWord(sUIN.length());
        buffer.writeStringAscii(sUIN);
        buffer.writeWord(0);
        buffer.writeWord(item.id);
        buffer.writeWord(listType);
        buffer.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 8, 0, 131073, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createRemoveFromLists(int seq, ssi_item item, int listType) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        String sUIN = item.uin;
        buffer.writeWord(sUIN.length());
        buffer.writeStringAscii(sUIN);
        buffer.writeWord(0);
        buffer.writeWord(item.id);
        buffer.writeWord(listType);
        buffer.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 10, 0, 131073, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createContactDelete(int seq, ICQContact contact) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        String sUIN = contact.ID;
        buffer.writeWord(sUIN.length());
        buffer.writeStringAscii(sUIN);
        buffer.writeWord(contact.group);
        buffer.writeWord(contact.id);
        buffer.writeWord(0);
        buffer.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 10, 0, 131082, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createGroupDelete(int seq, ICQGroup group, ICQProfile profile) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        try {
            byte[] raw = group.name.getBytes("utf8");
            buffer.writeWord(raw.length);
            buffer.write(raw);
            buffer.writeWord(group.id);
            buffer.writeWord(0);
            buffer.writeWord(1);
            buffer.writeWord(0);
        } catch (Exception e) {
        }
        ByteBuffer snc = SNAC.createSnac(19, 10, 0, 131105, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createContactDelete(int seq, String itemName, int group, int id, int type) {
        ByteBuffer buffer = new ByteBuffer(512);
        buffer.writeWord(itemName.length());
        buffer.writeStringAscii(itemName);
        buffer.writeWord(group);
        buffer.writeWord(id);
        buffer.writeWord(type);
        buffer.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 10, 0, 131082, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAddRosterIconRecord(String name, int group, int id, int seq) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeWord(name.length());
        buffer.writeStringAscii(name);
        buffer.writeWord(group);
        buffer.writeWord(id);
        buffer.writeWord(20);
        buffer.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 8, 0, 10, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAddContact(int seq, ICQContact contact) throws Exception {
        ByteBuffer buffer = new ByteBuffer(2048);
        String sUIN = contact.ID;
        buffer.writeWord(sUIN.length());
        buffer.writeStringAscii(sUIN);
        buffer.writeWord(contact.group);
        buffer.writeWord(contact.id);
        buffer.writeWord(0);
        ByteBuffer utf = new ByteBuffer(512);
        utf.writeStringUTF8(contact.name);
        buffer.writeWord(utf.writePos + 4);
        buffer.writeWord(305);
        buffer.writeWord(utf.writePos);
        buffer.write(ByteBuffer.normalizeBytes(utf.bytes, utf.writePos));
        ByteBuffer snc = SNAC.createSnac(19, 8, 0, 131080, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAddGroup(int seq, ICQGroup group) {
        ByteBuffer buffer = new ByteBuffer(512);
        try {
            byte[] raw = group.name.getBytes("utf8");
            buffer.writeWord(raw.length);
            buffer.write(raw);
            buffer.writeWord(group.id);
            buffer.writeWord(0);
            buffer.writeWord(1);
            buffer.writeWord(0);
        } catch (Exception e) {
        }
        ByteBuffer snc = SNAC.createSnac(19, 8, 0, 131106, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAddNotAuthContact(int seq, ICQContact contact) throws Exception {
        ByteBuffer buffer = new ByteBuffer(512);
        String sUIN = contact.ID;
        buffer.writeWord(sUIN.length());
        buffer.writeStringAscii(sUIN);
        buffer.writeWord(contact.group);
        buffer.writeWord(contact.id);
        buffer.writeWord(0);
        ByteBuffer temp = new ByteBuffer(256);
        ByteBuffer utf = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        utf.writeStringUTF8(contact.name);
        temp.writeWord(305);
        temp.writeWord(utf.writePos);
        temp.write(ByteBuffer.normalizeBytes(utf.bytes, utf.writePos));
        temp.writeWord(102);
        temp.writeWord(0);
        buffer.writeWord(temp.writePos);
        buffer.write(ByteBuffer.normalizeBytes(temp.bytes, temp.writePos));
        ByteBuffer snc = SNAC.createSnac(19, 8, 0, 131080, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAnotherOfflineMsgsRequest(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(4, 16, 0, 262431, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createLoginAuthorizationRequest(int seq, String uin) {
        ByteBuffer buffer = new ByteBuffer(64);
        buffer.writeWord(1);
        buffer.writePreLengthStringAscii(uin);
        ByteBuffer snc = SNAC.createSnac(23, 6, 0, 0, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAuthorizationRequest(int seq, String uin) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeByte((byte) uin.length());
        buffer.writeStringAscii(uin);
        String reason = resources.getString("s_icq_authorize_text");
        ByteBuffer r = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        try {
            r.writeStringUTF8(reason);
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.writeWord(r.writePos);
        buffer.write(ByteBuffer.normalizeBytes(r.bytes, r.writePos));
        buffer.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 24, 0, 24, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createFutureAuthGrand(int seq, String uin) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeByte((byte) uin.length());
        buffer.writeStringAscii(uin);
        buffer.writeDWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 20, 0, 20, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAuthReply(int seq, String uin, int reply) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeByte((byte) uin.length());
        buffer.writeStringAscii(uin);
        buffer.writeByte((byte) reply);
        buffer.writeDWord(0);
        ByteBuffer snc = SNAC.createSnac(19, 26, 0, 26, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createRatesRequest(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(1, 6, 0, 6, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createContactInfoRequest(int seq, String profileId, String uin, int flag) {
        ByteBuffer data = new ByteBuffer(256);
        data.writeWord(1);
        data.writeWord(16);
        data.writeWordLE(14);
        data.writeDWordLE(Integer.parseInt(profileId));
        data.writeWord(53255);
        data.writeWordLE(seq);
        data.writeWord(45572);
        data.writeDWordLE(Integer.parseInt(uin));
        ByteBuffer snc = SNAC.createSnac(21, 2, 0, flag, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createInfoChange(int seq, String profileId, InfoContainer container) {
        ByteBuffer tlv1 = new ByteBuffer(1024);
        ByteBuffer tlv1data = new ByteBuffer(1024);
        ByteBuffer tlv1subdata = new ByteBuffer(1024);
        tlv1subdata.writeDWordLE(Integer.parseInt(profileId));
        tlv1subdata.writeWord(53255);
        tlv1subdata.writeWordLE(seq + 1);
        tlv1subdata.writeWord(14860);
        tlv1subdata.write1251TLV(340, container.nickname);
        tlv1subdata.write1251TLV(320, container.name);
        tlv1subdata.write1251TLV(330, container.surname);
        tlv1subdata.writeByteTLV(380, (byte) container.sex_);
        tlv1subdata.writeWordLE(570);
        tlv1subdata.writeWordLE(6);
        tlv1subdata.writeWordLE(container.birthyear);
        tlv1subdata.writeWordLE(container.birthmonth);
        tlv1subdata.writeWordLE(container.birthday);
        tlv1subdata.write1251TLV(JConference.BANNED_LIST_RECEIVED, container.city);
        tlv1subdata.write1251TLV(350, container.email);
        tlv1subdata.write1251TLV(350, container.email);
        tlv1subdata.write1251TLV(350, container.email);
        tlv1subdata.write1251TLV(531, container.homepage);
        tlv1subdata.write1251TLV(600, container.about);
        tlv1data.writeWordLE(tlv1subdata.writePos);
        tlv1data.writeByteBuffer(tlv1subdata);
        tlv1.writeIcqTLV(tlv1data, 1);
        ByteBuffer snc = SNAC.createSnac(21, 2, 1, 1023, tlv1);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createReqLocation(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(2, 2, 0, 2, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createDelYourself(int seq, String uin) {
        ByteBuffer buffer = new ByteBuffer(16);
        buffer.writeByte((byte) uin.length());
        buffer.writeStringAscii(uin);
        ByteBuffer snc = SNAC.createSnac(19, 22, 0, 0, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createReqBuddy(int seq) {
        ByteBuffer buffer = new ByteBuffer(16);
        buffer.writeWord(5);
        buffer.writeWord(2);
        buffer.writeWord(3);
        ByteBuffer snc = SNAC.createSnac(3, 2, 0, 2, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createReqICBM(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(4, 4, 0, 4, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createReqBOS(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(9, 2, 0, 2, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createRosterAck(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(19, 7, 0, 7, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createReqLists(int seq) {
        ByteBuffer buffer = new ByteBuffer(16);
        buffer.writeWord(11);
        buffer.writeWord(2);
        buffer.writeWord(15);
        ByteBuffer snc = SNAC.createSnac(19, 2, 0, 2, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createReqInfo(int seq) {
        ByteBuffer buffer = new ByteBuffer(0);
        ByteBuffer snc = SNAC.createSnac(1, 14, 0, 14, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAckRates(int seq, int groups) {
        ByteBuffer buffer = new ByteBuffer(1024);
        for (int i = 0; i < groups; i++) {
            buffer.writeWord(i + 1);
        }
        ByteBuffer snc = SNAC.createSnac(1, 8, 0, 8, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAvatarRequest(byte[] hash, int id, int flag, String uin, int seq) {
        ByteBuffer buffer = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        buffer.writeByte((byte) uin.length());
        buffer.writeStringAscii(uin);
        buffer.writeByte((byte) 1);
        buffer.writeWord(id);
        buffer.writeByte((byte) flag);
        buffer.writeByte((byte) hash.length);
        buffer.write(hash);
        ByteBuffer snc = SNAC.createSnac(16, 6, 0, 6, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAvatarServiceRequest(int seq) {
        ByteBuffer buffer = new ByteBuffer(16);
        buffer.writeWord(16);
        ByteBuffer snc = SNAC.createSnac(1, 4, 0, 4, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createMsgAck(int seq, byte[] cookie, String receiver) throws Exception {
        ByteBuffer buffer = new ByteBuffer(256);
        if (cookie != null) {
            buffer.write(cookie);
        } else {
            buffer.writeDWord(0);
            buffer.writeDWord(0);
        }
        buffer.writeWord(2);
        buffer.writeByte((byte) receiver.length());
        buffer.writeStringAscii(receiver);
        buffer.writeWord(3);
        buffer.write(utilities.hexStringToBytesArray("1B00090000000000000000000000000000000000000001000000008CBE0E008CBE00000000000000000000000001000000000001000000000000FFFFFF00"));
        ByteBuffer snc = SNAC.createSnac(4, 11, 0, 11, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createXtrazAnswer(int seq, byte[] cookie, String receiver, ICQProfile profile) throws Exception {
        ByteBuffer buffer = new ByteBuffer(4192);
        buffer.write(cookie);
        buffer.writeWord(2);
        buffer.writeByte((byte) receiver.length());
        buffer.writeStringAscii(receiver);
        buffer.writeWord(3);
        buffer.write(utilities.hexStringToBytesArray("1B000800000000000000000000000000000000000000010000000000000E0000000000000000000000000000001A0000000000010000"));
        buffer.writeWord(20224);
        buffer.write(utilities.hexStringToBytesArray("3B60B3EFD82A6C45A4E09C5A5E67E865"));
        buffer.writeWord(2048);
        buffer.writeWord(10752);
        buffer.writeWord(0);
        buffer.writeStringAscii("Script Plug-in: Remote Notification Arrive");
        buffer.write(utilities.hexStringToBytesArray("000001000000000000000000000000"));
        ByteBuffer pluginData = new ByteBuffer();
        String str = "<ret event='OnRemoteNotification'><srv><id>cAwaySrv</id><val srv_id='cAwaySrv'><Root><CASXtraSetAwayMessage></CASXtraSetAwayMessage><uin>" + profile.ID + "</uin><index>1</index><title>" + xstatus.makeXPromt(profile.xtitle) + "</title><desc>" + xstatus.makeXPromt(profile.xdesc) + "</desc></Root></val></srv></ret>";
        String data = "<NR><RES>" + xstatus.makeXPromt(str) + "</RES></NR>";
        Log.e("ICQProtocol", "XTraz:\n" + data);
        pluginData.writeStringUTF8(data);
        pluginData.writeWord(3338);
        buffer.writeWordLE(pluginData.writePos + 4);
        buffer.writeWordLE(0);
        buffer.writeWordLE(pluginData.writePos);
        buffer.writeWordLE(0);
        buffer.write(ByteBuffer.normalizeBytes(pluginData.bytes, pluginData.writePos));
        ByteBuffer snc = SNAC.createSnac(4, 11, 0, 11, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createTransferAccept(byte[] cookie, String uin, int seq) {
        ByteBuffer data = new ByteBuffer(512);
        data.write(cookie);
        data.writeWord(2);
        data.writeByte((byte) uin.length());
        data.writeStringAscii(uin);
        ByteBuffer tlv5 = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        tlv5.writeWord(2);
        tlv5.write(cookie);
        tlv5.write(utilities.hexStringToBytesArray("094613434C7F11D18222444553540000"));
        data.writeIcqTLV(tlv5, 5);
        data.writeWord(3);
        data.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(4, 6, 0, 6, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createRedirectFromLocalToInverseProxy(byte[] cookie, String uin, byte[] ip, int port, int seq) {
        ByteBuffer data = new ByteBuffer(1280);
        data.write(cookie);
        data.writeWord(2);
        data.writeByte((byte) uin.length());
        data.writeStringAscii(uin);
        ByteBuffer tlv5 = new ByteBuffer(256);
        tlv5.writeWord(0);
        tlv5.write(cookie);
        tlv5.write(utilities.hexStringToBytesArray("094613434C7F11D18222444553540000"));
        tlv5.writeWord(10);
        tlv5.writeWord(2);
        tlv5.writeWord(2);
        tlv5.writeWord(2);
        tlv5.writeWord(4);
        tlv5.write(ip);
        tlv5.writeWord(22);
        tlv5.writeWord(4);
        tlv5.writeByte((byte) (255 - ip[0]));
        tlv5.writeByte((byte) (255 - ip[1]));
        tlv5.writeByte((byte) (255 - ip[2]));
        tlv5.writeByte((byte) (255 - ip[3]));
        tlv5.writeWord(5);
        tlv5.writeWord(2);
        tlv5.writeWord(port);
        tlv5.writeWord(23);
        tlv5.writeWord(2);
        tlv5.writeWord(getPortCheck(port));
        tlv5.writeWord(16);
        tlv5.writeWord(0);
        data.writeIcqTLV(tlv5, 5);
        ByteBuffer snc = SNAC.createSnac(4, 6, 0, 6, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createFileTransferSendRequest(byte[] cookie, String uin, byte[] ip, int port, File file, int seq) throws Exception {
        ByteBuffer data = new ByteBuffer(1280);
        data.write(cookie);
        data.writeWord(2);
        data.writeByte((byte) uin.length());
        data.writeStringAscii(uin);
        ByteBuffer tlv5 = new ByteBuffer(256);
        tlv5.writeWord(0);
        tlv5.write(cookie);
        tlv5.write(utilities.hexStringToBytesArray("094613434C7F11D18222444553540000"));
        tlv5.writeWord(10);
        tlv5.writeWord(2);
        tlv5.writeWord(1);
        tlv5.writeWord(15);
        tlv5.writeWord(0);
        tlv5.writeWord(2);
        tlv5.writeWord(4);
        tlv5.write(ip);
        tlv5.writeWord(22);
        tlv5.writeWord(4);
        tlv5.writeByte((byte) (255 - ip[0]));
        tlv5.writeByte((byte) (255 - ip[1]));
        tlv5.writeByte((byte) (255 - ip[2]));
        tlv5.writeByte((byte) (255 - ip[3]));
        tlv5.writeWord(3);
        tlv5.writeWord(4);
        tlv5.write(ip);
        tlv5.writeWord(5);
        tlv5.writeWord(2);
        tlv5.writeWord(port);
        tlv5.writeWord(23);
        tlv5.writeWord(2);
        tlv5.writeWord(getPortCheck(port));
        tlv5.writeWord(16);
        tlv5.writeWord(0);
        ByteBuffer rendezvous = new ByteBuffer(512);
        rendezvous.writeWord(1);
        rendezvous.writeWord(1);
        rendezvous.writeDWord((int) file.length());
        rendezvous.writeStringUTF8(file.getName());
        rendezvous.writeByte((byte) 0);
        tlv5.writeIcqTLV(rendezvous, 10001);
        ByteBuffer encoding = new ByteBuffer();
        encoding.writeStringAscii("utf-8");
        tlv5.writeIcqTLV(encoding, 10002);
        data.writeIcqTLV(tlv5, 5);
        ByteBuffer snc = SNAC.createSnac(4, 6, 0, 6, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    private static int getPortCheck(int port) {
        byte a = (byte) (port >> 8);
        byte b = (byte) port;
        byte b2 = (byte) (255 - b);
        int result = (((byte) (255 - a)) << 8) | b2;
        return result;
    }

    public static ByteBuffer createTransferCancel(byte[] cookie, String uin, int seq) {
        ByteBuffer data = new ByteBuffer(512);
        data.write(cookie);
        data.writeWord(2);
        data.writeByte((byte) uin.length());
        data.writeStringAscii(uin);
        ByteBuffer tlv5 = new ByteBuffer((int) ContactListActivity.UPDATE_BLINK_STATE);
        tlv5.writeWord(1);
        tlv5.write(cookie);
        tlv5.write(utilities.hexStringToBytesArray("094613434C7F11D18222444553540000"));
        data.writeIcqTLV(tlv5, 5);
        data.writeWord(3);
        data.writeWord(0);
        ByteBuffer snc = SNAC.createSnac(4, 6, 0, 6, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createAvatarUpload(File file, int seq) throws Exception {
        ByteBuffer data = new ByteBuffer();
        data.writeWord(1);
        data.writeWord((int) file.length());
        int readed_summ = 0;
        FileInputStream in = new FileInputStream(file);
        while (readed_summ < file.length()) {
            byte[] raw = new byte[GifDecoder.MaxStackSize];
            int readed = in.read(raw, 0, GifDecoder.MaxStackSize);
            if (readed > 0) {
                byte[] buffer = new byte[readed];
                System.arraycopy(raw, 0, buffer, 0, readed);
                data.write(buffer);
                readed_summ += readed;
            }
        }
        ByteBuffer snc = SNAC.createSnac(16, 2, 0, 10, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createUpdateIconHash(byte[] hash, String name, int group, int id, int seq) {
        ByteBuffer data = new ByteBuffer(256);
        data.writeWord(name.length());
        data.writeStringAscii(name);
        data.writeWord(0);
        data.writeWord(id);
        data.writeWord(20);
        ByteBuffer tlv = new ByteBuffer(64);
        tlv.writeByte((byte) 1);
        tlv.writeByte((byte) hash.length);
        tlv.write(hash);
        data.writeWord(tlv.writePos + 4);
        data.writeIcqTLV(tlv, 213);
        ByteBuffer snc = SNAC.createSnac(19, 9, 0, 10, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createSearchRequest(String ID, SearchCriteries criteries, int seq) {
        ByteBuffer data = new ByteBuffer(1024);
        ByteBuffer tlv1 = new ByteBuffer(512);
        ByteBuffer tlv1_data = new ByteBuffer(512);
        tlv1_data.writeDWordLE(Integer.parseInt(ID));
        tlv1_data.writeWordLE(2000);
        tlv1_data.writeWordLE(seq + 1024);
        tlv1_data.writeWordLE(popup_log_adapter.PRESENSE_DISPLAY_TIME);
        ByteBuffer tlv1_subdata = new ByteBuffer(512);
        tlv1_subdata.writeWord(1465);
        tlv1_subdata.writeWord(popup_log_adapter.PRESENSE_DISPLAY_TIME);
        tlv1_subdata.write(utilities.hexStringToBytesArray("000000000000000004E3000000020002"));
        tlv1_subdata.writeWord(criteries.page);
        tlv1_subdata.writeWord(1);
        ByteBuffer criteries_block = new ByteBuffer(512);
        if (criteries.nick.length() > 0) {
            byte[] raw_nick = utilities.prepareUTF8(criteries.nick);
            criteries_block.writeWord(120);
            criteries_block.writeWord(raw_nick.length);
            criteries_block.write(raw_nick);
        }
        if (criteries.name.length() > 0) {
            byte[] raw_name = utilities.prepareUTF8(criteries.name);
            criteries_block.writeWord(100);
            criteries_block.writeWord(raw_name.length);
            criteries_block.write(raw_name);
        }
        if (criteries.lastname.length() > 0) {
            byte[] raw_lastname = utilities.prepareUTF8(criteries.lastname);
            criteries_block.writeWord(110);
            criteries_block.writeWord(raw_lastname.length);
            criteries_block.write(raw_lastname);
        }
        if (criteries.gender != 0) {
            criteries_block.writeWord(130);
            criteries_block.writeWord(1);
            criteries_block.writeByte((byte) criteries.gender);
        }
        if (criteries.city.length() > 0) {
            byte[] raw_city = utilities.prepareUTF8(criteries.city);
            criteries_block.writeWord(160);
            criteries_block.writeWord(raw_city.length);
            criteries_block.write(raw_city);
        }
        tlv1_subdata.writeWord(criteries_block.writePos);
        tlv1_subdata.write(criteries_block.getBytes());
        tlv1_data.writeWordLE(tlv1_subdata.writePos);
        tlv1_data.write(tlv1_subdata.getBytes());
        tlv1.writeWordLE(tlv1_data.writePos);
        tlv1.write(tlv1_data.getBytes());
        data.writeIcqTLV(tlv1, 1);
        ByteBuffer snc = SNAC.createSnac(21, 2, 0, 10, data);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static ByteBuffer createInvalidPacket(int seq) {
        ByteBuffer buffer = new ByteBuffer(64);
        buffer.writeByte((byte) 9);
        ByteBuffer snc = SNAC.createSnac(4, 6, 0, 15, buffer);
        ByteBuffer flp = FLAP.createFlap((byte) 2, seq, snc);
        return flp;
    }

    public static final String preparePassword(String source) {
        if (source == null) {
            return "null";
        }
        if (source.length() > 8) {
            return source.substring(0, 8);
        }
        return source;
    }
}