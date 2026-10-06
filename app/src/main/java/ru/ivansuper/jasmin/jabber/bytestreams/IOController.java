package ru.ivansuper.jasmin.jabber.bytestreams;

import ru.ivansuper.jasmin.jabber.JProfile;

public class IOController {
    private OnEventListener listener;
    public String partner_jid;
    public JProfile profile;
    protected State state;

    public interface OnEventListener {
        void OnData(byte[] bArr, int i);

        void onStateChanged(State state);
    }

    public enum State {
        WAIT,
        HANDSHAKE,
        WORKING,
        CANCELED,
        CLOSED,
        ERROR;

        public static State[] valuesCustom() {
            State[] stateArrValuesCustom = values();
            int length = stateArrValuesCustom.length;
            State[] stateArr = new State[length];
            System.arraycopy(stateArrValuesCustom, 0, stateArr, 0, length);
            return stateArr;
        }
    }

    public void cancel() {
    }

    public void close() {
    }

    protected final void notifyListenerData(byte[] bArr, int i) {
        if (this.listener != null) {
            this.listener.OnData(bArr, i);
        }
    }

    protected final void notifyListenerState() {
        if (this.listener != null) {
            this.listener.onStateChanged(this.state);
        }
    }

    public void open() {
    }

    public final void removeListener() {
        this.listener = null;
    }

    public final void setEventListener(OnEventListener onEventListener) {
        this.listener = onEventListener;
    }

    public String write(byte[] bArr) {
        return "";
    }

    public String write(byte[] bArr, int i) {
        return "";
    }
}
