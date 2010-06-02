package se.grunka.cachey;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class CacheyTest {
    private CacheyProvider<String, String> provider;
    private Cachey<String, String> target;
    private CacheyEvictionPolicy policy;

    @Before
    public void before() {
        provider = mock(CacheyProvider.class);
        policy = mock(CacheyEvictionPolicy.class);
        when(policy.shouldEvict(any(CacheyElement.class))).thenReturn(false);
        target = new Cachey<String, String>(provider, policy);
    }

    @Test
    public void shouldGetValueFromProvider() {
        when(provider.get("hello")).thenReturn("world");
        String result = target.get("hello");
        assertEquals("world", result);
    }

    @Test
    public void shouldCacheResponseFromProvider() {
        when(provider.get("hello")).thenReturn("world");
        assertEquals("world", target.get("hello"));
        assertEquals("world", target.get("hello"));
        verify(provider, times(1)).get(any(String.class));
    }

    @Test
    public void shouldCacheNullResponsesFromProvider() {
        assertNull(target.get("hello"));
        assertNull(target.get("hello"));
        verify(provider, times(1)).get(any(String.class));
    }

    @Test
    public void shouldGetFromProviderAgainWhenElementIsEvicted() {
        when(policy.shouldEvict(any(CacheyElement.class))).thenReturn(true);
        when(provider.get("hello")).thenReturn("world");
        assertEquals("world", target.get("hello"));
        assertEquals("world", target.get("hello"));
        verify(provider, times(2)).get(any(String.class));
    }

    @Test
    public void shouldNotifyPolicyForAllReadsIncludingCached() {
        target.get("hello");
        target.get("hello");
        verify(policy, times(2)).elementRead("hello");
    }

    @Test
    public void shouldEvictTheElementThatElementAddedReturns() {
        when(policy.elementAdded("2")).thenReturn("1");
        target.get("1");
        target.get("2");
        target.get("1");
        verify(provider, times(2)).get("1");
    }
}
