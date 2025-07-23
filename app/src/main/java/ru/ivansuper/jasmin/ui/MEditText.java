package ru.ivansuper.jasmin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * A custom EditText widget that applies a specific background drawable
 * and registers itself with the application's resources.
 *
 * <p>This class extends the standard {@link EditText} and provides
 * a consistent look and feel for text input fields within the application.
 * The {@code init()} method is called during construction to set the background
 * and attach the EditText to the application's resource management.
 */
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