package com.jarvis.deploy.promote;

import java.time.Instant;
import java.util.Objects;

public class PromotionRequest {
    private final String deploymentId;
    private final String sourceEnvironment;
    private final String targetEnvironment;
    private final String requestedBy;
    private final Instant requestedAt;
    private PromotionStatus status;

    public PromotionRequest(String deploymentId, String sourceEnvironment,
                            String targetEnvironment, String requestedBy) {
        this.deploymentId = Objects.requireNonNull(deploymentId);
        this.sourceEnvironment = Objects.requireNonNull(sourceEnvironment);
        this.targetEnvironment = Objects.requireNonNull(targetEnvironment);
        this.requestedBy = Objects.requireNonNull(requestedBy);
        this.requestedAt = Instant.now();
        this.status = PromotionStatus.PENDING;
    }

    public String getDeploymentId() { return deploymentId; }
    public String getSourceEnvironment() { return sourceEnvironment; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public String getRequestedBy() { return requestedBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public PromotionStatus getStatus() { return status; }
    public void setStatus(PromotionStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("PromotionRequest{deploymentId='%s', %s -> %s, status=%s}",
                deploymentId, sourceEnvironment, targetEnvironment, status);
    }
}
