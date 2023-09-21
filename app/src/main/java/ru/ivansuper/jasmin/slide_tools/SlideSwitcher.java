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
    private boolean ANIMATION_RANDOMIZED = false;
    private int ANIMATION_TYPE = 9;
    private int DIVIDER_WIDTH = 1;
    private float FADING_LENGTH = 16.0F;
    private int PANEL_HEIGHT = 48;
    private int SCROLLING_TIME = 280;
    private boolean animation;
    public TypedArray attrs;
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
    private Vector<String> labels = new Vector();
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

    public SlideSwitcher(Context var1) {
        super(var1);
        this.init(var1);
    }

    public SlideSwitcher(Context var1, AttributeSet var2) {
        super(var1, var2);
        // TODO: !!!
        // this.attrs = var1.obtainStyledAttributes(var2, R.styleable.View);
        this.init(var1);
    }

    private void handleAnimationEnd() {
        int var1 = this.getChildCount();

        for(int var2 = 0; var2 < var1; ++var2) {
            View var3 = this.getChildAt(var2);
            if (var3 != null) {
                var3.setDrawingCacheEnabled(false);
                var3.setWillNotCacheDrawing(true);
            }
        }

    }

    private void handleAnimationStart() {
        int var1 = this.getChildCount();

        for(int var2 = 0; var2 < var1; ++var2) {
            View var3 = this.getChildAt(var2);
            if (var3 != null) {
                var3.setDrawingCacheEnabled(true);
                var3.setWillNotCacheDrawing(false);
            }
        }

    }

    private void init(Context var1) {
        this.setWillNotDraw(true);
        this.setDrawingCacheEnabled(false);
        this.setWillNotCacheDrawing(true);
        this.setStaticTransformationsEnabled(true);
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
        this.panel = this.getContext().getResources().getDrawable(R.drawable.slide_switcher_panel);
        this.highlight = resources.convertToMyFormat(resources.tab_highlight);
        this.highlight_ = new Paint(2);
        this.highlight.setCustomPaint(this.highlight_);
        resources.attachSlidePanel(this);
        this.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.scroller = new Scroller(var1, new DecelerateInterpolator());
        this.FADING_LENGTH *= resources.dm.density;
        this.fade_shader = new LinearGradient(0.0F, 0.0F, 0.0F, this.FADING_LENGTH, -1, 16777215, TileMode.CLAMP);
        this.fade_shader_ = new Paint();
        this.fade_shader_.setShader(this.fade_shader);
        this.fade_shader_.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        this.fade_shader_m = new Matrix();
        this.updateConfig();
    }

    private final boolean isInDisplay(View var1) {
        Rect var3 = new Rect(var1.getLeft(), var1.getTop(), var1.getRight(), var1.getBottom());
        int var2 = this.getScrollX();
        return var3.intersect(new Rect(var2, 0, this.getWidth() + var2, this.getHeight()));
    }

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
        if (!this.fully_locked) {
            if (this.currentScreen == this.getChildCount() - 1) {
                this.currentScreen = 0;
                this.wrapToFirst();
            } else {
                ++this.currentScreen;
                this.scroller.startScroll(this.getScrollX(), 0, 0, 0, this.SCROLLING_TIME);
                this.scroller.setFinalX(this.currentScreen * (this.getWidth() + this.DIVIDER_WIDTH));
                this.postInvalidate();
            }
        }

    }

    private final void switchToPrev() {
        if (!this.fully_locked) {
            if (this.currentScreen == 0) {
                this.currentScreen = this.getChildCount() - 1;
                this.wrapToLast();
            } else {
                --this.currentScreen;
                this.scroller.startScroll(this.getScrollX(), 0, 0, 0, this.SCROLLING_TIME);
                this.scroller.setFinalX(this.currentScreen * (this.getWidth() + this.DIVIDER_WIDTH));
                this.postInvalidate();
            }
        }

    }

    private void wrapToFirst() {
        this.wrap_mode = true;
        this.wrap_direction = 1;
        this.scroller.startScroll(this.getScrollX(), 0, 0, 0, this.SCROLLING_TIME);
        int var1 = this.getWidth();
        int var2 = this.DIVIDER_WIDTH;
        this.scroller.setFinalX(this.getChildCount() * (var1 + var2));
        this.postInvalidate();
    }

    private void wrapToLast() {
        this.wrap_mode = true;
        this.wrap_direction = -1;
        this.scroller.startScroll(this.getScrollX(), 0, 0, 0, this.SCROLLING_TIME);
        this.scroller.setFinalX(-(this.getWidth() + this.DIVIDER_WIDTH));
        this.postInvalidate();
    }

    public void addView(View var1, String var2) {
        this.labels.add(var2);
        this.blinks.add((Object)null);
        if ((LinearLayout.LayoutParams)var1.getLayoutParams() == null) {
            var1.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        }

        super.addView(var1);
    }

    public void clearupCaches() {
        int var1 = this.getChildCount();

        for(int var2 = 0; var2 < var1; ++var2) {
            View var3 = this.getChildAt(var2);

            try {
                ((MultiColumnList)var3).clearup();
            } catch (Exception var4) {
            }
        }

    }

    public void computeScroll() {
        if (!this.mIsBeingDragged) {
            if (this.scroller.computeScrollOffset()) {
                this.scrollTo(this.scroller.getCurrX(), 0);
                this.postInvalidate();
            } else if (this.wrap_mode) {
                if (this.wrap_direction < 0) {
                    int var1 = this.getWidth();
                    int var2 = this.DIVIDER_WIDTH;
                    this.scrollTo((this.getChildCount() - 1) * (var1 + var2), 0);
                    this.wrap_direction = 0;
                    this.setAnimationState(false);
                } else if (this.wrap_direction > 0) {
                    this.scrollTo(0, 0);
                    this.postInvalidate();
                    this.wrap_direction = 0;
                    this.setAnimationState(false);
                } else {
                    this.wrap_mode = false;
                }
            } else {
                this.setAnimationState(false);
            }
        }

    }

    public void dispatchDraw(Canvas var1) {
        if (!this.freezed) {
            int var2 = this.getChildCount();
            super.dispatchDraw(var1);
            if (this.show_panel) {
                float var3 = (float)this.getScrollX();
                float var4 = (float)(this.getWidth() + this.DIVIDER_WIDTH);
                float var5 = (float)((this.getWidth() + this.DIVIDER_WIDTH) / 2);
                this.panel.setBounds((int)var3, 0, (int)(var3 + var4), this.PANEL_HEIGHT);
                this.panel.draw(var1);
                int var6 = var1.saveLayer(var3, 0.0F, var3 + var4, (float)this.PANEL_HEIGHT, (Paint)null, Canvas.ALL_SAVE_FLAG);
                float var7 = (float)(-this.labels_.getFontMetricsInt().ascent - this.labels_.getFontMetricsInt().descent);
                int var8 = this.labels.size();
                int var9 = -2;

                while(true) {
                    if (var9 > var8 + 1) {
                        this.fade_shader_m.setRotate(-90.0F);
                        this.fade_shader.setLocalMatrix(this.fade_shader_m);
                        var1.translate(var3, 0.0F);
                        var1.drawRect(0.0F, 0.0F, this.FADING_LENGTH, (float)this.PANEL_HEIGHT, this.fade_shader_);
                        this.fade_shader_m.setRotate(90.0F);
                        this.fade_shader_m.postTranslate(this.FADING_LENGTH, 0.0F);
                        var1.translate((float)this.getWidth() - this.FADING_LENGTH, 0.0F);
                        this.fade_shader.setLocalMatrix(this.fade_shader_m);
                        var1.drawRect(0.0F, 0.0F, this.FADING_LENGTH, (float)this.PANEL_HEIGHT, this.fade_shader_);
                        var1.restoreToCount(var6);
                        break;
                    }

                    label107: {
                        String var10 = null;
                        boolean var11 = false;
                        int var12 = this.getScrollX();
                        float var13 = (float)(var12 / 2) + (float)var9 * var5;
                        if (var9 == -1) {
                            if (var2 == 1) {
                                break label107;
                            }

                            var10 = (String)this.labels.get(var8 - 1);
                            if (this.blinks.get(var8 - 1) != null) {
                                var11 = true;
                            } else {
                                var11 = false;
                            }

                            var12 = this.getScrollX();
                            var13 = (float)(var12 / 2) - var5;
                        } else if (var9 >= 0 && var9 < var8) {
                            var10 = (String)this.labels.get(var9);
                            if (this.blinks.get(var9) != null) {
                                var11 = true;
                            } else {
                                var11 = false;
                            }

                            var12 = this.getScrollX();
                            var13 = (float)(var12 / 2) + (float)var9 * var5;
                        } else if (var9 == var8) {
                            if (var2 == 1) {
                                break label107;
                            }

                            var10 = (String)this.labels.get(0);
                            if (this.blinks.get(0) != null) {
                                var11 = true;
                            } else {
                                var11 = false;
                            }

                            var12 = this.getScrollX();
                            var13 = (float)(var12 / 2) + (float)var9 * var5;
                        } else if (var9 == -2) {
                            if (var2 == 1) {
                                break label107;
                            }

                            var10 = (String)this.labels.get(var8 - 2);
                            if (this.blinks.get(var8 - 2) != null) {
                                var11 = true;
                            } else {
                                var11 = false;
                            }

                            var12 = this.getScrollX();
                            var13 = (float)(var12 / 2) - 2.0F * var5;
                        } else if (var9 == var8 + 1) {
                            if (var2 == 1) {
                                break label107;
                            }

                            var10 = (String)this.labels.get(1);
                            if (this.blinks.get(1) != null) {
                                var11 = true;
                            } else {
                                var11 = false;
                            }

                            var12 = this.getScrollX();
                            var13 = (float)(var12 / 2) + (float)var9 * var5;
                        }

                        float var14 = this.labels_.measureText(var10);
                        float var15 = var13 + var5 - var14 / 2.0F;
                        if (var15 + var14 > (float)var12 && var15 < (float)var12 + var4) {
                            var12 = 255 - (int)(Math.abs((float)var12 + var5 - var14 / 2.0F - var15) * 255.0F / (0.65F * var4));
                            int var16 = var12;
                            if (var12 > 255) {
                                var16 = 255;
                            }

                            var12 = var16;
                            if (var16 < 0) {
                                var12 = 0;
                            }

                            var14 = (float)(this.PANEL_HEIGHT / 2) + var7 / 2.0F;
                            if (var11) {
                                var1.drawText(var10, var15, var14, this.effect);
                                this.labels_.setStrokeWidth(1.0F);
                            } else {
                                this.labels_.setStrokeWidth(4.0F);
                            }

                            this.highlight.setBounds((int)var13, 0, (int)(var13 + var4), this.PANEL_HEIGHT);
                            this.highlight_.setAlpha(var12);
                            this.highlight.draw(var1);
                            this.labels_.setColor(-16777216);
                            TextPaint var17 = this.labels_;
                            if (var11) {
                                var16 = 255;
                            } else {
                                var16 = var12;
                            }

                            var17.setAlpha(var16);
                            this.labels_.setStyle(Style.STROKE);
                            var1.drawText(var10, var15, var14, this.labels_);
                            var17 = this.labels_;
                            if (var11) {
                                var16 = this.text_color;
                            } else {
                                var16 = -1;
                            }

                            var17.setColor(var16);
                            var17 = this.labels_;
                            if (var11) {
                                var12 = 255;
                            }

                            var17.setAlpha(var12);
                            this.labels_.setStyle(Style.FILL);
                            var1.drawText(var10, var15, var14, this.labels_);
                        }
                    }

                    ++var9;
                }
            }

            if (false) {
                this.invalidate();
            }
        }

    }

    public boolean dispatchKeyEvent(KeyEvent var1) {
        View var2 = this.getChildAt(this.currentScreen);
        boolean var3;
        if (var2 == null) {
            var3 = false;
        } else {
            var3 = var2.dispatchKeyEvent(var1);
            if (!var3 && var1.getAction() == 0 && this.scroller.isFinished()) {
                if (var1.getKeyCode() == 21 && !this.fully_locked) {
                    this.switchToPrev();
                    var3 = true;
                    return var3;
                }

                if (var1.getKeyCode() == 22 && !this.fully_locked) {
                    this.switchToNext();
                    var3 = true;
                    return var3;
                }
            }

            Log.e("KEY_EVENT", "CODE: " + var1.getKeyCode() + "     EVENT: " + var1.getAction() + "     HANDLED:" + var3);
        }

        return var3;
    }

    public boolean dispatchTouchEvent(MotionEvent var1) {
        boolean var2;
        if (this.wrap_mode) {
            var2 = false;
        } else if (this.getChildCount() == 0) {
            var2 = false;
        } else {
            float var3;
            switch (var1.getAction()) {
                case 0:
                    this.locked = false;
                    this.scrollX = var1.getX();
                    this.lastTouchX = this.scrollX;
                    this.lastTouchY = var1.getY();
                    if (!this.scroller.isFinished()) {
                        this.mIsBeingDragged = true;
                        this.scroller.forceFinished(true);
                        var2 = true;
                        return var2;
                    }

                    this.mIsBeingDragged = false;
                    break;
                case 1:
                case 3:
                    if (this.mIsBeingDragged) {
                        this.mIsBeingDragged = false;
                        var3 = var1.getX() - this.lastTouchX;
                        this.getChildCount();
                        if (Math.abs(var3) > 96.0F && !this.fully_locked) {
                            if (var3 < 0.0F) {
                                this.switchToNext();
                            } else {
                                this.switchToPrev();
                            }
                        } else {
                            this.scroller.startScroll(this.getScrollX(), 0, 0, 0, this.SCROLLING_TIME);
                            this.scroller.setFinalX(this.currentScreen * (this.getWidth() + this.DIVIDER_WIDTH));
                        }

                        this.postInvalidate();
                    }
                    break;
                case 2:
                    if (this.mIsBeingDragged) {
                        this.scrollBy((int)(this.scrollX - var1.getX()), 0);
                        this.scrollX = var1.getX();
                    } else {
                        var3 = Math.abs(this.lastTouchX - var1.getX());
                        float var4 = Math.abs(var1.getY() - this.lastTouchY);
                        if (var3 > 32.0F && !this.locked && !this.fully_locked) {
                            if (var4 > 32.0F) {
                                this.locked = true;
                            } else {
                                this.mIsBeingDragged = true;
                                this.scrollX = var1.getX();
                                this.lastTouchX = this.scrollX;
                                if (this.ANIMATION_RANDOMIZED) {
                                    this.ANIMATION_TYPE = (new Random(System.currentTimeMillis())).nextInt(8);
                                }

                                this.setAnimationState(true);
                            }
                        }
                    }
            }

            if (!this.mIsBeingDragged) {
                var2 = super.dispatchTouchEvent(var1);
            } else {
                super.dispatchTouchEvent(MotionEvent.obtain(1L, 1L, 3, var1.getX(), var1.getY(), 0));
                var2 = false;
            }
        }

        return var2;
    }

    protected boolean drawChild(Canvas var1, View var2, long var3) {
        int var5 = this.getScrollX();
        int var6 = this.getChildCount();
        boolean var7;
        if (var5 < 0) {
            var7 = true;
        } else {
            var7 = false;
        }

        boolean var10;
        if (var5 > (this.getWidth() + this.DIVIDER_WIDTH) * var6 - this.getWidth()) {
            var10 = true;
        } else {
            var10 = false;
        }

        int var8 = this.indexOfChild(var2);
        boolean var11;
        if (var8 == var6 - 1) {
            var11 = true;
        } else {
            var11 = false;
        }

        boolean var12;
        if (var8 == 0) {
            var12 = true;
        } else {
            var12 = false;
        }

        if ((!var7 || !var11) && (!var10 || !var12)) {
            var7 = false;
        } else {
            var7 = true;
        }

        boolean var9;
        if (!this.isInDisplay(var2) && !var7) {
            var9 = false;
        } else {
            var9 = super.drawChild(var1, var2, var3);
        }

        return var9;
    }

    public void freezeInvalidating(boolean var1) {
        this.freezed = var1;
        this.invalidate();
    }

    public int getAnimationType() {
        return this.ANIMATION_TYPE;
    }

    protected boolean getChildStaticTransformation(View var1, Transformation var2) {
        int var3 = this.getScrollX();
        int var4 = this.getChildCount();
        boolean var5;
        if (var3 < 0) {
            var5 = true;
        } else {
            var5 = false;
        }

        int var6 = (this.getWidth() + this.DIVIDER_WIDTH) * var4;
        boolean var7;
        if (var3 > var6 - this.getWidth()) {
            var7 = true;
        } else {
            var7 = false;
        }

        int var8 = this.indexOfChild(var1);
        boolean var9;
        if (var8 == var4 - 1) {
            var9 = true;
        } else {
            var9 = false;
        }

        boolean var10;
        if (var8 == 0) {
            var10 = true;
        } else {
            var10 = false;
        }

        int var11 = var1.getRight() - var1.getLeft();
        int var12 = var1.getBottom() - var1.getTop();
        var8 = 0;
        byte var13 = 0;
        if (var4 > 1) {
            var4 = var13;
            if (var5) {
                var4 = var13;
                if (var9) {
                    var4 = -var6;
                }
            }

            var8 = var4;
            if (var7) {
                var8 = var4;
                if (var10) {
                    var8 = var6;
                }
            }
        }

        var4 = var3 - (var1.getLeft() + var8);
        var2.clear();
        Matrix var15 = var2.getMatrix();
        float var14;
        switch (this.ANIMATION_TYPE) {
            case 0:
                var2.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCube(var15, var11, var12, (float)var4 * 180.0F / (float)var11, var4);
                break;
            case 1:
                var2.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCubeInv(var15, var11, var12, (float)var4 * 180.0F / (float)var11, var4);
                break;
            case 2:
                var2.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyTransformationFlip2((float)var4 * 180.0F / (float)var11, (float)(var11 / 2), (float)(var12 / 2), var15);
            case 3:
            default:
                break;
            case 4:
                var15.postRotate((float)var4 * 180.0F / (float)var11, (float)(var11 / 2), (float)(var12 / 2));
                break;
            case 5:
                var15.postRotate((float)(-var4) * 90.0F / (float)var11, (float)(var11 / 2), (float)var12);
                break;
            case 6:
                var15.postRotate((float)var4 * 90.0F / (float)var11, (float)(var11 / 2), 0.0F);
                break;
            case 7:
                var14 = Math.abs((float)var4 / (float)var11);
                if (var4 >= 0) {
                    var2.setTransformationType(Transformation.TYPE_BOTH);
                    var2.setAlpha(1.0F - var14);
                } else {
                    var2.setTransformationType(Transformation.TYPE_BOTH);
                    var2.setAlpha(1.0F - var14);
                    var14 = Math.abs((float)var4 / (float)var11) / 7.0F;
                    var15.postScale(1.0F - var14, 1.0F - var14, (float)(var11 / 2), (float)(var12 / 2));
                    var15.postTranslate((float)var4, 0.0F);
                }
                break;
            case 8:
                var2.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolySnake(var15, var11, var12, (float)var4 * 180.0F / (float)var11, var4);
                break;
            case 9:
                var14 = Math.abs((float)var4 / (float)var11);
                var2.setTransformationType(Transformation.TYPE_BOTH);
                var2.setAlpha(1.0F - var14);
                var15.postRotate((float)var4 * 90.0F / (float)var11, 0.0F, 0.0F);
                var15.postTranslate((float)var4, 0.0F);
                break;
            case 10:
                var2.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyTransformationFlip2((float)var4 * 20.0F / (float)var11, (float)(var11 / 2), (float)(var12 / 2), var15);
        }

        var15.postTranslate((float)var8, 0.0F);
        return true;
    }

    protected void measureChild(View var1, int var2, int var3) {
    }

    protected void measureChildWithMargins(View var1, int var2, int var3, int var4, int var5) {
    }

    protected void measureChildren(int var1, int var2) {
    }

    protected void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        var3 = 0;
        if (this.show_panel) {
            var3 = this.PANEL_HEIGHT;
        }

        int var6 = this.getChildCount();
        int var7 = this.getHeight();
        if (this.show_panel) {
            var5 = this.PANEL_HEIGHT;
        } else {
            var5 = 0;
        }

        for(int var8 = 0; var8 < var6; ++var8) {
            View var9 = this.getChildAt(var8);
            int var10 = this.getWidth() + this.DIVIDER_WIDTH;
            var9.measure(MeasureSpec.makeMeasureSpec(this.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(var7 - var5, MeasureSpec.UNSPECIFIED));
            var9.layout(var10 * var8 + var2, var3, var10 * var8 + var4, this.getHeight());
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

    public void removeViewAt(int var1) {
        int var2 = this.getChildCount();
        if (var2 > 0 && var1 < var2) {
            super.removeViewAt(var1);
            this.labels.remove(var1);
            this.blinks.remove(var1);
            if (var1 < this.currentScreen) {
                --this.currentScreen;
                if (this.scroller.isFinished()) {
                    this.scrollTo(this.currentScreen * (this.getWidth() + this.DIVIDER_WIDTH), 0);
                } else {
                    this.scrollTo((this.currentScreen - 1) * (this.getWidth() + this.DIVIDER_WIDTH), 0);
                }
            } else if (var1 == this.currentScreen && var2 > 1) {
                --this.currentScreen;
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

    public void setBlinkState(int var1, boolean var2) {
        if (var1 >= 0 && var1 < this.labels.size()) {
            Vector var3 = this.blinks;
            Object var4;
            if (var2) {
                var4 = new Object();
            } else {
                var4 = null;
            }

            var3.set(var1, var4);
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

    public void updateConfig() {
        float var1 = (float)PreferenceTable.clTextSize;
        var1 += var1 / 100.0F * 10.0F;
        this.labels_.setTextSize(resources.dm.density * var1);
        this.effect.setColor(ColorScheme.getColor(49));
        this.effect.setAlpha(160);
        this.effect.setTextSize(this.labels_.getTextSize());
        this.highlight_.setColorFilter(new LightingColorFilter(0, ColorScheme.getColor(49)));
        this.PANEL_HEIGHT = (int)((var1 / 100.0F * 70.0F + var1) * resources.dm.density);
        this.DIVIDER_WIDTH = (int)(0.0F * resources.dm.density);
        this.requestLayout();
    }

    public void updateLabel(int var1, String var2) {
        if (var1 >= 0 && var1 < this.labels.size()) {
            this.labels.set(var1, var2);
            this.invalidate();
        }

    }
}