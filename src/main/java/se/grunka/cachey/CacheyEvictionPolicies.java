package se.grunka.cachey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//TODO lru
public class CacheyEvictionPolicies {

    public static <K, V> CacheyEvictionPolicy<K, V> none() {
        return new CacheyEvictionPolicy<K, V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                return false;
            }

            public void elementRead(K key) {
            }

            public K elementAdded(K key) {
                return null;
            }

        };
    }

    public static <K, V> CacheyEvictionPolicy<K, V> timeout(long duration, TimeUnit unit) {
        final long limit = unit.toMillis(duration);
        return new CacheyEvictionPolicy<K, V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                long now = System.currentTimeMillis();
                long elapsed = now - element.created();
                return elapsed > limit;
            }

            public void elementRead(K key) {
            }

            public K elementAdded(K key) {
                return null;
            }
        };
    }

    public static <K, V> CacheyEvictionPolicy<K, V> any(final CacheyEvictionPolicy<K, V>... policies) {
        return new CacheyEvictionPolicy<K, V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                for (CacheyEvictionPolicy<K, V> policy : policies) {
                    if (policy.shouldEvict(element)) {
                        return true;
                    }
                }
                return false;
            }

            public void elementRead(K key) {
            }

            public K elementAdded(K key) {
                return null;
            }
        };
    }

    public static <K, V> CacheyEvictionPolicy<K, V> all(final CacheyEvictionPolicy<K, V>... policies) {
        return new CacheyEvictionPolicy<K, V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                for (CacheyEvictionPolicy<K, V> policy : policies) {
                    if (!policy.shouldEvict(element)) {
                        return false;
                    }
                }
                return true;
            }

            public void elementRead(K key) {
            }

            public K elementAdded(K key) {
                return null;
            }
        };
    }

    public static <K, V> CacheyEvictionPolicy<K, V> fifo(final int elements) {
        final List<K> queue = new ArrayList<K>();
        return new CacheyEvictionPolicy<K, V>() {
            public boolean shouldEvict(CacheyElement<V> vCacheyElement) {
                return false;
            }

            public void elementRead(K key) {
            }

            public K elementAdded(K key) {
                queue.add(key);
                if (queue.size() > elements) {
                    return queue.remove(0);
                } else {
                    return null;
                }
            }
        };
    }
}
