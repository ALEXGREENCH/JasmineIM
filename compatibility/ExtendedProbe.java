import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import ru.ivansuper.jasmin.*;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.jabber.*;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.bookmarks.*;
import ru.ivansuper.jasmin.jabber.vcard.VCard;
import ru.ivansuper.jasmin.jabber.vcard.VCardDecoder;
import ru.ivansuper.jasmin.jabber.bytestreams.IBBController;
import ru.ivansuper.jasmin.jabber.bytestreams.IOController;
import ru.ivansuper.jasmin.jabber.forms.Form;
import ru.ivansuper.jasmin.jabber.forms.Operation;

/** Application-model tests with SDK types available; no Android UI calls are simulated. */
public class ExtendedProbe extends BehaviorProbe {
    static Object allocate(Class<?> type) throws Exception {
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        Field field = unsafe.getDeclaredField("theUnsafe"); field.setAccessible(true);
        return unsafe.getMethod("allocateInstance", Class.class).invoke(field.get(null), type);
    }
    static void protocol() {
        for (String s : new String[]{null, "", "@", "/", "User@HOST/Phone", "HOST", "a@b/c/d", "A@B", "Ж@пример.рф/Телефон"}) {
            check("protocol/jid/" + s, () -> JProtocol.getJIDFromFullID(s));
            check("protocol/name/" + s, () -> JProtocol.getNameFromFullID(s));
            check("protocol/server/" + s, () -> JProtocol.getServerFromFullID(s));
            check("protocol/resource/" + s, () -> JProtocol.getResourceFromFullID(s));
            check("protocol/lower/" + s, () -> JProtocol.lowerCaseFullJID(s));
            check("protocol/is-server/" + s, () -> JProtocol.itIsServer(s));
            check("protocol/status/" + s, () -> JProtocol.parseStatus(s));
        }
        for (int n = -3; n < 9; n++) { final int status = n; check("protocol/status/" + n, () -> JProtocol.parseStatus(status)); }
        for (int year : new int[]{1969,1970,1999,2000,2004,2026}) for (int month : new int[]{1,2,3,12}) {
            check("protocol/date/" + year + "/" + month, () -> JProtocol.createLongTime(year,month,28,23,59,58));
        }
        for (long time : new long[]{-1,0,1,946684800000L,1791288000000L}) check("protocol/format/" + time, () -> JProtocol.createDateTimeString(time));
        check("protocol/plain", () -> JProtocol.getPlainArray("user", "example.org", "p&ssЖ"));
        check("protocol/digest", () -> JProtocol.getResponse("user", "example.org", "password", "nonce", "cnonce", "xmpp/example.org"));
        check("protocol/scram", () -> {
            JProtocol.SCRAM scram = new JProtocol.SCRAM();
            Field f = scram.getClass().getDeclaredField("clientFirstMessageBare"); f.setAccessible(true); f.set(scram,"n=user,r=nonce");
            return scram.getAnswerBase64("r=nonceSERVER,s=c2FsdA==,i=4096", "pencil");
        });
        for (long size : new long[]{-1,0,1,1023,1024,1048575,1048576,2147483647L}) check("transfer/size/" + size, () -> ru.ivansuper.jasmin.jabber.FileTransfer.FileTransfer.getSizeLabel(size));
    }
    static void vcards() {
        for (int mask = 0; mask < 64; mask++) {
            final int bits = mask;
            Node card = new Node("vCard", "", "vcard-temp");
            card.putChild(new Node("FN", "Иван & Alice"), new Node("NICKNAME", "nick"), new Node("DESC", "line1\nline2"));
            Node tel = new Node("TEL");
            String[] flags = {"WORK","HOME","VOICE","FAX","MSG","PREF"};
            for (int i=0;i<flags.length;i++) if ((mask & (1<<i)) != 0) tel.putChild(new Node(flags[i]));
            tel.putChild(new Node("NUMBER", "+1 234")); card.putChild(tel);
            Node address = new Node("ADR"); address.putChild(new Node((mask & 1)==0?"WORK":"HOME"), new Node("STREET", "Main"), new Node("CTRY", "RU")); card.putChild(address);
            card.putChild(new Node("EMAIL").putChild(new Node("USERID", "a@b")));
            check("vcard/text/" + bits, () -> VCardDecoder.decode(card));
            check("vcard/entries/" + bits, () -> {
                VCard value = VCard.getInstance(); String text = VCardDecoder.decode(card, value); StringBuilder result = new StringBuilder(text);
                for (VCard.Entry.Type type : VCard.Entry.Type.values()) result.append('|').append(type).append('=').append(value.getEntry(type));
                return result;
            });
        }
    }
    static void bookmarks() throws Exception {
        BookmarkList list = (BookmarkList) allocate(BookmarkList.class);
        Field f = BookmarkList.class.getDeclaredField("mList"); f.setAccessible(true); f.set(list,new Vector<BookmarkItem>());
        BookmarkItem item = new BookmarkItem(); item.NAME="Room"; item.JID_OR_URL="room@example.org"; list.mList.add(item);
        for (String s : new String[]{null,"", "ROOM@example.org", "other"}) {
            check("bookmarks/jid/" + s, () -> list.itIsExist(s));
            check("bookmarks/pair/" + s, () -> list.itIsExist("room",s));
        }
        check("bookmarks/item", () -> list.itIsExist(item));
        for (int left=0;left<3;left++) for (int right=0;right<3;right++) {
            BookmarkItem a=new BookmarkItem(),b=new BookmarkItem(); a.type=left;b.type=right;a.NAME="A";b.NAME="B";a.JID_OR_URL="a";b.JID_OR_URL="b";
            check("bookmarks/sort/"+left+"/"+right, () -> a.compareTo(b));
        }
    }
    static String history(Vector<HistoryItem> items) {
        StringBuilder s = new StringBuilder();
        for (HistoryItem h : items) s.append(h.direction).append('|').append(h.date).append('|').append(h.message).append('|').append(h.confirmed).append(';');
        return s.toString();
    }
    static void contacts() throws Exception {
        Path dir = Paths.get(System.getProperty("probe.data")); Files.createDirectories(dir.resolve("user@example.org/history"));
        resources.dataPath = dir.toString() + File.separator;
        JProfile profile = (JProfile) allocate(JProfile.class); profile.ID="user";profile.host="example.org";
        JContact contact = new JContact(profile,"friend");
        check("contact/empty", () -> contact.isOnline()+"|"+contact.getStatus()+"|"+contact.getStatusDescription());
        for (String name : new String[]{"Phone","Laptop","alpha"}) {
            JContact.Resource resource = contact.new Resource(); resource.name=name;resource.priority=1;resource.status=1;
            contact.getResources().add(resource);
        }
        check("contact/find", () -> contact.getResource("Laptop").name);
        check("contact/status", () -> contact.isOnline()+"|"+contact.getStatus());
        // Equal names avoid time-dependent presence behavior; comparison branches are separate probes.
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) {
            JContact.Resource left=contact.getResource(a),right=contact.getResource(b);
            check("contact/order/"+a+"/"+b, () -> left.compareTo(right));
        }
        check("contact/delete", () -> {contact.deleteResource("Laptop"); return contact.getResources().size()+"|"+contact.getResource("Laptop");});
        PreferenceTable.writeHistory = true;
        for(int i=1;i<=12;i++) {
            HistoryItem item=new HistoryItem(1700000000000L+i);item.direction=i%2;item.message="message "+i+" Привет 😀";
            contact.history.add(item);contact.writeMessageToHistory(item);
        }
        Path hst=dir.resolve("user@example.org/history/friend.hst"),cache=dir.resolve("user@example.org/history/friend.cache");
        check("history/bytes", () -> Files.readAllBytes(hst));
        check("history/cache", () -> Files.readAllBytes(cache));
        check("history/read", () -> {Vector<HistoryItem> result=new Vector<>();contact.loadHistoryUNI16(result);return history(result);});
        // Exercise the old CP1251 conversion and error branches with disposable files.
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
        out.writeByte(1);out.writeLong(1700000000000L);byte[] text="Привет".getBytes("windows-1251");out.writeInt(text.length);out.write(text);
        Files.write(hst,bytes.toByteArray());
        check("history/legacy-read", () -> {Vector<HistoryItem> result=new Vector<>();contact.loadHistory(result);return history(result);});
        check("history/convert", () -> invoke(contact,"PERFORM_CONVERT_TO_UNI16",new Class<?>[0]));
        check("history/converted-bytes", () -> Files.readAllBytes(hst));
        Files.write(hst,new byte[]{1,0,0});
        check("history/convert-truncated", () -> invoke(contact,"PERFORM_CONVERT_TO_UNI16",new Class<?>[0]));
    }
    public static class CaptureStream extends XMLStream {
        public List<String> packets;
        public CaptureStream() { super(null, false); }
        public void onConnect() { }
        public void onConnecting() { }
        public void onDisconnect() { }
        public void onError(int code) { }
        public void onLostConnection() { }
        public void onPacket(Node node) { }
        @Override public void write(Node node, JProfile profile) { packets.add(node.compile()); }
    }
    static void transfers() throws Exception {
        JProfile profile=(JProfile)allocate(JProfile.class);profile.ID="user";profile.host="example.org";profile.resource="test";
        CaptureStream stream=(CaptureStream)allocate(CaptureStream.class);stream.packets=new ArrayList<>();profile.stream=stream;
        XMPPInterface.packet_listeners.clear();
        IBBController controller=new IBBController(IBBController.Mode.IN,"peer@example.org",128,"iq","sid",profile);
        StringBuilder events=new StringBuilder();
        controller.setEventListener(new IOController.OnEventListener() {
            public void OnData(byte[] bytes,int length) { events.append("data:").append(render(bytes)).append('/').append(length).append(';'); }
            public void onStateChanged(IOController.State state) { events.append("state:").append(state).append(';'); }
        });
        XMPPInterface.OnXMLListener listener=XMPPInterface.packet_listeners.firstElement();
        String[] packets={
            "<iq id='0'><query xmlns='other'/></iq>",
            "<iq id='1'><open xmlns='http://jabber.org/protocol/ibb' sid='sid' block-size='128'/></iq>",
            "<iq id='2'><data xmlns='http://jabber.org/protocol/ibb' sid='wrong' seq='0'>YWJj</data></iq>",
            "<iq id='3'><data xmlns='http://jabber.org/protocol/ibb' sid='sid' seq='0'>YWJj</data></iq>",
            "<iq id='4'><data xmlns='http://jabber.org/protocol/ibb' sid='sid' seq='1'>0J/RgNC40LLQtdGC</data></iq>",
            "<iq id='5'><data xmlns='http://jabber.org/protocol/ibb' sid='sid' seq='9'>eA==</data></iq>",
            "<iq id='6'><close xmlns='http://jabber.org/protocol/ibb' sid='wrong'/></iq>",
            "<iq id='7'><close xmlns='http://jabber.org/protocol/ibb' sid='sid'/></iq>"};
        for(int i=0;i<packets.length;i++) {
            if(i==3) { Field f=IOController.class.getDeclaredField("state");f.setAccessible(true);f.set(controller,IOController.State.WORKING); }
            final Node node=ru.ivansuper.jasmin.jabber.XML_ENGINE.Decompiler.getInstance().Decompile(new StringBuffer(packets[i]));
            check("ibb/"+i, () -> listener.OnXMLData(profile,node)+"|"+events+"|"+stream.packets+"|listeners="+XMPPInterface.packet_listeners.size());
        }
        XMPPInterface.packet_listeners.clear();
    }
    static void formsAndMail() throws Exception {
        String[] types={null,"unknown","boolean","fixed","hidden","jid-multi","jid-single","list-multi","list-single","text-multi","text-private","text-single"};
        for(String type:types) {
            check("form/type/"+type, () -> Form.Field.detectType(type));
            check("form/mode/"+type, () -> Form.detectType(type));
            Node root=new Node("query","","test").putParameter("from","service").putParameter("id","form-id");
            Node x=new Node("x","","jabber:x:data").putParameter("type","form"); root.putChild(x);
            Node field=new Node("field").putParameter("var","value").putParameter("label","Label");
            if(type!=null)field.putParameter("type",type);
            field.putChild(new Node("value","1"),new Node("value","two"),new Node("required"),new Node("desc","description"));
            field.putChild(new Node("option").putParameter("label","One").putChild(new Node("value","1")));
            x.putChild(field);
            check("form/parse/"+type, () -> {
                Operation op=new Operation();op.prepareForm(root,"uid");Form.Field f=op.form.fields.get(0);
                return f.TYPE+"|"+f.VAR+"|"+f.VALUE1+"|"+f.VALUE2+"|"+Arrays.toString(f.VALUE3)+"|"+Arrays.toString(f.VALUE3_)+"|"+f.required+"|"+f.type_defined;
            });
        }
        Node iq=new Node("iq"),box=new Node("mailbox");iq.putChild(box);
        for(int n=0;n<3;n++) {
            Node mail=new Node("mail-thread-info").putParameter("tid","t"+n).putParameter("date","1700000000000").putParameter("url","https://example.org/"+n);
            mail.putChild(new Node("senders").putChild(new Node("sender").putParameter("address","a@b").putParameter("name","Alice")),new Node("subject","Subject &"),new Node("snippet","Preview"));box.putChild(mail);
        }
        check("gmail/parse", () -> {
            StringBuilder out=new StringBuilder();
            for(ru.ivansuper.jasmin.jabber.GMail.GoogleMail.Mail m:ru.ivansuper.jasmin.jabber.GMail.GoogleMail.parseXml(iq))out.append(m.tid).append('|').append(m.date).append('|').append(m.url).append('|').append(m.sender_name).append('|').append(m.sender_address).append('|').append(m.theme).append('|').append(m.preview).append(';');
            return out;
        });
    }
    public static void main(String[] args) throws Exception {
        protocol(); vcards(); bookmarks(); contacts(); transfers(); formsAndMail();
        System.out.println("CHECKS=" + count);
    }
}
