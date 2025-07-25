package ru.ivansuper.jasmin.icq;

import android.graphics.drawable.Drawable;
import android.util.Log;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.resources;

/**
 * Represents a container for user information, including personal details and avatar.
 * This class is used to store and manage data related to an ICQ user.
 */
public class InfoContainer {
    public int age;
    public Drawable avatar;
    public int birthday;
    public int birthmonth;
    public int birthyear;
    public Callback callback;
    public Callback redirect;
    public String uin = "";
    public String nickname = "";
    public String name = "";
    public String surname = "";
    public String city = "";
    public String email = "";
    public String sex = "";
    public String homepage = "";
    public String about = "";
    public int sex_ = 0;

    public void initAvatar() {
        this.redirect = null;
        this.avatar = resources.ctx.getResources().getDrawable(R.drawable.no_avatar);
        this.callback = new Callback() {
            @Override
            public void notify(Object object, int args) {
                if (InfoContainer.this.redirect != null) {
                    InfoContainer.this.redirect.notify(object, args);
                    return;
                }
                InfoContainer.this.avatar = (Drawable) object;
                Log.e("Callback", "Handled in info");
            }
        };
    }

    public void setRedirect(Callback callback) {
        this.redirect = callback;
    }
}
