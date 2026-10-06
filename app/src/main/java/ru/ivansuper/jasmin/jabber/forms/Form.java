package ru.ivansuper.jasmin.jabber.forms;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.util.Vector;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.jabber.AbstractForm;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.ui.MCheckBox;
import ru.ivansuper.jasmin.ui.Spinner;
import ru.ivansuper.jasmin.utilities;

public class Form extends AbstractForm {
    public static final int TYPE_CANCEL = 0;
    public static final int TYPE_FORM = 1;
    public static final int TYPE_RESULT = 2;
    public static final int TYPE_SUBMIT = 3;
    public String INSTR;
    public int TYPE;
    private LinearLayout form_layout;
    public Operation operation;
    public Vector<Field> fields = new Vector<>();
    public Vector<View> fields_wrappers = new Vector<>();
    public boolean it_is_report = false;
    public Vector<Field> report_cell = new Vector<>();
    public int report_cell_size = 0;
    public Vector<String> report_list = new Vector<>();

    public static class Field {
        public static final int TYPE_BOOLEAN = 0;
        public static final int TYPE_FIXED = 1;
        public static final int TYPE_HIDDEN = 2;
        public static final int TYPE_JID_MULTI = 3;
        public static final int TYPE_JID_SINGLE = 4;
        public static final int TYPE_LIST_MULTI = 5;
        public static final int TYPE_LIST_SINGLE = 6;
        public static final int TYPE_TEXT_MULTI = 7;
        public static final int TYPE_TEXT_PRIVATE = 8;
        public static final int TYPE_TEXT_SINGLE = 9;
        public String DESC;
        public String LABEL;
        public String[] LABELS;
        public int TYPE;
        public String VALUE1;
        public boolean VALUE2;
        public String[] VALUE3;
        public String[] VALUE3_;
        public String VAR;
        public Media media;
        public boolean required;
        public boolean type_defined;

        public static class Media {
            public static final int TYPE_AUDIO = 0;
            public static final int TYPE_IMAGE = 1;
            public static final int TYPE_NOT_SPECIFIED = 3;
            public static final int TYPE_VIDEO = 2;
            public int TYPE;
            public byte[] content;
            public String content_url;
            public boolean ready;

            public static final int detectType(String str) {
                if (str != null) {
                    if (str.contains("audio/")) {
                        return 0;
                    }
                    if (str.contains("image/")) {
                        return 1;
                    }
                    if (str.contains("video/")) {
                        return 2;
                    }
                }
                return 3;
            }
        }

        public static final int detectType(String str) {
            if (str != null) {
                if (str.equalsIgnoreCase("boolean")) {
                    return 0;
                }
                if (str.equalsIgnoreCase("fixed")) {
                    return 1;
                }
                if (str.equalsIgnoreCase("hidden")) {
                    return 2;
                }
                if (str.equalsIgnoreCase("jid-multi")) {
                    return 3;
                }
                if (str.equalsIgnoreCase("jid-single")) {
                    return 4;
                }
                if (str.equalsIgnoreCase("list-multi")) {
                    return 5;
                }
                if (str.equalsIgnoreCase("list-single")) {
                    return 6;
                }
                if (str.equalsIgnoreCase("text-multi")) {
                    return 7;
                }
                if (str.equalsIgnoreCase("text-private")) {
                    return 8;
                }
                str.equalsIgnoreCase("text-single");
            }
            return 9;
        }
    }

