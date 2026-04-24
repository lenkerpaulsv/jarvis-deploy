package com.jarvis.deploy.cooldown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentCooldownManagerTest {

    private DeploymentCooldownManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeploymentCooldownManager(Duration.ofMinutes(10));
    }

    @Test
    void isCooledDown_noDeploymentRecorded_returnsTrue() {
        assertTrue(manager.isCooledDown("production"));
    }

    @Test
    void isCooledDown_recentDeployment_returnsFalse() {
        manager.recordDeployment("production");
        assertFalse(manager.isCooledDown("production"));
    }

    @Test
    void isCooledDown_deploymentBeyondCooldown_returnsTrue() {
        Instant past = Instant.now().minus(Duration.ofMinutes(15));
        manager.recordDeployment("production", past);
        assertTrue(manager.isCooledDown("production"));
    }

    @Test
    void remainingCooldown_noDeployment_returnsEmpty() {
        assertEquals(Optional.empty(), manager.remainingCooldown("staging"));
    }

    @Test
    void remainingCooldown_recentDeployment_returnsPositiveDuration() {
        manager.recordDeployment("staging");
        Optional<Duration> remaining = manager.remainingCooldown("staging");
        assertTrue(remaining.isPresent());
        assertTrue(remaining.get().toSeconds() > 0);
    }

    @Test
    void remainingCooldown_expiredCooldown_returnsEmpty() {
        manager.recordDeployment("staging", Instant.now().minus(Duration.ofMinutes(20)));
        assertEquals(Optional.empty(), manager.remainingCooldown("staging"));
    }

    @Test
    void setEnvironmentCooldown_overridesDefault() {
        manager.setEnvironmentCooldown("dev", Duration.ofSeconds(30));
        assertEquals(Duration.ofSeconds(30), manager.getEffectiveCooldown("dev"));
        assertEquals(Duration.ofMinutes(10), manager.getEffectiveCooldown("production"));
    }

    @Test
    void reset_clearsLastDeploymentTime() {
        manager.recordDeployment("production");
        assertFalse(manager.isCooledDown("production"));
        manager.reset("production");
        assertTrue(manager.isCooledDown("production"));
    }

    @Test
    void constructor_negativeCooldown_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentCooldownManager(Duration.ofSeconds(-1)));
    }

    @Test
    void recordDeployment_blankEnvironment_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.recordDeployment("  "));
    }

    @Test
    void setEnvironmentCooldown_nullCooldown_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.setEnvironmentCooldown("dev", null));
    }
}
