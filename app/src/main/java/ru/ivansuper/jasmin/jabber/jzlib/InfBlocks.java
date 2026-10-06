package ru.ivansuper.jasmin.jabber.jzlib;

final class InfBlocks {
   private static final byte BAD = 9;
   private static final byte BTREE = 4;
   private static final byte CODES = 6;
   private static final byte DONE = 8;
   private static final byte DRY = 7;
   private static final byte DTREE = 5;
   private static final byte LENS = 1;
   private static final int MANY = 1440;
   private static final byte STORED = 2;
   private static final byte TABLE = 3;
   private static final byte TYPE = 0;
   private static final byte Z_BUF_ERROR = -5;
   private static final byte Z_DATA_ERROR = -3;
   private static final byte Z_ERRNO = -1;
   private static final byte Z_MEM_ERROR = -4;
   private static final byte Z_NEED_DICT = 2;
   private static final byte Z_OK = 0;
   private static final byte Z_STREAM_END = 1;
   private static final byte Z_STREAM_ERROR = -2;
   private static final byte Z_VERSION_ERROR = -6;
   static final int[] border;
   private static final int[] inflate_mask;
   int[] bb = new int[1];
   int bitb;
   int bitk;
   int[] blens;
   long check;
   Object checkfn;
   InfCodes codes;
   int end;
   int[] hufts;
   int index;
   InfTree inftree;
   int last;
   int left;
   int mode;
   int read;
   int table;
   int[] tb = new int[1];
   byte[] window;
   int write;

