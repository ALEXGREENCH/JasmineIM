package ru.ivansuper.jasmin.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

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

        this.window.setTouchInterceptor((v, event) -> {
            if (event.getAction() != 4) {
                return false;
            }
            CustomPopupWindow.this.dismiss();
            return true;
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
            //noinspection deprecation
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