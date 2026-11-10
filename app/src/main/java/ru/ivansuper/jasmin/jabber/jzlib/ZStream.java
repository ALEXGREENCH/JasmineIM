package ru.ivansuper.jasmin.jabber.jzlib;

public final class ZStream {
    private static final byte MAX_MEM_LEVEL = 9;
    private static final byte Z_BUF_ERROR = -5;
    private static final byte Z_DATA_ERROR = -3;
    private static final byte Z_ERRNO = -1;
    private static final byte Z_FINISH = 4;
    private static final byte Z_FULL_FLUSH = 3;
    private static final byte Z_MEM_ERROR = -4;
    private static final byte Z_NEED_DICT = 2;
    private static final byte Z_NO_FLUSH = 0;
    private static final byte Z_OK = 0;
    private static final byte Z_PARTIAL_FLUSH = 1;
    private static final byte Z_STREAM_END = 1;
    private static final byte Z_STREAM_ERROR = -2;
    private static final byte Z_SYNC_FLUSH = 2;
    private static final byte Z_VERSION_ERROR = -6;
    Adler32 _adler = new Adler32();
    public long adler;
    public int avail_in;
    public int avail_out;
    int data_type;
    Deflate dstate;
    Inflate istate;
    public String msg;
    public byte[] next_in;
    public int next_in_index;
    public byte[] next_out;
    public int next_out_index;
    public long total_in;
    public long total_out;

    public int deflate(int i) {
        if (this.dstate == null) {
            return -2;
        }
        return this.dstate.deflate(this, i);
    }

    public int deflateEnd() {
        if (this.dstate == null) {
            return -2;
        }
        int iDeflateEnd = this.dstate.deflateEnd();
        this.dstate = null;
        return iDeflateEnd;
    }

    public int deflateInit(int i) {
        return deflateInit(i, 9);
    }

    public int deflateInit(int i, int i2) {
        return deflateInit(i, i2, false);
    }

    public int deflateInit(int i, int i2, boolean z) {
        this.dstate = new Deflate();
        Deflate deflate = this.dstate;
        if (z) {
            i2 = -i2;
        }
        return deflate.deflateInit(this, i, i2);
    }

    public int deflateInit(int i, boolean z) {
        return deflateInit(i, 9, z);
    }

    public int deflateParams(int i, int i2) {
        if (this.dstate == null) {
            return -2;
        }
        return this.dstate.deflateParams(this, i, i2);
    }

    public int deflateSetDictionary(byte[] bArr, int i) {
        if (this.dstate == null) {
            return -2;
        }
        return this.dstate.deflateSetDictionary(this, bArr, i);
    }

    void flush_pending() {
        int i = this.dstate.pending;
        if (i > this.avail_out) {
            i = this.avail_out;
        }
        if (i == 0) {
            return;
        }
        System.arraycopy(this.dstate.pending_buf, this.dstate.pending_out, this.next_out, this.next_out_index, i);
        this.next_out_index += i;
        this.dstate.pending_out += i;
        this.total_out += (long) i;
        this.avail_out -= i;
        this.dstate.pending -= i;
        if (this.dstate.pending == 0) {
            this.dstate.pending_out = 0;
        }
    }

    public void free() {
        this.next_in = null;
        this.next_out = null;
        this.msg = null;
        this._adler = null;
    }

    public int inflate(int i) {
        if (this.istate == null) {
            return -2;
        }
        return this.istate.inflate(this, i);
    }

    public int inflateEnd() {
        if (this.istate == null) {
            return -2;
        }
        int iInflateEnd = this.istate.inflateEnd(this);
        this.istate = null;
        return iInflateEnd;
    }

    public int inflateInit() {
        return inflateInit(15);
    }

    public int inflateInit(int i) {
        return inflateInit(i, false);
    }

    public int inflateInit(int i, boolean z) {
        this.istate = new Inflate();
        Inflate inflate = this.istate;
        if (z) {
            i = -i;
        }
        return inflate.inflateInit(this, i);
    }

    public int inflateInit(boolean z) {
        return inflateInit(15, z);
    }

    public int inflateSetDictionary(byte[] bArr, int i) {
        if (this.istate == null) {
            return -2;
        }
        return this.istate.inflateSetDictionary(this, bArr, i);
    }

    public int inflateSync() {
        if (this.istate == null) {
            return -2;
        }
        return this.istate.inflateSync(this);
    }

    int read_buf(byte[] bArr, int i, int i2) {
        int i3 = this.avail_in;
        if (i3 <= i2) {
            i2 = i3;
        }
        if (i2 == 0) {
            return 0;
        }
        this.avail_in -= i2;
        if (this.dstate.noheader == 0) {
            this.adler = this._adler.adler32(this.adler, this.next_in, this.next_in_index, i2);
        }
        System.arraycopy(this.next_in, this.next_in_index, bArr, i, i2);
        this.next_in_index += i2;
        this.total_in += (long) i2;
        return i2;
    }
}
