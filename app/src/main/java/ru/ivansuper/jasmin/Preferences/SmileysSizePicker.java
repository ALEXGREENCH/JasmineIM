package ru.ivansuper.jasmin.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/* loaded from: classes.dex */
public class SmileysSizePicker extends DialogPreference {
    private int current;
    private SharedPreferences manager;
    private final int minimum;
    private RadioButton r1;
    private RadioButton r2;
    private RadioButton r3;
    private RadioButton r4;
    private RadioButton r5;
    private RadioButton r6;

    public SmileysSizePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.current = 0;
        this.minimum = 1;
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override // android.preference.DialogPreference
    protected View onCreateDialogView() {
        LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.columns_picker, null);
        return lay;
    }

    @Override // android.preference.DialogPreference
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.current = Integer.parseInt(this.manager.getString(super.getKey(), "1"));
        TextView title = (TextView) view.findViewById(R.id.l2);
        title.setText(getTitle());
        this.r1 = (RadioButton) view.findViewById(R.id.r1);
        this.r1.setText(resources.getString("s_ms_smileys_scale_1"));
        this.r2 = (RadioButton) view.findViewById(R.id.r2);
        this.r2.setText(resources.getString("s_ms_smileys_scale_2"));
        this.r3 = (RadioButton) view.findViewById(R.id.r3);
        this.r3.setText(resources.getString("s_ms_smileys_scale_3"));
        this.r4 = (RadioButton) view.findViewById(R.id.r4);
        this.r4.setText(resources.getString("s_ms_smileys_scale_4"));
        this.r5 = (RadioButton) view.findViewById(R.id.r5);
        this.r5.setText(resources.getString("s_ms_smileys_scale_5"));
        this.r6 = (RadioButton) view.findViewById(R.id.r6);
        this.r6.setText(resources.getString("s_ms_smileys_scale_6"));
        switch (this.current) {
            case 0:
                this.r1.setChecked(true);
                return;
            case 1:
                this.r2.setChecked(true);
                return;
            case 2:
                this.r3.setChecked(true);
                return;
            case 3:
                this.r4.setChecked(true);
                return;
            case 4:
                this.r5.setChecked(true);
                return;
            case 5:
                this.r6.setChecked(true);
                return;
            default:
                return;
        }
    }

    @Override // android.preference.DialogPreference
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (this.r1.isChecked()) {
                this.current = 0;
            }
            if (this.r2.isChecked()) {
                this.current = 1;
            }
            if (this.r3.isChecked()) {
                this.current = 2;
            }
            if (this.r4.isChecked()) {
                this.current = 3;
            }
            if (this.r5.isChecked()) {
                this.current = 4;
            }
            if (this.r6.isChecked()) {
                this.current = 5;
            }
            Log.e("ColumnsPicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}