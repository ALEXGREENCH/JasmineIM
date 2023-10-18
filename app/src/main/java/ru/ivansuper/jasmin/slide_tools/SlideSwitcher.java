package ru.ivansuper.jasmin.slide_tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
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

import java.util.Random;
import java.util.Vector;

import ru.ivansuper.jasmin.BitmapDrawable;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnList;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.animate_tools.Transform;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.resources;

public class SlideSwitcher extends ViewGroup {
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_CUBE = 0;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_FLIP_1 = 1;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_FLIP_2 = 2;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_FLIP_SIMPLE = 3;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_ICS = 7;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_ICS_2 = 10;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_ROTATE_1 = 4;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_ROTATE_2 = 5;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_ROTATE_3 = 6;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_ROTATE_4 = 9;
    /**
     * @noinspection unused
     */
    public static final int ANIMATION_TYPE_SNAKE = 8;
    /**
     * @noinspection unused
     */
    public static final int MODULATOR_SPEED = 10;
    private boolean ANIMATION_RANDOMIZED = false;
    private int ANIMATION_TYPE = 9;
    private int DIVIDER_WIDTH = 1;
    private float FADING_LENGTH = 16.0F;
    private int PANEL_HEIGHT = 48;
    private final int SCROLLING_TIME = 280;
    private boolean animation;
    public TypedArray attrs;
    /**
     * @noinspection FieldMayBeFinal
     */
    private Vector<Object> blinks = new Vector();
    private int currentScreen;
    private int direction_ = 10;
    private Paint effect;
    private Shader fade_shader;
    private Paint fade_shader_;
    private Matrix fade_shader_m;
    private boolean freezed;
    private boolean fully_locked = false;
    public BitmapDrawable highlight;
    private Paint highlight_;
    private final Vector<String> labels = new Vector();
    private TextPaint labels_;
    private float lastTouchX;
    private float lastTouchY;
    private boolean locked = false;
    private boolean mIsBeingDragged;
    public Drawable panel;
    private float scrollX;
    private Scroller scroller;
    private boolean show_panel;
    private int text_color;
    private int value_ = 0;
    private int wrap_direction = 0;
    private boolean wrap_mode = false;

    public SlideSwitcher(Context context) {
        super(context);
        this.init(context);
    }

    public SlideSwitcher(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.attrs = context.obtainStyledAttributes(attributeSet,
                new int[]{
                        android.R.attr.scrollbarSize,
                        android.R.attr.scrollbarThumbHorizontal,
                        android.R.attr.scrollbarThumbVertical,
                        android.R.attr.scrollbarTrackHorizontal,
                        android.R.attr.scrollbarTrackVertical,
                        android.R.attr.scrollbarAlwaysDrawHorizontalTrack,
                        android.R.attr.scrollbarAlwaysDrawVerticalTrack,
                        android.R.attr.scrollbarStyle,
                        android.R.attr.id,
                        android.R.attr.tag,
                        android.R.attr.scrollX,
                        android.R.attr.scrollY,
                        android.R.attr.background,
                        android.R.attr.padding,
                        android.R.attr.paddingLeft,
                        android.R.attr.paddingTop,
                        android.R.attr.paddingRight,
                        android.R.attr.paddingBottom,
                        android.R.attr.focusable,
                        android.R.attr.focusableInTouchMode,
                        android.R.attr.visibility,
                        android.R.attr.fitsSystemWindows,
                        android.R.attr.scrollbars,
                        android.R.attr.fadingEdge,
                        android.R.attr.fadingEdgeLength,
                        android.R.attr.nextFocusLeft,
                        android.R.attr.nextFocusRight,
                        android.R.attr.nextFocusUp,
                        android.R.attr.nextFocusDown,
                        android.R.attr.clickable,
                        android.R.attr.longClickable,
                        android.R.attr.saveEnabled,
                        android.R.attr.drawingCacheQuality,
                        android.R.attr.duplicateParentState,
                        android.R.attr.minWidth,
                        android.R.attr.minHeight,
                        android.R.attr.soundEffectsEnabled,
                        android.R.attr.keepScreenOn,
                        android.R.attr.isScrollContainer,
                        android.R.attr.hapticFeedbackEnabled,
                        android.R.attr.onClick,
                        android.R.attr.contentDescription,
                        android.R.attr.scrollbarFadeDuration,
                        android.R.attr.scrollbarDefaultDelayBeforeFade,
                        android.R.attr.fadeScrollbars
                });
        this.init(context);
    }