    private final void buildReport() {
        for (final String str : this.report_list) {
            TextView textView = new TextView(resources.ctx);
            textView.setBackgroundDrawable(resources.getListSelector());
            textView.setTextColor(-1);
            textView.setTextSize(14.0f);
            textView.setText(str);
            textView.setPadding(5, 5, 5, 5);
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    resources.service.moveToClipboard(str);
                    return true;
                }
            });
            this.form_layout.addView(textView);
            LinearLayout linearLayout = new LinearLayout(resources.ctx);
            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
            linearLayout.setBackgroundColor(ColorScheme.getColor(44));
            this.form_layout.addView(linearLayout);
        }
    }

    public static final int detectType(String str) {
        if (str != null) {
            if (str.equalsIgnoreCase("cancel")) {
                return 0;
            }
            if (!str.equalsIgnoreCase("form")) {
                if (str.equalsIgnoreCase("result")) {
                    return 2;
                }
                if (str.equalsIgnoreCase("submit")) {
                    return 3;
                }
            }
        }
        return 1;
    }

    public final void build() {
        TextView mCheckBox;
        View view;
        EditText editText;
        this.form_layout = new LinearLayout(resources.ctx);
        this.form = new ScrollView(resources.ctx);
        this.form.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        this.form_layout.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        this.form_layout.setOrientation(1);
        this.form.addView(this.form_layout);
        if (this.INSTR != null) {
            TextView textView = new TextView(resources.ctx);
            textView.setTextColor(-1);
            textView.setTextSize(14.0f);
            textView.setText(this.INSTR);
            textView.setCompoundDrawables(null, null, null, resources.ctx.getResources().getDrawable(0x7f02000b));
            this.form_layout.addView(textView);
        }
        if (this.it_is_report) {
            buildReport();
        }
        for (Field field : this.fields) {
            if (field.media != null && field.TYPE != 2 && field.media.ready) {
                switch (field.media.TYPE) {
                    case 1:
                        ImageView imageView = new ImageView(resources.ctx);
                        Drawable drawableCreateFromStream = Drawable.createFromStream(new ByteArrayInputStream(field.media.content), "image");
                        drawableCreateFromStream.setBounds(0, 0, drawableCreateFromStream.getIntrinsicWidth(), drawableCreateFromStream.getIntrinsicHeight());
                        imageView.setScaleType(ImageView.ScaleType.CENTER);
                        imageView.setImageDrawable(drawableCreateFromStream);
                        this.form_layout.addView(imageView);
                        break;
                }
            }
            if (field.LABEL != null && field.TYPE != 2 && field.TYPE != 0) {
                TextView textView2 = new TextView(resources.ctx);
                textView2.setTextColor(-1);
                textView2.setShadowLayer(1.0f, 1.0f, 1.0f, -16777216);
                textView2.setTextSize(14.0f);
                textView2.setText(field.LABEL);
                this.form_layout.addView(textView2);
            }
            if (field.DESC != null && field.TYPE != 2) {
                TextView textView3 = new TextView(resources.ctx);
                textView3.setTextColor(-1);
                textView3.setTextSize(14.0f);
                textView3.setText("(" + field.DESC + ")");
                this.form_layout.addView(textView3);
            }
            switch (field.TYPE) {
                case 0:
                    mCheckBox = new MCheckBox(resources.ctx);
                    mCheckBox.setTextColor(-1);
                    mCheckBox.setTextSize(14.0f);
                    ((MCheckBox) mCheckBox).setChecked(field.VALUE2);
                    mCheckBox.setText(field.LABEL);
                    this.form_layout.addView(mCheckBox);
                    this.fields_wrappers.add((View) mCheckBox);
                    LinearLayout linearLayout = new LinearLayout(resources.ctx);
                    linearLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout);
                    break;
                case 1:
                    mCheckBox = new TextView(resources.ctx);
                    mCheckBox.setTextColor(-1);
                    mCheckBox.setShadowLayer(1.0f, 1.0f, 1.0f, -16777216);
                    mCheckBox.setTextSize(14.0f);
                    mCheckBox.setText(field.VALUE1);
                    this.form_layout.addView(mCheckBox);
                    this.fields_wrappers.add((View) mCheckBox);
                    LinearLayout linearLayout2 = new LinearLayout(resources.ctx);
                    linearLayout2.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout2.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout2);
                    break;
                case 2:
                    break;
                case 3:
                    mCheckBox = new EditText(resources.ctx);
                    mCheckBox.setMinimumHeight(64);
                    mCheckBox.setBackgroundDrawable(resources.ctx.getResources().getDrawable(2130837551));
                    resources.attachEditText((EditText) mCheckBox);
                    mCheckBox.setTextSize(14.0f);
                    mCheckBox.setSingleLine(false);
                    for (String str : field.VALUE3) {
                        mCheckBox.append(String.valueOf(str) + "\n");
                    }
                    this.form_layout.addView(mCheckBox);
                    this.fields_wrappers.add((View) mCheckBox);
                    LinearLayout linearLayout3 = new LinearLayout(resources.ctx);
                    linearLayout3.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout3.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout3);
                    break;
                case 4:
                    editText = new EditText(resources.ctx);
                    editText.setBackgroundDrawable(resources.ctx.getResources().getDrawable(2130837551));
                    resources.attachEditText(editText);
                    editText.setTextSize(14.0f);
                    editText.setSingleLine(true);
                    mCheckBox = editText;
                    mCheckBox.setText(field.VALUE1);
                    this.form_layout.addView(mCheckBox);
                    this.fields_wrappers.add((View) mCheckBox);
                    LinearLayout linearLayout4 = new LinearLayout(resources.ctx);
                    linearLayout4.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout4.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout4);
                    break;
                case 5:
                    final FormListMap formListMap = new FormListMap(field.LABELS, field.VALUE3);
                    formListMap.setSelectionMode(true);
                    formListMap.setSelected(field.VALUE3_);
                    final ListView listView = new ListView(resources.ctx);
                    listView.setFocusable(true);
                    listView.setFocusableInTouchMode(true);
                    listView.setClickable(true);
                    listView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
                    listView.setSelector(resources.getListSelector());
                    listView.setAdapter((ListAdapter) formListMap);
                    utilities.setListViewHeightBasedOnChildren(listView);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view2, int i, long j) {
                            listView.requestFocus();
                            formListMap.toggleSelection(i);
                        }
                    });
                    view = listView;
                    this.form_layout.addView(view);
                    this.fields_wrappers.add(view);
                    LinearLayout linearLayout5 = new LinearLayout(resources.ctx);
                    linearLayout5.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout5.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout5);
                    break;
                case 6:
                    FormListMap formListMap2 = new FormListMap(field.LABELS, field.VALUE3);
                    formListMap2.setSelectionMode(false);
                    formListMap2.setSelected(field.VALUE3_);
                    Spinner spinner = new Spinner(resources.ctx);
                    spinner.setAdapter(formListMap2);
                    view = spinner;
                    this.form_layout.addView(view);
                    this.fields_wrappers.add(view);
                    LinearLayout linearLayout6 = new LinearLayout(resources.ctx);
                    linearLayout6.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout6.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout6);
                    break;
                case 7:
                    EditText editText2 = new EditText(resources.ctx);
                    editText2.setMinHeight(64);
                    editText2.setBackgroundDrawable(resources.ctx.getResources().getDrawable(2130837551));
                    resources.attachEditText(editText2);
                    editText2.setTextSize(14.0f);
                    editText2.setSingleLine(false);
                    mCheckBox = editText2;
                    mCheckBox.setText(field.VALUE1);
                    this.form_layout.addView(mCheckBox);
                    this.fields_wrappers.add((View) mCheckBox);
                    LinearLayout linearLayout7 = new LinearLayout(resources.ctx);
                    linearLayout7.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout7.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout7);
                    break;
                case 8:
                    editText = new EditText(resources.ctx);
                    editText.setBackgroundDrawable(resources.ctx.getResources().getDrawable(2130837551));
                    resources.attachEditText(editText);
                    editText.setTextSize(14.0f);
                    editText.setSingleLine(true);
                    mCheckBox = editText;
                    mCheckBox.setText(field.VALUE1);
                    this.form_layout.addView(mCheckBox);
                    this.fields_wrappers.add((View) mCheckBox);
                    LinearLayout linearLayout8 = new LinearLayout(resources.ctx);
                    linearLayout8.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout8.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout8);
                    break;
                case 9:
                    editText = new EditText(resources.ctx);
                    editText.setBackgroundDrawable(resources.ctx.getResources().getDrawable(2130837551));
                    resources.attachEditText(editText);
                    editText.setTextSize(14.0f);
                    editText.setSingleLine(true);
                    mCheckBox = editText;
                    mCheckBox.setText(field.VALUE1);
                    this.form_layout.addView(mCheckBox);
                    this.fields_wrappers.add((View) mCheckBox);
                    LinearLayout linearLayout9 = new LinearLayout(resources.ctx);
                    linearLayout9.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout9.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout9);
                    break;
                default:
                    LinearLayout linearLayout10 = new LinearLayout(resources.ctx);
                    linearLayout10.setLayoutParams(new ViewGroup.LayoutParams(-1, 2));
                    linearLayout10.setBackgroundColor(ColorScheme.getColor(44));
                    this.form_layout.addView(linearLayout10);
                    break;
            }
        }
    }

    @Override
    public void cancel() {
        this.operation.profile.stream.write(this.operation.compileCancel(), this.operation.profile);
    }

    @Override
    public void send() {
        this.operation.profile.stream.write(this.operation.compile(), this.operation.profile);
    }
}
