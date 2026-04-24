package com.jarvis.deploy.progress;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks real-time progress of an active deployment broken down by named stages.
 * Each stage records its start time, completion time, and final status.
 */
public class DeploymentProgressTracker {

    public enum StageStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED }

    public static class StageProgress {
        private final String stageName;
        private StageStatus status;
        private Instant startedAt;
        private Instant finishedAt;
        private String message;

        public StageProgress(String stageName) {
            this.stageName = stageName;
            this.status = StageStatus.PENDING;
        }

        public String getStageName()   { return stageName; }
        public StageStatus getStatus() { return status; }
        public Instant getStartedAt()  { return startedAt; }
        public Instant getFinishedAt() { return finishedAt; }
        public String getMessage()     { return message; }

        void start() {
            this.status = StageStatus.IN_PROGRESS;
            this.startedAt = Instant.now();
        }

        void complete(String message) {
            this.status = StageStatus.COMPLETED;
            this.finishedAt = Instant.now();
            this.message = message;
        }

        void fail(String message) {
            this.status = StageStatus.FAILED;
            this.finishedAt = Instant.now();
            this.message = message;
        }

        void skip(String reason) {
            this.status = StageStatus.SKIPPED;
            this.finishedAt = Instant.now();
            this.message = reason;
        }
    }

    private final String deploymentId;
    private final Map<String, StageProgress> stages = new LinkedHashMap<>();
    private final Map<String, StageProgress> view = Collections.unmodifiableMap(stages);

    public DeploymentProgressTracker(String deploymentId) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        this.deploymentId = deploymentId;
    }

    public String getDeploymentId() { return deploymentId; }

    public void registerStage(String stageName) {
        stages.putIfAbsent(stageName, new StageProgress(stageName));
    }

    public void startStage(String stageName) {
        getRequired(stageName).start();
    }

    public void completeStage(String stageName, String message) {
        getRequired(stageName).complete(message);
    }

    public void failStage(String stageName, String message) {
        getRequired(stageName).fail(message);
    }

    public void skipStage(String stageName, String reason) {
        getRequired(stageName).skip(reason);
    }

    public Optional<StageProgress> getStage(String stageName) {
        return Optional.ofNullable(stages.get(stageName));
    }

    public Map<String, StageProgress> getAllStages() { return view; }

    public int getCompletedCount() {
        return (int) stages.values().stream()
                .filter(s -> s.getStatus() == StageStatus.COMPLETED).count();
    }

    public boolean hasFailure() {
        return stages.values().stream().anyMatch(s -> s.getStatus() == StageStatus.FAILED);
    }

    public int getProgressPercent() {
        if (stages.isEmpty()) return 0;
        long done = stages.values().stream()
                .filter(s -> s.getStatus() != StageStatus.PENDING && s.getStatus() != StageStatus.IN_PROGRESS)
                .count();
        return (int) ((done * 100) / stages.size());
    }

    private StageProgress getRequired(String stageName) {
        StageProgress sp = stages.get(stageName);
        if (sp == null) throw new IllegalArgumentException("Unknown stage: " + stageName);
        return sp;
    }
}
