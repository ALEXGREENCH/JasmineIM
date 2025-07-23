package ru.ivansuper.jasmin.chats;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Vector;
import ru.ivansuper.jasmin.BufferedDialog;
import ru.ivansuper.jasmin.ConferenceAdapter;
import ru.ivansuper.jasmin.ContactListActivity;
import ru.ivansuper.jasmin.HistoryItem;
import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.VCard;
import ru.ivansuper.jasmin.jabber.commands.Callback;
import ru.ivansuper.jasmin.jabber.commands.CommandItem;
import ru.ivansuper.jasmin.jabber.conference.BannedListActivity;
import ru.ivansuper.jasmin.jabber.conference.ConfUsersAdapter;
import ru.ivansuper.jasmin.jabber.conference.Conference;
import ru.ivansuper.jasmin.jabber.forms.Operation;
import ru.ivansuper.jasmin.jabber.xml_utils;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.plugins._interface.IdentificatedRunnable;
import ru.ivansuper.jasmin.plugins._interface.JConferenceWindowInterface;
import ru.ivansuper.jasmin.plugins._interface.MenuItemWrapper;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.slide_tools.ListViewA;
import ru.ivansuper.jasmin.utilities;

public class JConference extends Chat implements Handler.Callback {
    public static final int BANNED_LIST_RECEIVED = 400;
    public static boolean INITIALIZED = false;
    public static final int SHOW_INFO = 66;
    public static final int SHOW_JABBER_FORM = 256;
    public static final int SHOW_VCARD = 65;
    public static Conference conference;
    public LinearLayout TOP_PANEL;
    public ConferenceAdapter chatAdp;
    public LinearLayout chat_back;
    public ConfUsersAdapter conf_users;
    private final Vector<IdentificatedRunnable> context_menus_runnables = new Vector<>();
    public Conference.User context_user;
    public BufferedDialog dialog_for_display;
    private ImageView mUsersToggleButton;
    public ImageView mainStatus;
    protected int moderation_operation;
    public TextView nick_;
    public TextView nickname;
    public TextView theme;
    public ListView userList;
    public VCard vcard_to_display;
    public ImageView xStatus;
    public static boolean multiquoting = false;
    public static boolean is_any_chat_opened = false;
    public static boolean TOP_PANEL_VISIBLED = true;

    private JConference(ChatInitCallback callback, Conference conference_) {
        setScrollStateHash(Integer.toHexString(utilities.getHash(conference_)));
        conference = conference_;
        this.init_callback = callback;
    }

    private synchronized void putIdentificatedTask(Runnable task, int id) {
        this.context_menus_runnables.add(new IdentificatedRunnable(task, id));
    }

    public synchronized void checkAndRunIdentificatedTask(int id) {
        for (int i = 0; i < context_menus_runnables.size(); i++) {
            IdentificatedRunnable task = context_menus_runnables.get(i);
            if (task.id == id) {
                context_menus_runnables.remove(i).task.run();
                break;
            }
        }
    }

    public static JConference getInstance(Conference conference_, ChatInitCallback callback) {
        return new JConference(callback, conference_);
    }

    public static JConference getInstance(String action, ChatInitCallback callback) {
        JProfile profile;
        Conference conference_ = null;
        if (action != null && action.startsWith("ITEM")) {
            String[] parts = utilities.split(action.substring(4), "***$$$SEPARATOR$$$***");
            if (parts.length != 2 || (profile = resources.service.profiles.getProfileByID(parts[0])) == null) {
                return null;
            }
            conference_ = profile.getConference(parts[1]);
        }
        return new JConference(callback, conference_);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        received_smile_tag = "";
        INITIALIZED = true;
        this.sp = getDefaultSharedPreferences();
        setVolumeControlStream(3);
        setContentView(resources.IT_IS_TABLET ? R.layout.conference_xhigh : R.layout.conference);
        input_manager = (InputMethodManager) getSystemService("input_method");
        service = resources.service;
    }

