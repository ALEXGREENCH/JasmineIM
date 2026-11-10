package ru.ivansuper.jasmin.jabber.jzlib;

public final class Deflate {
   private static final int BL_CODES = 19;
   private static final int BUSY_STATE = 113;
   private static final int BlockDone = 1;
   private static final int Buf_size = 16;
   private static final int DEF_MEM_LEVEL = 1;
   private static final int DYN_TREES = 2;
   private static final int D_CODES = 30;
   private static final int END_BLOCK = 256;
   private static final int FAST = 1;
   private static final int FINISH_STATE = 666;
   private static final int FinishDone = 3;
   private static final int FinishStarted = 2;
   private static final int HEAP_SIZE = 1144;
   private static final int INIT_STATE = 42;
   private static final int LENGTH_CODES = 29;
   private static final int LITERALS = 256;
   private static final int L_CODES = 286;
   private static final int MAX_BITS = 15;
   private static final int MAX_MATCH = 258;
   private static final int MAX_MEM_LEVEL = 4;
   public static final int MAX_WBITS = 9;
   private static final int MIN_LOOKAHEAD = 262;
   private static final int MIN_MATCH = 3;
   private static final int NeedMore = 0;
   private static final int PRESET_DICT = 32;
   private static final int REPZ_11_138 = 18;
   private static final int REPZ_3_10 = 17;
   private static final int REP_3_6 = 16;
   private static final int SLOW = 2;
   private static final int STATIC_TREES = 1;
   private static final int STORED = 0;
   private static final int STORED_BLOCK = 0;
   private static final int Z_ASCII = 1;
   private static final int Z_BINARY = 0;
   private static final int Z_BUF_ERROR = -5;
   private static final int Z_DATA_ERROR = -3;
   private static final int Z_DEFAULT_COMPRESSION = -1;
   private static final int Z_DEFAULT_STRATEGY = 0;
   private static final int Z_DEFLATED = 8;
   private static final int Z_ERRNO = -1;
   private static final int Z_FILTERED = 1;
   private static final int Z_FINISH = 4;
   private static final int Z_FULL_FLUSH = 3;
   private static final int Z_HUFFMAN_ONLY = 2;
   private static final int Z_MEM_ERROR = -4;
   private static final int Z_NEED_DICT = 2;
   private static final int Z_NO_FLUSH = 0;
   private static final int Z_OK = 0;
   private static final int Z_PARTIAL_FLUSH = 1;
   private static final int Z_STREAM_END = 1;
   private static final int Z_STREAM_ERROR = -2;
   private static final int Z_SYNC_FLUSH = 2;
   private static final int Z_UNKNOWN = 2;
   private static final int Z_VERSION_ERROR = -6;
   private static final Deflate.Config[] config_table = new Deflate.Config[10];
   private static final String[] z_errmsg = new String[]{
      "need dictionary", "stream end", "", "file error", "stream error", "data error", "insufficient memory", "buffer error", "incompatible version", ""
   };
   short bi_buf;
   int bi_valid;
   short[] bl_count;
   Tree bl_desc;
   short[] bl_tree;
   int block_start;
   int d_buf;
   Tree d_desc;
   byte data_type;
   byte[] depth;
   short[] dyn_dtree;
   short[] dyn_ltree;
   int good_match;
   int hash_bits;
   int hash_mask;
   int hash_shift;
   int hash_size;
   short[] head;
   int[] heap;
   int heap_len;
   int heap_max;
   int ins_h;
   int l_buf;
   Tree l_desc = new Tree();
   int last_eob_len;
   int last_flush;
   int last_lit;
   int level;
   int lit_bufsize;
   int lookahead;
   int match_available;
   int match_length;
   int match_start;
   int matches;
   int max_chain_length;
   int max_lazy_match;
   byte method;
   int nice_match;
   int noheader;
   int opt_len;
   int pending;
   byte[] pending_buf;
   int pending_buf_size;
   int pending_out;
   short[] prev;
   int prev_length;
   int prev_match;
   int static_len;
   int status;
   int strategy;
   ZStream strm;
   int strstart;
   int w_bits;
   int w_mask;
   int w_size;
   byte[] window;
   int window_size;

   static {
      config_table[0] = new Deflate.Config(0, 0, 0, 0, 0);
      config_table[1] = new Deflate.Config(4, 4, 8, 4, 1);
      config_table[2] = new Deflate.Config(4, 5, 16, 8, 1);
      config_table[3] = new Deflate.Config(4, 6, 32, 32, 1);
      config_table[4] = new Deflate.Config(4, 4, 16, 16, 2);
      config_table[5] = new Deflate.Config(8, 16, 32, 32, 2);
      config_table[6] = new Deflate.Config(8, 16, 128, 128, 2);
      config_table[7] = new Deflate.Config(8, 32, 128, 256, 2);
      config_table[8] = new Deflate.Config(32, 128, 258, 1024, 2);
      config_table[9] = new Deflate.Config(32, 258, 258, 4096, 2);
   }

   Deflate() {
      this.d_desc = new Tree();
      this.bl_desc = new Tree();
      this.bl_count = new short[16];
      this.heap = new int[1144];
      this.depth = new byte[1144];
      this.dyn_ltree = new short[2288];
      this.dyn_dtree = new short[240];
      this.bl_tree = new short[152];
   }

