package ru.ivansuper.jasmin.jabber.juick;

import android.text.style.ClickableSpan;
import android.view.View;

public class JuickIDSpan extends ClickableSpan {
    private String data;
    private TextParser.OnIDClickedListener listener;
    private Type type;

    public enum Type {
        User,
        Message;

        public static Type[] valuesCustom() {
            Type[] typeArrValuesCustom = values();
            int length = typeArrValuesCustom.length;
            Type[] typeArr = new Type[length];
            System.arraycopy(typeArrValuesCustom, 0, typeArr, 0, length);
            return typeArr;
        }
    }

    public JuickIDSpan(String str, TextParser.OnIDClickedListener onIDClickedListener, Type type) {
        this.data = str;
        this.listener = onIDClickedListener;
        this.type = type;
    }

    @Override
    public void onClick(View view) {
        if (this.type == Type.Message) {
            if (this.listener != null) {
                this.listener.OnMessageClicked(this.data);
            }
        } else {
            if (this.type != Type.User || this.listener == null) {
                return;
            }
            this.listener.OnUserClicked(this.data);
        }
    }
}
