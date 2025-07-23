package ru.ivansuper.jasmin.protocols.utils;

/**
 * An interface for objects that can be serialized and deserialized.
 * <p>
 * This interface defines methods for converting an object into a byte array
 * (serialization) and for reconstructing an object from a byte array
 * (deserialization).
 */
public interface Serializable {
    /** @noinspection unused*/
    void deserialize(byte[] bArr);

    /** @noinspection unused*/
    byte[] serialize();
}