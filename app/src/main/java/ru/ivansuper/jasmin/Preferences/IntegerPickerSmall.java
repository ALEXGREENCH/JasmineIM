package ru.ivansuper.jasmin.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/* loaded from: classes.dex */
public class IntegerPickerSmall extends DialogPreference {
    private int current;
    private SharedPreferences manager;
    private final int minimum;

    public IntegerPickerSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.current = 0;
        this.minimum = 1;
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override // android.preference.DialogPreference
    protected View onCreateDialogView() {
        LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.integer_picker, null);
        return lay;
    }

    @Override // android.preference.DialogPreference
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.current = Manager.getStringInt(super.getKey());
        final TextView label = (TextView) view.findViewById(R.id.l1);
        TextView title = (TextView) view.findViewById(R.id.l2);
        title.setText(getTitle());
        label.setText(String.valueOf(this.current));
        label.setTextSize(this.current);
        SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekbar1);
        seekbar.setMax(49);
        seekbar.setProgress(this.current - 1);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: ru.ivansuper.jasmin.Preferences.IntegerPickerSmall.1
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setText(String.valueOf(progress + 1));
                IntegerPickerSmall.this.current = progress + 1;
                label.setTextSize(IntegerPickerSmall.this.current);
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override // android.preference.DialogPreference
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.e("IntegerPicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}