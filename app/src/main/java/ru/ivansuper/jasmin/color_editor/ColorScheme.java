package ru.ivansuper.jasmin.color_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import ru.ivansuper.jasmin.resources;

/**
 * Manages the color scheme for the application.
 * This class provides methods to load, save, and access color values used throughout the UI.
 * It supports loading color schemes from internal storage, external storage, and application assets.
 * Color schemes are stored as a list of integer color values and corresponding names.
 */
public class ColorScheme {
    /** @noinspection unused*/
    private static final int COLORS_NUM = 53;
    /** @noinspection unused*/
    public static volatile Bitmap solid;
    public static ArrayList<Integer> colors = new ArrayList<>();
    public static ArrayList<String> names = new ArrayList<>();
    public static int group_item_back = 858827737;
    public static int group_item_text = -1;
    public static int profile_group_item_back = 1142257306;
    public static int profile_group_item_text = -8727564;
    public static int contact_item_text_norm = -1;
    public static int contact_item_text_chat = -5570646;
    public static int contact_item_text_typing = -103;
    public static int contact_item_text_offline = -21846;
    public static int contact_item_text_notadded = -1;
    public static int contactlist_bottompanel_back = 1996959293;
    public static int select_status_header = 573615065;
    public static int select_status_xback = 573615065;
    public static int chat_header = 570425344;
    public static int chat_header_nick = -1;
    public static int chat_header_typing = -16777216;
    public static int chat_bottompanel = 570425344;
    public static int chat_date = -4136193;
    public static int chat_inc_back = 573615065;
    public static int chat_inc_text = -1;
    public static int chat_inc_nick = -1;
    public static int chat_inc_bar = -4467743;
    public static int chat_out_text_not_confirmed = -1;
    public static int chat_out_bar_not_confirmed = -5592406;
    public static int chat_out_text_confirmed = -3414017;
    public static int chat_out_nick = -8076545;
    public static int chat_out_bar_confirmed = -16736021;
    public static int chat_out_back = 0;
    public static int chat_xtraz_text = -1;
    public static int chat_xtraz_back = 570490777;
    public static int chat_xtraz_bar = -4474112;
    public static int chat_auth_req_text = -1;
    public static int chat_auth_req_back = 872388864;
    public static int chat_auth_req_bar = -26368;
    public static int chat_auth_grand_text = -5570646;
    public static int chat_auth_grand_back = 855703296;
    public static int chat_auth_grand_bar = -16711936;
    public static int chat_auth_denied_text = -21846;
    public static int chat_auth_denied_back = 857866240;
    public static int chat_auth_denied_bar = -65536;
    public static int contactlist_overscroll = -16724737;
    public static int conference_me_nick = -6696705;
    public static int conference_message_highlight = 863471615;
    public static int conference_message_highlight_text = -6692609;
    public static int menu_dividers = -15103065;
    public static int menu_headers = -15103065;
    public static int cl_undernick_text = -855638017;
    public static int text_input_color = -16170673;
    public static int list_selector_color = -15103065;
    public static int contacts_chat_separator = -16746582;
    public static int contacts_screen_header_highlight = -13378049;
    public static int juick_username_highlight = -4136193;
    public static int juick_message_highlight = -4136193;
    public static int buttons_label_color = -16777216;

