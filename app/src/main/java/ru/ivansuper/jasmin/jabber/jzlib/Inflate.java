package ru.ivansuper.jasmin.jabber.jzlib;

final class Inflate {
   private static final byte BAD = 13;
   private static final byte BLOCKS = 7;
   private static final byte CHECK1 = 11;
   private static final byte CHECK2 = 10;
   private static final byte CHECK3 = 9;
   private static final byte CHECK4 = 8;
   private static final byte DICT0 = 6;
   private static final byte DICT1 = 5;
   private static final byte DICT2 = 4;
   private static final byte DICT3 = 3;
   private static final byte DICT4 = 2;
   private static final byte DONE = 12;
   private static final byte FLAG = 1;
   public static final int MAX_WBITS = 15;
   private static final byte METHOD = 0;
   private static final int PRESET_DICT = 32;
   private static final byte Z_BUF_ERROR = -5;
   private static final byte Z_DATA_ERROR = -3;
   private static final byte Z_DEFLATED = 8;
   private static final byte Z_ERRNO = -1;
   static final byte Z_FINISH = 4;
   static final byte Z_FULL_FLUSH = 3;
   private static final byte Z_MEM_ERROR = -4;
   private static final byte Z_NEED_DICT = 2;
   static final byte Z_NO_FLUSH = 0;
   private static final byte Z_OK = 0;
   static final byte Z_PARTIAL_FLUSH = 1;
   private static final byte Z_STREAM_END = 1;
   private static final byte Z_STREAM_ERROR = -2;
   static final byte Z_SYNC_FLUSH = 2;
   private static final byte Z_VERSION_ERROR = -6;
   private static byte[] mark;
   InfBlocks blocks;
   int marker;
   int method;
   int mode;
   long need;
   int nowrap;
   long[] was = new long[1];
   int wbits;

   static {
      byte[] var0 = new byte[]{0, 0, (byte)-1, (byte)-1};
      mark = var0;
   }

   int inflate(ZStream var1, int var2) {
      if (var1 != null && var1.istate != null && var1.next_in != null) {
         int var3;
         if (var2 == 4) {
            var3 = -5;
         } else {
            var3 = 0;
         }

         var2 = -5;

         while (true) {
            int var4 = var2;
            int var5 = var2;
            int var6 = var2;
            int var7 = var2;
            int var8 = var2;
            int var9 = var2;
            int var10 = var2;
            int var11 = var2;
            int var12 = var2;
            switch (var1.istate.mode) {
               case 0:
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var2 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var34 = var1.istate;
                  byte[] var43 = var1.next_in;
                  var6 = var1.next_in_index++;
                  byte var28 = var43[var6];
                  var34.method = var28;
                  if ((var28 & 15) != 8) {
                     var1.istate.mode = 13;
                     var1.istate.marker = 5;
                     break;
                  } else if ((var1.istate.method >> 4) + 8 > var1.istate.wbits) {
                     var1.istate.mode = 13;
                     var1.istate.marker = 5;
                     break;
                  } else {
                     var1.istate.mode = 1;
                     var4 = var2;
                  }
               case 1:
                  var2 = var4;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var2 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  byte[] var35 = var1.next_in;
                  var6 = var1.next_in_index++;
                  var6 = var35[var6] & 255;
                  if (((var1.istate.method << 8) + var6) % 31 != 0) {
                     var1.istate.mode = 13;
                     var1.istate.marker = 5;
                     break;
                  } else if ((var6 & 32) == 0) {
                     var1.istate.mode = 7;
                     break;
                  } else {
                     var1.istate.mode = 2;
                     var5 = var2;
                  }
               case 2:
                  var2 = var5;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var6 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var44 = var1.istate;
                  byte[] var36 = var1.next_in;
                  var2 = var1.next_in_index++;
                  var44.need = (var36[var2] & 255) << 24 & 4278190080L;
                  var1.istate.mode = 3;
               case 3:
                  var2 = var6;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var7 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var37 = var1.istate;
                  long var50 = var37.need;
                  byte[] var45 = var1.next_in;
                  var2 = var1.next_in_index++;
                  var37.need = var50 + ((var45[var2] & 255) << 16 & 16711680L);
                  var1.istate.mode = 4;
               case 4:
                  var2 = var7;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var38 = var1.istate;
                  long var51 = var38.need;
                  byte[] var46 = var1.next_in;
                  var2 = var1.next_in_index++;
                  var38.need = var51 + ((var46[var2] & 255) << 8 & 65280L);
                  var1.istate.mode = 5;
                  var8 = var3;
               case 5:
                  var2 = var8;
                  if (var1.avail_in != 0) {
                     var1.avail_in--;
                     var1.total_in++;
                     Inflate var47 = var1.istate;
                     long var52 = var47.need;
                     byte[] var39 = var1.next_in;
                     var2 = var1.next_in_index++;
                     var47.need = var52 + (var39[var2] & 255L);
                     var1.adler = var1.istate.need;
                     var1.istate.mode = 6;
                     var2 = 2;
                  }

                  return var2;
               case 6:
                  var1.istate.mode = 13;
                  var1.istate.marker = 0;
                  return -2;
               case 7:
                  var2 = var1.istate.blocks.proc(var1, var2);
                  if (var2 == -3) {
                     var1.istate.mode = 13;
                     var1.istate.marker = 0;
                     break;
                  } else {
                     var6 = var2;
                     if (var2 == 0) {
                        var6 = var3;
                     }

                     var2 = var6;
                     if (var6 != 1) {
                        return var2;
                     }

                     var2 = var3;
                     var1.istate.blocks.reset(var1, var1.istate.was);
                     if (var1.istate.nowrap != 0) {
                        var1.istate.mode = 12;
                        break;
                     } else {
                        var1.istate.mode = 8;
                        var9 = var2;
                     }
                  }
               case 8:
                  var2 = var9;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var10 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var14 = var1.istate;
                  byte[] var13 = var1.next_in;
                  var2 = var1.next_in_index++;
                  var14.need = (var13[var2] & 255) << 24 & 4278190080L;
                  var1.istate.mode = 9;
               case 9:
                  var2 = var10;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var11 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var40 = var1.istate;
                  long var15 = var40.need;
                  byte[] var31 = var1.next_in;
                  var2 = var1.next_in_index++;
                  var40.need = var15 + ((var31[var2] & 255) << 16 & 16711680L);
                  var1.istate.mode = 10;
               case 10:
                  var2 = var11;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var12 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var32 = var1.istate;
                  long var48 = var32.need;
                  byte[] var41 = var1.next_in;
                  var2 = var1.next_in_index++;
                  var32.need = var48 + ((var41[var2] & 255) << 8 & 65280L);
                  var1.istate.mode = 11;
               case 11:
                  var2 = var12;
                  if (var1.avail_in == 0) {
                     return var2;
                  }

                  var2 = var3;
                  var1.avail_in--;
                  var1.total_in++;
                  Inflate var33 = var1.istate;
                  long var49 = var33.need;
                  byte[] var42 = var1.next_in;
                  var6 = var1.next_in_index++;
                  var33.need = var49 + (var42[var6] & 255L);
                  if ((int)var1.istate.was[0] != (int)var1.istate.need) {
                     var1.istate.mode = 13;
                     var1.istate.marker = 5;
                     break;
                  } else {
                     var1.istate.mode = 12;
                  }
               case 12:
                  return 1;
               case 13:
                  return -3;
               default:
                  return -2;
            }
         }
      } else {
         return -2;
      }
   }

