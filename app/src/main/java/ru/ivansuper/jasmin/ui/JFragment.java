package ru.ivansuper.jasmin.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

/**
 * JFragment is a base class for fragments in the Jasmin application.
 * It provides a basic structure for managing the lifecycle and UI of a fragment.
 * <p>
 * Fragments are used to create modular and reusable UI components.
 * They can be combined to create complex user interfaces and can be easily
 * managed by the Android system.
 * <p>
 * This class provides methods for attaching the fragment to an activity,
 * setting the content view, and handling lifecycle events such as creation,
 * starting, pausing, resuming, and destruction.
 * <p>
 * Subclasses should override the lifecycle methods to implement their own
 * custom logic.
 *
 * @see android.app.Fragment
 */
public class JFragment {
    private Activity mActivity;
    private int mContainer;
    private View mContent;
    private int mContentId;
    private boolean mPaused;

    public void onAttach(Activity activity) {
        this.mActivity = activity;
    }

    public void setContentView(int res_id) {
        this.mContentId = res_id;
    }

    /** @noinspection unused*/
    public void createContent() {
        this.mContent = View.inflate(this.mActivity, this.mContentId, null);
    }

    public final View getView() {
        return this.mContent;
    }

    public void onCreate() {
    }

    public void onStart() {
    }

    public void onPause() {
        this.mPaused = true;
    }

    public void onResume() {
        this.mPaused = false;
    }

    public void onDestroy() {
    }

    public final int getId() {
        return this.mContainer;
    }

    public final void setId(int container) {
        this.mContainer = container;
    }

    public final boolean isPaused() {
        return this.mPaused;
    }

    public final void startActivity(Intent intent) {
        this.mActivity.startActivity(intent);
    }
}
