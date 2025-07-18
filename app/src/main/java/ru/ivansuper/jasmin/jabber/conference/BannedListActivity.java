package ru.ivansuper.jasmin.jabber.conference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

public class BannedListActivity extends Activity {

    public static boolean ACTIVE = false;
    private BannedAdapter mAdapter;
    @SuppressWarnings("FieldCanBeLocal")
    private ListView mList;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle bundle) {
        //noinspection deprecation
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String wallpaper_type = sp.getString("ms_wallpaper_type", "0");
        //noinspection DataFlowIssue
        switch (wallpaper_type) {
            case "0":
                setTheme(R.style.WallpaperNoTitleTheme);
                break;
            case "1":
                setTheme(R.style.BlackNoTitleTheme);
                getWindow().setBackgroundDrawable(resources.custom_wallpaper);
                break;
            case "2":
                setTheme(R.style.BlackNoTitleTheme);
                getWindow().setBackgroundDrawable(ColorScheme.getSolid(ColorScheme.getColor(13)));
                break;
        }
        super.onCreate(bundle);
        setVolumeControlStream(3);
        setContentView(R.layout.banned_list);
        SystemBarUtils.setupTransparentBars(this);
        init();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        ACTIVE = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        ACTIVE = false;
    }

    private void init() {
        if (!sp.getBoolean("ms_use_shadow", true)) {
            findViewById(R.id.banned_list_back).setBackgroundColor(0);
        }
        ((TextView) findViewById(R.id.l1)).setText(Locale.getString("s_banned_list"));
        mAdapter = JConference.conference.mBannedList;
        mList = findViewById(R.id.banned_list_list);
        mList.setSelector(new ColorDrawable(0));
        mList.setDivider(new ColorDrawable(ColorScheme.getColor(44)));
        mList.setDividerHeight(1);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        Button add = findViewById(R.id.banned_list_add);
        resources.attachButtonStyle(add);
        add.setText(Locale.getString("s_do_add"));
        //noinspection FieldCanBeLocal
        add.setOnClickListener(new View.OnClickListener() {
            private Dialog d = null;

            @Override
            public void onClick(View v) {
                final EditText input = new EditText(BannedListActivity.this);
                input.setSingleLine(false);
                input.setMinimumHeight(64);
                resources.attachEditText(input);
                d = DialogBuilder.createYesNo(BannedListActivity.this, input, 0, Locale.getString("s_do_add"), Locale.getString("s_do_add"), Locale.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String raw = input.getText().toString();
                        if (!raw.trim().isEmpty()) {
                            String[] items = raw.split("\n");
                            JConference.conference.banUsers(items);
                            d.dismiss();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                    }
                });
                d.show();
            }
        });
    }

    private void initAdapter() {
        mAdapter.clear();
        mAdapter.put(JConference.conference.mBannedList);
        mAdapter.notifyDataSetChanged();
    }
}
