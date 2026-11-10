package ru.ivansuper.jasmin.jabber.jzlib;

import java.io.IOException;
import java.io.OutputStream;

public class ZOutputStream extends OutputStream {
    protected byte[] buf;
    protected byte[] buf1;
    protected int bufsize;
    protected boolean compress;
    protected int flush;
    protected OutputStream out;
    protected ZStream z;

    public ZOutputStream(OutputStream outputStream) {
        this.z = new ZStream();
        this.bufsize = 512;
        this.flush = 0;
        this.buf = new byte[this.bufsize];
        this.buf1 = new byte[1];
        this.out = outputStream;
        this.z.inflateInit();
        this.compress = false;
    }

    public ZOutputStream(OutputStream outputStream, int i) {
        this(outputStream, i, false);
    }

    public ZOutputStream(OutputStream outputStream, int i, boolean z) {
        this.z = new ZStream();
        this.bufsize = 512;
        this.flush = 0;
        this.buf = new byte[this.bufsize];
        this.buf1 = new byte[1];
        this.out = outputStream;
        this.z.deflateInit(i, z);
        this.compress = true;
    }

    @Override
    public void close() throws IOException {
        try {
            finish();
        } catch (IOException e) {
        } finally {
            end();
            this.out.close();
            this.out = null;
        }
    }

    public void end() {
        if (this.z == null) {
            return;
        }
        if (this.compress) {
            this.z.deflateEnd();
        } else {
            this.z.inflateEnd();
        }
        this.z.free();
        this.z = null;
    }

    public void finish() throws IOException {
        while (true) {
            this.z.next_out = this.buf;
            this.z.next_out_index = 0;
            this.z.avail_out = this.bufsize;
            int iDeflate = this.compress ? this.z.deflate(4) : this.z.inflate(4);
            if (iDeflate != 1 && iDeflate != 0) {
                throw new IOException();
            }
            if (this.bufsize - this.z.avail_out > 0) {
                this.out.write(this.buf, 0, this.bufsize - this.z.avail_out);
            }
            if (this.z.avail_in <= 0 && this.z.avail_out != 0) {
                flush();
                return;
            }
        }
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
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

    public void setFlushMode(int i) {
        this.flush = i;
    }

    @Override
    public void write(int i) throws IOException {
        this.buf1[0] = (byte) i;
        write(this.buf1, 0, 1);
    }

    @Override
    public void write(byte[] bArr, int i, int i2) throws IOException {
        if (i2 == 0) {
            return;
        }
        this.z.next_in = bArr;
        this.z.next_in_index = i;
        this.z.avail_in = i2;
        while (true) {
            this.z.next_out = this.buf;
            this.z.next_out_index = 0;
            this.z.avail_out = this.bufsize;
            if ((this.compress ? this.z.deflate(this.flush) : this.z.inflate(this.flush)) != 0) {
                throw new IOException();
            }
            this.out.write(this.buf, 0, this.bufsize - this.z.avail_out);
            if (this.z.avail_in <= 0 && this.z.avail_out != 0) {
                return;
            }
        }
    }
}
