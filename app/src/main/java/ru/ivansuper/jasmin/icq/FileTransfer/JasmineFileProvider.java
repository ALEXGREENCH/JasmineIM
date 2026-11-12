package ru.ivansuper.jasmin.icq.FileTransfer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLConnection;

/**
 * Hands a sent or received file to a viewer app (Android 7+ refuses file:// links, and the received
 * files live in the app's own Android/data folder, which other apps may not read).
 *
 * <p>Not exported: an app can open a URI only through the one-off read grant {@link IntentSpan}
 * attaches to its VIEW intent. Read only.</p>
 *
 * <p>URI: content://ru.ivansuper.jasmin.files/f/&lt;absolute path, encoded&gt;</p>
 */
public class JasmineFileProvider extends ContentProvider {
    public static final String AUTHORITY = "ru.ivansuper.jasmin.files";

    public static Uri uriFor(File file) {
        return new Uri.Builder().scheme("content").authority(AUTHORITY)
                .appendPath("f").appendPath(file.getAbsolutePath()).build();
    }

    private static File fileOf(Uri uri) throws FileNotFoundException {
        java.util.List<String> seg = uri.getPathSegments();
        if (seg.size() != 2 || !"f".equals(seg.get(0))) {
            throw new FileNotFoundException(uri.toString());
        }
        File f = new File(seg.get(1));
        if (!f.isFile()) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        return f;
    }

    public static String mimeOf(String name) {
        String mime = null;
        try {
            mime = URLConnection.guessContentTypeFromName(name);
        } catch (Exception ignored) {
        }
        if (mime == null) {
            String lc = name.toLowerCase();
            if (lc.endsWith(".webp")) {
                mime = "image/webp";
            } else if (lc.endsWith(".heic")) {
                mime = "image/heic";
            } else if (lc.endsWith(".mp4")) {
                mime = "video/mp4";
            } else if (lc.endsWith(".mp3")) {
                mime = "audio/mpeg";
            } else if (lc.endsWith(".pdf")) {
                mime = "application/pdf";
            } else if (lc.endsWith(".apk")) {
                mime = "application/vnd.android.package-archive";
            } else {
                mime = "*/*";
            }
        }
        return mime;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        try {
            return mimeOf(fileOf(uri).getName());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (mode != null && mode.indexOf('w') >= 0) {
            throw new FileNotFoundException("read only");
        }
        return ParcelFileDescriptor.open(fileOf(uri), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        File f;
        try {
            f = fileOf(uri);
        } catch (FileNotFoundException e) {
            return null;
        }
        String[] cols = projection == null ? new String[]{"_display_name", "_size"} : projection;
        Object[] row = new Object[cols.length];
        for (int i = 0; i < cols.length; i++) {
            if ("_display_name".equals(cols[i])) {
                row[i] = f.getName();
            } else if ("_size".equals(cols[i])) {
                row[i] = f.length();
            } else if ("mime_type".equals(cols[i])) {
                row[i] = mimeOf(f.getName());
            }
        }
        MatrixCursor c = new MatrixCursor(cols, 1);
        c.addRow(row);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
