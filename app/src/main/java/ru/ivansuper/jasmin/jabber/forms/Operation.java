package ru.ivansuper.jasmin.jabber.forms;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import java.util.Vector;
import ru.ivansuper.jasmin.Base64Coder;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.ui.Spinner;
import ru.ivansuper.jasmin.utilities;

public class Operation {
    public String NS;
    public String TAG;
    public String UID;
    public Form form;
    public JProfile profile;
    public Node root;
    private Node source;
    public int to_type;

    private static final String getFirstNodeValueSafe(Vector<Node> vector) {
        String value;
        return (vector.size() <= 0 || (value = vector.get(0).getValue()) == null) ? "" : value;
    }

    private final void prepareReport(Node node, Vector<Node> vector) {
        this.form.report_cell_size = node.childs.size();
        for (Node node2 : node.findLocalNodesByName("field")) {
            Form.Field field = new Form.Field();
            field.TYPE = Form.Field.detectType(node2.getParameter("type"));
            field.LABEL = node2.getParameter("label");
            this.form.report_cell.add(field);
        }
        for (Node node3 : vector) {
            String str = "";
            for (int i = 0; i < this.form.report_cell_size; i++) {
                String value = node3.childs.get(i).findFirstLocalNodeByName("value").getValue();
                switch (this.form.report_cell.get(i).TYPE) {
                    case 0:
                        value = value.equals("1") ? "True" : "False";
                        break;
                }
                String str2 = this.form.report_cell.get(i).LABEL;
                if (str2 != null && str2.trim().length() > 0) {
                    value = String.valueOf(str2) + ": " + value;
                }
                str = String.valueOf(str) + (String.valueOf(value) + "\n");
            }
            if (str.trim().length() > 0) {
                this.form.report_list.add(str.trim());
            }
        }
    }

    public final Node compile() {
        String str;
        Node node = new Node("iq");
        node.putParameter("type", "set");
        node.putParameter("to", this.source.getParameter("from"));
        node.putParameter("id", "form_" + System.currentTimeMillis());
        node.putParameter("xml:lang", Locale.getCurrentLangCode());
        this.root = new Node(this.TAG, "", this.NS);
        switch (this.to_type) {
            case 0:
                str = "cancel";
                break;
            case 1:
                str = "form";
                break;
            case 2:
                str = "result";
                break;
            case 3:
                str = "submit";
                break;
            default:
                str = "";
                break;
        }
        Node node2 = new Node("x", "", "jabber:x:data");
        node2.putParameter("type", str);
        int i = 0;
        for (Form.Field field : this.form.fields) {
            Node node3 = new Node("field");
            if (field.VAR != null) {
                node3.putParameter("var", field.VAR);
            }
            boolean z = field.TYPE != 2;
            View view = z ? this.form.fields_wrappers.get(i) : null;
            switch (field.TYPE) {
                case 0:
                    node3.putChild(new Node("value", ((CheckBox) view).isChecked() ? "1" : "0"));
                    break;
                case 1:
                    node3.putChild(new Node("value", ((EditText) view).getText().toString()));
                    break;
                case 2:
                    node3.putChild(new Node("value", field.VALUE1));
                    break;
                case 3:
                    for (String str2 : utilities.split(((EditText) view).getText().toString(), "\n")) {
                        if (str2.trim().length() != 0) {
                            node3.putChild(new Node("value", str2.trim()));
                        }
                    }
                    break;
                case 4:
                    node3.putChild(new Node("value", ((EditText) view).getText().toString()));
                    break;
                case 5:
                    String[] selected = ((FormListMap) ((ListView) view).getAdapter()).getSelected();
                    if (selected.length > 0) {
                        for (String str3 : selected) {
                            node3.putChild(new Node("value", str3));
                        }
                    } else {
                        node3.putChild(new Node("value"));
                    }
                    break;
                case 6:
                    String[] selected2 = ((Spinner) view).getAdapter().getSelected();
                    node3.putChild(selected2.length > 0 ? new Node("value", selected2[0]) : new Node("value"));
                    break;
                case 7:
                    node3.putChild(new Node("value", ((EditText) view).getText().toString()));
                    break;
                case 8:
                    node3.putChild(new Node("value", ((EditText) view).getText().toString()));
                    break;
                case 9:
                    node3.putChild(new Node("value", ((EditText) view).getText().toString()));
                    break;
            }
            node2.putChild(node3);
            if (z) {
                i++;
            }
        }
        this.root.putChild(node2);
        node.putChild(this.root);
        return node;
    }

    public final Node compileCancel() {
        Node node = new Node("iq");
        node.putParameter("type", "error");
        node.putParameter("from", String.valueOf(this.profile.ID) + "@" + this.profile.host + "/" + this.profile.resource);
        node.putParameter("to", this.source.getParameter("from"));
        node.putParameter("id", this.source.getParameter("id"));
        Node node2 = new Node("error");
        node2.putParameter("type", "modify");
        node2.putChild(new Node("not-acceptable", "", "urn:ietf:params:xml:ns:xmpp-stanzas"));
        node.putChild(node2);
        return node;
    }

