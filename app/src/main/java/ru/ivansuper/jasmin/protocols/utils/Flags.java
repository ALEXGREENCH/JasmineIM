package ru.ivansuper.jasmin.protocols.utils;

/**
 * Represents a set of flags that can be manipulated and checked.
 * Flags are identified by long values.
 */
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