    @Override
    public void onStart() {
        super.onStart();
        initViews();
        checkOrientation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean catched = JConferenceWindowInterface.dispatchWindowResultEvent(this, requestCode, resultCode, data);
        if (!catched && requestCode == 162 && resultCode == -1) {
            received_smile_tag = data.getAction();
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        Dialog catched = JConferenceWindowInterface.dispatchWindowCreateDialogEvent(this, id);
        if (catched == null) {
            Dialog ad = null;
            if (id == 1 && conference != null) {
                UAdapter adp = new UAdapter();
                adp.setMode(2);
                adp.setTextSize(18);
                adp.setPadding(15);
                if (conference.profile.connected && !conference.isOnline()) {
                    adp.put(resources.getString("s_join_conference"), 5);
                }
                if (conference.isOnline()) {
                    adp.put(resources.getString("s_users_list"), 3);
                }
                if (!multiquoting) {
                    adp.put(resources.getString("s_turn_on_multiquote"), 1);
                }
                if (multiquoting) {
                    adp.put(resources.getString("s_turn_off_multiquote"), 2);
                }
                if (conference.isOnline()) {
                    adp.put(resources.getString("s_set_theme"), 4);
                }
                adp.put(resources.getString("s_change_nick"), 8);
                adp.put(resources.getString("s_clear_messages"), 7);
                if (conference.isMeAModerator()) {
                    adp.put(Locale.getString("s_banned_list"), 9);
                }
                if (conference.isMeAOwner()) {
                    adp.put(Locale.getString("s_room_settings"), 10);
                }
                if (conference.isOnline()) {
                    adp.put(resources.getString("s_leave_conference"), 0);
                }
                for (int i = 0; i < 64; i++) {
                    MenuItemWrapper wrapper = new MenuItemWrapper(null, null, 0);
                    IdentificatedRunnable task = JConferenceWindowInterface.dispatchBindMenuItem(this, 0, i, wrapper);
                    if (wrapper.label != null) {
                        if (wrapper.icon != null) {
                            adp.put(wrapper.icon, wrapper.label, wrapper.id + 1024);
                        } else {
                            adp.put(wrapper.label, wrapper.id + 1024);
                        }
                        if (task != null) {
                            putIdentificatedTask(task.task, task.id + 1024);
                        }
                    }
                }
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp, 48, new chatMenuListener(adp));
            }
            if (id == 2) {
                final EditText count = new EditText(this.ACTIVITY);
                count.setMinLines(2);
                count.setHint(resources.getString("s_clear_msgs_dialog_hint") + "(" + this.chatAdp.getCount() + ")");
                count.setInputType(8194);
                resources.attachEditText(count);
                ad = DialogBuilder.createYesNo(this.ACTIVITY, count, 48, resources.getString("s_clear_messages"), resources.getString("s_ok"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String cnt = count.getText().toString();
                        if (cnt.isEmpty()) {
                            cnt = String.valueOf(JConference.this.chatAdp.getCount());
                        }
                        try {
                            int cnt_ = Integer.parseInt(cnt);
                            if (cnt_ != 0) {
                                if (cnt_ > JConference.this.chatAdp.getCount()) {
                                    cnt_ = JConference.this.chatAdp.getCount();
                                }
                                JConference.this.chatAdp.cutoff(cnt_);
                                Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_messages_cleared"), Toast.LENGTH_SHORT).show();
                                JConference.this.removeDialog(2);
                            }
                        } catch (Exception e) {
                            //noinspection CallToPrintStackTrace
                            e.printStackTrace();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JConference.this.removeDialog(2);
                    }
                });
            }
            if (id == 3) {
                UAdapter adp2 = new UAdapter();
                adp2.setMode(2);
                adp2.setTextSize(18);
                adp2.setPadding(15);
                adp2.put(resources.getString("s_copy"), 13);
                adp2.put(resources.getString("s_quote"), 14);
                adp2.put(resources.getString("s_copy_only_text"), 18);
                adp2.put(resources.getString("s_quote_only_text"), 17);
                for (int i2 = 0; i2 < 64; i2++) {
                    MenuItemWrapper wrapper2 = new MenuItemWrapper(null, null, 0);
                    IdentificatedRunnable task2 = JConferenceWindowInterface.dispatchBindMenuItem(this, 2, i2, wrapper2);
                    if (wrapper2.label != null) {
                        if (wrapper2.icon != null) {
                            adp2.put(wrapper2.icon, wrapper2.label, wrapper2.id + 1024);
                        } else {
                            adp2.put(wrapper2.label, wrapper2.id + 1024);
                        }
                        if (task2 != null) {
                            putIdentificatedTask(task2.task, task2.id + 1024);
                        }
                    }
                }
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp2, 48, new chatMenuListener(adp2));
            }
            if (id == 4) {
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, this.conf_users, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        JConference.this.context_user = JConference.this.conf_users.getItem(i);
                        JConference.this.removeDialog(4);
                        JConference.this.removeDialog(6);
                        JConference.this.showDialog(6);
                    }
                });
            }
            if (id == 5) {
                final EditText theme = new EditText(this.ACTIVITY);
                theme.setText(conference.theme);
                theme.setMinLines(3);
                theme.setMaxLines(6);
                theme.setGravity(51);
                resources.attachEditText(theme);
                theme.setHint(resources.getString("s_set_theme_dialog_hint"));
                ad = DialogBuilder.createYesNo(this.ACTIVITY, theme, 48, resources.getString("s_set_theme"), resources.getString("s_ok"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String theme_ = theme.getText().toString();
                        if (theme_.isEmpty()) {
                            Toast toast = Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_set_theme_error"), Toast.LENGTH_SHORT);
                            toast.setGravity(48, 0, 0);
                            toast.show();
                        } else {
                            JConference.conference.setTheme(theme_);
                            JConference.this.removeDialog(5);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JConference.this.removeDialog(5);
                    }
                });
            }
            if (id == 6) {
                UAdapter adp3 = new UAdapter();
                adp3.setMode(2);
                adp3.setTextSize(18);
                adp3.setPadding(10);
                adp3.put(resources.getString("s_user_nick"), 0);
                adp3.put(resources.getString("s_start_personal_chat"), 1);
                adp3.put(resources.getString("s_user_vcard"), 2);
                if (this.context_user.jid != null && !this.context_user.jid.isEmpty()) {
                    adp3.put(resources.getString("s_copy_jid"), 5);
                }
                adp3.put(resources.getString("s_commands"), 6);
                int strength = conference.getUserStrength(conference.nick);
                int user_strength = conference.getUserStrength(this.context_user.nick);
                switch (strength) {
                    case 5:
                    case 6:
                        if (user_strength < strength) {
                            adp3.put(resources.getString("s_moderator_kick"), 3);
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_role"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_visitor"), 7);
                            if (this.context_user.role.equals("visitor")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_participant"), 8);
                            if (this.context_user.role.equals("participant")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        }
                        break;
                    case 7:
                        if (user_strength < strength) {
                            adp3.put(resources.getString("s_moderator_ban"), 4);
                            adp3.put(resources.getString("s_moderator_kick"), 3);
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_role"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_visitor"), 7);
                            if (this.context_user.role.equals("visitor")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_participant"), 8);
                            if (this.context_user.role.equals("participant")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_moderator"), 9);
                            if (this.context_user.role.equals("moderator")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_affiliation"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_none"), 10);
                            if (this.context_user.affiliation.equals("none")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_member"), 11);
                            if (this.context_user.affiliation.equals("member")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        }
                        break;
                    case 8:
                        if (user_strength == strength) {
                            adp3.put(resources.getString("s_moderator_ban"), 4);
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_affiliation"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_none"), 10);
                            if (this.context_user.affiliation.equals("none")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_member"), 11);
                            if (this.context_user.affiliation.equals("member")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_admin"), 12);
                            if (this.context_user.affiliation.equals("admin")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_owner"), 13);
                            if (this.context_user.affiliation.equals("owner")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        } else if (user_strength < strength) {
                            adp3.put(resources.getString("s_moderator_ban"), 4);
                            adp3.put(resources.getString("s_moderator_kick"), 3);
                            if (!conference.isHeAreAdmin(this.context_user.nick)) {
                                adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_role"));
                                adp3.put(Locale.getString("s_jabber_conf_usermenu_visitor"), 7);
                                if (this.context_user.role.equals("visitor")) {
                                    adp3.toggleSelection(adp3.getLastIndex());
                                }
                                adp3.put(Locale.getString("s_jabber_conf_usermenu_participant"), 8);
                                if (this.context_user.role.equals("participant")) {
                                    adp3.toggleSelection(adp3.getLastIndex());
                                }
                                adp3.put(Locale.getString("s_jabber_conf_usermenu_moderator"), 9);
                                if (this.context_user.role.equals("moderator")) {
                                    adp3.toggleSelection(adp3.getLastIndex());
                                }
                            }
                            adp3.put_separator(Locale.getString("s_jabber_conf_usermenu_affiliation"));
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_none"), 10);
                            if (this.context_user.affiliation.equals("none")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_member"), 11);
                            if (this.context_user.affiliation.equals("member")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_admin"), 12);
                            if (this.context_user.affiliation.equals("admin")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                            }
                            adp3.put(Locale.getString("s_jabber_conf_usermenu_owner"), 13);
                            if (this.context_user.affiliation.equals("owner")) {
                                adp3.toggleSelection(adp3.getLastIndex());
                                break;
                            }
                        }
                        break;
                }
                ad = DialogBuilder.createWithNoHeader(this.ACTIVITY, adp3, 48, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        JConference.this.removeDialog(6);
                        int id2 = (int) adapterView.getAdapter().getItemId(i);
                        JConference.this.checkAndRunIdentificatedTask(id2);
                        switch (id2) {
                            case 0:
                                if (JConference.this.input.length() > 0) {
                                    JConference.this.input.append((JConference.this.input.getText().toString().endsWith(" ") ? "" : " ") + JConference.this.context_user.nick + " ");
                                } else {
                                    JConference.this.input.append(JConference.this.context_user.nick + ": ");
                                }
                                break;
                            case 1:
                                JContact contact = JConference.conference.profile.createPMContainer(JConference.conference.JID + "/" + JConference.this.context_user.nick, JConference.conference);
                                if (contact != null) {
                                    ((ContactListActivity) JConference.this.ACTIVITY).startFragmentChat(contact);
                                    break;
                                }
                                break;
                            case 2:
                                JConference.conference.profile.doRequestInfoForDisplayRaw(JConference.conference.JID + "/" + JConference.this.context_user.nick);
                                break;
                            case 3:
                                JConference.this.moderation_operation = 0;
                                JConference.this.removeDialog(10);
                                JConference.this.showDialog(10);
                                break;
                            case 4:
                                JConference.this.moderation_operation = 1;
                                JConference.this.removeDialog(10);
                                JConference.this.showDialog(10);
                                break;
                            case 5:
                                //noinspection deprecation
                                ClipboardManager cm = (ClipboardManager) JConference.this.getSystemService("clipboard");
                                //noinspection deprecation
                                cm.setText(JConference.this.context_user.jid);
                                Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                                break;
                            case 6:
                                final Dialog load_progress = DialogBuilder.createProgress(JConference.this.ACTIVITY, Locale.getString("s_getting_commands"), true);
                                load_progress.show();
                                Callback callback = new Callback() {
                                    @Override
                                    public void onListLoaded(final Vector<CommandItem> list) {
                                        load_progress.dismiss();
                                        if (list.isEmpty()) {
                                            JConference.service.showMessageInContactList(Locale.getString("s_information"), Locale.getString("s_no_commands"));
                                            return;
                                        }
                                        UAdapter adp4 = new UAdapter();
                                        adp4.setMode(2);
                                        adp4.setPadding(14);
                                        for (int i3 = 0; i3 < list.size(); i3++) {
                                            CommandItem item = list.get(i3);
                                            adp4.put(item.name, i3);
                                        }
                                        Dialog commands = DialogBuilder.createWithNoHeader(JConference.this.ACTIVITY, adp4, 0, new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                CommandItem item2 = list.get(i);
                                                JConference.conference.profile.executeCommand(item2.jid, item2.node);
                                            }
                                        });
                                        commands.show();
                                    }
                                };
                                JConference.conference.profile.getCommandList(JConference.conference.JID + "/" + JConference.this.context_user.nick, callback);
                                break;
                            case 7:
                                JConference.conference.setUserRole(JConference.this.context_user.nick, "visitor");
                                break;
                            case 8:
                                JConference.conference.setUserRole(JConference.this.context_user.nick, "participant");
                                break;
                            case 9:
                                JConference.conference.setUserRole(JConference.this.context_user.nick, "moderator");
                                break;
                            case 10:
                                JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "none");
                                break;
                            case 11:
                                JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "member");
                                break;
                            case 12:
                                JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "admin");
                                break;
                            case 13:
                                JConference.conference.setUserAffiliation(JConference.this.context_user.nick, "owner");
                                break;
                        }
                    }
                });
            }
            if (id == 7) {
                LinearLayout vcard_lay = (LinearLayout) View.inflate(this.ACTIVITY, R.layout.vcard, null);
                ImageView vcard_avatar = vcard_lay.findViewById(R.id.vcard_avatar);
                EditText vcard_desc = vcard_lay.findViewById(R.id.vcard_desc);
                if (this.vcard_to_display.avatar != null) {
                    vcard_avatar.setImageBitmap(this.vcard_to_display.avatar);
                }
                vcard_desc.setText(this.vcard_to_display.desc);
                vcard_desc.setFilters(new InputFilter[]{
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                return source.length() < 1 ? dest.subSequence(dstart, dend) : "";
                            }
                        }
                });

                ad = DialogBuilder.createYesNo(
                        JConference.this.ACTIVITY,
                        vcard_lay,
                        48,
                        resources.getString("s_user_vcard"),
                        resources.getString("s_copy"),
                        resources.getString("s_close"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager cm = (ClipboardManager) JConference.this.getSystemService("clipboard");
                                //noinspection deprecation
                                cm.setText(JConference.this.vcard_to_display.desc);
                                Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                                JConference.this.vcard_to_display = null;
                                JConference.this.removeDialog(7);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JConference.this.vcard_to_display = null;
                                JConference.this.removeDialog(7);
                            }
                        }
                );
            }
            if (id == 9) {
                final EditText nick = new EditText(this.ACTIVITY);
                nick.setText(conference.nick);
                nick.setMinLines(1);
                nick.setMaxLines(1);
                nick.setGravity(51);
                resources.attachEditText(nick);

                ad = DialogBuilder.createYesNo(
                        this.ACTIVITY,
                        nick,
                        48,
                        resources.getString("s_change_nick"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String nick_ = nick.getText().toString().trim();
                                if (nick_.isEmpty()) {
                                    Toast toast = Toast.makeText(JConference.this.ACTIVITY, resources.getString("s_change_nick_error"), Toast.LENGTH_SHORT);
                                    toast.setGravity(48, 0, 0);
                                    toast.show();
                                } else {
                                    JConference.conference.updateNickname(nick_);
                                    JConference.this.removeDialog(9);
                                }
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JConference.this.removeDialog(9);
                            }
                        }
                );
            }
            if (id == 10) {
                final EditText input_ = new EditText(this.ACTIVITY);
                input_.setMinLines(3);
                input_.setMaxLines(3);
                input_.setGravity(51);
                resources.attachEditText(input_);
                input_.setHint(resources.getString("s_reason_input"));

                ad = DialogBuilder.createYesNo(
                        this.ACTIVITY,
                        input_,
                        48,
                        resources.getString("s_moderation"),
                        resources.getString("s_ok"),
                        resources.getString("s_cancel"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String reason = input_.getText().toString().trim();
                                if (JConference.this.moderation_operation == 0) {
                                    JConference.conference.kickUser(JConference.this.context_user, xml_utils.encodeString(reason));
                                } else if (JConference.this.moderation_operation == 1) {
                                    JConference.conference.banUser(JConference.this.context_user, xml_utils.encodeString(reason));
                                }
                                JConference.this.removeDialog(10);
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JConference.this.removeDialog(10);
                            }
                        }
                );
            }
            if (id == 8) {
                ad = DialogBuilder.createOk(this.ACTIVITY, this.dialog_for_display.header, this.dialog_for_display.text, resources.getString("s_close"), 48, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JConference.this.removeDialog(8);
                    }
                });
            }
            return ad;
        }
        return catched;
    }

    private void initSettings() {
        this.sendByEnter = this.sp.getBoolean("ms_send_by_enter", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        is_any_chat_opened = true;
        if (service == null) {
            service = resources.service;
        }
        initSettings();
        initChat();
        JConferenceWindowInterface.dispatchWindowEvent(this, 2);
    }

    @Override
    public void onPause() {
        is_any_chat_opened = false;
        super.onPause();
        if (service != null) {
            service.isAnyChatOpened = false;
        }
        if (conference != null) {
            if (this.input != null) {
                conference.typedText = this.input.getText().toString();
            }
            //noinspection DataFlowIssue
            MessageSaveHelper.putMessage(this.SAVE_HASH, this.input.getText().toString());
            saveScrollState();
        }
        multiquoting = false;
        this.messageList.setDragDropEnabled(true);
        JConferenceWindowInterface.dispatchWindowEvent(this, 1);
    }

    @Override
    public void onDestroy() {
        INITIALIZED = false;
        if (this.chatAdp != null) {
            resetSelection();
        }
        if (conference != null) {
            conference.callback = null;
        }
        JConferenceWindowInterface.dispatchWindowEvent(this, 3);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration, int diff) {
        super.onConfigurationChanged(configuration, diff);
        checkOrientation();
    }

    protected void finalize() throws Throwable {
        Log.e(getClass().getSimpleName(), "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
        super.finalize();
    }

    private void checkOrientation() {
        if (resources.ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (this.userList != null) {
                this.mUsersToggleButton.setVisibility(View.VISIBLE);
                toggleUserlistVisibility(false);
                return;
            }
            return;
        }
        if (this.userList != null) {
            this.mUsersToggleButton.setVisibility(View.GONE);
            this.userList.setVisibility(View.GONE);
        }
    }

    private void toggleUserlistVisibility(boolean toggle) {
        if (toggle) {
            boolean currentlyVisible = Manager.getBoolean("conf_userlist_state", true);
            Manager.putBoolean("conf_userlist_state", !currentlyVisible);
        }

        boolean visible = Manager.getBoolean("conf_userlist_state", true);
        userList.setVisibility(visible ? View.VISIBLE : View.GONE);
        mUsersToggleButton.setImageResource(
                visible ? R.drawable.conf_userslist_closed : R.drawable.conf_userslist_opened
        );
    }

    private void initChat() {
        this.hdl = new Handler(this);
        service.chatHdl = this.hdl;
        this.messageList.setService(service);
        initChatInterface();
    }

    public void initChatInterface() {
        service.cancelPersonalMessageNotify(utilities.getHash(conference));
        conference.item.setHasNoUnreadMessages();
        conference.unreaded = 0;
        service.removeMessageNotify(conference.item);
        service.handleContactlistDatasetChanged();
        drawReceiverData();

        // История сообщений
        if (this.chatAdp != null && this.chatAdp.isThatHistory(conference.history)) {
            this.chatAdp.refreshList();
        } else {
            this.chatAdp = new ConferenceAdapter(this.ACTIVITY, conference.history, this.messageList);
            this.messageList.setAdapter(this.chatAdp);
            restoreScrollState();
        }

        // Список пользователей конференции
        this.conf_users = new ConfUsersAdapter(this.ACTIVITY, conference.users);
        this.userList.setAdapter(this.conf_users);
        this.userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                context_user = conf_users.getItem(position);
                String append = input.length() > 0 && !input.getText().toString().endsWith(" ") ? " " : "";
                input.append(append + context_user.nick + (input.length() == 0 ? ": " : " "));
            }
        });

        this.userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                context_user = conf_users.getItem(position);
                removeDialog(4);
                removeDialog(6);
                showDialog(6);
                return false;
            }
        });

        // Callback обновления списка пользователей
        conference.callback = new Conference.RefreshCallback() {
            @Override
            public void update() {
                service.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        conf_users.notifyDataSetChanged();
                    }
                });
            }
        };

        service.isAnyChatOpened = true;

        // Ник в верхней панели
        this.nick_.setText(conference.nick);

        // Восстановление текста из сохранения
        String savedText = MessageSaveHelper.getMessage(this.SAVE_HASH);
        int cursorPosition = input.getSelectionStart();
        input.setText(savedText);

        if (!received_smile_tag.isEmpty()) {
            if (cursorPosition < 0) cursorPosition = 0;
            if (cursorPosition > input.length()) cursorPosition = input.length();
            StringBuilder builder = new StringBuilder(input.getText());
            builder.insert(cursorPosition, received_smile_tag);
            input.setText(builder.toString());
            cursorPosition += received_smile_tag.length();
            received_smile_tag = "";
        } else {
            cursorPosition = input.length();
        }

        input.setSelection(cursorPosition);

        if (PreferenceTable.auto_open_keyboard) {
            service.runOnUi(new Runnable() {
                @Override
                public void run() {
                    input.requestFocus();
                    input_manager.showSoftInput(input, 0);
                }
            }, 200L);
        }

        if (init_callback != null) {
            init_callback.chatInitialized();
            init_callback = null;
        }
    }

    @SuppressLint("SetTextI18n")
    public void drawReceiverData() {
        if (conference.isOnline()) {
            this.mainStatus.setImageDrawable(resources.jabber_conference);
        } else {
            this.mainStatus.setImageDrawable(resources.jabber_conference_offline);
        }
        this.xStatus.setVisibility(View.GONE);
        this.nickname.setText(JProtocol.getNameFromFullID(conference.JID) + " (" + conference.users.size() + ")");
        this.theme.setText(conference.theme);
    }

    @Override
    public void initViews() {
        super.initViews();

        // Views
        quot_view = (QuotingView) findViewById(R.id.chat_quoting_view);
        chat_back = (LinearLayout) findViewById(R.id.chat_back);
        mUsersToggleButton = (ImageView) findViewById(R.id.conf_users_toggle_button);
        mainStatus = (ImageView) findViewById(R.id.mainStatus);
        xStatus = (ImageView) findViewById(R.id.xStatus);
        nickname = (TextView) findViewById(R.id.nickname);
        userList = (ListView) findViewById(R.id.chat_conf_users);
        messageList = (ListViewA) findViewById(R.id.messages);
        input = (EditText) findViewById(R.id.input);
        send = (Button) findViewById(R.id.send);
        smileysSelectBtn = (Button) findViewById(R.id.chat_smiley_btn);
        nick_ = (TextView) findViewById(R.id.msg_nick);
        theme = (TextView) findViewById(R.id.encoding);
        TOP_PANEL = (LinearLayout) findViewById(R.id.chat_header);
        LinearLayout bottomPanel = (LinearLayout) findViewById(R.id.chat_bottom_panel);
        Button menuButton = (Button) findViewById(R.id.chat_menu_btn);
        if (utilities.hasHardwareMenuKey(getView().getContext())) {
            menuButton.setVisibility(View.GONE);
        }

        // User toggle button
        mUsersToggleButton.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(48), 2));
        mUsersToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleUserlistVisibility(true);
            }
        });

        // Nickname
        nickname.setTextColor(ColorScheme.getColor(12));

        // User list
        userList.setSelector(resources.getListSelector());

        // Message list
        messageList.setSelector(resources.getListSelector());
        messageList.setSlideEnabled(false);
        messageList.setUseCustomScrollControl(true);
        messageList.setOnItemLongClickListener(new cl());
        messageList.setDragDropEnabledA(PreferenceTable.ms_dragdrop_quoting);
        messageList.setOnMultitouchListener(new ListViewA.MultitouchListener() {
            @Override
            public void onStart(View view, int top) {
                quot_view.capture(view, top);
            }

            @Override
            public void onTouch(float x1, float y1) {
                int[] location = new int[2];
                input.getLocationOnScreen(location);
                boolean green = y1 > location[1];
                quot_view.updatePoints(x1, y1, green);
            }

            @Override
            public void onStop(float x1, float y1, int itemIdx) {
                quot_view.stop();
                int[] location = new int[2];
                input.getLocationOnScreen(location);
                if (y1 > location[1]) performQuote(itemIdx);
            }
        });
        messageList.setOnItemClickListener(new chat_click_listener());
        if (!PreferenceTable.chat_dividers) {
            messageList.setDivider(null);
        }

        // Input field
        input.setTextSize(PreferenceTable.chatTextSize);
        input.setTextColor(ColorScheme.getColor(46));
        input.addTextChangedListener(new el());
        input.setOnKeyListener(new inputKeyListener());
        input.setInputType(PreferenceTable.auto_cap ? 147457 : 131073);

        // Menu button
        resources.attachButtonStyle(menuButton);
        menuButton.setCompoundDrawables(resources.chat_menu_icon, null, null, null);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDialog(1);
                showDialog(1);
            }
        });

        // Send button
        send.setOnClickListener(new sndListener());

        // Smileys button
        smileysSelectBtn.setCompoundDrawables(resources.smileys_select_icon, null, null, null);
        smileysSelectBtn.setOnClickListener(new Chat.smileySelectBtnListener());

        // Chat style handling
        if (sp.getBoolean("ms_old_chat_style", true)) {
            nick_.setVisibility(View.GONE);
            resources.attachEditText(input);
        } else {
            input.setBackgroundResource(R.drawable.btn_default_transparent);
            input.setTextColor(Color.WHITE);
            smileysSelectBtn.setBackgroundResource(R.drawable.btn_default_transparent);
            send.setBackgroundResource(R.drawable.btn_default_transparent);
            menuButton.setBackgroundResource(R.drawable.btn_default_transparent);
            if (!resources.attachEditText(input)) {
                input.setTextColor(Color.WHITE);
            }
        }

        // Styles
        resources.attachButtonStyle(smileysSelectBtn);
        resources.attachButtonStyle(send);
        resources.attachButtonStyle(menuButton);

        // Theme
        if (!Manager.getBoolean("ms_conf_show_theme", true)) {
            theme.setVisibility(View.GONE);
        }

        // Top panel
        if (!TOP_PANEL_VISIBLED) {
            TOP_PANEL.setVisibility(View.GONE);
        }
        TOP_PANEL.setBackgroundColor(ColorScheme.getColor(11));

        // Bottom panel
        bottomPanel.setBackgroundColor(ColorScheme.getColor(9));

        // Backgrounds
        if (!getDefaultSharedPreferences().getBoolean("ms_use_solid_wallpaper", false)) {
            resources.attachChatMessagesBack(messageList);
            resources.attachChatMessagesBack(userList);
        }
        resources.attachChatTopPanel(TOP_PANEL);
        resources.attachChatBottomPanel(bottomPanel);

        // Text styles
        nick_.setTextSize(PreferenceTable.chatTextSize);
        nick_.setTextColor(ColorScheme.getColor(22));
        input.setTextSize(PreferenceTable.chatTextSize);

        // Arrows
        findViewById(R.id.chat_scroll_left).setVisibility(View.GONE);
        findViewById(R.id.chat_scroll_right).setVisibility(View.GONE);

        // Final state
        toggleUserlistVisibility(false);
    }

    public void performQuote(int idx) {
        if (this.chatAdp != null) {
            HistoryItem item = this.chatAdp.getItem(idx);
            String res = item.conf_nick + " [" + item.formattedDate + "]:\n" + item.message + "\n";
            int length = this.input.length();
            this.input.append((length > 0 ? "\n" : "") + res);
            this.input.setSelection(this.input.length());
        }
    }

    private class sndListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            JConference.this.doSend();
        }
    }

    public void doSend() {
        String message = this.input.getText().toString();
        if (!message.isEmpty()) {
            if (!this.input.getText().toString().trim().isEmpty() && conference.isOnline()) {
                ADB.proceedMessage(message);
                HistoryItem hst = new HistoryItem();
                hst.confirmed = true;
                hst.message = message;
                hst.conf_nick = conference.nick;
                hst.conf_profile = conference.profile;
                conference.sendMessage(hst.message);
                this.input.setText("");
                conference.profile.svc.playEvent(7);
                return;
            }
            return;
        }
        if (!resources.IT_IS_TABLET) {
            handleChatClosed();
        }
    }

    private void handleIncomingTextMessage(HistoryItem msg) {
        this.chatAdp.refreshList();
    }

    @Override
    public boolean handleMessage(Message msg) {
        BufferedDialog dialog;
        switch (msg.what) {
            case 2:
                drawReceiverData();
                break;
            case 4:
                try {
                    Object cli = msg.obj;
                    //noinspection ConditionCoveredByFurtherCondition
                    if (cli != null && (cli instanceof Conference) && cli.equals(conference)) {
                        handleIncomingTextMessage(null);
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
                break;
            case 5:
                try {
                    Object cli2 = msg.obj;
                    //noinspection ConditionCoveredByFurtherCondition
                    if (cli2 != null && (cli2 instanceof Conference) && cli2.equals(conference)) {
                        this.chatAdp.notifyDataSetChanged();
                        break;
                    }
                } catch (Exception e2) {
                    break;
                }
                break;
            case Chat.CLOSE /* 62 */:
                finish();
                break;
            case SHOW_VCARD:
                if (INITIALIZED) {
                    this.vcard_to_display = (VCard) msg.obj;
                    removeDialog(7);
                    showDialog(7);
                    break;
                }
                break;
            case SHOW_INFO:
                if (INITIALIZED && (dialog = (BufferedDialog) msg.obj) != null) {
                    this.dialog_for_display = dialog;
                    removeDialog(8);
                    showDialog(8);
                    break;
                }
                break;
            case Chat.CHAT_SHOW_MENU /* 97 */:
                removeDialog(1);
                showDialog(1);
                break;
            case SHOW_JABBER_FORM:
                final Operation op = (Operation) msg.obj;
                Dialog xform = DialogBuilder.createYesNo(this.ACTIVITY, op.form.form, 0, op.form.TITLE == null ? "Jabber form" : op.form.TITLE, Locale.getString("s_ok"), Locale.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        op.profile.stream.write(op.compile(), op.profile);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        op.profile.stream.write(op.compileCancel(), op.profile);
                    }
                });
                xform.show();
                break;
            case BANNED_LIST_RECEIVED /* 400 */:
                if (conference.mBannedList != null) {
                    Intent i = new Intent(this.ACTIVITY, BannedListActivity.class);
                    startActivity(i);
                    break;
                }
                break;
        }
        return false;
    }

    private class chatMenuListener implements AdapterView.OnItemClickListener {
        UAdapter menu;

        public chatMenuListener(UAdapter adp) {
            this.menu = adp;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            JConference.this.removeDialog(1);
            JConference.this.removeDialog(2);
            JConference.this.removeDialog(3);
            int id = (int) this.menu.getItemId(arg2);
            JConference.this.checkAndRunIdentificatedTask(id + 1024);
            switch (id) {
                case 0:
                    if (JConference.conference.isOnline()) {
                        JConference.conference.profile.logoutConference(JConference.conference.JID);
                        break;
                    }
                    break;
                case 1:
                    JConference.multiquoting = true;
                    JConference.this.messageList.setDragDropEnabled(false);
                    JConference.this.chatAdp.notifyDataSetChanged();
                    break;
                case 2:
                    JConference.multiquoting = false;
                    JConference.this.messageList.setDragDropEnabled(true);
                    JConference.this.resetSelection();
                    JConference.this.chatAdp.notifyDataSetChanged();
                    break;
                case 3:
                    JConference.this.removeDialog(4);
                    JConference.this.showDialog(4);
                    break;
                case 4:
                    JConference.this.removeDialog(5);
                    JConference.this.showDialog(5);
                    break;
                case 5:
                    JConference.conference.profile.joinConference(JConference.conference.JID, JConference.conference.nick, JConference.conference.pass);
                    break;
                case 7:
                    JConference.this.removeDialog(2);
                    JConference.this.showDialog(2);
                    break;
                case 8:
                    JConference.this.removeDialog(9);
                    JConference.this.showDialog(9);
                    break;
                case 9:
                    JConference.conference.doRequestBannedList();
                    break;
                case 10:
                    JConference.conference.showRoomPreferences();
                    break;
                case 13:
                    HistoryItem item = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    //noinspection deprecation
                    ClipboardManager cm = (ClipboardManager) JConference.this.getSystemService("clipboard");
                    //noinspection deprecation
                    cm.setText(item.conf_nick + " [" + item.formattedDate + "]:\n" + item.message + "\n");
                    Toast msg = Toast.makeText(JConference.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg.setGravity(48, 0, 0);
                    msg.show();
                    break;
                case 14:
                    HistoryItem item2 = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    String res = item2.conf_nick + " [" + item2.formattedDate + "]:\n" + item2.message + "\n";
                    JConference.this.input.setText(res);
                    JConference.this.input.setSelection(res.length(), res.length());
                    break;
                case 17:
                    HistoryItem item3 = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    Log.e("JChatActivity", String.valueOf(item3 == null));
                    //noinspection DataFlowIssue
                    JConference.this.input.setText(item3.message + "\n");
                    JConference.this.input.setSelection(item3.message.length());
                    break;
                case 18:
                    HistoryItem item4 = JConference.this.chatAdp.getItem(JConference.this.last_context_message);
                    //noinspection deprecation
                    ClipboardManager cm2 = (ClipboardManager) JConference.this.getSystemService("clipboard");
                    //noinspection deprecation
                    cm2.setText(item4.message + "\n");
                    Toast msg2 = Toast.makeText(JConference.service, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                    msg2.setGravity(48, 0, 0);
                    msg2.show();
                    break;
            }
        }
    }

    public void resetSelection() {
        for (int i = 0; i < this.chatAdp.getCount(); i++) {
            this.chatAdp.getItem(i).selected = false;
        }
    }

    public String computeMultiQuote() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < this.chatAdp.getCount(); i++) {
            HistoryItem hst = this.chatAdp.getItem(i);
            if (hst.selected) {
                res.append(hst.conf_nick).append(" [").append(hst.formattedDate).append("]:\n").append(hst.message).append("\n");
            }
        }
        return res.toString();
    }

    private class chat_click_listener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            ConferenceAdapter adp = (ConferenceAdapter) arg0.getAdapter();
            if (JConference.multiquoting) {
                HistoryItem hst = adp.getItem(arg2);
                hst.selected = !hst.selected;
                JConference.this.chatAdp.notifyDataSetChanged();
                String quote = JConference.this.computeMultiQuote();
                JConference.this.input.setText(quote);
                JConference.this.input.setSelection(quote.length(), quote.length());
                return;
            }
            HistoryItem hst2 = adp.getItem(arg2);
            if (!hst2.isTheme) {
                if (JConference.this.input.length() > 0) {
                    String append = JConference.this.input.getText().toString().endsWith(" ") ? "" : " ";
                    JConference.this.input.append(append + hst2.conf_nick + " ");
                } else {
                    JConference.this.input.append(hst2.conf_nick + ": ");
                }
            }
        }
    }

    private class el implements TextWatcher {
        private String buffer = "";

        public el() {
            if (!resources.IT_IS_TABLET) {
                JConference.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
            } else {
                JConference.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
            }
        }

        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!this.buffer.equals(s.toString()) && s.length() != 0) {
                if (s.length() - this.buffer.length() == 1) {
                    ADB.symbolTyped();
                }
                this.buffer = s.toString();
            }
            if (JConference.conference != null) {
                if (s.length() > 0) {
                    JConference.this.send.setCompoundDrawables(resources.send_msg_icon, null, null, null);
                } else if (!resources.IT_IS_TABLET) {
                    JConference.this.send.setCompoundDrawables(resources.back_to_cl_icon, null, null, null);
                }
            }
        }
    }

    private class inputKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View arg0, int keyCode, KeyEvent action) {
            if (keyCode != 66 || !JConference.this.sendByEnter || action.getAction() != 0) {
                return false;
            }
            JConference.this.doSend();
            return true;
        }
    }

    private class cl implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            JConference.this.last_context_message = arg2;
            JConference.this.removeDialog(3);
            JConference.this.showDialog(3);
            return false;
        }
    }
}