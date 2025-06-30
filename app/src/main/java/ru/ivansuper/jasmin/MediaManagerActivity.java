package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.utils.SystemBarUtils;


public class MediaManagerActivity extends Activity {
    
    private ServiceConnection jasminSvcCnt;
    private LinearLayout list;
    private jasminSvc service;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setVolumeControlStream(3);
        setContentView(R.layout.media_manager);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
        bindToService();
        fillInterface();
    }

    private void initViews() {
        ((TextView) findViewById(R.id.l1)).setText(resources.getString("s_media_title"));
        list = findViewById(R.id.media_manager_list);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != 0) {
            Log.v("Media", data.getAction());
            if (isMedia(data.getAction())) {
                switch (requestCode) {
                    case 0:
                        sp.edit().putString("im_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 1:
                        sp.edit().putString("aa_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 2:
                        sp.edit().putString("ad_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 3:
                        sp.edit().putString("ar_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 4:
                        sp.edit().putString("ci_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 5:
                        sp.edit().putString("co_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 6:
                        sp.edit().putString("if_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 7:
                        sp.edit().putString("om_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    case 8:
                        sp.edit().putString("tr_snd", data.getAction()).commit();
                        MediaTable.forceUpdate();
                        return;
                    default:
                }
            }
        }
    }

    private boolean isMedia(String file) {
        return file.toLowerCase().endsWith(".wav") || file.toLowerCase().endsWith(".mp3") || file.toLowerCase().endsWith(".ogg");
    }

    @SuppressLint("ApplySharedPref")
    private void fillInterface() {
        list.removeAllViews();
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff = new CheckBox(this);
        onoff.setText(resources.getString("s_media_enabled"));
        onoff.setChecked(MediaTable.auth_accepted_e);
        onoff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("aa_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select = new Button(this);
        resources.attachButtonStyle(select);
        select.setText(resources.getString("s_media_file"));
        select.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 1));
        Button standard = new Button(this);
        resources.attachButtonStyle(standard);
        standard.setText(resources.getString("s_media_std"));
        standard.setOnClickListener(arg0 -> {
            sp.edit().putString("aa_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview = new Button(this);
        resources.attachButtonStyle(preview);
        preview.setText(resources.getString("s_media_listen"));
        preview.setOnClickListener(v -> service.playEvent(1));
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.addView(select);
        panel.addView(standard);
        panel.addView(preview);
        TextView label = new TextView(this);
        label.setText(resources.getString("s_media_auth_allowed"));
        label.setTextColor(-1);
        label.setBackgroundColor(872415231);
        label.setPadding(5, 5, 5, 5);
        lay.addView(label);
        lay.addView(onoff);
        lay.addView(panel);
        lay.setPadding(10, 10, 10, 10);
        lay.setBackgroundColor(452984831);
        LinearLayout separator = new LinearLayout(this);
        separator.setPadding(0, 5, 0, 5);
        list.addView(lay);
        list.addView(separator);
        LinearLayout lay1 = new LinearLayout(this);
        lay1.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff1 = new CheckBox(this);
        onoff1.setText(resources.getString("s_media_enabled"));
        onoff1.setChecked(MediaTable.auth_denied_e);
        onoff1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ad_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select1 = new Button(this);
        resources.attachButtonStyle(select1);
        select1.setText(resources.getString("s_media_file"));
        select1.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 2));
        Button standard1 = new Button(this);
        resources.attachButtonStyle(standard1);
        standard1.setText(resources.getString("s_media_std"));
        standard1.setOnClickListener(arg0 -> {
            sp.edit().putString("ad_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview1 = new Button(this);
        resources.attachButtonStyle(preview1);
        preview1.setText(resources.getString("s_media_listen"));
        preview1.setOnClickListener(v -> service.playEvent(2));
        LinearLayout panel1 = new LinearLayout(this);
        panel1.setOrientation(LinearLayout.HORIZONTAL);
        panel1.addView(select1);
        panel1.addView(standard1);
        panel1.addView(preview1);
        TextView label1 = new TextView(this);
        label1.setText(resources.getString("s_media_auth_denied"));
        label1.setTextColor(-1);
        label1.setBackgroundColor(872415231);
        label1.setPadding(5, 5, 5, 5);
        lay1.addView(label1);
        lay1.addView(onoff1);
        lay1.addView(panel1);
        lay1.setPadding(10, 10, 10, 10);
        lay1.setBackgroundColor(452984831);
        LinearLayout separator1 = new LinearLayout(this);
        separator1.setPadding(0, 5, 0, 5);
        list.addView(lay1);
        list.addView(separator1);
        LinearLayout lay2 = new LinearLayout(this);
        lay2.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff2 = new CheckBox(this);
        onoff2.setText(resources.getString("s_media_enabled"));
        onoff2.setChecked(MediaTable.auth_req_e);
        onoff2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ar_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select2 = new Button(this);
        resources.attachButtonStyle(select2);
        select2.setText(resources.getString("s_media_file"));
        select2.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 3));
        Button standard2 = new Button(this);
        resources.attachButtonStyle(standard2);
        standard2.setText(resources.getString("s_media_std"));
        standard2.setOnClickListener(arg0 -> {
            sp.edit().putString("ar_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview2 = new Button(this);
        resources.attachButtonStyle(preview2);
        preview2.setText(resources.getString("s_media_listen"));
        preview2.setOnClickListener(v -> service.playEvent(3));
        LinearLayout panel2 = new LinearLayout(this);
        panel2.setOrientation(LinearLayout.HORIZONTAL);
        panel2.addView(select2);
        panel2.addView(standard2);
        panel2.addView(preview2);
        TextView label2 = new TextView(this);
        label2.setText(resources.getString("s_media_auth_req"));
        label2.setTextColor(-1);
        label2.setBackgroundColor(872415231);
        label2.setPadding(5, 5, 5, 5);
        lay2.addView(label2);
        lay2.addView(onoff2);
        lay2.addView(panel2);
        lay2.setPadding(10, 10, 10, 10);
        lay2.setBackgroundColor(452984831);
        LinearLayout separator2 = new LinearLayout(this);
        separator2.setPadding(0, 5, 0, 5);
        list.addView(lay2);
        list.addView(separator2);
        LinearLayout lay3 = new LinearLayout(this);
        lay3.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff3 = new CheckBox(this);
        onoff3.setText(resources.getString("s_media_enabled"));
        onoff3.setChecked(MediaTable.contact_in_e);
        onoff3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("ci_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select3 = new Button(this);
        resources.attachButtonStyle(select3);
        select3.setText(resources.getString("s_media_file"));
        select3.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 4));
        Button standard3 = new Button(this);
        resources.attachButtonStyle(standard3);
        standard3.setText(resources.getString("s_media_std"));
        standard3.setOnClickListener(arg0 -> {
            sp.edit().putString("ci_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview3 = new Button(this);
        resources.attachButtonStyle(preview3);
        preview3.setText(resources.getString("s_media_listen"));
        preview3.setOnClickListener(v -> service.playEvent(4));
        LinearLayout panel3 = new LinearLayout(this);
        panel3.setOrientation(LinearLayout.HORIZONTAL);
        panel3.addView(select3);
        panel3.addView(standard3);
        panel3.addView(preview3);
        TextView label3 = new TextView(this);
        label3.setText(resources.getString("s_media_contact_online"));
        label3.setTextColor(-1);
        label3.setBackgroundColor(872415231);
        label3.setPadding(5, 5, 5, 5);
        lay3.addView(label3);
        lay3.addView(onoff3);
        lay3.addView(panel3);
        lay3.setPadding(10, 10, 10, 10);
        lay3.setBackgroundColor(452984831);
        LinearLayout separator3 = new LinearLayout(this);
        separator3.setPadding(0, 5, 0, 5);
        list.addView(lay3);
        list.addView(separator3);
        LinearLayout lay4 = new LinearLayout(this);
        lay4.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff4 = new CheckBox(this);
        onoff4.setText(resources.getString("s_media_enabled"));
        onoff4.setChecked(MediaTable.contact_out_e);
        onoff4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("co_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select4 = new Button(this);
        resources.attachButtonStyle(select4);
        select4.setText(resources.getString("s_media_file"));
        select4.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 5));
        Button standard4 = new Button(this);
        resources.attachButtonStyle(standard4);
        standard4.setText(resources.getString("s_media_std"));
        standard4.setOnClickListener(arg0 -> {
            sp.edit().putString("co_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview4 = new Button(this);
        resources.attachButtonStyle(preview4);
        preview4.setText(resources.getString("s_media_listen"));
        preview4.setOnClickListener(v -> service.playEvent(5));
        LinearLayout panel4 = new LinearLayout(this);
        panel4.setOrientation(LinearLayout.HORIZONTAL);
        panel4.addView(select4);
        panel4.addView(standard4);
        panel4.addView(preview4);
        TextView label4 = new TextView(this);
        label4.setText(resources.getString("s_media_contact_offline"));
        label4.setTextColor(-1);
        label4.setBackgroundColor(872415231);
        label4.setPadding(5, 5, 5, 5);
        lay4.addView(label4);
        lay4.addView(onoff4);
        lay4.addView(panel4);
        lay4.setPadding(10, 10, 10, 10);
        lay4.setBackgroundColor(452984831);
        LinearLayout separator4 = new LinearLayout(this);
        separator4.setPadding(0, 5, 0, 5);
        list.addView(lay4);
        list.addView(separator4);
        LinearLayout lay5 = new LinearLayout(this);
        lay5.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff5 = new CheckBox(this);
        onoff5.setText(resources.getString("s_media_enabled"));
        onoff5.setChecked(MediaTable.inc_file_e);
        onoff5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("if_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select5 = new Button(this);
        resources.attachButtonStyle(select5);
        select5.setText(resources.getString("s_media_file"));
        select5.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 6));
        Button standard5 = new Button(this);
        resources.attachButtonStyle(standard5);
        standard5.setText(resources.getString("s_media_std"));
        standard5.setOnClickListener(arg0 -> {
            sp.edit().putString("if_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview5 = new Button(this);
        resources.attachButtonStyle(preview5);
        preview5.setText(resources.getString("s_media_listen"));
        preview5.setOnClickListener(v -> service.playEvent(6));
        LinearLayout panel5 = new LinearLayout(this);
        panel5.setOrientation(LinearLayout.HORIZONTAL);
        panel5.addView(select5);
        panel5.addView(standard5);
        panel5.addView(preview5);
        TextView label5 = new TextView(this);
        label5.setText(resources.getString("s_media_incoming_file"));
        label5.setTextColor(-1);
        label5.setBackgroundColor(872415231);
        label5.setPadding(5, 5, 5, 5);
        lay5.addView(label5);
        lay5.addView(onoff5);
        lay5.addView(panel5);
        lay5.setPadding(10, 10, 10, 10);
        lay5.setBackgroundColor(452984831);
        LinearLayout separator5 = new LinearLayout(this);
        separator5.setPadding(0, 5, 0, 5);
        list.addView(lay5);
        list.addView(separator5);
        LinearLayout lay6 = new LinearLayout(this);
        lay6.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff6 = new CheckBox(this);
        onoff6.setText(resources.getString("s_media_enabled"));
        onoff6.setChecked(MediaTable.inc_msg_e);
        onoff6.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("im_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select6 = new Button(this);
        resources.attachButtonStyle(select6);
        select6.setText(resources.getString("s_media_file"));
        select6.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 0));
        Button standard6 = new Button(this);
        resources.attachButtonStyle(standard6);
        standard6.setText(resources.getString("s_media_std"));
        standard6.setOnClickListener(arg0 -> {
            sp.edit().putString("im_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview6 = new Button(this);
        resources.attachButtonStyle(preview6);
        preview6.setText(resources.getString("s_media_listen"));
        preview6.setOnClickListener(v -> service.playEvent(0));
        LinearLayout panel6 = new LinearLayout(this);
        panel6.setOrientation(LinearLayout.HORIZONTAL);
        panel6.addView(select6);
        panel6.addView(standard6);
        panel6.addView(preview6);
        TextView label6 = new TextView(this);
        label6.setText(resources.getString("s_media_incoming_msg"));
        label6.setTextColor(-1);
        label6.setBackgroundColor(872415231);
        label6.setPadding(5, 5, 5, 5);
        lay6.addView(label6);
        lay6.addView(onoff6);
        lay6.addView(panel6);
        lay6.setPadding(10, 10, 10, 10);
        lay6.setBackgroundColor(452984831);
        LinearLayout separator6 = new LinearLayout(this);
        separator6.setPadding(0, 5, 0, 5);
        list.addView(lay6);
        list.addView(separator6);
        LinearLayout lay7 = new LinearLayout(this);
        lay7.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff7 = new CheckBox(this);
        onoff7.setText(resources.getString("s_media_enabled"));
        onoff7.setChecked(MediaTable.out_msg_e);
        onoff7.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("om_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select7 = new Button(this);
        resources.attachButtonStyle(select7);
        select7.setText(resources.getString("s_media_file"));
        select7.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 7));
        Button standard7 = new Button(this);
        resources.attachButtonStyle(standard7);
        standard7.setText(resources.getString("s_media_std"));
        standard7.setOnClickListener(arg0 -> {
            sp.edit().putString("om_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview7 = new Button(this);
        resources.attachButtonStyle(preview7);
        preview7.setText(resources.getString("s_media_listen"));
        preview7.setOnClickListener(v -> service.playEvent(7));
        LinearLayout panel7 = new LinearLayout(this);
        panel7.setOrientation(LinearLayout.HORIZONTAL);
        panel7.addView(select7);
        panel7.addView(standard7);
        panel7.addView(preview7);
        TextView label7 = new TextView(this);
        label7.setText(resources.getString("s_media_outgoing_msg"));
        label7.setTextColor(-1);
        label7.setBackgroundColor(872415231);
        label7.setPadding(5, 5, 5, 5);
        lay7.addView(label7);
        lay7.addView(onoff7);
        lay7.addView(panel7);
        lay7.setPadding(10, 10, 10, 10);
        lay7.setBackgroundColor(452984831);
        LinearLayout separator7 = new LinearLayout(this);
        separator7.setPadding(0, 5, 0, 5);
        list.addView(lay7);
        list.addView(separator7);
        LinearLayout lay8 = new LinearLayout(this);
        lay8.setOrientation(LinearLayout.VERTICAL);
        CheckBox onoff8 = new CheckBox(this);
        onoff8.setText(resources.getString("s_media_enabled"));
        onoff8.setChecked(MediaTable.transfer_rejected_e);
        onoff8.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("tr_snd_e", isChecked).commit();
            MediaTable.forceUpdate();
        });
        Button select8 = new Button(this);
        resources.attachButtonStyle(select8);
        select8.setText(resources.getString("s_media_file"));
        select8.setOnClickListener(arg0 -> startActivityForResult(new Intent(resources.ctx, FileBrowserActivity.class), 8));
        Button standard8 = new Button(this);
        resources.attachButtonStyle(standard8);
        standard8.setText(resources.getString("s_media_std"));
        standard8.setOnClickListener(arg0 -> {
            sp.edit().putString("tr_snd", "$*INTERNAL*$").commit();
            MediaTable.forceUpdate();
        });
        Button preview8 = new Button(this);
        resources.attachButtonStyle(preview8);
        preview8.setText(resources.getString("s_media_listen"));
        preview8.setOnClickListener(v -> service.playEvent(8));
        LinearLayout panel8 = new LinearLayout(this);
        panel8.setOrientation(LinearLayout.HORIZONTAL);
        panel8.addView(select8);
        panel8.addView(standard8);
        panel8.addView(preview8);
        TextView label8 = new TextView(this);
        label8.setText(resources.getString("s_media_transfer_canceled"));
        label8.setTextColor(-1);
        label8.setBackgroundColor(872415231);
        label8.setPadding(5, 5, 5, 5);
        lay8.addView(label8);
        lay8.addView(onoff8);
        lay8.addView(panel8);
        lay8.setPadding(10, 10, 10, 10);
        lay8.setBackgroundColor(452984831);
        LinearLayout separator8 = new LinearLayout(this);
        separator8.setPadding(0, 5, 0, 5);
        list.addView(lay8);
        list.addView(separator8);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (jasminSvcCnt != null) {
                unbindService(jasminSvcCnt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindToService() {
        jasminSvcCnt = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                service = ((jasminSvc.itf) arg1).getService();
                handleServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                service = null;
                finish();
            }
        };
        Intent svc = new Intent();
        svc.setClass(this, jasminSvc.class);
        bindService(svc, jasminSvcCnt, 0);
    }

    private void handleServiceConnected() {}
}