   static boolean smaller(short[] var0, int var1, int var2, byte[] var3) {
      short var4 = var0[var1 << 1];
      short var5 = var0[var2 << 1];
      boolean var6;
      if (var4 < var5 || var4 == var5 && var3[var1] <= var3[var2]) {
         var6 = true;
      } else {
         var6 = false;
      }

      return var6;
   }

   void _tr_align() {
      this.send_bits(2, 3);
      this.send_code(256, StaticTree.getStatic_ltree());
      this.bi_flush();
      if (this.last_eob_len + 1 + 10 - this.bi_valid < 9) {
         this.send_bits(2, 3);
         this.send_code(256, StaticTree.getStatic_ltree());
         this.bi_flush();
      }

      this.last_eob_len = 7;
   }

   void _tr_flush_block(int var1, int var2, boolean var3) {
      byte var4 = 1;
      byte var5 = 1;
      int var6 = 0;
      int var10;
      int var11;
      if (this.level > 0) {
         if (this.data_type == 2) {
            this.set_data_type();
         }

         this.l_desc.build_tree(this);
         this.d_desc.build_tree(this);
         int var7 = this.build_bl_tree();
         int var8 = this.opt_len + 3 + 7 >>> 3;
         int var9 = this.static_len + 3 + 7 >>> 3;
         var6 = var7;
         var10 = var8;
         var11 = var9;
         if (var9 <= var8) {
            var10 = var9;
            var11 = var9;
            var6 = var7;
         }
      } else {
         var11 = var2 + 5;
         var10 = var11;
      }

      if (var2 + 4 <= var10 && var1 != -1) {
         this._tr_stored_block(var1, var2, var3);
      } else if (var11 == var10) {
         byte var12;
         if (var3) {
            var12 = var5;
         } else {
            var12 = 0;
         }

         this.send_bits(var12 + 2, 3);
         this.compress_block(StaticTree.getStatic_ltree(), StaticTree.static_dtree);
      } else {
         byte var13;
         if (var3) {
            var13 = var4;
         } else {
            var13 = 0;
         }

         this.send_bits(var13 + 4, 3);
         this.send_all_trees(this.l_desc.max_code + 1, this.d_desc.max_code + 1, var6 + 1);
         this.compress_block(this.dyn_ltree, this.dyn_dtree);
      }

      this.init_block();
      if (var3) {
         this.bi_windup();
      }
   }

   void _tr_stored_block(int var1, int var2, boolean var3) {
      byte var4;
      if (var3) {
         var4 = 1;
      } else {
         var4 = 0;
      }

      this.send_bits(var4 + 0, 3);
      this.copy_block(var1, var2, true);
   }

   boolean _tr_tally(int var1, int var2) {
      boolean var3 = true;
      this.pending_buf[this.d_buf + (this.last_lit << 1)] = (byte)((byte)(var1 >>> 8));
      this.pending_buf[this.d_buf + (this.last_lit << 1) + 1] = (byte)((byte)var1);
      this.pending_buf[this.l_buf + this.last_lit] = (byte)((byte)var2);
      this.last_lit++;
      if (var1 == 0) {
         short[] var4 = this.dyn_ltree;
         var1 = var2 << 1;
         var4[var1] = (short)((short)(var4[var1] + 1));
      } else {
         this.matches++;
         short[] var12 = this.dyn_ltree;
         var2 = (Tree.getLength_code(var2) + 256 + 1) * 2;
         var12[var2] = (short)((short)(var12[var2] + 1));
         var12 = this.dyn_dtree;
         var1 = Tree.d_code(var1 - 1) * 2;
         var12[var1] = (short)((short)(var12[var1] + 1));
      }

      if ((this.last_lit & 8191) == 0 && this.level > 2) {
         var2 = this.last_lit << 3;
         int var5 = this.strstart;
         int var6 = this.block_start;

         for (int var9 = 0; var9 < 30; var9++) {
            var2 = (int)(var2 + this.dyn_dtree[var9 << 1] * (5L + Tree.extra_dbits[var9]));
         }

         if (this.matches < this.last_lit / 2 && var2 >>> 3 < (var5 - var6) / 2) {
            return var3;
         }
      }

      if (this.last_lit != this.lit_bufsize - 1) {
         var3 = false;
      }

      return var3;
   }

   void bi_flush() {
      if (this.bi_valid == 16) {
         this.put_short(this.bi_buf);
         this.bi_buf = (short)0;
         this.bi_valid = 0;
      } else if (this.bi_valid >= 8) {
         this.put_byte((byte)this.bi_buf);
         this.bi_buf = (short)((short)(this.bi_buf >>> 8));
         this.bi_valid -= 8;
      }
   }

   void bi_windup() {
      if (this.bi_valid > 8) {
         this.put_short(this.bi_buf);
      } else if (this.bi_valid > 0) {
         this.put_byte((byte)this.bi_buf);
      }

      this.bi_buf = (short)0;
      this.bi_valid = 0;
   }

   int build_bl_tree() {
      this.scan_tree(this.dyn_ltree, this.l_desc.max_code);
      this.scan_tree(this.dyn_dtree, this.d_desc.max_code);
      this.bl_desc.build_tree(this);
      int var1 = 18;

      while (var1 >= 3 && this.bl_tree[Tree.bl_order[var1] * 2 + 1] == 0) {
         var1--;
      }

      this.opt_len += (var1 + 1) * 3 + 5 + 5 + 4;
      return var1;
   }

