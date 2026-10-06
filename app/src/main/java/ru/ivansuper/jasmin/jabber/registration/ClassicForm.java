package ru.ivansuper.jasmin.jabber.registration;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import ru.ivansuper.jasmin.jabber.AbstractForm;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;

public class ClassicForm extends AbstractForm {
    public String UID;
    public EditText email;
    public TextView instructions;
    public LinearLayout layout;
    public EditText password;
    public JProfile profile;
    public String to;
    public EditText username;

    public final void build(Node node, String str, String str2, String str3, JProfile jProfile) {
        this.layout = (LinearLayout) View.inflate(resources.ctx, 2130903101, null);
        this.form = new ScrollView(resources.ctx);
        this.form.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        this.form.addView(this.layout);
        this.instructions = (TextView) this.layout.findViewById(2131427630);
        this.username = (EditText) this.layout.findViewById(2131427631);
        this.username.setHint(Locale.getString("s_reg_hint_username"));
        this.password = (EditText) this.layout.findViewById(2131427633);
        this.password.setHint(Locale.getString("s_reg_hint_password"));
        this.email = (EditText) this.layout.findViewById(2131427632);
        this.email.setHint(Locale.getString("s_reg_hint_email"));
        Node nodeFindFirstLocalNodeByName = node.findFirstLocalNodeByName("instructions");
        if (nodeFindFirstLocalNodeByName != null) {
            this.instructions.setText(nodeFindFirstLocalNodeByName.getValue());
        } else {
            this.instructions.setVisibility(8);
        }
        Node nodeFindFirstLocalNodeByName2 = node.findFirstLocalNodeByName("username");
        if (nodeFindFirstLocalNodeByName2 != null) {
            this.username.setText(nodeFindFirstLocalNodeByName2.getValue());
        } else {
            this.username.setVisibility(8);
        }
        Node nodeFindFirstLocalNodeByName3 = node.findFirstLocalNodeByName("password");
        if (nodeFindFirstLocalNodeByName3 != null) {
            this.password.setText(nodeFindFirstLocalNodeByName3.getValue());
        } else {
            this.password.setVisibility(8);
        }
        Node nodeFindFirstLocalNodeByName4 = node.findFirstLocalNodeByName("email");
        if (nodeFindFirstLocalNodeByName4 != null) {
            this.email.setText(nodeFindFirstLocalNodeByName4.getValue());
        } else {
            this.email.setVisibility(8);
        }
        this.to = str;
        this.UID = str2;
        this.TITLE = str3;
        this.profile = jProfile;
    }

    @Override
    public void cancel() {
    }

    public final Node compile() {
        Node node = new Node("iq");
        node.putParameter("type", "set").putParameter("id", this.UID);
        if (this.to != null || this.to.length() > 0) {
            node.putParameter("to", this.to);
        }
        Node node2 = new Node("query", "", "jabber:iq:register");
        if (this.username.getVisibility() == 0) {
            node2.putChild(new Node("username", this.username.getText().toString().trim()));
        }
        if (this.password.getVisibility() == 0) {
            node2.putChild(new Node("password", this.password.getText().toString().trim()));
        }
        if (this.email.getVisibility() == 0) {
            node2.putChild(new Node("email", this.email.getText().toString().trim()));
        }
        node.putChild(node2);
        return node;
    }

    @Override
    public void send() {
        this.profile.stream.write(compile(), this.profile);
    }
}
