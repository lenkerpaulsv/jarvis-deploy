package com.jarvis.deploy.correlation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks active and historical deployment correlation IDs.
 * Allows components to look up the correlation context for a given deployment.
 */
public class CorrelationTracker {

    private final Map<String, DeploymentCorrelationId> active = new ConcurrentHashMap<>();
    // Bounded history using a synchronized LinkedHashMap
    private final Map<String, DeploymentCorrelationId> history;
    private final int maxHistory;

    public CorrelationTracker(int maxHistory) {
        this.maxHistory = maxHistory;
        this.history = Collections.synchronizedMap(new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DeploymentCorrelationId> eldest) {
                return size() > maxHistory;
            }
        });
    }

    public CorrelationTracker() {
        this(100);
    }

    /** Register a new correlation ID as active. */
    public void register(DeploymentCorrelationId correlationId) {
        active.put(correlationId.getId(), correlationId);
    }

    /** Mark a correlation ID as completed; moves it to history. */
    public void complete(String correlationId) {
        DeploymentCorrelationId entry = active.remove(correlationId);
        if (entry != null) {
            history.put(entry.getId(), entry);
        }
    }

    public Optional<DeploymentCorrelationId> findActive(String correlationId) {
        return Optional.ofNullable(active.get(correlationId));
    }

    public Optional<DeploymentCorrelationId> findInHistory(String correlationId) {
        return Optional.ofNullable(history.get(correlationId));
    }

    public Optional<DeploymentCorrelationId> find(String correlationId) {
        return findActive(correlationId).or(() -> findInHistory(correlationId));
    }

    public int activeCount() { return active.size(); }
    public int historyCount() { return history.size(); }

    public Map<String, DeploymentCorrelationId> getActive() {
        return Collections.unmodifiableMap(active);
    }
}
