package ru.ivansuper.jasmin.jabber.commands;

import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.forms.Operation;
import ru.ivansuper.jasmin.locale.Locale;

public class Command {
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PREV = "prev";
    public static final int BUTTON_COMPLETE = 2;
    public static final int BUTTON_NEXT = 1;
    public static final int BUTTON_PREV = 0;
    public String UID;
    public int[] buttons;
    public boolean completed;
    public String from;
    private String node;
    public Operation op = new Operation();
    public int selected_button;
    public String sessionid;

    public Command(String str, String str2, Node node, JProfile jProfile) {
        this.UID = str2;
        this.from = str;
        this.op.profile = jProfile;
        this.op.prepareForm(node, str2);
        this.op.form.build();
        this.op.to_type = 3;
        this.sessionid = node.getParameter("sessionid");
        this.node = node.getParameter("node");
        this.completed = node.getParameter("status").equals("completed");
        if (this.completed) {
            return;
        }
        Node nodeFindFirstLocalNodeByName = node.findFirstLocalNodeByName("actions");
        if (nodeFindFirstLocalNodeByName == null) {
            this.buttons = new int[0];
            return;
        }
        this.buttons = new int[nodeFindFirstLocalNodeByName.childs.size()];
        for (int i = 0; i < this.buttons.length; i++) {
            if (nodeFindFirstLocalNodeByName.childs.get(i).getName().equals(ACTION_PREV)) {
                this.buttons[i] = 0;
            } else if (nodeFindFirstLocalNodeByName.childs.get(i).getName().equals(ACTION_NEXT)) {
                this.buttons[i] = 1;
            } else if (nodeFindFirstLocalNodeByName.childs.get(i).getName().equals(ACTION_COMPLETE)) {
                this.buttons[i] = 2;
            }
        }
    }

    public static final String getButtonName(int i) {
        String str;
        switch (i) {
            case 0:
                str = "s_form_back";
                break;
            case 1:
                str = "s_form_next";
                break;
            case 2:
                str = "s_form_complete";
                break;
            default:
                return "";
        }
        return Locale.getString(str);
    }

    public final void cancel() {
        Node node = new Node("iq");
        node.putParameter("type", "set").putParameter("to", this.from).putParameter("id", this.UID).putParameter("xml:lang", Locale.getCurrentLangCode());
        Node node2 = new Node("command", "", "http://jabber.org/protocol/commands");
        node2.putParameter("node", this.node).putParameter("sessionid", this.sessionid).putParameter("action", "cancel");
        node.putChild(node2);
        this.op.profile.stream.write(node, this.op.profile);
    }

    public final void proceed() {
        String str;
        Node nodeCompile = this.op.compile();
        Node nodeFindFirstLocalNodeByNameAndNamespace = nodeCompile.findFirstLocalNodeByNameAndNamespace("command", "http://jabber.org/protocol/commands");
        nodeFindFirstLocalNodeByNameAndNamespace.putParameter("node", this.node).putParameter("sessionid", this.sessionid);
        if (this.buttons.length == 2) {
            switch (this.buttons[this.selected_button]) {
                case 0:
                    str = ACTION_PREV;
                    break;
                case 1:
                    str = ACTION_NEXT;
                    break;
                case 2:
                    str = ACTION_COMPLETE;
                    break;
                default:
                    str = null;
                    break;
            }
            nodeFindFirstLocalNodeByNameAndNamespace.putParameter("action", str);
        }
        this.op.profile.stream.write(nodeCompile, this.op.profile);
    }
}
