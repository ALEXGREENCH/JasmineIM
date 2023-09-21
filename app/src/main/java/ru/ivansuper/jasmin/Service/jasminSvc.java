package ru.ivansuper.jasmin.Service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.nio.charset.IllegalCharsetNameException;
import java.util.DuplicateFormatFlagsException;
import java.util.Iterator;
import java.util.Vector;

import ru.ivansuper.jasmin.BReceiver;
import ru.ivansuper.jasmin.BufferedDialog;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.Media;
import ru.ivansuper.jasmin.MessagesDump;
import ru.ivansuper.jasmin.PopupTask;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.ProfilesManager;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.SearchActivity;
import ru.ivansuper.jasmin.SmileysManager;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.icq.InfoContainer;
import ru.ivansuper.jasmin.icq.SNAC;
import ru.ivansuper.jasmin.icq.SearchResultItem;
import ru.ivansuper.jasmin.jabber.AbstractForm;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.VCard;
import ru.ivansuper.jasmin.jabber.commands.Command;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.conference.ConferenceItem;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.log_adapter;
import ru.ivansuper.jasmin.main;
import ru.ivansuper.jasmin.plugins.kernel;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class jasminSvc extends Service implements SharedPreferences.OnSharedPreferenceChangeListener, Handler.Callback {

    public static final String ACTION_PING = "ru.ivansuper.jasmin.PING";
    public static final int ANTISPAM_NOTIFY_ID = 65531;
    @SuppressWarnings("unused")
    public static final int CLEAR_LOG = 5;
    public static IntentFilter INTENT_FILTER = null;
    public static final int MESSAGE_NOTIFY_ID = 65530;
    public static final int MULTICONNECT_NOTIFY_ID = 65532;
    @SuppressWarnings("unused")
    public static final int POPUP_MESSAGE = 6;
    @SuppressWarnings("unused")
    public static final int POPUP_MESSAGE_TASK = 8;
    @SuppressWarnings("unused")
    public static final int PUT_INTO_LOG = 4;
    @SuppressWarnings("unused")
    public static final int PUT_INTO_LOG_TASK = 7;
    public static final int REQUEST_CODE_PING = 241;
    public static AlarmManager alarm_manager;
    public static popup_log_adapter pla;
    public static Handler ui_thread;
    public Handler chatHdl;
    public Handler clHdl;
    public ContactListActivity cl_act;
    public ContactlistItem currentChatContact;
    public IMProfile currentChatProfile;
    @SuppressWarnings("unused")
    public boolean hideEmptyGroups;
    @SuppressWarnings("unused")
    public boolean hideOffline;
    @SuppressWarnings("unused")
    public ContactlistItem last_contact_for_non_multi_notify;
    private Media mp;
    private NotificationManager nm;
    private Notification notification;
    public SharedPreferences pm;
    public volatile ProfilesManager profiles;
    private BReceiver receiver;
    public Handler searchHdl;
    @SuppressWarnings("unused")
    public boolean showGroups;
    public Handler svcHdl;
    private PowerManager.WakeLock temp_wake_lock;
    public Vibrator vbr;
    @SuppressWarnings("unused")
    public boolean vibroEnabled;
    private PowerManager.WakeLock wake_lock;
    private WifiManager.WifiLock wifi_lock;
    public static boolean ACTIVE = false;
    public static final MessagesDump dump = new MessagesDump();
    private final IBinder myBinder = new itf();
    public boolean isAnyChatOpened = false;
    public boolean firstStart = true;
    @SuppressWarnings("unused")
    public int connectivity_state = 0;
    public log_adapter LOG_ADAPTER = new log_adapter();
    public final Vector<ContactlistItem> opened_chats = new Vector<>();
    public final Vector<String> wake_locks = new Vector<>();
    private final Vector<PendingIntentHandler> ping_tasks = new Vector<>();

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY_COMPATIBILITY;
        }
        String action = intent.getAction();
        if (action != null && action.equals(ACTION_PING)) {
            notifyPingTask(intent.getLongExtra("ID", -1L));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressWarnings("unused")
    private synchronized void holdWake(long id) {
        final String ID = "WAKE_HOLDER_" + utilities.RANDOM.nextLong();
        addWakeLock(ID);
        runOnUi(() -> jasminSvc.this.removeWakeLock(ID), 2500L);
    }

    @SuppressWarnings("unused")
    public synchronized void attachTimedTask(PendingIntentHandler handler, long interval) {
        if (handler != null) {
            Intent act = new Intent();
            act.setAction(ACTION_PING);
            act.putExtra("ID", handler.id);
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent intent = PendingIntent.getBroadcast(this, REQUEST_CODE_PING, act, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm_manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, intent);
            this.ping_tasks.add(handler);
            Log.e("TimedTask", "Added: " + handler.id + "     interval: " + interval);
        }
    }

    @SuppressWarnings("unused")
    public synchronized void removeTimedTask(PendingIntentHandler handler) {
        if (handler != null) {
            for (int i = 0; i < this.ping_tasks.size(); i++) {
                PendingIntentHandler pih = this.ping_tasks.get(i);
                if (pih.id == handler.id) {
                    alarm_manager.cancel(pih.intent);
                    this.ping_tasks.remove(handler);
                }
            }
            Log.e("TimedTask", "Removed: " + handler.id);
        }
    }

    public synchronized void notifyPingTask(long id) {
        for (int i = 0; i < this.ping_tasks.size(); i++) {
            PendingIntentHandler pih = this.ping_tasks.get(i);
            if (pih.id == id) {
                pih.run();
            }
        }
    }

    @SuppressLint({"InvalidWakeLockTag", "WakelockTimeout"})
    private void updateWake() {
        if (this.wake_lock == null) {
            if (this.wake_locks.size() > 0) {
                if (this.temp_wake_lock == null || !this.temp_wake_lock.isHeld()) {
                    PowerManager pMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    this.temp_wake_lock = pMan.newWakeLock(268435457, "ru.ivansuper.jasmin_reconnect_wake");
                    this.temp_wake_lock.acquire();
                }
            } else if (this.temp_wake_lock != null && this.temp_wake_lock.isHeld()) {
                this.temp_wake_lock.release();
            }
        }
    }

    public synchronized void addWakeLock(String tag) {
        if (!this.wake_locks.contains(tag)) {
            this.wake_locks.add(tag);
            updateWake();
        }
    }

    public synchronized void removeWakeLock(String tag) {
        if (this.wake_locks.contains(tag)) {
            this.wake_locks.remove(tag);
            updateWake();
        }
    }

    @SuppressWarnings("unused")
    public boolean isInOpenedChats(String ID) {
        Iterator<ContactlistItem> it = this.opened_chats.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) {
            ContactlistItem item = it.next();
            switch (item.itemType) {
                case 1:
                    if (!((ICQContact) item).ID.equals(ID)) {
                        break;
                    } else {
                        return true;
                    }
                case 4:
                    if (!((JContact) item).ID.equals(ID)) {
                        break;
                    } else {
                        return true;
                    }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void removeFromOpenedChats(String ID) {
        int i = 0;
        while (i < this.opened_chats.size()) {
            ContactlistItem item = this.opened_chats.get(i);
            switch (item.itemType) {
                case 1:
                    if (((ICQContact) item).ID.equals(ID)) {
                        this.opened_chats.removeElementAt(i);
                        i--;
                    }
                    break;
                case 4:
                    if (((JContact) item).ID.equals(ID)) {
                        this.opened_chats.removeElementAt(i);
                        i--;
                    }
                    break;
                case 7:
                    if (((MMPContact) item).ID.equals(ID)) {
                        this.opened_chats.removeElementAt(i);
                        i--;
                    }
                    break;
            }
            i++;
        }
    }

    @SuppressWarnings("unused")
    public boolean isNetworkAvailable() {
        return getInternet() && !getCallPresent();
    }

    private boolean getInternet() {
        NetworkInfo n;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && (n = cm.getActiveNetworkInfo()) != null && n.isAvailable() && n.isConnected();
    }

    private boolean getCallPresent() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return false;
        }
        return tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK || tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
    }

    void stopForegroundCompat(@SuppressWarnings({"SameParameterValue", "unused"}) int id) {
        stopForeground(true);
    }


    private void startFC() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Создаем канал уведомлений для Android 8.0 (Oreo) и выше
            createNotificationChannel();
        }

        Notification notification = getNotification(R.drawable.not_connected);

        if (notification != null) {
            startForeground(65331, notification);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "CHANNEL_JASMINE",
                "Jasmine IM Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification getNotification(int icon) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, main.class), PendingIntent.FLAG_MUTABLE);
        //this.notification.setLatestEventInfo(this, "Jasmine IM", "", contentIntent);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle("Jasmine IM")
                .setContentText("")
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("CHANNEL_JASMINE"); // Устанавливаем канал уведомлений для Android 8.0 (Oreo) и выше
        }

        return builder.build();
    }

    public class itf extends Binder {

        public jasminSvc getService() {
            return jasminSvc.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //boolean a = SNAC.sts.startsWith(utilities.randomized);
        //int zzs = Math.abs(3456);
        //int av = 234234 + zzs;
        //String aas = av + String.valueOf(av);
        //int zzd = av + aas.hashCode();
        //boolean b = SNAC.sts.startsWith(utilities.randomized2);
        //int zzs2 = Math.abs(zzd);
        //int av2 = 34545 + zzs2;
        //String aaf = String.valueOf(av2) + av2;
        //noinspection unused
        //int zze = av2 + aaf.hashCode();
        //if (!a && !b) {
            // TODO: :)
            // throw new NumberFormatException("");
        //}
        return this.myBinder;
    }

    @SuppressLint({"InvalidWakeLockTag", "WakelockTimeout"})
    @Override // android.app.Service
    public void onCreate() {
        ACTIVE = true;
        resources.service = this;
        alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        pla = new popup_log_adapter(this);
        ui_thread = new Handler(this);
        this.mp = new Media(this);
        this.svcHdl = new Handler(this);
        startFC();
        this.vbr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        this.nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.pm = PreferenceManager.getDefaultSharedPreferences(this);
        initSettings();
        this.pm.registerOnSharedPreferenceChangeListener(this);
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction("android.intent.action.PHONE_STATE");
        INTENT_FILTER.addAction("android.media.RINGER_MODE_CHANGED");
        INTENT_FILTER.addAction("android.intent.action.SCREEN_OFF");
        INTENT_FILTER.addAction("android.intent.action.SCREEN_ON");
        INTENT_FILTER.addAction(ACTION_PING);
        INTENT_FILTER.addAction(EventTranslator.ACTION);
        INTENT_FILTER.addAction("ru.ivansuper.jasmin.REQUEST_STATE");
        initPluginsKernel();
        this.receiver = new BReceiver(this);
        registerReceiver(this.receiver, INTENT_FILTER);
        if (this.pm.getBoolean("ms_wake_lock", false)) {
            PowerManager pMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.wake_lock = pMan.newWakeLock(1, "ru.ivansuper.jasmin_wake");
            this.wake_lock.acquire();
            Log.v("POWER", "WAKE_LOCK ENABLED");
        }
        if (this.pm.getBoolean("ms_wifi_lock", true)) {
            @SuppressLint("WifiManagerLeak")
            WifiManager pMan2 = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            this.wifi_lock = pMan2.createWifiLock(1, jasminSvc.class.getName());
            this.wifi_lock.acquire();
            Log.v("POWER", "WIFI_LOCK ENABLED");
        }
        EventTranslator.sendAppState(true);
        //boolean a = SNAC.sts.startsWith(utilities.randomized);
        //int zzs = Math.abs(3748);
        //int av = 502 + zzs;
        //String aas = String.valueOf(av) + av;
        //int zzd = av + aas.hashCode();
        //boolean b = SNAC.sts.startsWith(utilities.randomized2);
        //int zzs2 = Math.abs(zzd);
        //int av2 = 675 + zzs2;
        //String aaf = String.valueOf(av2) + av2;
        ////noinspection unused
        //int zze = av2 + aaf.hashCode();
        //if (!a && !b) {
        //    // TODO: :)
        //    // throw new IllegalCharsetNameException("");
        //}
    }

    @Override
    public void onDestroy() {
        stopForegroundCompat(65331);
        super.onDestroy();
    }

    public void performDestroying() {
        ACTIVE = false;
        Vector<IMProfile> p = this.profiles.getProfiles();
        for (int i = 0; i < p.size(); i++) {
            p.get(i).disconnect();
        }
        unregisterReceiver(this.receiver);
        if (this.wifi_lock != null && this.wifi_lock.isHeld()) {
            this.wifi_lock.release();
        }
        if (this.wifi_lock != null && this.wifi_lock.isHeld()) {
            this.wifi_lock.release();
        }
        if (this.temp_wake_lock != null && this.temp_wake_lock.isHeld()) {
            this.temp_wake_lock.release();
        }
        this.nm.cancelAll();
        EventTranslator.sendAppState(false);
    }

    @SuppressWarnings("unused")
    public void destroyNotification() {
        this.nm.cancelAll();
    }

    public void initPluginsKernel() {
        kernel.init();
    }

    @SuppressLint("WrongConstant")
    @SuppressWarnings("unused")
    private void createLogFloatingWindow() {
        int flags;
        PreferenceTable.ms_log_clickable = this.pm.getBoolean("ms_log_clickable", false);
        boolean clickable = PreferenceTable.ms_log_clickable;
        if (clickable) {
            int flags2 = 8;
            flags = flags2 + 32;
        } else {
            int flags3 = 8;
            flags = flags3 + 16;
        }
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-2, -2, 2003, flags, -3);
        lp.gravity = 53;
        LinearLayout lay = (LinearLayout) LayoutInflater.from(this).inflate(resources.IT_IS_TABLET ? R.layout.popup_log_window_xhigh : R.layout.popup_log_window, (ViewGroup) null);
        ListView log_list = (ListView) lay.findViewById(R.id.popup_log_list);
        log_list.setAdapter((ListAdapter) pla);
        wm.addView(lay, lp);
    }

    public void handleNetworkStateChanged(@SuppressWarnings("unused") int flags) {
    }

    @SuppressWarnings("unused")
    public void handleScreenTurnedOff() {
        Vector<IMProfile> list;
        if (this.profiles != null && (list = this.profiles.getProfiles()) != null) {
            int sz = list.size();
            for (int i = 0; i < sz; i++) {
                IMProfile profile = list.get(i);
                if (profile.connected) {
                    profile.handleScreenTurnedOff();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void handleScreenTurnedOn() {
        Vector<IMProfile> list;
        if (this.profiles != null && (list = this.profiles.getProfiles()) != null) {
            int sz = list.size();
            for (int i = 0; i < sz; i++) {
                IMProfile profile = list.get(i);
                if (profile.connected) {
                    profile.handleScreenTurnedOn();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void showInBarNotify(CharSequence header, CharSequence message, boolean led) {
        //noinspection deprecation
        Notification n = new Notification(R.drawable.icq_msg_in, null, System.currentTimeMillis());
        Intent intent = new Intent(this, main.class);
        /* - TODO: ...
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        n.setLatestEventInfo(this, header, message, contentIntent);
        */
        int flags = 0;
        if (led) {
            flags = 1;
            n.ledARGB = -16711936;
            n.ledOffMS = 1000;
            n.ledOnMS = 1000;
        }
        n.flags |= flags;
        this.nm.notify((int) SystemClock.uptimeMillis(), n);
    }

    @SuppressWarnings("unused")
    public void showPersonalMessageNotify(CharSequence header, CharSequence message, boolean led, int id, ContactlistItem contact) {
        if (PreferenceTable.multi_notify) {
            //noinspection deprecation
            Notification n = new Notification(R.drawable.icq_msg_in, null, 0L);
            Intent intent = new Intent(this, main.class);
            if (contact.itemType == 1) {
                ICQContact icontact = (ICQContact) contact;
                intent.setAction("ICQITEM" + icontact.profile.ID + "***$$$SEPARATOR$$$***" + icontact.ID);
            } else if (contact.itemType == 4) {
                JContact jcontact = (JContact) contact;
                intent.setAction("JBRITEM" + jcontact.profile.getFullJID() + "***$$$SEPARATOR$$$***" + jcontact.ID);
            } else if (contact.itemType == 7) {
                MMPContact mmpcontact = (MMPContact) contact;
                intent.setAction("MMPITEM" + mmpcontact.profile.ID + "***$$$SEPARATOR$$$***" + mmpcontact.ID);
            } else if (contact.itemType == 10) {
                ConferenceItem conf = (ConferenceItem) contact;
                intent.setAction("JCFITEM" + conf.conference.profile.getFullJID() + "***$$$SEPARATOR$$$***" + conf.conference.JID);
            }
            putMessageNotify(contact, header.toString(), message.toString());
            /* - TODO: ...
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
            n.setLatestEventInfo(this, header, message, contentIntent);
             */
            int flags = 0;
            if (led) {
                flags = 1;
                n.ledARGB = -16711936;
                n.ledOffMS = 1000;
                n.ledOnMS = 1000;
            }
            n.flags |= flags;
            int messages_count = 0;
            if (contact.itemType == 1) {
                messages_count = ((ICQContact) contact).getUnreadCount();
            } else if (contact.itemType == 4) {
                messages_count = ((JContact) contact).getUnreadCount();
            } else if (contact.itemType == 7) {
                messages_count = ((MMPContact) contact).getUnreadCount();
            } else if (contact.itemType == 10) {
                messages_count = ((ConferenceItem) contact).getUnreadCount();
            }
            n.icon = resources.getTrayMessageIconResId(messages_count);
            this.nm.notify(id, n);
        }
    }

    public synchronized void putMessageNotify(final ContactlistItem contact, final String nick, final String text) {
        runOnUi(() -> {
            MNotification mn = new MNotification();
            Intent intent = new Intent(jasminSvc.this, main.class);
            String scheme = IMProfile.getSchema(contact);
            intent.setAction(scheme);
            mn.intent = intent;
            mn.nick = nick;
            mn.text = text;
            mn.schema = IMProfile.getSchema(contact);
            NotifyManager.put(mn);
            jasminSvc.this.updateNotifyInternal();
        });
    }

    @SuppressWarnings("unused")
    public synchronized void removeMessageNotify(final ContactlistItem contact) {
        runOnUi(() -> {
            NotifyManager.remove(IMProfile.getSchema(contact));
            jasminSvc.this.updateNotifyInternal();
        });
    }

    @SuppressWarnings("unused")
    public synchronized void updateNotify() {
        //noinspection Convert2MethodRef
        runOnUi(() -> jasminSvc.this.updateNotifyInternal());
    }

    private synchronized void updateNotifyInternal() {
        int icon;
        if (ACTIVE && this.profiles != null) {
            if (this.profiles.isAnyProfileConnected()) {
                ADB.startOnlineCounter();
            } else {
                ADB.stopOnlineCounter();
            }
            dump.erase();
            this.profiles.getUnreadMessagesDump(dump);
            MNotification mn = null;
            if (dump.total_messages > 0 && NotifyManager.count() > 0) {
                mn = NotifyManager.get();
                EventTranslator.sendUnreadInfo(dump.total_messages, dump.from_contacts, mn.nick, mn.text, mn.schema);
            } else {
                EventTranslator.sendUnreadInfo(0, dump.from_contacts, "null", "null", "null");
            }
            if (dump.total_messages > 0 && NotifyManager.count() > 0 && !PreferenceTable.multi_notify) {
                if (this.nm != null) {
                    //noinspection ConstantConditions
                    String description = mn.nick.length() == 0 ? "" : mn.nick + ": " + mn.text;
                    showMessageNotification(utilities.match(resources.getString("s_unread_notify_text"), new String[]{String.valueOf(dump.total_messages), String.valueOf(dump.from_contacts)}), description, dump.total_messages, true, MESSAGE_NOTIFY_ID, mn.intent);
                } else {
                    this.profiles.isAnyProfileConnected();
                }
                this.clHdl.sendEmptyMessage(ContactListActivity.UPDATE_BLINK_STATE);
            } else {
                if (this.profiles.isAnyProfileConnected()) {
                    icon = R.drawable.connected;
                } else {
                    icon = R.drawable.not_connected;
                }
                cancelMessageNotification(MESSAGE_NOTIFY_ID);
                if (this.clHdl != null) {
                    this.clHdl.sendEmptyMessage(ContactListActivity.UPDATE_BLINK_STATE);
                }
                Notification n = getNotification(icon);
                this.nm.notify(65331, n);
            }
        }
    }

    public void showMessageNotification(@SuppressWarnings("unused") CharSequence header, @SuppressWarnings("unused") CharSequence message, int count, boolean led, int id, @SuppressWarnings("unused") Intent intent) {
        //noinspection deprecation
        Notification n = new Notification(R.drawable.icq_msg_in, null, 0L);
        /* - TODO: ...
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 134217728);
        n.setLatestEventInfo(this, header, message, contentIntent);
         */
        int flags = 0;
        if (led) {
            flags = 1;
            n.ledARGB = -16711936;
            n.ledOffMS = 1000;
            n.ledOnMS = 1000;
        }
        n.flags |= flags;
        n.icon = resources.getTrayMessageIconResId(count);
        this.nm.notify(id, n);
    }

    public void cancelMessageNotification(int id) {
        this.nm.cancel(id);
    }

    @SuppressWarnings("unused")
    public void showMailMessageNotify(CharSequence header, CharSequence message, boolean led, int id, int count, String JID) {
        //noinspection deprecation
        Notification n = new Notification(R.drawable.google_mail, null, 0L);
        Intent intent = new Intent(this, main.class);
        intent.setAction("%GMAIL%" + JID);
        /* - TODO: ...
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        n.setLatestEventInfo(this, header, message, contentIntent);
         */
        int flags = 0;
        if (led) {
            flags = 1;
            n.ledARGB = -16711936;
            n.ledOffMS = 1000;
            n.ledOnMS = 1000;
        }
        n.flags |= flags;
        n.number = count;
        this.nm.notify(id, n);
    }

    @SuppressWarnings("unused")
    public void cancelPersonalMessageNotify(int id) {
        this.nm.cancel(id);
    }

    @SuppressWarnings("unused")
    public void showAntispamNotify(String id, String message) {
        //noinspection deprecation
        this.notification = new Notification(R.drawable.cross, resources.getString("s_antispam_notify") + " " + id, System.currentTimeMillis());
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, main.class), 0);
        RemoteViews rv = new RemoteViews(getPackageName(), (int) R.layout.notify_remote_view);
        rv.setTextViewText(R.id.notify_remote_view, message);
        this.notification.icon = R.drawable.cross;
        this.notification.contentIntent = contentIntent;
        this.notification.contentView = rv;
        this.nm.notify(ANTISPAM_NOTIFY_ID, this.notification);
    }

    @SuppressWarnings("unused")
    public void cancelAntispamNotify() {
        this.nm.cancel(ANTISPAM_NOTIFY_ID);
    }

    @SuppressWarnings("unused")
    public void showTransferNotify(int id, String desc, String intent_scheme) {
        //noinspection deprecation
        this.notification = new Notification(R.drawable.file, desc, System.currentTimeMillis());
        Intent i = new Intent(this, ContactListActivity.class);
        i.setAction(intent_scheme);
        /* - TODO: ...
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 134217728);
        this.notification.contentIntent = contentIntent;
        this.notification.setLatestEventInfo(this, resources.getString("s_file_transfer_notify"), desc, contentIntent);
         */
        this.nm.notify(id, this.notification);
    }

    @SuppressWarnings("unused")
    public void cancelTransferNotify(int id) {
        this.nm.cancel(id);
    }

    @SuppressWarnings("unused")
    public void showMulticonnectNotify(String id) {
        //noinspection deprecation
        this.notification = new Notification(R.drawable.cross, resources.getString("s_moltilogin_notify") + " " + id, System.currentTimeMillis());
        /* - TODO: ...
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, main.class), 0);
        this.notification.setLatestEventInfo(this, "!!!", resources.getString("s_moltilogin_notify_desc"), contentIntent);
         */
        this.nm.notify(MULTICONNECT_NOTIFY_ID, this.notification);
    }

    public void cancelMultiloginNotify() {
        this.nm.cancel(MULTICONNECT_NOTIFY_ID);
    }

    @SuppressWarnings("unused")
    public void showCommandFormInContactList(Command cmd) {
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, ContactListActivity.SHOW_JABBER_CMD_FORM, cmd));
        }
    }

    @SuppressWarnings("unused")
    public void showXFormInContactList(AbstractForm form) {
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, 256, form));
        }
    }

    public void showMessageInContactList(String header, String message) {
        BufferedDialog dialog = new BufferedDialog(header, message);
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, 31, dialog));
        }
    }

    @SuppressWarnings("unused")
    public void showVCardInContactList(VCard vcard) {
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, 65, vcard));
        }
    }

    public void showVCardEditor(ru.ivansuper.jasmin.jabber.vcard.VCard vcard) {
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, 66, vcard));
        }
    }

    public void showChatMenu() {
        if (this.chatHdl != null) {
            this.chatHdl.sendMessage(Message.obtain(this.chatHdl, 97));
        }
    }

    @SuppressWarnings("unused")
    public void handleConferenceBannedListReceived(Conference conference) {
        if (JConference.INITIALIZED && JConference.conference.equals(conference)) {
            sendMessage(JConference.BANNED_LIST_RECEIVED, null, false);
        }
    }

    @SuppressWarnings("unused")
    public void handleChatNeedRefreshContact() {
        sendMessage(96, null, false);
    }

    public void handleContactlistReturnToContacts() {
        if (this.clHdl != null) {
            sendMessage(ContactListActivity.RETURN_TO_CONTACTS, null, false);
        }
    }

    @SuppressWarnings("unused")
    public void handleContactlistDatasetChanged() {
        sendMessage(2, null, false);
    }

    private void checkChats() {
        if (this.opened_chats.size() > 25) {
            ADB.setActivated(1);
        }
    }

    public void handleContactlistNeedRemake() {
        checkChats();
        sendMessage(3, null, false);
    }

    public void handleContactlistCheckConferences() {
        sendMessage(4, null, false);
    }

    public void handleProfileChanged() {
        sendMessage(1, null, false);
    }

    @SuppressWarnings("unused")
    public void closeChatIfShown() {
        sendMessage(62, null, false);
    }

    @SuppressWarnings("unused")
    public void handleChatUpdateInfo() {
        sendMessage(2, null, false);
    }

    @SuppressWarnings("unused")
    public void handleChatNeedRefresh(Object contact) {
        sendMessage(5, contact, true);
    }

    @SuppressWarnings("unused")
    public void handleChatNeedRebuild(Object contact) {
        sendMessage(4, contact, true);
    }

    @SuppressWarnings("unused")
    public void handleChatTransferNeedRebuild() {
        sendMessage(60, null, false);
    }

    @SuppressWarnings("unused")
    public void handleChatTransferRefreshProgress() {
        sendMessage(61, null, false);
    }

    @SuppressWarnings("unused")
    public void handleSearchResult(SearchResultItem item) {
        if (this.searchHdl != null) {
            this.searchHdl.sendMessage(Message.obtain(this.searchHdl, 0, item));
        }
    }

    @SuppressWarnings("unused")
    public void handleIncomingMessage() {
        if (PreferenceTable.soundEnabled) {
            this.mp.playEvent(0);
        }
        doVibrate(PreferenceTable.vibroLength);
    }

    @SuppressWarnings("unused")
    public void handleIncomingFile() {
        if (PreferenceTable.soundEnabled) {
            this.mp.playEvent(6);
        }
        doVibrate(PreferenceTable.vibroLength);
    }

    public void rebuildChatMarkers() {
        sendMessage(6, null, false);
        //boolean a = SNAC.sts.startsWith(utilities.randomized);
        //int zzs = Math.abs(32);
        //int av = 3234 + zzs;
        //String aas = String.valueOf(av) + av;
        //int zzd = av + aas.hashCode();
        //boolean b = SNAC.sts.startsWith(utilities.randomized2);
        //int zzs2 = Math.abs(zzd);
        //int av2 = 6778 + zzs2;
        //String aaf = String.valueOf(av2) + av2;
        ////noinspection unused
        //int zze = av2 + aaf.hashCode();
        //if (!a && !b) {
        //    throw new DuplicateFormatFlagsException("");
        //}
    }

    @SuppressWarnings("unused")
    public void handleIncomingMessage(MMPProfile profile, MMPContact contact, HistoryItem msg) {
        if (PreferenceTable.soundEnabled) {
            this.mp.playEvent(0);
        }
        doVibrate(PreferenceTable.vibroLength);
        if (this.isAnyChatOpened) {
            sendMessage(4, msg, true);
        }
        rebuildChatMarkers();
    }

    @SuppressWarnings("unused")
    public void handleIncomingMessage(HistoryItem msg) {
        if (PreferenceTable.soundEnabled) {
            this.mp.playEvent(0);
        }
        doVibrate(PreferenceTable.vibroLength);
        if (this.isAnyChatOpened) {
            sendMessage(4, msg, true);
        }
        rebuildChatMarkers();
    }

    @SuppressWarnings("unused")
    public void rebuildChat(ICQProfile profile, ICQContact contact, HistoryItem msg) {
        if (this.isAnyChatOpened) {
            sendMessage(4, msg, true);
        }
        rebuildChatMarkers();
    }

    public synchronized void playEvent(final int event) {
        if (PreferenceTable.soundEnabled) {
            runOnUi(() -> jasminSvc.this.mp.playEvent(event), 50L);
        }
    }

    @SuppressWarnings("unused")
    public void handleIncomingXtrazMessage(ICQProfile profile, ICQContact contact, HistoryItem msg) {
        sendMessage(4, msg, true);
    }

    @SuppressWarnings("unused")
    public void displayContactInfo(InfoContainer infoContainer) {
        if (!ContactListActivity.HIDDEN) {
            sendMessage(16, infoContainer, true);
        }
    }

    @SuppressWarnings("unused")
    public void displayContactInfoInSearch(InfoContainer infoContainer) {
        if (SearchActivity.VISIBLE && this.searchHdl != null) {
            this.searchHdl.sendMessage(Message.obtain(this.searchHdl, 1, infoContainer));
        }
    }

    @SuppressWarnings("unused")
    public void put_log(String variable) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 4, variable));
        }
    }

    @SuppressWarnings("unused")
    public void put_log(String variable, Runnable task, Drawable icon) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 7, new PopupTask(variable, task, icon)));
        }
    }

    @SuppressWarnings("unused")
    public void clear_log() {
        if (this.svcHdl != null) {
            this.svcHdl.sendEmptyMessage(5);
        }
    }

    @SuppressWarnings("unused")
    public void displayProgress() {
        if (this.clHdl != null) {
            this.clHdl.sendEmptyMessage(32);
        }
    }

    @SuppressWarnings("unused")
    public void displayProgress(String description) {
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, 32, description));
        }
    }

    @SuppressWarnings("unused")
    public void cancelProgress() {
        if (this.clHdl != null) {
            this.clHdl.sendEmptyMessage(33);
        }
    }

    public void doVibrate(long how_long) {
        if (Media.phone_mode == 0 && this.vbr != null && PreferenceTable.vibroEnabled) {
            this.vbr.vibrate(how_long);
        }
    }

    @SuppressWarnings("unused")
    public void doVibrate(long[] how_long) {
        if (Media.phone_mode == 0 && this.vbr != null && PreferenceTable.vibroEnabled) {
            this.vbr.vibrate(how_long, -1);
        }
    }

    public void sendMessage(int what, Object obj, boolean useObject) {
        Message msg = null;
        Message msgA = null;
        if (useObject) {
            if (this.clHdl != null) {
                msg = Message.obtain(this.clHdl, what, obj);
            }
        } else if (this.clHdl != null) {
            msg = Message.obtain(this.clHdl, what);
        }
        if (this.chatHdl != null) {
            msgA = Message.obtain(this.chatHdl, what, obj);
        }
        if (this.clHdl != null) {
            if (what == 3) {
                this.clHdl.removeMessages(3);
                this.clHdl.sendMessageDelayed(msg, 0L);
            } else if (what == 2) {
                this.clHdl.removeMessages(2);
                this.clHdl.sendMessageDelayed(msg, 0L);
            } else {
                this.clHdl.sendMessageDelayed(msg, 0L);
            }
        }
        if (this.chatHdl != null && msgA != null) {
            this.chatHdl.sendMessageDelayed(msgA, 50L);
        }
    }

    @SuppressWarnings("unused")
    public String getAntispamQuestion() {
        return this.pm.getString("ms_as_question", resources.getString("s_default_question"));
    }

    @SuppressWarnings("unused")
    public String getAntispamAnswer() {
        return this.pm.getString("ms_as_answer", resources.getString("s_default_answer"));
    }

    @SuppressWarnings("unused")
    public String getAntispamAllowed() {
        return this.pm.getString("ms_as_allowed", resources.getString("s_default_user_allowed"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
        initSettings();
        if (key.contains("ms_smileys_scale")) {
            if (SmileysManager.packLoaded) {
                SmileysManager.forceChangeScale();
            }
        } else if (key.contains("ms_enable_x_in_bottom_panel")) {
            handleProfileChanged();
        }
    }

    @SuppressWarnings("unused")
    public void forcePopUp(String value) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 6, value));
        }
    }

    @SuppressWarnings("unused")
    public void forcePopUp(String variable, Runnable task, Drawable icon) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 8, new PopupTask(variable, task, icon)));
        }
    }

    @SuppressLint("ApplySharedPref")
    private void initSettings() {
        PreferenceTable.vibroEnabled = this.pm.getBoolean("ms_vibro", true);
        PreferenceTable.showGroups = this.pm.getBoolean("ms_groups", true);
        PreferenceTable.hideOffline = this.pm.getBoolean("ms_offline", true);
        PreferenceTable.hideEmptyGroups = this.pm.getBoolean("ms_emptygroups", true);
        try {
            PreferenceTable.clTextSize = Integer.parseInt(this.pm.getString("ms_cl_font_size", "16"));
        } catch (Exception e) {
            this.pm.edit().putString("ms_cl_font_size", "16").commit();
        }
        try {
            PreferenceTable.chatTextSize = Integer.parseInt(this.pm.getString("ms_chat_text_size", "16"));
        } catch (Exception e2) {
            this.pm.edit().putString("ms_chat_text_size", "16").commit();
        }
        try {
            PreferenceTable.chatTimeSize = Integer.parseInt(this.pm.getString("ms_chat_time_size", "14"));
        } catch (Exception e3) {
            this.pm.edit().putString("ms_chat_time_size", "14").commit();
        }
        try {
            PreferenceTable.vibroLength = Long.parseLong(this.pm.getString("ms_vibro_length", "200"));
        } catch (Exception e4) {
            this.pm.edit().putString("ms_vibro_length", "200").commit();
        }
        try {
            PreferenceTable.smileys_scale = Integer.parseInt(this.pm.getString("ms_smileys_scale", "2"));
        } catch (Exception e5) {
            this.pm.edit().putString("ms_smileys_scale", "2").commit();
        }
        PreferenceTable.preloadHistory = this.pm.getBoolean("ms_preload_history", true);
        PreferenceTable.writeHistory = this.pm.getBoolean("ms_use_history", true);
        PreferenceTable.realtimeHistoryExport = this.pm.getBoolean("ms_realtime_history_record", false);
        PreferenceTable.nickInChat = this.pm.getBoolean("ms_nick_in_chat", false);
        try {
            PreferenceTable.smileysSelectorColumns = Integer.parseInt(this.pm.getString("ms_columns", "5"));
        } catch (Exception e6) {
            this.pm.edit().putString("ms_columns", "5").commit();
        }
        try {
            PreferenceTable.simple_cl_columns = Integer.parseInt(this.pm.getString("ms_cl_columns", "1"));
        } catch (Exception e7) {
            this.pm.edit().putString("ms_cl_columns", "1").commit();
        }
        PreferenceTable.sendByEnter = this.pm.getBoolean("ms_send_by_enter", false);
        PreferenceTable.soundEnabled = this.pm.getBoolean("ms_sounds", true);
        PreferenceTable.autoCloseSmileysSelector = this.pm.getBoolean("ms_auto_close_smileys_selector", true);
        PreferenceTable.as_only_roster = this.pm.getBoolean("ms_as_only_roster", false);
        PreferenceTable.as_enable_notify = this.pm.getBoolean("ms_as_notify", true);
        PreferenceTable.wake_lock = this.pm.getBoolean("ms_wake_lock", true);
        PreferenceTable.wifi_lock = this.pm.getBoolean("ms_wifi_lock", true);
        PreferenceTable.multi_notify = this.pm.getBoolean("ms_notify_mode", false);
        PreferenceTable.simple_cl = this.pm.getBoolean("ms_simple_list", false);
        PreferenceTable.pg_status = this.pm.getBoolean("ms_pg_status", true);
        PreferenceTable.use_ping = this.pm.getBoolean("ms_use_ping", true);
        try {
            PreferenceTable.ping_freq = Integer.parseInt(this.pm.getString("ms_ping_freq", "60"));
        } catch (Exception e8) {
            this.pm.edit().putString("ms_ping_freq", "60").commit();
        }
        PreferenceTable.auto_close_status_selector = this.pm.getBoolean("ms_auto_close_selector", true);
        PreferenceTable.as_qestion_enabled = this.pm.getBoolean("ms_as_qest_enable", true);
        PreferenceTable.auto_change_status = this.pm.getBoolean("ms_turn_sts_on_screen", false);
        try {
            PreferenceTable.auto_change_status_timeout = Integer.parseInt(this.pm.getString("ms_turn_sts_timeout", "300"));
        } catch (Exception e9) {
            this.pm.edit().putString("ms_turn_sts_timeout", "300").commit();
        }
        PreferenceTable.show_away_in_cl = this.pm.getBoolean("ms_show_away", false);
        PreferenceTable.send_typing_notify = this.pm.getBoolean("ms_typing_notify", false);
        PreferenceTable.auto_close_chat = this.pm.getBoolean("ms_auto_close_chat", false);
        PreferenceTable.auto_xtraz = this.pm.getBoolean("ms_auto_xtraz", false);
        PreferenceTable.auto_open_keyboard = this.pm.getBoolean("ms_auto_open_keyboard", false);
        PreferenceTable.auto_cap = this.pm.getBoolean("ms_use_auto_cap", true);
        PreferenceTable.enable_x_in_bottom_panel = this.pm.getBoolean("ms_enable_x_in_bottom_panel", true);
        PreferenceTable.log_xtraz_reading = this.pm.getBoolean("ms_log_xtraz_reading", true);
        PreferenceTable.log_online = this.pm.getBoolean("ms_log_online", true);
        PreferenceTable.log_offline = this.pm.getBoolean("ms_log_offline", true);
        PreferenceTable.use_popup = this.pm.getBoolean("ms_use_popup", true);
        PreferenceTable.use_contactlist_items_shadow = this.pm.getBoolean("ms_use_items_shadow", true);
        PreferenceTable.chat_zebra = this.pm.getBoolean("ms_chat_zebra", true);
        PreferenceTable.chat_dividers = this.pm.getBoolean("ms_chat_dividers", false);
        PreferenceTable.triple_vibro = this.pm.getBoolean("ms_triple_vibro", true);
        PreferenceTable.ms_animated_smileys = this.pm.getBoolean("ms_animated_smileys", true);
        PreferenceTable.ms_show_avatars = this.pm.getBoolean("ms_show_avatars", false);
        PreferenceTable.ms_chats_at_top = this.pm.getBoolean("ms_chats_at_top", false);
        PreferenceTable.ms_chat_style = Integer.parseInt(this.pm.getString("ms_chat_style", "0"));
        PreferenceTable.ms_dragdrop_quoting = this.pm.getBoolean("ms_dragdrop_quoting", true);
        PreferenceTable.ms_hide_not_connected_profiles = this.pm.getBoolean("ms_hide_not_connected_profiles", false);
        PreferenceTable.ms_links_to_images = this.pm.getBoolean("ms_links_to_images", false);
        PreferenceTable.ms_use_overscroll = this.pm.getBoolean("ms_use_overscroll", true);
        PreferenceTable.s_ms_show_xstatuses = this.pm.getBoolean("ms_show_xstatuses", true);
        PreferenceTable.s_ms_show_clients = this.pm.getBoolean("ms_show_clients", true);
        PreferenceTable.ms_check_tls_certificate = this.pm.getBoolean("ms_check_tls_certificate", true);
        PreferenceTable.ms_show_markers_in_chat = this.pm.getBoolean("ms_show_markers_in_chat", true);
        PreferenceTable.ms_two_screens_mode = this.pm.getBoolean("ms_two_screens_mode", true);
        PreferenceTable.ms_cl_transition_effect = this.pm.getInt("ms_cl_transition_effect", 7);
        PreferenceTable.ms_rejoin_to_conferences = this.pm.getBoolean("ms_restore_conf_presence", false);
        PreferenceTable.ms_use_bookmark_autojoin = this.pm.getBoolean("ms_use_bookmark_autojoin", false);
        PreferenceTable.ms_use_messages_merging = this.pm.getBoolean("ms_use_messages_merging", false);
        PreferenceTable.ms_messages_limit_enabled = this.pm.getBoolean("ms_messages_limit_enabled", true);
    }

    public void runOnUi(Runnable task) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.svcHdl) {
            this.svcHdl.post(task);
        }
    }

    public void runOnUi(Runnable task, long delay) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.svcHdl) {
            this.svcHdl.postDelayed(task, delay);
        }
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message arg0) {
        switch (arg0.what) {
            case 0:
                updateNotifyInternal();
                return false;
            case 1:
                handleNetworkStateChanged(arg0.arg1);
                return false;
            case 2:
            default:
                return false;
            case 3:
                if (arg0.obj != null) {
                    try {
                        Thread task_thread = new Thread((Runnable) arg0.obj);
                        task_thread.start();
                        return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return false;
            case 4:
                this.LOG_ADAPTER.put((String) arg0.obj);
                return false;
            case 5:
                this.LOG_ADAPTER.clear();
                return false;
            case 6:
                pla.put((String) arg0.obj);
                return false;
            case 7:
                this.LOG_ADAPTER.put(((PopupTask) arg0.obj).variable);
                return false;
            case 8:
                PopupTask task = (PopupTask) arg0.obj;
                pla.put(task.variable, task.task, task.icon);
                return false;
        }
    }

    @SuppressWarnings("unused")
    public final void moveToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        //noinspection deprecation
        cm.setText(text);
        Toast toast = Toast.makeText(this, Locale.getString("s_copied"), Toast.LENGTH_SHORT);
        toast.setGravity(48, 0, 0);
        toast.show();
    }

    public final void showToast(final String text, final int length) {
        runOnUi(() -> Toast.makeText(jasminSvc.this, text, length).show());
    }

    private static class NotifyManager {

        private static final Vector<MNotification> mList = new Vector<>();

        public static synchronized void put(MNotification n) {
            synchronized (NotifyManager.class) {
                remove(n.schema);
                mList.insertElementAt(n, 0);
            }
        }

        public static synchronized MNotification get() {
            MNotification mNotification;
            synchronized (NotifyManager.class) {
                mNotification = mList.size() == 0 ? null : mList.get(0);
            }
            return mNotification;
        }

        public static synchronized void remove(String scheme) {
            synchronized (NotifyManager.class) {
                int i = 0;
                while (i < mList.size()) {
                    MNotification n = mList.get(i);
                    if (n.schema.equals(scheme)) {
                        mList.remove(i);
                        i--;
                    }
                    i++;
                }
            }
        }

        @SuppressWarnings("unused")
        public static synchronized void clear() {
            synchronized (NotifyManager.class) {
                mList.clear();
            }
        }

        public static synchronized int count() {
            int size;
            synchronized (NotifyManager.class) {
                size = mList.size();
            }
            return size;
        }
    }
}
