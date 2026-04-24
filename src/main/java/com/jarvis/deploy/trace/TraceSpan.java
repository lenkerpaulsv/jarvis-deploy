package com.jarvis.deploy.trace;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single timed span within a deployment trace.
 */
public class TraceSpan {

    private final String deploymentId;
    private final String operation;
    private final Instant startTime;
    private Instant endTime;
    private Map<String, String> metadata = Collections.emptyMap();
    private boolean finished = false;

    public TraceSpan(String deploymentId, String operation, Instant startTime) {
        this.deploymentId = deploymentId;
        this.operation = operation;
        this.startTime = startTime;
    }

    void finish(Instant endTime, Map<String, String> metadata) {
        if (finished) {
            throw new IllegalStateException("Span '" + operation + "' for deployment '" + deploymentId + "' is already finished");
        }
        this.endTime = endTime;
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
        this.finished = true;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getOperation() {
        return operation;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Returns the duration of this span, or null if not yet finished.
     */
    public Duration getDuration() {
        if (!finished) return null;
        return Duration.between(startTime, endTime);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "TraceSpan{deploymentId='" + deploymentId + "', operation='" + operation +
                "', finished=" + finished +
                (finished ? ", durationMs=" + getDuration().toMillis() : "") + "}";
    }
}
