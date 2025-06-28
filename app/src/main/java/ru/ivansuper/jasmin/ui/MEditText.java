package ru.ivansuper.jasmin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

public class MEditText extends EditText {
    public MEditText(Context context) {
        super(context);
        init();
    }

    public MEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.edit_text);
        resources.attachEditText(this);
    }
}