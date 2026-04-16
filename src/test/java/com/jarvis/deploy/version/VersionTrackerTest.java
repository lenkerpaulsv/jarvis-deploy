package com.jarvis.deploy.version;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VersionTrackerTest {

    private VersionTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new VersionTracker();
    }

    @Test
    void testRecordAndGetLatest() {
        tracker.record("prod", "1.0.0", "alice");
        Optional<VersionEntry> latest = tracker.getLatest("prod");
        assertTrue(latest.isPresent());
        assertEquals("1.0.0", latest.get().getVersion());
        assertEquals("alice", latest.get().getDeployedBy());
    }

    @Test
    void testLatestReturnsNewest() {
        tracker.record("prod", "1.0.0", "alice");
        tracker.record("prod", "1.1.0", "bob");
        assertEquals("1.1.0", tracker.getLatest("prod").get().getVersion());
    }

    @Test
    void testGetHistoryOrder() {
        tracker.record("staging", "2.0.0", "alice");
        tracker.record("staging", "2.1.0", "bob");
        List<VersionEntry> history = tracker.getHistory("staging");
        assertEquals(2, history.size());
        assertEquals("2.1.0", history.get(0).getVersion());
        assertEquals("2.0.0", history.get(1).getVersion());
    }

    @Test
    void testGetLatestEmptyEnvironment() {
        assertTrue(tracker.getLatest("dev").isEmpty());
    }

    @Test
    void testHasEnvironment() {
        assertFalse(tracker.hasEnvironment("prod"));
        tracker.record("prod", "1.0.0", "alice");
        assertTrue(tracker.hasEnvironment("prod"));
    }

    @Test
    void testClear() {
        tracker.record("prod", "1.0.0", "alice");
        tracker.clear("prod");
        assertFalse(tracker.hasEnvironment("prod"));
        assertTrue(tracker.getHistory("prod").isEmpty());
    }

    @Test
    void testTrackedEnvironments() {
        tracker.record("prod", "1.0.0", "alice");
        tracker.record("dev", "0.9.0", "bob");
        assertTrue(tracker.trackedEnvironments().contains("prod"));
        assertTrue(tracker.trackedEnvironments().contains("dev"));
    }

    @Test
    void testRecordBlankEnvironmentThrows() {
        assertThrows(IllegalArgumentException.class, () -> tracker.record("", "1.0.0", "alice"));
    }

    @Test
    void testRecordBlankVersionThrows() {
        assertThrows(IllegalArgumentException.class, () -> tracker.record("prod", "", "alice"));
    }
}
