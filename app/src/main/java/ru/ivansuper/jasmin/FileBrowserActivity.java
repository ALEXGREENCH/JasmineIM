package ru.ivansuper.jasmin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

import ru.ivansuper.jasmin.utils.SystemBarUtils;

public class FileBrowserActivity extends Activity {
    
    @SuppressWarnings("FieldCanBeLocal")
    private files_adapter adp;
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(3);
        setContentView(R.layout.file_browser_activity);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
        adp = new files_adapter();
        File sd = new File(resources.SD_PATH);
        adp.setData(sd.listFiles(), sd.getParentFile());
        list.setAdapter(adp);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                files_adapter adp = (files_adapter) adapterView.getAdapter();
                if (i == 0) {
                    File path = adp.parent;
                    if (path != null) {
                        adp.setData(path.listFiles(), path.getParentFile());
                        return;
                    }
                    return;
                }
                File item = adp.getItem(i);
                if (item.isDirectory()) {
                    adp.setData(item.listFiles(), item.getParentFile());
                    return;
                }
                Intent result = new Intent();
                result.setAction(item.getAbsolutePath());
                setResult(-1, result);
                finish();
            }
        });
    }

    private void initViews() {
        list = (ListView) findViewById(R.id.file_browser_file_list);
        list.setSelector(resources.getListSelector());
    }
}
