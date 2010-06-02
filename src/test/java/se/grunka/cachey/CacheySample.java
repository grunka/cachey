package se.grunka.cachey;

public class CacheySample {
    public static void main(String... args) {
        CacheyProvider<String, String> provider = new CacheyProvider<String, String>() {
            public String get(String key) {
                return "Value for " + key;
            }
        };
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.none();
        Cachey<String, String> cachey = new Cachey<String, String>(provider, policy);
        String value = cachey.get("key");
        System.out.println(value);
    }
}
