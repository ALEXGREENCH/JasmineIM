package ru.ivansuper.jasmin.chats;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Vector;
import ru.ivansuper.jasmin.ChatAdapter;
import ru.ivansuper.jasmin.ContactHistoryActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.FileBrowserActivity;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.jabber.Clients;
import ru.ivansuper.jasmin.jabber.FileTransfer.SIFileSender;
import ru.ivansuper.jasmin.jabber.FileTransfer.TransferController;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.juick.TextParser;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.plugins._interface.IdentificatedRunnable;
import ru.ivansuper.jasmin.plugins._interface.JChatWindowInterface;
import ru.ivansuper.jasmin.plugins._interface.MenuItemWrapper;
import ru.ivansuper.jasmin.popup.PopupBuilder;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.slide_tools.AnimationCalculator;
import ru.ivansuper.jasmin.slide_tools.ListViewA;
import ru.ivansuper.jasmin.utilities;

public class JChatActivity extends Chat implements Handler.Callback {
    public static boolean INITIALIZED;
    private static boolean IT_IS_JUICK;
    /** @noinspection FieldCanBeLocal*/
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout TOP_PANEL;
    public static JContact contact;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout opened_chats_markers;
    private final typing_thread TypingThread;
    private ChatAdapter chatAdp;
    private final Vector<IdentificatedRunnable> context_menus_runnables = new Vector<>();
    private ImageView mainStatus;
    private TextView nick_;
    private TextView nickname;
    /** @noinspection FieldCanBeLocal*/
    private TextView status_text;
    private ImageView typing_field;
    private ImageView xStatus;
    public static boolean multiquoting = false;
    public static boolean is_any_chat_opened = false;
    private static final boolean TOP_PANEL_VISIBLED = true;

    private JChatActivity(ChatInitCallback callback, JContact contact_) {
        IT_IS_JUICK = false;
        setScrollStateHash(Integer.toHexString(utilities.getHash(contact_)));
        contact = contact_;
        this.init_callback = callback;
        this.TypingThread = new typing_thread();
        this.TypingThread.start();
        INITIALIZED = true;
    }

    private synchronized void putIdentificatedTask(Runnable task, int id) {
        this.context_menus_runnables.add(new IdentificatedRunnable(task, id));
    }

    /** @noinspection unused */
    private synchronized void checkAndRunIdentificatedTask(int id) {
        int i = 0;
        while (i < this.context_menus_runnables.size()) {
            IdentificatedRunnable task = this.context_menus_runnables.get(i);
            if (task.id == id) {
                this.context_menus_runnables.remove(i).task.run();
                break;
            } else {
                i++;
            }
        }
    }

    public static JChatActivity getInstance(JContact contact_, ChatInitCallback init_callback) {
        return new JChatActivity(init_callback, contact_);
    }

    public static JChatActivity getInstance(String action, ChatInitCallback init_callback) {
        JProfile profile;
        JContact contact_ = null;
        if (action != null && action.startsWith("ITEM")) {
            String[] parts = utilities.split(action.substring(4), "***$$$SEPARATOR$$$***");
            if (parts.length != 2 || (profile = resources.service.profiles.getProfileByID(parts[0])) == null) {
                return null;
            }
            contact_ = profile.getContactByJID(parts[1]);
        }
        return new JChatActivity(init_callback, contact_);
    }

    @Override // ru.ivansuper.jasmin.ui.ExFragment, ru.ivansuper.jasmin.ui.JFragment
    public void onCreate() {
        super.onCreate();
        received_smile_tag = "";
        INITIALIZED = true;
        this.sp = getDefaultSharedPreferences();
        setVolumeControlStream(3);
        setContentView(resources.IT_IS_TABLET ? R.layout.chat_xhigh : R.layout.chat);
        input_manager = (InputMethodManager) getSystemService("input_method");
        service = resources.service;
        JChatWindowInterface.dispatchWindowEvent(this, 0);
    }

    @Override // ru.ivansuper.jasmin.chats.Chat, ru.ivansuper.jasmin.ui.JFragment
    public void onStart() {
        super.onStart();
        initViews();
    }

