package ru.ivansuper.jasmin.jabber.bookmarks;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.forms.FormListMap;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.ui.Spinner;

public class BookmarksActivity extends Activity {

    public static JProfile PROFILE;
    private BookmarksAdapter mAdapter;
    @SuppressWarnings("FieldCanBeLocal")
    private Button mAddBtn;
    private ListView mList;
    private Dialog progress;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle bundle) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String wallpaper_type = sp.getString("ms_wallpaper_type", "0");
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
        if (PROFILE == null) {
            finish();
            return;
        }
        setVolumeControlStream(3);
        setContentView(R.layout.bookmarks);
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PROFILE.bookmarks.listener = null;
    }

    private void init() {
        progress = DialogBuilder.createProgress(this, Locale.getString("s_please_wait"), true);
        initViews();
        mAdapter = PROFILE.bookmarks.mAdapter;
        mList.setAdapter((ListAdapter) mAdapter);
        mList.setOnItemClickListener(new AnonymousClass1());
        PROFILE.bookmarks.listener = () -> progress.dismiss();
        PROFILE.bookmarks.performRequest();
        progress.show();
    }

    public class AnonymousClass1 implements AdapterView.OnItemClickListener {

        Dialog d;

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int item_idx, long arg3) {
            BookmarkItem item = mAdapter.getItem(item_idx);
            UAdapter adp = new UAdapter();
            adp.setMode(2);
            adp.setTextColor(-1);
            adp.setPadding(16);
            adp.setTextSize(16);
            if (item.type == 0) {
                adp.put(Locale.getString("s_join_conference"), 2);
                adp.put(Locale.getString("s_save_conference"), 3);
            } else {
                adp.put(Locale.getString("s_open"), 4);
            }
            adp.put(Locale.getString("s_change"), 0);
            adp.put(Locale.getString("s_bookmark_copy_address"), 5);
            adp.put(Locale.getString("s_do_delete"), 1);
            d = DialogBuilder.createWithNoHeader(BookmarksActivity.this, adp, 0, new C00201(adp, item));
            d.show();
        }

        class C00201 implements AdapterView.OnItemClickListener {
            Dialog d_;
            private int mode = 0;
            Dialog sure;
            private final /* synthetic */ UAdapter val$adp;
            private final /* synthetic */ BookmarkItem val$item;

            C00201(UAdapter uAdapter, BookmarkItem bookmarkItem) {
                val$adp = uAdapter;
                val$item = bookmarkItem;
            }

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                switch ((int) val$adp.getItemId(arg2)) {
                    case 0:
                        LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.bookmark_data, null);
                        ((TextView) lay.findViewById(R.id.l1)).setText(Locale.getString("s_bookmark_type"));
                        Spinner spinner = (Spinner) lay.findViewById(R.id.bookmark_type_list);
                        FormListMap list = new FormListMap(new String[]{Locale.getString("s_bookmark_type_conference"), Locale.getString("s_bookmark_type_url")}, new String[]{"0", "1"});
                        list.toggleSelection(val$item.type);
                        list.setSelectionMode(false);
                        spinner.setAdapter(list);
                        final EditText name = (EditText) lay.findViewById(R.id.bookmark_name);
                        final EditText data = (EditText) lay.findViewById(R.id.bookmark_data);
                        final EditText nick = (EditText) lay.findViewById(R.id.bookmark_nick);
                        final EditText pass = (EditText) lay.findViewById(R.id.bookmark_pass);
                        resources.attachEditText(name);
                        resources.attachEditText(data);
                        resources.attachEditText(nick);
                        resources.attachEditText(pass);
                        name.setHint(Locale.getString("s_bookmark_name"));
                        nick.setHint(Locale.getString("s_bookmark_nick"));
                        pass.setHint(Locale.getString("s_bookmark_pass"));
                        final CheckBox auto = (CheckBox) lay.findViewById(R.id.bookmark_autojoin);
                        auto.setText(Locale.getString("s_bookmark_autojoin"));
                        if (val$item.NAME != null) {
                            name.setText(val$item.NAME);
                        }
                        if (val$item.JID_OR_URL != null) {
                            data.setText(val$item.JID_OR_URL);
                        }
                        if (val$item.nick != null) {
                            nick.setText(val$item.nick);
                        }
                        if (val$item.password != null) {
                            pass.setText(val$item.password);
                        }
                        auto.setChecked(val$item.autojoin);
                        switch (val$item.type) {
                            case 0:
                                data.setHint(Locale.getString("s_bookmark_conf_jid"));
                                break;
                            case 1:
                                nick.setVisibility(View.GONE);
                                pass.setVisibility(View.GONE);
                                auto.setVisibility(View.GONE);
                                data.setHint(Locale.getString("s_bookmark_url"));
                                break;
                        }
                        spinner.listener = (selected_labels, selected_vals) -> {
                            if (selected_vals[0].equals("0")) {
                                nick.setVisibility(View.VISIBLE);
                                pass.setVisibility(View.VISIBLE);
                                auto.setVisibility(View.VISIBLE);
                                data.setHint(Locale.getString("s_bookmark_conf_jid"));
                                mode = 0;
                            } else if (selected_vals[0].equals("1")) {
                                nick.setVisibility(View.GONE);
                                pass.setVisibility(View.GONE);
                                auto.setVisibility(View.GONE);
                                data.setHint(Locale.getString("s_bookmark_url"));
                                mode = 1;
                            }
                        };
                        BookmarksActivity bookmarksActivity = BookmarksActivity.this;
                        String string = Locale.getString("s_change");
                        String string2 = Locale.getString("s_ok");
                        String string3 = Locale.getString("s_cancel");
                        final BookmarkItem bookmarkItem = val$item;
                        d_ = DialogBuilder.createYesNo(bookmarksActivity, lay, 0, string, string2, string3, v -> {
                            String n = name.getText().toString().trim();
                            String d = data.getText().toString().trim().toLowerCase();
                            if (n.length() != 0 && d.length() != 0) {
                                bookmarkItem.NAME = n;
                                bookmarkItem.JID_OR_URL = d;
                                bookmarkItem.autojoin = auto.isChecked();
                                bookmarkItem.type = mode;
                                if (mode == 0) {
                                    String nick_ = nick.getText().toString().trim();
                                    String pass_ = pass.getText().toString().trim();
                                    if (nick_.length() > 0) {
                                        bookmarkItem.nick = nick_;
                                    }
                                    if (pass_.length() > 0) {
                                        bookmarkItem.password = pass_;
                                    }
                                }
                                PROFILE.bookmarks.update();
                                d_.dismiss();
                                progress.show();
                            }
                        }, v -> d_.dismiss());
                        d_.show();
                        return;
                    case 1:
                        BookmarksActivity bookmarksActivity2 = BookmarksActivity.this;
                        String string4 = Locale.getString("s_do_delete");
                        String string5 = Locale.getString("s_are_you_sure");
                        String string6 = Locale.getString("s_yes");
                        String string7 = Locale.getString("s_no");
                        final BookmarkItem bookmarkItem2 = val$item;
                        sure = DialogBuilder.createYesNo(bookmarksActivity2, 0, string4, string5, string6, string7, v -> {
                            sure.dismiss();
                            PROFILE.bookmarks.remove(bookmarkItem2);
                            progress.show();
                        }, v -> sure.dismiss());
                        sure.show();
                        return;
                    case 2:
                        String n = "";
                        if (val$item.NAME != null) {
                            n = val$item.NAME;
                        }
                        String d = "";
                        if (val$item.JID_OR_URL != null) {
                            d = val$item.JID_OR_URL;
                        }
                        if (n.length() == 0 || d.length() == 0 || d.split("@").length != 2) {
                            Toast.makeText(BookmarksActivity.this, Locale.getString("s_conf_join_error"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String nick_ = PROFILE.ID;
                        if (val$item.nick != null && val$item.nick.length() > 0) {
                            nick_ = val$item.nick;
                        }
                        String pass_ = "";
                        if (val$item.password != null) {
                            pass_ = val$item.password;
                        }
                        PROFILE.joinConference(val$item.JID_OR_URL, nick_, pass_);
                        Toast.makeText(BookmarksActivity.this, Locale.getString("s_joining_to_conference"), Toast.LENGTH_SHORT).show();
                        return;
                    case 3:
                        String jid = "";
                        if (val$item.JID_OR_URL != null) {
                            jid = val$item.JID_OR_URL;
                        }
                        if (PROFILE.getConference(jid) != null) {
                            Toast.makeText(BookmarksActivity.this, Locale.getString("s_conference_already_exist"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String name_ = JProtocol.getNameFromFullID(jid);
                        if (val$item.NAME != null && val$item.NAME.length() > 0) {
                            name_ = val$item.NAME;
                        }
                        String nick_2 = PROFILE.ID;
                        if (val$item.nick != null && val$item.nick.length() > 0) {
                            nick_2 = val$item.nick;
                        }
                        String pass_2 = "";
                        if (val$item.password != null) {
                            pass_2 = val$item.password;
                        }
                        PROFILE.addConference(jid, name_, nick_2, pass_2, false);
                        Toast.makeText(BookmarksActivity.this, Locale.getString("s_saved"), Toast.LENGTH_SHORT).show();
                        return;
                    case 4:
                        String url = val$item.JID_OR_URL;
                        if (!url.startsWith("http://")) {
                            url = "http://" + url;
                        }
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent("android.intent.action.VIEW", uri);
                        intent.putExtra("com.android.browser.application_id", getPackageName());
                        startActivity(intent);
                        return;
                    case 5:
                        //noinspection deprecation
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        //noinspection deprecation
                        cm.setText(val$item.JID_OR_URL);
                        resources.service.showToast(Locale.getString("s_copied"), 0);
                        return;
                    default:
                }
            }
        }
    }

    private void initViews() {
        if (!sp.getBoolean("ms_use_shadow", true)) {
            ((LinearLayout) findViewById(R.id.bookmarks_back)).setBackgroundColor(0);
        }
        ((TextView) findViewById(R.id.l1)).setText(Locale.getString("s_bookmarks"));
        mList = (ListView) findViewById(R.id.bookmarks_list);
        mList.setDividerHeight(0);
        mList.setSelector(resources.getListSelector());
        mAddBtn = (Button) findViewById(R.id.bookmark_add_btn);
        resources.attachButtonStyle(mAddBtn);
        mAddBtn.setText(Locale.getString("s_do_add"));
        mAddBtn.setOnClickListener(new AnonymousClass3());
    }

    public class AnonymousClass3 implements View.OnClickListener {

        Dialog d;
        private int mode = 0;

        @Override
        public void onClick(View v) {
            LinearLayout lay = (LinearLayout) View.inflate(resources.ctx, R.layout.bookmark_data, null);
            ((TextView) lay.findViewById(R.id.l1)).setText(Locale.getString("s_bookmark_type"));
            Spinner spinner = (Spinner) lay.findViewById(R.id.bookmark_type_list);
            FormListMap list = new FormListMap(new String[]{Locale.getString("s_bookmark_type_conference"), Locale.getString("s_bookmark_type_url")}, new String[]{"0", "1"});
            list.toggleSelection(0);
            list.setSelectionMode(false);
            spinner.setAdapter(list);
            final EditText name = (EditText) lay.findViewById(R.id.bookmark_name);
            final EditText data = (EditText) lay.findViewById(R.id.bookmark_data);
            final EditText nick = (EditText) lay.findViewById(R.id.bookmark_nick);
            final EditText pass = (EditText) lay.findViewById(R.id.bookmark_pass);
            resources.attachEditText(name);
            resources.attachEditText(data);
            resources.attachEditText(nick);
            resources.attachEditText(pass);
            name.setHint(Locale.getString("s_bookmark_name"));
            data.setHint(Locale.getString("s_bookmark_conf_jid"));
            nick.setHint(Locale.getString("s_bookmark_nick"));
            pass.setHint(Locale.getString("s_bookmark_pass"));
            final CheckBox auto = (CheckBox) lay.findViewById(R.id.bookmark_autojoin);
            auto.setText(Locale.getString("s_bookmark_autojoin"));
            spinner.listener = (selected_labels, selected_vals) -> {
                if (selected_vals[0].equals("0")) {
                    nick.setVisibility(View.VISIBLE);
                    pass.setVisibility(View.VISIBLE);
                    auto.setVisibility(View.VISIBLE);
                    data.setHint(Locale.getString("s_bookmark_conf_jid"));
                    mode = 0;
                } else if (selected_vals[0].equals("1")) {
                    nick.setVisibility(View.GONE);
                    pass.setVisibility(View.GONE);
                    auto.setVisibility(View.GONE);
                    data.setHint(Locale.getString("s_bookmark_url"));
                    mode = 1;
                }
            };
            d = DialogBuilder.createYesNo(BookmarksActivity.this, lay, 0, Locale.getString("s_do_add"), Locale.getString("s_ok"), Locale.getString("s_cancel"), v2 -> {
                String n = name.getText().toString().trim();
                String d_ = data.getText().toString().trim().toLowerCase();
                if (n.length() != 0 && d_.length() != 0) {
                    BookmarkItem item = new BookmarkItem();
                    item.NAME = n;
                    item.JID_OR_URL = d_;
                    if (PROFILE.bookmarks.itIsExist(item)) {
                        Toast.makeText(BookmarksActivity.this, Locale.getString("s_bookmark_already_exist"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    item.autojoin = auto.isChecked();
                    if (mode == 0) {
                        String nick_ = nick.getText().toString().trim();
                        String pass_ = pass.getText().toString().trim();
                        if (nick_.length() > 0) {
                            item.nick = nick_;
                        }
                        if (pass_.length() > 0) {
                            item.password = pass_;
                        }
                    }
                    item.type = mode;
                    PROFILE.bookmarks.add(item);
                    d.dismiss();
                    progress.show();
                }
            }, v2 -> d.dismiss(), false);
            d.show();
        }
    }
}
