package ru.ivansuper.jasmin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.plugins._interface.ServiceBroadcastReceiver;
import ru.ivansuper.jasmin.protocols.IMProfile;

public class BReceiver extends BroadcastReceiver {
    private static final long WIDGET_REQUESTS_INTERVAL = 2000;
    public static boolean mWidgetLocked = false;
    private int mFastRequestsCount = 0;
    private long mLastRequestTimestamp = 0;
    private jasminSvc service;

    public BReceiver(jasminSvc service) {
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean catched = ServiceBroadcastReceiver.OnIntent(context, intent);

        String action = intent.getAction();
        if (action == null) return;

        // Handle PING only once
        if (action.contains(jasminSvc.ACTION_PING)) {
            long id = intent.getLongExtra("ID", -1L);
            if (id != -1L) {
                this.service.notifyPingTask(id);
            }
            return; // Skip further processing
        }

        if (!catched) {
            if (intent.getAction().contains("RINGER_MODE_CHANGED")) {
                AudioManager am = (AudioManager) this.service.getSystemService(Context.AUDIO_SERVICE);
                switch (am.getRingerMode()) {
                    case 1:
                        Media.ring_mode = 1;
                        return;
                    case 2:
                        Media.ring_mode = 0;
                        return;
                    default:
                        return;
                }
            } else if (intent.getAction().contains("SCREEN_OFF")) {
                this.service.handleScreenTurnedOff();
            } else if (intent.getAction().contains("SCREEN_ON")) {
                this.service.handleScreenTurnedOn();
            } else if (intent.getAction().contains(jasminSvc.ACTION_PING)) {
                this.service.notifyPingTask(intent.getLongExtra("ID", -1L));
            } else if (intent.getAction().contains("PHONE_STATE")) {
                TelephonyManager phone = (TelephonyManager) this.service.getSystemService(Context.TELEPHONY_SERVICE);
                if (phone.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    Media.phone_mode = 0;
                } else {
                    Media.phone_mode = 1;
                }
            } else if (intent.getAction().startsWith("ru.ivansuper.jasmin.REQUEST_STATE")) {
                if (!mWidgetLocked) {
                    if (SystemClock.uptimeMillis() - this.mLastRequestTimestamp < WIDGET_REQUESTS_INTERVAL) {
                        this.mFastRequestsCount++;
                    } else {
                        this.mFastRequestsCount = 0;
                    }
                    if (this.mFastRequestsCount > 20) {
                        this.service.showInBarNotify(Locale.getString("s_information"), Locale.getString("s_widget_locked"), true);
                        mWidgetLocked = true;
                        return;
                    }
                    Log.e("BReceiver", "State_request received");
                    if (resources.service != null && resources.service.profiles != null) {
                        EventTranslator.sendProfilesList();
                        Vector<IMProfile> profiles = resources.service.profiles.getProfiles();
                        for (IMProfile p : profiles) {
                            EventTranslator.sendProfilePresence(p);
                        }
                        this.service.updateNotify();
                        this.mLastRequestTimestamp = SystemClock.uptimeMillis();
                    }
                }
            } else if (intent.getAction().startsWith(jasminSvc.ACTION_PING)) {
                resources.service.notifyPingTask(intent.getLongExtra("ID", -1L));
            }
        }
    }
}