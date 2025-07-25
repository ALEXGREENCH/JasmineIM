package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Vector;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * Activity for displaying contact history.
 * <p>
 * This activity allows users to view the message history with a specific contact.
 * It supports different messaging protocols like ICQ, Jabber (JBR), and MMP.
 * Users can search within the history, copy messages, and use a multiquote feature
 * to copy multiple messages at once.
 * </p>
 * <p>
 * The activity initializes its UI, loads message history based on the provided
 * contact information (passed via Intent action), and displays it in a ListView.
 * It handles user interactions such as clicking on messages (for multiquoting)
 * and long-clicking (to copy a single message).
 * </p>
 * <p>
 * Key features:
 * <ul>
 *     <li>Displays message history for ICQ, Jabber, and MMP contacts.</li>
 *     <li>Allows searching within the message history.</li>
 *     <li>Supports copying individual messages to the clipboard.</li>
 *     <li>Provides a multiquote feature to select and copy multiple messages.</li>
 *     <li>Customizable wallpaper and theme settings.</li>
 * </ul>
 * </p>
 * <p>
 * The activity relies on a background service (jasminSvc) to fetch profile and
 * contact information and to load the message history.
 * </p>
 */
public class ContactHistoryActivity extends Activity {

    private static final int MODE_ICQ = 0;
    private static final int MODE_JBR = 1;
    private static final int MODE_MMP = 2;
    public static boolean multiquoting = false;

