package ru.ivansuper.jasmin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * A custom CheckBox widget with a predefined button drawable and style.
 *
 * <p>This class extends the standard Android {@link CheckBox} and applies
 * a specific drawable ({@code R.drawable.btn_check}) as its button and
 * attaches a custom style using {@code resources.attachCheckStyle(this)}.
 */
public class MCheckBox extends CheckBox {

    public MCheckBox(Context context) {
        super(context);
        init(context);
    }

    public MCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /** @noinspection unused*/
    private void init(Context context) {
        setButtonDrawable(R.drawable.btn_check);
        resources.attachCheckStyle(this);
    }
}