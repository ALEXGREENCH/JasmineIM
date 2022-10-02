package ru.ivansuper.jasmin;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
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
        boolean z = true;
        Preference p = prefs.findPreference("ms_chats_at_top");
        Preference p1 = prefs.findPreference("ms_two_screens_mode");
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ms_two_screens_mode", true)) {
            z = false;
        }
        p.setEnabled(z);
        p1.setOnPreferenceClickListener(preference -> {
            Log.e("SettingsActivity", "ms_two_screens_mode clicked");
            Preference p2 = SettingsActivity.this.findPreference("ms_chats_at_top");
            p2.setEnabled(!PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this.getApplicationContext()).getBoolean("ms_two_screens_mode", true));
            return true;
        });
        final Locker locker = new Locker(null);
        Preference p2 = prefs.findPreference("ms_use_pass_security");
        p2.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.e("SettingsActivity", "ms_use_pass_security changed");
            return !locker.locked;
        });
        p2.setOnPreferenceClickListener(new AnonymousClass3(locker));
    }

    public class AnonymousClass3 implements Preference.OnPreferenceClickListener {
        Dialog d = null;
        private final /* synthetic */ Locker val$locker;

        AnonymousClass3(Locker locker) {
            this.val$locker = locker;
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            Log.e("SettingsActivity", "ms_use_pass_security clicked");
            boolean state = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this.getApplicationContext()).getBoolean("ms_use_pass_security", false);
            final EditText pass = new EditText(resources.ctx);
            resources.attachEditText(pass);
            pass.setInputType(129);
            pass.setTextSize(16.0f);
            final CheckBox enabled = new CheckBox(resources.ctx);
            resources.attachCheckStyle(enabled);
            enabled.setTextColor(-1);
            enabled.setText(Locale.getString("s_turned_on"));
            enabled.setChecked(state);
            LinearLayout lay = new LinearLayout(resources.ctx);
            lay.setOrientation(LinearLayout.VERTICAL);
            lay.addView(pass);
            lay.addView(enabled);
            SettingsActivity settingsActivity = SettingsActivity.this;
            String string = Locale.getString("s_ms_use_pass_security_hint");
            String string2 = Locale.getString("s_ok");
            String string3 = Locale.getString("s_cancel");
            final Locker locker = this.val$locker;
            View.OnClickListener onClickListener = v -> {
                String password = pass.getText().toString();
                int length = password.length();
                if ((length == 0 && enabled.isChecked()) || length > 255) {
                    resources.service.showToast(Locale.getString("s_ms_use_pass_security_error"), 0);
                    return;
                }
                locker.locked = false;
                try {
                    PasswordManager.savePassword(password);
                    preference.getEditor().putBoolean("ms_use_pass_security", enabled.isChecked()).commit();
                    if (enabled.isChecked()) {
                        resources.service.showToast(Locale.getString("s_ms_use_pass_security_saved"), 0);
                    }
                } catch (Exception e) {
                    preference.getEditor().putBoolean("s_ms_use_pass_security_save_error", enabled.isChecked()).commit();
                }
                locker.locked = true;
                AnonymousClass3.this.d.dismiss();
                resources.service.runOnUi(SettingsActivity.this::onContentChanged, 500L);
            };
            final Locker locker2 = this.val$locker;
            this.d = DialogBuilder.createYesNo(settingsActivity, lay, 0, string, string2, string3, onClickListener, v -> {
                locker2.locked = true;
                AnonymousClass3.this.d.dismiss();
            }, false);
            this.d.show();
            return true;
        }
    }

    public static class Locker {
        public boolean locked;

        private Locker() {
            this.locked = true;
        }

        /* synthetic */
        @SuppressWarnings("CopyConstructorMissesField")
        Locker(@SuppressWarnings("unused") Locker locker) {
            this();
        }
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);
        proceedSetup(preferenceScreen);
        static_instance_preference_screen = preferenceScreen;
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            Preference p = preferenceScreen.getPreference(i);
            proceedPreference(p);
            try {
                PreferenceScreen s = (PreferenceScreen) p;
                for (int j = 0; j < s.getPreferenceCount(); j++) {
                    Preference pp = s.getPreference(j);
                    proceedPreference(pp);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void proceedPreference(Preference preference) {
        String p = preference.getKey();
        String title = resources.getString("s_" + p);
        try {
            DialogPreference dp = (DialogPreference) preference;
            dp.setPositiveButtonText(resources.getString("s_ok"));
            dp.setNegativeButtonText(resources.getString("s_cancel"));
        } catch (ClassCastException ignored) {
        }
        try {
            ListPreference lp = (ListPreference) preference;
            lp.setDialogTitle(title);
            lp.setPositiveButtonText(resources.getString("s_ok"));
            lp.setNegativeButtonText(resources.getString("s_cancel"));
        } catch (ClassCastException ignored) {
        }
        if (!title.equals("null")) {
            preference.setTitle(title);
            String desc = resources.getString("s_" + p + "_desc");
            if (!desc.equals("null")) {
                preference.setSummary(desc);
            }
        }
    }

    @SuppressWarnings("unused")
    public static void update() {
        boolean z = true;
        Log.e("Preferences", "static_instance_preference_screen is null: " + (static_instance_preference_screen == null));
        StringBuilder sb = new StringBuilder("static_instance is null: ");
        if (static_instance != null) {
            z = false;
        }
        Log.e("Preferences", sb.append(z).toString());
        if (static_instance_preference_screen != null) {
            static_instance.setPreferenceScreen(static_instance_preference_screen);
        }
        if (static_instance != null) {
            static_instance.onContentChanged();
        }
    }
}
