package ru.ivansuper.jasmin.chats;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.SmileysSelector;
import ru.ivansuper.jasmin.popup.PopupBuilder;
import ru.ivansuper.jasmin.popup.QuickAction;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.slide_tools.ListViewA;
import ru.ivansuper.jasmin.smileys_adapter;
import ru.ivansuper.jasmin.ui.ExFragment;

public abstract class Chat extends ExFragment implements Handler.Callback {
    /** @noinspection unused*/
    public static final int CHAT_SHOW_MENU = 97;
    /** @noinspection unused*/
    public static final int CHAT_UPDATE_CONTACT = 96;
    /** @noinspection unused*/
    public static final int CLOSE = 62;
    /** @noinspection unused*/
    public static final int DRAW_RECEIVER_DATA = 2;
    /** @noinspection unused*/
    public static final int REBUILD_CHAT = 4;
    /** @noinspection unused*/
    public static final int REBUILD_MARKERS = 6;
    /** @noinspection unused*/
    public static final int REBUILD_TRANSFER_FIELD = 60;
    /** @noinspection unused*/
    public static final int REFRESH_CHAT = 5;
    /** @noinspection unused*/
    public static final int REQUEST_CODE_FILE = 161;
    public static final int REQUEST_CODE_SMILE = 162;
    /** @noinspection unused*/
    public static final int UPDATE_TRANSFER_PROGRESS = 61;
    protected static InputMethodManager input_manager;
    public static String received_smile_tag = "";
    protected static jasminSvc service;
    protected QuickAction LAST_QUICK_ACTION;
    protected String SAVE_HASH;
    protected Handler hdl;
    protected ChatInitCallback init_callback;
    protected EditText input;
    protected int last_context_message = 0;
    protected ListViewA messageList;
    protected QuotingView quot_view;
    protected Button send;
    protected boolean sendByEnter;
    protected Button smileysSelectBtn;
    protected SharedPreferences sp;

    @Override
    public void onStart() {
        this.hdl = new Handler(this);
        service.chatHdl = this.hdl;
        super.onStart();
    }

    @Override
    public void onDestroy() {
        service.chatHdl = null;
        super.onDestroy();
    }

    protected final void setScrollStateHash(String hash) {
        this.SAVE_HASH = hash;
    }

    protected final void saveScrollState() {
        ScrollSaveHelper.putState(this.SAVE_HASH, this.messageList.onSaveInstanceState());
    }

    protected final void restoreScrollState() {
        final int mTranscript = this.messageList.getTranscriptMode();
        this.messageList.setTranscriptMode(0);
        Parcelable parcel = ScrollSaveHelper.getState(this.SAVE_HASH);
        if (parcel != null) {
            this.messageList.onRestoreInstanceState(parcel);
        }
        this.messageList.post(new Runnable() {
            @Override
            public void run() {
                Chat.this.messageList.setTranscriptMode(mTranscript);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration configuration, int diff) {
        if (diff > 48.0f * resources.dm.density && this.LAST_QUICK_ACTION != null) {
            this.LAST_QUICK_ACTION.dismiss();
        }
    }

    public void initViews() {
        View close_btn = findViewById(R.id.chat_close_btn);
        if (resources.IT_IS_TABLET) {
            close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Chat.this.handleChatClosed();
                }
            });
        }
    }

    protected final void handleChatClosed() {
        service.handleContactlistReturnToContacts();
        input_manager.hideSoftInputFromWindow(this.input.getWindowToken(), 2);
    }

    protected class smileySelectBtnListener implements View.OnClickListener {
        protected smileySelectBtnListener() {
        }

        @Override
        public void onClick(View v) {
            if (resources.IT_IS_TABLET) {
                Chat.this.LAST_QUICK_ACTION = PopupBuilder.buildGrid(
                        new smileys_adapter(),
                        v,
                        null,
                        PreferenceTable.smileysSelectorColumns,
                        JConference.BANNED_LIST_RECEIVED,
                        -1,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Chat.this.LAST_QUICK_ACTION.dismiss();
                                Chat.received_smile_tag = ((smileys_adapter) adapterView.getAdapter()).getTag(i);
                                int pos = Chat.this.input.getSelectionStart();
                                if (pos == -1) {
                                    pos = 0;
                                }
                                String typed = Chat.this.input.getText().toString();
                                if (pos > typed.length()) {
                                    pos = typed.length();
                                }
                                String first_part = typed.substring(0, pos) + " " + Chat.received_smile_tag + " ";
                                String current_text = first_part + typed.substring(pos);
                                Chat.received_smile_tag = "";
                                Chat.this.input.setText(current_text);
                                int cursor_pos = first_part.length();
                                Chat.this.input.setSelection(cursor_pos);
                            }
                        });
                Chat.this.LAST_QUICK_ACTION.show();
            } else {
                Intent i = new Intent(Chat.this.ACTIVITY, SmileysSelector.class);
                Chat.this.ACTIVITY.startActivityForResult(i, Chat.REQUEST_CODE_SMILE);
            }
        }
    }
}
