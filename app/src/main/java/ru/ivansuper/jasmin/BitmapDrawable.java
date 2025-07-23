package ru.ivansuper.jasmin;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;

import java.io.InputStream;

/**
 * A Drawable that wraps a bitmap and can be tiled, stretched, or aligned. You can create a
 * BitmapDrawable from a file path, an input stream, through XML inflation, or from a {@link
 * Bitmap} object.
 *
 * <p>It can be defined in an XML file with the <code>&lt;bitmap&gt;</code> element. For more
 * information, see the guide to <a
 * href="{@docRoot}guide/topics/resources/drawable-resource.html">Drawable Resources</a>.
 *
 * <p>Also see the {@link Bitmap} class, which handles the management and
 * transformation of raw bitmap pixels.
 *
 * @attr ref android.R.styleable#BitmapDrawable_src
 * @attr ref android.R.styleable#BitmapDrawable_antialias
 * @attr ref android.R.styleable#BitmapDrawable_filter
 * @attr ref android.R.styleable#BitmapDrawable_dither
 * @attr ref android.R.styleable#BitmapDrawable_gravity
 * @attr ref android.R.styleable#BitmapDrawable_tileMode
 */
public class BitmapDrawable extends Drawable {

    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;

    private final Rect mDstRect = new Rect();

    private Bitmap mBitmap;
    private BitmapState mBitmapState;
    private int mBitmapWidth = -1;
    private int mBitmapHeight = -1;
    private int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

    private boolean mApplyGravity = true;
    private boolean mRebuildShader = false;
    private boolean mMutated = false;

    private Paint mPaint;

    // Constructors
    @Deprecated
    public BitmapDrawable() {
        this.mBitmapState = new BitmapState((Bitmap) null);
    }

    public BitmapDrawable(Resources res) {
        this.mBitmapState = new BitmapState((Bitmap) null);
        this.mBitmapState.mTargetDensity = res.getDisplayMetrics().densityDpi;
    }

    public BitmapDrawable(Bitmap bitmap) {
        this(new BitmapState(bitmap), null);
    }

    public BitmapDrawable(Resources res, Bitmap bitmap) {
        this(new BitmapState(bitmap), res);
        this.mBitmapState.mTargetDensity = this.mTargetDensity;
    }

