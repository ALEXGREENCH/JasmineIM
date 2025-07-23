package ru.ivansuper.jasmin.Preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * A custom {@link DialogPreference} that allows the user to pick an integer value
 * using a {@link SeekBar}. The selected value is persisted in {@link SharedPreferences}.
 * This preference is designed for picking relatively small integer values, with a
 * default minimum of 1 and a maximum of 50 (inclusive, as SeekBar's max is 49, and progress starts from 0).
 *
 * <p>The dialog displays the current value, the title of the preference, and a SeekBar
 * to adjust the value. The text size of the displayed current value also changes
 * dynamically with the selected integer.
 *
 * <p>Usage in XML:
 * <pre>
 * &lt;ru.ivansuper.jasmin.Preferences.IntegerPickerSmall
 *     android:key="your_preference_key"
 *     android:title="Select a Value"
 *     android:summary="Current value: %s"
 *     android:defaultValue="10" /&gt;
 * </pre>
 * Note: The `android:defaultValue` should be a string representation of an integer.
 */
public class IntegerPickerSmall extends DialogPreference {
    private final SharedPreferences manager;
    /** @noinspection FieldCanBeLocal, unused */
    private final int minimum;
    private int current;

    public IntegerPickerSmall(Context context, AttributeSet attrs) {
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
        return View.inflate(resources.ctx, R.layout.integer_picker, null);
    }

    /** @noinspection deprecation*/
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.current = Manager.getStringInt(super.getKey());
        final TextView label = view.findViewById(R.id.l1);
        TextView title = view.findViewById(R.id.l2);
        title.setText(getTitle());
        label.setText(String.valueOf(this.current));
        label.setTextSize(this.current);
        SeekBar seekbar = view.findViewById(R.id.seekbar1);
        seekbar.setMax(49);
        seekbar.setProgress(this.current - 1);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setText(String.valueOf(progress + 1));
                IntegerPickerSmall.this.current = progress + 1;
                label.setTextSize(IntegerPickerSmall.this.current);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /** @noinspection deprecation*/
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.e("IntegerPicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}