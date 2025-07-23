package ru.ivansuper.jasmin;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * A {@link MetricAffectingSpan} that applies a specific style to text, including size, color, and bolding.
 * <p>
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

    /**
     * Constructs a {@code StyleSpan} with the specified size, color, and bold state.
     *
     * @param size The desired text size in density-independent pixels (dp).
     *             If negative, the default text size will be used.
     * @param color The desired text color as an ARGB integer.
     *              If 0, the default text color will be used.
     * @param bold {@code true} if the text should be bold, {@code false} otherwise.
     */
    public StyleSpan(int size, int color, boolean bold) {
        this.size = 0;
        this.color = 0;
        this.size = size;
        this.color = color;
        this.bold = bold;
    }

    /**
     * Updates the paint object used for measuring the text.
     *
     * <p>This method is called by the framework when the text needs to be measured.
     * It applies the style properties (size, color, bold) to the provided {@link TextPaint} object.
     *
     * <p>If {@link #color} is not 0, it sets the text color.
     * It always sets the {@link TextPaint#linkColor} to the current text color.
     * If {@link #size} is non-negative, it sets the text size, converting dp to pixels.
     * If {@link #bold} is true, it sets the typeface to bold.
     *
     * @param textPaintToUpdate The {@link TextPaint} object to update.
     *                          This object will be used to measure the text.
     *                          It should not be null.
     * @noinspection NullableProblems
     */
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

    /**
     * Updates the draw state of the text paint.
     * <p>
     * This method is called by the rendering system to apply the span's styling
     * to the {@link TextPaint} object that will be used to draw the text.
     *
     * <p>If a color is specified (not 0), it sets the text color.
     * It also sets the link color to the current text color.
     * <p>If a size is specified (not negative), it sets the text size, converting from dp to pixels.
     * Otherwise, it keeps the existing text size.
     * <p>If the bold flag is true, it sets the typeface to bold.
     *
     * @param textPaintToUpdate The {@link TextPaint} object to update.
     */
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
