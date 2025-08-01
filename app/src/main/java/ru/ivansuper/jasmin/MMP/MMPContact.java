package ru.ivansuper.jasmin.MMP;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import ru.ivansuper.jasmin.locale.Locale;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.HistoryTools.ExportImportActivity;
import ru.ivansuper.jasmin.HistoryTools.HistoryTools;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.icq.ByteCache;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * Represents a contact in the MMP (Mail.Ru Agent) protocol.
 * This class extends {@link ContactlistItem} and adds MMP-specific
 * properties and functionalities related to avatars, message history,
 * status, and unread message management.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Managing contact avatars (fetching from network, reading from local cache).</li>
 *     <li>Handling message history (loading, saving, clearing, converting formats).</li>
 *     <li>Tracking unread messages and alarms.</li>
 *     <li>Storing contact status and profile information.</li>
 * </ul>
 * </p>
 *
 * <p>History data is stored in {@code .hst} files and a cache file ({@code .cache})
 * for faster loading of recent messages. The class also supports conversion
 * of history files from older formats to Unicode (UNI16).
 * </p>
 */
public class MMPContact extends ContactlistItem {
    public Drawable avatar;
    public int group;
    public boolean hasUnreadMessages;
    public boolean hasUnreadedAlarm;
    /** @noinspection unused*/
    public File historyCacheFile;
    /** @noinspection unused*/
    public File historyFile;
    public boolean isChating;
    public MMPProfile profile;
    public String status_text;
    public boolean typing;
    private int unreadCount;
    public int status = 0;
    public ArrayList<HistoryItem> history = new ArrayList<>();
    /** @noinspection unused*/
    public String typedText = "";
    public boolean historyPreLoaded = false;

