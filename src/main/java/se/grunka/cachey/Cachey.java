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
    private final CacheyEvictionPolicy<K, V> policy;
    private final Map<K, CacheyElement<V>> cache = new HashMap<K, CacheyElement<V>>();

    public Cachey(CacheyProvider<K, V> provider, CacheyEvictionPolicy<K, V> policy) {
        this.provider = provider;
        this.policy = policy;
    }

    public V get(K key) {
        try {
            readLock.lock();
            try {
                CacheyElement<V> element = getValidElement(key);
                if (element != null) {
                    return element.value();
                }
            } finally {
                readLock.unlock();
            }
            return updateCache(key);
        } finally {
            policy.elementRead(key);
        }
    }

    private V updateCache(K key) {
        writeLock.lock();
        try {
            CacheyElement<V> element = getValidElement(key);
            if (element != null) {
                return element.value();
            }
            V value = provider.get(key);
            cache.put(key, new CacheyElement<V>(value));
            K keyToRemove = policy.elementAdded(key);
            if (keyToRemove != null) {
                cache.remove(keyToRemove);
            }
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    private CacheyElement<V> getValidElement(K key) {
        if (cache.containsKey(key)) {
            CacheyElement<V> element = cache.get(key);
            if (!policy.shouldEvict(element)) {
                return element;
            }
        }
        return null;
    }
}
