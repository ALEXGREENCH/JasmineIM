package ru.ivansuper.jasmin.icq.FileTransfer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import ru.ivansuper.jasmin.resources;

/**
 * A ClickableSpan that opens a sent or received file in a viewer app.
 * From Android 7 on the file goes through {@link JasmineFileProvider} with a one-off read grant;
 * older systems get the plain file:// link they expect.
 */
public class IntentSpan extends ClickableSpan {
    private final String url;

    public IntentSpan(String url) {
        this.url = url;
    }

    /** @noinspection NullableProblems*/
    @Override
    public void onClick(View view) {
        File file = new File(this.url);
        if (!file.isFile()) {
            Toast.makeText(view.getContext(), resources.getString("s_file_not_found") + "\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return;
        }
        String mime_type = JasmineFileProvider.mimeOf(file.getName());
        Uri uri = Build.VERSION.SDK_INT >= 24 ? JasmineFileProvider.uriFor(file) : Uri.fromFile(file);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime_type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (!(view.getContext() instanceof android.app.Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            view.getContext().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(view.getContext(), resources.getString("s_file_no_viewer") + "\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
