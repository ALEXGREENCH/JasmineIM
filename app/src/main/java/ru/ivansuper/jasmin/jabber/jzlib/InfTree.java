package ru.ivansuper.jasmin.jabber.jzlib;

final class InfTree {
   static final int BMAX = 15;
   private static final int MANY = 1440;
   private static final byte Z_BUF_ERROR = -5;
   private static final byte Z_DATA_ERROR = -3;
   private static final byte Z_ERRNO = -1;
   private static final byte Z_MEM_ERROR = -4;
   private static final byte Z_NEED_DICT = 2;
   private static final byte Z_OK = 0;
   private static final byte Z_STREAM_END = 1;
   private static final byte Z_STREAM_ERROR = -2;
   private static final byte Z_VERSION_ERROR = -6;
   static final int[] cpdext;
   static final int[] cpdist = new int[]{
      1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577
   };
   static final int[] cplens;
   static final int[] cplext;
   static final int fixed_bd = 5;
   static final int fixed_bl = 9;
   private static int[] fixed_td;
   private static int[] fixed_tl;
   int[] c;
   int[] hn = null;
   int[] r;
   int[] u;
   int[] v = null;
   int[] x;

   static {
      int[] var0 = new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258, 0, 0};
      cplens = var0;
      var0 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0, 112, 112};
      cplext = var0;
      var0 = new int[]{0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13};
      cpdext = var0;
   }

   InfTree() {
      this.c = null;
      this.r = null;
      this.u = null;
      this.x = null;
   }

   private int huft_build(int[] var1, int var2, int var3, int var4, int[] var5, int[] var6, int[] var7, int[] var8, int[] var9, int[] var10, int[] var11) {
      int var12 = 0;
      int var13 = var3;

      int var48;
      do {
         int[] var14 = this.c;
         int var15 = var1[var2 + var12];
         var14[var15]++;
         var12++;
         var48 = var13 - 1;
         var13 = var48;
      } while (var48 != 0);

      int var34;
      if (this.c[0] == var3) {
         var7[0] = -1;
         var8[0] = 0;
         var34 = 0;
      } else {
         var12 = var8[0];
         var13 = 1;

         while (var13 <= 15 && this.c[var13] == 0) {
            var13++;
         }

         int var16 = var13;
         int var17 = var12;
         if (var12 < var13) {
            var17 = var13;
         }

         var12 = 15;

         while (var12 != 0 && this.c[var12] == 0) {
            var12--;
         }

         var48 = var17;
         if (var17 > var12) {
            var48 = var12;
         }

         var8[0] = var48;
         var17 = 1 << var13;

         while (var13 < var12) {
            var17 -= this.c[var13];
            if (var17 < 0) {
               var34 = (byte)-3;
               return var34;
            }

            var13++;
            var17 <<= 1;
         }

         int var18 = var17 - this.c[var12];
         if (var18 < 0) {
            var34 = -3;
         } else {
            var8 = this.c;
            var8[var12] += var18;
            var8 = this.x;
            int var19 = 0;
            var8[1] = 0;
            var17 = 1;
            int var20 = 2;

            for (int var44 = var12; --var44 != 0; var17++) {
               var8 = this.x;
               var19 += this.c[var17];
               var8[var20] = var19;
               var20++;
            }

            var13 = 0;
            var17 = 0;

            do {
               var20 = var1[var2 + var17];
               if (var20 != 0) {
                  var8 = this.x;
                  var19 = var8[var20]++;
                  var11[var19] = var13;
               }

               var17++;
               var20 = var13 + 1;
               var13 = var20;
            } while (var20 < var3);

            int var21 = this.x[var12];
            var1 = this.x;
            var13 = 0;
            var1[0] = 0;
            var34 = 0;
            var19 = -1;
            var20 = -var48;
            this.u[0] = 0;
            int var22 = 0;
            int var23 = 0;
            var3 = var16;

            while (var3 <= var12) {
               int var24 = this.c[var3];

               while (true) {
                  int var25 = var24 - 1;
                  var17 = var19;
                  int var26 = var22;
                  var16 = var20;
                  int var27 = var23;
                  if (var24 == 0) {
                     var3++;
                     break;
                  }

                  while (var3 > var16 + var48) {
                     var22 = var17 + 1;
                     var23 = var16 + var48;
                     var16 = var12 - var23;
                     var17 = var16;
                     if (var16 > var48) {
                        var17 = var48;
                     }

                     var20 = var3 - var23;
                     var19 = 1 << var20;
                     var16 = var20;
                     if (var19 > var25 + 1) {
                        var27 = var19 - (var25 + 1);
                        var19 = var3;
                        var16 = var20;
                        if (var20 < var17) {
                           var16 = var19;
                           var19 = var27;

                           while (true) {
                              if (++var20 >= var17) {
                                 var16 = var20;
                                 break;
                              }

                              var19 <<= 1;
                              var1 = this.c;
                              var27 = var16 + 1;
                              var16 = var20;
                              if (var19 <= var1[var27]) {
                                 break;
                              }

                              var19 -= this.c[var27];
                              var16 = var27;
                           }
                        }
                     }

                     var27 = 1 << var16;
                     if (var10[0] + var27 > 1440) {
                        byte var81 = -3;
                        return var81;
                     }

                     var1 = this.u;
                     var26 = var10[0];
                     var1[var22] = var26;
                     var10[0] += var27;
                     if (var22 != 0) {
                        this.x[var22] = var13;
                        this.r[0] = (byte)var16;
                        this.r[1] = (byte)var48;
                        var17 = var13 >>> var23 - var48;
                        this.r[2] = var26 - this.u[var22 - 1] - var17;
                        System.arraycopy(this.r, 0, var9, (this.u[var22 - 1] + var17) * 3, 3);
                        var16 = var23;
                        var17 = var22;
                     } else {
                        var7[0] = var26;
                        var17 = var22;
                        var16 = var23;
                     }
                  }

                  this.r[1] = (byte)(var3 - var16);
                  if (var34 >= var21) {
                     this.r[0] = 192;
                  } else if (var11[var34] < var4) {
                     var1 = this.r;
                     int var71;
                     if (var11[var34] < 256) {
                        var71 = 0;
                     } else {
                        var71 = 96;
                     }

                     var1[0] = (byte)var71;
                     var1 = this.r;
                     var71 = var34 + 1;
                     var1[2] = var11[var34];
                     var34 = var71;
                  } else {
                     this.r[0] = (byte)(var6[var11[var34] - var4] + 16 + 64);
                     var1 = this.r;
                     var20 = var34 + 1;
                     var1[2] = var5[var11[var34] - var4];
                     var34 = var20;
                  }

                  for (int var74 = var13 >>> var16; var74 < var27; var74 += 1 << var3 - var16) {
                     System.arraycopy(this.r, 0, var9, (var26 + var74) * 3, 3);
                  }

                  for (var20 = 1 << var3 - 1; (var13 & var20) != 0; var20 >>>= 1) {
                     var13 ^= var20;
                  }

                  var20 = var13 ^ var20;

                  for (int var47 = (1 << var16) - 1; (var20 & var47) != this.x[var17]; var47 = (1 << var16) - 1) {
                     var17--;
                     var16 -= var48;
                  }

                  var24 = var25;
                  var19 = var17;
                  var13 = var20;
                  var22 = var26;
                  var20 = var16;
                  var23 = var27;
               }
            }

            if (var18 != 0 && var12 != 1) {
               var34 = -5;
            } else {
               var34 = 0;
            }
         }
      }

      return var34;
   }

   static int inflate_trees_fixed(int[] var0, int[] var1, int[][] var2, int[][] var3, ZStream var4) {
      var0[0] = 9;
      var1[0] = 5;
      if (fixed_tl == null) {
         new ArrayLoader();
         fixed_tl = ArrayLoader.readIntArray("fixed_tl");
      }

      var2[0] = fixed_tl;
      if (fixed_td == null) {
         new ArrayLoader();
         fixed_td = ArrayLoader.readIntArray("fixed_td");
      }

      var3[0] = fixed_td;
      return 0;
   }

   private void initWorkArea(int var1) {
      if (this.hn == null) {
         this.hn = new int[1];
         this.v = new int[var1];
         this.c = new int[16];
         this.r = new int[3];
         this.u = new int[15];
         this.x = new int[16];
      }

      if (this.v.length < var1) {
         this.v = new int[var1];
      }

      for (int var2 = 0; var2 < var1; var2++) {
         this.v[var2] = 0;
      }

      for (int var3 = 0; var3 < 16; var3++) {
         this.c[var3] = 0;
      }

      for (int var4 = 0; var4 < 3; var4++) {
         this.r[var4] = 0;
      }

      System.arraycopy(this.c, 0, this.u, 0, 15);
      System.arraycopy(this.c, 0, this.x, 0, 16);
   }

   int inflate_trees_bits(int[] var1, int[] var2, int[] var3, int[] var4, ZStream var5) {
      this.initWorkArea(19);
      this.hn[0] = 0;
      int var6 = this.huft_build(var1, 0, 19, 19, null, null, var3, var2, var4, this.hn, this.v);
      int var7 = var6;
      if (var6 != -3) {
         if (var6 != -5) {
            var7 = var6;
            if (var2[0] != 0) {
               return var7;
            }
         }

         var7 = -3;
      }

      return var7;
   }

   int inflate_trees_dynamic(int var1, int var2, int[] var3, int[] var4, int[] var5, int[] var6, int[] var7, int[] var8, ZStream var9) {
      this.initWorkArea(288);
      this.hn[0] = 0;
      int var10 = this.huft_build(var3, 0, var1, 257, cplens, cplext, var6, var4, var8, this.hn, this.v);
      if (var10 == 0 && var4[0] != 0) {
         this.initWorkArea(288);
         var2 = this.huft_build(var3, var1, var2, 0, cpdist, cpdext, var7, var5, var8, this.hn, this.v);
         if (var2 != 0 || var5[0] == 0 && var1 > 257) {
            var1 = var2;
            if (var2 != -3) {
               if (var2 == -5) {
                  var1 = -3;
               } else {
                  var1 = var2;
                  if (var2 != -4) {
                     var1 = -3;
                  }
               }
            }
         } else {
            var1 = 0;
         }
      } else {
         var1 = var10;
         if (var10 != -3) {
            var1 = var10;
            if (var10 != -4) {
               var1 = -3;
            }
         }
      }

      return var1;
   }
}
