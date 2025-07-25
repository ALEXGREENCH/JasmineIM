package ru.ivansuper.jasmin.icq;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import ru.ivansuper.jasmin.utilities;

public class ByteBuffer {
    public byte[] bytes;
    public int readPos;
    public int writePos;

    public ByteBuffer() {
        this.bytes = ByteCache.getByteArray(16384);
        this.writePos = 0;
        this.readPos = 0;
    }

    public ByteBuffer(int bufferLength) {
        this.bytes = ByteCache.getByteArray(bufferLength);
        this.writePos = 0;
        this.readPos = 0;
    }

    public ByteBuffer(byte[] source) {
        this.bytes = source;
        this.writePos = source.length;
        this.readPos = 0;
    }

    /** @noinspection unused*/
    public ByteBuffer(boolean flag) {
        this.bytes = null;
        this.writePos = 0;
        this.readPos = 0;
    }

    public static byte previewByte(int offset, byte[] array) {
        return array[offset];
    }

    public static int previewWord(int offset, byte[] array) {
        int i1 = (array[offset] & 255) << 8;
        //noinspection UnnecessaryLocalVariable
        int i3 = (array[offset + 1] & 255) | i1;
        return i3;
    }

    public static byte[] normalizeBytes(byte[] source, int len) {
        byte[] res = new byte[len];
        System.arraycopy(source, 0, res, 0, len);
        return res;
    }

    /** @noinspection unused*/
    public static byte[] normalizeBytes(byte[] source, int offset, int len) {
        byte[] res = new byte[len];
        System.arraycopy(source, offset, res, 0, len);
        return res;
    }

    public final int getBytesCountAvailableToRead() {
        return this.writePos - this.readPos;
    }

    public boolean available() {
        return this.writePos - this.readPos > 0;
    }

    public final int getZeroTerminatedStringLength() {
        int i = this.readPos;
        int j = 0;
        boolean zeroFound = false;
        while (true) {
            if (i >= this.writePos) {
                break;
            }
            if (this.bytes[i] == 0) {
                zeroFound = true;
                break;
            }
            i++;
            j++;
        }
        if (!zeroFound) {
            return -1;
        }
        return j;
    }

    public final int getDoubleZeroTerminatedStringLength() {
        int i = this.readPos;
        int j = 0;
        boolean zeroFound = false;
        while (true) {
            if (i >= this.writePos) {
                break;
            }
            if (this.bytes[i] + this.bytes[i + 1] == 0) {
                zeroFound = true;
                break;
            }
            i++;
            j++;
        }
        if (!zeroFound) {
            return -1;
        }
        return j;
    }

    public final byte[] readBytes(int len) {
        byte[] res = new byte[len];
        System.arraycopy(this.bytes, this.readPos, res, 0, len);
        this.readPos += len;
        return res;
    }

    public final void readBytes(int len, byte[] buffer) {
        System.arraycopy(this.bytes, this.readPos, buffer, 0, len);
        this.readPos += len;
    }

    public final byte readByte() {
        byte res = this.bytes[this.readPos];
        this.readPos++;
        return res;
    }

    public final byte previewByte(int offset) {
        return this.bytes[this.readPos + offset];
    }

    public final int readWord() {
        int j = this.readPos;
        this.readPos += 2;
        int i1 = (this.bytes[j] & 255) << 8;
        //noinspection UnnecessaryLocalVariable
        int i3 = (this.bytes[j + 1] & 255) | i1;
        return i3;
    }

    public final int previewWord(int offset) {
        int j = this.readPos + offset;
        int i1 = (this.bytes[j] & 255) << 8;
        //noinspection UnnecessaryLocalVariable
        int i3 = (this.bytes[j + 1] & 255) | i1;
        return i3;
    }

    public final int readDWord() {
        int j = this.readPos;
        this.readPos += 4;
        int i1 = (this.bytes[j] & 255) << 24;
        int i2 = j + 1;
        int i3 = (this.bytes[i2] & 255) << 16;
        int i4 = i1 | i3;
        int i5 = i2 + 1;
        int i6 = (this.bytes[i5] & 255) << 8;
        int i7 = i4 | i6;
        int i8 = i5 + 1;
        int i = this.bytes[i8];
        //noinspection UnnecessaryLocalVariable
        int i9 = (i & 255) | i7;
        return i9;
    }

