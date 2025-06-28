package ru.ivansuper.jasmin;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class SolidDrawable extends Drawable {
    private final int color;

    public SolidDrawable(int color) {
        this.color = color;
    }

    /** @noinspection NullableProblems*/
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