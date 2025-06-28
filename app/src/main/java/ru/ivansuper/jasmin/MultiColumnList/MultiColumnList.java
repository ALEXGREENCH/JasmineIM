package ru.ivansuper.jasmin.MultiColumnList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.Scroller;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import ru.ivansuper.jasmin.BitmapDrawable;
import ru.ivansuper.jasmin.MMP.MMPProtocol;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.resources;

@SuppressLint("ViewConstructor")
public class MultiColumnList extends ViewGroup {
    /** @noinspection unused*/
    public static final int LONG_CLICK_TIMEOUT = 500;
    private static int OVERSCROLL_EFFECT_AMOUNT = 192;
    private boolean IS_TOUCHED;
    private int dy;
    private boolean freezed;
    private boolean keyboard_used;
    private MultiColumnAdapter mAdapter;
    private int mBottomOverScrollAmount;
    private int mBottomViewIndex;
    private int mColumnsNumber;
    protected int mCurrentY;
    private boolean mDataChanged;
    private DataSetObserver mDataObserver;
    private int mDisplayOffset;
    private boolean mDrawSelector;
    private float mLastMoveY;
    private float mLastMoveY_;
    private float mLastTouchY;
    private int mMaxY;
    protected int mNextY;
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private BitmapDrawable mOverscroll;
    private final Queue<View> mRemovedViewQueue;
    private boolean mRenderOverscroll;
    protected Scroller mScroller;
    private boolean mScrolling;
    private int mSelectedViewIndex;
    private Drawable mSelector;
    private Rect mSelectorRect;
    private final boolean mSmoothScrollbarEnabled;
    private int mTopOverScrollAmount;
    private int mTopViewIndex;
    private long mTouchTime;
    /** @noinspection unused*/
    private Paint overscroll_color_filter;

    public interface OnItemClickListener {
        void onItemClick(MultiColumnList multiColumnList, View view, int i, long j);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(MultiColumnList multiColumnList, View view, int i, long j);
    }

    public MultiColumnList(Context context, TypedArray attrs) {
        super(context);
        this.mTopOverScrollAmount = 0;
        this.mBottomOverScrollAmount = 0;
        this.mDrawSelector = false;
        this.mTopViewIndex = -1;
        this.mBottomViewIndex = 0;
        this.mSelectedViewIndex = 0;
        this.mMaxY = Integer.MAX_VALUE;
        this.mDisplayOffset = 0;
        this.mRemovedViewQueue = new ArrayBlockingQueue<>(32);
        this.mDataChanged = false;
        this.mColumnsNumber = 1;
        this.mLastTouchY = 0.0f;
        this.mLastMoveY = 0.0f;
        this.mLastMoveY_ = 0.0f;
        this.mScrolling = false;
        this.mTouchTime = 0L;
        this.mSmoothScrollbarEnabled = true;
        this.IS_TOUCHED = false;
        this.mRenderOverscroll = false;
        this.keyboard_used = false;
        this.mDataObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                synchronized (MultiColumnList.this) {
                    MultiColumnList.this.mDataChanged = true;
                }
                MultiColumnList.this.invalidate();
                MultiColumnList.this.requestLayout();
            }

