package ru.ivansuper.jasmin.Preferences;

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
import java.util.Iterator;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.SettingsActivity;
import ru.ivansuper.jasmin.locale.Language;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;

public class LanguagePicker extends DialogPreference {
    private int current;
    private SharedPreferences manager;

    public LanguagePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.current = 0;
        this.manager = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.columns_picker, null);
        return lay;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.current = Integer.parseInt(this.manager.getString(super.getKey(), String.valueOf(Locale.DEFAULT)));
        TextView title = view.findViewById(R.id.l2);
        title.setText(getTitle());
        LinearLayout lay = (LinearLayout) view;
        RadioGroup rg = lay.findViewById(R.id.rg1);
        rg.removeAllViews();
        ArrayList<Language> list = Locale.getAvailable();
        int i = 0;
        Iterator<Language> it = list.iterator();
        while (it.hasNext()) {
            Language language = it.next();
            RadioButton r = new RadioButton(getContext());
            r.setText(String.valueOf(language.NAME) + "\n" + resources.getString("s_ms_select_language_language") + " " + language.LANGUAGE + "\n" + resources.getString("s_ms_select_language_author") + " " + language.AUTHOR);
            final int ii = i;
            r.setOnClickListener(arg0 -> LanguagePicker.this.current = ii);
            rg.addView(r);
            if (this.current == i) {
                r.setChecked(true);
            }
            i++;
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            this.manager.edit().putString(getKey(), String.valueOf(this.current)).commit();
            Locale.prepare();
            SettingsActivity.update();
        }
    }
}