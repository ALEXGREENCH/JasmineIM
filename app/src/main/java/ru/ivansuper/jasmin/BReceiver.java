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

/* loaded from: classes.dex */
public class BReceiver extends BroadcastReceiver {
    private static final long WIDGET_REQUESTS_INTERVAL = 2000;
    public static boolean mWidgetLocked = false;
    private int mFastRequestsCount = 0;
    private long mLastRequestTimestamp = 0;
    private jasminSvc service;

    public BReceiver(jasminSvc service) {
        this.service = service;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context arg0, Intent arg1) {
        boolean catched = ServiceBroadcastReceiver.OnIntent(arg0, arg1);
        if (!catched) {
            if (arg1.getAction().indexOf("RINGER_MODE_CHANGED") >= 0) {
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
            } else if (arg1.getAction().indexOf("SCREEN_OFF") >= 0) {
                this.service.handleScreenTurnedOff();
            } else if (arg1.getAction().indexOf("SCREEN_ON") >= 0) {
                this.service.handleScreenTurnedOn();
            } else if (arg1.getAction().indexOf(jasminSvc.ACTION_PING) >= 0) {
                this.service.notifyPingTask(arg1.getLongExtra("ID", -1L));
            } else if (arg1.getAction().indexOf("PHONE_STATE") >= 0) {
                TelephonyManager phone = (TelephonyManager) this.service.getSystemService(Context.TELEPHONY_SERVICE);
                if (phone.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    Media.phone_mode = 0;
                } else {
                    Media.phone_mode = 1;
                }
            } else if (arg1.getAction().startsWith("ru.ivansuper.jasmin.REQUEST_STATE")) {
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
                        Iterator<IMProfile> it = profiles.iterator();
                        while (it.hasNext()) {
                            IMProfile p = it.next();
                            EventTranslator.sendProfilePresence(p);
                        }
                        this.service.updateNotify();
                        this.mLastRequestTimestamp = SystemClock.uptimeMillis();
                    }
                }
            } else if (arg1.getAction().startsWith(jasminSvc.ACTION_PING)) {
                resources.service.notifyPingTask(arg1.getLongExtra("ID", -1L));
            }
        }
    }
}