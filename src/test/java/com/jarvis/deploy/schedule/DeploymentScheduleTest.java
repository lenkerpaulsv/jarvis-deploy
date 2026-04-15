package com.jarvis.deploy.schedule;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentScheduleTest {

    private DeploymentSchedule buildSchedule(Instant at) {
        return new DeploymentSchedule("s1", "staging", "/artifacts/app.jar", at);
    }

    @Test
    void defaultStatusIsPending() {
        DeploymentSchedule s = buildSchedule(Instant.now().plusSeconds(60));
        assertEquals(ScheduleStatus.PENDING, s.getStatus());
    }

    @Test
    void isDueWhenTimeHasPassed() {
        Instant past = Instant.now().minusSeconds(10);
        DeploymentSchedule s = buildSchedule(past);
        assertTrue(s.isDue(Instant.now()));
    }

    @Test
    void isNotDueWhenTimeIsInFuture() {
        Instant future = Instant.now().plusSeconds(60);
        DeploymentSchedule s = buildSchedule(future);
        assertFalse(s.isDue(Instant.now()));
    }

    @Test
    void isNotDueWhenNotPending() {
        Instant past = Instant.now().minusSeconds(10);
        DeploymentSchedule s = buildSchedule(past);
        s.setStatus(ScheduleStatus.COMPLETED);
        assertFalse(s.isDue(Instant.now()));
    }

    @Test
    void setStatusUpdatesCorrectly() {
        DeploymentSchedule s = buildSchedule(Instant.now());
        s.setStatus(ScheduleStatus.RUNNING);
        assertEquals(ScheduleStatus.RUNNING, s.getStatus());
    }

    @Test
    void constructorRejectsBlankEnvironment() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentSchedule("id", "", "/app.jar", Instant.now()));
    }

    @Test
    void toStringContainsKeyFields() {
        DeploymentSchedule s = buildSchedule(Instant.now());
        String str = s.toString();
        assertTrue(str.contains("s1"));
        assertTrue(str.contains("staging"));
        assertTrue(str.contains("PENDING"));
    }
}
