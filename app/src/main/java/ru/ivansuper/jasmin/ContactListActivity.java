package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import ru.ivansuper.jasmin.Clients.IcqCapsBase;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPGroup;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnList;
import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.base.destroyer;
import ru.ivansuper.jasmin.chats.ChatInitCallback;
import ru.ivansuper.jasmin.chats.ICQChatActivity;
import ru.ivansuper.jasmin.chats.JChatActivity;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.chats.MMPChatActivity;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.icq.Callback;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQGroup;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.icq.InfoContainer;
import ru.ivansuper.jasmin.icq.qip_statuses;
import ru.ivansuper.jasmin.icq.ssi_item;
import ru.ivansuper.jasmin.icq.xstatus;
import ru.ivansuper.jasmin.jabber.AbstractForm;
import ru.ivansuper.jasmin.jabber.Clients;
import ru.ivansuper.jasmin.jabber.GMail.GMailActivity;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JGroup;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.VCard;
import ru.ivansuper.jasmin.jabber.XMLConsole.XMLConsoleActivity;
import ru.ivansuper.jasmin.jabber.bookmarks.BookmarkItem;
import ru.ivansuper.jasmin.jabber.bookmarks.BookmarkList;
import ru.ivansuper.jasmin.jabber.bookmarks.BookmarksActivity;
import ru.ivansuper.jasmin.jabber.commands.Command;
import ru.ivansuper.jasmin.jabber.commands.CommandItem;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.conference.ConferenceItem;
import ru.ivansuper.jasmin.jabber.conference.RoomListCallback;
import ru.ivansuper.jasmin.jabber.conference.RoomsPreviewAdapter;
import ru.ivansuper.jasmin.jabber.disco.DiscoActivity;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.popup.PopupBuilder;
import ru.ivansuper.jasmin.popup.QuickAction;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.security.PasswordManager;
import ru.ivansuper.jasmin.slide_tools.SlideSwitcher;
import ru.ivansuper.jasmin.ui.ExFragmentManager;
import ru.ivansuper.jasmin.ui.JFragment;
import ru.ivansuper.jasmin.ui.JFragmentActivity;
import ru.ivansuper.jasmin.ui.Resizer;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

