package ru.ivansuper.jasmin;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * A Drawable that fills its bounds with a solid color.
 * This class is useful for creating simple backgrounds or placeholders.
 */
public class SolidDrawable extends Drawable {
    private final int color;

    public SolidDrawable(int color) {
        this.color = color;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(this.color);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(getBounds(), p);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }
}