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

/**
 * Manages smileys, including loading, scaling, and replacing text with smileys.
 * <p>
 * This class provides static methods to handle smiley packs. It can load smileys
 * from application assets or from external files. It also handles scaling of smileys
 * and provides a method to convert text containing smiley tags into a
 * SpannableStringBuilder with the tags replaced by smiley images.
 * </p>
 * <p>
 * Key functionalities include:
 * <ul>
 *     <li>Initialization with an Android Context.</li>
 *     <li>Loading smiley packs from assets or external storage.</li>
 *     <li>Forcing a rescale of all loaded smileys.</li>
 *     <li>Replacing smiley tags in text with corresponding smiley images.</li>
 *     <li>Retrieving a smiley tag from the beginning of a given text.</li>
 *     <li>Preloading smiley packs if they haven't been loaded yet.</li>
 * </ul>
 * </p>
 * <p>
 * Smileys are represented by {@link Movie} objects and their corresponding text tags.
 * The class maintains lists of these tags and movies.
 * </p>
 *
 * @see Movie
 * @see MySpan
 */
public class SmileysManager {
    /**
     * The application context, used for accessing resources and preferences.
     * This field is marked with {@code @SuppressLint("StaticFieldLeak")} because
     * it holds a static reference to a Context, which can potentially lead to
     * memory leaks if not managed carefully. However, in this class, it's
     * initialized in {@link #init(Context)} and used throughout the application's
     * lifecycle, so the risk is considered acceptable.
     */
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;
    /**
     * Flag indicating whether smiley loading is currently in progress.
     * True if loading, false otherwise.
     */
    public static boolean loading = false;
    /**
     * The maximum height of a smiley in pixels after scaling.
     * This value is updated when smileys are scaled.
     */
    public static int max_height = 1;
    /**
     * The maximum width of a smiley in pixels after scaling.
     * This value is updated when smileys are scaled.
     */
    public static int max_width = 1;
    /**
     * Indicates whether a smiley pack has been loaded.
     * True if a pack is loaded, false otherwise.
     */
    public static boolean packLoaded = false;
    /**
     * A vector of {@link Movie} objects representing the smileys used in the smiley selector.
     * Each {@link Movie} in this vector corresponds to a unique smiley image that can be chosen
     * by the user from a smiley selection interface. This vector is populated during the
     * smiley pack loading process.
     *
     * @see #selector_tags
     * @see #loadFromAssets()
     * @see #loadFromFile(File)
     */
    public static Vector<Movie> selector_smileys = new Vector<>();
    /**
     * A list of smiley tags used for display in a selector or picker UI.
     * Each tag in this list corresponds to a unique smiley image that can be chosen by the user.
     * This list typically contains the primary or most representative tag for each smiley.
     * The order of tags in this list matches the order of {@link Movie} objects in {@link #selector_smileys}.
     */
    public static Vector<String> selector_tags = new Vector<>();
    /**
     * A vector holding {@link Movie} objects representing all loaded smileys.
     * Each smiley in this list corresponds to a tag in the {@link #tags} vector
     * at the same index. This allows for mapping text tags to their visual representations.
     * The order of smileys and tags is significant.
     */
    public static Vector<Movie> smileys = new Vector<>();
    /**
     * A vector holding all smiley tags. Each tag corresponds to a smiley image.
     * These tags are used to identify and replace text with smileys.
     * The order of tags in this vector matches the order of {@link Movie} objects
     * in the {@link #smileys} vector.
     */
    public static Vector<String> tags = new Vector<>();

    /**
     * Forces a change in the scale of all loaded smileys.
     * <p>
     * This method retrieves the current smiley scale value from shared preferences.
     * If there are smileys loaded in the {@code selector_smileys} list, it iterates
     * through each smiley, applies the new scale, and updates the maximum width
     * and height observed among the scaled smileys.
     * </p>
     * <p>
     * The scale value is determined by the "ms_smileys_scale" preference, defaulting
     * to "3" if not set.
     * </p>
     * <p>
     * Note: This method directly modifies the {@code max_width} and {@code max_height}
     * static fields of this class.
     * </p>
     */
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

