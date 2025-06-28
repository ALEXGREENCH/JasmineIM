package ru.ivansuper.jasmin.protocols.utils;

public interface Flags {
    /** @noinspection unused*/
    void addFlag(long j);

    /** @noinspection unused*/
    boolean checkFlag(long j);

    /** @noinspection unused*/
    void clearFlags();

    /** @noinspection unused*/
    void removeFlag(long j);
}
