package ru.ivansuper.jasmin.compat;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

/** Donut fallback for the framework scrollbar, without hidden framework fields. */
public final class ScrollIndicatorCompat {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF thumb = new RectF();
    private long visibleUntil;
    private int lastOffset = -1;
    private int lastRange = -1;

    public void awaken(View view, int delay) {
        visibleUntil = SystemClock.uptimeMillis() + (delay < 0 ? 1000 : delay);
        view.invalidate();
    }

    public void draw(Canvas canvas, View view, int extent, int offset, int range,
                     float density, int color, boolean moving) {
        int inset = Math.max(1, Math.round(2 * density));
        int width = Math.max(2, Math.round(3 * density));
        int track = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom() - 2 * inset;
        int height = ScrollIndicatorGeometry.thumbHeight(track, extent, range, Math.round(24 * density));
        if (height <= 0) { lastRange = -1; return; }
        long now = SystemClock.uptimeMillis();
        if (moving || offset != lastOffset || range != lastRange) visibleUntil = now + 1000;
        lastOffset = offset;
        lastRange = range;
        int alpha = ScrollIndicatorGeometry.alpha(now, visibleUntil, 300);
        if (alpha == 0) return;
        int top = view.getPaddingTop() + inset + ScrollIndicatorGeometry.thumbTop(track, height, offset, extent, range);
        int right = view.getWidth() - view.getPaddingRight() - inset;
        thumb.set(right - width, top, right, top + height);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1, density));
        paint.setColor(0xff000000);
        paint.setAlpha(alpha / 2);
        canvas.drawRoundRect(thumb, width / 2f, width / 2f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAlpha(((color >>> 24) * alpha + 127) / 255);
        canvas.drawRoundRect(thumb, width / 2f, width / 2f, paint);
        view.postInvalidateDelayed(now < visibleUntil ? visibleUntil - now : 16);
    }
}
