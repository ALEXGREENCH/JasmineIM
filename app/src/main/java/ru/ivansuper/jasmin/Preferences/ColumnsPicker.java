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

public class ColumnsPicker extends DialogPreference {
    private final SharedPreferences manager;
    /** @noinspection FieldCanBeLocal, FieldCanBeLocal , unused */
    private final int minimum;
    private int current;
    private RadioButton r1;
    private RadioButton r2;
    private RadioButton r3;
    private RadioButton r4;
    private RadioButton r5;
    private RadioButton r6;

    public ColumnsPicker(Context context, AttributeSet attrs) {
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
        this.current = Integer.parseInt(this.manager.getString(super.getKey(), "3"));
        Log.e("Readed", String.valueOf(this.current));
        TextView title = view.findViewById(R.id.l2);
        //noinspection deprecation
        title.setText(getTitle());
        this.r1 = view.findViewById(R.id.r1);
        this.r2 = view.findViewById(R.id.r2);
        this.r3 = view.findViewById(R.id.r3);
        this.r4 = view.findViewById(R.id.r4);
        this.r5 = view.findViewById(R.id.r5);
        this.r6 = view.findViewById(R.id.r6);
        switch (this.current) {
            case 1:
                this.r1.setChecked(true);
                return;
            case 2:
                this.r2.setChecked(true);
                return;
            case 3:
                this.r3.setChecked(true);
                return;
            case 4:
                this.r4.setChecked(true);
                return;
            case 5:
                this.r5.setChecked(true);
                return;
            case 6:
                this.r6.setChecked(true);
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
                this.current = 1;
            }
            if (this.r2.isChecked()) {
                this.current = 2;
            }
            if (this.r3.isChecked()) {
                this.current = 3;
            }
            if (this.r4.isChecked()) {
                this.current = 4;
            }
            if (this.r5.isChecked()) {
                this.current = 5;
            }
            if (this.r6.isChecked()) {
                this.current = 6;
            }
            Log.e("ColumnsPicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}