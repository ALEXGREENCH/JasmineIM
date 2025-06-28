package ru.ivansuper.jasmin.slide_tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.Service.jasminSvc;

public class ListViewA extends ListView {
    /** @noinspection unused*/
    private Bitmap buffer;
    /** @noinspection unused*/
    private Canvas buffer_canvas;
    private boolean captured;
    /** @noinspection FieldCanBeLocal, unused */
    private int count = 0;
    private boolean drag_drop_enabled;
    private boolean drag_drop_enabledA;
    private boolean dragging = false;
    private Rect last_size;
    private int last_touched_item_idx;
    private SlideListener listener;
    /** @noinspection unused*/
    private Camera mCamera;
    private MultitouchListener mult_listener;
    /** @noinspection FieldCanBeLocal*/
    private float not_zero_offset = 0.0F;
    /** @noinspection FieldCanBeLocal*/
    private int offset = 0;
    public OnResizeListener resize_listener;
    private jasminSvc service;
    private boolean slide_enabled = true;
    private boolean sliding_handled;
    private boolean use_custom_scroll_control = false;
    private float x0 = 0.0F;
    private float y0 = 0.0F;

    public ListViewA(Context var1) {
        super(var1);
        this.init();
    }

    public ListViewA(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init();
    }

    private void init() {
        this.drag_drop_enabled = true;
        super.setStackFromBottom(true);
        super.setCacheColorHint(0);
        super.setBackgroundColor(0);
        super.setAlwaysDrawnWithCacheEnabled(false);
        super.setAnimationCacheEnabled(false);
        super.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        this.setTranscriptMode(1);
    }

    protected void finalize() {
    }

    /** @noinspection unused*/
    public final float getDegree() {
        return this.not_zero_offset;
    }

    public boolean onInterceptTouchEvent(MotionEvent var1) {
        boolean var5 = false;
        boolean var4;
        if ((var1.getAction() & 255) == 2 && !this.dragging && this.captured && this.sliding_handled) {
            if (this.listener != null) {
                this.listener.onMoving((float)((int)this.x0), (float)((int)var1.getRawX()));
            }

            var4 = var5;
            if (!this.captured) {
                if (this.listener != null) {
                    this.listener.onStartDrag();
                }

                this.captured = true;
                var4 = var5;
            }
        } else {
            float var2;
            float var3;
            if ((var1.getAction() & 255) == 2 && this.dragging && this.mult_listener != null) {
                int[] var9 = new int[2];
                this.getLocationOnScreen(var9);
                MultitouchListener var8 = this.mult_listener;
                var2 = var1.getX();
                var3 = var1.getY();
                var8.onTouch(var2, (float)var9[1] + var3);
                var4 = var5;
            } else {
                switch (var1.getAction() & 255) {
                    case 0:
                        this.sliding_handled = false;
                        this.captured = false;
                        this.x0 = var1.getRawX();
                        this.y0 = var1.getRawY();
                        break;
                    case 1:
                        if (this.captured && !this.dragging) {
                            if (Math.abs(this.x0 - var1.getRawX()) > 80.0F * resources.dm.density) {
                                if (this.service.opened_chats.size() > 1) {
                                    if (this.x0 - var1.getRawX() < 0.0F) {
                                        var4 = var5;
                                        if (this.listener != null) {
                                            this.listener.onFling(true, (var1.getRawX() - this.x0) / (float)this.getWidth());
                                            var4 = var5;
                                            return var4;
                                        }
                                    } else {
                                        var4 = var5;
                                        if (this.listener != null) {
                                            this.listener.onFling(false, (var1.getRawX() - this.x0) / (float)this.getWidth());
                                            var4 = var5;
                                            return var4;
                                        }
                                    }

                                    return var4;
                                } else {
                                    var4 = var5;
                                    if (this.listener != null) {
                                        this.listener.onCancel(var1.getRawX() - this.x0, (float)this.getWidth());
                                        var4 = var5;
                                        return var4;
                                    }

                                    return var4;
                                }
                            } else {
                                var4 = var5;
                                if (this.listener != null) {
                                    this.listener.onCancel(var1.getRawX() - this.x0, (float)this.getWidth());
                                    var4 = var5;
                                    return var4;
                                }

                                return var4;
                            }
                        }

                        if (this.dragging) {
                            int[] var6 = new int[2];
                            this.getLocationOnScreen(var6);
                            if (this.mult_listener != null && this.dragging) {
                                MultitouchListener var7 = this.mult_listener;
                                var3 = var1.getX();
                                var2 = var1.getY();
                                var7.onStop(var3, (float)var6[1] + var2, this.last_touched_item_idx);
                            }

                            this.dragging = false;
                            var4 = var5;
                            return var4;
                        }
                        break;
                    case 2:
                        if (this.dragging) {
                            var4 = true;
                            return var4;
                        }

                        if (Math.abs(this.y0 - var1.getRawY()) < 50.0F && Math.abs(this.x0 - var1.getRawX()) > 30.0F && !this.dragging && !this.captured && !this.sliding_handled && this.slide_enabled) {
                            this.sliding_handled = true;
                        }

                        if (this.sliding_handled) {
                            if (this.listener != null) {
                                this.listener.onMoving((float)((int)this.x0), (float)((int)var1.getRawX()));
                            }

                            var4 = var5;
                            if (!this.captured) {
                                if (this.listener != null) {
                                    this.listener.onStartDrag();
                                }

                                this.captured = true;
                                super.dispatchTouchEvent(MotionEvent.obtain(1L, 1L, 3, var1.getX(), var1.getY(), 0));
                                var4 = var5;
                            }

                            return var4;
                        }
                }

                var4 = super.onInterceptTouchEvent(var1);
            }
        }

        return var4;
    }

