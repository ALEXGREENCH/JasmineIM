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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * A DialogPreference that allows the user to select a transition effect.
 * The selected effect is saved in SharedPreferences.
 *
 * <p>The dialog displays a list of available transition effects as RadioButtons.
 * When an effect is selected, the corresponding integer value is stored in SharedPreferences
 * under the key provided by {@link #getKey()}.</p>
 *
 * <p>The available transition effects are:
 * <ul>
 *   <li>Cube (0)</li>
 *   <li>Flip 1 (1)</li>
 *   <li>Flip 2 (2)</li>
 *   <li>Shift (3)</li>
 *   <li>Rotation 1 (4)</li>
 *   <li>Rotation 2 (5)</li>
 *   <li>Rotation 3 (6)</li>
 *   <li>ICS (7)</li>
 *   <li>ICS 2 (10)</li>
 *   <li>Snake (8)</li>
 *   <li>Rotation 4 (9)</li>
 *   <li>Random (-1)</li>
 * </ul>
 * </p>
 *
 * @see DialogPreference
 * @see SharedPreferences
 */
public class TransitionPicker extends DialogPreference {
    private final SharedPreferences manager;
    /** @noinspection FieldCanBeLocal, unused */
    private final int minimum;
    private int current;
    /** @noinspection unused*/
    private RadioButton r1;
    /** @noinspection unused*/
    private RadioButton r2;
    /** @noinspection unused*/
    private RadioButton r3;
    /** @noinspection unused*/
    private RadioButton r4;
    /** @noinspection unused*/
    private RadioButton r5;
    /** @noinspection unused*/
    private RadioButton r6;

    public TransitionPicker(Context context, AttributeSet attrs) {
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
        this.current = this.manager.getInt(super.getKey(), 7);
        TextView title = view.findViewById(R.id.l2);
        title.setText(getTitle());
        LinearLayout lay = (LinearLayout) view;
        RadioGroup rg = lay.findViewById(R.id.rg1);
        rg.removeAllViews();
        RadioButton r = new RadioButton(getContext());
        r.setText(resources.getString("s_transition_effect_cube"));
        resources.attachRadioStyle(r);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 0;
            }
        });
        rg.addView(r);
        if (this.current == 0) {
            r.setChecked(true);
        }
        RadioButton r2 = new RadioButton(getContext());
        r2.setText(resources.getString("s_transition_effect_flip1"));
        resources.attachRadioStyle(r2);
        r2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 1;
            }
        });
        rg.addView(r2);
        if (this.current == 1) {
            r2.setChecked(true);
        }
        RadioButton r3 = new RadioButton(getContext());
        r3.setText(resources.getString("s_transition_effect_flip2"));
        resources.attachRadioStyle(r3);
        r3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 2;
            }
        });
        rg.addView(r3);
        if (this.current == 2) {
            r3.setChecked(true);
        }
        RadioButton r4 = new RadioButton(getContext());
        r4.setText(resources.getString("s_transition_effect_shift"));
        resources.attachRadioStyle(r4);
        r4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 3;
            }
        });
        rg.addView(r4);
        if (this.current == 3) {
            r4.setChecked(true);
        }
        RadioButton r5 = new RadioButton(getContext());
        r5.setText(resources.getString("s_transition_effect_rot1"));
        resources.attachRadioStyle(r5);
        r5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 4;
            }
        });
        rg.addView(r5);
        if (this.current == 4) {
            r5.setChecked(true);
        }
        RadioButton r6 = new RadioButton(getContext());
        r6.setText(resources.getString("s_transition_effect_rot2"));
        resources.attachRadioStyle(r6);
        r6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 5;
            }
        });
        rg.addView(r6);
        if (this.current == 5) {
            r6.setChecked(true);
        }
        RadioButton r7 = new RadioButton(getContext());
        r7.setText(resources.getString("s_transition_effect_rot3"));
        resources.attachRadioStyle(r7);
        r7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 6;
            }
        });
        rg.addView(r7);
        if (this.current == 6) {
            r7.setChecked(true);
        }
        RadioButton r8 = new RadioButton(getContext());
        r8.setText(resources.getString("s_transition_effect_ics"));
        resources.attachRadioStyle(r8);
        r8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 7;
            }
        });
        rg.addView(r8);
        if (this.current == 7) {
            r8.setChecked(true);
        }
        RadioButton r9 = new RadioButton(getContext());
        r9.setText(resources.getString("s_transition_effect_ics2"));
        resources.attachRadioStyle(r9);
        r9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 10;
            }
        });
        rg.addView(r9);
        if (this.current == 10) {
            r9.setChecked(true);
        }
        RadioButton r10 = new RadioButton(getContext());
        r10.setText(resources.getString("s_transition_effect_snake"));
        resources.attachRadioStyle(r10);
        r10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 8;
            }
        });
        rg.addView(r10);
        if (this.current == 8) {
            r10.setChecked(true);
        }
        RadioButton r11 = new RadioButton(getContext());
        r11.setText(resources.getString("s_transition_effect_rot4"));
        resources.attachRadioStyle(r11);
        r11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = 9;
            }
        });
        rg.addView(r11);
        if (this.current == 9) {
            r11.setChecked(true);
        }
        RadioButton r12 = new RadioButton(getContext());
        r12.setText(resources.getString("s_transition_effect_rnd"));
        resources.attachRadioStyle(r12);
        r12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionPicker.this.current = -1;
            }
        });
        rg.addView(r12);
        if (this.current == -1) {
            r12.setChecked(true);
        }
    }

    /** @noinspection deprecation*/
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.e("TransitionPicker", "Saving: " + this.current);
            this.manager.edit().putInt(getKey(), this.current).commit();
        }
    }
}