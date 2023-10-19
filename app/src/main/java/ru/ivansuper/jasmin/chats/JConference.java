package ru.ivansuper.jasmin.chats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import ru.ivansuper.jasmin.BufferedDialog;
import ru.ivansuper.jasmin.ConferenceAdapter;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.VCard;
import ru.ivansuper.jasmin.jabber.commands.Callback;
import ru.ivansuper.jasmin.jabber.commands.CommandItem;
import ru.ivansuper.jasmin.jabber.conference.BannedListActivity;
import ru.ivansuper.jasmin.jabber.conference.ConfUsersAdapter;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.forms.Operation;
import ru.ivansuper.jasmin.jabber.xml_utils;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.plugins._interface.IdentificatedRunnable;
import ru.ivansuper.jasmin.plugins._interface.JConferenceWindowInterface;
import ru.ivansuper.jasmin.plugins._interface.MenuItemWrapper;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.slide_tools.ListViewA;
import ru.ivansuper.jasmin.utilities;

public class JConference extends Chat implements Handler.Callback {
    public static final int BANNED_LIST_RECEIVED = 400;
    public static boolean INITIALIZED;
    /** @noinspection unused*/
    public static final int SHOW_INFO = 66;
    /** @noinspection unused*/
    public static final int SHOW_JABBER_FORM = 256;
    /** @noinspection unused*/
    public static final int SHOW_VCARD = 65;
    public static boolean TOP_PANEL_VISIBLED = true;
    public static Conference conference;
    public static boolean is_any_chat_opened = false;
    public static boolean multiquoting = false;
    public LinearLayout TOP_PANEL;
    public ConferenceAdapter chatAdp;
    public LinearLayout chat_back;
    public ConfUsersAdapter conf_users;
    private final Vector<IdentificatedRunnable> context_menus_runnables = new Vector<>();
    public Conference.User context_user;
    public BufferedDialog dialog_for_display;
    private ImageView mUsersToggleButton;
    public ImageView mainStatus;
    protected int moderation_operation;
    public TextView nick_;
    public TextView nickname;
    public TextView theme;
    public ListView userList;
    public VCard vcard_to_display;
    public ImageView xStatus;

    private JConference(ChatInitCallback var1, Conference var2) {
        this.setScrollStateHash(Integer.toHexString(utilities.getHash(var2)));
        conference = var2;
        this.init_callback = var1;
    }

