package ru.ivansuper.jasmin.jabber.dns;

import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.resources;

public class DNS {

    public interface DNSListener {
        void onResult(String str);
    }

    public static final void resolve(final String str, final DNSListener dNSListener) {
        new Thread() {
            @Override
            public void run() {
                DnsSrvResolver dnsSrvResolver = new DnsSrvResolver();
                dnsSrvResolver.getSrv(str, 1);
                final String host = dnsSrvResolver.getHost();
                if (dNSListener != null) {
                    jasminSvc jasminsvc = resources.service;
                    final DNSListener dNSListener2 = dNSListener;
                    jasminsvc.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            dNSListener2.onResult(host);
                        }
                    });
                }
            }
        }.start();
    }
}
