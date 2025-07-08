package ru.ivansuper.jasmin.chats;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import ru.ivansuper.jasmin.ChatAdapter;
import ru.ivansuper.jasmin.ContactHistoryActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.FileBrowserActivity;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.PB;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.icq.FileTransfer.FileReceiver;
import ru.ivansuper.jasmin.icq.FileTransfer.FileSender;
import ru.ivansuper.jasmin.icq.FileTransfer.FileTransfer;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.slide_tools.AnimationCalculator;
import ru.ivansuper.jasmin.slide_tools.ListViewA;
import ru.ivansuper.jasmin.utilities;

public class ICQChatActivity extends Chat {
    public static boolean INITIALIZED;
    /** @noinspection FieldCanBeLocal*/
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout TOP_PANEL;
    public static boolean VISIBLE;
    /** @noinspection FieldCanBeLocal, unused */
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout chat_back;
    public static ICQContact contact;
    @SuppressLint("StaticFieldLeak")
    private static TextView encoding;
    @SuppressLint("StaticFieldLeak")
    private static ImageView mainStatus;
    @SuppressLint("StaticFieldLeak")
    private static TextView nick_;
    @SuppressLint("StaticFieldLeak")
    private static TextView nickname;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout opened_chats_markers;
    @SuppressLint("StaticFieldLeak")
    private static ImageView typing_field;
    @SuppressLint("StaticFieldLeak")
    private static ImageView xStatus;
    private final typing_thread TypingThread;
    private ChatAdapter chatAdp;
    public static boolean multiquoting = false;
    private static final boolean TOP_PANEL_VISIBLED = true;

    private ICQChatActivity(ChatInitCallback callback, ICQContact contact_) {
        setScrollStateHash(Integer.toHexString(utilities.getHash(contact_)));
        contact = contact_;
        this.init_callback = callback;
        this.TypingThread = new typing_thread(this, null);
        this.TypingThread.start();
        INITIALIZED = true;
    }

    public static ICQChatActivity getInstance(ICQContact contact_, ChatInitCallback init_callback) {
        return new ICQChatActivity(init_callback, contact_);
    }

