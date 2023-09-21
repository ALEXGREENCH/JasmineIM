package ru.ivansuper.jasmin;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import ru.ivansuper.jasmin.HistoryTools.ExportImportActivity;
import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.icq.SNAC;

public class main extends Activity implements Handler.Callback {

    private final Handler hdl = new Handler(this);
    private jasminSvc service;
    private LinearLayout splash;
    private ServiceConnection svcc;

    int MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE = 112;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindowManager().getDefaultDisplay().getMetrics(resources.dm);
        setContentView(R.layout.main);
        splash = findViewById(R.id.splash);
        splash.setVisibility(View.INVISIBLE);
        TextView buyed = findViewById(R.id.buyed);
        buyed.setText(resources.getString("s_app_bought"));
        ((TextView) findViewById(R.id.loading)).setText(resources.getString("s_app_loading"));
        if (resources.service != null) {
            intentNext();
            finish();
            return;
        }
        new Intent().setClass(getApplicationContext(), jasminSvc.class);
        svcc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                service = ((jasminSvc.itf) arg1).getService();
                splashThread t = new splashThread();
                t.start();
                resources.service = service;
                unbindService(svcc);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {}
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Запрос разрешения у пользователя
                requestPermissions(
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE);
                // После ответа пользователя результат будет обработан в onRequestPermissionsResult
                return;
            }
        } else {
            startJasmineService();
        }


    }

    private void startJasmineService() {
        Intent svc = new Intent();
        svc.setClass(getApplicationContext(), jasminSvc.class);
        startService(svc);
        bindService(svc, svcc, 0);
    }

    private void intentNext() {
        File profs = new File(resources.dataPath + "profiles.cfg");
        if (!profs.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                profs.createNewFile();
                intentContactList(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else intentContactList(profs.length() <= 10);
        finish();
    }

    private void prepareProfiles() {
        if (resources.service.profiles == null) {
            resources.service.profiles = new ProfilesManager(resources.service);
            EventTranslator.sendProfilesList();
        }
    }

    private void intentContactList(boolean profiles_request) {
        Intent i = new Intent();
        i.setAction(getIntent().getAction());
        i.setClass(getApplicationContext(), ContactListActivity.class);
        if (profiles_request) {
            i.putExtra("no_profiles", true);
        }
        startActivity(i);
    }

    @Override
    public void onBackPressed() {}

    private class splashThread extends Thread {

        @Override
        public void run() {
            if (service.firstStart) {
                Process.setThreadPriority(-10);
                service.firstStart = false;
                hdl.sendEmptyMessage(1);
                /*
                int zzs = Math.abs(3748);
                int av = 502 + zzs;
                String aas = String.valueOf(av) + av;
                int zzd = av + aas.hashCode();
                boolean a = SNAC.sts.startsWith(utilities.randomized);
                boolean b = SNAC.sts.startsWith(utilities.randomized2);
                int zzs2 = Math.abs(zzd);
                int av2 = 675 + zzs2;
                String aaf = String.valueOf(av2) + av2;
                //noinspection unused
                int zze = av2 + aaf.hashCode();
                 */
                // TODO: :)
                //SmileysManager.preloadPack(a, b);
                SmileysManager.preloadPack();
                prepareProfiles();
            }
            hdl.sendEmptyMessage(0);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                if (ExportImportActivity.CONVERTING_DIALOG != null) {
                    ExportImportActivity.CONVERTING_DIALOG.dismiss();
                    ExportImportActivity.CONVERTING_DIALOG = null;
                }
                intentNext();
                break;
            case 1:
                splash.setVisibility(View.VISIBLE);
                break;
        }
        return false;
    }

    protected void finalize() {
        Log.e("Main", "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        startJasmineService();

    }
}
