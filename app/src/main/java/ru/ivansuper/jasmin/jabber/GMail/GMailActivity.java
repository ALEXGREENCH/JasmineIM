package ru.ivansuper.jasmin.jabber.GMail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

public class GMailActivity extends Activity {
    
    public static GMailListener listener;
    public static JProfile profile;
    public static boolean visible;
    private GMailAdapter adapter;
    @SuppressWarnings("FieldCanBeLocal")
    private ListView list;

    @SuppressLint({"WrongConstant", "SetTextI18n"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        visible = true;
        setContentView(R.layout.google_mail);
        SystemBarUtils.setupTransparentBars(this);
        list = findViewById(R.id.gmail_list);
        adapter = new GMailAdapter(profile.google_mail);
        TextView title = findViewById(R.id.gmail_title);
        title.setText(profile.ID + "@" + profile.host + " (" + adapter.getCount() + ")");
        list.setAdapter(adapter);
        list.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            Intent i = new Intent("android.intent.action.VIEW");
            i.setData(Uri.parse(adapter.getItem(arg2).url));
            i.setFlags(268435456);
            startActivity(i);
        });
        listener = () -> {
            doRefresh();
            Toast toast = Toast.makeText(GMailActivity.this, resources.getString("s_mail_list_refreshed"), 0);
            toast.setGravity(48, 0, 0);
            toast.show();
        };
        profile.gmail_listener = listener;
        profile.svc.cancelMessageNotification(profile.mail_notify_id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        visible = false;
        profile.gmail_listener = null;
    }

    @SuppressLint("SetTextI18n")
    public void doRefresh() {
        TextView title = findViewById(R.id.gmail_title);
        title.setText(profile.ID + "@" + profile.host + " (" + adapter.getCount() + ")");
        adapter.init(profile.google_mail);
    }
}
