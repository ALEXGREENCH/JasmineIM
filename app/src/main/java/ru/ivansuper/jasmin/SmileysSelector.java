package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.GridView;
import android.widget.ListAdapter;

import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.chats.Chat;
import ru.ivansuper.jasmin.color_editor.ColorScheme;

public class SmileysSelector extends Activity {
    
    public static boolean VISIBLE;
    private GridView smileys;

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(16973830);
        Window wnd = getWindow();
        String wallpaper_type = sp.getString("ms_wallpaper_type", "0");
        switch (wallpaper_type) {
            case "0":
                setTheme(R.style.WallpaperNoTitleTheme);
                break;
            case "1":
                setTheme(R.style.BlackNoTitleTheme);
                getWindow().setBackgroundDrawable(resources.custom_wallpaper);
                resources.attachChatMessagesBack(wnd);
                break;
            case "2":
                setTheme(R.style.BlackNoTitleTheme);
                getWindow().setBackgroundDrawable(ColorScheme.getSolid(ColorScheme.getColor(13)));
                resources.attachChatMessagesBack(wnd);
                break;
        }
        setVolumeControlStream(3);
        setContentView(R.layout.smileys_selector);
        smileys = (GridView) findViewById(R.id.smileys_selector_field);
        smileys.setSelector(resources.getListSelector());
        if (!sp.getBoolean("ms_use_shadow", true)) {
            smileys.setBackgroundColor(0);
        }
        smileys.setNumColumns(PreferenceTable.smileysSelectorColumns);
        final smileys_adapter adapter = new smileys_adapter();
        smileys.setAdapter((ListAdapter) adapter);
        smileys.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            String tag = adapter.getTag(arg2);
            Intent i = new Intent();
            i.setAction(" " + tag + " ");
            Chat.received_smile_tag = i.getAction();
            setResult(-1, i);
            finish();
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
