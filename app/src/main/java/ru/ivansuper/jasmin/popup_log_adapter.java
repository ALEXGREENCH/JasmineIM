package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.Preferences.PreferenceTable;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.ui.MyTextView;

/**
 * Adapter for displaying log messages in a popup window.
 * This adapter manages a list of {@link Item} objects, each representing a log entry.
 * It provides methods to add new log entries with various display options,
 * including custom display times, avatars, headers, and click actions.
 * <p>
 * Log entries are automatically removed after a specified display time using a
 * {@link destroyer} thread.
 * <p>
 * The appearance of each log item in the popup is determined by the
 * {@link #getView(int, View, ViewGroup)} method, which inflates a layout
 * (either for tablet or phone) and populates it with the log data.
 * <p>
 * This adapter is intended to be used with a {@link android.widget.ListView} or
 * a similar view that can display a list of items.
 *
 * @see BaseAdapter
 * @see Item
 * @see destroyer
 * @see jasminSvc
 */
public class popup_log_adapter extends BaseAdapter {
    public static final int DEFAULT_DISPLAY_TIME = 5000;
    public static final int INFO_DISPLAY_TIME = 3000;
    public static final int MESSAGE_DISPLAY_TIME = 6000;
    public static final int PRESENSE_DISPLAY_TIME = 4000;
    private final Vector<Item> mList = new Vector<>();
    private final jasminSvc svc;

    public popup_log_adapter(jasminSvc var1) {
        this.svc = var1;
    }

    private int getIdx(long var1) {
        int var3 = 0;

        int var4;
        while(true) {
            if (var3 >= this.mList.size()) {
                var4 = -1;
                break;
            }

            var4 = var3;
            if (this.mList.get(var3).id == var1) {
                break;
            }

            ++var3;
        }

        return var4;
    }

    public int getCount() {
        return this.mList.size();
    }

    public Item getItem(int var1) {
        return this.mList.get(var1);
    }

    public long getItemId(int var1) {
        return var1;
    }

