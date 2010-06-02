package se.grunka.cachey;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CacheyIntegrationTest {
    private CacheyProvider<String, String> provider;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        provider = mock(CacheyProvider.class);
    }

    @Test
    public void shouldUseFifoPolicy() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.fifo(3);
        Cachey<String, String> cachey = new Cachey<String, String>(provider, policy);
        cachey.get("1");
        cachey.get("2");
        cachey.get("1");
        cachey.get("3");
        cachey.get("4");
        cachey.get("1");
        verify(provider, times(2)).get("1");
        verify(provider, times(1)).get("2");
        verify(provider, times(1)).get("3");
        verify(provider, times(1)).get("4");
    }

    @Test
    public void shouldUseLRUPolicy() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.lru(3);
        Cachey<String, String> cachey = new Cachey<String, String>(provider, policy);
        cachey.get("1");
        cachey.get("2");
        cachey.get("1");
        cachey.get("3");
        cachey.get("4");
        cachey.get("2");
        verify(provider, times(1)).get("1");
        verify(provider, times(2)).get("2");
        verify(provider, times(1)).get("3");
        verify(provider, times(1)).get("4");
    }
}
