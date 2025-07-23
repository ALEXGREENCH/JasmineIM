package ru.ivansuper.jasmin.Clients;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.StringConvertor;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * Detects the ICQ client used by a contact based on their capabilities and other information.
 * This class uses a custom virtual machine (VM) to execute a set of rules defined in a binary file.
 * The VM interprets bytecode instructions to match patterns in the contact's data and determine the client.
 *
 * <p>The class loads client definitions and icons from external files (clients.bin and clients.png).
 * It provides methods to execute the VM for a given contact, retrieve client information, and access client icons.
 *
 * <p>Key functionalities include:
 * <ul>
 *   <li>Loading client definitions and icons from files.
 *   <li>Executing a custom VM to match client rules.
 *   <li>Extracting client name, version, and icon.
 *   <li>Handling different data types (GUIDs, fingerprints, protocol versions).
 *   <li>Supporting client masks for grouping clients.
 * </ul>
 */
public class IcqClientDetector {
    public static IcqClientDetector instance;
    private String[] clients;
    private byte[] code;
    private int[] dataFp;
    private byte[] dataGuid;
    private short[] iconIndex;
    private byte[] maskCode;
    private short maskDefault;
    private short[] maskIndex;
    private boolean unloaded = true;
    private final Vector<Drawable> clientIcons = new Vector<>();

    public boolean has(int id) {
        return !this.unloaded && id >= 0 && id < this.clients.length;
    }

    private short[] readBytes(DataInputStream in, int size) throws IOException {
        short[] array = new short[size];
        for (int i = 0; i < size; i++) {
            array[i] = (short) in.readUnsignedByte();
        }
        return array;
    }

    public static void init() {
        instance = new IcqClientDetector();
    }

