package ru.ivansuper.jasmin.chats;

import android.util.Log;

import java.util.Vector;

public class MessageSaveHelper {
    private static final Vector<Message> mStack = new Vector<>();

    public static class Message {
        public String id;
        public String message;
    }

    public static synchronized void putMessage(String id, String message) {
        synchronized (MessageSaveHelper.class) {
            Log.e("MessageSaveHelper", "Saving: " + message + "\n(" + id + ")");
            Message state = null;
            for (Message s : mStack) {
                if (s.id.equals(id)) {
                    state = s;
                    break;
                }
            }
            if (state == null) {
                Message state2 = new Message();
                state2.id = id;
                state2.message = message;
                mStack.add(state2);
            } else {
                state.message = message;
            }
        }
    }

    public static synchronized String getMessage(String id) {
        synchronized (MessageSaveHelper.class) {
            Log.e("MessageSaveHelper", "getMessage (" + id + ")");
            for (Message s : mStack) {
                if (s.id.equals(id)) {
                    return s.message;
                }
            }
            return "";
        }
    }
}