    public static void setDefault() {
        colors.clear();
        colors.add(chat_auth_denied_back);
        colors.add(chat_auth_denied_bar);
        colors.add(chat_auth_denied_text);
        colors.add(chat_auth_grand_back);
        colors.add(chat_auth_grand_bar);
        colors.add(chat_auth_grand_text);
        colors.add(chat_auth_req_back);
        colors.add(chat_auth_req_bar);
        colors.add(chat_auth_req_text);
        colors.add(chat_bottompanel);
        colors.add(chat_date);
        colors.add(chat_header);
        colors.add(chat_header_nick);
        colors.add(chat_header_typing);
        colors.add(chat_inc_back);
        colors.add(chat_inc_bar);
        colors.add(chat_inc_text);
        colors.add(chat_inc_nick);
        colors.add(chat_out_back);
        colors.add(chat_out_bar_confirmed);
        colors.add(chat_out_bar_not_confirmed);
        colors.add(chat_out_text_confirmed);
        colors.add(chat_out_nick);
        colors.add(chat_out_text_not_confirmed);
        colors.add(chat_xtraz_back);
        colors.add(chat_xtraz_bar);
        colors.add(chat_xtraz_text);
        colors.add(contact_item_text_chat);
        colors.add(contact_item_text_norm);
        colors.add(contact_item_text_notadded);
        colors.add(contact_item_text_offline);
        colors.add(contact_item_text_typing);
        colors.add(contactlist_bottompanel_back);
        colors.add(group_item_back);
        colors.add(group_item_text);
        colors.add(profile_group_item_back);
        colors.add(profile_group_item_text);
        colors.add(select_status_header);
        colors.add(select_status_xback);
        colors.add(contactlist_overscroll);
        colors.add(conference_me_nick);
        colors.add(conference_message_highlight);
        colors.add(conference_message_highlight_text);
        colors.add(menu_headers);
        colors.add(menu_dividers);
        colors.add(cl_undernick_text);
        colors.add(text_input_color);
        colors.add(list_selector_color);
        colors.add(contacts_chat_separator);
        colors.add(contacts_screen_header_highlight);
        colors.add(juick_username_highlight);
        colors.add(juick_message_highlight);
        colors.add(buttons_label_color);
        fillNames();
    }

    private static void fillNames() {
        names.clear();
        names.add("Отказ авторизации (фон)");
        names.add("Отказ авторизации (линия)");
        names.add("Отказ авторизации (текст)");
        names.add("Авторизация принята (фон)");
        names.add("Авторизация принята (линия)");
        names.add("Авторизация принята (текст)");
        names.add("Запрос авторизации (фон)");
        names.add("Запрос авторизации (линия)");
        names.add("Запрос авторизации (текст)");
        names.add("Фон нижней панели чата");
        names.add("Цвет даты/времени сообщений");
        names.add("Фон верхней панели чата");
        names.add("Цвет ника в верхней панели чата");
        names.add("Глобальный фон");
        names.add("Входящее сообщение (фон)");
        names.add("Входящее сообщение (линия)");
        names.add("Входящее сообщение (текст)");
        names.add("Входящее сообщение (ник)");
        names.add("Исходящее сообщение (фон)");
        names.add("Подтвержденное сообщение (линия)");
        names.add("Не подтвержденное сообщение (линия)");
        names.add("Подтвержденное сообщение (текст)");
        names.add("Подтвержденное сообщение (ник)");
        names.add("Не подтвержденное сообщение (текст)");
        names.add("XTRAZ сообщение (фон)");
        names.add("XTRAZ сообщение (линия)");
        names.add("XTRAZ сообщение (текст)");
        names.add("Контакт (текст, открытый чат)");
        names.add("Контакт (текст, обычное состояние)");
        names.add("Контакт (текст, не добавленный)");
        names.add("Контакт (текст, не в сети)");
        names.add("Контакт (текст, печатает)");
        names.add("Нижняя панель контакт-листа (фон)");
        names.add("Группа (фон)");
        names.add("Группа (текст)");
        names.add("Профиль-группа (фон)");
        names.add("Профиль-группа (текст)");
        names.add("Выбор статуса, заголовок (фон)");
        names.add("Выбор статуса, x-статус (фон)");
        names.add("Цвет эффекта перепрокрутки списка контактов");
        names.add("Цвет своего ника в окне конференции");
        names.add("Цвет подсветки обращения в конференции (фон)");
        names.add("Цвет подсветки обращения в конференции (текст)");
        names.add("Заголовок контекстных меню");
        names.add("Разделители меню");
        names.add("Текст статусов в КЛ");
        names.add("Цвет вводимого текста");
        names.add("Цвет подсветки выбранных пунктов");
        names.add("Цвет разделителя контактов/чата");
        names.add("Подсветка активного экрана в режиме трех экранов");
        names.add("Подсветка имени пользователя в Juick");
        names.add("Подсветка номера сообщения в Juick");
        names.add("Цвет текста на кнопках");
    }

    public static int getColor(int index) {
        if (index >= colors.size()) {
            return 0;
        }
        return colors.get(index);
    }

