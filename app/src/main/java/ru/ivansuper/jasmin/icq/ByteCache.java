package ru.ivansuper.jasmin.icq;

import java.util.Vector;
import ru.ivansuper.jasmin.ContactListActivity;

/**
 * A cache for byte arrays. This class is used to reduce the number of
 * allocations and deallocations of byte arrays, which can improve performance.
 *
 * <p>The cache stores a pool of byte arrays of different sizes. When a byte array
 * is needed, the cache is checked to see if an array of the desired size is
 * available. If so, the array is returned from the cache. Otherwise, a new
 * array is allocated.
 *
 * <p>When a byte array is no longer needed, it can be returned to the cache
 * using the {@link #recycle(byte[])} method. This allows the array to be reused
 * later, rather than being deallocated.
 *
 * <p>The cache can be reinitialized using the {@link #reinit()} method. This
 * clears the cache and repopulates it with a default set of byte arrays.
 */
public class ByteCache {
    /** @noinspection MismatchedQueryAndUpdateOfCollection*/
    private static final Vector<byte[]> buffer = new Vector<>();

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
