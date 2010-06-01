package se.grunka.cachey;

import java.util.concurrent.TimeUnit;

//TODO lru, fifo
public class CacheyEvictionPolicies {

    public static <V> CacheyEvictionPolicy<V> none() {
        return new CacheyEvictionPolicy<V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                return false;
            }

        };
    }

    public static <V> CacheyEvictionPolicy<V> timeout(long duration, TimeUnit unit) {
        final long limit = unit.toMillis(duration);
        return new CacheyEvictionPolicy<V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                long now = System.currentTimeMillis();
                long elapsed = now - element.created();
                return elapsed > limit;
            }

        };
    }

    public static <V> CacheyEvictionPolicy<V> any(final CacheyEvictionPolicy<V>... policies) {
        return new CacheyEvictionPolicy<V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                for (CacheyEvictionPolicy<V> policy : policies) {
                    if (policy.shouldEvict(element)) {
                        return true;
                    }
                }
                return false;
            }

        };
    }

    public static <V> CacheyEvictionPolicy<V> all(final CacheyEvictionPolicy<V>... policies) {
        return new CacheyEvictionPolicy<V>() {
            public boolean shouldEvict(CacheyElement<V> element) {
                for (CacheyEvictionPolicy<V> policy : policies) {
                    if (!policy.shouldEvict(element)) {
                        return false;
                    }
                }
                return true;
            }

        };
    }
}
