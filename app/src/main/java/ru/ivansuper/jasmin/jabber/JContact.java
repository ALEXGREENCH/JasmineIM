package ru.ivansuper.jasmin.jabber;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import ru.ivansuper.jasmin.ContactlistItem;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.HistoryTools.ExportImportActivity;
import ru.ivansuper.jasmin.HistoryTools.HistoryTools;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.vcard.Avatar;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class JContact extends ContactlistItem {
    public static final int STATUS_AWAY = 2;
    public static final int STATUS_CHAT = 0;
    public static final int STATUS_DND = 3;
    public static final int STATUS_NA = 4;
    public static final int STATUS_OFFLINE = -1;
    public static final int STATUS_ONLINE = 1;
    public static final int SUBSCRIPTION_BOTH = 3;
    public static final int SUBSCRIPTION_FROM = 1;
    public static final int SUBSCRIPTION_NONE = 0;
    public static final int SUBSCRIPTION_TO = 2;
    public Drawable avatar;
    public boolean conf_pm;
    public JProfile profile;
    private int unread_count;
    public int status = -1;
    public Drawable ext_status = null;
    private final Vector<Resource> resources_ = new Vector<>();
    public String group = "";
    public boolean isChating = false;
    public boolean typing = false;
    public boolean hasUnreadMessages = false;
    public final ArrayList<HistoryItem> history = new ArrayList<>();
    public String typedText = "";
    public boolean historyPreLoaded = false;
    public int subscription = 0;
    public long mLastOnlineTime = 0;
    public boolean mNeedLastOnlineTime = true;

    public final class Resource implements Comparable<Resource> {
        public String name;
        public int priority;
        public String status_desc;
        public int status = -1;
        public int client = -1;

        public Resource() {
        }

        @Override
        public int compareTo(Resource resource) {
            if (this.priority <= resource.priority) {
                if (this.priority < resource.priority || this.status > resource.status) {
                    return 1;
                }
                if (this.status >= resource.status) {
                    try {
                        String str = this.name;
                        String str2 = resource.name;
                        int length = str.length();
                        if (str2.length() < length) {
                            length = str2.length();
                        }
                        int i = 0;
                        while (true) {
                            int iIndexOf = utilities.chars.indexOf(str.charAt(i)) + 256;
                            if (iIndexOf == 255) {
                                iIndexOf = str.charAt(i);
                            }
                            int iIndexOf2 = utilities.chars.indexOf(str2.charAt(i)) + 256;
                            if (iIndexOf2 == 255) {
                                iIndexOf2 = str2.charAt(i);
                            }
                            if (iIndexOf == iIndexOf2) {
                                i++;
                                if (i >= length) {
                                    break;
                                }
                            } else {
                                return iIndexOf < iIndexOf2 ? -1 : 1;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            }
            return -1;
        }
    }

    public JContact(JProfile jProfile, String str) {
        File file;
        this.itemType = 4;
        this.profile = jProfile;
        this.ID = str;
        initHistoryFiles();
        if (new File(String.valueOf(resources.dataPath) + jProfile.ID + "@" + jProfile.host + "/avatars/" + str).exists()) {
            readLocalAvatar();
        } else {
            this.avatar = null;
        }
        if (checkHistoryFormat()) {
            if (!ExportImportActivity.CONVERTING_STARTED) {
                ExportImportActivity.CONVERTING_STARTED = true;
                resources.service.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
            Log.e("Converter", String.valueOf(str) + " -- conversion in process ...");
            try {
                if (PERFORM_CONVERT_TO_UNI16()) {
                } else {
                    throw new IllegalArgumentException(String.valueOf(str) + " -- conversion error");
                }
            } catch (Exception e) {
                new File(String.valueOf(resources.dataPath) + jProfile.ID + "@" + jProfile.host + "/history/" + str + ".hst").delete();
                file = new File(String.valueOf(resources.dataPath) + jProfile.ID + "@" + jProfile.host + "/history/" + str + ".cache");
                file.delete();
                initHistoryFiles();
            } catch (OutOfMemoryError e2) {
                new File(String.valueOf(resources.dataPath) + jProfile.ID + "@" + jProfile.host + "/history/" + str + ".hst").delete();
                file = new File(String.valueOf(resources.dataPath) + jProfile.ID + "@" + jProfile.host + "/history/" + str + ".cache");
                file.delete();
                initHistoryFiles();
            }
        }
    }

    private final boolean PERFORM_CONVERT_TO_UNI16() {
        DataInputStream input = null;
        DataOutputStream output = null;
        boolean success = false;
        try {
            File source = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst");
            File converted = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst_new");
            if (source.length() > 0) {
                input = new DataInputStream(new FileInputStream(source));
                output = new DataOutputStream(new FileOutputStream(converted));
                output.writeByte(85);
                output.writeByte(78);
                output.writeByte(73);
                while (input.available() > 0) {
                    byte direction = input.readByte();
                    long date = input.readLong();
                    int length = input.readInt();
                    byte[] bytes = new byte[length];
                    input.read(bytes, 0, length);
                    String message = new String(bytes, "windows1251");
                    output.writeByte(direction);
                    output.writeLong(date);
                    output.writeInt(0);
                    output.writeInt(length * 2);
                    utilities.writeStringUnicodeBE(message, output);
                }
                try { input.close(); } catch (IOException e) { e.printStackTrace(); }
                if (source.delete()) {
                    converted.renameTo(new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst"));
                }
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            input.close();
            output.close();
        } catch (Exception e) { }
        return success;
    }

    private final boolean checkHistoryFormat() {
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst")));
            byte b = dataInputStream.readByte();
            byte b2 = dataInputStream.readByte();
            byte b3 = dataInputStream.readByte();
            dataInputStream.close();
            Log.e("HistoryChecker", String.valueOf(Integer.toHexString(b)) + " " + Integer.toHexString(b2) + " " + Integer.toHexString(b3));
            return (b == 85 && b2 == 78 && b3 == 73) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }

    private final void dumpLastHistory() {
        int i;
        int size = this.history.size();
        if (size < 10) {
            i = 0;
        } else {
            i = size - 10;
            size = 10;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            for (int i2 = 0; i2 < size; i2++) {
                HistoryItem historyItem = this.history.get(i + i2);
                if (historyItem.jtransfer == null) {
                    dataOutputStream.writeByte((byte) historyItem.direction);
                    dataOutputStream.writeLong(historyItem.date);
                    dataOutputStream.writeInt(0);
                    dataOutputStream.writeInt(historyItem.message.length() * 2);
                    utilities.writeStringUnicodeBE(historyItem.message, dataOutputStream);
                }
            }
        FileOutputStream fileOutputStream = new FileOutputStream(new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".cache"), false);
        fileOutputStream.write(new byte[]{85, 78, 73});
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.close();
        dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.close();
        } catch (Exception e2) {
        }
    }

    public final void clearPreloadedHistory() {
        this.history.clear();
        this.historyPreLoaded = false;
        System.gc();
    }

    public final void clearResources() {
        synchronized (this) {
            this.resources_.clear();
        }
    }

    public final void deleteResource(String str) {
        synchronized (this) {
            for (int i = 0; i < this.resources_.size(); i++) {
                if (this.resources_.get(i).name.equals(str)) {
                    this.resources_.remove(i);
                    break;
                }
            }
        }
    }

    public final void getAvatar() {
        PacketHandler packetHandler = new PacketHandler(false) {
            @Override
            public void execute() {
                Node node = this.slot;
                if (node == null || node.getParameter("type").equals("error")) {
                    return;
                }
                JContact.this.saveAvatar(Avatar.getAvatar(node.findFirstLocalNodeByNameAndNamespace("vCard", "vcard-temp")));
                JContact.this.readLocalAvatar();
            }
        };
        this.profile.putPacketHandler(packetHandler);
        this.profile.sendVCardRequest(packetHandler.getID(), this.ID);
    }

    public final Drawable getClient() {
        if (this.resources_.size() > 0) {
            return Clients.getIcon(this.resources_.get(0).client);
        }
        return null;
    }

    public final Drawable getLocalAvatar() {
        BufferedInputStream bufferedInputStream;
        File file = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/avatars/" + this.ID);
        this.avatar = resources.ctx.getResources().getDrawable(2130837629);
        if (file.length() != 0) {
            try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                try {
                    Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(bufferedInputStream);
                    if (bitmapDecodeStream == null) {
                        throw new NullPointerException("Result bitmap is null");
                    }
                    this.avatar = new BitmapDrawable(bitmapDecodeStream.copy(Bitmap.Config.ARGB_4444, false));
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e2) {
                }
            } catch (Exception e3) {
                bufferedInputStream = null;
            }
        }
        return this.avatar;
    }

    public final Resource getResource(int i) {
        Resource resource;
        synchronized (this) {
            resource = this.resources_.size() > 0 ? this.resources_.get(i) : null;
        }
        return resource;
    }

    public final Resource getResource(String str) {
        Resource resource;
        synchronized (this) {
            for (int i = 0; i < this.resources_.size(); i++) {
                Resource resource2 = this.resources_.get(i);
                if (resource2.name.equals(str)) {
                    return resource2;
                }
            }
            resource = null;
        }
        return resource;
    }

    public final Vector<Resource> getResources() {
        return this.resources_;
    }

    public final int getStatus() {
        if (this.resources_.size() > 0) {
            return this.resources_.get(0).status;
        }
        return -1;
    }

    public final String getStatusDescription() {
        return this.resources_.size() > 0 ? this.resources_.get(0).status_desc : "";
    }

    public final int getUnreadCount() {
        return this.unread_count;
    }

    public final void initHistoryFiles() {
        File file = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst");
        if (!file.exists()) {
            try {
                file.createNewFile();
                Log.e("History", String.valueOf(this.ID) + " -- Creating file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (file.length() < 3) {
            try {
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(85);
                fileOutputStream.write(78);
                fileOutputStream.write(73);
                fileOutputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        File file2 = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".cache");
        if (file2.exists()) {
            return;
        }
        try {
            file2.createNewFile();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
    }

    public final boolean isOnline() {
        boolean z;
        synchronized (this) {
            z = false;
            for (int i = 0; i < this.resources_.size(); i++) {
                if (this.resources_.get(i).status != -1) {
                    z = true;
                    break;
                }
            }
        }
        return z;
    }

    public final boolean isReqAuth() {
        return (this.subscription == 1 || this.subscription == 3) ? false : true;
    }

    public final void loadHistory(Vector<HistoryItem> vector) {
        DataInputStream dataInputStream = null;
        try {
            File file = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst");
            if (file.length() > 0) {
                DataInputStream dataInputStream2 = new DataInputStream(new FileInputStream(file));
                try {
                    byte b = dataInputStream2.readByte();
                    byte b2 = dataInputStream2.readByte();
                    byte b3 = dataInputStream2.readByte();
                    if (b == 85 && b2 == 78 && b3 == 73) {
                        dataInputStream2.close();
                        loadHistoryUNI16(vector);
                        return;
                    }
                    dataInputStream2.close();
                    DataInputStream dataInputStream3 = new DataInputStream(new FileInputStream(file));
                    while (dataInputStream3.available() > 0) {
                        try {
                            byte b4 = dataInputStream3.readByte();
                            long j = dataInputStream3.readLong();
                            int i = dataInputStream3.readInt();
                            byte[] bArr = new byte[i];
                            dataInputStream3.read(bArr, 0, i);
                            String str = new String(bArr, "windows1251");
                            HistoryItem historyItem = new HistoryItem(j);
                            historyItem.direction = b4;
                            historyItem.confirmed = true;
                            historyItem.message = str;
                            historyItem.jcontact = this;
                            vector.add(historyItem);
                        } catch (Exception e) {
                            e = e;
                            dataInputStream = dataInputStream3;
                            e.printStackTrace();
                            break;
                        }
                    }
                    try {
                        dataInputStream3.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    dataInputStream = dataInputStream3;
                } catch (Exception e3) {
                    e3.printStackTrace();
                    dataInputStream = dataInputStream2;
                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        try {
            dataInputStream.close();
        } catch (Exception e5) {
        }
    }

    public final void loadHistoryUNI16(Vector<HistoryItem> vector) {
        DataInputStream dataInputStream = null;
        try {
            File file = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst");
            if (file.length() > 0) {
                DataInputStream dataInputStream2 = new DataInputStream(new FileInputStream(file));
                try {
                    dataInputStream2.skip(3L);
                    while (dataInputStream2.available() > 0) {
                        byte b = dataInputStream2.readByte();
                        long j = dataInputStream2.readLong();
                        dataInputStream2.readInt();
                        String stringUnicodeBE = utilities.readStringUnicodeBE(dataInputStream2, dataInputStream2.readInt());
                        HistoryItem historyItem = new HistoryItem(j);
                        historyItem.direction = b;
                        historyItem.confirmed = true;
                        historyItem.message = stringUnicodeBE;
                        historyItem.jcontact = this;
                        vector.add(historyItem);
                    }
                    try {
                        dataInputStream2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dataInputStream = dataInputStream2;
                } catch (Exception e2) {
                    dataInputStream = dataInputStream2;
                    e2.printStackTrace();
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            dataInputStream.close();
        } catch (Exception e4) {
        }
    }

    public final void loadLastHistory() {
        DataInputStream dataInputStream;
        if (PreferenceTable.preloadHistory && !this.historyPreLoaded) {
            Vector vector = new Vector();
            try {
                if (this.historyPreLoaded) {
                    dataInputStream = null;
                } else {
                    this.history.clear();
                    File file = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".cache");
                    if (file.length() > 0) {
                        DataInputStream dataInputStream2 = new DataInputStream(new FileInputStream(file));
                        byte b = dataInputStream2.readByte();
                        byte b2 = dataInputStream2.readByte();
                        byte b3 = dataInputStream2.readByte();
                        if (b == 85 && b2 == 78 && b3 == 73) {
                            dataInputStream2.close();
                            loadLastHistoryUNI16();
                            return;
                        }
                        dataInputStream2.close();
                        dataInputStream = new DataInputStream(new FileInputStream(file));
                        while (dataInputStream.available() > 0) {
                            byte b4 = dataInputStream.readByte();
                            long j = dataInputStream.readLong();
                            int i = dataInputStream.readInt();
                            byte[] bArr = new byte[i];
                            dataInputStream.read(bArr, 0, i);
                            String str = new String(bArr, "windows1251");
                            HistoryItem historyItem = new HistoryItem(j);
                            historyItem.direction = b4;
                            historyItem.confirmed = true;
                            historyItem.message = str;
                            historyItem.jcontact = this;
                            vector.add(historyItem);
                        }
                        try {
                            dataInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        dataInputStream = null;
                    }
                }
                try {
                    dataInputStream.close();
                } catch (Exception e2) {
                }
                this.historyPreLoaded = true;
                this.history.addAll(vector);
                vector.clear();
                this.profile.svc.handleChatNeedRefresh(this);
                System.gc();
            } catch (Exception e3) {
                e3.printStackTrace();
                this.historyPreLoaded = false;
            }
        }
    }

    public final void loadLastHistoryUNI16() {
        DataInputStream dataInputStream;
        if (PreferenceTable.preloadHistory && !this.historyPreLoaded) {
            Vector vector = new Vector();
            try {
                if (this.historyPreLoaded) {
                    dataInputStream = null;
                } else {
                    this.history.clear();
                    File file = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".cache");
                    if (file.length() > 0) {
                        dataInputStream = new DataInputStream(new FileInputStream(file));
                        dataInputStream.skip(3L);
                        while (dataInputStream.available() > 0) {
                            byte b = dataInputStream.readByte();
                            long j = dataInputStream.readLong();
                            dataInputStream.readInt();
                            String stringUnicodeBE = utilities.readStringUnicodeBE(dataInputStream, dataInputStream.readInt());
                            HistoryItem historyItem = new HistoryItem(j);
                            historyItem.direction = b;
                            historyItem.confirmed = true;
                            historyItem.message = stringUnicodeBE;
                            historyItem.jcontact = this;
                            vector.add(historyItem);
                        }
                        try {
                            dataInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        dataInputStream = null;
                    }
                }
                try {
                    dataInputStream.close();
                } catch (Exception e2) {
                }
                this.historyPreLoaded = true;
                this.history.addAll(vector);
                vector.clear();
                this.profile.svc.handleChatNeedRefresh(this);
                System.gc();
            } catch (Exception e3) {
                e3.printStackTrace();
                this.historyPreLoaded = false;
            }
        }
    }

    public final void readLocalAvatar() {
        File file = new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/avatars/" + this.ID);
        BufferedInputStream bufferedInputStream = null;
        this.avatar = null;
        if (file.length() == 0) {
            return;
        }
        try {
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new FileInputStream(file));
            try {
                Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(bufferedInputStream2);
                if (bitmapDecodeStream == null) {
                    throw new NullPointerException("Result bitmap is null");
                }
                this.avatar = new BitmapDrawable(bitmapDecodeStream.copy(Bitmap.Config.ARGB_4444, false));
                bufferedInputStream = bufferedInputStream2;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                this.profile.svc.handleContactlistDatasetChanged();
            } catch (Exception e2) {
            }
        } catch (Exception e3) {
        }
    }

    public final void saveAvatar(Bitmap bitmap) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/avatars/" + this.ID)));
        } catch (Exception e) {
        }
    }

    public final void saveAvatar(byte[] bArr) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/avatars/" + this.ID)));
            byte[] bArr2 = new byte[16384];
            while (byteArrayInputStream.available() > 0) {
                bufferedOutputStream.write(bArr2, 0, byteArrayInputStream.read(bArr2, 0, 16384));
            }
            bufferedOutputStream.close();
            byteArrayInputStream.close();
        } catch (Exception e) {
        }
    }

    public final void setHasNoUnreadMessages() {
        this.hasUnreadMessages = false;
        this.unread_count = 0;
    }

    public final void setHasUnreadMessages() {
        this.unread_count++;
        this.hasUnreadMessages = true;
    }

    public final void setResource(String str, int i, int i2, String str2, String str3) {
        Resource resource = getResource(str);
        if (resource == null) {
            Resource resource2 = new Resource();
            resource2.name = str;
            resource2.status = i;
            resource2.status_desc = str3;
            resource2.priority = i2;
            resource2.client = Clients.foundCap(str2);
            this.resources_.add(resource2);
        } else {
            resource.status = i;
            resource.status_desc = str3;
            resource.priority = i2;
            resource.client = Clients.foundCap(str2);
        }
        Collections.sort(this.resources_);
    }

    public final void showInfo() {
        this.profile.doRequestInfoForDisplayRaw(this.ID);
    }

    @Override
    public void update(ContactlistItem contactlistItem) {
        try {
            JContact jContact = (JContact) contactlistItem;
            this.name = jContact.name;
            this.group = jContact.group;
        } catch (Exception e) {
        }
    }

    public final void updateNick() {
        PacketHandler packetHandler = new PacketHandler(false) {
            @Override
            public void execute() {
                Node nodeFindFirstLocalNodeByNameAndNamespace;
                Node node = this.slot;
                if (node == null || node.getParameter("type").equals("error") || (nodeFindFirstLocalNodeByNameAndNamespace = node.findFirstLocalNodeByNameAndNamespace("vCard", "vcard-temp")) == null) {
                    return;
                }
                Node nodeFindFirstLocalNodeByName = nodeFindFirstLocalNodeByNameAndNamespace.findFirstLocalNodeByName("NICKNAME");
                Node nodeFindFirstLocalNodeByName2 = nodeFindFirstLocalNodeByNameAndNamespace.findFirstLocalNodeByName("FN");
                String value = nodeFindFirstLocalNodeByName != null ? nodeFindFirstLocalNodeByName.getValue() : null;
                String value2 = nodeFindFirstLocalNodeByName2 != null ? nodeFindFirstLocalNodeByName2.getValue() : null;
                String str = JContact.this.name;
                JContact jContact = JContact.this;
                if (value == null) {
                    value = value2 == null ? str : value2;
                }
                jContact.name = value;
                if (str.equals(JContact.this.name)) {
                    return;
                }
                JContact.this.profile.svc.handleContactlistDatasetChanged();
                JContact.this.profile.doModifyContact(JContact.this);
            }
        };
        this.profile.putPacketHandler(packetHandler);
        this.profile.sendVCardRequest(packetHandler.getID(), this.ID);
    }

    public final void writeMessageToHistory(HistoryItem historyItem) {
        dumpLastHistory();
        if (PreferenceTable.writeHistory) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                dataOutputStream.writeByte((byte) historyItem.direction);
                dataOutputStream.writeLong(historyItem.date);
                dataOutputStream.writeInt(0);
                dataOutputStream.writeInt(historyItem.message.length() * 2);
                utilities.writeStringUnicodeBE(historyItem.message, dataOutputStream);
                synchronized (HistoryTools.WRITE_READ_LOCKER) {
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(String.valueOf(resources.dataPath) + this.profile.ID + "@" + this.profile.host + "/history/" + this.ID + ".hst"), true);
                        fileOutputStream.write(byteArrayOutputStream.toByteArray());
                        fileOutputStream.close();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                dataOutputStream.close();
            } catch (Exception e2) {
            }
            if (PreferenceTable.realtimeHistoryExport) {
                String profileFullID = IMProfile.getProfileFullID(this.profile);
                if (resources.sd_mounted()) {
                    File file = new File(String.valueOf(resources.JASMINE_SD_PATH) + "NewExportedHistory(Unicode)/" + profileFullID);
                    if (!file.isDirectory()) {
                        file.mkdirs();
                    }
                    File file2 = new File(String.valueOf(resources.JASMINE_SD_PATH) + "NewExportedHistory(Unicode)/" + profileFullID + "/[" + this.ID + "].txt");
                    if (!file2.exists()) {
                        try {
                            file2.createNewFile();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            return;
                        }
                    }
                    if (file2.length() == 0) {
                        try {
                            FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                            fileOutputStream2.write(254);
                            fileOutputStream2.write(255);
                            fileOutputStream2.close();
                        } catch (Exception e4) {
                        }
                    }
                    try {
                        DataOutputStream dataOutputStream2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file2, true)));
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(historyItem.direction == 0 ? "<<: " : ">>: ");
                        stringBuffer.append(historyItem.fullFormattedDate());
                        stringBuffer.append(":\n");
                        if (historyItem.isXtrazMessage) {
                            stringBuffer.append("XTRAZ:\n");
                        }
                        stringBuffer.append(historyItem.message);
                        stringBuffer.append("\n---------------------\n");
                        try {
                            utilities.writeStringUnicodeBE(stringBuffer.toString(), dataOutputStream2);
                            try {
                                dataOutputStream2.close();
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
}
