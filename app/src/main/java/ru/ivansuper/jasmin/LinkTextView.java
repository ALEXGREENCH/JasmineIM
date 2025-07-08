package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkTextView extends TextView {
    private LinkListener listener;

    public interface LinkListener {
        void onLink();
    }

    public LinkTextView(Context context) {
        super(context);
        init();
    }

    public LinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinkTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setDrawingCacheEnabled(false);
        setWillNotCacheDrawing(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.listener != null) {
            this.listener.onLink();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onMeasure(int a, int b) {
        super.onMeasure(a, b);
    }

    protected void finalize() throws Throwable {
        Log.e(getClass().getSimpleName(), "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
        super.finalize();
    }

    /** @noinspection unused*/
    public void setLinkListener(LinkListener listener) {
        this.listener = listener;
    }
}
