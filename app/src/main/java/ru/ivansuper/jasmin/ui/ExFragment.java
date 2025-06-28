package ru.ivansuper.jasmin.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;

import java.util.Iterator;
import java.util.Vector;

/** @noinspection unused*/
public class ExFragment extends JFragment {

    public Activity ACTIVITY;
    private final DialogManager mDialogManager = new DialogManager();

    @Override
    public void onAttach(Activity activity) {
        this.ACTIVITY = activity;
        super.onAttach(activity);
    }

    public void setTheme(int theme) {
        this.ACTIVITY.setTheme(theme);
    }

    public SharedPreferences getDefaultSharedPreferences() {
        //noinspection deprecation
        return PreferenceManager.getDefaultSharedPreferences(this.ACTIVITY);
    }

    public void setVolumeControlStream(int stream) {
        this.ACTIVITY.setVolumeControlStream(stream);
    }

    public Window getWindow() {
        return this.ACTIVITY.getWindow();
    }

    public Object getSystemService(String service) {
        return this.ACTIVITY.getSystemService(service);
    }

    public void finish() {
        this.ACTIVITY.finish();
    }

    public View findViewById(int id) {
        View v = getView();
        return v != null ? v.findViewById(id) : null;
    }

    public Intent getIntent() {
        return this.ACTIVITY.getIntent();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // override in subclass if needed
    }

    public void onConfigurationChanged(Configuration configuration, int diff) {
        // override in subclass if needed
    }

    public void showDialog(int id) {
        Dialog d = onCreateDialog(id);
        if (d != null) {
            this.mDialogManager.showDialog(id, d);
        }
    }

    public Dialog onCreateDialog(int id) {
        return null; // override in subclass if needed
    }

    public void removeDialog(int id) {
        this.mDialogManager.removeDialog(id);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ExFragmentManager.addFragment(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ExFragmentManager.removeFragment(this);
    }

    private static class DialogManager {
        private final Vector<DialogItem> dialogs = new Vector<>();

        private synchronized Dialog getById(int id) {
            for (DialogItem item : dialogs) {
                if (item.id == id) {
                    return item.dialog;
                }
            }
            return null;
        }

        public synchronized void showDialog(int id, Dialog d) {
            Dialog existingDialog = getById(id);
            if (existingDialog == null) {
                DialogItem item = new DialogItem();
                item.id = id;
                item.dialog = d;
                dialogs.add(item);
                existingDialog = d;
            }
            existingDialog.show();
        }

        public synchronized void removeDialog(int id) {
            Dialog d = getById(id);
            if (d != null && d.isShowing()) {
                d.dismiss();
            }
            Iterator<DialogItem> it = dialogs.iterator();
            while (it.hasNext()) {
                DialogItem item = it.next();
                if (item.id == id) {
                    it.remove();
                    break;
                }
            }
        }

        private static class DialogItem {
            public Dialog dialog;
            public int id;
        }
    }
}
