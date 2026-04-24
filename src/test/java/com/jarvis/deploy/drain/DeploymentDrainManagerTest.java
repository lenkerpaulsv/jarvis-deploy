package com.jarvis.deploy.drain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentDrainManagerTest {

    private DeploymentDrainManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeploymentDrainManager(Duration.ofSeconds(2));
    }

    @Test
    void constructor_rejectsNullTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentDrainManager(null));
    }

    @Test
    void constructor_rejectsNegativeTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentDrainManager(Duration.ofSeconds(-1)));
    }

    @Test
    void register_tracksActiveDeployment() {
        manager.register("deploy-001");
        assertEquals(1, manager.activeCount());
    }

    @Test
    void register_rejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> manager.register("  "));
    }

    @Test
    void complete_removesDeployment() {
        manager.register("deploy-002");
        manager.complete("deploy-002");
        assertEquals(0, manager.activeCount());
    }

    @Test
    void drain_succeedsWhenNoActiveDeployments() throws InterruptedException {
        DrainResult result = manager.drain();
        assertTrue(result.isSuccess());
        assertTrue(result.getStuckDeployments().isEmpty());
    }

    @Test
    void drain_succeedsAfterDeploymentCompletes() throws InterruptedException {
        manager.register("deploy-003");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Thread.sleep(300);
                manager.complete("deploy-003");
            } catch (InterruptedException ignored) {}
        });
        DrainResult result = manager.drain();
        assertTrue(result.isSuccess());
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    void drain_timesOutWithStuckDeployments() throws InterruptedException {
        DeploymentDrainManager shortTimeout = new DeploymentDrainManager(Duration.ofMillis(300));
        shortTimeout.register("deploy-stuck");
        DrainResult result = shortTimeout.drain();
        assertFalse(result.isSuccess());
        assertTrue(result.getStuckDeployments().contains("deploy-stuck"));
    }

    @Test
    void register_throwsWhenDraining() throws InterruptedException {
        manager.drain();
        assertThrows(IllegalStateException.class, () -> manager.register("deploy-late"));
    }

    @Test
    void reset_clearsDrainStateAndDeployments() throws InterruptedException {
        manager.register("deploy-004");
        manager.drain();
        manager.reset();
        assertFalse(manager.isDraining());
        assertEquals(0, manager.activeCount());
        assertDoesNotThrow(() -> manager.register("deploy-005"));
    }
}