    public void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        if (resources.OS_VERSION <= 10 && this.use_custom_scroll_control) {
            int var7 = this.getFirstVisiblePosition() + this.getChildCount();
            int var6 = var7;
            if (var7 < 0) {
                var6 = 0;
            }

            boolean var8 = false;
            byte var9 = 0;

            boolean var12;
            label31: {
                try {
                    var7 = this.getAdapter().getCount();
                } catch (Exception var11) {
                    var7 = var9;
                    var12 = var8;
                    break label31;
                }

                if (var7 <= var6) {
                    var12 = true;
                } else {
                    var12 = false;
                }
            }

            if (var1 && !var12) {
                this.setTranscriptMode(0);
                super.onLayout(var1, var2, var3, var4, var5);
                this.post(new ListViewA$1(this));
            } else {
                this.setTranscriptMode(1);
                super.onLayout(var1, var2, var3, var4, var5);
            }

            this.count = var7;
        } else {
            super.onLayout(var1, var2, var3, var4, var5);
        }

    }

    public void onSizeChanged(int var1, int var2, int var3, int var4) {
        super.onSizeChanged(var1, var2, var3, var4);
        if (this.last_size == null) {
            this.last_size = new Rect(var1, var2, var3, var4);
        }

        if (this.resize_listener != null && Math.abs(this.last_size.left - var1) + Math.abs(this.last_size.top - var2) + Math.abs(this.last_size.right - var3) + Math.abs(this.last_size.bottom - var4) > 128) {
            this.resize_listener.onResize();
            this.last_size.set(var1, var2, var3, var4);
        }

    }

    public boolean onTouchEvent(MotionEvent var1) {
        boolean var6 = false;
        boolean var5;
        if ((var1.getAction() & 255) == 2 && !this.dragging && this.captured && this.sliding_handled) {
            if (this.listener != null) {
                this.listener.onMoving((float)((int)this.x0), (float)((int)var1.getRawX()));
            }

            var5 = var6;
            if (!this.captured) {
                if (this.listener != null) {
                    this.listener.onStartDrag();
                }

                this.captured = true;
                var5 = var6;
            }
        } else {
            float var2;
            float var3;
            if ((var1.getAction() & 255) == 2 && this.dragging && this.mult_listener != null) {
                int[] var12 = new int[2];
                this.getLocationOnScreen(var12);
                MultitouchListener var11 = this.mult_listener;
                var3 = var1.getX();
                var2 = var1.getY();
                var11.onTouch(var3, (float)var12[1] + var2);
                var5 = var6;
            } else {
                switch (var1.getAction() & 255) {
                    case 0:
                        if (var1.getX() > (float)this.getWidth() - 32.0F * resources.dm.density && !this.dragging && this.drag_drop_enabled && this.drag_drop_enabledA) {
                            int var4 = super.pointToPosition(1, (int)var1.getY());
                            if (var4 != -1) {
                                this.last_touched_item_idx = var4;
                                View var10 = this.getChildAt(var4 - super.getFirstVisiblePosition());
                                int[] var9 = new int[2];
                                this.getLocationOnScreen(var9);
                                this.mult_listener.onStart(var10, var9[1]);
                                this.dragging = true;
                                var5 = true;
                                return var5;
                            }
                        }

                        this.sliding_handled = false;
                        this.captured = false;
                        this.x0 = var1.getRawX();
                        this.y0 = var1.getRawY();
                        break;
                    case 1:
                        if (this.captured && !this.dragging) {
                            if (Math.abs(this.x0 - var1.getRawX()) > 80.0F * resources.dm.density) {
                                if (this.service.opened_chats.size() > 1) {
                                    if (this.x0 - var1.getRawX() < 0.0F) {
                                        var5 = var6;
                                        if (this.listener != null) {
                                            this.listener.onFling(true, (var1.getRawX() - this.x0) / (float)this.getWidth());
                                            var5 = var6;
                                            return var5;
                                        }
                                    } else {
                                        var5 = var6;
                                        if (this.listener != null) {
                                            this.listener.onFling(false, (var1.getRawX() - this.x0) / (float)this.getWidth());
                                            var5 = var6;
                                            return var5;
                                        }
                                    }

                                    return var5;
                                } else {
                                    var5 = var6;
                                    if (this.listener != null) {
                                        this.listener.onCancel(var1.getRawX() - this.x0, (float)this.getWidth());
                                        var5 = var6;
                                        return var5;
                                    }

                                    return var5;
                                }
                            } else {
                                var5 = var6;
                                if (this.listener != null) {
                                    this.listener.onCancel(var1.getRawX() - this.x0, (float)this.getWidth());
                                    var5 = var6;
                                    return var5;
                                }

                                return var5;
                            }
                        }

                        if (this.dragging) {
                            int[] var7 = new int[2];
                            this.getLocationOnScreen(var7);
                            if (this.mult_listener != null && this.dragging) {
                                MultitouchListener var8 = this.mult_listener;
                                var2 = var1.getX();
                                var3 = var1.getY();
                                var8.onStop(var2, (float)var7[1] + var3, this.last_touched_item_idx);
                            }

                            this.dragging = false;
                            var5 = var6;
                            return var5;
                        }
                        break;
                    case 2:
                        if (Math.abs(this.y0 - var1.getRawY()) < 50.0F && Math.abs(this.x0 - var1.getRawX()) > 30.0F && !this.dragging && !this.captured && this.slide_enabled) {
                            this.sliding_handled = true;
                        }

                        if (this.sliding_handled) {
                            if (this.listener != null) {
                                this.listener.onMoving((float)((int)this.x0), (float)((int)var1.getRawX()));
                            }

                            var5 = var6;
                            if (!this.captured) {
                                if (this.listener != null) {
                                    this.listener.onStartDrag();
                                }

                                this.captured = true;
                                super.dispatchTouchEvent(MotionEvent.obtain(1L, 1L, 3, var1.getX(), var1.getY(), 0));
                                var5 = var6;
                            }

                            return var5;
                        }
                }

                var5 = super.onTouchEvent(var1);
            }
        }

        return var5;
    }

    /** @noinspection unused*/
    public final void setDegree(int var1) {
        this.offset = var1;
        this.invalidate();
    }

    public void setDragDropEnabled(boolean var1) {
        this.drag_drop_enabled = var1;
    }

    public void setDragDropEnabledA(boolean var1) {
        this.drag_drop_enabledA = var1;
    }

    public void setOnMultitouchListener(MultitouchListener var1) {
        this.mult_listener = var1;
    }

    public void setService(jasminSvc var1) {
        this.service = var1;
    }

    public void setSlideEnabled(boolean var1) {
        this.slide_enabled = var1;
    }

    /** @noinspection unused*/
    public void setSlideListener(SlideListener var1) {
        this.listener = var1;
    }

    public final void setUseCustomScrollControl(boolean var1) {
        this.use_custom_scroll_control = var1;
    }

    public interface MultitouchListener {
        void onStart(View var1, int var2);

        void onStop(float var1, float var2, int var3);

        void onTouch(float var1, float var2);
    }

    public interface OnResizeListener {
        void onResize();
    }

    public interface SlideListener {
        void onCancel(float var1, float var2);

        void onFling(boolean var1, float var2);

        void onMoving(float var1, float var2);

        void onStartDrag();
    }
}