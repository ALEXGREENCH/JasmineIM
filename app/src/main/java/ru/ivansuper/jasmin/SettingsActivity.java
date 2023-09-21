package ru.ivansuper.jasmin;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.security.PasswordManager;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    public static SettingsActivity static_instance;
    public static PreferenceScreen static_instance_preference_screen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(3);
        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onResume() {
        super.onResume();
        static_instance = this;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void proceedSetup(PreferenceScreen prefs) {
        final Locker locker = new Locker();

        Preference p = prefs.findPreference("ms_chats_at_top");
        Preference p1 = prefs.findPreference("ms_two_screens_mode");
        Preference p2 = prefs.findPreference("ms_use_pass_security");

        boolean twoScreensModeEnabled = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ms_two_screens_mode", true);
        p.setEnabled(!twoScreensModeEnabled);

        p1.setOnPreferenceClickListener(preference -> {
            Log.e("SettingsActivity", "ms_two_screens_mode clicked");
            p.setEnabled(!twoScreensModeEnabled);
            return true;
        });

        p2.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.e("SettingsActivity", "ms_use_pass_security changed");
            return !locker.locked;
        });

        p2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            Dialog d = null;

            @Override
            public boolean onPreferenceClick(final Preference preference) {
                Log.e("SettingsActivity", "ms_use_pass_security clicked");
                boolean state = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this.getApplicationContext()).getBoolean("ms_use_pass_security", false);

                final EditText pass = new EditText(resources.ctx);
                resources.attachEditText(pass);
                pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pass.setTextSize(16.0f);

                final CheckBox enabled = new CheckBox(resources.ctx);
                resources.attachCheckStyle(enabled);
                enabled.setTextColor(Color.WHITE);
                enabled.setText(Locale.getString("s_turned_on"));
                enabled.setChecked(state);

                LinearLayout lay = new LinearLayout(resources.ctx);
                lay.setOrientation(LinearLayout.VERTICAL);
                lay.addView(pass);
                lay.addView(enabled);

                String hint = Locale.getString("s_ms_use_pass_security_hint");
                String okText = Locale.getString("s_ok");
                String cancelText = Locale.getString("s_cancel");

                View.OnClickListener okClickListener = v -> {
                    String password = pass.getText().toString();
                    int length = password.length();

                    if ((length == 0 && enabled.isChecked()) || length > 255) {
                        resources.service.showToast(Locale.getString("s_ms_use_pass_security_error"), 0);
                        return;
                    }

                    locker.locked = false;

                    try {
                        PasswordManager.savePassword(password);
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean("ms_use_pass_security", enabled.isChecked()).commit();

                        if (enabled.isChecked()) {
                            resources.service.showToast(Locale.getString("s_ms_use_pass_security_saved"), 0);
                        }
                    } catch (Exception e) {
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean("s_ms_use_pass_security_save_error", enabled.isChecked()).commit();
                    }

                    locker.locked = true;
                    d.dismiss();
                    resources.service.runOnUi(SettingsActivity.this::onContentChanged, 500L);
                };

                d = DialogBuilder.createYesNo(SettingsActivity.this, lay, 0, hint, okText, cancelText, okClickListener, v -> {
                    locker.locked = true;
                    d.dismiss();
                }, false);

                d.show();
                return true;
            }
        });
    }

    public static class Locker {
        public boolean locked = true;

        public Locker() {
        }
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);
        proceedSetup(preferenceScreen);
        static_instance_preference_screen = preferenceScreen;
        iteratePreferences(preferenceScreen);
    }

    private void iteratePreferences(Preference preference) {
        if (preference == null) {
            return;
        }

        String p = preference.getKey();
        String title = resources.getString("s_" + p);

        if (preference instanceof DialogPreference) {
            DialogPreference dp = (DialogPreference) preference;
            dp.setPositiveButtonText(resources.getString("s_ok"));
            dp.setNegativeButtonText(resources.getString("s_cancel"));
        } else //noinspection ConstantValue
            if (preference instanceof ListPreference) {
                ListPreference lp = (ListPreference) preference;
                lp.setDialogTitle(title);
                lp.setPositiveButtonText(resources.getString("s_ok"));
                lp.setNegativeButtonText(resources.getString("s_cancel"));
            }

        if (!title.equals("null")) {
            preference.setTitle(title);
            String desc = resources.getString("s_" + p + "_desc");
            if (!desc.equals("null")) {
                preference.setSummary(desc);
            }
        }

        if (preference instanceof PreferenceScreen) {
            PreferenceScreen screen = (PreferenceScreen) preference;
            for (int i = 0; i < screen.getPreferenceCount(); i++) {
                iteratePreferences(screen.getPreference(i));
            }
        }
    }

    /**
     * @noinspection unused
     */
    public static void update() {
        Log.e("Preferences", "static_instance_preference_screen is null: " + (static_instance_preference_screen == null));
        Log.e("Preferences", "static_instance is null: " + (static_instance == null));

        if (static_instance_preference_screen != null) {
            static_instance.setPreferenceScreen(static_instance_preference_screen);
        }

        if (static_instance != null) {
            static_instance.onContentChanged();
        }
    }
}
