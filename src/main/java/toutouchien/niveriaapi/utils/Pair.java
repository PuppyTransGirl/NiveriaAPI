package toutouchien.niveriaapi.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A generic Pair class that holds a key-value pair.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
public class Pair<K, V> {
    private K key;
    private V value;

    /**
     * Constructs a Pair with the specified key and value.
     *
     * @param key   The key.
     * @param value The value.
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Sets the key and value of the Pair.
     *
     * @param key   The new key.
     * @param value The new value.
     */
    public void set(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Sets the key of the Pair.
     *
     * @param key The new key.
     */
    public void key(K key) {
        this.key = key;
    }

    /**
     * Gets the key of the Pair.
     *
     * @return The key.
     */
    public K key() {
        return key;
    }

    /**
     * Sets the value of the Pair.
     *
     * @param value The new value.
     */
    public void value(V value) {
        this.value = value;
    }

    /**
     * Gets the value of the Pair.
     *
     * @return The value.
     */
    public V value() {
        return value;
    }

    /**
     * Swaps the key and value of the Pair.
     *
     * @return The Pair with swapped key and value.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public Pair<K, V> swap() {
        K tempKey = this.key;
        this.key = (K) this.value;
        this.value = (V) tempKey;
        return this;
    }

    /**
     * Checks if both the key and value are null.
     *
     * @return true if both key and value are null, false otherwise.
     */
    public boolean isEmpty() {
        return key == null && value == null;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    /**
     * Creates a copy of the Pair.
     *
     * @return A new Pair with the same key and value.
     */
    @NotNull
    public Pair<K, V> copy() {
        return new Pair<>(key, value);
    }
}
