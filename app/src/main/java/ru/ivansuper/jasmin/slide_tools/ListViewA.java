package ru.ivansuper.jasmin.slide_tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.resources;

public class ListViewA extends ListView {
    /** @noinspection unused*/
    private Bitmap buffer;
    /** @noinspection unused*/
    private Canvas buffer_canvas;
    private boolean captured;
    /** @noinspection unused*/
    private int count;
    private boolean drag_drop_enabled;
    private boolean drag_drop_enabledA;
    private boolean dragging;
    private Rect last_size;
    private int last_touched_item_idx;
    private SlideListener listener;
    /** @noinspection unused*/
    private Camera mCamera;
    private MultitouchListener mult_listener;
    private final float not_zero_offset;
    /** @noinspection unused*/
    private int offset;
    public OnResizeListener resize_listener;
    private jasminSvc service;
    private boolean slide_enabled;
    private boolean sliding_handled;
    private boolean use_custom_scroll_control;
    private float x0;
    private float y0;

    public interface MultitouchListener {
        void onStart(View view, int i);

        void onStop(float f, float f2, int i);

        void onTouch(float f, float f2);
    }

    public interface OnResizeListener {
        void onResize();
    }

    public interface SlideListener {
        void onCancel(float f, float f2);

        void onFling(boolean z, float f);

        void onMoving(float f, float f2);

        void onStartDrag();
    }

    public ListViewA(Context context) {
        super(context);
        this.x0 = 0.0f;
        this.y0 = 0.0f;
        this.offset = 0;
        this.not_zero_offset = 0.0f;
        this.dragging = false;
        this.slide_enabled = true;
        this.count = 0;
        this.use_custom_scroll_control = false;
        init();
    }

    public ListViewA(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.x0 = 0.0f;
        this.y0 = 0.0f;
        this.offset = 0;
        this.not_zero_offset = 0.0f;
        this.dragging = false;
        this.slide_enabled = true;
        this.count = 0;
        this.use_custom_scroll_control = false;
        init();
    }

    protected void finalize() {
    }

    private void init() {
        this.drag_drop_enabled = true;
        super.setStackFromBottom(true);
        super.setCacheColorHint(0);
        super.setBackgroundColor(0);
        super.setAlwaysDrawnWithCacheEnabled(false);
        super.setAnimationCacheEnabled(false);
        super.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        setTranscriptMode(1);
    }

    public void setService(jasminSvc service) {
        this.service = service;
    }

