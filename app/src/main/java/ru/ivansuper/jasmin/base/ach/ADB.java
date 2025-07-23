package ru.ivansuper.jasmin.base.ach;

import android.graphics.drawable.Drawable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import ru.ivansuper.jasmin.ISDialog;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.locale.Locale;

/**
 * The ADB class manages achievements in the application.
 * It tracks various user actions and awards achievements when certain criteria are met.
 * Achievements are stored in a file and loaded on initialization.
 * <p>
 * The class uses several counters to track user actions:
 * - {@code bad_words_count}: Counts the number of bad words used by the user.
 * - {@code devils_count}: Counts the number of "devil" emoticons used by the user.
 * - {@code last_symbol_timestamp}: Stores the timestamp of the last typed symbol.
 * - {@code scrolled_pixels}: Counts the number of pixels scrolled by the user.
 * - {@code typed_symbols}: Counts the number of symbols typed by the user within a certain time interval.
 * - {@code viewed_infos}: Counts the number of user profiles viewed by the user.
 * <p>
 * The class also uses an {@link OnlineCounter} to track the user's online time.
 * <p>
 * Achievements are represented by {@link Item} objects, which store the achievement's ID, icon, description, and rule.
 * The list of achievements is stored in the {@code list} vector.
 * <p>
 * The class provides methods to:
 * - Initialize the achievements system ({@link #init()}).
 * - Load and save achievements from/to a file ({@link #load(File)} and {@link #save(File)}).
 * - Check for achievement completion based on user actions ({@link #checkScroll()}, {@link #checkUserInfos()}, {@link #proceedMessage(String)}, {@link #symbolTyped()}).
 * - Activate an achievement ({@link #setActivated(int)}).
 * - Get the list of all achievements ({@link #getAll()}).
 * - Get the number of activated achievements ({@link #getActivatedCount()}).
 * - Start and stop the online time counter ({@link #startOnlineCounter()} and {@link #stopOnlineCounter()}).
 */
public class ADB {
    /**
     * Counter for the number of bad words used by the user.
     * This counter is incremented each time a bad word is detected in a user's message.
     * An achievement is awarded when this counter reaches a certain threshold (30).
     */
    private static int bad_words_count = 0;
    /**
     * Counts the number of "devil" emoticons used by the user.
     * This counter is used to track achievement progress related to emoticon usage.
     * @noinspection FieldCanBeLocal
     */
    private static int devils_count = 0;
    /**
     * Stores the timestamp of the last typed symbol.
     * This is used to determine if the user is typing quickly enough to earn the "fast typer" achievement.
     */
    private static long last_symbol_timestamp = 0L;
    /**
     * A vector containing all the achievements available in the application.
     * Each element is an {@link Item} object, representing a single achievement.
     */
    private static final Vector<Item> list = new Vector<>();
    /**
     * Counter for tracking the user's online time.
     * This is used to award achievements based on the duration of online presence.
     */
    private static OnlineCounter online_counter;
    /**
     * Stores the total number of pixels scrolled by the user.
     * This value is used to track the "Scroll Master" achievement,
     * which is awarded when the user scrolls a certain amount.
     * The value is reset to 0 after the achievement is checked.
     *
     * @see #checkScroll()
     */
    public static int scrolled_pixels = 0;
    /**
     * Counts the number of symbols typed by the user within a 0.2-second interval.
     * This counter is used to track rapid typing for achievements.
     */
    private static int typed_symbols = 0;
    /**
     * Counts the number of user profiles viewed by the user.
     * This counter is used to track the "viewed_infos" achievement.
     * The achievement is awarded when the user views more than 20 profiles in a single session.
     * When the achievement is awarded, this counter is set to -1 to prevent further counting in the current session.
     */
    public static int viewed_infos = 0;

    /**
     * Constructs a new ADB object.
     * This constructor is currently empty as the class uses static methods and fields
     * for managing achievements.
     */
    public ADB() {
    }

