package ru.ivansuper.jasmin.jabber;

public class XMLPacket {
    private String content;
    private String header;
    private String header_name;
    protected String xml;

    public XMLPacket(String str, String str2) {
        if (str != null) {
            this.header_name = xml_utils.resolveHeaderName(str);
            this.xml = str;
        }
        this.header = str2;
    }

    public String getContent() {
        return this.content;
    }

    public String getHeader() {
        return new String(this.header);
    }

    public String getHeaderName() {
        return new String(this.header_name);
    }

    public int getType() {
        return 0;
    }

    public String getXML() {
        return this.xml;
    }

    public void setContent(String str) {
        this.content = str;
    }

    public void setXML(String str) {
        this.xml = str;
    }
}
