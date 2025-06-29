package ru.ivansuper.jasmin.slide_tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.util.Vector;

import ru.ivansuper.jasmin.BitmapDrawable;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnList;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.resources;

/**
 * Simplified implementation of the original SlideSwitcher. The old version
 * relied on complex custom animations and touch handling which were fragile
 * and hard to maintain. This version keeps the public API intact but focuses
 * on providing a reliable sliding container with a simple tab panel on top.
 */
public class SlideSwitcher extends ViewGroup {
    // Animation type constants kept for compatibility. Only simple sliding is
    // implemented but callers may still query/set the type.
    public static final int ANIMATION_TYPE_CUBE = 0;
    public static final int ANIMATION_TYPE_FLIP_1 = 1;
    public static final int ANIMATION_TYPE_FLIP_2 = 2;
    public static final int ANIMATION_TYPE_FLIP_SIMPLE = 3;
    public static final int ANIMATION_TYPE_ICS = 7;
    public static final int ANIMATION_TYPE_ICS_2 = 10;
    public static final int ANIMATION_TYPE_ROTATE_1 = 4;
    public static final int ANIMATION_TYPE_ROTATE_2 = 5;
    public static final int ANIMATION_TYPE_ROTATE_3 = 6;
    public static final int ANIMATION_TYPE_ROTATE_4 = 9;
    public static final int ANIMATION_TYPE_SNAKE = 8;
    public static final int MODULATOR_SPEED = 10;

    // Configuration
    private boolean ANIMATION_RANDOMIZED = false;
    private int ANIMATION_TYPE = ANIMATION_TYPE_ROTATE_4;

    // Appearance
    private int DIVIDER_WIDTH = 0;
    private int PANEL_HEIGHT = 48;
    private boolean show_panel = true;

    // State
    public TypedArray attrs;
    public Drawable panel;
    public BitmapDrawable highlight;
    private final Vector<String> labels = new Vector<>();
    private final Vector<Object> blinks = new Vector<>();

    private final Scroller scroller;
    private int currentScreen = 0;
    private boolean wrap_mode = false;
    private int wrap_direction = 0;
    private boolean fully_locked = false;
    private boolean mIsBeingDragged = false;
    private float initialTouchX;
    private float lastTouchX;
    private boolean freezed = false;

    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int SCROLLING_TIME = 280;

    public SlideSwitcher(Context context) {
        super(context);
        scroller = new Scroller(context, new DecelerateInterpolator());
        init(context, null);
    }

