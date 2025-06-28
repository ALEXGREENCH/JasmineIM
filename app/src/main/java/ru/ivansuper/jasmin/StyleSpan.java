package ru.ivansuper.jasmin;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/** @noinspection unused*/
public class StyleSpan extends MetricAffectingSpan {
    private final boolean bold;
    private int color;
    private int size;

    public StyleSpan(int size, int color, boolean bold) {
        //noinspection UnusedAssignment
        this.size = 0;
        //noinspection UnusedAssignment
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
