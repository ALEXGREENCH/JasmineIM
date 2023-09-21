package ru.ivansuper.jasmin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.animate_tools.Movie;
import ru.ivansuper.jasmin.animate_tools.MySpan;

public class SmileysManager {
    private static Context ctx;
    public static boolean loading = false;
    public static int max_height = 1;
    public static int max_width = 1;
    public static boolean packLoaded = false;
    public static Vector<Movie> selector_smileys = new Vector();
    public static Vector<String> selector_tags = new Vector();
    public static Vector<Movie> smileys = new Vector();
    public static Vector<String> tags = new Vector();

    public SmileysManager() {
    }

    public static void forceChangeScale() {
        if (selector_smileys.size() != 0) {
            SharedPreferences var0 = PreferenceManager.getDefaultSharedPreferences(ctx);

            int var1;
            boolean var10001;
            Iterator var6;
            try {
                max_width = 0;
                max_height = 0;
                var1 = Integer.parseInt(var0.getString("ms_smileys_scale", "3"));
                var6 = selector_smileys.iterator();
            } catch (Exception var5) {
                var10001 = false;
                return;
            }

            while(true) {
                Movie var2;
                try {
                    if (!var6.hasNext()) {
                        break;
                    }

                    var2 = (Movie)var6.next();
                    var2.changeScale(ctx, var1);
                    if (max_width < var2.getWidth()) {
                        max_width = var2.getWidth();
                    }
                } catch (Exception var4) {
                    var10001 = false;
                    break;
                }

                try {
                    if (max_height < var2.getHeight()) {
                        max_height = var2.getHeight();
                    }
                } catch (Exception var3) {
                    var10001 = false;
                    break;
                }
            }
        }

    }

    private static String getPlomb(int var0) {
        String var1 = "";

        for(int var2 = 0; var2 < var0; ++var2) {
            var1 = var1 + "#";
        }

        return var1;
    }

    public static SpannableStringBuilder getSmiledText(SpannableStringBuilder var0, int var1, boolean var2) {
        return getSmiledText(var0, var1, var2, 0);
    }

    public static SpannableStringBuilder getSmiledText(SpannableStringBuilder var0, int var1, boolean var2, int var3) {
        SpannableStringBuilder var4 = var0;
        if (var0 == null) {
            var4 = new SpannableStringBuilder("");
        }

        String var12 = var4.toString();
        int var5 = tags.size();

        for(int var6 = 0; var6 < var5; ++var6) {
            String var7 = (String)tags.get(var6);
            int var8 = var7.length();
            int var9 = var1;
            String var10 = getPlomb(var8);

            while(true) {
                var9 = var12.indexOf(var7, var9);
                if (var9 < 0) {
                    break;
                }

                String var11 = var12;
                if (!utilities.isThereLinks(var4, new int[]{var9, var9 + var8})) {
                    var11 = var12.substring(0, var9);
                    var12 = var12.substring(var9 + var8, var12.length());
                    var11 = var11 + var10 + var12;
                    MySpan var13;
                    if (var3 <= 0) {
                        var13 = new MySpan((Movie)smileys.get(var6), var2);
                    } else {
                        var13 = new MySpan((Movie)smileys.get(var6), var2, var3);
                    }

                    var4.setSpan(var13, var9, var9 + var8, 33);
                }

                var9 += var8 - 1;
                var12 = var11;
            }
        }

        return var4;
    }

    public static SpannableStringBuilder getSmiledText(CharSequence var0, int var1, boolean var2) {
        return getSmiledText(new SpannableStringBuilder(var0), var1, var2, 0);
    }

    public static String getTag(String var0) {
        Object var1 = null;
        int var2 = 0;

        while(true) {
            if (var2 >= tags.size()) {
                var0 = (String)var1;
                break;
            }

            String var3 = (String)tags.get(var2);
            if (var0.startsWith(var3)) {
                var0 = var3;
                break;
            }

            ++var2;
        }

        return var0;
    }

    public static void init(Context var0) {
        ctx = var0;
        File var1 = new File(resources.SD_PATH + "/Jasmine/Smileys");
        if (!var1.isDirectory() || !var1.exists()) {
            var1.mkdirs();
        }

    }

