package ru.ivansuper.jasmin.jabber.XML_ENGINE;

import android.util.Log;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.jabber.xml_utils;

public class Node {
    public String NAME;
    public String VALUE;
    public final Vector<Node> childs;
    public Vector<Parameter> params;
    public Node parent;

    public Node() {
        this.params = new Vector<>();
        this.childs = new Vector<>();
        this.NAME = "";
        this.VALUE = "";
    }

    public Node(String str) {
        this.params = new Vector<>();
        this.childs = new Vector<>();
        setName(str);
        this.VALUE = "";
    }

    public Node(String str, String str2) {
        this.params = new Vector<>();
        this.childs = new Vector<>();
        setName(str);
        setValue(str2);
    }

    public Node(String str, String str2, String str3) {
        this.params = new Vector<>();
        this.childs = new Vector<>();
        setName(str);
        setValue(str2);
        putParameter("xmlns", str3);
    }

    public static final Node getInstance(String str) {
        return new Node(str);
    }

    public static final Node getInstance(String str, String str2) {
        return new Node(str, str2);
    }

    public static final Node getInstance(String str, String str2, String str3) {
        return new Node(str, str2, str3);
    }

    public void TraceNodes(String str) {
        String str2 = "";
        for (int i = 0; i < this.params.size(); i++) {
            Parameter parameter = this.params.get(i);
            str2 = String.valueOf(str2) + parameter.NAME + "='" + parameter.VALUE + "'; ";
        }
        Log.e("Node", String.valueOf(str) + this.NAME + " [" + str2 + "] Value: " + this.VALUE);
        for (int i2 = 0; i2 < this.childs.size(); i2++) {
            this.childs.get(i2).TraceNodes(String.valueOf(str) + str);
        }
    }

    public final String compile() {
        return getRoot().compileLocal();
    }

