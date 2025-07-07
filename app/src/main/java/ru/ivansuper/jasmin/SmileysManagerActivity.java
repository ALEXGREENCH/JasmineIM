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

public class SmileysManagerActivity extends Activity {

    private LinearLayout list;
    private String selectedPack = "";
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection deprecation
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        selectedPack = sp.getString("current_smileys_pack", "$*INTERNAL*$");
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.smileys_manager);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
        fillList();
    }

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

    private void addPackItem(String packName, String displayText) {
        TextView pack = createPackTextView(displayText, packName);

        if (selectedPack.equals(packName)) {
            pack.setBackgroundColor(ColorScheme.getColor(47));
        }

        list.addView(pack);
    }

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

    private class PackClickListener implements View.OnClickListener {

        private final String packName;

        PackClickListener(String packName) {
            this.packName = packName;
        }

        @Override
        public void onClick(View v) {
            selectedPack = packName;
            fillList();
        }
    }
}