    public static void loadFromAssets() {
        tags.clear();
        smileys.clear();
        selector_tags.clear();
        selector_smileys.clear();
        loading = true;

        label59: {
            Exception var10000;
            label63: {
                BufferedReader var0;
                boolean var10001;
                try {
                    InputStreamReader var1 = new InputStreamReader(resources.am.open("define.ini"));
                    var0 = new BufferedReader(var1);
                } catch (Exception var11) {
                    var10000 = var11;
                    var10001 = false;
                    break label63;
                }

                int var2 = 1;

                label54:
                while(true) {
                    String var13;
                    try {
                        var13 = var0.readLine();
                    } catch (Exception var7) {
                        var10000 = var7;
                        var10001 = false;
                        break;
                    }

                    if (var13 == null) {
                        try {
                            forceChangeScale();
                            packLoaded = true;
                            break label59;
                        } catch (Exception var6) {
                            var10000 = var6;
                            var10001 = false;
                            break;
                        }
                    }

                    Movie var4;
                    String[] var14;
                    try {
                        var14 = var13.split(",");
                        BufferedInputStream var3 = new BufferedInputStream(resources.am.open(String.valueOf(var2)));
                        var4 = new Movie(var3, ctx);
                        var3.close();
                        selector_tags.add(var14[0]);
                        selector_smileys.add(var4);
                    } catch (Exception var9) {
                        var10000 = var9;
                        var10001 = false;
                        break;
                    }

                    int var5 = 0;

                    while(true) {
                        try {
                            if (var5 >= var14.length) {
                                System.gc();
                                break;
                            }
                        } catch (Exception var10) {
                            var10000 = var10;
                            var10001 = false;
                            break label54;
                        }

                        try {
                            tags.add(var14[var5]);
                            smileys.add(var4);
                        } catch (Exception var8) {
                            var10000 = var8;
                            var10001 = false;
                            break label54;
                        }

                        ++var5;
                    }

                    ++var2;
                }
            }

            Exception var12 = var10000;
            tags.clear();
            smileys.clear();
            selector_tags.clear();
            selector_smileys.clear();
            packLoaded = false;
            Log.e("SmileysManager", "Smiley pack load error!");
            var12.printStackTrace();
        }

        loading = false;
    }

    public static void loadFromFile(File var0) {
        Exception var10000;
        label73: {
            File var1;
            boolean var10001;
            String var18;
            try {
                var18 = var0.getAbsolutePath();
                StringBuilder var2 = new StringBuilder(String.valueOf(var18));
                var1 = new File(var2.append("/define.ini").toString());
                if (!var1.exists()) {
                    return;
                }

                tags.clear();
                smileys.clear();
                selector_tags.clear();
                selector_smileys.clear();
                Log.v("loadFromFile", "DEFINE.INI FOUND!");
            } catch (Exception var17) {
                var10000 = var17;
                var10001 = false;
                break label73;
            }

            int var3 = 1;

            BufferedReader var22;
            try {
                FileReader var4 = new FileReader(var1);
                var22 = new BufferedReader(var4);
            } catch (Exception var15) {
                var10000 = var15;
                var10001 = false;
                break label73;
            }

            label65:
            while(true) {
                String var20;
                try {
                    var20 = var22.readLine();
                } catch (Exception var11) {
                    var10000 = var11;
                    var10001 = false;
                    break;
                }

                if (var20 == null) {
                    try {
                        forceChangeScale();
                        packLoaded = true;
                        return;
                    } catch (Exception var10) {
                        var10000 = var10;
                        var10001 = false;
                        break;
                    }
                }

                boolean var5 = false;

                String[] var23;
                Movie var24;
                try {
                    var23 = var20.split(",");
                    StringBuilder var8 = new StringBuilder(String.valueOf(var18));
                    File var7 = new File(var8.append("/").append(String.valueOf(var3)).toString());
                    FileInputStream var6 = new FileInputStream(var7);
                    BufferedInputStream var21 = new BufferedInputStream(var6);
                    var24 = new Movie(var21, ctx);
                    var21.close();
                } catch (Exception var14) {
                    var10000 = var14;
                    var10001 = false;
                    break;
                }

                int var9 = 0;

                while(true) {
                    try {
                        if (var9 >= var23.length) {
                            break;
                        }
                    } catch (Exception var16) {
                        var10000 = var16;
                        var10001 = false;
                        break label65;
                    }

                    try {
                        tags.add(var23[var9]);
                        smileys.add(var24);
                    } catch (Exception var13) {
                        var10000 = var13;
                        var10001 = false;
                        break label65;
                    }

                    if (!var5) {
                        try {
                            selector_tags.add(var23[0]);
                            selector_smileys.add(var24);
                        } catch (Exception var12) {
                            var10000 = var12;
                            var10001 = false;
                            break label65;
                        }
                    }

                    var5 = true;
                    ++var9;
                }

                ++var3;
            }
        }

        Exception var19 = var10000;
        tags.clear();
        smileys.clear();
        selector_tags.clear();
        selector_smileys.clear();
        packLoaded = false;
        Log.e("SmileysManager", "Smiley pack load error!");
        var19.printStackTrace();
    }

    public static void loadPack() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                String smilePackPath = PreferenceManager.getDefaultSharedPreferences(ctx).getString("current_smileys_pack", "$*INTERNAL*$");
                try {
                    if (smilePackPath.equals("$*INTERNAL*$")) {
                        SmileysManager.loadFromAssets();
                    } else {
                        StringBuilder stringBuilder = new StringBuilder(String.valueOf(resources.SD_PATH));
                        File var1 = new File(stringBuilder.append("/Jasmine/Smileys/").append(smilePackPath).toString());
                        SmileysManager.loadFromFile(var1);
                    }
                } catch (OutOfMemoryError var3) {
                    Log.e("SmileysManager", "====== SMILEYS PACK NOT LOADED! OUT OF MEMORY ERROR! ======");
                    var3.printStackTrace();
                }
            }
        };
        thread.setName("SmileysPack loader");
        thread.setPriority(10);
        thread.start();
    }

    public static void preloadPack() {
        if (!packLoaded) {
            loadPack();
        }
    }
}