package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import ru.ivansuper.jasmin.color_editor.ColorScheme;

public class SmileysManagerActivity extends Activity {
    
    @SuppressWarnings("FieldCanBeLocal")
    private Button apply;
    private LinearLayout list;
    private String selected_pack = "";
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        selected_pack = sp.getString("current_smileys_pack", "$*INTERNAL*$");
        setVolumeControlStream(3);
        setContentView(R.layout.smileys_manager);
        initViews();
        fillList();
    }

    @SuppressLint("ApplySharedPref")
    private void initViews() {
        ((TextView) findViewById(R.id.l1)).setText(resources.getString("s_smileys_manager_title_1"));
        ((TextView) findViewById(R.id.l2)).setText(resources.getString("s_smileys_manager_title_2"));
        list = (LinearLayout) findViewById(R.id.smileys_manager_pack_list);
        apply = (Button) findViewById(R.id.smileys_manager_apply);
        apply.setText(resources.getString("s_smileys_manager_apply"));
        apply.setOnClickListener(v -> {
            sp.edit().putString("current_smileys_pack", selected_pack).commit();
            SmileysManager.loadPack();
            Toast.makeText(this, resources.getString("s_selection_saved"), Toast.LENGTH_SHORT).show();
        });
    }

    public void fillList() {
        list.removeAllViews();
        TextView pack = new TextView(this);
        pack.setTextColor(-1);
        pack.setShadowLayer(1.0f, 0.0f, 0.0f, -16777216);
        pack.setTextSize(14.0f);
        pack.setPadding(15, 15, 15, 15);
        pack.setGravity(17);
        pack.setText(resources.getString("s_internal"));
        if (selected_pack.equals("$*INTERNAL*$")) {
            pack.setBackgroundColor(ColorScheme.getColor(47));
        }
        pack.setOnClickListener(new pack_listener("$*INTERNAL*$"));
        list.addView(pack);
        if (resources.sd_mounted()) {
            File smileys_dir = new File(resources.JASMINE_SD_PATH + "Smileys");
            File[] packs = smileys_dir.listFiles();
            assert packs != null;
            for (File file : packs) {
                if (file.isDirectory()) {
                    TextView pack2 = new TextView(this);
                    pack2.setTextColor(-1);
                    pack2.setShadowLayer(1.0f, 0.0f, 0.0f, -16777216);
                    pack2.setTextSize(14.0f);
                    pack2.setPadding(15, 15, 15, 15);
                    pack2.setGravity(17);
                    pack2.setOnClickListener(new pack_listener(file.getName()));
                    pack2.setText(file.getName());
                    if (selected_pack.equals(file.getName())) {
                        pack2.setBackgroundColor(ColorScheme.getColor(47));
                    }
                    list.addView(pack2);
                }
            }
        }
    }

    public class pack_listener implements View.OnClickListener {

        private String pack;

        public pack_listener(String pack_name) {
            //noinspection UnusedAssignment
            pack = "";
            pack = pack_name;
        }

        @Override
        public void onClick(View v) {
            selected_pack = pack;
            fillList();
        }
    }
}
