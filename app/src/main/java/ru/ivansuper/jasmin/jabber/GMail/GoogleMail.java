package ru.ivansuper.jasmin.jabber.GMail;

import java.util.Vector;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;

public class GoogleMail {

    public static class Mail {
        public Long date;
        public String preview;
        public String sender_address;
        public String sender_name;
        public String theme;
        public String tid;
        public String url;
    }

    public static Vector<Mail> parseXml(Node node) {
        Vector<Mail> vector = new Vector<>();
        for (Node node2 : node.findFirstLocalNodeByName("mailbox").childs) {
            Mail mail = new Mail();
            mail.tid = node2.getParameter("tid");
            mail.url = node2.getParameter("url");
            mail.date = Long.valueOf(Long.parseLong(node2.getParameter("date")));
            Node nodeFindFirstLocalNodeByName = node2.findFirstLocalNodeByName("senders").findFirstLocalNodeByName("sender");
            mail.sender_address = nodeFindFirstLocalNodeByName.getParameter("address");
            mail.sender_name = nodeFindFirstLocalNodeByName.getParameter("name");
            mail.theme = node2.findFirstLocalNodeByName("subject").getValue();
            mail.preview = node2.findFirstLocalNodeByName("snippet").getValue();
            vector.add(mail);
        }
        return vector;
    }
}
