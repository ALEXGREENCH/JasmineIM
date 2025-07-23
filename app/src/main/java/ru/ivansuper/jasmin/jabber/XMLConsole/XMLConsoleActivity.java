package ru.ivansuper.jasmin.jabber.XMLConsole;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * Activity for displaying and managing the XML console.
 *
 * <p>The XML console displays XML stanzas exchanged between the client and server.
 * Users can enable/disable the console, clear its contents, and copy individual stanzas
 * to the clipboard.
 *
 * <p>This activity uses a {@link ConsoleAdapter} to display the stanzas in a ListView.
 * It also uses a {@link Handler} to update the console when new stanzas are received.
 *
 * @see ConsoleAdapter
 * @see Stanzas
 * @see JProfile
 */
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
        SystemBarUtils.setupTransparentBars(this);
        setVolumeControlStream(3);
        console = findViewById(R.id.xml_console_list);
        console.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                //noinspection deprecation
                cm.setText(((Stanzas) adapterView.getAdapter().getItem(i)).xml.toString());
                Toast msg = Toast.makeText(XMLConsoleActivity.this, resources.getString("s_copied"), Toast.LENGTH_SHORT);
                msg.setGravity(48, 0, 0);
                msg.show();
                return false;
            }
        });
        enabled = findViewById(R.id.xml_console_enable);
        enabled.setText(resources.getString("s_xml_console_on_off"));
        if (profile != null) {
            enabled.setChecked(profile.CONSOLE_ENABLED);
        }
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                XMLConsoleActivity.profile.CONSOLE_ENABLED = checked;
            }
        });
        Button clear = findViewById(R.id.xml_console_clear);
        clear.setText(resources.getString("s_xml_console_clear"));
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (XMLConsoleActivity.profile != null) {
                    XMLConsoleActivity.profile.clearConsole();
                }
            }
        });
        adapter = new ConsoleAdapter(profile.CONSOLE);
        console.setAdapter(adapter);
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