   int inflateEnd(ZStream var1) {
      if (this.blocks != null) {
         this.blocks.free(var1);
      }

      this.blocks = null;
      return 0;
   }

   int inflateInit(ZStream var1, int var2) {
      Inflate var3 = null;
      this.blocks = null;
      this.nowrap = 0;
      int var4 = var2;
      if (var2 < 0) {
         var4 = -var2;
         this.nowrap = 1;
      }

      byte var6;
      if (var4 >= 8 && var4 <= 15) {
         this.wbits = var4;
         Inflate var5 = var1.istate;
         if (var1.istate.nowrap == 0) {
            var3 = this;
         }

         var5.blocks = new InfBlocks(var1, var3, 1 << var4);
         this.inflateReset(var1);
         var6 = 0;
      } else {
         this.inflateEnd(var1);
         var6 = -2;
      }

      return var6;
   }

   int inflateReset(ZStream var1) {
      byte var2 = 0;
      int var3;
      if (var1 != null && var1.istate != null) {
         var1.total_out = 0L;
         var1.total_in = 0L;
         Inflate var4 = var1.istate;
         if (var1.istate.nowrap != 0) {
            var3 = 7;
         } else {
            var3 = 0;
         }

         var4.mode = var3;
         var1.istate.blocks.reset(var1, null);
         var3 = var2;
      } else {
         var3 = -2;
      }

      return var3;
   }

   int inflateSetDictionary(ZStream var1, byte[] var2, int var3) {
      byte var4 = 0;
      int var5 = 0;
      int var6 = var3;
      byte var8;
      if (var1 != null && var1.istate != null && var1.istate.mode == 6) {
         if (var1._adler.adler32(1L, var2, 0, var3) != var1.adler) {
            var8 = -3;
         } else {
            var1.adler = var1._adler.adler32(0L, null, 0, 0);
            int var7 = var6;
            if (var6 >= 1 << var1.istate.wbits) {
               var7 = (1 << var1.istate.wbits) - 1;
               var5 = var3 - var7;
            }

            var1.istate.blocks.set_dictionary(var2, var5, var7);
            var1.istate.mode = 7;
            var8 = var4;
         }
      } else {
         var8 = -2;
      }

      return var8;
   }

   int inflateSync(ZStream var1) {
      byte var2 = 0;
      int var3;
      if (var1 != null && var1.istate != null) {
         if (var1.istate.mode != 13) {
            var1.istate.mode = 13;
            var1.istate.marker = 0;
         }

         int var4 = var1.avail_in;
         if (var4 == 0) {
            var3 = -5;
         } else {
            int var5 = var1.next_in_index;

            for (var3 = var1.istate.marker; var4 != 0 && var3 < 4; var4--) {
               if (var1.next_in[var5] == mark[var3]) {
                  var3++;
               } else if (var1.next_in[var5] != 0) {
                  var3 = 0;
               } else {
                  var3 = 4 - var3;
               }

               var5++;
            }

            var1.total_in = var1.total_in + (var5 - var1.next_in_index);
            var1.next_in_index = var5;
            var1.avail_in = var4;
            var1.istate.marker = var3;
            if (var3 != 4) {
               var3 = -3;
            } else {
               long var6 = var1.total_in;
               long var8 = var1.total_out;
               this.inflateReset(var1);
               var1.total_in = var6;
               var1.total_out = var8;
               var1.istate.mode = 7;
               var3 = var2;
            }
         }
      } else {
         var3 = -2;
      }

      return var3;
   }

   int inflateSyncPoint(ZStream var1) {
      int var2;
      if (var1 != null && var1.istate != null && var1.istate.blocks != null) {
         var2 = var1.istate.blocks.sync_point();
      } else {
         var2 = -2;
      }

      return var2;
   }
}
