package ru.ivansuper.jasmin.color_editor;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

public class ColorEditorActivity extends Activity {
    
    private SeekBar alpha;
    @SuppressWarnings("FieldCanBeLocal")
    private Button apply;
    private SeekBar blue;
    @SuppressWarnings("FieldCanBeLocal")
    private Button cancel;
    private boolean changing_by_bars;
    private EditText color_hex;
    private ListView colors;
    private SeekBar green;
    private int lastSelected;
    private ImageView preview;
    private SeekBar red;
    private LinearLayout selector;
    private boolean selectorVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_editor);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
    }

    @Override
    public void onBackPressed() {
        if (selectorVisible) {
            hideSelector();
        } else {
            finish();
        }
    }

    @Override
    protected Dialog onCreateDialog(int type) {
        if (type != 0) {
            return null;
        }
        UAdapter adp = new UAdapter();
        adp.setMode(2);
        adp.setTextSize(18);
        adp.setPadding(15);
        adp.put(resources.getString("s_reset_to_defaults"), 0);
        if (Environment.getExternalStorageState().equals("mounted")) {
            adp.put("Импорт (/Jasmine/colors.cfg)", 1);
            adp.put("Экспорт (/Jasmine/colors.cfg)", 2);
        }
        //noinspection UnnecessaryLocalVariable
        Dialog ad = DialogBuilder.createWithNoHeader(this, adp, 48, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                removeDialog(0);
                switch (i) {
                    case 0:
                        ColorScheme.setDefault();
                        applyAndSave();
                        return;
                    case 1:
                        ColorScheme.fillFromExternalFile();
                        applyAndSave();
                        return;
                    case 2:
                        ColorScheme.saveToExternalFile();
                        return;
                    default:
                }
            }
        });
        return ad;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        removeDialog(0);
        showDialog(0);
        return false;
    }

    private void initViews() {
        color_hex = findViewById(R.id.color_editor_hex);
        resources.attachEditText(color_hex);
        color_hex.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!changing_by_bars) {
                    try {
                        int color = (int) Long.parseLong(s.toString(), 16);
                        setColorToBars(color, false);
                        preview.setBackgroundColor(color);
                    } catch (Exception ignored) {}
                }
            }
        });
        colors = findViewById(R.id.color_editor_list);
        colors.setSelector(resources.getListSelector());
        colors.setAdapter(new ColorsAdapter(this));
        colors.setOnItemClickListener(new ColorListListener());
        selector = findViewById(R.id.color_editor_selector);
        preview = findViewById(R.id.color_editor_dialog_preview);
        alpha = findViewById(R.id.color_editor_dialog_alpha);
        red = findViewById(R.id.color_editor_dialog_red);
        green = findViewById(R.id.color_editor_dialog_green);
        blue = findViewById(R.id.color_editor_dialog_blue);
        alpha.setOnSeekBarChangeListener(new SeekListener());
        red.setOnSeekBarChangeListener(new SeekListener());
        green.setOnSeekBarChangeListener(new SeekListener());
        blue.setOnSeekBarChangeListener(new SeekListener());
        preview.setBackgroundColor(getColorFromBars());
        apply = findViewById(R.id.color_editor_dialog_apply);
        resources.attachButtonStyle(apply);
        apply.setText(resources.getString("s_apply"));
        apply.setOnClickListener(new ApplyListener());
        cancel = findViewById(R.id.color_editor_dialog_cancel);
        resources.attachButtonStyle(cancel);
        cancel.setText(resources.getString("s_cancel"));
        cancel.setOnClickListener(new CancelListener());
        ((TextView) findViewById(R.id.colors_preview)).setText(resources.getString("s_preview"));
        ((TextView) findViewById(R.id.colors_alpha)).setText(resources.getString("s_alpha"));
        ((TextView) findViewById(R.id.colors_red)).setText(resources.getString("s_red"));
        ((TextView) findViewById(R.id.colors_green)).setText(resources.getString("s_green"));
        ((TextView) findViewById(R.id.colors_blue)).setText(resources.getString("s_blue"));
    }

    public int getColorFromBars() {
        int a = alpha.getProgress();
        int r = red.getProgress();
        int g = green.getProgress();
        int b = blue.getProgress();
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void setColorToBars(int color, boolean a) {
        alpha.setProgress((color >> 24) & 255);
        red.setProgress((color >> 16) & 255);
        green.setProgress((color >> 8) & 255);
        blue.setProgress(color & 255);
        if (a) {
            color_hex.setText(getHEXColor(color));
        }
    }

    public String getHEXColor(int source) {
        StringBuilder res = new StringBuilder(Integer.toHexString(source));
        int to_add = 8 - res.length();
        for (int i = 0; i < to_add; i++) {
            res.insert(0, "0");
        }
        return res.toString();
    }

    public void showSelector() {
        selector.setVisibility(View.VISIBLE);
        selectorVisible = true;
    }

    public void hideSelector() {
        selector.setVisibility(View.INVISIBLE);
        selectorVisible = false;
    }


    public class ApplyListener implements View.OnClickListener {
        
        @Override
        public void onClick(View v) {
            ColorScheme.colors.set(lastSelected, getColorFromBars());
            applyAndSave();
            hideSelector();
        }
    }

    public void applyAndSave() {
        ColorScheme.saveToInternalFile();
        ((ColorsAdapter) colors.getAdapter()).notifyDataSetChanged();
    }
    
    public class CancelListener implements View.OnClickListener {
        
        @Override
        public void onClick(View v) {
            hideSelector();
        }
    }
    
    public class SeekListener implements SeekBar.OnSeekBarChangeListener {
        
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                int color = getColorFromBars();
                preview.setBackgroundColor(color);
                color_hex.setText(getHEXColor(color));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            changing_by_bars = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            changing_by_bars = false;
        }
    }


    public class ColorListListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long l) {
            lastSelected = arg2;
            ColorsAdapter adp = (ColorsAdapter) adapterView.getAdapter();
            showSelector();
            setColorToBars(adp.getItem(arg2), true);
            preview.setBackgroundColor(getColorFromBars());
        }
    }
}
