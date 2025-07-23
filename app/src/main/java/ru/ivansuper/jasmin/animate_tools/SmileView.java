package ru.ivansuper.jasmin.animate_tools;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.SmileysManager;

/**
 * Custom view for displaying animated smileys (emoticons).
 * <p>
 * This view handles the drawing and animation of a {@link Movie} object, which represents
 * an animated smiley. It supports custom scaling and can be configured to be either
 * temporary (adjusting its height to the smiley) or fixed (using a predefined maximum height).
 * <p>
 * Animation is achieved by using a {@link Handler} to schedule redraws at appropriate intervals.
 */
public class SmileView extends View implements Handler.Callback {

    private final Handler hdl;
    private int width = 1;
    private int height = 1;

    private Movie movie;
    private boolean temporary;

    public SmileView(Context context) {
        super(context);
        this.hdl = new Handler(this);
    }

    public SmileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.hdl = new Handler(this);
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setIsTemporary() {
        this.temporary = true;
    }

    public void setCustomScale(int value) {
        if (movie != null) {
            movie.changeScale(getContext(), value);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.width = MeasureSpec.getSize(widthMeasureSpec);

        if (movie == null) {
            this.height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            this.height = temporary ? movie.getHeight() : SmileysManager.max_height;
        }

        setMeasuredDimension(this.width, this.height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (movie != null) {
            int x = (width - movie.getWidth()) / 2;
            int y = (height - movie.getHeight()) / 2;

            movie.draw(canvas, x, y);

            if (movie.animated && PreferenceTable.ms_animated_smileys) {
                Movie.stamp = SystemClock.uptimeMillis();
                hdl.sendEmptyMessageDelayed(0, movie.minimal_refresh_rate);
            }
        }
    }

    /** @noinspection NullableProblems*/
    @Override
    public boolean handleMessage(Message msg) {
        postInvalidate();
        return false;
    }
}
