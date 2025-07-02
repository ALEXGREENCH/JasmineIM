package ru.ivansuper.jasmin.slide_tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
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

/**
 * SlideSwitcher is a ViewGroup that allows horizontal sliding between its child views.
 * Supports various 3D and fade animations, optional wrapping, and a floating label panel.
 */
public class SlideSwitcher extends ViewGroup {
    // Animation types
    public static final int ANIM_CUBE           = 0;
    public static final int ANIM_FLIP_1         = 1;
    public static final int ANIM_FLIP_2         = 2;
    /** @noinspection unused*/
    public static final int ANIM_FLIP_SIMPLE    = 3;
    public static final int ANIM_ROTATE_1       = 4;
    public static final int ANIM_ROTATE_2       = 5;
    public static final int ANIM_ROTATE_3       = 6;
    public static final int ANIM_FADE           = 7;
    public static final int ANIM_SNAKE          = 8;
    public static final int ANIM_FADE_ROTATE    = 9;
    public static final int ANIM_ICS_2          = 10;

    private int animationType      = ANIM_FADE_ROTATE;
    private boolean randomizedAnimation = false;

    private final Vector<String> screenLabels = new Vector<>();
    private final Vector<Object> blinkStates   = new Vector<>();

    private int currentScreen = 0;
    private boolean isAnimating = false;
    private boolean isFrozen    = false;
    private boolean isFullyLocked = false;

    private boolean isDragging = false;
    private boolean isLocked   = false;

    private float lastTouchX;
    private float lastTouchY;
    private float accumulatedScrollX;

    private Scroller scroller;

    // Panel
    public Drawable panelDrawable;
    private int panelHeight;
    private boolean showPanel = false;

    private TextPaint labelPaint;
    private Paint effectPaint;
    private Paint fadePaint;
    private Shader fadeShader;
    private Matrix fadeMatrix;
    private float fadeLength;

    // Highlight tab
    public BitmapDrawable highlightDrawable;
    private Paint highlightPaint;

    // Scroll & divider config
    private final int dividerWidth  = 1;
    private final int scrollDuration = 280;

    /** @noinspection unused*/ // Blink effect
    private final int blinkAlpha     = 0;
    /** @noinspection unused*/
    private final int alphaDirection = 10;

    // Wrapping
    private boolean wrapMode      = false;
    private int wrapDirection     = 0; // -1 = to last, +1 = to first

    private int textColor;

    public SlideSwitcher(Context context) {
        super(context);
        init(context);
    }

    public SlideSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void init(Context context) {
        setWillNotDraw(true);
        setDrawingCacheEnabled(false);
        setWillNotCacheDrawing(true);
        setStaticTransformationsEnabled(true);

        // Label paint
        labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(ColorScheme.getColor(49));
        labelPaint.setShadowLayer(1f, 0, 0, 0xFFCCCCCC);

        // Effect outline paint
        effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        effectPaint.setStyle(Paint.Style.STROKE);
        effectPaint.setStrokeWidth(4f);
        effectPaint.setAlpha(160);

        // Panel drawable
        panelDrawable = getContext().getResources().getDrawable(R.drawable.slide_switcher_panel);
        textColor = ColorScheme.getColor(49);

        // Highlight tab
        highlightDrawable = resources.convertToMyFormat(resources.tab_highlight);
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightDrawable.setCustomPaint(highlightPaint);
        resources.attachSlidePanel(this);

        // Scroller
        scroller = new Scroller(context, new DecelerateInterpolator());

        // Fade shader
        fadeLength = 16f * resources.dm.density;
        fadeShader = new LinearGradient(0, 0, 0, fadeLength, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
        fadePaint = new Paint();
        fadePaint.setShader(fadeShader);
        fadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        fadeMatrix = new Matrix();

        updateConfig();
    }

    /**
     * Update text sizes and panel height based on preferences.
     */
    public void updateConfig() {
        float baseText = PreferenceTable.clTextSize;
        float scaled = baseText * 1.1f * resources.dm.density;
        labelPaint.setTextSize(scaled);
        effectPaint.setTextSize(scaled);
        effectPaint.setColor(ColorScheme.getColor(49));

        panelHeight = (int)((scaled * 1.7f));
        requestLayout();
    }

    /**
     * Add a child view with an associated label.
     */
    public void addView(View child, String label) {
        screenLabels.add(label);
        blinkStates.add(null);
        if (child.getLayoutParams() == null) {
            child.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        super.addView(child);
    }

    public void updateLabel(int index, String text) {
        if (index >= 0 && index < screenLabels.size()) {
            screenLabels.set(index, text);
            invalidate();
        }
    }

    public void setBlinkState(int index, boolean blink) {
        if (index >= 0 && index < blinkStates.size()) {
            blinkStates.set(index, blink ? new Object() : null);
            invalidate();
        }
    }

    /** @noinspection unused*/
    public void setFullyLocked(boolean locked) {
        isFullyLocked = locked;
    }

    /** @noinspection unused*/
    public void freezeInvalidation(boolean freeze) {
        isFrozen = freeze;
        invalidate();
    }

    /** @noinspection unused*/
    public void showPanel(boolean show) {
        showPanel = show;
        requestLayout();
    }

    public void setAnimationType(int type) {
        animationType = type;
        invalidate();
    }

    /** @noinspection unused*/
    public int getAnimationType() {
        return animationType;
    }

    public void setRandomizedAnimation(boolean random) {
        randomizedAnimation = random;
    }

    private void handleAnimationStart() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.setDrawingCacheEnabled(true);
            v.setWillNotCacheDrawing(false);
        }
    }