    private IcqClientDetector() {
        File dir = new File(utilities.normalizePath(resources.JASMINE_SD_PATH) + "Clients/icq/");
        if (!dir.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            } catch (Exception ignored) {
            }
        }
        File config_ = new File(utilities.normalizePath(resources.JASMINE_SD_PATH) + "Clients/icq/clients.bin");
        File icons_ = new File(utilities.normalizePath(resources.JASMINE_SD_PATH) + "Clients/icq/clients.png");
        try {
            //noinspection IOStreamConstructor
            read(new FileInputStream(config_), new FileInputStream(icons_));
        } catch (Exception ignored) {

        }
        if (this.clientIcons.isEmpty()) {
            try {
                read(resources.am.open("icq/clients.bin"), resources.am.open("icq/clients.png"));
            } catch (Exception ignored) {

            }
        }
    }

    private void read(InputStream stream, InputStream icons) {
        boolean hasMasks;
        int count;
        int i = 0;
        DataInputStream is = null;
        try {
            DataInputStream is2 = new DataInputStream(stream);
            try {
                this.code = new byte[is2.readInt()];
                is2.readFully(this.code);
                this.dataGuid = new byte[is2.readInt()];
                is2.readFully(this.dataGuid);
                this.dataFp = new int[is2.readInt()];
                for (int i2 = 0; i2 < this.dataFp.length; i2++) {
                    this.dataFp[i2] = is2.readInt();
                }
                this.clients = new String[is2.readUnsignedByte()];
                for (int i3 = 0; i3 < this.clients.length; i3++) {
                    this.clients[i3] = is2.readUTF();
                }
                this.iconIndex = readBytes(is2, this.clients.length);
                for (int i4 = 0; i4 < this.iconIndex.length; i4++) {
                    this.iconIndex[i4] = (short) Math.max(-1, this.iconIndex[i4] - 1);
                }
                this.unloaded = false;
                is = is2;
            } catch (Exception e) {
                is = is2;
                this.unloaded = true;
                this.code = null;
                this.dataGuid = null;
                this.dataFp = null;
                this.clients = null;
                this.code = new byte[0];
                this.clients = new String[]{"None"};
                hasMasks = false;
                //noinspection StatementWithEmptyBody
                if (is.available() > 0) {

                }
                //noinspection StatementWithEmptyBody,ConstantValue
                if (!hasMasks) {

                }
                stream.close();
                is.close();
                Bitmap all = BitmapFactory.decodeStream(icons);
                all.setDensity(0);
                int side = all.getHeight();
                count = all.getWidth() / side;
                //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
                while (i < count) {

                }
                stream.close();
                is.close();
                _g();
            }
        } catch (Exception ignored) {

        }
        hasMasks = false;
        try {
            //noinspection DataFlowIssue
            if (is.available() > 0) {
                this.maskDefault = (short) is.readUnsignedByte();
                this.maskIndex = readBytes(is, is.readUnsignedByte());
                this.maskCode = new byte[is.readShort()];
                is.readFully(this.maskCode);
                hasMasks = true;
            }
        } catch (Exception ignored) {
        }
        if (!hasMasks) {
            this.maskIndex = new short[0];
            this.maskCode = new byte[0];
        }
        try {
            stream.close();
            is.close();
        } catch (Exception ignored) {
        }
        try {
            Bitmap all2 = BitmapFactory.decodeStream(icons);
            all2.setDensity(0);
            int side2 = all2.getHeight();
            count = all2.getWidth() / side2;
            for (i = 0; i < count; i++) {
                Bitmap icon = Bitmap.createBitmap(all2, side2 * i, 0, side2, side2);
                icon.setDensity(0);
                Drawable drw = resources.normalizeIconDPIBased(new BitmapDrawable(icon));
                this.clientIcons.add(drw);
            }
        } catch (Exception ignored) {
        }
        try {
            stream.close();
            is.close();
        } catch (Exception ignored) {
        }
        _g();
    }

    private int getByte(byte[] data, int offset) {
        return data[offset] & 255;
    }

    private int getWord(byte[] data, int offset) {
        return (data[offset + 1] & 255) | ((data[offset] & 255) << 8);
    }

    private byte[] getGuid(byte[] buf, int offset) {
        byte[] guid = new byte[16];
        System.arraycopy(buf, offset, guid, 0, 16);
        return guid;
    }

    private int findGuid(byte[] guids, int ip) {
        int packed = getByte(this.code, ip);
        int where = getWord(this.code, ip + 1);
        for (int guidNum = 0; guidNum < guids.length; guidNum += 16) {
            if (guids[guidNum] == this.dataGuid[where]) {
                int byteIndex = 0;
                while (byteIndex < packed && guids[guidNum + byteIndex] == this.dataGuid[where + byteIndex]) {
                    byteIndex++;
                }
                if (packed == byteIndex) {
                    return guidNum;
                }
            }
        }
        return -1;
    }

    private boolean execVMProc(ICQContact contact, byte[] guids, int[] fps, int protocol, int ip) {
        int ip2;
        int ip3 = ip + 1;
        byte opCode = this.code[ip];
        if ((opCode & 128) != 0) {
            int proto = getWord(this.code, ip3);
            if (protocol != proto) {
                return false;
            }
            ip3 += 2;
        }
        if ((opCode & 1) != 0) {
            int ip4 = ip3 + 1;
            if (guids.length / 16 != this.code[ip3]) {
                return false;
            }
            ip3 = ip4;
        }
        if ((opCode & 2) != 0) {
            int ip5 = ip3 + 1;
            if (fps[0] != this.dataFp[getByte(this.code, ip3)]) {
                return false;
            }
            ip3 = ip5;
        }
        if ((opCode & 4) != 0) {
            int ip6 = ip3 + 1;
            if (fps[1] != this.dataFp[getByte(this.code, ip3)]) {
                return false;
            }
            ip3 = ip6;
        }
        if ((opCode & 8) != 0) {
            int ip7 = ip3 + 1;
            if (fps[2] != this.dataFp[getByte(this.code, ip3)]) {
                return false;
            }
            ip3 = ip7;
        }
        if ((opCode & 16) != 0) {
            int ip8 = ip3 + 1;
            int guidsCount = this.code[ip3];
            for (int i = 0; i < guidsCount; i++) {
                if (-1 == findGuid(guids, ip8)) {
                    return false;
                }
                ip8 += 3;
            }
            ip3 = ip8;
        }
        if ((opCode & 32) != 0) {
            int ip9 = ip3 + 1;
            int guidsCount2 = this.code[ip3];
            for (int i2 = 0; i2 < guidsCount2; i2++) {
                if (-1 != findGuid(guids, ip9)) {
                    return false;
                }
                ip9 += 3;
            }
            ip3 = ip9;
        }
        String version = null;
        if ((opCode & 64) != 0) {
            ip2 = ip3 + 1;
            int versionType = this.code[ip3];
            switch (versionType) {
                case 0:
                case 1:
                case 2:
                    int versionGuid = findGuid(guids, ip2);
                    if (-1 == versionGuid) {
                        return false;
                    }
                    int ip10 = ip2 + 3;
                    int versionOffset = (this.code[ip10] >> 4) & 15;
                    int versionLength = this.code[ip10] & 15;
                    ip2 = ip10 + 1;
                    version = getGuidVersion(guids, versionGuid, versionOffset, versionLength, versionType);
                    break;
                case 3:
                case 4:
                case 5:
                    version = getFpVersion(fps[getByte(this.code, ip2)], versionType - 3);
                    ip2++;
                    break;
            }
        } else {
            ip2 = ip3;
        }
        contact.client.info_index = getByte(this.code, ip2);
        contact.client.icon = getIcon(this.iconIndex[contact.client.info_index]);
        if (contact.client.info_index != -1) {
            ClientInfo clientInfo = contact.client;
            StringBuilder sb = new StringBuilder(String.valueOf(this.clients[contact.client.info_index]));
            if (version == null) {
                version = "";
            }
            clientInfo.name = sb.append(version).toString();
        }
        return true;
    }

    private Drawable getIcon(int idx) {
        if (idx >= 0 && idx < this.clientIcons.size()) {
            return this.clientIcons.get(idx);
        }
        return null;
    }

    private String getGuidVersion(byte[] guids, int guidOffset, int offset, int length, int versionType) {
        if (versionType == 0) {
            return StringConvertor.byteArrayToString(guids, guidOffset + offset, length).trim();
        }
        if (1 == versionType) {
            StringBuilder version = new StringBuilder();
            int offset2 = offset + guidOffset;
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    version.append('.');
                }
                version.append(guids[offset2 + i]);
            }
            return version.toString();
        }
        byte[] buf = getGuid(guids, guidOffset);
        byte first = buf[0];
        if (105 == first || 115 == first || 101 == first) {
            String version2 = makeVersion(buf[4] & Byte.MAX_VALUE, buf[5], buf[6], buf[7]);
            if ((buf[4] & 128) != 0) {
                return version2 + "a";
            }
            return version2;
        }
        if (77 == buf[0] && 105 == buf[1]) {
            if (buf[12] == 0 && buf[13] == 0 && buf[14] == 0 && buf[15] == 1) {
                return "0.1.2.0";
            }
            if (buf[12] == 0 && buf[13] <= 3 && buf[14] <= 3 && buf[15] <= 1) {
                return makeVersion(0, buf[13], buf[14], buf[15]);
            }
            String ver = makeVersion(buf[8] & Byte.MAX_VALUE, buf[9], buf[10], buf[11]);
            if ((buf[8] & 128) != 0) {
                ver = ver + "a";
            }
            return ver;
        }
        String version3 = makeVersion(buf[offset] & Byte.MAX_VALUE, buf[offset + 1], buf[offset + 2], buf[offset + 3]);
        if ((buf[offset] & 128) != 0) {
            return version3 + "a";
        }
        return version3;
    }

    private String getFpVersion(int fp, int versionType) {
        switch (versionType) {
            case 0:
                return makeVersion(getByte(fp, 24), getByte(fp, 16), getByte(fp, 8), getByte(fp, 0));
            case 1:
                return String.valueOf(getByte(fp, 24)) + getByte(fp, 16) + getByte(fp, 8) + getByte(fp, 0);
            case 2:
                return String.valueOf(fp);
            default:
                return null;
        }
    }

    private String makeVersion(int v0, int v1, int v2, int v3) {
        String ver = v0 + "." + v1;
        if (v2 >= 0 || v3 >= 0) {
            String ver2 = ver + "." + v2;
            if (v3 >= 0) {
                return ver2 + "." + v3;
            }
            return ver2;
        }
        return ver;
    }

    private int getByte(int val, int index) {
        return (val >> index) & 255;
    }

    /** @noinspection unused*/
    private void println(String s) {
    }

    private void _g() {
        int ip_ = 0;
        //noinspection UnusedAssignment
        int ip = 0;
        try {
            byte[] _code = this.code;
            while (ip_ < _code.length) {
                int length = getByte(_code, ip_);
                byte[] cli = new byte[length];
                System.arraycopy(_code, ip_ + 1, cli, 0, length);
                ip_ += length + 1;
                int ip2 = 1;
                try {
                    byte opCode = cli[0];
                    if ((opCode & 128) != 0) {
                        println("protocol " + getWord(cli, ip2));
                        ip2 += 2;
                    }
                    if ((opCode & 1) != 0) {
                        int ip3 = ip2 + 1;
                        println("guid count " + ((int) cli[ip2]));
                        ip2 = ip3;
                    }
                    if ((opCode & 2) != 0) {
                        int ip4 = ip2 + 1;
                        println("FP1 " + this.dataFp[getByte(cli, ip2)]);
                        ip2 = ip4;
                    }
                    if ((opCode & 4) != 0) {
                        int ip5 = ip2 + 1;
                        println("FP2 " + this.dataFp[getByte(cli, ip2)]);
                        ip2 = ip5;
                    }
                    if ((opCode & 8) != 0) {
                        int ip6 = ip2 + 1;
                        println("FP3 " + this.dataFp[getByte(cli, ip2)]);
                        ip2 = ip6;
                    }
                    if ((opCode & 16) != 0) {
                        int ip7 = ip2 + 1;
                        int guidsCount = cli[ip2];
                        println("contains " + guidsCount);
                        for (int i = 0; i < guidsCount; i++) {
                            ip7 += 3;
                        }
                        ip2 = ip7;
                    }
                    if ((opCode & 32) != 0) {
                        int ip8 = ip2 + 1;
                        int guidsCount2 = cli[ip2];
                        println("uncontains " + guidsCount2);
                        for (int i2 = 0; i2 < guidsCount2; i2++) {
                            ip8 += 3;
                        }
                        ip2 = ip8;
                    }
                    if ((opCode & 64) != 0) {
                        ip = ip2 + 1;
                        int versionType = cli[ip2];
                        switch (versionType) {
                            case 0:
                            case 1:
                            case 2:
                                println("guid ver");
                                int ip9 = ip + 1;
                                int length_ = getByte(cli, ip);
                                int where_ = getWord(cli, ip9);
                                int ip10 = ip9 + 2;
                                //noinspection SpellCheckingInspection,MismatchedReadAndWriteOfArray
                                byte[] dddddddd = new byte[length_];
                                System.arraycopy(this.dataGuid, where_, dddddddd, 0, length_);
                                ip = ip10 + 1;
                                break;
                            case 3:
                            case 4:
                            case 5:
                                ip++;
                                println("fp ver");
                                break;
                        }
                    } else {
                        ip = ip2;
                    }
                    println("type " + getByte(cli, ip));
                    println("client " + this.clients[getByte(cli, ip)]);
                } catch (Exception e) {
                    ip = ip2;
                    println("type " + ip_ + ":" + ip);
                    return;
                }
            }
        } catch (Exception ignored) {

        }
    }

    public void execVM(ICQContact contact) {
        contact.client.reset();
        byte[] guids = contact.capabilities.toArray();
        int[] fps = {contact.dc_info.dc1, contact.dc_info.dc2, contact.dc_info.dc3};
        int protocol = contact.protoVersion;
        if (!this.unloaded) {
            int ip = 0;
            while (ip < this.code.length && !execVMProc(contact, guids, fps, protocol, ip + 1)) {
                try {
                    ip = (this.code[ip] & 255) + ip + 1;
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    /** @noinspection unused*/
    private int getClientInfoIndex(byte clientIndex) {
        int clientNum = -1;
        int i = 0;
        while (true) {
            if (i >= this.maskIndex.length) {
                break;
            }
            if (this.maskIndex[i] != clientIndex) {
                i++;
            } else {
                clientNum = i;
                break;
            }
        }
        if (-1 == clientNum) {
            return -1;
        }
        int ip = 0;
        while (ip < this.maskCode.length) {
            if (clientNum != 0) {
                ip = getByte(this.maskCode, ip) + ip + 1;
                clientNum--;
            } else {
                return ip;
            }
        }
        return -1;
    }

    /** @noinspection unused*/
    public short[] getClientsForMask() {
        return this.maskIndex;
    }

    /** @noinspection unused*/
    public short getDefaultClientForMask() {
        return this.maskDefault;
    }
}