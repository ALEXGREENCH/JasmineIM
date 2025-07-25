package ru.ivansuper.jasmin.icq;

import java.util.Vector;
import ru.ivansuper.jasmin.ContactListActivity;

public class ByteCache {
    private static Vector<byte[]> buffer = new Vector<>();

    /** @noinspection unused*/
    public static void recycle(byte[] array) {
    }

    /** @noinspection unused*/
    public static void reinit() {
        buffer.clear();
        System.gc();
        for (int i = 0; i < 150; i++) {
            byte[] small_array = new byte[ContactListActivity.UPDATE_BLINK_STATE];
            buffer.add(small_array);
        }
        for (int i2 = 0; i2 < 100; i2++) {
            byte[] medium_array = new byte[256];
            buffer.add(medium_array);
        }
        for (int i3 = 0; i3 < 50; i3++) {
            byte[] normal_array = new byte[512];
            buffer.add(normal_array);
        }
        for (int i4 = 0; i4 < 20; i4++) {
            byte[] big_array = new byte[1024];
            buffer.add(big_array);
        }
        for (int i5 = 0; i5 < 5; i5++) {
            byte[] very_big_array = new byte[16384];
            buffer.add(very_big_array);
        }
    }

    public static byte[] getByteArray(int desired_length) {
        return new byte[desired_length];
    }
}
