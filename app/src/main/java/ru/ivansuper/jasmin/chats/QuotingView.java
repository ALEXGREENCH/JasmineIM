package ru.ivansuper.jasmin.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

/**
 * A custom view that displays a quote with a background and allows for smooth animations.
 * <p>
 * This view can capture another view's content as a bitmap and display it as a quote.
 * It provides methods to update the quote's position and appearance, including a color filter.
 * The view also handles smooth animation of the quote's vertical position.
 * </p>
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *     <li>Captures a view's content into a bitmap.</li>
 *     <li>Displays the captured bitmap as a quote.</li>
 *     <li>Animates the quote's vertical position smoothly.</li>
 *     <li>Allows setting a color filter for the quote's background.</li>
 *     <li>Manages its own buffer bitmap and recycles it when stopped.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage:</b>
 * <ol>
 *     <li>Create an instance of {@code QuotingView} in your layout or programmatically.</li>
 *     <li>Call {@link #capture(View, int)} to capture the content of another view.</li>
 *     <li>Call {@link #updatePoints(float, float, boolean)} to update the quote's position and color.</li>
 *     <li>Call {@link #stop()} to release resources when the quote is no longer needed.</li>
 * </ol>
 * </p>
 */
public class QuotingView extends View {
    private float _y1;
    private Paint back_paint;
    private Bitmap buffer;
    private boolean enabled;
    private Paint paint;
    private float y1;

    public QuotingView(Context context) {
        super(context);
        this.enabled = false;
        init();
    }

    public QuotingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = false;
        init();
    }

    public QuotingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.enabled = false;
        init();
    }

    private void init() {
        this.paint = new Paint();
        this.back_paint = new Paint();
        this.back_paint.setColor(1996488704);
        this.back_paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onMeasure(int width_spec, int height_spec) {
        int width = View.MeasureSpec.getSize(width_spec);
        int height = View.MeasureSpec.getSize(height_spec);
        setMeasuredDimension(width, height);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (this.buffer == null) {
            Paint p = new Paint();
            p.setColor(0);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST));
            canvas.drawPaint(p);
            return;
        }
        canvas.save();
        canvas.translate(0.0f, this._y1);
        canvas.drawRect(0.0f, ((float) (-this.buffer.getHeight()) / 2) - 2, this.buffer.getWidth(), ((float) this.buffer.getHeight() / 2) + 2, this.back_paint);
        canvas.drawBitmap(this.buffer, 0.0f, (float) (-this.buffer.getHeight()) / 2, this.paint);
        canvas.restore();
        if (this.buffer != null) {
            invalidate();
        }
        this._y1 += (this.y1 - this._y1) / 2.0f;
    }

    public void stop() {
        if (this.buffer != null) {
            this.buffer.recycle();
            this.buffer = null;
        }
        this.enabled = false;
        invalidate();
    }

    /** @noinspection unused*/
    public void capture(View view, int top) {
        if (this.buffer != null) {
            this.buffer.recycle();
        }
        this.buffer = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_4444);
        Canvas wrap = new Canvas(this.buffer);
        view.draw(wrap);
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        int[] loc2 = new int[2];
        super.getLocationOnScreen(loc2);
        this._y1 = (loc[1] + ((float) this.buffer.getHeight() / 2)) - loc2[1];
    }

    /** @noinspection unused*/
    public void updatePoints(float x1, float y1, boolean green) {
        if (!this.enabled) {
            this.enabled = true;
        }
        int[] location = new int[2];
        getLocationOnScreen(location);
        this.y1 = y1 - location[1];
        if (green) {
            this.back_paint.setColorFilter(new PorterDuffColorFilter(1996553984, PorterDuff.Mode.XOR));
        } else {
            this.back_paint.setColorFilter(null);
        }
        invalidate();
    }
}