package ru.ivansuper.jasmin.compat;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;

/** Identical software/hardware text path, including the API 4 Canvas. */
public final class SectionTitleCompat {
    private final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private int shadowOffset;

    public void configure(float textSize, float density) {
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.clearShadowLayer();
        shadowOffset = Math.max(1, Math.round(density));
    }

    public float fontHeight() { return paint.descent() - paint.ascent(); }

    public void draw(Canvas canvas, String label, float center, int panelHeight,
                     float maxWidth, int color, int alpha) {
        if (label == null || maxWidth <= 0 || alpha <= 0) return;
        String text = TextUtils.ellipsize(label, paint, maxWidth, TextUtils.TruncateAt.END).toString();
        float left = Math.round(center - paint.measureText(text) / 2f);
        float baseline = Math.round((panelHeight - paint.ascent() - paint.descent()) / 2f);
        // A one-pixel/dp offset, not a blurred or stroked copy of the glyph.
        paint.setColor(0xff000000);
        paint.setAlpha(alpha * 140 / 255);
        canvas.drawText(text, left, baseline + shadowOffset, paint);
        paint.setColor(color);
        paint.setAlpha(((color >>> 24) * alpha + 127) / 255);
        canvas.drawText(text, left, baseline, paint);
    }
}
