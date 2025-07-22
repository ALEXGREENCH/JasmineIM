package ru.ivansuper.jasmin.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.ui.LoadingView;

public class DialogBuilder {
    private static LinearLayout prepareContainer(Context context, String caption) {
        LinearLayout header = (LinearLayout) View.inflate(context, R.layout.dialog_header, null);
        LinearLayout divider = header.findViewById(R.id.dialog_hdr_divider);
        divider.setBackgroundColor(ColorScheme.getColor(44));
        TextView header_label = header.findViewById(R.id.dialog_header);
        header_label.setTextColor(ColorScheme.getColor(43));
        header_label.setText(caption);
        return header.findViewById(R.id.dialog_view);
    }

    public static Dialog create(Context context, String hdr, CharSequence text, int gravity) {
        return create(context, hdr, text, gravity, R.style.TopDialogAnimation);
    }

    public static Dialog create(Context context, String header, BaseAdapter adapter, AdapterView.OnItemClickListener listener) {
        return create(context, header, adapter, listener, true);
    }

    public static Dialog create(Context context, String header, BaseAdapter adapter, final AdapterView.OnItemClickListener listener, final boolean autoclose) {
        LinearLayout container = prepareContainer(context, header);
        ListView list = new ListView(context);
        list.setDividerHeight(0);
        list.setCacheColorHint(0);
        list.setSelector(resources.getListSelector());
        list.setAdapter(adapter);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        list.setLayoutParams(lay_p);
        container.addView(list);
        final Dialog d = new Dialog(context, R.style.DialogTheme);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (autoclose) {
                    d.dismiss();
                }
                listener.onItemClick(arg0, arg1, arg2, arg3);
            }
        });
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    /** @noinspection unused*/
    public static Dialog create(Context context, String hdr, CharSequence text, int gravity, int win_animation) {
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        TextView txt = new TextView(context);
        txt.setTextSize(16.0f);
        txt.setTextColor(-1);
        txt.setPadding(5, 5, 5, 5);
        txt.setLinkTextColor(-1);
        txt.setText(text);
        txt.setMovementMethod(LinkMovementMethod.getInstance());
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        ScrollView sv = new ScrollView(resources.ctx);
        sv.setLayoutParams(lay_p);
        sv.addView(txt);
        lay.addView(sv);
        container.addView(lay);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    /** @noinspection unused*/
    public static Dialog create(Context context, String hdr, View content, int gravity) {
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        content.setLayoutParams(lay_p);
        lay.addView(content);
        container.addView(lay);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    public static Dialog create(Context context, String hdr, UAdapter adapter, int gravity, AdapterView.OnItemClickListener listener) {
        return create(context, hdr, adapter, gravity, listener, R.style.TopDialogAnimation);
    }

    /** @noinspection unused*/
    public static Dialog create(Context context, String hdr, UAdapter adapter, int gravity, AdapterView.OnItemClickListener listener, int animation) {
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        ListView list = new ListView(context);
        list.setDividerHeight(0);
        list.setCacheColorHint(0);
        list.setSelector(resources.getListSelector());
        list.setAdapter(adapter);
        list.setOnItemClickListener(listener);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        list.setLayoutParams(lay_p);
        lay.addView(list);
        container.addView(lay);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    /** @noinspection unused*/
    public static Dialog createWithNoHeader(Context context, View content, int gravity) {
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        content.setLayoutParams(lay_p);
        lay.addView(content);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView(lay);
        return d;
    }

    /** @noinspection unused*/
    public static Dialog createWithNoHeader(Context context, BaseAdapter adapter, int gravity, final AdapterView.OnItemClickListener listener) {
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        ListView list = new ListView(context);
        list.setDividerHeight(0);
        list.setCacheColorHint(0);
        list.setSelector(resources.getListSelector());
        list.setAdapter(adapter);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        list.setLayoutParams(lay_p);
        lay.addView(list);
        final Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.2
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                d.dismiss();
                listener.onItemClick(arg0, arg1, arg2, arg3);
            }
        });
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView(lay);
        return d;
    }

    public static Dialog createWithNoHeader(Context context, BaseAdapter adapter, AdapterView.OnItemLongClickListener listener) {
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        ListView list = new ListView(context);
        list.setDividerHeight(0);
        list.setCacheColorHint(0);
        list.setSelector(resources.getListSelector());
        list.setAdapter(adapter);
        list.setOnItemLongClickListener(listener);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        list.setLayoutParams(lay_p);
        lay.addView(list);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView(lay);
        return d;
    }

    /** @noinspection unused*/
    public static Dialog createOk(Context context, String hdr, String text, String ok, int gravity, View.OnClickListener listener) {
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        EditText txt = new EditText(context);
        txt.setBackgroundDrawable(resources.ctx.getResources().getDrawable(R.drawable.smiley_and_send_btn));
        txt.setTextSize(16.0f);
        txt.setTextColor(-1);
        txt.setLinkTextColor(-1);
        txt.setPadding(5, 5, 5, 5);
        txt.setText(text);
        txt.setFilters(new InputFilter[]{new InputFilter() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.3
            @Override // android.text.InputFilter
            public CharSequence filter(CharSequence source, int arg1, int arg2, Spanned dest, int dstart, int dend) {
                return source.length() < 1 ? dest.subSequence(dstart, dend) : "";
            }
        }});
        txt.setInputType(131073);
        ScrollView sv = new ScrollView(resources.ctx);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        sv.setLayoutParams(lay_p);
        sv.addView(txt);
        lay.addView(sv);
        Button ok_btn = new Button(resources.ctx);
        ok_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(ok_btn);
        ok_btn.setText(ok);
        ok_btn.setOnClickListener(listener);
        lay.addView(ok_btn);
        container.addView(lay);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    public static Dialog createOk(Context context, View cnt, String hdr, String ok, int gravity, View.OnClickListener listener) {
        return createOk(context, cnt, hdr, ok, gravity, listener, false);
    }

    /** @noinspection unused*/
    public static Dialog createOk(Context context, View cnt, String hdr, String ok, int gravity, final View.OnClickListener listener, final boolean autoclose) {
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        cnt.setLayoutParams(lay_p);
        lay.addView(cnt);
        cnt.setPadding(5, 5, 5, 5);
        Button ok_btn = new Button(resources.ctx);
        ok_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(ok_btn);
        ok_btn.setText(ok);
        lay.addView(ok_btn);
        container.addView(lay);
        final Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        ok_btn.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (autoclose) {
                    d.dismiss();
                }
                listener.onClick(v);
            }
        });
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    public static Dialog createOk(Context context, BaseAdapter adapter, String hdr, String ok, View.OnClickListener btn_listener, AdapterView.OnItemLongClickListener listener) {
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        ListView list = new ListView(context);
        list.setStackFromBottom(true);
        list.setDividerHeight(0);
        list.setCacheColorHint(0);
        list.setSelector(resources.getListSelector());
        list.setAdapter(adapter);
        list.setOnItemLongClickListener(listener);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        list.setLayoutParams(lay_p);
        lay.addView(list);
        Button ok_btn = new Button(resources.ctx);
        ok_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(ok_btn);
        ok_btn.setText(ok);
        ok_btn.setOnClickListener(btn_listener);
        lay.addView(ok_btn);
        container.addView(lay);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    /** @noinspection unused*/
    public static Dialog createYesNo(Context context, int gravity, String hdr, String text, String yes, String no, View.OnClickListener yes_listener, final View.OnClickListener no_listener) {
        LinearLayout lay_ = new LinearLayout(resources.ctx);
        lay_.setPadding(5, 5, 5, 5);
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay_.setOrientation(LinearLayout.HORIZONTAL);
        Button yes_btn = new Button(resources.ctx);
        yes_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(yes_btn);
        yes_btn.setText(yes);
        yes_btn.setOnClickListener(yes_listener);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -2);
        llp.weight = 1.0f;
        yes_btn.setLayoutParams(llp);
        final Button no_btn = new Button(resources.ctx);
        no_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(no_btn);
        no_btn.setText(no);
        no_btn.setOnClickListener(no_listener);
        no_btn.setLayoutParams(llp);
        TextView txt = new TextView(resources.ctx);
        txt.setTextSize(16.0f);
        txt.setTextColor(-1);
        txt.setPadding(5, 5, 5, 5);
        txt.setText(text);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        txt.setLayoutParams(lay_p);
        LinearLayout.LayoutParams lay_p2 = new LinearLayout.LayoutParams(-1, -1, 1.0f);
        ScrollView scr = new ScrollView(resources.ctx);
        scr.setLayoutParams(lay_p2);
        scr.addView(txt);
        lay.addView(scr);
        lay_.addView(yes_btn);
        lay_.addView(no_btn);
        lay.addView(lay_);
        container.addView(lay);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        d.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.5
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                no_listener.onClick(no_btn);
            }
        });
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    public static Dialog createYesNo(Context context, View content, int gravity, String hdr, String yes, String no, View.OnClickListener yes_listener, View.OnClickListener no_listener) {
        return createYesNo(context, content, gravity, hdr, yes, no, yes_listener, no_listener, false);
    }

    /** @noinspection unused*/
    public static Dialog createYesNo(Context context, View content, int gravity, String hdr, String yes, String no, final View.OnClickListener yes_listener, final View.OnClickListener no_listener, final boolean autoclose) {
        LinearLayout lay_ = new LinearLayout(resources.ctx);
        lay_.setPadding(5, 5, 5, 5);
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay_.setOrientation(LinearLayout.HORIZONTAL);
        final Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        final Button yes_btn = new Button(resources.ctx);
        yes_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(yes_btn);
        yes_btn.setText(yes);
        yes_btn.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.6
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (autoclose) {
                    d.dismiss();
                }
                yes_listener.onClick(yes_btn);
            }
        });
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -2);
        llp.weight = 1.0f;
        yes_btn.setLayoutParams(llp);
        final Button no_btn = new Button(resources.ctx);
        no_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(no_btn);
        no_btn.setText(no);
        no_btn.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.7
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (autoclose) {
                    d.dismiss();
                }
                no_listener.onClick(no_btn);
            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.8
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                no_listener.onClick(no_btn);
            }
        });
        no_btn.setLayoutParams(llp);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        content.setLayoutParams(lay_p);
        lay.addView(content);
        lay_.addView(yes_btn);
        lay_.addView(no_btn);
        lay.addView(lay_);
        container.addView(lay);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    public static Dialog createYesNoCancel(Context context, View content, String hdr, String yes, String no, String cancel, final View.OnClickListener yes_listener, final View.OnClickListener no_listener, final View.OnClickListener cancel_listener) {
        LinearLayout lay_ = new LinearLayout(resources.ctx);
        lay_.setPadding(5, 5, 5, 5);
        LinearLayout container = prepareContainer(context, hdr);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay_.setOrientation(LinearLayout.HORIZONTAL);
        final Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCanceledOnTouchOutside(true);
        final Button yes_btn = new Button(resources.ctx);
        yes_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(yes_btn);
        yes_btn.setText(yes);
        yes_btn.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.9
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                d.dismiss();
                yes_listener.onClick(yes_btn);
            }
        });
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -2);
        llp.weight = 1.0f;
        yes_btn.setLayoutParams(llp);
        final Button no_btn = new Button(resources.ctx);
        no_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(no_btn);
        no_btn.setText(no);
        no_btn.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.10
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                d.dismiss();
                no_listener.onClick(no_btn);
            }
        });
        no_btn.setLayoutParams(llp);
        Button cancel_btn = new Button(resources.ctx);
        cancel_btn.setBackgroundResource(R.drawable.btn_default_small);
        resources.attachButtonStyle(cancel_btn);
        cancel_btn.setText(cancel);
        cancel_btn.setOnClickListener(new View.OnClickListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.11
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                d.dismiss();
                cancel_listener.onClick(no_btn);
            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: ru.ivansuper.jasmin.dialogs.DialogBuilder.12
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                cancel_listener.onClick(no_btn);
            }
        });
        no_btn.setLayoutParams(llp);
        LinearLayout.LayoutParams lay_p = new LinearLayout.LayoutParams(-1, -1, 5.0f);
        content.setLayoutParams(lay_p);
        lay.addView(content);
        lay_.addView(yes_btn);
        lay_.addView(no_btn);
        lay_.addView(cancel_btn);
        lay.addView(lay_);
        container.addView(lay);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        wnd.setAttributes(lp);
        wnd.setContentView((View) container.getParent());
        return d;
    }

    public static Dialog createProgress(Context context, String text, boolean cancelable) {
        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.HORIZONTAL);
        lay.setGravity(16);
        lay.setPadding(10, 10, 10, 10);
        LoadingView loading = new LoadingView(context);
        TextView txt = new TextView(context);
        txt.setTextSize(16.0f);
        txt.setTextColor(-1);
        txt.setPadding(10, 5, 5, 5);
        txt.setText(text);
        lay.addView(loading);
        lay.addView(txt);
        Dialog d = new Dialog(context, R.style.DialogTheme);
        d.setCancelable(cancelable);
        Window wnd = d.getWindow();
        //noinspection DataFlowIssue
        wnd.setSoftInputMode(3);
        wnd.setBackgroundDrawableResource(R.drawable.grey_back);
        resources.attachDialogStyle(wnd);
        WindowManager.LayoutParams lp = wnd.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = R.style.TopDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED;
        wnd.setAttributes(lp);
        wnd.setContentView(lay);
        return d;
    }
}