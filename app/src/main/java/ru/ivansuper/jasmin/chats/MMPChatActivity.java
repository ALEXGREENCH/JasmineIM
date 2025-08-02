package ru.ivansuper.jasmin.chats;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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

import java.io.IOException;

import ru.ivansuper.jasmin.ChatAdapter;
import ru.ivansuper.jasmin.ContactHistoryActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.slide_tools.AnimationCalculator;
import ru.ivansuper.jasmin.slide_tools.ListViewA;
import ru.ivansuper.jasmin.utilities;

/**
 * Activity for handling MMP (Mail.Ru Agent Protocol) chats.
 * This class extends the base {@link Chat} activity and implements {@link Handler.Callback}
 * to manage messages and UI updates related to MMP chat sessions.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Initializing and managing the chat interface for a specific {@link MMPContact}.
 *     <li>Handling user input, sending messages, and displaying chat history.
 *     <li>Managing contact status updates (e.g., typing indicators, online status).
 *     <li>Supporting features like multi-quoting, smiley selection, and message context menus.
 *     <li>Integrating with the application's service for background operations and notifications.
 *     <li>Providing UI elements for navigating between multiple open chats (if applicable).
 *     <li>Customizing chat appearance based on user preferences and themes.
 * </ul>
 *
 * <p>Instances of this activity are typically created via {@link #getInstance(MMPContact, ChatInitCallback)}
 * or {@link #getInstance(String, ChatInitCallback)} which allows for chat initialization based on a contact
 * object or an action string respectively.
 *
 * <p>The activity lifecycle methods ({@code onCreate}, {@code onStart}, {@code onResume}, {@code onPause},
 * {@code onDestroy}) are overridden to manage resources, initialize UI components, and save chat state.
 *
 * <p>It uses a {@link ChatAdapter} to display messages in a {@link ListViewA} and handles various
 * UI interactions through listeners and helper classes.
 */
public class MMPChatActivity extends Chat implements Handler.Callback {
    public static boolean INITIALIZED;
    /** @noinspection FieldCanBeLocal*/
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout TOP_PANEL;
    public static MMPContact contact;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout opened_chats_markers;
    private ChatAdapter chatAdp;
    /** @noinspection FieldCanBeLocal, unused */
    private LinearLayout chat_back;
    /** @noinspection FieldCanBeLocal*/
    private TextView encoding;
    private ImageView mainStatus;
    private TextView nick_;
    private TextView nickname;
    private ImageView typing_field;
    private ImageView xStatus;
    public static boolean multiquoting = false;
    public static boolean is_any_chat_opened = false;
    private static final boolean TOP_PANEL_VISIBLED = true;

    private MMPChatActivity(ChatInitCallback callback, MMPContact contact_) {
        setScrollStateHash(Integer.toHexString(utilities.getHash(contact_)));
        contact = contact_;
        this.init_callback = callback;
    }

    public static MMPChatActivity getInstance(MMPContact contact_, ChatInitCallback callback) {
        return new MMPChatActivity(callback, contact_);
    }

