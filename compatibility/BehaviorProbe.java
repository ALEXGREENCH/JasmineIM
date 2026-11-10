import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import ru.ivansuper.jasmin.jabber.*;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.*;
import ru.ivansuper.jasmin.jabber.jzlib.*;

/** The same executable runs in separate JVMs against the original and recovered code. */
public class BehaviorProbe {
    interface Check { Object run() throws Exception; }
    static int count;
    static void check(String name, Check check) {
        String value;
        try { value = render(check.run()); }
        catch (Throwable e) {
            while (e instanceof InvocationTargetException) e = e.getCause();
            if (e instanceof LinkageError || e instanceof AssertionError) throw new AssertionError(name, e);
            value = "THROWS:" + e.getClass().getName();
        }
        System.out.println(name + "=" + value);
        count++;
    }
    static String render(Object o) {
        if (o == null) return "null";
        if (o instanceof byte[]) return Base64.getEncoder().encodeToString((byte[]) o);
        if (o instanceof Map) return new TreeMap<>((Map) o).toString();
        return String.valueOf(o).replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }
    static void xml() {
        List<String> inputs = new ArrayList<>(Arrays.asList(null, "", " ", "&<>'\"", "&amp;lt;", "$1\\x", "Привет 😀", "a@b.c", "x-y_z", "<x/>", "<x a='1'>Text</x>", "prefix<x>Text</x>", "<x a=\"1\"/>", "<x a='unterminated>", "<"));
        Random random = new Random(73921);
        String alphabet = "abc <>/'\"=&;$\\\nЖ";
        for (int n = 0; n < 300; n++) {
            StringBuilder s = new StringBuilder();
            for (int k = random.nextInt(60); k > 0; k--) s.append(alphabet.charAt(random.nextInt(alphabet.length())));
            inputs.add(s.toString());
        }
        int n = 0;
        for (final String s : inputs) {
            final String key = "xml/" + n++;
            check(key + "/encode", () -> xml_utils.encodeString(s));
            check(key + "/decode", () -> xml_utils.decodeString(s));
            check(key + "/header", () -> xml_utils.resolveHeader(s));
            check(key + "/name", () -> xml_utils.resolveHeaderName(s));
            check(key + "/params", () -> xml_utils.parseParams(s));
            check(key + "/tag", () -> xml_utils.getTagContent(s, "x"));
            check(key + "/jid", () -> xml_utils.verifyStringAsJID(s));
            check(key + "/bytes", () -> xml_utils.getRawMD5Hash(s));
            check(key + "/ascii", () -> xml_utils.convertAsciiToUtf8(s));
            check(key + "/parser", () -> { Parser p = new Parser(s); return p.getContent() + "|" + p.getHeaderValue("a") + "|" + p.getHeaderValue("missing"); });
            check(key + "/packet", () -> { XMLPacket p = new XMLPacket(s, "h"); p.setContent("c"); return p.getHeaderName() + "|" + p.getHeader() + "|" + p.getXML() + "|" + p.getContent() + "|" + p.getType(); });
        }
        for (int c = 0; c <= 65535; c++) {
            final char ch = (char)c;
            check("jid/" + c, () -> xmpp_utils.isJIDSymbolAllowed(ch) + "/" + xmpp_utils.isJIDSymbolAllowedWODog(ch));
        }
    }
    static String tree(Node n) {
        if (n == null) return "null";
        StringBuilder s = new StringBuilder("[" + n.NAME + "|" + n.VALUE + "|");
        for (Parameter p : n.params) s.append(p.NAME).append('=').append(p.VALUE).append(';');
        s.append('|');
        for (Node child : n.childs) s.append(tree(child));
        return s.append(']').toString();
    }
    static void nodes() {
        String[] texts = {"", " ", "hello", "Привет 😀", "&<>'\"", "&amp;lt;", "$1\\"};
        int index = 0;
        for (String t : texts) {
            final Node root = new Node(" message ", t, "jabber:client");
            root.putParameter("to", "user@example.org/a&b");
            root.putParameter("to", "other@example.org");
            final Node body = new Node("body", t);
            final Node query = new Node("query", "", "urn:test");
            query.putChild(new Node("item", "one"), new Node("item", "two"));
            root.putChild(body, query);
            String key = "node/" + index++;
            check(key + "/tree", () -> tree(root));
            check(key + "/compile", () -> root.compile());
            check(key + "/child-root", () -> body.compile());
            check(key + "/lookup", () -> tree(root.findFirstNodeByName("item")));
            check(key + "/local", () -> tree(root.findFirstLocalNodeByName(" BODY ")));
            check(key + "/namespace", () -> tree(root.findFirstNodeByNamespace("urn:test")));
            check(key + "/params", () -> root.getParameter("to") + "|" + root.getParameterWODecode("to") + "|" + root.getParameterSafe("missing"));
            check(key + "/value", () -> body.getValue());
            check(key + "/parse-compile", () -> tree(Decompiler.getInstance().Decompile(new StringBuffer(root.compile()))));
            check(key + "/remove", () -> { root.removeChild(body); return root.compile(); });
            check(key + "/reset", () -> { root.reset(); return tree(root); });
        }
        String[] xml = {"", " ", "<x/>", "<x></x>", "<x>text</x>", "<x a='1' b=\"2\"/>", "<x><y/><z>Ж&amp;&lt;</z></x>", "<message from='a@b'><body>Hello</body></message>", "<x>one<y/>two</x>", "<x/ ><y/>", "<x>", "<x a='", "<x></y>", "<x/><y/>", "<?xml version='1.0'?><x/>", "<!-- comment --><x/>", "<x><![CDATA[a<b]]></x>"};
        for (int i = 0; i < xml.length; i++) {
            final String input = xml[i];
            check("parse/" + i, () -> { StringBuffer b = new StringBuffer(input); Node n = Decompiler.getInstance().Decompile(b); return tree(n) + "|remaining=" + b; });
        }
        Random r = new Random(44);
        for (int i = 0; i < 200; i++) {
            StringBuilder b = new StringBuilder("<iq id='" + i + "'><query xmlns='urn:test'>");
            for (int j = r.nextInt(12); j > 0; j--) b.append("<item id='").append(j).append("'>value&amp;").append(j).append("</item>");
            final String text = b.append("</query></iq>").toString();
            check("parse/generated/" + i, () -> tree(Decompiler.getInstance().Decompile(new StringBuffer(text))));
        }
    }
    static byte[] compress(byte[] data, int level, boolean raw, int chunk) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZOutputStream z = new ZOutputStream(out, level, raw);
        for (int i = 0; i < data.length; i += chunk) z.write(data, i, Math.min(chunk, data.length - i));
        z.finish(); z.end();
        return out.toByteArray();
    }
    static byte[] inflate(byte[] data, boolean raw, int chunk) throws Exception {
        ZInputStream z = new ZInputStream(new ByteArrayInputStream(data), raw);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[chunk];
        for (int tries = 0; tries < 100000; tries++) {
            int n = z.read(buf, 0, buf.length);
            if (n < 0) { z.close(); return out.toByteArray(); }
            out.write(buf, 0, n);
        }
        throw new AssertionError("inflate did not terminate");
    }
    static void compression() throws Exception {
        Random random = new Random(9982);
        for (int size : new int[]{0, 1, 31, 512, 4096, 40000}) {
            for (int pattern = 0; pattern < 2; pattern++) {
                final byte[] data = new byte[size];
                if (pattern == 0) random.nextBytes(data); else Arrays.fill(data, (byte)'a');
                for (int level : new int[]{0, 1, 6, 9}) for (boolean raw : new boolean[]{false, true}) {
                    final String key = "zlib/" + size + "/" + pattern + "/" + level + "/" + raw;
                    check(key, () -> compress(data, level, raw, 113));
                    check(key + "/roundtrip", () -> {
                        byte[] result = inflate(compress(data, level, raw, 113), raw, 71);
                        return "equal=" + Arrays.equals(data, result) + ":" + render(result);
                    });
                    check(key + "/jdk", () -> {
                        java.util.zip.Deflater d = new java.util.zip.Deflater(level, raw);
                        d.setInput(data); d.finish(); ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buf = new byte[509];
                        while (!d.finished()) { int n = d.deflate(buf); out.write(buf, 0, n); }
                        d.end(); return inflate(out.toByteArray(), raw, 83);
                    });
                }
            }
        }
        for (int level : new int[]{-2, -1, 0, 10}) check("zlib/init/" + level, () -> { ZStream z = new ZStream(); int result = z.deflateInit(level); z.deflateEnd(); return result; });
        for (int bits : new int[]{-16, 0, 8, 9, 15, 16}) check("zlib/bits/" + bits, () -> { ZStream z = new ZStream(); int result = z.inflateInit(bits); z.inflateEnd(); return result; });
        for (byte[] bad : new byte[][]{{}, {0}, {1,2,3,4}, {120,-100,0,0,0,0}}) check("zlib/bad/" + render(bad), () -> inflate(bad, false, 23));
    }
    static void packetsAndServers() {
        PacketHandler.task_id = 0;
        check("packet/generated", () -> { PacketHandler p = new PacketHandler() { public void execute() {} }; return p.getID() + "|" + p.runOnUi; });
        check("packet/background", () -> { PacketHandler p = new PacketHandler(false) { public void execute() {} }; return p.getID() + "|" + p.runOnUi; });
        check("packet/named", () -> { PacketHandler p = new PacketHandler("abc") { public void execute() {} }; return p.getID() + "|" + p.runOnUi + "|" + PacketHandler.task_id; });
        ServerList list = new ServerList();
        check("servers/empty", () -> list.getProxy());
        list.put("ordinary", ServerList.Type.OTHER);
        check("servers/other", () -> list.getProxy());
        list.put("proxy1", ServerList.Type.PROXY, "127.0.0.1", 1080);
        list.put("proxy2", ServerList.Type.PROXY, "example.org", 9999);
        check("servers/proxy", () -> { ServerList.Server s = list.getProxy(); return s.jid + "|" + s.proxy_host + "|" + s.proxy_port; });
        check("servers/clear", () -> { list.clear(); return list.getProxy(); });
    }
    static Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name, types); m.setAccessible(true); return m.invoke(target, args);
    }
    static void dns() throws Exception {
        Object resolver = Class.forName("ru.ivansuper.jasmin.jabber.dns.DnsSrvResolver").getConstructor().newInstance();
        for (String name : new String[]{"example.org", "localhost", "", ".", "a..b", "пример.рф"}) for (int mode : new int[]{0, 1, 2, 3}) {
            check("dns/encode/" + name + "/" + mode, () -> invoke(resolver, "encode", new Class<?>[]{String.class, int.class}, name, mode));
        }
        for (int length = 0; length < 13; length++) {
            final byte[] bytes = new byte[length];
            check("dns/truncated/" + length, () -> invoke(resolver, "decode", new Class<?>[]{byte[].class}, bytes));
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(1); out.writeShort(0x8180); out.writeShort(1); out.writeShort(1); out.writeShort(0); out.writeShort(0);
        out.writeByte(7); out.writeBytes("example"); out.writeByte(3); out.writeBytes("org"); out.writeByte(0); out.writeShort(33); out.writeShort(1);
        out.writeShort(0xc00c); out.writeShort(33); out.writeShort(1); out.writeInt(60); out.writeShort(20);
        out.writeShort(0); out.writeShort(5); out.writeShort(5222); out.writeByte(4); out.writeBytes("xmpp"); out.writeByte(7); out.writeBytes("example"); out.writeByte(0);
        check("dns/srv-answer", () -> {
            Vector<?> result = (Vector<?>) invoke(resolver, "decode", new Class<?>[]{byte[].class}, bytes.toByteArray());
            StringBuilder text = new StringBuilder();
            for (Object entry : result) for (String field : new String[]{"host", "port", "ttl"}) {
                Field f = entry.getClass().getDeclaredField(field); f.setAccessible(true); text.append(field).append('=').append(f.get(entry)).append('|');
            }
            return text;
        });
    }
    static String socks(int addressType, int auth, int reply) throws Exception {
        ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        server.setSoTimeout(5000);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> transcript = executor.submit(() -> {
            try (Socket peer = server.accept()) {
                peer.setSoTimeout(5000);
                DataInputStream input = new DataInputStream(peer.getInputStream()); OutputStream out = peer.getOutputStream();
                byte[] hello = new byte[3]; input.readFully(hello);
                out.write(new byte[]{5, (byte)auth}); out.flush();
                if (auth != 0) return render(hello);
                byte[] header = new byte[5]; input.readFully(header);
                byte[] destination = new byte[(header[4] & 255) + 2]; input.readFully(destination);
                // Split the response to exercise fill() across multiple reads.
                out.write(5); out.flush(); out.write(new byte[]{(byte)reply, 0, (byte)addressType});
                if (reply == 0) {
                    if (addressType == 1) out.write(new byte[6]);
                    if (addressType == 4) out.write(new byte[18]);
                    if (addressType == 3) out.write(new byte[]{3, 'a', 'b', 'c', 0, 0});
                    out.write(42);
                }
                out.flush(); return render(hello) + "|" + render(header) + "|" + render(destination);
            }
        });
        Socket socket = null;
        try {
            socket = ru.ivansuper.jasmin.jabber.bytestreams.Socks5SocketFactory.getSocket("127.0.0.1", server.getLocalPort(), "0123456789abcdef", "unused");
            String result = socket == null ? "null" : "open=" + !socket.isClosed() + ",next=" + socket.getInputStream().read();
            return transcript.get(6, TimeUnit.SECONDS) + "|" + result;
        } finally {
            if (socket != null) socket.close(); server.close(); executor.shutdownNow();
        }
    }
    static void networking() throws Exception {
        packetsAndServers(); dns();
        for (int type : new int[]{1, 3, 4, 7}) check("socks/type/" + type, () -> socks(type, 0, 0));
        check("socks/auth-rejected", () -> socks(1, 255, 0));
        check("socks/connect-rejected", () -> socks(1, 0, 5));
    }
    public static void main(String[] args) throws Exception {
        xml(); nodes(); compression(); networking();
        System.out.println("CHECKS=" + count);
    }
}
