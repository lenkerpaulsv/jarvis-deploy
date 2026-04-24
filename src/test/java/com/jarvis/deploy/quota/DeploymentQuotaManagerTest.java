package com.jarvis.deploy.quota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentQuotaManagerTest {

    private DeploymentQuotaManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeploymentQuotaManager();
    }

    @Test
    void registerAndRetrieveQuota() {
        DeploymentQuota quota = new DeploymentQuota("staging", 10, 3, 50);
        manager.registerQuota(quota);
        assertTrue(manager.hasQuota("staging"));
        assertEquals(quota, manager.getQuota("staging"));
    }

    @Test
    void checkAndReserve_allowsWhenWithinLimits() {
        manager.registerQuota(new DeploymentQuota("dev", 5, 2, 20));
        assertDoesNotThrow(() -> manager.checkAndReserve("dev"));
        assertEquals(1, manager.getActiveCount("dev"));
    }

    @Test
    void checkAndReserve_noQuotaRegistered_alwaysAllows() {
        assertDoesNotThrow(() -> manager.checkAndReserve("production"));
    }

    @Test
    void checkAndReserve_throwsWhenConcurrentLimitReached() {
        manager.registerQuota(new DeploymentQuota("prod", 100, 1, 100));
        manager.checkAndReserve("prod");
        QuotaExceededException ex = assertThrows(QuotaExceededException.class,
                () -> manager.checkAndReserve("prod"));
        assertTrue(ex.getMessage().contains("concurrent"));
    }

    @Test
    void checkAndReserve_throwsWhenHourlyLimitReached() {
        manager.registerQuota(new DeploymentQuota("qa", 2, 10, 100));
        manager.checkAndReserve("qa");
        manager.checkAndReserve("qa");
        QuotaExceededException ex = assertThrows(QuotaExceededException.class,
                () -> manager.checkAndReserve("qa"));
        assertTrue(ex.getMessage().contains("hourly"));
    }

    @Test
    void releaseActive_decrementsActiveCount() {
        manager.registerQuota(new DeploymentQuota("staging", 10, 5, 50));
        manager.checkAndReserve("staging");
        manager.checkAndReserve("staging");
        assertEquals(2, manager.getActiveCount("staging"));
        manager.releaseActive("staging");
        assertEquals(1, manager.getActiveCount("staging"));
    }

    @Test
    void releaseActive_removesEntryWhenCountReachesZero() {
        manager.registerQuota(new DeploymentQuota("staging", 10, 5, 50));
        manager.checkAndReserve("staging");
        manager.releaseActive("staging");
        assertEquals(0, manager.getActiveCount("staging"));
    }

    @Test
    void removeQuota_clearsAllState() {
        manager.registerQuota(new DeploymentQuota("dev", 10, 3, 50));
        manager.checkAndReserve("dev");
        manager.removeQuota("dev");
        assertFalse(manager.hasQuota("dev"));
        assertEquals(0, manager.getActiveCount("dev"));
    }

    @Test
    void quota_invalidArgumentsThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentQuota("", 5, 2, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentQuota("dev", 0, 2, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentQuota("dev", 5, -1, 10));
    }
}