    public static ICQChatActivity getInstance(String action, ChatInitCallback init_callback) {
        ICQProfile profile;
        ICQContact contact_ = null;
        if (action != null && action.startsWith("ITEM")) {
            String[] parts = utilities.split(action.substring(4), "***$$$SEPARATOR$$$***");
            if (parts.length != 2 || (profile = resources.service.profiles.getProfileByUIN(parts[0])) == null) {
                return null;
            }
            contact_ = profile.contactlist.getContactByUIN(parts[1]);
        }
        return new ICQChatActivity(init_callback, contact_);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        received_smile_tag = "";
        INITIALIZED = true;
        this.sp = getDefaultSharedPreferences();
        setVolumeControlStream(3);
        setContentView(resources.IT_IS_TABLET ? R.layout.chat_xhigh : R.layout.chat);
        input_manager = (InputMethodManager) getSystemService("input_method");
        service = resources.service;
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        Log.i("Fragment", "OnStart()");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 161 && resultCode == -1) {
            File file = new File(data.getAction());
            startSendFile(file);
        }
        if (requestCode == 162 && resultCode == -1) {
            received_smile_tag = data.getAction();
        }
    }

    private void startSendFile(File file) {
        if (contact.profile.connected) {
            contact.profile.createOutgoingFile(file, contact);
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        Dialog ad = null;
        if (id == 1 && contact != null) {
            UAdapter adp = new UAdapter();
            adp.setMode(2);
            adp.setTextSize(18);
            adp.setPadding(15);
            if (contact.isChating) {
                adp.put(resources.getString("s_close_chat"), 11);
            }
            if (contact.xstatus != null) {
                adp.put(resources.getString("s_req_status"), 6);
            }
            if (contact.hasUnreadedAuthRequest) {
                adp.put(resources.getString("s_grand_auth"), 8);
                adp.put(resources.getString("s_disallow_auth"), 9);
            }
            if (!contact.authorized) {
                adp.put(resources.getString("s_do_req_auth"), 10);
            }
            if (utilities.isUIN(contact.ID) && contact.profile.connected && contact.transfer_cookie == null) {
                adp.put(resources.getString("s_do_send_file"), 12);
            }
            adp.put(resources.getString("s_full_history"), 0);
            adp.put(resources.getString("s_encoding"), 7);
            if (!multiquoting) {
                adp.put(resources.getString("s_turn_on_multiquote"), 15);
            }
            if (multiquoting) {
                adp.put(resources.getString("s_turn_off_multiquote"), 16);
            }
            ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp, 48, new chatMenuListener(adp));
        }
        if (id == 2) {
            UAdapter adp2 = new UAdapter();
            adp2.setMode(2);
            adp2.setTextSize(18);
            adp2.setPadding(15);
            adp2.put("UTF8", 1);
            adp2.put("Windows-1251", 2);
            adp2.put("Unicode", 3);
            adp2.put("Ascii", 4);
            adp2.put(resources.getString("s_auto_encoding"), 5);
            ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp2, 48, new chatMenuListener(adp2));
        }
        if (id == 3) {
            UAdapter adp3 = new UAdapter();
            adp3.setMode(2);
            adp3.setTextSize(18);
            adp3.setPadding(15);
            adp3.put(resources.getString("s_copy"), 13);
            adp3.put(resources.getString("s_quote"), 14);
            adp3.put(resources.getString("s_copy_only_text"), 18);
            adp3.put(resources.getString("s_quote_only_text"), 17);
            return DialogBuilder.createWithNoHeader(this.ACTIVITY, adp3, 48, new chatMenuListener(adp3));
        }
        return ad;
    }

    private void intentHistoryWindow() {
        Intent i = new Intent();
        i.setAction("ICQ" + contact.profile.ID + "***$$$SEPARATOR$$$***" + contact.ID);
        i.setClass(this.ACTIVITY, ContactHistoryActivity.class);
        startActivity(i);
    }

    private void initSettings() {
        this.sendByEnter = this.sp.getBoolean("ms_send_by_enter", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        VISIBLE = true;
        if (service == null) {
            service = resources.service;
        }
        initSettings();
        initChat();
    }

    @Override
    public void onPause() {
        super.onPause();
        service.chatHdl = null;
        VISIBLE = false;
        this.TypingThread.forceStop();
        if (service != null) {
            service.isAnyChatOpened = false;
        }
        if (contact != null && this.input != null) {
            contact.typedText = this.input.getText().toString();
        }
        //noinspection DataFlowIssue
        MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
        multiquoting = false;
        this.messageList.setDragDropEnabled(true);
    }

    @Override
    public void onDestroy() {
        if (contact != null && contact.historyPreLoaded && !contact.isChating) {
            contact.clearPreloadedHistory();
        }
        if (this.chatAdp != null) {
            resetSelection();
        }
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

    private void updateContact() {
        String backup = this.input.getText().toString();
        ICQContact c = contact.profile.contactlist.getContactByUIN(contact.ID);
        if (c != null) {
            contact = c;
            initChatInterface(true);
        }
        this.input.setText(backup);
    }

    private void initChatInterface(boolean sliding) {
        int cursor_pos;
        service.cancelPersonalMessageNotify(utilities.getHash(contact));
        contact.setHasNoUnreadMessages();
        service.removeMessageNotify(contact);
        service.handleContactlistDatasetChanged();
        contact.hasUnreadedFileRequest = false;
        drawReceiverData();
        contact.loadLastHistory();
        if (this.chatAdp != null && this.chatAdp.isThatHistory(contact.history)) {
            this.chatAdp.refreshList();
        } else {
            this.chatAdp = new ChatAdapter(this.ACTIVITY, contact.history, 0, this.messageList);
            this.messageList.setAdapter(this.chatAdp);
        }
        int cursor_pos2 = this.input.getSelectionStart();
        this.input.setText(MessageSaveHelper.getMessage(this.SAVE_HASH));
        if (sliding) {
            this.chatAdp.notifyDataSetInvalidated();
        } else {
            this.chatAdp.refreshList();
        }
        service.isAnyChatOpened = true;
        nick_.setText(contact.profile.nickname);
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
        buildTransferField();
        if (PreferenceTable.auto_open_keyboard && !sliding) {
            service.runOnUi(new Runnable() {
                @Override
                public void run() {
                    ICQChatActivity.this.input.requestFocus();
                    ICQChatActivity.input_manager.showSoftInput(ICQChatActivity.this.input, 0);
                }
            }, 200L);
        }
        if (this.init_callback != null) {
            this.init_callback.chatInitialized();
            this.init_callback = null;
        }
    }

    @SuppressLint("SetTextI18n")
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
            typing_field.setImageDrawable(resources.typing);
        } else {
            typing_field.setImageDrawable(null);
        }
        if (contact.added) {
            if (contact.authorized) {
                if (utilities.isUIN(contact.ID)) {
                    mainStatus.setImageDrawable(resources.getStatusIcon(contact.status));
                } else {
                    mainStatus.setImageDrawable(resources.getMrimStatusIcon(contact.status));
                }
            } else {
                mainStatus.setImageDrawable(resources.unauthorized);
            }
        } else {
            mainStatus.setImageDrawable(resources.unauthorized);
        }
        Drawable x = contact.xstatus;
        if (x != null) {
            xStatus.setVisibility(View.VISIBLE);
            xStatus.setImageDrawable(x);
        } else {
            xStatus.setVisibility(android.view.View.GONE);
        }
        nickname.setText(contact.name);
        switch (contact.currentEncoding) {
            case -1:
                encoding.setText("");
                break;
            case 0:
                encoding.setText("UTF-8");
                break;
            case 1:
                encoding.setText("Windows-1251");
                break;
            case 2:
                encoding.setText("Unicode");
                break;
            case 3:
                encoding.setText("Ascii");
                break;
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        this.quot_view = (QuotingView) findViewById(R.id.chat_quoting_view);
        chat_back = (LinearLayout) findViewById(R.id.chat_back);
        mainStatus = (ImageView) findViewById(R.id.mainStatus);
        xStatus = (ImageView) findViewById(R.id.xStatus);
        nickname = (TextView) findViewById(R.id.nickname);
        nickname.setTextColor(ColorScheme.getColor(12));
        this.messageList = (ListViewA) findViewById(R.id.messages);
        this.messageList.setSelector(resources.getListSelector());
        this.messageList.setDragDropEnabledA(PreferenceTable.ms_dragdrop_quoting);
        this.messageList.setOnMultitouchListener(new ListViewA.MultitouchListener() {
            @Override
            public void onStart(View view, int top) {
                ICQChatActivity.this.quot_view.capture(view, top);
            }

            @Override
            public void onTouch(float x1, float y1) {
                boolean green = false;
                int[] location = new int[2];
                ICQChatActivity.this.input.getLocationOnScreen(location);
                if (y1 > location[1]) {
                    green = true;
                }
                ICQChatActivity.this.quot_view.updatePoints(x1, y1, green);
            }

            @Override
            public void onStop(float x1, float y1, int item_idx) {
                ICQChatActivity.this.quot_view.stop();
                int[] location = new int[2];
                ICQChatActivity.this.input.getLocationOnScreen(location);
                if (y1 <= location[1]) {
                    return;
                }
                ICQChatActivity.this.performQuote(item_idx);
            }
        });
        this.messageList.setSlideListener(new ListViewA.SlideListener() {
            @Override
            public void onStartDrag() {
                ICQChatActivity.this.messageList.clearAnimation();
            }

            @Override
            public void onMoving(float lastX, float offset) {
                ICQChatActivity.this.setViewBufferOffset((int) lastX, (int) offset);
            }

            @Override
            public void onFling(boolean toRight, float factor) {
                ICQChatActivity.this.setViewBufferOffset(0, 0);
                ICQChatActivity.this.handleFling(toRight, factor);
            }

            @Override
            public void onCancel(float absolute_offset, float space) {
                ICQChatActivity.this.setViewBufferOffset(0, 0);
                ICQChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(absolute_offset, space));
            }
        });
        this.messageList.setOnItemLongClickListener(new cl());
        this.messageList.setOnItemClickListener(new chat_click_listener());
        if (!getDefaultSharedPreferences().getBoolean("ms_use_solid_wallpaper", false)) {
            resources.attachChatMessagesBack(this.messageList);
        }
        if (!PreferenceTable.chat_dividers) {
            this.messageList.setDivider(null);
        }
        this.input = (EditText) findViewById(R.id.input);
        this.input.setTextSize(PreferenceTable.chatTextSize);
        Button button = (Button) findViewById(R.id.chat_menu_btn);
        if (utilities.hasHardwareMenuKey(getView().getContext())) {
            button.setVisibility(View.GONE);
        }
        resources.attachButtonStyle(button);
        button.setCompoundDrawables(resources.chat_menu_icon, null, null, null);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ICQChatActivity.this.removeDialog(1);
                ICQChatActivity.this.showDialog(1);
            }
        });
        this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener(new sndListener());
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
        nick_ = (TextView) findViewById(R.id.msg_nick);
        if (this.sp.getBoolean("ms_old_chat_style", true)) {
            nick_.setVisibility(View.GONE);
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
        typing_field = (ImageView) findViewById(R.id.typing_field);
        encoding = (TextView) findViewById(R.id.encoding);
        TOP_PANEL = (LinearLayout) findViewById(R.id.chat_header);
        if (!TOP_PANEL_VISIBLED) {
            TOP_PANEL.setVisibility(View.GONE);
        }
        TOP_PANEL.setBackgroundColor(ColorScheme.getColor(11));
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chat_bottom_panel);
        linearLayout.setBackgroundColor(ColorScheme.getColor(9));
        resources.attachChatTopPanel(TOP_PANEL);
        resources.attachChatBottomPanel(linearLayout);
        nick_.setTextSize(PreferenceTable.chatTextSize);
        this.input.setTextSize(PreferenceTable.chatTextSize);
        nick_.setTextColor(ColorScheme.getColor(22));
        opened_chats_markers = (LinearLayout) findViewById(R.id.chat_chats_markers);
        opened_chats_markers.setVisibility(PreferenceTable.ms_show_markers_in_chat ? View.VISIBLE : View.GONE);
        Button button2 = (Button) findViewById(R.id.chat_scroll_left);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Scroller", "To right");
                if (ICQChatActivity.service.opened_chats.size() < 2) {
                    ICQChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(32.0f, ICQChatActivity.this.messageList.getWidth()));
                } else {
                    ICQChatActivity.this.handleFling(true, 0.0f);
                }
            }
        });
        Button button3 = (Button) findViewById(R.id.chat_scroll_right);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Scroller", "To left");
                if (ICQChatActivity.service.opened_chats.size() < 2) {
                    ICQChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(-32.0f, ICQChatActivity.this.messageList.getWidth()));
                } else {
                    ICQChatActivity.this.handleFling(false, 0.0f);
                }
            }
        });
        if (this.sp.getBoolean("ms_scroll_arrows", false)) {
            button2.setVisibility(View.VISIBLE);
            button3.setVisibility(View.VISIBLE);
        }
    }

    private void performQuote(int idx) {
        String nick;
        String res;
        if (this.chatAdp != null) {
            HistoryItem item = this.chatAdp.getItem(idx);
            if (item.direction == 1) {
                nick = item.contact.name;
            } else {
                nick = item.contact.profile.nickname;
            }
            if (!item.isFileMessage && !item.isXtrazMessage && !item.isAuthMessage) {
                res = nick + " [" + item.formattedDate + "]:\n" + item.message;
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
                    if (item.itemType == 1) {
                        ImageView marker = new ImageView(resources.ctx);
                        marker.setPadding(3, 0, 3, 0);
                        marker.setImageDrawable(contact.equals(item) ? resources.marker_active_chat : resources.marker_chat);
                        if (((ICQContact) item).hasUnreadMessages) {
                            marker.setImageDrawable(resources.marker_msg_chat);
                        }
                        opened_chats_markers.addView(marker);
                    }
                }
            }
        }
    }

    private void setViewBufferOffset(int static_offset, int offset) {
        this.messageList.scrollTo(-(offset - static_offset), this.messageList.getScrollY());
    }

    private void handleFling(boolean toRight, float factor) {
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
                if (item.itemType == 1) {
                    contact = (ICQContact) item;
                    MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
                    setScrollStateHash(Integer.toHexString(utilities.getHash(contact)));
                    TranslateAnimation ta = new TranslateAnimation(1, factor, 1, 1.0f, 1, 0.0f, 1, 0.0f);
                    ta.setDuration(150L);
                    ta.setInterpolator(resources.ctx, android.R.anim.linear_interpolator);
                    ta.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ICQChatActivity.this.initChatInterface(true);
                            TranslateAnimation ta2 = new TranslateAnimation(1, -1.5f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                            ta2.setDuration(250L);
                            ta2.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
                            ICQChatActivity.this.messageList.startAnimation(ta2);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
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
                if (item2.itemType == 1) {
                    saveScrollState();
                    contact = (ICQContact) item2;
                    MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
                    setScrollStateHash(Integer.toHexString(utilities.getHash(contact)));
                    TranslateAnimation ta2 = new TranslateAnimation(1, factor, 1, -1.0f, 1, 0.0f, 1, 0.0f);
                    ta2.setDuration(150L);
                    ta2.setInterpolator(resources.ctx, android.R.anim.linear_interpolator);
                    ta2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ICQChatActivity.this.initChatInterface(true);
                            TranslateAnimation ta3 = new TranslateAnimation(1, 1.5f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                            ta3.setDuration(250L);
                            ta3.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
                            ICQChatActivity.this.messageList.startAnimation(ta3);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
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

    private class sndListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ICQChatActivity.this.doSend();
        }
    }

    private void doSend() {
        String message = this.input.getText().toString();
        if (!message.isEmpty()) {
            if (!this.input.getText().toString().trim().isEmpty() && contact.profile.connected) {
                ADB.proceedMessage(message);
                String[] prepared = prepareAndSplit(message);
                for (String part : prepared) {
                    if (!part.trim().isEmpty()) {
                        HistoryItem hst = new HistoryItem();
                        hst.message = part;
                        hst.contact = contact;
                        contact.history.add(hst);
                        contact.profile.sendMessage(contact.ID, part, hst);
                    }
                }
                this.chatAdp.refreshList();
                this.input.setText("");
                return;
            }
            return;
        }
        if (!resources.IT_IS_TABLET) {
            handleChatClosed();
        }
    }

    private String[] prepareAndSplit(String source) {
        int length = source.length();
        int count = length / 1024;
        if (count * 1024 < length) {
            count++;
        }
        String[] result = new String[count];
        int remain = length;
        for (int i = 0; i < count; i++) {
            int cut = Math.min(remain, 1024);
            String part = source.substring(0, cut);
            result[i] = part;
            source = source.substring(cut);
            remain -= cut;
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    private void updateTransferProgress() {
        FileTransfer t;
        TextView label = (TextView) findViewById(R.id.chat_file_transfer_progress_label);
        TextView percentage = (TextView) findViewById(R.id.chat_file_transfer_progress_percentage);
        PB progress_bar = (PB) findViewById(R.id.chat_file_transfer_progresss_bar);
        byte[] c = contact.transfer_cookie;
        if (c != null && (t = contact.profile.getTransfer(c)) != null) {
            if (t.direction == 1) {
                FileReceiver r = (FileReceiver) t;
                int percent = (int) (((r.received * 100) / r.file_size) + 1);
                percentage.setText(percent + " %");
                progress_bar.setMax(r.file_size);
                progress_bar.setProgress(r.received);
                label.setText(utilities.match(resources.getString("s_receiving"), new String[]{String.valueOf(r.files_received + 1), String.valueOf(r.files_count), r.file_name.trim(), FileTransfer.getSizeLabel(r.received), FileTransfer.getSizeLabel(r.file_size)}));
                return;
            }
            FileSender s = (FileSender) t;
            int percent2 = (int) (((s.sended * 100) / s.file_size) + 1);
            percentage.setText(percent2 + " %");
            progress_bar.setMax(s.file_size);
            progress_bar.setProgress(s.sended);
            label.setText(utilities.match(resources.getString("s_sending"), new String[]{s.file_name.trim(), FileTransfer.getSizeLabel(s.sended), FileTransfer.getSizeLabel(s.file_size)}));
        }
    }

    @SuppressLint("SetTextI18n")
    private void buildTransferField() {
        final FileTransfer t;
        LinearLayout field = (LinearLayout) findViewById(R.id.chat_file_transfer_layout);
        LinearLayout progress_field = (LinearLayout) findViewById(R.id.chat_file_transfer_progress_layout);
        TextView label = (TextView) findViewById(R.id.chat_file_transfer_progress_label);
        label.setCompoundDrawables(resources.file_transfering, null, null, null);
        TextView percentage = (TextView) findViewById(R.id.chat_file_transfer_progress_percentage);
        PB progress_bar = (PB) findViewById(R.id.chat_file_transfer_progresss_bar);
        Button accept = (Button) findViewById(R.id.chat_file_transfer_accept);
        Button decline = (Button) findViewById(R.id.chat_file_transfer_decline);
        field.setVisibility(View.GONE);
        byte[] c = contact.transfer_cookie;
        if (c != null && (t = contact.profile.getTransfer(c)) != null) {
            field.setVisibility(View.VISIBLE);
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICQChatActivity.contact.profile.cancelTransferAndSendRejection(t.cookie);
                }
            });
            if (t.direction == 1) {
                final FileReceiver r = (FileReceiver) t;
                if (!r.accepted) {
                    progress_field.setVisibility(View.GONE);
                    accept.setVisibility(View.VISIBLE);
                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            r.init();
                        }
                    });
                    if (r.files_count == 1) {
                        label.setText(utilities.match(resources.getString("s_incoming_file"), new String[]{r.file_name.trim(), FileTransfer.getSizeLabel(r.file_size)}));
                    } else {
                        label.setText(utilities.match(resources.getString("s_incoming_files"), new String[]{String.valueOf(r.files_count)}));
                    }
                    return;
                }
                progress_field.setVisibility(View.VISIBLE);
                accept.setVisibility(View.GONE);
                label.setText(utilities.match(resources.getString("s_receiving"), new String[]{String.valueOf(r.files_received + 1), String.valueOf(r.files_count), r.file_name.trim(), FileTransfer.getSizeLabel(r.received), FileTransfer.getSizeLabel(r.file_size)}));
                int percent = (int) (((r.received * 100) / r.file_size) + 1);
                percentage.setText(percent + " %");
                progress_bar.setMax(r.file_size);
                progress_bar.setProgress(r.received);
                return;
            }
            accept.setVisibility(View.GONE);
            FileSender s = (FileSender) t;
            if (!s.accepted) {
                progress_field.setVisibility(View.GONE);
                label.setText(utilities.match(resources.getString("s_preparing_to_send"), new String[]{s.file_name.trim(), FileTransfer.getSizeLabel(s.file_size)}));
                return;
            }
            progress_field.setVisibility(View.VISIBLE);
            label.setText(utilities.match(resources.getString("s_sending"), new String[]{s.file_name.trim(), FileTransfer.getSizeLabel(s.sended), FileTransfer.getSizeLabel(s.file_size)}));
            int percent2 = (int) ((s.sended * 100) / (s.file_size + 1));
            percentage.setText(percent2 + " %");
            progress_bar.setMax(s.file_size);
            progress_bar.setProgress(s.sended);
        }
    }

    private void handleIncomingTextMessage(HistoryItem msg) {
        this.chatAdp.refreshList();
    }

    private class chatMenuListener implements AdapterView.OnItemClickListener {
        UAdapter menu;

        public chatMenuListener(UAdapter adp) {
            this.menu = adp;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            String nick;
            String res;
            String nick2;
            ICQChatActivity.this.removeDialog(1);
            ICQChatActivity.this.removeDialog(2);
            ICQChatActivity.this.removeDialog(3);
            int id = (int) this.menu.getItemId(arg2);
            switch (id) {
                case 0:
                    ICQChatActivity.this.intentHistoryWindow();
                    break;
                case 1:
                    ICQChatActivity.contact.currentEncoding = 0;
                    ICQChatActivity.this.drawReceiverData();
                    break;
                case 2:
                    ICQChatActivity.contact.currentEncoding = 1;
                    ICQChatActivity.this.drawReceiverData();
                    break;
                case 3:
                    ICQChatActivity.contact.currentEncoding = 2;
                    ICQChatActivity.this.drawReceiverData();
                    break;
                case 4:
                    ICQChatActivity.contact.currentEncoding = 3;
                    ICQChatActivity.this.drawReceiverData();
                    break;
                case 5:
                    ICQChatActivity.contact.currentEncoding = -1;
                    ICQChatActivity.this.drawReceiverData();
                    break;
                case 6:
                    ICQChatActivity.contact.profile.sendXtrazRequest(ICQChatActivity.contact.ID, 0);
                    ICQChatActivity.this.drawReceiverData();
                    break;
                case 7:
                    ICQChatActivity.this.removeDialog(2);
                    ICQChatActivity.this.showDialog(2);
                    break;
                case 8:
                    ICQChatActivity.contact.hasUnreadedAuthRequest = false;
                    ICQChatActivity.contact.profile.sendAuthorizationReply(ICQChatActivity.contact.ID, 1);
                    break;
                case 9:
                    ICQChatActivity.contact.hasUnreadedAuthRequest = false;
                    ICQChatActivity.contact.profile.sendAuthorizationReply(ICQChatActivity.contact.ID, 0);
                    break;
                case 10:
                    ICQChatActivity.contact.profile.sendAuthorizationRequest(ICQChatActivity.contact.ID);
                    break;
                case 11:
                    ICQChatActivity.contact.profile.closeChat(ICQChatActivity.contact);
                    ICQChatActivity.this.handleChatClosed();
                    break;
                case 12:
                    Intent fb = new Intent();
                    fb.setClass(ICQChatActivity.this.ACTIVITY, FileBrowserActivity.class);
                    ICQChatActivity.this.ACTIVITY.startActivityForResult(fb, Chat.REQUEST_CODE_FILE);
                    break;
                case 13:
                    HistoryItem item = ICQChatActivity.this.chatAdp.getItem(ICQChatActivity.this.last_context_message);
                    ClipboardManager cm = (ClipboardManager) ICQChatActivity.this.getSystemService("clipboard");
                    if (item.direction == 1) {
                        nick2 = item.contact.name;
                    } else {
                        nick2 = item.contact.profile.nickname;
                    }
                    if (!item.isFileMessage && !item.isXtrazMessage && !item.isAuthMessage) {
                        //noinspection deprecation
                        cm.setText(nick2 + " [" + item.formattedDate + "]:\n" + item.message + "\n");
                    } else {
                        //noinspection deprecation
                        cm.setText("[" + item.formattedDate + "]:\n" + item.message + "\n");
                    }
                    Toast msg = Toast.makeText(ICQChatActivity.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg.setGravity(48, 0, 0);
                    msg.show();
                    break;
                case 14:
                    HistoryItem item2 = ICQChatActivity.this.chatAdp.getItem(ICQChatActivity.this.last_context_message);
                    if (item2.direction == 1) {
                        nick = item2.contact.name;
                    } else {
                        nick = item2.contact.profile.nickname;
                    }
                    if (!item2.isFileMessage && !item2.isXtrazMessage && !item2.isAuthMessage) {
                        res = nick + " [" + item2.formattedDate + "]:\n" + item2.message;
                    } else {
                        res = "[" + item2.formattedDate + "]:\n" + item2.message;
                    }
                    ICQChatActivity.this.input.setText(res + "\n");
                    ICQChatActivity.this.input.setSelection(res.length(), res.length());
                    break;
                case 15:
                    ICQChatActivity.multiquoting = true;
                    ICQChatActivity.this.messageList.setDragDropEnabled(false);
                    ICQChatActivity.this.chatAdp.notifyDataSetChanged();
                    break;
                case 16:
                    ICQChatActivity.multiquoting = false;
                    ICQChatActivity.this.messageList.setDragDropEnabled(true);
                    ICQChatActivity.this.resetSelection();
                    ICQChatActivity.this.chatAdp.notifyDataSetChanged();
                    break;
                case 17:
                    HistoryItem item3 = ICQChatActivity.this.chatAdp.getItem(ICQChatActivity.this.last_context_message);
                    if (item3.direction == 1) {
                        String nick3 = item3.contact.name;
                    } else {
                        String nick4 = item3.contact.profile.nickname;
                    }
                    ICQChatActivity.this.input.setText(item3.message + "\n");
                    ICQChatActivity.this.input.setSelection(item3.message.length());
                    break;
                case 18:
                    HistoryItem item4 = ICQChatActivity.this.chatAdp.getItem(ICQChatActivity.this.last_context_message);
                    if (item4.direction == 1) {
                        //noinspection unused
                        String nick5 = item4.contact.name;
                    } else {
                        //noinspection unused
                        String nick6 = item4.contact.profile.nickname;
                    }
                    //noinspection deprecation
                    ((ClipboardManager) ICQChatActivity.this.getSystemService("clipboard")).setText(item4.message + "\n");
                    Toast msg2 = Toast.makeText(ICQChatActivity.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg2.setGravity(48, 0, 0);
                    msg2.show();
                    break;
            }
        }
    }

    private void resetSelection() {
        for (int i = 0; i < this.chatAdp.getCount(); i++) {
            this.chatAdp.getItem(i).selected = false;
        }
    }

    private String computeMultiQuote() {
        String nick;
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < this.chatAdp.getCount(); i++) {
            HistoryItem hst = this.chatAdp.getItem(i);
            if (hst.selected) {
                if (hst.direction == 1) {
                    nick = hst.contact.name;
                } else {
                    nick = hst.contact.profile.nickname;
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

    private class chat_click_listener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (ICQChatActivity.multiquoting) {
                ChatAdapter adp = (ChatAdapter) arg0.getAdapter();
                HistoryItem hst = adp.getItem(arg2);
                hst.selected = !hst.selected;
                ICQChatActivity.this.chatAdp.notifyDataSetChanged();
                String quote = ICQChatActivity.this.computeMultiQuote();
                ICQChatActivity.this.input.setText(quote);
                ICQChatActivity.this.input.setSelection(quote.length(), quote.length());
            }
        }
    }

    private class el implements TextWatcher {
        private String buffer = "";

        public el() {
            if (!resources.IT_IS_TABLET) {
                ICQChatActivity.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
            } else {
                ICQChatActivity.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
            }
        }

        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!this.buffer.equals(s.toString()) && s.length() != 0) {
                if (s.length() - this.buffer.length() == 1) {
                    ADB.symbolTyped();
                }
                this.buffer = s.toString();
                ICQChatActivity.this.TypingThread.resetCounter();
            }
            if (ICQChatActivity.contact != null) {
                if (s.length() > 0) {
                    ICQChatActivity.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
                } else if (!resources.IT_IS_TABLET) {
                    ICQChatActivity.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
                }
            }
        }
    }

    private class inputKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View arg0, int keyCode, KeyEvent action) {
            if (keyCode != 66 || !ICQChatActivity.this.sendByEnter || action.getAction() != 0) {
                return false;
            }
            ICQChatActivity.this.doSend();
            return true;
        }
    }

    private class cl implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            ICQChatActivity.this.last_context_message = arg2;
            ICQChatActivity.this.removeDialog(3);
            ICQChatActivity.this.showDialog(3);
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

        /** @noinspection unused*/ /* synthetic */ typing_thread(ICQChatActivity iCQChatActivity, typing_thread typing_threadVar) {
            this();
        }

        public void resetCounter() {
            if (this.counter == 0 && !this.typing && ICQChatActivity.contact != null) {
                ICQChatActivity.contact.profile.sendTypingNotify(ICQChatActivity.contact.ID, 2);
                this.typing = true;
            }
            this.counter = 30;
        }

        public void forceStop() {
            if (this.typing) {
                ICQChatActivity.contact.profile.sendTypingNotify(ICQChatActivity.contact.ID, 0);
                this.typing = false;
            }
            this.counter = 0;
        }

        @Override
        public void run() {
            while (ICQChatActivity.INITIALIZED) {
                try {
                    //noinspection BusyWait
                    sleep(100L);
                } catch (Exception ignored) {
                }
                if (ICQChatActivity.INITIALIZED) {
                    if (this.counter != 0) {
                        this.counter--;
                        if (this.counter <= 0) {
                            ICQChatActivity.contact.profile.sendTypingNotify(ICQChatActivity.contact.ID, 0);
                            this.typing = false;
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                drawReceiverData();
                break;
            case 3:
                //noinspection DuplicateBranchesInSwitch
                drawReceiverData();
                break;
            case 4:
                try {
                    HistoryItem hst = (HistoryItem) msg.obj;
                    ContactlistItem cli = hst.contact;
                    //noinspection ConditionCoveredByFurtherCondition,ConstantValue
                    if (cli != null && (cli instanceof ICQContact) && cli.equals(contact)) {
                        handleIncomingTextMessage(hst);
                        break;
                    }
                } catch (Exception e) {
                    try {
                        ContactlistItem cli2 = (ContactlistItem) msg.obj;
                        //noinspection ConditionCoveredByFurtherCondition
                        if (cli2 != null && (cli2 instanceof ICQContact) && cli2.equals(contact)) {
                            this.chatAdp.refreshList();
                            return false;
                        }
                        return false;
                    } catch (Exception e2) {
                        return false;
                    }
                }
                break;
            case 5:
                try {
                    ContactlistItem cli3 = (ContactlistItem) msg.obj;
                    //noinspection ConditionCoveredByFurtherCondition
                    if (cli3 != null && (cli3 instanceof ICQContact) && cli3.equals(contact)) {
                        this.chatAdp.notifyDataSetChanged();
                        break;
                    }
                } catch (Exception e3) {
                    return false;
                }
                break;
            case 6:
                rebuildChatsMarkers();
                break;
            case Chat.REBUILD_TRANSFER_FIELD /* 60 */:
                buildTransferField();
                break;
            case Chat.UPDATE_TRANSFER_PROGRESS /* 61 */:
                updateTransferProgress();
                break;
            case Chat.CLOSE /* 62 */:
                handleChatClosed();
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
}