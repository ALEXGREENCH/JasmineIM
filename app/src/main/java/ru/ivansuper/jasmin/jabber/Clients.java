package ru.ivansuper.jasmin.jabber;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class Clients {
    private static final Vector<Caps> caps = new Vector<>();
    private static final Vector<String> names = new Vector<>();
    private static final Vector<Drawable> icons = new Vector<>();

    private static class Caps {
        public String[] caps;

        public Caps() {
        }

        public Caps(String[] strArr) {
            this.caps = strArr;
        }
    }

    public static final int foundCap(String str) {
        if (str == null) {
            return -1;
        }
        for (int i = 0; i < caps.size(); i++) {
            for (String str2 : caps.get(i).caps) {
                if (str2.trim().length() > 0 && str.indexOf(str2) >= 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static final Drawable getIcon(int i) {
        if (i != -1 && i < icons.size()) {
            return icons.get(i);
        }
        return null;
    }

    public static final String getName(int i) {
        return (i != -1 && i < names.size()) ? names.get(i) : " - ";
    }

    public static final void load() {
        File file = new File(String.valueOf(utilities.normalizePath(resources.JASMINE_SD_PATH)) + "Clients/jabber/");
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
            }
        }
        try {
            read(new FileInputStream(new File(String.valueOf(utilities.normalizePath(resources.JASMINE_SD_PATH)) + "Clients/jabber/clients.txt")), new FileInputStream(new File(String.valueOf(utilities.normalizePath(resources.JASMINE_SD_PATH)) + "Clients/jabber/clients.png")));
        } catch (Exception e2) {
        }
        if (caps.size() == 0 || caps.size() != icons.size()) {
            try {
                read(resources.am.open("jabber/clients.txt"), resources.am.open("jabber/clients.png"));
            } catch (Exception e3) {
            }
        }
    }

    private static final void read(InputStream inputStream, InputStream inputStream2) throws Exception {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (bufferedReader.ready()) {
                try {
                    String[] strArrSplit = bufferedReader.readLine().split("\t");
                    if (strArrSplit.length == 2) {
                        caps.add(new Caps(strArrSplit[0].trim().split(",")));
                        names.add(strArrSplit[1]);
                    }
                } catch (Exception e) {
                    e = e;
                    e.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e2) {
                        }
                    }
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            bufferedReader = null;
        }
        try {
            bufferedReader.close();
        } catch (Exception e4) {
        }
        try {
            Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(inputStream2);
            bitmapDecodeStream.setDensity(0);
            int width = bitmapDecodeStream.getWidth();
            int height = bitmapDecodeStream.getHeight();
            int i = width / 16;
            for (int i2 = 0; i2 < height; i2 += i) {
                for (int i3 = 0; i3 < width; i3 += i) {
                    Bitmap bitmapCreateBitmap = Bitmap.createBitmap(bitmapDecodeStream, i3, i2, i, i);
                    bitmapCreateBitmap.setDensity(0);
                    icons.add(resources.normalizeIconDPIBased(new BitmapDrawable(bitmapCreateBitmap)));
                }
            }
        } catch (Exception e5) {
            e5.printStackTrace();
        }
    }
}
