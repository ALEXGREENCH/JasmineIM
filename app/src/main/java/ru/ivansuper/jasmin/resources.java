//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import ru.ivansuper.jasmin.MMP.MMPContact;
import ru.ivansuper.jasmin.MultiColumnList.MultiColumnList;
import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.slide_tools.SlideSwitcher;

public class resources {
    public static long DEVICE_HEAP_SIZE = 0L;
    public static long DEVICE_HEAP_USED_SIZE = 0L;
    public static String DEVICE_STR = "";
    public static boolean DONATE_INSTALLED;
    public static boolean IT_IS_TABLET;
    public static PackageManager JASMINE;
    public static String JASMINE_INCOMING_FILES_PATH;
    public static String JASMINE_JHA_PATH;
    public static String JASMINE_SD_PATH;
    public static String JASMINE_SKIN_PATH;
    public static int OS_VERSION = 3;
    public static String OS_VERSION_STR = "";
    public static String SD_PATH = "/mnt/sdcard/";
    public static String SOFTWARE_STR = "";
    public static String VERSION;
    public static AssetManager am;
    private static Bitmap auth_accepted_message_back;
    private static Rect auth_accepted_message_back_padding = new Rect();
    private static Bitmap auth_ask_message_back;
    private static Rect auth_ask_message_back_padding = new Rect();
    private static Bitmap auth_denied_message_back;
    private static Rect auth_denied_message_back_padding = new Rect();
    public static Drawable away;
    public static Drawable away_text;
    private static Bitmap away_text_back;
    public static Drawable back_to_cl_icon;
    public static Drawable bookmarks;
    public static Drawable bp_divider;
    private static Bitmap btn_disabled;
    private static Bitmap btn_disabled_focused;
    private static Rect btn_disabled_focused_padding = new Rect();
    private static Rect btn_disabled_padding = new Rect();
    private static Bitmap btn_normal;
    private static Rect btn_normal_padding = new Rect();
    private static Bitmap btn_pressed;
    private static Rect btn_pressed_padding = new Rect();
    private static Bitmap btn_selected;
    private static Rect btn_selected_padding = new Rect();
    public static Drawable chat;
    private static Bitmap chat_bottom_panel;
    private static Rect chat_bottom_panel_padding = new Rect();
    public static Drawable chat_menu_icon;
    private static Bitmap chat_messages_back;
    private static Rect chat_messages_back_padding = new Rect();
    private static Bitmap chat_top_panel;
    private static Rect chat_top_panel_padding = new Rect();
    private static Bitmap check_off;
    private static Bitmap check_on;
    public static Drawable connected;
    public static Drawable connecting;
    private static Bitmap contactlist_bottom_panel_back;
    private static Bitmap contactlist_bottom_panel_connection_back;
    private static Bitmap contactlist_chat_divider;
    private static Rect contactlist_chat_divider_rect;
    private static Bitmap contactlist_group_normal;
    public static Bitmap contactlist_item_normal;
    private static Bitmap contactlist_item_selected;
    private static Bitmap contactlist_items_back;
    private static Rect contactlist_items_back_padding = new Rect();
    public static Drawable cross;
    public static Context ctx;
    public static Drawable custom_wallpaper;
    public static String dataPath = "";
    public static Drawable depress;
    private static Bitmap dialogs_back;
    private static Rect dialogs_back_padding = new Rect();
    public static Drawable directory;
    public static Drawable directory_up;
    public static DisplayMetrics dm = new DisplayMetrics();
    public static Drawable dnd;
    public static Drawable eat;
    private static Bitmap edt_disabled;
    private static Bitmap edt_disabled_focused;
    private static Rect edt_disabled_focused_padding = new Rect();
    private static Rect edt_disabled_padding = new Rect();
    private static Bitmap edt_normal;
    private static Rect edt_normal_padding = new Rect();
    private static Bitmap edt_pressed;
    private static Rect edt_pressed_padding = new Rect();
    private static Bitmap edt_selected;
    private static Rect edt_selected_padding = new Rect();
    public static Drawable evil;
    public static Drawable file;
    public static Drawable file_brw;
    public static Drawable file_for_chat;
    public static Drawable file_transfering;
    public static Drawable for_all;
    public static Drawable for_all_e_invl;
    public static Drawable for_cl;
    public static Drawable for_vl;
    public static Drawable google_mail;
    public static Drawable group_closed;
    public static Drawable group_opened;
    public static Drawable gtalk_connecting;
    public static Drawable gtalk_offline;
    public static Drawable gtalk_online;
    public static Drawable home;
    public static Drawable ignore;
    public static Drawable img_file;
    public static Drawable img_file_bad;
    public static Drawable img_file_big;
    public static Drawable inc_file;
    private static Bitmap incoming_message_back;
    private static Rect incoming_message_back_padding = new Rect();
    public static Drawable invisible;
    public static Drawable ivn_for_all;
    public static Drawable jabber_away;
    public static Drawable jabber_chat;
    public static Drawable jabber_command;
    public static Drawable jabber_conf_admin;
    public static Drawable jabber_conf_member;
    public static Drawable jabber_conf_moderator;
    public static Drawable jabber_conf_outcast;
    public static Drawable jabber_conf_owner;
    public static Drawable jabber_conf_pm;
    public static Drawable jabber_conf_visitor;
    public static Drawable jabber_conference;
    public static Drawable jabber_conference_offline;
    public static Drawable jabber_connecting;
    public static Drawable jabber_disco;
    public static Drawable jabber_dnd;
    public static Drawable jabber_error;
    public static Drawable jabber_na;
    public static Drawable jabber_oc;
    public static Drawable jabber_offline;
    public static Drawable jabber_online;
    public static Drawable jabber_priority;
    public static Drawable jabber_server;
    public static Drawable jabber_waiting;
    public static Drawable jabber_xml_console;
    public static Drawable link_icon;
    public static Vector<String> log = new Vector();
    public static Drawable marker_active_chat;
    public static Drawable marker_chat;
    public static Drawable marker_msg_chat;
    public static Drawable mrim_angry;
    public static Drawable mrim_away;
    public static Drawable mrim_chat;
    public static Drawable mrim_connecting;
    public static Drawable mrim_depress;
    public static Drawable mrim_dnd;
    public static Drawable mrim_home;
    public static Drawable mrim_lunch;
    public static Drawable mrim_na;
    public static Drawable mrim_oc;
    public static Drawable mrim_offline;
    public static Drawable mrim_online;
    public static Drawable mrim_wakeup;
    public static Drawable mrim_work;
    public static Drawable msg_in;
    public static Drawable msg_in_blink;
    public static Drawable msg_out;
    public static Drawable msg_out_c;
    public static Drawable msgs_number_back;
    public static Drawable na;
    public static Drawable node_closed;
    public static Drawable node_opened;
    public static Drawable not_added;
    public static Drawable oc;
    public static Drawable odnoclass_offline;
    public static Drawable odnoclass_online;
    public static Drawable offline;
    public static Drawable online;
    private static Bitmap outgoing_message_back;
    private static Rect outgoing_message_back_padding = new Rect();
    public static Drawable phantom;
    private static Bitmap popup_back;
    private static Rect popup_back_padding = new Rect();
    public static Drawable profile_tools;
    public static Drawable qip_connecting;
    public static Drawable qip_offline;
    public static Drawable qip_online;
    public static Resources res;
    public static Drawable send_msg_icon;
    public static jasminSvc service;
    private static Bitmap slide_switcher_panel;
    private static Rect slide_switcher_panel_padding = new Rect();
    public static Drawable smileys_select_icon;
    public static Drawable sms;
    private static Bitmap status_message_back;
    private static Rect status_message_back_padding = new Rect();
    private static Bitmap status_selector_arrow;
    private static Bitmap status_selector_back;
    private static Rect status_selector_back_padding = new Rect();
    public static Drawable tab_highlight;
    public static Drawable toggle_offline;
    public static Drawable toggle_offline_a;
    public static Drawable toggle_sound;
    public static Drawable toggle_sound_a;
    public static Drawable toggle_vibro;
    public static Drawable toggle_vibro_a;
    private static Bitmap transfer_message_back;
    private static Rect transfer_message_back_padding = new Rect();
    public static Drawable typing;
    public static Drawable unauthorized;
    public static Drawable unauthorized_icon;
    public static Drawable url_icon;
    public static Drawable visible;
    public static Drawable vk_connecting;
    public static Drawable vk_offline;
    public static Drawable vk_online;
    public static Drawable work;
    public static Drawable x_angry;
    public static Drawable x_beer;
    public static Drawable x_business;
    public static Drawable x_camera;
    public static Drawable x_coffe;
    public static Drawable x_college;
    public static Drawable x_diary;
    public static Drawable x_duck;
    public static Drawable x_eating;
    public static Drawable x_engineering;
    public static Drawable x_friends;
    public static Drawable x_funny;
    public static Drawable x_games;
    public static Drawable x_internet;
    public static Drawable x_love;
    public static Drawable x_man;
    public static Drawable x_mobile;
    public static Drawable x_music;
    public static Drawable x_party;
    public static Drawable x_phone;
    public static Drawable x_picnic;
    public static Drawable x_ppc;
    public static Drawable x_question;
    public static Drawable x_rulove;
    public static Drawable x_search;
    public static Drawable x_sex;
    public static Drawable x_shopping;
    public static Drawable x_sick;
    public static Drawable x_sleep;
    public static Drawable x_smoke;
    public static Drawable x_surfing;
    public static Drawable x_think;
    public static Drawable x_tired;
    public static Drawable x_tv;
    public static Drawable x_typing;
    public static Drawable x_way;
    public static Drawable x_wc;
    public static Drawable yandex_connecting;
    public static Drawable yandex_offline;
    public static Drawable yandex_online;

