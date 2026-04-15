package com.jarvis.deploy.retry;

import com.jarvis.deploy.deployment.DeploymentException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryExecutorTest {

    @Test
    void execute_succeedsOnFirstAttempt() throws DeploymentException {
        RetryExecutor executor = new RetryExecutor(RetryPolicy.defaultPolicy());
        String result = executor.execute("test-op", () -> "ok");
        assertEquals("ok", result);
    }

    @Test
    void execute_retriesAndSucceeds() throws DeploymentException {
        AtomicInteger counter = new AtomicInteger(0);
        RetryPolicy policy = new RetryPolicy(3, 0, 1.0);
        RetryExecutor executor = new RetryExecutor(policy);

        String result = executor.execute("flaky-op", () -> {
            if (counter.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "success";
        });

        assertEquals("success", result);
        assertEquals(3, counter.get());
    }

    @Test
    void execute_throwsAfterAllAttemptsExhausted() {
        RetryPolicy policy = new RetryPolicy(2, 0, 1.0);
        RetryExecutor executor = new RetryExecutor(policy);

        DeploymentException ex = assertThrows(DeploymentException.class, () ->
                executor.execute("always-fail", () -> {
                    throw new RuntimeException("always fails");
                })
        );

        assertTrue(ex.getMessage().contains("always-fail"));
        assertTrue(ex.getMessage().contains("2 attempt(s)"));
    }

    @Test
    void retryPolicy_noRetry_failsImmediately() {
        RetryExecutor executor = new RetryExecutor(RetryPolicy.noRetry());

        assertThrows(DeploymentException.class, () ->
                executor.execute("single-attempt", () -> {
                    throw new RuntimeException("fail");
                })
        );
    }

    @Test
    void retryPolicy_computeDelay_exponentialBackoff() {
        RetryPolicy policy = new RetryPolicy(5, 1000L, 2.0);
        assertEquals(0L, policy.computeDelay(1));
        assertEquals(1000L, policy.computeDelay(2));
        assertEquals(2000L, policy.computeDelay(3));
        assertEquals(4000L, policy.computeDelay(4));
    }

    @Test
    void constructor_rejectsNullPolicy() {
        assertThrows(IllegalArgumentException.class, () -> new RetryExecutor(null));
    }

    @Test
    void retryPolicy_invalidArguments_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(0, 100, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(1, -1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(1, 100, 0.5));
    }
}