    public SlideSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context, new DecelerateInterpolator());
        this.attrs = context.obtainStyledAttributes(attrs, R.styleable.SlideSwitcher);
        init(context, attrs);
    }

    //region Public API
    public void setAnimationType(int type) {
        ANIMATION_TYPE = type;
    }

    public int getAnimationType() {
        return ANIMATION_TYPE;
    }

    public void setRandomizedAnimation(boolean randomized) {
        ANIMATION_RANDOMIZED = randomized;
    }

    public void addView(View view, String label) {
        labels.add(label);
        blinks.add(null);
        if (view.getLayoutParams() == null) {
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        super.addView(view);
    }

    public void updateLabel(int idx, String value) {
        if (idx >= 0 && idx < labels.size()) {
            labels.set(idx, value);
            invalidate();
        }
    }

    public void setBlinkState(int idx, boolean enabled) {
        if (idx >= 0 && idx < blinks.size()) {
            blinks.set(idx, enabled ? new Object() : null);
            invalidate();
        }
    }

    public void setLock(boolean locked) {
        fully_locked = locked;
    }

    public void freezeInvalidating(boolean freezed) {
        this.freezed = freezed;
        invalidate();
    }

    public void togglePanel(boolean show) {
        show_panel = show;
        requestLayout();
    }

    public void updateConfig() {
        float size = PreferenceTable.clTextSize;
        float textSize = (size + ((size / 100f) * 10f)) * resources.dm.density;
        labelPaint.setTextSize(textSize);
        labelPaint.setColor(ColorScheme.getColor(49));
        PANEL_HEIGHT = (int) ((((textSize / resources.dm.density) / 100f) * 70f + (textSize / resources.dm.density)) * resources.dm.density);
        highlightPaint.setColor(ColorScheme.getColor(49));
        requestLayout();
    }

    //endregion

    private void init(Context context, AttributeSet set) {
        labelPaint.setColor(ColorScheme.getColor(49));
        labelPaint.setShadowLayer(1f, 0, 0, 0x66000000);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        highlightPaint.setColor(ColorScheme.getColor(49));
        highlight = resources.convertToMyFormat(resources.tab_highlight);
        if (highlight != null) {
            highlight.setCustomPaint(highlightPaint);
        }
        resources.attachSlidePanel(this);
        updateConfig();
    }

    //region Scrolling helpers
    private void smoothScrollTo(int x) {
        scroller.startScroll(getScrollX(), 0, x - getScrollX(), 0, SCROLLING_TIME);
        invalidate();
    }

    private void switchToNext() {
        int count = getChildCount();
        if (count == 0) return;
        int width = getWidth() + DIVIDER_WIDTH;
        if (currentScreen == count - 1) {
            currentScreen = 0;
            wrap_mode = true;
            wrap_direction = 1;
            smoothScrollTo(width * count);
        } else {
            currentScreen++;
            smoothScrollTo(currentScreen * width);
        }
    }

    private void switchToPrev() {
        int count = getChildCount();
        if (count == 0) return;
        int width = getWidth() + DIVIDER_WIDTH;
        if (currentScreen == 0) {
            currentScreen = count - 1;
            wrap_mode = true;
            wrap_direction = -1;
            smoothScrollTo(-width);
        } else {
            currentScreen--;
            smoothScrollTo(currentScreen * width);
        }
    }

    private void snapToDestination() {
        int width = getWidth() + DIVIDER_WIDTH;
        int which = (getScrollX() + (width / 2)) / width;
        currentScreen = Math.max(0, Math.min(which, getChildCount() - 1));
        smoothScrollTo(currentScreen * width);
    }
    //endregion

    //region View overrides
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), 0);
            postInvalidate();
        } else if (wrap_mode) {
            int width = getWidth() + DIVIDER_WIDTH;
            if (wrap_direction > 0) {
                scrollTo(0, 0);
            } else {
                scrollTo((getChildCount() - 1) * width, 0);
            }
            wrap_mode = false;
            wrap_direction = 0;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View child = getChildAt(currentScreen);
        if (child != null && child.dispatchKeyEvent(event)) return true;
        if (event.getAction() == KeyEvent.ACTION_DOWN && scroller.isFinished()) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && !fully_locked) {
                switchToPrev();
                return true;
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && !fully_locked) {
                switchToNext();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (wrap_mode || getChildCount() == 0) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                initialTouchX = lastTouchX = event.getX();
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                    wrap_mode = false;
                    wrap_direction = 0;
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                if (!mIsBeingDragged) {
                    if (Math.abs(event.getX() - initialTouchX) > 32 && !fully_locked) {
                        mIsBeingDragged = true;
                    }
                }
                if (mIsBeingDragged) {
                    scrollBy((int) -dx, 0);
                    lastTouchX = event.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    float diff = event.getX() - initialTouchX;
                    int width = getWidth() + DIVIDER_WIDTH;
                    if (Math.abs(diff) > width / 4f && !fully_locked) {
                        if (diff < 0) {
                            switchToNext();
                        } else {
                            switchToPrev();
                        }
                    } else {
                        snapToDestination();
                    }
                }
                break;
        }
        if (!mIsBeingDragged) {
            return super.dispatchTouchEvent(event);
        }
        MotionEvent cancel = MotionEvent.obtain(event);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(cancel);
        cancel.recycle();
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        int childHeight = height - (show_panel ? PANEL_HEIGHT : 0);
        int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(childWidthSpec, childHeightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = show_panel ? PANEL_HEIGHT : 0;
        int width = getWidth() + DIVIDER_WIDTH;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childLeft = i * width;
            child.layout(childLeft, top, childLeft + getWidth(), top + child.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!freezed) {
            super.dispatchDraw(canvas);
            if (show_panel && panel != null) {
                int width = getWidth() + DIVIDER_WIDTH;
                float scrollX = getScrollX();
                panel.setBounds((int) scrollX, 0, (int) (scrollX + width), PANEL_HEIGHT);
                panel.draw(canvas);
                drawLabels(canvas, width, scrollX);
            }
        }
    }

    private void drawLabels(Canvas canvas, int width, float scrollX) {
        float baseline = (PANEL_HEIGHT - labelPaint.ascent() - labelPaint.descent()) / 2f;
        for (int i = 0; i < labels.size(); i++) {
            float left = i * width;
            boolean blink = blinks.get(i) != null;
            if (highlight != null && i == currentScreen) {
                highlight.setBounds((int) left, 0, (int) (left + width), PANEL_HEIGHT);
                highlight.draw(canvas);
            }
            String text = labels.get(i);
            if (text == null) continue;
            labelPaint.setColor(blink ? 0xFFFFFFFF : ColorScheme.getColor(49));
            float textWidth = labelPaint.measureText(text);
            canvas.drawText(text, left + (width - textWidth) / 2f, baseline, labelPaint);
        }
    }

    //endregion

    //region Legacy methods preserved for compatibility
    public void scrollTo(int screen) {
        int count = getChildCount();
        if (count > 0 && screen < count) {
            if (!scroller.isFinished()) scroller.abortAnimation();
            wrap_mode = false;
            wrap_direction = 0;
            currentScreen = screen;
            super.scrollTo((getWidth() + DIVIDER_WIDTH) * screen, 0);
        }
    }

    @Override
    public void removeViewAt(int idx) {
        int child_count = getChildCount();
        if (child_count > 0 && idx < child_count) {
            super.removeViewAt(idx);
            labels.remove(idx);
            blinks.remove(idx);
            if (idx < currentScreen) {
                currentScreen--;
                scrollTo(currentScreen);
            } else if (idx == currentScreen && child_count > 1) {
                currentScreen--;
                scrollTo(currentScreen);
            }
        }
    }

    public void clearupCaches() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof MultiColumnList) {
                try {
                    ((MultiColumnList) child).clearup();
                } catch (Exception ignored) {
                }
            }
        }
    }
    //endregion
}
