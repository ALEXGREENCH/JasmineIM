package ru.ivansuper.jasmin.compat;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.slide_tools.SlideSwitcher;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnList;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnAdapter;
import ru.ivansuper.jasmin.color_editor.ColorScheme;

/** Debug-only, offline render checks. Runs on Donut without AndroidX/JUnit. */
public class UiRenderProbe extends Instrumentation {
    private Throwable failure;

    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }

    @Override public void onStart() {
        runOnMainSync(new Runnable() {
            public void run() {
                try {
                    render(240, 320, .75f, 1f, "ldpi");
                    render(320, 480, 1f, 1f, "mdpi");
                    render(480, 800, 1.5f, 1f, "hdpi");
                    render(240, 320, .75f, 1.5f, "ldpi-large-font");
                } catch (Throwable error) { failure = error; }
            }
        });
        Bundle results = new Bundle();
        results.putString("stream", failure == null ? "PASS: four offline render scenarios\n" : "FAIL: " + android.util.Log.getStackTraceString(failure));
        finish(failure == null ? -1 : 0, results);
    }

    private void render(int width, int height, final float density, float fontScale, String name) throws Exception {
        resources.dm.density = density;
        resources.dm.scaledDensity = density * fontScale;
        getTargetContext().getResources().getDisplayMetrics().density = density;
        getTargetContext().getResources().getDisplayMetrics().scaledDensity = density * fontScale;
        PreferenceTable.clTextSize = 18;
        SlideSwitcher switcher = new SlideSwitcher(getTargetContext());
        switcher.showPanel(true);
        switcher.addView(new View(getTargetContext()), resources.getString("s_cl_panel_chats"));
        MultiColumnList list = new MultiColumnList(getTargetContext());
        list.setAdapter(new MultiColumnAdapter() {
            public int getCount() { return 30; }
            public Object getItem(int position) { return position; }
            public long getItemId(int position) { return position; }
            public int getItemType(int position) { return position >= 0 && position < getCount() ? ITEM_TYPE_ITEM : -1; }
            public View getView(int position, View recycled, ViewGroup parent) {
                TextView row = recycled instanceof TextView ? (TextView) recycled : new TextView(getTargetContext());
                row.setText("Контакт " + (position + 1));
                row.setTextSize(16);
                row.setTextColor(0xffeeeeee);
                row.setHeight(Math.round(36 * density));
                row.setPadding(Math.round(8 * density), 0, 0, 0);
                return row;
            }
        });
        switcher.addView(list, resources.getString("s_cl_panel_contacts"));
        switcher.addView(new View(getTargetContext()), resources.getString("s_cl_panel_confs"));
        switcher.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        switcher.layout(0, 0, width, height);
        for (int page = 0; page < 3; page++) {
            switcher.scrollTo(page);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(0xff101010);
            // A real parent applies this translation when drawing a scrolled child.
            canvas.translate(-switcher.getScrollX(), -switcher.getScrollY());
            switcher.draw(canvas);
            if (page == 1 && android.os.Build.VERSION.SDK_INT == 4) {
                int x = width - Math.max(1, Math.round(2 * density)) - Math.max(2, Math.round(3 * density)) / 2;
                int pixels = 0;
                for (int y = Math.round(70 * density * fontScale); y < height; y++) {
                    if (bitmap.getPixel(x, y) == ColorScheme.getColor(49)) pixels++;
                }
                if (pixels < 5) throw new AssertionError("Missing Donut scrollbar: " + name);
            }
            save(bitmap, name + "-page" + page);
            bitmap.recycle();
        }
        // Empty pager must not divide by zero while mapping wrapped labels.
        for (int page = 0; page < 3; page++) switcher.removeViewAt(0);
        Bitmap empty = Bitmap.createBitmap(width, 80, Bitmap.Config.ARGB_8888);
        switcher.draw(new Canvas(empty));
        empty.recycle();
    }

    private void save(Bitmap bitmap, String name) throws Exception {
        File directory = new File(getTargetContext().getFilesDir(), "ui-probes");
        if (!directory.isDirectory() && !directory.mkdirs()) throw new java.io.IOException("Cannot create probe directory");
        FileOutputStream out = new FileOutputStream(new File(directory, name + ".png"));
        try { bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); }
        finally { out.close(); }
    }
}
