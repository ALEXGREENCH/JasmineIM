package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.chats.ICQChatActivity;
import ru.ivansuper.jasmin.chats.JChatActivity;
import ru.ivansuper.jasmin.chats.MMPChatActivity;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.jabber.FileTransfer.TransferController;
import ru.ivansuper.jasmin.jabber.juick.TextParser;
import ru.ivansuper.jasmin.ui.MyTextView;

public class ChatAdapter extends BaseAdapter {
    private boolean IT_IS_JUICK;
    private Context ctx;
    private TextParser.OnIDClickedListener juick_listener;
    private ArrayList<HistoryItem> linkToHistory;
    /** @noinspection unused*/
    private final ListView list_view;
    private int profile_type;
    private final ArrayList<HistoryItem> list = new ArrayList<>();
    private int cutted = 0;

    public ChatAdapter(Context ctxParam, ArrayList<HistoryItem> history, int profile_type, ListView list) {
        //noinspection UnusedAssignment
        this.profile_type = -1;
        this.profile_type = profile_type;
        this.ctx = ctxParam;
        this.linkToHistory = history;
        this.list_view = list;
        refreshList();
    }

    public ChatAdapter(Context ctxParam, ArrayList<HistoryItem> history, int profile_type, boolean it_is_juick, ListView list) {
        //noinspection UnusedAssignment
        this.profile_type = -1;
        this.profile_type = profile_type;
        this.ctx = ctxParam;
        this.linkToHistory = history;
        this.IT_IS_JUICK = it_is_juick;
        this.list_view = list;
        refreshList();
    }

    public final void setJuick(boolean juick) {
        this.IT_IS_JUICK = juick;
    }

    public final void attachJuickListener(TextParser.OnIDClickedListener listener) {
        this.juick_listener = listener;
    }

    public boolean isThatHistory(ArrayList<HistoryItem> history) {
        return this.linkToHistory != null && history != null && this.linkToHistory.hashCode() == history.hashCode();
    }

    public void reset() {
        this.ctx = null;
        this.linkToHistory = null;
    }

