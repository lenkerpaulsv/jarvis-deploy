package com.jarvis.deploy.signal;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a control signal sent to an active deployment.
 */
public class DeploymentSignal {

    private final String deploymentId;
    private final DeploymentSignalType type;
    private final String issuedBy;
    private final String reason;
    private final Instant issuedAt;

    public DeploymentSignal(String deploymentId, DeploymentSignalType type, String issuedBy, String reason) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("Deployment ID must not be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Signal type must not be null");
        }
        this.deploymentId = deploymentId;
        this.type = type;
        this.issuedBy = Objects.requireNonNullElse(issuedBy, "system");
        this.reason = Objects.requireNonNullElse(reason, "");
        this.issuedAt = Instant.now();
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public DeploymentSignalType getType() {
        return type;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public String getReason() {
        return reason;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    @Override
    public String toString() {
        return "DeploymentSignal{deploymentId='" + deploymentId + "', type=" + type +
               ", issuedBy='" + issuedBy + "', reason='" + reason + "', issuedAt=" + issuedAt + "}";
    }
}
