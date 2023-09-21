package ru.ivansuper.jasmin.base.ach;

import android.graphics.drawable.Drawable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        synchronized(ADB.class){}
        int var0 = 0;

        Throwable var10000;
        label143: {
            Iterator var1;
            boolean var10001;
            try {
                var1 = list.iterator();
            } catch (Throwable var15) {
                var10000 = var15;
                var10001 = false;
                break label143;
            }

            while(true) {
                boolean var2;
                try {
                    var2 = var1.hasNext();
                } catch (Throwable var14) {
                    var10000 = var14;
                    var10001 = false;
                    break;
                }

                if (!var2) {
                    return var0;
                }

                try {
                    var2 = ((Item)var1.next()).activated;
                } catch (Throwable var13) {
                    var10000 = var13;
                    var10001 = false;
                    break;
                }

                byte var3;
                if (var2) {
                    var3 = 1;
                } else {
                    var3 = 0;
                }

                var0 += var3;
            }
        }

        Throwable var16 = var10000;
        try {
            throw var16;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static final Vector<Item> getAll() {
        synchronized(ADB.class){}

        Vector var0;
        try {
            var0 = (Vector)list.clone();
        } finally {
            ;
        }

        return var0;
    }

    public static final void init() {
        fill();
        File var0 = new File(resources.dataPath + "achs.adb");
        if (!var0.exists()) {
            try {
                var0.createNewFile();
                save(var0);
            } catch (Exception var2) {
            }
        } else {
            try {
                load(var0);
            } catch (Exception var1) {
            }
        }

    }

    private static final void load(File var0) throws Exception {
        DataInputStream var1 = new DataInputStream(new FileInputStream(var0));

        while(var1.available() > 0) {
            setActivationState(var1.readInt(), var1.readBoolean());
        }

    }

    public static final void proceedMessage(String var0) {
        countDevils(var0);
        countBadWords(var0);
    }

    public static final void save() {
        try {
            StringBuilder var1 = new StringBuilder(String.valueOf(resources.dataPath));
            File var0 = new File(var1.append("achs.adb").toString());
            save(var0);
        } catch (Exception var2) {
        }

    }

    private static final void save(File var0) throws Exception {
        DataOutputStream var1 = new DataOutputStream(new FileOutputStream(var0));
        Iterator var2 = list.iterator();

        while(var2.hasNext()) {
            Item var3 = (Item)var2.next();
            var1.writeInt(var3.id);
            var1.writeBoolean(var3.activated);
        }

    }

    public static final void setActivated(int var0) {
        synchronized(ADB.class){}

        label133: {
            Throwable var10000;
            label132: {
                Iterator var1;
                boolean var10001;
                try {
                    var1 = list.iterator();
                } catch (Throwable var15) {
                    var10000 = var15;
                    var10001 = false;
                    break label132;
                }

                while(true) {
                    boolean var2;
                    try {
                        var2 = var1.hasNext();
                    } catch (Throwable var14) {
                        var10000 = var14;
                        var10001 = false;
                        break;
                    }

                    if (!var2) {
                        break label133;
                    }

                    try {
                        Item var3 = (Item)var1.next();
                        if (var3.id == var0 && !var3.activated) {
                            ISDialog.showAch(var3.icon, var3.desc);
                            var3.activated = true;
                            save();
                            break label133;
                        }
                    } catch (Throwable var13) {
                        var10000 = var13;
                        var10001 = false;
                        break;
                    }
                }
            }

            Throwable var16 = var10000;
            try {
                throw var16;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static final void setActivationState(int var0, boolean var1) {
        synchronized(ADB.class){}

        label125: {
            Throwable var10000;
            label124: {
                boolean var10001;
                Iterator var2;
                try {
                    var2 = list.iterator();
                } catch (Throwable var16) {
                    var10000 = var16;
                    var10001 = false;
                    break label124;
                }

                while(true) {
                    boolean var3;
                    try {
                        var3 = var2.hasNext();
                    } catch (Throwable var15) {
                        var10000 = var15;
                        var10001 = false;
                        break;
                    }

                    if (!var3) {
                        break label125;
                    }

                    try {
                        Item var4 = (Item)var2.next();
                        if (var4.id == var0) {
                            var4.activated = var1;
                            break label125;
                        }
                    } catch (Throwable var14) {
                        var10000 = var14;
                        var10001 = false;
                        break;
                    }
                }
            }

            Throwable var17 = var10000;
            try {
                throw var17;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static final void startOnlineCounter() {
        synchronized(ADB.class){}

        try {
            if (online_counter != null) {
                online_counter.active = false;
            }

            online_counter = new OnlineCounter();
            online_counter.setPriority(1);
            online_counter.start();
        } finally {
            ;
        }

    }

    public static final void stopOnlineCounter() {
        synchronized(ADB.class){}

        try {
            if (online_counter != null) {
                online_counter.active = false;
            }
        } finally {
            ;
        }

    }

    public static final void symbolTyped() {
        long var0 = System.currentTimeMillis();
        if (var0 - last_symbol_timestamp > 200L) {
            typed_symbols = 1;
        } else {
            ++typed_symbols;
            if (typed_symbols >= 50) {
                setActivated(3);
            }
        }

        last_symbol_timestamp = var0;
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
        public boolean active;
        private long stamp;

        private OnlineCounter() {
            this.active = true;
            this.stamp = System.currentTimeMillis();
        }

        public void run() {
            while(this.active) {
                try {
                    sleep(1000L);
                    if ((System.currentTimeMillis() - this.stamp) / 1000L >= 259200L) {
                        ADB.setActivated(5);
                        this.active = false;
                    }
                } catch (InterruptedException var2) {
                    var2.printStackTrace();
                }
            }

        }
    }
}
