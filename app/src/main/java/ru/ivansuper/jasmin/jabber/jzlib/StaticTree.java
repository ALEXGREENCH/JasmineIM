package ru.ivansuper.jasmin.jabber.jzlib;


final class StaticTree {
    private static final int D_CODES = 30;
    private static final int LENGTH_CODES = 29;
    private static final int LITERALS = 256;
    private static final int MAX_BITS = 15;
    static final int MAX_BL_BITS = 7;
    static StaticTree static_d_desc;
    static final short[] static_dtree;
    private static short[] static_ltree;
    int elems;
    int extra_base;
    int[] extra_bits;
    int max_length;
    short[] static_tree;
    private static final int L_CODES = 286;
    static StaticTree static_l_desc = new StaticTree(getStatic_ltree(), Tree.extra_lbits, 257, L_CODES, 15);
    private static final int BL_CODES = 19;
    static StaticTree static_bl_desc = new StaticTree(null, Tree.extra_blbits, 0, BL_CODES, 7);

    static {
        short s = (short) 5;
        static_dtree = new short[]{0, s, (short) 16, s, (short) 8, s, (short) 24, s, (short) 4, s, (short) 20, s, (short) 12, s, (short) 28, s, (short) 2, s, (short) 18, s, (short) 10, s, (short) 26, s, (short) 6, s, (short) 22, s, (short) 14, s, (short) 30, s, (short) 1, s, (short) 17, s, (short) 9, s, (short) 25, s, s, s, (short) 21, s, (short) 13, s, (short) LENGTH_CODES, s, (short) 3, s, (short) BL_CODES, s, (short) 11, s, (short) 27, s, (short) 7, s, (short) 23, s};
        static_d_desc = new StaticTree(static_dtree, Tree.extra_dbits, 0, 30, 15);
    }

    StaticTree(short[] sArr, int[] iArr, int i, int i2, int i3) {
        this.static_tree = sArr;
        this.extra_bits = iArr;
        this.extra_base = i;
        this.elems = i2;
        this.max_length = i3;
    }

    public static short[] getStatic_ltree() {
        if (static_ltree == null) {
            new ArrayLoader();
            static_ltree = ArrayLoader.readShortArray("static_ltree");
        }
        return static_ltree;
    }
}
