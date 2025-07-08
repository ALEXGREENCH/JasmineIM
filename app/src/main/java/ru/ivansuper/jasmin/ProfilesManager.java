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

/* loaded from: classes.dex */
public class ProfilesManager {
    private boolean EnableFeatureFirstTime;
    private final Vector<IMProfile> profiles = new Vector<>();
    private final jasminSvc svc;

    public ProfilesManager(jasminSvc svcParam) {
        this.svc = svcParam;
        File enable_feature_checker = new File(utilities.normalizePath(resources.dataPath) + "ProfileEnableFeature");
        if (!enable_feature_checker.exists()) {
            try {
                enable_feature_checker.createNewFile();
            } catch (Exception e) {
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
        File profs = new File(String.valueOf(resources.dataPath) + "profiles.cfg");
        if (!profs.exists()) {
            try {
                profs.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (profs.length() > 0) {
            try {
                DataInputStream dis = new DataInputStream(new FileInputStream(profs));
                int profilesCount = dis.read();
                for (int i = 0; i < profilesCount; i++) {
                    int profile_type = dis.read();
                    switch (profile_type) {
                        case 0:
                            int nameLen = dis.read();
                            byte[] uin = new byte[nameLen];
                            dis.read(uin, 0, nameLen);
                            String sUIN = proceedISEM_B(uin);
                            int passLen = dis.read();
                            byte[] pass = new byte[passLen];
                            dis.read(pass, 0, passLen);
                            String sPASS = proceedISEM_B(pass);
                            String sNICK = null;
                            int nickLen = dis.read();
                            if (nickLen > 0) {
                                byte[] nick = new byte[nickLen];
                                dis.read(nick, 0, nickLen);
                                sNICK = proceedISEM_B(nick);
                            }
                            boolean autoconnect = dis.readBoolean();
                            boolean enabled = dis.readBoolean();
                            ICQProfile profile = new ICQProfile(sUIN, sPASS, this.svc, autoconnect, this.EnableFeatureFirstTime ? true : enabled);
                            if (sNICK != null) {
                                profile.nickname = sNICK;
                            }
                            this.profiles.add(profile);
                            dis.skip(127L);
                            continue;
                        case 1:
                            int idLen = dis.read();
                            byte[] id = new byte[idLen];
                            dis.read(id, 0, idLen);
                            String ID = proceedISEM_B(id);
                            int serverLen = dis.read();
                            byte[] server = new byte[serverLen];
                            dis.read(server, 0, serverLen);
                            String Server = proceedISEM_B(server);
                            int hostLen = dis.read();
                            byte[] host = new byte[hostLen];
                            dis.read(host, 0, hostLen);
                            String Host = proceedISEM_B(host);
                            int passLen2 = dis.read();
                            byte[] pass2 = new byte[passLen2];
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
                                dis.read(nick2, 0, nickLen2);
                                sNICK2 = proceedISEM_B(nick2);
                            }
                            JProfile jprofile = new JProfile(this.svc, ID, Host, Server, port, sPASS2, sNICK2, use_compression, use_tls, use_sasl, autoconnect2, this.EnableFeatureFirstTime ? true : enabled2, type);
                            this.profiles.add(jprofile);
                            dis.skip(121L);
                            continue;
                        case 2:
                            int nameLen2 = dis.read();
                            byte[] Id = new byte[nameLen2];
                            dis.read(Id, 0, nameLen2);
                            String sID = proceedISEM_B(Id);
                            int passLen3 = dis.read();
                            byte[] pass3 = new byte[passLen3];
                            dis.read(pass3, 0, passLen3);
                            String sPASS3 = proceedISEM_B(pass3);
                            int nickLen3 = dis.read();
                            if (nickLen3 > 0) {
                                byte[] nick3 = new byte[nickLen3];
                                dis.read(nick3, 0, nickLen3);
                                proceedISEM_B(nick3);
                            }
                            boolean autoconnect3 = dis.readBoolean();
                            boolean enabled3 = dis.readBoolean();
                            MMPProfile mmp_profile = new MMPProfile(this.svc, sID, sPASS3, autoconnect3, this.EnableFeatureFirstTime ? true : enabled3);
                            this.profiles.add(mmp_profile);
                            dis.skip(127L);
                            continue;
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public final synchronized void writeProfilesToFile() {
        try {
            File profs = new File(String.valueOf(resources.dataPath) + "profiles.cfg");
            profs.delete();
            profs.createNewFile();
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(profs));
            int profilesCount = this.profiles.size();
            dos.write(profilesCount);
            for (int i = 0; i < profilesCount; i++) {
                IMProfile profile = this.profiles.get(i);
                switch (profile.profile_type) {
                    case 0:
                        dos.write(0);
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
                        if (item3.length() > 0) {
                            dos.write(buf2);
                        }
                        dos.writeBoolean(i_profile.autoconnect);
                        dos.writeBoolean(profile.enabled);
                        byte[] reserved = new byte[127];
                        dos.write(reserved);
                        break;
                    case 1:
                        dos.write(1);
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
                    case 2:
                        dos.write(2);
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
                        if (item11.length() > 0) {
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
            e.printStackTrace();
        }
        EventTranslator.sendProfilesList();
    }

    public static final byte[] proceedISEM_A(String source) throws Exception {
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

    public static final String proceedISEM_B(byte[] source) throws Exception {
        byte j = 1;
        for (int i = 0; i < source.length; i++) {
            source[i] = (byte) (source[i] + j);
            j = (byte) (j + 1);
        }
        //noinspection InjectedReferences
        String stepA = new String(source, "windows1251");
        byte[] stepB = utilities.hexStringToBytesArray(stepA);
        //noinspection InjectedReferences
        String stepC = new String(stepB, "windows1251");
        return stepC;
    }

    public final boolean isProfileAlreadyExist(String ID) {
        int len = this.profiles.size();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type != 1) {
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
                if (profile.profile_type == 0) {
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
                if (profile.profile_type == 1) {
                    JProfile j_profile = (JProfile) profile;
                    if ((String.valueOf(j_profile.ID) + "@" + j_profile.host).equals(JID)) {
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
                if (profile.profile_type == 2) {
                    MMPProfile m_profile = (MMPProfile) profile;
                    if (m_profile.ID.equals(email)) {
                        return m_profile;
                    }
                }
            }
        }
        return null;
    }

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

    public final void removeProfile(String ID) {
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.ID.equals(ID)) {
                profile.disconnect();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
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
            if (profile.profile_type == 1) {
                ((JProfile) profile).getUnreadMessagesDump(dump);
            }
            if (profile.profile_type == 0) {
                ((ICQProfile) profile).getUnreadMessagesDump(dump);
            }
            if (profile.profile_type == 2) {
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

    public final Vector<ICQProfile> getIcqProfiles() {
        Vector<ICQProfile> list = new Vector<>();
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == 0) {
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
            if (profile.profile_type == 1) {
                list.add((JProfile) profile);
            }
        }
        return list;
    }

    public final boolean scanForConferences() {
        int len = this.profiles.size();
        for (int i = 0; i < len; i++) {
            IMProfile profile = this.profiles.get(i);
            if (profile.profile_type == 1 && ((JProfile) profile).conference_items.size() > 0) {
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
            if (profile.profile_type == 1) {
                count += ((JProfile) profile).isAnyChatOpened() ? 1 : 0;
            }
            if (profile.profile_type == 0) {
                count += ((ICQProfile) profile).contactlist.isAnyChatOpened() ? 1 : 0;
            }
            if (profile.profile_type == 2) {
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
            if (profile.profile_type == 1) {
                count += ((JProfile) profile).conference_items.size() > 0 ? 1 : 0;
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