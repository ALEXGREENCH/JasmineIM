package ru.ivansuper.jasmin.jabber.jzlib;

final class InfCodes {
   private static final byte BADCODE = 9;
   private static final byte COPY = 5;
   private static final byte DIST = 3;
   private static final byte DISTEXT = 4;
   private static final byte END = 8;
   private static final byte LEN = 1;
   private static final byte LENEXT = 2;
   private static final byte LIT = 6;
   private static final byte START = 0;
   private static final byte WASH = 7;
   private static final byte Z_BUF_ERROR = -5;
   private static final byte Z_DATA_ERROR = -3;
   private static final byte Z_ERRNO = -1;
   private static final byte Z_MEM_ERROR = -4;
   private static final byte Z_NEED_DICT = 2;
   private static final byte Z_OK = 0;
   private static final byte Z_STREAM_END = 1;
   private static final byte Z_STREAM_ERROR = -2;
   private static final byte Z_VERSION_ERROR = -6;
   private static final int[] inflate_mask;
   byte dbits;
   int dist;
   int[] dtree;
   int dtree_index;
   int get;
   byte lbits;
   int len;
   int lit;
   int[] ltree;
   int ltree_index;
   int mode;
   int need;
   int[] tree;
   int tree_index = 0;

   static {
      int[] var0 = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535};
      inflate_mask = var0;
   }

   void free(ZStream var1) {
   }

   int inflate_fast(int var1, int var2, int[] var3, int var4, int[] var5, int var6, InfBlocks var7, ZStream var8) {
      int var9 = var8.next_in_index;
      int var10 = var8.avail_in;
      int var11 = var7.bitb;
      int var12 = var7.bitk;
      int var13 = var7.write;
      int var14;
      if (var13 < var7.read) {
         var14 = var7.read - var13 - 1;
      } else {
         var14 = var7.end - var13;
      }

      int var15 = inflate_mask[var1];
      int var16 = inflate_mask[var2];
      var2 = var13;
      var1 = var9;
      var9 = var14;
      var14 = var12;

      label152:
      do {
         while (var14 < 20) {
            var10--;
            var11 |= (var8.next_in[var1] & 255) << var14;
            var14 += 8;
            var1++;
         }

         int var17 = var11 & var15;
         int var18 = (var4 + var17) * 3;
         int var19 = var3[var18];
         var12 = var11;
         var13 = var19;
         int var20 = var14;
         int var21 = var18;
         if (var19 == 0) {
            var11 >>= var3[var18 + 1];
            var14 -= var3[var18 + 1];
            byte[] var79 = var7.window;
            var12 = var2 + 1;
            var79[var2] = (byte)((byte)var3[var18 + 2]);
            var9--;
            var2 = var12;
         } else {
            do {
               var11 = var12 >> var3[var21 + 1];
               var14 = var20 - var3[var21 + 1];
               if ((var13 & 16) != 0) {
                  var13 &= 15;
                  var17 = var3[var21 + 2] + (inflate_mask[var13] & var11);
                  var12 = var11 >> var13;
                  var11 = var14 - var13;
                  var14 = var12;

                  while (var11 < 15) {
                     var10--;
                     var14 |= (var8.next_in[var1] & 255) << var11;
                     var11 += 8;
                     var1++;
                  }

                  var12 = var14 & var16;
                  var13 = (var6 + var12) * 3;
                  var21 = var5[var13];
                  var20 = var11;

                  while (true) {
                     var11 = var14 >> var5[var13 + 1];
                     var20 -= var5[var13 + 1];
                     if ((var21 & 16) != 0) {
                        var21 &= 15;
                        var14 = var1;
                        var12 = var10;

                        while (var20 < var21) {
                           var12--;
                           var11 |= (var8.next_in[var14] & 255) << var20;
                           var20 += 8;
                           var14++;
                        }

                        var1 = var5[var13 + 2] + (inflate_mask[var21] & var11);
                        var13 = var11 >> var21;
                        var20 -= var21;
                        var9 -= var17;
                        if (var2 >= var1) {
                           var1 = var2 - var1;
                           if (var2 - var1 > 0 && 2 > var2 - var1) {
                              byte[] var23 = var7.window;
                              var11 = var2 + 1;
                              byte[] var76 = var7.window;
                              var21 = var1 + 1;
                              var23[var2] = (byte)var76[var1];
                              var23 = var7.window;
                              var76 = var7.window;
                              var10 = var21 + 1;
                              var23[var11] = (byte)var76[var21];
                              var1 = var17 - 2;
                              var2 = var11 + 1;
                           } else {
                              System.arraycopy(var7.window, var1, var7.window, var2, 2);
                              var2 += 2;
                              var10 = var1 + 2;
                              var1 = var17 - 2;
                           }
                        } else {
                           var1 = var2 - var1;

                           do {
                              var10 = var1 + var7.end;
                              var1 = var10;
                           } while (var10 < 0);

                           var1 = var7.end - var10;
                           if (var17 <= var1) {
                              var1 = var17;
                           } else {
                              var21 = var17 - var1;
                              if (var2 - var10 > 0 && var1 > var2 - var10) {
                                 while (true) {
                                    byte[] var75 = var7.window;
                                    var11 = var2 + 1;
                                    var75[var2] = (byte)var7.window[var10];
                                    if (--var1 == 0) {
                                       var2 = var11;
                                       break;
                                    }

                                    var10++;
                                    var2 = var11;
                                 }
                              } else {
                                 System.arraycopy(var7.window, var10, var7.window, var2, var1);
                                 var2 += var1;
                              }

                              var10 = 0;
                              var1 = var21;
                           }
                        }

                        if (var2 - var10 > 0 && var1 > var2 - var10) {
                           var11 = var2;

                           while (true) {
                              byte[] var78 = var7.window;
                              var2 = var11 + 1;
                              var78[var11] = (byte)var7.window[var10];
                              if (--var1 == 0) {
                                 var1 = var14;
                                 var11 = var13;
                                 var14 = var20;
                                 var10 = var12;
                                 continue label152;
                              }

                              var10++;
                              var11 = var2;
                           }
                        } else {
                           System.arraycopy(var7.window, var10, var7.window, var2, var1);
                           var2 += var1;
                           var1 = var14;
                           var11 = var13;
                           var14 = var20;
                           var10 = var12;
                           continue label152;
                        }
                     }

                     if ((var21 & 64) != 0) {
                        var6 = var8.avail_in - var10;
                        var4 = var6;
                        if (var20 >> 3 < var6) {
                           var4 = var20 >> 3;
                        }

                        var1 -= var4;
                        var7.bitb = var11;
                        var7.bitk = var20 - (var4 << 3);
                        var8.avail_in = var10 + var4;
                        var8.total_in = var8.total_in + (var1 - var8.next_in_index);
                        var8.next_in_index = var1;
                        var7.write = var2;
                        return -3;
                     }

                     var12 = var12 + var5[var13 + 2] + (inflate_mask[var21] & var11);
                     var13 = (var6 + var12) * 3;
                     var21 = var5[var13];
                     var14 = var11;
                  }
               }

               if ((var13 & 64) != 0) {
                  int var26;
                  if ((var13 & 32) != 0) {
                     var6 = var8.avail_in - var10;
                     var4 = var6;
                     if (var14 >> 3 < var6) {
                        var4 = var14 >> 3;
                     }

                     var26 = var1 - var4;
                     var7.bitb = var11;
                     var7.bitk = var14 - (var4 << 3);
                     var8.avail_in = var10 + var4;
                     var8.total_in = var8.total_in + (var26 - var8.next_in_index);
                     var8.next_in_index = var26;
                     var7.write = var2;
                     var26 = 1;
                  } else {
                     var6 = var8.avail_in - var10;
                     var4 = var6;
                     if (var14 >> 3 < var6) {
                        var4 = var14 >> 3;
                     }

                     var26 = var1 - var4;
                     var7.bitb = var11;
                     var7.bitk = var14 - (var4 << 3);
                     var8.avail_in = var10 + var4;
                     var8.total_in = var8.total_in + (var26 - var8.next_in_index);
                     var8.next_in_index = var26;
                     var7.write = var2;
                     var26 = -3;
                  }

                  return var26;
               }

               var17 = var17 + var3[var21 + 2] + (inflate_mask[var13] & var11);
               var18 = (var4 + var17) * 3;
               var19 = var3[var18];
               var12 = var11;
               var13 = var19;
               var20 = var14;
               var21 = var18;
            } while (var19 != 0);

            var11 >>= var3[var18 + 1];
            var14 -= var3[var18 + 1];
            byte[] var22 = var7.window;
            var12 = var2 + 1;
            var22[var2] = (byte)((byte)var3[var18 + 2]);
            var9--;
            var2 = var12;
         }
      } while (var9 >= 258 && var10 >= 10);

      var6 = var8.avail_in - var10;
      var4 = var6;
      if (var14 >> 3 < var6) {
         var4 = var14 >> 3;
      }

      var1 -= var4;
      var7.bitb = var11;
      var7.bitk = var14 - (var4 << 3);
      var8.avail_in = var10 + var4;
      var8.total_in = var8.total_in + (var1 - var8.next_in_index);
      var8.next_in_index = var1;
      var7.write = var2;
      return 0;
   }

   void init(int var1, int var2, int[] var3, int var4, int[] var5, int var6, ZStream var7) {
      this.mode = 0;
      this.lbits = (byte)((byte)var1);
      this.dbits = (byte)((byte)var2);
      this.ltree = var3;
      this.ltree_index = var4;
      this.dtree = var5;
      this.dtree_index = var6;
      this.tree = null;
   }

   int proc(InfBlocks var1, ZStream var2, int var3) {
      int var4 = var2.next_in_index;
      int var5 = var2.avail_in;
      int var6 = var1.bitb;
      int var7 = var1.bitk;
      int var8 = var1.write;
      int var9;
      if (var8 < var1.read) {
         var9 = var1.read - var8 - 1;
      } else {
         var9 = var1.end - var8;
      }

      while (true) {
         int var10 = var6;
         int var11 = var7;
         int var12 = var9;
         int var13 = var5;
         int var14 = var4;
         int var15 = var8;
         int var16 = var3;
         int var17 = var6;
         int var18 = var7;
         int var19 = var5;
         int var20 = var4;
         int var21 = var3;
         int var22 = var6;
         int var23 = var7;
         int var24 = var5;
         int var25 = var4;
         int var26 = var3;
         int var27 = var7;
         int var28 = var5;
         int var29 = var4;
         int var30 = var8;
         switch (this.mode) {
            case 0:
               var10 = var6;
               var11 = var7;
               var26 = var9;
               var13 = var5;
               var14 = var4;
               var21 = var8;
               var16 = var3;
               if (var9 >= 258) {
                  var10 = var6;
                  var11 = var7;
                  var26 = var9;
                  var13 = var5;
                  var14 = var4;
                  var21 = var8;
                  var16 = var3;
                  if (var5 >= 10) {
                     var1.bitb = var6;
                     var1.bitk = var7;
                     var2.avail_in = var5;
                     var2.total_in = var2.total_in + (var4 - var2.next_in_index);
                     var2.next_in_index = var4;
                     var1.write = var8;
                     var3 = this.inflate_fast(this.lbits, this.dbits, this.ltree, this.ltree_index, this.dtree, this.dtree_index, var1, var2);
                     var4 = var2.next_in_index;
                     var5 = var2.avail_in;
                     var6 = var1.bitb;
                     var7 = var1.bitk;
                     var8 = var1.write;
                     if (var8 < var1.read) {
                        var9 = var1.read - var8 - 1;
                     } else {
                        var9 = var1.end - var8;
                     }

                     var10 = var6;
                     var11 = var7;
                     var26 = var9;
                     var13 = var5;
                     var14 = var4;
                     var21 = var8;
                     var16 = var3;
                     if (var3 != 0) {
                        byte var72;
                        if (var3 == 1) {
                           var72 = 7;
                        } else {
                           var72 = 9;
                        }

                        this.mode = var72;
                        break;
                     }
                  }
               }

               this.need = this.lbits;
               this.tree = this.ltree;
               this.tree_index = this.ltree_index;
               this.mode = 1;
               var15 = var21;
               var12 = var26;
            case 1:
               var4 = this.need;
               var3 = var14;
               var5 = var13;

               while (var11 < var4) {
                  if (var5 == 0) {
                     var1.bitb = var10;
                     var1.bitk = var11;
                     var2.avail_in = var5;
                     var2.total_in = var2.total_in + (var3 - var2.next_in_index);
                     var2.next_in_index = var3;
                     var1.write = var15;
                     return var1.inflate_flush(var2, var16);
                  }

                  var16 = 0;
                  var5--;
                  var10 |= (var2.next_in[var3] & 255) << var11;
                  var11 += 8;
                  var3++;
               }

               var4 = (this.tree_index + (inflate_mask[var4] & var10)) * 3;
               var6 = var10 >>> this.tree[var4 + 1];
               var7 = var11 - this.tree[var4 + 1];
               var9 = this.tree[var4];
               if (var9 == 0) {
                  this.lit = this.tree[var4 + 2];
                  this.mode = 6;
                  var9 = var12;
                  var4 = var3;
                  var8 = var15;
                  var3 = var16;
               } else if ((var9 & 16) != 0) {
                  this.get = var9 & 15;
                  this.len = this.tree[var4 + 2];
                  this.mode = 2;
                  var9 = var12;
                  var4 = var3;
                  var8 = var15;
                  var3 = var16;
               } else if ((var9 & 64) == 0) {
                  this.need = var9;
                  this.tree_index = var4 / 3 + this.tree[var4 + 2];
                  var9 = var12;
                  var4 = var3;
                  var8 = var15;
                  var3 = var16;
               } else {
                  if ((var9 & 32) == 0) {
                     this.mode = 9;
                     var1.bitb = var6;
                     var1.bitk = var7;
                     var2.avail_in = var5;
                     var2.total_in = var2.total_in + (var3 - var2.next_in_index);
                     var2.next_in_index = var3;
                     var1.write = var15;
                     return var1.inflate_flush(var2, -3);
                  }

                  this.mode = 7;
                  var9 = var12;
                  var4 = var3;
                  var8 = var15;
                  var3 = var16;
               }
               break;
            case 2:
               var16 = this.get;
               var21 = var3;

               for (var3 = var4; var7 < var16; var3++) {
                  if (var5 == 0) {
                     var1.bitb = var6;
                     var1.bitk = var7;
                     var2.avail_in = var5;
                     var2.total_in = var2.total_in + (var3 - var2.next_in_index);
                     var2.next_in_index = var3;
                     var1.write = var8;
                     return var1.inflate_flush(var2, var21);
                  }

                  var21 = 0;
                  var5--;
                  var6 |= (var2.next_in[var3] & 255) << var7;
                  var7 += 8;
               }

               this.len = this.len + (inflate_mask[var16] & var6);
               var17 = var6 >> var16;
               var18 = var7 - var16;
               this.need = this.dbits;
               this.tree = this.dtree;
               this.tree_index = this.dtree_index;
               this.mode = 3;
               var20 = var3;
               var19 = var5;
            case 3:
               var4 = this.need;
               var3 = var20;
               var5 = var19;

               while (var18 < var4) {
                  if (var5 == 0) {
                     var1.bitb = var17;
                     var1.bitk = var18;
                     var2.avail_in = var5;
                     var2.total_in = var2.total_in + (var3 - var2.next_in_index);
                     var2.next_in_index = var3;
                     var1.write = var8;
                     return var1.inflate_flush(var2, var21);
                  }

                  var21 = 0;
                  var5--;
                  var17 |= (var2.next_in[var3] & 255) << var18;
                  var18 += 8;
                  var3++;
               }

               var4 = (this.tree_index + (inflate_mask[var4] & var17)) * 3;
               var6 = var17 >> this.tree[var4 + 1];
               var7 = var18 - this.tree[var4 + 1];
               var16 = this.tree[var4];
               if ((var16 & 16) != 0) {
                  this.get = var16 & 15;
                  this.dist = this.tree[var4 + 2];
                  this.mode = 4;
                  var4 = var3;
                  var3 = var21;
                  break;
               } else {
                  if ((var16 & 64) == 0) {
                     this.need = var16;
                     this.tree_index = var4 / 3 + this.tree[var4 + 2];
                     var4 = var3;
                     var3 = var21;
                     break;
                  }

                  this.mode = 9;
                  var1.bitb = var6;
                  var1.bitk = var7;
                  var2.avail_in = var5;
                  var2.total_in = var2.total_in + (var3 - var2.next_in_index);
                  var2.next_in_index = var3;
                  var1.write = var8;
                  return var1.inflate_flush(var2, -3);
               }
            case 4:
               var16 = this.get;
               var26 = var3;

               for (var3 = var4; var7 < var16; var3++) {
                  if (var5 == 0) {
                     var1.bitb = var6;
                     var1.bitk = var7;
                     var2.avail_in = var5;
                     var2.total_in = var2.total_in + (var3 - var2.next_in_index);
                     var2.next_in_index = var3;
                     var1.write = var8;
                     return var1.inflate_flush(var2, var26);
                  }

                  var26 = 0;
                  var5--;
                  var6 |= (var2.next_in[var3] & 255) << var7;
                  var7 += 8;
               }

               this.dist = this.dist + (inflate_mask[var16] & var6);
               var22 = var6 >> var16;
               var23 = var7 - var16;
               this.mode = 5;
               var25 = var3;
               var24 = var5;
            case 5:
               var3 = var8 - this.dist;

               while (var3 < 0) {
                  var3 += var1.end;
               }

               var6 = var3;

               while (this.len != 0) {
                  var7 = var9;
                  var4 = var8;
                  var16 = var26;
                  if (var9 == 0) {
                     var3 = var9;
                     var5 = var8;
                     if (var8 == var1.end) {
                        var3 = var9;
                        var5 = var8;
                        if (var1.read != 0) {
                           var5 = 0;
                           if (var1.read < 0) {
                              var3 = var1.read - 0 - 1;
                           } else {
                              var3 = var1.end - 0;
                           }
                        }
                     }

                     var7 = var3;
                     var4 = var5;
                     var16 = var26;
                     if (var3 == 0) {
                        var1.write = var5;
                        var9 = var1.inflate_flush(var2, var26);
                        var7 = var1.write;
                        if (var7 < var1.read) {
                           var4 = var1.read - var7 - 1;
                        } else {
                           var4 = var1.end - var7;
                        }

                        var3 = var4;
                        var5 = var7;
                        if (var7 == var1.end) {
                           var3 = var4;
                           var5 = var7;
                           if (var1.read != 0) {
                              var5 = 0;
                              if (var1.read < 0) {
                                 var3 = var1.read - 0 - 1;
                              } else {
                                 var3 = var1.end - 0;
                              }
                           }
                        }

                        var7 = var3;
                        var4 = var5;
                        var16 = var9;
                        if (var3 == 0) {
                           var1.bitb = var22;
                           var1.bitk = var23;
                           var2.avail_in = var24;
                           var2.total_in = var2.total_in + (var25 - var2.next_in_index);
                           var2.next_in_index = var25;
                           var1.write = var5;
                           return var1.inflate_flush(var2, var9);
                        }
                     }
                  }

                  byte[] var31 = var1.window;
                  byte[] var32 = var1.window;
                  var3 = var6 + 1;
                  var31[var4] = (byte)var32[var6];
                  var9 = var7 - 1;
                  if (var3 == var1.end) {
                     var3 = 0;
                  }

                  this.len--;
                  var8 = var4 + 1;
                  var6 = var3;
                  var26 = var16;
               }

               this.mode = 0;
               var6 = var22;
               var7 = var23;
               var5 = var24;
               var4 = var25;
               var3 = var26;
               break;
            case 6:
               var13 = var9;
               var26 = var8;
               if (var9 == 0) {
                  var16 = var9;
                  var11 = var8;
                  if (var8 == var1.end) {
                     var16 = var9;
                     var11 = var8;
                     if (var1.read != 0) {
                        var11 = 0;
                        if (var1.read < 0) {
                           var16 = var1.read - 0 - 1;
                        } else {
                           var16 = var1.end - 0;
                        }
                     }
                  }

                  var13 = var16;
                  var26 = var11;
                  if (var16 == 0) {
                     var1.write = var11;
                     var11 = var1.inflate_flush(var2, var3);
                     var16 = var1.write;
                     if (var16 < var1.read) {
                        var8 = var1.read - var16 - 1;
                     } else {
                        var8 = var1.end - var16;
                     }

                     var3 = var8;
                     var9 = var16;
                     if (var16 == var1.end) {
                        var3 = var8;
                        var9 = var16;
                        if (var1.read != 0) {
                           var9 = 0;
                           if (var1.read < 0) {
                              var3 = var1.read - 0 - 1;
                           } else {
                              var3 = var1.end - 0;
                           }
                        }
                     }

                     var13 = var3;
                     var26 = var9;
                     if (var3 == 0) {
                        var1.bitb = var6;
                        var1.bitk = var7;
                        var2.avail_in = var5;
                        var2.total_in = var2.total_in + (var4 - var2.next_in_index);
                        var2.next_in_index = var4;
                        var1.write = var9;
                        return var1.inflate_flush(var2, var11);
                     }
                  }
               }

               var3 = 0;
               var1.window[var26] = (byte)((byte)this.lit);
               var9 = var13 - 1;
               this.mode = 0;
               var8 = var26 + 1;
               break;
            case 7:
               var9 = var7;
               var26 = var5;
               var16 = var4;
               if (var7 > 7) {
                  var9 = var7 - 8;
                  var26 = var5 + 1;
                  var16 = var4 - 1;
               }

               var1.write = var8;
               var3 = var1.inflate_flush(var2, var3);
               var30 = var1.write;
               if (var30 < var1.read) {
                  var4 = var1.read;
               } else {
                  var4 = var1.end;
               }

               if (var1.read != var1.write) {
                  var1.bitb = var6;
                  var1.bitk = var9;
                  var2.avail_in = var26;
                  var2.total_in = var2.total_in + (var16 - var2.next_in_index);
                  var2.next_in_index = var16;
                  var1.write = var30;
                  return var1.inflate_flush(var2, var3);
               }

               this.mode = 8;
               var29 = var16;
               var28 = var26;
               var27 = var9;
            case 8:
               var1.bitb = var6;
               var1.bitk = var27;
               var2.avail_in = var28;
               var2.total_in = var2.total_in + (var29 - var2.next_in_index);
               var2.next_in_index = var29;
               var1.write = var30;
               return var1.inflate_flush(var2, 1);
            case 9:
               var1.bitb = var6;
               var1.bitk = var7;
               var2.avail_in = var5;
               var2.total_in = var2.total_in + (var4 - var2.next_in_index);
               var2.next_in_index = var4;
               var1.write = var8;
               return var1.inflate_flush(var2, -3);
            default:
               var1.bitb = var6;
               var1.bitk = var7;
               var2.avail_in = var5;
               var2.total_in = var2.total_in + (var4 - var2.next_in_index);
               var2.next_in_index = var4;
               var1.write = var8;
               return var1.inflate_flush(var2, -2);
         }
      }
   }
}
