package ru.ivansuper.jasmin.animate_tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import ru.ivansuper.jasmin.color_editor.ColorScheme;

/**
 * A custom {@link ReplacementSpan} that draws a {@link Movie} (animated GIF)
 * within a text view. It can optionally draw a background color behind the movie.
 *
 * <p>This class handles the drawing and sizing of the movie within the text layout.
 * It also provides methods to query properties of the movie, such as its dimensions,
 * animation status, and minimal refresh rate.
 */
public class MySpan extends ReplacementSpan {

    private int left;
    private int top;
    private final Movie movie;
    private final Paint paint_;

    public MySpan(Movie movie, boolean back_color) {
        this.movie = movie;
        if (back_color) {
            this.paint_ = new Paint();
            this.paint_.setColor(ColorScheme.getColor(41));
            this.paint_.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            this.paint_ = null;
        }
    }

    public MySpan(Movie movie, boolean back_color, int height) {
        this.movie = movie;
        this.movie.recomputeSize(height);
        if (back_color) {
            this.paint_ = new Paint();
            this.paint_.setColor(ColorScheme.getColor(41));
            this.paint_.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            this.paint_ = null;
        }
    }

    /** @noinspection NullableProblems*/
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        if (movie != null) {
            this.left = (int) x;
            this.top = bottom - movie.height;
            if (paint_ != null) {
                float rectTop = top + paint.getFontMetricsInt().leading;
                canvas.drawRect(x, rectTop, x + movie.width, this.top + movie.height, paint_);
            }
            movie.draw(canvas, x, this.top);
        }
    }

    /** @noinspection NullableProblems*/
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null) {
            fm.ascent = -movie.getHeight();
            fm.descent = 0;
            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return movie.getWidth();
    }

    public int getMinimalRefreshRate() {
        return movie.minimal_refresh_rate;
    }

    public boolean animated() {
        return movie.animated;
    }

    public int getWidth() {
        return movie.getWidth();
    }

    public int getHeight() {
        return movie.getHeight();
    }

    public int getLeft() {
        return this.left;
    }

    public int getTop() {
        return this.top;
    }
}
