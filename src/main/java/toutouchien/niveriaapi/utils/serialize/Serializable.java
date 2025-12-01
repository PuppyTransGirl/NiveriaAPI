package toutouchien.niveriaapi.utils.serialize;

/**
 * Interface for serializable objects.
 *
 * @param <T> The type of the object to be serialized/deserialized.
 */
public interface Serializable<T> {
    /**
     * Serializes the object to a byte array.
     *
     * @return The serialized byte array.
     */
    byte[] serialize();

    /**
     * Deserializes the object from a byte array.
     *
     * @param data The byte array to deserialize.
     * @return The deserialized object.
     */
    T deserialize(byte[] data);
}
