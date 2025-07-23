package ru.ivansuper.jasmin;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.protocols.IMProfile;

public class ProfilesManager {
    private final boolean EnableFeatureFirstTime;
    private final Vector<IMProfile> profiles = new Vector<>();
    private final jasminSvc svc;

    public ProfilesManager(jasminSvc svcParam) {
        this.svc = svcParam;
        File enable_feature_checker = new File(utilities.normalizePath(resources.dataPath) + "ProfileEnableFeature");
        if (!enable_feature_checker.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                enable_feature_checker.createNewFile();
            } catch (Exception ignored) {
            }
            this.EnableFeatureFirstTime = true;
        } else {
            this.EnableFeatureFirstTime = false;
        }
        fillProfilesFromFile();
        Log.e("ProfilesManager", "Profiles initialized");
        this.svc.handleContactlistNeedRemake();
        this.svc.handleProfileChanged();
    }

    public final synchronized void fillProfilesFromFile() {
        File profs = new File(resources.dataPath + "profiles.cfg");
        if (!profs.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                profs.createNewFile();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        if (profs.length() > 0) {
            try {
                //noinspection IOStreamConstructor
                DataInputStream dis = new DataInputStream(new FileInputStream(profs));
                int profilesCount = dis.read();
                for (int i = 0; i < profilesCount; i++) {
                    int profile_type = dis.read();
                    switch (profile_type) {
                        case IMProfile.OSCAR:
                            int nameLen = dis.read();
                            byte[] uin = new byte[nameLen];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(uin, 0, nameLen);
                            String sUIN = proceedISEM_B(uin);
                            int passLen = dis.read();
                            byte[] pass = new byte[passLen];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(pass, 0, passLen);
                            String sPASS = proceedISEM_B(pass);
                            String sNICK = null;
                            int nickLen = dis.read();
                            if (nickLen > 0) {
                                byte[] nick = new byte[nickLen];
                                //noinspection ResultOfMethodCallIgnored
                                dis.read(nick, 0, nickLen);
                                sNICK = proceedISEM_B(nick);
                            }
                            boolean autoconnect = dis.readBoolean();
                            boolean enabled = dis.readBoolean();
                            ICQProfile profile = new ICQProfile(sUIN, sPASS, this.svc, autoconnect, this.EnableFeatureFirstTime || enabled);
                            if (sNICK != null) {
                                profile.nickname = sNICK;
                            }
                            this.profiles.add(profile);
                            //noinspection ResultOfMethodCallIgnored
                            dis.skip(127L);
                            continue;
                        case IMProfile.JABBER:
                            int idLen = dis.read();
                            byte[] id = new byte[idLen];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(id, 0, idLen);
                            String ID = proceedISEM_B(id);
                            int serverLen = dis.read();
                            byte[] server = new byte[serverLen];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(server, 0, serverLen);
                            String Server = proceedISEM_B(server);
                            int hostLen = dis.read();
                            byte[] host = new byte[hostLen];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(host, 0, hostLen);
                            String Host = proceedISEM_B(host);
                            int passLen2 = dis.read();
                            byte[] pass2 = new byte[passLen2];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(pass2, 0, passLen2);
                            String sPASS2 = proceedISEM_B(pass2);
                            int type = dis.read();
                            boolean use_tls = dis.readBoolean();
                            boolean use_sasl = dis.readBoolean();
                            boolean use_compression = dis.readBoolean();
                            boolean autoconnect2 = dis.readBoolean();
                            int port = dis.readShort();
                            boolean enabled2 = dis.readBoolean();
                            String sNICK2 = null;
                            int nickLen2 = dis.readInt();
                            if (nickLen2 > 0) {
                                byte[] nick2 = new byte[nickLen2];
                                //noinspection ResultOfMethodCallIgnored
                                dis.read(nick2, 0, nickLen2);
                                sNICK2 = proceedISEM_B(nick2);
                            }
                            JProfile jprofile = new JProfile(this.svc, ID, Host, Server, port, sPASS2, sNICK2, use_compression, use_tls, use_sasl, autoconnect2, this.EnableFeatureFirstTime || enabled2, type);
                            this.profiles.add(jprofile);
                            //noinspection ResultOfMethodCallIgnored
                            dis.skip(121L);
                            continue;
                        case IMProfile.MMP:
                            int nameLen2 = dis.read();
                            byte[] Id = new byte[nameLen2];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(Id, 0, nameLen2);
                            String sID = proceedISEM_B(Id);
                            int passLen3 = dis.read();
                            byte[] pass3 = new byte[passLen3];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(pass3, 0, passLen3);
                            String sPASS3 = proceedISEM_B(pass3);
                            int nickLen3 = dis.read();
                            if (nickLen3 > 0) {
                                byte[] nick3 = new byte[nickLen3];
                                //noinspection ResultOfMethodCallIgnored
                                dis.read(nick3, 0, nickLen3);
                                proceedISEM_B(nick3);
                            }
                            boolean autoconnect3 = dis.readBoolean();
                            boolean enabled3 = dis.readBoolean();
                            MMPProfile mmp_profile = new MMPProfile(this.svc, sID, sPASS3, autoconnect3, this.EnableFeatureFirstTime || enabled3);
                            this.profiles.add(mmp_profile);
                            //noinspection ResultOfMethodCallIgnored
                            dis.skip(127L);
                    }
                }
            } catch (Exception e2) {
                //noinspection CallToPrintStackTrace
                e2.printStackTrace();
            }
        }
    }

    public final synchronized void writeProfilesToFile() {
        try {
            File profs = new File(resources.dataPath + "profiles.cfg");
            //noinspection ResultOfMethodCallIgnored
            profs.delete();
            //noinspection ResultOfMethodCallIgnored
            profs.createNewFile();
            //noinspection IOStreamConstructor
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(profs));
            int profilesCount = this.profiles.size();
            dos.write(profilesCount);
            for (int i = 0; i < profilesCount; i++) {
                IMProfile profile = this.profiles.get(i);
                switch (profile.profile_type) {
                    case IMProfile.OSCAR:
                        dos.write(IMProfile.OSCAR);
                        ICQProfile i_profile = (ICQProfile) profile;
                        String item = i_profile.ID;
                        byte[] buffer = proceedISEM_A(item);
                        dos.write(buffer.length);
                        dos.write(buffer);
                        String item2 = i_profile.password;
                        byte[] buf = proceedISEM_A(item2);
                        dos.write(buf.length);
                        dos.write(buf);
                        String item3 = i_profile.nickname;
                        byte[] buf2 = proceedISEM_A(item3);
                        dos.write(buf2.length);
                        if (!item3.isEmpty()) {
                            dos.write(buf2);
                        }
                        dos.writeBoolean(i_profile.autoconnect);
                        dos.writeBoolean(profile.enabled);
                        byte[] reserved = new byte[127];
                        dos.write(reserved);
                        break;
                    case IMProfile.JABBER:
                        dos.write(IMProfile.JABBER);
                        JProfile j_profile = (JProfile) profile;
                        String item4 = j_profile.ID;
                        byte[] buffer2 = proceedISEM_A(item4);
                        dos.write(buffer2.length);
                        dos.write(buffer2);
                        String item5 = j_profile.server;
                        byte[] buffer3 = proceedISEM_A(item5);
                        dos.write(buffer3.length);
                        dos.write(buffer3);
                        String item6 = j_profile.host;
                        byte[] buffer4 = proceedISEM_A(item6);
                        dos.write(buffer4.length);
                        dos.write(buffer4);
                        String item7 = j_profile.PASS;
                        byte[] buffer5 = proceedISEM_A(item7);
                        dos.write(buffer5.length);
                        dos.write(buffer5);
                        dos.write(j_profile.type);
                        dos.writeBoolean(j_profile.use_tls);
                        dos.writeBoolean(j_profile.use_sasl);
                        dos.writeBoolean(j_profile.use_compression);
                        dos.writeBoolean(j_profile.autoconnect);
                        dos.writeShort(j_profile.port);
                        dos.writeBoolean(profile.enabled);
                        String item8 = j_profile.nickname;
                        byte[] buffer6 = proceedISEM_A(item8);
                        dos.writeInt(buffer6.length);
                        dos.write(buffer6);
                        byte[] reserved2 = new byte[121];
                        dos.write(reserved2);
                        break;
                    case IMProfile.MMP:
                        dos.write(IMProfile.MMP);
                        MMPProfile mmp_profile = (MMPProfile) profile;
                        String item9 = mmp_profile.ID;
                        byte[] buffer7 = proceedISEM_A(item9);
                        dos.write(buffer7.length);
                        dos.write(buffer7);
                        String item10 = mmp_profile.PASS;
                        byte[] buf3 = proceedISEM_A(item10);
                        dos.write(buf3.length);
                        dos.write(buf3);
                        String item11 = mmp_profile.ID;
                        byte[] buf4 = proceedISEM_A(item11);
                        dos.write(buf4.length);
                        if (!item11.isEmpty()) {
                            dos.write(buf4);
                        }
                        dos.writeBoolean(mmp_profile.autoconnect);
                        dos.writeBoolean(profile.enabled);
                        byte[] reserved3 = new byte[127];
                        dos.write(reserved3);
                        break;
                }
            }
            dos.close();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        EventTranslator.sendProfilesList();
    }

    public static byte[] proceedISEM_A(String source) throws Exception {
        //noinspection InjectedReferences
        byte[] stepA = source.getBytes("windows1251");
        String stepB = utilities.convertToHex(stepA);
        //noinspection InjectedReferences
        byte[] stepC = stepB.getBytes("windows1251");
        byte j = 1;
        for (int i = 0; i < stepC.length; i++) {
            stepC[i] = (byte) (stepC[i] - j);
            j = (byte) (j + 1);
        }
        return stepC;
    }

    public static String proceedISEM_B(byte[] source) throws Exception {
        byte j = 1;
        for (int i = 0; i < source.length; i++) {
            source[i] = (byte) (source[i] + j);
            j = (byte) (j + 1);
        }
        //noinspection InjectedReferences
        String stepA = new String(source, "windows1251");
        byte[] stepB = utilities.hexStringToBytesArray(stepA);
        //noinspection InjectedReferences
        return new String(stepB, "windows1251");
    }

    public final boolean isProfileAlreadyExist(String ID) {
        int len = this.profiles.size();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type != IMProfile.JABBER) {
                if (profile.ID.equals(ID)) {
                    return true;
                }
            } else {
                JProfile jprofile = (JProfile) profile;
                if ((jprofile.ID + "@" + jprofile.host).equals(ID)) {
                    return true;
                }
            }
        }
        return false;
    }

    public final ICQProfile getProfileByUIN(String UIN) {
        int len = this.profiles.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                IMProfile profile = this.profiles.get(i);
                if (profile.profile_type == IMProfile.OSCAR) {
                    ICQProfile i_profile = (ICQProfile) profile;
                    if (i_profile.ID.equals(UIN)) {
                        return i_profile;
                    }
                }
            }
        }
        return null;
    }

    public final JProfile getProfileByID(String JID) {
        int len = this.profiles.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                IMProfile profile = this.profiles.get(i);
                if (profile.profile_type == IMProfile.JABBER) {
                    JProfile j_profile = (JProfile) profile;
                    if ((j_profile.ID + "@" + j_profile.host).equals(JID)) {
                        return j_profile;
                    }
                }
            }
        }
        return null;
    }

    public final MMPProfile getProfileByEmail(String email) {
        int len = this.profiles.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                IMProfile profile = this.profiles.get(i);
                if (profile.profile_type == IMProfile.MMP) {
                    MMPProfile m_profile = (MMPProfile) profile;
                    if (m_profile.ID.equals(email)) {
                        return m_profile;
                    }
                }
            }
        }
        return null;
    }

    /** @noinspection unused*/
    public final IMProfile getProfile(String full_id) {
        int len = this.profiles.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                IMProfile profile = this.profiles.get(i);
                if (IMProfile.getProfileFullID(profile).equals(full_id)) {
                    return profile;
                }
            }
        }
        return null;
    }

    public final void addProfile(IMProfile profile) {
        this.profiles.add(profile);
    }

    /** @noinspection unused*/
    public final void removeProfile(String ID) {
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.ID.equals(ID)) {
                profile.disconnect();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
                this.profiles.remove(i);
                return;
            }
        }
    }

    public final boolean isAnyProfileConnected() {
        int len = this.profiles.size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                IMProfile profile = this.profiles.get(i);
                if (profile.connected) {
                    return true;
                }
            }
        }
        return false;
    }

    public final void getUnreadMessagesDump(MessagesDump dump) {
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == IMProfile.JABBER) {
                ((JProfile) profile).getUnreadMessagesDump(dump);
            }
            if (profile.profile_type == IMProfile.OSCAR) {
                //noinspection DataFlowIssue
                ((ICQProfile) profile).getUnreadMessagesDump(dump);
            }
            if (profile.profile_type == IMProfile.MMP) {
                //noinspection DataFlowIssue
                ((MMPProfile) profile).getUnreadMessagesDump(dump);
            }
        }
    }

    public final int getProfilesCount() {
        return this.profiles.size();
    }

    public final int getEnabledProfilesCount() {
        int count = 0;
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.enabled) {
                count++;
            }
        }
        return count;
    }

    public final Vector<IMProfile> getProfiles() {
        return this.profiles;
    }

    /** @noinspection unused*/
    public final Vector<ICQProfile> getIcqProfiles() {
        Vector<ICQProfile> list = new Vector<>();
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == IMProfile.OSCAR) {
                list.add((ICQProfile) profile);
            }
        }
        return list;
    }

    public final Vector<JProfile> getJabberProfiles() {
        Vector<JProfile> list = new Vector<>();
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == IMProfile.JABBER) {
                list.add((JProfile) profile);
            }
        }
        return list;
    }

    /**
     * Check if there is at least one enabled Jabber profile.
     */
    public final boolean hasEnabledJabberProfile() {
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == IMProfile.JABBER && profile.enabled) {
                return true;
            }
        }
        return false;
    }

    public final boolean scanForConferences() {
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == IMProfile.JABBER && !((JProfile) profile).conference_items.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public final int getActiveProfilesCount() {
        int count = 0;
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == IMProfile.JABBER) {
                count += ((JProfile) profile).isAnyChatOpened() ? 1 : 0;
            }
            if (profile.profile_type == IMProfile.OSCAR) {
                //noinspection DataFlowIssue
                count += ((ICQProfile) profile).contactlist.isAnyChatOpened() ? 1 : 0;
            }
            if (profile.profile_type == IMProfile.MMP) {
                //noinspection DataFlowIssue
                count += ((MMPProfile) profile).isAnyChatOpened() ? 1 : 0;
            }
        }
        return count;
    }

    public final int getConferencedProfilesCount() {
        int count = 0;
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == IMProfile.JABBER) {
                count += !((JProfile) profile).conference_items.isEmpty() ? 1 : 0;
            }
        }
        return count;
    }

    public final void disconnectAll() {
        int length = this.profiles.size();
        for (int i = 0; i < length; i++) {
            IMProfile profile = this.profiles.get(i);
            profile.disconnect();
        }
    }

    public final void closeAllChats() {
        int length = this.profiles.size();
        for (int i = 0; i < length; i++) {
            IMProfile profile = this.profiles.get(i);
            profile.closeAllChats();
        }
    }
}