   static {
      int[] var0 = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535};
      inflate_mask = var0;
      var0 = new int[]{16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15};
      border = var0;
   }

   InfBlocks(ZStream var1, Object var2, int var3) {
      this.codes = new InfCodes();
      this.inftree = new InfTree();
      this.hufts = new int[4320];
      this.window = new byte[var3];
      this.end = var3;
      this.checkfn = var2;
      this.mode = 0;
      this.reset(var1, null);
   }

   void free(ZStream var1) {
      this.reset(var1, null);
      this.window = null;
      this.hufts = null;
   }

   int inflate_flush(ZStream var1, int var2) {
      int var3 = var1.next_out_index;
      int var4 = this.read;
      int var5;
      if (var4 <= this.write) {
         var5 = this.write;
      } else {
         var5 = this.end;
      }

      var5 -= var4;
      int var6 = var5;
      if (var5 > var1.avail_out) {
         var6 = var1.avail_out;
      }

      var5 = var2;
      if (var6 != 0) {
         var5 = var2;
         if (var2 == -5) {
            var5 = 0;
         }
      }

      var1.avail_out -= var6;
      var1.total_out += var6;
      if (this.checkfn != null) {
         long var7 = var1._adler.adler32(this.check, this.window, var4, var6);
         this.check = var7;
         var1.adler = var7;
      }

      System.arraycopy(this.window, var4, var1.next_out, var3, var6);
      var3 += var6;
      int var9 = var4 + var6;
      var4 = var9;
      var6 = var3;
      var2 = var5;
      if (var9 == this.end) {
         if (this.write == this.end) {
            this.write = 0;
         }

         var2 = this.write - 0;
         var6 = var2;
         if (var2 > var1.avail_out) {
            var6 = var1.avail_out;
         }

         var2 = var5;
         if (var6 != 0) {
            var2 = var5;
            if (var5 == -5) {
               var2 = 0;
            }
         }

         var1.avail_out -= var6;
         var1.total_out += var6;
         if (this.checkfn != null) {
            long var19 = var1._adler.adler32(this.check, this.window, 0, var6);
            this.check = var19;
            var1.adler = var19;
         }

         System.arraycopy(this.window, 0, var1.next_out, var3, var6);
         var5 = var3 + var6;
         var4 = 0 + var6;
         var6 = var5;
      }

      var1.next_out_index = var6;
      this.read = var4;
      return var2;
   }

   int proc(ZStream var1, int var2) {
      int var3 = var1.next_in_index;
      int var4 = var1.avail_in;
      int var5 = this.bitb;
      int var6 = this.bitk;
      int var7 = this.write;
      int var8;
      if (var7 < this.read) {
         var8 = this.read - var7 - 1;
      } else {
         var8 = this.end - var7;
      }

      int var30;
      int var31;
      int var32;
      int var33;
      int var34;
      label309:
      while (true) {
         int var24;
         int var25;
         int var26;
         int var27;
         int var28;
         int var29;
         label302:
         while (true) {
            int var19;
            int var20;
            int var21;
            int var22;
            int var23;
            label300: {
               int var9 = var5;
               int var10 = var6;
               int var11 = var4;
               int var12 = var3;
               int var13 = var2;
               int var14 = var5;
               int var15 = var6;
               int var16 = var4;
               int var17 = var3;
               int var18 = var2;
               var19 = var5;
               var20 = var6;
               var21 = var4;
               var22 = var3;
               var23 = var2;
               var24 = var5;
               var25 = var6;
               var26 = var4;
               var27 = var3;
               var28 = var7;
               var29 = var2;
               var30 = var5;
               var31 = var6;
               var32 = var4;
               var33 = var3;
               var34 = var7;
               switch (this.mode) {
                  case 0:
                     var14 = var2;

                     for (var2 = var3; var6 < 3; var2++) {
                        if (var4 == 0) {
                           this.bitb = var5;
                           this.bitk = var6;
                           var1.avail_in = var4;
                           var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                           var1.next_in_index = var2;
                           this.write = var7;
                           return this.inflate_flush(var1, var14);
                        }

                        var14 = 0;
                        var4--;
                        var5 |= (var1.next_in[var2] & 255) << var6;
                        var6 += 8;
                     }

                     var3 = var5 & 7;
                     this.last = var3 & 1;
                     switch (var3 >>> 1) {
                        case 0:
                           var3 = var6 - 3;
                           var6 = var3 & 7;
                           var5 = var5 >>> 3 >>> var6;
                           var6 = var3 - var6;
                           this.mode = 1;
                           var3 = var2;
                           var2 = var14;
                           continue;
                        case 1:
                           int[] var98 = new int[1];
                           int[] var99 = new int[1];
                           int[][] var100 = new int[1][];
                           int[][] var105 = new int[1][];
                           InfTree.inflate_trees_fixed(var98, var99, var100, var105, var1);
                           this.codes.init(var98[0], var99[0], var100[0], 0, var105[0], 0, var1);
                           var5 >>>= 3;
                           var6 -= 3;
                           this.mode = 6;
                           var3 = var2;
                           var2 = var14;
                           continue;
                        case 2:
                           var5 >>>= 3;
                           var6 -= 3;
                           this.mode = 3;
                           var3 = var2;
                           var2 = var14;
                           continue;
                        case 3:
                           this.mode = 9;
                           this.bitb = var5 >>> 3;
                           this.bitk = var6 - 3;
                           var1.avail_in = var4;
                           var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                           var1.next_in_index = var2;
                           this.write = var7;
                           return this.inflate_flush(var1, -3);
                        default:
                           var3 = var2;
                           var2 = var14;
                           continue;
                     }
                  case 1:
                     var14 = var2;

                     for (var2 = var3; var6 < 32; var2++) {
                        if (var4 == 0) {
                           this.bitb = var5;
                           this.bitk = var6;
                           var1.avail_in = var4;
                           var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                           var1.next_in_index = var2;
                           this.write = var7;
                           return this.inflate_flush(var1, var14);
                        }

                        var14 = 0;
                        var4--;
                        var5 |= (var1.next_in[var2] & 255) << var6;
                        var6 += 8;
                     }

                     if ((~var5 >>> 16 & 65535) != (65535 & var5)) {
                        this.mode = 9;
                        this.bitb = var5;
                        this.bitk = var6;
                        var1.avail_in = var4;
                        var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                        var1.next_in_index = var2;
                        this.write = var7;
                        return this.inflate_flush(var1, -3);
                     }

                     this.left = 65535 & var5;
                     var6 = 0;
                     var5 = 0;
                     byte var65;
                     if (this.left != 0) {
                        var65 = 2;
                     } else if (this.last != 0) {
                        var65 = 7;
                     } else {
                        var65 = 0;
                     }

                     this.mode = var65;
                     var3 = var2;
                     var2 = var14;
                     continue;
                  case 2:
                     if (var4 == 0) {
                        this.bitb = var5;
                        this.bitk = var6;
                        var1.avail_in = var4;
                        var1.total_in = var1.total_in + (var3 - var1.next_in_index);
                        var1.next_in_index = var3;
                        this.write = var7;
                        return this.inflate_flush(var1, var2);
                     }

                     var14 = var8;
                     var17 = var7;
                     if (var8 == 0) {
                        var15 = var8;
                        var16 = var7;
                        if (var7 == this.end) {
                           var15 = var8;
                           var16 = var7;
                           if (this.read != 0) {
                              var16 = 0;
                              if (this.read < 0) {
                                 var15 = this.read - 0 - 1;
                              } else {
                                 var15 = this.end - 0;
                              }
                           }
                        }

                        var14 = var15;
                        var17 = var16;
                        if (var15 == 0) {
                           this.write = var16;
                           var15 = this.inflate_flush(var1, var2);
                           var14 = this.write;
                           if (var14 < this.read) {
                              var8 = this.read - var14 - 1;
                           } else {
                              var8 = this.end - var14;
                           }

                           var2 = var8;
                           var7 = var14;
                           if (var14 == this.end) {
                              var2 = var8;
                              var7 = var14;
                              if (this.read != 0) {
                                 var7 = 0;
                                 if (this.read < 0) {
                                    var2 = this.read - 0 - 1;
                                 } else {
                                    var2 = this.end - 0;
                                 }
                              }
                           }

                           var14 = var2;
                           var17 = var7;
                           if (var2 == 0) {
                              this.bitb = var5;
                              this.bitk = var6;
                              var1.avail_in = var4;
                              var1.total_in = var1.total_in + (var3 - var1.next_in_index);
                              var1.next_in_index = var3;
                              this.write = var7;
                              return this.inflate_flush(var1, var15);
                           }
                        }
                     }

                     byte var91 = 0;
                     var7 = this.left;
                     var2 = var7;
                     if (var7 > var4) {
                        var2 = var4;
                     }

                     var7 = var2;
                     if (var2 > var14) {
                        var7 = var14;
                     }

                     System.arraycopy(var1.next_in, var3, this.window, var17, var7);
                     var10 = var3 + var7;
                     var16 = var4 - var7;
                     var17 += var7;
                     var14 -= var7;
                     var9 = this.left - var7;
                     this.left = var9;
                     var8 = var14;
                     var4 = var16;
                     var3 = var10;
                     var7 = var17;
                     var2 = var91;
                     if (var9 == 0) {
                        byte var52;
                        if (this.last != 0) {
                           var52 = 7;
                        } else {
                           var52 = 0;
                        }

                        this.mode = var52;
                        var8 = var14;
                        var4 = var16;
                        var3 = var10;
                        var7 = var17;
                        var2 = var91;
                     }
                     continue;
                  case 3:
                     var13 = var2;

                     for (var2 = var3; var6 < 14; var2++) {
                        if (var4 == 0) {
                           this.bitb = var5;
                           this.bitk = var6;
                           var1.avail_in = var4;
                           var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                           var1.next_in_index = var2;
                           this.write = var7;
                           return this.inflate_flush(var1, var13);
                        }

                        var13 = 0;
                        var4--;
                        var5 |= (var1.next_in[var2] & 255) << var6;
                        var6 += 8;
                     }

                     var3 = var5 & 16383;
                     this.table = var3;
                     if ((var3 & 31) > 29 || (var3 >> 5 & 31) > 29) {
                        this.mode = 9;
                        this.bitb = var5;
                        this.bitk = var6;
                        var1.avail_in = var4;
                        var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                        var1.next_in_index = var2;
                        this.write = var7;
                        return this.inflate_flush(var1, -3);
                     }

                     var8 = (var3 & 31) + 258 + (var3 >> 5 & 31);
                     if (this.blens != null && this.blens.length >= var8) {
                        for (int var56 = 0; var56 < var8; var56++) {
                           this.blens[var56] = 0;
                        }
                     } else {
                        this.blens = new int[var8];
                     }

                     var9 = var5 >>> 14;
                     var10 = var6 - 14;
                     this.index = 0;
                     this.mode = 4;
                     var12 = var2;
                     var11 = var4;
                  case 4:
                     while (true) {
                        if (this.index >= (this.table >>> 10) + 4) {
                           while (this.index < 19) {
                              int[] var101 = this.blens;
                              int[] var96 = border;
                              var2 = this.index++;
                              var101[var96[var2]] = 0;
                           }

                           this.bb[0] = 7;
                           var2 = this.inftree.inflate_trees_bits(this.blens, this.bb, this.tb, this.hufts, var1);
                           if (var2 != 0) {
                              if (var2 == -3) {
                                 this.blens = null;
                                 this.mode = 9;
                              }

                              this.bitb = var9;
                              this.bitk = var10;
                              var1.avail_in = var11;
                              var1.total_in = var1.total_in + (var12 - var1.next_in_index);
                              var1.next_in_index = var12;
                              this.write = var7;
                              return this.inflate_flush(var1, var2);
                           }

                           this.index = 0;
                           this.mode = 5;
                           var18 = var13;
                           var17 = var12;
                           var16 = var11;
                           var15 = var10;
                           var14 = var9;
                           break;
                        }

                        for (var2 = var12; var10 < 3; var2++) {
                           if (var11 == 0) {
                              this.bitb = var9;
                              this.bitk = var10;
                              var1.avail_in = var11;
                              var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                              var1.next_in_index = var2;
                              this.write = var7;
                              return this.inflate_flush(var1, var13);
                           }

                           var13 = 0;
                           var11--;
                           var9 |= (var1.next_in[var2] & 255) << var10;
                           var10 += 8;
                        }

                        int[] var38 = this.blens;
                        int[] var35 = border;
                        var3 = this.index++;
                        var38[var35[var3]] = var9 & 7;
                        var9 >>>= 3;
                        var10 -= 3;
                        var12 = var2;
                     }
                  case 5:
                     break;
                  case 6:
                     break label300;
                  case 7:
                     break label302;
                  case 8:
                     break label309;
                  case 9:
                     this.bitb = var5;
                     this.bitk = var6;
                     var1.avail_in = var4;
                     var1.total_in = var1.total_in + (var3 - var1.next_in_index);
                     var1.next_in_index = var3;
                     this.write = var7;
                     return this.inflate_flush(var1, -3);
                  default:
                     this.bitb = var5;
                     this.bitk = var6;
                     var1.avail_in = var4;
                     var1.total_in = var1.total_in + (var3 - var1.next_in_index);
                     var1.next_in_index = var3;
                     this.write = var7;
                     return this.inflate_flush(var1, -2);
               }

               while (true) {
                  var2 = this.table;
                  if (this.index >= (var2 & 31) + 258 + (var2 >> 5 & 31)) {
                     this.tb[0] = -1;
                     int[] var104 = new int[1];
                     int[] var36 = new int[1];
                     int[] var97 = new int[1];
                     int[] var37 = new int[1];
                     var104[0] = 9;
                     var36[0] = 6;
                     var2 = this.table;
                     var2 = this.inftree
                        .inflate_trees_dynamic((var2 & 31) + 257, (var2 >> 5 & 31) + 1, this.blens, var104, var36, var97, var37, this.hufts, var1);
                     if (var2 != 0) {
                        if (var2 == -3) {
                           this.blens = null;
                           this.mode = 9;
                        }

                        this.bitb = var14;
                        this.bitk = var15;
                        var1.avail_in = var16;
                        var1.total_in = var1.total_in + (var17 - var1.next_in_index);
                        var1.next_in_index = var17;
                        this.write = var7;
                        return this.inflate_flush(var1, var2);
                     }

                     this.codes.init(var104[0], var36[0], this.hufts, var97[0], this.hufts, var37[0], var1);
                     this.mode = 6;
                     var23 = var18;
                     var22 = var17;
                     var21 = var16;
                     var20 = var15;
                     var19 = var14;
                     break;
                  }

                  var3 = this.bb[0];

                  for (var2 = var17; var15 < var3; var2++) {
                     if (var16 == 0) {
                        this.bitb = var14;
                        this.bitk = var15;
                        var1.avail_in = var16;
                        var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                        var1.next_in_index = var2;
                        this.write = var7;
                        return this.inflate_flush(var1, var18);
                     }

                     var18 = 0;
                     var16--;
                     var14 |= (var1.next_in[var2] & 255) << var15;
                     var15 += 8;
                  }

                  var5 = this.tb[0];
                  var8 = this.hufts[(this.tb[0] + (inflate_mask[var3] & var14)) * 3 + 1];
                  var4 = this.hufts[(this.tb[0] + (inflate_mask[var8] & var14)) * 3 + 2];
                  if (var4 < 16) {
                     var14 >>>= var8;
                     var15 -= var8;
                     int[] var103 = this.blens;
                     var3 = this.index++;
                     var103[var3] = var4;
                     var17 = var2;
                  } else {
                     if (var4 == 18) {
                        var3 = 7;
                     } else {
                        var3 = var4 - 14;
                     }

                     int var71;
                     if (var4 == 18) {
                        var71 = 11;
                     } else {
                        var71 = 3;
                     }

                     while (var15 < var8 + var3) {
                        if (var16 == 0) {
                           this.bitb = var14;
                           this.bitk = var15;
                           var1.avail_in = var16;
                           var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                           var1.next_in_index = var2;
                           this.write = var7;
                           return this.inflate_flush(var1, var18);
                        }

                        var18 = 0;
                        var16--;
                        var14 |= (var1.next_in[var2] & 255) << var15;
                        var15 += 8;
                        var2++;
                     }

                     var14 >>>= var8;
                     var6 = var71 + (inflate_mask[var3] & var14);
                     var14 >>>= var3;
                     var15 = var15 - var8 - var3;
                     var71 = this.index;
                     var3 = this.table;
                     if (var71 + var6 > (var3 & 31) + 258 + (var3 >> 5 & 31) || var4 == 16 && var71 < 1) {
                        this.blens = null;
                        this.mode = 9;
                        this.bitb = var14;
                        this.bitk = var15;
                        var1.avail_in = var16;
                        var1.total_in = var1.total_in + (var2 - var1.next_in_index);
                        var1.next_in_index = var2;
                        this.write = var7;
                        return this.inflate_flush(var1, -3);
                     }

                     if (var4 == 16) {
                        var3 = this.blens[var71 - 1];
                     } else {
                        var3 = 0;
                     }

                     while (true) {
                        int[] var102 = this.blens;
                        var4 = var71 + 1;
                        var102[var71] = var3;
                        if (--var6 == 0) {
                           this.index = var4;
                           var17 = var2;
                           break;
                        }

                        var71 = var4;
                     }
                  }
               }
            }

            this.bitb = var19;
            this.bitk = var20;
            var1.avail_in = var21;
            var1.total_in = var1.total_in + (var22 - var1.next_in_index);
            var1.next_in_index = var22;
            this.write = var7;
            var2 = this.codes.proc(this, var1, var23);
            if (var2 != 1) {
               return this.inflate_flush(var1, var2);
            }

            var29 = 0;
            var2 = 0;
            this.codes.free(var1);
            var3 = var1.next_in_index;
            var4 = var1.avail_in;
            var5 = this.bitb;
            var6 = this.bitk;
            var7 = this.write;
            if (var7 < this.read) {
               var8 = this.read - var7 - 1;
            } else {
               var8 = this.end - var7;
            }

            if (this.last != 0) {
               this.mode = 7;
               var28 = var7;
               var27 = var3;
               var26 = var4;
               var25 = var6;
               var24 = var5;
               break;
            }

            this.mode = 0;
         }

         this.write = var28;
         var2 = this.inflate_flush(var1, var29);
         var34 = this.write;
         if (var34 < this.read) {
            var3 = this.read;
         } else {
            var3 = this.end;
         }

         if (this.read != this.write) {
            this.bitb = var24;
            this.bitk = var25;
            var1.avail_in = var26;
            var1.total_in = var1.total_in + (var27 - var1.next_in_index);
            var1.next_in_index = var27;
            this.write = var34;
            return this.inflate_flush(var1, var2);
         }

         this.mode = 8;
         var33 = var27;
         var32 = var26;
         var31 = var25;
         var30 = var24;
         break;
      }

      this.bitb = var30;
      this.bitk = var31;
      var1.avail_in = var32;
      var1.total_in = var1.total_in + (var33 - var1.next_in_index);
      var1.next_in_index = var33;
      this.write = var34;
      return this.inflate_flush(var1, 1);
   }

   void reset(ZStream var1, long[] var2) {
      if (var2 != null) {
         var2[0] = this.check;
      }

      if (this.mode != 4 && this.mode != 5 && this.mode == 6) {
         this.codes.free(var1);
      }

      this.mode = 0;
      this.bitk = 0;
      this.bitb = 0;
      this.write = 0;
      this.read = 0;
      if (this.checkfn != null) {
         long var3 = var1._adler.adler32(0L, null, 0, 0);
         this.check = var3;
         var1.adler = var3;
      }
   }

   void set_dictionary(byte[] var1, int var2, int var3) {
      System.arraycopy(var1, var2, this.window, 0, var3);
      this.write = var3;
      this.read = var3;
   }

   int sync_point() {
      byte var1 = 1;
      if (this.mode != 1) {
         var1 = 0;
      }

      return var1;
   }
}
