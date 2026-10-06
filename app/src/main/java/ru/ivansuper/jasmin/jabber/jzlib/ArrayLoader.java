package ru.ivansuper.jasmin.jabber.jzlib;

import java.io.DataInputStream;
import java.io.InputStream;
import ru.ivansuper.jasmin.resources;

public class ArrayLoader {
    public static byte[] readByteArray(String str) {
        Exception e;
        InputStream inputStreamOpen;
        byte[] bArr = null;
        try {
            inputStreamOpen = resources.am.open(str);
            try {
                DataInputStream dataInputStream = new DataInputStream(inputStreamOpen);
                int i = dataInputStream.readInt();
                byte[] bArr2 = new byte[i];
                dataInputStream.read(bArr2, 0, i);
                dataInputStream.close();
                bArr = bArr2;
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            e = e3;
            inputStreamOpen = null;
        }
        try {
            inputStreamOpen.close();
        } catch (Exception e4) {
        }
        return bArr;
    }

    public static int[] readIntArray(String str) {
        Exception e;
        InputStream inputStreamOpen;
        int[] iArr = null;
        try {
            inputStreamOpen = resources.am.open(str);
            try {
                DataInputStream dataInputStream = new DataInputStream(inputStreamOpen);
                int i = dataInputStream.readInt();
                int[] iArr2 = new int[i];
                for (int i2 = 0; i2 < i; i2++) {
                    iArr2[i2] = dataInputStream.readInt();
                }
                dataInputStream.close();
                iArr = iArr2;
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            e = e3;
            inputStreamOpen = null;
        }
        try {
            inputStreamOpen.close();
        } catch (Exception e4) {
        }
        return iArr;
    }

    public static short[] readShortArray(String str) {
        Exception e;
        InputStream inputStreamOpen;
        short[] sArr = null;
        try {
            inputStreamOpen = resources.am.open(str);
            try {
                DataInputStream dataInputStream = new DataInputStream(inputStreamOpen);
                int i = dataInputStream.readInt();
                short[] sArr2 = new short[i];
                for (int i2 = 0; i2 < i; i2++) {
                    sArr2[i2] = dataInputStream.readShort();
                }
                dataInputStream.close();
                sArr = sArr2;
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            e = e3;
            inputStreamOpen = null;
        }
        try {
            inputStreamOpen.close();
        } catch (Exception e4) {
        }
        return sArr;
    }
}
