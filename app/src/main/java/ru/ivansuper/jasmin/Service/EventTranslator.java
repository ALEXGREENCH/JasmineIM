package ru.ivansuper.jasmin.Service;

import android.content.Intent;
import android.util.Log;

import java.util.Vector;

import ru.ivansuper.jasmin.BReceiver;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;

public class EventTranslator {
    public static final String ACTION = "ru.ivansuper.jasmin.JASMINE_NOTIFICATION";
    /** @noinspection unused*/
    public static final int PROFILE_TYPE_ICQ = 0;
    /** @noinspection unused*/
    public static final int PROFILE_TYPE_JABBER = 1;
    /** @noinspection unused*/
    public static final int PROFILE_TYPE_MRIM = 2;
    /** @noinspection unused*/
    public static final int TYPE_APP_CLOSED = 1;
    /** @noinspection unused*/
    public static final int TYPE_APP_STARTED = 0;
    /** @noinspection unused*/
    public static final int TYPE_PROFILES_LIST = 2;
    /** @noinspection unused*/
    public static final int TYPE_PROFILE_STATUS = 3;
    /** @noinspection unused*/
    public static final int TYPE_UNREADED_INFO = 4;

    public static synchronized void sendProfilePresence(final IMProfile profile) {
        synchronized (EventTranslator.class) {
            if (!BReceiver.mWidgetLocked && profile.enabled) {
                resources.service.runOnUi(() -> {
                    Intent i = new Intent();
                    i.setAction(EventTranslator.ACTION);
                    i.putExtra("notify_type", 3);
                    i.putExtra("profile_type", profile.profile_type);
                    if (profile.profile_type == 1) {
                        i.putExtra("profile_xmpp_subtype", ((JProfile) profile).type);
                    } else {
                        i.putExtra("profile_xmpp_subtype", -1);
                    }
                    i.putExtra("profile_id", IMProfile.getProfileFullID(profile));
                    int status = IMProfile.getAbstractedStatus(profile);
                    Log.e("EventTranslator", "=-=-=-=-=-=-=-= [" + IMProfile.getProfileFullID(profile) + "]Sending presence: " + status);
                    i.putExtra("profile_status", IMProfile.getAbstractedStatus(profile));
                    resources.service.sendBroadcast(i);
                });
            }
        }
    }

    public static synchronized void sendUnreadInfo(final int total_unreaded, final int from_contacts, final String last_nick, final String last_message, final String launch_schema) {
        synchronized (EventTranslator.class) {
            if (!BReceiver.mWidgetLocked) {
                resources.service.runOnUi(() -> {
                    Log.e("EventTranslator", "=-=-=-=-=-=-=-= Sending unreded info");
                    Intent i = new Intent();
                    i.setAction(EventTranslator.ACTION);
                    i.putExtra("notify_type", 4);
                    i.putExtra("unreaded_count", total_unreaded);
                    i.putExtra("from_contacts", from_contacts);
                    i.putExtra("last_message_nick", last_nick);
                    i.putExtra("last_message_text", last_message);
                    i.putExtra("launch_schema", launch_schema);
                    resources.service.sendBroadcast(i);
                });
            }
        }
    }

    public static synchronized void sendAppState(final boolean started) {
        synchronized (EventTranslator.class) {
            resources.service.runOnUi(() -> {
                Log.e("EventTranslator", "=-=-=-=-=-=-=-= Sending app state");
                Intent i = new Intent();
                i.setAction(EventTranslator.ACTION);
                if (started) {
                    i.putExtra("notify_type", 0);
                } else {
                    i.putExtra("notify_type", 1);
                }
                resources.service.sendBroadcast(i);
            });
        }
    }

    public static synchronized void sendProfilesList() {
        synchronized (EventTranslator.class) {
            if (!BReceiver.mWidgetLocked) {
                resources.service.runOnUi(() -> {
                    Log.e("EventTranslator", "=-=-=-=-=-=-=-= Sending profiles list");
                    Intent i = new Intent();
                    i.setAction(EventTranslator.ACTION);
                    i.putExtra("notify_type", 2);
                    Vector<IMProfile> profiles = resources.service.profiles.getProfiles();
                    int count = resources.service.profiles.getEnabledProfilesCount();
                    int[] types = new int[count];
                    int[] xmpp_subtypes = new int[count];
                    String[] ids = new String[count];
                    String[] names = new String[count];
                    int jj = 0;
                    for (int j = 0; j < profiles.size(); j++) {
                        IMProfile p = profiles.get(j);
                        if (p.enabled) {
                            types[jj] = p.profile_type;
                            if (p.profile_type == 1) {
                                xmpp_subtypes[jj] = ((JProfile) p).type;
                            } else {
                                xmpp_subtypes[jj] = -1;
                            }
                            ids[jj] = IMProfile.getProfileFullID(p);
                            names[jj] = IMProfile.getProfileNick(p);
                            jj++;
                        }
                    }
                    for (String id : ids) {
                        Log.e("EventTranslator", "Sending profile: " + id);
                    }
                    i.putExtra("profiles_types", types);
                    i.putExtra("profiles_xmpp_subtype", xmpp_subtypes);
                    i.putExtra("profiles_ids", ids);
                    i.putExtra("profiles_names", names);
                    resources.service.sendBroadcast(i);
                });
            }
        }
    }
}