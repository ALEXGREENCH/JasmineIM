package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import ru.ivansuper.jasmin.MMP.MMPProfile;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.dns.DNS;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * Activity for managing user profiles.
 * Allows users to add, edit, delete, and reorder profiles for various instant messaging services.
 * <p>
 * This activity interacts with a {@link jasminSvc} to perform profile operations and update the UI accordingly.
 * It uses a {@link ProfilesAdapter} to display the list of profiles in a {@link ListView}.
 * Dialogs are used for user interaction when adding, editing, or deleting profiles.
 * </p>
 * <p>
 * Supported profile types include:
 * <ul>
 *     <li>ICQ</li>
 *     <li>Jabber (XMPP)</li>
 *     <li>VK (V Kontakte)</li>
 *     <li>Yandex</li>
 *     <li>Mail.Ru Agent (MMP)</li>
 *     <li>QIP</li>
 *     <li>GTalk</li>
 * </ul>
 * </p>
 * <p>
 * Key functionalities:
 * <ul>
 *     <li>Displaying a list of configured profiles.</li>
 *     <li>Adding new profiles of different types.</li>
 *     <li>Editing existing profile details (credentials, server settings, etc.).</li>
 *     <li>Deleting profiles.</li>
 *     <li>Reordering profiles in the list.</li>
 *     <li>Handling back button press to save changes and update the contact list.</li>
 *     <li>Displaying a menu for adding new profiles (conditionally shown based on hardware menu key presence).</li>
 * </ul>
 * </p>
 */
public class ProfilesActivity extends Activity {

    @SuppressWarnings("unused")
    private ServiceConnection jasminSvcCnt;
    private ProfilesAdapter pa;
    @SuppressWarnings("FieldCanBeLocal")
    private ListView profiles;
    public int selectedIdx = -1;
    private jasminSvc service;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(3);
        setContentView(R.layout.profiles);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
        this.service = resources.service;
        handleServiceConnected();

