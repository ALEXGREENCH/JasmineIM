package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Utility class for displaying custom dialogs.
 * This class provides static methods to show different types of dialogs within the application.
 */
public class ISDialog {
    public static void showAch(final Drawable icon, final String text) {
        resources.service.runOnUi(new Runnable() {
            @SuppressLint({"WrongConstant", "ClickableViewAccessibility"})
            @Override
            public void run() {
                final Dialog dialog = new Dialog(resources.service, R.style.DialogTheme);
                Window wnd = dialog.getWindow();
                //noinspection DataFlowIssue
                wnd.setGravity(85);
                WindowManager.LayoutParams lp = wnd.getAttributes();
                lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                lp.flags = 40;
                lp.width = -2;
                lp.height = -2;
                LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.ach, null);

                lay.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() != 0) {
                            return false;
                        }
                        dialog.dismiss();
                        return true;
                    }
                });
                ((ImageView) lay.findViewById(R.id.ach_icon)).setImageDrawable(icon);
                ((TextView) lay.findViewById(R.id.ach_title)).setText("Открыто достижение!");
                ((TextView) lay.findViewById(R.id.ach_desc)).setText(text);
                lp.windowAnimations = R.style.TopDialogAnimation;
                wnd.setBackgroundDrawable(new ColorDrawable(0));
                wnd.setContentView(lay);
                wnd.setAttributes(lp);
                dialog.show();
            }
        });
    }
}
