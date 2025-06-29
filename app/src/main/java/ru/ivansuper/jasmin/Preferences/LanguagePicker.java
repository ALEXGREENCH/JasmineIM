package ru.ivansuper.jasmin.Preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.SettingsActivity;
import ru.ivansuper.jasmin.locale.Language;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;

public class LanguagePicker extends DialogPreference {
    private final SharedPreferences manager;
    private int current;

    public LanguagePicker(Context context, AttributeSet attrs) {
        //noinspection deprecation
        super(context, attrs);
        this.current = 0;
        //noinspection deprecation
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /** @noinspection deprecation*/
    @Override
    protected View onCreateDialogView() {
        return View.inflate(resources.ctx, R.layout.columns_picker, null);
    }

    /** @noinspection deprecation*/
    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        //noinspection DataFlowIssue
        this.current = Integer.parseInt(this.manager.getString(super.getKey(), String.valueOf(Locale.DEFAULT)));
        TextView title = view.findViewById(R.id.l2);
        title.setText(getTitle());
        LinearLayout lay = (LinearLayout) view;
        RadioGroup rg = lay.findViewById(R.id.rg1);
        rg.removeAllViews();
        ArrayList<Language> list = Locale.getAvailable();
        int i = 0;
        for (Language language : list) {
            RadioButton r = new RadioButton(getContext());
            r.setText(language.NAME + "\n" + resources.getString("s_ms_select_language_language") + " " + language.LANGUAGE + "\n" + resources.getString("s_ms_select_language_author") + " " + language.AUTHOR);
            final int ii = i;
            r.setOnClickListener(arg0 -> LanguagePicker.this.current = ii);
            rg.addView(r);
            if (this.current == i) {
                r.setChecked(true);
            }
            i++;
        }
    }

    /** @noinspection deprecation*/
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
            Locale.prepare();
            SettingsActivity.update();
        }
    }
}