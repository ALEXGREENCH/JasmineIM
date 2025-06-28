package ru.ivansuper.jasmin.ui;

import android.app.Activity;
import android.view.ViewGroup;

import java.util.Vector;

public class JFragmentActivity extends Activity {
    private final Vector<JFragment> mStack = new Vector<>();

    /** @noinspection unused*/
    public final JFragment findFragment(int id) {
        for (JFragment f : this.mStack) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    public final void attachFragment(int container, JFragment fragment) {
        fragment.setId(container);
        fragment.onAttach(this);
        fragment.onCreate();
        fragment.createContent();
        ViewGroup vg = findViewById(container);
        vg.addView(fragment.getView());
        fragment.onStart();
        fragment.onResume();
        this.mStack.add(fragment);
    }

    public final void removeFragment(int id) {
        for (JFragment f : this.mStack) {
            if (f.getId() == id) {
                f.onPause();
                f.onDestroy();
                ViewGroup vg = findViewById(f.getId());
                vg.removeView(f.getView());
                this.mStack.remove(f);
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for (JFragment f : this.mStack) {
            if (f.isPaused()) {
                f.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (JFragment f : this.mStack) {
            if (!f.isPaused()) {
                f.onPause();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (JFragment f : this.mStack) {
            if (!f.isPaused()) {
                f.onPause();
            }
            f.onDestroy();
        }
        this.mStack.clear();
    }
}