    @Deprecated
    public BitmapDrawable(String filepath) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), null);
        if (this.mBitmap == null) {
            Log.w("BitmapDrawable", "Cannot decode: " + filepath);
        }
    }

    public BitmapDrawable(Resources res, String filepath) {
        this(new BitmapState(BitmapFactory.decodeFile(filepath)), res);
        if (this.mBitmap == null) {
            Log.w("BitmapDrawable", "Cannot decode: " + filepath);
        }
    }

    @Deprecated
    public BitmapDrawable(InputStream is) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), null);
        if (this.mBitmap == null) {
            Log.w("BitmapDrawable", "Cannot decode InputStream");
        }
    }

    public BitmapDrawable(Resources res, InputStream is) {
        this(new BitmapState(BitmapFactory.decodeStream(is)), res);
        if (this.mBitmap == null) {
            Log.w("BitmapDrawable", "Cannot decode InputStream");
        }
    }

    public final Paint getPaint() {
        return mBitmapState.mPaint;
    }

    public final Bitmap getBitmap() {
        return mBitmap;
    }

    public final void setCustomPaint(Paint paint) {
        this.mPaint = paint;
    }

    /** @noinspection unused*/
    public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }

    /** @noinspection unused*/
    public void setTargetDensity(DisplayMetrics metrics) {
        setTargetDensity(metrics.densityDpi);
    }

    public void setTargetDensity(int density) {
        if (density == 0) density = 160;
        this.mTargetDensity = density;
        if (mBitmap != null) computeBitmapSize();
    }

    public int getGravity() {
        return mBitmapState.mGravity;
    }

    public void setGravity(int gravity) {
        mBitmapState.mGravity = gravity;
        mApplyGravity = true;
    }

    /** @noinspection unused*/
    public void setAntiAlias(boolean aa) {
        mBitmapState.mPaint.setAntiAlias(aa);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mBitmapState.mPaint.setFilterBitmap(filter);
    }

    @Override
    public void setDither(boolean dither) {
        mBitmapState.mPaint.setDither(dither);
    }

    /** @noinspection unused*/
    public Shader.TileMode getTileModeX() {
        return mBitmapState.mTileModeX;
    }

    /** @noinspection unused*/
    public Shader.TileMode getTileModeY() {
        return mBitmapState.mTileModeY;
    }

    /** @noinspection unused*/
    public void setTileModeX(Shader.TileMode mode) {
        setTileModeXY(mode, mBitmapState.mTileModeY);
    }

    /** @noinspection unused*/
    public void setTileModeY(Shader.TileMode mode) {
        setTileModeXY(mBitmapState.mTileModeX, mode);
    }

    public void setTileModeXY(Shader.TileMode xmode, Shader.TileMode ymode) {
        if (mBitmapState.mTileModeX != xmode || mBitmapState.mTileModeY != ymode) {
            mBitmapState.mTileModeX = xmode;
            mBitmapState.mTileModeY = ymode;
            mRebuildShader = true;
        }
    }

    /** @noinspection NullableProblems*/
    @Override
    public void draw(Canvas canvas) {
        if (mBitmap == null) return;

        final BitmapState state = mBitmapState;

        if (mRebuildShader) {
            Shader.TileMode tmx = state.mTileModeX != null ? state.mTileModeX : Shader.TileMode.CLAMP;
            Shader.TileMode tmy = state.mTileModeY != null ? state.mTileModeY : Shader.TileMode.CLAMP;

            state.mPaint.setShader(new BitmapShader(mBitmap, tmx, tmy));
            mRebuildShader = false;
            copyBounds(mDstRect);
        }

        Shader shader = state.mPaint.getShader();

        if (shader == null) {
            if (mApplyGravity) {
                Gravity.apply(state.mGravity, mBitmapWidth, mBitmapHeight, getBounds(), mDstRect);
                mApplyGravity = false;
            }
            Paint paint = (mPaint != null) ? mPaint : state.mPaint;
            canvas.drawBitmap(mBitmap, null, mDstRect, paint);
        } else {
            if (mApplyGravity) {
                mDstRect.set(getBounds());
                mApplyGravity = false;
            }
            canvas.drawRect(mDstRect, state.mPaint);
        }
    }

    /** @noinspection unused*/
    public static void draw(Canvas canvas, Bitmap bitmap, Rect rect, int x, int y, Paint paint) {
        int save = canvas.save();
        // Здесь возможно раньше была трансформация или поворот
        canvas.restoreToCount(save);
    }

    @Override
    public void setAlpha(int alpha) {
        mBitmapState.mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mBitmapState.mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return (mBitmapState.mGravity == Gravity.FILL && mBitmap != null && !mBitmap.hasAlpha()
                && mBitmapState.mPaint.getAlpha() >= 255) ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }

    @Override
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mApplyGravity = true;
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mBitmapState.mChangingConfigurations;
    }

    /** @noinspection UnclearExpression, NullableProblems */
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mBitmapState = new BitmapState(mBitmapState);
            mMutated = true;
        }
        return this;
    }

    @Override
    public ConstantState getConstantState() {
        mBitmapState.mChangingConfigurations = getChangingConfigurations();
        return mBitmapState;
    }

    private void computeBitmapSize() {
        mBitmapWidth = mBitmap.getScaledWidth(mTargetDensity);
        mBitmapHeight = mBitmap.getScaledHeight(mTargetDensity);
    }

    private void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (bitmap != null) {
            computeBitmapSize();
        } else {
            mBitmapWidth = -1;
            mBitmapHeight = -1;
        }
    }

    private BitmapDrawable(BitmapState state, Resources res) {
        this.mBitmapState = state;
        this.mTargetDensity = (res != null)
                ? res.getDisplayMetrics().densityDpi
                : (state != null ? state.mTargetDensity : DisplayMetrics.DENSITY_DEFAULT);
        //noinspection DataFlowIssue
        setBitmap(state.mBitmap);
    }

    /** @noinspection unused*/
    BitmapDrawable(BitmapState state, Resources res, BitmapDrawable dummy) {
        this(state, res);
    }


    static final class BitmapState extends ConstantState {
        Bitmap mBitmap;
        int mChangingConfigurations;
        int mGravity = Gravity.FILL;
        Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);
        int mTargetDensity = 160;
        Shader.TileMode mTileModeX;
        Shader.TileMode mTileModeY;

        BitmapState(Bitmap bitmap) {
            this.mBitmap = bitmap;
        }

        BitmapState(BitmapState copy) {
            this.mBitmap = copy.mBitmap;
            this.mChangingConfigurations = copy.mChangingConfigurations;
            this.mGravity = copy.mGravity;
            this.mTileModeX = copy.mTileModeX;
            this.mTileModeY = copy.mTileModeY;
            this.mTargetDensity = copy.mTargetDensity;
            this.mPaint = new Paint(copy.mPaint);
        }

        /** @noinspection NullableProblems*/
        @Override
        public Drawable newDrawable() {
            return new BitmapDrawable(this, null);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }
    }
}
