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
import android.widget.RadioGroup;
import android.widget.TextView;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/* loaded from: classes.dex */
public class TransitionPicker extends DialogPreference {
    private int current;
    private SharedPreferences manager;
    private final int minimum;
    private RadioButton r1;
    private RadioButton r2;
    private RadioButton r3;
    private RadioButton r4;
    private RadioButton r5;
    private RadioButton r6;

    public TransitionPicker(Context context, AttributeSet attrs) {
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
        this.current = this.manager.getInt(super.getKey(), 7);
        TextView title = (TextView) view.findViewById(R.id.l2);
        title.setText(getTitle());
        LinearLayout lay = (LinearLayout) view;
        RadioGroup rg = (RadioGroup) lay.findViewById(R.id.rg1);
        rg.removeAllViews();
        RadioButton r = new RadioButton(getContext());
        r.setText(resources.getString("s_transition_effect_cube"));
        r.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.1
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 0;
            }
        });
        rg.addView(r);
        if (this.current == 0) {
            r.setChecked(true);
        }
        RadioButton r2 = new RadioButton(getContext());
        r2.setText(resources.getString("s_transition_effect_flip1"));
        r2.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.2
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 1;
            }
        });
        rg.addView(r2);
        if (this.current == 1) {
            r2.setChecked(true);
        }
        RadioButton r3 = new RadioButton(getContext());
        r3.setText(resources.getString("s_transition_effect_flip2"));
        r3.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.3
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 2;
            }
        });
        rg.addView(r3);
        if (this.current == 2) {
            r3.setChecked(true);
        }
        RadioButton r4 = new RadioButton(getContext());
        r4.setText(resources.getString("s_transition_effect_shift"));
        r4.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.4
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 3;
            }
        });
        rg.addView(r4);
        if (this.current == 3) {
            r4.setChecked(true);
        }
        RadioButton r5 = new RadioButton(getContext());
        r5.setText(resources.getString("s_transition_effect_rot1"));
        r5.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.5
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 4;
            }
        });
        rg.addView(r5);
        if (this.current == 4) {
            r5.setChecked(true);
        }
        RadioButton r6 = new RadioButton(getContext());
        r6.setText(resources.getString("s_transition_effect_rot2"));
        r6.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.6
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 5;
            }
        });
        rg.addView(r6);
        if (this.current == 5) {
            r6.setChecked(true);
        }
        RadioButton r7 = new RadioButton(getContext());
        r7.setText(resources.getString("s_transition_effect_rot3"));
        r7.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.7
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 6;
            }
        });
        rg.addView(r7);
        if (this.current == 6) {
            r7.setChecked(true);
        }
        RadioButton r8 = new RadioButton(getContext());
        r8.setText(resources.getString("s_transition_effect_ics"));
        r8.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.8
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 7;
            }
        });
        rg.addView(r8);
        if (this.current == 7) {
            r8.setChecked(true);
        }
        RadioButton r9 = new RadioButton(getContext());
        r9.setText(resources.getString("s_transition_effect_ics2"));
        r9.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.9
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 10;
            }
        });
        rg.addView(r9);
        if (this.current == 10) {
            r9.setChecked(true);
        }
        RadioButton r10 = new RadioButton(getContext());
        r10.setText(resources.getString("s_transition_effect_snake"));
        r10.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.10
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 8;
            }
        });
        rg.addView(r10);
        if (this.current == 8) {
            r10.setChecked(true);
        }
        RadioButton r11 = new RadioButton(getContext());
        r11.setText(resources.getString("s_transition_effect_rot4"));
        r11.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.11
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = 9;
            }
        });
        rg.addView(r11);
        if (this.current == 9) {
            r11.setChecked(true);
        }
        RadioButton r12 = new RadioButton(getContext());
        r12.setText(resources.getString("s_transition_effect_rnd"));
        r12.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.Preferences.TransitionPicker.12
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                TransitionPicker.this.current = -1;
            }
        });
        rg.addView(r12);
        if (this.current == -1) {
            r12.setChecked(true);
        }
    }

    @Override // android.preference.DialogPreference
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.e("TransitionPicker", "Saving: " + this.current);
            this.manager.edit().putInt(getKey(), this.current).commit();
        }
    }
}