   void compress_block(short[] var1, short[] var2) {
      int var3 = 0;
      int var6;
      if (this.last_lit != 0) {
         do {
            int var4 = this.pending_buf[this.d_buf + var3 * 2] << 8 & 0xFF00 | this.pending_buf[this.d_buf + var3 * 2 + 1] & 255;
            int var5 = this.pending_buf[this.l_buf + var3] & 255;
            var6 = var3 + 1;
            if (var4 == 0) {
               this.send_code(var5, var1);
            } else {
               byte var7 = Tree.getLength_code(var5);
               this.send_code(var7 + 256 + 1, var1);
               var3 = Tree.extra_lbits[var7];
               if (var3 != 0) {
                  this.send_bits(var5 - Tree.base_length[var7], var3);
               }

               var3 = var4 - 1;
               var4 = Tree.d_code(var3);
               this.send_code(var4, var2);
               var5 = Tree.extra_dbits[var4];
               if (var5 != 0) {
                  this.send_bits(var3 - Tree.base_dist[var4], var5);
               }
            }

            var3 = var6;
         } while (var6 < this.last_lit);
      }

      this.send_code(256, var1);
      this.last_eob_len = var1[513];
   }

   void copy_block(int var1, int var2, boolean var3) {
      this.bi_windup();
      this.last_eob_len = 8;
      if (var3) {
         this.put_short((short)var2);
         this.put_short((short)(~var2));
      }

      this.put_byte(this.window, var1, var2);
   }

   int deflate(ZStream var1, int var2) {
      int var7;
      if (var2 <= 4 && var2 >= 0) {
         if (var1.next_out != null && (var1.next_in != null || var1.avail_in == 0) && (this.status != 666 || var2 == 4)) {
            if (var1.avail_out == 0) {
               var7 = -5;
            } else {
               this.strm = var1;
               int var3 = this.last_flush;
               this.last_flush = var2;
               if (this.status == 42) {
                  int var4 = this.w_bits;
                  int var5 = (this.level - 1 & 0xFF) >> 1;
                  int var6 = var5;
                  if (var5 > 3) {
                     var6 = 3;
                  }

                  var5 = (var4 - 8 << 4) + 8 << 8 | var6 << 6;
                  var6 = var5;
                  if (this.strstart != 0) {
                     var6 = var5 | 32;
                  }

                  this.status = 113;
                  this.putShortMSB(var6 + (31 - var6 % 31));
                  if (this.strstart != 0) {
                     this.putShortMSB((int)(var1.adler >>> 16));
                     this.putShortMSB((int)(var1.adler & 65535L));
                  }

                  var1.adler = var1._adler.adler32(0L, null, 0, 0);
               }

               if (this.pending != 0) {
                  var1.flush_pending();
                  if (var1.avail_out == 0) {
                     this.last_flush = -1;
                     var7 = 0;
                     return var7;
                  }
               } else if (var1.avail_in == 0 && var2 <= var3 && var2 != 4) {
                  var7 = -5;
                  return var7;
               }

               if (this.status == 666 && var1.avail_in != 0) {
                  var7 = -5;
               } else {
                  if (var1.avail_in != 0 || this.lookahead != 0 || var2 != 0 && this.status != 666) {
                     int var10 = -1;
                     switch (config_table[this.level].func) {
                        case 0:
                           var10 = this.deflate_stored(var2);
                           break;
                        case 1:
                           var10 = this.deflate_fast(var2);
                           break;
                        case 2:
                           var10 = this.deflate_slow(var2);
                     }

                     if (var10 == 2 || var10 == 3) {
                        this.status = 666;
                     }

                     if (var10 == 0 || var10 == 2) {
                        if (var1.avail_out == 0) {
                           this.last_flush = -1;
                        }

                        var7 = 0;
                        return var7;
                     }

                     if (var10 == 1) {
                        if (var2 == 1) {
                           this._tr_align();
                        } else {
                           this._tr_stored_block(0, 0, false);
                           if (var2 == 3) {
                              for (int var11 = 0; var11 < this.hash_size; var11++) {
                                 this.head[var11] = (short)0;
                              }
                           }
                        }

                        var1.flush_pending();
                        if (var1.avail_out == 0) {
                           this.last_flush = -1;
                           var7 = 0;
                           return var7;
                        }
                     }
                  }

                  if (var2 != 4) {
                     var7 = 0;
                  } else if (this.noheader != 0) {
                     var7 = 1;
                  } else {
                     this.putShortMSB((int)(var1.adler >>> 16));
                     this.putShortMSB((int)(var1.adler & 65535L));
                     var1.flush_pending();
                     this.noheader = -1;
                     if (this.pending != 0) {
                        var7 = 0;
                     } else {
                        var7 = 1;
                     }
                  }
               }
            }
         } else {
            var7 = -2;
         }
      } else {
         var7 = -2;
      }

      return var7;
   }

   int deflateEnd() {
      byte var1;
      if (this.status != 42 && this.status != 113 && this.status != 666) {
         var1 = -2;
      } else {
         this.pending_buf = null;
         this.head = null;
         this.prev = null;
         this.window = null;
         if (this.status == 113) {
            var1 = -3;
         } else {
            var1 = 0;
         }
      }

      return var1;
   }

   int deflateInit(ZStream var1, int var2) {
      return this.deflateInit(var1, var2, 9);
   }

   int deflateInit(ZStream var1, int var2, int var3) {
      return this.deflateInit2(var1, var2, 8, var3, 1, 0);
   }

