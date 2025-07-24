package ru.ivansuper.jasmin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * Activity for managing smiley packs.
 * Allows users to select and apply different smiley packs.
 */
public class SmileysManagerActivity extends Activity {

    /**
     * LinearLayout that displays the list of available smiley packs.
     */
    private LinearLayout list;
    /**
     * The name of the currently selected smiley pack.
     * This value is loaded from shared preferences and updated when the user selects a different pack.
     * The default value is "$*INTERNAL*$", representing the built-in smiley pack.
     */
    private String selectedPack = "";
    /**
     * SharedPreferences instance for storing and retrieving smiley pack preferences.
     */
    private SharedPreferences sp;

    /**
     * Called when the activity is first created.
     * Initializes the activity, sets up the layout, and loads smiley packs.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources.applyFontScale(this);
        //noinspection deprecation
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        selectedPack = sp.getString("current_smileys_pack", "$*INTERNAL*$");
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.smileys_manager);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
        fillList();
    }

    /**
     * Initializes the views for the activity.
     * Sets the text for title TextViews, initializes the smiley pack list,
     * and sets up the "Apply" button with its click listener.
     */
    private void initViews() {
        TextView l1TextView = findViewById(R.id.l1);
        l1TextView.setText(resources.getString("s_smileys_manager_title_1"));

        TextView l2TextView = findViewById(R.id.l2);
        l2TextView.setText(resources.getString("s_smileys_manager_title_2"));

        list = findViewById(R.id.smileys_manager_pack_list);

        Button apply = findViewById(R.id.smileys_manager_apply);
        apply.setText(resources.getString("s_smileys_manager_apply"));
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putString("current_smileys_pack", selectedPack).apply();
                SmileysManager.loadPack();
                Toast.makeText(SmileysManagerActivity.this, resources.getString("s_selection_saved"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Populates the list of smiley packs.
     * Clears the existing list and adds the internal pack.
     * If an SD card is mounted, it scans the "Smileys" directory for additional packs
     * and adds them to the list.
     */
    private void fillList() {
        list.removeAllViews();

        addPackItem("$*INTERNAL*$", resources.getString("s_internal"));

        if (resources.sd_mounted()) {
            File smileysDir = new File(resources.JASMINE_SD_PATH + "Smileys");
            File[] packs = smileysDir.listFiles();
            if (packs != null) {
                for (File file : packs) {
                    if (file.isDirectory()) {
                        addPackItem(file.getName(), file.getName());
                    }
                }
            }
        }
    }

    /**
     * Adds a smiley pack item to the list.
     *
     * @param packName    The internal name of the smiley pack.
     * @param displayText The text to display for the smiley pack.
     */
    private void addPackItem(String packName, String displayText) {
        TextView pack = createPackTextView(displayText, packName);

        if (selectedPack.equals(packName)) {
            pack.setBackgroundColor(ColorScheme.getColor(47));
        }

        list.addView(pack);
    }

    /**
     * Creates a TextView for a smiley pack.
     *
     * @param text The text to display on the TextView.
     * @param packName The name of the smiley pack.
     * @return The created TextView.
     */
    private TextView createPackTextView(String text, String packName) {
        TextView packTextView = new TextView(this);
        packTextView.setTextColor(Color.WHITE);
        packTextView.setShadowLayer(1.0f, 0.0f, 0.0f, Color.BLACK);
        packTextView.setTextSize(14.0f);
        packTextView.setPadding(15, 15, 15, 15);
        packTextView.setGravity(Gravity.CENTER);
        packTextView.setText(text);
        packTextView.setOnClickListener(new PackClickListener(packName));
        return packTextView;
    }

    /**
     * Handles click events on smiley pack items.
     * When a pack is clicked, it updates the selected pack and refreshes the list.
     */
    private class PackClickListener implements View.OnClickListener {

        /**
         * The name of the smiley pack associated with this listener.
         * This field stores the identifier for the smiley pack that
         * will be selected when the associated view is clicked.
         */
        private final String packName;

        /**
         * Constructs a new PackClickListener.
         *
         * @param packName The name of the smiley pack associated with this listener.
         */
        PackClickListener(String packName) {
            this.packName = packName;
        }

        /**
         * Handles the click event on a smiley pack.
         * Sets the selected pack to the clicked pack and refreshes the list.
         *
         * @param v The view that was clicked (the smiley pack TextView).
         */
        @Override
        public void onClick(View v) {
            selectedPack = packName;
            fillList();
        }
    }
}
