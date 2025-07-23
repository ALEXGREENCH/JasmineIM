package ru.ivansuper.jasmin.base;

import android.app.Instrumentation;

/**
 * Custom Instrumentation class.
 * This class extends the base Android Instrumentation class and can be used to
 * override default Android testing behavior or to provide custom application
 * lifecycle management during testing.
 * <p>
 * The {@code @noinspection unused} annotation is present because this class might be
 * instantiated by the Android framework via reflection during testing, even if it's
 * not directly referenced in the application code.
 */
public class Ins extends Instrumentation {

}