package se.grunka.cachey;

public interface CacheyProvider<K, V> {
    V get(K key);
}
