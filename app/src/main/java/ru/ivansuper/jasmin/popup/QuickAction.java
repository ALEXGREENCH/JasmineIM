package ru.ivansuper.jasmin.popup;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.color_editor.ColorKey;
import ru.ivansuper.jasmin.color_editor.ColorUtils;
import ru.ivansuper.jasmin.resources;

/**
 * Represents a quick action popup window.
 * This class extends {@link CustomPopupWindow} and provides functionality
 * for displaying a popup with a header and a track for custom views.
 * It supports different animation styles for showing and dismissing the popup.
 */
public class QuickAction extends CustomPopupWindow {

    /**
     * @noinspection unused
     */
    protected static final int ANIM_AUTO = 5;
    /**
     * @noinspection unused
     */
    protected static final int ANIM_GROW_FROM_CENTER = 3;
    /**
     * @noinspection unused
     */
    protected static final int ANIM_GROW_FROM_LEFT = 1;
    /**
     * @noinspection unused
     */
    protected static final int ANIM_GROW_FROM_RIGHT = 2;
    /**
     * @noinspection unused
     */
    protected static final int ANIM_REFLECT = 4;
    /**
     * @noinspection FieldCanBeLocal, FieldMayBeFinal
     */
    private int animStyle;
    private final Context context;
    private final LayoutInflater inflater;
    private final TextView mHeaderLabel;
    private ViewGroup mTrack;
    private final View root;

    public QuickAction(View var1, String var2) {
        super(var1);
        this.context = var1.getContext();
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.root = (ViewGroup) this.inflater.inflate(R.layout.popup, (ViewGroup) null);
        this.mHeaderLabel = (TextView) this.root.findViewById(R.id.popup_wnd_header);
        LinearLayout var3 = (LinearLayout) this.root.findViewById(R.id.popup_header_divider);
        this.mTrack = (ViewGroup) this.root.findViewById(R.id.tracks);
        if (var2 != null) {
            var3.setBackgroundColor(ColorUtils.getColor(ColorKey.MENU_DIVIDERS));
            this.mHeaderLabel.setTextColor(ColorUtils.getColor(ColorKey.MENU_HEADERS));
            this.mHeaderLabel.setText(var2);
        } else {
            this.mTrack.removeView(var3);
            this.mTrack.removeView(this.mHeaderLabel);
        }

        resources.attachStatusSelectorBackAndArrow((LinearLayout) this.root.findViewById(R.id.popup_container));
        this.setContentView(this.root);
        this.animStyle = 5;
    }

    private void update() {
        int[] var1 = new int[2];
        this.anchor.getLocationOnScreen(var1);
        Rect var7 = new Rect(var1[0], var1[1], var1[0] + this.anchor.getWidth(), var1[1] + this.anchor.getHeight());
        this.root.measure(-2, -2);
        int var2 = this.root.getMeasuredHeight();
        int var3 = this.root.getMeasuredWidth();
        int var4 = var3;
        if (var3 > 1000) {
            var4 = this.root.getWidth();
        }

        int var5 = this.windowManager.getDefaultDisplay().getWidth();
        var3 = this.windowManager.getDefaultDisplay().getHeight();
        if (var7.left + var4 > var5) {
            var4 = var7.left - (var4 - this.anchor.getWidth());
        } else if (this.anchor.getWidth() > var4) {
            var4 = var7.centerX() - var4 / 2;
        } else {
            var4 = var7.left;
        }

        var5 = var7.top;
        int var6 = var3 - var7.bottom;
        boolean var8;
        if (var5 > var6) {
            var8 = true;
        } else {
            var8 = false;
        }

        if (var8) {
            if (var2 > var5) {
                var3 = 0;
                this.mTrack.getLayoutParams().height = var5 - this.anchor.getHeight();
            } else {
                var3 = var7.top - var2;
            }
        } else {
            var5 = var7.bottom;
            var3 = var5;
            if (var2 > var6) {
                this.mTrack.getLayoutParams().height = var6;
                var3 = var5;
            }
        }

        var5 = Math.max(var4, 0);

        Log.e("INFO:X", String.valueOf(var5));
        Log.e("INFO:Y", String.valueOf(var3));
        this.window.update(this.anchor, var5, var3);
    }

    public void dismiss() {
        super.dismiss();
    }

    public void setCustomView(View var1) {
        this.mTrack.addView(var1);
    }

    public void show() {
        this.preShow();
        int[] var1 = new int[2];
        this.anchor.getLocationOnScreen(var1);
        Rect var7 = new Rect(var1[0], var1[1], var1[0] + this.anchor.getWidth(), var1[1] + this.anchor.getHeight());
        this.root.setLayoutParams(new FrameLayout.LayoutParams(-2, -2));
        this.root.measure(-2, -2);
        int var2 = this.root.getMeasuredHeight();
        int var3 = this.root.getMeasuredWidth();
        int var4 = var3;
        if (var3 > 1000) {
            var4 = this.root.getWidth();
        }

        Log.e("INFO:WIDTH", String.valueOf(var2));
        Log.e("INFO:HEIGHT", String.valueOf(var4));
        int var5 = this.windowManager.getDefaultDisplay().getWidth();
        var3 = this.windowManager.getDefaultDisplay().getHeight();
        if (var7.left + var4 > var5) {
            var4 = var7.left - (var4 - this.anchor.getWidth());
        } else if (this.anchor.getWidth() > var4) {
            var4 = var7.centerX() - var4 / 2;
        } else {
            var4 = var7.left;
        }

        var5 = var7.top;
        int var6 = var3 - var7.bottom;
        if (true) {
            if (var2 > var5) {
                var3 = 0;
                this.mTrack.getLayoutParams().height = var5;
            } else {
                var3 = var7.top - var2;
            }
        } else {
            var5 = var7.bottom;
            var3 = var5;
            if (var2 > var6) {
                this.mTrack.getLayoutParams().height = var6;
                var3 = var5;
            }
        }

        var5 = Math.max(var4, 0);

        Log.e("INFO:X", String.valueOf(var5));
        Log.e("INFO:Y", String.valueOf(var3));
        this.window.showAtLocation(this.anchor, 0, var5, var3);
    }
}
