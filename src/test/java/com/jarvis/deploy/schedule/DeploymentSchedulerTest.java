package com.jarvis.deploy.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSchedulerTest {

    private DeploymentScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DeploymentScheduler();
    }

    private DeploymentSchedule schedule(String id, Instant at) {
        return new DeploymentSchedule(id, "prod", "/app.jar", at);
    }

    @Test
    void registerAndFindById() {
        DeploymentSchedule s = schedule("s1", Instant.now().plusSeconds(30));
        scheduler.register(s);
        Optional<DeploymentSchedule> found = scheduler.findById("s1");
        assertTrue(found.isPresent());
        assertEquals("s1", found.get().getScheduleId());
    }

    @Test
    void registerDuplicateThrows() {
        scheduler.register(schedule("s1", Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> scheduler.register(schedule("s1", Instant.now())));
    }

    @Test
    void getDueSchedulesReturnsPastPending() {
        Instant now = Instant.now();
        scheduler.register(schedule("s1", now.minusSeconds(5)));
        scheduler.register(schedule("s2", now.plusSeconds(60)));
        List<DeploymentSchedule> due = scheduler.getDueSchedules(now);
        assertEquals(1, due.size());
        assertEquals("s1", due.get(0).getScheduleId());
    }

    @Test
    void getDueSchedulesOrderedByTime() {
        Instant now = Instant.now();
        scheduler.register(schedule("s2", now.minusSeconds(2)));
        scheduler.register(schedule("s1", now.minusSeconds(10)));
        List<DeploymentSchedule> due = scheduler.getDueSchedules(now);
        assertEquals("s1", due.get(0).getScheduleId());
        assertEquals("s2", due.get(1).getScheduleId());
    }

    @Test
    void cancelPendingSchedule() {
        scheduler.register(schedule("s1", Instant.now().plusSeconds(30)));
        boolean result = scheduler.cancel("s1");
        assertTrue(result);
        assertEquals(ScheduleStatus.CANCELLED, scheduler.findById("s1").get().getStatus());
    }

    @Test
    void cancelNonExistentReturnsFalse() {
        assertFalse(scheduler.cancel("nonexistent"));
    }

    @Test
    void cancelAlreadyRunningReturnsFalse() {
        DeploymentSchedule s = schedule("s1", Instant.now());
        s.setStatus(ScheduleStatus.RUNNING);
        scheduler.register(s);
        assertFalse(scheduler.cancel("s1"));
    }

    @Test
    void sizeReflectsRegisteredCount() {
        assertEquals(0, scheduler.size());
        scheduler.register(schedule("s1", Instant.now()));
        scheduler.register(schedule("s2", Instant.now()));
        assertEquals(2, scheduler.size());
    }
}
