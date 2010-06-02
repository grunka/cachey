package se.grunka.cachey;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class CacheyEvictionPoliciesTest {
    private CacheyEvictionPolicy<String, String> falsePolicy;
    private CacheyEvictionPolicy<String, String> truePolicy;

    @Before
    public void before() {
        falsePolicy = mock(CacheyEvictionPolicy.class);
        truePolicy = mock(CacheyEvictionPolicy.class);
        when(falsePolicy.shouldEvict(any(CacheyElement.class))).thenReturn(false);
        when(truePolicy.shouldEvict(any(CacheyElement.class))).thenReturn(true);
    }

    @Test
    public void shouldNotEvictAnything() {
        assertFalse(CacheyEvictionPolicies.none().shouldEvict(null));
    }

    @Test
    public void shouldNotEvictNonTimedOutElement() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.timeout(1, TimeUnit.SECONDS);
        assertFalse(policy.shouldEvict(new CacheyElement<String>("hello")));
    }

    @Test
    public void shouldEvictTimedOutElement() throws InterruptedException {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.timeout(0, TimeUnit.SECONDS);
        CacheyElement<String> element = new CacheyElement<String>("hello");
        Thread.sleep(10);
        assertTrue(policy.shouldEvict(element));
    }

    @Test
    public void shouldNotEvictIfNoPolicyShouldEvict() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.any(falsePolicy, falsePolicy);
        assertFalse(policy.shouldEvict(null));
        verify(falsePolicy, times(2)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldEvictIfAtLeastOnePolicyShouldEvict() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.any(falsePolicy, truePolicy);
        assertTrue(policy.shouldEvict(null));
        verify(falsePolicy, times(1)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldNotCallSecondPolicyIfFirstShouldEvict() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.any(truePolicy, falsePolicy);
        assertTrue(policy.shouldEvict(null));
        verify(falsePolicy, times(0)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldNotEvictIfNotAllPoliciesAgree() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.all(truePolicy, falsePolicy);
        assertFalse(policy.shouldEvict(null));
        verify(falsePolicy, times(1)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldEvictIfAllPoliciesAgree() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.all(truePolicy, truePolicy);
        assertTrue(policy.shouldEvict(null));
        verify(truePolicy, times(2)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldNotCallSecondPolicyIfFirstOneShouldNotEvict() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.all(falsePolicy, truePolicy);
        assertFalse(policy.shouldEvict(null));
        verify(falsePolicy, times(1)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(0)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldEvictTheFirstElementAddedFirst() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.fifo(3);
        assertNull(policy.elementAdded("1"));
        assertNull(policy.elementAdded("2"));
        assertNull(policy.elementAdded("3"));
        assertEquals("1", policy.elementAdded("4"));
    }

    @Test
    public void shouldNotBeAffectedByReadOrder() {
        CacheyEvictionPolicy<String, String> policy = CacheyEvictionPolicies.fifo(3);
        assertNull(policy.elementAdded("1"));
        assertNull(policy.elementAdded("2"));
        assertNull(policy.elementAdded("3"));
        policy.elementRead("1");
        assertEquals("1", policy.elementAdded("4"));
    }

    //TODO tests for usage of add and read of elements for other policies
}
