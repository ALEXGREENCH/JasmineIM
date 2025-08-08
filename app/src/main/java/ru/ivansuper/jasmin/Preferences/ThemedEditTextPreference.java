package ru.ivansuper.jasmin.Preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import ru.ivansuper.jasmin.resources;

/**
 * EditTextPreference that applies Jasmine's theme to the embedded EditText.
 * This ensures text color and background match the current color scheme
 * when the preference dialog is displayed.
 */
public class ThemedEditTextPreference extends EditTextPreference {
    public ThemedEditTextPreference(Context context, AttributeSet attrs) {
        //noinspection deprecation
        super(context, attrs);
    }

    /** @noinspection deprecation*/
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText edit = getEditText();
        if (edit != null) {
            resources.attachEditText(edit);
        }
    }
}
