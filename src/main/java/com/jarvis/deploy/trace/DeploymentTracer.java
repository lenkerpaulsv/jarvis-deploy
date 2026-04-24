package com.jarvis.deploy.trace;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks deployment execution traces across stages and environments.
 * Each trace captures a named span with timing and metadata.
 */
public class DeploymentTracer {

    private final Map<String, List<TraceSpan>> traces = new ConcurrentHashMap<>();

    /**
     * Begins a new trace span for the given deployment ID and operation name.
     *
     * @param deploymentId the deployment identifier
     * @param operation    the name of the operation being traced
     * @return a started TraceSpan
     */
    public TraceSpan startSpan(String deploymentId, String operation) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("operation must not be blank");
        }
        TraceSpan span = new TraceSpan(deploymentId, operation, Instant.now());
        traces.computeIfAbsent(deploymentId, k -> Collections.synchronizedList(new ArrayList<>())).add(span);
        return span;
    }

    /**
     * Finishes a span by recording its end time and optional metadata.
     *
     * @param span     the span to finish
     * @param metadata optional key-value metadata to attach
     */
    public void finishSpan(TraceSpan span, Map<String, String> metadata) {
        if (span == null) {
            throw new IllegalArgumentException("span must not be null");
        }
        span.finish(Instant.now(), metadata);
    }

    /**
     * Returns all spans recorded for a given deployment ID.
     *
     * @param deploymentId the deployment identifier
     * @return unmodifiable list of spans, or empty list if none
     */
    public List<TraceSpan> getSpans(String deploymentId) {
        return Collections.unmodifiableList(
                traces.getOrDefault(deploymentId, Collections.emptyList())
        );
    }

    /**
     * Clears all trace data for a given deployment ID.
     *
     * @param deploymentId the deployment identifier
     */
    public void clearTrace(String deploymentId) {
        traces.remove(deploymentId);
    }

    /**
     * Returns the total number of deployments currently being traced.
     */
    public int activeTraceCount() {
        return traces.size();
    }
}
