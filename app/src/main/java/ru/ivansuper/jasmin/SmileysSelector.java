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

public class SmileysSelector extends Activity {

    public static boolean VISIBLE;
    private GridView smileys;

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeTheme();
        initializeUI();
    }

    private void initializeTheme() {
        //noinspection deprecation
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String wallpaper_type = sp.getString("ms_wallpaper_type", "0");

        //noinspection DataFlowIssue
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

    @Override
    public void onResume() {
        super.onResume();
        VISIBLE = true;
        if (smileys != null) {
            ((smileys_adapter) smileys.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        VISIBLE = false;
    }
}