    public final void setUseCustomScrollControl(boolean used) {
        this.use_custom_scroll_control = used;
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (resources.OS_VERSION <= 10 && this.use_custom_scroll_control) {
            int last = getFirstVisiblePosition() + getChildCount();
            if (last < 0) {
                last = 0;
            }
            boolean scroll = false;
            int new_count = 0;
            try {
                new_count = getAdapter().getCount();
                scroll = new_count <= last;
            } catch (Exception ignored) {
            }
            if (changed && !scroll) {
                setTranscriptMode(0);
                //noinspection ConstantValue
                super.onLayout(changed, l, t, r, b);
                post(new Runnable() {
                    @Override
                    public void run() {
                        ListViewA.this.setTranscriptMode(1);
                        ListViewA.this.requestLayout();
                    }
                });
            } else {
                setTranscriptMode(1);
                super.onLayout(changed, l, t, r, b);
            }
            this.count = new_count;
            return;
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void onSizeChanged(int a, int b, int c, int d) {
        super.onSizeChanged(a, b, c, d);
        if (this.last_size == null) {
            this.last_size = new Rect(a, b, c, d);
        }
        if (this.resize_listener == null || Math.abs(this.last_size.left - a) + Math.abs(this.last_size.top - b) + Math.abs(this.last_size.right - c) + Math.abs(this.last_size.bottom - d) <= 128) {
            return;
        }
        this.resize_listener.onResize();
        this.last_size.set(a, b, c, d);
    }

    /** @noinspection unused*/
    public final void setDegree(int offset) {
        this.offset = offset;
        invalidate();
    }

    /** @noinspection unused*/
    public final float getDegree() {
        return this.not_zero_offset;
    }

    public void setDragDropEnabledA(boolean enabled) {
        this.drag_drop_enabledA = enabled;
    }

    public void setDragDropEnabled(boolean enabled) {
        this.drag_drop_enabled = enabled;
    }

    public void setSlideEnabled(boolean enabled) {
        this.slide_enabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if ((ev.getAction() & 255) == 2 && !this.dragging && this.captured && this.sliding_handled) {
            if (this.listener != null) {
                this.listener.onMoving((int) this.x0, (int) ev.getRawX());
            }
            if (this.captured) {
                return false;
            }
            if (this.listener != null) {
                this.listener.onStartDrag();
            }
            this.captured = true;
            return false;
        }
        if ((ev.getAction() & 255) == 2 && this.dragging && this.mult_listener != null) {
            int[] location = new int[2];
            getLocationOnScreen(location);
            this.mult_listener.onTouch(ev.getX(), location[1] + ev.getY());
            return false;
        }
        switch (ev.getAction() & 255) {
            case 0:
                this.sliding_handled = false;
                this.captured = false;
                this.x0 = ev.getRawX();
                this.y0 = ev.getRawY();
                break;
            case 1:
                if (this.captured && !this.dragging) {
                    if (Math.abs(this.x0 - ev.getRawX()) > 80.0f * resources.dm.density) {
                        if (this.service.opened_chats.size() <= 1) {
                            if (this.listener == null) {
                                return false;
                            }
                            this.listener.onCancel(ev.getRawX() - this.x0, getWidth());
                            return false;
                        }
                        if (this.x0 - ev.getRawX() < 0.0f) {
                            if (this.listener == null) {
                                return false;
                            }
                            this.listener.onFling(true, (ev.getRawX() - this.x0) / getWidth());
                            return false;
                        }
                        if (this.listener == null) {
                            return false;
                        }
                        this.listener.onFling(false, (ev.getRawX() - this.x0) / getWidth());
                        return false;
                    }
                    if (this.listener == null) {
                        return false;
                    }
                    this.listener.onCancel(ev.getRawX() - this.x0, getWidth());
                    return false;
                }
                if (this.dragging) {
                    int[] location2 = new int[2];
                    getLocationOnScreen(location2);
                    if (this.mult_listener != null && this.dragging) {
                        this.mult_listener.onStop(ev.getX(), location2[1] + ev.getY(), this.last_touched_item_idx);
                    }
                    this.dragging = false;
                    return false;
                }
                break;
            case 2:
                if (this.dragging) {
                    return true;
                }
                if (Math.abs(this.y0 - ev.getRawY()) < 50.0f && Math.abs(this.x0 - ev.getRawX()) > 30.0f && !this.dragging && !this.captured && !this.sliding_handled && this.slide_enabled) {
                    this.sliding_handled = true;
                }
                if (this.sliding_handled) {
                    if (this.listener != null) {
                        this.listener.onMoving((int) this.x0, (int) ev.getRawX());
                    }
                    if (this.captured) {
                        return false;
                    }
                    if (this.listener != null) {
                        this.listener.onStartDrag();
                    }
                    this.captured = true;
                    super.dispatchTouchEvent(MotionEvent.obtain(1L, 1L, 3, ev.getX(), ev.getY(), 0));
                    return false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int pos;
        if ((ev.getAction() & 255) == 2 && !this.dragging && this.captured && this.sliding_handled) {
            if (this.listener != null) {
                this.listener.onMoving((int) this.x0, (int) ev.getRawX());
            }
            if (this.captured) {
                return false;
            }
            if (this.listener != null) {
                this.listener.onStartDrag();
            }
            this.captured = true;
            return false;
        }
        if ((ev.getAction() & 255) == 2 && this.dragging && this.mult_listener != null) {
            int[] location = new int[2];
            getLocationOnScreen(location);
            this.mult_listener.onTouch(ev.getX(), location[1] + ev.getY());
            return false;
        }
        switch (ev.getAction() & 255) {
            case 0:
                if (ev.getX() > getWidth() - (32.0f * resources.dm.density) && !this.dragging && this.drag_drop_enabled && this.drag_drop_enabledA && (pos = super.pointToPosition(1, (int) ev.getY())) != -1) {
                    this.last_touched_item_idx = pos;
                    View view = getChildAt(pos - super.getFirstVisiblePosition());
                    int[] location2 = new int[2];
                    getLocationOnScreen(location2);
                    this.mult_listener.onStart(view, location2[1]);
                    this.dragging = true;
                    return true;
                }
                this.sliding_handled = false;
                this.captured = false;
                this.x0 = ev.getRawX();
                this.y0 = ev.getRawY();
                break;
            case 1:
                if (this.captured && !this.dragging) {
                    if (Math.abs(this.x0 - ev.getRawX()) > 80.0f * resources.dm.density) {
                        if (this.service.opened_chats.size() <= 1) {
                            if (this.listener == null) {
                                return false;
                            }
                            this.listener.onCancel(ev.getRawX() - this.x0, getWidth());
                            return false;
                        }
                        if (this.x0 - ev.getRawX() < 0.0f) {
                            if (this.listener == null) {
                                return false;
                            }
                            this.listener.onFling(true, (ev.getRawX() - this.x0) / getWidth());
                            return false;
                        }
                        if (this.listener == null) {
                            return false;
                        }
                        this.listener.onFling(false, (ev.getRawX() - this.x0) / getWidth());
                        return false;
                    }
                    if (this.listener == null) {
                        return false;
                    }
                    this.listener.onCancel(ev.getRawX() - this.x0, getWidth());
                    return false;
                }
                if (this.dragging) {
                    int[] location3 = new int[2];
                    getLocationOnScreen(location3);
                    if (this.mult_listener != null && this.dragging) {
                        this.mult_listener.onStop(ev.getX(), location3[1] + ev.getY(), this.last_touched_item_idx);
                    }
                    this.dragging = false;
                    return false;
                }
                break;
            case 2:
                if (Math.abs(this.y0 - ev.getRawY()) < 50.0f && Math.abs(this.x0 - ev.getRawX()) > 30.0f && !this.dragging && !this.captured && this.slide_enabled) {
                    this.sliding_handled = true;
                }
                if (this.sliding_handled) {
                    if (this.listener != null) {
                        this.listener.onMoving((int) this.x0, (int) ev.getRawX());
                    }
                    if (this.captured) {
                        return false;
                    }
                    if (this.listener != null) {
                        this.listener.onStartDrag();
                    }
                    this.captured = true;
                    super.dispatchTouchEvent(MotionEvent.obtain(1L, 1L, 3, ev.getX(), ev.getY(), 0));
                    return false;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /** @noinspection unused*/
    public void setSlideListener(SlideListener listener) {
        this.listener = listener;
    }

    public void setOnMultitouchListener(MultitouchListener listener) {
        this.mult_listener = listener;
    }
}