   int deflateInit2(ZStream var1, int var2, int var3, int var4, int var5, int var6) {
      byte var7 = 0;
      int var8 = var2;
      if (var2 == -1) {
         var8 = 6;
      }

      var2 = var4;
      if (var4 < 0) {
         var7 = 1;
         var2 = -var4;
      }

      if (var5 >= 1 && var5 <= 4 && var3 == 8 && var2 >= 9 && var2 <= 15 && var8 >= 0 && var8 <= 9 && var6 >= 0 && var6 <= 2) {
         var1.dstate = this;
         this.noheader = var7;
         this.w_bits = var2;
         this.w_size = 1 << this.w_bits;
         this.w_mask = this.w_size - 1;
         this.hash_bits = var5 + 7;
         this.hash_size = 1 << this.hash_bits;
         this.hash_mask = this.hash_size - 1;
         this.hash_shift = (this.hash_bits + 3 - 1) / 3;
         this.window = new byte[this.w_size << 1];
         this.prev = new short[this.w_size];
         this.head = new short[this.hash_size];
         this.lit_bufsize = 1 << var5 + 6;
         this.pending_buf = new byte[this.lit_bufsize << 2];
         this.pending_buf_size = this.lit_bufsize << 2;
         this.d_buf = this.lit_bufsize / 2;
         this.l_buf = this.lit_bufsize * 3;
         this.level = var8;
         this.strategy = var6;
         this.method = (byte)((byte)var3);
         var2 = this.deflateReset(var1);
      } else {
         var2 = -2;
      }

      return var2;
   }

   int deflateParams(ZStream var1, int var2, int var3) {
      byte var4 = 0;
      int var5 = var2;
      if (var2 == -1) {
         var5 = 6;
      }

      if (var5 >= 0 && var5 <= 9 && var3 >= 0 && var3 <= 2) {
         var2 = var4;
         if (config_table[this.level].func != config_table[var5].func) {
            var2 = var4;
            if (var1.total_in != 0L) {
               var2 = var1.deflate(1);
            }
         }

         if (this.level != var5) {
            this.level = var5;
            this.max_lazy_match = config_table[this.level].max_lazy;
            this.good_match = config_table[this.level].good_length;
            this.nice_match = config_table[this.level].nice_length;
            this.max_chain_length = config_table[this.level].max_chain;
         }

         this.strategy = var3;
      } else {
         var2 = -2;
      }

      return var2;
   }

   int deflateReset(ZStream var1) {
      var1.total_out = 0L;
      var1.total_in = 0L;
      var1.data_type = 2;
      this.pending = 0;
      this.pending_out = 0;
      if (this.noheader < 0) {
         this.noheader = 0;
      }

      byte var2;
      if (this.noheader != 0) {
         var2 = 113;
      } else {
         var2 = 42;
      }

      this.status = var2;
      var1.adler = var1._adler.adler32(0L, null, 0, 0);
      this.last_flush = 0;
      this.tr_init();
      this.lm_init();
      return 0;
   }

   int deflateSetDictionary(ZStream var1, byte[] var2, int var3) {
      byte var4 = 0;
      int var5 = var3;
      int var6 = 0;
      int var7;
      if (var2 != null && this.status == 42) {
         var1.adler = var1._adler.adler32(var1.adler, var2, 0, var3);
         var7 = var4;
         if (var5 >= 3) {
            var7 = var6;
            var6 = var5;
            if (var5 > this.w_size - 262) {
               var6 = this.w_size - 262;
               var7 = var3 - var6;
            }

            System.arraycopy(var2, var7, this.window, 0, var6);
            this.strstart = var6;
            this.block_start = var6;
            this.ins_h = this.window[0] & 255;
            this.ins_h = (this.ins_h << this.hash_shift ^ this.window[1] & 255) & this.hash_mask;
            var3 = 0;

            while (true) {
               var7 = var4;
               if (var3 > var6 - 3) {
                  break;
               }

               this.ins_h = (this.ins_h << this.hash_shift ^ this.window[var3 + 2] & 255) & this.hash_mask;
               this.prev[this.w_mask & var3] = (short)this.head[this.ins_h];
               this.head[this.ins_h] = (short)((short)var3);
               var3++;
            }
         }
      } else {
         var7 = -2;
      }

      return var7;
   }

