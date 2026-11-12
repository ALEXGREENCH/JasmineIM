package ru.ivansuper.jasmin.icq.FileTransfer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ReplacementSpan;

import java.io.File;
import java.util.List;

import ru.ivansuper.jasmin.resources;

/**
 * The chat line of a finished transfer: a title, then per file its name (tap to open), a
 * thumbnail for pictures (tap to open too) and, for received files, the folder they were saved in.
 */
public final class FileMessage {
    private FileMessage() {
    }

    public static SpannableStringBuilder build(String title, List<File> files, boolean showFolder) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(title).append(":");
        String folder = null;
        for (File f : files) {
            if (f == null) {
                continue;
            }
            ssb.append("\n");
            String name = f.getName();
            int start = ssb.length();
            ssb.append(name);
            ssb.setSpan(new IntentSpan(f.getAbsolutePath()), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            Bitmap thumb = thumbnail(f);
            if (thumb != null) {
                ssb.append("\n");
                int s = ssb.length();
                ssb.append("￼");
                ssb.setSpan(new ThumbSpan(thumb), s, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new IntentSpan(f.getAbsolutePath()), s, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (f.getParentFile() != null) {
                folder = f.getParentFile().getAbsolutePath();
            }
        }
        if (showFolder && folder != null) {
            ssb.append("\n").append(resources.getString("s_file_saved_to")).append(" ").append(folder).append("/");
        }
        return ssb;
    }

    public static boolean isPicture(String name) {
        String lc = name.toLowerCase();
        return lc.endsWith(".jpg") || lc.endsWith(".jpeg") || lc.endsWith(".png") || lc.endsWith(".gif")
                || lc.endsWith(".bmp") || lc.endsWith(".webp");
    }

    /** A picture scaled down to fit in about 200dp; null for other files or when it cannot be read. */
    static Bitmap thumbnail(File f) {
        if (!f.isFile() || !isPicture(f.getName())) {
            return null;
        }
        try {
            float density = 1f;
            try {
                density = resources.ctx.getResources().getDisplayMetrics().density;
            } catch (Exception ignored) {
            }
            int max = Math.max(96, (int) (200 * density));
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            if (o.outWidth <= 0 || o.outHeight <= 0) {
                return null;
            }
            int sample = 1;
            while (o.outWidth / (sample * 2) >= max || o.outHeight / (sample * 2) >= max) {
                sample *= 2;
            }
            o = new BitmapFactory.Options();
            o.inSampleSize = sample;
            Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            if (b == null) {
                return null;
            }
            float k = Math.min(1f, Math.min((float) max / b.getWidth(), (float) max / b.getHeight()));
            if (k < 1f) {
                Bitmap s = Bitmap.createScaledBitmap(b, Math.max(1, (int) (b.getWidth() * k)), Math.max(1, (int) (b.getHeight() * k)), true);
                if (s != b) {
                    b.recycle();
                }
                b = s;
            }
            b.setDensity(0);
            return b;
        } catch (Throwable e) { // OutOfMemoryError on old phones too
            return null;
        }
    }

    /** Draws a thumbnail in place of one character, like URLImageSpan does for image links. */
    static final class ThumbSpan extends ReplacementSpan {
        private final Bitmap image;

        ThumbSpan(Bitmap image) {
            this.image = image;
        }

        /** @noinspection NullableProblems*/
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            if (fm != null) {
                fm.ascent = -image.getHeight();
                fm.descent = 0;
                fm.top = fm.ascent;
                fm.bottom = 0;
            }
            return image.getWidth();
        }

        /** @noinspection NullableProblems*/
        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            if (!image.isRecycled()) {
                canvas.drawBitmap(image, x, top, null);
            }
        }
    }
}