    /**
     * Generates a string of '#' characters of a specified length.
     * <p>
     * This method is used internally to create a "plomb" string, which seems
     * to be a placeholder string of '#' characters. The length of this string
     * is determined by the {@code length} parameter.
     * </p>
     * <p>
     * For example, if {@code length} is 5, this method will return "#####".
     * </p>
     *
     * @param length The desired length of the plomb string.
     * @return A string consisting of {@code length} '#' characters.
     */
    private static String getPlomb(int length) {
        StringBuilder plomb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            plomb.append("#");
        }

        return plomb.toString();
    }

    /**
     * Replaces smiley tags in a SpannableStringBuilder with corresponding smiley images.
     * <p>
     * This method is an overload of {@link #getSmiledText(SpannableStringBuilder, int, boolean, int)}
     * and calls it with a default {@code var3} value of 0.
     * </p>
     * <p>
     * It iterates through the input {@code text}, finds smiley tags, and replaces them
     * with {@link MySpan} objects containing the smiley images. The size of the emoticons
     * and whether dynamic emoticons are enabled are controlled by the {@code emoticonSize}
     * and {@code enableDynamicEmoticons} parameters, respectively.
     * </p>
     * <p>
     * The method assumes that smiley tags and their corresponding {@link Movie} objects
     * are already loaded and available in the static {@code tags} and {@code smileys}
     * vectors of this class.
     * </p>
     * <p>
     * If the input {@code text} is null, a new empty SpannableStringBuilder is created.
     * The method avoids replacing tags that are part of a URL or link.
     * </p>
     *
     * @param text The SpannableStringBuilder containing text with smiley tags.
     *             If null, an empty SpannableStringBuilder is used.
     * @param emoticonSize The desired size for the emoticons. This parameter is passed
     *                     to the {@link MySpan} constructor.
     * @param enableDynamicEmoticons A boolean flag indicating whether dynamic (animated)
     *                               emoticons should be enabled. This is also passed to
     *                               the {@link MySpan} constructor.
     * @return A SpannableStringBuilder with smiley tags replaced by smiley images.
     *
     * @see #getSmiledText(SpannableStringBuilder, int, boolean, int)
     * @see MySpan
     * @see Movie
     * @noinspection unused
     */
    public static SpannableStringBuilder getSmiledText(SpannableStringBuilder text, int emoticonSize, boolean enableDynamicEmoticons) {
        return getSmiledText(text, emoticonSize, enableDynamicEmoticons, 0);
    }

    /**
     * Replaces smiley tags in a SpannableStringBuilder with corresponding smiley images.
     * <p>
     * This method iterates through the provided {@code text} and searches for smiley tags
     * defined in the {@code tags} list. When a tag is found, it's replaced with a
     * {@link MySpan} containing the corresponding smiley image. The replacement process
     * starts from {@code tagStartIndex}.
     * </p>
     * <p>
     * If {@code animate} is true, animated smileys will be used if available.
     * The {@code iconSize} parameter can be used to specify a custom size for the smiley
     * icons; if less than or equal to 0, the default size is used.
     * </p>
     * <p>
     * This method avoids replacing tags that are part of a URL or link.
     * </p>
     *
     * @param text The SpannableStringBuilder to process. If null, an empty
     *             SpannableStringBuilder is created.
     * @param tagStartIndex The index from which to start searching for tags.
     * @param animate True to use animated smileys, false for static ones.
     * @param iconSize The desired size of the smiley icon. If less than or equal to 0,
     *                 the default size is used.
     * @return A SpannableStringBuilder with smiley tags replaced by smiley images.
     */
    public static SpannableStringBuilder getSmiledText(SpannableStringBuilder text, int tagStartIndex, boolean animate, int iconSize) {
        SpannableStringBuilder result = (text != null) ? text : new SpannableStringBuilder("");
        String textString = result.toString();
        int tagCount = tags.size();

        for (int i = 0; i < tagCount; i++) {
            String tag = tags.get(i);
            int tagLength = tag.length();
            int startIdx = tagStartIndex;
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

                    if (iconSize <= 0) {
                        span = new MySpan(smileys.get(i), animate);
                    } else {
                        span = new MySpan(smileys.get(i), animate, iconSize);
                    }

                    result.setSpan(span, startIdx, startIdx + tagLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                startIdx += tagLength - 1;
            }
        }

        return result;
    }

    /**
     * Converts a CharSequence containing smiley tags into a SpannableStringBuilder
     * with the tags replaced by smiley images.
     * <p>
     * This is an overloaded version of {@link #getSmiledText(SpannableStringBuilder, int, boolean, int)}.
     * It creates a new {@link SpannableStringBuilder} from the input CharSequence and
     * then calls the main {@code getSmiledText} method.
     * </p>
     *
     * @param text The input CharSequence to process.
     * @param defaultTextSize The default text size to use for the smileys. This might be used
     *                        to scale the smileys appropriately within the text.
     * @param enableDynamicEmoticons A boolean flag indicating whether dynamic (animated)
     *                               emoticons should be enabled. If true, animated smileys
     *                               will be used; otherwise, static images might be used.
     * @return A SpannableStringBuilder with smiley tags replaced by smiley images.
     *         Returns an empty SpannableStringBuilder if the input text is null.
     * @see #getSmiledText(SpannableStringBuilder, int, boolean, int)
     * @noinspection unused
     */
    public static SpannableStringBuilder getSmiledText(CharSequence text, int defaultTextSize, boolean enableDynamicEmoticons) {
        return getSmiledText(new SpannableStringBuilder(text), defaultTextSize, enableDynamicEmoticons, 0);
    }

    /**
     * Retrieves the smiley tag from the beginning of the given text.
     * <p>
     * This method iterates through the list of known smiley tags ({@link #tags})
     * and checks if the input {@code text} starts with any of these tags.
     * If a match is found, that tag is returned.
     * </p>
     * <p>
     * If no smiley tag is found at the beginning of the text, an empty string
     * is returned.
     * </p>
     * <p>
     * The {@code @noinspection unused} annotation is present because this method
     * might appear unused by static analysis tools if it's primarily called
     * from dynamically generated code or reflection, though it is a public API.
     * </p>
     *
     * @param text The string to check for a leading smiley tag.
     * @return The smiley tag if found at the beginning of the text, otherwise an empty string.
     * @noinspection unused
     */
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

    /**
     * Initializes the SmileysManager with the application context.
     * <p>
     * This method sets the static context field {@code ctx} and ensures that
     * the directory for storing smileys on the SD card exists. If the directory
     * {@code /Jasmine/Smileys} under the path specified by {@code resources.SD_PATH}
     * does not exist or is not a directory, it attempts to create it.
     * </p>
     * <p>
     * This method should be called once, typically during application startup,
     * before any other methods of this class are used.
     * </p>
     *
     * @param context The application context.
     */
    public static void init(Context context) {
        ctx = context;
        File smileysDir = new File(resources.SD_PATH + "/Jasmine/Smileys");

        if (!smileysDir.isDirectory() || !smileysDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            smileysDir.mkdirs();
        }
    }

    /**
     * Loads smiley definitions and images from the application's assets.
     * <p>
     * This method reads a "define.ini" file from the assets, which contains
     * mappings of smiley tags to their corresponding image files. For each line
     * in "define.ini", it expects a comma-separated list of tags. The first tag
     * in the list is considered the primary tag and is added to {@link #selector_tags}.
     * All tags from the line are added to the general {@link #tags} list.
     * </p>
     * <p>
     * Smiley image files are assumed to be named sequentially (e.g., "1", "2", "3", ...).
     * The method loads each image file as a {@link Movie} object. The same {@link Movie}
     * object is associated with all tags defined on the same line in "define.ini".
     * These {@link Movie} objects are added to both {@link #selector_smileys} (for the
     * primary tag) and {@link #smileys}.
     * </p>
     * <p>
     * Before loading, all existing smiley data (tags and movies) is cleared.
     * The {@link #loading} flag is set to true during the loading process and false
     * upon completion or if an error occurs.
     * </p>
     * <p>
     * After successfully loading all smileys, {@link #forceChangeScale()} is called to
     * apply the current scale settings, and {@link #packLoaded} is set to true.
     * </p>
     * <p>
     * If any exception occurs during loading (e.g., file not found, parsing error),
     * all smiley data is cleared, {@link #packLoaded} is set to false, and an error
     * message is logged.
     * </p>
     *
     * @see #loadFromFile(File)
     * @see #forceChangeScale()
     * @see Movie
     */
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

    /**
     * Loads smiley definitions and images from a specified directory.
     * <p>
     * This method reads a "define.ini" file located within the given directory.
     * The "define.ini" file is expected to contain comma-separated tags for smileys.
     * Each line in "define.ini" corresponds to a set of tags for a single smiley image.
     * The smiley image files are expected to be named sequentially (1, 2, 3, etc.)
     * in the same directory.
     * </p>
     * <p>
     * The method first clears any existing smiley data (tags and movie objects).
     * It then iterates through each line of "define.ini":
     * <ul>
     *     <li>For each line, it splits the line into individual tags.</li>
     *     <li>The first tag encountered for a new smiley image is added to {@link #selector_tags},
     *         and the corresponding {@link Movie} object (loaded from the sequentially named file)
     *         is added to {@link #selector_smileys}.</li>
     *     <li>All tags from the line are added to {@link #tags}, and the same {@link Movie} object
     *         is added to {@link #smileys} for each of these tags.</li>
     * </ul>
     * After processing all lines, it calls {@link #forceChangeScale()} to apply the
     * current scale settings and sets {@link #packLoaded} to true.
     * </p>
     * <p>
     * If "define.ini" does not exist, the method returns without loading any smileys.
     * If any exception occurs during loading (e.g., file not found, I/O error),
     * all smiley data is cleared, {@link #packLoaded} is set to false, and an error
     * is logged.
     * </p>
     *
     * @param directory The directory containing the "define.ini" file and smiley image files.
     */
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

    /**
     * Loads the currently selected smiley pack.
     * <p>
     * This method determines which smiley pack to load based on the "current_smileys_pack"
     * preference. If the preference is set to "$*INTERNAL*$", it loads the default
     * smiley pack from the application's assets using {@link #loadFromAssets()}.
     * Otherwise, it constructs the path to the custom smiley pack directory on the
     * external storage (SD card) and loads the smileys from there using
     * {@link #loadFromFile(File)}.
     * </p>
     * <p>
     * The loading process is performed on a new background thread with maximum priority
     * to avoid blocking the UI. Any {@link OutOfMemoryError} encountered during loading
     * is caught and logged.
     * </p>
     * <p>
     * The path for external smiley packs is constructed as:
     * {@code [resources.SD_PATH]/Jasmine/Smileys/[smilePackPath]}
     * </p>
     *
     * @see #loadFromAssets()
     * @see #loadFromFile(File)
     * @see PreferenceManager
     * @see resources#SD_PATH
     */
    public static void loadPack() {
        //noinspection deprecation
        final String smilePackPath = PreferenceManager.getDefaultSharedPreferences(ctx).getString("current_smileys_pack", "$*INTERNAL*$");

        Runnable loadRunnable = new Runnable() {
            @Override
            public void run() {
                try {
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

    /**
     * Preloads the smiley pack if it hasn't been loaded already.
     * <p>
     * This method checks the {@code packLoaded} flag. If the flag is false,
     * indicating that no smiley pack is currently loaded, it calls the
     * {@link #loadPack()} method to initiate the loading process.
     * </p>
     * <p>
     * This can be used to ensure that smileys are available when needed,
     * potentially improving performance by loading them in advance.
     * </p>
     *
     * @see #packLoaded
     * @see #loadPack()
     */
    public static void preloadPack() {
        if (!packLoaded) {
            loadPack();
        }
    }
}