   int deflate_fast(int var1) {
      int var8;
      byte var2 = 1;
      byte var3 = 0;
      int var4 = 0;

      while (true) {
         if (this.lookahead < 262) {
            this.fill_window();
            if (this.lookahead < 262 && var1 == 0) {
               var8 = var3;
               break;
            }

            if (this.lookahead == 0) {
               boolean var9;
               if (var1 == 4) {
                  var9 = true;
               } else {
                  var9 = false;
               }

               this.flush_block_only(var9);
               if (this.strm.avail_out == 0) {
                  var8 = var3;
                  if (var1 == 4) {
                     var8 = 2;
                  }
               } else {
                  var8 = var2;
                  if (var1 == 4) {
                     var8 = 3;
                  }
               }
               break;
            }
         }

         if (this.lookahead >= 3) {
            this.ins_h = (this.ins_h << this.hash_shift ^ this.window[this.strstart + 2] & 255) & this.hash_mask;
            var4 = this.head[this.ins_h] & '\uffff';
            this.prev[this.strstart & this.w_mask] = (short)this.head[this.ins_h];
            this.head[this.ins_h] = (short)((short)this.strstart);
         }

         if (var4 != 0L && (this.strstart - var4 & 65535) <= this.w_size - 262 && this.strategy != 2) {
            this.match_length = this.longest_match(var4);
         }

         boolean var5;
         int var6;
         if (this.match_length >= 3) {
            var5 = this._tr_tally(this.strstart - this.match_start, this.match_length - 3);
            this.lookahead = this.lookahead - this.match_length;
            if (this.match_length <= this.max_lazy_match && this.lookahead >= 3) {
               this.match_length--;

               do {
                  this.strstart++;
                  this.ins_h = (this.ins_h << this.hash_shift ^ this.window[this.strstart + 2] & 255) & this.hash_mask;
                  var6 = this.head[this.ins_h] & '\uffff';
                  this.prev[this.strstart & this.w_mask] = (short)this.head[this.ins_h];
                  this.head[this.ins_h] = (short)((short)this.strstart);
                  var4 = this.match_length - 1;
                  this.match_length = var4;
               } while (var4 != 0);

               this.strstart++;
            } else {
               this.strstart = this.strstart + this.match_length;
               this.match_length = 0;
               this.ins_h = this.window[this.strstart] & 255;
               this.ins_h = (this.ins_h << this.hash_shift ^ this.window[this.strstart + 1] & 255) & this.hash_mask;
               var6 = var4;
            }
         } else {
            var5 = this._tr_tally(0, this.window[this.strstart] & 255);
            this.lookahead--;
            this.strstart++;
            var6 = var4;
         }

         var4 = var6;
         if (var5) {
            this.flush_block_only(false);
            var4 = var6;
            if (this.strm.avail_out == 0) {
               var8 = var3;
               break;
            }
         }
      }

      return var8;
   }

   int deflate_slow(int var1) {
      byte var2 = 1;
      byte var3 = 0;
      int var4 = 0;

      int var11;
      while (true) {
         if (this.lookahead < 262) {
            this.fill_window();
            if (this.lookahead < 262 && var1 == 0) {
               var11 = var3;
               break;
            }

            if (this.lookahead == 0) {
               if (this.match_available != 0) {
                  this._tr_tally(0, this.window[this.strstart - 1] & 255);
                  this.match_available = 0;
               }

               boolean var13;
               if (var1 == 4) {
                  var13 = true;
               } else {
                  var13 = false;
               }

               this.flush_block_only(var13);
               if (this.strm.avail_out == 0) {
                  var11 = var3;
                  if (var1 == 4) {
                     var11 = 2;
                  }
               } else {
                  var11 = var2;
                  if (var1 == 4) {
                     var11 = 3;
                  }
               }
               break;
            }
         }

         var11 = var4;
         if (this.lookahead >= 3) {
            this.ins_h = (this.ins_h << this.hash_shift ^ this.window[this.strstart + 2] & 255) & this.hash_mask;
            var11 = this.head[this.ins_h] & '\uffff';
            this.prev[this.strstart & this.w_mask] = (short)this.head[this.ins_h];
            this.head[this.ins_h] = (short)((short)this.strstart);
         }

         this.prev_length = this.match_length;
         this.prev_match = this.match_start;
         this.match_length = 2;
         if (var11 != 0 && this.prev_length < this.max_lazy_match && (this.strstart - var11 & 65535) <= this.w_size - 262) {
            if (this.strategy != 2) {
               this.match_length = this.longest_match(var11);
            }

            if (this.match_length <= 5 && (this.strategy == 1 || this.match_length == 3 && this.strstart - this.match_start > 4096)) {
               this.match_length = 2;
            }
         }

         if (this.prev_length >= 3 && this.match_length <= this.prev_length) {
            int var7 = this.strstart;
            int var8 = this.lookahead;
            boolean var6 = this._tr_tally(this.strstart - 1 - this.prev_match, this.prev_length - 3);
            this.lookahead = this.lookahead - (this.prev_length - 1);
            this.prev_length -= 2;
            var4 = var11;

            int var14;
            do {
               var14 = this.strstart + 1;
               this.strstart = var14;
               var11 = var4;
               if (var14 <= var7 + var8 - 3) {
                  this.ins_h = (this.ins_h << this.hash_shift ^ this.window[this.strstart + 2] & 255) & this.hash_mask;
                  var11 = this.head[this.ins_h] & '\uffff';
                  this.prev[this.strstart & this.w_mask] = (short)this.head[this.ins_h];
                  this.head[this.ins_h] = (short)((short)this.strstart);
               }

               var14 = this.prev_length - 1;
               this.prev_length = var14;
               var4 = var11;
            } while (var14 != 0);

            this.match_available = 0;
            this.match_length = 2;
            this.strstart++;
            var4 = var11;
            if (var6) {
               this.flush_block_only(false);
               var4 = var11;
               if (this.strm.avail_out == 0) {
                  var11 = var3;
                  break;
               }
            }
         } else if (this.match_available != 0) {
            if (this._tr_tally(0, this.window[this.strstart - 1] & 255)) {
               this.flush_block_only(false);
            }

            this.strstart++;
            this.lookahead--;
            var4 = var11;
            if (this.strm.avail_out == 0) {
               var11 = var3;
               break;
            }
         } else {
            this.match_available = 1;
            this.strstart++;
            this.lookahead--;
            var4 = var11;
         }
      }

      return var11;
   }