    public final int previewDWord(int offset) {
        int j = this.readPos + offset;
        int i1 = (this.bytes[j] & 255) << 24;
        int i2 = j + 1;
        int i3 = (this.bytes[i2] & 255) << 16;
        int i4 = i1 | i3;
        int i5 = i2 + 1;
        int i6 = (this.bytes[i5] & 255) << 8;
        int i7 = i4 | i6;
        int i8 = i5 + 1;
        int i = this.bytes[i8];
        //noinspection UnnecessaryLocalVariable
        int i9 = (i & 255) | i7;
        return i9;
    }

    public final int readWordLE() {
        int j = this.readPos;
        this.readPos += 2;
        int i1 = this.bytes[j] & 255;
        int i2 = j + 1;
        int i = this.bytes[i2];
        //noinspection UnnecessaryLocalVariable
        int i3 = ((i & 255) << 8) | i1;
        return i3;
    }

    /** @noinspection unused*/
    public final int previewWordLE(int offset) {
        int j = this.readPos + offset;
        int i1 = this.bytes[j] & 255;
        int i2 = j + 1;
        int i = this.bytes[i2];
        //noinspection UnnecessaryLocalVariable
        int i3 = ((i & 255) << 8) | i1;
        return i3;
    }

    public final int readDWordLE() {
        int j = this.readPos;
        this.readPos += 4;
        int i1 = this.bytes[j] & 255;
        int i2 = j + 1;
        int i3 = (this.bytes[i2] & 255) << 8;
        int i4 = i1 | i3;
        int i5 = i2 + 1;
        int i6 = (this.bytes[i5] & 255) << 16;
        int i7 = i4 | i6;
        int i8 = i5 + 1;
        int i = this.bytes[i8];
        //noinspection UnnecessaryLocalVariable
        int i9 = ((i & 255) << 24) | i7;
        return i9;
    }

    /** @noinspection unused*/
    public final int previewDWordLE(int offset) {
        int j = this.readPos + offset;
        int i1 = this.bytes[j] & 255;
        int i2 = j + 1;
        int i3 = (this.bytes[i2] & 255) << 8;
        int i4 = i1 | i3;
        int i5 = i2 + 1;
        int i6 = (this.bytes[i5] & 255) << 16;
        int i7 = i4 | i6;
        int i8 = i5 + 1;
        int i = this.bytes[i8];
        //noinspection UnnecessaryLocalVariable
        int i9 = ((i & 255) << 24) | i7;
        return i9;
    }

    /** @noinspection unused, ShiftOutOfRange */
    public final long readLong() {
        int j = this.readPos;
        this.readPos += 8;
        int i1 = this.bytes[j] & 255;
        int i2 = j + 1;
        int i3 = (this.bytes[i2] & 255) << 8;
        int i4 = i1 | i3;
        int i5 = i2 + 1;
        int i6 = (this.bytes[i5] & 255) << 16;
        int i7 = i4 | i6;
        int i8 = i5 + 1;
        int i = this.bytes[i8];
        int i9 = ((i & 255) << 24) | i7;
        //noinspection PointlessBitwiseExpression
        long l1 = i9 & (-1);
        //noinspection IntegerMultiplicationImplicitCastToLong
        long l2 = i9 << 32;
        //noinspection UnnecessaryLocalVariable
        long l3 = l1 | l2;
        return l3;
    }

    /** @noinspection unused*/
    public final String previewStringAscii(int paramInt) {
        StringBuilder sb = new StringBuilder();
        int i = paramInt;
        int backupPos = this.readPos;
        while (true) {
            i--;
            if (i < 0) {
                String res = sb.toString();
                this.readPos = backupPos;
                return res;
            }
            sb.append((char) readByte());
        }
    }

    /** @noinspection InjectedReferences, UnnecessaryLocalVariable , CallToPrintStackTrace */
    public final String readString1251(int paramInt) {
        byte[] bt = readBytes(paramInt);
        try {
            String res = new String(bt, "windows1251");
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR in 'readString1251(I)'";
        }
    }