    public resources() {
    }

    public static void attachAuthAccMsg(View var0) {
        if (auth_accepted_message_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), auth_accepted_message_back, auth_accepted_message_back.getNinePatchChunk(), auth_accepted_message_back_padding, (String)null));
        }

    }

    public static void attachAuthAskMsg(View var0) {
        if (auth_ask_message_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), auth_ask_message_back, auth_ask_message_back.getNinePatchChunk(), auth_ask_message_back_padding, (String)null));
        }

    }

    public static void attachAuthDenMsg(View var0) {
        if (auth_denied_message_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), auth_denied_message_back, auth_denied_message_back.getNinePatchChunk(), auth_denied_message_back_padding, (String)null));
        }

    }

    public static void attachAwayTextBackground(View var0) {
        if (away_text_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), away_text_back, away_text_back.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachButtonStyle(View var0) {
        try {
            ((Button)var0).setTextColor(ColorScheme.getColor(52));
        } catch (Exception var3) {
        }

        if (btn_normal != null && btn_disabled != null && btn_pressed != null && btn_selected != null && btn_disabled_focused != null) {
            StateListDrawable var1 = new StateListDrawable();
            if (btn_normal_padding == null) {
                btn_normal_padding = new Rect(5, 5, 5, 5);
            }

            NinePatchDrawable var2 = new NinePatchDrawable(ctx.getResources(), btn_normal, btn_normal.getNinePatchChunk(), btn_normal_padding, (String)null);
            var1.addState(new int[]{-16842909, 16842908}, var2);
            if (btn_disabled_padding == null) {
                btn_disabled_padding = new Rect(5, 5, 5, 5);
            }

            var2 = new NinePatchDrawable(ctx.getResources(), btn_disabled, btn_disabled.getNinePatchChunk(), btn_disabled_padding, (String)null);
            var1.addState(new int[]{-16842909, -16842908}, var2);
            if (btn_pressed_padding == null) {
                btn_pressed_padding = new Rect(5, 5, 5, 5);
            }

            var2 = new NinePatchDrawable(ctx.getResources(), btn_pressed, btn_pressed.getNinePatchChunk(), btn_pressed_padding, (String)null);
            var1.addState(new int[]{16842919}, var2);
            if (btn_selected_padding == null) {
                btn_selected_padding = new Rect(5, 5, 5, 5);
            }

            var2 = new NinePatchDrawable(ctx.getResources(), btn_selected, btn_selected.getNinePatchChunk(), btn_selected_padding, (String)null);
            var1.addState(new int[]{16842908, 16842910}, var2);
            if (btn_normal_padding == null) {
                btn_normal_padding = new Rect(5, 5, 5, 5);
            }

            var2 = new NinePatchDrawable(ctx.getResources(), btn_normal, btn_normal.getNinePatchChunk(), btn_normal_padding, (String)null);
            var1.addState(new int[]{16842910}, var2);
            if (btn_disabled_focused_padding == null) {
                btn_disabled_focused_padding = new Rect();
            }

            var2 = new NinePatchDrawable(ctx.getResources(), btn_disabled_focused, btn_disabled_focused.getNinePatchChunk(), btn_disabled_focused_padding, (String)null);
            var1.addState(new int[]{16842908}, var2);
            var0.setBackgroundDrawable(var1);
        }

    }

    public static void attachChatBottomPanel(View var0) {
        if (chat_bottom_panel != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), chat_bottom_panel, chat_bottom_panel.getNinePatchChunk(), chat_bottom_panel_padding, (String)null));
        }

    }

    public static void attachChatMessagesBack(View var0) {
        if (chat_messages_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), chat_messages_back, chat_messages_back.getNinePatchChunk(), chat_messages_back_padding, (String)null));
        }

    }

    public static void attachChatMessagesBack(Window var0) {
        if (chat_messages_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), chat_messages_back, chat_messages_back.getNinePatchChunk(), chat_messages_back_padding, (String)null));
        }

    }

    public static void attachChatTopPanel(View var0) {
        if (chat_top_panel != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), chat_top_panel, chat_top_panel.getNinePatchChunk(), chat_top_panel_padding, (String)null));
        }

    }

    public static void attachCheckStyle(CheckBox var0) {
        if (check_on != null && check_off != null) {
            StateListDrawable var1 = new StateListDrawable();
            BitmapDrawable var2 = new BitmapDrawable(ctx.getResources(), check_on);
            BitmapDrawable var3 = new BitmapDrawable(ctx.getResources(), check_off);
            var1.addState(new int[]{16843014, 16842908, 16842766}, var2);
            var1.addState(new int[]{-16843014, -16842908, 16842766}, var3);
            var1.addState(new int[]{16843014, 16842919, 16842766}, var2);
            var1.addState(new int[]{-16843014, 16842919, 16842766}, var3);
            var1.addState(new int[]{-16843014}, var3);
            var1.addState(new int[]{16843014}, var2);
            var0.setBackgroundDrawable(var1);
        }

    }

    public static void attachContactlistBack(View var0) {
        if (contactlist_items_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), contactlist_items_back, contactlist_items_back.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachContactlistBack(Window var0) {
        if (contactlist_items_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), contactlist_items_back, contactlist_items_back.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachContactlistBottomConnectionStatusPanel(LinearLayout var0) {
        if (contactlist_bottom_panel_connection_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), contactlist_bottom_panel_connection_back, contactlist_bottom_panel_connection_back.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachContactlistBottomPanel(View var0) {
        if (contactlist_bottom_panel_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), contactlist_bottom_panel_back, contactlist_bottom_panel_back.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachContactlistChatDivider(View var0) {
        if (contactlist_chat_divider == null) {
            var0.setBackgroundResource(R.drawable.contactlist_chat_divider);
        } else {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), contactlist_chat_divider, contactlist_chat_divider.getNinePatchChunk(), contactlist_chat_divider_rect, (String)null));
        }

    }

    public static void attachContactlistGroupItemBack(View var0) {
        if (contactlist_group_normal != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), contactlist_group_normal, contactlist_group_normal.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachContactlistItemBack(View var0) {
        if (contactlist_item_normal != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), contactlist_item_normal, contactlist_item_normal.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachDialogStyle(Window var0) {
        if (dialogs_back != null) {
            if (dialogs_back_padding == null) {
                dialogs_back_padding = new Rect(15, 15, 15, 15);
            }

            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), dialogs_back, dialogs_back.getNinePatchChunk(), dialogs_back_padding, (String)null));
        }

    }

    public static boolean attachEditText(EditText var0) {
        return attachEditTextInternal(var0);
    }

    private static boolean attachEditTextInternal(EditText var0) {
        int var1 = ColorScheme.getColor(46);
        int var2 = Color.alpha(var1);
        int var3 = Color.red(var1);
        int var4 = Color.green(var1);
        int var5 = Color.blue(var1);
        var0.setTextColor(var1);
        var0.setShadowLayer(1.0F, 0.0F, 0.0F, Color.argb(var2 / 5, var3, var4, var5));
        boolean var6;
        if (edt_normal == null) {
            var6 = false;
        } else if (edt_disabled == null) {
            var6 = false;
        } else if (edt_pressed == null) {
            var6 = false;
        } else if (edt_selected == null) {
            var6 = false;
        } else if (edt_disabled_focused == null) {
            var6 = false;
        } else {
            StateListDrawable var7 = new StateListDrawable();
            if (edt_normal_padding == null) {
                edt_normal_padding = new Rect(18, 5, 18, 5);
            }

            NinePatchDrawable var8 = new NinePatchDrawable(ctx.getResources(), edt_normal, edt_normal.getNinePatchChunk(), edt_normal_padding, (String)null);
            var7.addState(new int[]{-16842909, 16842908}, var8);
            if (edt_disabled_padding == null) {
                edt_disabled_padding = new Rect(18, 5, 18, 5);
            }

            var8 = new NinePatchDrawable(ctx.getResources(), edt_disabled, edt_disabled.getNinePatchChunk(), edt_disabled_padding, (String)null);
            var7.addState(new int[]{-16842909, -16842908}, var8);
            if (edt_pressed_padding == null) {
                edt_pressed_padding = new Rect(18, 5, 18, 5);
            }

            var8 = new NinePatchDrawable(ctx.getResources(), edt_pressed, edt_pressed.getNinePatchChunk(), edt_pressed_padding, (String)null);
            var7.addState(new int[]{16842919}, var8);
            if (edt_selected_padding == null) {
                edt_selected_padding = new Rect(18, 5, 18, 5);
            }

            var8 = new NinePatchDrawable(ctx.getResources(), edt_selected, edt_selected.getNinePatchChunk(), edt_selected_padding, (String)null);
            var7.addState(new int[]{16842908, 16842910}, var8);
            if (edt_normal_padding == null) {
                edt_normal_padding = new Rect(18, 5, 18, 5);
            }

            var8 = new NinePatchDrawable(ctx.getResources(), edt_normal, edt_normal.getNinePatchChunk(), edt_normal_padding, (String)null);
            var7.addState(new int[]{16842910}, var8);
            if (edt_disabled_focused_padding == null) {
                edt_disabled_focused_padding = new Rect();
            }

            var8 = new NinePatchDrawable(ctx.getResources(), edt_disabled_focused, edt_disabled_focused.getNinePatchChunk(), edt_disabled_focused_padding, (String)null);
            var7.addState(new int[]{16842908}, var8);
            var0.setBackgroundDrawable(var7);
            var6 = true;
        }

        return var6;
    }

    public static void attachIngMsg(View var0) {
        if (incoming_message_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), incoming_message_back, incoming_message_back.getNinePatchChunk(), incoming_message_back_padding, (String)null));
        }

    }

    public static void attachListSelector(MultiColumnList var0) {
        if (contactlist_item_selected != null) {
            var0.setListSelector(new NinePatchDrawable(ctx.getResources(), contactlist_item_selected, contactlist_item_selected.getNinePatchChunk(), new Rect(), (String)null));
        }

    }

    public static void attachOutMsg(View var0) {
        if (outgoing_message_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), outgoing_message_back, outgoing_message_back.getNinePatchChunk(), outgoing_message_back_padding, (String)null));
        }

    }

    public static void attachPopupBack(View var0) {
        if (popup_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), popup_back, popup_back.getNinePatchChunk(), popup_back_padding, (String)null));
        }

    }

    public static void attachSlidePanel(SlideSwitcher var0) {
        if (slide_switcher_panel != null) {
            if (slide_switcher_panel_padding == null) {
                slide_switcher_panel_padding = new Rect(1, 1, 1, 1);
            }

            var0.panel = new NinePatchDrawable(ctx.getResources(), slide_switcher_panel, slide_switcher_panel.getNinePatchChunk(), slide_switcher_panel_padding, (String)null);
        }

    }

    public static void attachStatusMsg(View var0) {
        if (status_message_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), status_message_back, status_message_back.getNinePatchChunk(), status_message_back_padding, (String)null));
        }

    }

    public static void attachStatusSelectorBackAndArrow(View var0) {
        if (status_selector_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), status_selector_back, status_selector_back.getNinePatchChunk(), status_selector_back_padding, (String)null));
        }

    }

    public static void attachTransferMsg(View var0) {
        if (transfer_message_back != null) {
            var0.setBackgroundDrawable(new NinePatchDrawable(ctx.getResources(), transfer_message_back, transfer_message_back.getNinePatchChunk(), transfer_message_back_padding, (String)null));
        }

    }

    public static ru.ivansuper.jasmin.BitmapDrawable convertToMyFormat(Drawable var0) {
        ru.ivansuper.jasmin.BitmapDrawable var1;
        if (var0 == null) {
            var1 = null;
        } else {
            Bitmap var2 = ((BitmapDrawable)var0).getBitmap();
            var2.setDensity(0);
            var1 = new ru.ivansuper.jasmin.BitmapDrawable(var2);
            var1.setBounds(0, 0, var1.getIntrinsicWidth(), var1.getIntrinsicHeight());
            var1 = (ru.ivansuper.jasmin.BitmapDrawable)var1;
        }

        return var1;
    }

    public static void copyAssetToSD(String var0, String var1) {
        File var10 = new File(var1);
        if (!var10.exists()) {
            IOException var10000;
            label40: {
                InputStream var2;
                FileOutputStream var8;
                byte[] var11;
                boolean var10001;
                try {
                    var10.createNewFile();
                    var2 = am.open(var0);
                    var8 = new FileOutputStream(var10);
                    var11 = new byte[8192];
                } catch (IOException var7) {
                    var10000 = var7;
                    var10001 = false;
                    break label40;
                }

                while(true) {
                    try {
                        if (var2.available() <= 0) {
                            var8.close();
                            var2.close();
                            return;
                        }
                    } catch (IOException var6) {
                        var10000 = var6;
                        var10001 = false;
                        break;
                    }

                    int var3;
                    try {
                        var3 = var2.read(var11, 0, 8192);
                    } catch (IOException var5) {
                        var10000 = var5;
                        var10001 = false;
                        break;
                    }

                    if (var3 > 0) {
                        try {
                            var8.write(var11, 0, var3);
                        } catch (IOException var4) {
                            var10000 = var4;
                            var10001 = false;
                            break;
                        }
                    }
                }
            }

            IOException var9 = var10000;
            var9.printStackTrace();
        }

    }

    public static NinePatchDrawable getContactlistGroupItemBack() {
        NinePatchDrawable var0 = null;
        if (contactlist_group_normal != null) {
            var0 = new NinePatchDrawable(ctx.getResources(), contactlist_group_normal, contactlist_group_normal.getNinePatchChunk(), new Rect(), (String)null);
        }

        return var0;
    }

    public static NinePatchDrawable getContactlistItemBack() {
        NinePatchDrawable var0 = null;
        if (contactlist_item_normal != null) {
            var0 = new NinePatchDrawable(ctx.getResources(), contactlist_item_normal, contactlist_item_normal.getNinePatchChunk(), new Rect(), (String)null);
        }

        return var0;
    }

    public static double getDiagonal() {
        double var3;
        try {
            DisplayMetrics var0 = ctx.getResources().getDisplayMetrics();
            float var1 = (float)(var0.widthPixels / var0.densityDpi);
            float var2 = (float)(var0.heightPixels / var0.densityDpi);
            var3 = Math.sqrt(Math.pow((double)var1, 2.0) + Math.pow((double)var2, 2.0));
        } catch (Throwable var5) {
            var3 = 0.0;
        }

        return var3;
    }

    public static Drawable getICQStatusIconFull(ICQContact var0) {
        Drawable var1;
        if (var0.added) {
            if (var0.authorized) {
                if (utilities.isUIN(var0.ID)) {
                    var1 = getStatusIcon(var0.status);
                } else {
                    var1 = getMrimStatusIcon(var0.status);
                }
            } else {
                var1 = unauthorized;
            }
        } else {
            var1 = not_added;
        }

        return var1;
    }

    public static int getIcqAbstractStatus(int var0) {
        byte var1 = 0;
        byte var2;
        switch (var0) {
            case -1:
                var2 = -1;
                break;
            case 0:
                var2 = 0;
                break;
            case 1:
                var2 = 7;
                break;
            case 2:
                var2 = 10;
                break;
            case 4:
                var2 = 8;
                break;
            case 5:
                var2 = 8;
                break;
            case 16:
                var2 = 9;
                break;
            case 17:
                var2 = 9;
                break;
            case 32:
                var2 = 1;
                break;
            case 8193:
                var2 = 6;
                break;
            case 12288:
                var2 = 2;
                break;
            case 16384:
                var2 = 3;
                break;
            case 20480:
                var2 = 4;
                break;
            case 24576:
                var2 = 5;
                break;
            default:
                var2 = var1;
        }

        return var2;
    }

    public static Drawable getListSelector() {
        StateListDrawable var0 = new StateListDrawable();
        SolidDrawable var1 = new SolidDrawable(0);
        var0.addState(new int[]{-16842909}, var1);
        var1 = new SolidDrawable(0);
        var0.addState(new int[]{16842908, -16842910, 16842919}, var1);
        var1 = new SolidDrawable(0);
        var0.addState(new int[]{16842908, -16842910}, var1);
        var1 = new SolidDrawable(ColorScheme.getColor(47));
        var0.addState(new int[]{16842908, 16842919}, var1);
        var1 = new SolidDrawable(ColorScheme.getColor(47));
        var0.addState(new int[]{-16842908, 16842919}, var1);
        var1 = new SolidDrawable(0);
        var0.addState(new int[]{-16842908}, var1);
        var1 = new SolidDrawable(ColorScheme.getColor(47));
        var0.addState(new int[]{16842908}, var1);
        return var0;
    }

    public static Drawable getMMPStatusIconFull(MMPContact var0) {
        Drawable var1;
        switch (var0.status) {
            case 0:
                var1 = mrim_offline;
                break;
            case 1:
            case 3:
            case 4:
            default:
                var1 = mrim_online;
                break;
            case 2:
                var1 = mrim_away;
                break;
            case 5:
                var1 = mrim_dnd;
                break;
            case 6:
                var1 = mrim_oc;
                break;
            case 7:
                var1 = mrim_na;
                break;
            case 8:
                var1 = mrim_lunch;
                break;
            case 9:
                var1 = mrim_work;
                break;
            case 10:
                var1 = mrim_home;
                break;
            case 11:
                var1 = mrim_depress;
                break;
            case 12:
                var1 = mrim_angry;
                break;
            case 13:
                var1 = mrim_chat;
        }

        return var1;
    }

    public static Drawable getMrimStatusIcon(int var0) {
        Drawable var1 = online;
        switch (var0) {
            case -1:
                var1 = mrim_offline;
                break;
            case 0:
                var1 = mrim_online;
                break;
            case 1:
                var1 = mrim_away;
        }

        return var1;
    }

    public static final String getSomething(String var0) {
        return "Result from another class!";
    }

    @Deprecated
    public static Drawable getStatusIcon(int var0) {
        Drawable var1 = online;
        switch (var0) {
            case -1:
                var1 = offline;
                break;
            case 0:
                var1 = online;
                break;
            case 1:
                var1 = away;
                break;
            case 2:
                var1 = dnd;
                break;
            case 4:
                var1 = na;
                break;
            case 5:
                var1 = na;
                break;
            case 16:
                var1 = oc;
                break;
            case 17:
                var1 = oc;
                break;
            case 32:
                var1 = chat;
                break;
            case 8193:
                var1 = eat;
                break;
            case 12288:
                var1 = evil;
                break;
            case 16384:
                var1 = depress;
                break;
            case 20480:
                var1 = home;
                break;
            case 24576:
                var1 = work;
        }

        return var1;
    }

    public static String getString(int var0) {
        return ctx.getResources().getString(var0);
    }

    public static String getString(String var0) {
        return utilities.replace(Locale.getString(var0), "[NL]", "\n");
    }

    public static final int getTrayMessageIconResId(int var0) {
        int var1 = 2130837572;
        int var2 = var1;
        switch (var0) {
            case 1:
                break;
            case 2:
                var2 = 2130837598;
                break;
            case 3:
                var2 = 2130837599;
                break;
            case 4:
                var2 = 2130837600;
                break;
            case 5:
                var2 = 2130837601;
                break;
            case 6:
                var2 = 2130837602;
                break;
            case 7:
                var2 = 2130837603;
                break;
            case 8:
                var2 = 2130837604;
                break;
            case 9:
                var2 = 2130837605;
                break;
            default:
                var2 = var1;
                if (var0 > 9) {
                    var2 = 2130837606;
                }
        }

        return var2;
    }

    public static Drawable getXMPPStatusIcon(int var0) {
        Drawable var1;
        switch (var0) {
            case 0:
                var1 = jabber_chat;
                break;
            case 1:
                var1 = jabber_online;
                break;
            case 2:
                var1 = jabber_away;
                break;
            case 3:
                var1 = jabber_dnd;
                break;
            case 4:
                var1 = jabber_na;
                break;
            default:
                var1 = jabber_offline;
        }

        return var1;
    }

    public static Drawable getXMPPStatusIconFull(JContact var0) {
        Object var1 = null;
        Drawable var2;
        switch (var0.profile.type) {
            case 0:
                if (var0.conf_pm) {
                    var2 = jabber_conf_pm;
                } else if (var0.isOnline()) {
                    switch (var0.getStatus()) {
                        case 0:
                            var2 = jabber_chat;
                            return var2;
                        case 1:
                            var2 = jabber_online;
                            return var2;
                        case 2:
                            var2 = jabber_away;
                            return var2;
                        case 3:
                            var2 = jabber_dnd;
                            return var2;
                        case 4:
                            var2 = jabber_na;
                            return var2;
                        default:
                            var2 = (Drawable)var1;
                    }
                } else {
                    var2 = jabber_offline;
                }
                break;
            case 1:
                if (var0.isOnline()) {
                    var2 = vk_online;
                } else {
                    var2 = vk_offline;
                }
                break;
            case 2:
                if (var0.isOnline()) {
                    var2 = yandex_online;
                } else {
                    var2 = yandex_offline;
                }
                break;
            case 3:
                if (var0.isOnline()) {
                    var2 = gtalk_online;
                } else {
                    var2 = gtalk_offline;
                }
                break;
            case 4:
                if (var0.isOnline()) {
                    var2 = qip_online;
                } else {
                    var2 = qip_offline;
                }
                break;
            default:
                var2 = (Drawable)var1;
        }

        return var2;
    }

    public static void initInternalGraphics() {
        if (custom_wallpaper == null) {
            custom_wallpaper = ColorScheme.getSolid(-16777216);
        }

        if (mrim_online == null) {
            mrim_online = normalize(res.getDrawable(R.drawable.mrim_contact_status_online));
        }

        if (mrim_connecting == null) {
            mrim_connecting = normalize(res.getDrawable(R.drawable.mrim_connecting));
        }

        if (mrim_offline == null) {
            mrim_offline = normalize(res.getDrawable(R.drawable.mrim_contact_status_offline));
        }

        if (mrim_away == null) {
            mrim_away = normalize(res.getDrawable(R.drawable.mrim_contact_status_away));
        }

        if (mrim_home == null) {
            mrim_home = normalize(res.getDrawable(R.drawable.mrim_contact_status_home));
        }

        if (mrim_work == null) {
            mrim_work = normalize(res.getDrawable(R.drawable.mrim_contact_status_work));
        }

        if (mrim_dnd == null) {
            mrim_dnd = normalize(res.getDrawable(R.drawable.mrim_contact_status_dnd));
        }

        if (mrim_angry == null) {
            mrim_angry = normalize(res.getDrawable(R.drawable.mrim_contact_status_angry));
        }

        if (mrim_chat == null) {
            mrim_chat = normalize(res.getDrawable(R.drawable.mrim_contact_status_chat));
        }

        if (mrim_lunch == null) {
            mrim_lunch = normalize(res.getDrawable(R.drawable.mrim_contact_status_lunch));
        }

        if (mrim_na == null) {
            mrim_na = normalize(res.getDrawable(R.drawable.mrim_contact_status_na));
        }

        if (mrim_oc == null) {
            mrim_oc = normalize(res.getDrawable(R.drawable.mrim_contact_status_oc));
        }

        if (mrim_depress == null) {
            mrim_depress = normalize(res.getDrawable(R.drawable.mrim_contact_status_depress));
        }

        if (mrim_wakeup == null) {
            mrim_wakeup = normalize(res.getDrawable(R.drawable.mrim_wakeup));
        }

        if (away_text == null) {
            away_text = normalize(res.getDrawable(R.drawable.icq_away_text));
        }

        if (profile_tools == null) {
            profile_tools = normalize(res.getDrawable(R.drawable.profile_tools));
        }

        if (google_mail == null) {
            google_mail = normalize(res.getDrawable(R.drawable.google_mail));
        }

        if (odnoclass_online == null) {
            odnoclass_online = normalize(res.getDrawable(R.drawable.odnoklassniki_online));
        }

        if (odnoclass_offline == null) {
            odnoclass_offline = normalize(res.getDrawable(R.drawable.odnoklassniki_offline));
        }

        if (gtalk_online == null) {
            gtalk_online = normalize(res.getDrawable(R.drawable.gtalk_online));
        }

        if (gtalk_offline == null) {
            gtalk_offline = normalize(res.getDrawable(R.drawable.gtalk_offline));
        }

        if (gtalk_connecting == null) {
            gtalk_connecting = normalize(res.getDrawable(R.drawable.gtalk_connecting));
        }

        if (qip_online == null) {
            qip_online = normalize(res.getDrawable(R.drawable.qip_online));
        }

        if (qip_offline == null) {
            qip_offline = normalize(res.getDrawable(R.drawable.qip_offline));
        }

        if (qip_connecting == null) {
            qip_connecting = normalize(res.getDrawable(R.drawable.qip_connecting));
        }

        if (node_opened == null) {
            node_opened = normalize(res.getDrawable(R.drawable.node_opened));
        }

        if (node_closed == null) {
            node_closed = normalize(res.getDrawable(R.drawable.node_closed));
        }

        if (tab_highlight == null) {
            tab_highlight = res.getDrawable(R.drawable.slide_switcher_tab_highlight);
        }

        if (link_icon == null) {
            link_icon = normalize(res.getDrawable(R.drawable.link_icon));
        }

        if (bookmarks == null) {
            bookmarks = normalize(res.getDrawable(R.drawable.bookmarks));
        }

        if (jabber_server == null) {
            jabber_server = normalize(res.getDrawable(R.drawable.xmpp_server));
        }

        if (jabber_command == null) {
            jabber_command = normalize(res.getDrawable(R.drawable.xmpp_command));
        }

        if (jabber_waiting == null) {
            jabber_waiting = normalize(res.getDrawable(R.drawable.xmpp_waiting));
        }

        if (jabber_error == null) {
            jabber_error = normalize(res.getDrawable(R.drawable.xmpp_error));
        }

        if (chat_menu_icon == null) {
            chat_menu_icon = normalize(res.getDrawable(R.drawable.ic_menu));
        }

        if (smileys_select_icon == null) {
            smileys_select_icon = normalize(res.getDrawable(R.drawable.smile_button));
        }

        if (send_msg_icon == null) {
            send_msg_icon = normalize(res.getDrawable(R.drawable.send_msg_icon));
        }

        if (back_to_cl_icon == null) {
            back_to_cl_icon = normalize(res.getDrawable(R.drawable.back_to_cl_icon));
        }

        if (marker_chat == null) {
            marker_chat = res.getDrawable(R.drawable.chat_other_chats);
        }

        if (marker_active_chat == null) {
            marker_active_chat = res.getDrawable(R.drawable.chat_current_chat);
        }

        if (marker_msg_chat == null) {
            marker_msg_chat = res.getDrawable(R.drawable.chat_other_chat_not_readed);
        }

        if (msgs_number_back == null) {
            msgs_number_back = res.getDrawable(R.drawable.msgs_number_back);
        }

        if (jabber_disco == null) {
            jabber_disco = normalize(res.getDrawable(R.drawable.xmpp_disco));
        }

        if (jabber_xml_console == null) {
            jabber_xml_console = normalize(res.getDrawable(R.drawable.xml_console));
        }

        if (jabber_priority == null) {
            jabber_priority = normalize(res.getDrawable(R.drawable.xmpp_priority));
        }

        if (jabber_conf_pm == null) {
            jabber_conf_pm = normalize(res.getDrawable(R.drawable.xmpp_conference_pm));
        }

        if (jabber_conf_owner == null) {
            jabber_conf_owner = normalize(res.getDrawable(R.drawable.xmpp_conf_owner));
        }

        if (jabber_conf_admin == null) {
            jabber_conf_admin = normalize(res.getDrawable(R.drawable.xmpp_conf_admin));
        }

        if (jabber_conf_moderator == null) {
            jabber_conf_moderator = normalize(res.getDrawable(R.drawable.xmpp_conf_moderator));
        }

        if (jabber_conf_visitor == null) {
            jabber_conf_visitor = normalize(res.getDrawable(R.drawable.xmpp_conf_visitor));
        }

        if (jabber_conf_member == null) {
            jabber_conf_member = normalize(res.getDrawable(R.drawable.xmpp_conf_member));
        }

        if (jabber_conf_outcast == null) {
            jabber_conf_outcast = normalize(res.getDrawable(R.drawable.xmpp_conf_outcast));
        }

        if (jabber_conference_offline == null) {
            jabber_conference_offline = normalize(res.getDrawable(R.drawable.xmpp_conference_offline));
        }

        if (jabber_conference == null) {
            jabber_conference = normalize(res.getDrawable(R.drawable.xmpp_conference));
        }

        if (url_icon == null) {
            url_icon = normalize(res.getDrawable(R.drawable.url_icon));
        }

        if (jabber_online == null) {
            jabber_online = normalize(res.getDrawable(R.drawable.xmpp_online));
        }

        if (jabber_away == null) {
            jabber_away = normalize(res.getDrawable(R.drawable.xmpp_away));
        }

        if (jabber_oc == null) {
            jabber_oc = normalize(res.getDrawable(R.drawable.xmpp_oc));
        }

        if (jabber_dnd == null) {
            jabber_dnd = normalize(res.getDrawable(R.drawable.xmpp_dnd));
        }

        if (jabber_na == null) {
            jabber_na = normalize(res.getDrawable(R.drawable.xmpp_na));
        }

        if (jabber_chat == null) {
            jabber_chat = normalize(res.getDrawable(R.drawable.xmpp_chat));
        }

        if (jabber_offline == null) {
            jabber_offline = normalize(res.getDrawable(R.drawable.xmpp_offline));
        }

        if (jabber_connecting == null) {
            jabber_connecting = normalize(res.getDrawable(R.drawable.xmpp_connecting));
        }

        if (vk_online == null) {
            vk_online = normalize(res.getDrawable(R.drawable.vk_online));
        }

        if (vk_offline == null) {
            vk_offline = normalize(res.getDrawable(R.drawable.vk_offline));
        }

        if (vk_connecting == null) {
            vk_connecting = normalize(res.getDrawable(R.drawable.vk_connecting));
        }

        if (yandex_online == null) {
            yandex_online = normalize(res.getDrawable(R.drawable.vk_online));
        }

        if (yandex_offline == null) {
            yandex_offline = normalize(res.getDrawable(R.drawable.ya_offline));
        }

        if (yandex_connecting == null) {
            yandex_connecting = normalize(res.getDrawable(R.drawable.ya_connecting));
        }

        if (online == null) {
            online = normalize(res.getDrawable(R.drawable.icq_status_online));
        }

        if (offline == null) {
            offline = normalize(res.getDrawable(R.drawable.icq_status_offline));
        }

        if (connecting == null) {
            connecting = normalize(res.getDrawable(R.drawable.icq_status_connecting));
        }

        if (away == null) {
            away = normalize(res.getDrawable(R.drawable.icq_status_away));
        }

        if (oc == null) {
            oc = normalize(res.getDrawable(R.drawable.icq_status_oc));
        }

        if (dnd == null) {
            dnd = normalize(res.getDrawable(R.drawable.icq_status_dnd));
        }

        if (na == null) {
            na = normalize(res.getDrawable(R.drawable.icq_status_na));
        }

        if (home == null) {
            home = normalize(res.getDrawable(R.drawable.icq_status_home));
        }

        if (chat == null) {
            chat = normalize(res.getDrawable(R.drawable.icq_status_chat));
        }

        if (eat == null) {
            eat = normalize(res.getDrawable(R.drawable.icq_status_lunch));
        }

        if (work == null) {
            work = normalize(res.getDrawable(R.drawable.icq_status_work));
        }

        if (depress == null) {
            depress = normalize(res.getDrawable(R.drawable.icq_status_depress));
        }

        if (evil == null) {
            evil = normalize(res.getDrawable(R.drawable.icq_status_evil));
        }

        if (bp_divider == null) {
            bp_divider = normalize(res.getDrawable(R.drawable.bottom_panel_divider));
        }

        if (group_closed == null) {
            group_closed = normalize(res.getDrawable(R.drawable.group_closed));
        }

        if (group_opened == null) {
            group_opened = normalize(res.getDrawable(R.drawable.group_opened));
        }

        if (sms == null) {
            sms = normalize(res.getDrawable(R.drawable.icq_msg_in));
        }

        if (msg_in_blink == null) {
            msg_in_blink = normalize(res.getDrawable(R.drawable.icq_msg_in));
        }

        if (msg_in == null) {
            msg_in = normalize(res.getDrawable(R.drawable.icq_msg_in));
        }

        if (msg_out == null) {
            msg_out = normalize(res.getDrawable(R.drawable.icq_msg_out));
        }

        if (msg_out_c == null) {
            msg_out_c = normalize(res.getDrawable(R.drawable.icq_msg_out_confirmed));
        }

        if (typing == null) {
            typing = normalize(res.getDrawable(R.drawable.typing));
        }

        if (cross == null) {
            cross = normalize(res.getDrawable(R.drawable.cross));
        }

        if (unauthorized == null) {
            unauthorized = normalize(res.getDrawable(R.drawable.icq_status_grey));
        }

        if (unauthorized_icon == null) {
            unauthorized_icon = normalize(res.getDrawable(R.drawable.auth_icon));
        }

        if (not_added == null) {
            not_added = normalize(res.getDrawable(R.drawable.not_added));
        }

        if (for_all == null) {
            for_all = normalize(res.getDrawable(R.drawable.icq_private_status_visible_to_all));
        }

        if (for_vl == null) {
            for_vl = normalize(res.getDrawable(R.drawable.icq_private_status_visible_to_white_list));
        }

        if (for_all_e_invl == null) {
            for_all_e_invl = normalize(res.getDrawable(R.drawable.icq_private_status_visible_to_all_exclude_black_list));
        }

        if (for_cl == null) {
            for_cl = normalize(res.getDrawable(R.drawable.icq_private_status_visible_to_contacts_only));
        }

        if (ivn_for_all == null) {
            ivn_for_all = normalize(res.getDrawable(R.drawable.icq_private_status_invisible_for_all));
        }

        if (visible == null) {
            visible = normalize(res.getDrawable(R.drawable.icq_l_vis));
        }

        if (invisible == null) {
            invisible = normalize(res.getDrawable(R.drawable.icq_l_invis));
        }

        if (ignore == null) {
            ignore = normalize(res.getDrawable(R.drawable.icq_l_ignore));
        }

        if (connected == null) {
            connected = normalize(res.getDrawable(R.drawable.connected));
        }

        if (toggle_offline == null) {
            toggle_offline = normalize(res.getDrawable(R.drawable.toggle_offline));
        }

        if (toggle_offline_a == null) {
            toggle_offline_a = normalize(res.getDrawable(R.drawable.toggle_offline_a));
        }

        if (toggle_vibro == null) {
            toggle_vibro = normalize(res.getDrawable(R.drawable.toggle_vibro));
        }

        if (toggle_vibro_a == null) {
            toggle_vibro_a = normalize(res.getDrawable(R.drawable.toggle_vibro_a));
        }

        if (toggle_sound == null) {
            toggle_sound = normalize(res.getDrawable(R.drawable.toggle_sound));
        }

        if (toggle_sound_a == null) {
            toggle_sound_a = normalize(res.getDrawable(R.drawable.toggle_sound_a));
        }

        if (phantom == null) {
            phantom = normalize(res.getDrawable(R.drawable.phantom));
        }

        if (img_file_bad == null) {
            img_file_bad = normalize(res.getDrawable(R.drawable.image_file_invalid));
        }

        if (img_file_big == null) {
            img_file_big = normalize(res.getDrawable(R.drawable.image_file_too_big));
        }

        if (img_file == null) {
            img_file = normalize(res.getDrawable(R.drawable.image_file));
        }

        if (img_file == null) {
            img_file = normalize(res.getDrawable(R.drawable.image_file));
        }

        if (file == null) {
            file = normalize(res.getDrawable(R.drawable.file));
        }

        file_for_chat = file;
        if (inc_file == null) {
            inc_file = normalize(res.getDrawable(R.drawable.incoming_file));
        }

        if (file_transfering == null) {
            file_transfering = normalize(res.getDrawable(R.drawable.file_transfering));
        }

        if (file_brw == null) {
            file_brw = normalize(res.getDrawable(R.drawable.file_browsing));
        }

        if (directory == null) {
            directory = normalize(res.getDrawable(R.drawable.directory ));
        }

        if (directory_up == null) {
            directory_up = normalize(res.getDrawable(R.drawable.browser_back));
        }

        initX();
    }

    private static void initX() {
        res = ctx.getResources();
        if (x_angry == null) {
            x_angry = normalize(res.getDrawable(R.drawable.x_angry));
        }

        if (x_beer == null) {
            x_beer = normalize(res.getDrawable(R.drawable.x_beer));
        }

        if (x_business == null) {
            x_business = normalize(res.getDrawable(R.drawable.x_business));
        }

        if (x_camera == null) {
            x_camera = normalize(res.getDrawable(R.drawable.x_camera));
        }

        if (x_coffe == null) {
            x_coffe = normalize(res.getDrawable(R.drawable.x_coffee));
        }

        if (x_college == null) {
            x_college = normalize(res.getDrawable(R.drawable.x_college));
        }

        if (x_diary == null) {
            x_diary = normalize(res.getDrawable(R.drawable.x_diary));
        }

        if (x_duck == null) {
            x_duck = normalize(res.getDrawable(R.drawable.x_duck));
        }

        if (x_eating == null) {
            x_eating = normalize(res.getDrawable(R.drawable.x_eating));
        }

        if (x_engineering == null) {
            x_engineering = normalize(res.getDrawable(R.drawable.x_engineering));
        }

        if (x_friends == null) {
            x_friends = normalize(res.getDrawable(R.drawable.x_friends));
        }

        if (x_funny == null) {
            x_funny = normalize(res.getDrawable(R.drawable.x_funny));
        }

        if (x_games == null) {
            x_games = normalize(res.getDrawable(R.drawable.x_games));
        }

        if (x_internet == null) {
            x_internet = normalize(res.getDrawable(R.drawable.x_internet));
        }

        if (x_love == null) {
            x_love = normalize(res.getDrawable(R.drawable.x_love));
        }

        if (x_man == null) {
            x_man = normalize(res.getDrawable(R.drawable.x_man));
        }

        if (x_mobile == null) {
            x_mobile = normalize(res.getDrawable(R.drawable.x_mobile));
        }

        if (x_music == null) {
            x_music = normalize(res.getDrawable(R.drawable.x_music));
        }

        if (x_party == null) {
            x_party = normalize(res.getDrawable(R.drawable.x_party));
        }

        if (x_phone == null) {
            x_phone = normalize(res.getDrawable(R.drawable.x_phone));
        }

        if (x_picnic == null) {
            x_picnic = normalize(res.getDrawable(R.drawable.x_picnic));
        }

        if (x_ppc == null) {
            x_ppc = normalize(res.getDrawable(R.drawable.x_ppc));
        }

        if (x_question == null) {
            x_question = normalize(res.getDrawable(R.drawable.x_question));
        }

        if (x_rulove == null) {
            x_rulove = normalize(res.getDrawable(R.drawable.x_rulove));
        }

        if (x_search == null) {
            x_search = normalize(res.getDrawable(R.drawable.x_search));
        }

        if (x_sex == null) {
            x_sex = normalize(res.getDrawable(R.drawable.x_sex));
        }

        if (x_shopping == null) {
            x_shopping = normalize(res.getDrawable(R.drawable.x_shopping));
        }

        if (x_sick == null) {
            x_sick = normalize(res.getDrawable(R.drawable.x_sick));
        }

        if (x_sleep == null) {
            x_sleep = normalize(res.getDrawable(R.drawable.x_sleep));
        }

        if (x_smoke == null) {
            x_smoke = normalize(res.getDrawable(R.drawable.x_smoke));
        }

        if (x_surfing == null) {
            x_surfing = normalize(res.getDrawable(R.drawable.x_surfing));
        }

        if (x_think == null) {
            x_think = normalize(res.getDrawable(R.drawable.x_think));
        }

        if (x_tired == null) {
            x_tired = normalize(res.getDrawable(R.drawable.x_tired));
        }

        if (x_tv == null) {
            x_tv = normalize(res.getDrawable(R.drawable.x_tv));
        }

        if (x_typing == null) {
            x_typing = normalize(res.getDrawable(R.drawable.x_typing));
        }

        if (x_way == null) {
            x_way = normalize(res.getDrawable(R.drawable.x_way));
        }

        if (x_wc == null) {
            x_wc = normalize(res.getDrawable(R.drawable.x_wc));
        }

    }

    public static boolean isTablet() {
        boolean var0 = false;

        double var4;
        try {
            DisplayMetrics var1 = ctx.getResources().getDisplayMetrics();
            float var2 = (float)(var1.widthPixels / var1.densityDpi);
            float var3 = (float)(var1.heightPixels / var1.densityDpi);
            var4 = Math.sqrt(Math.pow((double)var2, 2.0) + Math.pow((double)var3, 2.0));
            StringBuilder var7 = new StringBuilder();
            Log.e("ScreenSize", var7.append(var4).toString());
        } catch (Throwable var6) {
            Log.e("resources", "Failed to compute screen size", var6);
            return var0;
        }

        if (var4 >= 6.0) {
            var0 = true;
        }

        return var0;
    }

    public static void loadGraphics() {
        if (sd_mounted()) {
            away_text = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/away_text_icon.png"));
            mrim_online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_online.png"));
            mrim_connecting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_connecting.png"));
            mrim_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_offline.png"));
            mrim_away = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_away.png"));
            mrim_home = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_home.png"));
            mrim_work = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_work.png"));
            mrim_dnd = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_dnd.png"));
            mrim_angry = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_angry.png"));
            mrim_chat = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_chat.png"));
            mrim_lunch = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_lunch.png"));
            mrim_na = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_na.png"));
            mrim_oc = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_oc.png"));
            mrim_depress = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_mrim_depress.png"));
            mrim_wakeup = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/mrim_wakeup.png"));
            profile_tools = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/profile_tools.png"));
            google_mail = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/google_mail_icon.png"));
            odnoclass_online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_gtalk_online.png"));
            odnoclass_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_gtalk_online.png"));
            gtalk_online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_gtalk_online.png"));
            gtalk_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_gtalk_offline.png"));
            gtalk_connecting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_gtalk_connecting.png"));
            qip_online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_qip_online.png"));
            qip_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_qip_offline.png"));
            qip_connecting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_qip_connecting.png"));
            chat_menu_icon = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/chat_menu_btn.png"));
            smileys_select_icon = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/smileys_select_icon.png"));
            send_msg_icon = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/send_msg_icon.png"));
            back_to_cl_icon = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/back_to_cl_icon.png"));
            marker_chat = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/marker_chat.png"));
            marker_active_chat = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/marker_active_chat.png"));
            marker_msg_chat = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/marker_msg_chat.png"));
            tab_highlight = Drawable.createFromPath(JASMINE_SD_PATH + "Skin/tab_highlight.png");
            link_icon = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/link_icon.png"));
            bookmarks = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/bookmarks.png"));
            jabber_server = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_server.png"));
            jabber_command = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_command.png"));
            jabber_waiting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_waiting.png"));
            jabber_error = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_error.png"));
            jabber_disco = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_disco.png"));
            jabber_xml_console = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_xml_console.png"));
            jabber_priority = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_priority.png"));
            jabber_conf_pm = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conf_pm.png"));
            jabber_conf_owner = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conf_owner.png"));
            jabber_conf_admin = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conf_admin.png"));
            jabber_conf_moderator = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conf_moderator.png"));
            jabber_conf_visitor = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conf_visitor.png"));
            jabber_conf_member = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conf_member.png"));
            jabber_conf_outcast = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conf_outcast.png"));
            jabber_conference_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conference_offline.png"));
            jabber_conference = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/jabber_conference.png"));
            url_icon = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/url_icon"));
            jabber_online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_online.png"));
            jabber_away = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_away.png"));
            jabber_oc = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_oc.png"));
            jabber_dnd = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_dnd.png"));
            jabber_na = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_na.png"));
            jabber_chat = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_chat.png"));
            jabber_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_offline.png"));
            jabber_connecting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_jabber_connecting.png"));
            vk_online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_vk_online.png"));
            vk_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_vk_offline.png"));
            vk_connecting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_vk_connecting.png"));
            yandex_online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_yandex_online.png"));
            yandex_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_yandex_offline.png"));
            yandex_connecting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_yandex_connecting.png"));
            online = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_online.png"));
            offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_offline.png"));
            connecting = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_connecting.png"));
            away = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_away.png"));
            oc = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_oc.png"));
            dnd = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_dnd.png"));
            na = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_na.png"));
            home = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_home.png"));
            chat = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_chat.png"));
            eat = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_eat.png"));
            work = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_work.png"));
            depress = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_depress.png"));
            evil = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_evil.png"));
            bp_divider = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/bottom_panel_divider.png"));
            group_closed = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/group_closed.png"));
            group_opened = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/group_opened.png"));
            sms = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sms_message.png"));
            msg_in_blink = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/incoming_message.png"));
            msg_in = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/incoming_message.png"));
            msg_out = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/outgoing_message.png"));
            msg_out_c = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/outgoing_message_confirmed.png"));
            typing = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/typing.png"));
            cross = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/red_cross.png"));
            unauthorized = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_grey.png"));
            unauthorized_icon = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/auth_flag.png"));
            not_added = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_not_added.png"));
            for_all = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/vis_for_all.png"));
            for_vl = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/vis_for_vl.png"));
            for_all_e_invl = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/vis_for_all_e_invl.png"));
            for_cl = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/vis_for_cl.png"));
            ivn_for_all = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/ivn_for_all.png"));
            visible = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/visible.png"));
            invisible = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/invisible.png"));
            ignore = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/ignore.png"));
            toggle_offline = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/toggle_offline_0.png"));
            toggle_offline_a = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/toggle_offline_1.png"));
            toggle_vibro = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/toggle_vibro_0.png"));
            toggle_vibro_a = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/toggle_vibro_1.png"));
            toggle_sound = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/toggle_sound_0.png"));
            toggle_sound_a = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/toggle_sound_1.png"));
            phantom = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/sts_phantom.png"));
            img_file_bad = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/image_file_invalid.png"));
            img_file_big = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/image_file_too_big.png"));
            img_file = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/image_file.png"));
            file = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/file.png"));
            inc_file = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/inc_file.png"));
            file_transfering = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/file_transfering.png"));
            directory = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/browser_directory.png"));
            directory_up = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/browser_directory_up.png"));
            file_brw = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/browser_file.png"));
            custom_wallpaper = Drawable.createFromPath(JASMINE_SD_PATH + "Skin/background.png");
            x_angry = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_angry.png"));
            x_beer = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_beer.png"));
            x_business = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_business.png"));
            x_camera = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_camera.png"));
            x_coffe = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_coffe.png"));
            x_college = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_college.png"));
            x_diary = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_diary.png"));
            x_duck = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_duck.png"));
            x_eating = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_eating.png"));
            x_engineering = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_engineering.png"));
            x_friends = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_friends.png"));
            x_funny = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_funny.png"));
            x_games = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_games.png"));
            x_internet = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_internet.png"));
            x_love = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_love.png"));
            x_man = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_man.png"));
            x_mobile = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_mobile.png"));
            x_music = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_music.png"));
            x_party = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_party.png"));
            x_phone = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_phone.png"));
            x_picnic = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_picnic.png"));
            x_ppc = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_ppc.png"));
            x_question = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_question.png"));
            x_rulove = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_rulove.png"));
            x_search = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_search.png"));
            x_sex = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_sex.png"));
            x_shopping = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_shopping.png"));
            x_sick = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_sick.png"));
            x_sleep = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_sleep.png"));
            x_smoke = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_smoke.png"));
            x_surfing = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_surfing.png"));
            x_think = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_think.png"));
            x_tired = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_tired.png"));
            x_tv = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_tv.png"));
            x_typing = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_typing.png"));
            x_way = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_way.png"));
            x_wc = normalize(Drawable.createFromPath(JASMINE_SD_PATH + "Skin/x_wc.png"));
            incoming_message_back_padding = new Rect();
            outgoing_message_back_padding = new Rect();
            auth_ask_message_back_padding = new Rect();
            auth_accepted_message_back_padding = new Rect();
            auth_denied_message_back_padding = new Rect();
            status_message_back_padding = new Rect();
            transfer_message_back_padding = new Rect();
            contactlist_items_back_padding = new Rect();
            chat_messages_back_padding = new Rect();
            popup_back_padding = new Rect();
            status_selector_back_padding = new Rect();
            dialogs_back_padding = new Rect();
            contactlist_item_normal = BitmapFactory.decodeFile(JASMINE_SD_PATH + "Skin/contact_list_contact_back.png");
            contactlist_group_normal = BitmapFactory.decodeFile(JASMINE_SD_PATH + "Skin/contact_list_group_back.png");
            contactlist_item_selected = BitmapFactory.decodeFile(JASMINE_SD_PATH + "Skin/contact_list_item_selected.png");
            away_text_back = BitmapFactory.decodeFile(JASMINE_SD_PATH + "Skin/away_text_back.png");
            contactlist_bottom_panel_back = BitmapFactory.decodeFile(JASMINE_SD_PATH + "Skin/contactlist_bottom_panel_back.png");
            contactlist_bottom_panel_connection_back = BitmapFactory.decodeFile(JASMINE_SD_PATH + "Skin/contactlist_bottom_panel_connection_back.png");
            status_selector_arrow = BitmapFactory.decodeFile(JASMINE_SD_PATH + "Skin/status_selector_arrow.png");
            BitmapFactory.Options var0 = new BitmapFactory.Options();

            FileInputStream var1;
            File var2;
            StringBuilder var3;
            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var3.append("Skin/slide_switcher_panel.png").toString());
                var1 = new FileInputStream(var2);
                slide_switcher_panel = BitmapFactory.decodeFileDescriptor(var1.getFD(), slide_switcher_panel_padding, var0);
                var1.close();
            } catch (Exception var31) {
            }

            File var32;
            FileInputStream var33;
            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var3.append("Skin/dialogs_back.png").toString());
                var33 = new FileInputStream(var32);
                dialogs_back = BitmapFactory.decodeFileDescriptor(var33.getFD(), dialogs_back_padding, var0);
                var33.close();
            } catch (Exception var30) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var3.append("Skin/textfield_normal.png").toString());
                var1 = new FileInputStream(var2);
                edt_normal = BitmapFactory.decodeFileDescriptor(var1.getFD(), edt_normal_padding, var0);
                var1.close();
            } catch (Exception var29) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var3.append("Skin/textfield_disabled.png").toString());
                var33 = new FileInputStream(var32);
                edt_disabled = BitmapFactory.decodeFileDescriptor(var33.getFD(), edt_disabled_padding, var0);
                var33.close();
            } catch (Exception var28) {
            }

            StringBuilder var34;
            File var36;
            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var36 = new File(var34.append("Skin/textfield_disabled_focused.png").toString());
                var1 = new FileInputStream(var36);
                edt_disabled_focused = BitmapFactory.decodeFileDescriptor(var1.getFD(), edt_disabled_focused_padding, var0);
                var1.close();
            } catch (Exception var27) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var3.append("Skin/textfield_pressed.png").toString());
                var1 = new FileInputStream(var2);
                edt_pressed = BitmapFactory.decodeFileDescriptor(var1.getFD(), edt_pressed_padding, var0);
                var1.close();
            } catch (Exception var26) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var3.append("Skin/textfield_selected.png").toString());
                var33 = new FileInputStream(var32);
                edt_selected = BitmapFactory.decodeFileDescriptor(var33.getFD(), edt_selected_padding, var0);
                var33.close();
            } catch (Exception var25) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var36 = new File(var34.append("Skin/check_on.png").toString());
                var1 = new FileInputStream(var36);
                check_on = BitmapFactory.decodeFileDescriptor(var1.getFD(), (Rect)null, var0);
                var1.close();
            } catch (Exception var24) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var36 = new File(var34.append("Skin/check_off.png").toString());
                var1 = new FileInputStream(var36);
                check_off = BitmapFactory.decodeFileDescriptor(var1.getFD(), (Rect)null, var0);
                var1.close();
            } catch (Exception var23) {
            }

            StringBuilder var35;
            FileInputStream var37;
            try {
                var35 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var35.append("Skin/contactlist_chat_divider.png").toString());
                var37 = new FileInputStream(var2);
                contactlist_chat_divider = BitmapFactory.decodeFileDescriptor(var37.getFD(), contactlist_chat_divider_rect, var0);
                var37.close();
            } catch (Exception var22) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var34.append("Skin/btn_normal.png").toString());
                var37 = new FileInputStream(var32);
                btn_normal = BitmapFactory.decodeFileDescriptor(var37.getFD(), btn_normal_padding, var0);
                var37.close();
            } catch (Exception var21) {
            }

            try {
                var35 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var35.append("Skin/btn_disabled.png").toString());
                var37 = new FileInputStream(var2);
                btn_disabled = BitmapFactory.decodeFileDescriptor(var37.getFD(), btn_disabled_padding, var0);
                var37.close();
            } catch (Exception var20) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var34.append("Skin/btn_disabled_focused.png").toString());
                var37 = new FileInputStream(var32);
                btn_disabled_focused = BitmapFactory.decodeFileDescriptor(var37.getFD(), btn_disabled_focused_padding, var0);
                var37.close();
            } catch (Exception var19) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var3.append("Skin/btn_pressed.png").toString());
                var33 = new FileInputStream(var32);
                btn_pressed = BitmapFactory.decodeFileDescriptor(var33.getFD(), btn_pressed_padding, var0);
                var33.close();
            } catch (Exception var18) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var34.append("Skin/btn_selected.png").toString());
                var37 = new FileInputStream(var32);
                btn_selected = BitmapFactory.decodeFileDescriptor(var37.getFD(), btn_selected_padding, var0);
                var37.close();
            } catch (Exception var17) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var3.append("Skin/contactlist_items_back.png").toString());
                var1 = new FileInputStream(var2);
                contactlist_items_back = BitmapFactory.decodeFileDescriptor(var1.getFD(), contactlist_items_back_padding, var0);
                var1.close();
            } catch (Exception var16) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var34.append("Skin/status_selector_back.png").toString());
                var37 = new FileInputStream(var32);
                status_selector_back = BitmapFactory.decodeFileDescriptor(var37.getFD(), status_selector_back_padding, var0);
                var37.close();
            } catch (Exception var15) {
            }

            Rect var38;
            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var34.append("Skin/chat_top_panel.png").toString());
                var37 = new FileInputStream(var32);
                chat_top_panel = BitmapFactory.decodeFileDescriptor(var37.getFD(), chat_top_panel_padding, var0);
                if (chat_top_panel_padding == null) {
                    var38 = new Rect();
                    chat_top_panel_padding = var38;
                }

                var37.close();
            } catch (Exception var14) {
            }

            try {
                var35 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var35.append("Skin/chat_bottom_panel.png").toString());
                var37 = new FileInputStream(var2);
                chat_bottom_panel = BitmapFactory.decodeFileDescriptor(var37.getFD(), chat_bottom_panel_padding, var0);
                if (chat_bottom_panel_padding == null) {
                    var38 = new Rect();
                    chat_bottom_panel_padding = var38;
                }

                var37.close();
            } catch (Exception var13) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var3.append("Skin/chat_messages_back.png").toString());
                var1 = new FileInputStream(var2);
                chat_messages_back = BitmapFactory.decodeFileDescriptor(var1.getFD(), chat_messages_back_padding, var0);
                var1.close();
            } catch (Exception var12) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var36 = new File(var34.append("Skin/transfer_message_back.png").toString());
                var1 = new FileInputStream(var36);
                transfer_message_back = BitmapFactory.decodeFileDescriptor(var1.getFD(), transfer_message_back_padding, var0);
                var1.close();
            } catch (Exception var11) {
            }

            try {
                var35 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var36 = new File(var35.append("Skin/status_message_back.png").toString());
                var33 = new FileInputStream(var36);
                status_message_back = BitmapFactory.decodeFileDescriptor(var33.getFD(), status_message_back_padding, var0);
                var33.close();
            } catch (Exception var10) {
            }

            try {
                var35 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var35.append("Skin/incoming_message_back.png").toString());
                var37 = new FileInputStream(var2);
                incoming_message_back = BitmapFactory.decodeFileDescriptor(var37.getFD(), incoming_message_back_padding, var0);
                var37.close();
            } catch (Exception var9) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var36 = new File(var34.append("Skin/outgoing_message_back.png").toString());
                var1 = new FileInputStream(var36);
                outgoing_message_back = BitmapFactory.decodeFileDescriptor(var1.getFD(), outgoing_message_back_padding, var0);
                var1.close();
            } catch (Exception var8) {
            }

            try {
                var34 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var34.append("Skin/auth_ask_message_back.png").toString());
                var37 = new FileInputStream(var32);
                auth_ask_message_back = BitmapFactory.decodeFileDescriptor(var37.getFD(), auth_ask_message_back_padding, var0);
                var37.close();
            } catch (Exception var7) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var3.append("Skin/auth_accepted_message_back.png").toString());
                var33 = new FileInputStream(var32);
                auth_accepted_message_back = BitmapFactory.decodeFileDescriptor(var33.getFD(), auth_accepted_message_back_padding, var0);
                var33.close();
            } catch (Exception var6) {
            }

            try {
                var35 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var2 = new File(var35.append("Skin/auth_denied_message_back.png").toString());
                var37 = new FileInputStream(var2);
                auth_denied_message_back = BitmapFactory.decodeFileDescriptor(var37.getFD(), auth_denied_message_back_padding, var0);
                var37.close();
            } catch (Exception var5) {
            }

            try {
                var3 = new StringBuilder(String.valueOf(JASMINE_SD_PATH));
                var32 = new File(var3.append("Skin/popup_back.png").toString());
                var33 = new FileInputStream(var32);
                popup_back = BitmapFactory.decodeFileDescriptor(var33.getFD(), popup_back_padding, var0);
                var33.close();
            } catch (Exception var4) {
            }
        }

    }

    public static Drawable mutateDrawableA(Drawable var0) {
        Bitmap var2 = ((BitmapDrawable)var0).getBitmap();
        BitmapDrawable var1 = new BitmapDrawable(var2);
        var1.setBounds(0, 0, var2.getWidth(), var2.getHeight());
        return var1;
    }

    public static Drawable normalize(Drawable var0) {
        ru.ivansuper.jasmin.BitmapDrawable var1;
        if (var0 == null) {
            var1 = null;
        } else {
            Bitmap var2 = ((BitmapDrawable)var0).getBitmap().copy(Config.ARGB_4444, false);
            var2.setDensity(0);
            var1 = new ru.ivansuper.jasmin.BitmapDrawable(var2);
            var1.setBounds(0, 0, var1.getIntrinsicWidth(), var1.getIntrinsicHeight());
        }

        return var1;
    }

    public static Drawable normalizeIconDPIBased(Drawable var0) {
        ru.ivansuper.jasmin.BitmapDrawable var2;
        if (var0 == null) {
            var2 = null;
        } else {
            Bitmap var1 = ((BitmapDrawable)var0).getBitmap();
            Bitmap var3;
            switch (ctx.getResources().getDisplayMetrics().densityDpi) {
                case 120:
                    var3 = Bitmap.createScaledBitmap(var1, 16, 16, true);
                    break;
                case 160:
                    var3 = Bitmap.createScaledBitmap(var1, 24, 24, true);
                    break;
                case 240:
                    var3 = Bitmap.createScaledBitmap(var1, 32, 32, true);
                    break;
                default:
                    var3 = Bitmap.createScaledBitmap(var1, var0.getIntrinsicWidth(), var0.getIntrinsicHeight(), true);
            }

            var3 = var3.copy(Config.ARGB_4444, false);
            var3.setDensity(0);
            var2 = new ru.ivansuper.jasmin.BitmapDrawable(ctx.getResources(), var3);
            var2.setBounds(0, 0, var2.getIntrinsicWidth(), var2.getIntrinsicHeight());
        }

        return var2;
    }

    public static void prerareInterface() {
        if (!PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("skin_loaded", false) && sd_mounted()) {
            if (dm.widthPixels < 400) {
                PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean("skin_loaded", true).commit();
            } else {
                copyAssetToSD("Interface/auth_accepted_message_back.png", "auth_accepted_message_back.png");
                copyAssetToSD("Interface/auth_ask_message_back.png", "auth_ask_message_back.png");
                copyAssetToSD("Interface/auth_denied_message_back.png", "auth_denied_message_back.png");
                copyAssetToSD("Interface/away_text_back.png", "away_text_back.png");
                copyAssetToSD("Interface/chat_bottom_panel.png", "chat_bottom_panel.png");
                copyAssetToSD("Interface/chat_messages_back.png", "chat_messages_back.png");
                copyAssetToSD("Interface/chat_top_panel.png", "chat_top_panel.png");
                copyAssetToSD("Interface/contact_list_contact_back.png", "contact_list_contact_back.png");
                copyAssetToSD("Interface/contact_list_group_back.png", "contact_list_group_back.png");
                copyAssetToSD("Interface/contactlist_bottom_panel_back.png", "contactlist_bottom_panel_back.png");
                copyAssetToSD("Interface/contactlist_bottom_panel_connection_back.png", "contactlist_bottom_panel_connection_back.png");
                copyAssetToSD("Interface/contactlist_items_back.png", "contactlist_items_back.png");
                copyAssetToSD("Interface/incoming_message_back.png", "incoming_message_back.png");
                copyAssetToSD("Interface/outgoing_message_back.png", "outgoing_message_back.png");
                copyAssetToSD("Interface/status_message_back.png", "status_message_back.png");
                copyAssetToSD("Interface/status_selector_arrow.png", "status_selector_arrow.png");
                copyAssetToSD("Interface/status_selector_back.png", "status_selector_back.png");
                copyAssetToSD("Interface/transfer_message_back.png", "transfer_message_back.png");
                copyAssetToSD("Interface/textfield_disabled_focused.png", "textfield_disabled_focused.png");
                copyAssetToSD("Interface/textfield_disabled.png", "textfield_disabled.png");
                copyAssetToSD("Interface/textfield_normal.png", "textfield_normal.png");
                copyAssetToSD("Interface/textfield_pressed.png", "textfield_pressed.png");
                copyAssetToSD("Interface/textfield_selected.png", "textfield_selected.png");
                copyAssetToSD("Interface/btn_disabled_focused.png", "btn_disabled_focused.png");
                copyAssetToSD("Interface/btn_disabled.png", "btn_disabled.png");
                copyAssetToSD("Interface/btn_normal.png", "btn_normal.png");
                copyAssetToSD("Interface/btn_pressed.png", "btn_pressed.png");
                copyAssetToSD("Interface/btn_selected.png", "btn_selected.png");
                ColorScheme.fillFromAsset();
                ColorScheme.saveToInternalFile();
                PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean("skin_loaded", true).commit();
            }
        }

    }

    @SuppressLint("WrongConstant")
    public static void putContext(Context var0) {
        boolean var1 = false;
        if (ctx == null) {
            DEVICE_HEAP_SIZE = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
            DEVICE_HEAP_USED_SIZE = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L;
            ctx = var0;
            IT_IS_TABLET = isTablet();
            ApplicationInfo var13 = null;

            ApplicationInfo var2;
            label83: {
                try {
                    var2 = ctx.getPackageManager().getApplicationInfo("ru.ivansuper.jasmindonate", 128);
                } catch (PackageManager.NameNotFoundException var12) {
                    break label83;
                }

                var13 = var2;
            }

            boolean var3 = false;
            boolean var4 = false;
            if (var13 != null) {
                var3 = true;
            }

            var13 = null;

            label76: {
                try {
                    var2 = ctx.getPackageManager().getApplicationInfo("ru.ivansuper.jasmindonatesmall", 128);
                } catch (PackageManager.NameNotFoundException var11) {
                    break label76;
                }

                var13 = var2;
            }

            if (var13 != null) {
                var4 = true;
            }

            if (var3 || var4) {
                var1 = true;
            }

            DONATE_INSTALLED = var1;

            try {
                VERSION = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
            } catch (Exception var10) {
                var10.printStackTrace();
            }

            try {
                StringBuilder var14 = new StringBuilder(String.valueOf(ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), 0).dataDir));
                dataPath = var14.append("/").toString();
            } catch (PackageManager.NameNotFoundException var9) {
                var9.printStackTrace();
            }

            am = ctx.getAssets();
            Locale.prepare();
            OS_VERSION = Integer.parseInt(android.os.Build.VERSION.SDK);
            SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            JASMINE_SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Jasmine/";
            JASMINE_INCOMING_FILES_PATH = JASMINE_SD_PATH + "RcvdFiles/";
            JASMINE_JHA_PATH = JASMINE_SD_PATH + "Jasmine History Archive/";
            JASMINE_SKIN_PATH = JASMINE_SD_PATH + "Skin/";
            JASMINE = ctx.getPackageManager();
            OS_VERSION_STR = android.os.Build.VERSION.RELEASE;
            DEVICE_STR = utilities.compute(Build.BRAND, Build.MODEL);
            SOFTWARE_STR = Build.ID;

            File var15;
            try {
                var15 = new File(JASMINE_INCOMING_FILES_PATH);
                if (!var15.exists()) {
                    var15.mkdirs();
                }
            } catch (Exception var8) {
                var8.printStackTrace();
            }

            try {
                var15 = new File(JASMINE_SD_PATH);
                if (!var15.exists()) {
                    var15.mkdirs();
                }
            } catch (Exception var7) {
                var7.printStackTrace();
            }

            try {
                var15 = new File(JASMINE_JHA_PATH);
                if (!var15.exists()) {
                    var15.mkdirs();
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }

            try {
                var15 = new File(JASMINE_SKIN_PATH);
                if (!var15.exists()) {
                    var15.mkdirs();
                }
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            res = ctx.getResources();
            loadGraphics();
            initInternalGraphics();
            res = null;
            System.gc();
        }

    }

    public static boolean sd_mounted() {
        boolean var0;
        if (Environment.getExternalStorageState().equals("mounted")) {
            var0 = true;
        } else {
            var0 = false;
        }

        return var0;
    }

    protected void finalize() throws Throwable {
        throw new RuntimeException("Canceling finalization of resources class");
    }
}