    private void checkAndRunIdentificatedTask(int var1) {
        //noinspection EmptySynchronizedStatement
        synchronized(this){}
        int var2 = 0;

        Throwable var10000;
        while(true) {
            //noinspection unused
            boolean var10001;
            int var3;
            try {
                var3 = this.context_menus_runnables.size();
            } catch (Throwable var10) {
                var10000 = var10;
                break;
            }

            if (var2 >= var3) {
                return;
            }

            try {
                if (((IdentificatedRunnable)this.context_menus_runnables.get(var2)).id == var1) {
                    ((IdentificatedRunnable)this.context_menus_runnables.remove(var2)).task.run();
                    return;
                }
            } catch (Throwable var9) {
                var10000 = var9;
                //noinspection UnusedAssignment
                var10001 = false;
                break;
            }

            ++var2;
        }

        Throwable var4 = var10000;
        try {
            throw var4;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void checkOrientation() {
        if (resources.ctx.getResources().getConfiguration().orientation == 2) {
            if (this.userList != null) {
                this.mUsersToggleButton.setVisibility(View.VISIBLE);
                this.toggleUserlistVisibility(false);
            }
        } else if (this.userList != null) {
            this.mUsersToggleButton.setVisibility(View.GONE);
            this.userList.setVisibility(View.GONE);
        }

    }

    public static JConference getInstance(String action, ChatInitCallback callback) {
        JProfile profile;
        Conference conference_ = null;
        if (action != null && action.startsWith("ITEM")) {
            String[] parts = utilities.split(action.substring(4), "***$$$SEPARATOR$$$***");
            if (parts.length != 2 || (profile = resources.service.profiles.getProfileByID(parts[0])) == null) {
                return null;
            }
            conference_ = profile.getConference(parts[1]);
        }
        return new JConference(callback, conference_);
    }

    public static JConference getInstance(Conference var0, ChatInitCallback var1) {
        return new JConference(var1, var0);
    }

    /** @noinspection unused*/
    private void handleIncomingTextMessage(HistoryItem var1) {
        this.chatAdp.refreshList();
    }

    private void initChat() {
        this.hdl = new Handler(this);
        service.chatHdl = this.hdl;
        this.messageList.setService(service);
        this.initChatInterface();
    }

    private void initSettings() {
        this.sendByEnter = this.sp.getBoolean("ms_send_by_enter", false);
    }

    private void putIdentificatedTask(Runnable var1, int var2) {
        //noinspection EmptySynchronizedStatement
        synchronized(this){}
        IdentificatedRunnable identificatedRunnable = new IdentificatedRunnable(var1, var2);
        this.context_menus_runnables.add(identificatedRunnable);

    }

    public void toggleUserlistVisibility(boolean toggle) {
        boolean visible = Manager.getBoolean("conf_userlist_state", true);
        if (toggle) {
            Manager.putBoolean("conf_userlist_state", !visible);
        }
        boolean visible2 = Manager.getBoolean("conf_userlist_state", true);
        if (visible2) {
            this.userList.setVisibility(View.VISIBLE);
            this.mUsersToggleButton.setImageResource(R.drawable.conf_userslist_closed);
            return;
        }
        this.userList.setVisibility(View.GONE);
        this.mUsersToggleButton.setImageResource(R.drawable.conf_userslist_opened);
    }

    public String computeMultiQuote() {
        String var1 = "";

        String var4;
        for(int var2 = 0; var2 < this.chatAdp.getCount(); var1 = var4) {
            HistoryItem var3 = this.chatAdp.getItem(var2);
            var4 = var1;
            if (var3.selected) {
                var4 = var1 + var3.conf_nick + " [" + var3.formattedDate + "]:\n" + var3.message + "\n";
            }

            ++var2;
        }

        return var1;
    }

    public void doSend() {
        String var1 = this.input.getText().toString();
        if (var1.length() > 0) {
            if (this.input.getText().toString().trim().length() != 0 && conference.isOnline()) {
                ADB.proceedMessage(var1);
                HistoryItem var2 = new HistoryItem();
                var2.confirmed = true;
                var2.message = var1;
                var2.conf_nick = conference.nick;
                var2.conf_profile = conference.profile;
                conference.sendMessage(var2.message);
                this.input.setText("");
                conference.profile.svc.playEvent(7);
            }
        } else if (!resources.IT_IS_TABLET) {
            this.handleChatClosed();
        }

    }

    @SuppressLint("SetTextI18n")
    public void drawReceiverData() {
        if (conference.isOnline()) {
            this.mainStatus.setImageDrawable(resources.jabber_conference);
        } else {
            this.mainStatus.setImageDrawable(resources.jabber_conference_offline);
        }

        this.xStatus.setVisibility(View.GONE);
        this.nickname.setText(JProtocol.getNameFromFullID(conference.JID) + " (" + conference.users.size() + ")");
        this.theme.setText(conference.theme);
    }

    protected void finalize() throws Throwable {
        Log.e(this.getClass().getSimpleName(), "Class 0x" + Integer.toHexString(this.hashCode()) + " finalized");
        super.finalize();
    }

    public boolean handleMessage(Message var1) {
        //noinspection unused
        boolean var10001;
        Object var11;
        switch (var1.what) {
            case 2:
                this.drawReceiverData();
                break;
            case 4:
                try {
                    var11 = var1.obj;
                } catch (Exception var8) {
                    //noinspection UnusedAssignment
                    var10001 = false;
                    break;
                }

                if (var11 != null) {
                    try {
                        if (var11 instanceof Conference && var11.equals(conference)) {
                            this.handleIncomingTextMessage((HistoryItem)null);
                        }
                    } catch (Exception var7) {
                        //noinspection UnusedAssignment
                        var10001 = false;
                    }
                }
                break;
            case 5:
                try {
                    var11 = var1.obj;
                } catch (Exception var6) {
                    //noinspection UnusedAssignment
                    var10001 = false;
                    break;
                }

                if (var11 != null) {
                    try {
                        if (var11 instanceof Conference && var11.equals(conference)) {
                            this.chatAdp.notifyDataSetChanged();
                        }
                    } catch (Exception var5) {
                        //noinspection UnusedAssignment
                        var10001 = false;
                    }
                }
                break;
            case 62:
                this.finish();
                break;
            case 65:
                if (INITIALIZED) {
                    this.vcard_to_display = (VCard)var1.obj;
                    this.removeDialog(7);
                    this.showDialog(7);
                }
                break;
            case 66:
                if (INITIALIZED) {
                    BufferedDialog var10 = (BufferedDialog)var1.obj;
                    if (var10 != null) {
                        this.dialog_for_display = var10;
                        this.removeDialog(8);
                        this.showDialog(8);
                    }
                }
                break;
            case 97:
                this.removeDialog(1);
                this.showDialog(1);
                break;
            case 256:
                final Operation var2 = (Operation)var1.obj;
                Activity var3 = this.ACTIVITY;
                ScrollView var4 = var2.form.form;
                String var9;
                if (var2.form.TITLE == null) {
                    var9 = "Jabber form";
                } else {
                    var9 = var2.form.TITLE;
                }

                DialogBuilder.createYesNo(var3, var4, 0, var9, Locale.getString("s_ok"), Locale.getString("s_cancel"), var122 -> var2.profile.stream.write(var2.compile(), var2.profile), var12 -> var2.profile.stream.write(var2.compileCancel(), var2.profile)).show();
                break;
            case 400:
                if (conference.mBannedList != null) {
                    this.startActivity(new Intent(this.ACTIVITY, BannedListActivity.class));
                }
        }

        return false;
    }

    public void initChatInterface() {
        service.cancelPersonalMessageNotify(utilities.getHash(conference));
        conference.item.setHasNoUnreadMessages();
        conference.unreaded = 0;
        service.removeMessageNotify(conference.item);
        service.handleContactlistDatasetChanged();
        this.drawReceiverData();
        if (this.chatAdp != null && this.chatAdp.isThatHistory(conference.history)) {
            this.chatAdp.refreshList();
        } else {
            this.chatAdp = new ConferenceAdapter(this.ACTIVITY, conference.history, this.messageList);
            this.messageList.setAdapter(this.chatAdp);
            this.restoreScrollState();
        }

        this.conf_users = new ConfUsersAdapter(this.ACTIVITY, conference.users);
        this.userList.setAdapter(this.conf_users);
        this.userList.setOnItemClickListener((var1, var2, var3, var4) -> {
            JConference.this.context_user = JConference.this.conf_users.getItem(var3);
            if (JConference.this.input.length() > 0) {
                EditText editText = JConference.this.input;
                String var6;
                if (JConference.this.input.getText().toString().endsWith(" ")) {
                    var6 = "";
                } else {
                    var6 = " ";
                }

                editText.append(var6 + JConference.this.context_user.nick + " ");
            } else {
                JConference.this.input.append(JConference.this.context_user.nick + ": ");
            }

        });
        this.userList.setOnItemLongClickListener((var1, var2, var3, var4) -> {
            JConference.this.context_user = JConference.this.conf_users.getItem(var3);
            JConference.this.removeDialog(4);
            JConference.this.removeDialog(6);
            JConference.this.showDialog(6);
            return false;
        });
        conference.callback = () -> JConference.service.runOnUi(() -> JConference.this.conf_users.notifyDataSetChanged());
        service.isAnyChatOpened = true;
        this.nick_.setText(conference.nick);
        int var1 = this.input.getSelectionStart();
        this.input.setText(MessageSaveHelper.getMessage(this.SAVE_HASH));
        int var2;
        if (received_smile_tag.length() > 0) {
            var2 = var1;
            if (var1 == -1) {
                var2 = 0;
            }

            var1 = Math.min(var2, this.input.length());
            String var3 = this.input.getText().toString().substring(0, var1) + received_smile_tag;
            String var4 = var3 + this.input.getText().toString().substring(var1);
            received_smile_tag = "";
            var2 = var3.length();
            this.input.setText(var4);
        } else {
            var2 = this.input.length();
        }

        this.input.setSelection(var2);
        if (PreferenceTable.auto_open_keyboard) {
            service.runOnUi(() -> {
                JConference.this.input.requestFocus();
                JConference.input_manager.showSoftInput(JConference.this.input, 0);
            }, 200L);
        }

        if (this.init_callback != null) {
            this.init_callback.chatInitialized();
            this.init_callback = null;
        }

    }

    public void initViews() {
        super.initViews();
        this.quot_view = (QuotingView)this.findViewById(2131427408);
        this.chat_back = (LinearLayout)this.findViewById(2131427382);
        this.mUsersToggleButton = (ImageView)this.findViewById(2131427455);
        this.mUsersToggleButton.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(48), 2));
        this.mUsersToggleButton.setOnClickListener(var1 -> JConference.this.toggleUserlistVisibility(true));
        this.mainStatus = (ImageView)this.findViewById(2131427384);
        this.xStatus = (ImageView)this.findViewById(2131427385);
        this.nickname = (TextView)this.findViewById(2131427386);
        this.nickname.setTextColor(ColorScheme.getColor(12));
        this.userList = (ListView)this.findViewById(2131427454);
        this.userList.setSelector(resources.getListSelector());
        this.messageList = (ListViewA)this.findViewById(2131427390);
        this.messageList.setSelector(resources.getListSelector());
        this.messageList.setSlideEnabled(false);
        this.messageList.setUseCustomScrollControl(true);
        this.messageList.setOnItemLongClickListener(new cl());
        this.messageList.setDragDropEnabledA(PreferenceTable.ms_dragdrop_quoting);
        this.messageList.setOnMultitouchListener(new ListViewA.MultitouchListener() {
            public void onStart(View var1, int var2) {
                JConference.this.quot_view.capture(var1, var2);
            }

            public void onStop(float var1, float var2, int var3) {
                JConference.this.quot_view.stop();
                int[] var4 = new int[2];
                JConference.this.input.getLocationOnScreen(var4);
                if (var2 > (float)var4[1]) {
                    JConference.this.performQuote(var3);
                }

            }

            public void onTouch(float var1, float var2) {
                boolean var3 = false;
                int[] var4 = new int[2];
                JConference.this.input.getLocationOnScreen(var4);
                if (var2 > (float)var4[1]) {
                    var3 = true;
                }

                JConference.this.quot_view.updatePoints(var1, var2, var3);
            }
        });
        this.messageList.setOnItemClickListener(new chat_click_listener());
        if (!PreferenceTable.chat_dividers) {
            this.messageList.setDivider((Drawable)null);
        }

        this.input = (EditText)this.findViewById(2131427405);
        this.input.setTextSize((float)PreferenceTable.chatTextSize);
        this.input.setTextColor(ColorScheme.getColor(46));
        Button menu_btn = (Button)this.findViewById(2131427404);
        resources.attachButtonStyle(menu_btn);
        menu_btn.setCompoundDrawables(resources.chat_menu_icon, (Drawable)null, (Drawable)null, (Drawable)null);
        menu_btn.setOnClickListener(var1 -> {
            JConference.this.removeDialog(1);
            JConference.this.showDialog(1);
        });
        if (!resources.IT_IS_TABLET) {
            menu_btn.setVisibility(View.GONE);
        }

        this.send = (Button)this.findViewById(2131427407);
        this.send.setOnClickListener(new sndListener());
        el var2 = new el();
        this.input.addTextChangedListener(var2);
        this.input.setOnKeyListener(new inputKeyListener());
        if (PreferenceTable.auto_cap) {
            this.input.setInputType(147457);
        } else {
            this.input.setInputType(131073);
        }

        this.smileysSelectBtn = (Button)this.findViewById(2131427406);
        this.smileysSelectBtn.setCompoundDrawables(resources.smileys_select_icon, (Drawable)null, (Drawable)null, (Drawable)null);
        this.smileysSelectBtn.setOnClickListener((View.OnClickListener) new smileySelectBtnListener());
        this.nick_ = (TextView)this.findViewById(2131427403);
        if (this.sp.getBoolean("ms_old_chat_style", true)) {
            this.nick_.setVisibility(View.GONE);
            resources.attachEditText(this.input);
        } else {
            this.input.setBackgroundResource(R.drawable.btn_default_transparent);
            this.input.setTextColor(-1);
            this.smileysSelectBtn.setBackgroundResource(R.drawable.btn_default_transparent);
            this.send.setBackgroundResource(R.drawable.btn_default_transparent);
            menu_btn.setBackgroundResource(R.drawable.btn_default_transparent);
            if (!resources.attachEditText(this.input)) {
                this.input.setTextColor(-1);
            }

        }
        resources.attachButtonStyle(this.smileysSelectBtn);
        resources.attachButtonStyle(this.send);
        resources.attachButtonStyle(menu_btn);

        this.theme = (TextView)this.findViewById(2131427388);
        if (!Manager.getBoolean("ms_conf_show_theme", true)) {
            this.theme.setVisibility(View.GONE);
        }

        this.TOP_PANEL = (LinearLayout)this.findViewById(2131427383);
        if (!TOP_PANEL_VISIBLED) {
            this.TOP_PANEL.setVisibility(View.GONE);
        }

        this.TOP_PANEL.setBackgroundColor(ColorScheme.getColor(11));
        LinearLayout var3 = (LinearLayout)this.findViewById(2131427394);
        var3.setBackgroundColor(ColorScheme.getColor(9));
        if (!this.getDefaultSharedPreferences().getBoolean("ms_use_solid_wallpaper", false)) {
            resources.attachChatMessagesBack(this.messageList);
            resources.attachChatMessagesBack(this.userList);
        }

        resources.attachChatTopPanel(this.TOP_PANEL);
        resources.attachChatBottomPanel(var3);
        this.nick_.setTextSize((float)PreferenceTable.chatTextSize);
        this.input.setTextSize((float)PreferenceTable.chatTextSize);
        this.nick_.setTextColor(ColorScheme.getColor(22));
        ((Button)this.findViewById(2131427393)).setVisibility(View.GONE);
        ((Button)this.findViewById(2131427392)).setVisibility(View.GONE);
        this.toggleUserlistVisibility(false);
    }

