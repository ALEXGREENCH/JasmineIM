package ru.ivansuper.jasmin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

public class MyButton extends Button {
    public MyButton(Context context) {
        super(context);
        init();
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(this);
    }
}
