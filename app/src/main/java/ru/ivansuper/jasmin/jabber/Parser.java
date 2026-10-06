package ru.ivansuper.jasmin.jabber;

import java.util.HashMap;

public class Parser {
    private String content;
    private HashMap<String, String> values;

    public Parser(String str) {
        int iIndexOf;
        str.replaceAll("\"", "'");
        if (str.length() >= 3 && str.startsWith("<") && (iIndexOf = str.indexOf(">")) != -1) {
            String strSubstring = str.substring(0, iIndexOf + 1);
            parseValues(xml_utils.resolveHeader(strSubstring));
            if (strSubstring.endsWith("/>")) {
                return;
            }
            int iIndexOf2 = str.indexOf("</" + xml_utils.resolveHeaderName(strSubstring) + ">");
            if (iIndexOf2 != -1) {
                this.content = str.substring(strSubstring.length(), iIndexOf2);
            }
        }
    }

    private void parseValues(String str) {
        if (str == null) {
            return;
        }
        this.values = xml_utils.parseParams(str);
    }

    public String getContent() {
        return this.content;
    }

    public String getHeaderValue(String str) {
        return this.values != null ? this.values.get(str) : "";
    }
}