    /**
     * Checks if the user has scrolled enough to unlock an achievement.
     * If the user has scrolled more than 10000 pixels, the "scrolling" achievement (ID 0) is activated.
     * After checking, the scrolled pixels counter is reset to 0.
     *
     * @noinspection unused
     */
    public static void checkScroll() {
        if (scrolled_pixels > 10000) {
            setActivated(0);
        }

        scrolled_pixels = 0;
    }

    /**
     * Checks if the user has viewed enough user profiles to unlock an achievement.
     * If the user has viewed more than 20 profiles, the "Viewed 20 profiles" achievement is unlocked.
     * This method is synchronized to prevent race conditions.
     */
    public static void checkUserInfos() {
        synchronized (ADB.class) {
            if (viewed_infos != -1) {
                viewed_infos++;
                if (viewed_infos > 20) {
                    viewed_infos = -1;
                    setActivated(7);
                }
            }
        }
    }

    /**
     * Counts the occurrences of bad words in the given text and activates an achievement if the count reaches 30.
     * The method converts the input text to lowercase and then iterates through a predefined list of bad words.
     * For each bad word, it searches for its occurrences in the text and increments the {@code bad_words_count}.
     * If the {@code bad_words_count} reaches or exceeds 30, the achievement with ID 2 is activated.
     *
     * @param text The text to scan for bad words.
     */
    private static void countBadWords(String text) {
        String lowerText = text.toLowerCase();
        String[] badWords = {
                "бляд", "сука", "суки", "пиздец", "хуй",
                "пизда", "ебаный", "ебать", "хуило"
        };

        for (String word : badWords) {
            int index = 0;
            while ((index = lowerText.indexOf(word, index)) != -1) {
                bad_words_count++;
                index++;
            }
        }

        if (bad_words_count >= 30) {
            setActivated(2);
        }
    }

    /**
     * Counts the occurrences of the "devil" emoticon ("]_->") in the given text.
     * If the count reaches 30 or more, it activates achievement with ID 4.
     * The devil count is reset to 0 after checking.
     *
     * @param text The text to search for "devil" emoticons.
     */
    private static void countDevils(String text) {
        devils_count = 0;
        int index = 0;

        while ((index = text.indexOf("]:->", index)) != -1) {
            devils_count++;
            index++;
        }

        if (devils_count >= 30) {
            setActivated(4);
        }

        devils_count = 0;
    }

