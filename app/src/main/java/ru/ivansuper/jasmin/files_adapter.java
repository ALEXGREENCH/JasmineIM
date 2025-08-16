package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.io.File;
import java.util.Collections;
import java.util.Vector;
import ru.ivansuper.jasmin.icq.FileTransfer.FileTransfer;

/**
 * Adapter for displaying a list of files and directories.
 * It handles loading icons for image files asynchronously in the background.
 */
public class files_adapter extends BaseAdapter {
    public File parent;
    private final Vector<Drawable> icons = new Vector<>();
    private final Vector<File> dirs = new Vector<>();
    private final Vector<File> files = new Vector<>();
    private final Vector<File> list = new Vector<>();
    private final Object locker = new Object();
    private final ThreadGroup tg = new ThreadGroup("ImageLoaders");
    private long timestamp = 0;

    public void setData(File[] list, File parent) {
        this.timestamp = System.currentTimeMillis();
        for (int i = 0; i < this.icons.size(); i++) {
            this.icons.set(i, null);
        }
        this.icons.clear();
        this.parent = parent;
        this.list.clear();
        if (list != null) {
            for (File file : list) {
                try {
                    if (file.isDirectory()) {
                        this.dirs.add(file);
                    } else {
                        this.files.add(file);
                    }
                    this.icons.add(null);
                } catch (Exception e) {
                    this.dirs.clear();
                    this.files.clear();
                    this.list.clear();
                    this.list.add(parent);
                }
            }
        }
        Collections.sort(this.dirs);
        Collections.sort(this.files);
        this.icons.add(null);
        this.list.add(parent);
        this.list.addAll(this.dirs);
        this.list.addAll(this.files);
        this.dirs.clear();
        this.files.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public File getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView label;
        if (convertView == null) {
            label = new TextView(resources.ctx);
            label.setPadding(10, 10, 10, 10);
            label.setTextColor(-1);
            label.setTextSize(18.0f);
            label.setShadowLayer(3.0f, 1.0f, 1.0f, -16777216);
            label.setBackgroundColor(855638016);
            label.setCompoundDrawablePadding(10);
            label.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        } else {
            label = (TextView) convertView;
        }
        final TextView label_a = label;
        final File item = getItem(position);
        if (position == 0) {
            label.setCompoundDrawables(resources.directory_up, null, null, null);
            label.setText("..");
        } else if (item.isDirectory()) {
            label.setCompoundDrawables(resources.directory, null, null, null);
            label.setText(item.getName());
        } else {
            if (itIsImage(item)) {
                Drawable icon = this.icons.get(position);
                if (icon == null) {
                    label.setCompoundDrawables(resources.file_brw, null, null, null);
                    this.icons.set(position, resources.file_brw);
                    Runnable rr = new Runnable() {
                        final long stamp;

                        {
                            this.stamp = files_adapter.this.timestamp;
                        }

                        @Override
                        public void run() {
                            Thread.currentThread().setPriority(1);
                            synchronized (files_adapter.this.locker) {
                                if (this.stamp == files_adapter.this.timestamp) {
                                    BitmapFactory.Options opts = new BitmapFactory.Options();
                                    opts.inDither = true;
                                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                                    opts.inJustDecodeBounds = true;
                                    BitmapFactory.decodeFile(item.getAbsolutePath(), opts);
                                    if (opts.outWidth != -1) {
                                        opts.inSampleSize = Math.max(opts.outWidth, opts.outHeight) / 48;
                                        opts.inJustDecodeBounds = false;
                                        Bitmap bmpA = BitmapFactory.decodeFile(item.getAbsolutePath(), opts);
                                        if (bmpA == null) {
                                            return;
                                        }
                                        if (this.stamp == files_adapter.this.timestamp) {
                                            android.graphics.drawable.BitmapDrawable bitmapDrawable = new android.graphics.drawable.BitmapDrawable(bmpA);
                                            bitmapDrawable.setBounds(0, 0, 48, 48);
                                            if (this.stamp == files_adapter.this.timestamp) {
                                                files_adapter.this.icons.set(position, bitmapDrawable);
                                                //noinspection ConstantValue
                                                if (this.stamp == files_adapter.this.timestamp) {
                                                    Runnable r = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            files_adapter.this.notifyDataSetChanged();
                                                        }
                                                    };
                                                    label_a.post(r);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    };
                    Thread t = new Thread(this.tg, rr);
                    t.setName("ImageLoader");
                    t.start();
                } else {
                    label.setCompoundDrawables(icon, null, null, null);
                }
            } else {
                label.setCompoundDrawables(resources.file_brw, null, null, null);
            }
            label.setText(item.getName() + "\n" + FileTransfer.getSizeLabel(item.length()));
        }
        return label;
    }

    private boolean itIsImage(File file) {
        String file_name = file.getAbsolutePath();
        return file_name.toLowerCase().endsWith(".gif") || file_name.toLowerCase().endsWith(".jpg") || file_name.toLowerCase().endsWith(".jpeg") || file_name.toLowerCase().endsWith(".bmp") || file_name.toLowerCase().endsWith(".png");
    }
}
