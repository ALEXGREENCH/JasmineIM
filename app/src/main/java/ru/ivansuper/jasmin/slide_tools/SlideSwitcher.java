package ru.ivansuper.jasmin.slide_tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.util.Vector;

import ru.ivansuper.jasmin.BitmapDrawable;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnList;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.animate_tools.Transform;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.resources;

public class SlideSwitcher extends ViewGroup {
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_CUBE = 0;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_FLIP_1 = 1;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_FLIP_2 = 2;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_FLIP_SIMPLE = 3;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_ICS = 7;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_ICS_2 = 10;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_ROTATE_1 = 4;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_ROTATE_2 = 5;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_ROTATE_3 = 6;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_ROTATE_4 = 9;
    /** @noinspection unused*/
    public static final int ANIMATION_TYPE_SNAKE = 8;
    /** @noinspection unused*/
    public static final int MODULATOR_SPEED = 10;
    private boolean ANIMATION_RANDOMIZED;
    private int ANIMATION_TYPE;
    private int DIVIDER_WIDTH;
    private float FADING_LENGTH;
    private int PANEL_HEIGHT;
    private final int SCROLLING_TIME;
    private boolean animation;
    public TypedArray attrs;
    private final Vector<Object> blinks;
    private int currentScreen;
    private int direction_;
    private Paint effect;
    private Shader fade_shader;
    private Paint fade_shader_;
    private Matrix fade_shader_m;
    private boolean freezed;
    private boolean fully_locked;
    public BitmapDrawable highlight;
    private Paint highlight_;
    private final Vector<String> labels;
    private TextPaint labels_;
    private float lastTouchX;
    private float lastTouchY;
    private float initialTouchX;
    private float initialTouchY;
    private boolean locked;
    private boolean mIsBeingDragged;
    public Drawable panel;
    private float scrollX;
    private Scroller scroller;
    private boolean show_panel;
    private int text_color;
    private int value_;
    private int wrap_direction;
    private boolean wrap_mode;

    public void setAnimationType(int type) {
        this.ANIMATION_TYPE = type;
        invalidate();
    }

    /** @noinspection unused*/
    public int getAnimationType() {
        return this.ANIMATION_TYPE;
    }

    public void setRandomizedAnimation(boolean randomized) {
        this.ANIMATION_RANDOMIZED = randomized;
    }

    public SlideSwitcher(Context context) {
        super(context);
        this.ANIMATION_TYPE = 9;
        this.ANIMATION_RANDOMIZED = false;
        this.value_ = 0;
        this.direction_ = 10;
        this.locked = false;
        this.fully_locked = false;
        this.labels = new Vector<>();
        this.blinks = new Vector<>();
        this.PANEL_HEIGHT = 48;
        this.DIVIDER_WIDTH = 1;
        this.SCROLLING_TIME = 280;
        this.wrap_mode = false;
        this.wrap_direction = 0;
        this.FADING_LENGTH = 16.0f;
        this.initialTouchX = 0f;
        this.initialTouchY = 0f;
        init(context);
    }

