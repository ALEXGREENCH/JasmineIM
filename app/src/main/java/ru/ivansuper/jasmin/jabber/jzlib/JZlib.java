package ru.ivansuper.jasmin.jabber.jzlib;

public final class JZlib {
    public static final byte Z_BEST_COMPRESSION = 9;
    public static final byte Z_BEST_SPEED = 1;
    public static final byte Z_BUF_ERROR = -5;
    public static final byte Z_DATA_ERROR = -3;
    public static final byte Z_DEFAULT_COMPRESSION = -1;
    public static final byte Z_DEFAULT_STRATEGY = 0;
    public static final byte Z_ERRNO = -1;
    public static final byte Z_FILTERED = 1;
    public static final byte Z_FINISH = 4;
    public static final byte Z_FULL_FLUSH = 3;
    public static final byte Z_HUFFMAN_ONLY = 2;
    public static final byte Z_MEM_ERROR = -4;
    public static final byte Z_NEED_DICT = 2;
    public static final byte Z_NO_COMPRESSION = 0;
    public static final byte Z_NO_FLUSH = 0;
    public static final byte Z_OK = 0;
    public static final byte Z_PARTIAL_FLUSH = 1;
    public static final byte Z_STREAM_END = 1;
    public static final byte Z_STREAM_ERROR = -2;
    public static final byte Z_SYNC_FLUSH = 2;
    public static final byte Z_VERSION_ERROR = -6;
    private static final String version = "1.0.2";

    public static String version() {
        return version;
    }
}
