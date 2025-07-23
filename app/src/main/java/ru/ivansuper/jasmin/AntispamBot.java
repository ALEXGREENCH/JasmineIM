package ru.ivansuper.jasmin;

import android.preference.PreferenceManager;
import android.util.Log;

import ru.ivansuper.jasmin.protocols.IMProfile;

/**
 * Utility class for handling anti-spam measures.
 * This class provides methods to check user responses to anti-spam questions,
 * retrieve anti-spam questions and acceptance messages, and check if anti-spam is enabled.
 * <p>
 * The anti-spam mechanism works as follows:
 * <ul>
 *     <li>When a user sends a message, their ID is checked against a ban list.</li>
 *     <li>If the user is not banned, they are presented with an anti-spam question.</li>
 *     <li>The user's response is then checked against a list of correct answers.</li>
 *     <li>If the response is correct, the user is removed from the ban list and allowed to send messages.</li>
 *     <li>If the response is incorrect, the user's attempt count is incremented. If the attempt count exceeds a threshold (3 attempts), the user is banned.</li>
 * </ul>
 * <p>
 * The anti-spam questions, acceptance messages, and enabled status are configurable through shared preferences.
 */
public class AntispamBot {

    public static final int BANNED = 0;
    public static final int NEED_QUEST = 1;
    public static final int ACCEPTED = 2;

    /**
     * Проверяет ответ пользователя на антиспам-вопрос.
     *
     * @param id      — идентификатор пользователя
     * @param message — сообщение от пользователя (предположительно ответ)
     * @param profile — профиль, содержащий настройки и банлист
     * @return {@link #ACCEPTED}, {@link #NEED_QUEST} или {@link #BANNED}
     */
    public static synchronized int checkQuestion(String id, String message, IMProfile profile) {
        synchronized (AntispamBot.class) {
            // Увеличиваем счётчик попыток пользователя
            IMProfile.BanList.Item item = profile.banlist.get(id);
            if (item != null) {
                if (item.tryes < 3) {
                    profile.banlist.increase(id);
                }
            } else {
                profile.banlist.increase(id);
            }

            // Получаем правильные ответы на вопрос
            String answer = profile.svc.getAntispamAnswer(); // например: "russia,россия"
            String[] variants = answer.split(",");

            // Сравниваем ответы
            for (String variant : variants) {
                if (message.trim().equalsIgnoreCase(variant.trim())) {
                    // Успешный ответ — удаляем из банлиста
                    Log.e("AntispamBot", "User: " + id + " | accepted");
                    profile.banlist.remove(id);
                    return ACCEPTED;
                }
            }

            // Неверный ответ, но ещё есть попытки
            return NEED_QUEST;
        }
    }

    /**
     * Returns the antispam question (with translation).
     * The question is retrieved from SharedPreferences. If not found, a default question is used.
     * The default question is: "[EN] Antispam: What is the biggest country in the world?_[RU] Антиспам: Самая большая по площади страна в мире?"
     * The "_" character is replaced with a newline character.
     * @return The antispam question.
     * @noinspection unused
     */
    public static String getQuestion() {
        //noinspection deprecation,DataFlowIssue
        return PreferenceManager.getDefaultSharedPreferences(resources.ctx)
                .getString("antispam_question",
                        "[EN] Antispam: What is the biggest country in the world?_" +
                                "[RU] Антиспам: Самая большая по площади страна в мире?")
                .replace("_", "\n");
    }

    /**
     * Сообщение, отправляемое при успешном прохождении антиспам-проверки.
     * @noinspection unused, DataFlowIssue
     */
    public static String getAccepted() {
        //noinspection deprecation
        return PreferenceManager.getDefaultSharedPreferences(resources.ctx)
                .getString("antispam_accepted_msg",
                        "[EN] Thank you. Now you can send messages directly to my contact list._" +
                                "[RU] Спасибо. Теперь вы можете писать прямо мне.")
                .replace("_", "\n");
    }

    /**
     * Проверка, включён ли антиспам.
     */
    public static boolean enabled() {
        //noinspection deprecation
        return PreferenceManager.getDefaultSharedPreferences(resources.ctx)
                .getBoolean("antispam_enabled", false);
    }
}
