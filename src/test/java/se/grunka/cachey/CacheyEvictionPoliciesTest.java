package se.grunka.cachey;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class CacheyEvictionPoliciesTest {
    private CacheyEvictionPolicy<String> falsePolicy;
    private CacheyEvictionPolicy<String> truePolicy;

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
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.timeout(1, TimeUnit.SECONDS);
        assertFalse(policy.shouldEvict(new CacheyElement<String>("hello")));
    }

    @Test
    public void shouldNotEvictTimedOutElement() throws InterruptedException {
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.timeout(0, TimeUnit.SECONDS);
        Thread.sleep(10);
        assertTrue(policy.shouldEvict(new CacheyElement<String>("hello")));
    }

    @Test
    public void shouldNotEvictIfNoPolicyShouldEvict() {
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.any(falsePolicy, falsePolicy);
        assertFalse(policy.shouldEvict(null));
        verify(falsePolicy, times(2)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldEvictIfAtLeastOnePolicyShouldEvict() {
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.any(falsePolicy, truePolicy);
        assertTrue(policy.shouldEvict(null));
        verify(falsePolicy, times(1)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldNotCallSecondPolicyIfFirstShouldEvict() {
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.any(truePolicy, falsePolicy);
        assertTrue(policy.shouldEvict(null));
        verify(falsePolicy, times(0)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldNotEvictIfNotAllPoliciesAgree() {
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.all(truePolicy, falsePolicy);
        assertFalse(policy.shouldEvict(null));
        verify(falsePolicy, times(1)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldEvictIfAllPoliciesAgree() {
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.all(truePolicy, truePolicy);
        assertTrue(policy.shouldEvict(null));
        verify(falsePolicy, times(1)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }

    @Test
    public void shouldNotCallSecondPolicyIfFirstOneShouldEvict() {
        CacheyEvictionPolicy<String> policy = CacheyEvictionPolicies.all(truePolicy, falsePolicy);
        assertFalse(policy.shouldEvict(null));
        verify(falsePolicy, times(0)).shouldEvict(any(CacheyElement.class));
        verify(truePolicy, times(1)).shouldEvict(any(CacheyElement.class));
    }
}
