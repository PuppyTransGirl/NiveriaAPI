package toutouchien.niveriaapi.utils;

public class Pair<K, V> {
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

	public K key() {
		return key;
	}

	public void key(K key) {
		this.key = key;
	}

	public V value() {
		return value;
	}

	public void value(V value) {
		this.value = value;
	}
}
