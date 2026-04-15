package com.jarvis.deploy.retry;

/**
 * Defines the retry policy for deployment operations.
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final long delayMillis;
    private final double backoffMultiplier;

    public RetryPolicy(int maxAttempts, long delayMillis, double backoffMultiplier) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (delayMillis < 0) throw new IllegalArgumentException("delayMillis must be >= 0");
        if (backoffMultiplier < 1.0) throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
        this.backoffMultiplier = backoffMultiplier;
    }

    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, 0, 1.0);
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, 1000L, 2.0);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    /**
     * Computes the delay before the given attempt (1-indexed).
     */
    public long computeDelay(int attempt) {
        if (attempt <= 1) return 0;
        long delay = delayMillis;
        for (int i = 2; i < attempt; i++) {
            delay = (long) (delay * backoffMultiplier);
        }
        return delay;
    }

    @Override
    public String toString() {
        return String.format("RetryPolicy{maxAttempts=%d, delayMillis=%d, backoffMultiplier=%.1f}",
                maxAttempts, delayMillis, backoffMultiplier);
    }
}
