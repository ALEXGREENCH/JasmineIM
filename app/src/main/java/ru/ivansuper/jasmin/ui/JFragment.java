package ru.ivansuper.jasmin.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

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
