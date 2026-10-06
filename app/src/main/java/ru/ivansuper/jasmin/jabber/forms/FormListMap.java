package ru.ivansuper.jasmin.jabber.forms;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Arrays;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.resources;

public class FormListMap extends BaseAdapter {
    private String[] labels;
    private boolean multiselection;
    private boolean[] selections;
    private String[] values;

    public FormListMap(String[] strArr, String[] strArr2) {
        this.labels = strArr;
        this.values = strArr2;
        this.selections = new boolean[strArr2.length];
    }

    private void resetSelection() {
        Arrays.fill(this.selections, false);
    }

    @Override
    public int getCount() {
        return this.values.length;
    }

    @Override
    public String getItem(int i) {
        return this.values[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public String[] getSelected() {
        String[] strArr = new String[getSelectedCount()];
        int i = 0;
        for (int i2 = 0; i2 < this.values.length; i2++) {
            if (this.selections[i2]) {
                strArr[i] = this.values[i2];
                i++;
            }
        }
        return strArr;
    }

    public int getSelectedCount() {
        int i = 0;
        for (boolean z : this.selections) {
            i += z ? 1 : 0;
        }
        return i;
    }

    public String[] getSelectedLabels() {
        String[] strArr = new String[getSelectedCount()];
        int i = 0;
        for (int i2 = 0; i2 < this.values.length; i2++) {
            if (this.selections[i2]) {
                strArr[i] = this.labels[i2];
                i++;
            }
        }
        return strArr;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView;
        if (view == null) {
            textView = new TextView(resources.ctx);
            textView.setTextColor(-1);
            textView.setTextSize(14.0f);
            textView.setPadding(10, 10, 10, 10);
        } else {
            textView = (TextView) view;
        }
        textView.setText(this.labels[i].length() == 0 ? getItem(i) : this.labels[i]);
        textView.setBackgroundColor(this.selections[i] ? ColorScheme.getColor(47) : 0);
        return textView;
    }

    public boolean isMulti() {
        return this.multiselection;
    }

    public void setSelected(String str) {
        resetSelection();
        int i = 0;
        for (String str2 : this.values) {
            if (str2.equals(str)) {
                this.selections[i] = true;
                break;
            }
            i++;
        }
        notifyDataSetChanged();
    }

    public void setSelected(String[] strArr) {
        resetSelection();
        int i = 0;
        for (String str : this.values) {
            for (String str2 : strArr) {
                if (str.equals(str2)) {
                    this.selections[i] = true;
                }
            }
            i++;
        }
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean z) {
        this.multiselection = z;
    }

    public void toggleSelection(int i) {
        if (i < 0) {
            i = 0;
        }
        if (i >= this.values.length) {
            i = this.values.length - 1;
        }
        boolean z = this.selections[i];
        if (!this.multiselection) {
            resetSelection();
        }
        if (this.multiselection) {
            this.selections[i] = !z;
        } else {
            this.selections[i] = true;
        }
        notifyDataSetChanged();
    }
}
