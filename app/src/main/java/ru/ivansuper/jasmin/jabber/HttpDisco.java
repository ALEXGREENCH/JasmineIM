package ru.ivansuper.jasmin.jabber;

import android.util.Log;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class HttpDisco {
    private String result = "null";
    private final Object locker = new Object();

    public interface OnDiscoListener {
        void OnDisco(String str);
    }

    private HttpDisco() {
    }

    private final String extractTitle(String str) {
        Matcher matcher = Pattern.compile("<title.*?>(.*?)</title>", 34).matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static HttpDisco getInstance() {
        HttpDisco httpDisco;
        synchronized (HttpDisco.class) {
            try {
                httpDisco = new HttpDisco();
            } catch (Throwable th) {
                throw th;
            }
        }
        return httpDisco;
    }

    public String discovery(final String str) {
        this.result = str;
        Thread thread = new Thread() {
            @Override
            public void run() {
                Header header;
                try {
                    HttpResponse httpResponseExecute = new DefaultHttpClient().execute(new HttpGet(str));
                    Header[] allHeaders = httpResponseExecute.getAllHeaders();
                    int length = allHeaders.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            header = null;
                            break;
                        }
                        Header header2 = allHeaders[i];
                        if (header2.getName().equalsIgnoreCase("content-type")) {
                            header = header2;
                            break;
                        }
                        i++;
                    }
                    Log.e("Content-Type", "Type: " + header.getValue());
                    if (header.getValue().toLowerCase().contains("text/html")) {
                        byte[] bArr = new byte[10000];
                        InputStream content = httpResponseExecute.getEntity().getContent();
                        int i2 = 0;
                        do {
                            int i3 = content.read();
                            if (i3 == -1) {
                                break;
                            }
                            bArr[i2] = (byte) i3;
                            i2++;
                        } while (i2 < 10000);
                        String str2 = new String(utilities.copyOfRange(bArr, 0, i2), "utf8");
                        Log.e("HttpDisco", str2);
                        String strExtractTitle = HttpDisco.this.extractTitle(str2);
                        if (strExtractTitle != null) {
                            HttpDisco.this.result = xml_utils.decodeString(strExtractTitle);
                        }
                        content.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (HttpDisco.this.locker) {
                    HttpDisco.this.locker.notify();
                }
            }
        };
        thread.setPriority(1);
        thread.start();
        synchronized (this.locker) {
            try {
                this.locker.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.result;
    }

    public void discoveryAsync(final String str, final OnDiscoListener onDiscoListener) {
        this.result = str;
        Thread thread = new Thread() {
            @Override
            public void run() {
                HttpDisco.this.discovery(str);
                jasminSvc jasminsvc = resources.service;
                final OnDiscoListener onDiscoListener2 = onDiscoListener;
                jasminsvc.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        if (onDiscoListener2 != null) {
                            onDiscoListener2.OnDisco(HttpDisco.this.result);
                        }
                    }
                });
            }
        };
        thread.setPriority(1);
        thread.start();
    }
}
