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
import android.os.VibrationEffect;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Vector;

import ru.ivansuper.jasmin.BReceiver;
import ru.ivansuper.jasmin.BufferedDialog;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.MainActivity;
import ru.ivansuper.jasmin.Media;
import ru.ivansuper.jasmin.MessagesDump;
import ru.ivansuper.jasmin.popup_log_adapter;
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
import ru.ivansuper.jasmin.icq.SearchResultItem;
import ru.ivansuper.jasmin.jabber.AbstractForm;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.VCard;
import ru.ivansuper.jasmin.jabber.commands.Command;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.conference.ConferenceItem;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.log_adapter;
import ru.ivansuper.jasmin.plugins.kernel;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class jasminSvc extends Service implements SharedPreferences.OnSharedPreferenceChangeListener, Handler.Callback {

    private final String CHANNEL_ID = "JASMINE_CHANEL";

    /** @noinspection unused*/
    public static final int PUT_INTO_LOG = 4;
    /** @noinspection unused*/
    public static final int CLEAR_LOG = 5;
    /** @noinspection unused*/
    public static final int POPUP_MESSAGE = 6;
    /** @noinspection unused*/
    public static final int PUT_INTO_LOG_TASK = 7;
    /** @noinspection unused*/
    public static final int POPUP_MESSAGE_TASK = 8;

    public static final int MESSAGE_NOTIFY_ID = 65530;
    public static final int ANTISPAM_NOTIFY_ID = 65531;
    public static final int MULTICONNECT_NOTIFY_ID = 65532;

    public static final String ACTION_PING = "ru.ivansuper.jasmin.PING";

    public static IntentFilter INTENT_FILTER = null;

    public static final int REQUEST_CODE_PING = 241;
    public static AlarmManager alarmManager;
    public static popup_log_adapter pla;

    public static Handler uiHandler;
    public Handler chatHdl;
    public Handler clHdl;

    public ContactListActivity contactListActivity;
    public ContactlistItem currentChatContact;
    public IMProfile currentChatProfile;
    /** @noinspection unused*/
    public boolean hideEmptyGroups;
    /** @noinspection unused*/
    public boolean hideOffline;
    /** @noinspection unused*/
    public ContactlistItem lastContactForNonMultiNotify;
    private Media media;
    private NotificationManager notificationManager;
    /** @noinspection FieldCanBeLocal*/
    private Notification notification;
    public SharedPreferences sharedPreferences;
    public volatile ProfilesManager profiles;
    private BReceiver receiver;
    public Handler searchHdl;
    /** @noinspection unused*/
    public boolean showGroups;
    public Handler svcHdl;
    private PowerManager.WakeLock tempWakeLock;
    public Vibrator vibrator;
    /** @noinspection unused*/
    public boolean vibroEnabled;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    public static boolean ACTIVE = false;
    public static final MessagesDump MESSAGES_DUMP = new MessagesDump();
    private final IBinder myBinder = new itf();
    public boolean isAnyChatOpened = false;
    public boolean firstStart = true;
    /** @noinspection unused*/
    public int connectivity_state = 0;
    public log_adapter logAdapter = new log_adapter();
    public final Vector<ContactlistItem> opened_chats = new Vector<>();
    public final Vector<String> wakeLocks = new Vector<>();
    private final Vector<PendingIntentHandler> pendingIntentHandlers = new Vector<>();

    @Override
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

    /** @noinspection unused*/
    private synchronized void holdWake(long id) {
        final String ID = "WAKE_HOLDER_" + utilities.RANDOM.nextLong();
        addWakeLock(ID);
        runOnUi(new Runnable() {
            @Override
            public void run() {
                jasminSvc.this.removeWakeLock(ID);
            }
        }, 2500L);
    }

    public synchronized void attachTimedTask(PendingIntentHandler handler, long interval) {
        if (handler != null) {
            Intent act = new Intent();
            act.setAction(ACTION_PING);
            act.putExtra("ID", handler.id);
            PendingIntent intent = PendingIntent.getBroadcast(this, REQUEST_CODE_PING, act, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, intent);
            this.pendingIntentHandlers.add(handler);
            Log.e("TimedTask", "Added: " + handler.id + "     interval: " + interval);
        }
    }

    public synchronized void removeTimedTask(PendingIntentHandler handler) {
        if (handler != null) {
            for (int i = 0; i < this.pendingIntentHandlers.size(); i++) {
                PendingIntentHandler pih = this.pendingIntentHandlers.get(i);
                if (pih.id == handler.id) {
                    // Add null check before canceling
                    if (pih.intent != null) {
                        alarmManager.cancel(pih.intent);
                    } else {
                        Log.w("TimedTask", "Attempted to cancel null PendingIntent for ID: " + handler.id);
                    }
                    this.pendingIntentHandlers.remove(i); // Remove by index
                    Log.e("TimedTask", "Removed: " + handler.id);
                    break; // Exit after removal
                }
            }
        }
    }

    public synchronized void notifyPingTask(long id) {
        for (int i = 0; i < this.pendingIntentHandlers.size(); i++) {
            PendingIntentHandler pih = this.pendingIntentHandlers.get(i);
            if (pih.id == id) {
                pih.run();
            }
        }
    }

    @SuppressLint({"InvalidWakeLockTag", "Wakelock"})
    private void updateWake() {
        if (this.wakeLock == null) {
            if (!this.wakeLocks.isEmpty()) {
                if (this.tempWakeLock == null || !this.tempWakeLock.isHeld()) {
                    PowerManager pMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    this.tempWakeLock = pMan.newWakeLock(268435457, "ru.ivansuper.jasmin_reconnect_wake");
                    this.tempWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                }
            } else if (this.tempWakeLock != null && this.tempWakeLock.isHeld()) {
                this.tempWakeLock.release();
            }
        }
    }

    public synchronized void addWakeLock(String tag) {
        if (!this.wakeLocks.contains(tag)) {
            this.wakeLocks.add(tag);
            updateWake();
        }
    }

    public synchronized void removeWakeLock(String tag) {
        if (this.wakeLocks.contains(tag)) {
            this.wakeLocks.remove(tag);
            updateWake();
        }
    }

    /** @noinspection unused*/
    public boolean isInOpenedChats(String ID) {
        for (ContactlistItem item : this.opened_chats) {
            switch (item.itemType) {
                case 1:
                    if (!item.ID.equals(ID)) {
                        break;
                    } else {
                        return true;
                    }
                case 4:
                    if (!item.ID.equals(ID)) {
                        break;
                    } else {
                        return true;
                    }
            }
        }
        return false;
    }

    public void removeFromOpenedChats(String ID) {
        int i = 0;
        while (i < this.opened_chats.size()) {
            ContactlistItem item = this.opened_chats.get(i);
            switch (item.itemType) {
                case 1:
                case 4:
                case 7:
                    if (item.ID.equals(ID)) {
                        this.opened_chats.removeElementAt(i);
                        i--;
                    }
                    break;
            }
            i++;
        }
    }

    public boolean isNetworkAvailable() {
        return getInternet() && !getCallPresent();
    }

    private boolean getInternet() {
        NetworkInfo networkInfo;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //noinspection deprecation
        return cm != null && (networkInfo = cm.getActiveNetworkInfo()) != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    private boolean getCallPresent() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return false;
        }
        return tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK || tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
    }

    /** @noinspection unused, SameParameterValue */
    void stopForegroundCompat(int id) {
        stopForeground(true);
    }

    private void startFC() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        startForeground(65331, getNotification(R.drawable.not_connected));
    }

    @SuppressLint("UseRequiresApi")
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Jasmine IM Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressWarnings("deprecation")
    private Notification getNotification(int icon) {
        Notification notification;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // API 11+
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(this);
            }

            builder.setSmallIcon(icon);
            builder.setContentTitle("Jasmine IM");
            builder.setContentText("");
            builder.setContentIntent(contentIntent);
            builder.setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build(); // API 16+
            } else {
                notification = builder.getNotification(); // API 11–15
            }

        } else {
            // Для API 10 и ниже — вручную создаём Notification
            try {
                // setLatestEventInfo устарел, но нужен для API <= 10
                //noinspection JavaReflectionMemberAccess
                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class,
                        CharSequence.class,
                        CharSequence.class,
                        PendingIntent.class
                );

                notification = new Notification();
                notification.icon = icon;
                notification.when = System.currentTimeMillis();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                setLatestEventInfo.invoke(
                        notification,
                        this,
                        "Jasmine IM",
                        "",
                        contentIntent
                );
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                // fallback
                notification = new Notification(icon, "Jasmine IM", System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
        }

        return notification;
    }

    public class itf extends Binder {
        public jasminSvc getService() {
            return jasminSvc.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return this.myBinder;
    }

    @SuppressLint({"WrongConstant", "InvalidWakeLockTag", "UnspecifiedRegisterReceiverFlag"})
    @Override
    public void onCreate() {
        ACTIVE = true;
        resources.service = this;
        alarmManager = (AlarmManager) getSystemService("alarm");
        pla = new popup_log_adapter(this);
        uiHandler = new Handler(this);
        this.media = new Media(this);
        this.svcHdl = new Handler(this);
        startFC();
        this.vibrator = (Vibrator) getSystemService("vibrator");
        this.notificationManager = (NotificationManager) getSystemService("notification");
        //noinspection deprecation
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initSettings();
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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
        if (this.sharedPreferences.getBoolean("ms_wake_lock", false)) {
            PowerManager pMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.wakeLock = pMan.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ru.ivansuper.jasmin_wake");
            this.wakeLock.acquire(); // Hold indefinitely until released
            Log.v("POWER", "WAKE_LOCK ENABLED");
        }
        if (this.sharedPreferences.getBoolean("ms_wifi_lock", true)) {
            WifiManager pMan2 = (WifiManager) getSystemService("wifi");
            this.wifiLock = pMan2.createWifiLock(1, jasminSvc.class.getName());
            this.wifiLock.acquire();
            Log.v("POWER", "WIFI_LOCK ENABLED");
        }
        EventTranslator.sendAppState(true);
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
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
        if (this.wifiLock != null && this.wifiLock.isHeld()) {
            this.wifiLock.release();
        }
        if (this.wakeLock != null && this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
        if (this.tempWakeLock != null && this.tempWakeLock.isHeld()) {
            this.tempWakeLock.release();
        }
        this.notificationManager.cancelAll();
        EventTranslator.sendAppState(false);
    }

    /** @noinspection unused*/
    public void destroyNotification() {
        this.notificationManager.cancelAll();
    }

    public void initPluginsKernel() {
        kernel.init();
    }

    /** @noinspection unused*/
    @SuppressLint("WrongConstant")
    private void createLogFloatingWindow() {
        int flags;
        PreferenceTable.ms_log_clickable = this.sharedPreferences.getBoolean("ms_log_clickable", false);
        boolean clickable = PreferenceTable.ms_log_clickable;
        if (clickable) {
            int flags2 = 8;
            flags = flags2 + 32;
        } else {
            int flags3 = 8;
            flags = flags3 + 16;
        }

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2003, flags, -3);
        layoutParams.gravity = 53;
        LinearLayout lay = (LinearLayout) LayoutInflater.from(this).inflate(resources.IT_IS_TABLET ? R.layout.popup_log_window_xhigh : R.layout.popup_log_window, null);
        ListView log_list = lay.findViewById(R.id.popup_log_list);
        log_list.setAdapter(pla);
        windowManager.addView(lay, layoutParams);
    }

    /** @noinspection unused*/
    public void handleNetworkStateChanged(int flags) {
    }

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

    @SuppressLint("NotificationPermission")
    public void showInBarNotify(CharSequence header, CharSequence message, boolean led) {
        Notification notification;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_MUTABLE
                        : 0
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.icq_msg_in)
                    .setContentTitle(header)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            if (led) {
                builder.setLights(0xFF00FF00, 1000, 1000);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build(); // API 16+
            } else {
                notification = builder.getNotification(); // API 11–15
            }

        } else {
            // API 10 и ниже — через рефлексию
            try {
                notification = new Notification();
                notification.icon = R.drawable.icq_msg_in;
                notification.when = System.currentTimeMillis();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                if (led) {
                    notification.ledARGB = 0xFF00FF00;
                    notification.ledOnMS = 1000;
                    notification.ledOffMS = 1000;
                    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                }

                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class,
                        CharSequence.class,
                        CharSequence.class,
                        PendingIntent.class
                );

                setLatestEventInfo.invoke(notification, this, header, message, contentIntent);

            } catch (Exception e) {
                e.printStackTrace();
                // fallback
                notification = new Notification(R.drawable.icq_msg_in, header, System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) SystemClock.uptimeMillis(), notification);
    }

    @SuppressLint("NotificationPermission")
    public void showPersonalMessageNotify(final CharSequence header, final CharSequence message, final boolean led, final int id, final ContactlistItem contact) {
        if (!PreferenceTable.multi_notify) return;

        Notification notification;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_MUTABLE
                        : 0
        );

        int messagesCount = getMessagesCount(contact);
        int iconResId = resources.getTrayMessageIconResId(messagesCount);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(iconResId)
                    .setContentTitle(header)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            if (led) {
                builder.setLights(0xFF00FF00, 1000, 1000);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build();
            } else {
                notification = builder.getNotification();
            }

        } else {
            try {
                notification = new Notification();
                notification.icon = iconResId;
                notification.when = System.currentTimeMillis();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                if (led) {
                    notification.ledARGB = 0xFF00FF00;
                    notification.ledOnMS = 1000;
                    notification.ledOffMS = 1000;
                    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                }

                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class,
                        CharSequence.class,
                        CharSequence.class,
                        PendingIntent.class
                );
                setLatestEventInfo.invoke(notification, this, header, message, contentIntent);

            } catch (Exception e) {
                e.printStackTrace();
                // fallback для совсем древних API
                notification = new Notification(iconResId, header, System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
        }

        this.notificationManager.notify(id, notification);
    }

    private static int getMessagesCount(ContactlistItem contact) {
        int messagesCount = 0;
        if (contact.itemType == 1) {
            messagesCount = ((ICQContact) contact).getUnreadCount();
        } else if (contact.itemType == 4) {
            messagesCount = ((JContact) contact).getUnreadCount();
        } else if (contact.itemType == 7) {
            messagesCount = ((MMPContact) contact).getUnreadCount();
        } else if (contact.itemType == 10) {
            messagesCount = ((ConferenceItem) contact).getUnreadCount();
        }
        return messagesCount;
    }

    public synchronized void putMessageNotify(final ContactlistItem contact, final String nick, final String text) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                MNotification mn = new MNotification();
                Intent intent = new Intent(jasminSvc.this, MainActivity.class);
                String scheme = IMProfile.getSchema(contact);
                intent.setAction(scheme);
                mn.intent = intent;
                mn.nick = nick;
                mn.text = text;
                mn.schema = IMProfile.getSchema(contact);
                NotifyManager.put(mn);
                jasminSvc.this.updateNotifyInternal();
            }
        });
    }

    public synchronized void removeMessageNotify(final ContactlistItem contact) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                NotifyManager.remove(IMProfile.getSchema(contact));
                jasminSvc.this.updateNotifyInternal();
            }
        });
    }

    public synchronized void updateNotify() {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                updateNotifyInternal();
            }
        });
    }

    @SuppressLint("NotificationPermission")
    private synchronized void updateNotifyInternal() {
        int icon;
        if (ACTIVE && this.profiles != null) {
            if (this.profiles.isAnyProfileConnected()) {
                ADB.startOnlineCounter();
            } else {
                ADB.stopOnlineCounter();
            }
            MESSAGES_DUMP.erase();
            this.profiles.getUnreadMessagesDump(MESSAGES_DUMP);
            MNotification mNotification = null;
            if (MESSAGES_DUMP.total_messages > 0 && NotifyManager.count() > 0) {
                mNotification = NotifyManager.get();
                EventTranslator.sendUnreadInfo(MESSAGES_DUMP.total_messages, MESSAGES_DUMP.from_contacts, mNotification.nick, mNotification.text, mNotification.schema);
            } else {
                EventTranslator.sendUnreadInfo(0, MESSAGES_DUMP.from_contacts, "null", "null", "null");
            }
            if (MESSAGES_DUMP.total_messages > 0 && NotifyManager.count() > 0 && !PreferenceTable.multi_notify) {
                if (this.notificationManager != null) {
                    //noinspection DataFlowIssue
                    String description = mNotification.nick.isEmpty() ? "" : mNotification.nick + ": " + mNotification.text;
                    showMessageNotification(utilities.match(resources.getString("s_unread_notify_text"), new String[]{String.valueOf(MESSAGES_DUMP.total_messages), String.valueOf(MESSAGES_DUMP.from_contacts)}), description, MESSAGES_DUMP.total_messages, true, MESSAGE_NOTIFY_ID, mNotification.intent);
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
                this.notificationManager.notify(65331, n);
            }
        }
    }

    @SuppressLint("NotificationPermission")
    public void showMessageNotification(final CharSequence header, final CharSequence message, final int count, final boolean led, final int id, final Intent intent) {
        Notification notification;

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.icq_msg_in)
                    .setContentTitle(header)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            if (led) {
                builder.setLights(0xFF00FF00, 1000, 1000);
            }

            // setNumber доступен с API 11
            builder.setNumber(count);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build();
            } else {
                notification = builder.getNotification();
            }

        } else {
            // API 10 и ниже — ручная сборка
            try {
                notification = new Notification();
                notification.icon = R.drawable.icq_msg_in;
                notification.when = System.currentTimeMillis();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                if (led) {
                    notification.ledARGB = 0xFF00FF00;
                    notification.ledOnMS = 1000;
                    notification.ledOffMS = 1000;
                    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                }

                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class,
                        CharSequence.class,
                        CharSequence.class,
                        PendingIntent.class
                );
                setLatestEventInfo.invoke(notification, this, header, message, contentIntent);

            } catch (Exception e) {
                e.printStackTrace();
                notification = new Notification(R.drawable.icq_msg_in, header, System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
        }

        this.notificationManager.notify(id, notification);
    }

    public void cancelMessageNotification(int id) {
        this.notificationManager.cancel(id);
    }

    /** @noinspection unused*/
    @SuppressLint("NotificationPermission")
    public void showMailMessageNotify(final CharSequence header, final CharSequence message, final boolean led, final int id, final int count, final String JID) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("%GMAIL%" + JID);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_MUTABLE
                        : 0
        );

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.google_mail)
                    .setContentTitle(header)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.setPriority(Notification.PRIORITY_DEFAULT);
            }

            if (led) {
                builder.setLights(0xFF00FF00, 1000, 1000); // зелёный
            }

            builder.setNumber(count);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Channel Name",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
                builder.setChannelId(CHANNEL_ID);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build(); // API 16+
            } else {
                notification = builder.getNotification(); // API 11–15
            }

        } else {
            // API 10 и ниже — ручная сборка через рефлексию
            try {
                notification = new Notification();
                notification.icon = R.drawable.google_mail;
                notification.when = System.currentTimeMillis();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                if (led) {
                    notification.ledARGB = 0xFF00FF00;
                    notification.ledOnMS = 1000;
                    notification.ledOffMS = 1000;
                    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                }

                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class,
                        CharSequence.class,
                        CharSequence.class,
                        PendingIntent.class
                );
                setLatestEventInfo.invoke(notification, this, header, message, contentIntent);

            } catch (Exception e) {
                e.printStackTrace();
                notification = new Notification(R.drawable.google_mail, header, System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(id, notification);
        }
    }

    public void cancelPersonalMessageNotify(int id) {
        this.notificationManager.cancel(id);
    }

    @SuppressLint("NotificationPermission")
    public void showAntispamNotify(String id, String message) {
        this.notification = new Notification(R.drawable.cross, resources.getString("s_antispam_notify") + " " + id, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_MUTABLE);
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notify_remote_view);
        rv.setTextViewText(R.id.notify_remote_view, message);
        this.notification.icon = R.drawable.cross;
        this.notification.contentIntent = contentIntent;
        this.notification.contentView = rv;
        this.notificationManager.notify(ANTISPAM_NOTIFY_ID, this.notification);
    }

    /** @noinspection unused*/
    public void cancelAntispamNotify() {
        this.notificationManager.cancel(ANTISPAM_NOTIFY_ID);
    }

    @SuppressLint("NotificationPermission")
    public void showTransferNotify(final int id, final String desc, final String intent_scheme) {
        Notification notification;

        Intent intent = new Intent(this, ContactListActivity.class);
        intent.setAction(intent_scheme);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.file)
                    .setContentTitle(resources.getString("s_file_transfer_notify"))
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build(); // API 16+
            } else {
                notification = builder.getNotification(); // API 11–15
            }

        } else {
            try {
                notification = new Notification();
                notification.icon = R.drawable.file;
                notification.when = System.currentTimeMillis();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class,
                        CharSequence.class,
                        CharSequence.class,
                        PendingIntent.class
                );
                setLatestEventInfo.invoke(notification, this, resources.getString("s_file_transfer_notify"), desc, contentIntent);

            } catch (Exception e) {
                e.printStackTrace();
                // fallback
                notification = new Notification(R.drawable.file, resources.getString("s_file_transfer_notify"), System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
        }

        this.notificationManager.notify(id, notification);
    }

    public void cancelTransferNotify(int id) {
        this.notificationManager.cancel(id);
    }

    /** @noinspection unused*/
    @SuppressLint("NotificationPermission")
    public void showMulticonnectNotify(final String id) {
        Notification notification;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_MUTABLE
                        : 0
        );

        String title = "!!!";
        String text = resources.getString("s_moltilogin_notify_desc");
        String bigText = resources.getString("s_moltilogin_notify") + " " + id;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // API 16+ — с BigTextStyle и build()
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.cross)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(bigText))
                    .setAutoCancel(true);

            notification = builder.build();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // API 11–15 — без BigTextStyle
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.cross)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            notification = builder.getNotification();

        } else {
            // API ≤ 10 — вручную через рефлексию
            try {
                notification = new Notification();
                notification.icon = R.drawable.cross;
                notification.when = System.currentTimeMillis();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;

                Method setLatestEventInfo = Notification.class.getMethod(
                        "setLatestEventInfo",
                        Context.class,
                        CharSequence.class,
                        CharSequence.class,
                        PendingIntent.class
                );
                setLatestEventInfo.invoke(notification, this, title, bigText, contentIntent);

            } catch (Exception e) {
                e.printStackTrace();
                // fallback
                notification = new Notification(R.drawable.cross, title, System.currentTimeMillis());
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
            }
        }

        this.notificationManager.notify(MULTICONNECT_NOTIFY_ID, notification);
    }

    public void cancelMultiloginNotify() {
        this.notificationManager.cancel(MULTICONNECT_NOTIFY_ID);
    }

    /** @noinspection unused*/
    public void showCommandFormInContactList(Command cmd) {
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, ContactListActivity.SHOW_JABBER_CMD_FORM, cmd));
        }
    }

    /** @noinspection unused*/
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

    /** @noinspection unused*/
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

    /** @noinspection unused*/
    public void handleConferenceBannedListReceived(Conference conference) {
        if (JConference.INITIALIZED && JConference.conference.equals(conference)) {
            sendMessage(JConference.BANNED_LIST_RECEIVED, null, false);
        }
    }

    public void handleChatNeedRefreshContact() {
        sendMessage(96, null, false);
    }

    public void handleContactlistReturnToContacts() {
        if (this.clHdl != null) {
            sendMessage(ContactListActivity.RETURN_TO_CONTACTS, null, false);
        }
    }

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

    public void closeChatIfShown() {
        sendMessage(62, null, false);
    }

    public void handleChatUpdateInfo() {
        sendMessage(2, null, false);
    }

    public void handleChatNeedRefresh(Object contact) {
        sendMessage(5, contact, true);
    }

    /** @noinspection unused*/
    public void handleChatNeedRebuild(Object contact) {
        sendMessage(4, contact, true);
    }

    public void handleChatTransferNeedRebuild() {
        sendMessage(60, null, false);
    }

    /** @noinspection unused*/
    public void handleChatTransferRefreshProgress() {
        sendMessage(61, null, false);
    }

    public void handleSearchResult(SearchResultItem item) {
        if (this.searchHdl != null) {
            this.searchHdl.sendMessage(Message.obtain(this.searchHdl, 0, item));
        }
    }

    /** @noinspection unused*/
    public void handleIncomingMessage() {
        if (PreferenceTable.soundEnabled) {
            this.media.playEvent(0);
        }
        doVibrate(PreferenceTable.vibroLength);
    }

    public void handleIncomingFile() {
        if (PreferenceTable.soundEnabled) {
            this.media.playEvent(6);
        }
        doVibrate(PreferenceTable.vibroLength);
    }

    public void rebuildChatMarkers() {
        sendMessage(6, null, false);
    }

    /** @noinspection unused*/
    public void handleIncomingMessage(MMPProfile profile, MMPContact contact, HistoryItem msg) {
        if (PreferenceTable.soundEnabled) {
            this.media.playEvent(0);
        }
        doVibrate(PreferenceTable.vibroLength);
        if (this.isAnyChatOpened) {
            sendMessage(4, msg, true);
        }
        rebuildChatMarkers();
    }

    public void handleIncomingMessage(HistoryItem msg) {
        if (PreferenceTable.soundEnabled) {
            this.media.playEvent(0);
        }
        doVibrate(PreferenceTable.vibroLength);
        if (this.isAnyChatOpened) {
            sendMessage(4, msg, true);
        }
        rebuildChatMarkers();
    }

    /** @noinspection unused*/
    public void rebuildChat(ICQProfile profile, ICQContact contact, HistoryItem msg) {
        if (this.isAnyChatOpened) {
            sendMessage(4, msg, true);
        }
        rebuildChatMarkers();
    }

    public synchronized void playEvent(final int event) {
        if (PreferenceTable.soundEnabled) {
            runOnUi(new Runnable() {
                @Override
                public void run() {
                    jasminSvc.this.media.playEvent(event);
                }
            }, 50L);
        }
    }

    /** @noinspection unused*/
    public void handleIncomingXtrazMessage(ICQProfile profile, ICQContact contact, HistoryItem msg) {
        sendMessage(4, msg, true);
    }

    public void displayContactInfo(InfoContainer infoContainer) {
        if (!ContactListActivity.HIDDEN) {
            sendMessage(16, infoContainer, true);
        }
    }

    public void displayContactInfoInSearch(InfoContainer infoContainer) {
        if (SearchActivity.VISIBLE && this.searchHdl != null) {
            this.searchHdl.sendMessage(Message.obtain(this.searchHdl, 1, infoContainer));
        }
    }

    public void put_log(String variable) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 4, variable));
        }
    }

    /** @noinspection unused*/
    public void put_log(String variable, Runnable task, Drawable icon) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 7, new PopupTask(variable, task, icon)));
        }
    }

    /** @noinspection unused*/
    public void clear_log() {
        if (this.svcHdl != null) {
            this.svcHdl.sendEmptyMessage(5);
        }
    }

    /** @noinspection unused*/
    public void displayProgress() {
        if (this.clHdl != null) {
            this.clHdl.sendEmptyMessage(32);
        }
    }

    public void displayProgress(String description) {
        if (this.clHdl != null) {
            this.clHdl.sendMessage(Message.obtain(this.clHdl, 32, description));
        }
    }

    public void cancelProgress() {
        if (this.clHdl != null) {
            this.clHdl.sendEmptyMessage(33);
        }
    }

    public void doVibrate(long how_long) {
        if (Media.phone_mode == 0 && this.vibrator != null && PreferenceTable.vibroEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.vibrator.vibrate(VibrationEffect.createOneShot(how_long, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                this.vibrator.vibrate(how_long);
            }
        }
    }

    public void doVibrate(long[] how_long) {
        if (Media.phone_mode == 0 && this.vibrator != null && PreferenceTable.vibroEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.vibrator.vibrate(VibrationEffect.createWaveform(how_long, -1));
            } else {
                this.vibrator.vibrate(how_long, -1);
            }
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
                //noinspection DataFlowIssue
                this.clHdl.sendMessageDelayed(msg, 0L);
            } else if (what == 2) {
                this.clHdl.removeMessages(2);
                //noinspection DataFlowIssue
                this.clHdl.sendMessageDelayed(msg, 0L);
            } else {
                //noinspection DataFlowIssue
                this.clHdl.sendMessageDelayed(msg, 0L);
            }
        }
        if (this.chatHdl != null && msgA != null) {
            this.chatHdl.sendMessageDelayed(msgA, 50L);
        }
    }

    public String getAntispamQuestion() {
        return this.sharedPreferences.getString("ms_as_question", resources.getString("s_default_question"));
    }

    /** @noinspection unused*/
    public String getAntispamAnswer() {
        return this.sharedPreferences.getString("ms_as_answer", resources.getString("s_default_answer"));
    }

    public String getAntispamAllowed() {
        return this.sharedPreferences.getString("ms_as_allowed", resources.getString("s_default_user_allowed"));
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

    public void forcePopUp(String value) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 6, value));
        }
    }

    /** @noinspection unused*/
    public void forcePopUp(String variable, Runnable task, Drawable icon) {
        if (this.svcHdl != null) {
            this.svcHdl.sendMessage(Message.obtain(this.svcHdl, 8, new PopupTask(variable, task, icon)));
        }
    }

    /** @noinspection DataFlowIssue*/
    @SuppressLint("ApplySharedPref")
    private void initSettings() {
        PreferenceTable.vibroEnabled = this.sharedPreferences.getBoolean("ms_vibro", true);
        PreferenceTable.showGroups = this.sharedPreferences.getBoolean("ms_groups", true);
        PreferenceTable.hideOffline = this.sharedPreferences.getBoolean("ms_offline", true);
        PreferenceTable.hideEmptyGroups = this.sharedPreferences.getBoolean("ms_emptygroups", true);
        try {
            PreferenceTable.clTextSize = Integer.parseInt(this.sharedPreferences.getString("ms_cl_font_size", "16"));
        } catch (Exception e) {
            this.sharedPreferences.edit().putString("ms_cl_font_size", "16").commit();
        }
        try {
            PreferenceTable.chatTextSize = Integer.parseInt(this.sharedPreferences.getString("ms_chat_text_size", "16"));
        } catch (Exception e2) {
            this.sharedPreferences.edit().putString("ms_chat_text_size", "16").commit();
        }
        try {
            PreferenceTable.chatTimeSize = Integer.parseInt(this.sharedPreferences.getString("ms_chat_time_size", "14"));
        } catch (Exception e3) {
            this.sharedPreferences.edit().putString("ms_chat_time_size", "14").commit();
        }
        try {
            PreferenceTable.vibroLength = Long.parseLong(this.sharedPreferences.getString("ms_vibro_length", "200"));
        } catch (Exception e4) {
            this.sharedPreferences.edit().putString("ms_vibro_length", "200").commit();
        }
        try {
            PreferenceTable.smileys_scale = Integer.parseInt(this.sharedPreferences.getString("ms_smileys_scale", "2"));
        } catch (Exception e5) {
            this.sharedPreferences.edit().putString("ms_smileys_scale", "2").commit();
        }
        PreferenceTable.preloadHistory = this.sharedPreferences.getBoolean("ms_preload_history", true);
        PreferenceTable.writeHistory = this.sharedPreferences.getBoolean("ms_use_history", true);
        PreferenceTable.realtimeHistoryExport = this.sharedPreferences.getBoolean("ms_realtime_history_record", false);
        PreferenceTable.nickInChat = this.sharedPreferences.getBoolean("ms_nick_in_chat", false);
        try {
            PreferenceTable.smileysSelectorColumns = Integer.parseInt(this.sharedPreferences.getString("ms_columns", "5"));
        } catch (Exception e6) {
            this.sharedPreferences.edit().putString("ms_columns", "5").commit();
        }
        try {
            PreferenceTable.simple_cl_columns = Integer.parseInt(this.sharedPreferences.getString("ms_cl_columns", "1"));
        } catch (Exception e7) {
            this.sharedPreferences.edit().putString("ms_cl_columns", "1").commit();
        }
        PreferenceTable.sendByEnter = this.sharedPreferences.getBoolean("ms_send_by_enter", false);
        PreferenceTable.soundEnabled = this.sharedPreferences.getBoolean("ms_sounds", true);
        PreferenceTable.autoCloseSmileysSelector = this.sharedPreferences.getBoolean("ms_auto_close_smileys_selector", true);
        PreferenceTable.as_only_roster = this.sharedPreferences.getBoolean("ms_as_only_roster", false);
        PreferenceTable.as_enable_notify = this.sharedPreferences.getBoolean("ms_as_notify", true);
        PreferenceTable.wake_lock = this.sharedPreferences.getBoolean("ms_wake_lock", true);
        PreferenceTable.wifi_lock = this.sharedPreferences.getBoolean("ms_wifi_lock", true);
        PreferenceTable.multi_notify = this.sharedPreferences.getBoolean("ms_notify_mode", false);
        PreferenceTable.simple_cl = this.sharedPreferences.getBoolean("ms_simple_list", false);
        PreferenceTable.pg_status = this.sharedPreferences.getBoolean("ms_pg_status", true);
        PreferenceTable.use_ping = this.sharedPreferences.getBoolean("ms_use_ping", true);
        try {
            PreferenceTable.ping_freq = Integer.parseInt(this.sharedPreferences.getString("ms_ping_freq", "60"));
        } catch (Exception e8) {
            this.sharedPreferences.edit().putString("ms_ping_freq", "60").commit();
        }
        PreferenceTable.auto_close_status_selector = this.sharedPreferences.getBoolean("ms_auto_close_selector", true);
        PreferenceTable.as_qestion_enabled = this.sharedPreferences.getBoolean("ms_as_qest_enable", true);
        PreferenceTable.auto_change_status = this.sharedPreferences.getBoolean("ms_turn_sts_on_screen", false);
        try {
            PreferenceTable.auto_change_status_timeout = Integer.parseInt(this.sharedPreferences.getString("ms_turn_sts_timeout", "300"));
        } catch (Exception e9) {
            this.sharedPreferences.edit().putString("ms_turn_sts_timeout", "300").commit();
        }
        PreferenceTable.show_away_in_cl = this.sharedPreferences.getBoolean("ms_show_away", false);
        PreferenceTable.send_typing_notify = this.sharedPreferences.getBoolean("ms_typing_notify", false);
        PreferenceTable.auto_close_chat = this.sharedPreferences.getBoolean("ms_auto_close_chat", false);
        PreferenceTable.auto_xtraz = this.sharedPreferences.getBoolean("ms_auto_xtraz", false);
        PreferenceTable.auto_open_keyboard = this.sharedPreferences.getBoolean("ms_auto_open_keyboard", false);
        PreferenceTable.auto_cap = this.sharedPreferences.getBoolean("ms_use_auto_cap", true);
        PreferenceTable.enable_x_in_bottom_panel = this.sharedPreferences.getBoolean("ms_enable_x_in_bottom_panel", true);
        PreferenceTable.log_xtraz_reading = this.sharedPreferences.getBoolean("ms_log_xtraz_reading", true);
        PreferenceTable.log_online = this.sharedPreferences.getBoolean("ms_log_online", true);
        PreferenceTable.log_offline = this.sharedPreferences.getBoolean("ms_log_offline", true);
        PreferenceTable.use_popup = this.sharedPreferences.getBoolean("ms_use_popup", true);
        PreferenceTable.use_contactlist_items_shadow = this.sharedPreferences.getBoolean("ms_use_items_shadow", true);
        PreferenceTable.chat_zebra = this.sharedPreferences.getBoolean("ms_chat_zebra", true);
        PreferenceTable.chat_dividers = this.sharedPreferences.getBoolean("ms_chat_dividers", false);
        PreferenceTable.triple_vibro = this.sharedPreferences.getBoolean("ms_triple_vibro", true);
        PreferenceTable.ms_animated_smileys = this.sharedPreferences.getBoolean("ms_animated_smileys", true);
        PreferenceTable.ms_show_avatars = this.sharedPreferences.getBoolean("ms_show_avatars", false);
        PreferenceTable.ms_chats_at_top = this.sharedPreferences.getBoolean("ms_chats_at_top", false);
        PreferenceTable.ms_chat_style = Integer.parseInt(this.sharedPreferences.getString("ms_chat_style", "0"));
        PreferenceTable.ms_dragdrop_quoting = this.sharedPreferences.getBoolean("ms_dragdrop_quoting", true);
        PreferenceTable.ms_hide_not_connected_profiles = this.sharedPreferences.getBoolean("ms_hide_not_connected_profiles", false);
        PreferenceTable.ms_links_to_images = this.sharedPreferences.getBoolean("ms_links_to_images", false);
        PreferenceTable.ms_use_overscroll = this.sharedPreferences.getBoolean("ms_use_overscroll", true);
        PreferenceTable.s_ms_show_xstatuses = this.sharedPreferences.getBoolean("ms_show_xstatuses", true);
        PreferenceTable.s_ms_show_clients = this.sharedPreferences.getBoolean("ms_show_clients", true);
        PreferenceTable.ms_check_tls_certificate = this.sharedPreferences.getBoolean("ms_check_tls_certificate", true);
        PreferenceTable.ms_show_markers_in_chat = this.sharedPreferences.getBoolean("ms_show_markers_in_chat", true);
        PreferenceTable.ms_two_screens_mode = this.sharedPreferences.getBoolean("ms_two_screens_mode", true);
        PreferenceTable.ms_cl_transition_effect = this.sharedPreferences.getInt("ms_cl_transition_effect", 3);
        PreferenceTable.ms_rejoin_to_conferences = this.sharedPreferences.getBoolean("ms_restore_conf_presence", false);
        PreferenceTable.ms_use_bookmark_autojoin = this.sharedPreferences.getBoolean("ms_use_bookmark_autojoin", false);
        PreferenceTable.ms_use_messages_merging = this.sharedPreferences.getBoolean("ms_use_messages_merging", false);
        PreferenceTable.ms_messages_limit_enabled = this.sharedPreferences.getBoolean("ms_messages_limit_enabled", true);
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

    @Override
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
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        return false;
                    }
                }
                return false;
            case 4:
                this.logAdapter.put((String) arg0.obj);
                return false;
            case 5:
                this.logAdapter.clear();
                return false;
            case 6:
                pla.put((String) arg0.obj);
                return false;
            case 7:
                this.logAdapter.put(((PopupTask) arg0.obj).variable);
                return false;
            case 8:
                PopupTask task = (PopupTask) arg0.obj;
                pla.put(task.variable, task.task, task.icon);
                return false;
        }
    }

    /** @noinspection unused*/
    @SuppressWarnings("deprecation")
    public final void moveToClipboard(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // API 11+ — новый ClipboardManager
            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("copied_text", text);
            cm.setPrimaryClip(clip);
        } else {
            // API 10 и ниже — устаревший ClipboardManager из android.text
            android.text.ClipboardManager cm = (android.text.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(text);
        }

        Toast toast = Toast.makeText(this, Locale.getString("s_copied"), Toast.LENGTH_SHORT);
        toast.setGravity(48, 0, 0);
        toast.show();
    }

    public final void showToast(final String text, final int length) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(jasminSvc.this, text, length).show();
            }
        });
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
                mNotification = mList.isEmpty() ? null : mList.get(0);
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

        /** @noinspection unused*/
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