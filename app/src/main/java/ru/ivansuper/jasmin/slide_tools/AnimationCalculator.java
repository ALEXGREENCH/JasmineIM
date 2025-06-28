package ru.ivansuper.jasmin.slide_tools;

import android.view.animation.TranslateAnimation;

import ru.ivansuper.jasmin.resources;

public class AnimationCalculator {
    public static final int ANIMATION_LENGTH = 150;

    public static TranslateAnimation calculateCancelAnimation(float var0, float var1) {
        TranslateAnimation var2 = new TranslateAnimation(1, var0 / var1, 1, 0.0F, 1, 0.0F, 1, 0.0F);
        var2.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
        var2.setDuration((long) 150);
        return var2;
    }
}