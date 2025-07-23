package ru.ivansuper.jasmin.ui;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.resources;

/**
 * Allows resizing of two panels (e.g., contact list and message area)
 * by dragging a separator bar.
 *
 * <p>This class manages the layout and touch events to enable dynamic resizing
 * of two adjacent views. It enforces minimum and maximum size constraints
 * and persists the last known size of the first panel.
 *
 * <p><b>Usage:</b>
 * <pre>
 * {@code
 * Resizer.BIND(contactListPanel, messageAreaPanel, separatorBarView);
 * }
 * </pre>
 *
 * <p><b>Note:</b> This class uses a singleton pattern for simplicity, but care
 * should be taken if multiple resizable panel pairs are needed in the same activity
 * or fragment. The {@code StaticFieldLeak} suppression is due to the singleton
 * holding a reference to views, which can lead to memory leaks if not managed
 * carefully with the Android lifecycle. Ideally, the Resizer instance should be
 * cleared when the views are no longer needed (e.g., in {@code onDestroyView} or {@code onDestroy}).
 */
public class Resizer {

    /** @noinspection FieldCanBeLocal, unused */
    @SuppressLint("StaticFieldLeak")
    private static Resizer mSingleton;

    private final View mPanelA;
    private final View mPanelB;
    private final View mBar;

    private LinearLayout.LayoutParams lpA;
    /** @noinspection FieldCanBeLocal*/
    private LinearLayout.LayoutParams lpB;

    /** @noinspection FieldCanBeLocal*/
    private int mPanelA_size;
    /** @noinspection FieldCanBeLocal*/
    private int mPanelB_size;

    private int minimum;
    private int maximum;

    /**
     * Привязывает панели к ресайзеру.
     *
     * @param panel_a левая панель (например, список контактов)
     * @param panel_b правая панель
     * @param bar     полоса-разделитель
     */
    public static void BIND(View panel_a, View panel_b, View bar) {
        mSingleton = new Resizer(panel_a, panel_b, bar);
    }

    private Resizer(View panel_a, View panel_b, View bar) {
        this.mPanelA = panel_a;
        this.mPanelB = panel_b;
        this.mBar = bar;
        init();
    }

    private void init() {
        // Значения в dp
        this.minimum = (int) (500.0f * resources.dm.density);
        this.maximum = (int) (1600.0f * resources.dm.density);

        // Стартовая ширина панели A
        this.mPanelA_size = (int) (1000.0f * resources.dm.density);
        this.mPanelA_size = Manager.getInt("contactlist_bar_offset", this.mPanelA_size);

        // Текущая ширина панели B
        this.mPanelB_size = this.mPanelB.getWidth();

        // Параметры лейаута
        this.lpA = new LinearLayout.LayoutParams(this.mPanelA_size, LinearLayout.LayoutParams.MATCH_PARENT);
        this.lpB = new LinearLayout.LayoutParams(this.mPanelB_size, LinearLayout.LayoutParams.MATCH_PARENT);

        this.lpA.weight = 1.0f;
        this.lpB.weight = 1.0f;

        this.mPanelA.setLayoutParams(this.lpA);

        // Слушатель касаний на разделителе
        this.mBar.setOnTouchListener(new View.OnTouchListener() {

            boolean resizing = false;
            int lastX = 0;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float raw_x = event.getX();
                int X = (int) (raw_x - (raw_x % 1.0f)); // Округление до int

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        this.lastX = X;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (this.resizing) {
                            int diff = this.lastX - X;
                            Resizer.this.lpA.width -= diff;

                            // Ограничения по минимуму и максимуму
                            if (Resizer.this.lpA.width < Resizer.this.minimum) {
                                Resizer.this.lpA.width = Resizer.this.minimum;
                            }
                            if (Resizer.this.lpA.width > Resizer.this.maximum) {
                                Resizer.this.lpA.width = Resizer.this.maximum;
                            }

                            Resizer.this.mPanelA.setLayoutParams(Resizer.this.lpA);
                            this.lastX = X;
                        } else if (Math.abs(this.lastX - X) > 10) {
                            // Если пользователь начал двигать
                            this.lastX = X;
                            this.resizing = true;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        this.resizing = false;
                        Manager.putInt("contactlist_bar_offset", Resizer.this.lpA.width);
                        return true;
                }

                return false;
            }
        });
    }
}
