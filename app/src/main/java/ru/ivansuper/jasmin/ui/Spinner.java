package ru.ivansuper.jasmin.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.jabber.forms.FormListMap;
import ru.ivansuper.jasmin.resources;

public class Spinner extends View {
    public OnSelectListener listener;
    private FormListMap mAdapter;
    private Dialog mPicker;
    private TextPaint text_;
    private int width;

    public interface OnSelectListener {
        void OnSelect(String[] strArr, String[] strArr2);
    }

    public Spinner(Context context) {
        super(context);
        this.width = 0;
        init();
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.width = 0;
        init();
    }

    private void init() {
        setClickable(true);
        this.text_ = new TextPaint();
        this.text_.setColor(-16777216);
        this.text_.setAntiAlias(true);
        this.text_.setTextSize(14.0f * resources.dm.density);
        setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.btn_default_small));
        resources.attachButtonStyle(this);
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner.this.showPicker();
            }
        });
    }

    private void showPicker() {
        this.mPicker = new Dialog(resources.service, R.style.DialogTheme);
        Window window = this.mPicker.getWindow();
        resources.attachDialogStyle(window);
        int w = getWidth();
        //noinspection DataFlowIssue
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        lp.gravity = Gravity.CENTER;
        lp.width = w;
        lp.windowAnimations = 0;
        window.setAttributes(lp);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        ListView list = new ListView(getContext());
        list.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        list.setDivider(new ColorDrawable(ColorScheme.getColor(44)));
        list.setDividerHeight(1);
        list.setSelector(resources.getListSelector());
        list.setAdapter(this.mAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner.this.mAdapter.toggleSelection(i);
                if (!Spinner.this.mAdapter.isMulti()) {
                    Spinner.this.mPicker.dismiss();
                }
                if (Spinner.this.listener != null) {
                    Spinner.this.listener.OnSelect(Spinner.this.mAdapter.getSelectedLabels(), Spinner.this.mAdapter.getSelected());
                }
                Spinner.this.invalidate();
            }
        });
        lay.addView(list);
        window.setContentView(lay);
        this.mPicker.show();
    }

    public void setAdapter(FormListMap adapter) {
        this.mAdapter = adapter;
        invalidate();
    }

    public FormListMap getAdapter() {
        return this.mAdapter;
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mPicker != null) {
            this.mPicker.dismiss();
        }
    }

    @Override
    public void onMeasure(int a, int b) {
        super.onMeasure(a, b);
        int w = View.MeasureSpec.getSize(a);
        this.width = w;
        this.width -= getPaddingLeft() + getPaddingRight();
        int padding = getPaddingTop() + getPaddingBottom();
        int height = padding + ((int) this.text_.getTextSize());
        int d_height = getBackground() == null ? 0 : getBackground().getIntrinsicHeight();
        setMeasuredDimension(w, Math.max(height, d_height));
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        @SuppressLint("DrawAllocation")
        StringBuilder selected = new StringBuilder("...");
        if (this.mAdapter != null && this.mAdapter.getSelectedCount() > 0) {
            selected = new StringBuilder();
            String[] selected_ = this.mAdapter.getSelectedLabels();
            for (int i = 0; i < selected_.length; i++) {
                String s = selected_[i];
                if (i == selected_.length - 1) {
                    selected.append(s);
                } else {
                    selected.append(s).append(", ");
                }
            }
        }
        CharSequence selected2 = TextUtils.ellipsize(selected.toString(), this.text_, this.width, TextUtils.TruncateAt.MARQUEE);
        float size = (-this.text_.getFontMetricsInt().ascent) - this.text_.getFontMetricsInt().descent;
        float top = ((float) getMeasuredHeight() / 2) + (size / 2.0f);
        canvas.drawText(selected2.toString(), getPaddingLeft(), top, this.text_);
    }
}