package ru.ivansuper.jasmin.security;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import ru.ivansuper.jasmin.resources;

/**
 * TLS on top of an already connected socket, done the way modern servers require it and old
 * Android versions do not do by default:
 * <ul>
 * <li>every TLS version the platform supports is enabled (TLS 1.1/1.2 exist but are switched
 *     off on Android 4.1-4.4, so a TLS 1.2-only server was unreachable);</li>
 * <li>SNI carries the XMPP domain, not the host we happened to connect to;</li>
 * <li>the handshake runs eagerly, so a failure surfaces here instead of on the first read;</li>
 * <li>the certificate chain is checked against the system store plus the roots bundled in
 *     {@code assets/tls_roots.pem} (Let's Encrypt's ISRG roots are missing before Android 7.1.1);</li>
 * <li>the certificate's name is verified against the domain (or the explicitly configured
 *     server host) - something the previous code never did.</li>
 * </ul>
 */
public final class TlsSupport {
    private static final String TAG = "TlsSupport";
    private static final String EXTRA_ROOTS_ASSET = "tls_roots.pem";

    private static X509Certificate[] extraRoots;

    private TlsSupport() {
    }

    /** Thrown when the peer certificate is not acceptable (chain or name), as opposed to an I/O failure. */
    public static final class CertificateRejectedException extends IOException {
        public CertificateRejectedException(String message, Throwable cause) {
            super(message);
            initCause(cause);
        }
    }

