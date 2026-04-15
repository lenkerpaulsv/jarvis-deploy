package com.jarvis.deploy.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an ordered sequence of deployment stages to be executed.
 */
public class DeploymentPipeline {

    private final String pipelineId;
    private final String environment;
    private final List<PipelineStage> stages;
    private PipelineStatus status;
    private int currentStageIndex;

    public DeploymentPipeline(String pipelineId, String environment) {
        this.pipelineId = Objects.requireNonNull(pipelineId, "pipelineId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.stages = new ArrayList<>();
        this.status = PipelineStatus.PENDING;
        this.currentStageIndex = 0;
    }

    public void addStage(PipelineStage stage) {
        if (status != PipelineStatus.PENDING) {
            throw new IllegalStateException("Cannot add stages to a pipeline that has already started.");
        }
        stages.add(Objects.requireNonNull(stage, "stage must not be null"));
    }

    public List<PipelineStage> getStages() {
        return Collections.unmodifiableList(stages);
    }

    public String getPipelineId() { return pipelineId; }
    public String getEnvironment() { return environment; }
    public PipelineStatus getStatus() { return status; }
    public int getCurrentStageIndex() { return currentStageIndex; }

    public void setStatus(PipelineStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public void advanceStage() {
        if (currentStageIndex < stages.size()) {
            currentStageIndex++;
        }
    }

    public boolean hasNextStage() {
        return currentStageIndex < stages.size();
    }

    public PipelineStage currentStage() {
        if (!hasNextStage()) return null;
        return stages.get(currentStageIndex);
    }

    @Override
    public String toString() {
        return "DeploymentPipeline{id='" + pipelineId + "', env='" + environment +
               "', stages=" + stages.size() + ", status=" + status + "}";
    }
}