    public static void initialize() {
        File colorFile = new File(resources.dataPath + "colors.cfg");
        if (!colorFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                colorFile.createNewFile();
                setDefault();
                saveToInternalFile();
                return;
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return;
            }
        }
        fillFromInternalFile();
    }

    public static void saveToInternalFile() {
        File colorFile = new File(resources.dataPath + "colors.cfg");
        try {
            //noinspection IOStreamConstructor
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(colorFile)));
            int sz = colors.size();
            for (int i = 0; i < sz; i++) {
                dos.writeInt(colors.get(i));
            }
            dos.close();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /** @noinspection unused*/
    public static void saveToCustomExternalFile(String name) {
        File colorFile = new File(resources.JASMINE_SD_PATH + name);
        if (!colorFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                colorFile.createNewFile();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                Toast.makeText(resources.ctx, resources.getString("s_save_colors_error_1"), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        try {
            //noinspection IOStreamConstructor
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(colorFile)));
            int sz = colors.size();
            for (int i = 0; i < sz; i++) {
                dos.writeInt(colors.get(i));
            }
            dos.close();
        } catch (Exception e2) {
            //noinspection CallToPrintStackTrace
            e2.printStackTrace();
            Toast.makeText(resources.ctx, resources.getString("s_save_colors_error_2"), Toast.LENGTH_SHORT).show();
            //noinspection ResultOfMethodCallIgnored
            colorFile.delete();
        }
    }

    public static void saveToExternalFile() {
        File colorFile = new File(resources.JASMINE_SD_PATH + "colors.cfg");
        if (!colorFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                colorFile.createNewFile();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                Toast.makeText(resources.ctx, resources.getString("s_save_colors_error_1"), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        try {
            //noinspection IOStreamConstructor
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(colorFile)));
            int sz = colors.size();
            for (int i = 0; i < sz; i++) {
                dos.writeInt(colors.get(i));
            }
            dos.close();
            Toast.makeText(resources.ctx, resources.getString("s_save_colors_success"), Toast.LENGTH_SHORT).show();
        } catch (Exception e2) {
            //noinspection CallToPrintStackTrace
            e2.printStackTrace();
            Toast.makeText(resources.ctx, resources.getString("s_save_colors_error_2"), Toast.LENGTH_SHORT).show();
            //noinspection ResultOfMethodCallIgnored
            colorFile.delete();
        }
    }

    public static void fillFromInternalFile() {
        File colorFile = new File(resources.dataPath + "colors.cfg");
        try {
            //noinspection IOStreamConstructor
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(colorFile)));
            colors.clear();
            while (dis.available() > 0) {
                colors.add(dis.readInt());
            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        fillNames();
        int d = 53 - colors.size();
        for (int i = 0; i < d; i++) {
            colors.add(Color.rgb(255, 0, 0));
        }
        if (d != 0) {
            Toast.makeText(resources.ctx, resources.getString("s_import_colors_error_3"), Toast.LENGTH_LONG).show();
            saveToInternalFile();
        }
    }

    public static void fillFromAsset() {
        try {
            DataInputStream dis = new DataInputStream(resources.am.open("Interface/colors.cfg"));
            colors.clear();
            while (dis.available() > 0) {
                colors.add(dis.readInt());
            }
            fillNames();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            colors.clear();
        }
    }

    public static void fillFromExternalFile() {
        File colorFile = new File(resources.JASMINE_SD_PATH + "colors.cfg");
        if (!colorFile.exists()) {
            Toast.makeText(resources.ctx, resources.getString("s_import_colors_error_1"), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            //noinspection IOStreamConstructor
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(colorFile)));
            colors.clear();
            while (dis.available() > 0) {
                colors.add(dis.readInt());
            }
            int d = 49 - colors.size();
            for (int i = 0; i < d; i++) {
                colors.add(Color.rgb(255, 0, 0));
            }
            fillNames();
            if (d == 0) {
                Toast.makeText(resources.ctx, resources.getString("s_import_colors_success"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(resources.ctx, resources.getString("s_import_colors_error_3"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Toast.makeText(resources.ctx, resources.getString("s_import_colors_error_2"), Toast.LENGTH_SHORT).show();
            //noinspection ResultOfMethodCallIgnored
            colorFile.delete();
            colors.clear();
        }
    }

    public static Drawable getSolid(int color) {
        return new ColorDrawable(color);
    }

    public static int divideAlpha(int source, int divider) {
        if (divider != 0) {
            int a = Color.alpha(source);
            int r = Color.red(source);
            int g = Color.green(source);
            int b = Color.blue(source);
            return Color.argb(a / divider, r, g, b);
        }
        return source;
    }
}