    @SuppressLint("ResourceType")
    public View getView(int var1, View var2, ViewGroup var3) {
        LinearLayout var10;
        if (var2 == null) {
            LayoutInflater var9 = LayoutInflater.from(resources.ctx);
            int var4;
            if (resources.IT_IS_TABLET) {
                var4 = 2130903082;
            } else {
                var4 = 2130903081;
            }

            var10 = (LinearLayout)var9.inflate(var4, null);
            resources.attachPopupBack(var10);
        } else {
            var10 = (LinearLayout)var2;
        }

        Item var5 = this.getItem(var1);
        ImageView var6 = var10.findViewById(2131427587);
        final destroyer var7 = var5.dest;
        final Runnable var11 = var5.task;
        if (var6 != null) {
            if (PreferenceTable.ms_log_clickable) {
                var6.setVisibility(View.VISIBLE);
                var6.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View var1, MotionEvent var2) {
                        boolean var3 = true;
                        boolean var4;
                        if (var2.getAction() == 1) {
                            var4 = var3;
                            if (var7 != null) {
                                var7.forceDestroy();
                                var4 = var3;
                            }
                        } else {
                            var4 = var3;
                            if (var2.getAction() != 0) {
                                var4 = false;
                            }
                        }

                        return var4;
                    }
                });
            } else {
                var6.setVisibility(8);
            }
        }

        var6 = var10.findViewById(2131427585);
        Drawable var8 = var5.avatar;
        if (var8 == null) {
            var6.setVisibility(8);
        } else {
            var6.setVisibility(0);
            var6.setImageBitmap(((BitmapDrawable)var8).getBitmap());
        }

        TextView var13 = var10.findViewById(2131427584);
        if (var5.header != null) {
            var13.setVisibility(0);
            var13.setTextSize(11.0F);
            var13.setText(var5.header);
            var13.setCompoundDrawables(var5.header_drw, null, null, null);
        } else {
            var13.setVisibility(8);
        }

        MyTextView var14 = var10.findViewById(2131427586);
        if (var5.value.toString().trim().length() != 0) {
            var14.setVisibility(0);
            var14.setTextSize(11.0F);
            var14.setText(var5.value, false);
            var14.setMaxLines(5);
            var14.setForcedAnimation(true);
            var14.setLinkTextColor(var14.getTextColor());
            var14.computeRefreshRate();
            var14.relayout();
        } else {
            var14.setVisibility(8);
        }

        var10.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View var1, MotionEvent var2) {
                boolean var3;
                if (var2.getAction() == 0) {
                    if (var7 != null) {
                        var7.forceDestroy();
                    }

                    if (var11 != null) {
                        var11.run();
                    }

                    var3 = true;
                } else {
                    var3 = false;
                }

                return var3;
            }
        });
        var10.setDescendantFocusability(262144);
        return var10;
    }

    public void put(String var1) {
        if (PreferenceTable.use_popup) {
            Item var2 = new Item();
            var2.value = var1;
            var2.id = System.currentTimeMillis();
            var2.dest = new destroyer(var2.id, 5000);
            var2.dest.start();
            this.mList.add(0, var2);
            this.notifyDataSetChanged();
        }

    }

    public void put(String var1, int var2) {
        if (PreferenceTable.use_popup) {
            Item var3 = new Item();
            var3.value = var1;
            var3.id = System.currentTimeMillis();
            var3.dest = new destroyer(var3.id, var2);
            var3.dest.start();
            this.mList.add(0, var3);
            this.notifyDataSetChanged();
        }

    }

    public void put(final String var1, final CharSequence var2, final Drawable var3, final Drawable var4, final int var5, final Runnable var6) {
        if (PreferenceTable.use_popup) {
            this.svc.runOnUi(new Runnable() {
                public void run() {
                    int var1x = var5;
                    int var2x = var1x;
                    if (var1x <= 0) {
                        var2x = 5000;
                    }

                    Item var3x = popup_log_adapter.this.new Item();
                    var3x.header_drw = var3;
                    var3x.avatar = var4;
                    var3x.header = var1;
                    var3x.value = var2;
                    if (var3x.value == null) {
                        var3x.value = "";
                    }

                    var3x.id = System.currentTimeMillis();
                    var3x.task = var6;
                    var3x.dest = popup_log_adapter.this.new destroyer(var3x.id, var2x);
                    var3x.dest.start();
                    popup_log_adapter.this.mList.add(0, var3x);
                    popup_log_adapter.this.notifyDataSetChanged();
                }
            });
        }

    }

    public void put(String var1, Runnable var2, Drawable var3) {
        if (PreferenceTable.use_popup) {
            Item var4 = new Item();
            var4.value = var1;
            var4.task = var2;
            var4.avatar = var3;
            var4.id = System.currentTimeMillis();
            var4.dest = new destroyer(var4.id, 5000);
            var4.dest.start();
            this.mList.add(0, var4);
            this.notifyDataSetChanged();
        }

    }

    private class Item {
        public Drawable avatar;
        public destroyer dest;
        public String header;
        public Drawable header_drw;
        public long id;
        public Runnable task;
        public CharSequence value;
    }

    private class destroyer extends Thread {
        private final int display_time;
        private Long id = null;
        private boolean used = false;

        public destroyer(Long var2, int var3) {
            this.id = var2;
            this.display_time = var3;
        }

        private void performDestroy() {
            if (!this.used) {
                int var1 = popup_log_adapter.this.getIdx(this.id);
                if (var1 >= 0) {
                    popup_log_adapter.this.mList.remove(var1);
                }

                popup_log_adapter.this.notifyDataSetChanged();
            }

        }

        public void forceDestroy() {
            this.performDestroy();
            this.used = true;
        }

        public void run() {
            try {
                Thread.sleep(this.display_time);
                Runnable var1 = new Runnable() {
                    public void run() {
                        destroyer.this.performDestroy();
                    }
                };
                popup_log_adapter.this.svc.runOnUi(var1);
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }

        }
    }
}
