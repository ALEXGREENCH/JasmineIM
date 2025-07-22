package ru.ivansuper.jasmin;

public abstract class ContactlistItem implements Comparable<ContactlistItem> {

    /** @noinspection unused*/ // Типы элементов в списке контактов
    public static final int CONTACT = 1;
    /** @noinspection unused*/
    public static final int GROUP = 2;
    /** @noinspection unused*/
    public static final int PROFILE_GROUP = 3;
    /** @noinspection unused*/
    public static final int JABBER_CONTACT = 4;
    /** @noinspection unused*/
    public static final int JABBER_PROFILE_GROUP = 5;
    /** @noinspection unused*/
    public static final int JABBER_GROUP = 6;
    /** @noinspection unused*/
    public static final int MMP_CONTACT = 7;
    /** @noinspection unused*/
    public static final int MMP_PROFILE_GROUP = 8;
    /** @noinspection unused*/
    public static final int MMP_GROUP = 9;
    /** @noinspection unused*/
    public static final int JABBER_CONFERENCE = 10;
    /** @noinspection unused*/
    public static final int SPLITTER = 11;

    // Публичные поля
    public int itemType;
    public boolean presence_initialized;
    public long presense_timestamp;
    public String ID = "";
    public String name = "";

    // Получение уникального хеша для сравнения/сопоставления
    public int getHash() {
        return this.ID.hashCode();
    }

    // Переопределяемый метод для обновления содержимого (если нужно)
    public void update(ContactlistItem item) {
        // Обычно реализуется в подклассах
    }

    // Метод сравнения для сортировки списка
    @Override
    public final int compareTo(ContactlistItem contact) {
        try {
            // Установка флага для старого алгоритма сортировки (устарело, но оставлено как было)
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

            String nameA = this.name;
            String nameB = contact.name;

            int minLen = Math.min(nameA.length(), nameB.length());
            int lvl = 0;

            while (lvl < minLen) {
                int a = utilities.chars.indexOf(nameA.charAt(lvl));
                int b = utilities.chars.indexOf(nameB.charAt(lvl));

                a = (a >= 0 ? a : nameA.charAt(lvl)) + 256;
                b = (b >= 0 ? b : nameB.charAt(lvl)) + 256;

                if (a == b) {
                    lvl++;
                } else {
                    return Integer.compare(a, b);
                }
            }

            // Если строки одинаковы по началу, но разной длины
            return Integer.compare(nameA.length(), nameB.length());

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return 0;
        }
    }

    // Отметка, что нужно "подсветить" элемент (например, пришло сообщение)
    public final void requestBlink() {
        this.presense_timestamp = System.currentTimeMillis();
    }

    // Сброс подсветки (событие обработано)
    public final void resetBlink() {
        this.presense_timestamp = 0L;
    }
}
