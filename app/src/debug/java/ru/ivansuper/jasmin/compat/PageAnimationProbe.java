package ru.ivansuper.jasmin.compat;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import ru.ivansuper.jasmin.slide_tools.SlideSwitcher;

/** Offline smoke checks against real framework Canvas, Scroller and event dispatch. */
public final class PageAnimationProbe extends Instrumentation {
    private static final int WIDTH = 240, HEIGHT = 280, STRIDE = WIDTH + 1;
    private static final int[] COLORS = {0xffb04444, 0xff44a060, 0xff4060b0};
    private final StringBuilder failures = new StringBuilder();
    private int checks;
    private File output;

    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }

    @Override public void onStart() {
        output = new File(getTargetContext().getFilesDir(), "animation-probes");
        output.mkdirs();
        runOnMainSync(new Runnable() {
            public void run() {
                try { verify(); }
                catch (Throwable error) { failures.append(android.util.Log.getStackTraceString(error)); }
            }
        });
        String report = "Checks: " + checks + "\n" + (failures.length() == 0 ? "PASS: all 11 page effects and navigation smoke checks\n" : failures.toString());
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(output, "report.txt")), "UTF-8");
            writer.write(report);
            writer.close();
        } catch (Exception error) { failures.append(error.toString()); }
        Bundle result = new Bundle();
        result.putString("stream", report);
        finish(failures.length() == 0 ? -1 : 0, result);
    }

    private void expect(boolean value, String message) {
        checks++;
        if (!value) failures.append(message).append('\n');
    }

    private void verify() throws Exception {
        ProbePager pager = new ProbePager(getTargetContext());
        for (int page = 0; page < 3; page++) pager.addView(new Card(getTargetContext(), page), "Page " + page);
        pager.measure(View.MeasureSpec.makeMeasureSpec(WIDTH, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(HEIGHT, View.MeasureSpec.EXACTLY));
        pager.layout(0, 0, WIDTH, HEIGHT);
        for (int type = 0; type <= SlideSwitcher.ANIM_ICS_2; type++) {
            pager.setAnimationType(type);
            for (int route = 0; route < 3; route++) {
                for (int frame = 0; frame <= 4; frame++) {
                    int x = route == 0 ? STRIDE * frame / 4 : route == 1 ? -STRIDE * frame / 4 : 2 * STRIDE + STRIDE * frame / 4;
                    pager.scrollTo(x, 0);
                    pager.callbacks = 0;
                    pager.drawn = 0;
                    Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(0xff101010);
                    canvas.translate(-x, 0);
                    pager.draw(canvas);
                    expect(pager.callbacks <= pager.drawn, "Effect " + type + " route " + route + " frame " + frame + ": transform applied more than once");
                    if (frame == 0 || frame == 4) {
                        int page = route == 0 ? (frame == 0 ? 0 : 1) : route == 1 ? (frame == 0 ? 0 : 2) : (frame == 0 ? 2 : 0);
                        expect(bitmap.getPixel(WIDTH / 2, HEIGHT / 2) == COLORS[page], "Effect " + type + " route " + route + " frame " + frame + ": wrong/blank settled page");
                    }
                    FileOutputStream stream = new FileOutputStream(new File(output, type + "-" + route + "-" + frame + ".png"));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();
                    bitmap.recycle();
                }
            }
            pager.scrollTo(0);
            key(pager, KeyEvent.KEYCODE_DPAD_RIGHT); settle(pager, STRIDE, type + " next");
            key(pager, KeyEvent.KEYCODE_DPAD_LEFT); settle(pager, 0, type + " previous");
            key(pager, KeyEvent.KEYCODE_DPAD_LEFT); settle(pager, 2 * STRIDE, type + " wrap previous");
            key(pager, KeyEvent.KEYCODE_DPAD_RIGHT); settle(pager, 0, type + " wrap next");
            swipe(pager, false, true); settle(pager, STRIDE, type + " swipe next");
            swipe(pager, true, true); settle(pager, STRIDE, type + " cancelled swipe");
            pager.scrollTo(0);
            swipe(pager, false, false); settle(pager, 0, type + " short swipe");
        }
        pager.scrollTo(1);
        pager.setFullyLocked(true);
        key(pager, KeyEvent.KEYCODE_DPAD_RIGHT);
        settle(pager, STRIDE, "locked pager");
        pager.setFullyLocked(false);
        // Seed the application's random picker so coverage is repeatable.
        java.lang.reflect.Field randomField = SlideSwitcher.class.getDeclaredField("animationRandom");
        randomField.setAccessible(true);
        ((java.util.Random) randomField.get(pager)).setSeed(1234);
        java.lang.reflect.Field scrollerField = SlideSwitcher.class.getDeclaredField("scroller");
        scrollerField.setAccessible(true);
        android.widget.Scroller scroller = (android.widget.Scroller) scrollerField.get(pager);
        boolean[] selected = new boolean[11];
        pager.setRandomizedAnimation(true);
        for (int attempt = 0; attempt < 128; attempt++) {
            pager.scrollTo(0);
            swipe(pager, true, false);
            selected[pager.getAnimationType()] = true;
            scroller.forceFinished(true);
            pager.computeScroll();
        }
        for (int type = 0; type < selected.length; type++) expect(selected[type], "Random picker never selected effect " + type);
        pager.setRandomizedAnimation(false);
        pager.scrollTo(1);
        pager.scrollTo(-1);
        expect(pager.getScrollX() == STRIDE, "Invalid page index changed selection");
        pager.layout(17, 23, 17 + WIDTH, 23 + HEIGHT);
        expect(pager.getChildAt(0).getLeft() == 0 && pager.getChildAt(0).getBottom() == HEIGHT,
                "Pager parent offset leaked into child layout");
        pager.scrollTo(0);
        pager.removeViewAt(2);
        pager.removeViewAt(1);
        swipe(pager, false, true);
        key(pager, KeyEvent.KEYCODE_DPAD_RIGHT);
        settle(pager, 0, "single page");
        for (int page = 0; page < 3; page++) pager.removeViewAt(0);
        key(pager, KeyEvent.KEYCODE_DPAD_LEFT);
        settle(pager, 0, "empty pager");
    }

    private void key(ProbePager pager, int code) {
        pager.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
    }

    private void settle(ProbePager pager, int x, String scenario) {
        SystemClock.sleep(320);
        pager.computeScroll();
        pager.computeScroll();
        expect(pager.getScrollX() == x, scenario + ": expected " + x + ", got " + pager.getScrollX());
    }

    private void swipe(ProbePager pager, boolean cancel, boolean longSwipe) {
        long time = SystemClock.uptimeMillis();
        touch(pager, time, MotionEvent.ACTION_DOWN, 225);
        expect(touch(pager, time, MotionEvent.ACTION_MOVE, 180), "Pager stopped consuming an active drag");
        touch(pager, time, MotionEvent.ACTION_MOVE, longSwipe ? 10 : 150);
        touch(pager, time, cancel ? MotionEvent.ACTION_CANCEL : MotionEvent.ACTION_UP, longSwipe ? 10 : 150);
    }

    private boolean touch(ProbePager pager, long time, int action, float x) {
        MotionEvent event = MotionEvent.obtain(time, SystemClock.uptimeMillis(), action, x, HEIGHT / 2, 0);
        try { return pager.dispatchTouchEvent(event); } finally { event.recycle(); }
    }

    private static final class ProbePager extends SlideSwitcher {
        int callbacks, drawn;
        ProbePager(Context context) { super(context); }
        @Override protected boolean getChildStaticTransformation(View child, Transformation result) {
            callbacks++;
            return super.getChildStaticTransformation(child, result);
        }
        @Override protected boolean drawChild(Canvas canvas, View child, long time) {
            drawn++;
            return super.drawChild(canvas, child, time);
        }
    }

    private static final class Card extends View {
        private final int page;
        private final Paint paint = new Paint();
        Card(Context context, int page) { super(context); this.page = page; setClickable(true); }
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(COLORS[page]);
            paint.setColor(0xffffffff);
            paint.setTextSize(22);
            canvas.drawText("PAGE " + page, 18, 35, paint);
            canvas.drawRect(10, 50, 35, 100, paint);
            canvas.drawRect(getWidth() - 55, getHeight() - 35, getWidth() - 10, getHeight() - 10, paint);
        }
    }
}