    public static MMPChatActivity getInstance(String action, ChatInitCallback callback) {
        MMPProfile profile;
        MMPContact contact_ = null;
        if (action != null && action.startsWith("ITEM")) {
            String[] parts = utilities.split(action.substring(4), "***$$$SEPARATOR$$$***");
            if (parts.length != 2 || (profile = resources.service.profiles.getProfileByEmail(parts[0])) == null) {
                return null;
            }
            contact_ = profile.getContactByID(parts[1]);
        }
        return new MMPChatActivity(callback, contact_);
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 162 && resultCode == -1) {
            received_smile_tag = data.getAction();
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
            adp.put(resources.getString("s_full_history"), 19);
            if (!multiquoting) {
                adp.put(resources.getString("s_turn_on_multiquote"), 15);
            }
            if (multiquoting) {
                adp.put(resources.getString("s_turn_off_multiquote"), 16);
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

    private void initSettings() {
        this.sendByEnter = this.sp.getBoolean("ms_send_by_enter", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        is_any_chat_opened = true;
        initSettings();
        initChat();
    }

    @Override
    public void onPause() {
        is_any_chat_opened = false;
        super.onPause();
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
        if (contact != null) {
            initChatInterface(false);
        }
    }

    private void initChatInterface(boolean fling) {
        int cursor_pos;
        service.cancelPersonalMessageNotify(utilities.getHash(contact));
        contact.setHasNoUnreadMessages();
        service.removeMessageNotify(contact);
        service.handleContactlistDatasetChanged();
        drawReceiverData();
        contact.loadLastHistory();
        if (this.chatAdp != null && this.chatAdp.isThatHistory(contact.history)) {
            this.chatAdp.refreshList();
        } else {
            this.chatAdp = new ChatAdapter(this.ACTIVITY, contact.history, 2, this.messageList);
            this.messageList.setAdapter(this.chatAdp);
        }
        if (fling) {
            this.chatAdp.notifyDataSetInvalidated();
        } else {
            this.chatAdp.refreshList();
        }
        service.isAnyChatOpened = true;
        this.nick_.setText(contact.profile.ID);
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
        contact.loadLastHistory();
        this.chatAdp.refreshList();
        rebuildChatsMarkers();
        if (PreferenceTable.auto_open_keyboard && !fling) {
            service.runOnUi(new Runnable() {
                @Override
                public void run() {
                    MMPChatActivity.this.input.requestFocus();
                    MMPChatActivity.input_manager.showSoftInput(MMPChatActivity.this.input, 0);
                }
            }, 200L);
        }
        if (this.init_callback != null) {
            this.init_callback.chatInitialized();
            this.init_callback = null;
        }
    }

    private void drawReceiverData() {
        ImageView avatar = (ImageView) findViewById(R.id.chat_avatar);
        if (PreferenceTable.ms_show_avatars) {
            avatar.setVisibility(View.VISIBLE);
            Bitmap bmp = null;
            if (contact.avatar != null) {
                bmp = ((BitmapDrawable) contact.avatar).getBitmap();
            }
            if (contact.avatar == null) {
                bmp = ((BitmapDrawable) resources.ctx.getResources().getDrawable(R.drawable.no_avatar)).getBitmap();
            }
            avatar.setImageBitmap(bmp);
        } else {
            avatar.setVisibility(View.GONE);
        }

        if (contact.typing) {
            this.typing_field.setImageDrawable(resources.typing);
        } else {
            this.typing_field.setImageDrawable(null);
        }
        if (contact.status != 0) {
            switch (contact.status) {
                case 1:
                case 3:
                case 4:
                    this.mainStatus.setImageDrawable(resources.mrim_online);
                    break;
                case 2:
                    this.mainStatus.setImageDrawable(resources.mrim_away);
                    break;
                case 5:
                    this.mainStatus.setImageDrawable(resources.mrim_dnd);
                    break;
                case 6:
                    this.mainStatus.setImageDrawable(resources.mrim_oc);
                    break;
                case 7:
                    this.mainStatus.setImageDrawable(resources.mrim_na);
                    break;
                case 8:
                    this.mainStatus.setImageDrawable(resources.mrim_lunch);
                    break;
                case 9:
                    this.mainStatus.setImageDrawable(resources.mrim_work);
                    break;
                case 10:
                    this.mainStatus.setImageDrawable(resources.mrim_home);
                    break;
                case 11:
                    this.mainStatus.setImageDrawable(resources.mrim_depress);
                    break;
                case 12:
                    this.mainStatus.setImageDrawable(resources.mrim_angry);
                    break;
                case 13:
                    this.mainStatus.setImageDrawable(resources.mrim_chat);
                    break;
            }
        } else {
            this.mainStatus.setImageDrawable(resources.mrim_offline);
        }
        this.xStatus.setVisibility(View.GONE);
        this.nickname.setText(contact.name);
    }

    @Override
    public void initViews() {
        super.initViews();
        this.quot_view = (QuotingView) findViewById(R.id.chat_quoting_view);
        this.chat_back = (LinearLayout) findViewById(R.id.chat_back);
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
                MMPChatActivity.this.quot_view.capture(view, top);
            }

            @Override
            public void onTouch(float x1, float y1) {
                boolean green = false;
                int[] location = new int[2];
                MMPChatActivity.this.input.getLocationOnScreen(location);
                if (y1 > location[1]) {
                    green = true;
                }
                MMPChatActivity.this.quot_view.updatePoints(x1, y1, green);
            }

            @Override
            public void onStop(float x1, float y1, int item_idx) {
                MMPChatActivity.this.quot_view.stop();
                int[] location = new int[2];
                MMPChatActivity.this.input.getLocationOnScreen(location);
                if (y1 <= location[1]) {
                    return;
                }
                MMPChatActivity.this.performQuote(item_idx);
            }
        });
        this.messageList.setSlideListener(new ListViewA.SlideListener() {
            @Override
            public void onStartDrag() {
                MMPChatActivity.this.messageList.clearAnimation();
            }

            @Override
            public void onMoving(float lastX, float offset) {
                MMPChatActivity.this.setViewBufferOffset((int) lastX, (int) offset);
            }

            @Override
            public void onFling(boolean toRight, float factor) {
                MMPChatActivity.this.setViewBufferOffset(0, 0);
                MMPChatActivity.this.handleFling(toRight, factor);
            }

            @Override
            public void onCancel(float absolute_offset, float space) {
                MMPChatActivity.this.setViewBufferOffset(0, 0);
                MMPChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(absolute_offset, space));
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MMPChatActivity.this.removeDialog(1);
                MMPChatActivity.this.showDialog(1);
            }
        });
        if (!resources.IT_IS_TABLET) {
            button.setVisibility(View.GONE);
        }
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
        this.encoding = (TextView) findViewById(R.id.encoding);
        this.encoding.setVisibility(View.GONE);
        findViewById(R.id.chat_file_transfer_layout).setVisibility(View.GONE);
        TOP_PANEL = (LinearLayout) findViewById(R.id.chat_header);
        if (!TOP_PANEL_VISIBLED) {
            TOP_PANEL.setVisibility(View.GONE);
        }
        TOP_PANEL.setBackgroundColor(ColorScheme.getColor(11));
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chat_bottom_panel);
        linearLayout.setBackgroundColor(ColorScheme.getColor(9));
        //noinspection deprecation
        if (!PreferenceManager.getDefaultSharedPreferences(this.ACTIVITY).getBoolean("ms_use_solid_wallpaper", false)) {
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
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i("Scroller", "To right");
                if (MMPChatActivity.service.opened_chats.size() < 2) {
                    MMPChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(32.0f, MMPChatActivity.this.messageList.getWidth()));
                } else {
                    MMPChatActivity.this.handleFling(true, 0.0f);
                }
            }
        });
        Button button3 = (Button) findViewById(R.id.chat_scroll_right);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i("Scroller", "To left");
                if (MMPChatActivity.service.opened_chats.size() < 2) {
                    MMPChatActivity.this.messageList.startAnimation(AnimationCalculator.calculateCancelAnimation(-32.0f, MMPChatActivity.this.messageList.getWidth()));
                } else {
                    MMPChatActivity.this.handleFling(false, 0.0f);
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
        if (this.chatAdp != null) {
            HistoryItem item = this.chatAdp.getItem(idx);
            if (item.direction == 1) {
                nick = item.mcontact.name;
            } else {
                nick = item.mcontact.profile.ID;
            }
            String res = nick + " [" + item.formattedDate + "]:\n" + item.message + "\n";
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
                    if (item.itemType == 7) {
                        ImageView marker = new ImageView(resources.ctx);
                        marker.setPadding(3, 0, 3, 0);
                        marker.setImageDrawable(contact.equals(item) ? resources.marker_active_chat : resources.marker_chat);
                        if (((MMPContact) item).hasUnreadMessages) {
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
                if (item.itemType == 7) {
                    contact = (MMPContact) item;
                    setScrollStateHash(Integer.toHexString(utilities.getHash(contact)));
                    TranslateAnimation ta = new TranslateAnimation(1, factor, 1, 1.0f, 1, 0.0f, 1, 0.0f);
                    ta.setDuration(150L);
                    ta.setInterpolator(resources.ctx, android.R.anim.linear_interpolator);
                    ta.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            MMPChatActivity.this.initChatInterface(true);
                            TranslateAnimation ta2 = new TranslateAnimation(1, -1.5f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                            ta2.setDuration(250L);
                            ta2.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
                            MMPChatActivity.this.messageList.startAnimation(ta2);
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
                if (item2.itemType == 7) {
                    saveScrollState();
                    contact = (MMPContact) item2;
                    setScrollStateHash(Integer.toHexString(utilities.getHash(contact)));
                    TranslateAnimation ta2 = new TranslateAnimation(1, factor, 1, -1.0f, 1, 0.0f, 1, 0.0f);
                    ta2.setDuration(150L);
                    ta2.setInterpolator(resources.ctx, android.R.anim.linear_interpolator);
                    ta2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            MMPChatActivity.this.initChatInterface(true);
                            TranslateAnimation ta3 = new TranslateAnimation(1, 1.5f, 1, 0.0f, 1, 0.0f, 1, 0.0f);
                            ta3.setDuration(250L);
                            ta3.setInterpolator(resources.ctx, android.R.anim.decelerate_interpolator);
                            MMPChatActivity.this.messageList.startAnimation(ta3);
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
            MMPChatActivity.this.doSend();
        }
    }

    private void doSend() {
        String message = this.input.getText().toString();
        if (!message.isEmpty()) {
            if (!this.input.getText().toString().trim().isEmpty() && contact.profile.connected) {
                ADB.proceedMessage(message);
                HistoryItem hst = new HistoryItem();
                hst.confirmed = false;
                hst.message = message;
                hst.mcontact = contact;
                contact.loadLastHistory();
                contact.history.add(hst);
                try {
                    contact.writeMessageToHistory(hst);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.chatAdp.refreshList();
                contact.profile.sendMessage(contact, hst);
                this.input.setText("");
                contact.profile.svc.playEvent(7);
                return;
            }
            return;
        }
        if (!resources.IT_IS_TABLET) {
            handleChatClosed();
        }
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
                    ContactlistItem cli = hst.mcontact;
                    //noinspection ConditionCoveredByFurtherCondition,ConstantValue
                    if (cli != null && (cli instanceof MMPContact) && cli.equals(contact)) {
                        handleIncomingTextMessage(hst);
                        break;
                    }
                } catch (Exception e) {
                    return false;
                }
                break;
            case 5:
                try {
                    ContactlistItem cli2 = (ContactlistItem) msg.obj;
                    //noinspection ConditionCoveredByFurtherCondition
                    if (cli2 != null && (cli2 instanceof MMPContact)) {
                        this.chatAdp.notifyDataSetChanged();
                        break;
                    }
                } catch (Exception e2) {
                    try {
                        MMPProfile p = (MMPProfile) msg.obj;
                        if (p != null && p.equals(contact.profile)) {
                            this.chatAdp.notifyDataSetChanged();
                            return false;
                        }
                        return false;
                    } catch (Exception e3) {
                        return false;
                    }
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
        MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
        MMPContact c = contact.profile.getContactByID(contact.ID);
        if (c != null) {
            contact = c;
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
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            String nick;
            String nick2;
            MMPChatActivity.this.removeDialog(1);
            MMPChatActivity.this.removeDialog(2);
            MMPChatActivity.this.removeDialog(3);
            int id = (int) this.menu.getItemId(arg2);
            switch (id) {
                case 11:
                    MMPChatActivity.contact.profile.closeChat(MMPChatActivity.contact);
                    MMPChatActivity.this.handleChatClosed();
                    break;
                case 13:
                    HistoryItem item = MMPChatActivity.this.chatAdp.getItem(MMPChatActivity.this.last_context_message);
                    ClipboardManager cm = (ClipboardManager) MMPChatActivity.this.getSystemService("clipboard");
                    if (item.direction == 1) {
                        nick2 = item.mcontact.name;
                    } else {
                        nick2 = item.mcontact.profile.ID;
                    }
                    //noinspection deprecation
                    cm.setText(nick2 + " [" + item.formattedDate + "]:\n" + item.message + "\n");
                    Toast msg = Toast.makeText(MMPChatActivity.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg.setGravity(48, 0, 0);
                    msg.show();
                    break;
                case 14:
                    HistoryItem item2 = MMPChatActivity.this.chatAdp.getItem(MMPChatActivity.this.last_context_message);
                    if (item2.direction == 1) {
                        nick = item2.mcontact.name;
                    } else {
                        nick = item2.mcontact.profile.ID;
                    }
                    String res = nick + " [" + item2.formattedDate + "]:\n" + item2.message + "\n";
                    MMPChatActivity.this.input.setText(res);
                    MMPChatActivity.this.input.setSelection(res.length(), res.length());
                    break;
                case 15:
                    MMPChatActivity.multiquoting = true;
                    MMPChatActivity.this.messageList.setDragDropEnabled(false);
                    MMPChatActivity.this.chatAdp.notifyDataSetChanged();
                    break;
                case 16:
                    MMPChatActivity.multiquoting = false;
                    MMPChatActivity.this.messageList.setDragDropEnabled(true);
                    MMPChatActivity.this.resetSelection();
                    MMPChatActivity.this.chatAdp.notifyDataSetChanged();
                    break;
                case 17:
                    HistoryItem item3 = MMPChatActivity.this.chatAdp.getItem(MMPChatActivity.this.last_context_message);
                    if (item3.direction == 1) {
                        //noinspection unused
                        String nick3 = item3.mcontact.name;
                    } else {
                        //noinspection unused
                        String nick4 = item3.mcontact.profile.ID;
                    }
                    MMPChatActivity.this.input.setText(item3.message + "\n");
                    MMPChatActivity.this.input.setSelection(item3.message.length());
                    break;
                case 18:
                    HistoryItem item4 = MMPChatActivity.this.chatAdp.getItem(MMPChatActivity.this.last_context_message);
                    if (item4.direction == 1) {
                        //noinspection unused
                        String nick5 = item4.mcontact.name;
                    } else {
                        //noinspection unused
                        String nick6 = item4.mcontact.profile.ID;
                    }
                    ClipboardManager cm2 = (ClipboardManager) MMPChatActivity.this.getSystemService("clipboard");
                    //noinspection deprecation
                    cm2.setText(item4.message + "\n");
                    Toast msg2 = Toast.makeText(MMPChatActivity.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg2.setGravity(48, 0, 0);
                    msg2.show();
                    break;
                case 19:
                    Intent i = new Intent(MMPChatActivity.this.ACTIVITY, ContactHistoryActivity.class);
                    i.setAction("MMP" + MMPChatActivity.contact.profile.ID + "***$$$SEPARATOR$$$***" + MMPChatActivity.contact.ID);
                    MMPChatActivity.this.startActivity(i);
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
                    nick = hst.mcontact.name;
                } else {
                    nick = hst.mcontact.profile.ID;
                }
                res.append(nick).append(" [").append(hst.formattedDate).append("]:\n").append(hst.message).append("\n");
            }
        }
        return res.toString();
    }

    private class chat_click_listener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (MMPChatActivity.multiquoting) {
                ChatAdapter adp = (ChatAdapter) arg0.getAdapter();
                HistoryItem hst = adp.getItem(arg2);
                hst.selected = !hst.selected;
                MMPChatActivity.this.chatAdp.notifyDataSetChanged();
                String quote = MMPChatActivity.this.computeMultiQuote();
                MMPChatActivity.this.input.setText(quote);
                MMPChatActivity.this.input.setSelection(quote.length(), quote.length());
            }
        }
    }

    private class el implements TextWatcher {
        private String buffer = "";

        public el() {
            if (!resources.IT_IS_TABLET) {
                MMPChatActivity.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
            } else {
                MMPChatActivity.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
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
            }
            if (MMPChatActivity.contact != null) {
                if (s.length() > 0) {
                    MMPChatActivity.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
                } else if (!resources.IT_IS_TABLET) {
                    MMPChatActivity.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
                }
            }
        }
    }

    private class inputKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View arg0, int keyCode, KeyEvent action) {
            if (keyCode != 66 || !MMPChatActivity.this.sendByEnter || action.getAction() != 0) {
                return false;
            }
            MMPChatActivity.this.doSend();
            return true;
        }
    }

    private class cl implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            MMPChatActivity.this.last_context_message = arg2;
            MMPChatActivity.this.removeDialog(3);
            MMPChatActivity.this.showDialog(3);
            return false;
        }
    }
}