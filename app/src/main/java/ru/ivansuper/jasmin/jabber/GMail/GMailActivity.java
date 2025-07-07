package ru.ivansuper.jasmin.jabber.GMail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

    @SuppressLint("SetTextI18n")
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
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Intent i = new Intent("android.intent.action.VIEW");
                i.setData(Uri.parse(adapter.getItem(index).url));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        listener = new GMailListener() {
            @Override
            public void onListChanged() {
                doRefresh();
                Toast toast = Toast.makeText(GMailActivity.this, resources.getString("s_mail_list_refreshed"), Toast.LENGTH_SHORT);
                toast.setGravity(48, 0, 0);
                toast.show();
            }
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
