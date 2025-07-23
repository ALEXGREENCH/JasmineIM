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

    /**
     * Creates a new SolidDrawable with the specified color.
     *
     * @param color The color to fill the drawable with.
     *              This should be an ARGB color value (e.g., 0xFFFF0000 for red).
     */
    public SolidDrawable(int color) {
        this.color = color;
    }

    /**
     * Draws the solid color within the bounds of this drawable onto the provided canvas.
     *
     * @param canvas The canvas to draw on.
     */
    @Override
    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(this.color);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(getBounds(), p);
    }

    /**
     * Returns the opacity of the drawable.
     *
     * <p>This implementation always returns {@link PixelFormat#TRANSLUCENT}
     * because the color may have an alpha component, making it translucent.
     *
     * @return {@link PixelFormat#TRANSLUCENT}
     */
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * Sets the alpha value for this drawable.
     * This method is currently a no-op as the alpha is determined by the color.
     *
     * @param alpha the alpha value to set, from 0 (transparent) to 255 (opaque)
     */
    @Override
    public void setAlpha(int alpha) {
    }

    /**
     * Set a color filter for this drawable.
     * <p>
     * A color filter can be used to tint the drawable with a specific color,
     * or to apply more complex color transformations.
     * <p>
     * Note: This implementation is a no-op as {@link SolidDrawable} inherently
     * defines its color during construction and does not support dynamic color filtering.
     *
     * @param cf The color filter to apply, or {@code null} to remove any existing filter.
     */
    @Override
    public void setColorFilter(ColorFilter cf) {
    }
}