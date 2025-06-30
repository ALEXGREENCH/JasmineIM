package ru.ivansuper.jasmin.jabber.disco;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Vector;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.PacketHandler;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

public class DiscoActivity extends Activity {

    public static JProfile PROFILE;
    public static String SERVER_TO_DISCO;
    private static int packet_id = 0;
    private DiscoAdapter mAdapter;
    private ListView mList;
    private EditText mServer;
    private SharedPreferences sp;

    @SuppressWarnings("unused")
    public static String generateID() {
        int res = packet_id;
        packet_id++;
        return "disco_req_" + res;
    }

    public static void putSources(String server, JProfile profile) {
        SERVER_TO_DISCO = server;
        PROFILE = profile;
    }

    @Override
    public void onCreate(Bundle bundle) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String wallpaper_type = sp.getString("ms_wallpaper_type", "0");
        switch (wallpaper_type) {
            case "0":
                setTheme(R.style.WallpaperNoTitleTheme);
                break;
            case "1":
                setTheme(R.style.BlackNoTitleTheme);
                getWindow().setBackgroundDrawable(resources.custom_wallpaper);
                break;
            case "2":
                setTheme(R.style.BlackNoTitleTheme);
                getWindow().setBackgroundDrawable(ColorScheme.getSolid(ColorScheme.getColor(13)));
                break;
        }
        setVolumeControlStream(3);
        super.onCreate(bundle);
        setContentView(R.layout.disco_activity);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
    }

    private void initViews() {
        if (!sp.getBoolean("ms_use_shadow", true)) {
            findViewById(R.id.l1).setBackgroundColor(0);
        }
        mServer = findViewById(R.id.disco_source_server);
        resources.attachEditText(mServer);
        mList = findViewById(R.id.disco_list);
        mList.setSelector(new ColorDrawable(0));
        mList.setDividerHeight(0);
        mList.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            Item item = mAdapter.getItem(arg2);
            if (item.childs_loaded || item.status != 0) {
                item.opened = !item.opened;
                mAdapter.build();
                return;
            }
            doDisco(item);
        });
        mList.setOnItemLongClickListener(new AnonymousClass2());
        Button do_disco = findViewById(R.id.do_disco_btn);
        resources.attachButtonStyle(do_disco);
        do_disco.setText(Locale.getString("s_disco"));
        do_disco.setOnClickListener(v -> {
            boolean z = false;
            if (mServer.getText().toString().trim().length() == 0) {
                return;
            }
            initRoot();
            Item item = mAdapter.getItem(0);
            if (item.childs_loaded || item.status != 0) {
                if (!item.opened) {
                    z = true;
                }
                item.opened = z;
                mAdapter.build();
                return;
            }
            doDisco(item);
        });
        mServer.setText(SERVER_TO_DISCO);
    }

    public class AnonymousClass2 implements AdapterView.OnItemLongClickListener {
        Dialog d = null;
        Dialog progress = null;

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            final Item item = mAdapter.getItem(arg2);
            if (item.status != 1) {
                return false;
            }
            final UAdapter adp = new UAdapter();
            adp.setMode(2);
            adp.setTextSize(18);
            adp.setPadding(15);
            adp.put(Locale.getString("s_node_info"), 0);
            if (item.isTypePresent("command-node")) {
                adp.put(Locale.getString("s_execute_command"), 1);
            }
            progress = DialogBuilder.createProgress(DiscoActivity.this, Locale.getString("s_please_wait"), true);
            boolean show_progress = false;
            if (item.features.contains("jabber:iq:register")) {
                show_progress = true;
                PacketHandler h = new PacketHandler() {
                    @Override
                    public void execute() {
                        Node reg_query;
                        Node stanzas = slot;
                        if (stanzas != null && (reg_query = stanzas.findFirstLocalNodeByNameAndNamespace("query", "jabber:iq:register")) != null) {
                            Node registered = reg_query.findFirstLocalNodeByName("registered");
                            if (registered != null) {
                                adp.put(Locale.getString("s_do_unregister"), 3);
                            } else {
                                adp.put(Locale.getString("s_do_register"), 2);
                            }
                            adp.notifyDataSetChanged();
                        }
                        progress.dismiss();
                    }
                };
                DiscoActivity.PROFILE.putPacketHandler(h);
                Node iq = new Node("iq");
                iq.putParameter("type", "get").putParameter("to", item.JID).putParameter("id", h.getID());
                Node query = new Node("query", "", "jabber:iq:register");
                iq.putChild(query);
                DiscoActivity.PROFILE.stream.write(iq, DiscoActivity.PROFILE);
            }
            d = DialogBuilder.createWithNoHeader(DiscoActivity.this, adp, 0, (arg02, arg12, arg22, arg32) -> {
                d.dismiss();
                switch ((int) adp.getItemId(arg22)) {
                    case 0:
                        StringBuilder buf = new StringBuilder();
                        buf.append("JID: ").append(item.JID).append("\n");
                        buf.append("\nIdentities:\n");
                        if (item.identities.size() > 0) {
                            for (Item.Identity i : item.identities) {
                                buf.append("*");
                                if (i.name != null) {
                                    buf.append("\"").append(i.name).append("\"/");
                                }
                                if (i.category != null) {
                                    buf.append("Category: ").append(i.category).append(", ");
                                }
                                if (i.type != null) {
                                    buf.append("Type: ").append(i.type);
                                }
                                buf.append("\n");
                            }
                        } else {
                            buf.append("No identities\n");
                        }
                        buf.append("\nFeatures:\n");
                        if (item.features.size() > 0) {
                            for (String f : item.features) {
                                buf.append("*").append(f).append("\n");
                            }
                        } else {
                            buf.append("No features");
                        }
                        d = DialogBuilder.createOk(DiscoActivity.this, Locale.getString("s_node_info"), buf.toString(), Locale.getString("s_ok"), 0, v -> d.dismiss());
                        d.show();
                        return;
                    case 1:
                        AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                        DiscoActivity discoActivity = DiscoActivity.this;
                        String string = Locale.getString("s_execute_command");
                        String string2 = Locale.getString("s_window_will_be_closed");
                        String string3 = Locale.getString("s_yes");
                        String string4 = Locale.getString("s_no");
                        //noinspection UnnecessaryLocalVariable
                        final Item item2 = item;
                        anonymousClass2.d = DialogBuilder.createYesNo(discoActivity, 0, string, string2, string3, string4, v -> {
                            DiscoActivity.PROFILE.executeCommand(item2.JID, item2.XML_NODE);
                            d.dismiss();
                            finish();
                        }, v -> d.dismiss());
                        d.show();
                        return;
                    case 2:
                        AnonymousClass2 anonymousClass22 = AnonymousClass2.this;
                        DiscoActivity discoActivity2 = DiscoActivity.this;
                        String string5 = Locale.getString("s_do_register");
                        String string6 = Locale.getString("s_window_will_be_closed");
                        String string7 = Locale.getString("s_yes");
                        String string8 = Locale.getString("s_no");
                        //noinspection UnnecessaryLocalVariable
                        final Item item3 = item;
                        anonymousClass22.d = DialogBuilder.createYesNo(discoActivity2, 0, string5, string6, string7, string8, v -> {
                            DiscoActivity.PROFILE.launchRegistration(item3.JID);
                            d.dismiss();
                            finish();
                        }, v -> d.dismiss());
                        d.show();
                        return;
                    case 3:
                        AnonymousClass2 anonymousClass23 = AnonymousClass2.this;
                        DiscoActivity discoActivity3 = DiscoActivity.this;
                        String string9 = Locale.getString("s_do_unregister");
                        String string10 = Locale.getString("s_are_you_sure");
                        String string11 = Locale.getString("s_yes");
                        String string12 = Locale.getString("s_no");
                        //noinspection UnnecessaryLocalVariable
                        final Item item4 = item;
                        anonymousClass23.d = DialogBuilder.createYesNo(discoActivity3, 0, string9, string10, string11, string12, v -> {
                            DiscoActivity.PROFILE.cancelRegistration(item4.JID);
                            d.dismiss();
                        }, v -> d.dismiss());
                        d.show();
                        return;
                    default:
                }
            });
            d.show();
            if (show_progress) {
                progress.show();
            }
            return true;
        }
    }

    public final void initRoot() {
        SERVER_TO_DISCO = mServer.getText().toString();
        Item root = new Item(SERVER_TO_DISCO, null, null, SERVER_TO_DISCO);
        mAdapter = new DiscoAdapter(root);
        mList.setAdapter(mAdapter);
    }

    public final void buildList() {
        if (mList != null) {
            mList.post(() -> mAdapter.build());
        }
    }

    private void doDisco(final Item item) {
        item.status = 2;
        PROFILE.sendDiscoRequest(item.JID, item.XML_NODE, new PacketHandler(false) {
            @Override
            public void execute() {
                Node stanzas = slot;
                String type = stanzas.getParameter("type");
                if (type.equals("error")) {
                    item.status = 3;
                    item.childs_loaded = true;
                    buildList();
                    return;
                }
                Node query = stanzas.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/disco#items");
                if (query != null && query.hasChilds()) {
                    Vector<Node> items = query.findLocalNodesByName("item");
                    Vector<Item> items_ = new Vector<>();
                    for (int i = 0; i < items.size(); i++) {
                        Node n = items.get(i);
                        String name = n.getParameterWODecode("name");
                        String node = n.getParameterWODecode("node");
                        String jid = n.getParameterWODecode("jid");
                        if (name == null) {
                            name = node;
                        }
                        if (name == null) {
                            name = jid;
                        }
                        Item item_ = new Item(name, item, node, jid);
                        items_.add(item_);
                    }
                    item.append(items_);
                }
                item.childs_loaded = true;
                buildList();
                item.status = 1;
            }
        }, false);
        PROFILE.sendDiscoRequest(item.JID, item.XML_NODE, new PacketHandler(false) {
            @Override
            public void execute() {
                Node stanzas = slot;
                String type = stanzas.getParameter("type");
                if (!type.equals("error")) {
                    Node query = stanzas.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/disco#info");
                    if (query != null && query.hasChilds()) {
                        Vector<Node> identities = query.findLocalNodesByName("identity");
                        item.identities.clear();
                        for (int i = 0; i < identities.size(); i++) {
                            Node idn = identities.get(i);
                            Item.Identity idn_ = new Item.Identity();
                            idn_.name = idn.getParameter("name");
                            idn_.category = idn.getParameter("category");
                            idn_.type = idn.getParameter("type");
                            item.identities.add(idn_);
                        }
                        item.detectType();
                        Vector<Node> features = query.findLocalNodesByName("feature");
                        item.features.clear();
                        for (int i2 = 0; i2 < features.size(); i2++) {
                            Node f = features.get(i2);
                            item.features.add(f.getParameter("var"));
                        }
                    }
                    buildList();
                }
            }
        }, true);
        buildList();
    }
}
