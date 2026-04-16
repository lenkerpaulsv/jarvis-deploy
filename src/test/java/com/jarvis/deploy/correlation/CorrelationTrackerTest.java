package com.jarvis.deploy.correlation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationTrackerTest {

    private CorrelationTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new CorrelationTracker(10);
    }

    @Test
    void testRegisterAndFindActive() {
        DeploymentCorrelationId cid = new DeploymentCorrelationId("prod", "my-app-1.0");
        tracker.register(cid);

        Optional<DeploymentCorrelationId> found = tracker.findActive(cid.getId());
        assertTrue(found.isPresent());
        assertEquals(cid, found.get());
        assertEquals(1, tracker.activeCount());
    }

    @Test
    void testCompleteMovesToHistory() {
        DeploymentCorrelationId cid = new DeploymentCorrelationId("staging", "my-app-2.0");
        tracker.register(cid);
        tracker.complete(cid.getId());

        assertFalse(tracker.findActive(cid.getId()).isPresent());
        assertTrue(tracker.findInHistory(cid.getId()).isPresent());
        assertEquals(0, tracker.activeCount());
        assertEquals(1, tracker.historyCount());
    }

    @Test
    void testFindSearchesBothActiveAndHistory() {
        DeploymentCorrelationId cid1 = new DeploymentCorrelationId("dev", "app-a");
        DeploymentCorrelationId cid2 = new DeploymentCorrelationId("prod", "app-b");
        tracker.register(cid1);
        tracker.register(cid2);
        tracker.complete(cid2.getId());

        assertTrue(tracker.find(cid1.getId()).isPresent());
        assertTrue(tracker.find(cid2.getId()).isPresent());
    }

    @Test
    void testCompleteNonExistentDoesNotThrow() {
        assertDoesNotThrow(() -> tracker.complete("non-existent-id"));
        assertEquals(0, tracker.historyCount());
    }

    @Test
    void testMaxHistoryEviction() {
        CorrelationTracker smallTracker = new CorrelationTracker(3);
        for (int i = 0; i < 5; i++) {
            DeploymentCorrelationId cid = new DeploymentCorrelationId("env", "app-" + i);
            smallTracker.register(cid);
            smallTracker.complete(cid.getId());
        }
        assertTrue(smallTracker.historyCount() <= 3);
    }

    @Test
    void testCorrelationIdEquality() {
        DeploymentCorrelationId cid = new DeploymentCorrelationId("prod", "app");
        DeploymentCorrelationId same = new DeploymentCorrelationId(cid.getId(), cid.getEnvironment(), cid.getArtifactId(), cid.getCreatedAt());
        assertEquals(cid, same);
        assertEquals(cid.hashCode(), same.hashCode());
    }

    @Test
    void testToStringContainsRelevantInfo() {
        DeploymentCorrelationId cid = new DeploymentCorrelationId("uat", "service-3.1");
        String str = cid.toString();
        assertTrue(str.contains("uat"));
        assertTrue(str.contains("service-3.1"));
        assertTrue(str.contains(cid.getId()));
    }
}
