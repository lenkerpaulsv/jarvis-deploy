package com.jarvis.deploy.canary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CanaryManagerTest {

    private CanaryManager manager;

    @BeforeEach
    void setUp() {
        manager = new CanaryManager();
    }

    @Test
    void startCanary_createsActiveCanary() {
        CanaryDeployment canary = manager.startCanary("dep-1", "staging", "1.5.0", 10);
        assertNotNull(canary);
        assertEquals("dep-1", canary.getDeploymentId());
        assertEquals("staging", canary.getEnvironment());
        assertEquals("1.5.0", canary.getVersion());
        assertEquals(10, canary.getTrafficPercentage());
        assertEquals(CanaryStatus.ACTIVE, canary.getStatus());
        assertTrue(canary.isActive());
    }

    @Test
    void startCanary_duplicateId_throwsException() {
        manager.startCanary("dep-1", "staging", "1.5.0", 10);
        assertThrows(IllegalStateException.class,
                () -> manager.startCanary("dep-1", "staging", "1.5.1", 20));
    }

    @Test
    void startCanary_invalidTraffic_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.startCanary("dep-2", "prod", "1.5.0", 0));
        assertThrows(IllegalArgumentException.class,
                () -> manager.startCanary("dep-3", "prod", "1.5.0", 101));
    }

    @Test
    void adjustTraffic_updatesPercentage() {
        manager.startCanary("dep-1", "staging", "1.5.0", 10);
        CanaryDeployment updated = manager.adjustTraffic("dep-1", 50);
        assertEquals(50, updated.getTrafficPercentage());
    }

    @Test
    void promote_setsStatusAndTimestamp() {
        manager.startCanary("dep-1", "staging", "1.5.0", 10);
        CanaryDeployment promoted = manager.promote("dep-1");
        assertEquals(CanaryStatus.PROMOTED, promoted.getStatus());
        assertFalse(promoted.isActive());
        assertNotNull(promoted.getPromotedAt());
    }

    @Test
    void abort_setsStatusAborted() {
        manager.startCanary("dep-1", "staging", "1.5.0", 10);
        CanaryDeployment aborted = manager.abort("dep-1");
        assertEquals(CanaryStatus.ABORTED, aborted.getStatus());
        assertFalse(aborted.isActive());
    }

    @Test
    void promote_onAbortedCanary_throwsException() {
        manager.startCanary("dep-1", "staging", "1.5.0", 10);
        manager.abort("dep-1");
        assertThrows(IllegalStateException.class, () -> manager.promote("dep-1"));
    }

    @Test
    void listActive_returnsOnlyActiveCanaries() {
        manager.startCanary("dep-1", "staging", "1.5.0", 10);
        manager.startCanary("dep-2", "staging", "1.6.0", 20);
        manager.promote("dep-1");
        Collection<CanaryDeployment> active = manager.listActive();
        assertEquals(1, active.size());
        assertEquals("dep-2", active.iterator().next().getDeploymentId());
    }

    @Test
    void find_returnsEmptyForUnknownId() {
        assertTrue(manager.find("unknown").isEmpty());
    }

    @Test
    void find_returnsCanaryForKnownId() {
        manager.startCanary("dep-1", "staging", "1.5.0", 10);
        assertTrue(manager.find("dep-1").isPresent());
    }
}
