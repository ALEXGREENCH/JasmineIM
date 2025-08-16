package ru.ivansuper.jasmin.icq;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.ivansuper.jasmin.Clients.ClientInfo;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.HistoryTools.ExportImportActivity;
import ru.ivansuper.jasmin.HistoryTools.HistoryTools;
import ru.ivansuper.jasmin.LogW;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * Represents an ICQ contact, extending the base ContactlistItem.
 * This class stores various details about an ICQ contact, including their
 * avatar, status, profile information, capabilities, message history,
 * and other ICQ-specific attributes.
 * <p>
 * It provides methods for managing contact information, handling avatars
 * (fetching, reading local), managing message history (loading, writing,
 * preloading, clearing), and checking contact states (visible, invisible,
 * ignore).
 * <p>
 * The class also includes functionality for history format conversion
 * (e.g., to UNI16) and real-time history export.
 */
public class ICQContact extends ContactlistItem {
    public final Capabilities capabilities = new Capabilities();
    public final InfoContainer inf = new InfoContainer();
    public final ClientInfo client = new ClientInfo();
    public final DCInfo dc_info = new DCInfo();
    public final ArrayList<HistoryItem> history = new ArrayList<>();
    public Drawable avatar;
    public String away_status;
    public int group;
    public int id;
    public ICQProfile profile;
    public byte[] transfer_cookie;
    public String xtraz_text;
    public int status = -1;
    public int protoVersion = 0;
    /** @noinspection unused*/
    public int fb0 = 0;
    /** @noinspection unused*/
    public int fb1 = 0;
    /** @noinspection unused*/
    public int fb2 = 0;
    public long signOnTime = 0;
    public boolean hasUnreadMessages = false;
    public boolean typing = false;
    public boolean authorized = true;
    public boolean added = true;
    public int currentEncoding = -1;
    public Drawable xstatus = null;
    /** @noinspection unused*/
    public int xsts = -1;
    public boolean hasUnreadedAuthRequest = false;
    public boolean hasUnreadedFileRequest = false;
    public boolean historyPreLoaded = false;
    /** @noinspection unused*/
    public String typedText = "";
    public boolean isChating = false;
    public boolean as_accepted = true;
    private int unread_count;

    public ICQContact() {
        this.itemType = 1;
    }

