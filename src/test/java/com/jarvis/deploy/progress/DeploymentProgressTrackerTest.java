package com.jarvis.deploy.progress;

import com.jarvis.deploy.progress.DeploymentProgressTracker.StageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentProgressTrackerTest {

    private DeploymentProgressTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new DeploymentProgressTracker("deploy-001");
        tracker.registerStage("PREFLIGHT");
        tracker.registerStage("BUILD");
        tracker.registerStage("DEPLOY");
        tracker.registerStage("HEALTH_CHECK");
    }

    @Test
    void constructorRejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentProgressTracker(""));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentProgressTracker(null));
    }

    @Test
    void initialStagesArePending() {
        tracker.getAllStages().values().forEach(s ->
                assertEquals(StageStatus.PENDING, s.getStatus()));
    }

    @Test
    void startStageTransitionsToInProgress() {
        tracker.startStage("PREFLIGHT");
        assertEquals(StageStatus.IN_PROGRESS, tracker.getStage("PREFLIGHT").orElseThrow().getStatus());
        assertNotNull(tracker.getStage("PREFLIGHT").orElseThrow().getStartedAt());
    }

    @Test
    void completeStageTransitionsToCompleted() {
        tracker.startStage("BUILD");
        tracker.completeStage("BUILD", "Build successful");
        var stage = tracker.getStage("BUILD").orElseThrow();
        assertEquals(StageStatus.COMPLETED, stage.getStatus());
        assertEquals("Build successful", stage.getMessage());
        assertNotNull(stage.getFinishedAt());
    }

    @Test
    void failStageTransitionsToFailed() {
        tracker.startStage("DEPLOY");
        tracker.failStage("DEPLOY", "Connection refused");
        var stage = tracker.getStage("DEPLOY").orElseThrow();
        assertEquals(StageStatus.FAILED, stage.getStatus());
        assertEquals("Connection refused", stage.getMessage());
    }

    @Test
    void skipStageTransitionsToSkipped() {
        tracker.skipStage("HEALTH_CHECK", "Skipped in dry-run mode");
        assertEquals(StageStatus.SKIPPED, tracker.getStage("HEALTH_CHECK").orElseThrow().getStatus());
    }

    @Test
    void progressPercentReflectsCompletedAndNonPendingStages() {
        assertEquals(0, tracker.getProgressPercent());
        tracker.startStage("PREFLIGHT");
        tracker.completeStage("PREFLIGHT", "ok");
        tracker.startStage("BUILD");
        tracker.completeStage("BUILD", "ok");
        // 2 of 4 done
        assertEquals(50, tracker.getProgressPercent());
    }

    @Test
    void hasFailureReturnsTrueWhenAnyStageHasFailed() {
        assertFalse(tracker.hasFailure());
        tracker.startStage("DEPLOY");
        tracker.failStage("DEPLOY", "timeout");
        assertTrue(tracker.hasFailure());
    }

    @Test
    void getCompletedCountOnlyCountsCompletedStages() {
        tracker.startStage("PREFLIGHT");
        tracker.completeStage("PREFLIGHT", "ok");
        tracker.skipStage("BUILD", "skipped");
        assertEquals(1, tracker.getCompletedCount());
    }

    @Test
    void unknownStageThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> tracker.startStage("NONEXISTENT"));
    }

    @Test
    void registerStageTwiceIsIdempotent() {
        tracker.registerStage("PREFLIGHT");
        assertEquals(4, tracker.getAllStages().size());
    }
}
