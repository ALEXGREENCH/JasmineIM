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
import ru.ivansuper.jasmin.SmileysManager;
import ru.ivansuper.jasmin.animate_tools.SmileView;
import ru.ivansuper.jasmin.resources;

/* loaded from: classes.dex */
public class SmileysSizePickerSlide extends DialogPreference {
    private int current;
    private SharedPreferences manager;
    private final int minimum;
    private int old;
    private SmileView sv;

    public SmileysSizePickerSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.current = 0;
        this.minimum = 1;
        this.old = 0;
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override // android.preference.DialogPreference
    protected View onCreateDialogView() {
        LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.smiley_size_picker, null);
        return lay;
    }

    @Override // android.preference.DialogPreference
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        int parseInt = Integer.parseInt(this.manager.getString(super.getKey(), "100"));
        this.current = parseInt;
        this.old = parseInt;
        Log.e("Readed", new StringBuilder().append(this.current).toString());
        final TextView label = (TextView) view.findViewById(R.id.l1);
        TextView title = (TextView) view.findViewById(R.id.l2);
        title.setText(getTitle());
        label.setText(String.valueOf(String.valueOf(this.current)) + " %");
        label.setTextSize(18.0f);
        if (SmileysManager.selector_smileys.size() > 0 && SmileysManager.packLoaded) {
            LinearLayout container = (LinearLayout) view.findViewById(R.id.smiley_container);
            container.removeAllViews();
            this.sv = new SmileView(getContext());
            this.sv.setIsTemporary();
            this.sv.setMovie(SmileysManager.selector_smileys.get(0));
            container.addView(this.sv);
        }
        SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekbar1);
        seekbar.setMax(49);
        seekbar.setProgress((this.current / 10) - 1);
        final SmileView wrap = this.sv;
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: ru.ivansuper.jasmin.Preferences.SmileysSizePickerSlide.1
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SmileysSizePickerSlide.this.current = (progress + 1) * 10;
                label.setText(String.valueOf(String.valueOf(SmileysSizePickerSlide.this.current)) + " %");
                wrap.setCustomScale(SmileysSizePickerSlide.this.current);
                wrap.postInvalidateDelayed(500L);
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
        this.sv.setCustomScale(this.old * 10);
        if (positiveResult) {
            Log.e("SmileysSizePicker", "Saving: " + this.current);
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
        }
    }
}