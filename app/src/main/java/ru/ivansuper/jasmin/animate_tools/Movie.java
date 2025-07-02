package ru.ivansuper.jasmin.animate_tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.ivansuper.jasmin.popup_log_adapter;

public class Movie {

    public static long stamp;

    public boolean animated;
    public boolean gif;

    public int width;
    public int height;
    public int minimal_refresh_rate;

    private int src_width;
    private int src_height;
    private int frame_count;

    private volatile Bitmap frame;
    private GifDecoder decoder;
    private GifDecoder.GifFrame[] frames;
    private int[] timeline;

    private long draw_start = -1;
    private long timeline_now = -1;

    private final Rect dest = new Rect();
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public static final int SCALE_VERY_SMALL = 0;
    public static final int SCALE_SMALL = 1;
    public static final int SCALE_MEDIUM = 2;
    public static final int SCALE_HIGH = 3;

    public Movie(BufferedInputStream is, Context context) {
        this.src_width = 1;
        this.src_height = 1;
        this.width = 1;
        this.height = 1;
        this.minimal_refresh_rate = popup_log_adapter.DEFAULT_DISPLAY_TIME;
        this.frame_count = 0;

        boolean isGif = itIsGIF(is);

        if (isGif) {
            this.gif = true;
            this.decoder = new GifDecoder();
            try {
                is.mark(0);
                decoder.read(is);

                if (decoder.getFrameCount() > 1) {
                    this.animated = true;
                    this.frame_count = decoder.getFrameCount();

                    this.frames = new GifDecoder.GifFrame[frame_count];
                    decoder.frames.toArray(this.frames);

                    this.timeline = new int[frame_count];
                    int sum = 0;
                    for (int i = 0; i < frame_count; i++) {
                        int delay = decoder.getDelay(i);
                        if (delay == 0) delay = 100;
                        if (delay < minimal_refresh_rate) minimal_refresh_rate = delay;
                        sum += delay;
                        timeline[i] = sum;
                    }

                    this.frame = Bitmap.createBitmap(decoder.getWidth(), decoder.getHeight(), Bitmap.Config.ALPHA_8);
                    this.width = this.src_width = frame.getWidth();
                    this.height = this.src_height = frame.getHeight();
                } else {
                    this.gif = false;
                    is.reset();
                    loadAsSingle(is);
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.gif = false;
                loadAsSingle(is);
            } finally {
                this.decoder = null;
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        } else {
            loadAsSingle(is);
            try {
                is.close();
            } catch (IOException ignored) {}
        }

        changeScale(context, 1); // default scale
    }

    private void loadAsSingle(InputStream is) {
        Bitmap bmp = BitmapFactory.decodeStream(is);
        if (bmp != null) {
            this.frame = bmp.copy(Bitmap.Config.ARGB_4444, false);
            this.width = this.src_width = frame.getWidth();
            this.height = this.src_height = frame.getHeight();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void recomputeSize(float newHeight) {
        float ratio = newHeight / this.height;
        this.width = (int) (this.width * ratio);
        this.height = (int) newHeight;
    }

    public void changeScale(Context context, int value) {
        if (value < 10 || value > 500) value = 100;

        float scale = value / 100.0f;
        this.width = (int) (src_width * scale);
        this.height = (int) (src_height * scale);

        if (gif && frame_count > 0) {
            for (GifDecoder.GifFrame frame : frames) {
                frame.image.setDensity(0); // disable auto-scaling
            }
        }
    }

    public void draw(Canvas canvas, float x, float y) {
        draw(canvas, x, y, height);
    }

    public void draw(Canvas canvas, float x, float y, float heightOverride) {
        dest.left = (int) x;
        dest.top = (int) y;
        dest.right = (int) (x + width);
        dest.bottom = (int) (y + heightOverride);

        if (gif) {
            if (frame_count > 1) {
                if (draw_start == -1) draw_start = 0L;

                timeline_now = (stamp - draw_start) % timeline[frame_count - 1];
                for (int i = 0; i < frame_count; i++) {
                    if (timeline_now <= timeline[i]) {
                        canvas.drawBitmap(frames[i].image, null, dest, paint);
                        return;
                    }
                }
            } else if (frame_count == 1) {
                canvas.drawBitmap(frames[0].image, null, dest, paint);
            }
        } else if (frame != null) {
            canvas.drawBitmap(frame, null, dest, paint);
        }
    }

    public static synchronized boolean itIsGIF(InputStream is) {
        try {
            is.mark(3);
            if (is.available() < 3) return false;

            byte[] signature = new byte[3];
            int read = is.read(signature, 0, 3);
            is.reset();

            return read == 3 && signature[0] == 'G' && signature[1] == 'I' && signature[2] == 'F';
        } catch (IOException e) {
            try {
                is.reset();
            } catch (IOException ignored) {}
            return false;
        }
    }
}