    /** @noinspection unused*/
    public int getProfileType() {
        return this.profile_type;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public HistoryItem getItem(int position) {
        return this.list.get(position);
    }

    public void refreshList() {
        int limit;
        int sz_a = this.list.size() + this.cutted;
        int sz_b = this.linkToHistory.size();
        if (sz_a < sz_b) {
            this.list.addAll(this.linkToHistory.subList(sz_a, sz_b));
        }
        if (Manager.getBoolean("ms_messages_limit_enabled") && (limit = Manager.getStringInt("ms_messages_limit_value")) != 0) {
            int i = 0;
            while (this.list.size() > limit) {
                HistoryItem hst = this.list.get(i);
                if (hst.jtransfer == null) {
                    this.list.remove(i);
                    this.cutted++;
                    i--;
                }
                i++;
                if (i > this.list.size()) {
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout msg;
        if (arg1 == null) {
            msg = (LinearLayout) LayoutInflater.from(this.ctx).inflate(R.layout.chat_item, null);
        } else {
            msg = (LinearLayout) arg1;
        }
        View top_panel = msg.findViewById(R.id.chat_item_top_panel);
        ImageView dragdrop = msg.findViewById(R.id.quote_button);
        dragdrop.setVisibility(PreferenceTable.ms_dragdrop_quoting ? View.VISIBLE : View.GONE);
        TextView nick = msg.findViewById(R.id.msg_nick);
        nick.setVisibility(View.VISIBLE);
        if (!PreferenceTable.nickInChat) {
            nick.setVisibility(View.GONE);
        }
        TextView time = msg.findViewById(R.id.msg_time);
        MyTextView message = msg.findViewById(R.id.msg_text);
        message.setFocusable(false);
        LinearLayout status = msg.findViewById(R.id.msg_status);
        ImageView sts = msg.findViewById(R.id.msg_sts_icon);
        CheckBox selector = msg.findViewById(R.id.chat_item_checkbox);
        HistoryItem hst = getItem(arg0);
        if (arg0 > 0 && PreferenceTable.ms_use_messages_merging) {
            if (getItem(arg0 - 1).direction == hst.direction && !hst.isAuthMessage && !hst.isFileMessage && !hst.isXtrazMessage && hst.jtransfer == null) {
                top_panel.setVisibility(View.GONE);
            } else {
                top_panel.setVisibility(View.VISIBLE);
            }
        }
        time.setText(hst.formattedDate);
        nick.setTextSize(PreferenceTable.chatTextSize);
        time.setTextSize(PreferenceTable.chatTimeSize);
        time.setTextColor(ColorScheme.getColor(10));
        if (hst.message == null) {
            hst.message = "NULL";
        }
        String text_for_display = null;
        if (ICQChatActivity.multiquoting || JChatActivity.multiquoting || MMPChatActivity.multiquoting) {
            selector.setVisibility(View.VISIBLE);
            selector.setChecked(hst.selected);
        } else {
            selector.setVisibility(View.GONE);
        }
        message.setTextSize(PreferenceTable.chatTextSize);
        if (PreferenceTable.ms_chat_style == 1) {
            status.setVisibility(View.GONE);
            if (hst.direction == 1) {
                sts.setImageDrawable(resources.msg_in);
            } else if (hst.confirmed) {
                sts.setImageDrawable(resources.msg_out_c);
            } else {
                sts.setImageDrawable(resources.msg_out);
            }
        } else {
            status.setVisibility(View.VISIBLE);
            sts.setImageDrawable(null);
        }
        if (hst.isAuthMessage) {
            if (hst.contact != null) {
                nick.setText(hst.contact.name);
            }
            if (hst.jcontact != null) {
                nick.setText(hst.jcontact.name);
            }
            if (hst.mcontact != null) {
                nick.setText(hst.mcontact.name);
            }
            nick.setTextColor(ColorScheme.chat_inc_nick);
            switch (hst.authType) {
                case 0:
                    status.setBackgroundColor(ColorScheme.getColor(1));
                    msg.setBackgroundColor(ColorScheme.getColor(0));
                    resources.attachAuthDenMsg(msg);
                    message.setTextColor(ColorScheme.getColor(2));
                    message.setLinkTextColor(ColorScheme.getColor(2));
                    text_for_display = resources.getString("s_jabber_authorization_rejected");
                    break;
                case 1:
                    status.setBackgroundColor(ColorScheme.getColor(4));
                    msg.setBackgroundColor(ColorScheme.getColor(3));
                    resources.attachAuthAccMsg(msg);
                    message.setTextColor(ColorScheme.getColor(5));
                    message.setLinkTextColor(ColorScheme.getColor(5));
                    text_for_display = resources.getString("s_jabber_authorization_accepted");
                    break;
                case 2:
                    status.setBackgroundColor(ColorScheme.getColor(7));
                    msg.setBackgroundColor(ColorScheme.getColor(6));
                    resources.attachAuthAskMsg(msg);
                    message.setTextColor(ColorScheme.getColor(8));
                    message.setLinkTextColor(ColorScheme.getColor(8));
                    if (!hst.message.isEmpty()) {
                        text_for_display = resources.getString("s_icq_authorization_req") + ":\n" + hst.message;
                    } else {
                        text_for_display = resources.getString("s_icq_authorization_req");
                    }
                    break;
            }
        } else if (hst.direction == 1) {
            if (hst.contact != null) {
                nick.setText(hst.contact.name);
            }
            if (hst.jcontact != null) {
                nick.setText(hst.jcontact.name);
            }
            if (hst.mcontact != null) {
                nick.setText(hst.mcontact.name);
            }
            nick.setTextColor(ColorScheme.getColor(17));
            if (hst.isXtrazMessage) {
                nick.setVisibility(View.GONE);
                sts.setImageDrawable(null);
                status.setBackgroundColor(0);
                message.setTextColor(ColorScheme.getColor(26));
                message.setLinkTextColor(ColorScheme.getColor(26));
                msg.setBackgroundColor(ColorScheme.getColor(24));
                resources.attachStatusMsg(msg);
                time.setPadding(5, 5, 5, 5);
                time.setCompoundDrawables(hst.xTrazIcon, null, null, null);
            } else if (hst.isFileMessage) {
                nick.setText("");
                status.setBackgroundColor(ColorScheme.getColor(15));
                message.setTextColor(ColorScheme.getColor(16));
                message.setLinkTextColor(ColorScheme.getColor(16));
                msg.setBackgroundColor(ColorScheme.getColor(14));
                resources.attachTransferMsg(msg);
                time.setPadding(5, 5, 5, 5);
                time.setCompoundDrawables(resources.file_for_chat, null, null, null);
            } else {
                status.setBackgroundColor(ColorScheme.getColor(15));
                message.setTextColor(ColorScheme.getColor(16));
                message.setLinkTextColor(ColorScheme.getColor(16));
                msg.setBackgroundColor(ColorScheme.getColor(14));
                resources.attachIngMsg(msg);
                time.setPadding(0, 0, 0, 0);
                if (hst.wakeup_alarm) {
                    time.setCompoundDrawables(resources.mrim_wakeup, null, null, null);
                } else {
                    time.setCompoundDrawables(null, null, null, null);
                }
            }
        } else {
            if (hst.contact != null) {
                nick.setText(hst.contact.profile.nickname);
            }
            if (hst.jcontact != null) {
                nick.setText(hst.jcontact.profile.ID);
            }
            if (hst.mcontact != null) {
                nick.setText(hst.mcontact.profile.ID);
            }
            nick.setTextColor(ColorScheme.getColor(22));
            time.setPadding(0, 0, 0, 0);
            time.setCompoundDrawables(null, null, null, null);
            if (hst.confirmed) {
                status.setBackgroundColor(ColorScheme.getColor(19));
                message.setTextColor(ColorScheme.getColor(21));
                message.setLinkTextColor(ColorScheme.getColor(21));
            } else {
                status.setBackgroundColor(ColorScheme.getColor(20));
                message.setTextColor(ColorScheme.getColor(23));
                message.setLinkTextColor(ColorScheme.getColor(23));
            }
            msg.setBackgroundColor(ColorScheme.getColor(18));
            resources.attachOutMsg(msg);
        }
        boolean update_imgs = false;
        if (hst.messageS == null) {
            hst.messageS = MyTextView.detectLinks(hst.message);
            hst.messageS = SmileysManager.getSmiledText(hst.messageS, 0, false);
            if (this.IT_IS_JUICK) {
                //noinspection DataFlowIssue
                hst.messageS = TextParser.getInstance(this.juick_listener, message).parse(hst.messageS);
            }
            update_imgs = true;
        }
        if (text_for_display == null) {
            message.setText(hst.messageS, false);
            if (update_imgs && PreferenceTable.ms_links_to_images) {
                //noinspection DataFlowIssue
                hst.messageS = message.checkAndReplaceImageLinks(hst.messageS);
            }
        } else {
            message.setText(text_for_display);
        }
        message.computeRefreshRate();
        ViewGroup transfer_display = msg.findViewById(R.id.transfer_layout);
        if (hst.jtransfer != null) {
            TextView file_label = transfer_display.findViewById(R.id.transfer_file_name);
            file_label.setTextSize(PreferenceTable.chatTextSize);
            file_label.setTextColor(hst.jtransfer.direction == 0 ? ColorScheme.getColor(16) : ColorScheme.getColor(21));
            hst.jtransfer.setDisplay(transfer_display);
            message.setVisibility(View.GONE);
        } else {
            TransferController.clearDisplay(transfer_display);
            transfer_display.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
        }
        msg.setDescendantFocusability(393216);
        message.relayout();
        return msg;
    }

    public void clear() {
        this.list.clear();
        notifyDataSetInvalidated();
    }

    protected void finalize() throws Throwable {
        Log.e(getClass().getSimpleName(), "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
        super.finalize();
    }
}