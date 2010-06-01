package se.grunka.cachey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cachey<K, V> {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final CacheyProvider<K, V> provider;
    private final CacheyEvictionPolicy<V> policy;
    private final Map<K, CacheyElement<V>> cache = new HashMap<K, CacheyElement<V>>();

    public Cachey(CacheyProvider<K, V> provider, CacheyEvictionPolicy<V> policy) {
        this.provider = provider;
        this.policy = policy;
    }

    public V get(K key) {
        readLock.lock();
        try {
            CacheyElement<V> element = getValidCachedElement(key);
            if (element != null) {
                return element.value();
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            CacheyElement<V> element = getValidCachedElement(key);
            if (element != null) {
                return element.value();
            }
            V value = provider.get(key);
            cache.put(key, new CacheyElement<V>(value));
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    private CacheyElement<V> getValidCachedElement(K key) {
        if (cache.containsKey(key)) {
            CacheyElement<V> element = cache.get(key);
            if (!policy.shouldEvict(element)) {
                return element;
            }
        }
        return null;
    }
}