    public final String compileLocal() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<");
        stringBuffer.append(this.NAME);
        StringBuffer stringBuffer2 = new StringBuffer();
        for (Parameter parameter : this.params) {
            stringBuffer2.append(parameter.NAME);
            stringBuffer2.append("='");
            stringBuffer2.append(parameter.VALUE);
            stringBuffer2.append("' ");
        }
        String strTrim = stringBuffer2.toString().trim();
        if (strTrim.length() > 0) {
            stringBuffer.append(" ");
            stringBuffer.append(strTrim);
        }
        if (this.childs.size() <= 0) {
            if (this.VALUE.trim().length() > 0) {
                stringBuffer.append(">");
                stringBuffer.append(this.VALUE);
                stringBuffer.append("</");
                stringBuffer.append(this.NAME);
                stringBuffer.append(">");
            } else {
                stringBuffer.append("/>");
            }
            return stringBuffer.toString();
        }
        stringBuffer.append(">");
        Iterator<Node> it = this.childs.iterator();
        while (it.hasNext()) {
            stringBuffer.append(it.next().compileLocal());
        }
        stringBuffer.append("</");
        stringBuffer.append(this.NAME);
        stringBuffer.append(">");
        return stringBuffer.toString();
    }

    public final Node findFirstLocalNodeByName(String str) {
        for (Node node : this.childs) {
            if (node.getName().equalsIgnoreCase(str.trim())) {
                return node;
            }
        }
        return null;
    }

    public final Node findFirstLocalNodeByNameAndNamespace(String str, String str2) {
        for (Node node : this.childs) {
            String namespace = node.getNamespace();
            if (namespace != null && node.getName().equalsIgnoreCase(str.trim()) && namespace.equalsIgnoreCase(str2)) {
                return node;
            }
        }
        return null;
    }

    public final Node findFirstLocalNodeByNameAndParameter(String str, String str2, String str3) {
        for (Node node : this.childs) {
            String parameter = node.getParameter(str2);
            if (parameter != null && node.getName().equalsIgnoreCase(str.trim()) && parameter.equalsIgnoreCase(str3)) {
                return node;
            }
        }
        return null;
    }

    public final Node findFirstLocalNodeByNamespace(String str) {
        for (Node node : this.childs) {
            if (node.getNamespace() != null) {
                return node;
            }
        }
        return null;
    }

    public final Node findFirstNodeByName(String str) {
        if (getName().equalsIgnoreCase(str)) {
            return this;
        }
        Node nodeFindFirstNodeByName = null;
        for (int i = 0; i < this.childs.size(); i++) {
            nodeFindFirstNodeByName = this.childs.get(i).findFirstNodeByName(str);
            if (nodeFindFirstNodeByName != null) {
                return nodeFindFirstNodeByName;
            }
        }
        return nodeFindFirstNodeByName;
    }

    public final Node findFirstNodeByNameAndNamespace(String str, String str2) {
        String namespace = getNamespace();
        if (namespace != null && getName().equalsIgnoreCase(str) && namespace.equalsIgnoreCase(str2)) {
            return this;
        }
        Node node = null;
        for (Node node2 : this.childs) {
            Node nodeFindFirstNodeByNamespace = node2.findFirstNodeByNamespace(str2);
            if (node2 != null) {
                return nodeFindFirstNodeByNamespace;
            }
            node = nodeFindFirstNodeByNamespace;
        }
        return node;
    }

    public final Node findFirstNodeByNamespace(String str) {
        if (getNamespace() != null) {
            return this;
        }
        Node node = null;
        for (Node node2 : this.childs) {
            Node nodeFindFirstNodeByNamespace = node2.findFirstNodeByNamespace(str);
            if (node2 != null) {
                return nodeFindFirstNodeByNamespace;
            }
            node = nodeFindFirstNodeByNamespace;
        }
        return node;
    }

    public final Vector<Node> findLocalNodesByName(String str) {
        Vector<Node> vector = new Vector<>();
        for (Node node : this.childs) {
            if (node.getName().equalsIgnoreCase(str.trim())) {
                vector.add(node);
            }
        }
        return vector;
    }

    public final Vector<Node> findLocalNodesByNameAndNamespace(String str, String str2) {
        Vector<Node> vector = new Vector<>();
        for (Node node : this.childs) {
            String namespace = node.getNamespace();
            if (namespace != null && namespace.equalsIgnoreCase(str2) && node.getName().equalsIgnoreCase(str)) {
                vector.add(node);
            }
        }
        return vector;
    }

    public final Vector<Node> findLocalNodesByNamespace(String str) {
        Vector<Node> vector = new Vector<>();
        for (Node node : this.childs) {
            String namespace = node.getNamespace();
            if (namespace != null && namespace.equalsIgnoreCase(str)) {
                vector.add(node);
            }
        }
        return vector;
    }

    public final Vector<Node> findNodesByNamespace(String str) {
        Vector<Node> vector = new Vector<>();
        Iterator<Node> it = this.childs.iterator();
        while (it.hasNext()) {
            vector.addAll(it.next().findNodesByNamespace(str));
        }
        return vector;
    }

    public final String getName() {
        return xml_utils.decodeString(this.NAME);
    }

    public final String getNamespace() {
        if (this.params.size() != 0) {
            for (Parameter parameter : this.params) {
                if (parameter.NAME.equalsIgnoreCase("xmlns")) {
                    return xml_utils.decodeString(parameter.VALUE);
                }
            }
        }
        return null;
    }

    public final Node getNodeContainValue(String str) {
        for (Node node : this.childs) {
            if (node.getValue().contains(str)) {
                return node;
            }
        }
        return null;
    }

    public final Node getNodeContainValuePrefix(String str) {
        for (Node node : this.childs) {
            if (node.getValue().startsWith(str)) {
                return node;
            }
        }
        return null;
    }

    public final String getParameter(String str) {
        String strEncodeString = xml_utils.encodeString(str);
        for (Parameter parameter : this.params) {
            if (parameter.NAME.equalsIgnoreCase(strEncodeString)) {
                if (parameter == null) {
                    return null;
                }
                return xml_utils.decodeString(parameter.VALUE);
            }
        }
        return null;
    }

    public final String getParameterSafe(String str) {
        Parameter parameter;
        String strEncodeString = xml_utils.encodeString(str);
        for (Parameter parameter2 : this.params) {
            if (parameter2.NAME.equalsIgnoreCase(strEncodeString)) {
                parameter = parameter2;
                if (parameter == null) {
                    return "";
                }
                return xml_utils.decodeString(parameter.VALUE);
            }
        }
        parameter = null;
        if (parameter == null) {
            return "";
        }
        return xml_utils.decodeString(parameter.VALUE);
    }

    public final String getParameterWODecode(String str) {
        for (Parameter parameter : this.params) {
            if (parameter.NAME.equalsIgnoreCase(str)) {
                if (parameter == null) {
                    return null;
                }
                return xml_utils.decodeString(parameter.VALUE);
            }
        }
        return null;
    }

    public final Node getRoot() {
        return this.parent == null ? this : this.parent.getRoot();
    }

    public final String getValue() {
        return xml_utils.decodeString(this.VALUE);
    }

    public final boolean hasChilds() {
        return this.childs.size() > 0;
    }

    public final Node putChild(Node... nodeArr) {
        for (Node node : nodeArr) {
            this.childs.add(node);
        }
        return this;
    }

    public final Node putParameter(String str, String str2) {
        Parameter parameter;
        String strEncodeString = xml_utils.encodeString(str);
        String strEncodeString2 = xml_utils.encodeString(str2);
        Iterator<Parameter> it = this.params.iterator();
        while (true) {
            if (!it.hasNext()) {
                parameter = null;
                break;
            }
            Parameter next = it.next();
            if (next.NAME.equalsIgnoreCase(strEncodeString)) {
                parameter = next;
                break;
            }
        }
        if (parameter == null) {
            Parameter parameter2 = new Parameter();
            parameter2.NAME = strEncodeString;
            parameter2.VALUE = strEncodeString2;
            this.params.add(parameter2);
        } else {
            parameter.VALUE = strEncodeString2;
        }
        return this;
    }

    public final void removeChild(Node node) {
        this.childs.remove(node);
    }

    public void reset() {
        this.NAME = "";
        this.VALUE = "";
        this.params.clear();
        this.childs.clear();
    }

    public final void setName(String str) {
        this.NAME = xml_utils.encodeString(str);
    }

    public final void setValue(String str) {
        this.VALUE = xml_utils.encodeString(str);
    }
}