   int deflate_stored(int var1) {
      byte var2 = 1;
      byte var3 = 0;
      int var4 = 65535;
      if (65535 > this.pending_buf_size - 5) {
         var4 = this.pending_buf_size - 5;
      }

      int var7;
      while (true) {
         if (this.lookahead <= 1) {
            this.fill_window();
            if (this.lookahead == 0 && var1 == 0) {
               var7 = var3;
               break;
            }

            if (this.lookahead == 0) {
               boolean var6;
               if (var1 == 4) {
                  var6 = true;
               } else {
                  var6 = false;
               }

               this.flush_block_only(var6);
               if (this.strm.avail_out == 0) {
                  var7 = var3;
                  if (var1 == 4) {
                     var7 = 2;
                  }
               } else {
                  var7 = var2;
                  if (var1 == 4) {
                     var7 = 3;
                  }
               }
               break;
            }
         }

         this.strstart = this.strstart + this.lookahead;
         this.lookahead = 0;
         var7 = this.block_start + var4;
         if (this.strstart == 0 || this.strstart >= var7) {
            this.lookahead = this.strstart - var7;
            this.strstart = var7;
            this.flush_block_only(false);
            var7 = var3;
            if (this.strm.avail_out == 0) {
               break;
            }
         }

         if (this.strstart - this.block_start >= this.w_size - 262) {
            this.flush_block_only(false);
            if (this.strm.avail_out == 0) {
               var7 = var3;
               break;
            }
         }
      }

      return var7;
   }

   void fill_window() {
      while (true) {
         int var1 = this.window_size - this.lookahead - this.strstart;
         int var2;
         if (var1 == 0 && this.strstart == 0 && this.lookahead == 0) {
            var2 = this.w_size;
         } else if (var1 == -1) {
            var2 = var1 - 1;
         } else {
            var2 = var1;
            if (this.strstart >= this.w_size + this.w_size - 262) {
               System.arraycopy(this.window, this.w_size, this.window, 0, this.w_size);
               this.match_start = this.match_start - this.w_size;
               this.strstart = this.strstart - this.w_size;
               this.block_start = this.block_start - this.w_size;
               var2 = this.hash_size;
               int var3 = var2;

               int var6;
               do {
                  short[] var4 = this.head;
                  int var5 = var3 - 1;
                  var3 = var4[var5] & '\uffff';
                  var4 = this.head;
                  short var11;
                  if (var3 >= this.w_size) {
                     var11 = (short)(var3 - this.w_size);
                  } else {
                     var11 = 0;
                  }

                  var4[var5] = (short)var11;
                  var6 = var2 - 1;
                  var2 = var6;
                  var3 = var5;
               } while (var6 != 0);

               var2 = this.w_size;
               var3 = var2;

               do {
                  short[] var16 = this.prev;
                  int var18 = var3 - 1;
                  var3 = var16[var18] & '\uffff';
                  var16 = this.prev;
                  short var14;
                  if (var3 >= this.w_size) {
                     var14 = (short)(var3 - this.w_size);
                  } else {
                     var14 = 0;
                  }

                  var16[var18] = (short)var14;
                  var6 = var2 - 1;
                  var2 = var6;
                  var3 = var18;
               } while (var6 != 0);

               var2 = var1 + this.w_size;
            }
         }

         if (this.strm.avail_in != 0) {
            var2 = this.strm.read_buf(this.window, this.strstart + this.lookahead, var2);
            this.lookahead += var2;
            if (this.lookahead >= 3) {
               this.ins_h = this.window[this.strstart] & 255;
               this.ins_h = (this.ins_h << this.hash_shift ^ this.window[this.strstart + 1] & 255) & this.hash_mask;
            }

            if (this.lookahead < 262 && this.strm.avail_in != 0) {
               continue;
            }
         }

         return;
      }
   }

   void flush_block_only(boolean var1) {
      int var2;
      if (this.block_start >= 0) {
         var2 = this.block_start;
      } else {
         var2 = -1;
      }

      this._tr_flush_block(var2, this.strstart - this.block_start, var1);
      this.block_start = this.strstart;
      this.strm.flush_pending();
   }

   void init_block() {
      for (int var1 = 0; var1 < 286; var1++) {
         this.dyn_ltree[var1 << 1] = (short)0;
      }

      for (int var2 = 0; var2 < 30; var2++) {
         this.dyn_dtree[var2 << 1] = (short)0;
      }

      for (int var3 = 0; var3 < 19; var3++) {
         this.bl_tree[var3 << 1] = (short)0;
      }

      this.dyn_ltree[512] = (short)1;
      this.static_len = 0;
      this.opt_len = 0;
      this.matches = 0;
      this.last_lit = 0;
   }

   void lm_init() {
      this.window_size = this.w_size << 1;
      this.head[this.hash_size - 1] = (short)0;

      for (int var1 = 0; var1 < this.hash_size - 1; var1++) {
         this.head[var1] = (short)0;
      }

      this.max_lazy_match = config_table[this.level].max_lazy;
      this.good_match = config_table[this.level].good_length;
      this.nice_match = config_table[this.level].nice_length;
      this.max_chain_length = config_table[this.level].max_chain;
      this.strstart = 0;
      this.block_start = 0;
      this.lookahead = 0;
      this.prev_length = 2;
      this.match_length = 2;
      this.match_available = 0;
      this.ins_h = 0;
   }

