package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.jabber.FileTransfer.FileTransfer;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.conference.Conference;

/**
 * Represents an item in the chat history.
 * This class stores information about a single message or event in a chat conversation,
 * including its content, sender, timestamp, and various flags indicating its type and status.
 */
public class HistoryItem {
    public boolean addTwoPoints;
    public int authType;
    public Conference conf;
    public String conf_nick;
    public JProfile conf_profile;
    public int conf_warn;
    public boolean confirmed;
    public ICQContact contact;
    public byte[] cookie;
    public long date;
    public int direction;
    public String formattedDate;
    public boolean isAuthMessage;
    public boolean isFileMessage;
    public boolean isMe;
    public boolean isTheme;
    public boolean isXtrazMessage;
    public boolean itIsForMe;
    public String jabber_cookie;
    public JContact jcontact;
    public FileTransfer jtransfer;
    public MMPContact mcontact;
    public String me;
    public String message;
    public SpannableStringBuilder messageS;
    public int mmp_cookie;
    public boolean selected;
    public boolean wakeup_alarm;
    public Drawable xTrazIcon;

    public HistoryItem() {
        this.date = 0L;
        this.formattedDate = "null";
        this.direction = 0;
        this.cookie = new byte[8];
        this.mmp_cookie = -1;
        this.confirmed = true;
        this.isXtrazMessage = false;
        this.isAuthMessage = false;
        this.isFileMessage = false;
        this.authType = 0;
        this.xTrazIcon = null;
        this.selected = false;
        this.conf_warn = 0;
        this.date = System.currentTimeMillis();
        getFormattedDate();
    }

    public HistoryItem(long time) {
        this.date = 0L;
        this.formattedDate = "null";
        this.direction = 0;
        this.cookie = new byte[8];
        this.mmp_cookie = -1;
        this.confirmed = true;
        this.isXtrazMessage = false;
        this.isAuthMessage = false;
        this.isFileMessage = false;
        this.authType = 0;
        this.xTrazIcon = null;
        this.selected = false;
        this.conf_warn = 0;
        this.date = time;
        if (this.date == 0) {
            this.date = System.currentTimeMillis();
        }
        getFormattedDate();
    }

    /** @noinspection UnusedReturnValue*/
    public final String getFormattedDate() {
        String format;
        if (Math.abs(System.currentTimeMillis() - this.date) / 1000 < 86400) {
            format = "HH:mm:ss";
        } else {
            format = "dd.MM.yyyy-HH:mm:ss";
        }
        Date dt = new Date(this.date);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        this.formattedDate = sdf.format(dt);
        return this.formattedDate;
    }

    /** @noinspection unused*/
    public final String fullFormattedDate() {
        Date dt = new Date(this.date);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy-HH:mm:ss");
        return sdf.format(dt);
    }
}