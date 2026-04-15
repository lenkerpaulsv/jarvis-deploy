package com.jarvis.deploy.health;

import com.jarvis.deploy.config.EnvironmentConfig;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Performs pre-deployment and post-deployment health checks
 * for a given environment, verifying connectivity and disk space.
 */
public class HealthChecker {

    private static final Logger logger = Logger.getLogger(HealthChecker.class.getName());

    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final long MIN_FREE_DISK_BYTES = 100L * 1024 * 1024; // 100 MB

    private final int timeoutMs;

    public HealthChecker() {
        this(DEFAULT_TIMEOUT_MS);
    }

    public HealthChecker(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Runs all health checks for the given environment config.
     *
     * @param config the environment configuration to validate
     * @return a HealthCheckResult summarising the outcome
     */
    public HealthCheckResult check(EnvironmentConfig config) {
        if (config == null) {
            return HealthCheckResult.failure("EnvironmentConfig must not be null");
        }

        String endpointFailure = checkEndpoint(config.getHealthCheckUrl());
        if (endpointFailure != null) {
            return HealthCheckResult.failure(endpointFailure);
        }

        String diskFailure = checkDiskSpace(config.getDeployDirectory());
        if (diskFailure != null) {
            return HealthCheckResult.failure(diskFailure);
        }

        logger.info("Health check passed for environment: " + config.getEnvironmentName());
        return HealthCheckResult.success();
    }

    private String checkEndpoint(String healthCheckUrl) {
        if (healthCheckUrl == null || healthCheckUrl.isBlank()) {
            logger.warning("No health-check URL configured; skipping endpoint check.");
            return null;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(healthCheckUrl).openConnection();
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 400) {
                return null;
            }
            return "Health-check endpoint returned HTTP " + responseCode + " for URL: " + healthCheckUrl;
        } catch (Exception e) {
            return "Health-check endpoint unreachable: " + healthCheckUrl + " — " + e.getMessage();
        }
    }

    private String checkDiskSpace(String deployDirectory) {
        if (deployDirectory == null || deployDirectory.isBlank()) {
            return "Deploy directory is not configured";
        }
        File dir = new File(deployDirectory);
        if (!dir.exists() && !dir.mkdirs()) {
            return "Deploy directory does not exist and could not be created: " + deployDirectory;
        }
        long freeBytes = dir.getFreeSpace();
        if (freeBytes < MIN_FREE_DISK_BYTES) {
            return String.format("Insufficient disk space in '%s': %.1f MB free (minimum 100 MB required)",
                    deployDirectory, freeBytes / (1024.0 * 1024.0));
        }
        return null;
    }
}
