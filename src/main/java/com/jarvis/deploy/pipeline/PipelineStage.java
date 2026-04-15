package com.jarvis.deploy.pipeline;

import java.util.Objects;

/**
 * Represents a single named stage within a deployment pipeline.
 */
public class PipelineStage {

    private final String name;
    private final Runnable action;
    private StageResult result;
    private String failureReason;

    public PipelineStage(String name, Runnable action) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.result = StageResult.PENDING;
    }

    public String getName() { return name; }
    public StageResult getResult() { return result; }
    public String getFailureReason() { return failureReason; }

    public void execute() {
        try {
            action.run();
            this.result = StageResult.SUCCESS;
        } catch (Exception e) {
            this.result = StageResult.FAILED;
            this.failureReason = e.getMessage();
            throw e;
        }
    }

    public enum StageResult {
        PENDING, SUCCESS, FAILED, SKIPPED
    }

    @Override
    public String toString() {
        return "PipelineStage{name='" + name + "', result=" + result + "}";
    }
}
