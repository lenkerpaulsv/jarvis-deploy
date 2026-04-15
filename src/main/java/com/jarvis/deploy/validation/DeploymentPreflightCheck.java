package com.jarvis.deploy.validation;

import com.jarvis.deploy.config.EnvironmentConfig;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.lock.DeploymentLockManager;
import com.jarvis.deploy.health.HealthChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs a series of preflight checks before a deployment is executed.
 * Aggregates all failures so callers receive a complete picture.
 */
public class DeploymentPreflightCheck {

    private final HealthChecker healthChecker;
    private final DeploymentLockManager lockManager;

    public DeploymentPreflightCheck(HealthChecker healthChecker, DeploymentLockManager lockManager) {
        if (healthChecker == null) throw new IllegalArgumentException("healthChecker must not be null");
        if (lockManager == null) throw new IllegalArgumentException("lockManager must not be null");
        this.healthChecker = healthChecker;
        this.lockManager = lockManager;
    }

    /**
     * Executes all preflight checks and returns a result containing any violations.
     *
     * @param config the target environment configuration
     * @param record the deployment record about to be executed
     * @return a {@link PreflightResult} describing pass/fail status
     */
    public PreflightResult run(EnvironmentConfig config, DeploymentRecord record) {
        if (config == null) throw new IllegalArgumentException("config must not be null");
        if (record == null) throw new IllegalArgumentException("record must not be null");

        List<String> violations = new ArrayList<>();

        // 1. Ensure the environment is healthy before deploying
        boolean healthy = healthChecker.isHealthy(config.getEnvironmentName());
        if (!healthy) {
            violations.add("Environment '" + config.getEnvironmentName() + "' failed health check.");
        }

        // 2. Ensure no concurrent deployment is in progress
        boolean locked = lockManager.isLocked(config.getEnvironmentName());
        if (locked) {
            violations.add("Environment '" + config.getEnvironmentName() + "' is currently locked by another deployment.");
        }

        // 3. Artifact path must be non-empty
        if (record.getArtifactPath() == null || record.getArtifactPath().isBlank()) {
            violations.add("Deployment record has no artifact path specified.");
        }

        // 4. Version must be non-empty
        if (record.getVersion() == null || record.getVersion().isBlank()) {
            violations.add("Deployment record has no version specified.");
        }

        return new PreflightResult(violations);
    }

    /** Immutable result of a preflight check run. */
    public static final class PreflightResult {
        private final List<String> violations;

        public PreflightResult(List<String> violations) {
            this.violations = Collections.unmodifiableList(new ArrayList<>(violations));
        }

        public boolean isPassed() {
            return violations.isEmpty();
        }

        public List<String> getViolations() {
            return violations;
        }

        @Override
        public String toString() {
            return isPassed() ? "PreflightResult{PASSED}" : "PreflightResult{FAILED, violations=" + violations + "}";
        }
    }
}