    /** @noinspection unused*/
    public static void getAvatar(final Callback callback, final String UIN, String PID, final jasminSvc service) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    service.showAvatarProgress(Locale.getString("s_loading_avatar"));
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(resources.ctx);
                    String base = sp.getString("ms_avatar_base_url", "http://217.147.15.238");
                    URL url = new URL(base + "/avatar/" + UIN + "?hq=1");
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    if (c.getResponseCode() == 200) {
                        InputStream in = new BufferedInputStream(c.getInputStream());
                        Drawable icon = Drawable.createFromStream(in, "Avatar");
                        in.close();
                        if (callback != null) {
                            callback.notify(icon, 0);
                        }
                    }
                    service.cancelAvatarProgress();
                } catch (Exception e) {
                    service.cancelAvatarProgress();
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    /** @noinspection unused*/
    public final void setStatus1(String status) {
        this.xtraz_text = status;
    }

    /** @noinspection unused*/
    public final void setStatus2(String status) {
        this.away_status = status;
    }

    public final void init() {
        initHistoryFiles();
        File avatar_file = new File(resources.dataPath + this.profile.ID + "/avatars/" + this.ID);
        if (!avatar_file.exists()) {
            this.avatar = null;
        } else {
            try {
                readLocalAvatar();
            } catch (OutOfMemoryError e) {
                LogW.trw("ICQContact", e);
            }
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
                    throw new IllegalArgumentException(this.ID + " -- conversion error");
                }
            } catch (Exception e2) {
                File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
                //noinspection ResultOfMethodCallIgnored
                historyFile.delete();
                File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
                //noinspection ResultOfMethodCallIgnored
                historyCacheFile.delete();
                initHistoryFiles();
            } catch (OutOfMemoryError e3) {
                File historyFile2 = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
                //noinspection ResultOfMethodCallIgnored
                historyFile2.delete();
                File historyCacheFile2 = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
                //noinspection ResultOfMethodCallIgnored
                historyCacheFile2.delete();
                initHistoryFiles();
            }
        }
    }

    private void initHistoryFiles() {
        File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
        if (!historyFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                historyFile.createNewFile();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        if (historyFile.length() < 3) {
            try {
                //noinspection ResultOfMethodCallIgnored
                historyFile.createNewFile();
                //noinspection IOStreamConstructor
                OutputStream os = new FileOutputStream(historyFile);
                os.write(85);
                os.write(78);
                os.write(73);
                os.close();
            } catch (IOException e2) {
                //noinspection CallToPrintStackTrace
                e2.printStackTrace();
            }
        }
        File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
        if (!historyCacheFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                historyCacheFile.createNewFile();
            } catch (IOException e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
            }
        }
    }

    public final void getAvatar(final ICQContact contact, final jasminSvc service) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    service.showAvatarProgress(Locale.getString("s_loading_avatar"));
                    File avatar_file = new File(resources.dataPath + ICQContact.this.profile.ID + "/avatars/" + ICQContact.this.ID);
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(resources.ctx);
                    String base = sp.getString("ms_avatar_base_url", "http://217.147.15.238");
                    URL url = new URL(base + "/avatar/" + contact.ID);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();

                    if (c.getResponseCode() == 200) {
                        InputStream in = c.getInputStream();
                        byte[] buffer = ByteCache.getByteArray(GifDecoder.MaxStackSize);

                        try {
                            if (!avatar_file.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                avatar_file.createNewFile();
                            }
                        } catch (Exception ignored) {
                        }

                        FileOutputStream fos = new FileOutputStream(resources.dataPath + contact.profile.ID + "/avatars/" + contact.ID);

                        while (true) {
                            int readed = in.read(buffer, 0, buffer.length);
                            if (readed >= 0) {
                                fos.write(buffer, 0, readed);
                            } else {
                                try {
                                    in.close();
                                } catch (Exception ignored) {
                                }

                                try {
                                    fos.close();
                                } catch (Exception ignored) {
                                }

                                ByteCache.recycle(buffer);
                                service.cancelAvatarProgress();
                                service.runOnUi(new Runnable() {
                                    @Override
                                    public void run() {
                                        contact.readLocalAvatar();
                                    }
                                });

                                return;
                            }
                        }
                    } else {
                        try {
                            if (!avatar_file.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                avatar_file.createNewFile();
                            }
                        } catch (Exception e2) {
                            // ignore
                        }
                        service.cancelAvatarProgress();
                    }
                    service.cancelAvatarProgress();
                } catch (Exception e3) {
                    service.cancelAvatarProgress();
                    //noinspection CallToPrintStackTrace
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
            BufferedInputStream fis2;
            try {
                //noinspection IOStreamConstructor
                fis = new BufferedInputStream(new FileInputStream(avatar_file));
                try {
                    bmp = BitmapFactory.decodeStream(fis);
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
            }
            if (bmp == null) {
                throw new NullPointerException("Result bitmap is null");
            }
            this.avatar = new BitmapDrawable(bmp.copy(Bitmap.Config.ARGB_4444, false));
            fis2 = fis;
            //noinspection ConstantValue
            if (fis2 != null) {
                try {
                    fis2.close();
                } catch (IOException e3) {
                    //noinspection CallToPrintStackTrace
                    e3.printStackTrace();
                }
            }
            this.profile.svc.handleContactlistDatasetChanged();
        }
    }

    /** @noinspection unused*/
    public final Drawable getLocalAvatar() {
        BufferedInputStream fis = null;
        Bitmap bmp = null;
        File avatar_file = new File(resources.dataPath + this.profile.ID + "/avatars/" + this.ID);
        Drawable avatar = resources.ctx.getResources().getDrawable(R.drawable.no_avatar);
        if (avatar_file.length() == 0) {
            return avatar;
        }
        BufferedInputStream fis2;
        try {
            //noinspection IOStreamConstructor
            fis = new BufferedInputStream(new FileInputStream(avatar_file));
            try {
                bmp = BitmapFactory.decodeStream(fis);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        if (bmp == null) {
            throw new NullPointerException("Result bitmap is null");
        }
        fis2 = fis;
        avatar = new BitmapDrawable(bmp.copy(Bitmap.Config.ARGB_4444, false));
        //noinspection ConstantValue
        if (fis2 != null) {
            try {
                fis2.close();
            } catch (IOException e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
            }
        }
        return avatar;
    }

    public final void clearPreloadedHistory() {
        this.history.clear();
        this.historyPreLoaded = false;
        System.gc();
    }

    public final boolean isVisible() {
        return this.profile.isInVisible(this.ID) != null;
    }

    public final boolean isInvisible() {
        return this.profile.isInInvisible(this.ID) != null;
    }

    public final boolean isIgnore() {
        return this.profile.isInIgnore(this.ID) != null;
    }

    public final void setHasUnreadMessages() {
        this.unread_count++;
        this.hasUnreadMessages = true;
    }

    /** @noinspection unused*/
    public final void setHasNoUnreadMessages() {
        this.hasUnreadMessages = false;
        this.unread_count = 0;
    }

    public final int getUnreadCount() {
        return this.unread_count;
    }

    public final InfoContainer getInfo() {
        this.inf.uin = this.ID;
        return this.inf;
    }

    private void dumpLastHistory() {
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
                bos.writeBoolean(message.isXtrazMessage);
                bos.writeInt(0);
                bos.writeLong(message.date);
                bos.writeInt(message.message.length() * 2);
                utilities.writeStringUnicodeBE(message.message, new DataOutputStream(bos));
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
        OutputStream os;
        try {
            os = new FileOutputStream(historyCacheFile, false);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            os.write(new byte[]{85, 78, 73});
            os.write(baos.toByteArray());
            os.close();
            bos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            bos.close();
        } catch (Exception ignored) {
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
                        //noinspection IOStreamConstructor
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
                            //noinspection IOStreamConstructor
                            dis = new DataInputStream(new FileInputStream(historyCacheFile));
                            while (dis.available() > 0) {
                                int direction = dis.readByte();
                                boolean xtraz = dis.readBoolean();
                                long time = dis.readLong();
                                int msgLen = dis.readInt();
                                byte[] message = new byte[msgLen];
                                //noinspection ResultOfMethodCallIgnored
                                dis.read(message, 0, msgLen);
                                //noinspection InjectedReferences
                                String msg = new String(message, "windows1251");
                                HistoryItem item = new HistoryItem(time);
                                item.direction = direction;
                                item.confirmed = true;
                                item.message = msg;
                                item.isXtrazMessage = xtraz;
                                item.contact = this;
                                temp.add(item);
                            }
                            try {
                                dis.close();
                            } catch (Exception e) {
                                //noinspection CallToPrintStackTrace
                                e.printStackTrace();
                            }
                        } catch (Exception e2) {
                            //noinspection CallToPrintStackTrace
                            e2.printStackTrace();
                            this.historyPreLoaded = false;
                            this.profile.makeShortToast(resources.getString("s_history_preload_error"));
                            return;
                        }
                    }
                }
                try {
                    //noinspection DataFlowIssue
                    dis.close();
                } catch (Exception ignored) {
                }
                this.historyPreLoaded = true;
                this.history.addAll(temp);
                temp.clear();
                this.profile.svc.handleChatNeedRefresh(this);
                System.gc();
            } catch (Exception e4) {
                ////e = e4;
            }
        }
    }

    private void loadLastHistoryUNI16() {
        if (PreferenceTable.preloadHistory && !this.historyPreLoaded) {
            Vector<HistoryItem> temp = new Vector<>();
            DataInputStream dis = null;
            try {
                if (!this.historyPreLoaded) {
                    this.history.clear();
                    File historyCacheFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".cache");
                    if (historyCacheFile.length() > 0) {
                        //noinspection IOStreamConstructor
                        DataInputStream dis2 = new DataInputStream(new FileInputStream(historyCacheFile));
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            dis2.skip(3L);
                            while (dis2.available() > 0) {
                                int direction = dis2.readByte();
                                boolean xtraz = dis2.readBoolean();
                                dis2.readInt();
                                long time = dis2.readLong();
                                int msgLen = dis2.readInt();
                                String msg = utilities.readStringUnicodeBE(dis2, msgLen);
                                HistoryItem item = new HistoryItem(time);
                                item.direction = direction;
                                item.confirmed = true;
                                item.message = msg;
                                item.isXtrazMessage = xtraz;
                                item.contact = this;
                                temp.add(item);
                            }
                            try {
                                dis2.close();
                                dis = dis2;
                            } catch (Exception e) {
                                //noinspection CallToPrintStackTrace
                                e.printStackTrace();
                                dis = dis2;
                            }
                        } catch (Exception e2) {
                            //noinspection CallToPrintStackTrace
                            e2.printStackTrace();
                            this.historyPreLoaded = false;
                            this.profile.makeShortToast(resources.getString("s_history_preload_error"));
                            return;
                        }
                    }
                }
                try {
                    //noinspection DataFlowIssue
                    dis.close();
                } catch (Exception ignored) {
                }
                this.historyPreLoaded = true;
                this.history.addAll(temp);
                temp.clear();
                this.profile.svc.handleChatNeedRefresh(this);
                System.gc();
            } catch (Exception ignored) {

            }
        }
    }

    public final void loadHistory(Vector<HistoryItem> temp) {
        DataInputStream dis = null;
        try {
            try {
                File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
                if (historyFile.length() > 0) {
                    //noinspection IOStreamConstructor
                    DataInputStream dis2 = new DataInputStream(new FileInputStream(historyFile));
                    try {
                        byte sig1 = dis2.readByte();
                        byte sig2 = dis2.readByte();
                        byte sig3 = dis2.readByte();
                        if (sig1 == 85 && sig2 == 78 && sig3 == 73) {
                            loadHistoryUNI16(temp);
                            return;
                        }
                        dis2.close();
                        //noinspection IOStreamConstructor
                        dis = new DataInputStream(new FileInputStream(historyFile));
                        while (dis.available() > 0) {
                            int direction = dis.readByte();
                            boolean xtraz = dis.readBoolean();
                            long time = dis.readLong();
                            int msgLen = dis.readInt();
                            byte[] message = new byte[msgLen];
                            //noinspection ResultOfMethodCallIgnored
                            dis.read(message, 0, msgLen);
                            //noinspection InjectedReferences
                            String msg = new String(message, "windows1251");
                            HistoryItem item = new HistoryItem(time);
                            item.direction = direction;
                            item.confirmed = true;
                            item.message = msg;
                            item.isXtrazMessage = xtraz;
                            item.contact = this;
                            temp.add(item);
                        }
                        try {
                            dis.close();
                        } catch (IOException e) {
                            //noinspection CallToPrintStackTrace
                            e.printStackTrace();
                        }
                    } catch (Exception e2) {
                        dis = dis2;
                        //noinspection CallToPrintStackTrace
                        e2.printStackTrace();
                        dis.close();
                    }
                }
            } catch (Exception e3) {
                //noinspection CallToPrintStackTrace
                e3.printStackTrace();
                //noinspection DataFlowIssue
                dis.close();
            }
            //noinspection DataFlowIssue
            dis.close();
        } catch (Exception ignored) {
        }
    }

    private void loadHistoryUNI16(Vector<HistoryItem> temp) {
        DataInputStream dis = null;
        try {
            File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
            if (historyFile.length() > 3) {
                //noinspection IOStreamConstructor
                DataInputStream dis2 = new DataInputStream(new FileInputStream(historyFile));
                try {
                    //noinspection ResultOfMethodCallIgnored
                    dis2.skip(3L);
                    while (dis2.available() > 0) {
                        int direction = dis2.readByte();
                        boolean xtraz = dis2.readBoolean();
                        dis2.readInt();
                        long time = dis2.readLong();
                        int msgLen = dis2.readInt();
                        String msg = utilities.readStringUnicodeBE(dis2, msgLen);
                        HistoryItem item = new HistoryItem(time);
                        item.direction = direction;
                        item.confirmed = true;
                        item.message = msg;
                        item.isXtrazMessage = xtraz;
                        item.contact = this;
                        temp.add(item);
                    }
                    try {
                        dis2.close();
                        dis = dis2;
                    } catch (IOException e) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                        dis = dis2;
                    }
                } catch (Exception e2) {
                    dis = dis2;
                    //noinspection CallToPrintStackTrace
                    e2.printStackTrace();
                    dis.close();
                }
            }
        } catch (Exception ignored) {
        }
        try {
            //noinspection DataFlowIssue
            dis.close();
        } catch (Exception ignored) {
        }
    }

    public final void writeMessageToHistory(HistoryItem message) {
        dumpLastHistory();
        if (PreferenceTable.writeHistory) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream bos = new DataOutputStream(baos);
            try {
                bos.writeByte((byte) message.direction);
                bos.writeBoolean(message.isXtrazMessage);
                bos.writeInt(0);
                bos.writeLong(message.date);
                bos.writeInt(message.message.length() * 2);
                utilities.writeStringUnicodeBE(message.message, bos);
                synchronized (HistoryTools.WRITE_READ_LOCKER) {
                    OutputStream os = new FileOutputStream(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst", true);
                    os.write(baos.toByteArray());
                    os.close();
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
            //noinspection CatchMayIgnoreException
            try {
                bos.close();
            } catch (Exception e2) {
            }
            if (PreferenceTable.realtimeHistoryExport) {
                String full_id = IMProfile.getProfileFullID(this.profile);
                if (resources.sd_mounted()) {
                    File sdHistoryDir = new File(resources.JASMINE_SD_PATH + "NewExportedHistory(Unicode)/" + full_id);
                    if (!sdHistoryDir.isDirectory()) {
                        //noinspection ResultOfMethodCallIgnored
                        sdHistoryDir.mkdirs();
                    }
                    File historyFile = new File(resources.JASMINE_SD_PATH + "NewExportedHistory(Unicode)/" + full_id + "/[" + this.ID + "].txt");
                    if (!historyFile.exists()) {
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            historyFile.createNewFile();
                        } catch (IOException e3) {
                            //noinspection CallToPrintStackTrace
                            e3.printStackTrace();
                            return;
                        }
                    }
                    if (historyFile.length() == 0) {
                        try {
                            //noinspection IOStreamConstructor
                            OutputStream os2 = new FileOutputStream(historyFile);
                            os2.write(254);
                            os2.write(255);
                            os2.close();
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(historyFile, true)));
                        StringBuilder msg = new StringBuilder();
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
                            } catch (Exception ignored) {
                            }
                        } catch (Exception e6) {
                            //noinspection CallToPrintStackTrace
                            e6.printStackTrace();
                        }
                    } catch (FileNotFoundException e7) {
                        //noinspection CallToPrintStackTrace
                        e7.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean checkHistoryFormat() {
        try {
            File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
            //noinspection IOStreamConstructor
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

    private boolean PERFORM_CONVERT_TO_UNI16() {
        boolean success = false;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            File historyFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst");
            File newHistoryFile = new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst_new");
            if (historyFile.length() > 0) {
                //noinspection IOStreamConstructor
                DataInputStream dis2 = new DataInputStream(new FileInputStream(historyFile));
                try {
                    //noinspection IOStreamConstructor
                    DataOutputStream dos2 = new DataOutputStream(new FileOutputStream(newHistoryFile));
                    try {
                        dos2.writeByte(85);
                        dos2.writeByte(78);
                        dos2.writeByte(73);
                        while (dis2.available() > 0) {
                            int direction = dis2.readByte();
                            boolean xtraz = dis2.readBoolean();
                            long time = dis2.readLong();
                            int msgLen = dis2.readInt();
                            byte[] message = new byte[msgLen];
                            //noinspection ResultOfMethodCallIgnored
                            dis2.read(message, 0, msgLen);
                            //noinspection InjectedReferences
                            String msg = new String(message, "windows1251");
                            dos2.writeByte(direction);
                            dos2.writeBoolean(xtraz);
                            dos2.writeInt(0);
                            dos2.writeLong(time);
                            dos2.writeInt(msgLen * 2);
                            utilities.writeStringUnicodeBE(msg, dos2);
                        }
                        try {
                            dis2.close();
                        } catch (IOException e) {
                            //noinspection CallToPrintStackTrace
                            e.printStackTrace();
                        }
                        if (historyFile.delete()) {
                            //noinspection ResultOfMethodCallIgnored
                            newHistoryFile.renameTo(new File(resources.dataPath + this.profile.ID + "/history/" + this.ID + ".hst"));
                        }
                        dos = dos2;
                        dis = dis2;
                    } catch (Exception e2) {
                        dos = dos2;
                        dis = dis2;
                        //noinspection DataFlowIssue
                        success = false;
                        //noinspection CallToPrintStackTrace
                        e2.printStackTrace();
                        dis.close();
                        dos.close();
                        //noinspection ConstantValue
                        return success;
                    }
                } catch (Exception e3) {
                    dis = dis2;
                }
            }
            success = true;
        } catch (Exception ignored) {

        }
        try {
            //noinspection DataFlowIssue
            dis.close();
            //noinspection DataFlowIssue
            dos.close();
        } catch (Exception ignored) {
        }
        return success;
    }
}