   int longest_match(int var1) {
      int var2 = this.max_chain_length;
      int var3 = this.strstart;
      int var4 = this.prev_length;
      int var5;
      if (this.strstart > this.w_size - 262) {
         var5 = this.strstart - (this.w_size - 262);
      } else {
         var5 = 0;
      }

      int var6 = this.nice_match;
      int var7 = this.w_mask;
      int var8 = this.strstart + 258;
      byte var9 = this.window[var3 + var4 - 1];
      int var10 = this.window[var3 + var4];
      int var11 = var2;
      if (this.prev_length >= this.good_match) {
         var11 = var2 >> 2;
      }

      int var12 = var4;
      var2 = var11;
      int var13 = var6;
      int var14 = var3;
      byte var15 = (byte)var10;
      byte var16 = var9;
      int var17 = var1;
      if (var6 > this.lookahead) {
         var13 = this.lookahead;
         var17 = var1;
         var16 = var9;
         var15 = (byte)var10;
         var14 = var3;
         var2 = var11;
         var12 = var4;
      }

      while (true) {
         var1 = var12;
         var4 = var14;
         byte var24 = var15;
         var9 = var16;
         if (this.window[var17 + var12] == var15) {
            var1 = var12;
            var4 = var14;
            var24 = var15;
            var9 = var16;
            if (this.window[var17 + var12 - 1] == var16) {
               var1 = var12;
               var4 = var14;
               var24 = var15;
               var9 = var16;
               if (this.window[var17] == this.window[var14]) {
                  byte[] var18 = this.window;
                  var1 = var17 + 1;
                  if (var18[var1] != this.window[var14 + 1]) {
                     var9 = var16;
                     var24 = var15;
                     var4 = var14;
                     var1 = var12;
                  } else {
                     var14 += 2;
                     var1++;

                     while (true) {
                        var18 = this.window;
                        var11 = var14 + 1;
                        byte var26 = var18[var11];
                        var18 = this.window;
                        var14 = var1 + 1;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var18 = this.window;
                        var26 = var18[++var11];
                        var18 = this.window;
                        var14++;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var18 = this.window;
                        var26 = var18[++var11];
                        var18 = this.window;
                        var14++;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var18 = this.window;
                        var26 = var18[++var11];
                        var18 = this.window;
                        var14++;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var18 = this.window;
                        var26 = var18[++var11];
                        var18 = this.window;
                        var14++;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var18 = this.window;
                        var26 = var18[++var11];
                        var18 = this.window;
                        var14++;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var18 = this.window;
                        var26 = var18[++var11];
                        var18 = this.window;
                        var14++;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var18 = this.window;
                        var26 = var18[++var11];
                        var18 = this.window;
                        var14++;
                        var1 = var11;
                        if (var26 != var18[var14]) {
                           break;
                        }

                        var1 = var14;
                        var14 = var11;
                        if (var11 >= var8) {
                           var1 = var11;
                           break;
                        }
                     }

                     var10 = 258 - (var8 - var1);
                     var14 = var8 - 258;
                     var1 = var12;
                     var4 = var14;
                     var24 = var15;
                     var9 = var16;
                     if (var10 > var12) {
                        this.match_start = var17;
                        var1 = var10;
                        var11 = var1;
                        if (var10 >= var13) {
                           break;
                        }

                        var9 = this.window[var14 + var1 - 1];
                        var24 = this.window[var14 + var1];
                        var4 = var14;
                     }
                  }
               }
            }
         }

         var17 = this.prev[var17 & var7] & '\uffff';
         var11 = var1;
         if (var17 <= var5) {
            break;
         }

         var11 = var2 - 1;
         var12 = var1;
         var2 = var11;
         var14 = var4;
         var15 = var24;
         var16 = var9;
         if (var11 == 0) {
            var11 = var1;
            break;
         }
      }

      if (var11 > this.lookahead) {
         var11 = this.lookahead;
      }

      return var11;
   }

   void pqdownheap(short[] var1, int var2) {
      int var3 = this.heap[var2];
      int var4 = var2 << 1;
      int var5 = var2;

      while (var4 <= this.heap_len) {
         var2 = var4;
         if (var4 < this.heap_len) {
            var2 = var4;
            if (smaller(var1, this.heap[var4 + 1], this.heap[var4], this.depth)) {
               var2 = var4 + 1;
            }
         }

         if (smaller(var1, var3, this.heap[var2], this.depth)) {
            break;
         }

         this.heap[var5] = this.heap[var2];
         var5 = var2;
         var4 = var2 << 1;
      }

      this.heap[var5] = var3;
   }

   final void putShortMSB(int var1) {
      this.put_byte((byte)(var1 >> 8));
      this.put_byte((byte)var1);
   }

   final void put_byte(byte var1) {
      byte[] var2 = this.pending_buf;
      int var3 = this.pending++;
      var2[var3] = (byte)var1;
   }

   final void put_byte(byte[] var1, int var2, int var3) {
      System.arraycopy(var1, var2, this.pending_buf, this.pending, var3);
      this.pending += var3;
   }

   final void put_short(int var1) {
      this.put_byte((byte)var1);
      this.put_byte((byte)(var1 >>> 8));
   }

