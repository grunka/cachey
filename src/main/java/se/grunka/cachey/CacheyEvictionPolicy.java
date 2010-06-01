package se.grunka.cachey;

public interface CacheyEvictionPolicy<V> {
    boolean shouldEvict(CacheyElement<V> element);
}
