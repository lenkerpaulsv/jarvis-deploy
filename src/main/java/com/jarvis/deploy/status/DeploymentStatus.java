package com.jarvis.deploy.status;

import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.DeploymentHistory;

import java.util.List;
import java.util.Optional;

/**
 * Provides status reporting for deployments across environments.
 */
public class DeploymentStatus {

    private final DeploymentHistory history;

    public DeploymentStatus(DeploymentHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("DeploymentHistory must not be null");
        }
        this.history = history;
    }

    /**
     * Returns the latest deployment record for the given environment.
     */
    public Optional<DeploymentRecord> getLatest(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be null or blank");
        }
        List<DeploymentRecord> records = history.getRecordsForEnvironment(environment);
        if (records == null || records.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(records.get(records.size() - 1));
    }

    /**
     * Returns a formatted status summary for the given environment.
     */
    public String getSummary(String environment) {
        Optional<DeploymentRecord> latest = getLatest(environment);
        if (latest.isEmpty()) {
            return "[" + environment + "] No deployments recorded.";
        }
        DeploymentRecord record = latest.get();
        return String.format("[%s] Latest: version=%s, status=%s, timestamp=%s",
                environment,
                record.getVersion(),
                record.getStatus(),
                record.getTimestamp());
    }

    /**
     * Returns true if the latest deployment for the environment succeeded.
     */
    public boolean isHealthy(String environment) {
        return getLatest(environment)
                .map(r -> "SUCCESS".equalsIgnoreCase(r.getStatus()))
                .orElse(false);
    }
}
