package ru.ivansuper.jasmin.icq;

import android.graphics.drawable.Drawable;

import java.util.Objects;

import ru.ivansuper.jasmin.resources;

public class xstatus {
    public static final String[] guids = {"63627337A03F49FF80E5F709CDE0A4EE", "5A581EA1E580430CA06F612298B7E4C7", "83C9B78E77E74378B2C5FB6CFCC35BEC", "E601E41C33734BD1BC06811D6C323D81", "8C50DBAE81ED4786ACCA16CC3213C7B7", "3FB0BD36AF3B4A609EEFCF190F6A5A7F", "F8E8D7B282C4414290F810C6CE0A89A6", "80537DE2A4674A76B3546DFD075F5EC6", "F18AB52EDC57491D99DC6444502457AF", "1B78AE31FA0B4D3893D1997EEEAFB218", "61BEE0DD8BDD475D8DEE5F4BAACF19A7", "488E14898ACA4A0882AA77CE7A165208", "107A9A1812324DA4B6CD0879DB780F09", "6F4930984F7C4AFFA27634A03BCEAEA7", "1292E5501B644F66B206B29AF378E48D", "D4A611D08F014EC09223C5B6BEC6CCF0", "609D52F8A29A49A6B2A02524C5E9D260", "1F7A4071BF3B4E60BC324C5787B04CF1", "785E8C4840D34C65886F04CF3F3F43DF", "A6ED557E6BF744D4A5D4D2E7D95CE81F", "12D07E3EF885489E8E97A72A6551E58D", "BA74DB3E9E24434B87B62F6B8DFEE50F", "634F6BD8ADD24AA1AAB9115BC26D05A1", "01D8D7EEAC3B492AA58DD3D877E66B92", "2CE0E4E57C6443709C3A7A1CE878A7DC", "101117C9A3B040F981AC49E159FBD5D4", "160C60BBDD4443F39140050F00E6C009", "6443C6AF22604517B58CD7DF8E290352", "16F5B76FA9D240358CC5C084703C98FA", "631436FF3F8A40D0A5CB7B66E051B364", "B70867F538254327A1FFCF4CC1939797", "DDCF0EA971954048A9C6413206D6F280", "3FB0BD36AF3B4A609EEFCF190F6A5A7E", "E601E41C33734BD1BC06811D6C323D82", "D4E2B0BA334E4FA598D0117DBF4D3CC8", "0072D9084AD143DD91996F026966026F", "CD5643A2C94C4724B52CDC0124A1D0CD"};
    public static final Drawable[] icons = {resources.x_shopping, resources.x_duck, resources.x_tired, resources.x_party, resources.x_beer, resources.x_think, resources.x_eating, resources.x_tv, resources.x_friends, resources.x_coffe, resources.x_music, resources.x_business, resources.x_camera, resources.x_funny, resources.x_phone, resources.x_games, resources.x_college, resources.x_sick, resources.x_sleep, resources.x_surfing, resources.x_internet, resources.x_engineering, resources.x_typing, resources.x_angry, resources.x_picnic, resources.x_ppc, resources.x_mobile, resources.x_man, resources.x_wc, resources.x_question, resources.x_way, resources.x_love, resources.x_smoke, resources.x_sex, resources.x_search, resources.x_diary, resources.x_rulove};

    public static Drawable getIcon(String guid) {
        for (int i = 0; i < guids.length; i++) {
            if (guid.equals(guids[i])) {
                return icons[i];
            }
        }
        return null;
    }

    /** @noinspection unused*/
    public static int getIdx(String guid) {
        for (int i = 0; i < guids.length; i++) {
            if (guid.equals(guids[i])) {
                return i;
            }
        }
        return -1;
    }

    public static String makeXPromt(String promt) {
        StringBuilder sb = new StringBuilder((promt.length() * 3) + 1);
        for (int i = 0; i < promt.length(); i++) {
            char ch = promt.charAt(i);
            if ('<' == ch) {
                sb.append("&lt;");
            } else if ('>' == ch) {
                sb.append("&gt;");
            } else if ('&' == ch) {
                sb.append("&amp;");
            } else if ('\'' == ch) {
                sb.append("&amp;");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String getText(String text) {
        String oldText = null;
        while (!Objects.equals(oldText, text)) {
            oldText = text;
            text = text.replace("&gt;", ">");
        }
        String oldText2 = null;
        while (!Objects.equals(oldText2, text)) {
            oldText2 = text;
            text = text.replace("&lt;", "<");
        }
        String oldText3 = null;
        while (!Objects.equals(oldText3, text)) {
            oldText3 = text;
            text = text.replace("&amp;", "&");
        }
        String oldText4 = null;
        while (!Objects.equals(oldText4, text)) {
            oldText4 = text;
            text = text.replace("&apos;", "'");
        }
        return text;
    }

    public static String getTagContent(String xml, String tag) {
        int offset = tag.length() + 2;
        int begin = xml.indexOf("<" + tag + ">");
        int end = xml.indexOf("</" + tag + ">");
        return (begin < 0 || end <= begin) ? "" : xml.substring(begin + offset, end);
    }
}
