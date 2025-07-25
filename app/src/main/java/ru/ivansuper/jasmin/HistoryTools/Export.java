package ru.ivansuper.jasmin.HistoryTools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Vector;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * The Export class provides functionality for exporting chat history data for various instant messaging profiles.
 * It supports exporting data for ICQ, JABBER, and MMP profiles. The exported data is written to a specified file
 * in a custom binary format.
 *
 * <p>The class uses a singleton pattern, and an instance can be obtained using the {@link #getInstance()} method.
 * The main export functionality is provided by the {@link #performExport(File, IMProfile)} method, which
 * delegates to profile-specific export methods based on the profile type.
 *
 * <p>Each profile-specific export method writes a header with a magic number ("JHA2"), the profile ID, profile type,
 * and profile-specific data (e.g., JABBER profile type). It then iterates over the contacts in the profile's
 * contact list and writes the contact ID, history cache file, and history file for each contact.
 *
 * <p>The {@link #writePart(DataOutputStream, File)} method is a helper method used to write the contents of a file
 * (e.g., history cache or history file) to the output stream. It first writes the length of the file as an integer,
 * followed by the file's content.
 *
 * <p>Error handling is implemented throughout the class, with exceptions caught and handled appropriately.
 * In case of errors during file operations or data writing, the export process may be aborted, and the
 * corresponding export method will return {@code false}.
 */
public class Export {
    public static Export getInstance() {
        return new Export();
    }

    /** @noinspection SameReturnValue*/
    public final boolean performExport(File file, IMProfile profile) {
        if (file == null || profile == null) {
            return false;
        }
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (Exception e) {
                return false;
            }
        }
        //noinspection StatementWithEmptyBody
        switch (profile.profile_type) {
        }
        return false;
    }

    /** @noinspection unused*/
    private boolean exportICQ(File file, ICQProfile profile) {
        DataOutputStream dos = null;
        DataOutputStream dos2;
        try {
            //noinspection IOStreamConstructor
            dos = new DataOutputStream(new FileOutputStream(file));
        } catch (Exception ignored) {
        }
        try {
            //noinspection DataFlowIssue
            dos.write(new byte[]{74, 72, 65, 50});
            Vector<ICQContact> contacts = profile.contactlist.getContacts();
            utilities.writePreLengthStringUnicodeBE(IMProfile.getProfileFullID(profile), dos);
            dos.write(profile.profile_type);
            dos.write(0);
            for (ICQContact contact : contacts) {
                File historyCacheFile = new File(resources.dataPath + contact.profile.ID + "/history/" + contact.ID + ".cache");
                File historyFile = new File(resources.dataPath + contact.profile.ID + "/history/" + contact.ID + ".hst");
                utilities.writePreLengthStringUnicodeBE(contact.ID, dos);
                writePart(dos, historyCacheFile);
                writePart(dos, historyFile);
            }
            try {
                dos.close();
            } catch (Exception ignored) {
            }
            return true;
        } catch (Exception e3) {
            dos2 = dos;
            try {
                dos2.close();
                return false;
            } catch (Exception e4) {
                return false;
            }
        }
    }

    /** @noinspection unused*/
    private boolean exportJABBER(File file, JProfile profile) {
        DataOutputStream dos = null;
        DataOutputStream dos2;
        //noinspection CatchMayIgnoreException
        try {
            //noinspection IOStreamConstructor
            dos = new DataOutputStream(new FileOutputStream(file));
        } catch (Exception e) {

        }
        try {
            //noinspection DataFlowIssue
            dos.write(new byte[]{74, 72, 65, 50});
            Vector<JContact> contacts = profile.getContactsCasted();
            utilities.writePreLengthStringUnicodeBE(IMProfile.getProfileFullID(profile), dos);
            dos.write(profile.profile_type);
            dos.write(profile.type);
            for (JContact contact : contacts) {
                File historyCacheFile = new File(resources.dataPath + profile.ID + "@" + profile.host + "/history/" + contact.ID + ".cache");
                File historyFile = new File(resources.dataPath + profile.ID + "@" + profile.host + "/history/" + contact.ID + ".hst");
                utilities.writePreLengthStringUnicodeBE(contact.ID, dos);
                writePart(dos, historyCacheFile);
                writePart(dos, historyFile);
            }
            try {
                dos.close();
            } catch (Exception e2) {
                //noinspection CallToPrintStackTrace
                e2.printStackTrace();
            }
            return true;
        } catch (Exception e3) {
            dos2 = dos;
            //noinspection CallToPrintStackTrace
            e3.printStackTrace();
            try {
                dos2.close();
            } catch (Exception e1) {
                //noinspection CallToPrintStackTrace
                e1.printStackTrace();
            }
            return false;
        }
    }

    /** @noinspection unused*/
    private boolean exportMMP(File file, MMPProfile profile) {
        DataOutputStream dos;
        try {
            //noinspection IOStreamConstructor
            DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(file));
            try {
                dos2.write(new byte[]{74, 72, 65, 50});
                Vector<MMPContact> contacts = profile.getContacts();
                utilities.writePreLengthStringUnicodeBE(IMProfile.getProfileFullID(profile), dos2);
                dos2.write(profile.profile_type);
                dos2.write(0);
                for (MMPContact contact : contacts) {
                    File historyCacheFile = new File(resources.dataPath + profile.ID + "/history/" + contact.ID + ".cache");
                    File historyFile = new File(resources.dataPath + profile.ID + "/history/" + contact.ID + ".hst");
                    utilities.writePreLengthStringUnicodeBE(contact.ID, dos2);
                    writePart(dos2, historyCacheFile);
                    writePart(dos2, historyFile);
                }
                try {
                    dos2.close();
                } catch (Exception ignored) {
                }
                return true;
            } catch (Exception e2) {
                dos = dos2;
                //noinspection CallToPrintStackTrace
                e2.printStackTrace();
                try {
                    dos.close();
                    return false;
                } catch (Exception e3) {
                    return false;
                }
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    private void writePart(DataOutputStream dos, File part) throws Exception {
        dos.writeInt((int) part.length());
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(part));
            while (dis.available() > 0) {
                byte[] buffer = new byte[32768];
                int readed = dis.read(buffer, 0, 32768);
                dos.write(buffer, 0, readed);
            }
            try {
                dis.close();
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        } catch (FileNotFoundException ignored) {
        }
    }
}