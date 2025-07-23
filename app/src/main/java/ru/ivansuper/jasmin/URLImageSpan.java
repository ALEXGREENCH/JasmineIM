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

/**
 * A {@link ReplacementSpan} that loads an image from a URL and displays it.
 * <p>
 * This class handles asynchronous image loading in a separate thread.
 * It initially displays a placeholder image ({@code resources.img_file}) and updates
 * it with the loaded image, a "too big" image ({@code resources.img_file_big}), or an
 * error image ({@code resources.img_file_bad}) upon completion or failure.
 * </p>
 * <p>
 * Image dimensions are constrained to a maximum of 1024x1024 and are scaled to fit
 * within the available width of the container {@link MyTextView}.
 * </p>
 * <p>
 * After the image is loaded (or an error occurs), a layout request is posted to the
 * container to reflect the changes.
 * </p>
 */
public class URLImageSpan extends ReplacementSpan {
    /**
     * The Bitmap to be displayed.
     * Initially, this is a placeholder image ({@code resources.img_file}).
     * It is updated asynchronously with the loaded image, a "too big" image
     * ({@code resources.img_file_big}), or an error image
     * ({@code resources.img_file_bad}).
     */
    private Bitmap image = ((BitmapDrawable) resources.img_file).getBitmap();
    /**
     * The width of the image to be displayed.
     * This value is initialized with the width of the placeholder image and updated
     * after the actual image is loaded and potentially scaled.
     */
    private int width = this.image.getWidth();
    /**
     * The height of the image to be displayed.
     * <p>
     * Initially, this is the height of the placeholder image.
     * After successful loading and scaling, it's updated to the height of the loaded image.
     * In case of errors or if the image is too large, it will be the height of the
     * respective error or "too big" placeholder image.
     * </p>
     * <p>
     * This value is used in {@link #getSize(Paint, CharSequence, int, int, Paint.FontMetricsInt)}
     * to determine the vertical space the span will occupy.
     * </p>
     */
    private int height = this.image.getHeight();

    /**
     * Constructs a {@code URLImageSpan}.
     * <p>
     * This constructor initiates the asynchronous loading of the image from the specified URL.
     * A placeholder image ({@code resources.img_file}) is displayed initially.
     * The image loading happens on a background thread.
     * </p>
     * <p>
     * If the image dimensions exceed 1024x1024, a "too big" image ({@code resources.img_file_big})
     * is displayed. If any other error occurs during loading or decoding, an error image
     * ({@code resources.img_file_bad}) is displayed.
     * </p>
     * <p>
     * Successfully loaded images are scaled to fit the available width of the {@code container}
     * {@link MyTextView}, maintaining their aspect ratio.
     * </p>
     * <p>
     * After the image is loaded (or an error occurs), a layout request is posted to the
     * {@code container} with a delay of 250 milliseconds to update the display.
     * </p>
     *
     * @param url The URL of the image to load.
     * @param container The {@link MyTextView} that will contain this span. This is used
     *                  to determine available space for scaling and to request layout updates.
     */
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

    /**
     * Draws the image onto the canvas.
     * <p>
     * If the image is available ({@code this.image != null}), it is drawn at the
     * specified coordinates ({@code x}, {@code top}).
     *
     * @param canvas The canvas to draw on.
     * @param text The text being spanned.
     * @param start The starting index of the span in the text.
     * @param end The ending index of the span in the text.
     * @param x The x-coordinate of the leading edge of the span.
     * @param top The top y-coordinate of the line.
     * @param y The baseline y-coordinate.
     * @param bottom The bottom y-coordinate of the line.
     * @param paint The paint to use for drawing.
     * @noinspection NullableProblems
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        if (this.image != null) {
            canvas.drawBitmap(this.image, x, top, null);
        }
    }

    /**
     * Returns the width of the image.
     * <p>
     * If {@code fm} is not null, it also sets the font metrics to accommodate the image's height.
     * The ascent is set to the negative of the image height, and descent, top, and bottom are set to 0.
     * </p>
     *
     * @param paint The paint instance, not used.
     * @param text  The text, not used.
     * @param start The start index, not used.
     * @param end   The end index, not used.
     * @param fm    The font metrics to be updated. If null, font metrics are not modified.
     * @return The width of the image.
     * @noinspection NullableProblems
     */
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
