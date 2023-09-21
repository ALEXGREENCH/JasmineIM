package ru.ivansuper.jasmin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

public class MCheckBox extends CheckBox {

    public MCheckBox(Context context) {
        super(context);
        init(context);
    }

    public MCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setButtonDrawable(R.drawable.btn_check);
        resources.attachCheckStyle(this);
    }
}