        Button menuButton = findViewById(R.id.menu_btn);
        if (utilities.hasHardwareMenuKey(this)) {
            menuButton.setVisibility(View.GONE);
        } else {
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeDialog(4);
                    showDialog(4);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* todo;
        //if (this.jasminSvcCnt != null) {
        }*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    removeDialog(4);
                    showDialog(4);
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    handleBackButton();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleBackButton() {
        if (service != null) {
            // Обработка нажатия кнопки "Назад"
            service.handleContactlistNeedRemake();
            service.handleProfileChanged();
        }
        finish();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                //noinspection UnnecessaryLocalVariable
                Dialog ad = DialogBuilder.createYesNo(this, Gravity.TOP, resources.getString("s_delete_profile"), this.pa.getItem(this.selectedIdx).id + ":\n" + resources.getString("s_are_you_sure"), resources.getString("s_yes"), resources.getString("s_no"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(0);
                        Toast.makeText(ProfilesActivity.this, resources.getString("s_deleting_successful"), Toast.LENGTH_SHORT).show();
                        service.profiles.getProfiles().remove(selectedIdx).disconnect();
                        pa.remove(selectedIdx);
                        service.profiles.writeProfilesToFile();
                        service.handleProfileChanged();
                        service.handleContactlistCheckConferences();
                        service.handleContactlistNeedRemake();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(0);
                    }
                });
                return ad;
            case 1:
                @SuppressLint("InflateParams")
                LinearLayout lay = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.icq_profile_add, null);
                ((TextView) lay.findViewById(R.id.l1)).setText(resources.getString("s_icq_dialog_login"));
                ((TextView) lay.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText login_edit = lay.findViewById(R.id.icq_profile_add_uin);
                resources.attachEditText(login_edit);
                final EditText password_edit = lay.findViewById(R.id.icq_profile_add_pass);
                resources.attachEditText(password_edit);
                final CheckBox enabled = lay.findViewById(R.id.icq_profile_add_enabled);
                enabled.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect = lay.findViewById(R.id.icq_profile_add_autoconnect);
                autoconnect.setText(resources.getString("s_dialog_autologin"));
                login_edit.setText(this.pa.getItem(this.selectedIdx).id);
                password_edit.setText(this.pa.getItem(this.selectedIdx).pass);
                enabled.setChecked(this.pa.getItem(this.selectedIdx).enabled);
                autoconnect.setChecked(this.pa.getItem(this.selectedIdx).autoconnect);
                //noinspection UnnecessaryLocalVariable
                Dialog ad2 = DialogBuilder.createYesNo(this, lay, Gravity.TOP, resources.getString("s_profile_changing"), resources.getString("s_change"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = pa.getItem(selectedIdx);
                        String uin = login_edit.getText().toString().trim();
                        if (!utilities.isUIN(uin) && !utilities.isEmail(uin)) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_1"), Toast.LENGTH_SHORT).show();
                        } else if (uin.length() >= 3) {
                            pdata.id = uin;
                            String pass = password_edit.getText().toString();
                            if (pass.length() >= 3) {
                                pdata.pass = pass;
                                pdata.autoconnect = autoconnect.isChecked();
                                pdata.enabled = enabled.isChecked();
                                pa.notifyDataSetChanged();
                                ((ICQProfile) service.profiles.getProfiles().get(selectedIdx)).reinitParams(pdata);
                                service.profiles.writeProfilesToFile();
                                service.handleProfileChanged();
                                service.handleContactlistNeedRemake();
                                removeDialog(1);
                            }
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(1);
                    }
                });
                return ad2;
            case 2:
                final UAdapter adp_ = new UAdapter();
                adp_.setMode(2);
                adp_.setTextSize(18);
                adp_.setPadding(15);
                if (this.selectedIdx > 0) {
                    adp_.put(resources.getString("s_move_profile_up"), 3);
                }
                if (this.selectedIdx < this.service.profiles.getProfilesCount() - 1) {
                    adp_.put(resources.getString("s_move_profile_down"), 2);
                }
                adp_.put(resources.getString("s_change_profile"), 0);
                adp_.put(resources.getString("s_delete_profile"), 1);
                //noinspection UnnecessaryLocalVariable
                Dialog ad3 = DialogBuilder.createWithNoHeader(this, adp_, Gravity.TOP, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        removeDialog(2);
                        switch ((int) adp_.getItemId(i)) {
                            case 0:
                                switch (pa.getItem(selectedIdx).profile_type) {
                                    case 0:
                                        removeDialog(1);
                                        showDialog(1);
                                        return;
                                    case 1:
                                        switch (pa.getItem(selectedIdx).proto_type) {
                                            case 0:
                                                removeDialog(6);
                                                showDialog(6);
                                                return;
                                            case 1:
                                                removeDialog(8);
                                                showDialog(8);
                                                return;
                                            case 2:
                                                removeDialog(10);
                                                showDialog(10);
                                                return;
                                            case 3:
                                                removeDialog(16);
                                                showDialog(16);
                                                return;
                                            case 4:
                                                removeDialog(14);
                                                showDialog(14);
                                                return;
                                            default:
                                                return;
                                        }
                                    case 2:
                                        removeDialog(12);
                                        showDialog(12);
                                        return;
                                    case 3:
                                        removeDialog(18);
                                        showDialog(18);
                                        return;
                                    default:
                                        return;
                                }
                            case 1:
                                removeDialog(0);
                                showDialog(0);
                                return;
                            case 2:
                                Vector<IMProfile> profiles = service.profiles.getProfiles();
                                IMProfile profile = profiles.remove(selectedIdx);
                                profiles.add(selectedIdx + 1, profile);
                                fillFromProfilesManager();
                                service.profiles.writeProfilesToFile();
                                service.handleProfileChanged();
                                service.handleContactlistNeedRemake();
                                return;
                            case 3:
                                Vector<IMProfile> profiles2 = service.profiles.getProfiles();
                                IMProfile profile2 = profiles2.remove(selectedIdx);
                                profiles2.add(selectedIdx - 1, profile2);
                                fillFromProfilesManager();
                                service.profiles.writeProfilesToFile();
                                service.handleProfileChanged();
                                service.handleContactlistNeedRemake();
                                return;
                            default:
                        }
                    }
                });
                return ad3;
            case 3:
                @SuppressLint("InflateParams")
                LinearLayout lay2 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.icq_profile_add, null);
                ((TextView) lay2.findViewById(R.id.l1)).setText(resources.getString("s_icq_dialog_login"));
                ((TextView) lay2.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText login_edit1 = lay2.findViewById(R.id.icq_profile_add_uin);
                resources.attachEditText(login_edit1);
                final EditText password_edit1 = lay2.findViewById(R.id.icq_profile_add_pass);
                resources.attachEditText(password_edit1);
                final CheckBox enabled1 = lay2.findViewById(R.id.icq_profile_add_enabled);
                enabled1.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect1 = lay2.findViewById(R.id.icq_profile_add_autoconnect);
                autoconnect1.setText(resources.getString("s_dialog_autologin"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad4 = DialogBuilder.createYesNo(this, lay2, Gravity.TOP, resources.getString("s_profile_adding"), resources.getString("s_do_add"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = new ProfilesAdapterItem();
                        pdata.profile_type = 0;
                        String uin = login_edit1.getText().toString().trim();
                        if (utilities.isUIN(uin) || utilities.isEmail(uin)) {
                            if (!service.profiles.isProfileAlreadyExist(uin) && uin.length() >= 3) {
                                pdata.id = uin;
                                String pass = password_edit1.getText().toString();
                                if (pass.length() >= 3) {
                                    pdata.pass = pass;
                                    pdata.autoconnect = autoconnect1.isChecked();
                                    pdata.enabled = enabled1.isChecked();
                                    pa.add(pdata);
                                    ICQProfile profile = new ICQProfile(pdata.id, pdata.pass, service, pdata.autoconnect, pdata.enabled);
                                    service.profiles.addProfile(profile);
                                    service.profiles.writeProfilesToFile();
                                    service.handleProfileChanged();
                                    service.handleContactlistCheckConferences();
                                    service.handleContactlistNeedRemake();
                                    removeDialog(3);
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                        Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_1"), Toast.LENGTH_SHORT).show();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(3);
                    }
                });
                return ad4;
            case 4:
                final UAdapter adp = new UAdapter();
                adp.setPadding(10);
                adp.setTextColor(-1);
                adp.put(this.service.getResources().getDrawable(R.drawable.icq_status_online), resources.getString("s_profile_type_icq"), 0);
                adp.put(this.service.getResources().getDrawable(R.drawable.mrim_contact_status_online), resources.getString("s_profile_type_mail"), 4);
                adp.put(this.service.getResources().getDrawable(R.drawable.xmpp), resources.getString("s_profile_type_jabber"), 1);
                adp.put(this.service.getResources().getDrawable(R.drawable.vk_online), resources.getString("s_profile_type_vk"), 2);
                adp.put(this.service.getResources().getDrawable(R.drawable.ya_online), resources.getString("s_profile_type_yandex"), 3);
                adp.put(this.service.getResources().getDrawable(R.drawable.qip_online), resources.getString("s_profile_type_qip"), 5);
                adp.put(this.service.getResources().getDrawable(R.drawable.gtalk_online), resources.getString("s_profile_type_gtalk"), 6);
                //noinspection UnnecessaryLocalVariable
                Dialog ad5 = DialogBuilder.create(this, resources.getString("s_add_profile"), adp, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        removeDialog(4);
                        switch ((int) adp.getItemId(i)) {
                            case 0:
                                removeDialog(3);
                                showDialog(3);
                                return;
                            case 1:
                                removeDialog(5);
                                showDialog(5);
                                return;
                            case 2:
                                removeDialog(7);
                                showDialog(7);
                                return;
                            case 3:
                                removeDialog(9);
                                showDialog(9);
                                return;
                            case 4:
                                removeDialog(11);
                                showDialog(11);
                                return;
                            case 5:
                                removeDialog(13);
                                showDialog(13);
                                return;
                            case 6:
                                removeDialog(15);
                                showDialog(15);
                                return;
                            default:
                        }
                    }
                });
                return ad5;
            case 5:
                @SuppressLint("InflateParams")
                LinearLayout lay3 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_profile_add, null);
                ((TextView) lay3.findViewById(R.id.l1)).setText(resources.getString("s_jabber_dialog_login"));
                ((TextView) lay3.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                ((TextView) lay3.findViewById(R.id.l3)).setText(resources.getString("s_jabber_dialog_server"));
                ((TextView) lay3.findViewById(R.id.l4)).setText(resources.getString("s_jabber_dialog_port"));
                Button detect_srv = lay3.findViewById(R.id.detect_srv_btn);
                resources.attachButtonStyle(detect_srv);
                detect_srv.setText(Locale.getString("s_detect"));
                final EditText jid = lay3.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid);
                final EditText password = lay3.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password);
                final EditText server = lay3.findViewById(R.id.xmpp_profile_add_server);
                resources.attachEditText(server);
                final EditText port = lay3.findViewById(R.id.xmpp_profile_add_port);
                resources.attachEditText(port);
                final CheckBox enabled2 = lay3.findViewById(R.id.xmpp_profile_add_enabled);
                enabled2.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect2 = lay3.findViewById(R.id.xmpp_profile_add_autoconnect);
                final CheckBox zlib = lay3.findViewById(R.id.xmpp_profile_add_zlib);
                final CheckBox tls = lay3.findViewById(R.id.xmpp_profile_add_tls);
                autoconnect2.setText(resources.getString("s_dialog_autologin"));
                zlib.setText(resources.getString("s_dialog_compression"));
                tls.setText(resources.getString("s_dialog_tls"));
                detect_srv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String JID = jid.getText().toString().toLowerCase().trim();
                        String[] parts = JID.split("@");
                        if (parts.length == 2) {
                            final Dialog progress = DialogBuilder.createProgress(ProfilesActivity.this, Locale.getString("s_please_wait"), true);
                            progress.show();
                            String str = parts[1];
                            //noinspection UnnecessaryLocalVariable
                            final EditText editText = server;
                            DNS.resolve(str, new DNS.DNSListener() {
                                @Override
                                public void onResult(String server_) {
                                    editText.setText(server_);
                                    progress.dismiss();
                                }
                            });
                        }
                    }
                });
                //noinspection UnnecessaryLocalVariable
                Dialog ad6 = DialogBuilder.createYesNo(this, lay3, Gravity.TOP, resources.getString("s_profile_adding"), resources.getString("s_do_add"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = new ProfilesAdapterItem();
                        pdata.profile_type = 1;
                        String JID = jid.getText().toString().toLowerCase().trim();
                        String[] parts = JID.split("@");
                        if (parts.length == 2) {
                            if (service.profiles.isProfileAlreadyExist(JID)) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_3"), Toast.LENGTH_SHORT).show();
                                return;
                            } else if (JID.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_2"), Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                pdata.id = parts[0];
                                pdata.host = parts[1];
                                String pass = password.getText().toString();
                                if (pass.length() < 2) {
                                    Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                pdata.pass = pass;
                                String Server = server.getText().toString().trim();
                                if (Server.length() < 2) {
                                    Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_5"), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                pdata.server = Server;
                                String Port = port.getText().toString().trim();
                                if (Port.length() < 2) {
                                    Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_6"), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                pdata.enabled = enabled2.isChecked();
                                pdata.port = Integer.parseInt(Port);
                                pdata.autoconnect = autoconnect2.isChecked();
                                pdata.compression = zlib.isChecked();
                                pdata.tls = tls.isChecked();
                                pdata.proto_type = 0;
                                pa.add(pdata);
                                JProfile profile = new JProfile(service, parts[0], parts[1], pdata.server, pdata.port, pdata.pass, null, pdata.compression, pdata.tls, pdata.sasl, pdata.autoconnect, pdata.enabled, pdata.proto_type);
                                service.profiles.addProfile(profile);
                                service.profiles.writeProfilesToFile();
                                service.handleProfileChanged();
                                service.handleContactlistCheckConferences();
                                service.handleContactlistNeedRemake();
                                removeDialog(5);
                                return;
                            }
                        }
                        Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_2"), Toast.LENGTH_SHORT).show();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(5);
                    }
                });
                return ad6;
            case 6:
                @SuppressLint("InflateParams")
                LinearLayout lay4 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_profile_add, null);
                ((TextView) lay4.findViewById(R.id.l1)).setText(resources.getString("s_jabber_dialog_login"));
                ((TextView) lay4.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                ((TextView) lay4.findViewById(R.id.l3)).setText(resources.getString("s_jabber_dialog_server"));
                ((TextView) lay4.findViewById(R.id.l4)).setText(resources.getString("s_jabber_dialog_port"));
                Button detect_srv3 = lay4.findViewById(R.id.detect_srv_btn);
                resources.attachButtonStyle(detect_srv3);
                detect_srv3.setText(Locale.getString("s_detect"));
                final EditText jid3 = lay4.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid3);
                final EditText password3 = lay4.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password3);
                final EditText server3 = lay4.findViewById(R.id.xmpp_profile_add_server);
                resources.attachEditText(server3);
                final EditText port3 = lay4.findViewById(R.id.xmpp_profile_add_port);
                resources.attachEditText(port3);
                final CheckBox enabled3 = lay4.findViewById(R.id.xmpp_profile_add_enabled);
                enabled3.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect3 = lay4.findViewById(R.id.xmpp_profile_add_autoconnect);
                final CheckBox zlib3 = lay4.findViewById(R.id.xmpp_profile_add_zlib);
                final CheckBox tls3 = lay4.findViewById(R.id.xmpp_profile_add_tls);
                autoconnect3.setText(resources.getString("s_dialog_autologin"));
                zlib3.setText(resources.getString("s_dialog_compression"));
                tls3.setText(resources.getString("s_dialog_tls"));
                jid3.setText(pa.getItem(selectedIdx).id + "@" + pa.getItem(selectedIdx).host);
                password3.setText(this.pa.getItem(this.selectedIdx).pass);
                server3.setText(this.pa.getItem(this.selectedIdx).server);
                port3.setText(String.valueOf(this.pa.getItem(this.selectedIdx).port));
                enabled3.setChecked(this.pa.getItem(this.selectedIdx).enabled);
                autoconnect3.setChecked(this.pa.getItem(this.selectedIdx).autoconnect);
                zlib3.setChecked(this.pa.getItem(this.selectedIdx).compression);
                tls3.setChecked(this.pa.getItem(this.selectedIdx).tls);
                detect_srv3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String JID = jid3.getText().toString().toLowerCase().trim();
                        String[] parts = JID.split("@");
                        if (parts.length == 2) {
                            final Dialog progress = DialogBuilder.createProgress(ProfilesActivity.this, Locale.getString("s_please_wait"), true);
                            progress.show();
                            String str = parts[1];
                            //noinspection UnnecessaryLocalVariable
                            final EditText editText = server3;
                            DNS.resolve(str, new DNS.DNSListener() {
                                @Override
                                public void onResult(String server_) {
                                    editText.setText(server_);
                                    progress.dismiss();
                                }
                            });
                        }
                    }
                });
                //noinspection UnnecessaryLocalVariable
                Dialog ad7 = DialogBuilder.createYesNo(this, lay4, Gravity.TOP, resources.getString("s_profile_changing"), resources.getString("s_change"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = pa.getItem(selectedIdx);
                        pdata.profile_type = 1;
                        String JID = jid3.getText().toString().toLowerCase().trim();
                        String[] parts = JID.split("@");
                        if (parts.length != 2) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_2"), Toast.LENGTH_SHORT).show();
                        } else if (JID.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_2"), Toast.LENGTH_SHORT).show();
                        } else {
                            pdata.id = parts[0];
                            pdata.host = parts[1];
                            String pass = password3.getText().toString();
                            if (pass.length() < 2) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.pass = pass;
                            String Server = server3.getText().toString().trim();
                            if (Server.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_5"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String Port = port3.getText().toString().trim();
                            if (Port.length() < 2) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_6"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.enabled = enabled3.isChecked();
                            pdata.port = Integer.parseInt(Port);
                            pdata.server = Server;
                            pdata.autoconnect = autoconnect3.isChecked();
                            pdata.compression = zlib3.isChecked();
                            pdata.tls = tls3.isChecked();
                            pa.notifyDataSetChanged();
                            ((JProfile) service.profiles.getProfiles().get(selectedIdx)).reinitParams(pdata);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistNeedRemake();
                            removeDialog(6);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(6);
                    }
                });
                return ad7;
            case 7:
                @SuppressLint("InflateParams")
                LinearLayout lay5 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_vk_profile_add, null);
                ((TextView) lay5.findViewById(R.id.l1)).setText(resources.getString("s_vk_dialog_login"));
                ((TextView) lay5.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid4 = lay5.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid4);
                final EditText password4 = lay5.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password4);
                final CheckBox enabled4 = lay5.findViewById(R.id.xmpp_profile_add_enabled);
                enabled4.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect4 = lay5.findViewById(R.id.xmpp_profile_add_autoconnect);
                final CheckBox sasl4 = lay5.findViewById(R.id.xmpp_profile_add_sasl);
                final CheckBox tls4 = lay5.findViewById(R.id.xmpp_profile_add_tls);
                autoconnect4.setText(resources.getString("s_dialog_autologin"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad8 = DialogBuilder.createYesNo(this, lay5, Gravity.TOP, resources.getString("s_profile_adding"), resources.getString("s_do_add"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = new ProfilesAdapterItem();
                        pdata.profile_type = 1;
                        String JID = jid4.getText().toString().toLowerCase().trim();
                        if (service.profiles.isProfileAlreadyExist(JID + "@vk.com")) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_3"), Toast.LENGTH_SHORT).show();
                        } else if (JID.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_1"), Toast.LENGTH_SHORT).show();
                        } else {
                            pdata.id = JID;
                            String pass = password4.getText().toString();
                            if (pass.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.enabled = enabled4.isChecked();
                            pdata.pass = pass;
                            pdata.server = "vkmessenger.com";
                            pdata.autoconnect = autoconnect4.isChecked();
                            pdata.sasl = sasl4.isChecked();
                            pdata.tls = tls4.isChecked();
                            pdata.proto_type = 1;
                            pa.add(pdata);
                            JProfile profile = new JProfile(service, pdata.id, "vk.com", pdata.server, 5222, pdata.pass, null, pdata.compression, pdata.tls, pdata.sasl, pdata.autoconnect, pdata.enabled, pdata.proto_type);
                            service.profiles.addProfile(profile);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistCheckConferences();
                            service.handleContactlistNeedRemake();
                            removeDialog(7);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(7);
                    }
                });
                return ad8;
            case 8:
                @SuppressLint("InflateParams")
                LinearLayout lay6 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_vk_profile_add, null);
                ((TextView) lay6.findViewById(R.id.l1)).setText(resources.getString("s_vk_dialog_login"));
                ((TextView) lay6.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid5 = lay6.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid5);
                final EditText password5 = lay6.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password5);
                final CheckBox enabled5 = lay6.findViewById(R.id.xmpp_profile_add_enabled);
                enabled5.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect5 = lay6.findViewById(R.id.xmpp_profile_add_autoconnect);
                final CheckBox sasl5 = lay6.findViewById(R.id.xmpp_profile_add_sasl);
                final CheckBox tls5 = lay6.findViewById(R.id.xmpp_profile_add_tls);
                enabled5.setChecked(this.pa.getItem(this.selectedIdx).enabled);
                autoconnect5.setText(resources.getString("s_dialog_autologin"));
                String[] parts = this.pa.getItem(this.selectedIdx).id.split("@");
                jid5.setText(parts[0]);
                password5.setText(this.pa.getItem(this.selectedIdx).pass);
                autoconnect5.setChecked(this.pa.getItem(this.selectedIdx).autoconnect);
                //noinspection UnnecessaryLocalVariable
                Dialog ad9 = DialogBuilder.createYesNo(this, lay6, Gravity.TOP, resources.getString("s_profile_changing"), resources.getString("s_change"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = pa.getItem(selectedIdx);
                        pdata.profile_type = 1;
                        String JID = jid5.getText().toString().toLowerCase().trim();
                        if (JID.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_1"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pdata.id = JID;
                        pdata.host = "vk.com";
                        String pass = password5.getText().toString();
                        if (pass.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pdata.enabled = enabled5.isChecked();
                        pdata.pass = pass;
                        pdata.autoconnect = autoconnect5.isChecked();
                        pdata.sasl = sasl5.isChecked();
                        pdata.tls = tls5.isChecked();
                        pa.notifyDataSetChanged();
                        ((JProfile) service.profiles.getProfiles().get(selectedIdx)).reinitParams(pdata);
                        service.profiles.writeProfilesToFile();
                        service.handleProfileChanged();
                        service.handleContactlistNeedRemake();
                        removeDialog(8);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(8);
                    }
                });
                return ad9;
            case 9:
                @SuppressLint("InflateParams")
                LinearLayout lay7 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_ya_profile_add, null);
                ((TextView) lay7.findViewById(R.id.l1)).setText(resources.getString("s_ya_dialog_login"));
                ((TextView) lay7.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid7 = lay7.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid7);
                final EditText password7 = lay7.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password7);
                final CheckBox enabled7 = lay7.findViewById(R.id.xmpp_profile_add_enabled);
                enabled7.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect7 = lay7.findViewById(R.id.xmpp_profile_add_autoconnect);
                final CheckBox tls7 = lay7.findViewById(R.id.xmpp_profile_add_tls);
                final CheckBox zlib7 = lay7.findViewById(R.id.xmpp_profile_add_zlib);
                autoconnect7.setText(resources.getString("s_dialog_autologin"));
                zlib7.setText(resources.getString("s_dialog_compression"));
                tls7.setText(resources.getString("s_dialog_tls"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad10 = DialogBuilder.createYesNo(this, lay7, Gravity.TOP, resources.getString("s_profile_adding"), resources.getString("s_do_add"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = new ProfilesAdapterItem();
                        pdata.profile_type = 1;
                        String JID = jid7.getText().toString().toLowerCase().trim();
                        if (service.profiles.isProfileAlreadyExist(JID)) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_3"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String[] parts2 = JID.split("@");
                        if (parts2.length != 2 || parts2[1].split("\\.").length < 2) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else if (JID.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else {
                            pdata.id = parts2[0];
                            pdata.host = parts2[1];
                            String pass = password7.getText().toString();
                            if (pass.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.enabled = enabled7.isChecked();
                            pdata.pass = pass;
                            pdata.server = "xmpp.yandex.ru";
                            pdata.autoconnect = autoconnect7.isChecked();
                            pdata.compression = zlib7.isChecked();
                            pdata.tls = tls7.isChecked();
                            pdata.proto_type = 2;
                            pa.add(pdata);
                            JProfile profile = new JProfile(service, pdata.id, pdata.host, pdata.server, 5222, pdata.pass, null, pdata.compression, pdata.tls, pdata.sasl, pdata.autoconnect, pdata.enabled, pdata.proto_type);
                            service.profiles.addProfile(profile);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistCheckConferences();
                            service.handleContactlistNeedRemake();
                            removeDialog(9);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(9);
                    }
                });
                return ad10;
            case 10:
                @SuppressLint("InflateParams")
                LinearLayout lay8 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_ya_profile_add, null);
                ((TextView) lay8.findViewById(R.id.l1)).setText(resources.getString("s_ya_dialog_login"));
                ((TextView) lay8.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid6 = lay8.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid6);
                final EditText password6 = lay8.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password6);
                final CheckBox enabled6 = lay8.findViewById(R.id.xmpp_profile_add_enabled);
                enabled6.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect6 = lay8.findViewById(R.id.xmpp_profile_add_autoconnect);
                jid6.setText(this.pa.getItem(this.selectedIdx).id + "@" + this.pa.getItem(this.selectedIdx).host);
                password6.setText(this.pa.getItem(this.selectedIdx).pass);
                final CheckBox tls6 = lay8.findViewById(R.id.xmpp_profile_add_tls);
                tls6.setChecked(this.pa.getItem(this.selectedIdx).tls);
                final CheckBox zlib6 = lay8.findViewById(R.id.xmpp_profile_add_zlib);
                zlib6.setChecked(this.pa.getItem(this.selectedIdx).compression);
                enabled6.setChecked(this.pa.getItem(this.selectedIdx).enabled);
                autoconnect6.setText(resources.getString("s_dialog_autologin"));
                zlib6.setText(resources.getString("s_dialog_compression"));
                tls6.setText(resources.getString("s_dialog_tls"));
                autoconnect6.setChecked(this.pa.getItem(this.selectedIdx).autoconnect);
                //noinspection UnnecessaryLocalVariable
                Dialog ad11 = DialogBuilder.createYesNo(this, lay8, Gravity.TOP, resources.getString("s_profile_changing"), resources.getString("s_change"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = pa.getItem(selectedIdx);
                        pdata.profile_type = 1;
                        String JID = jid6.getText().toString().toLowerCase().trim();
                        String[] parts2 = JID.split("@");
                        if (parts2.length != 2 || parts2[1].split("\\.").length < 2) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else if (JID.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else {
                            pdata.id = parts2[0];
                            pdata.host = parts2[1];
                            String pass = password6.getText().toString();
                            if (pass.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.enabled = enabled6.isChecked();
                            pdata.tls = tls6.isChecked();
                            pdata.compression = zlib6.isChecked();
                            pdata.pass = pass;
                            pdata.autoconnect = autoconnect6.isChecked();
                            pa.notifyDataSetChanged();
                            ((JProfile) service.profiles.getProfiles().get(selectedIdx)).reinitParams(pdata);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistNeedRemake();
                            removeDialog(10);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(10);
                    }
                });
                return ad11;
            case 11:
                @SuppressLint("InflateParams")
                LinearLayout lay9 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.mrim_profile_add, null);
                ((TextView) lay9.findViewById(R.id.l1)).setText(resources.getString("s_mrim_dialog_login"));
                ((TextView) lay9.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                ((TextView) lay9.findViewById(R.id.l3)).setText(resources.getString("s_mrim_dialog_warning"));
                final EditText mmp_login_edit = lay9.findViewById(R.id.mrim_profile_add_email);
                resources.attachEditText(mmp_login_edit);
                final EditText mmp_password_edit = lay9.findViewById(R.id.mrim_profile_add_pass);
                resources.attachEditText(mmp_password_edit);
                final CheckBox mmp_enabled = lay9.findViewById(R.id.mrim_profile_add_enabled);
                mmp_enabled.setText(Locale.getString("s_profile_enabled"));
                final CheckBox mmp_autoconnect = lay9.findViewById(R.id.mrim_profile_add_autoconnect);
                mmp_autoconnect.setText(resources.getString("s_dialog_autologin"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad12 = DialogBuilder.createYesNo(this, lay9, Gravity.TOP, resources.getString("s_profile_adding"), resources.getString("s_do_add"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = new ProfilesAdapterItem();
                        pdata.profile_type = 2;
                        String ID = mmp_login_edit.getText().toString().toLowerCase().trim();
                        if (!utilities.isMrim(ID)) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else if (ID.length() >= 7) {
                            if (service.profiles.isProfileAlreadyExist(ID)) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_3"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.id = ID;
                            String pass = mmp_password_edit.getText().toString();
                            if (pass.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.enabled = mmp_enabled.isChecked();
                            pdata.pass = pass;
                            pdata.autoconnect = mmp_autoconnect.isChecked();
                            pa.add(pdata);
                            MMPProfile mmp_profile = new MMPProfile(service, pdata.id, pdata.pass, pdata.autoconnect, pdata.enabled);
                            service.profiles.addProfile(mmp_profile);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistNeedRemake();
                            removeDialog(11);
                        } else {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(11);
                    }
                });
                return ad12;
            case 12:
                @SuppressLint("InflateParams")
                LinearLayout lay10 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.mrim_profile_add, null);
                ((TextView) lay10.findViewById(R.id.l1)).setText(resources.getString("s_mrim_dialog_login"));
                ((TextView) lay10.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                ((TextView) lay10.findViewById(R.id.l3)).setText(resources.getString("s_mrim_dialog_warning"));
                ProfilesAdapterItem item = this.pa.getItem(this.selectedIdx);
                final EditText mmp_login_edit2 = lay10.findViewById(R.id.mrim_profile_add_email);
                resources.attachEditText(mmp_login_edit2);
                mmp_login_edit2.setText(item.id);
                final EditText mmp_password_edit2 = lay10.findViewById(R.id.mrim_profile_add_pass);
                resources.attachEditText(mmp_password_edit2);
                mmp_password_edit2.setText(item.pass);
                final CheckBox mmp_enabled2 = lay10.findViewById(R.id.mrim_profile_add_enabled);
                mmp_enabled2.setText(Locale.getString("s_profile_enabled"));
                final CheckBox mmp_autoconnect2 = lay10.findViewById(R.id.mrim_profile_add_autoconnect);
                mmp_enabled2.setChecked(this.pa.getItem(this.selectedIdx).enabled);
                mmp_autoconnect2.setChecked(item.autoconnect);
                //noinspection UnnecessaryLocalVariable
                Dialog ad13 = DialogBuilder.createYesNo(this, lay10, Gravity.TOP, resources.getString("s_profile_changing"), resources.getString("s_change"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = pa.getItem(selectedIdx);
                        pdata.profile_type = 2;
                        String ID = mmp_login_edit2.getText().toString().toLowerCase().trim();
                        if (!utilities.isMrim(ID)) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else if (ID.length() < 7) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else {
                            pdata.id = ID;
                            String pass = mmp_password_edit2.getText().toString();
                            if (pass.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.pass = pass;
                            pdata.autoconnect = mmp_autoconnect2.isChecked();
                            pdata.enabled = mmp_enabled2.isChecked();
                            MMPProfile mmp_profile = (MMPProfile) service.profiles.getProfiles().get(selectedIdx);
                            mmp_profile.reinitParams(pdata);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistNeedRemake();
                            removeDialog(12);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(12);
                    }
                });
                return ad13;
            case 13:
                @SuppressLint("InflateParams")
                LinearLayout lay11 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_ya_profile_add, null);
                ((TextView) lay11.findViewById(R.id.l1)).setText(resources.getString("s_qip_dialog_login"));
                ((TextView) lay11.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid8 = lay11.findViewById(R.id.xmpp_profile_add_jid);
                jid8.setHint("");
                resources.attachEditText(jid8);
                final EditText password8 = lay11.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password8);
                final CheckBox tls8 = lay11.findViewById(R.id.xmpp_profile_add_tls);
                final CheckBox zlib8 = lay11.findViewById(R.id.xmpp_profile_add_zlib);
                final CheckBox enabled8 = lay11.findViewById(R.id.xmpp_profile_add_enabled);
                enabled8.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect8 = lay11.findViewById(R.id.xmpp_profile_add_autoconnect);
                autoconnect8.setText(resources.getString("s_dialog_autologin"));
                zlib8.setText(resources.getString("s_dialog_compression"));
                tls8.setText(resources.getString("s_dialog_tls"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad14 = DialogBuilder.createYesNo(this, lay11, Gravity.TOP, resources.getString("s_profile_adding"), resources.getString("s_do_add"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = new ProfilesAdapterItem();
                        pdata.profile_type = 1;
                        String JID = jid8.getText().toString().toLowerCase().trim();
                        String[] parts2 = JID.split("@");
                        if (service.profiles.isProfileAlreadyExist(JID)) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_3"), Toast.LENGTH_SHORT).show();
                        } else if (JID.length() < 3 || parts2.length < 2) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_1"), Toast.LENGTH_SHORT).show();
                        } else {
                            pdata.id = parts2[0];
                            pdata.host = parts2[1];
                            String pass = password8.getText().toString();
                            if (pass.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.enabled = enabled8.isChecked();
                            pdata.pass = pass;
                            pdata.server = "webim.qip.ru";
                            pdata.autoconnect = autoconnect8.isChecked();
                            pdata.compression = zlib8.isChecked();
                            pdata.tls = tls8.isChecked();
                            pdata.proto_type = 4;
                            pa.add(pdata);
                            JProfile profile = new JProfile(service, pdata.id, pdata.host, pdata.server, 5222, pdata.pass, null, pdata.compression, pdata.tls, pdata.sasl, pdata.autoconnect, pdata.enabled, pdata.proto_type);
                            service.profiles.addProfile(profile);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistCheckConferences();
                            service.handleContactlistNeedRemake();
                            removeDialog(13);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(13);
                    }
                });
                return ad14;
            case 14:
                @SuppressLint("InflateParams")
                LinearLayout lay12 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_ya_profile_add, null);
                ((TextView) lay12.findViewById(R.id.l1)).setText(resources.getString("s_qip_dialog_login"));
                ((TextView) lay12.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid9 = lay12.findViewById(R.id.xmpp_profile_add_jid);
                jid9.setHint("");
                resources.attachEditText(jid9);
                final EditText password9 = lay12.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password9);
                final CheckBox enabled9 = lay12.findViewById(R.id.xmpp_profile_add_enabled);
                enabled9.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect9 = lay12.findViewById(R.id.xmpp_profile_add_autoconnect);
                jid9.setText(this.pa.getItem(this.selectedIdx).id + "@" + this.pa.getItem(this.selectedIdx).host);
                password9.setText(this.pa.getItem(this.selectedIdx).pass);
                final CheckBox tls9 = lay12.findViewById(R.id.xmpp_profile_add_tls);
                tls9.setChecked(this.pa.getItem(this.selectedIdx).tls);
                CheckBox zlib9 = lay12.findViewById(R.id.xmpp_profile_add_zlib);
                zlib9.setChecked(this.pa.getItem(this.selectedIdx).compression);
                enabled9.setChecked(this.pa.getItem(this.selectedIdx).enabled);
                autoconnect9.setText(resources.getString("s_dialog_autologin"));
                zlib9.setText(resources.getString("s_dialog_compression"));
                tls9.setText(resources.getString("s_dialog_tls"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad15 = DialogBuilder.createYesNo(this, lay12, Gravity.TOP, resources.getString("s_profile_changing"), resources.getString("s_change"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = pa.getItem(selectedIdx);
                        pdata.profile_type = 1;
                        String JID = jid9.getText().toString().toLowerCase().trim();
                        String[] parts2 = JID.split("@");
                        if (JID.length() < 3 || parts2.length < 2) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_1"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pdata.id = parts2[0];
                        pdata.host = parts2[1];
                        String pass = password9.getText().toString();
                        if (pass.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pdata.enabled = enabled9.isChecked();
                        pdata.pass = pass;
                        pdata.server = "webim.qip.ru";
                        pdata.autoconnect = autoconnect9.isChecked();
                        pdata.sasl = true;
                        pdata.tls = tls9.isChecked();
                        pdata.proto_type = 4;
                        pa.notifyDataSetChanged();
                        ((JProfile) service.profiles.getProfiles().get(selectedIdx)).reinitParams(pdata);
                        service.profiles.writeProfilesToFile();
                        service.handleProfileChanged();
                        service.handleContactlistNeedRemake();
                        removeDialog(14);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(14);
                    }
                });
                return ad15;
            case 15:
                @SuppressLint("InflateParams")
                LinearLayout lay13 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_ya_profile_add, null);
                ((TextView) lay13.findViewById(R.id.l1)).setText(resources.getString("s_gtalk_dialog_login"));
                ((TextView) lay13.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid10 = lay13.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid10);
                final EditText password10 = lay13.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password10);
                final CheckBox tls10 = lay13.findViewById(R.id.xmpp_profile_add_tls);
                final CheckBox zlib10 = lay13.findViewById(R.id.xmpp_profile_add_zlib);
                final CheckBox enabled10 = lay13.findViewById(R.id.xmpp_profile_add_enabled);
                enabled10.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect10 = lay13.findViewById(R.id.xmpp_profile_add_autoconnect);
                autoconnect10.setText(resources.getString("s_dialog_autologin"));
                zlib10.setText(resources.getString("s_dialog_compression"));
                tls10.setText(resources.getString("s_dialog_tls"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad16 = DialogBuilder.createYesNo(this, lay13, Gravity.TOP, resources.getString("s_profile_adding"), resources.getString("s_do_add"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = new ProfilesAdapterItem();
                        pdata.profile_type = 1;
                        String JID = jid10.getText().toString().toLowerCase().trim();
                        if (service.profiles.isProfileAlreadyExist(JID)) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_3"), Toast.LENGTH_SHORT).show();
                        } else if (JID.length() < 11) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                        } else {
                            String[] parts2 = JID.split("@");
                            if (parts2.length != 2) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.id = parts2[0];
                            String pass = password10.getText().toString();
                            if (pass.length() < 3) {
                                Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            pdata.enabled = enabled10.isChecked();
                            pdata.pass = pass;
                            pdata.server = "talk.google.com";
                            pdata.host = parts2[1];
                            pdata.autoconnect = autoconnect10.isChecked();
                            pdata.compression = zlib10.isChecked();
                            pdata.tls = tls10.isChecked();
                            pdata.proto_type = 3;
                            pa.add(pdata);
                            JProfile profile = new JProfile(service, pdata.id, pdata.host, pdata.server, 5222, pdata.pass, null, pdata.compression, pdata.tls, pdata.sasl, pdata.autoconnect, pdata.enabled, pdata.proto_type);
                            service.profiles.addProfile(profile);
                            service.profiles.writeProfilesToFile();
                            service.handleProfileChanged();
                            service.handleContactlistCheckConferences();
                            service.handleContactlistNeedRemake();
                            removeDialog(15);
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(15);
                    }
                });
                return ad16;
            case 16:
                @SuppressLint("InflateParams") LinearLayout lay14 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.xmpp_ya_profile_add, null);
                ((TextView) lay14.findViewById(R.id.l1)).setText(resources.getString("s_gtalk_dialog_login"));
                ((TextView) lay14.findViewById(R.id.l2)).setText(resources.getString("s_dialog_pass"));
                final EditText jid11 = lay14.findViewById(R.id.xmpp_profile_add_jid);
                resources.attachEditText(jid11);
                final EditText password11 = lay14.findViewById(R.id.xmpp_profile_add_pass);
                resources.attachEditText(password11);
                final CheckBox enabled11 = lay14.findViewById(R.id.xmpp_profile_add_enabled);
                enabled11.setText(Locale.getString("s_profile_enabled"));
                final CheckBox autoconnect11 = lay14.findViewById(R.id.xmpp_profile_add_autoconnect);
                jid11.setText(this.pa.getItem(this.selectedIdx).id + "@" + this.pa.getItem(this.selectedIdx).host);
                password11.setText(this.pa.getItem(this.selectedIdx).pass);
                enabled11.setChecked(this.pa.getItem(this.selectedIdx).enabled);
                final CheckBox tls11 = lay14.findViewById(R.id.xmpp_profile_add_tls);
                tls11.setChecked(this.pa.getItem(this.selectedIdx).tls);
                final CheckBox zlib11 = lay14.findViewById(R.id.xmpp_profile_add_zlib);
                zlib11.setChecked(this.pa.getItem(this.selectedIdx).tls);
                autoconnect11.setText(resources.getString("s_dialog_autologin"));
                zlib11.setText(resources.getString("s_dialog_compression"));
                tls11.setText(resources.getString("s_dialog_tls"));
                //noinspection UnnecessaryLocalVariable
                Dialog ad17 = DialogBuilder.createYesNo(this, lay14, Gravity.TOP, resources.getString("s_profile_changing"), resources.getString("s_change"), resources.getString("s_cancel"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProfilesAdapterItem pdata = pa.getItem(selectedIdx);
                        pdata.profile_type = 1;
                        String JID = jid11.getText().toString().toLowerCase().trim();
                        if (JID.length() < 11) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String[] parts2 = JID.split("@");
                        if (parts2.length != 2) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_7"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pdata.id = parts2[0];
                        pdata.host = parts2[1];
                        String pass = password11.getText().toString();
                        if (pass.length() < 3) {
                            Toast.makeText(ProfilesActivity.this, resources.getString("s_profile_error_4"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pdata.enabled = enabled11.isChecked();
                        pdata.pass = pass;
                        pdata.autoconnect = autoconnect11.isChecked();
                        pdata.tls = tls11.isChecked();
                        pdata.compression = zlib11.isChecked();
                        pa.notifyDataSetChanged();
                        ((JProfile) service.profiles.getProfiles().get(selectedIdx)).reinitParams(pdata);
                        service.profiles.writeProfilesToFile();
                        service.handleProfileChanged();
                        service.handleContactlistNeedRemake();
                        removeDialog(16);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDialog(16);
                    }
                });
                return ad17;
            default:
                return null;
        }
    }

    private void initViews() {
        ((TextView) findViewById(R.id.l1)).setText(resources.getString("s_profiles_manager_title"));
        ((TextView) findViewById(R.id.l2)).setText(resources.getString("s_profiles_manager_hint"));
        profiles = findViewById(R.id.profiles_list);
        profiles.setSelector(resources.getListSelector());
        profiles.setOnItemClickListener(new item_click_listener());
        pa = new ProfilesAdapter();
        profiles.setAdapter(this.pa);
    }

    private void handleServiceConnected() {
        fillFromProfilesManager();
    }

    public void fillFromProfilesManager() {
        Vector<IMProfile> profiles = this.service.profiles.getProfiles();
        Vector<ProfilesAdapterItem> pdatas = new Vector<>();
        for (IMProfile improfile : profiles) {
            ProfilesAdapterItem pdata = new ProfilesAdapterItem();
            pdata.enabled = improfile.enabled;
            switch (improfile.profile_type) {
                case 0:
                    ICQProfile icqprofile = (ICQProfile) improfile;
                    pdata.profile_type = 0;
                    pdata.id = icqprofile.ID;
                    pdata.pass = icqprofile.password;
                    pdata.autoconnect = icqprofile.autoconnect;
                    pdatas.add(pdata);
                    break;
                case 1:
                    JProfile jprofile = (JProfile) improfile;
                    pdata.profile_type = 1;
                    pdata.id = jprofile.ID;
                    pdata.host = jprofile.host;
                    if (jprofile.type == 1) {
                        pdata.host = "vk.com";
                    }
                    pdata.pass = jprofile.PASS;
                    pdata.autoconnect = jprofile.autoconnect;
                    pdata.server = jprofile.server;
                    pdata.port = jprofile.port;
                    pdata.tls = jprofile.use_tls;
                    pdata.compression = jprofile.use_compression;
                    pdata.sasl = jprofile.use_sasl;
                    pdata.proto_type = jprofile.type;
                    pdatas.add(pdata);
                    break;
                case 2:
                    MMPProfile mmp_profile = (MMPProfile) improfile;
                    pdata.profile_type = 2;
                    pdata.id = mmp_profile.ID;
                    pdata.pass = mmp_profile.PASS;
                    pdata.autoconnect = mmp_profile.autoconnect;
                    pdatas.add(pdata);
                    break;
            }
        }
        this.pa.setProfilesAdapter(pdatas);
    }

    public class item_click_listener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            selectedIdx = arg2;
            removeDialog(2);
            showDialog(2);
        }
    }
}
