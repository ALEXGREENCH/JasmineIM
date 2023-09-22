package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/* loaded from: classes.dex */
public class PB extends View {
    private long max;
    private android.graphics.drawable.BitmapDrawable progress;
    private int shadow_border;
    private long value;
    private long value_;
    private int view_height;
    private int view_width;

    public PB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.max = 100L;
        this.value = 0L;
        this.value_ = 0L;
        this.view_width = 0;
        this.view_height = 14;
        this.shadow_border = 3;
        init(context);
    }

    public PB(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.max = 100L;
        this.value = 0L;
        this.value_ = 0L;
        this.view_width = 0;
        this.view_height = 14;
        this.shadow_border = 3;
        init(context);
    }

    public PB(Context context) {
        super(context);
        this.max = 100L;
        this.value = 0L;
        this.value_ = 0L;
        this.view_width = 0;
        this.view_height = 14;
        this.shadow_border = 3;
        init(context);
    }

    private final void init(Context context) {
        this.progress = (android.graphics.drawable.BitmapDrawable) context.getResources().getDrawable(R.drawable.progress_line);
        this.progress.setTileModeX(Shader.TileMode.REPEAT);
        this.progress.setTileModeY(Shader.TileMode.REPEAT);
        this.view_height = (int) (this.view_height * resources.dm.density);
    }

    @Override // android.view.View
    protected void onMeasure(int a, int b) {
        this.view_width = (View.MeasureSpec.getSize(a) - (this.shadow_border * 2)) - 1;
        setMeasuredDimension(this.view_width + 1, this.view_height);
    }

    @SuppressLint("MissingSuperCall")
    @Override // android.view.View
    public void draw(Canvas canvas) {
        this.value_ += (this.value - this.value_) / 5;
        Paint p = new Paint();
        p.setColor(1996488704);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(this.shadow_border, this.shadow_border, this.view_width, this.view_height - this.shadow_border, p);
        Paint p2 = new Paint();
        p2.setColor(-1);
        p2.setStyle(Paint.Style.STROKE);
        canvas.drawRect(this.shadow_border + 1, this.shadow_border + 1, this.view_width - 2, (this.view_height - this.shadow_border) - 2, p2);
        Paint p3 = new Paint();
        p3.setStyle(Paint.Style.FILL);
        p3.setColor(-1);
        p3.setShadowLayer(1.0f, 0.0f, 0.0f, 1728053247);
        long width = (this.value_ * (this.view_width - 4)) / this.max;
        if (width > 4) {
            this.progress.setBounds(this.shadow_border + 3, this.shadow_border + 3, (int) width, (this.view_height - this.shadow_border) - 3);
            this.progress.draw(canvas);
            if (this.value_ != this.value) {
                if ((Math.abs(this.value_ - this.value) * 100) / this.max < 5) {
                    this.value_ = this.value;
                }
                invalidate();
            }
        }
    }

    public void setMax(long maximum) {
        this.max = maximum;
        if (this.max <= 0) {
            this.max = 1L;
        }
        if (this.value > this.max) {
            this.value = this.max;
        }
    }

    public void setProgress(long progress) {
        if (this.max > progress) {
            this.value = progress;
            if (this.value < this.value_) {
                this.value_ = this.value;
            }
        }
        invalidate();
    }
}