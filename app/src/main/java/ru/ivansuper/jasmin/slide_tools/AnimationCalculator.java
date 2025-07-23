package ru.ivansuper.jasmin.slide_tools;

import android.view.animation.TranslateAnimation;

import ru.ivansuper.jasmin.resources;

/**
 * Calculates animations for slide gestures.
 */
public class AnimationCalculator {
    public static final int ANIMATION_LENGTH = 150;

    public static TranslateAnimation calculateCancelAnimation(float currentX, float endX) {
        TranslateAnimation animation = new TranslateAnimation(1, currentX / endX, 1, 0.0F, 1, 0.0F, 1, 0.0F);
        animation.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
        animation.setDuration(ANIMATION_LENGTH);
        return animation;
    }
}