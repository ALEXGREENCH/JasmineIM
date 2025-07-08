package ru.ivansuper.jasmin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import ru.ivansuper.jasmin.ui.MyTextView;

public class URLImageSpan extends ReplacementSpan {
    private Bitmap image = ((BitmapDrawable) resources.img_file).getBitmap();
    private int width = this.image.getWidth();
    private int height = this.image.getHeight();

    public URLImageSpan(final String url, final MyTextView container) {
        Thread loader = new Thread() {
            @Override
            public void run() {
                try {
                    URL url_ = new URL(url);
                    URLConnection connection = url_.openConnection();
                    BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                    in.mark(0);
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(in, null, opts);
                    if (opts.outHeight <= 0) {
                        throw new Exception("Can't decode bitmap");
                    }
                    if (opts.outWidth > 1024 || opts.outHeight > 1024) {
                        container.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                URLImageSpan.this.image = ((BitmapDrawable) resources.img_file_big).getBitmap();
                                container.requestLayout();
                            }
                        }, 250L);
                        return;
                    }
                    int max_dim = opts.outWidth;
                    float factor = (float) max_dim / opts.outHeight;
                    int container_width = container.getParentAvailableSpace();
                    int control = container_width - 8;
                    if (max_dim > control) {
                        max_dim = control;
                    }
                    int height_ = (int) (max_dim / factor);
                    opts.inJustDecodeBounds = false;
                    in.reset();
                    URLImageSpan.this.image = BitmapFactory.decodeStream(in, null, opts);
                    //noinspection DataFlowIssue
                    URLImageSpan.this.image = Bitmap.createScaledBitmap(URLImageSpan.this.image, max_dim, height_, true);
                    in.close();
                    URLImageSpan.this.image.setDensity(0);
                    URLImageSpan.this.width = URLImageSpan.this.image.getWidth();
                    URLImageSpan.this.height = URLImageSpan.this.image.getHeight();
                    container.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            container.requestLayout();
                        }
                    }, 250L);
                } catch (Exception e) {
                    container.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            URLImageSpan.this.image = ((BitmapDrawable) resources.img_file_bad).getBitmap();
                            container.requestLayout();
                        }
                    }, 250L);
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        };
        loader.setPriority(1);
        loader.setName("URLImageSpan loader");
        loader.start();
    }

    /** @noinspection NullableProblems*/
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        if (this.image != null) {
            canvas.drawBitmap(this.image, x, top, null);
        }
    }

    /** @noinspection NullableProblems*/
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null) {
            fm.ascent = -this.height;
            fm.descent = 0;
            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return this.width;
    }
}
