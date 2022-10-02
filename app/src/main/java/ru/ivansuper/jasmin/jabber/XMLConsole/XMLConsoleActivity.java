package ru.ivansuper.jasmin.jabber.XMLConsole;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.resources;

public class XMLConsoleActivity extends Activity implements Handler.Callback {
    
    private static ConsoleAdapter adapter;
    public static JProfile profile;
    public static Handler updater;
    @SuppressWarnings("FieldCanBeLocal")
    private ListView console;
    @SuppressWarnings("FieldCanBeLocal")
    private CheckBox enabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xml_console);
        setVolumeControlStream(3);
        console = (ListView) findViewById(R.id.xml_console_list);
        console.setOnItemLongClickListener((arg0, arg1, arg2, arg3) -> {
            //noinspection deprecation
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            //noinspection deprecation
            cm.setText(((Stanzas) arg0.getAdapter().getItem(arg2)).xml.toString());
            Toast msg = Toast.makeText(XMLConsoleActivity.this, resources.getString("s_copied"), Toast.LENGTH_SHORT);
            msg.setGravity(48, 0, 0);
            msg.show();
            return false;
        });
        enabled = (CheckBox) findViewById(R.id.xml_console_enable);
        enabled.setText(resources.getString("s_xml_console_on_off"));
        if (profile != null) {
            enabled.setChecked(profile.CONSOLE_ENABLED);
        }
        enabled.setOnCheckedChangeListener((arg0, checked) -> XMLConsoleActivity.profile.CONSOLE_ENABLED = checked);
        Button clear = (Button) findViewById(R.id.xml_console_clear);
        clear.setText(resources.getString("s_xml_console_clear"));
        clear.setOnClickListener(arg0 -> {
            if (XMLConsoleActivity.profile != null) {
                XMLConsoleActivity.profile.clearConsole();
            }
        });
        adapter = new ConsoleAdapter(profile.CONSOLE);
        console.setAdapter((ListAdapter) adapter);
        updater = new Handler(this);
    }

    @SuppressWarnings("unused")
    public static void update(JProfile jprofile) {
        if (profile != null && profile.equals(jprofile)) {
            updater.sendEmptyMessage(0);
        }
    }

    @Override
    public boolean handleMessage(Message arg0) {
        adapter.notifyDataSetChanged();
        return false;
    }
}
