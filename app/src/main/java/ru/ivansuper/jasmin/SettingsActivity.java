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
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * Activity for managing application settings.
 * It extends {@link PreferenceActivity} to provide a standard interface for settings.
 * This class handles the initialization and management of various preferences,
 * including UI settings, security options, and other application-specific configurations.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Loading preferences from an XML resource.</li>
 *     <li>Setting up transparent system bars.</li>
 *     <li>Handling lifecycle events (onCreate, onResume, onPause).</li>
 *     <li>Dynamically enabling/disabling preferences based on other settings (e.g., "ms_chats_at_top" based on "ms_two_screens_mode").</li>
 *     <li>Managing password security settings, including a dialog for password input.</li>
 *     <li>Iterating through preferences to set localized titles, summaries, and dialog button texts.</li>
 *     <li>Providing a static method to update the preference screen and content when settings change externally.</li>
 * </ul>
 * </p>
 *
 * <p>It uses a {@link Locker} inner class to prevent unintended preference changes during certain operations,
 * particularly when handling password setup.
 * </p>
 *
 * <p>Static instances ({@code static_instance} and {@code static_instance_preference_screen}) are used
 * to allow other parts of the application to trigger updates to the settings UI.
 * </p>
 *
 * @see PreferenceActivity
 * @see PreferenceScreen
 * @see Preference
 * @see PasswordManager
 * @see Locale
 * @see SystemBarUtils
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    /**
     * Static instance of the SettingsActivity.
     * This is used to allow other parts of the application to access and interact with the
     * settings activity, such as triggering UI updates when settings are changed externally.
     * It is set in {@code onResume} and can be null if the activity is not currently active.
     */
    public static SettingsActivity static_instance;
    /**
     * Static instance of the preference screen, used to allow other parts of the application
     * to trigger updates to the settings UI. This is typically updated when the preference
     * screen is set or when settings are changed externally.
     */
    public static PreferenceScreen static_instance_preference_screen;

    /**
     * Called when the activity is first created.
     * This method initializes the activity by:
     * <ul>
     *     <li>Setting the volume control stream to STREAM_MUSIC (value 3).</li>
     *     <li>Loading preferences from the {@code R.xml.prefs} resource file.</li>
     *     <li>Setting up transparent system bars using {@link SystemBarUtils}.</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(3);
        addPreferencesFromResource(R.xml.prefs);
        SystemBarUtils.setupTransparentBars(this);
    }

    /**
     * Called when the activity will start interacting with the user.
     * At this point your activity is at the top of the activity stack,
     * with user input going to it.
     *
     * <p>This method sets the static instance of this activity to the current instance.
     * This allows other parts of the application to access this activity, for example,
     * to update the settings UI when a setting is changed externally.
     * </p>
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        static_instance = this;
    }

    /**
     * Called when the activity is no longer the current activity.
     * This method is part of the Android activity lifecycle.
     * It ensures that the superclass's onPause logic is executed.
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Sets up the behavior and interactions for specific preferences within the given PreferenceScreen.
     * This method is responsible for:
     * <p>
     * 1.  **"ms_chats_at_top" preference (p):**
     *     - Its enabled state is determined by the "ms_two_screens_mode" preference.
     *     - If "ms_two_screens_mode" is enabled, "ms_chats_at_top" is disabled, and vice-versa.
     * <p>
     * 2.  **"ms_two_screens_mode" preference (p1):**
     *     -   When clicked, it toggles the enabled state of the "ms_chats_at_top" preference.
     * <p>
     * 3.  **"ms_use_pass_security" preference (p2):**
     *     -   **Change Listener:** Prevents changes if the {@code locker.locked} flag is true.
     *     -   **Click Listener:**
     *         -   Displays a dialog for password setup.
     *         -   The dialog contains an {@link EditText} for password input and a {@link CheckBox} to enable/disable password security.
     *         -   **OK Button:**
     *             -   Validates the password length (must not be empty if security is enabled, and not longer than 255 characters).
     *             -   If valid, saves the password using {@link PasswordManager#savePassword(String)}.
     *             -   Updates the "ms_use_pass_security" preference value.
     *             -   Shows a toast message confirming the save or indicating an error.
     *             -   Unlocks and then re-locks the {@code locker}.
     *             -   Dismisses the dialog and triggers {@link #onContentChanged()} after a delay.
     *         -   **Cancel Button:**
     *             -   Re-locks the {@code locker}.
     *             -   Dismisses the dialog.
     *
     * @param prefs The {@link PreferenceScreen} containing the preferences to be set up.
     */
    private void proceedSetup(PreferenceScreen prefs) {
        final Locker locker = new Locker();

        final Preference p = prefs.findPreference("ms_chats_at_top");
        Preference p1 = prefs.findPreference("ms_two_screens_mode");
        Preference p2 = prefs.findPreference("ms_use_pass_security");

        final boolean twoScreensModeEnabled = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ms_two_screens_mode", true);
        p.setEnabled(!twoScreensModeEnabled);

        p1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.e("SettingsActivity", "ms_two_screens_mode clicked");
                p.setEnabled(!twoScreensModeEnabled);
                return true;
            }
        });

        p2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Log.e("SettingsActivity", "ms_use_pass_security changed");
                return !locker.locked;
            }
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

                View.OnClickListener okClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                        resources.service.runOnUi(new Runnable() {
                            @Override
                            public void run() {
                                SettingsActivity.this.onContentChanged();
                            }
                        }, 500L);
                    }
                };

                d = DialogBuilder.createYesNo(SettingsActivity.this, lay, 0, hint, okText, cancelText, okClickListener, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        locker.locked = true;
                        d.dismiss();
                    }
                }, false);

                d.show();
                return true;
            }
        });
    }

    /**
     * A simple locking mechanism to prevent unintended preference changes.
     * This is used, for example, to disable preference changes while a password dialog is active.
     * <p>
     * The {@code locked} field determines the state of the lock.
     * By default, the locker is initialized in a locked state ({@code locked = true}).
     */
    public static class Locker {
        /**
         * Flag to indicate if preference changes are locked.
         * When true, preference changes are prevented, typically during sensitive operations like password setup.
         * Defaults to true.
         */
        public boolean locked = true;

        /**
         * Constructs a new Locker instance.
         * The locker is initially in a locked state.
         */
        public Locker() {
        }
    }

    /**
     * Sets the preference screen for the activity.
     * This method is called to display a new preference screen. It performs the following actions:
     * <ol>
     *     <li>Calls the superclass's {@code setPreferenceScreen} method to handle the basic setup.</li>
     *     <li>Invokes {@link #proceedSetup(PreferenceScreen)} to configure specific preference behaviors,
     *         such as enabling/disabling dependent preferences and setting up listeners for security settings.</li>
     *     <li>Assigns the provided {@code preferenceScreen} to the static field {@code static_instance_preference_screen},
     *         allowing other parts of the application to access and potentially update the current preference screen.</li>
     *     <li>Calls {@link #iteratePreferences(Preference)} to traverse through all preferences in the screen
     *         and apply localized titles, summaries, and dialog button texts.</li>
     * </ol>
     *
     * @param preferenceScreen The {@link PreferenceScreen} to display.
     */
    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);
        proceedSetup(preferenceScreen);
        static_instance_preference_screen = preferenceScreen;
        iteratePreferences(preferenceScreen);
    }

    /**
     * Recursively iterates through the preference hierarchy, setting localized titles,
     * summaries, and dialog button texts.
     * <p>
     * For {@link DialogPreference} and {@link ListPreference} instances, it sets the
     * positive and negative button texts to localized "OK" and "Cancel" strings.
     * For {@link ListPreference}, it also sets the dialog title.
     * <p>
     * It attempts to load the title and summary for each preference using a key
     * constructed as "s_" + preferenceKey and "s_" + preferenceKey + "_desc"
     * respectively from the application's resources. If a valid title is found,
     * it's set on the preference. If a valid description is found, it's set as
     * the summary.
     * <p>
     * If the given preference is a {@link PreferenceScreen}, this method is called
     * recursively for each of its child preferences.
     *
     * @param preference The {@link Preference} object to process. If null, the method returns immediately.
     */
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
     * Updates the preference screen and content.
     * This method is used to refresh the settings UI when changes are made externally.
     * It checks if the static instances of the preference screen and the activity are available,
     * and if so, it updates the preference screen and notifies that the content has changed.
     *
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