    private int MODE = MODE_ICQ;
    private HistoryAdapter adp;
    private ICQContact icontact;
    private JContact jcontact;
    private MMPContact mcontact;
    private ListView messagesList;
    private TextView nickname;
    private Dialog progress;
    private jasminSvc service;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //noinspection deprecation
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("ms_telegram_style", false)) {
            setTheme(R.style.TelegramTheme);
        } else {
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
        }
        super.onCreate(savedInstanceState);
        resources.applyFontScale(this);
        SystemBarUtils.setupTransparentBars(this);
        setVolumeControlStream(3);
        setContentView(R.layout.contact_history);
        initViews();
        service = resources.service;
        handleServiceConnected();
    }

    @Override
    public boolean onKeyDown(int code, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (code) {
                case KeyEvent.KEYCODE_MENU:
                    removeDialog(1);
                    showDialog(1);
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    break;
            }
        }
        return false;
    }

    private void initViews() {
        nickname = findViewById(R.id.history_wnd_nickname);
        resources.attachChatTopPanel(findViewById(R.id.history_header_layout));
        EditText search_input = findViewById(R.id.search_input);
        resources.attachEditText(search_input);
        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                adp.setFilter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        messagesList = findViewById(R.id.history_wnd_msglist);
        messagesList.setSelector(resources.getListSelector());
        //noinspection deprecation
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ms_use_shadow", true)) {
            messagesList.setBackgroundColor(0);
        }
        if (!PreferenceTable.chat_dividers) {
            messagesList.setDivider(null);
        }
        //noinspection deprecation
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ms_use_solid_wallpaper", false)) {
            resources.attachChatMessagesBack(messagesList);
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleServiceConnected() {
        Intent i = getIntent();
        String action = i.getAction();
        String prefix = action.substring(0, 3);
        String action2 = action.substring(3);
        if (prefix.equalsIgnoreCase("ICQ")) {
            MODE = MODE_ICQ;
            String[] parts = utilities.split(action2, "***$$$SEPARATOR$$$***");
            if (parts.length != 2) {
                finish();
                return;
            }
            ICQProfile profile = service.profiles.getProfileByUIN(parts[0]);
            if (profile == null) {
                finish();
                return;
            }
            icontact = profile.contactlist.getContactByUIN(parts[1]);
        } else if (prefix.equalsIgnoreCase("JBR")) {
            MODE = MODE_JBR;
            String[] parts2 = utilities.split(action2, "***$$$SEPARATOR$$$***");
            if (parts2.length != 2) {
                finish();
                return;
            }
            JProfile profile2 = service.profiles.getProfileByID(parts2[0]);
            if (profile2 == null) {
                finish();
                return;
            }
           jcontact = profile2.getContactByJID(parts2[1]);
        } else if (prefix.equalsIgnoreCase("MMP")) {
            MODE = MODE_MMP;
            String[] parts3 = utilities.split(action2, "***$$$SEPARATOR$$$***");
            if (parts3.length != 2) {
                finish();
                return;
            }
            MMPProfile profile3 = service.profiles.getProfileByEmail(parts3[0]);
            if (profile3 == null) {
                finish();
                return;
            }
            mcontact = profile3.getContactByID(parts3[1]);
        }
        String name = "";
        switch (MODE) {
            case MODE_ICQ:
                name = icontact.name;
                break;
            case MODE_JBR:
                name = jcontact.name;
                break;
            case MODE_MMP:
                name = mcontact.name;
                break;
        }
        nickname.setText(resources.getString("s_full_history") + "\n" + name);
        progress = DialogBuilder.createProgress(this, resources.getString("s_please_wait"), false);
        progress.show();
        Thread t = new Thread() {
            @Override
            public void run() {
                setName("History loader");
                setPriority(10);
                final Vector<HistoryItem> temp = new Vector<>();
                switch (MODE) {
                    case MODE_ICQ:
                        icontact.loadHistory(temp);
                        break;
                    case MODE_JBR:
                        jcontact.loadHistory(temp);
                        break;
                    case MODE_MMP:
                        try {
                            mcontact.loadHistory(temp);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        adp = new HistoryAdapter(ContactHistoryActivity.this, temp);
                        messagesList.setAdapter(adp);
                    }
                };
                service.runOnUi(r);
            }
        };
        t.start();
        messagesList.setOnItemClickListener(new messagelist_click_listener());
        messagesList.setOnItemLongClickListener(new historyListListener());
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class messagelist_click_listener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (ContactHistoryActivity.multiquoting) {
                HistoryAdapter adp = (HistoryAdapter) arg0.getAdapter();
                HistoryItem hst = adp.getItem(arg2);
                hst.selected = !hst.selected;
                adp.notifyDataSetChanged();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id != 1) {
            return null;
        }
        final UAdapter adp = new UAdapter();
        adp.setMode(2);
        adp.setTextSize(18);
        adp.setPadding(15);
        if (!multiquoting) {
            adp.put(resources.getString("s_turn_on_multiquote"), 1);
        }
        if (multiquoting) {
            adp.put(resources.getString("s_copy_selected"), 3);
            adp.put(resources.getString("s_turn_off_multiquote"), 2);
        }
        //noinspection UnnecessaryLocalVariable
        Dialog ad = DialogBuilder.createWithNoHeader(this, adp, 48, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int idx = (int) adp.getItemId(i);
                removeDialog(1);
                switch (idx) {
                    case 1:
                        ContactHistoryActivity.multiquoting = true;
                        messagesList.setItemsCanFocus(false);
                        adp.notifyDataSetChanged();
                        return;
                    case 2:
                        ContactHistoryActivity.multiquoting = false;
                        messagesList.setItemsCanFocus(true);
                        resetSelection();
                        adp.notifyDataSetChanged();
                        return;
                    case 3:
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setText(computeMultiQuote());
                        Toast msg = Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                        msg.setGravity(48, 0, 0);
                        msg.show();
                        return;
                    default:
                }
            }
        });
        return ad;
    }

    private void resetSelection() {
        for (int i = 0; i < adp.getCount(); i++) {
            adp.getItem(i).selected = false;
        }
    }

    private String computeMultiQuote() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < adp.getCount(); i++) {
            HistoryItem hst = adp.getItem(i);
            if (hst.selected) {
                String nick = null;
                if (hst.direction == 1) {
                    if (hst.contact != null) {
                        nick = hst.contact.name;
                    }
                    if (hst.jcontact != null) {
                        nick = hst.jcontact.name;
                    }
                    if (hst.mcontact != null) {
                        nick = hst.mcontact.name;
                    }
                } else {
                    if (hst.contact != null) {
                        nick = hst.contact.profile.nickname;
                    }
                    if (hst.jcontact != null) {
                        nick = hst.jcontact.profile.ID;
                    }
                    if (hst.mcontact != null) {
                        nick = hst.mcontact.profile.ID;
                    }
                }
                if (!hst.isFileMessage && !hst.isXtrazMessage && !hst.isAuthMessage) {
                    res.append(nick).append(" [").append(hst.formattedDate).append("]:\n").append(hst.message).append("\n");
                } else {
                    res.append("[").append(hst.formattedDate).append("]:\n").append(hst.message).append("\n");
                }
            }
        }
        return res.toString();
    }


    private class historyListListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            HistoryAdapter adp = (HistoryAdapter) arg0.getAdapter();
            HistoryItem item = adp.getItem(arg2);
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            //noinspection deprecation
            cm.setText(item.formattedDate + ":\n" + item.message);
            Toast msg = Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
            msg.setGravity(48, 0, 0);
            msg.show();
            return false;
        }
    }
}
