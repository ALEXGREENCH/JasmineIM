package ru.ivansuper.jasmin.Preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * A dialog preference for selecting the chat style.
 *
 * This preference allows the user to choose between different chat styles,
 * which are presented as radio buttons in a dialog. The selected style is
 * saved in SharedPreferences.
 *
 * <p>The available chat styles are:
 * <ul>
 *   <li>Style 1: Defined by the string resource "s_ms_chat_style_1"
 *   <li>Style 2: Defined by the string resource "s_ms_chat_style_2"
 * </ul>
 *
 * <p>The dialog layout is inflated from {@code R.layout.columns_picker}.
 * The selected style is stored as a string ("0" for Style 1, "1" for Style 2)
 * under the preference key.
 */
public class ChatStylePicker extends DialogPreference {
    private final SharedPreferences manager;
    /** @noinspection FieldCanBeLocal, unused */
    private final int minimum;
    private int current;
    private RadioButton r1;
    private RadioButton r2;
    /** @noinspection FieldCanBeLocal*/
    private RadioButton r3;
    /** @noinspection FieldCanBeLocal*/
    private RadioButton r4;
    /** @noinspection FieldCanBeLocal*/
    private RadioButton r5;
    /** @noinspection FieldCanBeLocal*/
    private RadioButton r6;

    public ChatStylePicker(Context context, AttributeSet attrs) {
        //noinspection deprecation
        super(context, attrs);
        this.current = 0;
        this.minimum = 1;
        //noinspection deprecation
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /** @noinspection deprecation*/
    @Override
    protected View onCreateDialogView() {
        return View.inflate(resources.ctx, R.layout.columns_picker, null);
    }

    /** @noinspection deprecation*/
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        //noinspection DataFlowIssue
        this.current = Integer.parseInt(this.manager.getString(super.getKey(), "1"));
        TextView title = view.findViewById(R.id.l2);
        title.setText(getTitle());
        this.r1 = view.findViewById(R.id.r1);
        this.r2 = view.findViewById(R.id.r2);
        this.r3 = view.findViewById(R.id.r3);
        this.r4 = view.findViewById(R.id.r4);
        this.r5 = view.findViewById(R.id.r5);
        this.r6 = view.findViewById(R.id.r6);
        resources.attachRadioStyle(this.r1);
        resources.attachRadioStyle(this.r2);
        resources.attachRadioStyle(this.r3);
        resources.attachRadioStyle(this.r4);
        resources.attachRadioStyle(this.r5);
        resources.attachRadioStyle(this.r6);
        this.r1.setText(resources.getString("s_ms_chat_style_1"));
        this.r2.setText(resources.getString("s_ms_chat_style_2"));
        this.r3.setVisibility(View.GONE);
        this.r4.setVisibility(View.GONE);
        this.r5.setVisibility(View.GONE);
        this.r6.setVisibility(View.GONE);
        switch (this.current) {
            case 0:
                this.r1.setChecked(true);
                return;
            case 1:
                this.r2.setChecked(true);
                return;
            default:
        }
    }

    /** @noinspection deprecation*/
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (this.r1.isChecked()) {
                this.current = 0;
            }
            if (this.r2.isChecked()) {
                this.current = 1;
            }
            Log.e("ColumnsPicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}