    public void onActivityResult(int var1, int var2, Intent var3) {
        if (!JConferenceWindowInterface.dispatchWindowResultEvent(this, var1, var2, var3) && var1 == 162 && var2 == -1) {
            received_smile_tag = var3.getAction();
        }

    }

    public void onConfigurationChanged(Configuration var1, int var2) {
        super.onConfigurationChanged(var1, var2);
        this.checkOrientation();
    }

    public void onCreate() {
        super.onCreate();
        received_smile_tag = "";
        INITIALIZED = true;
        this.sp = this.getDefaultSharedPreferences();
        this.setVolumeControlStream(3);
        int var1;
        if (resources.IT_IS_TABLET) {
            var1 = 2130903060;
        } else {
            var1 = 2130903057;
        }

        this.setContentView(var1);
        input_manager = (InputMethodManager)this.getSystemService("input_method");
        service = resources.service;
    }

    @Override // ru.ivansuper.jasmin.ui.ExFragment
    public Dialog onCreateDialog(int id) {
        Dialog catched = JConferenceWindowInterface.dispatchWindowCreateDialogEvent(this, id);
        if (catched == null) {
            Dialog ad = null;
            if (id == 1 && conference != null) {
                UAdapter adp = new UAdapter();
                adp.setMode(2);
                adp.setTextSize(18);
                adp.setPadding(15);
                if (conference.profile.connected && !conference.isOnline()) {
                    adp.put(resources.getString("s_join_conference"), 5);
                }
                if (conference.isOnline()) {
                    adp.put(resources.getString("s_users_list"), 3);
                }
                if (!multiquoting) {
                    adp.put(resources.getString("s_turn_on_multiquote"), 1);
                }
                if (multiquoting) {
                    adp.put(resources.getString("s_turn_off_multiquote"), 2);
                }
                if (conference.isOnline()) {
                    adp.put(resources.getString("s_set_theme"), 4);
                }
                adp.put(resources.getString("s_change_nick"), 8);
                adp.put(resources.getString("s_clear_messages"), 7);
                if (conference.isMeAModerator()) {
                    adp.put(Locale.getString("s_banned_list"), 9);
                }
                if (conference.isMeAOwner()) {
                    adp.put(Locale.getString("s_room_settings"), 10);
                }
                if (conference.isOnline()) {
                    adp.put(resources.getString("s_leave_conference"), 0);
                }
                for (int i = 0; i < 64; i++) {
                    MenuItemWrapper wrapper = new MenuItemWrapper(null, null, 0);
                    IdentificatedRunnable task = JConferenceWindowInterface.dispatchBindMenuItem(this, 0, i, wrapper);
                    if (wrapper.label != null) {
                        if (wrapper.icon != null) {
                            adp.put(wrapper.icon, wrapper.label, wrapper.id + 1024);
                        } else {
                            adp.put(wrapper.label, wrapper.id + 1024);
                        }
                        if (task != null) {
                            putIdentificatedTask(task.task, task.id + 1024);
                        }
                    }
                }
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp, 48, new chatMenuListener(adp));
            }
            if (id == 2) {
                final EditText count = new EditText(this.ACTIVITY);
                count.setMinLines(2);
                count.setHint(resources.getString("s_clear_msgs_dialog_hint") + "(" + this.chatAdp.getCount() + ")");
                count.setInputType(8194);
                resources.attachEditText(count);
                ad = DialogBuilder.createYesNo(this.ACTIVITY, count, 48, resources.getString("s_clear_messages"), resources.getString("s_ok"), resources.getString("s_cancel"), v -> {
                    String cnt = count.getText().toString();
                    if (cnt.length() == 0) {
                        cnt = String.valueOf(JConference.this.chatAdp.getCount());
                    }
                    try {
                        int cnt_ = Integer.parseInt(cnt);
                        if (cnt_ != 0) {
                            if (cnt_ > JConference.this.chatAdp.getCount()) {
                                cnt_ = JConference.this.chatAdp.getCount();
                            }
                            JConference.this.chatAdp.cutoff(cnt_);
                            Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_messages_cleared"), Toast.LENGTH_SHORT).show();
                            JConference.this.removeDialog(2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, v -> JConference.this.removeDialog(2));
            }
            if (id == 3) {
                UAdapter adp2 = new UAdapter();
                adp2.setMode(2);
                adp2.setTextSize(18);
                adp2.setPadding(15);
                adp2.put(resources.getString("s_copy"), 13);
                adp2.put(resources.getString("s_quote"), 14);
                adp2.put(resources.getString("s_copy_only_text"), 18);
                adp2.put(resources.getString("s_quote_only_text"), 17);
                for (int i2 = 0; i2 < 64; i2++) {
                    MenuItemWrapper wrapper2 = new MenuItemWrapper(null, null, 0);
                    IdentificatedRunnable task2 = JConferenceWindowInterface.dispatchBindMenuItem(this, 2, i2, wrapper2);
                    if (wrapper2.label != null) {
                        if (wrapper2.icon != null) {
                            adp2.put(wrapper2.icon, wrapper2.label, wrapper2.id + 1024);
                        } else {
                            adp2.put(wrapper2.label, wrapper2.id + 1024);
                        }
                        if (task2 != null) {
                            putIdentificatedTask(task2.task, task2.id + 1024);
                        }
                    }
                }
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp2, 48, new chatMenuListener(adp2));
            }
            if (id == 4) {
                // from class: ru.ivansuper.jasmin.chats.JConference.3
// android.widget.AdapterView.OnItemClickListener
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, this.conf_users, 48, (arg0, arg1, arg2, arg3) -> {
                    JConference.this.context_user = JConference.this.conf_users.getItem(arg2);
                    JConference.this.removeDialog(4);
                    JConference.this.removeDialog(6);
                    JConference.this.showDialog(6);
                });
            }
            if (id == 5) {
                final EditText theme = new EditText(this.ACTIVITY);
                theme.setText(conference.theme);
                theme.setMinLines(3);
                theme.setMaxLines(6);
                theme.setGravity(51);
                resources.attachEditText(theme);
                theme.setHint(resources.getString("s_set_theme_dialog_hint"));
                // from class: ru.ivansuper.jasmin.chats.JConference.5
// android.view.View.OnClickListener
                // from class: ru.ivansuper.jasmin.chats.JConference.4
// android.view.View.OnClickListener
                ad = DialogBuilder.createYesNo(this.ACTIVITY, theme, 48, resources.getString("s_set_theme"), resources.getString("s_ok"), resources.getString("s_cancel"), v -> {
                    String theme_ = theme.getText().toString();
                    if (theme_.length() == 0) {
                        Toast toast = Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_set_theme_error"), Toast.LENGTH_SHORT);
                        toast.setGravity(48, 0, 0);
                        toast.show();
                        return;
                    }
                    JConference.conference.setTheme(theme_);
                    JConference.this.removeDialog(5);
                }, v -> JConference.this.removeDialog(5));
            }
            if (id == 6) {
                UAdapter adp3 = new UAdapter();
                adp3.setMode(2);
                adp3.setTextSize(18);
                adp3.setPadding(10);
                adp3.put(resources.getString("s_user_nick"), 0);
                adp3.put(resources.getString("s_start_personal_chat"), 1);
                adp3.put(resources.getString("s_user_vcard"), 2);
                if (this.context_user.jid != null && this.context_user.jid.length() > 0) {
                    adp3.put(resources.getString("s_copy_jid"), 5);
                }
                adp3.put(resources.getString("s_commands"), 6);
                int strength = conference.getUserStrength(conference.nick);
                int user_strength = conference.getUserStrength(this.context_user.nick);
                switch (strength) {
                    case 5:
                    case 6:
                        if (user_strength < strength) {
                            adp3.put(resources.getString("s_moderator_kick"), 3);
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_role"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_visitor"), 7);
                            if (this.context_user.role.equals("visitor")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_participant"), 8);
                            if (this.context_user.role.equals("participant")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        }
                        break;
                    case 7:
                        if (user_strength < strength) {
                            adp3.put(resources.getString("s_moderator_ban"), 4);
                            adp3.put(resources.getString("s_moderator_kick"), 3);
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_role"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_visitor"), 7);
                            if (this.context_user.role.equals("visitor")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_participant"), 8);
                            if (this.context_user.role.equals("participant")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_moderator"), 9);
                            if (this.context_user.role.equals("moderator")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_affiliation"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_none"), 10);
                            if (this.context_user.affiliation.equals("none")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_member"), 11);
                            if (this.context_user.affiliation.equals("member")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        }
                        break;
                    case 8:
                        if (user_strength == strength) {
                            adp3.put(resources.getString("s_moderator_ban"), 4);
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_affiliation"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_none"), 10);
                            if (this.context_user.affiliation.equals("none")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_member"), 11);
                            if (this.context_user.affiliation.equals("member")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_admin"), 12);
                            if (this.context_user.affiliation.equals("admin")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_owner"), 13);
                            if (this.context_user.affiliation.equals("owner")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        } else if (user_strength < strength) {
                            adp3.put(resources.getString("s_moderator_ban"), 4);
                            adp3.put(resources.getString("s_moderator_kick"), 3);
                            if (!conference.isHeAreAdmin(this.context_user.nick)) {
                                adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_role"));
                                adp3.put(Locale.getString("s_jabber_conf_usermenu_visitor"), 7);
                                if (this.context_user.role.equals("visitor")) {
                                    adp3.toggleSelection(adp3.getLastIndex());
                                }
                                adp3.put(Locale.getString("s_jabber_conf_usermenu_participant"), 8);
                                if (this.context_user.role.equals("participant")) {
                                    adp3.toggleSelection(adp3.getLastIndex());
                                }
                                adp3.put(Locale.getString("s_jabber_conf_usermenu_moderator"), 9);
                                if (this.context_user.role.equals("moderator")) {
                                    adp3.toggleSelection(adp3.getLastIndex());
                                }
                            }
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_affiliation"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_none"), 10);
                            if (this.context_user.affiliation.equals("none")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_member"), 11);
                            if (this.context_user.affiliation.equals("member")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_admin"), 12);
                            if (this.context_user.affiliation.equals("admin")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_owner"), 13);
                            if (this.context_user.affiliation.equals("owner")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        }
                        break;
                }
                // from class: ru.ivansuper.jasmin.chats.JConference.6
// android.widget.AdapterView.OnItemClickListener
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp3, 48, (arg0, arg1, arg2, arg3) -> {
                    JConference.this.removeDialog(6);
                    int id2 = (int) arg0.getAdapter().getItemId(arg2);
                    JConference.this.checkAndRunIdentificatedTask(id2);
                    switch (id2) {
                        case 0:
                            if (JConference.this.input.length() > 0) {
                                JConference.this.input.append((JConference.this.input.getText().toString().endsWith(" ") ? "" : " ") + JConference.this.context_user.nick + " ");
                            } else {
                                JConference.this.input.append(JConference.this.context_user.nick + ": ");
                            }
                            return;
                        case 1:
                            JContact contact = JConference.conference.profile.createPMContainer(JConference.conference.JID + "/" + JConference.this.context_user.nick, JConference.conference);
                            if (contact != null) {
                                ((ContactListActivity) JConference.this.ACTIVITY).startFragmentChat(contact);
                                return;
                            }
                            return;
                        case 2:
                            JConference.conference.profile.doRequestInfoForDisplayRaw(JConference.conference.JID + "/" + JConference.this.context_user.nick);
                            return;
                        case 3:
                            JConference.this.moderation_operation = 0;
                            JConference.this.removeDialog(10);
                            JConference.this.showDialog(10);
                            return;
                        case 4:
                            JConference.this.moderation_operation = 1;
                            JConference.this.removeDialog(10);
                            JConference.this.showDialog(10);
                            return;
                        case 5:
                            //noinspection deprecation
                            ClipboardManager cm = (ClipboardManager) JConference.this.getSystemService("clipboard");
                            //noinspection deprecation
                            cm.setText(JConference.this.context_user.jid);
                            Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                            return;
                        case 6:
                            final Dialog load_progress = DialogBuilder.createProgress(JConference.this.ACTIVITY, Locale.getString("s_getting_commands"), true);
                            load_progress.show();
                            Callback callback = list -> {
                                load_progress.dismiss();
                                if (list.size() == 0) {
                                    JConference.service.showMessageInContactList(Locale.getString("s_information"), Locale.getString("s_no_commands"));
                                    return;
                                }
                                UAdapter adp4 = new UAdapter();
                                adp4.setMode(2);
                                adp4.setPadding(14);
                                for (int i3 = 0; i3 < list.size(); i3++) {
                                    CommandItem item = list.get(i3);
                                    adp4.put(item.name, i3);
                                }
                                Dialog commands = DialogBuilder.createWithNoHeader(JConference.this.ACTIVITY, adp4, 0, (arg02, arg12, arg22, arg32) -> {
                                    CommandItem item2 = (CommandItem) list.get(arg22);
                                    JConference.conference.profile.executeCommand(item2.jid, item2.node);
                                });
                                commands.show();
                            };
                            JConference.conference.profile.getCommandList(JConference.conference.JID + "/" + JConference.this.context_user.nick, callback);
                            return;
                        case 7:
                            JConference.conference.setUserRole(JConference.this.context_user.nick, "visitor");
                            return;
                        case 8:
                            JConference.conference.setUserRole(JConference.this.context_user.nick, "participant");
                            return;
                        case 9:
                            JConference.conference.setUserRole(JConference.this.context_user.nick, "moderator");
                            return;
                        case 10:
                            JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "none");
                            return;
                        case 11:
                            JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "member");
                            return;
                        case 12:
                            JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "admin");
                            return;
                        case 13:
                            JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "owner");
                            return;
                        default:
                    }
                });
            }
            if (id == 7) {
                LinearLayout vcard_lay = (LinearLayout) View.inflate(this.ACTIVITY, R.layout.vcard, null);
                ImageView vcard_avatar = (ImageView) vcard_lay.findViewById(R.id.vcard_avatar);
                EditText vcard_desc = (EditText) vcard_lay.findViewById(R.id.vcard_desc);
                if (this.vcard_to_display.avatar != null) {
                    vcard_avatar.setImageBitmap(this.vcard_to_display.avatar);
                }
                vcard_desc.setText(this.vcard_to_display.desc);
                vcard_desc.setFilters(new InputFilter[]{(source, arg1, arg2, dest, dstart, dend) -> source.length() < 1 ? dest.subSequence(dstart, dend) : ""});
                ad = DialogBuilder.createYesNo(this.ACTIVITY, vcard_lay, 48, resources.getString("s_user_vcard"), resources.getString("s_copy"), resources.getString("s_close"), v -> {
                    //noinspection deprecation
                    ClipboardManager cm = (ClipboardManager) JConference.this.getSystemService("clipboard");
                    //noinspection deprecation
                    cm.setText(JConference.this.vcard_to_display.desc);
                    Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                    JConference.this.vcard_to_display = null;
                    JConference.this.removeDialog(7);
                }, v -> {
                    JConference.this.vcard_to_display = null;
                    JConference.this.removeDialog(7);
                });
            }
            if (id == 9) {
                final EditText nick = new EditText(this.ACTIVITY);
                nick.setText(conference.nick);
                nick.setMinLines(1);
                nick.setMaxLines(1);
                nick.setGravity(51);
                resources.attachEditText(nick);
                // from class: ru.ivansuper.jasmin.chats.JConference.10
// android.view.View.OnClickListener
                // from class: ru.ivansuper.jasmin.chats.JConference.11
// android.view.View.OnClickListener
                ad = DialogBuilder.createYesNo(this.ACTIVITY, nick, 48, resources.getString("s_change_nick"), resources.getString("s_ok"), resources.getString("s_cancel"), v -> {
                    String nick_ = nick.getText().toString().trim();
                    if (nick_.length() == 0) {
                        Toast toast = Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_change_nick_error"), Toast.LENGTH_SHORT);
                        toast.setGravity(48, 0, 0);
                        toast.show();
                        return;
                    }
                    JConference.conference.updateNickname(nick_);
                    JConference.this.removeDialog(9);
                }, v -> JConference.this.removeDialog(9));
            }
            if (id == 10) {
                final EditText input_ = new EditText(this.ACTIVITY);
                input_.setMinLines(3);
                input_.setMaxLines(3);
                input_.setGravity(51);
                resources.attachEditText(input_);
                input_.setHint(resources.getString("s_reason_input"));
                // from class: ru.ivansuper.jasmin.chats.JConference.12
// android.view.View.OnClickListener
                // from class: ru.ivansuper.jasmin.chats.JConference.13
// android.view.View.OnClickListener
                ad = DialogBuilder.createYesNo(this.ACTIVITY, input_, 48, resources.getString("s_moderation"), resources.getString("s_ok"), resources.getString("s_cancel"), v -> {
                    String reason = input_.getText().toString().trim();
                    if (JConference.this.moderation_operation == 0) {
                        JConference.conference.kickUser(JConference.this.context_user, xml_utils.encodeString(reason));
                    } else if (JConference.this.moderation_operation == 1) {
                        JConference.conference.banUser(JConference.this.context_user, xml_utils.encodeString(reason));
                    }
                    JConference.this.removeDialog(10);
                }, v -> JConference.this.removeDialog(10));
            }
            if (id == 8) {
                // from class: ru.ivansuper.jasmin.chats.JConference.14
// android.view.View.OnClickListener
                ad = DialogBuilder.createOk(this.ACTIVITY, this.dialog_for_display.header, this.dialog_for_display.text, resources.getString("s_close"), 48, v -> JConference.this.removeDialog(8));
            }
            return ad;
        }
        return catched;
    }


    public void onDestroy() {
        INITIALIZED = false;
        if (this.chatAdp != null) {
            this.resetSelection();
        }

        if (conference != null) {
            conference.callback = null;
        }

        JConferenceWindowInterface.dispatchWindowEvent(this, 3);
        super.onDestroy();
    }

    public void onPause() {
        is_any_chat_opened = false;
        super.onPause();
        if (service != null) {
            service.isAnyChatOpened = false;
        }

        if (conference != null) {
            if (this.input != null) {
                conference.typedText = this.input.getText().toString();
            }
            //noinspection DataFlowIssue
            MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
            this.saveScrollState();
        }

        multiquoting = false;
        this.messageList.setDragDropEnabled(true);
        JConferenceWindowInterface.dispatchWindowEvent(this, 1);
    }

    public void onResume() {
        super.onResume();
        is_any_chat_opened = true;
        if (service == null) {
            service = resources.service;
        }

        this.initSettings();
        this.initChat();
        JConferenceWindowInterface.dispatchWindowEvent(this, 2);
    }

    public void onStart() {
        super.onStart();
        this.initViews();
        this.checkOrientation();
    }

    public void performQuote(int var1) {
        if (this.chatAdp != null) {
            HistoryItem var2 = this.chatAdp.getItem(var1);
            String var3 = var2.conf_nick + " [" + var2.formattedDate + "]:\n" + var2.message + "\n";
            var1 = this.input.length();
            EditText var4 = this.input;
            String var5;
            if (var1 > 0) {
                var5 = "\n";
            } else {
                var5 = "";
            }

            var4.append(var5 + var3);
            this.input.setSelection(this.input.length());
        }

    }

    public void resetSelection() {
        for(int var1 = 0; var1 < this.chatAdp.getCount(); ++var1) {
            this.chatAdp.getItem(var1).selected = false;
        }

    }

    private class chatMenuListener implements AdapterView.OnItemClickListener {
        UAdapter menu;

        public chatMenuListener(UAdapter var2) {
            this.menu = var2;
        }

        @SuppressLint("SetTextI18n")
        public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
            JConference.this.removeDialog(1);
            JConference.this.removeDialog(2);
            JConference.this.removeDialog(3);
            var3 = (int)this.menu.getItemId(var3);
            JConference.this.checkAndRunIdentificatedTask(var3 + 1024);
            HistoryItem var7;
            Toast var8;
            switch (var3) {
                case 0:
                    if (JConference.conference.isOnline()) {
                        JConference.conference.profile.logoutConference(JConference.conference.JID);
                    }
                    break;
                case 1:
                    JConference.multiquoting = true;
                    JConference.this.messageList.setDragDropEnabled(false);
                    JConference.this.chatAdp.notifyDataSetChanged();
                    break;
                case 2:
                    JConference.multiquoting = false;
                    JConference.this.messageList.setDragDropEnabled(true);
                    JConference.this.resetSelection();
                    JConference.this.chatAdp.notifyDataSetChanged();
                    break;
                case 3:
                    JConference.this.removeDialog(4);
                    JConference.this.showDialog(4);
                    break;
                case 4:
                    JConference.this.removeDialog(5);
                    JConference.this.showDialog(5);
                    break;
                case 5:
                    JConference.conference.profile.joinConference(JConference.conference.JID, JConference.conference.nick, JConference.conference.pass);
                case 6:
                case 11:
                case 12:
                case 15:
                case 16:
                default:
                    break;
                case 7:
                    JConference.this.removeDialog(2);
                    JConference.this.showDialog(2);
                    break;
                case 8:
                    JConference.this.removeDialog(9);
                    JConference.this.showDialog(9);
                    break;
                case 9:
                    JConference.conference.doRequestBannedList();
                    break;
                case 10:
                    JConference.conference.showRoomPreferences();
                    break;
                case 13:
                    var7 = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    //noinspection deprecation
                    ((ClipboardManager)JConference.this.getSystemService("clipboard")).setText(var7.conf_nick + " [" + var7.formattedDate + "]:\n" + var7.message + "\n");
                    var8 = Toast.makeText(JConference.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    var8.setGravity(48, 0, 0);
                    var8.show();
                    break;
                case 14:
                    var7 = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    String var11 = var7.conf_nick + " [" + var7.formattedDate + "]:\n" + var7.message + "\n";
                    JConference.this.input.setText(var11);
                    JConference.this.input.setSelection(var11.length(), var11.length());
                    break;
                case 17:
                    HistoryItem var10 = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    StringBuilder var9 = new StringBuilder();
                    boolean var6;
                    var6 = var10 == null;
                    Log.e("JChatActivity", var9.append(var6).toString());
                    //noinspection DataFlowIssue
                    JConference.this.input.setText(var10.message + "\n");
                    JConference.this.input.setSelection(var10.message.length());
                    break;
                case 18:
                    var7 = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    //noinspection deprecation
                    ((ClipboardManager)JConference.this.getSystemService("clipboard")).setText(var7.message + "\n");
                    var8 = Toast.makeText(JConference.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    var8.setGravity(48, 0, 0);
                    var8.show();
            }

        }
    }

    private class chat_click_listener implements AdapterView.OnItemClickListener {
        private chat_click_listener() {
        }

        public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
            ConferenceAdapter var7 = (ConferenceAdapter)var1.getAdapter();
            String var9;
            if (JConference.multiquoting) {
                HistoryItem var8 = var7.getItem(var3);
                boolean var6;
                var6 = !var8.selected;

                var8.selected = var6;
                JConference.this.chatAdp.notifyDataSetChanged();
                var9 = JConference.this.computeMultiQuote();
                JConference.this.input.setText(var9);
                JConference.this.input.setSelection(var9.length(), var9.length());
            } else {
                HistoryItem var10 = var7.getItem(var3);
                if (!var10.isTheme) {
                    if (JConference.this.input.length() > 0) {
                        if (JConference.this.input.getText().toString().endsWith(" ")) {
                            var9 = "";
                        } else {
                            var9 = " ";
                        }

                        JConference.this.input.append(var9 + var10.conf_nick + " ");
                    } else {
                        JConference.this.input.append(var10.conf_nick + ": ");
                    }
                }
            }

        }
    }

    private class cl implements AdapterView.OnItemLongClickListener {
        private cl() {
        }

        public boolean onItemLongClick(AdapterView<?> var1, View var2, int var3, long var4) {
            JConference.this.last_context_message = var3;
            JConference.this.removeDialog(3);
            JConference.this.showDialog(3);
            return false;
        }
    }

    private class el implements TextWatcher {
        private String buffer = "";

        public el() {
            if (!resources.IT_IS_TABLET) {
                JConference.this.send.setCompoundDrawables(resources.back_to_cl_icon, (Drawable)null, (Drawable)null, (Drawable)null);
            } else {
                JConference.this.send.setCompoundDrawables(resources.send_msg_icon, (Drawable)null, (Drawable)null, (Drawable)null);
            }

        }

        public void afterTextChanged(Editable var1) {
        }

        public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4) {
        }

        public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {
            if (!this.buffer.equals(var1.toString()) && var1.length() != 0) {
                if (var1.length() - this.buffer.length() == 1) {
                    ADB.symbolTyped();
                }

                this.buffer = var1.toString();
            }

            if (JConference.conference != null) {
                if (var1.length() > 0) {
                    JConference.this.send.setCompoundDrawables(resources.send_msg_icon, (Drawable)null, (Drawable)null, (Drawable)null);
                } else if (!resources.IT_IS_TABLET) {
                    JConference.this.send.setCompoundDrawables(resources.back_to_cl_icon, (Drawable)null, (Drawable)null, (Drawable)null);
                }
            }

        }
    }

    private class inputKeyListener implements View.OnKeyListener {
        private inputKeyListener() {
        }

        public boolean onKey(View var1, int var2, KeyEvent var3) {
            boolean var4;
            if (var2 == 66 && JConference.this.sendByEnter && var3.getAction() == 0) {
                JConference.this.doSend();
                var4 = true;
            } else {
                var4 = false;
            }

            return var4;
        }
    }

    private class sndListener implements View.OnClickListener {
        private sndListener() {
        }

        public void onClick(View var1) {
            JConference.this.doSend();
        }
    }
}
