package com.jarvis.deploy.concurrency;

/**
 * Thrown when a deployment attempt violates the active concurrency policy.
 */
public class ConcurrencyViolationException extends RuntimeException {

    private final String environment;
    private final String appName;
    private final int currentCount;
    private final int maxAllowed;

    public ConcurrencyViolationException(String environment, String appName,
                                          int currentCount, int maxAllowed) {
        super(String.format(
                "Concurrency limit reached for environment '%s': %d/%d active deployments (app: %s)",
                environment, currentCount, maxAllowed, appName));
        this.environment = environment;
        this.appName = appName;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAppName() {
        return appName;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }
}
