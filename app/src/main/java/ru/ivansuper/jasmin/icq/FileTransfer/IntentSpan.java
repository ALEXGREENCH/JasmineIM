package ru.ivansuper.jasmin.icq.FileTransfer;

import android.content.Intent;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.view.View;
import java.io.File;
import java.net.URLConnection;

public class IntentSpan extends ClickableSpan {
    private final String url;

    public IntentSpan(String url) {
        this.url = url;
    }

    /** @noinspection NullableProblems*/
    @Override
    public void onClick(View view) {
        File file = new File(this.url);
        Uri uri = Uri.fromFile(file);
        String mime_type = URLConnection.guessContentTypeFromName(uri.toString());
        if (mime_type == null) {
            mime_type = "*/*";
        }
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(uri, mime_type);
            view.getContext().startActivity(intent);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