    public final void prepareForm(Node node, String str) {
        String firstNodeValueSafe;
        this.source = node.getRoot();
        this.TAG = node.getName();
        this.NS = node.getNamespace();
        this.UID = str;
        Node nodeFindFirstLocalNodeByNameAndNamespace = node.findFirstLocalNodeByNameAndNamespace("x", "jabber:x:data");
        this.form = new Form();
        this.form.operation = this;
        Node nodeFindFirstLocalNodeByName = nodeFindFirstLocalNodeByNameAndNamespace.findFirstLocalNodeByName("title");
        if (nodeFindFirstLocalNodeByName != null) {
            this.form.TITLE = nodeFindFirstLocalNodeByName.getValue();
        }
        Node nodeFindFirstLocalNodeByName2 = nodeFindFirstLocalNodeByNameAndNamespace.findFirstLocalNodeByName("instructions");
        if (nodeFindFirstLocalNodeByName2 != null) {
            this.form.INSTR = nodeFindFirstLocalNodeByName2.getValue();
        }
        this.form.TYPE = Form.detectType(nodeFindFirstLocalNodeByNameAndNamespace.getParameter("type"));
        Node nodeFindFirstLocalNodeByName3 = nodeFindFirstLocalNodeByNameAndNamespace.findFirstLocalNodeByName("reported");
        if (nodeFindFirstLocalNodeByName3 != null) {
            this.form.it_is_report = true;
            prepareReport(nodeFindFirstLocalNodeByName3, nodeFindFirstLocalNodeByNameAndNamespace.findLocalNodesByName("item"));
            return;
        }
        for (Node node2 : nodeFindFirstLocalNodeByNameAndNamespace.findLocalNodesByName("field")) {
            Form.Field field = new Form.Field();
            field.LABEL = node2.getParameter("label");
            field.TYPE = Form.Field.detectType(node2.getParameter("type"));
            field.VAR = node2.getParameter("var");
            Node nodeFindFirstLocalNodeByNameAndNamespace2 = node2.findFirstLocalNodeByNameAndNamespace("media", "urn:xmpp:media-element");
            if (nodeFindFirstLocalNodeByNameAndNamespace2 != null) {
                Log.e("Forms", "Media found");
                Form.Field.Media media = new Form.Field.Media();
                Node nodeContainValuePrefix = nodeFindFirstLocalNodeByNameAndNamespace2.getNodeContainValuePrefix("cid:");
                if (nodeContainValuePrefix != null) {
                    Log.e("Forms", "cid'ed bob_uri found");
                    media.TYPE = Form.Field.Media.detectType(nodeContainValuePrefix.getParameter("type"));
                    Node nodeFindFirstLocalNodeByNameAndParameter = node.getRoot().findFirstLocalNodeByNameAndParameter("data", "cid", nodeContainValuePrefix.getValue().substring(4));
                    if (nodeFindFirstLocalNodeByNameAndParameter != null) {
                        Log.e("Forms", "Data block found");
                        try {
                            media.content = Base64Coder.decode(nodeFindFirstLocalNodeByNameAndParameter.getValue());
                            media.ready = true;
                            Log.e("Forms", "Decoded successfully");
                        } catch (Exception e) {
                        }
                    }
                }
                field.media = media;
            }
            Node nodeFindFirstLocalNodeByName4 = node2.findFirstLocalNodeByName("desc");
            if (nodeFindFirstLocalNodeByName4 != null) {
                field.DESC = nodeFindFirstLocalNodeByName4.getValue();
            }
            int i = 0;
            field.required = node2.findFirstLocalNodeByName("required") != null;
            switch (field.TYPE) {
                case 0:
                    field.VALUE2 = getFirstNodeValueSafe(node2.findLocalNodesByName("value")).equals("1");
                    field.type_defined = true;
                    break;
                case 1:
                case 2:
                case 4:
                    firstNodeValueSafe = getFirstNodeValueSafe(node2.findLocalNodesByName("value"));
                    field.VALUE1 = firstNodeValueSafe;
                    field.type_defined = true;
                    break;
                case 3:
                    Vector<Node> vectorFindLocalNodesByName = node2.findLocalNodesByName("value");
                    int size = vectorFindLocalNodesByName.size();
                    String[] strArr = new String[size];
                    while (i < size) {
                        strArr[i] = vectorFindLocalNodesByName.get(i).getValue();
                        i++;
                    }
                    field.VALUE3 = strArr;
                    field.type_defined = true;
                    break;
                case 5:
                case 6:
                    Vector<Node> vectorFindLocalNodesByName2 = node2.findLocalNodesByName("value");
                    if (vectorFindLocalNodesByName2.size() > 0) {
                        field.VALUE3_ = new String[vectorFindLocalNodesByName2.size()];
                        for (int i2 = 0; i2 < vectorFindLocalNodesByName2.size(); i2++) {
                            field.VALUE3_[i2] = vectorFindLocalNodesByName2.get(i2).getValue();
                        }
                    } else {
                        field.VALUE3_ = new String[0];
                    }
                    Vector<Node> vectorFindLocalNodesByName3 = node2.findLocalNodesByName("option");
                    int size2 = vectorFindLocalNodesByName3.size();
                    String[] strArr2 = new String[size2];
                    field.LABELS = new String[vectorFindLocalNodesByName3.size()];
                    while (i < size2) {
                        Node node3 = vectorFindLocalNodesByName3.get(i);
                        field.LABELS[i] = node3.getParameter("label");
                        strArr2[i] = node3.findFirstLocalNodeByName("value").getValue();
                        i++;
                    }
                    field.VALUE3 = strArr2;
                    field.type_defined = true;
                    break;
                case 7:
                case 8:
                case 9:
                    Vector<Node> vectorFindLocalNodesByName4 = node2.findLocalNodesByName("value");
                    firstNodeValueSafe = vectorFindLocalNodesByName4.size() > 0 ? getFirstNodeValueSafe(vectorFindLocalNodesByName4) : "";
                    field.VALUE1 = firstNodeValueSafe;
                    field.type_defined = true;
                    break;
            }
            this.form.fields.add(field);
        }
    }
}
