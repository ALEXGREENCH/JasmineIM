package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.chats.Chat;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * Activity for selecting smileys.
 *
 * <p>This activity displays a grid of smileys and allows the user to select one.
 * The selected smiley is then returned to the calling activity.
 *
 * <p>The activity's theme and UI are initialized in the {@link #onCreate(Bundle)} method.
 * The theme is determined by the "ms_wallpaper_type" preference, which can be set to "0", "1", or "2".
 * The UI consists of a {@link GridView} that displays the smileys.
 *
 * <p>When a smiley is selected, the {@link AdapterView.OnItemClickListener} is triggered.
 * This listener creates an {@link Intent} containing the selected smiley's tag and returns it to the calling activity.
 *
 * <p>The activity's visibility is tracked by the {@link #VISIBLE} flag.
 * This flag is set to {@code true} in {@link #onResume()} and {@code false} in {@link #onPause()}.
 *
 * <p>The {@link #onResume()} method also notifies the adapter that the data set has changed,
 * ensuring that the smileys are displayed correctly.
 */
public class SmileysSelector extends Activity {

    /**
     * Flag indicating whether the activity is visible.
     * Set to {@code true} in {@link #onResume()} and {@code false} in {@link #onPause()}.
     */
    public static boolean VISIBLE;
    /**
     * The GridView used to display the smileys.
     */
    private GridView smileys;

    /**
     * Called when the activity is first created.
     * Initializes the activity's theme and UI.
     *
     * <p>The theme is determined by the "ms_wallpaper_type" preference, which can be set to "0", "1", or "2".
     * If the preference is set to "0", the activity uses the "WallpaperNoTitleTheme".
     * If the preference is set to "1" or "2", the activity uses the "BlackNoTitleTheme".
     * If the preference is set to "1", the activity's background is set to a custom wallpaper.
     * If the preference is set to "2", the activity's background is set to a solid color.
     *
     * <p>The UI consists of a {@link GridView} that displays the smileys.
     * The number of columns in the grid is determined by the "smileysSelectorColumns" preference.
     * The grid's adapter is a {@link smileys_adapter}.
     * When a smiley is selected, the {@link AdapterView.OnItemClickListener} is triggered.
     * This listener creates an {@link Intent} containing the selected smiley's tag and returns it to the calling activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources.applyFontScale(this);
        initializeTheme();
        initializeUI();
    }

    /**
     * Initializes the theme of the activity based on the "ms_wallpaper_type" preference.
     *
     * <p>The "ms_wallpaper_type" preference can have the following values:
     * <ul>
     *   <li>"0": Sets the theme to {@link R.style#WallpaperNoTitleTheme}.
     *   <li>"1": Sets the theme to {@link R.style#BlackNoTitleTheme} and sets the window background to a custom wallpaper.
     *   <li>"2": Sets the theme to {@link R.style#BlackNoTitleTheme} and sets the window background to a solid color.
     * </ul>
     *
     * <p>If the "ms_wallpaper_type" preference is "1" or "2", this method also attaches chat messages back to the window.
     */
    private void initializeTheme() {
        //noinspection deprecation
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String wallpaper_type = sp.getString("ms_wallpaper_type", "0");

        //noinspection DataFlowIssue
        if (sp.getBoolean("ms_telegram_style", false)) {
            setTheme(R.style.TelegramTheme);
        } else {
            switch (wallpaper_type) {
                case "0":
                    setTheme(R.style.WallpaperNoTitleTheme);
                    break;
                case "1":
                case "2":
                    setTheme(R.style.BlackNoTitleTheme);
                    Window wnd = getWindow();
                    if (wallpaper_type.equals("1")) {
                        wnd.setBackgroundDrawable(resources.custom_wallpaper);
                    } else //noinspection ConstantValue
                        if (wallpaper_type.equals("2")) {
                        wnd.setBackgroundDrawable(ColorScheme.getSolid(ColorScheme.getColor(13)));
                    }
                    resources.attachChatMessagesBack(wnd);
                    break;
            }
        }
    }

    /**
     * Initializes the UI components of the activity.
     *
     * <p>This method sets up the volume control stream, content view, and system bars.
     * It also initializes the {@link GridView} for displaying smileys, sets its adapter,
     * and defines the behavior for when a smiley is selected.
     *
     * <p>The number of columns in the grid is determined by the {@link PreferenceTable#smileysSelectorColumns} preference.
     * The background color of the grid is set to transparent if the "ms_use_shadow" preference is false.
     *
     * <p>When a smiley is selected, an {@link Intent} is created containing the smiley's tag.
     * This intent is then set as the result of the activity, and the activity is finished.
     */
    private void initializeUI() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.smileys_selector);
        SystemBarUtils.setupTransparentBars(this);
        smileys = findViewById(R.id.smileys_selector_field);
        smileys.setSelector(resources.getListSelector());

        //noinspection deprecation
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.getBoolean("ms_use_shadow", true)) {
            smileys.setBackgroundColor(Color.TRANSPARENT);
        }

        smileys.setNumColumns(PreferenceTable.smileysSelectorColumns);
        final smileys_adapter adapter = new smileys_adapter();
        smileys.setAdapter(adapter);
        smileys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tag = adapter.getTag(position);
                Intent i = new Intent(" " + tag + " ");
                Chat.received_smile_tag = i.getAction();
                setResult(Activity.RESULT_OK, i);
                finish();
            }
        });
    }

    /**
     * Called when the activity will start interacting with the user.
     * At this point your activity is at the top of the activity stack,
     * with user input going to it.
     *
     * <p>This method sets the {@link #VISIBLE} flag to {@code true} and notifies the adapter
     * that the data set has changed. This ensures that the smileys are displayed correctly.
     */
    @Override
    public void onResume() {
        super.onResume();
        VISIBLE = true;
        if (smileys != null) {
            ((smileys_adapter) smileys.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Called when the activity is no longer the current activity.
     * Sets the {@link #VISIBLE} flag to {@code false}.
     */
    @Override
    public void onPause() {
        super.onPause();
        VISIBLE = false;
    }
}