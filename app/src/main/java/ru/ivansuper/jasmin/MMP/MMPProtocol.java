package ru.ivansuper.jasmin.MMP;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.animate_tools.GifDecoder;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.resources;

/**
 * MMPProtocol class provides constants and methods for interacting with the MMP (Mail.ru Messaging Protocol).
 * It includes status constants, methods for creating various MMP packets, and utility functions for email parsing and status translation.
 */
public class MMPProtocol {
    public static final int MMP_ANGRY = 12;
    /** @noinspection unused*/
    public static final int MMP_AWAY = 2;
    public static final int MMP_CHAT = 13;
    public static final int MMP_DEPRESS = 11;
    public static final int MMP_DND = 5;
    /** @noinspection unused*/
    public static final int MMP_FLAG_INVISIBLE = Integer.MIN_VALUE;
    public static final int MMP_HOME = 10;
    public static final int MMP_LUNCH = 8;
    public static final int MMP_NA = 7;
    public static final int MMP_OC = 6;
    public static final int MMP_OFFLINE = 0;
    public static final int MMP_ONLINE = 1;
    /** @noinspection unused*/
    public static final int MMP_UNDETERMINATED = 3;
    /** @noinspection unused*/
    public static final int MMP_USER_DEFINED = 4;
    public static final int MMP_WORK = 9;

    public interface AuthListener {
        void onResult(String str);
    }

    private static void notifyAuthListener(final String address, jasminSvc svc, final AuthListener l) {
        svc.runOnUi(new Runnable() {
            @Override
            public void run() {
                l.onResult(address);
            }
        });
    }

    public static void getAddress(final jasminSvc svc, final AuthListener l) {
        Thread t = new Thread() {
            @Override
            public void run() {
                InetSocketAddress addr = new InetSocketAddress("mrim.mail.ru", 2042);
                Socket sock = new Socket();
                try {
                    sock.connect(addr, popup_log_adapter.DEFAULT_DISPLAY_TIME);
                    try {
                        InputStream is = sock.getInputStream();
                        byte[] buffer = new byte[GifDecoder.MaxStackSize];
                        int total = 0;
                        while (true) {
                            try {
                                int readed = is.read(buffer, total, 4096 - total);
                                if (readed <= 0) {
                                    try {
                                        break;
                                    } catch (Exception ignored) {
                                    }
                                } else {
                                    total += readed;
                                }
                            } catch (IOException e2) {
                                //noinspection CallToPrintStackTrace
                                e2.printStackTrace();
                                MMPProtocol.notifyAuthListener(null, svc, l);
                                return;
                            }
                        }
                        is.close();
                        sock.close();
                        byte[] raw = new byte[total];
                        System.arraycopy(buffer, 0, raw, 0, total);
                        String address = new String(raw);
                        MMPProtocol.notifyAuthListener(address.trim(), svc, l);
                    } catch (IOException e3) {
                        //noinspection CallToPrintStackTrace
                        e3.printStackTrace();
                    }
                } catch (IOException e4) {
                    //noinspection CallToPrintStackTrace
                    e4.printStackTrace();
                    MMPProtocol.notifyAuthListener(null, svc, l);
                }
            }
        };
        t.start();
    }

    public static ByteBuffer createCsLogin3(int seq, String ID, String PASS) {
        ByteBuffer data = new ByteBuffer();
        data.writeLPSA(ID);
        data.writeLPSA(PASS);
        data.writeDWordLE(86);
        data.writeLPSA("client=\"Jasmine IM\" version=\"" + resources.VERSION + "\" desc=\"Mail.ru Agent module\"");
        data.writeLPSA("ru");
        data.writeDWordLE(16);
        data.writeDWordLE(1);
        data.writeLPSA("geo-list");
        data.writeLPSA("Jasmine IM for Android");
        return Packet.createPacket(seq, 4216, data);
    }

    public static ByteBuffer createChangeStatus(int seq, int status, String status_uri, String title, String desc, String email) {
        if (status_uri == null) {
            status_uri = "";
        }
        if (title == null) {
            title = "";
        }
        if (desc == null) {
            desc = "";
        }
        ByteBuffer data = new ByteBuffer();
        data.writeDWordLE(status);
        data.writeLPSA(status_uri);
        data.writeLPSULE(title);
        data.writeLPSULE(desc);
        data.writeLPSA(email);
        data.writeDWordLE(18);
        return Packet.createPacket(seq, 4130, data);
    }

    public static ByteBuffer createMessage(int seq, String to, String message) {
        ByteBuffer data = new ByteBuffer();
        data.writeDWordLE(0);
        data.writeLPSA(to);
        data.writeLPSULE(message);
        return Packet.createPacket(seq, 4104, data);
    }

    public static ByteBuffer createMessageConfirm(int seq, String to, int id) {
        ByteBuffer data = new ByteBuffer();
        data.writeLPSA(to);
        data.writeDWordLE(id);
        return Packet.createPacket(seq, 4113, data);
    }

    public static ByteBuffer createSmsMessage(int seq, String phone, String message) {
        ByteBuffer data = new ByteBuffer();
        data.writeDWordLE(0);
        data.writeLPSA(phone);
        data.writeLPSULE(message);
        return Packet.createPacket(seq, 4153, data);
    }

    public static String retreiveDomain(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "";
        }
        String[] parts2 = parts[1].split("\\.");
        return parts2.length != 2 ? "" : parts2[0];
    }

    public static String retreiveLogin(String email) {
        String[] parts = email.split("@");
        return parts.length != 2 ? "" : parts[0];
    }

    public static int translateStatus(String status_uri) {
        if (status_uri == null) {
            return MMP_OFFLINE;
        }
        if (status_uri.equalsIgnoreCase("status_dnd")) {
            return MMP_DND;
        }
        if (status_uri.equalsIgnoreCase("status_46")) {
            return MMP_OC;
        }
        if (status_uri.equalsIgnoreCase("status_10")) {
            return MMP_NA;
        }
        if (status_uri.equalsIgnoreCase("status_6")) {
            return MMP_LUNCH;
        }
        if (status_uri.equalsIgnoreCase("status_22")) {
            return MMP_WORK;
        }
        if (status_uri.equalsIgnoreCase("status_5")) {
            return MMP_HOME;
        }
        if (status_uri.equalsIgnoreCase("status_34")) {
            return MMP_DEPRESS;
        }
        if (status_uri.equalsIgnoreCase("status_38")) {
            return MMP_ANGRY;
        }
        if (status_uri.equalsIgnoreCase("status_chat")) {
            return MMP_CHAT;
        }
        return MMP_ONLINE;
    }

    public static String translateStatus(int status) {
        switch (status) {
            case MMP_DND:
                return "status_dnd";
            case MMP_OC:
                return "status_46";
            case MMP_NA:
                return "status_10";
            case MMP_LUNCH:
                return "status_6";
            case MMP_WORK:
                return "status_22";
            case MMP_HOME:
                return "status_5";
            case MMP_DEPRESS:
                return "status_34";
            case MMP_ANGRY:
                return "status_38";
            case MMP_CHAT:
                return "status_chat";
            default:
                return "";
        }
    }
}