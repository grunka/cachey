package se.grunka.cachey;

public class CacheyElement<V> {
    private final V value;
    private final long created;

    public CacheyElement(V value) {
        this.value = value;
        created = System.currentTimeMillis();
    }

    public V value() {
        return value;
    }

    public long created() {
        return created;
    }
}
