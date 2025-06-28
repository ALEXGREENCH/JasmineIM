package ru.ivansuper.jasmin.protocols.utils;

public interface Serializable {
    /** @noinspection unused*/
    void deserialize(byte[] bArr);

    /** @noinspection unused*/
    byte[] serialize();
}