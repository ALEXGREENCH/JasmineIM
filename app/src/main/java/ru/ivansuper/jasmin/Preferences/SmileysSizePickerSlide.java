package ru.ivansuper.jasmin.Preferences;

import android.annotation.SuppressLint;
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
import ru.ivansuper.jasmin.SmileysManager;
import ru.ivansuper.jasmin.animate_tools.SmileView;
import ru.ivansuper.jasmin.resources;

/**
 * A custom DialogPreference that allows the user to select the size of smileys using a SeekBar.
 *
 * <p>This preference displays a dialog with a SeekBar to adjust the smiley size and a preview of
 * a smiley with the selected size. The selected size is persisted in SharedPreferences.
 *
 * <p><b>XML attributes:</b>
 *
 * <ul>
 *   <li>{@code android:key}: The key to use for storing the preference value.
 *   <li>{@code android:title}: The title to display for the preference.
 *   <li>{@code android:defaultValue}: The default value for the smiley size (as a percentage, e.g.,
 *       "100").
 * </ul>
 *
 * <p><b>Usage:</b>
 *
 * <pre>{@code
 * <ru.ivansuper.jasmin.Preferences.SmileysSizePickerSlide
 *     android:key="smiley_size"
 *     android:title="Smiley Size"
 *     android:defaultValue="100" />
 * }</pre>
 */
public class SmileysSizePickerSlide extends DialogPreference {
    private final SharedPreferences manager;
    /** @noinspection FieldCanBeLocal, unused */
    private final int minimum;
    private int current;
    private int old;
    private SmileView sv;

    public SmileysSizePickerSlide(Context context, AttributeSet attrs) {
        //noinspection deprecation
        super(context, attrs);
        this.current = 0;
        this.minimum = 1;
        this.old = 0;
        //noinspection deprecation
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /** @noinspection deprecation*/
    @Override
    protected View onCreateDialogView() {
        return View.inflate(resources.ctx, R.layout.smiley_size_picker, null);
    }

    /** @noinspection deprecation*/
    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        //noinspection DataFlowIssue
        int parseInt = Integer.parseInt(this.manager.getString(super.getKey(), "100"));
        this.current = parseInt;
        this.old = parseInt;
        Log.e("Readed", String.valueOf(this.current));
        final TextView label = view.findViewById(R.id.l1);
        TextView title = view.findViewById(R.id.l2);
        title.setText(getTitle());
        label.setText(this.current + " %");
        label.setTextSize(18.0f);
        if (!SmileysManager.selector_smileys.isEmpty() && SmileysManager.packLoaded) {
            LinearLayout container = view.findViewById(R.id.smiley_container);
            container.removeAllViews();
            this.sv = new SmileView(getContext());
            this.sv.setIsTemporary();
            this.sv.setMovie(SmileysManager.selector_smileys.get(0));
            container.addView(this.sv);
        }
        SeekBar seekbar = view.findViewById(R.id.seekbar1);
        seekbar.setMax(49);
        seekbar.setProgress((this.current / 10) - 1);
        final SmileView wrap = this.sv;
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SmileysSizePickerSlide.this.current = (progress + 1) * 10;
                label.setText(SmileysSizePickerSlide.this.current + " %");
                wrap.setCustomScale(SmileysSizePickerSlide.this.current);
                wrap.postInvalidateDelayed(500L);
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
        this.sv.setCustomScale(this.old * 10);
        if (positiveResult) {
            Log.e("SmileysSizePicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}