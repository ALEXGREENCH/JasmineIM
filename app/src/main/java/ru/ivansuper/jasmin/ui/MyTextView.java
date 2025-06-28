package ru.ivansuper.jasmin.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.ClipboardManager;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Vector;

import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.SmileysSelector;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.URLImageSpan;
import ru.ivansuper.jasmin.animate_tools.Movie;
import ru.ivansuper.jasmin.animate_tools.MySpan;
import ru.ivansuper.jasmin.chats.ICQChatActivity;
import ru.ivansuper.jasmin.chats.JChatActivity;
import ru.ivansuper.jasmin.chats.JConference;
import ru.ivansuper.jasmin.chats.MMPChatActivity;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.jabber.HttpDisco;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.bookmarks.BookmarkItem;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.popup_log_adapter;
import ru.ivansuper.jasmin.resources;

public class MyTextView extends View implements Handler.Callback {
    private boolean animated;
    private boolean forced_animation;
    private final Handler hdl;
    private BackgroundColorSpan highlight_span;
    private Layout layout;
    private boolean mClickHandled;
    private Dialog mContextMenu;
    private URLSpan mContextURL;
    private boolean mLongClickHandled;
    private int max_lines;
    private int max_x;
    private int max_y;
    private int min_x;
    private int min_y;
    private TextPaint paint;
    private int parentSpace;
    private int refresh_rate;
    private CharSequence text;

    public MyTextView(Context context) {
        super(context);
        this.refresh_rate = popup_log_adapter.DEFAULT_DISPLAY_TIME;
        this.hdl = new Handler(this);
        this.forced_animation = false;
        this.max_lines = 0;
        this.mClickHandled = false;
        this.mLongClickHandled = false;
        init();
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.refresh_rate = popup_log_adapter.DEFAULT_DISPLAY_TIME;
        this.hdl = new Handler(this);
        this.forced_animation = false;
        this.max_lines = 0;
        this.mClickHandled = false;
        this.mLongClickHandled = false;
        init();
    }

    private void init() {
        this.paint = new TextPaint();
        this.paint.setTextSize(14.0f);
        this.paint.setColor(-1);
        this.paint.setAntiAlias(true);
        this.paint.setTypeface(Typeface.SANS_SERIF);
        this.text = "";
    }

    public void setText(CharSequence text, boolean detect_links) {
        Spannable s;
        this.min_x = 0;
        this.min_y = 0;
        this.max_x = 0;
        this.max_y = 0;
        this.text = text;
        if (detect_links) {
            if (this.text instanceof Spannable) {
                s = (Spannable) this.text;
            } else {
                s = Spannable.Factory.getInstance().newSpannable(this.text);
            }
            Linkify.addLinks(s, Linkify.WEB_URLS);
            this.text = s;
        }
        invalidate();
    }

    public static synchronized SpannableStringBuilder detectLinks(String source) {
        SpannableStringBuilder s = null;
        synchronized (MyTextView.class) {
            try {
                s = new SpannableStringBuilder(source);
            } catch (Throwable th) {
                ////th = th;
            }
            try {
                if (s != null) {
                    Linkify.addLinks(s, Linkify.WEB_URLS);
                }
                return s;
            } catch (Throwable th2) {
                ///throw th2;
            }
        }
        return s;
    }

    /** @noinspection unused*/
    public static synchronized SpannableStringBuilder detectLinks(SpannableStringBuilder source) {
        synchronized (MyTextView.class) {
            Linkify.addLinks(source, Linkify.WEB_URLS);
        }
        return source;
    }

    public void setText(CharSequence text) {
        setText(text, true);
    }

