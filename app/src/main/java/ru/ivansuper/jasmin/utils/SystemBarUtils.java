package ru.ivansuper.jasmin.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

public class SystemBarUtils {
    public static void setupTransparentBars(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            final View root = activity.findViewById(android.R.id.content);
            if (root == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                root.setOnApplyWindowInsetsListener((v, insets) -> {
                    v.setPadding(
                            v.getPaddingLeft(),
                            insets.getSystemWindowInsetTop(),
                            v.getPaddingRight(),
                            insets.getSystemWindowInsetBottom());
                    return insets.consumeSystemWindowInsets();
                });
                root.requestApplyInsets();
            } else {
                int top = getInternalDimen(activity, "status_bar_height");
                int bottom = getInternalDimen(activity, "navigation_bar_height");
                root.setPadding(root.getPaddingLeft(), top, root.getPaddingRight(), bottom);
            }
        }
    }

    private static int getInternalDimen(Activity activity, String name) {
        int resId = activity.getResources().getIdentifier(name, "dimen", "android");
        return resId > 0 ? activity.getResources().getDimensionPixelSize(resId) : 0;
    }
}
