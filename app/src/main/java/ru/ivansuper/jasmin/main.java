package ru.ivansuper.jasmin;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import ru.ivansuper.jasmin.HistoryTools.ExportImportActivity;
import ru.ivansuper.jasmin.Service.EventTranslator;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * The main entry point of the application. This activity handles the initial setup,
 * permission checks, and service binding. It displays a splash screen during initialization
 * and then navigates to the appropriate next activity (either profile setup or contact list).
 */
public class main extends Activity implements Handler.Callback {

    ////public static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";

    private static final int FOREGROUND_SERVICE_PERMISSION_REQUEST = 112;
    private static final int READ_PHONE_STATE_PERMISSION_REQUEST = 113;
    private static final int READ_STORAGE_PERMISSION_REQUEST = 114;
    ////private static final int POST_NOTIFICATIONS_PERMISSION_REQUEST = 115;

    private final Handler handler = new Handler(this);
    private jasminSvc jasminService;
    private LinearLayout splashView;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources.applyFontScale(this);
        getWindowManager().getDefaultDisplay().getMetrics(resources.dm);
        setContentView(R.layout.main);
        SystemBarUtils.setupTransparentBars(this);

        initializeViews();

        if (resources.service != null) {
            proceedToNextActivity();
            finish();
            return;
        }

        setupServiceConnection();
        checkPermissionsAndStartService();
    }

    private void initializeViews() {
        splashView = findViewById(R.id.splash);
        splashView.setVisibility(View.GONE);

        TextView purchasedText = findViewById(R.id.buyed);
        purchasedText.setText(resources.getString("s_app_bought"));

        TextView loadingText = findViewById(R.id.loading);
        loadingText.setText(resources.getString("s_app_loading"));
    }

    private void setupServiceConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                jasminService = ((jasminSvc.itf) binder).getService();
                resources.service = jasminService;
                new SplashThread().start();
                unbindService(serviceConnection);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    private void checkPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean needsForegroundServicePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED;

            boolean needsReadPhoneStatePermission =
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;

            boolean needsReadStoragePermission =
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

            ////boolean needsPostNotificationsPermission =
            ////        Build.VERSION.SDK_INT >= 33 &&
            ////                checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;

            if (needsForegroundServicePermission || needsReadPhoneStatePermission ||
                    ////needsReadStoragePermission || needsPostNotificationsPermission) {
                    needsReadStoragePermission) {
                int size = 0;
                if (needsForegroundServicePermission) size++;
                if (needsReadPhoneStatePermission) size++;
                if (needsReadStoragePermission) size++;
                ///if (needsPostNotificationsPermission) size++;
                String[] permissions = new String[size];
                int i = 0;
                if (needsForegroundServicePermission) permissions[i++] = Manifest.permission.FOREGROUND_SERVICE;
                if (needsReadPhoneStatePermission) permissions[i++] = Manifest.permission.READ_PHONE_STATE;
                if (needsReadStoragePermission) permissions[i++] = Manifest.permission.READ_EXTERNAL_STORAGE;
                ////if (needsPostNotificationsPermission) permissions[i] = POST_NOTIFICATIONS;

                ////int requestCode = needsPostNotificationsPermission
                ////        ? POST_NOTIFICATIONS_PERMISSION_REQUEST
                ////        : (needsReadStoragePermission ? READ_STORAGE_PERMISSION_REQUEST : READ_PHONE_STATE_PERMISSION_REQUEST);

                int requestCode = needsReadStoragePermission ? READ_STORAGE_PERMISSION_REQUEST : READ_PHONE_STATE_PERMISSION_REQUEST;

                requestPermissions(permissions, requestCode);
                return;
            }
        }

        startJasminService();
    }

    private void startJasminService() {
        Intent serviceIntent = new Intent(this, jasminSvc.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void proceedToNextActivity() {
        File profilesFile = new File(resources.dataPath + "profiles.cfg");
        boolean needsProfileSetup = !profilesFile.exists() || profilesFile.length() <= 10;

        if (!profilesFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                profilesFile.createNewFile();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        launchContactListActivity(needsProfileSetup);
        finish();
    }

    private void prepareProfiles() {
        if (resources.service.profiles == null) {
            resources.service.profiles = new ProfilesManager(resources.service);
            EventTranslator.sendProfilesList();
        }
    }

    private void launchContactListActivity(boolean needsProfileSetup) {
        Intent intent = new Intent(getIntent().getAction())
                .setClass(getApplicationContext(), ContactListActivity.class);

        if (needsProfileSetup) {
            intent.putExtra("no_profiles", true);
        }

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during initialization
    }

    private class SplashThread extends Thread {
        @Override
        public void run() {
            if (jasminService.firstStart) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
                jasminService.firstStart = false;
                handler.sendEmptyMessage(1);  // Show splash
                SmileysManager.preloadPack();
                prepareProfiles();
            }
            handler.sendEmptyMessage(0);  // Proceed to next activity
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0: // Hide loading and proceed
                dismissConversionDialog();
                proceedToNextActivity();
                break;
            case 1: // Show splash screen
                splashView.setVisibility(View.VISIBLE);
                splashView.bringToFront();
                break;
        }
        return true;
    }

    private void dismissConversionDialog() {
        if (ExportImportActivity.CONVERTING_DIALOG != null) {
            ExportImportActivity.CONVERTING_DIALOG.dismiss();
            ExportImportActivity.CONVERTING_DIALOG = null;
        }
    }

    /** @noinspection NullableProblems*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FOREGROUND_SERVICE_PERMISSION_REQUEST ||
                requestCode == READ_PHONE_STATE_PERMISSION_REQUEST ||
                requestCode == READ_STORAGE_PERMISSION_REQUEST) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            //noinspection StatementWithEmptyBody
            if (granted) {
                startJasminService();
            } else {
                ///
            }
        }
    }
}
