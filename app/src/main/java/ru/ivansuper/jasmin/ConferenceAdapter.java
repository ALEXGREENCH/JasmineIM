package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.ArrayList;
import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.ui.MyTextView;

public class ConferenceAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<HistoryItem> linkToHistory;
    /** @noinspection FieldCanBeLocal, unused */
    private final ListView list_view;
    private final ArrayList<HistoryItem> list = new ArrayList<>();
    /** @noinspection SpellCheckingInspection*/
    private int cutted = 0;

    public ConferenceAdapter(Context ctxParam, ArrayList<HistoryItem> history, ListView list) {
        this.ctx = ctxParam;
        this.linkToHistory = history;
        this.list_view = list;
        refreshList();
    }

    public boolean isThatHistory(ArrayList<HistoryItem> history) {
        return this.linkToHistory != null && history != null && this.linkToHistory.hashCode() == history.hashCode();
    }

    public void reset() {
        this.ctx = null;
        this.linkToHistory = null;
    }

    public void cutoff(int count) {
        for (int i = 0; i < count; i++) {
            this.linkToHistory.remove(0);
            this.list.remove(0);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public HistoryItem getItem(int arg0) {
        return this.list.get(arg0);
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
    public long getItemId(int arg0) {
        return arg0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout msg;
        int NICK_COLOR;
        int MESSAGE_COLOR;
        if (arg1 == null) {
            msg = (LinearLayout) LayoutInflater.from(this.ctx).inflate(R.layout.conference_item, null);
        } else {
            msg = (LinearLayout) arg1;
        }
        ImageView dragdrop = msg.findViewById(R.id.quote_button);
        dragdrop.setVisibility(PreferenceTable.ms_dragdrop_quoting ? View.VISIBLE : View.GONE);
        MyTextView message = msg.findViewById(R.id.msg_text);
        CheckBox selector = msg.findViewById(R.id.chat_item_checkbox);
        HistoryItem hst = getItem(arg0);
        String TIME = "[" + hst.formattedDate + "]";
        int TIME_COLOR = ColorScheme.getColor(10);
        int TIME_SIZE = PreferenceTable.chatTimeSize;
        int NICK_SIZE = PreferenceTable.chatTextSize;
        int MESSAGE_SIZE = PreferenceTable.chatTextSize;
        if (hst.message == null) {
            hst.message = "NULL";
        }
        String MESSAGE = hst.message;
        if (JConference.multiquoting) {
            selector.setVisibility(View.VISIBLE);
            selector.setChecked(hst.selected);
        } else {
            selector.setVisibility(View.GONE);
        }
        message.setTextSize(PreferenceTable.chatTextSize);
        String NICK = hst.conf_nick + (hst.addTwoPoints ? ": " : " ");
        if (hst.direction == 1) {
            NICK_COLOR = ColorScheme.getColor(17);
            MESSAGE_COLOR = ColorScheme.getColor(16);
            message.setLinkTextColor(ColorScheme.getColor(16));
            if (hst.conf_warn != 0 && hst.conf_nick.equals(hst.me)) {
                MESSAGE_COLOR = ColorScheme.getColor(2);
                message.setLinkTextColor(ColorScheme.getColor(2));
            }
            msg.setBackgroundColor(ColorScheme.getColor(14));
            resources.attachIngMsg(msg);
        } else {
            NICK_COLOR = ColorScheme.getColor(40);
            if (hst.confirmed) {
                MESSAGE_COLOR = ColorScheme.getColor(21);
                message.setLinkTextColor(ColorScheme.getColor(21));
            } else {
                MESSAGE_COLOR = ColorScheme.getColor(23);
                message.setLinkTextColor(ColorScheme.getColor(23));
            }
            msg.setBackgroundColor(ColorScheme.getColor(18));
            resources.attachOutMsg(msg);
        }
        boolean update_imgs = false;
        if (hst.isTheme) {
            TIME = "";
        }
        boolean highlight = false;
        if (hst.messageS == null) {
            SpannableStringBuilder res = new SpannableStringBuilder(TIME + NICK + MESSAGE);
            boolean it_is_for_me = hst.itIsForMe && !hst.isTheme && hst.conf_warn == 0;
            if (!hst.isTheme) {
                if (hst.conf_warn != 0) {
                    TIME_SIZE = (int) (TIME_SIZE / 1.2d);
                }
                res.setSpan(new StyleSpan(TIME_SIZE, TIME_COLOR, false), 0, TIME.length(), 33);
                if (hst.isTheme || hst.isMe || hst.conf_warn != 0) {
                    NICK_SIZE = (int) (NICK_SIZE / 1.2d);
                }
                res.setSpan(new StyleSpan(NICK_SIZE, NICK_COLOR, true), TIME.length(), TIME.length() + NICK.length(), 33);
                if (hst.isTheme || hst.isMe || hst.conf_warn != 0) {
                    MESSAGE_SIZE = (int) (MESSAGE_SIZE / 1.2d);
                }
                if (it_is_for_me) {
                    MESSAGE_COLOR = ColorScheme.getColor(42);
                }
                //noinspection DuplicateExpressions
                res.setSpan(new StyleSpan(MESSAGE_SIZE, MESSAGE_COLOR, hst.isTheme || hst.isMe || hst.conf_warn != 0), TIME.length() + NICK.length(), TIME.length() + NICK.length() + MESSAGE.length(), 33);
                if (it_is_for_me) {
                    highlight = true;
                    //noinspection DuplicateExpressions
                    res.setSpan(new BackgroundColorSpan(ColorScheme.getColor(41)), TIME.length() + NICK.length(), TIME.length() + NICK.length() + MESSAGE.length(), 33);
                }
            } else {
                res.setSpan(new StyleSpan((int) (NICK_SIZE / 1.2d), NICK_COLOR, true), 0, NICK.length(), 33);
                res.setSpan(new StyleSpan((int) (MESSAGE_SIZE / 1.2d), MESSAGE_COLOR, true), NICK.length(), NICK.length() + MESSAGE.length(), 33);
            }
            update_imgs = true;
            hst.messageS = MyTextView.detectLinks(res);
            hst.messageS = SmileysManager.getSmiledText(hst.messageS, TIME.length() + NICK.length(), highlight);
        }
        message.setText(hst.messageS, false);
        if (update_imgs && PreferenceTable.ms_links_to_images) {
            //noinspection DataFlowIssue
            hst.messageS = message.checkAndReplaceImageLinks(hst.messageS);
        }
        message.computeRefreshRate();
        msg.setDescendantFocusability(393216);
        message.relayout();
        return msg;
    }

    public void clear() {
        this.list.clear();
        notifyDataSetInvalidated();
    }

    protected void finalize() {
    }
}