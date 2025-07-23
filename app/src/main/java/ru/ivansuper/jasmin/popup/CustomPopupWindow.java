package ru.ivansuper.jasmin.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Represents a custom popup window that can be anchored to a view.
 *
 * <p>This class provides a flexible way to create and display popup windows
 * with custom content and behavior. It handles the creation and management
 * of the underlying {@link PopupWindow} and provides methods for setting
 * the content, background, and other properties.
 *
 * <p>Key features:
 * <ul>
 *   <li>Anchoring to a specific view.
 *   <li>Customizable content using a layout resource or a {@link View} object.
 *   <li>Optional background drawable.
 *   <li>Automatic dismissal on outside touch.
 *   <li>Lifecycle methods ({@code onCreate}, {@code onShow}, {@code preShow}) for custom setup.
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Assuming 'anchorView' is the view to which the popup will be anchored
 * CustomPopupWindow popup = new CustomPopupWindow(anchorView);
 * popup.setContentView(R.layout.my_popup_layout);
 * // ... further customization ...
 * popup.show(); // (Note: 'show()' method is typically implemented in a subclass)
 * }</pre>
 */
public class CustomPopupWindow {
    protected final View anchor;
    private Drawable background = null;
    private View root;
    protected final PopupWindow window;
    protected final WindowManager windowManager;

    @SuppressLint("ClickableViewAccessibility")
    public CustomPopupWindow(View anchor) {
        this.anchor = anchor;
        this.window = new PopupWindow(anchor.getContext());

        this.window.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 4) {
                    return false;
                }
                CustomPopupWindow.this.dismiss();
                return true;
            }
        });
        this.windowManager = (WindowManager) anchor.getContext().getSystemService(Context.WINDOW_SERVICE);
        onCreate();
    }

    protected void onCreate() {
    }

    protected void onShow() {
    }

    protected void preShow() {
        if (this.root == null) {
            throw new IllegalStateException("error");
        }
        onShow();
        if (this.background == null) {
            this.window.setBackgroundDrawable(new BitmapDrawable());
        } else {
            this.window.setBackgroundDrawable(this.background);
        }
        this.window.setWidth(-2);
        this.window.setHeight(-2);
        this.window.setTouchable(true);
        this.window.setFocusable(true);
        this.window.setOutsideTouchable(true);
        this.window.setContentView(this.root);
    }

    public void setBackgroundDrawable(Drawable background) {
        this.background = background;
    }

    public void setContentView(View root) {
        this.root = root;
        this.window.setContentView(root);
    }

    public void setContentView(int layoutResID) {
        LayoutInflater inflator = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(inflator.inflate(layoutResID, null));
    }

    /** @noinspection unused*/
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        this.window.setOnDismissListener(listener);
    }

    public void dismiss() {
        this.window.dismiss();
    }
}