    public MMPContact(String ID, String NAME, int group, MMPProfile profile) {
        this.itemType = 7;
        this.ID = ID;
        this.name = NAME;
        this.group = group;
        this.profile = profile;
        initHistoryFiles();
        File avatar_file = new File(resources.dataPath + profile.ID + "/avatars/" + ID);
        if (!avatar_file.exists()) {
            this.avatar = null;
        } else {
            readLocalAvatar();
        }
        boolean conversion_needed = checkHistoryFormat();
        if (conversion_needed) {
            if (!ExportImportActivity.CONVERTING_STARTED) {
                ExportImportActivity.CONVERTING_STARTED = true;
                resources.service.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
            try {
                if (!PERFORM_CONVERT_TO_UNI16()) {
                    throw new IllegalArgumentException(ID + " -- conversion error");
                }
            } catch (Exception e) {
                File historyFile = new File(resources.dataPath + profile.ID + "/history/" + ID + ".hst");
                historyFile.delete();
                File historyCacheFile = new File(resources.dataPath + profile.ID + "/history/" + ID + ".cache");
                historyCacheFile.delete();
                initHistoryFiles();
            } catch (OutOfMemoryError e2) {
                File historyFile2 = new File(resources.dataPath + profile.ID + "/history/" + ID + ".hst");
                historyFile2.delete();
                File historyCacheFile2 = new File(resources.dataPath + profile.ID + "/history/" + ID + ".cache");
                historyCacheFile2.delete();
                initHistoryFiles();
            }
        }
    }

    private void initHistoryFiles() {
        File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (historyFile.length() < 3) {
            try {
                historyFile.createNewFile();
                OutputStream os = new FileOutputStream(historyFile);
                os.write(85);
                os.write(78);
                os.write(73);
                os.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
        if (!historyCacheFile.exists()) {
            try {
                historyCacheFile.createNewFile();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
    }

    public final void getAvatar(final MMPContact contact, final jasminSvc service) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    service.showAvatarProgress(Locale.getString("s_loading_avatar"));
                    File avatar_file = new File(resources.dataPath + MMPContact.this.profile.ID + "/avatars/" + MMPContact.this.ID);
                    String url_ = "http://obraz.foto.mail.ru/" + MMPProtocol.retreiveDomain(contact.ID) + "/" + MMPProtocol.retreiveLogin(contact.ID) + "/_mrimavatarsmall";
                    Log.e("AvatarURL", url_);
                    URL url = new URL(url_);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    if (c.getResponseCode() == 200) {
                        InputStream in = c.getInputStream();
                        byte[] buffer = ByteCache.getByteArray(GifDecoder.MaxStackSize);
                        try {
                            if (!avatar_file.exists()) {
                                avatar_file.createNewFile();
                            }
                        } catch (Exception e) {
                        }
                        FileOutputStream fos = new FileOutputStream(resources.dataPath + contact.profile.ID + "/avatars/" + contact.ID);
                        while (true) {
                            int readed = in.read(buffer, 0, buffer.length);
                            if (readed >= 0) {
                                fos.write(buffer, 0, readed);
                            } else {
                                in.close();
                                fos.close();
                                ByteCache.recycle(buffer);
                                service.cancelAvatarProgress();
                                jasminSvc jasminsvc = service;
                                final MMPContact mMPContact = contact;
                                jasminsvc.runOnUi(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMPContact.readLocalAvatar();
                                    }
                                });
                                return;
                            }
                        }
                    } else {
                        try {
                            if (!avatar_file.exists()) {
                                avatar_file.createNewFile();
                            }
                        } catch (Exception e2) {
                        }
                        service.cancelAvatarProgress();
                    }
                    service.cancelAvatarProgress();
                } catch (Exception e3) {
                    service.cancelAvatarProgress();
                    e3.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public final void readLocalAvatar() {
        BufferedInputStream fis = null;
        Bitmap bmp = null;
        File avatar_file = new File(resources.dataPath + this.profile.ID + "/avatars/" + this.ID);
        this.avatar = null;
        if (avatar_file.length() != 0) {
            BufferedInputStream fis2 = null;
            try {
                fis = new BufferedInputStream(new FileInputStream(avatar_file));
                try {
                    bmp = BitmapFactory.decodeStream(fis);
                } catch (Exception e) {
                    fis2 = fis;
                }
            } catch (Exception e2) {
            }
            if (bmp == null) {
                throw new NullPointerException("Result bitmap is null");
            }
            this.avatar = new BitmapDrawable(bmp.copy(Bitmap.Config.ARGB_4444, false));
            fis2 = fis;
            if (fis2 != null) {
                try {
                    fis2.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            this.profile.svc.handleContactlistDatasetChanged();
        }
    }

    public final void clearPreloadedHistory() {
        this.history.clear();
        this.historyPreLoaded = false;
        System.gc();
    }

    public final void setHasUnreadedAlarm() {
        this.hasUnreadedAlarm = true;
    }

    public final void setHasUnreadMessages() {
        this.hasUnreadMessages = true;
        this.unreadCount++;
    }

    /** @noinspection unused*/
    public final void setHasNoUnreadMessages() {
        this.hasUnreadedAlarm = false;
        this.hasUnreadMessages = false;
        this.unreadCount = 0;
    }

    public final int getUnreadCount() {
        return this.unreadCount;
    }

    private void dumpLastHistory() throws IOException {
        int i = 10;
        int sz = this.history.size();
        int start = 0;
        if (sz < 10) {
            i = sz;
        } else {
            start = sz - 10;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream bos = new DataOutputStream(baos);
        for (int j = 0; j < i; j++) {
            try {
                HistoryItem message = this.history.get(start + j);
                bos.writeByte((byte) message.direction);
                bos.writeLong(message.date);
                bos.writeInt(0);
                bos.writeInt(message.message.length() * 2);
                utilities.writeStringUnicodeBE(message.message, bos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
        OutputStream os = new FileOutputStream(historyCacheFile, false);
        os.write(new byte[]{85, 78, 73});
        os.write(baos.toByteArray());
        os.close();
        bos.close();
        try {
            bos.close();
        } catch (Exception e2) {
        }
    }

    public final void loadLastHistory() {
        if (PreferenceTable.preloadHistory && !this.historyPreLoaded) {
            Vector<HistoryItem> temp = new Vector<>();
            DataInputStream dis = null;
            try {
                if (!this.historyPreLoaded) {
                    this.history.clear();
                    File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
                    if (historyCacheFile.length() > 0) {
                        DataInputStream dis2 = new DataInputStream(new FileInputStream(historyCacheFile));
                        try {
                            byte sig1 = dis2.readByte();
                            byte sig2 = dis2.readByte();
                            byte sig3 = dis2.readByte();
                            if (sig1 == 85 && sig2 == 78 && sig3 == 73) {
                                loadLastHistoryUNI16();
                                return;
                            }
                            dis2.close();
                            dis = new DataInputStream(new FileInputStream(historyCacheFile));
                            while (dis.available() > 0) {
                                int direction = dis.readByte();
                                long time = dis.readLong();
                                int msgLen = dis.readInt();
                                byte[] message = new byte[msgLen];
                                dis.read(message, 0, msgLen);
                                //noinspection InjectedReferences
                                String msg = new String(message, "windows1251");
                                HistoryItem item = new HistoryItem(time);
                                item.direction = direction;
                                item.confirmed = true;
                                item.message = msg;
                                item.mcontact = this;
                                temp.add(item);
                            }
                            try {
                                dis.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            this.historyPreLoaded = false;
                        }
                    }
                }
                try {
                    dis.close();
                } catch (Exception e3) {
                }
                this.historyPreLoaded = true;
                this.history.addAll(temp);
                temp.clear();
                this.profile.svc.handleChatNeedRefresh(this);
                System.gc();
            } catch (Exception e4) {
                e4.printStackTrace();
                this.historyPreLoaded = false;
            }
        }
    }

    public final void loadLastHistoryUNI16() {
        if (PreferenceTable.preloadHistory && !this.historyPreLoaded) {
            Vector<HistoryItem> temp = new Vector<>();
            DataInputStream dis = null;
            try {
                if (!this.historyPreLoaded) {
                    this.history.clear();
                    File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
                    if (historyCacheFile.length() > 0) {
                        DataInputStream dis2 = new DataInputStream(new FileInputStream(historyCacheFile));
                        try {
                            dis2.skip(3L);
                            while (dis2.available() > 0) {
                                int direction = dis2.readByte();
                                long time = dis2.readLong();
                                dis2.readInt();
                                int msgLen = dis2.readInt();
                                String msg = utilities.readStringUnicodeBE(dis2, msgLen);
                                HistoryItem item = new HistoryItem(time);
                                item.direction = direction;
                                item.confirmed = true;
                                item.message = msg;
                                item.mcontact = this;
                                temp.add(item);
                            }
                            try {
                                dis2.close();
                                dis = dis2;
                            } catch (Exception e) {
                                e.printStackTrace();
                                dis = dis2;
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            this.historyPreLoaded = false;
                            return;
                        }
                    }
                }
                try {
                    dis.close();
                } catch (Exception e3) {
                }
                this.historyPreLoaded = true;
                this.history.addAll(temp);
                temp.clear();
                this.profile.svc.handleChatNeedRefresh(this);
                System.gc();
            } catch (Exception e4) {

            }
        }
    }

    public final void loadHistory(Vector<HistoryItem> temp) throws IOException {
        DataInputStream dis = null;
        try {
            File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
            if (historyFile.length() > 0) {
                DataInputStream dis2 = new DataInputStream(new FileInputStream(historyFile));
                try {
                    byte sig1 = dis2.readByte();
                    byte sig2 = dis2.readByte();
                    byte sig3 = dis2.readByte();
                    if (sig1 == 85 && sig2 == 78 && sig3 == 73) {
                        dis2.close();
                        loadHistoryUNI16(temp);
                        return;
                    }
                    dis2.close();
                    dis = new DataInputStream(new FileInputStream(historyFile));
                    while (dis.available() > 0) {
                        int direction = dis.readByte();
                        long time = dis.readLong();
                        int msgLen = dis.readInt();
                        byte[] message = new byte[msgLen];
                        dis.read(message, 0, msgLen);
                        //noinspection InjectedReferences
                        String msg = new String(message, "windows1251");
                        HistoryItem item = new HistoryItem(time);
                        item.direction = direction;
                        item.confirmed = true;
                        item.message = msg;
                        item.mcontact = this;
                        temp.add(item);
                    }
                    try {
                        dis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e2) {
                    dis = dis2;
                    e2.printStackTrace();
                    dis.close();
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            dis.close();
        }
        try {
            dis.close();
        } catch (Exception e4) {
        }
    }

    public final void loadHistoryUNI16(Vector<HistoryItem> temp) {
        DataInputStream dis = null;
        try {
            File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
            if (historyFile.length() > 0) {
                DataInputStream dis2 = new DataInputStream(new FileInputStream(historyFile));
                try {
                    dis2.skip(3L);
                    while (dis2.available() > 0) {
                        int direction = dis2.readByte();
                        long time = dis2.readLong();
                        dis2.readInt();
                        int msgLen = dis2.readInt();
                        String msg = utilities.readStringUnicodeBE(dis2, msgLen);
                        HistoryItem item = new HistoryItem(time);
                        item.direction = direction;
                        item.confirmed = true;
                        item.message = msg;
                        item.mcontact = this;
                        temp.add(item);
                    }
                    try {
                        dis2.close();
                        dis = dis2;
                    } catch (IOException e) {
                        e.printStackTrace();
                        dis = dis2;
                    }
                } catch (Exception e2) {
                    dis = dis2;
                    e2.printStackTrace();
                    dis.close();
                }
            }
        } catch (Exception e3) {

        }
        try {
            dis.close();
        } catch (Exception e4) {
        }
    }

    public final void writeMessageToHistory(HistoryItem message) throws IOException {
        dumpLastHistory();
        if (PreferenceTable.writeHistory) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream bos = new DataOutputStream(baos);
            try {
                bos.writeByte((byte) message.direction);
                bos.writeLong(message.date);
                bos.writeInt(0);
                bos.writeInt(message.message.length() * 2);
                utilities.writeStringUnicodeBE(message.message, bos);
                synchronized (HistoryTools.WRITE_READ_LOCKER) {
                    OutputStream os = new FileOutputStream(new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst"), true);
                    os.write(baos.toByteArray());
                    os.close();
                }
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                bos.close();
            } catch (Exception e2) {
            }
            if (PreferenceTable.realtimeHistoryExport) {
                String full_id = IMProfile.getProfileFullID(this.profile);
                if (resources.sd_mounted()) {
                    File sdHistoryDir = new File(resources.JASMINE_SD_PATH + "NewExportedHistory(Unicode)/" + full_id);
                    if (!sdHistoryDir.isDirectory()) {
                        sdHistoryDir.mkdirs();
                    }
                    File historyFile = new File(resources.JASMINE_SD_PATH + "NewExportedHistory(Unicode)/" + full_id + "/[" + this.ID + "].txt");
                    if (!historyFile.exists()) {
                        try {
                            historyFile.createNewFile();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            return;
                        }
                    }
                    if (historyFile.length() == 0) {
                        try {
                            OutputStream os2 = new FileOutputStream(historyFile);
                            os2.write(254);
                            os2.write(255);
                            os2.close();
                        } catch (Exception e4) {
                        }
                    }
                    try {
                        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(historyFile, true)));
                        StringBuffer msg = new StringBuffer();
                        if (message.direction == 0) {
                            msg.append("<<: ");
                        } else {
                            msg.append(">>: ");
                        }
                        msg.append(message.fullFormattedDate());
                        msg.append(":\n");
                        if (message.isXtrazMessage) {
                            msg.append("XTRAZ:\n");
                        }
                        msg.append(message.message);
                        msg.append("\n---------------------\n");
                        try {
                            utilities.writeStringUnicodeBE(msg.toString(), out);
                            try {
                                out.close();
                            } catch (Exception e5) {
                            }
                        } catch (Exception e6) {
                            e6.printStackTrace();
                        }
                    } catch (FileNotFoundException e7) {
                        e7.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean checkHistoryFormat() {
        try {
            File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
            DataInputStream dis = new DataInputStream(new FileInputStream(historyFile));
            try {
                byte sig1 = dis.readByte();
                byte sig2 = dis.readByte();
                byte sig3 = dis.readByte();
                dis.close();
                return sig1 != 85 || sig2 != 78 || sig3 != 73;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e2) {
            return false;
        }
    }

    private final boolean PERFORM_CONVERT_TO_UNI16() {
        boolean success = false;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
            File newHistoryFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst_new");
            if (historyFile.length() > 0) {
                DataInputStream dis2 = new DataInputStream(new FileInputStream(historyFile));
                try {
                    DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(newHistoryFile));
                    try {
                        dos2.writeByte(85);
                        dos2.writeByte(78);
                        dos2.writeByte(73);
                        while (dis2.available() > 0) {
                            int direction = dis2.readByte();
                            long time = dis2.readLong();
                            int msgLen = dis2.readInt();
                            byte[] message = new byte[msgLen];
                            dis2.read(message, 0, msgLen);
                            //noinspection InjectedReferences
                            String msg = new String(message, "windows1251");
                            dos2.writeByte(direction);
                            dos2.writeLong(time);
                            dos2.writeInt(0);
                            dos2.writeInt(msgLen * 2);
                            utilities.writeStringUnicodeBE(msg, dos2);
                        }
                        try {
                            dis2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (historyFile.delete()) {
                            newHistoryFile.renameTo(new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst"));
                        }
                        dos = dos2;
                        dis = dis2;
                    } catch (Exception e2) {
                        dos = dos2;
                        dis = dis2;
                        success = false;
                        e2.printStackTrace();
                        dis.close();
                        dos.close();
                        return success;
                    }
                } catch (Exception e3) {
                    dis = dis2;
                }
            }
            success = true;
        } catch (Exception e4) {
        }
        try {
            dis.close();
            dos.close();
        } catch (Exception e5) {
        }
        return success;
    }
}