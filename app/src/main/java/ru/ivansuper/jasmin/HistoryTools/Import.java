package ru.ivansuper.jasmin.HistoryTools;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class Import {

    public enum Result {
        UNKNOWN_ERROR,
        INCORRECT_PROFILE,
        SUCCESS;

        /** @noinspection unused*/
        public static Result[] valuesCustom() {
            Result[] valuesCustom = values();
            int length = valuesCustom.length;
            Result[] resultArr = new Result[length];
            System.arraycopy(valuesCustom, 0, resultArr, 0, length);
            return resultArr;
        }
    }

    public static Import getInstance() {
        return new Import();
    }

    public final Result performImport(File file, IMProfile profile_) {
        if (file != null && file.exists()) {
            DataInputStream dis;
            try {
                //noinspection IOStreamConstructor
                DataInputStream dis2 = new DataInputStream(new FileInputStream(file));
                try {
                    byte sig1 = dis2.readByte();
                    byte sig2 = dis2.readByte();
                    byte sig3 = dis2.readByte();
                    byte sig4 = dis2.readByte();
                    if (sig1 != 74 || sig2 != 72 || sig3 != 65 || sig4 != 50) {
                        throw new Exception("Invalid format");
                    }
                    String profile_id = utilities.readPreLengthStringUnicodeBE(dis2);
                    IMProfile profile = resources.service.profiles.getProfile(profile_id);
                    if (!profile_id.equals(IMProfile.getProfileFullID(profile_))) {
                        return Result.INCORRECT_PROFILE;
                    }
                    if (profile == null) {
                        return Result.INCORRECT_PROFILE;
                    }
                    //noinspection ResultOfMethodCallIgnored
                    dis2.skip(2L);
                    Log.e("Import", "Importing parts ...");
                    switch (profile.profile_type) {
                        case 0:
                            import_(dis2, (ICQProfile) profile);
                            break;
                        case 1:
                            import_(dis2, (JProfile) profile);
                            break;
                        case 2:
                            import_(dis2, (MMPProfile) profile);
                            break;
                    }
                    return Result.SUCCESS;
                } catch (Exception e) {
                    dis = dis2;
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    try {
                        dis.close();
                    } catch (Exception ignored) {
                    }
                    return Result.UNKNOWN_ERROR;
                }
            } catch (Exception ignored) {

            }
        }
        return Result.UNKNOWN_ERROR;
    }

    private void import_(DataInputStream dis, ICQProfile profile) throws IOException {
        while (dis.available() > 0) {
            try {
                String contact_id = utilities.readPreLengthStringUnicodeBE(dis);
                if (profile.contactlist.getContactByUIN(contact_id) != null) {
                    File historyCacheFile = new File(resources.dataPath + profile.ID + "/history/" + contact_id + ".cache");
                    File historyFile = new File(resources.dataPath + profile.ID + "/history/" + contact_id + ".hst");
                    readPart(dis, historyCacheFile);
                    readPart(dis, historyFile);
                } else {
                    return;
                }
            } catch (Exception e) {
                return;
            }
        }
    }

    private void import_(DataInputStream dis, JProfile profile) throws IOException {
        while (dis.available() > 0) {
            try {
                String contact_id = utilities.readPreLengthStringUnicodeBE(dis);
                if (profile.getContactByJID(contact_id) != null) {
                    Log.e(getClass().getName(), "Processing contact: " + contact_id);
                    File historyCacheFile = new File(resources.dataPath + profile.ID + "@" + profile.host + "/history/" + contact_id + ".cache");
                    File historyFile = new File(resources.dataPath + profile.ID + "@" + profile.host + "/history/" + contact_id + ".hst");
                    readPart(dis, historyCacheFile);
                    readPart(dis, historyFile);
                } else {
                    return;
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return;
            }
        }
    }

    private void import_(DataInputStream dis, MMPProfile profile) throws IOException {
        while (dis.available() > 0) {
            try {
                String contact_id = utilities.readPreLengthStringUnicodeBE(dis);
                if (profile.getContactByID(contact_id) != null) {
                    File historyCacheFile = new File(resources.dataPath + profile.ID + "/history/" + contact_id + ".cache");
                    File historyFile = new File(resources.dataPath + profile.ID + "/history/" + contact_id + ".hst");
                    readPart(dis, historyCacheFile);
                    readPart(dis, historyFile);
                } else {
                    return;
                }
            } catch (Exception e) {
                return;
            }
        }
    }

    private void readPart(DataInputStream dis, File file) {
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (Exception e) {
                return;
            }
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            dis.skip(3L);
            int length = dis.readInt();
            //noinspection IOStreamConstructor
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
            int total = 0;
            int needed = 255;
            while (needed > 0) {
                needed = length - total;
                byte[] buffer = new byte[GifDecoder.MaxStackSize];
                int readed = dis.read(buffer, 0, Math.min(needed, 4096));
                if (readed >= 0) {
                    dos.write(buffer, 0, readed);
                    total += readed;
                }
            }
            try {
                dos.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e3) {
            //noinspection CallToPrintStackTrace
            e3.printStackTrace();
        }
    }
}