    /**
     * Wraps {@code plain} (already connected) in TLS and completes the handshake.
     *
     * @param sniHost       name sent in SNI and used for session caching; the XMPP domain
     * @param acceptedNames names the certificate may be issued for; the first match wins
     * @param verify        false skips both chain and name checks (the user's "check certificate" preference)
     */
    public static SSLSocket upgrade(Socket plain, String sniHost, int port, Collection<String> acceptedNames, boolean verify) throws IOException {
        SSLSocket ssl;
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new ChainTrustManager(verify)}, null);
            SSLSocketFactory factory = ctx.getSocketFactory();
            ssl = (SSLSocket) factory.createSocket(plain, sniHost, port, true);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("cannot create TLS socket: " + e);
        }
        ssl.setUseClientMode(true);
        enableAllTlsVersions(ssl);
        setSni(ssl, sniHost);
        try {
            ssl.startHandshake();
        } catch (IOException e) {
            if (e.getCause() instanceof CertificateException) {
                throw new CertificateRejectedException(e.getMessage(), e);
            }
            throw e;
        }
        SSLSession session = ssl.getSession();
        Log.i(TAG, "TLS " + session.getProtocol() + " " + session.getCipherSuite() + " to " + sniHost);
        if (verify) {
            verifyPeerName(session, acceptedNames);
        }
        return ssl;
    }

    private static void enableAllTlsVersions(SSLSocket ssl) {
        List<String> protocols = new ArrayList<>();
        for (String p : ssl.getSupportedProtocols()) {
            if (p.startsWith("TLSv1")) protocols.add(p);   // no SSLv3
        }
        if (!protocols.isEmpty()) {
            try {
                ssl.setEnabledProtocols(protocols.toArray(new String[0]));
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "cannot enable " + protocols + ": " + e);
            }
        }
        // Before Android 5 the default cipher list may miss the TLS 1.2 suites a modern server
        // insists on; widen it to everything supported that is not obviously weak.
        if (Build.VERSION.SDK_INT < 21) {
            List<String> suites = new ArrayList<>();
            for (String c : ssl.getSupportedCipherSuites()) {
                if (c.contains("_NULL_") || c.contains("_anon_") || c.contains("EXPORT") || c.contains("_DES_")
                        || c.contains("RC4") || c.contains("_MD5") || c.contains("PSK") || c.contains("KRB5")) {
                    continue;
                }
                suites.add(c);
            }
            if (!suites.isEmpty()) {
                try {
                    ssl.setEnabledCipherSuites(suites.toArray(new String[0]));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "cannot set cipher suites: " + e);
                }
            }
        }
    }

    private static void setSni(SSLSocket ssl, String host) {
        if (host == null || host.isEmpty() || isIpAddress(host)) return;
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                SSLParameters params = ssl.getSSLParameters();
                params.setServerNames(Collections.singletonList(new SNIHostName(host)));
                ssl.setSSLParameters(params);
                return;
            } catch (Exception e) {
                Log.w(TAG, "SSLParameters SNI failed: " + e);
            }
        }
        // Android 4.2+: the platform socket has a public setHostname(String) that drives SNI.
        try {
            Method m = ssl.getClass().getMethod("setHostname", String.class);
            m.invoke(ssl, host);
        } catch (Exception e) {
            Log.w(TAG, "no setHostname on " + ssl.getClass().getName() + ", SNI relies on the factory host");
        }
    }

    /**
     * RFC 6125-style check of the leaf certificate against the accepted names: SAN dNSName
     * entries (a wildcard covers exactly one label), the subject CN only when there is no
     * dNSName at all. The platform's own verifier is consulted as a second opinion.
     */
    private static void verifyPeerName(SSLSession session, Collection<String> acceptedNames) throws IOException {
        X509Certificate leaf = null;
        try {
            Certificate[] chain = session.getPeerCertificates();
            if (chain.length > 0 && chain[0] instanceof X509Certificate) leaf = (X509Certificate) chain[0];
        } catch (Exception e) {
            Log.w(TAG, "no peer certificate: " + e);
        }
        if (leaf == null) throw new CertificateRejectedException("no peer certificate", new SSLPeerUnverifiedException("no certificate"));

        List<String> certNames = certificateNames(leaf);
        for (String name : acceptedNames) {
            if (name == null || name.isEmpty()) continue;
            String wanted = name.toLowerCase(java.util.Locale.US);
            for (String cn : certNames) {
                if (nameMatches(cn, wanted)) return;
            }
        }
        try {
            HostnameVerifier verifier = HttpsURLConnection.getDefaultHostnameVerifier();
            for (String name : acceptedNames) {
                if (name != null && !name.isEmpty() && verifier.verify(name, session)) return;
            }
        } catch (Exception e) {
            Log.w(TAG, "platform verifier failed: " + e);
        }
        throw new CertificateRejectedException("certificate for " + certNames + " is not valid for " + acceptedNames,
                new SSLPeerUnverifiedException("hostname mismatch"));
    }

    /** dNSName SAN entries, lower-cased; falls back to the subject CN when the certificate has none. */
    static List<String> certificateNames(X509Certificate cert) {
        List<String> names = new ArrayList<>();
        try {
            Collection<List<?>> sans = cert.getSubjectAlternativeNames();
            if (sans != null) {
                for (List<?> san : sans) {
                    if (san.size() >= 2 && Integer.valueOf(2).equals(san.get(0)) && san.get(1) instanceof String) {
                        names.add(((String) san.get(1)).toLowerCase(java.util.Locale.US));
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot read SAN: " + e);
        }
        if (names.isEmpty()) {
            String dn = cert.getSubjectDN().getName();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)(?:^|,)\\s*CN=([^,]+)").matcher(dn);
            if (m.find()) names.add(m.group(1).trim().toLowerCase(java.util.Locale.US));
        }
        return names;
    }

    /** {@code pattern} is a certificate name, possibly {@code *.example.org}; {@code host} is lower-case. */
    static boolean nameMatches(String pattern, String host) {
        if (pattern.startsWith("*.")) {
            String suffix = pattern.substring(1);              // ".example.org"
            if (!host.endsWith(suffix)) return false;
            String label = host.substring(0, host.length() - suffix.length());
            return !label.isEmpty() && label.indexOf('.') < 0;   // exactly one label, no empty match
        }
        return pattern.equals(host);
    }

    private static boolean isIpAddress(String host) {
        return host.matches("[0-9.]+") || host.indexOf(':') >= 0;
    }

    // -- trust ------------------------------------------------------------------

    /**
     * System trust store first; if that rejects the chain, the roots bundled with the app.
     * With {@code verify} off, everything is accepted (the user asked for it).
     */
    private static final class ChainTrustManager implements X509TrustManager {
        private final boolean verify;

        ChainTrustManager(boolean verify) {
            this.verify = verify;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (!verify) {
                Log.w(TAG, "certificate check disabled by preference; accepting " + describe(chain));
                return;
            }
            CertificateException first;
            try {
                check(systemTrustManagers(), chain, authType);
                return;
            } catch (CertificateException e) {
                first = e;
            }
            try {
                X509TrustManager[] extra = bundledTrustManagers();
                if (extra.length > 0) {
                    check(extra, chain, authType);
                    Log.i(TAG, "chain accepted via bundled roots: " + describe(chain));
                    return;
                }
            } catch (CertificateException e) {
                Log.w(TAG, "bundled roots rejected " + describe(chain) + ": " + e);
            }
            throw first;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        private static void check(X509TrustManager[] managers, X509Certificate[] chain, String authType) throws CertificateException {
            if (managers.length == 0) throw new CertificateException("no trust managers");
            managers[0].checkServerTrusted(chain, authType);
        }

        private static String describe(X509Certificate[] chain) {
            return chain != null && chain.length > 0 ? chain[0].getSubjectDN().getName() : "empty chain";
        }
    }

    private static X509TrustManager[] systemTrustManagers() throws CertificateException {
        return trustManagersFor(null);
    }

    private static X509TrustManager[] bundledTrustManagers() throws CertificateException {
        X509Certificate[] roots = loadExtraRoots();
        if (roots.length == 0) return new X509TrustManager[0];
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            for (int i = 0; i < roots.length; i++) ks.setCertificateEntry("extra" + i, roots[i]);
            return trustManagersFor(ks);
        } catch (Exception e) {
            throw new CertificateException("bundled roots unusable: " + e);
        }
    }

    private static X509TrustManager[] trustManagersFor(KeyStore ks) throws CertificateException {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            List<X509TrustManager> out = new ArrayList<>();
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) out.add((X509TrustManager) tm);
            }
            return out.toArray(new X509TrustManager[0]);
        } catch (Exception e) {
            throw new CertificateException("trust manager init failed: " + e);
        }
    }

    private static synchronized X509Certificate[] loadExtraRoots() {
        if (extraRoots != null) return extraRoots;
        List<X509Certificate> list = new ArrayList<>();
        InputStream in = null;
        try {
            in = resources.ctx.getAssets().open(EXTRA_ROOTS_ASSET);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (Certificate c : cf.generateCertificates(in)) {
                if (c instanceof X509Certificate) list.add((X509Certificate) c);
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot load " + EXTRA_ROOTS_ASSET + ": " + e);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ignored) {
            }
        }
        extraRoots = list.toArray(new X509Certificate[0]);
        Log.i(TAG, extraRoots.length + " bundled root(s) loaded");
        return extraRoots;
    }
}
