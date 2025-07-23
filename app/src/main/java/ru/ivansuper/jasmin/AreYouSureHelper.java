package ru.ivansuper.jasmin;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.locale.Locale;

/**
 * Helper class to display a "Are you sure?" dialog.
 * <p>
 * This class simplifies the creation and display of a standard confirmation dialog
 * with "Yes" and "No" options. It uses {@link DialogBuilder#createYesNo} to construct
 * the dialog and handles the click events for the buttons.
 */
public class AreYouSureHelper {
    Dialog dialog;

    public AreYouSureHelper(Activity context, String title, final View.OnClickListener yes, final View.OnClickListener no) {
        this.dialog = DialogBuilder.createYesNo(
                context,
                0,
                title,
                Locale.getString("s_are_you_sure"),
                Locale.getString("s_yes"),
                Locale.getString("s_no"),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AreYouSureHelper.this.dialog.dismiss();
                        if (yes != null) {
                            yes.onClick(view);
                        }
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        AreYouSureHelper.this.dialog.dismiss();
                        if (no != null) {
                            no.onClick(arg0);
                        }
                    }
                });
        this.dialog.show();
    }
}