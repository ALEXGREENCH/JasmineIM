package ru.ivansuper.jasmin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * A custom Button widget that applies a default background and style.
 *
 * <p>This class extends the standard {@link Button} and automatically
 * sets a predefined background drawable ({@code R.drawable.btn_default_small})
 * and applies a custom button style using {@code resources.attachButtonStyle(this)}
 * during its initialization.
 *
 * <p>It provides constructors that mirror the standard Button constructors, allowing
 * it to be used both programmatically and in XML layouts.
 */
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