            @Override
            public void onInvalidated() {
                MultiColumnList.this.reset();
                MultiColumnList.this.invalidate();
                MultiColumnList.this.requestLayout();
            }
        };
        initView(context, attrs);
    }

    /** @noinspection unused*/
    private void initView(Context context, TypedArray attrs) {
        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);
        Log.e(getClass().getSimpleName(), "Heap: " + resources.DEVICE_HEAP_SIZE);
        setDrawingCacheQuality(resources.DEVICE_HEAP_SIZE > 24 ? View.DRAWING_CACHE_QUALITY_HIGH : View.DRAWING_CACHE_QUALITY_LOW);
        setStaticTransformationsEnabled(true);
        OVERSCROLL_EFFECT_AMOUNT = (int) (128.0f * resources.dm.density);
        if (OVERSCROLL_EFFECT_AMOUNT == 0) {
            OVERSCROLL_EFFECT_AMOUNT = 1;
        }
        this.mTopOverScrollAmount = 0;
        this.mBottomOverScrollAmount = 0;
        this.mTopViewIndex = -1;
        this.mBottomViewIndex = 0;
        this.mDisplayOffset = 0;
        this.mCurrentY = 0;
        this.mNextY = 0;
        this.mMaxY = Integer.MAX_VALUE;
        this.mOverscroll = resources.convertToMyFormat(getResources().getDrawable(R.drawable.overscroll));
        this.mOverscroll.getPaint().setColorFilter(new LightingColorFilter(0, ColorScheme.getColor(39)));
        this.mSelector = getResources().getDrawable(R.drawable.contactlist_selector);
        this.mSelectorRect = new Rect();
        LinearInterpolator i = new LinearInterpolator();
        this.mScroller = new Scroller(getContext(), i);
        setVerticalScrollBarEnabled(true);
        // todo;
        /////initializeScrollbars(attrs);
        setVerticalScrollBarEnabled(true);
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(48);
    }

    @Override
    public final boolean isInEditMode() {
        return false;
    }

    @Override
    public final void onMeasure(int w, int h) {
        int width = View.MeasureSpec.getSize(w);
        int height = View.MeasureSpec.getSize(h);
        setMeasuredDimension(width, height);
    }

    private synchronized void initView() {
        this.mTopOverScrollAmount = 0;
        this.mBottomOverScrollAmount = 0;
        this.mTopViewIndex = -1;
        this.mBottomViewIndex = 0;
        this.mDisplayOffset = 0;
        this.mCurrentY = 0;
        this.mNextY = 0;
        this.mMaxY = Integer.MAX_VALUE;
    }

    @Override
    public final void destroyDrawingCache() {
    }

    private synchronized void initViewA() {
        this.mTopViewIndex = -1;
        this.mBottomViewIndex = 0;
        this.mDisplayOffset = 0;
        this.mCurrentY = 0;
        this.mMaxY = Integer.MAX_VALUE;
    }

    public final void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClicked = listener;
    }

    public final void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClicked = listener;
    }

    /** @noinspection unused*/
    public final OnItemClickListener getOnItemClickListenerA() {
        return this.mOnItemClicked;
    }

    /** @noinspection unused*/
    public final OnItemLongClickListener getOnItemLongClickListenerA() {
        return this.mOnItemLongClicked;
    }

    public final void setColumnsNumber(int count) {
        if (count < 1) {
            count = 1;
        }
        this.mColumnsNumber = count;
        if (this.mDataObserver != null) {
            this.mDataObserver.onInvalidated();
        }
    }

    public final void setListSelector(Drawable selector) {
        this.mSelector = selector;
        invalidate();
    }

    public final int getColumnsNumber() {
        return this.mColumnsNumber;
    }

    public final MultiColumnAdapter getAdapter() {
        return this.mAdapter;
    }

    /** @noinspection unused*/
    public final View getSelectedView() {
        return null;
    }

    public final void setAdapter(MultiColumnAdapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataObserver);
        }
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(this.mDataObserver);
        reset();
    }

    /** @noinspection unused*/
    public final void setSelection(int position) {
    }

    public final void freezeInvalidating(boolean freezed) {
        this.freezed = freezed;
        invalidate();
    }

    private void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    private void addAndMeasureChild(View child, int viewPos, boolean its_group) {
        int width;
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(-1, -1);
        }
        addViewInLayout(child, viewPos, params, true);
        if (its_group) {
            width = getWidth();
        } else {
            width = ((getWidth() - getPaddingLeft()) - getPaddingRight()) / this.mColumnsNumber;
        }
        child.measure(View.MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.AT_MOST));
    }

    @Override
    protected final boolean getChildStaticTransformation(View child, Transformation t) {
        t.clear();
        Matrix m = t.getMatrix();
        m.reset();
        t.setTransformationType(Transformation.TYPE_MATRIX);
        if (this.mTopViewIndex < 0) {
            if (this.mTopOverScrollAmount == 0) {
                return false;
            }
            float dist_top = getMeasuredHeight() - child.getTop();
            if (dist_top < 0.0f) {
                dist_top = 0.0f;
            }
            float factor = dist_top / 32.0f;
            if (factor < 0.0f) {
                factor = 0.0f;
            }
            float amount = this.mTopOverScrollAmount / factor;
            if (amount < 0.0f) {
                amount = 0.0f;
            }
            m.preTranslate(0.0f, amount);
        } else {
            if (this.mBottomOverScrollAmount == 0) {
                return false;
            }
            float dist_bottom = child.getBottom();
            if (dist_bottom < 0.0f) {
                dist_bottom = 0.0f;
            }
            float factor2 = dist_bottom / 32.0f;
            if (factor2 < 0.0f) {
                factor2 = 0.0f;
            }
            float amount2 = this.mBottomOverScrollAmount / factor2;
            if (amount2 < 0.0f) {
                amount2 = 0.0f;
            }
            m.preTranslate(0.0f, -amount2);
        }
        return true;
    }

    @Override
    public final void dispatchDraw(Canvas canvas) {
        if ((this.mDrawSelector || this.keyboard_used) && this.mSelector != null) {
            Drawable selector = this.mSelector;
            if (this.keyboard_used) {
                selector.setBounds(getChildRect(this.mSelectedViewIndex));
            } else {
                selector.setBounds(this.mSelectorRect);
            }
            selector.draw(canvas);
        }
        super.dispatchDraw(canvas);
        boolean need_invalidate = false;
        int amount = Math.max((this.mTopOverScrollAmount * 255) / OVERSCROLL_EFFECT_AMOUNT, 0);
        this.mOverscroll.getPaint().setAlpha(amount);
        this.mOverscroll.setBounds(0, 0, getWidth(), OVERSCROLL_EFFECT_AMOUNT);
        this.mOverscroll.draw(canvas);
        if (this.mTopOverScrollAmount > 0) {
            if (this.IS_TOUCHED) {
                this.mTopOverScrollAmount -= 10;
            } else {
                this.mTopOverScrollAmount -= 25;
            }
            need_invalidate = true;
        }
        int amount2 = Math.max((this.mBottomOverScrollAmount * 255) / OVERSCROLL_EFFECT_AMOUNT, 0);
        this.mOverscroll.getPaint().setAlpha(amount2);
        this.mOverscroll.setBounds(0, 0, getWidth(), OVERSCROLL_EFFECT_AMOUNT);
        canvas.save();
        canvas.translate(0.0f, getHeight() - OVERSCROLL_EFFECT_AMOUNT);
        canvas.rotate(180.0f, (float) getWidth() / 2, (float) OVERSCROLL_EFFECT_AMOUNT / 2);
        this.mOverscroll.draw(canvas);
        canvas.restore();
        if (this.mBottomOverScrollAmount > 0) {
            if (this.IS_TOUCHED) {
                this.mBottomOverScrollAmount -= 10;
            } else {
                this.mBottomOverScrollAmount -= 25;
            }
            need_invalidate = true;
        }
        if (need_invalidate && !this.freezed) {
            invalidate();
        }
    }

    private Rect getChildRect(int idx) {
        int local_idx = (idx - this.mTopViewIndex) - 1;
        Rect rect = new Rect();
        View child = getChildAt(local_idx);
        if (child != null) {
            return new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        }
        return rect;
    }

    @Override
    protected final int computeVerticalScrollExtent() {
        int count = getChildCount();
        if (count <= 0) {
            return 0;
        }
        if (this.mSmoothScrollbarEnabled) {
            int extent = count * 100;
            View view = getChildAt(0);
            int top = view.getTop();
            int height = view.getHeight();
            if (height > 0) {
                extent += ((top * 100) / height) * this.mColumnsNumber;
            }
            View view2 = getChildAt(count - 1);
            int bottom = view2.getBottom();
            int height2 = view2.getHeight();
            if (height2 > 0) {
                return extent - ((((bottom - getHeight()) * 100) / height2) * this.mColumnsNumber);
            }
            return extent;
        }
        return 1;
    }

    @Override
    protected final int computeVerticalScrollOffset() {
        int index;
        int firstPosition = this.mTopViewIndex + 1;
        int childCount = getChildCount();
        int mItemCount = 0;
        if (this.mAdapter != null) {
            mItemCount = this.mAdapter.getCount();
        }
        if (firstPosition < 0 || childCount <= 0) {
            return 0;
        }
        if (this.mSmoothScrollbarEnabled) {
            View view = getChildAt(0);
            int top = view.getTop();
            int height = view.getHeight();
            if (height > 0) {
                return Math.max(((firstPosition * 100) - (((top * 100) / height) * this.mColumnsNumber)) + ((int) (((float) getScrollY() / getHeight()) * mItemCount * 100.0f)), 0);
            }
            return 0;
        }
        int count = mItemCount;
        if (firstPosition == 0) {
            index = 0;
        } else if (firstPosition + childCount == count) {
            index = count;
        } else {
            index = firstPosition + (childCount / 2);
        }
        return firstPosition + (childCount * (index / count));
    }

    @Override
    protected final int computeVerticalScrollRange() {
        if (this.mAdapter == null) {
            return 0;
        }
        if (this.mSmoothScrollbarEnabled) {
            return Math.max(this.mAdapter.getCount() * 100, 0);
        }
        return this.mAdapter.getCount();
    }

    @Override
    protected final float getTopFadingEdgeStrength() {
        if (this.mTopViewIndex >= 0) {
            return 1.0f;
        }
        View child = getChildAt(0);
        if (child == null) {
            return 0.0f;
        }
        int height = child.getBottom() - child.getTop();
        int offset = Math.abs(child.getTop());
        return (offset * 1.0f) / height;
    }

    @Override
    protected final float getBottomFadingEdgeStrength() {
        int count = this.mAdapter != null ? this.mAdapter.getCount() : 0;
        if (this.mBottomViewIndex < count) {
            return 1.0f;
        }
        View child = getChildAt(getChildCount() - 1);
        if (child == null) {
            return 0.0f;
        }
        int height = child.getBottom() - child.getTop();
        int offset = Math.abs(child.getBottom()) - getHeight();
        return (offset * 1.0f) / height;
    }

    @Override
    protected final void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
    }

    @Override
    protected final void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
    }

    @Override
    protected final void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
    }

    @Override
    protected final void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mDataChanged = true;
        requestLayout();
    }

    private void removeAllChilds() {
        while (getChildCount() > 0) {
            this.mRemovedViewQueue.offer(getChildAt(0));
            removeViewAt(0);
        }
        if (this.mRemovedViewQueue.size() > 64) {
            this.mRemovedViewQueue.clear();
        }
    }

    @Override
    protected final void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mAdapter != null) {
            if (this.mDataChanged) {
                int oldCurrentY = this.mCurrentY;
                initViewA();
                removeAllChilds();
                this.mNextY = oldCurrentY;
                this.mDataChanged = false;
                this.mRenderOverscroll = true;
            }
            if (this.mScroller.computeScrollOffset()) {
                this.mNextY = this.mScroller.getCurrY();
            }
            if (this.mNextY <= 0) {
                this.mTopOverScrollAmount += Math.abs(this.mNextY * 2);
                if (this.mCurrentY != 0) {
                    this.mTopOverScrollAmount = Math.abs(this.mCurrentY - this.mNextY) * 3;
                }
                if (this.mTopOverScrollAmount > OVERSCROLL_EFFECT_AMOUNT) {
                    this.mTopOverScrollAmount = OVERSCROLL_EFFECT_AMOUNT;
                }
                this.mNextY = 0;
                this.mScroller.forceFinished(true);
            }
            if (this.mNextY > this.mMaxY) {
                if (!this.mRenderOverscroll) {
                    this.mBottomOverScrollAmount += Math.abs((this.mNextY - this.mCurrentY) * 2);
                    if (this.mCurrentY != this.mMaxY) {
                        this.mBottomOverScrollAmount = Math.abs(this.mCurrentY - this.mNextY) * 3;
                    }
                    if (this.mBottomOverScrollAmount > OVERSCROLL_EFFECT_AMOUNT) {
                        this.mBottomOverScrollAmount = OVERSCROLL_EFFECT_AMOUNT;
                    }
                }
                this.mNextY = this.mMaxY;
                this.mScroller.forceFinished(true);
            }
            this.mRenderOverscroll = false;
            this.dy = this.mCurrentY - this.mNextY;
            if (this.dy != 0) {
                this.mRenderOverscroll = true;
            }
            fillList();
            if (this.mNextY > this.mMaxY) {
                if (!this.mRenderOverscroll) {
                    this.mBottomOverScrollAmount += Math.abs((this.mNextY - this.mCurrentY) * 2);
                    if (this.mCurrentY != this.mMaxY) {
                        this.mBottomOverScrollAmount = Math.abs(this.mCurrentY - this.mNextY) * 3;
                    }
                    if (this.mBottomOverScrollAmount > OVERSCROLL_EFFECT_AMOUNT) {
                        this.mBottomOverScrollAmount = OVERSCROLL_EFFECT_AMOUNT;
                    }
                }
                this.mNextY = this.mMaxY;
                this.dy = this.mCurrentY - this.mNextY;
                this.mRenderOverscroll = true;
            }
            positionItems();
            removeNonVisibleItems();
            this.mCurrentY = this.mNextY;
            this.mRenderOverscroll = false;
            if (this.keyboard_used) {
                awakenScrollBars(1500);
            }
            if (!this.mScroller.isFinished()) {
                awakenScrollBars();
                post(MultiColumnList.this::requestLayout);
            }
        }
    }

    /** @noinspection unused*/
    private int getOffset() {
        int count = getChildCount();
        if (count == 0) {
            return 0;
        }
        View child = getChildAt(0);
        if ((child != null && child.getTop() == 0 && this.mTopViewIndex == -1) || this.mBottomViewIndex != this.mAdapter.getCount()) {
            return 0;
        }
        int diff = getHeight() - getChildAt(count - 1).getBottom();
        return Math.max(diff, 0);
    }

    private void fillList() {
        int edge = 0;
        View child = getChildAt(getChildCount() - 1);
        if (child != null) {
            edge = child.getBottom();
        }
        fillListBottom(edge);
        int edge2 = 0;
        View child2 = getChildAt(0);
        if (child2 != null) {
            edge2 = child2.getTop();
        }
        fillListTop(edge2);
    }

    private void fillListBottom(int bottomEdge) {
        int height = getMeasuredHeight();
        int count = this.mAdapter.getCount();
        while (this.dy + bottomEdge < height && this.mBottomViewIndex < count) {
            int last_height = addRowToBottom();
            bottomEdge += last_height;
        }
        if (this.mBottomViewIndex >= this.mAdapter.getCount()) {
            this.mMaxY = (this.mCurrentY + bottomEdge) - height;
            if (this.mMaxY < 0) {
                this.mMaxY = 0;
                return;
            }
            return;
        }
        this.mMaxY = Integer.MAX_VALUE;
    }

    private void fillListTop(int topEdge) {
        while (this.dy + topEdge >= 0 && this.mTopViewIndex >= 0) {
            int height = addRowToTop();
            topEdge -= height;
        }
    }

    private int getItemsAvailabled(int idx) {
        int counter = idx;
        int res = 0;
        int type = this.mAdapter.getItemType(counter);
        if (type == -1) {
            return 0;
        }
        if (type == 0) {
            return 1;
        }
        while (true) {
            type = this.mAdapter.getItemType(counter);
            if (type == -1 || type == 0) {
                break;
            }
            counter--;
            res++;
        }
        if (res <= this.mColumnsNumber) {
            return res;
        }
        int last_count = res % this.mColumnsNumber;
        return last_count == 0 ? this.mColumnsNumber : last_count;
    }

    private int getItemsAvailabledBottom(int idx) {
        int counter = idx;
        int res = 0;
        int type = this.mAdapter.getItemType(counter);
        if (type < 0) {
            return 0;
        }
        if (type == 0) {
            return 1;
        }

        while (true) {
            type = this.mAdapter.getItemType(counter);
            if (type <= 0) {
                break;
            }
            counter++;
            res++;
        }

        if (res <= this.mColumnsNumber) {
            return res;
        }

        int last_count = res % this.mColumnsNumber;
        return last_count == 0 ? this.mColumnsNumber : last_count;
    }

    private int addRowToTop() {
        int available = getItemsAvailabled(this.mTopViewIndex);
        if (available == 0) {
            return 0;
        }
        int[] heights = new int[available];
        Arrays.fill(heights, 0);
        int j = 0;
        for (int i = available - 1; i >= 0; i--) {
            View child = this.mAdapter.getView(this.mTopViewIndex, this.mRemovedViewQueue.poll(), this);
            int type = this.mAdapter.getItemType(this.mTopViewIndex);
            this.mTopViewIndex--;
            addAndMeasureChild(child, 0, type == 0);
            int height = child.getMeasuredHeight();
            switch (type) {
                case 0:
                    this.mDisplayOffset -= height;
                    Arrays.fill(heights, 0);
                    return height;
                case 1:
                    heights[j] = height;
                    j++;
                    if (j >= available) {
                        this.mDisplayOffset -= getMax(heights);
                        return height;
                    }
                    break;
            }
        }
        return 0;
    }

    private int addRowToBottom() {
        int available = getItemsAvailabledBottom(this.mBottomViewIndex);
        if (available == 0) {
            return 0;
        }
        int[] heights = new int[available];
        Arrays.fill(heights, 0);
        int j = 0;
        for (int i = 0; i < available; i++) {
            View child = this.mAdapter.getView(this.mBottomViewIndex, this.mRemovedViewQueue.poll(), this);
            int type = this.mAdapter.getItemType(this.mBottomViewIndex);
            this.mBottomViewIndex++;
            switch (type) {
                case 0:
                    addAndMeasureChild(child, -1, true);
                    return child.getMeasuredHeight();
                case 1:
                    addAndMeasureChild(child, -1, false);
                    int height2 = child.getMeasuredHeight();
                    heights[j] = height2;
                    j++;
                    if (i == available - 1) {
                        return getMax(heights);
                    }
                    break;
            }
        }
        return 0;
    }

    /** @noinspection SameParameterValue*/
    private void clearTopViews(int dy) {
        View child = getChildAt(0);
        if (child != null) {
            int offset = 0;
            int[] heights = new int[this.mColumnsNumber];
            Arrays.fill(heights, 0);
            int j = 0;
            while (child != null && child.getBottom() + dy < 0) {
                int type = this.mAdapter.getItemType(this.mTopViewIndex + 1);
                if (type == 0) {
                    offset = offset + (child.getBottom() - child.getTop()) + getMax(heights);
                    Arrays.fill(heights, 0);
                    j = 0;
                } else if (type == 1) {
                    int last_child_height = child.getBottom() - child.getTop();
                    heights[j] = last_child_height;
                    j++;
                    if (j >= heights.length) {
                        int max = getMax(heights);
                        offset += max;
                        Arrays.fill(heights, 0);
                        j = 0;
                    }
                }
                this.mRemovedViewQueue.offer(child);
                removeViewInLayout(child);
                this.mTopViewIndex++;
                child = getChildAt(0);
            }
            this.mDisplayOffset += offset + getMax(heights);
        }
    }

    private void removeNonVisibleItems() {
        if (this.dy < 0) {
            clearTopViews(0);
        }
        if (this.dy > 0) {
            View child = getChildAt(getChildCount() - 1);
            while (child != null && child.getTop() > getMeasuredHeight()) {
                this.mRemovedViewQueue.offer(child);
                removeViewInLayout(child);
                this.mBottomViewIndex--;
                child = getChildAt(getChildCount() - 1);
                this.mMaxY = Integer.MAX_VALUE;
            }
        }
    }

    private void positionItems() {
        int padding_left = getPaddingLeft();
        int padding_right = getPaddingRight();
        int childs_count = 0;
        int step = ((getWidth() - padding_left) - padding_right) / this.mColumnsNumber;
        int shift = padding_left;
        int last_height = 0;
        int last_type = 0;
        View[] views = new View[this.mColumnsNumber + 1];
        int k = 0;
        int[] heights = new int[this.mColumnsNumber];
        Arrays.fill(heights, 0);
        int j = 0;
        int count = getChildCount();
        if (count > 0) {
            this.mDisplayOffset += this.dy;
            int top = this.mDisplayOffset;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                int type = this.mAdapter.getItemType(this.mTopViewIndex + i + 1);
                int childHeight = child.getMeasuredHeight();
                if (type == 0) {
                    if (last_type == 1) {
                        layoutChilds(views, getMax(heights));
                        k = 0;
                        top = last_height;
                    }
                    child.layout(padding_left, top, getWidth() - padding_right, top + childHeight);
                    top += childHeight;
                    childs_count = 0;
                    shift = padding_left;
                    Arrays.fill(heights, 0);
                    j = 0;
                } else if (type == 1) {
                    child.layout(shift, top, shift + step, top);
                    views[k] = child;
                    k++;
                    childs_count++;
                    shift += step;
                    heights[j] = childHeight;
                    j++;
                    if (childs_count >= this.mColumnsNumber || i == count - 1) {
                        layoutChilds(views, getMax(heights));
                        k = 0;
                        top += getMax(heights);
                        last_height = top;
                        shift = padding_left;
                        childs_count = 0;
                        Arrays.fill(heights, 0);
                        j = 0;
                    } else {
                        last_height = top + getMax(heights);
                    }
                }
                last_type = type;
            }
        }
    }

    private void layoutChilds(View[] childs, int height) {
        for (View child : childs) {
            if (child == null) {
                break;
            }
            child.layout(child.getLeft(), child.getTop(), child.getRight(), child.getTop() + height);
        }
        Arrays.fill(childs, null);
    }

    /** @noinspection unused*/
    public final void scrollTo(int y) {
        this.mScroller.startScroll(0, this.mNextY, 0, y - this.mNextY);
        requestLayout();
    }

    private void onTouchDown(float X, float Y) {
        Rect viewRect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            int left = child.getLeft();
            int right = child.getRight();
            int top = child.getTop();
            int bottom = child.getBottom();
            viewRect.set(left, top, right, bottom);
            if (viewRect.contains((int) X, (int) Y)) {
                this.mSelectedViewIndex = this.mTopViewIndex + i + 1;
                boolean enabled = this.mAdapter.isEnabled(this.mSelectedViewIndex);
                if (enabled) {
                    this.mTouchTime = System.currentTimeMillis();
                    final int j = i;
                    if (this.mOnItemLongClicked != null) {
                        postDelayed(() -> {
                            if (MultiColumnList.this.mOnItemLongClicked != null && MultiColumnList.this.mTouchTime != 0 && Math.abs(System.currentTimeMillis() - MultiColumnList.this.mTouchTime) > 450) {
                                MultiColumnList.this.mOnItemLongClicked.onItemLongClick(MultiColumnList.this, child, MultiColumnList.this.mTopViewIndex + 1 + j, MultiColumnList.this.mAdapter.getItemId(MultiColumnList.this.mTopViewIndex + 1 + j));
                            }
                        }, 500L);
                    }
                    setSelector(left, top, right, bottom, true);
                } else {
                    return;
                }
            }
        }
    }

    private void dropAnimations() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.clearAnimation();
            }
        }
    }

    private void onTouchUp(float X, float Y) {
        if (this.mOnItemLongClicked == null || Math.abs(System.currentTimeMillis() - this.mTouchTime) <= 450) {
            Rect viewRect = new Rect();
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int left = child.getLeft();
                int right = child.getRight();
                int top = child.getTop();
                int bottom = child.getBottom();
                viewRect.set(left, top, right, bottom);
                if (viewRect.contains((int) X, (int) Y) && this.mOnItemClicked != null) {
                    this.mOnItemClicked.onItemClick(this, child, this.mTopViewIndex + 1 + i, this.mAdapter.getItemId(this.mTopViewIndex + 1 + i));
                }
            }
        }
    }

    private Rect attachSelector(int item) {
        int local_idx = item - (this.mTopViewIndex + 1);
        View child = getChildAt(local_idx);
        if (child == null) {
            return new Rect();
        }
        int left = child.getLeft();
        int right = child.getRight();
        int top = child.getTop();
        int bottom = child.getBottom();
        this.mSelectorRect.set(left, top, right, bottom);
        return new Rect(left, top, right, bottom);
    }

    /** @noinspection SameParameterValue*/
    private void setSelector(int left, int top, int right, int bottom, boolean draw) {
        this.mSelectorRect.set(left, top, right, bottom);
        this.mDrawSelector = draw;
        invalidate();
    }

    private void hideSelector() {
        if (this.mDrawSelector) {
            this.mSelectorRect.set(0, 0, 0, 0);
            this.mDrawSelector = false;
            invalidate();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        Log.e("KEY_EVENT:child", "CODE: " + keyCode + "     EVENT: " + event.getAction());
        boolean handled = false;
        if (event.getAction() == 0) {
            if (this.keyboard_used) {
                switch (keyCode) {
                    case 19:
                        if (this.mSelectedViewIndex > 0) {
                            this.mSelectedViewIndex--;
                            handled = true;
                            break;
                        }
                        break;
                    case 20:
                        if (this.mSelectedViewIndex < this.mAdapter.getCount() - 1) {
                            this.mSelectedViewIndex = getNextBottomDirectionIndex(this.mSelectedViewIndex);
                            handled = true;
                            break;
                        }
                        break;
                }
            } else {
                this.keyboard_used = true;
                handled = true;
            }
        }
        if (handled) {
            requestLayout();
            Rect rect = attachSelector(this.mSelectedViewIndex);
            int height = getHeight();
            if (rect.bottom > height) {
                this.mNextY += (rect.bottom - height) * 2;
            } else if (rect.top < 0) {
                this.mNextY += rect.top * 2;
            }
        }
        return handled;
    }

    private int getChildRowNumber(int index) {
        int local_idx = (index - this.mTopViewIndex) - 1;
        View child = getChildAt(local_idx);
        if (child == null) {
            return 1;
        }
        return (child.getLeft() / (child.getRight() - child.getLeft())) + 1;
    }

    /** @noinspection unused*/ /* JADX WARN: Code restructure failed: missing block: B:10:0x001c, code lost:

        r3 = true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    /**
    private int getNextUpDirectionIndex(int source) {
        int available = 0;
        int count = this.mAdapter.getCount() - 1;
        int counter = source;
        while (true) {
            int type = this.mAdapter.getItemType(counter);
            if (type == 0 || available >= this.mColumnsNumber || counter >= count) {
                break;
            }
            counter--;
            available++;
        }
        boolean last_is_group = false;
        //noinspection ConstantValue
        if (last_is_group && counter < count && available == 0) {
            available++;
        }
        int row = getChildRowNumber(source);
        Log.e("CurrentRow", String.valueOf(row));
        Log.e("Available", String.valueOf(available));
        Log.e("LastIsGroup", String.valueOf(last_is_group));
        if (!last_is_group) {
            return source + available;
        }
        int available2 = available - 1;
        if (available2 <= this.mColumnsNumber - row) {
            return source + available2 + 1;
        }
        return source + available2;
    }
     * @noinspection unused
     */

    private int getNextUpDirectionIndex(int source) {
        int available = 0;
        int counter = source;

        while (counter > 0 && available < mColumnsNumber) {
            int type = mAdapter.getItemType(counter - 1);
            if (type == 0) break;
            counter--;
            available++;
        }

        //noinspection unused
        int row = getChildRowNumber(source);
        return source - available;
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x001c, code lost:

        r3 = true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    /**
    private int getNextBottomDirectionIndex(int source) {
        int available = 0;
        int count = this.mAdapter.getCount() - 1;
        int counter = source;
        while (true) {
            int type = this.mAdapter.getItemType(counter);
            if (type == 0 || available >= this.mColumnsNumber || counter >= count) {
                break;
            }
            counter++;
            available++;
        }
        boolean last_is_group = false;
        //noinspection ConstantValue
        if (last_is_group && counter < count && available == 0) {
            //noinspection unused
            int i = counter + 1;
            available++;
        }
        int row = getChildRowNumber(source);
        Log.e("CurrentRow", String.valueOf(row));
        Log.e("Available", String.valueOf(available));
        Log.e("LastIsGroup", String.valueOf(last_is_group));
        if (!last_is_group) {
            return source + available;
        }
        int available2 = available - 1;
        if (available2 <= this.mColumnsNumber - row) {
            return source + available2 + 1;
        }
        return source + available2;
    }
     */

    private int getNextBottomDirectionIndex(int source) {
        int available = 0;
        int count = mAdapter.getCount();
        int counter = source;

        while (counter < count - 1 && available < mColumnsNumber) {
            int type = mAdapter.getItemType(counter + 1);
            if (type == 0) break;
            counter++;
            available++;
        }

        //noinspection unused
        int row = getChildRowNumber(source);
        return source + available;
    }


    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override
    public final boolean dispatchTouchEvent(MotionEvent event) {
        this.keyboard_used = false;
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
            this.mScrolling = true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.IS_TOUCHED = true;
                this.mLastTouchY = event.getY();
                this.mLastMoveY = this.mLastTouchY;
                onTouchDown(event.getX(), this.mLastTouchY);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                this.IS_TOUCHED = false;
                hideSelector();
                if (this.mScrolling) {
                    this.mTouchTime = 0L;
                    float diff = this.mLastMoveY_ - event.getY();
                    if (Math.abs(diff) > 3.0f) {
                        onFling(event, event, 0.0f, 32.0f * diff);
                    }
                    this.mScrolling = false;
                } else {
                    onTouchUp(event.getX(), event.getY());
                    this.mTouchTime = 0L;
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                float Y = event.getY();
                if (mScrolling) {
                    awakenScrollBars(2000, true);
                    mTouchTime = 0L;
                    hideSelector();
                    float diff = mLastMoveY - Y;
                    synchronized (this) {
                        mNextY += (int) diff;
                    }
                    requestLayout();
                    mLastMoveY_ = mLastMoveY;
                    mLastMoveY = Y;
                } else if (Math.abs(mLastTouchY - Y) > 15.0f) {
                    mLastMoveY = Y;
                    mScrolling = true;
                    dropAnimations();
                    hideSelector();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                this.mScrolling = false;
                this.mTouchTime = 0L;
                hideSelector();
                return true;
            default:
                return true;
        }
    }

    /** @noinspection UnusedReturnValue, unused , SameParameterValue */
    /**
    protected final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        synchronized (this) {
            this.mScroller.fling(0, this.mNextY, 0, (int) velocityY, 0, 0, MMPProtocol.MMP_FLAG_INVISIBLE, Integer.MAX_VALUE);
        }
        requestLayout();
        return true;
    }
     * @noinspection unused, SameParameterValue
     */

    protected final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mScroller != null) {
            synchronized (this) {
                mScroller.fling(0, mNextY, 0, (int) velocityY, 0, 0, 0, Integer.MAX_VALUE);
            }
            requestLayout();
        }
        return true;
    }

    /** @noinspection unused*/
    protected final boolean onDown(MotionEvent e) {
        this.mScroller.forceFinished(true);
        return true;
    }

    /**
    private static int getMax(int[] array) {
        int max = MMPProtocol.MMP_FLAG_INVISIBLE;
        if (array == null || array.length == 0) {
            return 0;
        }
        for (int i : array) {
            if (max < i) {
                max = i;
            }
        }
        return max;
    }
     */

    private static int getMax(int[] array) {
        if (array == null || array.length == 0) return 0;
        int max = array[0];
        for (int value : array) {
            if (value > max) max = value;
        }
        return max;
    }

    /** @noinspection SpellCheckingInspection*/
    /**
    public final void clearup() {
        removeAllViews();
        this.mDataObserver = null;
        this.mAdapter = null;
        super.destroyDrawingCache();
    }
     */
    public final void clearup() {
        removeAllViews();
        if (mAdapter != null && mDataObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = null;
        mDataObserver = null;
        super.destroyDrawingCache();
    }
}
