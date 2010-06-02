package se.grunka.cachey;

public interface CacheyEvictionPolicy<K, V> {
    boolean shouldEvict(CacheyElement<V> element);

    void elementRead(K key);

    K elementAdded(K key);
}