    @Override // ru.ivansuper.jasmin.ui.ExFragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean catched = JChatWindowInterface.dispatchWindowResultEvent(this, requestCode, resultCode, data);
        if (!catched) {
            if (requestCode == 15 && resultCode == 162) {
                received_smile_tag = data.getAction();
            }
            if (requestCode == 161 && resultCode == -1) {
                //noinspection DataFlowIssue
                File file = new File(data.getAction());
                showFileSendResourceSelector(file);
            }
        }
    }

    private void showFileSendResourceSelector(final File file) {
        UAdapter adp = new UAdapter();
        adp.setMode(2);
        adp.setPadding(10);
        adp.setTextSize(16);
        final Vector<JContact.Resource> resources_ = contact.getResources();
        for (JContact.Resource r : resources_) {
            adp.put(r.name, 0);
        }
        Dialog res = DialogBuilder.create(resources.ctx, Locale.getString("s_jabber_connected_resources"), adp, new AdapterView.OnItemClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                JChatActivity.this.startSendFile(file, resources_.get(arg2).name);
            }
        }, true);
        res.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void startSendFile(File file, String resource) {
        SIFileSender sender = new SIFileSender(contact.profile, contact, resource, file);
        TransferController.putTransfer(sender);
        sender.start();
        HistoryItem hst = new HistoryItem(System.currentTimeMillis());
        hst.jtransfer = sender;
        hst.direction = 1;
        hst.jcontact = contact;
        hst.jcontact.loadLastHistory();
        hst.jcontact.history.add(hst);
        service.handleIncomingMessage(hst);
        if (!hst.jcontact.isChating) {
            contact.profile.openChat(hst.jcontact);
            hst.jcontact.isChating = true;
        }
        service.handleContactlistNeedRemake();
    }

    @Override // ru.ivansuper.jasmin.ui.ExFragment
    public Dialog onCreateDialog(int id) {
        Dialog catched = JChatWindowInterface.dispatchWindowCreateDialogEvent(this, id);
        if (catched == null) {
            Dialog ad = null;
            if (id == 1 && contact != null) {
                UAdapter adp = new UAdapter();
                adp.setMode(2);
                adp.setTextSize(16);
                adp.setPadding(16);
                if (contact.isChating) {
                    adp.put(resources.getString("s_close_chat"), 11);
                }
                if (contact.isOnline()) {
                    adp.put(resources.getString("s_do_send_file"), 28);
                }
                if (contact.profile.connected && contact.subscription != 1 && contact.subscription != 3 && !contact.conf_pm) {
                    adp.put(resources.getString("s_do_req_auth"), 19);
                }
                if (!contact.conf_pm) {
                    adp.put(resources.getString("s_full_history"), 20);
                }
                if (!multiquoting) {
                    adp.put(resources.getString("s_turn_on_multiquote"), 15);
                }
                if (multiquoting) {
                    adp.put(resources.getString("s_turn_off_multiquote"), 16);
                }
                if (IT_IS_JUICK && contact.profile.connected) {
                    adp.put_separator("JUICK");
                    adp.put(Locale.getString("s_juick_tool_0"), 21);
                    adp.put(Locale.getString("s_juick_tool_1"), 22);
                    adp.put(Locale.getString("s_juick_tool_2"), 23);
                    adp.put(Locale.getString("s_juick_tool_3"), 24);
                    adp.put(Locale.getString("s_juick_tool_4"), 31);
                    adp.put(Locale.getString("s_juick_tool_5"), 25);
                    adp.put(Locale.getString("s_juick_tool_6"), 26);
                    adp.put(Locale.getString("s_more_tools"), 27);
                }
                for (int i = 0; i < 64; i++) {
                    MenuItemWrapper wrapper = new MenuItemWrapper(null, null, 0);
                    IdentificatedRunnable task = JChatWindowInterface.dispatchBindMenuItem(this, 10, i, wrapper);
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
                    IdentificatedRunnable task2 = JChatWindowInterface.dispatchBindMenuItem(this, 11, i2, wrapper2);
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
                HistoryItem item = this.chatAdp.getItem(this.last_context_message);
                if (item == null) {
                    return null;
                }
                TextView txt = new TextView(this.ACTIVITY);
                txt.setAutoLinkMask(7);
                txt.setText(item.message);
                txt.setTextSize(18.0f);
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, txt, 48);
            }
            return ad;
        }
        return catched;
    }

    private void initSettings() {
        this.sendByEnter = this.sp.getBoolean("ms_send_by_enter", false);
    }

    @Override // ru.ivansuper.jasmin.ui.JFragment
    public void onResume() {
        super.onResume();
        is_any_chat_opened = true;
        if (service == null) {
            service = resources.service;
        }
        initSettings();
        initChat();
        JChatWindowInterface.dispatchWindowEvent(this, 2);
    }

    @Override
    public void onPause() {
        View child = this.messageList.getChildAt(0);
        if (child != null) {
            //noinspection ResultOfMethodCallIgnored
            Math.abs(child.getTop());
        }
        MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
        is_any_chat_opened = false;
        super.onPause();
        if (this.TypingThread != null) {
            this.TypingThread.forceStop();
        }
        if (service != null) {
            service.isAnyChatOpened = false;
        }
        if (contact != null && this.input != null) {
            contact.typedText = this.input.getText().toString();
        }
        multiquoting = false;
        this.messageList.setDragDropEnabled(true);
        JChatWindowInterface.dispatchWindowEvent(this, 1);
    }

    @Override // ru.ivansuper.jasmin.chats.Chat, ru.ivansuper.jasmin.ui.ExFragment, ru.ivansuper.jasmin.ui.JFragment
    public void onDestroy() {
        if (contact != null && contact.historyPreLoaded && !contact.isChating) {
            contact.clearPreloadedHistory();
        }
        if (this.chatAdp != null) {
            resetSelection();
        }
        JChatWindowInterface.dispatchWindowEvent(this, 3);
        INITIALIZED = false;
        super.onDestroy();
    }

    protected void finalize() throws Throwable {
        Log.e(getClass().getSimpleName(), "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
        super.finalize();
    }

    private void initChat() {
        this.hdl = new Handler(this);
        service.chatHdl = this.hdl;
        this.messageList.setService(service);
        initChatInterface(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initChatInterface(boolean fling) {
        int cursor_pos;
        service.cancelPersonalMessageNotify(utilities.getHash(contact));
        contact.setHasNoUnreadMessages();
        service.removeMessageNotify(contact);
        service.handleContactlistDatasetChanged();
        drawReceiverData();
        contact.loadLastHistory();
        IT_IS_JUICK = contact.ID.startsWith("juick@juick.com");
        if (this.chatAdp != null && this.chatAdp.isThatHistory(contact.history)) {
            this.chatAdp.setJuick(IT_IS_JUICK);
            this.chatAdp.refreshList();
        } else {
            this.chatAdp = new ChatAdapter(this.ACTIVITY, contact.history, 1, IT_IS_JUICK, this.messageList);
            this.messageList.setAdapter(this.chatAdp);
        }
        this.chatAdp.attachJuickListener(new AnonymousClass2());
        if (fling) {
            this.chatAdp.notifyDataSetInvalidated();
        } else {
            this.chatAdp.refreshList();
        }
        service.isAnyChatOpened = true;
        this.nick_.setText(contact.profile.nickname);
        int cursor_pos2 = this.input.getSelectionStart();
        this.input.setText(MessageSaveHelper.getMessage(this.SAVE_HASH));
        if (!received_smile_tag.isEmpty()) {
            if (cursor_pos2 == -1) {
                cursor_pos2 = 0;
            }
            if (cursor_pos2 > this.input.length()) {
                cursor_pos2 = this.input.length();
            }
            String first_part = this.input.getText().toString().substring(0, cursor_pos2) + received_smile_tag;
            String current_text = first_part + this.input.getText().toString().substring(cursor_pos2);
            received_smile_tag = "";
            cursor_pos = first_part.length();
            this.input.setText(current_text);
        } else {
            cursor_pos = this.input.length();
        }
        this.input.setSelection(cursor_pos);
        rebuildChatsMarkers();
        if (PreferenceTable.auto_open_keyboard && !fling) {
            service.runOnUi(new Runnable() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.3
                @Override // java.lang.Runnable
                public void run() {
                    JChatActivity.this.input.requestFocus();
                    JChatActivity.this.popupKeyboard();
                }
            }, 200L);
        }
        if (this.init_callback != null) {
            this.init_callback.chatInitialized();
            this.init_callback = null;
        }
    }

    /* renamed from: ru.ivansuper.jasmin.chats.JChatActivity$2, reason: invalid class name */
    class AnonymousClass2 implements TextParser.OnIDClickedListener {
        Dialog d;

        AnonymousClass2() {
        }

        @Override // ru.ivansuper.jasmin.jabber.juick.TextParser.OnIDClickedListener
        public void OnUserClicked(final String user) {
            if (JChatActivity.contact.profile.connected) {
                final UAdapter adp = new UAdapter();
                adp.setMode(2);
                adp.setTextSize(16);
                adp.setPadding(16);
                adp.put(Locale.getString("s_juick_user_tool_0"), 0);
                adp.put(Locale.getString("s_juick_user_tool_1"), 1);
                adp.put(Locale.getString("s_juick_user_tool_2"), 2);
                adp.put(Locale.getString("s_juick_user_tool_3"), 3);
                adp.put(Locale.getString("s_juick_user_tool_4"), 4);
                adp.put(Locale.getString("s_juick_user_tool_5"), 5);
                adp.put(Locale.getString("s_juick_user_tool_6"), 6);
                this.d = DialogBuilder.createWithNoHeader(JChatActivity.this.ACTIVITY, adp, 0, new AdapterView.OnItemClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.2.1
                    @SuppressLint("SetTextI18n")
                    @Override // android.widget.AdapterView.OnItemClickListener
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        switch ((int) adp.getItemId(arg2)) {
                            case 0:
                                JChatActivity.this.input.setText("PM " + user + " ");
                                JChatActivity.this.input.setSelection(JChatActivity.this.input.length());
                                JChatActivity.this.input.requestFocus();
                                resources.service.runOnUi(new Runnable() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.2.1.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        JChatActivity.this.popupKeyboard();
                                    }
                                }, 500L);
                                break;
                            case 1:
                                JChatActivity.this.sendRaw("S " + user, null, false);
                                break;
                            case 2:
                                JChatActivity.this.sendRaw("U " + user, null, false);
                                break;
                            case 3:
                                JChatActivity.this.sendRaw(user, null, false);
                                break;
                            case 4:
                                JChatActivity.this.sendRaw(user + "+", null, false);
                                break;
                            case 5:
                                JChatActivity.this.sendRaw("BL " + user, null, false);
                                break;
                            case 6:
                                JChatActivity.this.sendRaw("WL " + user, null, false);
                                break;
                        }
                    }
                });
                this.d.show();
            }
        }

        @Override // ru.ivansuper.jasmin.jabber.juick.TextParser.OnIDClickedListener
        public void OnMessageClicked(final String message_id) {
            final UAdapter adp = new UAdapter();
            adp.setMode(2);
            adp.setTextSize(16);
            adp.setPadding(16);
            adp.put(Locale.getString("s_juick_message_tool_5"), 5);
            adp.put(Locale.getString("s_juick_message_tool_0"), 0);
            adp.put(Locale.getString("s_juick_message_tool_1"), 1);
            adp.put(Locale.getString("s_juick_message_tool_2"), 2);
            adp.put(Locale.getString("s_juick_message_tool_3"), 3);
            adp.put(Locale.getString("s_juick_message_tool_4"), 4);
            this.d = DialogBuilder.createWithNoHeader(JChatActivity.this.ACTIVITY, adp, 0, new AdapterView.OnItemClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.2.2
                @SuppressLint("SetTextI18n")
                @Override // android.widget.AdapterView.OnItemClickListener
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    switch ((int) adp.getItemId(arg2)) {
                        case 0:
                            JChatActivity.this.sendRaw(message_id, null, false);
                            break;
                        case 1:
                            JChatActivity.this.sendRaw(message_id + "+", null, false);
                            break;
                        case 2:
                            JChatActivity.this.sendRaw("S " + message_id, null, false);
                            break;
                        case 3:
                            JChatActivity.this.sendRaw("U " + message_id, null, false);
                            break;
                        case 4:
                            JChatActivity.this.sendRaw("D " + message_id, null, false);
                            break;
                        case 5:
                            JChatActivity.this.input.setText(message_id + " ");
                            JChatActivity.this.input.setSelection(JChatActivity.this.input.length());
                            JChatActivity.this.input.requestFocus();
                            resources.service.runOnUi(new Runnable() {
                                @Override
                                public void run() {
                                    JChatActivity.this.popupKeyboard();
                                }
                            }, 500L);
                            break;
                    }
                }
            });
            this.d.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void popupKeyboard() {
        input_manager.showSoftInput(this.input, 0);
    }

    private void drawReceiverData() {
        ImageView avatar = (ImageView) findViewById(R.id.chat_avatar);
        if (avatar != null) {
            Bitmap bmp = null;
            if (contact.avatar != null) {
                bmp = ((BitmapDrawable) contact.avatar).getBitmap();
            }
            if (contact.avatar == null) {
                bmp = ((BitmapDrawable) resources.ctx.getResources().getDrawable(R.drawable.no_avatar)).getBitmap();
            }
            avatar.setImageBitmap(bmp);
        }
        if (contact.typing) {
            this.typing_field.setImageDrawable(resources.typing);
        } else {
            this.typing_field.setImageDrawable(null);
        }
        switch (contact.profile.type) {
            case 0:
                if (contact.conf_pm) {
                    this.mainStatus.setImageDrawable(resources.jabber_conf_pm);
                    break;
                } else if (contact.isOnline()) {
                    switch (contact.getStatus()) {
                        case 0:
                            this.mainStatus.setImageDrawable(resources.jabber_chat);
                            break;
                        case 1:
                            this.mainStatus.setImageDrawable(resources.jabber_online);
                            break;
                        case 2:
                            this.mainStatus.setImageDrawable(resources.jabber_away);
                            break;
                        case 3:
                            this.mainStatus.setImageDrawable(resources.jabber_dnd);
                            break;
                        case 4:
                            this.mainStatus.setImageDrawable(resources.jabber_na);
                            break;
                    }
                } else {
                    this.mainStatus.setImageDrawable(resources.jabber_offline);
                    break;
                }
                break;
            case 1:
                if (contact.isOnline()) {
                    this.mainStatus.setImageDrawable(resources.vk_online);
                } else {
                    this.mainStatus.setImageDrawable(resources.vk_offline);
                }
                break;
            case 2:
                if (contact.isOnline()) {
                    this.mainStatus.setImageDrawable(resources.yandex_online);
                } else {
                    this.mainStatus.setImageDrawable(resources.yandex_offline);
                }
                break;
            case 3:
                if (contact.isOnline()) {
                    this.mainStatus.setImageDrawable(resources.gtalk_online);
                } else {
                    this.mainStatus.setImageDrawable(resources.gtalk_offline);
                }
                break;
            case 4:
                if (contact.isOnline()) {
                    this.mainStatus.setImageDrawable(resources.qip_online);
                } else {
                    this.mainStatus.setImageDrawable(resources.qip_offline);
                }
                break;
        }
        this.xStatus.setImageDrawable(contact.ext_status);
        this.nickname.setText(contact.conf_pm ? resources.getString("s_personal_chat") + " " + contact.name : contact.name);
    }

    @Override
    public void initViews() {
        super.initViews();
        this.quot_view = (QuotingView) findViewById(R.id.chat_quoting_view);
        this.mainStatus = (ImageView) findViewById(R.id.mainStatus);
        this.xStatus = (ImageView) findViewById(R.id.xStatus);
        this.nickname = (TextView) findViewById(R.id.nickname);
        this.nickname.setTextColor(ColorScheme.getColor(12));
        this.messageList = (ListViewA) findViewById(R.id.messages);
        this.messageList.setSelector(resources.getListSelector());
        this.messageList.setOnItemLongClickListener(new cl());
        this.messageList.setDragDropEnabledA(PreferenceTable.ms_dragdrop_quoting);
        this.messageList.setOnMultitouchListener(new ListViewA.MultitouchListener() {
            @Override
            public void onStart(View view, int top) {
                JChatActivity.this.quot_view.capture(view, top);
            }

            @Override
            public void onTouch(float x1, float y1) {
                boolean green = false;
                int[] location = new int[2];
                JChatActivity.this.input.getLocationOnScreen(location);
                if (y1 > location[1]) {
                    green = true;
                }
                JChatActivity.this.quot_view.updatePoints(x1, y1, green);
            }

            @Override
            public void onStop(float x1, float y1, int item_idx) {
                JChatActivity.this.quot_view.stop();
                int[] location = new int[2];
                JChatActivity.this.input.getLocationOnScreen(location);
                if (y1 <= location[1]) {
                    return;
                }
                JChatActivity.this.performQuote(item_idx);
            }
        });
        this.messageList.setSlideListener(new ListViewA.SlideListener() {
            @Override
            public void onStartDrag() {
                JChatActivity.this.messageList.clearAnimation();
            }

            @Override // ru.ivansuper.jasmin.slide_tools.ListViewA.SlideListener
            public void onMoving(float lastX, float offset) {
                JChatActivity.this.setViewBufferOffset((int) lastX, (int) offset);
            }

            @Override // ru.ivansuper.jasmin.slide_tools.ListViewA.SlideListener
            public void onFling(boolean toRight, float factor) {
                JChatActivity.this.setViewBufferOffset(0, 0);
                JChatActivity.this.handleFling(toRight, factor);
            }

            @Override // ru.ivansuper.jasmin.slide_tools.ListViewA.SlideListener
            public void onCancel(float absolute_offset, float space) {
                JChatActivity.this.setViewBufferOffset(0, 0);
                JChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(absolute_offset, space));
            }
        });
        this.messageList.setOnItemClickListener(new chat_click_listener());
        if (!PreferenceTable.chat_dividers) {
            this.messageList.setDivider(null);
        }
        this.input = (EditText) findViewById(R.id.input);
        this.input.setTextSize(PreferenceTable.chatTextSize);
        this.input.setTextColor(ColorScheme.getColor(46));
        Button button = (Button) findViewById(R.id.chat_menu_btn);
        resources.attachButtonStyle(button);
        button.setCompoundDrawables(resources.chat_menu_icon, null, null, null);
        button.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.6
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                JChatActivity.this.removeDialog(1);
                JChatActivity.this.showDialog(1);
            }
        });
        if (!resources.IT_IS_TABLET) {
            button.setVisibility(View.GONE);
        }
        this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener(new sndListener());
        this.send.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                if (!JChatActivity.this.input.getText().toString().trim().isEmpty()) {
                    Vector<JContact.Resource> resources_ = JChatActivity.contact.getResources();
                    UAdapter list = new UAdapter();
                    list.setTextSize(16);
                    for (JContact.Resource res : resources_) {
                        list.put(Clients.getIcon(res.client), res.name, 0);
                    }
                    JChatActivity.this.LAST_QUICK_ACTION = PopupBuilder.buildList(list, JChatActivity.this.send, resources.getString("s_jabber_connected_resources"), 320, -2, new AdapterView.OnItemClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.7.1
                        @Override // android.widget.AdapterView.OnItemClickListener
                        public void onItemClick(AdapterView<?> arg02, View arg1, int arg2, long arg3) {
                            JChatActivity.this.LAST_QUICK_ACTION.dismiss();
                            UAdapter adapter = (UAdapter) arg02.getAdapter();
                            String resource = adapter.getItem(arg2);
                            JChatActivity.this.doSend(resource);
                        }
                    });
                    JChatActivity.this.LAST_QUICK_ACTION.show();
                }
                return false;
            }
        });
        this.input.addTextChangedListener(new el());
        this.input.setOnKeyListener(new inputKeyListener());
        if (PreferenceTable.auto_cap) {
            this.input.setInputType(147457);
        } else {
            this.input.setInputType(131073);
        }
        this.smileysSelectBtn = (Button) findViewById(R.id.chat_smiley_btn);
        this.smileysSelectBtn.setCompoundDrawables(resources.smileys_select_icon, null, null, null);
        this.smileysSelectBtn.setOnClickListener(new Chat.smileySelectBtnListener());
        this.nick_ = (TextView) findViewById(R.id.msg_nick);
        if (this.sp.getBoolean("ms_old_chat_style", true)) {
            this.nick_.setVisibility(View.GONE);
            resources.attachEditText(this.input);
        } else {
            this.input.setBackgroundResource(R.drawable.btn_default_transparent);
            this.input.setTextColor(-1);
            this.smileysSelectBtn.setBackgroundResource(R.drawable.btn_default_transparent);
            this.send.setBackgroundResource(R.drawable.btn_default_transparent);
            button.setBackgroundResource(R.drawable.btn_default_transparent);
            if (!resources.attachEditText(this.input)) {
                this.input.setTextColor(-1);
            }
        }
        resources.attachButtonStyle(this.smileysSelectBtn);
        resources.attachButtonStyle(this.send);
        resources.attachButtonStyle(button);
        this.typing_field = (ImageView) findViewById(R.id.typing_field);
        this.status_text = (TextView) findViewById(R.id.encoding);
        this.status_text.setVisibility(View.GONE);
        findViewById(R.id.chat_file_transfer_layout).setVisibility(View.GONE);
        TOP_PANEL = (LinearLayout) findViewById(R.id.chat_header);
        if (!TOP_PANEL_VISIBLED) {
            TOP_PANEL.setVisibility(View.GONE);
        }
        TOP_PANEL.setBackgroundColor(ColorScheme.getColor(11));
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chat_bottom_panel);
        linearLayout.setBackgroundColor(ColorScheme.getColor(9));
        if (!getDefaultSharedPreferences().getBoolean("ms_use_solid_wallpaper", false)) {
            resources.attachChatMessagesBack(this.messageList);
        }
        resources.attachChatTopPanel(TOP_PANEL);
        resources.attachChatBottomPanel(linearLayout);
        opened_chats_markers = (LinearLayout) findViewById(R.id.chat_chats_markers);
        opened_chats_markers.setVisibility(PreferenceTable.ms_show_markers_in_chat ? View.VISIBLE : View.GONE);
        this.nick_.setTextSize(PreferenceTable.chatTextSize);
        this.input.setTextSize(PreferenceTable.chatTextSize);
        this.nick_.setTextColor(ColorScheme.getColor(22));
        Button button2 = (Button) findViewById(R.id.chat_scroll_left);
        button2.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.8
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                Log.i("Scroller", "To right");
                if (JChatActivity.service.opened_chats.size() < 2) {
                    JChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(32.0f, JChatActivity.this.messageList.getWidth()));
                } else {
                    JChatActivity.this.handleFling(true, 0.0f);
                }
            }
        });
        Button button3 = (Button) findViewById(R.id.chat_scroll_right);
        button3.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.9
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                Log.i("Scroller", "To left");
                if (JChatActivity.service.opened_chats.size() < 2) {
                    JChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(-32.0f, JChatActivity.this.messageList.getWidth()));
                } else {
                    JChatActivity.this.handleFling(false, 0.0f);
                }
            }
        });
        if (this.sp.getBoolean("ms_scroll_arrows", false)) {
            button2.setVisibility(View.VISIBLE);
            button3.setVisibility(View.VISIBLE);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setViewBufferOffset(int static_offset, int offset) {
        this.messageList.scrollTo(-(offset - static_offset), this.messageList.getScrollY());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFling(boolean toRight, float factor) {
        resetSelection();
        this.TypingThread.forceStop();
        int chats_count = service.opened_chats.size();
        if (contact != null && this.input != null) {
            contact.typedText = this.input.getText().toString();
        }
        int current_idx = service.opened_chats.indexOf(contact);
        if (current_idx == -1) {
            current_idx = 0;
        }
        if (toRight) {
            int next_idx = current_idx - 1;
            while (true) {
                if (next_idx < 0) {
                    next_idx = chats_count - 1;
                }
                ContactlistItem item = service.opened_chats.get(next_idx);
                if (item.itemType == ContactlistItem.JABBER_CONTACT) {
                    contact = (JContact) item;
                    MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
                    setScrollStateHash(Integer.toHexString(utilities.getHash(contact)));
                    TranslateAnimation ta = new TranslateAnimation(1, factor, 1, 1.0f, 1, 0.0f, 1, 0.0f);
                    ta.setDuration(150L);
                    ta.setInterpolator(resources.ctx, android.R.anim.linear_interpolator);
                    ta.setAnimationListener(new Animation.AnimationListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.10
                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationEnd(Animation animation) {
                            JChatActivity.this.initChatInterface(true);
                            TranslateAnimation ta2 = new TranslateAnimation(1, -1.5f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                            ta2.setDuration(250L);
                            ta2.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
                            JChatActivity.this.messageList.startAnimation(ta2);
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationStart(Animation animation) {
                        }
                    });
                    this.messageList.startAnimation(ta);
                    return;
                }
                next_idx--;
            }
        } else {
            int next_idx2 = current_idx + 1;
            while (true) {
                if (next_idx2 >= chats_count) {
                    next_idx2 = 0;
                }
                ContactlistItem item2 = service.opened_chats.get(next_idx2);
                if (item2.itemType == ContactlistItem.JABBER_CONTACT) {
                    contact = (JContact) item2;
                    MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
                    setScrollStateHash(Integer.toHexString(utilities.getHash(contact)));
                    TranslateAnimation ta2 = new TranslateAnimation(1, factor, 1, -1.0f, 1, 0.0f, 1, 0.0f);
                    ta2.setDuration(150L);
                    ta2.setInterpolator(resources.ctx, android.R.anim.linear_interpolator);
                    ta2.setAnimationListener(new Animation.AnimationListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.11
                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationEnd(Animation animation) {
                            JChatActivity.this.initChatInterface(true);
                            TranslateAnimation ta3 = new TranslateAnimation(1, 1.5f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                            ta3.setDuration(250L);
                            ta3.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
                            JChatActivity.this.messageList.startAnimation(ta3);
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationStart(Animation animation) {
                        }
                    });
                    this.messageList.startAnimation(ta2);
                    return;
                }
                next_idx2++;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void performQuote(int idx) {
        String nick;
        String res;
        if (this.chatAdp != null) {
            HistoryItem item = this.chatAdp.getItem(idx);
            if (item.direction == 1) {
                nick = item.jcontact.name;
            } else {
                nick = item.jcontact.profile.ID;
            }
            if (!item.isFileMessage && !item.isXtrazMessage && !item.isAuthMessage) {
                if (item.jtransfer != null) {
                    res = "[" + item.formattedDate + "]:\n" + item.jtransfer.getStatusString();
                } else {
                    res = nick + " [" + item.formattedDate + "]:\n" + item.message;
                }
            } else {
                res = "[" + item.formattedDate + "]:\n" + item.message;
            }
            int length = this.input.length();
            this.input.append((length > 0 ? "\n" : "") + res);
            this.input.setSelection(this.input.length());
        }
    }

    private void rebuildChatsMarkers() {
        if (service != null) {
            opened_chats_markers.removeAllViews();
            if (service.opened_chats.size() >= 2) {
                for (int i = 0; i < service.opened_chats.size(); i++) {
                    ContactlistItem item = service.opened_chats.get(i);
                    if (item.itemType == ContactlistItem.JABBER_CONTACT) {
                        ImageView marker = new ImageView(resources.ctx);
                        marker.setPadding(3, 0, 3, 0);
                        marker.setImageDrawable(contact.equals(item) ? resources.marker_active_chat : resources.marker_chat);
                        if (((JContact) item).hasUnreadMessages) {
                            marker.setImageDrawable(resources.marker_msg_chat);
                        }
                        opened_chats_markers.addView(marker);
                    }
                }
            }
        }
    }

    private class sndListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            JChatActivity.this.doSend(null);
        }
    }

    private void doSend(String resource) {
        String message = this.input.getText().toString();
        if (!message.isEmpty()) {
            if (!this.input.getText().toString().trim().isEmpty() && contact.profile.connected) {
                sendRaw(message, resource, true);
                return;
            }
            return;
        }
        if (!resources.IT_IS_TABLET) {
            handleChatClosed();
        }
    }

    private void sendRaw(String message, String resource, boolean add_to_history) {
        ADB.proceedMessage(message);
        HistoryItem hst = new HistoryItem();
        hst.confirmed = false;
        hst.message = message;
        hst.jcontact = contact;
        hst.jabber_cookie = String.valueOf(System.currentTimeMillis());
        if (add_to_history) {
            contact.loadLastHistory();
            contact.history.add(hst);
            contact.writeMessageToHistory(hst);
            this.chatAdp.refreshList();
        }
        contact.profile.sendMessage(contact, hst.message, hst, resource);
        this.input.setText("");
        contact.profile.svc.playEvent(7);
    }

    /** @noinspection unused*/
    private void handleIncomingTextMessage(HistoryItem msg) {
        this.chatAdp.refreshList();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                drawReceiverData();
                break;
            case 4:
                try {
                    HistoryItem hst = (HistoryItem) msg.obj;
                    ContactlistItem cli = hst.jcontact;
                    //noinspection ConstantValue,ConditionCoveredByFurtherCondition
                    if (cli != null && (cli instanceof JContact) && cli.equals(contact)) {
                        handleIncomingTextMessage(hst);
                        break;
                    }
                } catch (Exception e) {
                    return false;
                }
                break;
            case 5:
                try {
                    Log.e("JChatActivity", "Refreshed");
                    ContactlistItem cli2 = (ContactlistItem) msg.obj;
                    //noinspection ConditionCoveredByFurtherCondition
                    if (cli2 != null && (cli2 instanceof JContact) && cli2.equals(contact)) {
                        Log.e("JChatActivity", "Refreshed");
                        this.chatAdp.refreshList();
                        break;
                    }
                } catch (Exception e2) {
                    return false;
                }
                break;
            case 6:
                rebuildChatsMarkers();
                break;
            case Chat.CLOSE /* 62 */:
                finish();
                break;
            case Chat.CHAT_UPDATE_CONTACT /* 96 */:
                updateContact();
                break;
            case Chat.CHAT_SHOW_MENU /* 97 */:
                removeDialog(1);
                showDialog(1);
                break;
        }
        return false;
    }

    private void updateContact() {
        String backup = this.input.getText().toString();
        JContact c = contact.profile.getContactByJID(contact.ID);
        if (c != null) {
            contact = contact.profile.getContactByJID(contact.ID);
            initChatInterface(true);
        }
        this.input.setText(backup);
    }

    private class chatMenuListener implements AdapterView.OnItemClickListener {
        UAdapter menu;

        public chatMenuListener(UAdapter adp) {
            this.menu = adp;
        }

        @SuppressLint("SetTextI18n")
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            String nick;
            String nick2;
            JChatActivity.this.removeDialog(1);
            JChatActivity.this.removeDialog(2);
            JChatActivity.this.removeDialog(3);
            int id = (int) this.menu.getItemId(arg2);
            switch (id) {
                case 11:
                    JChatActivity.contact.profile.closeChat(JChatActivity.contact);
                    JChatActivity.this.handleChatClosed();
                    break;
                case 13:
                    HistoryItem item = JChatActivity.this.chatAdp.getItem(JChatActivity.this.last_context_message);
                    ClipboardManager cm = (ClipboardManager) JChatActivity.this.getSystemService("clipboard");
                    if (item.direction == 1) {
                        nick2 = item.jcontact.name;
                    } else {
                        nick2 = item.jcontact.profile.ID;
                    }
                    //noinspection deprecation
                    cm.setText(nick2 + " [" + item.formattedDate + "]:\n" + item.message + "\n");
                    Toast msg = Toast.makeText(JChatActivity.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg.setGravity(48, 0, 0);
                    msg.show();
                    break;
                case 14:
                    HistoryItem item2 = JChatActivity.this.chatAdp.getItem(JChatActivity.this.last_context_message);
                    if (item2.direction == 1) {
                        nick = item2.jcontact.name;
                    } else {
                        nick = item2.jcontact.profile.ID;
                    }
                    String res = nick + " [" + item2.formattedDate + "]:\n" + item2.message + "\n";
                    JChatActivity.this.input.setText(res);
                    JChatActivity.this.input.setSelection(res.length(), res.length());
                    break;
                case 15:
                    JChatActivity.multiquoting = true;
                    JChatActivity.this.messageList.setDragDropEnabled(false);
                    JChatActivity.this.chatAdp.notifyDataSetChanged();
                    break;
                case 16:
                    JChatActivity.multiquoting = false;
                    JChatActivity.this.messageList.setDragDropEnabled(true);
                    JChatActivity.this.resetSelection();
                    JChatActivity.this.chatAdp.notifyDataSetChanged();
                    break;
                case 17:
                    HistoryItem item3 = JChatActivity.this.chatAdp.getItem(JChatActivity.this.last_context_message);
                    Log.e("JChatActivity", String.valueOf(item3 == null));
                    //noinspection DataFlowIssue
                    if (item3.direction == 1) {
                        //noinspection unused
                        String nick3 = item3.jcontact.name;
                    } else {
                        //noinspection unused
                        String nick4 = item3.jcontact.profile.ID;
                    }
                    JChatActivity.this.input.setText(item3.message + "\n");
                    JChatActivity.this.input.setSelection(item3.message.length());
                    break;
                case 18:
                    HistoryItem item4 = JChatActivity.this.chatAdp.getItem(JChatActivity.this.last_context_message);
                    if (item4.direction == 1) {
                        //noinspection unused
                        String nick5 = item4.jcontact.name;
                    } else {
                        //noinspection unused
                        String nick6 = item4.jcontact.profile.ID;
                    }
                    ClipboardManager cm2 = (ClipboardManager) JChatActivity.this.getSystemService("clipboard");
                    //noinspection deprecation
                    cm2.setText(item4.message + "\n");
                    Toast msg2 = Toast.makeText(JChatActivity.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg2.setGravity(48, 0, 0);
                    msg2.show();
                    break;
                case 19:
                    JChatActivity.contact.profile.doRequestAuth(JChatActivity.contact);
                    break;
                case 20:
                    Intent i = new Intent(JChatActivity.this.ACTIVITY, ContactHistoryActivity.class);
                    i.setAction("JBR" + JChatActivity.contact.profile.getFullJID() + "***$$$SEPARATOR$$$***" + JChatActivity.contact.ID);
                    JChatActivity.this.startActivity(i);
                    break;
                case 21:
                    JChatActivity.this.sendRaw("#", null, false);
                    break;
                case 22:
                    JChatActivity.this.sendRaw("##", null, false);
                    break;
                case 23:
                    JChatActivity.this.sendRaw("###", null, false);
                    break;
                case 24:
                    JChatActivity.this.sendRaw("#+", null, false);
                    break;
                case 25:
                    JChatActivity.this.sendRaw("@", null, false);
                    break;
                case 26:
                    JChatActivity.this.sendRaw("*", null, false);
                    break;
                case 27:
                    final UAdapter adp = new UAdapter();
                    adp.setMode(2);
                    adp.setTextSize(16);
                    adp.setPadding(16);
                    adp.put(Locale.getString("s_juick_tool_7"), 28);
                    adp.put(Locale.getString("s_juick_tool_8"), 29);
                    adp.put(Locale.getString("s_juick_tool_9"), 30);
                    adp.put(Locale.getString("s_juick_tool_10"), 32);
                    adp.put(Locale.getString("s_juick_tool_11"), 33);
                    adp.put(Locale.getString("s_juick_tool_12"), 34);
                    Dialog d = DialogBuilder.createWithNoHeader(JChatActivity.this.ACTIVITY, adp, 0, new AdapterView.OnItemClickListener() { // from class: ru.ivansuper.jasmin.chats.JChatActivity.chatMenuListener.1
                        @Override // android.widget.AdapterView.OnItemClickListener
                        public void onItemClick(AdapterView<?> arg02, View arg12, int arg22, long arg32) {
                            switch ((int) adp.getItemId(arg22)) {
                                case 28:
                                    JChatActivity.this.sendRaw("S", null, false);
                                    break;
                                case 29:
                                    JChatActivity.this.sendRaw("ON", null, false);
                                    break;
                                case 30:
                                    JChatActivity.this.sendRaw("OFF", null, false);
                                    break;
                                case 32:
                                    JChatActivity.this.sendRaw("WL", null, false);
                                    break;
                                case 33:
                                    JChatActivity.this.sendRaw("BL", null, false);
                                    break;
                                case 34:
                                    JChatActivity.this.sendRaw("VCARD", null, false);
                                    break;
                            }
                        }
                    });
                    d.show();
                    break;
                case 28:
                    Intent fb = new Intent();
                    fb.setClass(JChatActivity.this.ACTIVITY, FileBrowserActivity.class);
                    JChatActivity.this.ACTIVITY.startActivityForResult(fb, Chat.REQUEST_CODE_FILE);
                    break;
                case 31:
                    JChatActivity.this.sendRaw("DL", null, false);
                    break;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetSelection() {
        for (int i = 0; i < this.chatAdp.getCount(); i++) {
            this.chatAdp.getItem(i).selected = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String computeMultiQuote() {
        String nick;
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < this.chatAdp.getCount(); i++) {
            HistoryItem hst = this.chatAdp.getItem(i);
            if (hst.selected) {
                if (hst.direction == 1) {
                    nick = hst.jcontact.name;
                } else {
                    nick = hst.jcontact.profile.ID;
                }
                if (hst.jtransfer != null) {
                    res.append(nick).append(" [").append(hst.formattedDate).append("]:\n").append(hst.jtransfer.getStatusString()).append("\n");
                } else {
                    res.append(nick).append(" [").append(hst.formattedDate).append("]:\n").append(hst.message).append("\n");
                }
            }
        }
        return res.toString();
    }

    private class chat_click_listener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            boolean catched = JChatWindowInterface.dispatchMessagesClickEvent(arg0, arg1, arg2, arg3);
            if (!catched && JChatActivity.multiquoting) {
                ChatAdapter adp = (ChatAdapter) arg0.getAdapter();
                HistoryItem hst = adp.getItem(arg2);
                hst.selected = !hst.selected;
                JChatActivity.this.chatAdp.notifyDataSetChanged();
                String quote = JChatActivity.this.computeMultiQuote();
                JChatActivity.this.input.setText(quote);
                JChatActivity.this.input.setSelection(quote.length(), quote.length());
            }
        }
    }

    private class el implements TextWatcher {
        private String buffer = "";

        public el() {
            if (!resources.IT_IS_TABLET) {
                JChatActivity.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
            } else {
                JChatActivity.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
            }
        }

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable arg0) {
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!this.buffer.equals(s.toString()) && s.length() != 0) {
                if (s.length() - this.buffer.length() == 1) {
                    ADB.symbolTyped();
                }
                this.buffer = s.toString();
                JChatActivity.this.TypingThread.resetCounter();
            }
            if (JChatActivity.contact != null) {
                if (s.length() > 0) {
                    JChatActivity.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
                } else if (!resources.IT_IS_TABLET) {
                    JChatActivity.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
                }
            }
        }
    }

    private class inputKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View arg0, int keyCode, KeyEvent action) {
            if (keyCode != 66 || !JChatActivity.this.sendByEnter || action.getAction() != 0) {
                return false;
            }
            JChatActivity.this.doSend(null);
            return true;
        }
    }

    private class cl implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            JChatActivity.this.last_context_message = arg2;
            JChatActivity.this.removeDialog(3);
            JChatActivity.this.showDialog(3);
            return false;
        }
    }

    private static class typing_thread extends Thread {
        int counter;
        boolean typing;

        private typing_thread() {
            this.typing = false;
            this.counter = 0;
        }

        public void resetCounter() {
            if (this.counter == 0 && !this.typing && JChatActivity.contact != null && JChatActivity.contact.isOnline()) {
                JChatActivity.contact.profile.sendTypingNotify(JChatActivity.contact.ID, 1);
                this.typing = true;
            }
            this.counter = 30;
        }

        public void forceStop() {
            if (this.typing) {
                JChatActivity.contact.profile.sendTypingNotify(JChatActivity.contact.ID, 0);
                this.typing = false;
            }
            this.counter = 0;
        }

        @Override
        public void run() {
            while (JChatActivity.INITIALIZED) {
                try {
                    //noinspection BusyWait
                    sleep(100L);
                } catch (Exception ignored) {
                }
                if (JChatActivity.INITIALIZED) {
                    if (this.counter != 0) {
                        this.counter--;
                        if (this.counter <= 0) {
                            if (JChatActivity.contact.isOnline()) {
                                JChatActivity.contact.profile.sendTypingNotify(JChatActivity.contact.ID, 0);
                            }
                            this.typing = false;
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }
}
