package ru.ivansuper.jasmin;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * A {@link MetricAffectingSpan} that applies a specific style to text, including size, color, and bolding.
 *
 * This class is used to modify the appearance of a portion of text within a larger string.
 * It can change the text size, color, and make it bold.
 *
 * <p>The size is specified in density-independent pixels (dp) and will be converted to pixels
 * based on the device's screen density. If the size is negative, the default text size will be used.
 *
 * <p>The color is an integer representing an ARGB color value. If the color is 0, the default
 * text color will be used.
 *
 * <p>The bold attribute is a boolean that determines whether the text should be rendered in bold.
 *
 * <p>This span affects both the measurement and drawing of the text.
 *
 * @see MetricAffectingSpan
 * @see TextPaint
 * @see Typeface
 */
public class StyleSpan extends MetricAffectingSpan {
    private final boolean bold;
    private int color;
    private int size;

    public StyleSpan(int size, int color, boolean bold) {
        this.size = 0;
        this.color = 0;
        this.size = size;
        this.color = color;
        this.bold = bold;
    }

    /** @noinspection NullableProblems*/
    @Override
    public void updateMeasureState(TextPaint textPaintToUpdate) {
        if (this.color != 0) {
            textPaintToUpdate.setColor(this.color);
        }
        textPaintToUpdate.linkColor = textPaintToUpdate.getColor();
        textPaintToUpdate.setTextSize(this.size < 0 ? textPaintToUpdate.getTextSize() : this.size * resources.dm.density);
        if (this.bold) {
            textPaintToUpdate.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
    }

    @Override
    public void updateDrawState(TextPaint textPaintToUpdate) {
        if (this.color != 0) {
            textPaintToUpdate.setColor(this.color);
        }
        textPaintToUpdate.linkColor = textPaintToUpdate.getColor();
        textPaintToUpdate.setTextSize(this.size < 0 ? textPaintToUpdate.getTextSize() : this.size * resources.dm.density);
        if (this.bold) {
            textPaintToUpdate.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
    }
}
