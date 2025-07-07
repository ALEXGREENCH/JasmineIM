package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Vector;
import ru.ivansuper.jasmin.animate_tools.Movie;
import ru.ivansuper.jasmin.animate_tools.MySpan;

public class SmileysManager {
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;
    public static boolean loading = false;
    public static int max_height = 1;
    public static int max_width = 1;
    public static boolean packLoaded = false;
    public static Vector<Movie> selector_smileys = new Vector();
    public static Vector<String> selector_tags = new Vector();
    public static Vector<Movie> smileys = new Vector();
    public static Vector<String> tags = new Vector();

    public static void forceChangeScale() {
        //noinspection deprecation
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        int scaleValue = Integer.parseInt(preferences.getString("ms_smileys_scale", "3"));

        if (!selector_smileys.isEmpty()) {
            max_width = 0;
            max_height = 0;

            for (Movie movie : selector_smileys) {
                movie.changeScale(ctx, scaleValue);

                if (max_width < movie.getWidth()) {
                    max_width = movie.getWidth();
                }

                if (max_height < movie.getHeight()) {
                    max_height = movie.getHeight();
                }
            }
        }
    }

    private static String getPlomb(int length) {
        StringBuilder plomb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            plomb.append("#");
        }

        return plomb.toString();
    }

    /** @noinspection unused*/
    public static SpannableStringBuilder getSmiledText(SpannableStringBuilder text, int var1, boolean var2) {
        return getSmiledText(text, var1, var2, 0);
    }

    public static SpannableStringBuilder getSmiledText(SpannableStringBuilder text, int var1, boolean var2, int var3) {
        SpannableStringBuilder result = (text != null) ? text : new SpannableStringBuilder("");
        String textString = result.toString();
        int tagCount = tags.size();

        for (int i = 0; i < tagCount; i++) {
            String tag = tags.get(i);
            int tagLength = tag.length();
            int startIdx = var1;
            String plomb = getPlomb(tagLength);

            while (true) {
                startIdx = textString.indexOf(tag, startIdx);

                if (startIdx < 0) {
                    break;
                }

                if (!utilities.isThereLinks(result, startIdx, startIdx + tagLength)) {
                    String beforeTag = textString.substring(0, startIdx);
                    textString = textString.substring(startIdx + tagLength);
                    textString = beforeTag + plomb + textString;
                    MySpan span;

                    if (var3 <= 0) {
                        span = new MySpan(smileys.get(i), var2);
                    } else {
                        span = new MySpan(smileys.get(i), var2, var3);
                    }

                    result.setSpan(span, startIdx, startIdx + tagLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                startIdx += tagLength - 1;
            }
        }

        return result;
    }

    /** @noinspection unused*/
    public static SpannableStringBuilder getSmiledText(CharSequence text, int var1, boolean var2) {
        return getSmiledText(new SpannableStringBuilder(text), var1, var2, 0);
    }

    /** @noinspection unused*/
    public static String getTag(String text) {
        String result = null;
        int tagIndex = 0;

        while (tagIndex < tags.size()) {
            String tag = tags.get(tagIndex);

            if (text.startsWith(tag)) {
                result = tag;
                break;
            }

            tagIndex++;
        }

        return (result != null) ? result : "";
    }

    public static void init(Context context) {
        ctx = context;
        File smileysDir = new File(resources.SD_PATH + "/Jasmine/Smileys");

        if (!smileysDir.isDirectory() || !smileysDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            smileysDir.mkdirs();
        }
    }

    public static void loadFromAssets() {
        try {
            tags.clear();
            smileys.clear();
            selector_tags.clear();
            selector_smileys.clear();
            loading = true;

            BufferedReader reader = new BufferedReader(new InputStreamReader(resources.am.open("define.ini")));
            int var2 = 1;

            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    forceChangeScale();
                    packLoaded = true;
                    break;
                }

                String[] parts = line.split(",");
                BufferedInputStream stream = new BufferedInputStream(resources.am.open(String.valueOf(var2)));
                Movie movie = new Movie(stream, ctx);
                stream.close();

                selector_tags.add(parts[0]);
                selector_smileys.add(movie);

                for (String part : parts) {
                    tags.add(part);
                    smileys.add(movie);
                }

                var2++;
            }

            loading = false;
        } catch (Exception e) {
            tags.clear();
            smileys.clear();
            selector_tags.clear();
            selector_smileys.clear();
            packLoaded = false;
            Log.e("SmileysManager", "Smiley pack load error!", e);
        }
    }

    public static void loadFromFile(File directory) {
        try {
            File defineIniFile = new File(directory, "define.ini");

            if (!defineIniFile.exists()) {
                return;
            }

            tags.clear();
            smileys.clear();
            selector_tags.clear();
            selector_smileys.clear();
            Log.v("loadFromFile", "DEFINE.INI FOUND!");

            int fileIndex = 1;
            BufferedReader reader = new BufferedReader(new FileReader(defineIniFile));

            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    forceChangeScale();
                    packLoaded = true;
                    return;
                }

                String[] parts = line.split(",");
                boolean isFirstTag = true;
                Movie movie = null;

                for (String part : parts) {
                    File smileyFile = new File(directory, String.valueOf(fileIndex));
                    FileInputStream inputStream = new FileInputStream(smileyFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                    if (isFirstTag) {
                        selector_tags.add(part);
                        isFirstTag = false;
                        movie = new Movie(bufferedInputStream, ctx);
                        selector_smileys.add(movie);
                    }

                    tags.add(part);
                    smileys.add(movie);

                    bufferedInputStream.close();
                    fileIndex++;
                }
            }
        } catch (Exception e) {
            tags.clear();
            smileys.clear();
            selector_tags.clear();
            selector_smileys.clear();
            packLoaded = false;
            Log.e("SmileysManager", "Smiley pack load error!", e);
        }
    }

    public static void loadPack() {
        final String smilePackPath = PreferenceManager.getDefaultSharedPreferences(ctx).getString("current_smileys_pack", "$*INTERNAL*$");

        Runnable loadRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //noinspection DataFlowIssue
                    if (smilePackPath.equals("$*INTERNAL*$")) {
                        SmileysManager.loadFromAssets();
                    } else {
                        File packDirectory = new File(resources.SD_PATH, "Jasmine/Smileys/" + smilePackPath);
                        SmileysManager.loadFromFile(packDirectory);
                    }
                } catch (OutOfMemoryError e) {
                    Log.e("SmileysManager", "====== SMILEYS PACK NOT LOADED! OUT OF MEMORY ERROR! ======");
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(loadRunnable, "SmileysPack loader");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public static void preloadPack() {
        if (!packLoaded) {
            loadPack();
        }
    }
}