    public void setTextSize(float size) {
        this.paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, resources.dm));
        invalidate();
    }

    public void setTextColor(int color) {
        this.paint.setColor(color);
        invalidate();
    }

    public int getTextColor() {
        return this.paint.getColor();
    }

    public void setLinkTextColor(int color) {
        this.paint.linkColor = color;
        invalidate();
    }

    public void setMaxLines(int lines) {
        this.max_lines = lines;
    }

    public void relayout() {
        if (this.layout != null) {
            makeNewLayout(this.layout.getWidth());
        }
        requestLayout();
    }

    private void makeNewLayout(int width) {
        CharSequence cs = this.text;
        if (this.max_lines != 0) {
            cs = TextUtils.ellipsize(this.text, this.paint, this.max_lines * width, TextUtils.TruncateAt.END);
        }
        this.layout = new StaticLayout(cs, this.paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    @Override // android.view.View
    public void onMeasure(int a, int b) {
        int w = MeasureSpec.getSize(a);
        this.parentSpace = w;
        if (this.layout == null) {
            makeNewLayout(w);
        } else if (w != this.layout.getWidth()) {
            makeNewLayout(w);
        }
        int dh = this.layout.getLineTop(this.layout.getLineCount());
        setMeasuredDimension(w, dh);
    }

    private void attachHightlight(ClickableSpan span) {
        try {
            SpannableStringBuilder ssb = (SpannableStringBuilder) this.text;
            this.highlight_span = new BackgroundColorSpan(ColorScheme.getColor(47));
            ssb.setSpan(this.highlight_span, ssb.getSpanStart(span), ssb.getSpanEnd(span), 33);
            setText(ssb, false);
        } catch (Exception ignored) {
        }
    }

    private void removeHighlight() {
        if (this.highlight_span != null) {
            try {
                SpannableStringBuilder ssb = (SpannableStringBuilder) this.text;
                ssb.removeSpan(this.highlight_span);
                setText(ssb, false);
                this.highlight_span = null;
            } catch (Exception ignored) {
            }
        }
    }

    @Override // android.view.View
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            if (event.getAction() == 0) {
                this.mClickHandled = false;
                this.mLongClickHandled = false;
                int idx = getCharIndexFromCoordinate(event.getX(), event.getY());
                ClickableSpan span = getSpanAt(idx);
                if (span != null) {
                    attachHightlight(span);
                    if (!(span instanceof URLSpan)) {
                        return true;
                    }
                    postDelayed(() -> {
                        if (!MyTextView.this.mClickHandled) {
                            MyTextView.this.mLongClickHandled = true;
                            MyTextView.this.showMyContextMenu();
                        }
                    }, 600L);
                    this.mContextURL = (URLSpan) span;
                    return true;
                }
            }
            switch (event.getAction()) {
                case 1:
                    removeHighlight();
                    if (!this.mLongClickHandled) {
                        this.mClickHandled = true;
                        int idx2 = getCharIndexFromCoordinate(event.getX(), event.getY());
                        ClickableSpan span2 = getSpanAt(idx2);
                        if (span2 != null) {
                            span2.onClick(this);
                            return true;
                        }
                    }
                    break;
                case 3:
                case 4:
                    this.mClickHandled = true;
                    removeHighlight();
                    break;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void showMyContextMenu() {
        final UAdapter adp = new UAdapter();
        adp.setMode(2);
        adp.setTextSize(16);
        adp.setPadding(16);
        adp.put(Locale.getString("s_open"), 0);
        adp.put(Locale.getString("s_copy"), 1);
        Vector<JProfile> profiles = resources.service.profiles.getJabberProfiles();
        final Vector<JProfile> connected = new Vector<>();
        for (JProfile p : profiles) {
            if (p.connected && (p.type == 3 || p.type == 4 || p.type == 0 || p.type == 2)) {
                connected.add(p);
            }
        }
        if (!connected.isEmpty()) {
            adp.put_separator(Locale.getString("s_add_to_bookmarks"));
            int i = 2;
            for (JProfile jProfile : connected) {
                adp.put(jProfile.getFullJID(), i);
                i++;
            }
        }
        this.mContextMenu = new Dialog(resources.service, R.style.DialogTheme);
        Window window = this.mContextMenu.getWindow();
        //noinspection DataFlowIssue
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        lp.gravity = Gravity.CENTER;
        lp.width = (int) (getWidth() * 0.8d);
        lp.windowAnimations = 0;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED;
        resources.attachDialogStyle(window);
        window.setAttributes(lp);
        LinearLayout lay = new LinearLayout(resources.ctx);
        lay.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        //noinspection ExtractMethodRecommender
        ListView list = new ListView(getContext());
        list.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        list.setDividerHeight(0);
        list.setSelector(resources.getListSelector());
        list.setAdapter(adp);
        list.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            MyTextView.this.mContextMenu.dismiss();
            int id = (int) adp.getItemId(arg2);
            if (id < 2) {
                switch (id) {
                    case 0:
                        MyTextView.this.mContextURL.onClick(MyTextView.this);
                        break;
                    case 1:
                        //noinspection deprecation
                        ClipboardManager cm = (ClipboardManager) MyTextView.this.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        //noinspection deprecation
                        cm.setText(MyTextView.this.mContextURL.getURL());
                        Toast.makeText(MyTextView.this.getContext(), Locale.getString("s_copied"), Toast.LENGTH_SHORT).show();
                        break;
                }
                return;
            }
            final JProfile p2 = connected.get(id - 2);
            if (p2.connected) {
                final BookmarkItem item = new BookmarkItem();
                item.type = 1;
                item.JID_OR_URL = MyTextView.this.mContextURL.getURL();
                final Dialog progress = DialogBuilder.createProgress(MyTextView.this.getContext(), Locale.getString("s_please_wait"), true);
                progress.show();
                HttpDisco.getInstance().discoveryAsync(MyTextView.this.mContextURL.getURL(), result -> {
                    item.NAME = result;
                    if (p2.bookmarks.itIsExist(item)) {
                        resources.service.showToast(Locale.getString("s_bookmark_already_exist"), 0);
                    } else {
                        p2.bookmarks.add(item, progress);
                    }
                });
            }
        });
        lay.addView(list);
        window.setContentView(lay);
        this.mContextMenu.show();
    }

    private ClickableSpan getSpanAt(int idx) {
        ClickableSpan span = null;
        if (idx < 0) {
            return null;
        }
        try {
            Spanned spn = (Spannable) this.text;
            ClickableSpan[] spans = spn.getSpans(0, this.text.length(), ClickableSpan.class);
            int i = 0;
            while (true) {
                if (i < spans.length) {
                    ClickableSpan s = spans[i];
                    int start = spn.getSpanStart(s);
                    int end = spn.getSpanEnd(s);
                    if (start > idx || idx > end) {
                        i++;
                    } else {
                        span = s;
                        break;
                    }
                } else {
                    break;
                }
            }
            return span;
        } catch (Exception e) {
            return null;
        }
    }

    public int getCharIndexFromCoordinate(float x, float y) {
        int line = this.layout.getLineForVertical((int) y);
        int index = this.layout.getOffsetForHorizontal(line, x);
        if (x > this.layout.getLineWidth(line)) {
            return -1;
        }
        return index;
    }

    /** @noinspection unused*/
    public final int getParentAvailableSpace() {
        return this.parentSpace;
    }

    public final void computeRefreshRate() {
        this.refresh_rate = computeMinimalRefreshRate();
        this.hdl.sendEmptyMessageDelayed(0, 100L);
    }

    @SuppressLint("DrawAllocation")
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        if (this.layout != null) {
            this.layout.draw(canvas);
        }
        new Paint();
        getHeight();
    }

    public void selectMatches(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            int pattern_length = pattern.length();
            String text = this.text.toString();
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            String text2 = text.toLowerCase();
            int idx = 0;
            while (true) {
                int idx2 = text2.indexOf(pattern, idx);
                if (idx2 != -1) {
                    ssb.setSpan(new BackgroundColorSpan(1996553984), idx2, idx2 + pattern_length, 33);
                    idx = idx2 + pattern_length;
                } else {
                    setText(ssb);
                    return;
                }
            }
        }
    }

    public final SpannableStringBuilder checkAndReplaceImageLinks(SpannableStringBuilder spanned) {
        String lc_url;
        String text = this.text.toString();
        URLSpan[] spans = spanned.getSpans(0, text.length(), URLSpan.class);
        if (spans.length != 0) {
            for (URLSpan span : spans) {
                String url = span.getURL().toLowerCase();
                int question_idx = url.indexOf("?");
                if (question_idx > 0) {
                    lc_url = url.substring(0, question_idx - 1);
                } else {
                    lc_url = url;
                }
                if (lc_url.endsWith(".jpg") || lc_url.endsWith(".jpeg") || lc_url.endsWith(".png") || lc_url.endsWith(".gif") || lc_url.endsWith(".bmp")) {
                    URLImageSpan img = new URLImageSpan(span.getURL(), this);
                    int start = spanned.getSpanStart(span);
                    int end = spanned.getSpanEnd(span);
                    spanned.insert(start, "\n");
                    spanned.insert(end + 1, "\n");
                    spanned.setSpan(img, start + 1, end + 1, 33);
                    spanned.removeSpan(span);
                }
            }
            setText(spanned);
        }
        return spanned;
    }

    public final void setForcedAnimation(boolean forced_animation) {
        this.forced_animation = forced_animation;
    }

    private int computeMinimalRefreshRate() {
        MySpan[] spans = new MySpan[0];
        boolean ani_computed = false;
        int rate = popup_log_adapter.DEFAULT_DISPLAY_TIME;
        try {
            String text = this.text.toString();
            spans = ((Spanned) this.text).getSpans(0, text.length(), MySpan.class);
        } catch (Exception ignored) {
        }
        if (spans.length == 0) {
            return popup_log_adapter.DEFAULT_DISPLAY_TIME;
        }
        for (MySpan span : spans) {
            if (!ani_computed && span.animated()) {
                ani_computed = true;
                this.animated = true;
            }
            int delay = span.getMinimalRefreshRate();
            if (delay < rate) {
                rate = delay;
            }
        }
        return rate;
    }

    private void invalidateSpans() {
        String text = this.text.toString();
        MySpan[] spans = ((Spanned) this.text).getSpans(0, text.length(), MySpan.class);
        if (spans.length != 0) {
            boolean init = true;
            for (MySpan span : spans) {
                int left = span.getLeft();
                int top = span.getTop();
                int right = left + span.getWidth();
                int bottom = top + span.getHeight();
                if (init) {
                    this.min_x = left;
                    this.max_x = right;
                    this.min_y = top;
                    this.max_y = bottom;
                    init = false;
                } else {
                    if (left < this.min_x) {
                        this.min_x = left;
                    }
                    if (top < this.min_y) {
                        this.min_y = top;
                    }
                    if (right > this.max_x) {
                        this.max_x = right;
                    }
                    if (bottom > this.max_y) {
                        this.max_y = bottom;
                    }
                }
            }
            if ((this.max_x - this.min_x) * (this.max_y - this.min_y) != 0) {
                invalidate(this.min_x, this.min_y, this.max_x, this.max_y);
            }
        }
    }

    /** @noinspection NullableProblems*/
    @Override
    public final boolean handleMessage(Message message) {
        if (this.animated || this.forced_animation) {
            if (!PreferenceTable.ms_animated_smileys) {
                Movie.stamp = 0L;
            } else if (ICQChatActivity.VISIBLE || JChatActivity.is_any_chat_opened || MMPChatActivity.is_any_chat_opened || JConference.is_any_chat_opened || SmileysSelector.VISIBLE || this.forced_animation) {
                Movie.stamp = SystemClock.uptimeMillis();
                try {
                    invalidateSpans();
                } catch (Exception ignored) {
                }
                this.hdl.sendEmptyMessageDelayed(0, this.refresh_rate);
            }
        }
        return false;
    }

    protected void finalize() {
        Log.e("MyTextView", "Class 0x" + Integer.toHexString(hashCode()) + " finalized");
    }
}