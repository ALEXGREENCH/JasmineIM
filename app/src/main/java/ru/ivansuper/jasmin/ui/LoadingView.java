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
    private int current = 0;
    private int current_alpha = 255;
    private int height = 32;
    private int next = 2;
    private int width = 32;

    public LoadingView(Context var1) {
        super(var1);
        this.init();
    }

    public LoadingView(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.init();
    }

    private void getNext() {
        for(Random var1 = new Random(System.currentTimeMillis()); this.next == this.current; this.next = var1.nextInt(this.ani.length)) {
        }

    }

    private void init() {
        Resources var1 = this.getContext().getResources();
        this.back = var1.getDrawable(R.drawable.ani_loading_back);
        this.ani = new Drawable[5];
        this.ani[0] = var1.getDrawable(R.drawable.ani_loading_0);
        this.ani[1] = var1.getDrawable(R.drawable.ani_loading_1);
        this.ani[2] = var1.getDrawable(R.drawable.ani_loading_2);
        this.ani[3] = var1.getDrawable(R.drawable.ani_loading_3);
        this.ani[4] = var1.getDrawable(R.drawable.ani_loading_4);
        this.width = this.back.getIntrinsicWidth();
        this.height = this.back.getIntrinsicHeight();
        if (this.width <= 0) {
            this.width = 32;
        }

        if (this.height <= 0) {
            this.height = 32;
        }

        this.back.setBounds(0, 0, this.width, this.height);
        Drawable[] var4 = this.ani;
        int var2 = var4.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            var4[var3].setBounds(0, 0, this.width, this.height);
        }

    }

    private void switchToNext() {
        this.current = this.next++;
        if (this.next >= this.ani.length) {
            this.next = 0;
        }

        this.getNext();
    }

    public void onDraw(Canvas var1) {
        Drawable var2 = this.ani[this.current];
        Drawable var3 = this.ani[this.next];
        var2.setAlpha(this.current_alpha);
        var3.setAlpha(255 - this.current_alpha);
        this.back.draw(var1);
        var2.draw(var1);
        var3.draw(var1);
        this.current_alpha -= 15;
        if (this.current_alpha <= 0) {
            this.current_alpha = 255;
            this.switchToNext();
        }

        this.invalidate();
    }

    public void onMeasure(int var1, int var2) {
        this.setMeasuredDimension(this.width, this.height);
    }
}