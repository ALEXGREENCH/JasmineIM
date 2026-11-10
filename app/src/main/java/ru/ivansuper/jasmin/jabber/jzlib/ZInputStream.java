package ru.ivansuper.jasmin.jabber.jzlib;

import java.io.IOException;
import java.io.InputStream;

public class ZInputStream extends InputStream {
    protected byte[] buf;
    protected byte[] buf1;
    protected int bufsize;
    protected boolean compress;
    protected int flush;
    protected InputStream in;
    private boolean nomoreinput;
    protected ZStream z;

    public ZInputStream(InputStream inputStream) {
        this(inputStream, false);
    }

    public ZInputStream(InputStream inputStream, int i) {
        this.z = new ZStream();
        this.bufsize = 512;
        this.flush = 0;
        this.buf = new byte[this.bufsize];
        this.buf1 = new byte[1];
        this.in = null;
        this.nomoreinput = false;
        this.in = inputStream;
        this.z.deflateInit(i);
        this.compress = true;
        this.z.next_in = this.buf;
        this.z.next_in_index = 0;
        this.z.avail_in = 0;
    }

    public ZInputStream(InputStream inputStream, boolean z) {
        this.z = new ZStream();
        this.bufsize = 512;
        this.flush = 0;
        this.buf = new byte[this.bufsize];
        this.buf1 = new byte[1];
        this.in = null;
        this.nomoreinput = false;
        this.in = inputStream;
        this.z.inflateInit(z);
        this.compress = false;
        this.z.next_in = this.buf;
        this.z.next_in_index = 0;
        this.z.avail_in = 0;
    }

    @Override
    public int available() throws IOException {
        return this.bufsize;
    }

    @Override
    public void close() throws IOException {
        this.z.free();
        this.in.close();
        this.in = null;
    }

    public int getFlushMode() {
        return this.flush;
    }

    public long getTotalIn() {
        return this.z.total_in;
    }

    public long getTotalOut() {
        return this.z.total_out;
    }

    @Override
    public int read() throws IOException {
        if (read(this.buf1, 0, 1) == -1) {
            return -1;
        }
        return this.buf1[0] & 255;
    }

    @Override
    public int read(byte[] bArr, int i, int i2) throws IOException {
        int iDeflate;
        if (i2 == 0) {
            return 0;
        }
        this.z.next_out = bArr;
        this.z.next_out_index = i;
        this.z.avail_out = i2;
        do {
            if (this.z.avail_in == 0 && !this.nomoreinput) {
                this.z.next_in_index = 0;
                this.z.avail_in = this.in.read(this.buf, 0, this.bufsize);
                if (this.z.avail_in == -1) {
                    this.z.avail_in = 0;
                    this.nomoreinput = true;
                }
            }
            iDeflate = this.compress ? this.z.deflate(this.flush) : this.z.inflate(this.flush);
            if (!this.nomoreinput || iDeflate != -5) {
                if (iDeflate != 0 && iDeflate != 1) {
                    throw new IOException(String.valueOf(this.compress ? "de" : "in") + "flating: " + this.z.msg);
                }
                if ((!this.nomoreinput && iDeflate != 1) || this.z.avail_out != i2) {
                    if (this.z.avail_out != i2) {
                        break;
                    }
                }
            }
            return -1;
        } while (iDeflate == 0);
        return i2 - this.z.avail_out;
    }

    public void setFlushMode(int i) {
        this.flush = i;
    }

    @Override
    public long skip(long j) throws IOException {
        return read(new byte[j < ((long) 512) ? (int) j : 512]);
    }
}