    private void handleAnimationEnd() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.setDrawingCacheEnabled(false);
            v.setWillNotCacheDrawing(true);
        }
    }

    private void setAnimating(boolean anim) {
        if (anim) {
            if (!isAnimating) {
                handleAnimationStart();
                isAnimating = true;
            }
        } else {
            if (isAnimating) {
                handleAnimationEnd();
                isAnimating = false;
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void invalidateOnAnimationCompat() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            postInvalidate();
        }
    }

    @Override
    public void computeScroll() {
        if (!isDragging && scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), 0);
            invalidateOnAnimationCompat();
        } else if (!isDragging) {
            if (wrapMode) {
                int width = getWidth() + dividerWidth;
                if (wrapDirection < 0) {
                    scrollTo((getChildCount()-1)*width, 0);
                } else {
                    scrollTo(0,0);
                }
                wrapDirection = 0;
                setAnimating(false);
                wrapMode = false;
            } else {
                setAnimating(false);
            }
        }
    }

    private void wrapToFirst() {
        wrapMode = true;
        wrapDirection = 1;
        int width = getWidth() + dividerWidth;
        scroller.startScroll(getScrollX(),0,0,0,scrollDuration);
        scroller.setFinalX(getChildCount()*width);
        invalidateOnAnimationCompat();
    }

    private void wrapToLast() {
        wrapMode = true;
        wrapDirection = -1;
        int width = getWidth() + dividerWidth;
        scroller.startScroll(getScrollX(),0,0,0,scrollDuration);
        scroller.setFinalX(-width);
        invalidateOnAnimationCompat();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View focused = getChildAt(currentScreen);
        if (focused!=null && focused.dispatchKeyEvent(event)) return true;
        if (event.getAction()==KeyEvent.ACTION_DOWN && scroller.isFinished()) {
            if (event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT && !isFullyLocked) {
                switchToPrevious(); return true;
            } else if (event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT && !isFullyLocked) {
                switchToNext(); return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void switchToNext() {
        if (isFullyLocked) return;
        if (currentScreen==getChildCount()-1) {
            currentScreen=0; wrapToFirst();
        } else {
            currentScreen++;
            int width = getWidth()+dividerWidth;
            scroller.startScroll(getScrollX(),0,0,0,scrollDuration);
            scroller.setFinalX(currentScreen*width);
            invalidateOnAnimationCompat();
        }
    }

    private void switchToPrevious() {
        if (isFullyLocked) return;
        if (currentScreen==0) {
            currentScreen=getChildCount()-1; wrapToLast();
        } else {
            currentScreen--;
            int width = getWidth()+dividerWidth;
            scroller.startScroll(getScrollX(),0,0,0,scrollDuration);
            scroller.setFinalX(currentScreen*width);
            invalidateOnAnimationCompat();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (wrapMode || getChildCount()==0) return false;
        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLocked=false;
                accumulatedScrollX=ev.getX();
                lastTouchX=accumulatedScrollX;
                lastTouchY=ev.getY();
                if (!scroller.isFinished()) {
                    isDragging=true;
                    scroller.forceFinished(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = lastTouchX-ev.getX();
                float dy = Math.abs(ev.getY()-lastTouchY);
                if (isDragging) {
                    scrollBy((int)dx,0);
                    lastTouchX=ev.getX();
                } else if (Math.abs(lastTouchX-ev.getX())>32 && dy<32 && !isLocked && !isFullyLocked) {
                    isDragging=true;
                    accumulatedScrollX=ev.getX();
                    lastTouchX=accumulatedScrollX;
                    if (randomizedAnimation) {
                        animationType=new Random().nextInt(8);
                    }
                    setAnimating(true);
                } else if (dy>32) {
                    isLocked=true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    isDragging=false;
                    float diff = ev.getX()-accumulatedScrollX;
                    if (Math.abs(diff)>96 && !isFullyLocked) {
                        if (diff<0) switchToNext(); else switchToPrevious();
                    } else {
                        scroller.startScroll(getScrollX(),0,0,0,scrollDuration);
                        scroller.setFinalX(currentScreen*(getWidth()+dividerWidth));
                    }
                    invalidateOnAnimationCompat();
                }
                break;
        }
        if (!isDragging) return super.dispatchTouchEvent(ev);
        super.dispatchTouchEvent(MotionEvent.obtain(ev.getDownTime(),ev.getEventTime(),MotionEvent.ACTION_CANCEL,ev.getX(),ev.getY(),0));
        return false;
    }

    @Override
    public void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        int w=MeasureSpec.getSize(widthMeasureSpec);
        int h=MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(w,h);
    }

    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        scrollTo(currentScreen*(getWidth()+dividerWidth),0);
        requestLayout();
    }

    @Override
    protected boolean getChildStaticTransformation(View child,Transformation t) {
        int scrollX=getScrollX();
        int idx=indexOfChild(child);
        int childCount=getChildCount();
        int width=getWidth()+dividerWidth;
        boolean wrapStart=scrollX>=(width*childCount-getWidth());
        boolean wrapEnd=scrollX<0;
        int shift=0;
        if (wrapEnd && idx==childCount-1) shift=-width*childCount;
        if (wrapStart && idx==0) shift=width*childCount;
        int center=child.getLeft()+shift;
        int dist=scrollX-center;
        t.clear();
        Matrix m=t.getMatrix();
        switch(animationType) {
            case ANIM_CUBE:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCube(m,child.getWidth(),child.getHeight(),(dist*180f)/child.getWidth(),dist);
                break;
            case ANIM_FLIP_1:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolyCubeInv(m,child.getWidth(),child.getHeight(),(dist*180f)/child.getWidth(),dist);
                break;
            case ANIM_FLIP_2:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyTransformationFlip2((dist*180f)/child.getWidth(),child.getWidth()/2f,child.getHeight()/2f,m);
                break;
            case ANIM_ROTATE_1:
                m.postRotate((dist*180f)/child.getWidth(),child.getWidth()/2f,child.getHeight()/2f);
                break;
            case ANIM_ROTATE_2:
                m.postRotate((-dist*90f)/child.getWidth(),child.getWidth()/2f,child.getHeight());
                break;
            case ANIM_ROTATE_3:
                m.postRotate((dist*90f)/child.getWidth(),child.getWidth()/2f,0f);
                break;
            case ANIM_FADE:
                t.setTransformationType(Transformation.TYPE_BOTH);
                float alphaFac=Math.abs(dist/(float)child.getWidth());
                t.setAlpha(1f-alphaFac);
                if (dist<0) {
                    float factor=alphaFac/7f;
                    m.postScale(1f-factor,1f-factor,child.getWidth()/2f,child.getHeight()/2f);
                    m.postTranslate(dist,0f);
                }
                break;
            case ANIM_SNAKE:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyPolySnake(m,child.getWidth(),child.getHeight(),(dist*180f)/child.getWidth(),dist);
                break;
            case ANIM_FADE_ROTATE:
                t.setTransformationType(Transformation.TYPE_BOTH);
                t.setAlpha(1f-Math.abs(dist/(float)child.getWidth()));
                m.postRotate((dist*90f)/child.getWidth(),0f,0f);
                m.postTranslate(dist,0f);
                break;
            case ANIM_ICS_2:
                t.setTransformationType(Transformation.TYPE_MATRIX);
                Transform.applyTransformationFlip2((dist*20f)/child.getWidth(),child.getWidth()/2f,child.getHeight()/2f,m);
                break;
        }
        m.postTranslate(shift,0);
        return true;
    }

    @Override
    protected boolean drawChild(Canvas canvas,View child,long time) {
        boolean wrapEnd=getScrollX()<0;
        boolean wrapStart=getScrollX()>((getWidth()+dividerWidth)*getChildCount()-getWidth());
        int idx=indexOfChild(child);
        boolean last=(idx==getChildCount()-1), first=(idx==0);
        if (wrapEnd&&last||wrapStart&&first||isChildVisible(child)) {
            return super.drawChild(canvas,child,time);
        }
        return false;
    }

    private boolean isChildVisible(View child) {
        Rect r=new Rect(child.getLeft(),child.getTop(),child.getRight(),child.getBottom());
        Rect display=new Rect(getScrollX(),0,getScrollX()+getWidth(),getHeight());
        return r.intersect(display);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (isFrozen) return;
        super.dispatchDraw(canvas);
        if (!showPanel) return;
        float scrollX=getScrollX();
        int width=getWidth()+dividerWidth;
        float half=width/2f;
        panelDrawable.setBounds((int)scrollX,0,(int)(scrollX+width),panelHeight);
        panelDrawable.draw(canvas);
        int save=canvas.saveLayer(scrollX,0,scrollX+width,panelHeight,null,Canvas.ALL_SAVE_FLAG);
        float textHeight=-labelPaint.getFontMetrics().ascent-labelPaint.getFontMetrics().descent;
        int count=screenLabels.size();
        for(int i=-2;i<count+2;i++){
            String lbl; boolean blink;
            int sx=getScrollX();
            float x=(sx/2f)+(i*half);
            int idx;
            if(i<0){ idx=(count+i%count)%count; }
            else if(i>=count){ idx=i%count; }
            else{ idx=i; }
            lbl=screenLabels.get(idx);
            blink=(blinkStates.get(idx)!=null);
            float textW=labelPaint.measureText(lbl);
            float left=(x+half)-(textW/2f);
            if(left+textW>sx&&left<sx+width){
                float dist=((sx+half)-(textW/2f))-left;
                int alpha=255-(int)(Math.abs(dist)*255/(0.65f*width));
                alpha=Math.max(0,Math.min(255,alpha));
                float y=(panelHeight/2f)+(textHeight/2f);
                if(!blink) canvas.drawText(lbl,left,y,effectPaint);
                highlightDrawable.setBounds((int)x,0,(int)(x+width),panelHeight);
                highlightPaint.setAlpha(alpha);
                highlightDrawable.draw(canvas);
                labelPaint.setStyle(Paint.Style.STROKE);
                labelPaint.setColor(0xFF000000);
                labelPaint.setAlpha(blink?alpha:255);
                canvas.drawText(lbl,left,y,labelPaint);
                labelPaint.setStyle(Paint.Style.FILL);
                labelPaint.setColor(blink?0xFFFFFFFF:textColor);
                labelPaint.setAlpha(blink?255:alpha);
                canvas.drawText(lbl,left,y,labelPaint);
            }
        }
        // draw fade edges
        fadeMatrix.reset(); fadeMatrix.setRotate(-90);
        fadeShader.setLocalMatrix(fadeMatrix);
        canvas.translate(scrollX,0);
        canvas.drawRect(0,0,fadeLength,panelHeight,fadePaint);
        fadeMatrix.reset(); fadeMatrix.setRotate(90); fadeMatrix.postTranslate(fadeLength,0);
        fadeShader.setLocalMatrix(fadeMatrix);
        canvas.translate(getWidth()-fadeLength,0);
        canvas.drawRect(0,0,fadeLength,panelHeight,fadePaint);
        canvas.restoreToCount(save);
    }

    @Override
    protected void onLayout(boolean changed,int l,int t,int r,int b) {
        int top=showPanel?panelHeight:0;
        int h=getHeight()-top;
        int w=getWidth()+dividerWidth;
        for(int i=0;i<getChildCount();i++){
            View c=getChildAt(i);
            c.measure(MeasureSpec.makeMeasureSpec(getWidth(),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(h,MeasureSpec.EXACTLY));
            c.layout(l+i*w,top,r+i*w,b);
        }
    }

    @Override
    public void removeViewAt(int index) {
        if(index<0||index>=getChildCount()) return;
        super.removeViewAt(index);
        screenLabels.remove(index);
        blinkStates.remove(index);
        if(index<currentScreen) currentScreen--;
        else if(index==currentScreen&&getChildCount()>0) currentScreen=Math.max(0,currentScreen-1);
        scrollTo(currentScreen*(getWidth()+dividerWidth),0);
    }

    public void scrollTo(int screen) {
        int child_count = getChildCount();
        if (child_count > 0 && screen < child_count) {
            scrollTo((getWidth() + this.dividerWidth) * screen, 0);
            this.currentScreen = screen;
        }
    }

    /**
     * Clears cache on child MultiColumnList views
     */
    public void clearupCaches() {
        for(int i=0;i<getChildCount();i++){
            View c=getChildAt(i);
            if(c instanceof MultiColumnList) {
                try { ((MultiColumnList)c).clearup(); } catch(Exception ignored){}
            }
        }
    }


    public void setLock(boolean locked) {
        this.isLocked = locked;
    }

    public void togglePanel(boolean show) {
        this.showPanel = show;
        requestLayout();
    }
}