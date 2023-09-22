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

public class IntegerPickerMedium extends DialogPreference {
    private int current;
    private SharedPreferences manager;
    private final int minimum;

    public IntegerPickerMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.current = 0;
        this.minimum = 5;
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.integer_picker, null);
        return lay;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.current = Manager.getStringInt(super.getKey());
        final TextView label = view.findViewById(R.id.l1);
        TextView title = view.findViewById(R.id.l2);
        title.setText(getTitle());
        label.setText(String.valueOf(this.current));
        SeekBar seekbar = view.findViewById(R.id.seekbar1);
        seekbar.setMax(495);
        seekbar.setProgress(this.current - 5);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setText(String.valueOf(progress + 5));
                IntegerPickerMedium.this.current = progress + 5;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.e("IntegerPicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}