    public final String readStringUnicode(int paramInt) {
        StringBuilder sb = new StringBuilder();
        int i = paramInt;
        while (true) {
            i -= 2;
            if (i < 0) {
                //noinspection UnnecessaryLocalVariable
                String res = sb.toString();
                return res;
            }
            sb.append((char) readWord());
        }
    }

    public final String readStringAscii(int length) {
        byte[] temp = new byte[length];
        System.arraycopy(this.bytes, this.readPos, temp, 0, length);
        this.readPos += length;
        return new String(temp);
    }

    /** @noinspection unused*/
    public final String readStringAsciiZ() {
        StringBuilder sb = new StringBuilder();
        int i = getZeroTerminatedStringLength();
        while (true) {
            i--;
            if (i < 0) {
                String res = sb.toString();
                this.readPos += res.length();
                return res;
            }
            sb.append((char) readByte());
        }
    }

    public final String readStringUTF8(int paramInt) throws IOException {
        int rp = this.readPos;
        skip(paramInt);
        int i = paramInt + 2;
        byte[] arrayOfByte = new byte[i];
        System.arraycopy(this.bytes, rp, arrayOfByte, 2, paramInt);
        byte j = (byte) ((paramInt >> 8) & 255);
        arrayOfByte[0] = j;
        byte k = (byte) (paramInt & 255);
        arrayOfByte[1] = k;
        ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(arrayOfByte);
        DataInputStream dis = new DataInputStream(localByteArrayInputStream);
        String str1 = dis.readUTF();
        try {
            dis.close();
        } catch (Exception ignored) {
        }
        return str1;
    }

    /** @noinspection unused*/
    public final String readIP() {
        int pos = this.readPos;
        String ip = Math.abs(this.bytes[pos]) + ".";
        String ip2 = ip + Math.abs(this.bytes[pos + 1]) + "." + Math.abs(this.bytes[pos + 2]) + "." + Math.abs(this.bytes[pos + 3]);
        this.readPos += 4;
        return ip2;
    }

    public final String readIPA() {
        StringBuilder ip = new StringBuilder();
        int val = readDWord();
        ip.append((val >> 24) & 255);
        ip.append(".");
        ip.append((val >> 16) & 255);
        ip.append(".");
        ip.append((val >> 8) & 255);
        ip.append(".");
        ip.append(val & 255);
        return ip.toString();
    }

    public void write(byte[] source) {
        System.arraycopy(source, 0, this.bytes, this.writePos, source.length);
        this.writePos += source.length;
    }

    public final void writeByte(byte source) {
        this.bytes[this.writePos] = source;
        this.writePos++;
    }

    public final void writeWord(int source) {
        int i = this.writePos;
        int j = i + 1;
        byte k = (byte) (source >> 8);
        this.bytes[i] = k;
        byte m = (byte) source;
        this.bytes[j] = m;
        //noinspection UnnecessaryLocalVariable
        int n = this.writePos + 2;
        this.writePos = n;
    }

    public final void writeWordLE(int source) {
        int i = this.writePos;
        int j = i + 1;
        byte k = (byte) source;
        this.bytes[i] = k;
        byte m = (byte) (source >> 8);
        this.bytes[j] = m;
        //noinspection UnnecessaryLocalVariable
        int n = this.writePos + 2;
        this.writePos = n;
    }

    public final void writeDWord(int source) {
        int i = this.writePos;
        int j = i + 1;
        byte k = (byte) (source >> 24);
        this.bytes[i] = k;
        int m = j + 1;
        byte n = (byte) (source >> 16);
        this.bytes[j] = n;
        int i1 = m + 1;
        byte i2 = (byte) (source >> 8);
        this.bytes[m] = i2;
        byte i3 = (byte) source;
        this.bytes[i1] = i3;
        this.writePos += 4;
    }