    /**
     * Обработка завершения анимации для дочерних элементов.
     */
    private void handleAnimationEnd() {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; ++i) {
            View childView = getChildAt(i);
            if (childView != null) {
                childView.setDrawingCacheEnabled(false);
                childView.setWillNotCacheDrawing(true);
            }
        }
    }

    /**
     * Обработка начала анимации для дочерних элементов.
     */
    private void handleAnimationStart() {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; ++i) {
            View childView = getChildAt(i);
            if (childView != null) {
                childView.setDrawingCacheEnabled(true);
                childView.setWillNotCacheDrawing(false);
            }
        }
    }

    private void init(Context context) {
        // Инициализация виджета
        this.setWillNotDraw(true);
        this.setDrawingCacheEnabled(false);
        this.setWillNotCacheDrawing(true);
        this.setStaticTransformationsEnabled(true);

        // Инициализация текстовых стилей
        this.labels_ = new TextPaint();
        this.labels_.setColor(-1);
        this.labels_.setShadowLayer(1.0F, 0.0F, 0.0F, -13421773);
        this.labels_.setAntiAlias(true);
        this.labels_.setStrokeWidth(3.4F);

        this.effect = new TextPaint();
        this.effect.setAntiAlias(true);
        this.effect.setStyle(Style.STROKE);
        this.effect.setStrokeWidth(4.0F);
        this.effect.setAlpha(192);
        this.text_color = ColorScheme.getColor(49);

        // Инициализация ресурсов
        this.panel = getResources().getDrawable(R.drawable.slide_switcher_panel);
        this.highlight = resources.convertToMyFormat(resources.tab_highlight);
        this.highlight_ = new Paint(2);
        this.highlight.setCustomPaint(this.highlight_);
        resources.attachSlidePanel(this);

        // Инициализация анимации и эффектов
        this.scroller = new Scroller(context, new DecelerateInterpolator());
        this.FADING_LENGTH *= resources.dm.density;
        this.fade_shader = new LinearGradient(0.0F, 0.0F, 0.0F, this.FADING_LENGTH, -1, 16777215, TileMode.CLAMP);
        this.fade_shader_ = new Paint();
        this.fade_shader_.setShader(this.fade_shader);
        this.fade_shader_.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        this.fade_shader_m = new Matrix();

        // Установка параметров и конфигурации
        this.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.updateConfig();
    }

    /**
     * Проверяет, находится ли указанное представление в пределах видимой области этого виджета.
     *
     * @param viewToCheck Представление, которое нужно проверить.
     * @return true, если представление находится в пределах видимой области, иначе false.
     */
    private boolean isInDisplay(View viewToCheck) {
        // Создаем прямоугольник, представляющий границы проверяемого представления.
        Rect viewBounds = new Rect(
                viewToCheck.getLeft(),
                viewToCheck.getTop(),
                viewToCheck.getRight(),
                viewToCheck.getBottom()
        );

        // Получаем текущий горизонтальный сдвиг виджета.
        int currentScrollX = this.getScrollX();

        // Создаем прямоугольник, представляющий видимую область виджета.
        Rect displayBounds = new Rect(
                currentScrollX,
                0,
                this.getWidth() + currentScrollX,
                this.getHeight()
        );

        // Проверяем, пересекаются ли границы представления и видимой области.
        return viewBounds.intersect(displayBounds);
    }


    /**
     * @noinspection unused
     */
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

    private void setAnimationState(boolean var1) {
        if (var1) {
            if (!this.animation) {
                this.handleAnimationStart();
                this.animation = true;
            }
        } else if (this.animation) {
            this.handleAnimationEnd();
            this.animation = false;
        }

    }

    private final void switchToNext() {
        if (!fully_locked) {
            if (currentScreen == getChildCount() - 1) {
                currentScreen = 0;
                wrapToFirst();
            } else {
                ++currentScreen;
                scroller.startScroll(getScrollX(), 0, 0, 0, SCROLLING_TIME);
                scroller.setFinalX(currentScreen * (getWidth() + DIVIDER_WIDTH));
                postInvalidate();
            }
        }

    }

    private void switchToPrev() {
        if (!fully_locked) {
            if (currentScreen == 0) {
                currentScreen = getChildCount() - 1;
                wrapToLast();
            } else {
                --currentScreen;
                scroller.startScroll(getScrollX(), 0, 0, 0, SCROLLING_TIME);
                scroller.setFinalX(currentScreen * (getWidth() + DIVIDER_WIDTH));
                postInvalidate();
            }
        }

    }

    private void wrapToFirst() {
        wrap_mode = true;
        wrap_direction = 1;
        scroller.startScroll(getScrollX(), 0, 0, 0, SCROLLING_TIME);
        int var1 = getWidth();
        int var2 = DIVIDER_WIDTH;
        scroller.setFinalX(getChildCount() * (var1 + var2));
        postInvalidate();
    }

    private void wrapToLast() {
        wrap_mode = true;
        wrap_direction = -1;
        scroller.startScroll(getScrollX(), 0, 0, 0, SCROLLING_TIME);
        scroller.setFinalX(-(getWidth() + DIVIDER_WIDTH));
        postInvalidate();
    }

    public void addView(View var1, String var2) {
        labels.add(var2);
        blinks.add((Object) null);
        if ((LinearLayout.LayoutParams) var1.getLayoutParams() == null) {
            var1.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        }

        super.addView(var1);
    }

    /**
     * Очищает кешированные данные во всех дочерних элементах MultiColumnList.
     * Этот метод обходит все дочерние элементы и вызывает их метод clearup() для очистки кешей.
     */
    public void clearupCaches() {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);

            try {
                // Проверяем, является ли дочерний элемент MultiColumnList
                if (child instanceof MultiColumnList) {
                    // Вызываем метод clearup() для очистки кешей
                    ((MultiColumnList) child).clearup();
                }
            } catch (Exception e) {
                // Обрабатываем исключение, если произошла ошибка при очистке
                e.printStackTrace();
            }
        }
    }

    public void computeScroll() {
        if (!mIsBeingDragged) {
            if (scroller.computeScrollOffset()) {
                scrollTo(scroller.getCurrX(), 0);
                postInvalidate();
            } else if (wrap_mode) {
                if (wrap_direction < 0) {
                    int var1 = getWidth();
                    int var2 = DIVIDER_WIDTH;
                    scrollTo((getChildCount() - 1) * (var1 + var2), 0);
                    wrap_direction = 0;
                    setAnimationState(false);
                } else if (wrap_direction > 0) {
                    scrollTo(0, 0);
                    postInvalidate();
                    wrap_direction = 0;
                    setAnimationState(false);
                } else {
                    wrap_mode = false;
                }
            } else {
                setAnimationState(false);
            }
        }

    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (!freezed) {
            int childCount = getChildCount();
            super.dispatchDraw(canvas);
            if (show_panel) {
                float scrollX = (float) getScrollX();
                float childWidthWithDivider = (float) (getWidth() + DIVIDER_WIDTH);
                float childHalfWidthWithDivider = (float) ((getWidth() + DIVIDER_WIDTH) / 2);
                panel.setBounds((int) scrollX, 0, (int) (scrollX + childWidthWithDivider), PANEL_HEIGHT);
                panel.draw(canvas);
                int saveCount = canvas.saveLayer(scrollX, 0.0F, scrollX + childWidthWithDivider, (float) PANEL_HEIGHT, (Paint) null, Canvas.ALL_SAVE_FLAG);
                float labelBaseline = (float) (-labels_.getFontMetricsInt().ascent - labels_.getFontMetricsInt().descent);
                int labelCount = labels.size();
                int labelIndex = -2;

                while (true) {
                    if (labelIndex > labelCount + 1) {
                        fade_shader_m.setRotate(-90.0F);
                        fade_shader.setLocalMatrix(fade_shader_m);
                        canvas.translate(scrollX, 0.0F);
                        canvas.drawRect(0.0F, 0.0F, FADING_LENGTH, (float) PANEL_HEIGHT, fade_shader_);
                        fade_shader_m.setRotate(90.0F);
                        fade_shader_m.postTranslate(FADING_LENGTH, 0.0F);
                        canvas.translate((float) getWidth() - FADING_LENGTH, 0.0F);
                        fade_shader.setLocalMatrix(fade_shader_m);
                        canvas.drawRect(0.0F, 0.0F, FADING_LENGTH, (float) PANEL_HEIGHT, fade_shader_);
                        canvas.restoreToCount(saveCount);
                        break;
                    }

                    label107:
                    {
                        String var10 = null;
                        boolean var11 = false;
                        int var12 = getScrollX();
                        float var13 = (float) (var12 / 2) + (float) labelIndex * childHalfWidthWithDivider;
                        if (labelIndex == -1) {
                            if (childCount == 1) {
                                break label107;
                            }

                            var10 = (String) labels.get(labelCount - 1);
                            var11 = blinks.get(labelCount - 1) != null;

                            var12 = getScrollX();
                            var13 = (float) (var12 / 2) - childHalfWidthWithDivider;
                        } else if (labelIndex >= 0 && labelIndex < labelCount) {
                            var10 = (String) labels.get(labelIndex);
                            var11 = blinks.get(labelIndex) != null;

                            var12 = getScrollX();
                            var13 = (float) (var12 / 2) + (float) labelIndex * childHalfWidthWithDivider;
                        } else if (labelIndex == labelCount) {
                            if (childCount == 1) {
                                break label107;
                            }

                            var10 = (String) labels.get(0);
                            var11 = blinks.get(0) != null;
                            var12 = getScrollX();
                            var13 = (float) (var12 / 2) + (float) labelIndex * childHalfWidthWithDivider;
                        } else if (labelIndex == -2) {
                            if (childCount == 1) {
                                break label107;
                            }

                            var10 = (String) labels.get(labelCount - 2);
                            var11 = blinks.get(labelCount - 2) != null;
                            var12 = getScrollX();
                            var13 = (float) (var12 / 2) - 2.0F * childHalfWidthWithDivider;
                        } else if (labelIndex == labelCount + 1) {
                            if (childCount == 1) {
                                break label107;
                            }
                            var10 = (String) labels.get(1);
                            var11 = blinks.get(1) != null;
                            var12 = getScrollX();
                            var13 = (float) (var12 / 2) + (float) labelIndex * childHalfWidthWithDivider;
                        }

                        float var14 = labels_.measureText(var10);
                        float var15 = var13 + childHalfWidthWithDivider - var14 / 2.0F;
                        if (var15 + var14 > (float) var12 && var15 < (float) var12 + childWidthWithDivider) {
                            var12 = 255 - (int) (Math.abs((float) var12 + childHalfWidthWithDivider - var14 / 2.0F - var15) * 255.0F / (0.65F * childWidthWithDivider));
                            int var16 = Math.min(var12, 255);
                            var12 = Math.max(var16, 0);
                            var14 = (float) (PANEL_HEIGHT / 2) + labelBaseline / 2.0F;
                            if (var11) {
                                canvas.drawText(var10, var15, var14, effect);
                                labels_.setStrokeWidth(1.0F);
                            } else {
                                labels_.setStrokeWidth(4.0F);
                            }

                            highlight.setBounds((int) var13, 0, (int) (var13 + childWidthWithDivider), PANEL_HEIGHT);
                            highlight_.setAlpha(var12);
                            highlight.draw(canvas);
                            labels_.setColor(-16777216);
                            TextPaint var17 = labels_;
                            if (var11) {
                                var16 = 255;
                            } else {
                                var16 = var12;
                            }

                            var17.setAlpha(var16);
                            labels_.setStyle(Style.STROKE);
                            canvas.drawText(var10, var15, var14, labels_);
                            var17 = labels_;
                            if (var11) {
                                var16 = text_color;
                            } else {
                                var16 = -1;
                            }

                            var17.setColor(var16);
                            var17 = labels_;
                            if (var11) {
                                var12 = 255;
                            }

                            var17.setAlpha(var12);
                            labels_.setStyle(Style.FILL);
                            canvas.drawText(var10, var15, var14, labels_);
                        }
                    }

                    ++labelIndex;
                }
            }

            //noinspection ConstantValue
            if (false) {
                invalidate();
            }
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        View currentView = getChildAt(currentScreen);
        boolean handled;

        if (currentView == null) {
            handled = false;
        } else {
            handled = currentView.dispatchKeyEvent(keyEvent);

            if (!handled && keyEvent.getAction() == KeyEvent.ACTION_DOWN && scroller.isFinished()) {
                int keyCode = keyEvent.getKeyCode();

                // Handle left arrow key press to switch to the previous screen
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && !fully_locked) {
                    switchToPrev();
                    handled = true;
                }
                // Handle right arrow key press to switch to the next screen
                else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && !fully_locked) {
                    switchToNext();
                    handled = true;
                }
            }

            Log.e("KEY_EVENT", "CODE: " + keyEvent.getKeyCode() +
                    "     EVENT: " + keyEvent.getAction() +
                    "     HANDLED: " + handled);
        }

        return handled;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled;
        if (wrap_mode) {
            // Режим обертки отключен, игнорируем событие
            handled = false;
        } else if (getChildCount() == 0) {
            // Нет дочерних элементов, игнорируем событие
            handled = false;
        } else {
            float touchX;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Пользователь начал касание
                    locked = false;
                    scrollX = event.getX();
                    lastTouchX = scrollX;
                    lastTouchY = event.getY();

                    if (!scroller.isFinished()) {
                        // Если была выполняется анимация прокрутки, принудительно завершаем её
                        mIsBeingDragged = true;
                        scroller.forceFinished(true);
                        return true;
                    }

                    mIsBeingDragged = false;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Пользователь завершил касание или оно было отменено
                    if (mIsBeingDragged) {
                        mIsBeingDragged = false;
                        touchX = event.getX() - lastTouchX;

                        if (Math.abs(touchX) > 96.0F && !fully_locked) {
                            // Если пользователь сделал достаточно длинный жест, переключаем экраны
                            if (touchX < 0.0F) {
                                switchToNext();
                            } else {
                                switchToPrev();
                            }
                        } else {
                            // В противном случае, запускаем анимацию прокрутки на текущем экране
                            scroller.startScroll(getScrollX(), 0, 0, 0, SCROLLING_TIME);
                            scroller.setFinalX(currentScreen * (getWidth() + DIVIDER_WIDTH));
                        }

                        postInvalidate();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Пользователь двигает палец по экрану
                    if (mIsBeingDragged) {
                        // Если уже в режиме перетаскивания, прокручиваем экран
                        scrollBy((int) (scrollX - event.getX()), 0);
                        scrollX = event.getX();
                    } else {
                        // В противном случае, проверяем возможность начать прокрутку
                        float deltaX = Math.abs(lastTouchX - event.getX());
                        float deltaY = Math.abs(event.getY() - lastTouchY);

                        if (deltaX > 32.0F && !locked && !fully_locked) {
                            // Если пользователь сдвинул палец на достаточное расстояние, входим в режим перетаскивания
                            if (deltaY > 32.0F) {
                                locked = true;
                            } else {
                                mIsBeingDragged = true;
                                scrollX = event.getX();
                                lastTouchX = scrollX;

                                if (ANIMATION_RANDOMIZED) {
                                    // Случайно выбираем тип анимации
                                    ANIMATION_TYPE = (new Random(System.currentTimeMillis())).nextInt(8);
                                }

                                setAnimationState(true);
                            }
                        }
                    }
            }

            if (!mIsBeingDragged) {
                // Если не находимся в режиме перетаскивания, передаем событие родительскому элементу
                handled = super.dispatchTouchEvent(event);
            } else {
                // Иначе, создаем и отправляем завершающее событие ACTION_UP
                super.dispatchTouchEvent(MotionEvent.obtain(1L, 1L, MotionEvent.ACTION_CANCEL, event.getX(), event.getY(), 0));
                handled = false;
            }
        }

        return handled;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int scrollX = this.getScrollX();
        int childCount = this.getChildCount();
        boolean scrollAtStart;
        boolean scrollAtEnd;

        // Определяем, находимся ли в начале или конце прокрутки
        scrollAtStart = scrollX < 0;

        scrollAtEnd = scrollX > (this.getWidth() + this.DIVIDER_WIDTH) * childCount - this.getWidth();

        int childIndex = this.indexOfChild(child);
        boolean childAtEnd;
        boolean childAtStart;

        // Определяем, находится ли текущий дочерний элемент в начале или конце списка
        childAtEnd = childIndex == childCount - 1;
        childAtStart = childIndex == 0;

        // Проверяем, должен ли данный дочерний элемент отображаться на экране
        boolean shouldDisplay = this.isInDisplay(child);

        // Определяем, должен ли данный дочерний элемент быть отрисован
        boolean shouldDraw;

        shouldDraw = (scrollAtStart && childAtEnd) || (scrollAtEnd && childAtStart);

        if (!shouldDisplay && !shouldDraw) {
            // Данный дочерний элемент не должен быть отрисован
            return false;
        } else {
            // Отрисовываем дочерний элемент, если он должен быть отображен
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    public void freezeInvalidating(boolean var1) {
        this.freezed = var1;
        this.invalidate();
    }

    public int getAnimationType() {
        return this.ANIMATION_TYPE;
    }

    protected boolean getChildStaticTransformation(View childView, Transformation transformation) {
        int scrollX = this.getScrollX();
        int childCount = this.getChildCount();
        boolean isScrollingLeft = scrollX < 0;
        boolean isScrollingRight = scrollX > ((this.getWidth() + this.DIVIDER_WIDTH) * childCount - this.getWidth());
        int childIndex = this.indexOfChild(childView);
        boolean isFirstChild = (childIndex == 0);
        boolean isLastChild = (childIndex == childCount - 1);

        int offset = 0;
        if (childCount > 1) {
            if (isScrollingLeft && isFirstChild) {
                offset = -((this.getWidth() + this.DIVIDER_WIDTH) * childCount);
            } else if (isScrollingRight && isLastChild) {
                offset = (this.getWidth() + this.DIVIDER_WIDTH) * childCount;
            }
        }

        int childOffset = scrollX - (childView.getLeft() + offset);
        transformation.clear();
        Matrix matrix = transformation.getMatrix();
        float alpha;

        switch (this.ANIMATION_TYPE) {
            case 0:
                // Применяем трансформацию куба
                transformation.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCube(matrix, childView.getRight() - childView.getLeft(), childView.getBottom() - childView.getTop(), (float) childOffset * 180.0F / (float) (childView.getRight() - childView.getLeft()), childOffset);
                break;
            case 1:
                // Применяем инверсную трансформацию куба
                transformation.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCubeInv(matrix, childView.getRight() - childView.getLeft(), childView.getBottom() - childView.getTop(), (float) childOffset * 180.0F / (float) (childView.getRight() - childView.getLeft()), childOffset);
                break;
            case 2:
                // Применяем трансформацию Flip2
                transformation.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyTransformationFlip2((float) childOffset * 180.0F / (float) (childView.getRight() - childView.getLeft()), (float) ((childView.getRight() - childView.getLeft()) / 2), (float) ((childView.getBottom() - childView.getTop()) / 2), matrix);
                break;
            case 3:
            default:
                break;
            case 4:
                // Поворачиваем на 180 градусов
                matrix.postRotate((float) childOffset * 180.0F / (float) (childView.getRight() - childView.getLeft()), (float) ((childView.getRight() - childView.getLeft()) / 2), (float) ((childView.getBottom() - childView.getTop()) / 2));
                break;
            case 5:
                // Поворачиваем вверх на 90 градусов
                matrix.postRotate((float) (-childOffset) * 90.0F / (float) (childView.getRight() - childView.getLeft()), (float) ((childView.getRight() - childView.getLeft()) / 2), (float) (childView.getBottom() - childView.getTop()));
                break;
            case 6:
                // Поворачиваем вниз на 90 градусов
                matrix.postRotate((float) childOffset * 90.0F / (float) (childView.getRight() - childView.getLeft()), (float) ((childView.getRight() - childView.getLeft()) / 2), 0.0F);
                break;
            case 7:
                // Применяем альфа-трансформацию
                alpha = Math.abs((float) childOffset / (float) (childView.getRight() - childView.getLeft()));
                transformation.setTransformationType(Transformation.TYPE_BOTH);
                transformation.setAlpha(1.0F - alpha);
                if (childOffset < 0) {
                    alpha = Math.abs((float) childOffset / (float) (childView.getRight() - childView.getLeft())) / 7.0F;
                    matrix.postScale(1.0F - alpha, 1.0F - alpha, (float) ((childView.getRight() - childView.getLeft()) / 2), (float) ((childView.getBottom() - childView.getTop()) / 2));
                    matrix.postTranslate((float) childOffset, 0.0F);
                }
                break;
            case 8:
                // Применяем трансформацию Snake
                transformation.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolySnake(matrix, childView.getRight() - childView.getLeft(), childView.getBottom() - childView.getTop(), (float) childOffset * 180.0F / (float) (childView.getRight() - childView.getLeft()), childOffset);
                break;
            case 9:
                // Применяем альфа-трансформацию и поворот на 90 градусов
                alpha = Math.abs((float) childOffset / (float) (childView.getRight() - childView.getLeft()));
                transformation.setTransformationType(Transformation.TYPE_BOTH);
                transformation.setAlpha(1.0F - alpha);
                matrix.postRotate((float) childOffset * 90.0F / (float) (childView.getRight() - childView.getLeft()), 0.0F, 0.0F);
                matrix.postTranslate((float) childOffset, 0.0F);
                break;
            case 10:
                // Применяем трансформацию Flip2
                transformation.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyTransformationFlip2((float) childOffset * 20.0F / (float) (childView.getRight() - childView.getLeft()), (float) ((childView.getRight() - childView.getLeft()) / 2), (float) ((childView.getBottom() - childView.getTop()) / 2), matrix);
        }

        matrix.postTranslate((float) offset, 0.0F);
        return true;
    }

    protected void measureChild(View var1, int var2, int var3) {
    }

    protected void measureChildWithMargins(View var1, int var2, int var3, int var4, int var5) {
    }

    protected void measureChildren(int var1, int var2) {
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int panelHeight = 0;

        // Если панель должна быть видимой, устанавливаем её высоту
        if (this.show_panel) {
            panelHeight = this.PANEL_HEIGHT;
        }

        int childCount = this.getChildCount();
        int parentHeight = this.getHeight();

        // Если панель видима, устанавливаем её высоту для вычисления позиций дочерних элементов
        int availableHeight = this.show_panel ? this.PANEL_HEIGHT : 0;

        for (int i = 0; i < childCount; ++i) {
            View childView = this.getChildAt(i);
            int childWidth = this.getWidth() + this.DIVIDER_WIDTH;

            // Измеряем дочерний элемент с учетом доступной высоты и ширины родителя
            childView.measure(
                    MeasureSpec.makeMeasureSpec(this.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(parentHeight - availableHeight, MeasureSpec.UNSPECIFIED)
            );

            // Размещаем дочерний элемент на экране
            childView.layout(
                    childWidth * i + left,  // Left
                    panelHeight,            // Top (с учетом высоты панели)
                    childWidth * i + right, // Right
                    parentHeight            // Bottom
            );
        }
    }

    public void onMeasure(int var1, int var2) {
        this.setMeasuredDimension(MeasureSpec.getSize(var1), MeasureSpec.getSize(var2));
    }

    protected final void onSizeChanged(int var1, int var2, int var3, int var4) {
        super.onSizeChanged(var1, var2, var3, var4);
        this.scrollTo(this.currentScreen * (this.getWidth() + this.DIVIDER_WIDTH), 0);
        this.requestLayout();
    }

    public void removeViewAt(int index) {
        int childCount = this.getChildCount();

        if (childCount > 0 && index < childCount) {
            // Удаляем дочернее представление по указанному индексу
            super.removeViewAt(index);

            // Удаляем соответствующие метки и мигание
            this.labels.remove(index);
            this.blinks.remove(index);

            // Пересчитываем текущий экран, если удаленное представление было слева от текущего
            if (index < this.currentScreen) {
                --this.currentScreen;

                // Перемещаем вид в новое положение
                if (this.scroller.isFinished()) {
                    this.scrollTo(this.currentScreen * (this.getWidth() + this.DIVIDER_WIDTH), 0);
                } else {
                    this.scrollTo((this.currentScreen - 1) * (this.getWidth() + this.DIVIDER_WIDTH), 0);
                }
            } else if (index == this.currentScreen && childCount > 1) {
                // Если удаленное представление было текущим и есть другие представления
                --this.currentScreen;

                // Перемещаем вид в новое положение
                if (this.scroller.isFinished()) {
                    this.scrollTo(this.currentScreen * (this.getWidth() + this.DIVIDER_WIDTH), 0);
                } else {
                    this.scrollTo((this.currentScreen - 1) * (this.getWidth() + this.DIVIDER_WIDTH), 0);
                }
            }
        }
    }

    public void scrollTo(int var1) {
        int var2 = this.getChildCount();
        if (var2 > 0 && var1 < var2) {
            this.scrollTo((this.getWidth() + this.DIVIDER_WIDTH) * var1, 0);
            this.currentScreen = var1;
        }

    }

    public void setAnimationType(int var1) {
        this.ANIMATION_TYPE = var1;
        this.invalidate();
    }

    /**
     * Устанавливает состояние мигания (blink) для элемента в списке меток по его индексу.
     *
     * @param index       Индекс элемента в списке меток.
     * @param shouldBlink true, если элемент должен мигать, false в противном случае.
     */
    public void setBlinkState(int index, boolean shouldBlink) {
        // Проверяем, что индекс находится в пределах допустимых значений
        if (index >= 0 && index < this.labels.size()) {
            Vector<Object> blinksVector = this.blinks;
            Object blinkState;

            // Создаем объект-мигатель, если shouldBlink равно true, иначе устанавливаем null
            if (shouldBlink) {
                blinkState = new Object();
            } else {
                blinkState = null;
            }

            // Устанавливаем состояние мигания для элемента по заданному индексу
            blinksVector.set(index, blinkState);

            // Запрашиваем перерисовку виджета для отображения изменений
            this.invalidate();
        }
    }

    public void setLock(boolean var1) {
        this.fully_locked = var1;
    }

    public void setRandomizedAnimation(boolean var1) {
        this.ANIMATION_RANDOMIZED = var1;
    }

    public void togglePanel(boolean var1) {
        this.show_panel = var1;
        this.requestLayout();
    }

    /**
     * Обновляет конфигурацию внешнего вида элементов в соответствии с текущими настройками.
     * Этот метод обновляет размер текста, цветовые параметры и высоту панели с метками.
     */
    public void updateConfig() {
        // Получаем размер текста из настроек и увеличиваем его на 10%.
        float textSize = (float) PreferenceTable.clTextSize;
        textSize += textSize / 100.0F * 10.0F;

        // Устанавливаем размер текста в соответствии с настройками плотности экрана.
        this.labels_.setTextSize(resources.dm.density * textSize);

        // Устанавливаем цвет и прозрачность для текстового эффекта.
        this.effect.setColor(ColorScheme.getColor(49));
        this.effect.setAlpha(160);
        this.effect.setTextSize(this.labels_.getTextSize());

        // Устанавливаем цветовой фильтр для подсветки меток.
        this.highlight_.setColorFilter(new LightingColorFilter(0, ColorScheme.getColor(49)));

        // Рассчитываем высоту панели с метками на основе размера текста и дополнительного значения.
        this.PANEL_HEIGHT = (int) ((textSize / 100.0F * 70.0F + textSize) * resources.dm.density);

        // Устанавливаем ширину разделителя (DIVIDER_WIDTH), но в данном случае она всегда равна 0.
        this.DIVIDER_WIDTH = (int) (0.0F * resources.dm.density);

        // Запрашиваем перерасчет макета для обновления отображения элементов.
        this.requestLayout();
    }

    public void updateLabel(int index, String element) {
        if (index >= 0 && index < this.labels.size()) {
            this.labels.set(index, element);
            this.invalidate();
        }
    }
}