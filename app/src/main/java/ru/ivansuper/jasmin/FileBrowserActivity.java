package ru.ivansuper.jasmin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;

public class FileBrowserActivity extends Activity {
    
    @SuppressWarnings("FieldCanBeLocal")
    private files_adapter adp;
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(3);
        setContentView(R.layout.file_browser_activity);
        initViews();
        adp = new files_adapter();
        File sd = new File(resources.SD_PATH);
        adp.setData(sd.listFiles(), sd.getParentFile());
        list.setAdapter((ListAdapter) adp);
        list.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            files_adapter adp = (files_adapter) arg0.getAdapter();
            if (arg2 == 0) {
                File path = adp.parent;
                if (path != null) {
                    adp.setData(path.listFiles(), path.getParentFile());
                    return;
                }
                return;
            }
            File item = adp.getItem(arg2);
            if (item.isDirectory()) {
                adp.setData(item.listFiles(), item.getParentFile());
                return;
            }
            Intent result = new Intent();
            result.setAction(item.getAbsolutePath());
            setResult(-1, result);
            finish();
        });
    }

    private void initViews() {
        list = (ListView) findViewById(R.id.file_browser_file_list);
        list.setSelector(resources.getListSelector());
    }
}
