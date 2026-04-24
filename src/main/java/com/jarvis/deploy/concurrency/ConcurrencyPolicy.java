package com.jarvis.deploy.concurrency;

/**
 * Defines the concurrency policy for deployments across environments.
 */
public class ConcurrencyPolicy {

    private final String environment;
    private final int maxConcurrentDeployments;
    private final boolean allowSameAppConcurrency;
    private final long waitTimeoutMillis;

    public ConcurrencyPolicy(String environment, int maxConcurrentDeployments,
                              boolean allowSameAppConcurrency, long waitTimeoutMillis) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (maxConcurrentDeployments < 1) {
            throw new IllegalArgumentException("maxConcurrentDeployments must be >= 1");
        }
        if (waitTimeoutMillis < 0) {
            throw new IllegalArgumentException("waitTimeoutMillis must be >= 0");
        }
        this.environment = environment;
        this.maxConcurrentDeployments = maxConcurrentDeployments;
        this.allowSameAppConcurrency = allowSameAppConcurrency;
        this.waitTimeoutMillis = waitTimeoutMillis;
    }

    public String getEnvironment() {
        return environment;
    }

    public int getMaxConcurrentDeployments() {
        return maxConcurrentDeployments;
    }

    public boolean isAllowSameAppConcurrency() {
        return allowSameAppConcurrency;
    }

    public long getWaitTimeoutMillis() {
        return waitTimeoutMillis;
    }

    @Override
    public String toString() {
        return "ConcurrencyPolicy{env='" + environment + "', max=" + maxConcurrentDeployments
                + ", sameApp=" + allowSameAppConcurrency + ", timeout=" + waitTimeoutMillis + "ms}";
    }
}
