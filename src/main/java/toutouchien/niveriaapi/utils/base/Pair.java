package toutouchien.niveriaapi.utils.base;

import java.util.Objects;

public class Pair<K, V> implements Cloneable {
	private K key;
	private V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public void set(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public void key(K key) {
		this.key = key;
	}

	public K key() {
		return key;
	}

	public void value(V value) {
		this.value = value;
	}

	public V value() {
		return value;
	}

	@SuppressWarnings("unchecked")
	public Pair<K, V> swap() {
		K tempKey = key;
		this.key = (K) value;
		this.value = (V) tempKey;
		return this;
	}

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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public Pair<K, V> clone() {
		return new Pair<>(key, value);
	}
}
