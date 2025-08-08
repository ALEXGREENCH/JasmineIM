package ru.ivansuper.jasmin.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

/**
 * Utility class for managing system bars (status bar and navigation bar).
 * Provides methods to configure their appearance, such as making them transparent.
 */
public class SystemBarUtils {
    public static void setupTransparentBars(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            final View root = activity.findViewById(android.R.id.content);
            if (root == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        v.setPadding(
                                v.getPaddingLeft(),
                                insets.getSystemWindowInsetTop(),
                                v.getPaddingRight(),
                                insets.getSystemWindowInsetBottom());
                        return insets.consumeSystemWindowInsets();
                    }
                });
                root.requestApplyInsets();
            } else {
                int top = getInternalDimen(activity, "status_bar_height");
                int bottom = getInternalDimen(activity, "navigation_bar_height");
                root.setPadding(root.getPaddingLeft(), top, root.getPaddingRight(), bottom);
            }
        }
    }

    public static void setupTransparentBars(Dialog dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && dialog != null) {
            Window window = dialog.getWindow();
            if (window == null) return;
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            final View root = window.findViewById(android.R.id.content);
            if (root == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        v.setPadding(
                                v.getPaddingLeft(),
                                insets.getSystemWindowInsetTop(),
                                v.getPaddingRight(),
                                insets.getSystemWindowInsetBottom());
                        return insets.consumeSystemWindowInsets();
                    }
                });
                root.requestApplyInsets();
            } else {
                int top = getInternalDimen(dialog.getContext(), "status_bar_height");
                int bottom = getInternalDimen(dialog.getContext(), "navigation_bar_height");
                root.setPadding(root.getPaddingLeft(), top, root.getPaddingRight(), bottom);
            }
        }
    }

    private static int getInternalDimen(Context context, String name) {
        int resId = context.getResources().getIdentifier(name, "dimen", "android");
        return resId > 0 ? context.getResources().getDimensionPixelSize(resId) : 0;
    }
}
