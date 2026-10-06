package ru.ivansuper.jasmin.jabber.jzlib;

final class Tree {
   private static final int BL_CODES = 19;
   static final int Buf_size = 16;
   static final int DIST_CODE_LEN = 512;
   private static final int D_CODES = 30;
   static final int END_BLOCK = 256;
   private static final int HEAP_SIZE = 573;
   private static final int LENGTH_CODES = 29;
   private static final int LITERALS = 256;
   private static final int L_CODES = 286;
   private static final int MAX_BITS = 15;
   static final int MAX_BL_BITS = 7;
   static final int REPZ_11_138 = 18;
   static final int REPZ_3_10 = 17;
   static final int REP_3_6 = 16;
   static byte[] _dist_code;
   private static byte[] _length_code;
   static final int[] base_dist;
   static final int[] base_length;
   static final byte[] bl_order;
   static final int[] extra_blbits;
   static final int[] extra_dbits;
   static final int[] extra_lbits;
   short[] dyn_tree;
   int max_code;
   StaticTree stat_desc;

   static {
      int[] var0 = new int[29];
      var0[8] = 1;
      var0[9] = 1;
      var0[10] = 1;
      var0[11] = 1;
      var0[12] = 2;
      var0[13] = 2;
      var0[14] = 2;
      var0[15] = 2;
      var0[16] = 3;
      var0[17] = 3;
      var0[18] = 3;
      var0[19] = 3;
      var0[20] = 4;
      var0[21] = 4;
      var0[22] = 4;
      var0[23] = 4;
      var0[24] = 5;
      var0[25] = 5;
      var0[26] = 5;
      var0[27] = 5;
      extra_lbits = var0;
      var0 = new int[]{0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13};
      extra_dbits = var0;
      var0 = new int[19];
      var0[16] = 2;
      var0[17] = 3;
      var0[18] = 7;
      extra_blbits = var0;
      byte[] order = new byte[]{
         (byte)16,
         (byte)17,
         (byte)18,
         0,
         (byte)8,
         (byte)7,
         (byte)9,
         (byte)6,
         (byte)10,
         (byte)5,
         (byte)11,
         (byte)4,
         (byte)12,
         (byte)3,
         (byte)13,
         (byte)2,
         (byte)14,
         (byte)1,
         (byte)15
      };
      bl_order = order;
      int[] var3 = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 16, 20, 24, 28, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 0};
      base_length = var3;
      var3 = new int[]{
         0, 1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128, 192, 256, 384, 512, 768, 1024, 1536, 2048, 3072, 4096, 6144, 8192, 12288, 16384, 24576
      };
      base_dist = var3;
   }

   static int bi_reverse(int var0, int var1) {
      int var2 = 0;

      int var4;
      int var5;
      do {
         int var3 = var0;
         var0 = var3 >>> 1;
         var4 = (var2 | var3 & 1) << 1;
         var5 = var1 - 1;
         var2 = var4;
         var1 = var5;
      } while (var5 > 0);

      return var4 >>> 1;
   }

   static int d_code(int var0) {
      if (_dist_code == null) {
         _dist_code = ArrayLoader.readByteArray("dist_code");
      }

      byte var1;
      if (var0 < 256) {
         var1 = _dist_code[var0];
      } else {
         var1 = _dist_code[(var0 >>> 7) + 256];
      }

      return var1;
   }

   static void gen_codes(short[] var0, int var1, short[] var2) {
      short[] var3 = new short[16];
      short var4 = 0;

      for (int var5 = 1; var5 <= 15; var5++) {
         var4 = (short)(var2[var5 - 1] + var4 << 1);
         var3[var5] = (short)var4;
      }

      for (int var7 = 0; var7 <= var1; var7++) {
         short var6 = var0[(var7 << 1) + 1];
         if (var6 != 0) {
            short var8 = var3[var6];
            var3[var6] = (short)((short)(var8 + 1));
            var0[var7 << 1] = (short)((short)bi_reverse(var8, var6));
         }
      }
   }

   public static byte getLength_code(int var0) {
      if (_length_code == null) {
         _length_code = ArrayLoader.readByteArray("length_code");
      }

      return _length_code[var0];
   }

   void build_tree(Deflate var1) {
      short[] var2 = this.dyn_tree;
      short[] staticTree = this.stat_desc.static_tree;
      int[] var3;
      int var4 = this.stat_desc.elems;
      int var5 = -1;
      var1.heap_len = 0;
      var1.heap_max = 573;

      for (int var6 = 0; var6 < var4; var6++) {
         int var8 = var6 << 1;
         if (var2[var8] != 0) {
            int[] var7 = var1.heap;
            var8 = var1.heap_len + 1;
            var1.heap_len = var8;
            var5 = var6;
            var7[var8] = var6;
            var1.depth[var6] = (byte)0;
         } else {
            var2[var8 + 1] = (short)0;
         }
      }

      while (var1.heap_len < 2) {
         int[] var20 = var1.heap;
         int var9 = var1.heap_len + 1;
         var1.heap_len = var9;
         int var16;
         if (var5 < 2) {
            var16 = var5 + 1;
            var5 = var16;
         } else {
            byte var23 = 0;
            var16 = var5;
            var5 = var23;
         }

         var20[var9] = var5;
         int var24 = var5 << 1;
         var2[var24] = (short)1;
         var1.depth[var5] = (byte)0;
         var1.opt_len--;
         var5 = var16;
         if (staticTree != null) {
            var1.static_len = var1.static_len - staticTree[var24 + 1];
            var5 = var16;
         }
      }

      this.max_code = var5;

      for (int var17 = var1.heap_len / 2; var17 >= 1; var17--) {
         var1.pqdownheap(var2, var17);
      }

      int var18 = var4;

      while (true) {
         var4 = var1.heap[1];
         var3 = var1.heap;
         int[] var21 = var1.heap;
         int var25 = var1.heap_len--;
         var3[1] = var21[var25];
         var1.pqdownheap(var2, 1);
         var25 = var1.heap[1];
         var3 = var1.heap;
         int var27 = var1.heap_max - 1;
         var1.heap_max = var27;
         var3[var27] = var4;
         var3 = var1.heap;
         var27 = var1.heap_max - 1;
         var1.heap_max = var27;
         var3[var27] = var25;
         var2[var18 << 1] = (short)((short)(var2[var4 << 1] + var2[var25 << 1]));
         var1.depth[var18] = (byte)((byte)(Math.max(var1.depth[var4], var1.depth[var25]) + 1));
         short var29 = (short)var18;
         var2[(var25 << 1) + 1] = (short)var29;
         var2[(var4 << 1) + 1] = (short)var29;
         var1.heap[1] = var18;
         var1.pqdownheap(var2, 1);
         if (var1.heap_len < 2) {
            var3 = var1.heap;
            var18 = var1.heap_max - 1;
            var1.heap_max = var18;
            var3[var18] = var1.heap[1];
            this.gen_bitlen(var1);
            gen_codes(var2, var5, var1.bl_count);
            return;
         }

         var18++;
      }
   }

   void gen_bitlen(Deflate var1) {
      short[] var2 = this.dyn_tree;
      short[] var3 = this.stat_desc.static_tree;
      int[] var4 = this.stat_desc.extra_bits;
      int var5 = this.stat_desc.extra_base;
      int var6 = this.stat_desc.max_length;
      int var7 = 0;

      for (int var8 = 0; var8 <= 15; var8++) {
         var1.bl_count[var8] = (short)0;
      }

      var2[var1.heap[var1.heap_max] * 2 + 1] = (short)0;
      int var28 = var1.heap_max + 1;

      while (var28 < 573) {
         int var10 = var1.heap[var28];
         int var11 = var10 << 1;
         int var12 = var2[var2[var11 + 1] * 2 + 1] + 1;
         int var13 = var12;
         int var9 = var7;
         if (var12 > var6) {
            var13 = var6;
            var9 = var7 + 1;
         }

         var2[var11 + 1] = (short)((short)var13);
         if (var10 <= this.max_code) {
            short[] var14 = var1.bl_count;
            var14[var13] = (short)((short)(var14[var13] + 1));
            var7 = 0;
            if (var10 >= var5) {
               var7 = var4[var10 - var5];
            }

            short var32 = var2[var11];
            var1.opt_len += (var13 + var7) * var32;
            if (var3 != null) {
               var1.static_len = var1.static_len + (var3[var11 + 1] + var7) * var32;
            }
         }

         var28++;
         var7 = var9;
      }

      int var30 = var7;
      if (var7 != 0) {
         do {
            var7 = var6 - 1;

            while (var1.bl_count[var7] == 0) {
               var7--;
            }

            short[] var34 = var1.bl_count;
            var34[var7] = (short)((short)(var34[var7] - 1));
            var34 = var1.bl_count;
            var34[++var7] = (short)((short)(var34[var7] + 2));
            var34 = var1.bl_count;
            var34[var6] = (short)((short)(var34[var6] - 1));
            var7 = var30 - 2;
            var30 = var7;
         } while (var7 > 0);

         var7 = var6;
         var6 = var28;

         for (int var29 = var7; var29 != 0; var29--) {
            var7 = var1.bl_count[var29];

            while (var7 != 0) {
               int[] var37 = var1.heap;
               var30 = var6 - 1;
               int var33 = var37[var30];
               var6 = var30;
               if (var33 <= this.max_code) {
                  var6 = (var33 << 1) + 1;
                  if (var2[var6] != var29) {
                     long var15 = var1.opt_len;
                     long var17 = var29;
                     var6++;
                     var1.opt_len = (int)(var15 + (var17 - var2[var6]) * var2[var6]);
                     var2[var6 + 1] = (short)((short)var29);
                  }

                  var7--;
                  var6 = var30;
               }
            }
         }
      }
   }
}
