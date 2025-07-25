package ru.ivansuper.jasmin.icq;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.InputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import ru.ivansuper.jasmin.Base64Coder;

/**
 * The HTTPAuthorizer class handles the HTTP-based authorization process for an ICQ profile.
 * It communicates with the ICQ authentication server to obtain session credentials.
 * The class uses an {@link HTTPAuthListener} to report progress, success, or errors during authorization.
 *
 * <p>The authorization process involves two main steps:
 * <ol>
 *     <li>Requesting a session secret and token from the ICQ login API.</li>
 *     <li>Using the obtained credentials to start an OSCAR session.</li>
 * </ol>
 *
 * <p>This class is designed to be run in a separate thread to avoid blocking the main UI thread.
 *
 * @see ICQProfile
 * @see HTTPAuthListener
 */
public class HTTPAuthorizer {
    private final HTTPAuthListener listener;
    private final ICQProfile profile;

    public interface HTTPAuthListener {
        void onError(int i);

        void onProgress(int i);

        void onSuccess(String str, byte[] bArr);
    }

    public HTTPAuthorizer(ICQProfile profile, HTTPAuthListener listener) {
        this.profile = profile;
        this.listener = listener;
    }

    public void performAuthorization() {
        if (this.profile.connecting) {
            Thread auth_thread = new Thread() {
                @Override
                public void run() {
                    setName("HTTP authorization thread");
                    try {
                        HTTPAuthorizer.this.doAuth();
                    } catch (Exception e) {
                        HTTPAuthorizer.this.listener.onError(255);
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                    }
                }
            };
            auth_thread.start();
        }
    }

    /** @noinspection deprecation*/
    private void doAuth() throws Exception {
        this.listener.onProgress(1);
        Log.e("Auth", "Connecting");
        BasicHttpParams bhp = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(bhp, 20000);
        DefaultHttpClient client = new DefaultHttpClient(bhp);
        HttpPost post = new HttpPost("https://api.login.icq.net/auth/clientLogin");
        ArrayList<BasicNameValuePair> pairs = new ArrayList<>(4);
        pairs.add(new BasicNameValuePair("f", "xml"));
        pairs.add(new BasicNameValuePair("k", "ic1vDmhXTZE4LgYh"));
        pairs.add(new BasicNameValuePair("pwd", this.profile.password));
        pairs.add(new BasicNameValuePair("s", this.profile.ID));
        UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(pairs);
        post.setEntity(uefe);
        HttpResponse response = client.execute(post);
        if (this.profile.connecting) {
            this.listener.onProgress(2);
            StatusLine status = response.getStatusLine();
            int code = status.getStatusCode();
            if (code != 200) {
                Log.e("Auth:code", "HTTP Error: " + code);
                this.listener.onError(code);
                return;
            }
            Log.e("Auth:status", status.getReasonPhrase());
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            StringBuilder result = new StringBuilder();
            int readed = 0;
            while (readed != -1) {
                readed = is.read();
                if (readed == -1) {
                    break;
                } else {
                    result.append((char) readed);
                }
            }
            is.close();
            String xml = result.toString();
            Log.i("Auth:result:xml:string", xml);
            String session_secret = getXMLNodeValue(xml, "sessionSecret");
            String token = getXMLNodeValue(xml, "a");
            String host_time = getXMLNodeValue(xml, "hostTime");
            Log.e("Auth:result:xml:secret", session_secret);
            Log.e("Auth:result:xml:token", token);
            Log.e("Auth:result:xml:time", host_time);
            String answer = "a=" + URLEncoder.encode(token) + "&buildNumber=1000&clientName=Jasmine%20IM&clientVersion=1000&distId=20300&f=xml&k=ic1vDmhXTZE4LgYh&majorVersion=33&minorVersion=0&pointVersion=0&port=5190&ts=" + host_time;
            String answer2 = "GET&" + URLEncoder.encode("http://api.icq.net:5190/aim/startOSCARSession") + "&" + URLEncoder.encode(answer);
            String first = getHmacSha256Base64(session_secret, this.profile.password);
            String final_ = getHmacSha256Base64(answer2, first);
            String a = "http://api.icq.net:5190/aim/startOSCARSession?" + answer + "&sig_sha256=" + final_;
            BasicHttpParams bhp2 = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(bhp2, 20000);
            DefaultHttpClient client2 = new DefaultHttpClient(bhp2);
            if (this.profile.connecting) {
                HttpGet hget = new HttpGet(a);
                HttpResponse response2 = client2.execute(hget);
                this.listener.onProgress(3);
                int code2 = response2.getStatusLine().getStatusCode();
                if (code2 != 200) {
                    Log.e("Auth:code", "HTTP Error: " + code2);
                    this.listener.onError(code2);
                    return;
                }
                HttpEntity entity2 = response2.getEntity();
                InputStream is2 = entity2.getContent();
                StringBuilder result2 = new StringBuilder();
                int readed2 = 0;
                while (readed2 != -1) {
                    readed2 = is2.read();
                    if (readed2 == -1) {
                        break;
                    } else {
                        result2.append((char) readed2);
                    }
                }
                is2.close();
                String xml2 = result2.toString();
                Log.i("Auth:final:xml:string", xml2);
                String status_detail_code = getXMLNodeValue(xml2, "statusDetailCode");
                if (status_detail_code != null) {
                    this.listener.onError(Integer.parseInt(status_detail_code));
                    return;
                }
                String bos = getXMLNodeValue(xml2, "host") + ":" + getXMLNodeValue(xml2, "port");
                byte[] cookie = Base64Coder.decode(getXMLNodeValue(xml2, "cookie"));
                Log.i("Auth:final:xml:bos", bos);
                if (this.profile.connecting) {
                    this.listener.onSuccess(bos, cookie);
                }
            }
        }
    }

    private static String getHmacSha256Base64(String a, String b) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec sks = new SecretKeySpec(b.getBytes(), "HmacSHA256");
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(sks);
        m.update(a.getBytes());
        byte[] result = m.doFinal();
        //noinspection UnnecessaryLocalVariable
        String encoded = new String(Base64Coder.encode(result));
        return encoded;
    }

    private static String getXMLNodeValue(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">");
        int end = xml.indexOf("</" + tag + ">");
        if (start == -1 || end == -1) {
            return null;
        }
        //noinspection UnnecessaryLocalVariable
        String result = xml.substring(start + 2 + tag.length(), end);
        return result;
    }
}