    public final void writeDWordLE(int source) {
        int i = this.writePos;
        int j = i + 1;
        byte k = (byte) source;
        this.bytes[i] = k;
        int m = j + 1;
        byte n = (byte) (source >> 8);
        this.bytes[j] = n;
        int i1 = m + 1;
        byte i2 = (byte) (source >> 16);
        this.bytes[m] = i2;
        byte i3 = (byte) (source >> 24);
        this.bytes[i1] = i3;
        this.writePos += 4;
    }

    public final void writeLong(long source) throws Exception {
        ByteArrayOutputStream btOut = new ByteArrayOutputStream(8);
        DataOutputStream out = new DataOutputStream(btOut);
        out.writeLong(source);
        System.arraycopy(btOut.toByteArray(), 0, this.bytes, this.writePos, 8);
        this.writePos += 8;
        try {
            out.close();
        } catch (Exception ignored) {
        }
    }

    public void writeString1251(String source) {
        byte[] bytes = utilities.stringToByteArray1251(source);
        write(bytes);
    }

    public void writeStringUnicode(String paramString) {
        int i = paramString.length();
        for (int k = 0; k < i; k++) {
            int m = paramString.charAt(k);
            writeWord(m);
        }
    }

    /** @noinspection CallToPrintStackTrace*/
    public final void writeString1251A(String source) {
        try {
            //noinspection InjectedReferences
            byte[] bytes = source.getBytes("windows1251");
            write(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public final void writeStringAscii(String source) {
        int len = source.length();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                byte bt = (byte) source.charAt(i);
                this.bytes[this.writePos + i] = bt;
            }
            this.writePos += len;
        }
    }

    public final void writeByteTLV(int type, byte value) {
        writeWordLE(type);
        writeWordLE(1);
        writeByte(value);
    }

    /** @noinspection unused*/
    public final void writeAsciiTLV(int type, String value) {
        writeWordLE(type);
        writeWordLE(value.length() + 3);
        writeWordLE(value.length() + 1);
        writeStringAscii(value);
        writeByte((byte) 0);
    }

    public final void write1251TLV(int type, String value) {
        writeWordLE(type);
        if (!value.isEmpty()) {
            writeWordLE(value.length() + 3);
            writeWordLE(value.length() + 1);
            writeString1251(value);
            writeByte((byte) 0);
            return;
        }
        writeWordLE(value.length() + 2);
        writeWordLE(0);
    }

    /** @noinspection unused*/
    public final void writeUtf8TLV(int type, String value) {
        try {
            writeWordLE(type);

            byte[] raw_value;
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                raw_value = value.getBytes(StandardCharsets.UTF_8);
            } else {
                //noinspection CharsetObjectCanBeUsed
                raw_value = value.getBytes("UTF-8");
            }

            writeWordLE(raw_value.length + 3);
            writeWordLE(raw_value.length + 1);
            write(raw_value);
            writeByte((byte) 0);
        } catch (Exception ignored) {
        }
    }

    public final void writePreLengthStringAscii(String source) {
        writeWord(source.length());
        writeStringAscii(source);
    }

    public final void writeStringUTF8(String source) {
        ByteArrayOutputStream btOut = new ByteArrayOutputStream();
        DataOutputStream dtOut = new DataOutputStream(btOut);
        try {
            dtOut.writeUTF(source);
            byte[] bt = btOut.toByteArray();
            System.arraycopy(bt, 2, this.bytes, this.writePos, bt.length - 2);
            this.writePos += bt.length - 2;
        } catch (Exception ignored) {
        }
        try {
            dtOut.close();
        } catch (Exception ignored) {
        }
    }

    public final void writeByteBuffer(ByteBuffer source) {
        System.arraycopy(source.bytes, 0, this.bytes, this.writePos, source.writePos);
        this.writePos += source.writePos;
    }

    public final void writeIcqTLV(ByteBuffer source, int TLVType) {
        writeWord(TLVType);
        writeWord(source.writePos);
        writeByteBuffer(source);
    }

    public final void skip(int count) {
        this.readPos += count;
    }

    public final void reset() {
        this.bytes = new byte[16384];
        this.readPos = 0;
        this.writePos = 0;
    }

    public byte[] getBytes() {
        return normalizeBytes(this.bytes, this.writePos);
    }

    protected void finalize() {
    }
}