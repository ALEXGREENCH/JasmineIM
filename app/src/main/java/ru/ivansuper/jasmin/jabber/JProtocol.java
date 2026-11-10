package ru.ivansuper.jasmin.jabber;

import android.util.Log;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import ru.ivansuper.jasmin.Base64Coder;
import ru.ivansuper.jasmin.MD5;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Parameter;
import ru.ivansuper.jasmin.jabber.bytestreams.IBBController;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class JProtocol {
    public static final int MD5_DIGEST_METHOD = 1;
    public static final int SCRAM_SHA1_METHOD = 2;

    public static class SCRAM {
        private String clientFirstMessageBare;

        private static final byte[] getHI(byte[] bArr, byte[] bArr2, int i) {
            Mac macPrepareHMAC = prepareHMAC(bArr);
            macPrepareHMAC.update(bArr2);
            macPrepareHMAC.update((byte) 0);
            macPrepareHMAC.update((byte) 0);
            macPrepareHMAC.update((byte) 0);
            macPrepareHMAC.update((byte) 1);
            byte[] bArrDoFinal = macPrepareHMAC.doFinal();
            byte[] bArr3 = (byte[]) bArrDoFinal.clone();
            while (true) {
                i--;
                if (i <= 0) {
                    return bArr3;
                }
                bArrDoFinal = macPrepareHMAC.doFinal(bArrDoFinal);
                for (int i2 = 0; i2 < bArr3.length; i2++) {
                    bArr3[i2] = (byte) (bArr3[i2] ^ bArrDoFinal[i2]);
                }
            }
        }

        private static final HashMap<String, String> parseParams(String str) {
            String[] strArrSplit = str.split(",");
            HashMap<String, String> map = new HashMap<>();
            for (String str2 : strArrSplit) {
                String strTrim = str2.trim();
                map.put(strTrim.substring(0, 1), strTrim.substring(2));
            }
            return map;
        }

        private static final Mac prepareHMAC(byte[] bArr) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(bArr, "HmacSHA1");
            Mac mac = null;
            try {
                mac = Mac.getInstance("HmacSHA1");
                mac.init(secretKeySpec);
                return mac;
            } catch (Exception e) {
                return mac;
            }
        }

        public final String getAnswerBase64(String str, String str2) {
            byte[] bytes;
            HashMap<String, String> params = parseParams(str);
            int i = Integer.parseInt(params.get("i"));
            String str3 = params.get("s");
            String str4 = params.get("r");
            byte[] bArrDigest = null;
            try {
                bytes = str2.getBytes("UTF-8");
            } catch (Exception e) {
                bytes = null;
            }
            byte[] bArrDoFinal = prepareHMAC(getHI(bytes, Base64Coder.decode(str3), i)).doFinal("Client Key".getBytes());
            try {
                bArrDigest = MessageDigest.getInstance("SHA-1").digest(bArrDoFinal);
            } catch (Exception e2) {
            }
            String str5 = "c=biws,r=" + str4;
            byte[] bArrDoFinal2 = prepareHMAC(bArrDigest).doFinal((String.valueOf(this.clientFirstMessageBare) + "," + str + "," + str5).getBytes());
            byte[] bArr = (byte[]) bArrDoFinal.clone();
            for (int i2 = 0; i2 < bArr.length; i2++) {
                bArr[i2] = (byte) (bArr[i2] ^ bArrDoFinal2[i2]);
            }
            return Base64Coder.encodeString(String.valueOf(str5) + ",p=" + Base64Coder.encodeLines(bArr));
        }

        public final String getFirstMessageBase64(String str) {
            this.clientFirstMessageBare = "n=" + str + ",r=" + Base64Coder.encodeString(String.valueOf(System.currentTimeMillis()));
            return Base64Coder.encodeString("n,," + this.clientFirstMessageBare);
        }
    }

    public static final String createDateTimeString(long j) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(j - ((long) (calendar.get(15) + calendar.get(16))));
        return String.valueOf(String.valueOf("") + new SimpleDateFormat("yyyy-MM-dd").format(date) + "T") + new SimpleDateFormat("HH:mm:ss").format(date) + "Z";
    }

    public static final Node createDiscoInfo(String str, String str2, Vector<Parameter> vector) {
        Node node = new Node("iq");
        node.putParameter("type", "result");
        node.putParameter("to", str);
        node.putParameter("xml:lang", Locale.getCurrentLangCode());
        node.putParameter("id", str2);
        Node node2 = new Node("query", "", "http://jabber.org/protocol/disco#info");
        node2.params = vector;
        node2.putChild(new Node("identity").putParameter("category", "client").putParameter("type", "mobile").putParameter("name", "Jasmine IM"));
        node2.putChild(new Node("feature").putParameter("var", "http://jabber.org/protocol/disco#info"));
        node2.putChild(new Node("feature").putParameter("var", "http://jabber.org/protocol/chatstates"));
        node2.putChild(new Node("feature").putParameter("var", "http://jabber.org/protocol/rosterx"));
        node2.putChild(new Node("feature").putParameter("var", "http://jabber.org/protocol/muc"));
        node2.putChild(new Node("feature").putParameter("var", "http://jabber.org/protocol/si"));
        node2.putChild(new Node("feature").putParameter("var", "http://jabber.org/protocol/si/profile/file-transfer"));
        node2.putChild(new Node("feature").putParameter("var", IBBController.NAMESPACE));
        node2.putChild(new Node("feature").putParameter("var", "jabber:iq:version"));
        node2.putChild(new Node("feature").putParameter("var", "jabber:client"));
        node2.putChild(new Node("feature").putParameter("var", "urn:xmpp:ping"));
        node.putChild(node2);
        return node;
    }

    public static long createLongTime(int i, int i2, int i3, int i4, int i5, int i6) {
        byte b;
        int i7 = ((i - 1970) * 365) + i3 + ((i - 1968) / 4);
        if (i >= 2000) {
            i7--;
        }
        if (i % 4 != 0 || i == 2000) {
            b = 28;
        } else {
            i7--;
            b = 29;
        }
        int i8 = 0;
        while (i8 < i2 - 1) {
            i7 += i8 == 1 ? b : new byte[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}[i8];
            i8++;
        }
        return ((((long) i7) * 24 * 3600) + (((long) i4) * 3600) + (((long) i5) * 60) + ((long) i6)) * 1000;
    }

    public static final String createTimeZonePattern() {
        Calendar calendar = Calendar.getInstance();
        long j = calendar.get(15) + calendar.get(16);
        boolean z = j < 0;
        String strValueOf = String.valueOf(Math.abs((int) ((j / 1000) / 3600)));
        if (strValueOf.length() == 1) {
            strValueOf = "0" + strValueOf;
        }
        if (z) {
            strValueOf = "-" + strValueOf;
        }
        return String.valueOf(strValueOf) + ":00";
    }

    public static String getJIDFromFullID(String str) {
        return str == null ? "null" : str.split("/")[0];
    }

    public static String getNameFromFullID(String str) {
        return str == null ? "null" : str.split("@")[0];
    }

    public static final byte[] getPlainArray(String str, String str2, String str3) throws Exception {
        byte[] bytes = str.getBytes("utf8");
        byte[] bytes2 = str2.getBytes("utf8");
        byte[] bytes3 = str3.getBytes();
        byte[] bArr = new byte[bytes.length + 1 + bytes3.length + 1 + bytes.length + 1 + bytes2.length];
        System.arraycopy(bytes, 0, bArr, 0, bytes.length);
        int length = bytes.length + 0;
        bArr[length] = (byte) "@".charAt(0);
        int i = length + 1;
        System.arraycopy(bytes3, 0, bArr, i, bytes3.length);
        int length2 = i + bytes3.length;
        byte b = (byte) 0;
        bArr[length2] = b;
        int i2 = length2 + 1;
        System.arraycopy(bytes, 0, bArr, i2, bytes.length);
        int length3 = i2 + bytes.length;
        bArr[length3] = b;
        System.arraycopy(bytes2, 0, bArr, length3 + 1, bytes2.length);
        int length4 = bytes2.length;
        return bArr;
    }

    public static String getResourceFromFullID(String str) {
        int iIndexOf;
        return (str != null && (iIndexOf = str.indexOf("/")) >= 0) ? str.substring(iIndexOf + 1, str.length()) : "";
    }

    public static final String getResponse(String str, String str2, String str3, String str4, String str5, String str6) throws Exception {
        return "username=\"" + str + "\",realm=\"" + str3 + "\",nonce=\"" + str5 + "\",cnonce=\"" + str6 + "\",nc=00000001,qop=auth,digest-uri=\"" + str4 + "\",charset=utf-8,response=" + getResponseHash(str, str2, str3, str4, str5, str6);
    }

    public static final String getResponseHash(String str, String str2, String str3, String str4, String str5, String str6) throws Exception {
        byte[] bArrCalculateMD5 = MD5.calculateMD5((String.valueOf(str) + ":" + str3 + ":" + str2).getBytes("utf8"));
        byte[] bytes = (":" + str5 + ":" + str6).getBytes("utf8");
        byte[] bArr = new byte[bArrCalculateMD5.length + bytes.length];
        System.arraycopy(bArrCalculateMD5, 0, bArr, 0, bArrCalculateMD5.length);
        System.arraycopy(bytes, 0, bArr, bArrCalculateMD5.length, bytes.length);
        byte[] bytes2 = ("AUTHENTICATE:" + str4).getBytes("utf8");
        return utilities.convertToHex(MD5.calculateMD5((String.valueOf(utilities.convertToHex(MD5.calculateMD5(bArr))) + ":" + str5 + ":00000001:" + str6 + ":auth:" + utilities.convertToHex(MD5.calculateMD5(bytes2))).getBytes("utf8")));
    }

    public static String getServerFromFullID(String str) {
        if (str == null) {
            return "null";
        }
        int iIndexOf = str.indexOf("@");
        return iIndexOf >= 0 ? str.substring(iIndexOf + 1, str.length()) : str;
    }

    public static byte[] getXGoogleToken(String str, String str2) throws Exception {
        byte[] bArr = new byte[1];
        BasicHttpParams basicHttpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(basicHttpParams, 20000);
        HttpResponse httpResponseExecute = new DefaultHttpClient(basicHttpParams).execute(new HttpGet("https://www.google.com/accounts/ClientLogin?accountType=GOOGLE&Email=" + str + "&Passwd=" + str2 + "&service=mail"));
        int statusCode = httpResponseExecute.getStatusLine().getStatusCode();
        Log.e("Auth:code", "HTTP Code: " + statusCode);
        if (statusCode != 200) {
            throw new Exception("Invalid login");
        }
        try {
            InputStream content = httpResponseExecute.getEntity().getContent();
            StringBuffer stringBuffer = new StringBuffer();
            int i = 0;
            while (i != -1 && (i = content.read()) != -1) {
                stringBuffer.append((char) i);
            }
            content.close();
            String string = stringBuffer.toString();
            int iIndexOf = string.indexOf("Auth=");
            if (iIndexOf == -1) {
                return bArr;
            }
            String strSubstring = string.substring(iIndexOf + 5);
            byte[] bytes = (String.valueOf(str) + "@gmail.com").getBytes();
            byte[] bytes2 = strSubstring.getBytes();
            byte[] bArr2 = new byte[bytes.length + 1 + 1 + bytes2.length];
            byte b = (byte) 0;
            bArr2[0] = b;
            System.arraycopy(bytes, 0, bArr2, 1, bytes.length);
            int length = bytes.length + 1;
            bArr2[length] = b;
            System.arraycopy(bytes2, 0, bArr2, length + 1, bytes2.length);
            return bArr2;
        } catch (Exception e) {
            return bArr;
        }
    }

    public static final boolean itIsServer(String str) {
        return str.indexOf("@") < 0;
    }

    public static final String lowerCaseFullJID(String str) {
        if (str == null) {
            return str;
        }
        String resourceFromFullID = getResourceFromFullID(str);
        return String.valueOf(getJIDFromFullID(str).toLowerCase()) + ((resourceFromFullID == null || resourceFromFullID.length() <= 0) ? "" : "/" + getResourceFromFullID(str));
    }

    public static final JGroup makeWOGroup(JProfile jProfile) {
        JGroup jGroup = new JGroup(jProfile, "  \r  [" + resources.getString("s_jabber_without_group") + "] ");
        jGroup.id = -1;
        return jGroup;
    }

    public static int parseStatus(String str) {
        if (str == null) {
            str = "";
        }
        if (str.equalsIgnoreCase("dnd")) {
            return 3;
        }
        if (str.equalsIgnoreCase("away")) {
            return 2;
        }
        if (str.equalsIgnoreCase("chat")) {
            return 0;
        }
        return str.equalsIgnoreCase("xa") ? 4 : 1;
    }

    public static String parseStatus(int i) {
        if (i == 3) {
            return "dnd";
        }
        if (i == 2) {
            return "away";
        }
        if (i == 0) {
            return "chat";
        }
        return i == 4 ? "xa" : "";
    }

    public static final long parseTimeStamp(String str) {
        int i;
        int i2;
        String str2;
        if (str == null) {
            return 0L;
        }
        String[] strArrSplit = str.split("T");
        if (strArrSplit.length != 2) {
            return 0L;
        }
        if (strArrSplit[0].indexOf("-") > 0) {
            i = Integer.parseInt(strArrSplit[0].substring(0, 4));
            strArrSplit[0] = strArrSplit[0].substring(5);
            i2 = Integer.parseInt(strArrSplit[0].substring(0, 2));
            strArrSplit[0] = strArrSplit[0].substring(3);
            str2 = strArrSplit[0];
        } else {
            i = Integer.parseInt(strArrSplit[0].substring(0, 4));
            strArrSplit[0] = strArrSplit[0].substring(4);
            i2 = Integer.parseInt(strArrSplit[0].substring(0, 2));
            strArrSplit[0] = strArrSplit[0].substring(2);
            str2 = strArrSplit[0];
        }
        int i3 = Integer.parseInt(str2.substring(0, 2));
        int i4 = i;
        String[] strArrSplit2 = strArrSplit[1].split(":");
        if (strArrSplit2.length != 3) {
            return 0L;
        }
        int i5 = Integer.parseInt(strArrSplit2[0]);
        int i6 = Integer.parseInt(strArrSplit2[1]);
        int i7 = Integer.parseInt("0");
        Calendar calendar = Calendar.getInstance();
        calendar.set(i4, i2 - 1, i3, i5, i6, i7);
        long timeInMillis = calendar.getTimeInMillis();
        Calendar calendar2 = Calendar.getInstance();
        return timeInMillis + ((long) (calendar2.get(15) + calendar2.get(16)));
    }

    public static String translateStatus(int i) {
        String str;
        if (i == 3) {
            str = "s_jabber_dnd";
        } else if (i == 2) {
            str = "s_jabber_away";
        } else if (i == 0) {
            str = "s_jabber_chat";
        } else {
            str = i == 4 ? "s_jabber_na" : "s_jabber_online";
        }
        return resources.getString(str);
    }
}