public class ContactListActivity extends JFragmentActivity implements Handler.Callback, SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressLint("StaticFieldLeak")
    private static LinearLayout BOTTOM_PANEL = null;
    @SuppressWarnings("unused")
    public static final int CHECK_CONFERENCES = 4;
    public static final int HIDE_PROGRESS_DIALOG = 33;
    private static final int ICQ_AVATAR_REQUEST = 4084;
    private static final int JABBER_AVATAR_REQUEST = 4085;
    public static int LastContextAction = 0;
    @SuppressWarnings("unused")
    public static final int REBUILD_CONTACTLIST = 3;
    @SuppressWarnings("unused")
    public static final int REFRESH_CONTACTLIST = 2;
    @SuppressWarnings("unused")
    public static final int REFRESH_PROFILE_DATA = 1;
    public static final int RETURN_TO_CONTACTS = 270;
    @SuppressWarnings("unused")
    public static final int SHOW_CONTACT_INFO_DIALOG = 16;
    public static final int SHOW_JABBER_CMD_FORM = 257;
    public static final int SHOW_JABBER_FORM = 256;
    public static final int SHOW_NOTIFY = 31;
    public static final int SHOW_PROGRESS_DIALOG = 32;
    public static final int SHOW_VCARD = 65;
    public static final int SHOW_VCARD_EDITOR = 66;
    public static final int UPDATE_BLINK_STATE = 128;
    @SuppressLint("StaticFieldLeak")
    private static ContactsAdapter chats_listAdp;
    public static String client_info;
    @SuppressLint("StaticFieldLeak")
    private static ContactsAdapter confs_listAdp;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout connectionStatusPanel;
    private static ConferenceItem contextConference;
    private static ICQContact contextContact;
    public static ICQGroup contextGroup;
    public static JContact contextJContact;
    private static JProfile contextJProfile;
    public static MMPContact contextMMPContact;
    private static MMPProfile contextMrimProfile;
    private static ICQProfile contextProfile;
    private static BufferedDialog dialog_for_display;
    public static Vector<BufferedDialog> dialogs;
    @SuppressWarnings("FieldCanBeLocal")
    private static Handler hdl;
    @SuppressLint("StaticFieldLeak")
    private static QuickAction last_quick_action;
    private static Dialog last_shown_notify_dialog;
    @SuppressLint("StaticFieldLeak")
    private static ContactsAdapter listAdp;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout profilesPanel;
    private static jasminSvc service;
    private static SharedPreferences sp;
    public static Dialog ssi_progress;
    private static SlideSwitcher switcher;
    private static ICQContact tempContactForAddingDialog;
    private static InfoContainer tempContactForDisplayInfo;
    @SuppressLint("StaticFieldLeak")
    private static ImageView toggle_offline;
    @SuppressLint("StaticFieldLeak")
    private static ImageView toggle_sound;
    @SuppressLint("StaticFieldLeak")
    private static ImageView toggle_vibro;
    @SuppressWarnings("unused")
    private static String transport_for_registration;
    private static VCard vcard_to_display;
    @SuppressLint("StaticFieldLeak")
    private static EditText xdesc;
    @SuppressLint("StaticFieldLeak")
    private static EditText xtitle;
    @SuppressWarnings("FieldCanBeLocal")
    private ConfigListenerView CONFIG_LISTENER;
    private MultiColumnList chats_contactlist;
    private MultiColumnList confs_contactlist;
    private MultiColumnList contactlist;
    private LinearLayout search_panel;
    private LinearLayout search_panel_slot;
    private static int selectedX = -1;
    public static boolean exiting = false;
    private static boolean BOTTOM_PANEL_VISIBLED = true;
    public static boolean HIDDEN = false;
    public static boolean CURRENT_IS_CONTACTS = true;
    private boolean SEARCH_PANEL_VISIBLE = false;
    private boolean ANY_CHAT_ACTIVE = false;
    private boolean IT_IS_PORTRAIT = false;

    private void checkForBufferedDialogs() {
        if (!dialogs.isEmpty()) {
            dialog_for_display = dialogs.remove(0);
            showDialogA(23);
        }
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.IT_IS_PORTRAIT = true;
        super.onCreate(savedInstanceState);
        resources.applyFontScale(this);
        Intent i = getIntent();
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
        if (resources.IT_IS_TABLET) {
            getWindow().addFlags(16777216);
        }
        setVolumeControlStream(3);
        setContentView(R.layout.contactlist);
        SystemBarUtils.setupTransparentBars(this);
        service = resources.service;
        if (service != null && service.profiles == null) {
            service.profiles = new ProfilesManager(service);
            EventTranslator.sendProfilesList();
        }
        dialogs = new Vector<>();
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            IT_IS_PORTRAIT = false;
        }
        initViews();
        updateUI();
        sp.registerOnSharedPreferenceChangeListener(this);
        if (i.getBooleanExtra("no_profiles", false)) {
            showDialogA(17);
        }
        File marker = new File(resources.dataPath + "ForceClosed.marker");
        if (marker.exists()) {
            if (resources.sd_mounted()) {
                //noinspection ResultOfMethodCallIgnored
                marker.delete();
                copyDumpsToSD();
                showDialogA(25);
            } else {
                return;
            }
        }
        //noinspection DataFlowIssue
        if (!sp.getString("chlver", "0.0.0").equals(resources.VERSION)) {
            showDialogA(26);
            sp.edit().putString("chlver", resources.VERSION).commit();
        }
        handleServiceConnected();
        if (resources.DONATE_INSTALLED) {
            ADB.setActivated(8);
        }
        boolean pwd = Manager.getBoolean("ms_use_pass_security");
        if (pwd) {
            checkPwd();
        }

        View menuToggle = findViewById(R.id.toggle_menu);
        if (utilities.hasHardwareMenuKey(this)) {
            menuToggle.setVisibility(View.GONE);
        }
        menuToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleMenuKey();
            }
        });
    }

    private void checkPwd() {
        if (!PasswordManager.TYPED) {
            final WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 0.0f;
            getWindow().setAttributes(lp);
            final EditText pass = new EditText(this);
            pass.setInputType(129);
            resources.attachEditText(pass);
            Dialog d = DialogBuilder.createOk(
                    this,
                    pass,
                    Locale.getString("s_ms_use_pass_security_hint"),
                    Locale.getString("s_ok"),
                    0,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    },
                    true
            );
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (PasswordManager.verifyPassword(pass.getText().toString())) {
                        lp.alpha = 1.0f;
                        getWindow().setAttributes(lp);
                        PasswordManager.TYPED = true;
                        return;
                    }
                    exiting = true;
                    finish();
                }
            });
            d.show();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action != null) {
            proceedIntent(action);
        }
    }

    private void proceedIntent(String action) {
        if (action.startsWith("ICQ")) {
            startFragmentChatICQ(action.substring(3));
        }
        if (action.startsWith("JBR")) {
            startFragmentChatJabber(action.substring(3));
        }
        if (action.startsWith("JCF")) {
            startFragmentChatJConference(action.substring(3));
        }
        if (action.startsWith("MMP")) {
            startFragmentChatMMP(action.substring(3));
        }
        if (action.startsWith("%GMAIL%")) {
            String raw = action.substring(7);
            JProfile profile = service.profiles.getProfileByID(raw);
            if (profile != null && profile.connected) {
                GMailActivity.profile = profile;
                Intent gmail = new Intent(this, GMailActivity.class);
                startActivity(gmail);
            }
        }
    }

    private void copyDumpsToSD() {
        //noinspection deprecation
        new CopyFilesTask().execute();
    }

    /** @noinspection deprecation*/
    @SuppressLint("StaticFieldLeak")
    private static class CopyFilesTask extends AsyncTask<Void, Void, Void> {

        /** @noinspection deprecation*/
        @Override
        protected Void doInBackground(Void... params) {
            byte[] buffer = new byte[16384];
            File data_dir = new File(resources.dataPath);
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".st");
                }
            };
            File[] dumps = data_dir.listFiles(filter);
            assert dumps != null;
            for (File dump : dumps) {
                try {
                    File out = new File(resources.JASMINE_SD_PATH + "/" + dump.getName());
                    FileOutputStream fos = new FileOutputStream(out);
                    FileInputStream fis = new FileInputStream(dump);
                    while (fis.available() > 0) {
                        //noinspection SpellCheckingInspection
                        int readed = fis.read(buffer, 0, 16384);
                        fos.write(buffer, 0, readed);
                    }
                    fos.close();
                    fis.close();
                    //noinspection ResultOfMethodCallIgnored
                    dump.delete();
                } catch (Exception e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.confs_contactlist != null) {
            this.confs_contactlist.freezeInvalidating(true);
        }
        if (this.chats_contactlist != null) {
            this.chats_contactlist.freezeInvalidating(true);
        }
        if (this.contactlist != null) {
            this.contactlist.freezeInvalidating(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        localOnResume();
        Intent i = getIntent();
        String action = i.getAction();
        if (action != null) {
            proceedIntent(action);
            i.setAction(null);
            setIntent(i);
        }
    }

    private void localOnResume() {
        boolean z = true;
        resources.ctx = this;
        service = resources.service;
        if (this.confs_contactlist != null) {
            this.confs_contactlist.freezeInvalidating(false);
        }
        if (this.chats_contactlist != null) {
            this.chats_contactlist.freezeInvalidating(false);
        }
        if (this.contactlist != null) {
            this.contactlist.freezeInvalidating(false);
        }
        if (listAdp != null) {
            refreshContactlist();
            if (listAdp.getCount() == 0 && service != null && service.profiles != null) {
                createContactlistFromProfiles();
            }
        }
        if (switcher != null) {
            switcher.updateConfig();
            switcher.updateLabel(0, resources.getString("s_cl_panel_chats"));
            switcher.updateLabel(1, resources.getString("s_cl_panel_contacts"));
            switcher.updateLabel(2, resources.getString("s_cl_panel_confs"));
            if (PreferenceTable.ms_cl_transition_effect < 0) {
                switcher.setRandomizedAnimation(true);
            } else {
                switcher.setRandomizedAnimation(false);
                switcher.setAnimationType(PreferenceTable.ms_cl_transition_effect);
            }
            if (!PreferenceTable.ms_two_screens_mode) {
                switcher.post(new Runnable() {
                    @Override
                    public void run() {
                        switcher.scrollTo(1);
                    }
                });
            }
            SlideSwitcher slideSwitcher = switcher;
            if (PreferenceTable.ms_two_screens_mode) {
                z = false;
            }
            slideSwitcher.setLock(z);
            switcher.togglePanel(PreferenceTable.ms_two_screens_mode);
            updateBlinkState();
        }
        int columns = PreferenceTable.simple_cl_columns;
        if (findViewById(R.id.chat_fragment).getVisibility() == View.VISIBLE && resources.IT_IS_TABLET && columns > 2) {
            columns = 2;
        }
        if (this.contactlist.getColumnsNumber() != columns) {
            this.contactlist.setColumnsNumber(columns);
        }
        if (this.chats_contactlist.getColumnsNumber() != columns) {
            this.chats_contactlist.setColumnsNumber(columns);
        }
        if (this.confs_contactlist != null && this.confs_contactlist.getColumnsNumber() != columns) {
            this.confs_contactlist.setColumnsNumber(columns);
        }
        if (service != null) {
            initToolsPanel();
            service.cancelMultiloginNotify();
            service.handleContactlistNeedRemake();
            // ensure conference tab state is up to date when returning from
            // ProfilesActivity or other screens
            checkConferences();
        }
        HIDDEN = false;
        checkForBufferedDialogs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(getClass().getSimpleName(), "Destroyed");
        HIDDEN = true;
        if (exiting) {
            Intent svc = new Intent();
            svc.setClass(getApplicationContext(), jasminSvc.class);
            service.performDestroying();
            stopService(svc);
            destroyer d = new destroyer();
            d.start();
        }
        if (switcher != null) {
            switcher.clearupCaches();
        }
        System.gc();
    }

    protected void finalize() throws Throwable {
        Log.e(getClass().getSimpleName(), "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
        super.finalize();
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    handleBackKey();
                    return true;
                case KeyEvent.KEYCODE_MENU:
                    handleMenuKey();
                    return true;
                case KeyEvent.KEYCODE_SEARCH:
                    handleSearchKey();
                    return true;
                case KeyEvent.KEYCODE_WINDOW:
                    handleWindowKey();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleBackKey() {
        if (CURRENT_IS_CONTACTS) {
            if (ANY_CHAT_ACTIVE) {
                service.handleContactlistReturnToContacts();
            } else if (SEARCH_PANEL_VISIBLE) {
                hideSearchPanel();
            } else {
                finish();
            }
        } else {
            CURRENT_IS_CONTACTS = true;
            updateUI();
        }
    }

    private void handleMenuKey() {
        if (!resources.IT_IS_TABLET && !CURRENT_IS_CONTACTS) {
            service.showChatMenu();
        } else {
            removeDialog(2);
            showDialogA(2);
        }
    }

    @SuppressLint("ResourceType")
    private void handleSearchKey() {
        if (CURRENT_IS_CONTACTS) {
            if (BOTTOM_PANEL_VISIBLED) {
                BOTTOM_PANEL_VISIBLED = false;
                TranslateAnimation t = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.0f, 1, 1.0f);
                t.setInterpolator(resources.ctx, 17432582);
                t.setDuration(250L);
                t.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ContactListActivity.BOTTOM_PANEL.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                });
                BOTTOM_PANEL.startAnimation(t);
            } else {
                BOTTOM_PANEL_VISIBLED = true;
                BOTTOM_PANEL.setVisibility(View.VISIBLE);
                TranslateAnimation t2 = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 1.0f, 1, 0.0f);
                t2.setInterpolator(resources.ctx, 17432582);
                t2.setDuration(250L);
                BOTTOM_PANEL.startAnimation(t2);
            }
        }

    }

    private void handleWindowKey() {
        if (CURRENT_IS_CONTACTS) {
            if (BOTTOM_PANEL_VISIBLED) {
                hideBottomPanel();
            } else {
                showBottomPanel();
            }
        }
    }

    @SuppressLint("ResourceType")
    private void hideBottomPanel() {
        BOTTOM_PANEL_VISIBLED = false;
        TranslateAnimation t = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.0f, 1, 1.0f);
        t.setInterpolator(resources.ctx, 17432582);
        t.setDuration(250L);
        t.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                BOTTOM_PANEL.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        BOTTOM_PANEL.startAnimation(t);
    }

    @SuppressLint("ResourceType")
    private void showBottomPanel() {
        BOTTOM_PANEL_VISIBLED = true;
        BOTTOM_PANEL.setVisibility(View.VISIBLE);
        TranslateAnimation t2 = new TranslateAnimation(
                1,
                0.0f,
                1,
                0.0f,
                1,
                1.0f,
                1,
                0.0f
        );
        t2.setInterpolator(resources.ctx, 17432582);
        t2.setDuration(250L);
        BOTTOM_PANEL.startAnimation(t2);
    }

    /** @noinspection NullableProblems*/
    @Override
    public void onConfigurationChanged(Configuration configuration) {
        onConfigurationChangedLocal(configuration, 0);
        super.onConfigurationChanged(configuration);
    }

    public void onConfigurationChangedLocal(final Configuration configuration, final int diff) {
        this.IT_IS_PORTRAIT = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        service.runOnUi(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        }, 100L);
        if (last_quick_action != null) {
            last_quick_action.dismiss();
        }
        ExFragmentManager.executeEvent(new ExFragmentManager.ExRunnable() {
            @Override
            public void run() {
                this.fragment.onConfigurationChanged(configuration, diff);
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        ExFragmentManager.executeEvent(new ExFragmentManager.ExRunnable() {
            @Override
            public void run() {
                this.fragment.onActivityResult(requestCode, resultCode, data);
            }
        });
        if (requestCode == ICQ_AVATAR_REQUEST && resultCode != 0) {
            String file_name = data.getAction();
            //noinspection DataFlowIssue
            if (file_name.toLowerCase().endsWith(".gif")) {
                File file = new File(data.getAction());
                uploadAvatar(file);
            } else if (file_name.toLowerCase().endsWith(".jpg")) {
                File file2 = new File(data.getAction());
                uploadAvatar(file2);
            } else if (file_name.toLowerCase().endsWith(".jpeg")) {
                File file3 = new File(data.getAction());
                uploadAvatar(file3);
            } else if (file_name.toLowerCase().endsWith(".bmp")) {
                File file4 = new File(data.getAction());
                uploadAvatar(file4);
            } else {
                service.showToast(Locale.getString("s_change_avatar_invalid_image"), 1);
            }
        }
        if (requestCode == JABBER_AVATAR_REQUEST && resultCode != 0) {
            String file_name2 = data.getAction();
            //noinspection DataFlowIssue
            if (file_name2.toLowerCase().endsWith(".gif")) {
                File file5 = new File(data.getAction());
                uploadJabberAvatar(file5);
            } else if (file_name2.toLowerCase().endsWith(".jpg")) {
                File file6 = new File(data.getAction());
                uploadJabberAvatar(file6);
            } else if (file_name2.toLowerCase().endsWith(".jpeg")) {
                File file7 = new File(data.getAction());
                uploadJabberAvatar(file7);
            } else if (file_name2.toLowerCase().endsWith(".bmp")) {
                File file8 = new File(data.getAction());
                uploadJabberAvatar(file8);
            } else {
                service.showToast(Locale.getString("s_change_avatar_invalid_image"), 1);
            }
        }
    }

    private void uploadAvatar(File file) {
        ////if (file.length() <= 11264) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        ////if (opts.outWidth <= 64 && opts.outHeight <= 64) {
        // TODO: ?
        //if (0 != 0) {
        //    Toast.makeText(this, resources.getString("s_avatar_size_too_big"), Toast.LENGTH_LONG).show();
        //} else {
        contextProfile.doChangeAvatar(file);
        //}
        ////return;
        ////}
        ////return;
        ////}
        ////Toast.makeText(this, resources.getString("s_avatar_file_too_big"), Toast.LENGTH_LONG).show();
    }


    private void uploadJabberAvatar(File file) {
        contextJProfile.updateAvatar(file);
    }

    public void showDialogA(int id) {
        if (!HIDDEN) {
            showDialog(id);
        }
    }

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog ad = null;
        switch (id) {
            case 0:
                final UAdapter adp_0 = new UAdapter();
                adp_0.setMode(2);
                adp_0.setTextSize(18);
                adp_0.setPadding(15);
                adp_0.put(resources.getString("s_add_contact"), 0);
                adp_0.put(resources.getString("s_add_group"), 6);
                adp_0.put(resources.getString("s_do_change_avatar"), 1);
                adp_0.put(resources.getString("s_icq_search_by_uin"), 2);
                adp_0.put(resources.getString("s_icq_global_search"), 3);
                adp_0.put(resources.getString("s_do_refresh_avatars"), 4);
                adp_0.put(resources.getString("s_do_change_own_info"), 5);
                ad = DialogBuilder.createWithNoHeader(this, adp_0, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                        removeDialog(0);
                        int id2 = (int) adp_0.getItemId(index);
                        switch (id2) {
                            case 0:
                                removeDialog(6);
                                showDialogA(6);
                                return;
                            case 1:
                                Intent i = new Intent();
                                i.setClass(ContactListActivity.this, FileBrowserActivity.class);
                                startActivityForResult(i, ICQ_AVATAR_REQUEST);
                                contextProfile.checkRosterRecord();
                                return;
                            case 2:
                                removeDialog(24);
                                showDialogA(24);
                                return;
                            case 3:
                                Intent search = new Intent();
                                search.setClass(ContactListActivity.this, SearchActivity.class);
                                search.setAction(contextProfile.ID);
                                startActivity(search);
                                return;
                            case 4:
                                Thread t = new Thread() {
                                    @Override
                                    public void run() {
                                        setPriority(1);
                                        Vector<ICQContact> list = contextProfile.contactlist.getContacts();
                                        for (ICQContact contact : list) {
                                            contact.getAvatar(contact, service);
                                            try {
                                                Thread.sleep(1000L);
                                            } catch (InterruptedException ignored) {
                                            }
                                        }
                                    }
                                };
                                t.start();
                                return;
                            case 5:
                                removeDialog(27);
                                showDialogA(27);
                                return;
                            case 6:
                                removeDialog(31);
                                showDialogA(31);
                                return;
                            default:
                        }
                    }
                });
                break;
            case 2:
                final UAdapter adp_11 = new UAdapter();
                adp_11.setMode(2);
                adp_11.setPadding(15);
                adp_11.setTextSize(18);
                adp_11.put(resources.getString("s_log_list"), 1);
                if (CURRENT_IS_CONTACTS) {
                    adp_11.put(resources.getString("s_search"), 7);
                }
                adp_11.put(resources.getString("s_settings"), 2);
                ///  TODO: БЕЗ ДОНАТОВ :)
                ///if (!resources.DONATE_INSTALLED) {
                ///    adp_11.put(resources.getString("s_donate"), 5);
                ///}
                adp_11.put(resources.getString("s_about"), 3);
                if (ADB.getActivatedCount() > 0) {
                    adp_11.put(Locale.getString("s_achs"), 8);
                }
                adp_11.put(resources.getString("s_exit"), 4);
                ad = DialogBuilder.create(this, resources.getString("s_main_menu"), adp_11, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                        removeDialog(2);
                        int id2 = (int) adp_11.getItemId(index);
                        switch (id2) {
                            case 1:
                                removeDialog(4);
                                showDialogA(4);
                                return;
                            case 2:
                                Intent sts = new Intent();
                                sts.setClass(ContactListActivity.this, SettingsActivity.class);
                                startActivity(sts);
                                return;
                            case 3:
                                removeDialog(3);
                                showDialogA(3);
                                return;
                            case 4:
                                removeDialog(16);
                                showDialogA(16);
                                return;
                            case 5:
                                removeDialog(18);
                                showDialogA(18);
                                return;
                            case 6:
                                if (!resources.sd_mounted()) {
                                    Toast.makeText(ContactListActivity.this, resources.getString("s_plug_in_a_memory_card"), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                removeDialog(19);
                                showDialogA(19);
                                return;
                            case 7:
                                if (!SEARCH_PANEL_VISIBLE) {
                                    SEARCH_PANEL_VISIBLE = true;
                                    search_panel.setVisibility(View.VISIBLE);
                                    EditText search_panel_input = new EditText(ContactListActivity.this);
                                    resources.attachEditText(search_panel_input);
                                    search_panel_slot.removeAllViews();
                                    search_panel_slot.addView(search_panel_input);
                                    search_panel_input.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            if (listAdp != null) {
                                                listAdp.setFilter(s.toString());
                                            }
                                        }

                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        }
                                    });
                                    return;
                                }
                                return;
                            case 8:
                                UAdapter achs_list = new UAdapter();
                                achs_list.setMode(2);
                                achs_list.setPadding(10);
                                achs_list.setTextSize(16);
                                Vector<ADB.Item> achs = ADB.getAll();
                                int i = 0;
                                for (ADB.Item ach : achs) {
                                    achs_list.put(ach.desc, i);
                                    if (ach.activated) {
                                        achs_list.toggleSelection(achs_list.getLastIndex());
                                    }
                                    i++;
                                }
                                Dialog achs_ = DialogBuilder.create(
                                        ContactListActivity.this,
                                        Locale.getString("s_achs"),
                                        achs_list,
                                        new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                            }
                                        },
                                        false
                                );
                                achs_.show();
                                return;
                            default:
                        }
                    }
                });
                break;
            case 3:
                LinearLayout about = (LinearLayout) View.inflate(this, R.layout.about, null);
                ((TextView) about.findViewById(R.id.about1)).setText(resources.getString("s_about_1_new"));
                ((TextView) about.findViewById(R.id.about2)).setText(resources.getString("s_about_2_new"));
                ((TextView) about.findViewById(R.id.about3)).setText(resources.getString("s_about_3_new"));
                ((TextView) about.findViewById(R.id.about4)).setText(resources.getString("s_about_4_new"));
                ((TextView) about.findViewById(R.id.about5)).setText(resources.getString("s_about_5_new"));
                ((TextView) about.findViewById(R.id.about6)).setText(resources.getString("s_about_6_new"));
                ((TextView) about.findViewById(R.id.about7)).setText(resources.getString("s_about_7_new"));
                TextView version = about.findViewById(R.id.version);
                version.setText(resources.VERSION);
                ad = DialogBuilder.create(this, resources.getString("s_about"), about, 48);
                break;
            case 4:
                ad = DialogBuilder.createOk(
                        this,
                        service.logAdapter,
                        resources.getString("s_log_list"),
                        resources.getString("s_clear"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                service.logAdapter.clear();
                                removeDialog(4);
                            }
                        }, new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String item = service.logAdapter.getItem(i);
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(item);
                                Toast.makeText(ContactListActivity.this, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        });
                break;
            case 5:
                ad = DialogBuilder.createYesNo(resources.ctx, 0, resources.getString("s_information"), client_info, Locale.getString("s_copy"), Locale.getString("s_close"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        service.showToast(Locale.getString("s_copied"), 0);
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setText(client_info);
                        removeDialog(5);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(5);
                    }
                });
                break;
            case 6:
                UAdapter adp = new UAdapter();
                adp.setMode(2);
                adp.setTextSize(18);
                adp.setTextColor(-7829368);
                adp.setPadding(15);
                final Vector<ICQGroup> groups = contextProfile.contactlist.getGroups(false);
                for (ICQGroup group : groups) {
                    adp.put(group.name, group.id);
                }
                @SuppressLint("InflateParams") final LinearLayout lay = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.add_contact_dialog, null);
                ((TextView) lay.findViewById(R.id.l1)).setText(resources.getString("s_contact_id"));
                ((TextView) lay.findViewById(R.id.l2)).setText(resources.getString("s_contact_name"));
                ((TextView) lay.findViewById(R.id.l3)).setText(resources.getString("s_contact_group"));
                @SuppressLint("CutPasteId")
                EditText uin = lay.findViewById(R.id.add_contact_uin);
                resources.attachEditText(uin);
                @SuppressLint("CutPasteId")
                EditText name = lay.findViewById(R.id.add_contact_name);
                resources.attachEditText(name);
                if (tempContactForAddingDialog != null) {
                    uin.setText(tempContactForAddingDialog.ID);
                    name.setText(tempContactForAddingDialog.name);
                    name.requestFocus();
                }
                @SuppressLint("CutPasteId")
                Spinner spn = lay.findViewById(R.id.add_contact_groups);
                spn.setAdapter(adp);
                tempContactForAddingDialog = null;
                ad = DialogBuilder.createYesNo(this, lay, 48, resources.getString("s_add_contact"), resources.getString("s_ok"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (groups.isEmpty()) {
                            Toast.makeText(ContactListActivity.this, resources.getString("s_you_must_add_group"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        @SuppressLint("CutPasteId")
                        EditText uin2 = lay.findViewById(R.id.add_contact_uin);
                        String sUIN = uin2.getText().toString();
                        if (sUIN.length() < 4) {
                            Toast.makeText(ContactListActivity.this, resources.getString("s_incorrect_icq_id"), Toast.LENGTH_SHORT).show();
                        } else if (!utilities.isUIN(sUIN) && !utilities.isMrim(sUIN)) {
                            Toast.makeText(ContactListActivity.this, resources.getString("s_incorrect_icq_id"), Toast.LENGTH_SHORT).show();
                        } else {
                            @SuppressLint("CutPasteId")
                            EditText name2 = lay.findViewById(R.id.add_contact_name);
                            String sNAME = name2.getText().toString();
                            if (sNAME.isEmpty()) {
                                sNAME = sUIN;
                            }
                            @SuppressLint("CutPasteId")
                            Spinner spn2 = lay.findViewById(R.id.add_contact_groups);
                            UAdapter adp2 = (UAdapter) spn2.getAdapter();
                            int groupId = (int) adp2.getItemId(spn2.getSelectedItemPosition());
                            ICQContact contact = new ICQContact();
                            contact.ID = sUIN;
                            contact.name = sNAME;
                            contact.group = groupId;
                            contact.id = utilities.getRandomSSIId();
                            contact.profile = contextProfile;
                            contact.init();
                            try {
                                contextProfile.doAddContact(contact, 0);
                            } catch (Exception e) {
                                Toast.makeText(ContactListActivity.this, resources.getString("s_icq_contact_add_error"), Toast.LENGTH_SHORT).show();
                                //noinspection CallToPrintStackTrace
                                e.printStackTrace();
                            }
                            removeDialog(6);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(6);
                    }
                });
                break;
            case 7:
                if (contextContact != null) {
                    UAdapter adp2 = new UAdapter();
                    adp2.setMode(2);
                    adp2.setPadding(15);
                    adp2.setTextSize(18);
                    if (contextContact.isChating) {
                        adp2.put(resources.getString("s_close_chat"), 0);
                    }
                    if (service.opened_chats.size() > 1) {
                        adp2.put(resources.getString("s_close_all_chats"), 14);
                    }
                    if (contextContact.xstatus != null) {
                        adp2.put(resources.getString("s_req_status"), 13);
                    }
                    if (contextContact.profile.connected && utilities.isUIN(contextContact.ID)) {
                        adp2.put(resources.getString("s_contact_info"), 3);
                    }
                    if (contextContact.authorized && contextContact.profile.connected) {
                        adp2.put(resources.getString("s_client_info"), 4);
                    }
                    if (!contextContact.added) {
                        adp2.put(resources.getString("s_add_contact"), 8);
                    }
                    if (contextContact.profile.connected) {
                        adp2.put(resources.getString("s_delete_self_from_user_cl"), 5);
                    }
                    adp2.put(resources.getString("s_copy_nick"), 7);
                    if (utilities.isUIN(contextContact.ID)) {
                        adp2.put(resources.getString("s_copy_uin"), 6);
                    } else {
                        adp2.put(resources.getString("s_copy_email"), 6);
                    }
                    adp2.put(resources.getString("s_do_update_avatar"), 15);
                    if (utilities.isUIN(contextContact.ID)) {
                        adp2.put(resources.getString("s_do_update_nick"), 9);
                    }
                    if (contextContact.added) {
                        adp2.put(resources.getString("s_do_rename"), 10);
                    }
                    if (contextContact.profile.connected && contextContact.added) {
                        adp2.put(resources.getString("s_visibility"), 11);
                    }
                    adp2.put(resources.getString("s_delete_history"), 12);
                    if (contextContact.profile.connected) {
                        adp2.put(resources.getString("s_do_delete"), 2);
                    }
                    ad = DialogBuilder.createWithNoHeader(this, adp2, 48, new ContactContextMenuListener(adp2));
                    break;
                } else //noinspection ConstantConditions
                    if (contextContact == null && contextJContact != null && contextMMPContact == null) {
                        UAdapter adp3 = new UAdapter();
                        adp3.setMode(2);
                        adp3.setPadding(15);
                        adp3.setTextSize(18);
                        if (contextJContact.isChating) {
                            adp3.put(resources.getString("s_close_chat"), 0);
                        }
                        if (service.opened_chats.size() > 1) {
                            adp3.put(resources.getString("s_close_all_chats"), 14);
                        }
                        if (contextJContact.profile.connected) {
                            adp3.put(resources.getString("s_user_vcard"), 18);
                        }
                        if (!contextJContact.conf_pm) {
                            if (contextJContact.profile.connected) {
                                adp3.put(resources.getString("s_do_update_avatar"), 19);
                                adp3.put(resources.getString("s_do_update_nick"), 30);
                                adp3.put(resources.getString("s_do_rename"), 28);
                                adp3.put(resources.getString("s_move"), 27);
                            }
                            if (contextJContact.isOnline()) {
                                adp3.put(resources.getString("s_commands"), 29);
                            }
                            adp3.put(resources.getString("s_copy_jid"), 20);
                            adp3.put(resources.getString("s_copy_nick"), 21);
                            if (contextJContact.isOnline()) {
                                adp3.put(resources.getString("s_jabber_resources"), 16);
                            }
                            if (contextJContact.profile.connected) {
                                if (contextJContact.subscription != 2 && contextJContact.subscription != 3) {
                                    adp3.put(resources.getString("s_do_req_auth"), 17);
                                }
                                if (JProtocol.itIsServer(contextJContact.ID)) {
                                    adp3.put(Locale.getString("s_jabber_server_login"), 31);
                                    adp3.put(Locale.getString("s_jabber_server_logout"), 32);
                                }
                            }
                        }
                        adp3.put(resources.getString("s_delete_history"), 25);
                        if (contextJContact.conf_pm) {
                            adp3.put(resources.getString("s_do_delete"), 2);
                        } else if (contextJContact.profile.connected) {
                            adp3.put(resources.getString("s_do_delete"), 2);
                        }
                        ad = DialogBuilder.createWithNoHeader(this, adp3, 48, new ContactContextMenuListener(adp3));
                        break;
                    } else //noinspection ConstantConditions
                        if (contextContact == null && contextJContact == null && contextMMPContact != null) {
                            UAdapter adp4 = new UAdapter();
                            adp4.setMode(2);
                            adp4.setPadding(15);
                            adp4.setTextSize(18);
                            if (contextMMPContact.isChating) {
                                adp4.put(resources.getString("s_close_chat"), 0);
                            }
                            if (service.opened_chats.size() > 1) {
                                adp4.put(resources.getString("s_close_all_chats"), 14);
                            }
                            adp4.put(resources.getString("s_copy_email"), 23);
                            adp4.put(resources.getString("s_copy_nick"), 24);
                            if (contextMMPContact.profile.connected) {
                                adp4.put(resources.getString("s_do_update_avatar"), 22);
                            }
                            adp4.put(resources.getString("s_delete_history"), 26);
                            ad = DialogBuilder.createWithNoHeader(this, adp4, 48, new ContactContextMenuListener(adp4));
                            break;
                        }
                break;
            case 10:
                if (tempContactForDisplayInfo == null) {
                    return null;
                }
                LinearLayout info_lay = (LinearLayout) View.inflate(this, R.layout.vcard, null);
                final ImageView vcard_avatar = info_lay.findViewById(R.id.vcard_avatar);
                EditText vcard_desc = info_lay.findViewById(R.id.vcard_desc);
                final String data = resources.getString("s_icq_info_nick") + ": " + tempContactForDisplayInfo.nickname + "\n" + resources.getString("s_icq_info_name") + ": " + tempContactForDisplayInfo.name + "\n" + resources.getString("s_icq_info_surname") + ": " + tempContactForDisplayInfo.surname + "\n" + resources.getString("s_icq_info_city") + ": " + tempContactForDisplayInfo.city + "\n\n" + resources.getString("s_icq_info_birthdate") + ": " + tempContactForDisplayInfo.birthday + "/" + tempContactForDisplayInfo.birthmonth + "/" + tempContactForDisplayInfo.birthyear + "\n" + resources.getString("s_icq_info_age") + ": " + tempContactForDisplayInfo.age + "\n" + resources.getString("s_icq_info_gender") + ": " + tempContactForDisplayInfo.sex + "\n\n" + resources.getString("s_icq_info_homepage") + "\n" + tempContactForDisplayInfo.homepage + "\n\nE-Mail:\n" + tempContactForDisplayInfo.email + "\n\n" + resources.getString("s_icq_info_about") + "\n" + tempContactForDisplayInfo.about;
                vcard_avatar.setImageDrawable(tempContactForDisplayInfo.avatar);
                tempContactForDisplayInfo.setRedirect(new Callback() {
                    @Override
                    public void notify(final Object object, int args) {
                        vcard_avatar.post(new Runnable() {
                            @Override
                            public void run() {
                                vcard_avatar.setImageDrawable((Drawable) object);
                            }
                        });
                    }
                });
                vcard_desc.setText(data);
                vcard_desc.setFilters(new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                return source.length() < 1 ? dest.subSequence(dstart, dend) : "";
                            }
                        }
                });

                ad = DialogBuilder.createYesNo(
                        this,
                        info_lay,
                        48,
                        resources.getString("s_icq_info") + "[" + tempContactForDisplayInfo.uin + "]",
                        resources.getString("s_copy"),
                        resources.getString("s_close"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager cb = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                                cb.setText(data);
                                Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                                removeDialog(10);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                removeDialog(10);
                            }
                        }
                );
                break;
            case 11:
                @SuppressLint("InflateParams")
                LinearLayout lay_0 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.rename_dialog, null);
                final EditText edt = lay_0.findViewById(R.id.rename_nick);
                resources.attachEditText(edt);
                edt.setText(contextContact.name);
                ad = DialogBuilder.createYesNo(
                        this,
                        lay_0,
                        48,
                        resources.getString("s_renaming"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (edt.length() != 0) {
                                    contextContact.profile.doRenameContact(contextContact, edt.getText().toString().trim());
                                    removeDialog(11);
                                }
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                removeDialog(11);
                            }
                        }
                );
                break;
            case 12:
                final UAdapter adp_2 = new UAdapter();
                adp_2.setPadding(15);
                adp_2.setTextSize(18);
                if (contextContact.isVisible()) {
                    adp_2.put(resources.visible, resources.getString("s_del_from_vis"), 0);
                } else {
                    adp_2.put(resources.visible, resources.getString("s_add_to_vis"), 1);
                }
                if (contextContact.isInvisible()) {
                    adp_2.put(resources.invisible, resources.getString("s_del_from_invis"), 2);
                } else {
                    adp_2.put(resources.invisible, resources.getString("s_add_to_invis"), 3);
                }
                if (contextContact.isIgnore()) {
                    adp_2.put(resources.ignore, resources.getString("s_del_from_ignore"), 4);
                } else {
                    adp_2.put(resources.ignore, resources.getString("s_add_to_ignore"), 5);
                }
                ad = DialogBuilder.createWithNoHeader(this, adp_2, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        removeDialog(12);
                        int id2 = (int) adp_2.getItemId(arg2);
                        switch (id2) {
                            case 0: {
                                ssi_item i = contextContact.profile.isInVisible(contextContact.ID);
                                //noinspection DataFlowIssue
                                contextContact.profile.doRemoveFromLists(i, i.listType);
                                return;
                            }
                            case 1: {
                                ssi_item i2 = new ssi_item();
                                i2.uin = contextContact.ID;
                                i2.id = utilities.getRandomSSIId();
                                i2.listType = 2;
                                contextContact.profile.doAddToLists(i2, i2.listType);
                                return;
                            }
                            case 2: {
                                ssi_item i3 = contextContact.profile.isInInvisible(contextContact.ID);
                                //noinspection DataFlowIssue
                                contextContact.profile.doRemoveFromLists(i3, i3.listType);
                                return;
                            }
                            case 3: {
                                ssi_item i4 = new ssi_item();
                                i4.uin = contextContact.ID;
                                i4.id = utilities.getRandomSSIId();
                                i4.listType = 3;
                                contextContact.profile.doAddToLists(i4, i4.listType);
                                return;
                            }
                            case 4: {
                                ssi_item i5 = contextContact.profile.isInIgnore(contextContact.ID);
                                //noinspection DataFlowIssue
                                contextContact.profile.doRemoveFromLists(i5, i5.listType);
                                return;
                            }
                            case 5: {
                                ssi_item i6 = new ssi_item();
                                i6.uin = contextContact.ID;
                                i6.id = utilities.getRandomSSIId();
                                i6.listType = 14;
                                contextContact.profile.doAddToLists(i6, i6.listType);
                                return;
                            }
                            default:
                                break;
                        }
                    }
                });
                break;
            case 13:
                @SuppressLint("InflateParams")
                LinearLayout lay_1 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xtraz_dialog, null);
                xtitle = lay_1.findViewById(R.id.xtraz_title);
                xdesc = lay_1.findViewById(R.id.xtraz_text);
                resources.attachEditText(xtitle);
                resources.attachEditText(xdesc);
                xtitle.setHint(resources.getString("s_xstatus_hint_title"));
                xdesc.setHint(resources.getString("s_xstatus_hint_desc"));
                ad = DialogBuilder.createYesNo(
                        this,
                        lay_1,
                        48,
                        resources.getString("s_status_text"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                contextProfile.xtitle = xtitle.getText().toString();
                                contextProfile.xdesc = xdesc.getText().toString();
                                contextProfile.xsts = selectedX;
                                contextProfile.saveXStatus();
                                contextProfile.updateUserInfo();
                                contextProfile.notifyStatusIcon();
                                removeDialog(13);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                removeDialog(13);
                            }
                        }
                );
                break;
            case 14:
                LastContextAction = 0;
                ContextConfirmListener confirm_listener = new ContextConfirmListener();
                ad = DialogBuilder.createYesNo(this, 48, contextContact == null ? contextJContact.name : contextContact.name, resources.getString("s_delete_confirm"), resources.getString("s_yes"), resources.getString("s_no"), confirm_listener, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(14);
                    }
                });
                break;
            case 15:
                LastContextAction = 1;
                ContextConfirmListener confirm_listener2 = new ContextConfirmListener();
                ad = DialogBuilder.createYesNo(this, 48, contextContact.name, resources.getString("s_delete_confirm"), resources.getString("s_yes"), resources.getString("s_no"), confirm_listener2, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(15);
                    }
                });
                break;
            case 16:
                LastContextAction = 2;
                ContextConfirmListener confirm_listener3 = new ContextConfirmListener();
                ad = DialogBuilder.createYesNo(this, 48, resources.getString("s_exit"), resources.getString("s_exit_confirm"), resources.getString("s_yes"), resources.getString("s_no"), confirm_listener3, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(16);
                    }
                });
                break;
            case 17:
                ad = DialogBuilder.createYesNo(this, 48, resources.getString("s_information"), resources.getString("s_no_profiles_notify"), resources.getString("s_yes"), resources.getString("s_no"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intentProfilesManager();
                        removeDialog(17);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(17);
                    }
                });
                break;
            case 18:
                URLSpan span = new URLSpan("market://details?id=ru.ivansuper.jasmindonate");
                SpannableStringBuilder ssb = new SpannableStringBuilder(resources.getString("s_donate_dialog") + "\nmarket://details?id=ru.ivansuper.jasmindonate");
                ssb.setSpan(span, ssb.length() - "market://details?id=ru.ivansuper.jasmindonate".length(), ssb.length(), 33);
                ad = DialogBuilder.create(this, resources.getString("s_donate"), ssb, 48);
                break;
            case 23:
                if (dialog_for_display == null) {
                    return null;
                }
                ad = DialogBuilder.createOk(this, dialog_for_display.header, dialog_for_display.text, resources.getString("s_close"), 48, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(23);
                    }
                });
                break;
            case 24:
                final EditText raw_uin = new EditText(this);
                raw_uin.setInputType(8194);
                resources.attachEditText(raw_uin);
                ad = DialogBuilder.createOk(this, raw_uin, resources.getString("s_icq_search_by_uin"), resources.getString("s_ok"), 48, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uin2 = raw_uin.getText().toString().trim();
                        if (uin2.length() < 4 || uin2.length() > 9 || !utilities.isUIN(uin2)) {
                            Toast.makeText(ContactListActivity.this, resources.getString("s_incorrect_uin"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        removeDialog(24);
                        contextProfile.doRequestContactInfoForDisplay(uin2);
                    }
                });
                break;
            case 25:
                ad = DialogBuilder.create(this, resources.getString("s_information"), resources.getString("s_force_close_info"), 48);
                break;
            case 26:
                ad = DialogBuilder.createOk(this, "Список изменений\n(changelog)", resources.VERSION + " (english version at bottom):\n" + utilities.readChangeLog(), resources.getString("s_close"), 48, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(26);
                    }
                });
                break;
            case 27:
                if (!contextProfile.connected) {
                    Toast.makeText(this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                } else {
                    LinearLayout info_dialog = (LinearLayout) View.inflate(this, R.layout.change_info_dialog, null);
                    ((TextView) info_dialog.findViewById(R.id.l1)).setText(resources.getString("s_icq_change_info_nick"));
                    ((TextView) info_dialog.findViewById(R.id.l2)).setText(resources.getString("s_icq_change_info_name"));
                    ((TextView) info_dialog.findViewById(R.id.l3)).setText(resources.getString("s_icq_change_info_surname"));
                    ((TextView) info_dialog.findViewById(R.id.l4)).setText(resources.getString("s_icq_change_info_gender"));
                    ((TextView) info_dialog.findViewById(R.id.l5)).setText(resources.getString("s_icq_change_info_birthday"));
                    ((TextView) info_dialog.findViewById(R.id.l7)).setText(resources.getString("s_icq_change_info_homepage"));
                    ((TextView) info_dialog.findViewById(R.id.l8)).setText(resources.getString("s_icq_change_info_city"));
                    ((TextView) info_dialog.findViewById(R.id.l9)).setText(resources.getString("s_icq_change_info_about"));
                    final EditText nick_e = info_dialog.findViewById(R.id.info_dialog_nick_e);
                    resources.attachEditText(nick_e);
                    final EditText name_e = info_dialog.findViewById(R.id.info_dialog_name_e);
                    resources.attachEditText(name_e);
                    final EditText surname_e = info_dialog.findViewById(R.id.info_dialog_surname_e);
                    resources.attachEditText(surname_e);
                    final EditText email_e = info_dialog.findViewById(R.id.info_dialog_email);
                    resources.attachEditText(email_e);
                    final EditText homepage_e = info_dialog.findViewById(R.id.info_dialog_homepage);
                    resources.attachEditText(homepage_e);
                    final EditText city_e = info_dialog.findViewById(R.id.info_dialog_city_e);
                    resources.attachEditText(city_e);
                    final EditText about_e = info_dialog.findViewById(R.id.info_dialog_about_e);
                    resources.attachEditText(about_e);
                    final EditText b_year = info_dialog.findViewById(R.id.info_dialog_b_year);
                    resources.attachEditText(b_year);
                    final EditText b_month = info_dialog.findViewById(R.id.info_dialog_b_month);
                    resources.attachEditText(b_month);
                    final EditText b_day = info_dialog.findViewById(R.id.info_dialog_b_day);
                    resources.attachEditText(b_day);
                    final RadioButton gender_m = info_dialog.findViewById(R.id.info_dialog_gender_m);
                    gender_m.setText(resources.getString("s_icq_change_info_gender_man"));
                    final RadioButton gender_w = info_dialog.findViewById(R.id.info_dialog_gender_w);
                    gender_w.setText(resources.getString("s_icq_change_info_gender_woman"));
                    final RadioButton gender_none = info_dialog.findViewById(R.id.info_dialog_gender_none);
                    gender_none.setText(resources.getString("s_icq_change_info_gender_none"));
                    nick_e.setText(contextProfile.info_container.nickname);
                    name_e.setText(contextProfile.info_container.name);
                    surname_e.setText(contextProfile.info_container.surname);
                    email_e.setText(contextProfile.info_container.email);
                    homepage_e.setText(contextProfile.info_container.homepage);
                    city_e.setText(contextProfile.info_container.city);
                    about_e.setText(contextProfile.info_container.about);
                    b_year.setText(String.valueOf(contextProfile.info_container.birthyear));
                    b_month.setText(String.valueOf(contextProfile.info_container.birthmonth));
                    b_day.setText(String.valueOf(contextProfile.info_container.birthday));
                    switch (contextProfile.info_container.sex_) {
                        case 0:
                            gender_none.setChecked(true);
                            break;
                        case 1:
                            gender_w.setChecked(true);
                            break;
                        case 2:
                            gender_m.setChecked(true);
                            break;
                    }
                    ad = DialogBuilder.createYesNo(
                            this,
                            info_dialog,
                            80,
                            resources.getString("s_do_change_own_info"),
                            resources.getString("s_ok"),
                            resources.getString("s_cancel"),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    contextProfile.info_container.nickname = nick_e.getText().toString();
                                    contextProfile.info_container.name = name_e.getText().toString();
                                    contextProfile.info_container.surname = surname_e.getText().toString();
                                    contextProfile.info_container.email = email_e.getText().toString();
                                    contextProfile.info_container.homepage = homepage_e.getText().toString();
                                    contextProfile.info_container.city = city_e.getText().toString();
                                    contextProfile.info_container.about = about_e.getText().toString();
                                    if (gender_m.isChecked()) {
                                        contextProfile.info_container.sex_ = 2;
                                    }
                                    if (gender_w.isChecked()) {
                                        contextProfile.info_container.sex_ = 1;
                                    }
                                    if (gender_none.isChecked()) {
                                        contextProfile.info_container.sex_ = 0;
                                    }
                                    try {
                                        int year = Integer.parseInt(b_year.getText().toString().trim());
                                        int month = Integer.parseInt(b_month.getText().toString().trim());
                                        int day = Integer.parseInt(b_day.getText().toString().trim());
                                        contextProfile.info_container.birthyear = year;
                                        contextProfile.info_container.birthmonth = month;
                                        contextProfile.info_container.birthday = day;
                                        contextProfile.doUpdateInfo();
                                        removeDialog(27);
                                    } catch (Exception e) {
                                        Toast.makeText(ContactListActivity.this, resources.getString("s_incorrect_birthdayя"), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    removeDialog(27);
                                }
                            }
                    );
                }
                break;
            case 28:
                UAdapter group_actions = new UAdapter();
                group_actions.setMode(2);
                group_actions.setTextSize(18);
                group_actions.setPadding(15);
                group_actions.put(resources.getString("s_do_rename"), 0);
                group_actions.put(resources.getString("s_do_delete"), 1);
                ad = DialogBuilder.create(this, resources.getString("s_group_tools"), group_actions, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i) {
                            case 0:
                                removeDialog(28);
                                removeDialog(29);
                                showDialogA(29);
                                return;
                            case 1:
                                removeDialog(28);
                                removeDialog(30);
                                showDialogA(30);
                                return;
                            default:
                        }
                    }
                });
                break;
            case 29:
                @SuppressLint("InflateParams")
                LinearLayout lay_2 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.rename_dialog, null);
                final EditText edt1 = lay_2.findViewById(R.id.rename_nick);
                resources.attachEditText(edt1);
                edt1.setText(contextGroup.name);
                ad = DialogBuilder.createYesNo(this, lay_2, 48, resources.getString("s_renaming"), resources.getString("s_ok"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!edt1.getText().toString().trim().isEmpty()) {
                            contextGroup.profile.doRenameGroup(contextGroup, edt1.getText().toString());
                            removeDialog(29);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(29);
                    }
                });
                break;
            case 30:
                LastContextAction = 10;
                ContextConfirmListener confirm_listener4 = new ContextConfirmListener();
                ad = DialogBuilder.createYesNo(this, 48, contextGroup.name, resources.getString("s_delete_confirm"), resources.getString("s_yes"), resources.getString("s_no"), confirm_listener4, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(30);
                    }
                });
                break;
            case SHOW_NOTIFY:
                @SuppressLint("InflateParams")
                LinearLayout lay_3 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.rename_dialog, null);
                final EditText edt2 = lay_3.findViewById(R.id.rename_nick);
                resources.attachEditText(edt2);
                ad = DialogBuilder.createYesNo(this, lay_3, 48, resources.getString("s_add_group"), resources.getString("s_ok"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!edt2.getText().toString().trim().isEmpty()) {
                            ICQGroup group2 = new ICQGroup();
                            group2.name = edt2.getText().toString().trim();
                            group2.id = utilities.getRandomSSIId();
                            group2.profile = contextProfile;
                            contextProfile.doAddGroup(group2);
                            removeDialog(31);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(31);
                    }
                });
                break;
            case SHOW_PROGRESS_DIALOG:
                @SuppressLint("InflateParams")
                LinearLayout away = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.away_text_dialog, null);
                final EditText away_text = away.findViewById(R.id.away_dialog_text);
                resources.attachEditText(away_text);
                away_text.setText(contextProfile.away_text);
                ad = DialogBuilder.createYesNo(
                        this,
                        away,
                        48,
                        resources.getString("s_status_text"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                contextProfile.setAwayText(away_text.getText().toString().trim());
                                removeDialog(32);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                removeDialog(32);
                            }
                        }
                );
                break;
            case HIDE_PROGRESS_DIALOG:
                final UAdapter adp_1 = new UAdapter();
                adp_1.setMode(2);
                adp_1.setTextSize(18);
                adp_1.setPadding(15);
                adp_1.put(resources.getString("s_add_contact"), 0);
                adp_1.put(resources.getString("s_add_group"), 1);
                adp_1.put(resources.getString("s_do_refresh_avatars"), 2);
                adp_1.put(resources.getString("s_do_refresh_names"), 3);
                adp_1.put(resources.getString("s_do_change_avatar"), 4);
                adp_1.put(resources.getString("s_edit_vcard"), 5);
                ad = DialogBuilder.createWithNoHeader(this, adp_1, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                        removeDialog(33);
                        int id2 = (int) adp_1.getItemId(index);
                        switch (id2) {
                            case 0:
                                removeDialog(36);
                                showDialogA(36);
                                return;
                            case 1:
                                removeDialog(37);
                                showDialogA(37);
                                return;
                            case 2:
                                Thread t = new Thread() {
                                    @Override
                                    public void run() {
                                        setPriority(1);
                                        Vector<JContact> list = contextJProfile.getOnlyContacts();
                                        for (JContact contact : list) {
                                            contact.getAvatar();
                                            try {
                                                Thread.sleep(1000L);
                                            } catch (InterruptedException ignored) {
                                            }
                                        }
                                    }
                                };
                                t.start();
                                return;
                            case 3:
                                Thread t2 = new Thread() {
                                    @Override
                                    public void run() {
                                        setPriority(1);
                                        Vector<JContact> list = contextJProfile.getOnlyContacts();
                                        for (JContact contact : list) {
                                            contact.updateNick();
                                            try {
                                                Thread.sleep(1000L);
                                            } catch (InterruptedException ignored) {
                                            }
                                        }
                                    }
                                };
                                t2.start();
                                return;
                            case 4:
                                Intent i = new Intent();
                                i.setClass(ContactListActivity.this, FileBrowserActivity.class);
                                startActivityForResult(i, JABBER_AVATAR_REQUEST);
                                return;
                            case 5:
                                service.showVCardEditor(contextJProfile.my_vcard);
                                return;
                            default:
                        }
                    }
                });
                break;
            case 34:
                SpannableStringBuilder ssb2 = new SpannableStringBuilder(resources.getString("s_jabber_connected_resources") + ":\n");
                Vector<JContact.Resource> resources_ = contextJContact.getResources();
                for (JContact.Resource res : resources_) {
                    Drawable client = Clients.getIcon(res.client);
                    if (client != null) {
                        ssb2.append(" ");
                        ImageSpan client_span = new ImageSpan(client);
                        ssb2.setSpan(client_span, ssb2.length() - 1, ssb2.length(), 33);
                    } else {
                        ssb2.append("*");
                    }
                    ssb2.append(res.name).append(" (").append(String.valueOf(res.priority)).append(": ").append(JProtocol.translateStatus(res.status)).append(")");
                    ssb2.append("\n");
                }
                ad = DialogBuilder.create(this, resources.getString("s_jabber_resources"), ssb2, 48);
                break;
            case 35:
                @SuppressLint("InflateParams")
                LinearLayout jstatus = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.away_text_dialog, null);
                final EditText jstatus_text = jstatus.findViewById(R.id.away_dialog_text);
                resources.attachEditText(jstatus_text);
                jstatus_text.setText(contextJProfile.getStatusDescription());
                ad = DialogBuilder.createYesNo(
                        this,
                        jstatus,
                        48,
                        resources.getString("s_status_text"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                contextJProfile.setStatusDescription(jstatus_text.getText().toString().trim());
                                removeDialog(35);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                removeDialog(35);
                            }
                        }
                );
                break;
            case 36:
                UAdapter jgroups = new UAdapter();
                jgroups.setMode(2);
                jgroups.setTextSize(18);
                jgroups.setTextColor(-7829368);
                jgroups.setPadding(15);
                final Vector<JGroup> j_groups = contextJProfile.getGroupsA();
                for (JGroup group2 : j_groups) {
                    jgroups.put(group2.name, group2.id);
                }
                @SuppressLint("InflateParams") final LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.add_contact_dialog, null);
                ((TextView) layout.findViewById(R.id.l1)).setText(resources.getString("s_contact_id"));
                ((TextView) layout.findViewById(R.id.l2)).setText(resources.getString("s_contact_name"));
                ((TextView) layout.findViewById(R.id.l3)).setText(resources.getString("s_contact_group"));
                EditText contact_ID = layout.findViewById(R.id.add_contact_uin);
                contact_ID.setHint("nickname@" + contextJProfile.host);
                resources.attachEditText(contact_ID);
                EditText contact_name = layout.findViewById(R.id.add_contact_name);
                resources.attachEditText(contact_name);
                Spinner spn2 = layout.findViewById(R.id.add_contact_groups);
                spn2.setAdapter(jgroups);
                ad = DialogBuilder.createYesNo(
                        this,
                        layout,
                        48,
                        resources.getString("s_add_contact"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditText ID = layout.findViewById(R.id.add_contact_uin);
                                String sID = ID.getText().toString().toLowerCase().trim();
                                String[] parts = sID.split("@");
                                if (parts.length != 2 || parts[1].isEmpty()) {
                                    Toast.makeText(ContactListActivity.this, resources.getString("s_incorrect_jid"), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                EditText name2 = layout.findViewById(R.id.add_contact_name);
                                String sNAME = name2.getText().toString();
                                if (sNAME.isEmpty()) {
                                    sNAME = sID;
                                }
                                Spinner spn3 = layout.findViewById(R.id.add_contact_groups);
                                String groupId = "";
                                if (!j_groups.isEmpty()) {
                                    UAdapter adp5 = (UAdapter) spn3.getAdapter();
                                    groupId = adp5.getItem(spn3.getSelectedItemPosition());
                                }
                                JContact contact = new JContact(contextJProfile, sID);
                                contact.name = sNAME;
                                contact.group = groupId;
                                contact.profile = contextJProfile;
                                try {
                                    contextJProfile.doAddContact(contact);
                                } catch (Exception e) {
                                    Toast.makeText(ContactListActivity.this, resources.getString("s_icq_contact_add_error"), Toast.LENGTH_SHORT).show();
                                    //noinspection CallToPrintStackTrace
                                    e.printStackTrace();
                                }
                                removeDialog(36);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                removeDialog(36);
                            }
                        }
                );
                break;
            case 37:
                @SuppressLint("InflateParams")
                LinearLayout lay_4 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.rename_dialog, null);
                final EditText edt3 = lay_4.findViewById(R.id.rename_nick);
                resources.attachEditText(edt3);
                ad = DialogBuilder.createYesNo(
                        this,
                        lay_4,
                        48,
                        resources.getString("s_add_group"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!edt3.getText().toString().trim().isEmpty()) {
                                    String name2 = edt3.getText().toString().trim();
                                    JGroup group3 = new JGroup(contextJProfile, name2);
                                    contextJProfile.doAddGroup(group3);
                                    removeDialog(37);
                                }
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                removeDialog(37);
                            }
                        }
                );
                break;
            case 38:
                @SuppressLint("InflateParams") final LinearLayout sms_layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.send_sms, null);
                ((TextView) sms_layout.findViewById(R.id.l1)).setText(resources.getString("s_send_sms_dialog_hint_1"));
                ((TextView) sms_layout.findViewById(R.id.l2)).setText(resources.getString("s_send_sms_dialog_hint_2"));
                @SuppressLint("CutPasteId") EditText phone = sms_layout.findViewById(R.id.send_sms_phone);
                resources.attachEditText(phone);
                @SuppressLint("CutPasteId") EditText message_text = sms_layout.findViewById(R.id.send_sms_text);
                resources.attachEditText(message_text);
                ad = DialogBuilder.createYesNo(this, sms_layout, 48, resources.getString("s_send_sms"), resources.getString("s_ok"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        @SuppressLint("CutPasteId") EditText phone2 = sms_layout.findViewById(R.id.send_sms_phone);
                        String sPHONE = phone2.getText().toString().trim();
                        if (sPHONE.length() != 12 && !sPHONE.startsWith("+")) {
                            Toast.makeText(ContactListActivity.this, resources.getString("s_incorrect_phone"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        @SuppressLint("CutPasteId") EditText message_text2 = sms_layout.findViewById(R.id.send_sms_text);
                        String sTEXT = message_text2.getText().toString();
                        if (!sTEXT.isEmpty()) {
                            if (contextMrimProfile != null) {
                                contextMrimProfile.doSendSMS(sPHONE, sTEXT);
                            }
                            removeDialog(38);
                            return;
                        }
                        Toast.makeText(ContactListActivity.this, resources.getString("s_empty_sms_error"), Toast.LENGTH_SHORT).show();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(38);
                    }
                });
                break;
            case 42:
                @SuppressLint("InflateParams")
                LinearLayout conf_lay = (LinearLayout) View.inflate(this, R.layout.conference_login, null);
                final EditText server = conf_lay.findViewById(R.id.conf_server);
                server.setHint(resources.getString("s_join_conference_dialog_server"));
                final EditText room = conf_lay.findViewById(R.id.conf_room);
                room.setHint(resources.getString("s_join_conference_dialog_room"));
                final EditText nickname = conf_lay.findViewById(R.id.conf_nick);
                nickname.setHint(resources.getString("s_join_conference_dialog_nick"));
                final EditText pass = conf_lay.findViewById(R.id.conf_pass);
                pass.setHint(resources.getString("s_join_conference_dialog_pass"));
                Button server_select = conf_lay.findViewById(R.id.conf_server_select);
                Button room_select = conf_lay.findViewById(R.id.conf_room_select);
                resources.attachEditText(server);
                resources.attachEditText(room);
                resources.attachEditText(nickname);
                resources.attachEditText(pass);
                resources.attachButtonStyle(server_select);
                nickname.setText(JProtocol.getNameFromFullID(contextJProfile.ID));
                final Spinner servers = conf_lay.findViewById(R.id.conf_servers);
                final UAdapter srvs = new UAdapter();
                srvs.setMode(2);
                srvs.setPadding(15);
                srvs.setTextSize(18);
                srvs.setTextColor(-1);
                srvs.setUseShadow(true);
                for (int i = 0; i < contextJProfile.conferences.size(); i++) {
                    srvs.put(contextJProfile.conferences.get(i), i);
                }
                server.setText("conference." + contextJProfile.host);
                servers.setAdapter(srvs);
                server_select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        servers.performClick();
                    }
                });
                servers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        server.setText(srvs.getItem(arg2));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
                room_select.setOnClickListener(new AnonymousClass59(server, room));
                ad = DialogBuilder.createYesNo(
                        this,
                        conf_lay,
                        48,
                        resources.getString("s_jabber_join_conference_dialog"),
                        resources.getString("s_join"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String room_ = room.getText().toString();
                                String nick = nickname.getText().toString();
                                if (room_.isEmpty() || nick.isEmpty()) {
                                    Toast toast = Toast.makeText(ContactListActivity.this, resources.getString("s_join_parameters_required"), Toast.LENGTH_SHORT);
                                    toast.setGravity(48, 0, 0);
                                    toast.show();
                                    return;
                                }
                                String server_ = server.getText().toString().trim();
                                contextJProfile.joinConference((room_ + "@" + server_).toLowerCase(), nick, pass.getText().toString());
                                removeDialog(42);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                removeDialog(42);
                            }
                        }
                );
                break;
            case 43:
                UAdapter adp5 = new UAdapter();
                adp5.setMode(2);
                adp5.setPadding(15);
                adp5.setTextSize(18);
                if (contextConference.conference.profile.connected) {
                    if (contextConference.conference.isOnline()) {
                        adp5.put(resources.getString("s_leave_conference"), 0);
                    } else {
                        adp5.put(resources.getString("s_join_conference"), 1);
                    }
                    adp5.put(Locale.getString("s_add_to_bookmarks"), 3);
                }
                adp5.put(resources.getString("s_delete_conference"), 2);
                ad = DialogBuilder.createWithNoHeader(this, adp5, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch ((int) adapterView.getAdapter().getItemId(i)) {
                            case 0:
                                contextConference.conference.profile.logoutConference(contextConference.conference.JID);
                                break;
                            case 1:
                                contextConference.conference.profile.joinConference(contextConference.conference.JID, contextConference.conference.nick, contextConference.conference.pass);
                                break;
                            case 2:
                                new AreYouSureHelper(ContactListActivity.this, Locale.getString("s_delete_conference"), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (contextConference.conference.isOnline()) {
                                            contextConference.conference.profile.logoutConference(contextConference.conference.JID);
                                        }
                                        contextConference.conference.profile.removeConference(contextConference.conference.JID);
                                    }
                                }, null);
                                break;
                            case 3:
                                BookmarkList bl = contextConference.conference.profile.bookmarks;
                                if (bl.itIsExist(contextConference.conference.JID)) {
                                    Toast.makeText(ContactListActivity.this, Locale.getString("s_bookmark_already_exist"), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                BookmarkItem item = new BookmarkItem();
                                item.type = 0;
                                item.NAME = contextConference.conference.JID;
                                item.JID_OR_URL = item.NAME;
                                item.autojoin = false;
                                item.nick = contextConference.conference.nick;
                                item.password = contextConference.conference.pass;
                                bl.add(item);
                                Toast.makeText(ContactListActivity.this, Locale.getString("s_saved"), Toast.LENGTH_SHORT).show();
                                break;
                        }
                        removeDialog(43);
                    }
                });
                break;
            case 44:
                LinearLayout vcard_lay = (LinearLayout) View.inflate(this, R.layout.vcard, null);
                ImageView vcard_avatar1 = vcard_lay.findViewById(R.id.vcard_avatar);
                vcard_avatar1.requestFocus();
                EditText vcard_desc1 = vcard_lay.findViewById(R.id.vcard_desc);
                if (vcard_to_display.avatar != null) {
                    vcard_avatar1.setImageBitmap(vcard_to_display.avatar);
                }
                vcard_desc1.setText(vcard_to_display.desc);
                vcard_desc1.setFilters(new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                return source.length() < 1 ? dest.subSequence(dstart, dend) : "";
                            }
                        }
                });

                ad = DialogBuilder.createYesNo(
                        this,
                        vcard_lay,
                        48,
                        resources.getString("s_user_vcard"),
                        resources.getString("s_copy"),
                        resources.getString("s_close"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(vcard_to_display.desc);
                                Toast.makeText(ContactListActivity.this, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                                vcard_to_display = null;
                                removeDialog(44);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                vcard_to_display = null;
                                removeDialog(44);
                            }
                        }
                );
                break;
            case 45:
                final UAdapter adp_21 = new UAdapter();
                adp_21.setMode(2);
                adp_21.setTextSize(18);
                adp_21.setPadding(15);
                adp_21.put(resources.getString("s_do_refresh_avatars"), 2);
                ad = DialogBuilder.createWithNoHeader(this, adp_21, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        removeDialog(45);
                        int id2 = (int) adp_21.getItemId(i);
                        switch (id2) {
                            case 0:
                            case 1:
                            case 2:
                                Thread t = new Thread() {
                                    @Override
                                    public void run() {
                                        setPriority(1);
                                        Vector<MMPContact> list = contextMrimProfile.getContacts();
                                        for (MMPContact contact : list) {
                                            contact.getAvatar(contact, service);
                                            try {
                                                sleep(1000L);
                                            } catch (InterruptedException ignored) {
                                            }
                                        }
                                    }
                                };
                                t.start();
                            default:
                        }
                    }
                });
                break;
            case 46:
                final UAdapter groups_list = new UAdapter();
                groups_list.setMode(2);
                groups_list.setTextSize(18);
                groups_list.setPadding(15);
                groups_list.put("[" + resources.getString("s_jabber_without_group") + "]   ", 0);
                Vector<JGroup> group_list = contextJContact.profile.getGroupsA();
                for (JGroup jGroup : group_list) {
                    groups_list.put(jGroup.name, 0);
                }
                ad = DialogBuilder.create(this, resources.getString("s_moving"), groups_list, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String new_group;
                        String group3 = groups_list.getItem(i);
                        //noinspection unused
                        String str = contextJContact.group;
                        //noinspection ConstantConditions
                        if (contextJContact != null) {
                            if (contextJContact.group.equals(group3)) {
                                Toast.makeText(ContactListActivity.this, resources.getString("s_contact_already_in_this_group"), Toast.LENGTH_SHORT).show();
                            } else if (contextJContact.group.isEmpty() && i == 0) {
                                Toast.makeText(ContactListActivity.this, resources.getString("s_contact_already_in_this_group"), Toast.LENGTH_SHORT).show();
                            } else {
                                if (i == 0) {
                                    new_group = "";
                                } else {
                                    new_group = group3;
                                }
                                contextJContact.profile.doModifyContactRaw(contextJContact.subscription, contextJContact.ID, contextJContact.name, new_group);
                                removeDialog(46);
                            }
                        }
                    }
                });
                break;
            case 47:
                final EditText priority = new EditText(this);
                priority.setInputType(8194);
                resources.attachEditText(priority);
                priority.setText(String.valueOf(contextJProfile.priority));
                ad = DialogBuilder.createYesNo(
                        this,
                        priority,
                        48,
                        resources.getString("s_jabber_priority"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String prior = priority.getText().toString();
                                if (!prior.isEmpty()) {
                                    try {
                                        int priority_ = Integer.parseInt(prior);
                                        contextJProfile.changePriority(priority_);
                                        removeDialog(47);
                                    } catch (Exception e) {
                                        //noinspection CallToPrintStackTrace
                                        e.printStackTrace();
                                    }
                                }
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                removeDialog(47);
                            }
                        }
                );
                break;
            case 48:
                @SuppressLint("InflateParams")
                LinearLayout lay_11 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.rename_dialog, null);
                final EditText edt11 = lay_11.findViewById(R.id.rename_nick);
                resources.attachEditText(edt11);
                edt11.setText(contextJContact.name);
                ad = DialogBuilder.createYesNo(this, lay_11, 48, resources.getString("s_renaming"), resources.getString("s_ok"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (edt11.length() != 0) {
                            contextJContact.profile.doModifyContactRaw(contextJContact.subscription, contextJContact.ID, edt11.getText().toString().trim(), contextJContact.group);
                            removeDialog(48);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(48);
                    }
                });
                break;
        }
        last_shown_notify_dialog = ad;
        if (last_shown_notify_dialog != null) {
            last_shown_notify_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    checkForBufferedDialogs();
                }
            });
        }
        return ad;
    }

    class AnonymousClass59 implements View.OnClickListener {
        Dialog d;
        private final /* synthetic */ EditText val$room;
        private final /* synthetic */ EditText val$server;

        AnonymousClass59(EditText editText, EditText editText2) {
            this.val$server = editText;
            this.val$room = editText2;
        }

        @Override
        public void onClick(View arg0) {
            LinearLayout rooms = (LinearLayout) View.inflate(ContactListActivity.this, R.layout.rooms_preview, null);
            EditText filter = rooms.findViewById(R.id.rooms_preview_filter);
            filter.setHint(resources.getString("s_conference_filter"));
            ListView list = rooms.findViewById(R.id.rooms_preview_list);
            final RoomsPreviewAdapter adp = new RoomsPreviewAdapter();
            list.setAdapter(adp);
            list.setSelector(resources.getListSelector());
            RoomListCallback callback = new RoomListCallback() {
                @Override
                public void roomsLoaded(final Vector<RoomsPreviewAdapter.Item> list2) {
                    jasminSvc jasminsvc = service;
                    //noinspection UnnecessaryLocalVariable
                    final RoomsPreviewAdapter roomsPreviewAdapter = adp;
                    jasminsvc.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            roomsPreviewAdapter.fill(list2);
                        }
                    });
                }

                @Override
                public void error() {
                    jasminSvc jasminsvc = service;
                    //noinspection UnnecessaryLocalVariable
                    final RoomsPreviewAdapter roomsPreviewAdapter = adp;
                    jasminsvc.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            roomsPreviewAdapter.error();
                        }
                    });
                }
            };
            filter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!adp.init || s == null) {
                        return;
                    }
                    adp.applyFilter(s.toString());
                }
            });
            final EditText editText = this.val$room;
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (adp.init) {
                        String jid = adp.getItem(i).desc;
                        editText.setText(JProtocol.getNameFromFullID(jid));
                        AnonymousClass59.this.d.dismiss();
                    }
                }
            });
            contextJProfile.requestRoomsList(this.val$server.getText().toString().trim(), callback);
            this.d = DialogBuilder.createWithNoHeader(ContactListActivity.this, rooms, 80);
            this.d.show();
        }
    }

    private void hideSearchPanel() {
        this.SEARCH_PANEL_VISIBLE = false;
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.search_panel_slot.getChildAt(0).getWindowToken(), 0);
        this.search_panel.setVisibility(View.GONE);
        this.search_panel_slot.removeAllViews();
        if (listAdp != null) {
            listAdp.setFilter(null);
        }
    }

    private void initViews() {
        this.CONFIG_LISTENER = findViewById(R.id.config_listener);
        this.CONFIG_LISTENER.listener = new ConfigListenerView.OnLayoutListener() {
            @Override
            public void onNewLayout(int w, int h, int oldw, int oldh) {
                int diff = Math.max(Math.abs(w - oldw), Math.abs(h - oldh));
                onConfigurationChangedLocal(resources.ctx.getResources().getConfiguration(), diff);
            }
        };

        Resizer.BIND(findViewById(R.id.contacts_fragment), findViewById(R.id.chat_fragment), findViewById(R.id.contactlist_list_chat_separator));

        View chat_contacts_separator = findViewById(R.id.contactlist_list_chat_separator);
        resources.attachContactlistChatDivider(chat_contacts_separator);
        if (!resources.IT_IS_TABLET) {
            chat_contacts_separator.setVisibility(View.GONE);
        }

        LinearLayout cl_back = findViewById(R.id.cl_back);
        if (!sp.getBoolean("ms_use_shadow", true)) {
            cl_back.setBackgroundColor(0);
        }

        ImageView search_panel_hide = findViewById(R.id.contactlist_search_hide);
        search_panel_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SEARCH_PANEL_VISIBLE) {
                    hideSearchPanel();
                }
            }
        });

        this.search_panel = findViewById(R.id.contactlist_search_bar);
        this.search_panel_slot = findViewById(R.id.contactlist_search_panel_slot);

        switcher = findViewById(R.id.contactlist);

        // === Создание и добавление вкладок ===
        this.chats_contactlist = new MultiColumnList(this);
        this.contactlist = new MultiColumnList(this);
        switcher.addView(this.chats_contactlist, resources.getString("s_cl_panel_chats"));
        switcher.addView(this.contactlist, resources.getString("s_cl_panel_contacts"));

        // === Селекторы и фон ===
        resources.attachListSelector(this.chats_contactlist);
        resources.attachListSelector(this.contactlist);
        //noinspection deprecation
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ms_use_solid_wallpaper", false)) {
            resources.attachContactlistBack(this.chats_contactlist);
            resources.attachContactlistBack(this.contactlist);
        }

        // === Конфигурация switcher ===
        switcher.updateConfig();
        if (PreferenceTable.ms_cl_transition_effect < 0) {
            switcher.setRandomizedAnimation(true);
        } else {
            switcher.setRandomizedAnimation(false);
            switcher.setAnimationType(PreferenceTable.ms_cl_transition_effect);
        }
        switcher.togglePanel(PreferenceTable.ms_two_screens_mode);
        switcher.setLock(!PreferenceTable.ms_two_screens_mode);
        updateBlinkState();

        // === Переход на вкладку "контакты" после layout'а ===
        switcher.post(new Runnable() {
            @Override
            public void run() {
                switcher.scrollTo(1);
            }
        });

        connectionStatusPanel = findViewById(R.id.profiles_connection_bars);
        profilesPanel = findViewById(R.id.profilesPanel);

        toggle_offline = findViewById(R.id.toggle_offline);
        toggle_offline.setOnClickListener(new ToolsPanelListener());

        toggle_vibro = findViewById(R.id.toggle_vibro);
        toggle_vibro.setOnClickListener(new ToolsPanelListener());

        toggle_sound = findViewById(R.id.toggle_sound);
        toggle_sound.setOnClickListener(new ToolsPanelListener());

        connectionStatusPanel.setBackgroundColor(ColorScheme.getColor(32));
        findViewById(R.id.bottomPanel).setBackgroundColor(ColorScheme.getColor(32));
        BOTTOM_PANEL = findViewById(R.id.bottomPanel);
        if (!BOTTOM_PANEL_VISIBLED) {
            BOTTOM_PANEL.setVisibility(View.GONE);
        }

        resources.attachContactlistBottomConnectionStatusPanel(connectionStatusPanel);
        resources.attachContactlistBottomPanel(BOTTOM_PANEL);

        // === Проверка конференций ===
        //noinspection IfStatementWithIdenticalBranches,deprecation
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ms_use_solid_wallpaper", false)) {
            checkConferences();
        } else {
            // даже если фон однотонный — нужно проверить наличие вкладки конференций
            checkConferences();
        }

        ////switcher.attrs.recycle();

        initToolsPanel();
    }

    private void checkConferences() {
        if (service != null && service.profiles != null) {
            try {
                boolean confs_present = service.profiles.scanForConferences();
                if (!confs_present) {
                    confs_present = service.profiles.hasEnabledJabberProfile();
                }
                if (confs_present && this.confs_contactlist == null) {
                    this.confs_contactlist = new MultiColumnList(this);
                    resources.attachListSelector(this.confs_contactlist);
                    //noinspection deprecation
                    if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ms_use_solid_wallpaper", false)) {
                        resources.attachContactlistBack(this.confs_contactlist);
                    }
                    switcher.addView(this.confs_contactlist, resources.getString("s_cl_panel_confs"));
                    initConferencesScreen();
                } else if (!confs_present && this.confs_contactlist != null) {
                    switcher.removeViewAt(2);
                    this.confs_contactlist = null;
                    confs_listAdp = null;
                }
            } catch (Exception e) {
                switcher.removeViewAt(2);
                this.confs_contactlist = null;
                confs_listAdp = null;
            }
        }
    }

    private void initToolsPanel() {
        //noinspection deprecation
        SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hideOffline = sp2.getBoolean("ms_offline", true);
        if (hideOffline) {
            toggle_offline.setImageDrawable(resources.toggle_offline_a);
        } else {
            toggle_offline.setImageDrawable(resources.toggle_offline);
        }
        boolean enableVibro = sp2.getBoolean("ms_vibro", true);
        if (enableVibro) {
            toggle_vibro.setImageDrawable(resources.toggle_vibro_a);
        } else {
            toggle_vibro.setImageDrawable(resources.toggle_vibro);
        }
        boolean enableSound = sp2.getBoolean("ms_sounds", true);
        if (enableSound) {
            toggle_sound.setImageDrawable(resources.toggle_sound_a);
        } else {
            toggle_sound.setImageDrawable(resources.toggle_sound);
        }
    }

    private void handleServiceConnected() {
        putHandler();
        chats_listAdp = new ContactsAdapter(this, service, true, false);
        this.chats_contactlist.setAdapter(chats_listAdp);
        listAdp = new ContactsAdapter(this, service, false, false);
        this.contactlist.setAdapter(listAdp);
        cl_listener l1 = new cl_listener();
        contactLongClickListener l2 = new contactLongClickListener();
        this.chats_contactlist.setOnItemClickListener(l1);
        this.chats_contactlist.setOnItemLongClickListener(l2);
        this.contactlist.setOnItemClickListener(l1);
        this.contactlist.setOnItemLongClickListener(l2);
        createContactlistFromProfiles();
        otherInit();
        if (service != null) {
            service.cancelMultiloginNotify();

        }
    }

    private void initConferencesScreen() {
        if (this.confs_contactlist != null) {
            confs_listAdp = new ContactsAdapter(this, service, false, true);
            this.confs_contactlist.setAdapter(confs_listAdp);
            this.confs_contactlist.setOnItemClickListener(new cl_listener());
            this.confs_contactlist.setOnItemLongClickListener(new contactLongClickListener());
        }
    }

    private void putHandler() {
        if (service != null) {
            hdl = new Handler(this);
            service.clHdl = hdl;
            service.contactListActivity = this;
        }
    }

    private void otherInit() {
        if (service != null) {
            service.isAnyChatOpened = false;
            updateBottomPanel();
        }
    }

    private void refreshContactlist() {
        if (confs_listAdp != null) {
            confs_listAdp.notifyDataSetChanged();
        }
        chats_listAdp.notifyDataSetChanged();
        listAdp.notifyDataSetChanged();
    }

    public void createContactlistFromProfiles() {
        ProfilesManager pm;
        if (service != null && (pm = service.profiles) != null) {
            if (listAdp != null) {
                listAdp.createFromProfileManager(pm);
            }
            if (chats_listAdp != null && PreferenceTable.ms_two_screens_mode) {
                chats_listAdp.createFromProfileManager(pm);
            }
            if (confs_listAdp == null || !PreferenceTable.ms_two_screens_mode) {
                return;
            }
            confs_listAdp.createFromProfileManager(pm);
        }
    }

    private static View[] buildProgress() {
        LinearLayout lay = new LinearLayout(resources.ctx);
        TextView label = new TextView(resources.ctx);
        PB progress = new PB(resources.ctx);
        lay.setOrientation(LinearLayout.HORIZONTAL);
        lay.setGravity(16);
        lay.addView(label);
        lay.addView(progress);
        label.setTextColor(-1);
        label.setTextSize(16.0f);
        label.setPadding(5, 5, 5, 5);
        label.setShadowLayer(1.0f, 1.0f, 1.0f, -16777216);
        return new View[]{lay, label, progress};
    }

    private void checkConnectionPanelVisibility() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean visible = false;
                int count = connectionStatusPanel.getChildCount();
                int i = 0;
                while (true) {
                    if (i >= count) {
                        break;
                    }
                    View v = connectionStatusPanel.getChildAt(i);
                    if (v.getVisibility() != View.VISIBLE) {
                        i++;
                    } else {
                        visible = true;
                        break;
                    }
                }

                final boolean finalVisible = visible;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectionStatusPanel.setVisibility(finalVisible ? View.VISIBLE : View.GONE);
                    }
                });
            }
        }).start();
    }

    private void updateBottomPanel() {

        if (service == null || service.profiles == null) {
            return;
        }

        int sizeInPixels = getResources().getDimensionPixelSize(R.dimen.bottom_panel_icon_size);
        int marginStart = getResources().getDimensionPixelSize(R.dimen.start_margin);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                sizeInPixels, sizeInPixels
        );
        params.setMargins(marginStart, 0, 0, 0);

        connectionStatusPanel.removeAllViews();
        profilesPanel.removeAllViews();
        ProfilesManager pm = service.profiles;
        Vector<IMProfile> list = pm.getProfiles();
        int sz = list.size();
        for (int i = 0; i < sz; i++) {
            final IMProfile improfile = list.get(i);
            if (improfile.enabled) {
                View[] views = buildProgress();
                final View cbLine = views[0];
                final TextView cbLabel = (TextView) views[1];
                final PB cbProgress = (PB) views[2];
                connectionStatusPanel.addView(cbLine);
                if (i != 0) {
                    ImageView divider = new ImageView(this);
                    divider.setImageDrawable(resources.bp_divider);
                    divider.setPadding(5, 0, 5, 0);
                    profilesPanel.addView(divider);
                }
                switch (improfile.profile_type) {
                    case 0:
                        final ICQProfile icqProfile = (ICQProfile) improfile;
                        final ImageView status2 = new ImageView(this);
                        status2.setClickable(true);

                        status2.setLayoutParams(params);

                        status2.setBackgroundDrawable(resources.getListSelector());
                        status2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                contextProfile = icqProfile;
                                UAdapter list2 = new UAdapter();
                                list2.setTextSize(18);
                                list2.setPadding(5);
                                list2.put(resources.profile_tools, resources.getString("s_more_tools"), 13);
                                list2.put(resources.away_text, resources.getString("s_status_text"), 12);
                                list2.put_separator();
                                list2.put(resources.online, resources.getString("s_status_online"), 0);
                                list2.put(resources.offline, resources.getString("s_status_offline"), 11);
                                list2.put_separator();
                                list2.put(resources.away, resources.getString("s_status_away"), 2);
                                list2.put(resources.oc, resources.getString("s_status_oc"), 3);
                                list2.put(resources.dnd, resources.getString("s_status_dnd"), 5);
                                list2.put(resources.na, resources.getString("s_status_na"), 4);
                                list2.put_separator();
                                list2.put(resources.chat, resources.getString("s_status_chat"), 1);
                                list2.put(resources.eat, resources.getString("s_status_eat"), 6);
                                list2.put(resources.evil, resources.getString("s_status_angry"), 7);
                                list2.put(resources.depress, resources.getString("s_status_depress"), 8);
                                list2.put(resources.home, resources.getString("s_status_at_home"), 9);
                                list2.put(resources.work, resources.getString("s_status_at_work"), 10);
                                last_quick_action = PopupBuilder.buildList(list2, view, icqProfile.nickname, RETURN_TO_CONTACTS, -2, new statusListListener(icqProfile));
                                last_quick_action.show();
                            }
                        });
                        profilesPanel.addView(status2);
                        final ImageView status3 = new ImageView(this);
                        status3.setClickable(true);
                        status3.setPadding(marginStart, 0, 0, 0);

                        status3.setLayoutParams(params);

                        status3.setBackgroundDrawable(resources.getListSelector());
                        status3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                XStatusAdapter list2 = new XStatusAdapter();
                                String str = icqProfile.nickname;
                                //noinspection UnnecessaryLocalVariable
                                final ICQProfile iCQProfile = icqProfile;
                                last_quick_action = PopupBuilder.buildGrid(list2, view, str, 6, -2, -2, new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        last_quick_action.dismiss();
                                        contextProfile = iCQProfile;
                                        selectedX = i;
                                        if (selectedX != 37) {
                                            removeDialog(13);
                                            showDialogA(13);
                                            xtitle.setText(contextProfile.getSavedXTitle(selectedX));
                                            xdesc.setText(contextProfile.getSavedXDesc(selectedX));
                                            return;
                                        }
                                        iCQProfile.xsts = -1;
                                        iCQProfile.saveXStatus();
                                        iCQProfile.updateUserInfo();
                                        iCQProfile.notifyStatusIcon();
                                    }
                                });
                                last_quick_action.show();
                            }
                        });
                        profilesPanel.addView(status3);
                        final ImageView status4 = new ImageView(this);
                        status4.setClickable(true);
                        status4.setLayoutParams(params);

                        status4.setBackgroundDrawable(resources.getListSelector());
                        status4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                UAdapter vlist = new UAdapter();
                                vlist.setTextSize(18);
                                vlist.setPadding(5);
                                vlist.put(resources.for_all, resources.getString("s_vis_1"), 0);
                                vlist.put(resources.for_vl, resources.getString("s_vis_2"), 1);
                                vlist.put(resources.for_all_e_invl, resources.getString("s_vis_3"), 2);
                                vlist.put(resources.for_cl, resources.getString("s_vis_4"), 3);
                                vlist.put(resources.ivn_for_all, resources.getString("s_vis_5"), 4);
                                switch (icqProfile.visibilityStatus) {
                                    case 1:
                                        vlist.setSelected(0);
                                        break;
                                    case 2:
                                        vlist.setSelected(4);
                                        break;
                                    case 3:
                                        vlist.setSelected(1);
                                        break;
                                    case 4:
                                        vlist.setSelected(2);
                                        break;
                                    case 5:
                                        vlist.setSelected(3);
                                        break;
                                }
                                String str = icqProfile.nickname;
                                //noinspection UnnecessaryLocalVariable
                                final ICQProfile iCQProfile = icqProfile;
                                last_quick_action = PopupBuilder.buildList(vlist, view, str, 450, -2, new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        last_quick_action.dismiss();
                                        contextProfile = iCQProfile;
                                        //noinspection ConstantConditions
                                        if (contextProfile != null) {
                                            switch (i) {
                                                case 0:
                                                    contextProfile.setVisibility(1);
                                                    break;
                                                case 1:
                                                    contextProfile.setVisibility(3);
                                                    break;
                                                case 2:
                                                    contextProfile.setVisibility(4);
                                                    break;
                                                case 3:
                                                    contextProfile.setVisibility(5);
                                                    break;
                                                case 4:
                                                    contextProfile.setVisibility(2);
                                                    break;
                                            }
                                            iCQProfile.notifyStatusIcon();
                                        }
                                    }
                                });
                                last_quick_action.show();
                            }
                        });
                        profilesPanel.addView(status4);
                        icqProfile.setNotifier(new IMProfile.BottomPanelNotifier() {
                            @Override
                            public void onStatusChanged() {
                                if (icqProfile.connected) {
                                    if (icqProfile.qip_status != null) {
                                        status2.setImageDrawable(qip_statuses.getIcon(icqProfile.qip_status));
                                    } else {
                                        status2.setImageDrawable(resources.getStatusIcon(icqProfile.status));
                                    }
                                } else if (icqProfile.connecting) {
                                    status2.setImageDrawable(resources.connecting);
                                } else {
                                    status2.setImageDrawable(resources.offline);
                                }
                                if (icqProfile.xsts != -1) {
                                    status3.setImageDrawable(xstatus.icons[icqProfile.xsts]);
                                } else {
                                    status3.setImageDrawable(resources.cross);
                                }
                                switch (icqProfile.visibilityStatus) {
                                    case 1:
                                        status4.setImageDrawable(resources.for_all);
                                        return;
                                    case 2:
                                        status4.setImageDrawable(resources.ivn_for_all);
                                        return;
                                    case 3:
                                        status4.setImageDrawable(resources.for_vl);
                                        return;
                                    case 4:
                                        status4.setImageDrawable(resources.for_all_e_invl);
                                        return;
                                    case 5:
                                        status4.setImageDrawable(resources.for_cl);
                                        return;
                                    default:
                                }
                            }

                            @Override
                            public void onConnectionStatusChanged() {
                                Log.e("PanelNotifier", "ICQ updated");
                                if (improfile.connection_status > 0 && improfile.connection_status < 100) {
                                    cbLine.setVisibility(View.VISIBLE);
                                    cbLabel.setText(improfile.ID);
                                    cbProgress.setProgress(improfile.connection_status);
                                } else {
                                    cbLine.setVisibility(View.GONE);
                                }
                                checkConnectionPanelVisibility();
                            }
                        });
                        break;
                    case 1:
                        final JProfile jprofile = (JProfile) improfile;
                        final ImageView status = new ImageView(this);
                        status.setClickable(true);

                        status.setLayoutParams(params);

                        status.setBackgroundDrawable(resources.getListSelector());
                        profilesPanel.addView(status);
                        switch (jprofile.type) {
                            case 0:
                                status.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        contextJProfile = jprofile;
                                        UAdapter vlist = new UAdapter();
                                        vlist.setTextSize(18);
                                        vlist.setPadding(5);
                                        if (jprofile.connected) {
                                            vlist.put(resources.jabber_conference, resources.getString("s_create_or_join_conference"), 4);
                                        }
                                        vlist.put(resources.profile_tools, resources.getString("s_more_tools"), 5);
                                        vlist.put_separator();
                                        vlist.put(resources.jabber_online, resources.getString("s_status_online"), 0);
                                        vlist.put(resources.jabber_offline, resources.getString("s_status_offline"), 1);
                                        vlist.put_separator();
                                        vlist.put(resources.jabber_chat, resources.getString("s_status_chat"), 6);
                                        vlist.put(resources.jabber_away, resources.getString("s_status_away"), 7);
                                        vlist.put(resources.jabber_dnd, resources.getString("s_status_dnd"), 8);
                                        vlist.put(resources.jabber_na, resources.getString("s_status_na"), 9);
                                        vlist.put_separator();
                                        vlist.put(resources.away_text, resources.getString("s_status_text"), 2);
                                        vlist.put(resources.jabber_priority, resources.getString("s_jabber_priority"), 10);
                                        if (jprofile.connected) {
                                            vlist.put(resources.bookmarks, resources.getString("s_bookmarks"), 13);
                                            vlist.put(resources.jabber_disco, resources.getString("s_service_discovery"), 12);
                                        }
                                        vlist.put(resources.jabber_xml_console, resources.getString("s_xml_console"), 11);
                                        String str = jprofile.nickname;
                                        //noinspection UnnecessaryLocalVariable
                                        final JProfile jProfile = jprofile;
                                        last_quick_action = PopupBuilder.buildList(vlist, view, str, 320, -2, new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                last_quick_action.dismiss();
                                                switch ((int) adapterView.getAdapter().getItemId(i)) {
                                                    case 0:
                                                        jProfile.setStatus(1);
                                                        if (jProfile.connected || jProfile.connecting) {
                                                            return;
                                                        }
                                                        jProfile.startConnectingChosed();
                                                        return;
                                                    case 1:
                                                        jProfile.disconnect();
                                                        return;
                                                    case 2:
                                                        removeDialog(35);
                                                        showDialogA(35);
                                                        return;
                                                    case 3:
                                                        removeDialog(40);
                                                        showDialogA(40);
                                                        return;
                                                    case 4:
                                                        removeDialog(42);
                                                        showDialogA(42);
                                                        return;
                                                    case 5:
                                                        if (!jProfile.connected) {
                                                            Toast.makeText(ContactListActivity.this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        removeDialog(33);
                                                        showDialogA(33);
                                                        return;
                                                    case 6:
                                                        jProfile.setStatus(0);
                                                        if (jProfile.connected || jProfile.connecting) {
                                                            return;
                                                        }
                                                        jProfile.startConnectingChosed();
                                                        return;
                                                    case 7:
                                                        jProfile.setStatus(2);
                                                        if (jProfile.connected || jProfile.connecting) {
                                                            return;
                                                        }
                                                        jProfile.startConnectingChosed();
                                                        return;
                                                    case 8:
                                                        jProfile.setStatus(3);
                                                        if (jProfile.connected || jProfile.connecting) {
                                                            return;
                                                        }
                                                        jProfile.startConnectingChosed();
                                                        return;
                                                    case 9:
                                                        jProfile.setStatus(4);
                                                        if (jProfile.connected || jProfile.connecting) {
                                                            return;
                                                        }
                                                        jProfile.startConnectingChosed();
                                                        return;
                                                    case 10:
                                                        removeDialog(47);
                                                        showDialogA(47);
                                                        return;
                                                    case 11:
                                                        XMLConsoleActivity.profile = contextJProfile;
                                                        Intent i2 = new Intent(ContactListActivity.this, XMLConsoleActivity.class);
                                                        startActivity(i2);
                                                        return;
                                                    case 12:
                                                        DiscoActivity.putSources(contextJProfile.host, contextJProfile);
                                                        Intent disco = new Intent(ContactListActivity.this, DiscoActivity.class);
                                                        startActivity(disco);
                                                        return;
                                                    case 13:
                                                        Intent bookmarks = new Intent(ContactListActivity.this, BookmarksActivity.class);
                                                        BookmarksActivity.PROFILE = contextJProfile;
                                                        startActivity(bookmarks);
                                                        return;
                                                    default:
                                                }
                                            }
                                        });
                                        last_quick_action.show();
                                    }
                                });
                                break;
                            case 1:
                                status.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        contextJProfile = jprofile;
                                        UAdapter vlist = new UAdapter();
                                        vlist.setTextSize(18);
                                        vlist.setPadding(5);
                                        vlist.put(resources.profile_tools, resources.getString("s_more_tools"), 2);
                                        vlist.put_separator();
                                        vlist.put(resources.vk_online, resources.getString("s_status_online"), 0);
                                        vlist.put(resources.vk_offline, resources.getString("s_status_offline"), 1);
                                        vlist.put_separator();
                                        vlist.put(resources.jabber_xml_console, resources.getString("s_xml_console"), 3);
                                        //noinspection UnnecessaryLocalVariable
                                        final JProfile jProfile = jprofile;
                                        last_quick_action = PopupBuilder.buildList(vlist, view, jprofile.ID + "@" + jprofile.host, 450, -2, new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                last_quick_action.dismiss();
                                                switch ((int) adapterView.getAdapter().getItemId(i)) {
                                                    case 0:
                                                        jProfile.setStatus(1);
                                                        jProfile.startConnecting();
                                                        return;
                                                    case 1:
                                                        jProfile.disconnect();
                                                        return;
                                                    case 2:
                                                        if (!jProfile.connected) {
                                                            Toast.makeText(ContactListActivity.this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        removeDialog(33);
                                                        showDialogA(33);
                                                        return;
                                                    case 3:
                                                        XMLConsoleActivity.profile = contextJProfile;
                                                        Intent i2 = new Intent(ContactListActivity.this, XMLConsoleActivity.class);
                                                        startActivity(i2);
                                                        return;
                                                    default:
                                                }
                                            }
                                        });
                                        last_quick_action.show();
                                    }
                                });
                                break;
                            case 2:
                                status.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        contextJProfile = jprofile;
                                        UAdapter vlist = new UAdapter();
                                        vlist.setTextSize(18);
                                        vlist.setPadding(5);
                                        if (jprofile.connected) {
                                            vlist.put(resources.jabber_conference, resources.getString("s_create_or_join_conference"), 6);
                                        }
                                        vlist.put(resources.profile_tools, resources.getString("s_more_tools"), 2);
                                        vlist.put_separator();
                                        vlist.put(resources.yandex_online, resources.getString("s_status_online"), 0);
                                        vlist.put(resources.yandex_offline, resources.getString("s_status_offline"), 1);
                                        vlist.put_separator();
                                        if (jprofile.connected) {
                                            vlist.put(resources.bookmarks, resources.getString("s_bookmarks"), 5);
                                            vlist.put(resources.jabber_disco, resources.getString("s_service_discovery"), 4);
                                        }
                                        vlist.put(resources.jabber_xml_console, resources.getString("s_xml_console"), 3);
                                        String str = jprofile.ID;
                                        //noinspection UnnecessaryLocalVariable
                                        final JProfile jProfile = jprofile;
                                        last_quick_action = PopupBuilder.buildList(vlist, view, str, 450, -2, new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                last_quick_action.dismiss();
                                                switch ((int) adapterView.getAdapter().getItemId(i)) {
                                                    case 0:
                                                        jProfile.setStatus(1);
                                                        jProfile.startConnecting();
                                                        return;
                                                    case 1:
                                                        jProfile.disconnect();
                                                        return;
                                                    case 2:
                                                        if (!jProfile.connected) {
                                                            Toast.makeText(ContactListActivity.this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        removeDialog(33);
                                                        showDialogA(33);
                                                        return;
                                                    case 3:
                                                        XMLConsoleActivity.profile = contextJProfile;
                                                        Intent i2 = new Intent(ContactListActivity.this, XMLConsoleActivity.class);
                                                        startActivity(i2);
                                                        return;
                                                    case 4:
                                                        DiscoActivity.putSources(contextJProfile.host, contextJProfile);
                                                        Intent disco = new Intent(ContactListActivity.this, DiscoActivity.class);
                                                        startActivity(disco);
                                                        return;
                                                    case 5:
                                                        Intent bookmarks = new Intent(ContactListActivity.this, BookmarksActivity.class);
                                                        BookmarksActivity.PROFILE = contextJProfile;
                                                        startActivity(bookmarks);
                                                        return;
                                                    case 6:
                                                        removeDialog(42);
                                                        showDialogA(42);
                                                        return;
                                                    default:
                                                }
                                            }
                                        });
                                        last_quick_action.show();
                                    }
                                });
                                break;
                            case 3:
                                status.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        contextJProfile = jprofile;
                                        final UAdapter vlist = new UAdapter();
                                        vlist.setTextSize(18);
                                        vlist.setPadding(5);
                                        if (jprofile.connected) {
                                            vlist.put(resources.jabber_conference, resources.getString("s_create_or_join_conference"), 4);
                                        }
                                        if (jprofile.connected) {
                                            vlist.put(resources.google_mail, resources.getString("s_check_gmail"), 2);
                                        }
                                        vlist.put(resources.profile_tools, resources.getString("s_more_tools"), 3);
                                        vlist.put_separator();
                                        vlist.put(resources.gtalk_online, resources.getString("s_status_online"), 0);
                                        vlist.put(resources.gtalk_offline, resources.getString("s_status_offline"), 1);
                                        vlist.put_separator();
                                        if (jprofile.connected) {
                                            vlist.put(resources.bookmarks, resources.getString("s_bookmarks"), 7);
                                            vlist.put(resources.jabber_disco, resources.getString("s_service_discovery"), 6);
                                        }
                                        vlist.put(resources.jabber_xml_console, resources.getString("s_xml_console"), 5);
                                        String str = jprofile.ID;
                                        //noinspection UnnecessaryLocalVariable
                                        final JProfile jProfile = jprofile;
                                        last_quick_action = PopupBuilder.buildList(vlist, view, str, 450, -2, new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                last_quick_action.dismiss();
                                                switch ((int) vlist.getItemId(i)) {
                                                    case 0:
                                                        jProfile.setStatus(1);
                                                        jProfile.startConnecting();
                                                        return;
                                                    case 1:
                                                        jProfile.disconnect();
                                                        return;
                                                    case 2:
                                                        GMailActivity.profile = jProfile;
                                                        Intent gmail = new Intent(ContactListActivity.this, GMailActivity.class);
                                                        startActivity(gmail);
                                                        return;
                                                    case 3:
                                                        if (!jProfile.connected) {
                                                            Toast.makeText(ContactListActivity.this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        removeDialog(33);
                                                        showDialogA(33);
                                                        return;
                                                    case 4:
                                                        removeDialog(42);
                                                        showDialogA(42);
                                                        return;
                                                    case 5:
                                                        XMLConsoleActivity.profile = contextJProfile;
                                                        Intent i2 = new Intent(ContactListActivity.this, XMLConsoleActivity.class);
                                                        startActivity(i2);
                                                        return;
                                                    case 6:
                                                        DiscoActivity.putSources(contextJProfile.host, contextJProfile);
                                                        Intent disco = new Intent(ContactListActivity.this, DiscoActivity.class);
                                                        startActivity(disco);
                                                        return;
                                                    case 7:
                                                        Intent bookmarks = new Intent(ContactListActivity.this, BookmarksActivity.class);
                                                        BookmarksActivity.PROFILE = contextJProfile;
                                                        startActivity(bookmarks);
                                                        return;
                                                    default:
                                                }
                                            }
                                        });
                                        last_quick_action.show();
                                    }
                                });
                                break;
                            case 4:
                                status.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        contextJProfile = jprofile;
                                        final UAdapter vlist = new UAdapter();
                                        vlist.setTextSize(18);
                                        vlist.setPadding(5);
                                        if (jprofile.connected) {
                                            vlist.put(resources.jabber_conference, resources.getString("s_create_or_join_conference"), 3);
                                        }
                                        vlist.put(resources.profile_tools, resources.getString("s_more_tools"), 2);
                                        vlist.put_separator();
                                        vlist.put(resources.qip_online, resources.getString("s_status_online"), 0);
                                        vlist.put(resources.qip_offline, resources.getString("s_status_offline"), 1);
                                        vlist.put_separator();
                                        if (jprofile.connected) {
                                            vlist.put(resources.bookmarks, resources.getString("s_bookmarks"), 6);
                                            vlist.put(resources.jabber_disco, resources.getString("s_service_discovery"), 5);
                                        }
                                        vlist.put(resources.jabber_xml_console, resources.getString("s_xml_console"), 4);
                                        String str = jprofile.ID;
                                        //noinspection UnnecessaryLocalVariable
                                        final JProfile jProfile = jprofile;
                                        last_quick_action = PopupBuilder.buildList(vlist, view, str, 450, -2, new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                last_quick_action.dismiss();
                                                switch ((int) vlist.getItemId(i)) {
                                                    case 0:
                                                        jProfile.setStatus(1);
                                                        jProfile.startConnecting();
                                                        return;
                                                    case 1:
                                                        jProfile.disconnect();
                                                        return;
                                                    case 2:
                                                        if (!jProfile.connected) {
                                                            Toast.makeText(ContactListActivity.this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        removeDialog(33);
                                                        showDialogA(33);
                                                        return;
                                                    case 3:
                                                        removeDialog(42);
                                                        showDialogA(42);
                                                        return;
                                                    case 4:
                                                        XMLConsoleActivity.profile = contextJProfile;
                                                        Intent i2 = new Intent(ContactListActivity.this, XMLConsoleActivity.class);
                                                        startActivity(i2);
                                                        return;
                                                    case 5:
                                                        DiscoActivity.putSources(contextJProfile.host, contextJProfile);
                                                        Intent disco = new Intent(ContactListActivity.this, DiscoActivity.class);
                                                        startActivity(disco);
                                                        return;
                                                    case 6:
                                                        Intent bookmarks = new Intent(ContactListActivity.this, BookmarksActivity.class);
                                                        BookmarksActivity.PROFILE = contextJProfile;
                                                        startActivity(bookmarks);
                                                        return;
                                                    default:
                                                }
                                            }
                                        });
                                        last_quick_action.show();
                                    }
                                });
                                break;
                        }
                        jprofile.setNotifier(new IMProfile.BottomPanelNotifier() {
                            @Override
                            public void onStatusChanged() {
                                switch (jprofile.type) {
                                    case 0:
                                        if (jprofile.connecting) {
                                            status.setImageDrawable(resources.jabber_connecting);
                                            return;
                                        } else if (jprofile.connected) {
                                            switch (jprofile.status) {
                                                case 0:
                                                    status.setImageDrawable(resources.jabber_chat);
                                                    return;
                                                case 1:
                                                    status.setImageDrawable(resources.jabber_online);
                                                    return;
                                                case 2:
                                                    status.setImageDrawable(resources.jabber_away);
                                                    return;
                                                case 3:
                                                    status.setImageDrawable(resources.jabber_dnd);
                                                    return;
                                                case 4:
                                                    status.setImageDrawable(resources.jabber_na);
                                                    return;
                                                default:
                                                    return;
                                            }
                                        } else {
                                            status.setImageDrawable(resources.jabber_offline);
                                            return;
                                        }
                                    case 1:
                                        if (jprofile.connecting) {
                                            status.setImageDrawable(resources.vk_connecting);
                                            return;
                                        } else if (jprofile.connected) {
                                            status.setImageDrawable(resources.vk_online);
                                            return;
                                        } else {
                                            status.setImageDrawable(resources.vk_offline);
                                            return;
                                        }
                                    case 2:
                                        if (jprofile.connecting) {
                                            status.setImageDrawable(resources.yandex_connecting);
                                            return;
                                        } else if (jprofile.connected) {
                                            status.setImageDrawable(resources.yandex_online);
                                            return;
                                        } else {
                                            status.setImageDrawable(resources.yandex_offline);
                                            return;
                                        }
                                    case 3:
                                        if (jprofile.connecting) {
                                            status.setImageDrawable(resources.gtalk_connecting);
                                            return;
                                        } else if (jprofile.connected) {
                                            status.setImageDrawable(resources.gtalk_online);
                                            return;
                                        } else {
                                            status.setImageDrawable(resources.gtalk_offline);
                                            return;
                                        }
                                    case 4:
                                        if (jprofile.connecting) {
                                            status.setImageDrawable(resources.qip_connecting);
                                            return;
                                        } else if (jprofile.connected) {
                                            status.setImageDrawable(resources.qip_online);
                                            return;
                                        } else {
                                            status.setImageDrawable(resources.qip_offline);
                                            return;
                                        }
                                    default:
                                }
                            }

                            @Override
                            public void onConnectionStatusChanged() {
                                Log.e("PanelNotifier", "JABBER updated");
                                if (improfile.connection_status > 0 && improfile.connection_status < 100) {
                                    cbLine.setVisibility(View.VISIBLE);
                                    cbLabel.setText(improfile.ID);
                                    cbProgress.setProgress(improfile.connection_status);
                                } else {
                                    cbLine.setVisibility(View.GONE);
                                }
                                checkConnectionPanelVisibility();
                            }
                        });
                        break;
                    case 2:
                        final MMPProfile m_profile = (MMPProfile) improfile;
                        final ImageView status1 = new ImageView(this);
                        status1.setClickable(true);
                        status1.setPadding(8, 7, 8, 7);
                        status1.setBackgroundDrawable(resources.getListSelector());
                        m_profile.setNotifier(new IMProfile.BottomPanelNotifier() {
                            @Override
                            public void onStatusChanged() {
                                if (m_profile.connected) {
                                    switch (m_profile.getTranslatedStatus()) {
                                        case 2:
                                            status1.setImageDrawable(resources.mrim_away);
                                            return;
                                        case 3:
                                        case 4:
                                        case 5:
                                            status1.setImageDrawable(resources.mrim_dnd);
                                            return;
                                        case 6:
                                            status1.setImageDrawable(resources.mrim_oc);
                                            return;
                                        case 7:
                                            status1.setImageDrawable(resources.mrim_na);
                                            return;
                                        case 8:
                                            status1.setImageDrawable(resources.mrim_lunch);
                                            return;
                                        case 9:
                                            status1.setImageDrawable(resources.mrim_work);
                                            return;
                                        case 10:
                                            status1.setImageDrawable(resources.mrim_home);
                                            return;
                                        case 11:
                                            status1.setImageDrawable(resources.mrim_depress);
                                            return;
                                        case 12:
                                            status1.setImageDrawable(resources.mrim_angry);
                                            return;
                                        case 13:
                                            status1.setImageDrawable(resources.mrim_chat);
                                        default:
                                            status1.setImageDrawable(resources.mrim_online);
                                    }
                                } else if (m_profile.connecting) {
                                    status1.setImageDrawable(resources.mrim_connecting);
                                } else {
                                    status1.setImageDrawable(resources.mrim_offline);
                                }
                            }

                            @Override
                            public void onConnectionStatusChanged() {
                                Log.e("PanelNotifier", "MMP updated");
                                if (improfile.connection_status > 0 && improfile.connection_status < 100) {
                                    cbLine.setVisibility(View.VISIBLE);
                                    cbLabel.setText(improfile.ID);
                                    cbProgress.setProgress(improfile.connection_status);
                                } else {
                                    cbLine.setVisibility(View.GONE);
                                }
                                checkConnectionPanelVisibility();
                            }
                        });
                        status1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                contextMrimProfile = m_profile;
                                UAdapter list2 = new UAdapter();
                                list2.setTextSize(18);
                                list2.setPadding(5);
                                list2.put(resources.profile_tools, resources.getString("s_more_tools"), 3);
                                if (m_profile.connected) {
                                    list2.put(resources.sms, resources.getString("s_send_sms"), 2);
                                }
                                list2.put_separator();
                                list2.put(resources.mrim_online, resources.getString("s_status_online"), 0);
                                list2.put(resources.mrim_offline, resources.getString("s_status_offline"), 1);
                                list2.put_separator();
                                list2.put(resources.mrim_away, resources.getString("s_status_away"), 4);
                                list2.put(resources.mrim_oc, resources.getString("s_status_oc"), 5);
                                list2.put(resources.mrim_dnd, resources.getString("s_status_dnd"), 6);
                                list2.put(resources.mrim_na, resources.getString("s_status_na"), 7);
                                list2.put_separator();
                                list2.put(resources.mrim_chat, resources.getString("s_status_chat"), 8);
                                list2.put(resources.mrim_lunch, resources.getString("s_status_eat"), 9);
                                list2.put(resources.mrim_angry, resources.getString("s_status_angry"), 10);
                                list2.put(resources.mrim_depress, resources.getString("s_status_depress"), 11);
                                list2.put(resources.mrim_home, resources.getString("s_status_at_home"), 12);
                                list2.put(resources.mrim_work, resources.getString("s_status_at_work"), 13);
                                String str = m_profile.ID;
                                //noinspection UnnecessaryLocalVariable
                                final MMPProfile mMPProfile = m_profile;
                                last_quick_action = PopupBuilder.buildList(list2, view, str, 320, -2, new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        last_quick_action.dismiss();
                                        switch ((int) adapterView.getAdapter().getItemId(i)) {
                                            case 0:
                                                mMPProfile.setStatus(1);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 1:
                                                mMPProfile.disconnect();
                                                return;
                                            case 2:
                                                removeDialog(38);
                                                showDialogA(38);
                                                return;
                                            case 3:
                                                if (!mMPProfile.connected) {
                                                    Toast.makeText(ContactListActivity.this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                                removeDialog(45);
                                                showDialogA(45);
                                                return;
                                            case 4:
                                                mMPProfile.setStatus(2);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 5:
                                                mMPProfile.setStatus(6);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 6:
                                                mMPProfile.setStatus(5);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 7:
                                                mMPProfile.setStatus(7);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 8:
                                                mMPProfile.setStatus(13);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 9:
                                                mMPProfile.setStatus(8);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 10:
                                                mMPProfile.setStatus(12);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 11:
                                                mMPProfile.setStatus(11);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 12:
                                                mMPProfile.setStatus(10);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            case 13:
                                                mMPProfile.setStatus(9);
                                                if (!mMPProfile.connected && !mMPProfile.connecting) {
                                                    mMPProfile.startConnectingChosed();
                                                    return;
                                                }
                                                return;
                                            default:
                                        }
                                    }
                                });
                                last_quick_action.show();
                            }
                        });
                        profilesPanel.addView(status1);
                        break;
                }
            }
        }
    }

    public class ToolsPanelListener implements View.OnClickListener {

        @SuppressLint("ApplySharedPref")
        @Override
        public void onClick(View arg0) {
            if (arg0 != toggle_offline) {
                if (arg0 != toggle_vibro) {
                    if (arg0 == toggle_sound) {
                        //noinspection deprecation
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ContactListActivity.this);
                        boolean enableSound = sp.getBoolean("ms_sounds", true);
                        if (enableSound) {
                            sp.edit().putBoolean("ms_sounds", false).commit();
                            toggle_sound.setImageDrawable(resources.toggle_sound);
                        } else {
                            sp.edit().putBoolean("ms_sounds", true).commit();
                            toggle_sound.setImageDrawable(resources.toggle_sound_a);
                        }
                        service.doVibrate(20L);
                        return;
                    }
                    return;
                }
                //noinspection deprecation
                SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(ContactListActivity.this);
                boolean enableVibro = sp2.getBoolean("ms_vibro", true);
                if (enableVibro) {
                    sp2.edit().putBoolean("ms_vibro", false).commit();
                    toggle_vibro.setImageDrawable(resources.toggle_vibro);
                } else {
                    sp2.edit().putBoolean("ms_vibro", true).commit();
                    toggle_vibro.setImageDrawable(resources.toggle_vibro_a);
                }
                service.doVibrate(20L);
                return;
            }
            //noinspection deprecation
            SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(ContactListActivity.this);
            boolean hideOffline = sp3.getBoolean("ms_offline", true);
            if (hideOffline) {
                sp3.edit().putBoolean("ms_offline", false).commit();
                toggle_offline.setImageDrawable(resources.toggle_offline);
            } else {
                sp3.edit().putBoolean("ms_offline", true).commit();
                toggle_offline.setImageDrawable(resources.toggle_offline_a);
            }
            service.doVibrate(20L);
        }
    }

    public void intentProfilesManager() {
        Intent pa = new Intent();
        pa.setClass(this, ProfilesActivity.class);
        startActivity(pa);
    }

    private class ContextConfirmListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            last_shown_notify_dialog.dismiss();
            switch (LastContextAction) {
                case 0:
                    if (contextContact != null) {
                        contextContact.profile.doDeleteContact(contextContact);
                        return;
                    } else if (contextJContact != null) {
                        if (contextJContact.conf_pm) {
                            contextJContact.profile.doLocalDeleteContact(contextJContact);
                        } else {
                            contextJContact.profile.doDeleteContact(contextJContact);
                        }
                        return;
                    } else {
                        return;
                    }
                case 1:
                    contextContact.profile.sendDeleteYourself(contextContact.ID);
                    return;
                case 2:
                    exiting = true;
                    finish();
                    return;
                case 10:
                    contextGroup.profile.doDeleteGroup(contextGroup);
                    return;
                default:
            }
        }
    }

    private class ContactContextMenuListener implements AdapterView.OnItemClickListener {
        private final UAdapter adp;

        public ContactContextMenuListener(UAdapter adpParam) {
            this.adp = adpParam;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            removeDialog(7);
            int idA = (int) this.adp.getItemId(arg2);
            switch (idA) {
                case 0:
                    if (contextContact != null || contextJContact == null || contextMMPContact != null) {
                        if (contextContact != null) {
                            contextContact.profile.closeChat(contextContact);
                            return;
                        } else //noinspection ConstantConditions
                            if (contextContact == null && contextJContact == null && contextMMPContact != null) {
                                contextMMPContact.profile.closeChat(contextMMPContact);
                                return;
                            } else {
                                return;
                            }
                    }
                    contextJContact.profile.closeChat(contextJContact);
                    return;
                case 1:
                case 2:
                    removeDialog(14);
                    showDialogA(14);
                    return;
                case 3:
                    if (contextContact != null) {
                        contextContact.profile.doRequestContactInfoForDisplay(contextContact.ID);
                        return;
                    }
                    return;
                case 4:
                    if (contextContact != null && contextContact.added && contextContact.authorized) {
                        client_info = resources.getString("s_icq_info_nick") + ":\n";
                        client_info = client_info + contextContact.name + "\n\n";
                        client_info = client_info + "UIN:\n" + contextContact.ID + "\n\n";
                        client_info = client_info + resources.getString("s_signon_time") + ":\n";
                        Date dt = new Date(contextContact.signOnTime);
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
                        client_info = client_info + sdf.format(dt) + "\n";
                        client_info = client_info + resources.getString("s_online_time") + ":\n";
                        int onlineTime = (int) Math.abs((System.currentTimeMillis() - contextContact.signOnTime) / 1000);
                        client_info = client_info + utilities.longitudeToString(onlineTime) + "\n";
                        client_info = client_info + resources.getString("s_client_label") + ": ";
                        if (contextContact.client.name != null) {
                            client_info = client_info + contextContact.client.name + "\n\n";
                        } else {
                            client_info = client_info + "-\n\n";
                        }
                        client_info = client_info + resources.getString("s_protocol_version") + ": ";
                        client_info = client_info + contextContact.protoVersion + "\n\n";
                        client_info = client_info + "Futures: " + utilities.to4ByteHEX(contextContact.dc_info.futures) + "\n";
                        client_info = client_info + "DC_TYPE: " + contextContact.dc_info.getDCType() + "\n";
                        client_info = client_info + "DC1: " + utilities.to4ByteHEX(contextContact.dc_info.dc1) + "\n";
                        client_info = client_info + "DC2: " + utilities.to4ByteHEX(contextContact.dc_info.dc2) + "\n";
                        client_info = client_info + "DC3: " + utilities.to4ByteHEX(contextContact.dc_info.dc3) + "\n";
                        client_info = client_info + "\n[" + resources.getString("s_capabilities") + "]\n";
                        int sz = contextContact.capabilities.list.size();
                        StringBuilder caps = new StringBuilder();
                        for (int j = 0; j < sz; j++) {
                            String raw = contextContact.capabilities.list.get(j);
                            caps.append(IcqCapsBase.translateGuid(raw)).append("\n");
                        }
                        client_info = client_info + caps;
                        removeDialog(5);
                        showDialogA(5);
                        return;
                    }
                    return;
                case 5:
                    if (contextContact != null) {
                        removeDialog(15);
                        showDialogA(15);
                        return;
                    }
                    return;
                case 6:
                    if (contextContact != null) {
                        ClipboardManager cb = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                        //noinspection deprecation
                        cb.setText(contextContact.ID);
                        Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    return;
                case 7:
                    if (contextContact != null) {
                        ClipboardManager cbA = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                        //noinspection deprecation
                        cbA.setText(contextContact.name);
                        Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    return;
                case 8:
                    if (contextContact != null) {
                        tempContactForAddingDialog = contextContact;
                        removeDialog(6);
                        showDialogA(6);
                        return;
                    }
                    return;
                case 9:
                    if (contextContact != null) {
                        contextContact.profile.doRequestContactInfoForNickRefresh(contextContact.ID);
                        return;
                    }
                    return;
                case 10:
                    if (contextContact != null) {
                        removeDialog(11);
                        showDialogA(11);
                        return;
                    }
                    return;
                case 11:
                    if (contextContact != null) {
                        removeDialog(12);
                        showDialogA(12);
                        return;
                    }
                    return;
                case 12:
                    if (contextContact != null) {
                        File cache = new File(resources.dataPath + contextContact.profile.ID + "/history/" + contextContact.ID + ".cache");
                        //noinspection ResultOfMethodCallIgnored
                        cache.delete();
                        File history = new File(resources.dataPath + contextContact.profile.ID + "/history/" + contextContact.ID + ".hst");
                        //noinspection ResultOfMethodCallIgnored
                        history.delete();
                        contextContact.history.clear();
                        Toast.makeText(service, resources.getString("s_history_cleared"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    return;
                case 13:
                    if (contextContact != null) {
                        contextContact.profile.sendXtrazRequest(contextContact.ID, 0);
                        return;
                    }
                    return;
                case 14:
                    Vector<IMProfile> profiles = service.profiles.getProfiles();
                    for (IMProfile profile : profiles) {
                        switch (profile.profile_type) {
                            case 0:
                            case 2:
                            case 1:
                                profile.closeAllChats();
                                break;
                        }
                    }
                    return;
                case 15:
                    contextContact.getAvatar(contextContact, service);
                    return;
                case 16:
                    removeDialog(34);
                    showDialogA(34);
                    return;
                case 17:
                    if (contextJContact != null) {
                        contextJContact.profile.doRequestAuth(contextJContact);
                        return;
                    }
                    return;
                case 18:
                    if (contextJContact != null) {
                        contextJContact.showInfo();
                        return;
                    }
                    return;
                case 19:
                    if (contextJContact != null) {
                        contextJContact.getAvatar();
                        return;
                    }
                    return;
                case 20:
                    ClipboardManager cb2 = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                    //noinspection deprecation
                    cb2.setText(contextJContact.ID);
                    Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                    return;
                case 21:
                    ClipboardManager cb3 = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                    //noinspection deprecation
                    cb3.setText(contextJContact.name);
                    Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                    return;
                case 22:
                    if (contextMMPContact != null) {
                        contextMMPContact.getAvatar(contextMMPContact, service);
                        return;
                    }
                    return;
                case 23:
                    ClipboardManager cb4 = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                    //noinspection deprecation
                    cb4.setText(contextMMPContact.ID);
                    Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                    return;
                case 24:
                    ClipboardManager cb5 = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                    //noinspection deprecation
                    cb5.setText(contextMMPContact.name);
                    Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                    return;
                case 25:
                    if (contextJContact != null) {
                        File cache1 = new File(resources.dataPath + contextJContact.profile.ID + "@" + contextJContact.profile.host + "/history/" + contextJContact.ID + ".cache");
                        //noinspection ResultOfMethodCallIgnored
                        cache1.delete();
                        File history1 = new File(resources.dataPath + contextJContact.profile.ID + "@" + contextJContact.profile.host + "/history/" + contextJContact.ID + ".hst");
                        //noinspection ResultOfMethodCallIgnored
                        history1.delete();
                        contextJContact.history.clear();
                        Toast.makeText(service, resources.getString("s_history_cleared"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    return;
                case 26:
                    if (contextMMPContact != null) {
                        File cache2 = new File(resources.dataPath + contextMMPContact.profile.ID + "/history/" + contextMMPContact.ID + ".cache");
                        //noinspection ResultOfMethodCallIgnored
                        cache2.delete();
                        File history2 = new File(resources.dataPath + contextMMPContact.profile.ID + "/history/" + contextMMPContact.ID + ".hst");
                        //noinspection ResultOfMethodCallIgnored
                        history2.delete();
                        contextMMPContact.history.clear();
                        Toast.makeText(service, resources.getString("s_history_cleared"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    return;
                case 27:
                    if (contextJContact != null) {
                        removeDialog(46);
                        showDialogA(46);
                        return;
                    }
                    return;
                case 28:
                    if (contextJContact != null) {
                        removeDialog(48);
                        showDialogA(48);
                        return;
                    }
                    return;
                case 29:
                    final Dialog load_progress = DialogBuilder.createProgress(
                            ContactListActivity.this,
                            Locale.getString("s_getting_commands"),
                            true
                    );
                    load_progress.show();
                    ru.ivansuper.jasmin.jabber.commands.Callback callback = new ru.ivansuper.jasmin.jabber.commands.Callback() {
                        @Override
                        public void onListLoaded(final Vector<CommandItem> list) {
                            load_progress.dismiss();
                            if (list.isEmpty()) {
                                service.showMessageInContactList(Locale.getString("s_information"), Locale.getString("s_no_commands"));
                                return;
                            }
                            UAdapter adp = new UAdapter();
                            adp.setMode(2);
                            adp.setPadding(14);
                            for (int i = 0; i < list.size(); i++) {
                                CommandItem item = list.get(i);
                                adp.put(item.name, i);
                            }
                            Dialog commands = DialogBuilder.createWithNoHeader(
                                    ContactListActivity.this,
                                    adp,
                                    0,
                                    new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            CommandItem item2 = list.get(i);
                                            contextJContact.profile.executeCommand(item2.jid, item2.node);
                                        }
                                    }
                            );
                            commands.show();
                        }
                    };
                    contextJContact.profile.getCommandList(contextJContact.ID, callback);
                    return;
                case 30:
                    contextJContact.updateNick();
                    return;
                case 31:
                    contextJContact.profile.sendPresence(contextJContact.ID, true);
                    return;
                case 32:
                    contextJContact.profile.sendPresence(contextJContact.ID, false);
                default:
            }
        }
    }

    public class cl_listener implements MultiColumnList.OnItemClickListener {

        @SuppressLint("ApplySharedPref")
        @Override
        public void onItemClick(MultiColumnList arg0, View view, int arg2, long arg3) {
            if (CURRENT_IS_CONTACTS) {
                ContactsAdapter adp = (ContactsAdapter) arg0.getAdapter();
                ContactlistItem item = adp.getItem(arg2);
                if (item.itemType == ContactlistItem.GROUP) {
                    ICQGroup grp = (ICQGroup) item;
                    grp.opened = !grp.opened;
                    sp.edit().putBoolean("g" + grp.id, grp.opened).commit();
                    createContactlistFromProfiles();
                } else if (item.itemType == ContactlistItem.CONTACT) {
                    ICQContact contact = (ICQContact) item;
                    service.currentChatContact = contact;
                    service.currentChatProfile = contact.profile;
                    startICQChatActivity(contact);
                } else if (item.itemType == ContactlistItem.JABBER_CONTACT) {
                    JContact jcontact = (JContact) item;
                    service.currentChatContact = jcontact;
                    service.currentChatProfile = jcontact.profile;
                    startJChatActivity(jcontact);
                } else if (item.itemType == ContactlistItem.JABBER_CONFERENCE) {
                    ConferenceItem conference = (ConferenceItem) item;
                    service.currentChatContact = conference;
                    service.currentChatProfile = conference.conference.profile;
                    startJConferenceActivity(conference);
                } else if (item.itemType == ContactlistItem.MMP_CONTACT) {
                    MMPContact mmpcontact = (MMPContact) item;
                    service.currentChatContact = mmpcontact;
                    service.currentChatProfile = mmpcontact.profile;
                    startMMPChatActivity(mmpcontact);
                } else if (item.itemType == ContactlistItem.MMP_GROUP) {
                    MMPGroup mmpgroup = (MMPGroup) item;
                    mmpgroup.opened = !mmpgroup.opened;
                    sp.edit().putBoolean("mmpg" + mmpgroup.id, mmpgroup.opened).commit();
                    createContactlistFromProfiles();
                } else if (item.itemType == ContactlistItem.MMP_PROFILE_GROUP) {
                    MMPGroup mmpgroup2 = (MMPGroup) item;
                    mmpgroup2.profile.openedInContactList = !mmpgroup2.profile.openedInContactList;
                    sp.edit().putBoolean("mmpg" + mmpgroup2.profile.ID, mmpgroup2.profile.openedInContactList).commit();
                    createContactlistFromProfiles();
                } else if (item.itemType == ContactlistItem.JABBER_GROUP) {
                    JGroup jgroup = (JGroup) item;
                    jgroup.opened = !jgroup.opened;
                    sp.edit().putBoolean("jg" + jgroup.name, jgroup.opened).commit();
                    createContactlistFromProfiles();
                } else if (item.itemType == ContactlistItem.PROFILE_GROUP) {
                    ICQGroup grp2 = (ICQGroup) item;
                    grp2.profile.openedInContactList = !grp2.profile.openedInContactList;
                    sp.edit().putBoolean("pg" + grp2.profile.ID, grp2.profile.openedInContactList).commit();
                    createContactlistFromProfiles();
                } else if (item.itemType == ContactlistItem.JABBER_PROFILE_GROUP) {
                    ICQGroup grp3 = (ICQGroup) item;
                    grp3.jprofile.openedInContactList = !grp3.jprofile.openedInContactList;
                    sp.edit().putBoolean("pg" + grp3.jprofile.ID, grp3.jprofile.openedInContactList).commit();
                    createContactlistFromProfiles();
                }
            }
        }
    }

    private void updateUI() {
        if (resources.IT_IS_TABLET) {
            if (this.ANY_CHAT_ACTIVE) {
                findViewById(R.id.chat_fragment).setVisibility(View.VISIBLE);
                if (this.IT_IS_PORTRAIT) {
                    findViewById(R.id.contactlist_list_chat_separator).setVisibility(View.GONE);
                    findViewById(R.id.contacts_fragment).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.contactlist_list_chat_separator).setVisibility(View.VISIBLE);
                    findViewById(R.id.contacts_fragment).setVisibility(View.VISIBLE);
                }
            } else {
                findViewById(R.id.chat_fragment).setVisibility(View.GONE);
                findViewById(R.id.contactlist_list_chat_separator).setVisibility(View.GONE);
                findViewById(R.id.contacts_fragment).setVisibility(View.VISIBLE);
            }
            localOnResume();
        } else if (CURRENT_IS_CONTACTS) {
            checkAndRemoveChat();
            findViewById(R.id.contacts_fragment).setVisibility(View.VISIBLE);
            findViewById(R.id.chat_fragment).setVisibility(View.GONE);
            localOnResume();
        } else {
            findViewById(R.id.contacts_fragment).setVisibility(View.GONE);
            findViewById(R.id.chat_fragment).setVisibility(View.VISIBLE);
        }
    }

    private void checkAndRemoveChat() {
        removeFragment(R.id.contactlist_chat);
        this.ANY_CHAT_ACTIVE = false;
    }

    private void startChatFragment(JFragment chat) {
        checkAndRemoveChat();
        this.ANY_CHAT_ACTIVE = true;
        if (!resources.IT_IS_TABLET) {
            CURRENT_IS_CONTACTS = false;
        }
        attachFragment(R.id.contactlist_chat, chat);
    }

    private void startFragmentChat(Conference conference) {
        JConference chat = JConference.getInstance(conference, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    private void startFragmentChatJConference(String action) {
        JConference chat = JConference.getInstance(action, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    public void startFragmentChatJabber(String action) {
        JChatActivity chat = JChatActivity.getInstance(action, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    private void startFragmentChatICQ(String action) {
        ICQChatActivity chat = ICQChatActivity.getInstance(action, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    private void startFragmentChatMMP(String action) {
        MMPChatActivity chat = MMPChatActivity.getInstance(action, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    private void startFragmentChat(ICQContact contact) {
        ICQChatActivity chat = ICQChatActivity.getInstance(contact, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    public void startFragmentChat(JContact contact) {
        JChatActivity chat = JChatActivity.getInstance(contact, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    private void startFragmentChat(MMPContact contact) {
        MMPChatActivity chat = MMPChatActivity.getInstance(contact, new ChatInitCallback() {
            @Override
            public void chatInitialized() {
                updateUI();
            }
        });
        startChatFragment(chat);
    }

    private void startICQChatActivity(ICQContact contact) {
        startFragmentChat(contact);
    }

    public void startJConferenceActivity(ConferenceItem conference) {
        startFragmentChat(conference.conference);
    }

    public void startJChatActivity(JContact contact) {
        startFragmentChat(contact);
    }

    public void startMMPChatActivity(MMPContact contact) {
        startFragmentChat(contact);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Dialog cmd_form;
        switch (msg.what) {
            case 1:
                updateBottomPanel();
                break;
            case 2:
                refreshContactlist();
                break;
            case 3:
                createContactlistFromProfiles();
                break;
            case 4:
                checkConferences();
                break;
            case 16:
                tempContactForDisplayInfo = (InfoContainer) msg.obj;
                if (tempContactForDisplayInfo != null) {
                    ADB.checkUserInfos();
                    removeDialog(10);
                    showDialogA(10);
                    break;
                } else {
                    return false;
                }
            case 31:
                BufferedDialog dialog = (BufferedDialog) msg.obj;
                if (dialog != null) {
                    dialog_for_display = dialog;
                    if (!HIDDEN) {
                        if (last_shown_notify_dialog == null) {
                            removeDialog(23);
                            showDialogA(23);
                            break;
                        } else if (!last_shown_notify_dialog.isShowing()) {
                            removeDialog(23);
                            showDialogA(23);
                            break;
                        } else {
                            dialogs.add(dialog);
                            break;
                        }
                    } else {
                        dialogs.add(dialog);
                        break;
                    }
                } else {
                    return false;
                }
            case 32:
                if (msg.obj == null) {
                    ssi_progress = DialogBuilder.createProgress(this, resources.getString("s_please_wait"), true);
                } else {
                    ssi_progress = DialogBuilder.createProgress(this, (String) msg.obj, true);
                }
                ssi_progress.show();
                break;
            case 33:
                if (ssi_progress != null) {
                    ssi_progress.dismiss();
                    break;
                }
                break;
            case SHOW_VCARD:
                vcard_to_display = (VCard) msg.obj;
                ADB.checkUserInfos();
                removeDialog(44);
                showDialogA(44);
                break;
            case SHOW_VCARD_EDITOR:
                ru.ivansuper.jasmin.jabber.vcard.VCard vcard = (ru.ivansuper.jasmin.jabber.vcard.VCard) msg.obj;
                final LinearLayout editor_lay = ru.ivansuper.jasmin.jabber.vcard.VCard.prepareEditor(this);
                vcard.fillFields(editor_lay);
                Dialog editor = DialogBuilder.createYesNo(
                        this,
                        editor_lay,
                        0,
                        Locale.getString("s_user_vcard"),
                        Locale.getString("s_ok"),
                        Locale.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Dialog progress = DialogBuilder.createProgress(
                                        ContactListActivity.this,
                                        Locale.getString("s_please_wait"),
                                        true
                                );
                                progress.show();
                                contextJProfile.my_vcard.readFieldsTemporary(editor_lay);
                                contextJProfile.updateVCard(progress);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        },
                        true
                );
                editor.show();
                break;
            case UPDATE_BLINK_STATE:
                updateBlinkState();
                break;
            case SHOW_JABBER_FORM:
                final AbstractForm form = (AbstractForm) msg.obj;
                Dialog xform = DialogBuilder.createYesNo(
                        this,
                        form.getContent(),
                        0,
                        form.TITLE == null ? "Jabber form" : form.TITLE,
                        Locale.getString("s_ok"),
                        Locale.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                form.send();
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                form.cancel();
                            }
                        }, true
                );
                xform.show();
                break;
            case SHOW_JABBER_CMD_FORM:
                final Command cmd = (Command) msg.obj;
                if (cmd.completed) {
                    cmd_form = DialogBuilder.createOk(
                            this,
                            cmd.op.form.getContent(),
                            cmd.op.form.TITLE == null ? "Jabber form" : cmd.op.form.TITLE,
                            Locale.getString("s_ok"),
                            0,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            },
                            true
                    );
                } else if (cmd.buttons.length == 2) {
                    cmd_form = DialogBuilder.createYesNoCancel(
                            this,
                            cmd.op.form.getContent(),
                            cmd.op.form.TITLE == null ? "Jabber form" : cmd.op.form.TITLE,
                            Command.getButtonName(cmd.buttons[0]),
                            Command.getButtonName(cmd.buttons[1]),
                            Locale.getString("s_cancel"),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cmd.selected_button = 0;
                                    cmd.proceed();
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cmd.selected_button = 1;
                                    cmd.proceed();
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cmd.cancel();
                                }
                            });
                } else {
                    cmd_form = DialogBuilder.createYesNo(
                            this,
                            cmd.op.form.getContent(),
                            0,
                            cmd.op.form.TITLE == null ? "Jabber form" : cmd.op.form.TITLE,
                            Command.getButtonName(1),
                            Locale.getString("s_cancel"), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cmd.proceed();
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cmd.cancel();
                                }
                            }, true);
                }
                cmd_form.show();
                break;
            case RETURN_TO_CONTACTS:
                if (!CURRENT_IS_CONTACTS) {
                    CURRENT_IS_CONTACTS = true;
                } else {
                    checkAndRemoveChat();
                }
                updateUI();
                break;
        }
        return false;
    }

    private void updateBlinkState() {
        switcher.setBlinkState(0, jasminSvc.MESSAGES_DUMP.simple_messages);
        switcher.setBlinkState(2, jasminSvc.MESSAGES_DUMP.conferences);
    }

    public class contactLongClickListener implements MultiColumnList.OnItemLongClickListener {

        @Override
        public void onItemLongClick(MultiColumnList multiColumnList, View view, int i, long arg3) {
            if (CURRENT_IS_CONTACTS) {
                ContactsAdapter adp = (ContactsAdapter) multiColumnList.getAdapter();
                ContactlistItem item = adp.getItem(i);
                if (item.itemType == ContactlistItem.CONTACT) {
                    contextContact = (ICQContact) item;
                    contextProfile = contextContact.profile;
                    removeDialog(7);
                    showDialogA(7);
                } else if (item.itemType == ContactlistItem.GROUP) {
                    contextGroup = (ICQGroup) item;
                    if (!contextGroup.profile.connected) {
                        Toast.makeText(
                                ContactListActivity.this,
                                resources.getString("s_profile_must_be_connected"),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                    removeDialog(28);
                    showDialogA(28);
                } else if (item.itemType == ContactlistItem.JABBER_CONTACT) {
                    contextContact = null;
                    contextJContact = (JContact) item;
                    contextMMPContact = null;
                    removeDialog(7);
                    showDialogA(7);
                } else if (item.itemType == ContactlistItem.MMP_CONTACT) {
                    contextContact = null;
                    contextJContact = null;
                    contextMMPContact = (MMPContact) item;
                    removeDialog(7);
                    showDialogA(7);
                } else if (item.itemType == ContactlistItem.JABBER_CONFERENCE) {
                    contextConference = (ConferenceItem) item;
                    removeDialog(43);
                    showDialogA(43);
                }
            }
        }
    }

    private class statusListListener implements AdapterView.OnItemClickListener {
        ICQProfile tempProfile;

        public statusListListener(ICQProfile param) {
            this.tempProfile = param;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            last_quick_action.dismiss();
            contextProfile = this.tempProfile;
            switch ((int) arg0.getAdapter().getItemId(arg2)) {
                case 0:
                    contextProfile.setStatus(0);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 1:
                    contextProfile.setStatus(32);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 2:
                    contextProfile.setStatus(1);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 3:
                    contextProfile.setStatus(16);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 4:
                    contextProfile.setStatus(4);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 5:
                    contextProfile.setStatus(2);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 6:
                    contextProfile.setStatus(8193);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 7:
                    contextProfile.setStatus(12288);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 8:
                    contextProfile.setStatus(16384);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 9:
                    contextProfile.setStatus(20480);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 10:
                    contextProfile.setStatus(24576);
                    if (!contextProfile.connected && !contextProfile.connecting) {
                        contextProfile.startConnectingChosed();
                        return;
                    }
                    return;
                case 11:
                    contextProfile.disconnect();
                    return;
                case 12:
                    removeDialog(32);
                    showDialogA(32);
                    return;
                case 13:
                    if (!contextProfile.connected) {
                        Toast.makeText(ContactListActivity.this, resources.getString("s_profile_must_be_connected"), Toast.LENGTH_LONG).show();
                        return;
                    }
                    removeDialog(0);
                    showDialogA(0);
                    return;
                default:
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (service != null) {
            service.handleContactlistNeedRemake();
        }
    }


}