   void scan_tree(short[] var1, int var2) {
      short var3 = -1;
      short var4 = var1[1];
      int var5 = 0;
      int var6 = 7;
      int var7 = 4;
      if (var4 == 0) {
         var6 = 138;
         var7 = 3;
      }

      var1[(var2 + 1) * 2 + 1] = (short)-1;
      int var8 = 0;

      while (var8 <= var2) {
         short var9 = var1[(var8 + 1) * 2 + 1];
         byte var11;
         if (++var5 < var6 && var4 == var9) {
            var11 = (byte)var7;
            var7 = var5;
         } else {
            if (var5 < var7) {
               short[] var10 = this.bl_tree;
               var6 = var4 << 1;
               var10[var6] = (short)((short)(var10[var6] + var5));
            } else if (var4 != 0) {
               if (var4 != var3) {
                  short[] var16 = this.bl_tree;
                  var6 = var4 << 1;
                  var16[var6] = (short)((short)(var16[var6] + 1));
               }

               short[] var17 = this.bl_tree;
               var17[32] = (short)((short)(var17[32] + 1));
            } else if (var5 <= 10) {
               short[] var18 = this.bl_tree;
               var18[34] = (short)((short)(var18[34] + 1));
            } else {
               short[] var19 = this.bl_tree;
               var19[36] = (short)((short)(var19[36] + 1));
            }

            var7 = 0;
            var3 = var4;
            if (var9 == 0) {
               var6 = 138;
               var11 = 3;
            } else if (var4 == var9) {
               var6 = 6;
               var11 = 3;
            } else {
               var6 = 7;
               var11 = 4;
            }
         }

         var8++;
         var5 = var7;
         var7 = var11;
         var4 = var9;
      }
   }

   void send_all_trees(int var1, int var2, int var3) {
      this.send_bits(var1 - 257, 5);
      this.send_bits(var2 - 1, 5);
      this.send_bits(var3 - 4, 4);

      for (int var4 = 0; var4 < var3; var4++) {
         this.send_bits(this.bl_tree[Tree.bl_order[var4] * 2 + 1], 3);
      }

      this.send_tree(this.dyn_ltree, var1 - 1);
      this.send_tree(this.dyn_dtree, var2 - 1);
   }

   void send_bits(int var1, int var2) {
      if (this.bi_valid > 16 - var2) {
         this.bi_buf = (short)((short)(this.bi_buf | var1 << this.bi_valid & 65535));
         this.put_short(this.bi_buf);
         this.bi_buf = (short)((short)(var1 >>> 16 - this.bi_valid));
         this.bi_valid += var2 - 16;
      } else {
         this.bi_buf = (short)((short)(this.bi_buf | var1 << this.bi_valid & 65535));
         this.bi_valid += var2;
      }
   }

   final void send_code(int var1, short[] var2) {
      var1 <<= 1;
      this.send_bits(var2[var1] & 65535, var2[var1 + 1] & 65535);
   }

   void send_tree(short[] var1, int var2) {
      short var3 = -1;
      short var4 = var1[1];
      short var5 = 0;
      int var6 = 7;
      int var7 = 4;
      if (var4 == 0) {
         var6 = 138;
         var7 = 3;
      }

      int var8 = 0;
      byte var9 = (byte)var7;
      var7 = var5;

      while (var8 <= var2) {
         var5 = var1[(var8 + 1) * 2 + 1];
         byte var10;
         if (++var7 < var6 && var4 == var5) {
            var10 = var9;
         } else {
            if (var7 < var9) {
               do {
                  this.send_code(var4, this.bl_tree);
                  var6 = var7 - 1;
                  var7 = var6;
               } while (var6 != 0);
            } else if (var4 != 0) {
               var6 = var7;
               if (var4 != var3) {
                  this.send_code(var4, this.bl_tree);
                  var6 = var7 - 1;
               }

               this.send_code(16, this.bl_tree);
               this.send_bits(var6 - 3, 2);
            } else if (var7 <= 10) {
               this.send_code(17, this.bl_tree);
               this.send_bits(var7 - 3, 3);
            } else {
               this.send_code(18, this.bl_tree);
               this.send_bits(var7 - 11, 7);
            }

            var7 = 0;
            var3 = var4;
            if (var5 == 0) {
               var6 = 138;
               var10 = 3;
            } else if (var4 == var5) {
               var6 = 6;
               var10 = 3;
            } else {
               var6 = 7;
               var10 = 4;
            }
         }

         var8++;
         var9 = var10;
         var4 = var5;
      }
   }

   void set_data_type() {
      int var1 = 0;
      short var2 = 0;
      short var3 = 0;

      while (var1 < 7) {
         var3 += this.dyn_ltree[var1 << 1];
         var1++;
      }

      while (var1 < 128) {
         var2 += this.dyn_ltree[var1 << 1];
         var1++;
      }

      while (var1 < 256) {
         var3 += this.dyn_ltree[var1 << 1];
         var1++;
      }

      byte var4;
      if (var3 > var2 >>> 2) {
         var4 = 0;
      } else {
         var4 = 1;
      }

      this.data_type = (byte)((byte)var4);
   }

   void tr_init() {
      this.l_desc.dyn_tree = this.dyn_ltree;
      this.l_desc.stat_desc = StaticTree.static_l_desc;
      this.d_desc.dyn_tree = this.dyn_dtree;
      this.d_desc.stat_desc = StaticTree.static_d_desc;
      this.bl_desc.dyn_tree = this.bl_tree;
      this.bl_desc.stat_desc = StaticTree.static_bl_desc;
      this.bi_buf = (short)0;
      this.bi_valid = 0;
      this.last_eob_len = 8;
      this.init_block();
   }

   static class Config {
      int func;
      int good_length;
      int max_chain;
      int max_lazy;
      int nice_length;

      Config(int var1, int var2, int var3, int var4, int var5) {
         this.good_length = var1;
         this.max_lazy = var2;
         this.nice_length = var3;
         this.max_chain = var4;
         this.func = var5;
      }
   }
}