    public SlideSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ANIMATION_TYPE = 9;
        this.ANIMATION_RANDOMIZED = false;
        this.value_ = 0;
        this.direction_ = 10;
        this.locked = false;
        this.fully_locked = false;
        this.labels = new Vector<>();
        this.blinks = new Vector<>();
        this.PANEL_HEIGHT = 48;
        this.DIVIDER_WIDTH = 1;
        this.SCROLLING_TIME = 280;
        this.wrap_mode = false;
        this.wrap_direction = 0;
        this.FADING_LENGTH = 16.0f;
        this.attrs = context.obtainStyledAttributes(attrs, R.styleable.SlideSwitcher);
        this.initialTouchX = 0f;
        this.initialTouchY = 0f;
        init(context);
    }

    public void addView(View view, String label) {
        this.labels.add(label);
        this.blinks.add(null);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (lp == null) {
            view.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        }
        super.addView(view);
    }

    public void updateLabel(int idx, String value) {
        if (idx >= 0 && idx < this.labels.size()) {
            this.labels.set(idx, value);
            invalidate();
        }
    }

    public void setBlinkState(int idx, boolean enabled) {
        if (idx >= 0 && idx < this.labels.size()) {
            this.blinks.set(idx, enabled ? new Object() : null);
            invalidate();
        }
    }

    public void setLock(boolean locked) {
        this.fully_locked = locked;
    }

    /** @noinspection unused*/
    public void freezeInvalidating(boolean freezed) {
        this.freezed = freezed;
        invalidate();
    }

    public void togglePanel(boolean show) {
        this.show_panel = show;
        requestLayout();
    }

    private void init(Context context) {
        setWillNotDraw(true);
        setDrawingCacheEnabled(false);
        setWillNotCacheDrawing(true);
        setStaticTransformationsEnabled(true);
        this.labels_ = new TextPaint();
        this.labels_.setColor(-1);
        this.labels_.setShadowLayer(1.0f, 0.0f, 0.0f, -13421773);
        this.labels_.setAntiAlias(true);
        this.labels_.setStrokeWidth(3.4f);
        this.effect = new TextPaint();
        this.effect.setAntiAlias(true);
        this.effect.setStyle(Paint.Style.STROKE);
        this.effect.setStrokeWidth(4.0f);
        this.effect.setAlpha(192);
        this.text_color = ColorScheme.getColor(49);
        this.panel = getContext().getResources().getDrawable(R.drawable.slide_switcher_panel);
        this.highlight = resources.convertToMyFormat(resources.tab_highlight);
        this.highlight_ = new Paint(2);
        this.highlight.setCustomPaint(this.highlight_);
        resources.attachSlidePanel(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(-1, -1);
        setLayoutParams(lp);
        if (resources.OS_VERSION <= 15) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        this.scroller = new Scroller(context, new DecelerateInterpolator());
        this.FADING_LENGTH *= resources.dm.density;
        this.fade_shader = new LinearGradient(0.0f, 0.0f, 0.0f, this.FADING_LENGTH, -1, 16777215, Shader.TileMode.CLAMP);
        this.fade_shader_ = new Paint();
        this.fade_shader_.setShader(this.fade_shader);
        this.fade_shader_.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        this.fade_shader_m = new Matrix();
        updateConfig();
    }

    public void updateConfig() {
        float size = PreferenceTable.clTextSize;
        float size2 = size + ((size / 100.0f) * 10.0f);
        this.labels_.setTextSize(resources.dm.density * size2);
        this.effect.setColor(ColorScheme.getColor(49));
        this.effect.setAlpha(160);
        this.effect.setTextSize(this.labels_.getTextSize());
        this.highlight_.setColorFilter(new LightingColorFilter(0, ColorScheme.getColor(49)));
        this.PANEL_HEIGHT = (int) ((((size2 / 100.0f) * 70.0f) + size2) * resources.dm.density);
        this.DIVIDER_WIDTH = (int) (0.0f * resources.dm.density);
        requestLayout();
    }

    private void handleAnimationStart() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.setDrawingCacheEnabled(true);
                child.setWillNotCacheDrawing(false);
            }
        }
    }

    private void handleAnimationEnd() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.setDrawingCacheEnabled(false);
                child.setWillNotCacheDrawing(true);
                // Make sure hardware layers do not keep a faded copy
                // of the view after the animation is finished
                child.setAlpha(1f);
            }
        }
    }

    private void setAnimationState(boolean active) {
        if (active) {
            if (!this.animation) {
                handleAnimationStart();
                this.animation = true;
                return;
            }
            return;
        }
        if (this.animation) {
            handleAnimationEnd();
            this.animation = false;
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), 0);
            postInvalidate();
            return;
        }

        if (wrap_mode) {
            int width = getWidth() + DIVIDER_WIDTH;

            if (wrap_direction > 0) {
                // переход с последнего на 0
                super.scrollTo(0, 0);
                currentScreen = 0;
            } else if (wrap_direction < 0) {
                // переход с 0 на последний
                int last = getChildCount() - 1;
                super.scrollTo(last * width, 0);
                currentScreen = last;
            }

            wrap_mode = false;
            wrap_direction = 0;
            setAnimationState(false);
            postInvalidate();
        } else {
            int width = getWidth() + DIVIDER_WIDTH;
            if (width > 0) {
                int newScreen = Math.round((float) getScrollX() / width);
                if (newScreen < 0) {
                    newScreen = 0;
                }
                int childCount = getChildCount();
                if (childCount > 0 && newScreen >= childCount) {
                    newScreen = childCount - 1;
                }
                currentScreen = newScreen;
            }
            setAnimationState(false);
        }
    }

    private void wrapToFirst() {
        wrap_mode = true;
        wrap_direction = 1;
        int width = getWidth() + DIVIDER_WIDTH;
        scroller.startScroll(getScrollX(), 0, width, 0, SCROLLING_TIME);
        setAnimationState(true);
        invalidate();
    }

    private void wrapToLast() {
        wrap_mode = true;
        wrap_direction = -1;
        int width = getWidth() + DIVIDER_WIDTH;
        scroller.startScroll(getScrollX(), 0, -width, 0, SCROLLING_TIME);
        setAnimationState(true);
        invalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        View child = getChildAt(this.currentScreen);
        if (child == null) {
            return false;
        }
        boolean handled = child.dispatchKeyEvent(event);
        if (!handled && event.getAction() == 0 && this.scroller.isFinished()) {
            if (event.getKeyCode() == 21 && !this.fully_locked) {
                switchToPrev();
                return true;
            }
            if (event.getKeyCode() == 22 && !this.fully_locked) {
                switchToNext();
                return true;
            }
        }
        Log.e("KEY_EVENT", "CODE: " + event.getKeyCode() + "     EVENT: " + event.getAction() + "     HANDLED:" + handled);
        return handled;
    }

    private void switchToNext() {
        if (fully_locked) {
            smoothScrollToCurrent();
            return;
        }
        int count = getChildCount();
        if (count == 0) return;

        int width = getWidth() + DIVIDER_WIDTH;
        if (currentScreen == count - 1) {
            currentScreen = 0;
            wrapToFirst();
        } else {
            currentScreen++;
            int targetScroll = currentScreen * width;
            scroller.startScroll(getScrollX(), 0, targetScroll - getScrollX(), 0, SCROLLING_TIME);
            setAnimationState(true);
            invalidate();
        }
    }

    private void switchToPrev() {
        if (fully_locked) {
            smoothScrollToCurrent();
            return;
        }
        int count = getChildCount();
        if (count == 0) return;

        int width = getWidth() + DIVIDER_WIDTH;
        if (currentScreen == 0) {
            currentScreen = count - 1;
            wrapToLast();
        } else {
            currentScreen--;
            int targetScroll = currentScreen * width;
            scroller.startScroll(getScrollX(), 0, targetScroll - getScrollX(), 0, SCROLLING_TIME);
            setAnimationState(true);
            invalidate();
        }
    }

    private void smoothScrollToCurrent() {
        int width = getWidth() + this.DIVIDER_WIDTH;
        int target = this.currentScreen * width;
        this.scroller.startScroll(getScrollX(), 0, target - getScrollX(), 0, this.SCROLLING_TIME);
        setAnimationState(true);
        postInvalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.wrap_mode || getChildCount() == 0) {
            return false;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.locked = false;
                this.initialTouchX = event.getX();
                this.initialTouchY = event.getY();
                this.lastTouchX = event.getX();
                this.lastTouchY = event.getY();
                if (!this.scroller.isFinished()) {
                    this.scroller.abortAnimation();
                    this.wrap_mode = false;
                    this.wrap_direction = 0;
                    setAnimationState(false);
                    this.mIsBeingDragged = true;
                } else {
                    this.mIsBeingDragged = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - this.lastTouchX;
                float totalDx = event.getX() - this.initialTouchX;
                float dy = Math.abs(event.getY() - this.initialTouchY);
                if (!this.mIsBeingDragged) {
                    if (Math.abs(totalDx) > 32.0f && dy < 32.0f && !this.fully_locked) {
                        this.mIsBeingDragged = true;
                        setAnimationState(true);
                    } else if (dy > 32.0f) {
                        this.locked = true;
                    }
                }
                if (this.mIsBeingDragged) {
                    scrollBy((int) (-dx), 0);
                    this.lastTouchX = event.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (this.mIsBeingDragged) {
                    this.mIsBeingDragged = false;
                    float diff = event.getX() - this.initialTouchX;
                    int width = getWidth() + this.DIVIDER_WIDTH;
                    if (Math.abs(diff) > width / 4f && !this.fully_locked) {
                        if (diff < 0) {
                            switchToNext();
                        } else {
                            switchToPrev();
                        }
                    } else {
                        smoothScrollToCurrent();
                    }
                }
                break;
        }
        if (!this.mIsBeingDragged) {
            return super.dispatchTouchEvent(event);
        }
        MotionEvent cancel = MotionEvent.obtain(event);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(cancel);
        cancel.recycle();
        return true;
    }

    @Override // android.view.View
    public void onMeasure(int a, int b) {
        int width = View.MeasureSpec.getSize(a);
        int height = View.MeasureSpec.getSize(b);
        setMeasuredDimension(width, height);
    }

    @Override // android.view.View
    protected final void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scrollTo(this.currentScreen * (getWidth() + this.DIVIDER_WIDTH), 0);
        requestLayout();
    }

    /** @noinspection unused*/
    private void prepareModulator() {
        this.value_ += this.direction_;
        if (this.value_ > 255) {
            this.value_ = 255;
            this.direction_ = -10;
        }
        if (this.value_ < 0) {
            this.value_ = 0;
            this.direction_ = 10;
        }
        this.effect.setAlpha(this.value_);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
    }

    private boolean isInDisplay(View child) {
        Rect rect = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        int scrollx = getScrollX();
        Rect display = new Rect(scrollx, 0, getWidth() + scrollx, getHeight());
        return rect.intersect(display);
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        final int scrollX = getScrollX();
        final int childIndex = indexOfChild(child);
        final int childCount = getChildCount();
        final int childWidth = getWidth() + DIVIDER_WIDTH;

        boolean isFirst = (childIndex == 0);
        boolean isLast = (childIndex == childCount - 1);
        boolean wrapLeft = scrollX < 0 && isLast;
        boolean wrapRight = scrollX > (childWidth * (childCount - 1)) && isFirst;

        int wrapOffset = 0;
        if (wrapLeft) wrapOffset = -childCount * childWidth;
        if (wrapRight) wrapOffset = childCount * childWidth;

        int childCenter = childIndex * childWidth + wrapOffset;
        int distance = scrollX - childCenter;

        t.clear();
        Matrix matrix = t.getMatrix();
        switch (ANIMATION_TYPE) {
            case ANIMATION_TYPE_CUBE:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCube(matrix, getWidth(), getHeight(), distance * 180f / childWidth, distance);
                break;
            case ANIMATION_TYPE_FLIP_1:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCubeInv(matrix, getWidth(), getHeight(), distance * 180f / childWidth, distance);
                break;
            case ANIMATION_TYPE_FLIP_2:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyTransformationFlip2(distance * 180f / childWidth, getWidth() / 2f, getHeight() / 2f, matrix);
                break;
            case ANIMATION_TYPE_ROTATE_1:
                matrix.postRotate(distance * 90f / childWidth, getWidth() / 2f, getHeight() / 2f);
                break;
            case ANIMATION_TYPE_ICS:
                float alpha = 1f - Math.abs((float) distance / childWidth);
                t.setAlpha(alpha);
                t.setTransformationType(Transformation.TYPE_BOTH);
                matrix.postTranslate(distance, 0f);
                float scale = 1f - Math.abs(distance) / (7f * childWidth);
                matrix.postScale(scale, scale, getWidth() / 2f, getHeight() / 2f);
                break;
            default:
                matrix.postTranslate(distance, 0f);
                break;
        }

        matrix.postTranslate(wrapOffset, 0f);
        return true;
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int scrollx = getScrollX();
        int child_count = getChildCount();
        boolean wrap_to_end = scrollx < 0;
        int total_width = (getWidth() + this.DIVIDER_WIDTH) * child_count;
        boolean wrap_to_start = scrollx > total_width - getWidth();
        int child_idx = indexOfChild(child);
        boolean it_is_last = child_idx == child_count + (-1);
        boolean it_is_first = child_idx == 0;
        boolean wrap = (wrap_to_end && it_is_last) || (wrap_to_start && it_is_first);
        if (isInDisplay(child) || wrap) {
            return super.drawChild(canvas, child, drawingTime);
        }
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (freezed) return;

        super.dispatchDraw(canvas);

        if (!show_panel) return;

        final int childCount = getChildCount();
        final int labelCount = labels.size();
        final int width = getWidth();
        final float halfWidth = width / 2f;
        final float fullWidth = width + DIVIDER_WIDTH;
        final float scrollX = getScrollX();
        final float fontHeight = labels_.getFontMetrics().bottom - labels_.getFontMetrics().top;
        final float centerY = PANEL_HEIGHT / 2f + fontHeight / 2f;

        panel.setBounds((int) scrollX, 0, (int) (scrollX + fullWidth), PANEL_HEIGHT);
        panel.draw(canvas);

        int save = canvas.saveLayer(scrollX, 0, scrollX + fullWidth, PANEL_HEIGHT, null, Canvas.ALL_SAVE_FLAG);

        // Отрисовка лейблов (в т.ч. wrap left/right)
        for (int i = -2; i <= labelCount + 1; i++) {
            int index = i;

            boolean isWrapped = false;
            if (index < 0) {
                index = labelCount + index;
                isWrapped = true;
            } else if (index >= labelCount) {
                index = index - labelCount;
                isWrapped = true;
            }

            if (index < 0 || index >= labelCount) continue;

            String label = labels.get(index);
            if (label == null || label.isEmpty()) continue;

            boolean blink = blinks.get(index) != null;
            float screenX = (i * halfWidth) + (scrollX / 2f);
            float textWidth = labels_.measureText(label);
            float textLeft = screenX + halfWidth - textWidth / 2f;

            if (textLeft + textWidth < scrollX || textLeft > scrollX + width) continue;

            float dx = ((scrollX + halfWidth) - textWidth / 2f) - textLeft;
            int alpha = 255 - (int) ((Math.abs(dx) * 255f) / (0.65f * width));
            alpha = Math.max(0, Math.min(255, alpha));

            // highlight
            highlight.setBounds((int) screenX, 0, (int) (screenX + fullWidth), PANEL_HEIGHT);
            highlight_.setAlpha(alpha);
            highlight.draw(canvas);

            // Тень
            labels_.setStrokeWidth(blink ? 4f : 1f);
            labels_.setColor(0xFF000000); // черный
            labels_.setAlpha(alpha);
            labels_.setStyle(Paint.Style.STROKE);
            canvas.drawText(label, textLeft, centerY, labels_);

            // Основной текст
            labels_.setColor(blink ? 0xFFFFFFFF : text_color);
            labels_.setAlpha(alpha);
            labels_.setStyle(Paint.Style.FILL);
            canvas.drawText(label, textLeft, centerY, labels_);
        }

        // Левая затухающая маска
        fade_shader_m.setRotate(-90f);
        fade_shader.setLocalMatrix(fade_shader_m);
        canvas.translate(scrollX, 0);
        canvas.drawRect(0, 0, FADING_LENGTH, PANEL_HEIGHT, fade_shader_);

        // Правая затухающая маска
        fade_shader_m.setRotate(90f);
        fade_shader_m.postTranslate(FADING_LENGTH, 0);
        fade_shader.setLocalMatrix(fade_shader_m);
        canvas.translate(width - FADING_LENGTH, 0);
        canvas.drawRect(0, 0, FADING_LENGTH, PANEL_HEIGHT, fade_shader_);

        canvas.restoreToCount(save);

        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = this.show_panel ? this.PANEL_HEIGHT : 0;
        int count = getChildCount();
        int height = getHeight() - (this.show_panel ? this.PANEL_HEIGHT : 0);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int width = getWidth() + this.DIVIDER_WIDTH;
            child.measure(View.MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            child.layout((width * i) + l, top, (width * i) + r, getHeight());
        }
    }

    public void scrollTo(int screen) {
        int count = getChildCount();
        if (count == 0 || screen >= count) return;

        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }

        wrap_mode = false;
        wrap_direction = 0;
        setAnimationState(false);

        int target = screen * (getWidth() + DIVIDER_WIDTH);
        super.scrollTo(target, 0);
        currentScreen = screen;
        int child_count = getChildCount();
        if (child_count > 0 && screen < child_count) {
            if (!this.scroller.isFinished()) {
                this.scroller.abortAnimation();
            }
            this.wrap_mode = false;
            this.wrap_direction = 0;
            setAnimationState(false);
            if (getWidth() == 0) {
                final int scr = screen;
                post(() -> scrollTo(scr));
            } else {
                super.scrollTo((getWidth() + this.DIVIDER_WIDTH) * screen, 0);
                this.currentScreen = screen;
            }
        }
    }

    @Override
    public void removeViewAt(int idx) {
        int child_count = getChildCount();
        if (child_count > 0 && idx < child_count) {
            super.removeViewAt(idx);
            this.labels.remove(idx);
            this.blinks.remove(idx);
            if (idx < this.currentScreen) {
                this.currentScreen--;
                if (this.scroller.isFinished()) {
                    scrollTo(this.currentScreen * (getWidth() + this.DIVIDER_WIDTH), 0);
                } else {
                    scrollTo((this.currentScreen - 1) * (getWidth() + this.DIVIDER_WIDTH), 0);
                }
                return;
            }
            if (idx == this.currentScreen && child_count > 1) {
                this.currentScreen--;
                if (this.scroller.isFinished()) {
                    scrollTo(this.currentScreen * (getWidth() + this.DIVIDER_WIDTH), 0);
                } else {
                    scrollTo((this.currentScreen - 1) * (getWidth() + this.DIVIDER_WIDTH), 0);
                }
            }
        }
    }

    public void clearupCaches() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            try {
                ((MultiColumnList) child).clearup();
            } catch (Exception ignored) {
            }
        }
    }
}
