package com.jarvis.deploy.retry;

import com.jarvis.deploy.deployment.DeploymentException;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes a callable with retry logic based on a {@link RetryPolicy}.
 */
public class RetryExecutor {

    private static final Logger logger = Logger.getLogger(RetryExecutor.class.getName());

    private final RetryPolicy policy;

    public RetryExecutor(RetryPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("RetryPolicy must not be null");
        this.policy = policy;
    }

    /**
     * Executes the given callable, retrying on exception up to the policy's maxAttempts.
     *
     * @param operation a descriptive name used in log messages
     * @param callable  the operation to execute
     * @param <T>       the return type
     * @return the result of the callable
     * @throws DeploymentException if all attempts fail
     */
    public <T> T execute(String operation, Callable<T> callable) throws DeploymentException {
        int attempt = 1;
        Exception lastException = null;

        while (attempt <= policy.getMaxAttempts()) {
            try {
                if (attempt > 1) {
                    long delay = policy.computeDelay(attempt);
                    logger.info(String.format("Retrying '%s' (attempt %d/%d) after %d ms",
                            operation, attempt, policy.getMaxAttempts(), delay));
                    Thread.sleep(delay);
                }
                return callable.call();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new DeploymentException("Retry interrupted for operation: " + operation, ie);
            } catch (Exception e) {
                lastException = e;
                logger.log(Level.WARNING,
                        String.format("Attempt %d/%d failed for '%s': %s",
                                attempt, policy.getMaxAttempts(), operation, e.getMessage()));
                attempt++;
            }
        }

        throw new DeploymentException(
                String.format("Operation '%s' failed after %d attempt(s)", operation, policy.getMaxAttempts()),
                lastException);
    }

    public RetryPolicy getPolicy() {
        return policy;
    }
}
