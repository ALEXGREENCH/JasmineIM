package ru.ivansuper.jasmin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ConfigListenerView extends View {
    public OnLayoutListener listener;

    public interface OnLayoutListener {
        void onNewLayout(int w, int h, int oldw, int oldh);
    }

    public ConfigListenerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), View.MeasureSpec.getSize(heightSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (this.listener != null) {
            this.listener.onNewLayout(w, h, oldw, oldh);
        }
    }
}