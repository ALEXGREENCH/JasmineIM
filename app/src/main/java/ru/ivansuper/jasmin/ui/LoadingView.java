package ru.ivansuper.jasmin.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;
import ru.ivansuper.jasmin.R;

public class LoadingView extends View {
    private Drawable[] ani;
    private Drawable back;
    private int current;
    private int current_alpha;
    private int height;
    private int next;
    private int width;

    public LoadingView(Context context) {
        super(context);
        this.current = 0;
        this.next = 2;
        this.current_alpha = 255;
        this.width = 32;
        this.height = 32;
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.current = 0;
        this.next = 2;
        this.current_alpha = 255;
        this.width = 32;
        this.height = 32;
        init();
    }

    private void init() {
        Resources r = getContext().getResources();
        this.back = r.getDrawable(R.drawable.ani_loading_back);
        this.ani = new Drawable[5];
        this.ani[0] = r.getDrawable(R.drawable.ani_loading_0);
        this.ani[1] = r.getDrawable(R.drawable.ani_loading_1);
        this.ani[2] = r.getDrawable(R.drawable.ani_loading_2);
        this.ani[3] = r.getDrawable(R.drawable.ani_loading_3);
        this.ani[4] = r.getDrawable(R.drawable.ani_loading_4);
        this.width = this.back.getIntrinsicWidth();
        this.height = this.back.getIntrinsicHeight();
        if (this.width <= 0) {
            this.width = 32;
        }
        if (this.height <= 0) {
            this.height = 32;
        }
        this.back.setBounds(0, 0, this.width, this.height);
        for (Drawable d : this.ani) {
            d.setBounds(0, 0, this.width, this.height);
        }
    }

    private void switchToNext() {
        this.current = this.next;
        this.next++;
        if (this.next >= this.ani.length) {
            this.next = 0;
        }
        getNext();
    }

    private void getNext() {
        Random rnd = new Random(System.currentTimeMillis());
        while (this.next == this.current) {
            this.next = rnd.nextInt(this.ani.length);
        }
    }

    @Override
    public void onMeasure(int a, int b) {
        setMeasuredDimension(this.width, this.height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Drawable a = this.ani[this.current];
        Drawable b = this.ani[this.next];
        a.setAlpha(this.current_alpha);
        b.setAlpha(255 - this.current_alpha);
        this.back.draw(canvas);
        a.draw(canvas);
        b.draw(canvas);
        this.current_alpha -= 15;
        if (this.current_alpha <= 0) {
            this.current_alpha = 255;
            switchToNext();
        }
        invalidate();
    }
}
