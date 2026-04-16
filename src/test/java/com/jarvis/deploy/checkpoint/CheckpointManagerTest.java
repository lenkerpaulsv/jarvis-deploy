package com.jarvis.deploy.checkpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointManagerTest {

    private CheckpointManager manager;

    @BeforeEach
    void setUp() {
        manager = new CheckpointManager();
    }

    @Test
    void shouldCreateCheckpointWithPendingStatus() {
        DeploymentCheckpoint cp = manager.createCheckpoint("dep-1", "build");
        assertNotNull(cp.getCheckpointId());
        assertEquals("dep-1", cp.getDeploymentId());
        assertEquals("build", cp.getStageName());
        assertEquals(CheckpointStatus.PENDING, cp.getStatus());
    }

    @Test
    void shouldFindCheckpointById() {
        DeploymentCheckpoint cp = manager.createCheckpoint("dep-1", "test");
        Optional<DeploymentCheckpoint> found = manager.findById(cp.getCheckpointId());
        assertTrue(found.isPresent());
        assertEquals(cp.getCheckpointId(), found.get().getCheckpointId());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertTrue(manager.findById("unknown-id").isEmpty());
    }

    @Test
    void shouldFindCheckpointsByDeploymentId() {
        manager.createCheckpoint("dep-2", "build");
        manager.createCheckpoint("dep-2", "deploy");
        manager.createCheckpoint("dep-3", "build");
        List<DeploymentCheckpoint> results = manager.findByDeploymentId("dep-2");
        assertEquals(2, results.size());
    }

    @Test
    void shouldMarkCheckpointCompleted() {
        DeploymentCheckpoint cp = manager.createCheckpoint("dep-1", "build");
        manager.markCompleted(cp.getCheckpointId());
        assertEquals(CheckpointStatus.COMPLETED, cp.getStatus());
        assertTrue(cp.isCompleted());
    }

    @Test
    void shouldMarkCheckpointFailed() {
        DeploymentCheckpoint cp = manager.createCheckpoint("dep-1", "deploy");
        manager.markFailed(cp.getCheckpointId());
        assertEquals(CheckpointStatus.FAILED, cp.getStatus());
    }

    @Test
    void shouldReturnTrueWhenAllCheckpointsCompleted() {
        DeploymentCheckpoint cp1 = manager.createCheckpoint("dep-4", "build");
        DeploymentCheckpoint cp2 = manager.createCheckpoint("dep-4", "deploy");
        manager.markCompleted(cp1.getCheckpointId());
        manager.markCompleted(cp2.getCheckpointId());
        assertTrue(manager.allCompleted("dep-4"));
    }

    @Test
    void shouldReturnFalseWhenSomeCheckpointsPending() {
        DeploymentCheckpoint cp1 = manager.createCheckpoint("dep-5", "build");
        manager.createCheckpoint("dep-5", "deploy");
        manager.markCompleted(cp1.getCheckpointId());
        assertFalse(manager.allCompleted("dep-5"));
    }

    @Test
    void shouldGetLastFailedCheckpoint() {
        DeploymentCheckpoint cp1 = manager.createCheckpoint("dep-6", "build");
        DeploymentCheckpoint cp2 = manager.createCheckpoint("dep-6", "deploy");
        manager.markFailed(cp1.getCheckpointId());
        manager.markFailed(cp2.getCheckpointId());
        Optional<DeploymentCheckpoint> last = manager.getLastFailedCheckpoint("dep-6");
        assertTrue(last.isPresent());
    }

    @Test
    void shouldThrowWhenCreatingCheckpointWithBlankDeploymentId() {
        assertThrows(IllegalArgumentException.class, () -> manager.createCheckpoint("", "build"));
    }
}
