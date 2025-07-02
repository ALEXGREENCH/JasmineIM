package ru.ivansuper.jasmin.animate_tools;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class GifDecoder {
    public static final int MaxStackSize = 4096;
    public static final int STATUS_OK = 0;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OPEN_ERROR = 2;

    protected int[] act;
    protected int bgColor;
    protected int bgIndex;
    protected int[] gct;
    protected boolean gctFlag;
    protected int gctSize;
    protected int[] lct;
    protected boolean lctFlag;
    protected int lctSize;
    protected int width;
    protected int height;
    protected int pixelAspect;
    protected int frameCount;
    protected int delay = 0;
    protected int dispose = 0;
    protected int lastDispose = 0;
    protected boolean transparency = false;
    protected int transIndex;
    protected int ix, iy, iw, ih;
    protected boolean interlace;
    protected InputStream in;
    protected byte[] block = new byte[256];
    protected int blockSize = 0;
    protected int[] image;
    protected int[] lastImage;
    protected Rect lastRect;
    protected int lastBgColor;
    protected byte[] pixels;
    protected byte[] pixelStack;
    protected short[] prefix;
    protected byte[] suffix;
    protected int status;
    protected int loopCount = 1;
    public final ArrayList<GifFrame> frames = new ArrayList<>();

    public static class GifFrame {
        public final Bitmap image;
        public final int delay;

        public GifFrame(Bitmap im, int del) {
            this.image = im;
            this.delay = del;
        }
    }

    public int read(InputStream is) {
        init();
        if (is != null) {
            this.in = is;
            readHeader();
            if (!err()) {
                readContents();
                if (frameCount < 0) status = STATUS_FORMAT_ERROR;
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }
        return status;
    }

    public int read(String path) {
        status = STATUS_OK;
        try {
            if (path.trim().toLowerCase().contains("file:") || path.contains(":/")) {
                in = new URL(path).openStream();
            } else {
                in = new FileInputStream(path);
            }
            status = read(in);
        } catch (IOException e) {
            status = STATUS_OPEN_ERROR;
        }
        return status;
    }

    public int getDelay(int n) {
        return (n >= 0 && n < frameCount) ? frames.get(n).delay : -1;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public Bitmap getFrame(int n) {
        return (n >= 0 && n < frameCount) ? frames.get(n).image : null;
    }

    public int[] getFrame(int n, boolean copyPixels) {
        if (n < 0 || n >= frameCount) return null;
        Bitmap bmp = frames.get(n).image;
        int[] array = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(array, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        return array;
    }

    /** @noinspection unused*/
    public int getLoopCount() {
        return loopCount;
    }

    public final int[] copyPixelToSetInteger(int[] B) {
        return B;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void init() {
        status = 0;
        frameCount = 0;
        frames.clear();
        gct = null;
        lct = null;
    }

    protected boolean err() {
        return status != STATUS_OK;
    }

    protected int read() {
        try {
            return in.read();
        } catch (IOException e) {
            status = STATUS_FORMAT_ERROR;
            return 0;
        }
    }

    protected int readShort() {
        return read() | (read() << 8);
    }

    protected int readBlock() {
        blockSize = read();
        int n = 0;
        if (blockSize > 0) {
            try {
                while (n < blockSize) {
                    int count = in.read(block, n, blockSize - n);
                    if (count == -1) break;
                    n += count;
                }
            } catch (IOException ignored) {
            }
            if (n < blockSize) status = STATUS_FORMAT_ERROR;
        }
        return n;
    }

    protected int[] readColorTable(int ncolors) {
        int[] table = new int[256];
        byte[] c = new byte[ncolors * 3];
        try {
            int n = in.read(c);
            if (n < c.length) {
                status = STATUS_FORMAT_ERROR;
                return null;
            }
        } catch (IOException e) {
            status = STATUS_FORMAT_ERROR;
            return null;
        }
        for (int i = 0, j = 0; i < ncolors; i++) {
            int r = c[j++] & 0xFF;
            int g = c[j++] & 0xFF;
            int b = c[j++] & 0xFF;
            table[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return table;
    }

    protected void readHeader() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 6; i++) id.append((char) read());
        if (!id.toString().startsWith("GIF")) {
            status = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (gctFlag && !err()) {
            gct = readColorTable(gctSize);
            bgColor = gct[bgIndex];
        }
    }

    protected void readLSD() {
        width = readShort();
        height = readShort();
        int packed = read();
        gctFlag = (packed & 0x80) != 0;
        gctSize = 2 << (packed & 7);
        bgIndex = read();
        pixelAspect = read();
    }

    protected void readContents() {
        boolean done = false;
        while (!done && !err()) {
            int code = read();
            switch (code) {
                case 0x2C: // Image Descriptor
                    readImage();
                    break;
                case 0x21: // Extension
                    int extType = read();
                    if (extType == 0xF9) {
                        readGraphicControlExt();
                    } else if (extType == 0xFF) {
                        readBlock();
                        StringBuilder app = new StringBuilder();
                        for (int i = 0; i < 11; i++) app.append((char) block[i]);
                        if (app.toString().equals("NETSCAPE2.0")) {
                            readNetscapeExt();
                        } else {
                            skip();
                        }
                    } else {
                        skip();
                    }
                    break;
                case 0x3B: // Trailer
                    done = true;
                    break;
                default:
                    status = STATUS_FORMAT_ERROR;
                    break;
            }
        }
    }

    protected void readGraphicControlExt() {
        read(); // block size
        int packed = read();
        dispose = (packed >> 2) & 0x07;
        if (dispose == 0) dispose = 1;
        transparency = (packed & 1) != 0;
        delay = readShort() * 10;
        transIndex = read();
        read(); // block terminator
    }

    protected void readImage() {
        ix = readShort();
        iy = readShort();
        iw = readShort();
        ih = readShort();

        int packed = read();
        lctFlag = (packed & 0x80) != 0;
        interlace = (packed & 0x40) != 0;
        lctSize = 2 << (packed & 7);

        if (lctFlag) {
            lct = readColorTable(lctSize);
            act = lct;
        } else {
            act = gct;
        }

        if (transparency && transIndex < act.length) {
            int save = act[transIndex];
            act[transIndex] = 0; // Transparent

            decodeImageData();
            skip();

            if (!err()) {
                frameCount++;
                image = new int[width * height];
                setPixels();

                Bitmap bmp = Bitmap.createBitmap(image, width, height, Bitmap.Config.ARGB_4444);
                frames.add(new GifFrame(bmp, delay));
                act[transIndex] = save;
            }
        } else {
            decodeImageData();
            skip();

            if (!err()) {
                frameCount++;
                image = new int[width * height];
                setPixels();
                Bitmap bmp = Bitmap.createBitmap(image, width, height, Bitmap.Config.ARGB_4444);
                frames.add(new GifFrame(bmp, delay));
            }
        }

        resetFrame();
    }

    protected void readNetscapeExt() {
        do {
            readBlock();
            if (block[0] == 1) {
                loopCount = ((block[2] & 0xFF) << 8) | (block[1] & 0xFF);
            }
        } while (blockSize > 0 && !err());
    }

    protected final void decodeImageData() {
        int dataSize = read(); // LZW minimum code size
        int clearCode = 1 << dataSize;
        int endOfInfo = clearCode + 1;
        int codeSize = dataSize + 1;
        int codeMask = (1 << codeSize) - 1;

        int available = clearCode + 2;
        int oldCode = -1;
        int code;
        int pixelIndex = 0;

        int data = 0;
        int bits = 0;
        int count = 0;
        int blockIndex = 0;

        int outputSize = iw * ih;
        if (pixels == null || pixels.length < outputSize) {
            pixels = new byte[outputSize];
        }
        if (prefix == null) prefix = new short[MaxStackSize];
        if (suffix == null) suffix = new byte[MaxStackSize];
        if (pixelStack == null) pixelStack = new byte[MaxStackSize + 1];

        for (int i = 0; i < clearCode; i++) {
            prefix[i] = 0;
            suffix[i] = (byte) i;
        }

        int top = 0;
        int first = 0;

        while (pixelIndex < outputSize) {
            if (top == 0) {
                while (bits < codeSize) {
                    if (count == 0) {
                        count = readBlock();
                        if (count <= 0) break;
                        blockIndex = 0;
                    }
                    data += (block[blockIndex] & 0xFF) << bits;
                    bits += 8;
                    blockIndex++;
                    count--;
                }

                code = data & codeMask;
                data >>= codeSize;
                bits -= codeSize;

                if (code == clearCode) {
                    codeSize = dataSize + 1;
                    codeMask = (1 << codeSize) - 1;
                    available = clearCode + 2;
                    oldCode = -1;
                    continue;
                } else if (code == endOfInfo || code > available) {
                    break;
                }

                if (oldCode == -1) {
                    pixelStack[top++] = suffix[code];
                    oldCode = code;
                    first = code;
                    continue;
                }

                int inCode = code;
                if (code == available) {
                    pixelStack[top++] = (byte) first;
                    code = oldCode;
                }

                while (code >= clearCode) {
                    pixelStack[top++] = suffix[code];
                    code = prefix[code];
                }

                first = suffix[code] & 0xFF;
                pixelStack[top++] = (byte) first;

                if (available < MaxStackSize) {
                    prefix[available] = (short) oldCode;
                    suffix[available] = (byte) first;
                    available++;
                    if ((available & codeMask) == 0 && available < MaxStackSize) {
                        codeSize++;
                        codeMask = (1 << codeSize) - 1;
                    }
                }

                oldCode = inCode;
            }

            top--;
            pixels[pixelIndex++] = pixelStack[top];
        }

        while (pixelIndex < outputSize) {
            pixels[pixelIndex++] = 0; // fill with 0 if something goes wrong
        }
    }

    protected final void setPixels() {
        int[] dest = copyPixelToSetInteger(this.image);

        if (lastDispose > 0) {
            if (lastDispose == 3) {
                int prevFrameIndex = frameCount - 2;
                lastImage = (prevFrameIndex >= 0) ? getFrame(prevFrameIndex, true) : null;
            }
            if (lastDispose != 1 && lastDispose != 3) {
                lastImage = null;
            }
            if (lastImage != null) {
                System.arraycopy(lastImage, 0, dest, 0, width * height);
            }
        }

        int pass = 1;
        int inc = 8;
        int iline = 0;

        for (int i = 0; i < ih; i++) {
            int line = i;
            if (interlace) {
                if (iline >= ih) {
                    pass++;
                    switch (pass) {
                        case 2: iline = 4; break;
                        case 3: iline = 2; inc = 4; break;
                        case 4: iline = 1; inc = 2; break;
                    }
                }
                line = iline;
                iline += inc;
            }

            int destY = line + iy;
            if (destY >= height) continue;

            int destIndex = destY * width + ix;
            int srcIndex = i * iw;
            int lineEnd = destIndex + iw;
            if (destIndex + iw > dest.length) lineEnd = dest.length;

            while (destIndex < lineEnd && srcIndex < pixels.length) {
                int index = pixels[srcIndex++] & 0xFF;
                int color = act != null && index < act.length ? act[index] : 0;
                if (color != 0) dest[destIndex] = color;
                destIndex++;
            }
        }

        System.arraycopy(dest, 0, image, 0, dest.length);
    }

    protected void resetFrame() {
        lastDispose = dispose;
        lastRect = new Rect(ix, iy, ix + iw, iy + ih);
        lastImage = image;
        lastBgColor = bgColor;
        dispose = 0;
        transparency = false;
        delay = 0;
        lct = null;
    }

    protected void skip() {
        do {
            readBlock();
        } while (blockSize > 0 && !err());
    }
}
