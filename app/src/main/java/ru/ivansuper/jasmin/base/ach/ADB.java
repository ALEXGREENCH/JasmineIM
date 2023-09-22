package ru.ivansuper.jasmin.base.ach;

import android.graphics.drawable.Drawable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.ISDialog;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.locale.Locale;

public class ADB {
    private static int bad_words_count = 0;
    private static int devils_count = 0;
    private static long last_symbol_timestamp = 0L;
    private static final Vector<Item> list = new Vector();
    private static OnlineCounter online_counter;
    public static int scrolled_pixels = 0;
    private static int typed_symbols = 0;
    public static int viewed_infos = 0;

    public ADB() {
    }

    public static final void checkScroll() {
        if (scrolled_pixels > 10000) {
            setActivated(0);
        }

        scrolled_pixels = 0;
    }

    public static final void checkUserInfos() {
        synchronized(ADB.class){}

        label78: {
            Throwable var10000;
            label82: {
                int var0;
                boolean var10001;
                try {
                    var0 = viewed_infos;
                } catch (Throwable var7) {
                    var10000 = var7;
                    var10001 = false;
                    break label82;
                }

                if (var0 == -1) {
                    break label78;
                }

                label73:
                try {
                    ++viewed_infos;
                    if (viewed_infos > 20) {
                        viewed_infos = -1;
                        setActivated(7);
                    }
                    break label78;
                } catch (Throwable var6) {
                    var10000 = var6;
                    var10001 = false;
                    break label73;
                }
            }

            Throwable var1 = var10000;
            try {
                throw var1;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static final void countBadWords(String var0) {
        int var1 = 0;
        String var2 = var0.toLowerCase();
        String[] var3 = new String[]{"бляд", "сука", "суки", "пиздец", "хуй", "пизда", "ебаный", "ебать", "хуило"};

        for(int var4 = var3.length; var1 < var4; ++var1) {
            var0 = var3[var1];
            int var5 = 0;

            while(true) {
                var5 = var2.indexOf(var0, var5);
                if (var5 == -1) {
                    break;
                }

                ++bad_words_count;
                ++var5;
            }
        }

        if (bad_words_count >= 30) {
            setActivated(2);
        }

    }

    private static final void countDevils(String var0) {
        devils_count = 0;
        int var1 = 0;

        while(true) {
            var1 = var0.indexOf("]:->", var1);
            if (var1 == -1) {
                if (devils_count >= 30) {
                    setActivated(4);
                }

                devils_count = 0;
                return;
            }

            ++devils_count;
            ++var1;
        }
    }

    private static final void fill() {
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

    public static final int getActivatedCount() {
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

    public static final Vector<Item> getAll() {
        synchronized (ADB.class) {
            return new Vector<>(list);
        }
    }


    public static final void init() {
        fill();
        File achsFile = new File(resources.dataPath + "achs.adb");
        if (!achsFile.exists()) {
            try {
                achsFile.createNewFile();
                save(achsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                load(achsFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final void load(File achsFile) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(achsFile));

        while (dataInputStream.available() > 0) {
            setActivationState(dataInputStream.readInt(), dataInputStream.readBoolean());
        }

        dataInputStream.close();
    }

    public static final void proceedMessage(String message) {
        countDevils(message);
        countBadWords(message);
    }

    public static final void save() {
        try {
            File achsFile = new File(resources.dataPath + "achs.adb");
            save(achsFile);
        } catch (Exception e) {
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
                    Thread.sleep(1000L);
                    if ((System.currentTimeMillis() - this.stamp) / 1000L >= 259200L) {
                        ADB.setActivated(5);
                        this.active = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
