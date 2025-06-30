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

public class ADB {
    private static int bad_words_count = 0;
    /** @noinspection FieldCanBeLocal*/
    private static int devils_count = 0;
    private static long last_symbol_timestamp = 0L;
    private static final Vector<Item> list = new Vector<>();
    private static OnlineCounter online_counter;
    public static int scrolled_pixels = 0;
    private static int typed_symbols = 0;
    public static int viewed_infos = 0;

    public ADB() {
    }

    /** @noinspection unused*/
    public static void checkScroll() {
        if (scrolled_pixels > 10000) {
            setActivated(0);
        }

        scrolled_pixels = 0;
    }

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

    private static void fill() {
        Item var0 = new Item(0, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_0"), "Проскроллить список контактов на 10000 пикселей не отпуская пальца");
        list.add(var0);
        var0 = new Item(1, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_1"), "Открыть 25 чатов за 1 сеанс работы");
        list.add(var0);
        var0 = new Item(2, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_2"), "Употребить не менее 30 стандартных матных слов за 1 сеанс работы");
        list.add(var0);
        var0 = new Item(3, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_3"), "Напечатать 50 символов с интервалом не более 0.2 секунды");
        list.add(var0);
        var0 = new Item(4, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_4"), "Употребить не менее 30 чертей в 1 сообщении");
        list.add(var0);
        var0 = new Item(5, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_5"), "Пробыть онлайн на протяжении 3 суток");
        list.add(var0);
        var0 = new Item(6, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_6"), "Найти в списке контактов 1 контакт так, чтобы других на экране не было");
        list.add(var0);
        var0 = new Item(7, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_7"), "Просмотреть не менее 20 анкет за 1 сеанс работы");
        list.add(var0);
        var0 = new Item(8, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_8"), "Купить приложение");
        list.add(var0);
        var0 = new Item(9, resources.ctx.getResources().getDrawable(R.drawable.ach_0), Locale.getString("s_ach_9"), "Пробыть в невидимости не менее 6 часов");
        list.add(var0);
    }

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

    public static Vector<Item> getAll() {
        synchronized (ADB.class) {
            return new Vector<>(list);
        }
    }


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

    private static void load(File achsFile) throws IOException {
        //noinspection IOStreamConstructor
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(achsFile));

        while (dataInputStream.available() > 0) {
            setActivationState(dataInputStream.readInt(), dataInputStream.readBoolean());
        }

        dataInputStream.close();
    }

    public static void proceedMessage(String message) {
        countDevils(message);
        countBadWords(message);
    }

    public static void save() {
        try {
            File achsFile = new File(resources.dataPath + "achs.adb");
            save(achsFile);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public static void save(File file) throws IOException {
        synchronized (list) {
            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file))) {
                for (Item item : list) {
                    outputStream.writeInt(item.id);
                    outputStream.writeBoolean(item.activated);
                }
            }
        }
    }

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

    public static void stopOnlineCounter() {
        synchronized (ADB.class) {
            if (online_counter != null) {
                online_counter.active = false;
            }
        }
    }

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

    public static class Item {
        public boolean activated;
        public String desc;
        public Drawable icon;
        public int id;
        public String rule;

        public Item(int var1, Drawable var2, String var3, String var4) {
            this.id = var1;
            this.icon = var2;
            this.desc = var3;
            this.rule = var4;
        }
    }

    private static class OnlineCounter extends Thread {
        private volatile boolean active;
        private long stamp;

        private OnlineCounter() {
            this.active = true;
            this.stamp = System.currentTimeMillis();
        }

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