    /**
     * Fills the list of achievements with predefined data.
     * Each achievement is created with a unique ID, icon, description, and rule.
     */
    private static void fill() {
        Item achievement = new Item(0, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_0"), "Проскроллить список контактов на 10000 пикселей не отпуская пальца");
        list.add(achievement);
        achievement = new Item(1, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_1"), "Открыть 25 чатов за 1 сеанс работы");
        list.add(achievement);
        achievement = new Item(2, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_2"), "Употребить не менее 30 стандартных матных слов за 1 сеанс работы");
        list.add(achievement);
        achievement = new Item(3, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_3"), "Напечатать 50 символов с интервалом не более 0.2 секунды");
        list.add(achievement);
        achievement = new Item(4, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_4"), "Употребить не менее 30 чертей в 1 сообщении");
        list.add(achievement);
        achievement = new Item(5, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_5"), "Пробыть онлайн на протяжении 3 суток");
        list.add(achievement);
        achievement = new Item(6, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_6"), "Найти в списке контактов 1 контакт так, чтобы других на экране не было");
        list.add(achievement);
        achievement = new Item(7, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_7"), "Просмотреть не менее 20 анкет за 1 сеанс работы");
        list.add(achievement);
        achievement = new Item(8, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_8"), "Купить приложение");
        list.add(achievement);
        achievement = new Item(9, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_9"), "Пробыть в невидимости не менее 6 часов");
        list.add(achievement);
    }

    /**
     * Returns the number of activated achievements.
     *
     * @return The number of activated achievements.
     */
    public static int getActivatedCount() {
        synchronized (ADB.class) {
            int count = 0;

            for (Item item : list) {
                if (item.activated) {
                    count++;
                }
            }

            return count;
        }
    }

    /**
     * Returns a copy of the list of all achievements.
     * The method is synchronized to prevent concurrent modification issues.
     *
     * @return A new {@code Vector} containing all {@link Item} objects.
     */
    public static Vector<Item> getAll() {
        synchronized (ADB.class) {
            return new Vector<>(list);
        }
    }


    /**
     * Initializes the achievements system.
     * This method fills the list of achievements and loads their states from a file.
     * If the file doesn't exist, it creates a new one and saves the initial states.
     */
    public static void init() {
        fill();
        File achsFile = new File(resources.dataPath + "achs.adb");
        if (!achsFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                achsFile.createNewFile();
                save(achsFile);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        } else {
            try {
                load(achsFile);
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads achievement data from the specified file.
     * The method reads achievement IDs and their activation states from the file
     * and updates the corresponding achievements in the {@code list}.
     *
     * @param achsFile The file to load achievement data from.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    private static void load(File achsFile) throws IOException {
        //noinspection IOStreamConstructor
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(achsFile));

        while (dataInputStream.available() > 0) {
            setActivationState(dataInputStream.readInt(), dataInputStream.readBoolean());
        }

        dataInputStream.close();
    }

    /**
     * Processes a message to check for achievements related to its content.
     * Specifically, it counts the occurrences of "devil" emoticons and bad words.
     *
     * @param message The message to be processed.
     */
    public static void proceedMessage(String message) {
        countDevils(message);
        countBadWords(message);
    }

    /**
     * Saves the current state of achievements to the default achievements file.
     * The default file is named "achs.adb" and is located in the application's data path.
     * If any exception occurs during the save operation, it is caught and its stack trace is printed.
     */
    public static void save() {
        try {
            File achsFile = new File(resources.dataPath + "achs.adb");
            save(achsFile);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * Saves the current state of all achievements to the specified file.
     * Each achievement's ID and activation status is written to the file.
     * This method is synchronized on the {@code list} to prevent concurrent modification issues.
     *
     * @param file The file to save the achievement data to.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public static void save(File file) throws IOException {
        synchronized (list) {
            DataOutputStream outputStream = null;
            //noinspection TryFinallyCanBeTryWithResources
            try {
                //noinspection IOStreamConstructor
                outputStream = new DataOutputStream(new FileOutputStream(file));
                for (Item item : list) {
                    outputStream.writeInt(item.id);
                    outputStream.writeBoolean(item.activated);
                }
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ignore) {

                    }
                }
            }
        }
    }


    /**
     * Activates the achievement with the given ID.
     * If the achievement is found and not already activated, it is marked as activated,
     * a notification is shown to the user, and the achievements state is saved.
     * The method is synchronized to prevent race conditions when modifying the achievement list.
     *
     * @param id The ID of the achievement to activate.
     */
    public static void setActivated(int id) {
        synchronized (list) {
            for (Item item : list) {
                if (item.id == id && !item.activated) {
                    ISDialog.showAch(item.icon, item.desc);
                    item.activated = true;
                    save();
                    return;
                }
            }
        }
    }


    /**
     * Sets the activation state of an achievement.
     * This method is synchronized to ensure thread safety when modifying the achievement list.
     *
     * @param id The ID of the achievement to modify.
     * @param activated The new activation state (true for activated, false for not activated).
     */
    private static void setActivationState(int id, boolean activated) {
        synchronized (list) {
            for (Item item : list) {
                if (item.id == id) {
                    item.activated = activated;
                    return;
                }
            }
        }
    }

    /**
     * Starts the online time counter.
     * If an online counter is already running, it is stopped and a new one is started.
     * The new counter is given a priority of 1.
     * This method is synchronized to ensure thread safety.
     */
    public static void startOnlineCounter() {
        synchronized (ADB.class) {
            if (online_counter != null) {
                online_counter.active = false;
            }

            online_counter = new OnlineCounter();
            online_counter.setPriority(1);
            online_counter.start();
        }
    }

    /**
     * Stops the online time counter.
     * This method sets the {@code active} flag of the {@link OnlineCounter} instance to {@code false},
     * effectively stopping its execution.
     * The method is synchronized on the {@code ADB.class} to prevent race conditions
     * when accessing the {@code online_counter} field.
     */
    public static void stopOnlineCounter() {
        synchronized (ADB.class) {
            if (online_counter != null) {
                online_counter.active = false;
            }
        }
    }

    /**
     * Tracks the number of symbols typed by the user and activates an achievement if a certain threshold is reached.
     * If the time elapsed since the last symbol typed is greater than 0.2 seconds, the symbol counter is reset to 1.
     * Otherwise, the symbol counter is incremented.
     * If the symbol counter reaches 50, the "fast typer" achievement (ID 3) is activated.
     * The timestamp of the last typed symbol is updated to the current time.
     */
    public static void symbolTyped() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - last_symbol_timestamp > 200L) {
            typed_symbols = 1;
        } else {
            typed_symbols++;
            if (typed_symbols >= 50) {
                setActivated(3);
            }
        }

        last_symbol_timestamp = currentTime;
    }

    /**
     * Represents an achievement item.
     * Each item has an ID, icon, description, rule, and activation status.
     */
    public static class Item {
        /**
         * Indicates whether the achievement has been activated by the user.
         * True if activated, false otherwise.
         */
        public boolean activated;
        /**
         * The description of the achievement.
         * This string provides a user-friendly explanation of what the achievement represents.
         * It is typically displayed to the user when the achievement is unlocked or viewed.
         */
        public String desc;
        /**
         * The drawable icon representing the achievement.
         * This icon is displayed in the UI when the achievement is shown.
         */
        public Drawable icon;
        /**
         * The unique identifier for this achievement item.
         */
        public int id;
        public String rule;

        /**
         * Constructs a new Item object.
         *
         * @param itemId The unique identifier for the achievement item.
         * @param itemIcon The drawable resource representing the icon of the achievement.
         * @param itemDesc The description of the achievement.
         * @param itemRule The rule or condition to unlock the achievement.
         */
        public Item(int itemId, Drawable itemIcon, String itemDesc, String itemRule) {
            this.id = itemId;
            this.icon = itemIcon;
            this.desc = itemDesc;
            this.rule = itemRule;
        }
    }

    /**
     * A thread that tracks the user's online time and grants an achievement
     * if the user stays online for a certain duration (3 days).
     * <p>
     * The counter starts when the user goes online and stops when the user goes offline
     * or when the achievement is granted.
     * <p>
     * The online time is checked every second. If the online time reaches 3 days (259200 seconds),
     * the "Online for 3 days" achievement (ID 5) is granted, and the counter stops.
     */
    private static class OnlineCounter extends Thread {
        /**
         * Indicates whether the online counter is currently active.
         * This field is volatile to ensure visibility of changes across threads.
         * When set to {@code false}, the counter thread will terminate.
         */
        private volatile boolean active;
        /**
         * The timestamp when the online counter was started.
         * This is used to calculate the total online time for achievements.
         */
        private final long stamp;

        /**
         * Constructs a new OnlineCounter.
         * Initializes the counter as active and records the current time as the starting point.
         */
        private OnlineCounter() {
            this.active = true;
            this.stamp = System.currentTimeMillis();
        }

        /**
         * Continuously checks if the user has been online for 3 days (259200 seconds).
         * If the condition is met, it activates achievement with ID 5 and stops the thread.
         * The thread sleeps for 1 second between checks.
         * If the thread is interrupted, it prints the stack trace.
         */
        @Override
        public void run() {
            while (this.active) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(1000L);
                    if ((System.currentTimeMillis() - this.stamp) / 1000L >= 259200L) {
                        ADB.setActivated(5);
                        this.active = false;
                    }
                } catch (InterruptedException e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        }
    }
}
