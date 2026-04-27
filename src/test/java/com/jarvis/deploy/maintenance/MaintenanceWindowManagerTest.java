package com.jarvis.deploy.maintenance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceWindowManagerTest {

    private MaintenanceWindowManager manager;
    private final String ENV = "production";

    @BeforeEach
    void setUp() {
        manager = new MaintenanceWindowManager();
    }

    @Test
    void scheduleCreatesWindowWithUniqueId() {
        MaintenanceWindow w1 = manager.schedule(ENV, past(), future(), "upgrade");
        MaintenanceWindow w2 = manager.schedule(ENV, past(), future(), "patch");
        assertNotEquals(w1.getId(), w2.getId());
    }

    @Test
    void isUnderMaintenanceReturnsTrueForActiveWindow() {
        manager.schedule(ENV, past(), future(), "planned");
        assertTrue(manager.isUnderMaintenance(ENV));
    }

    @Test
    void isUnderMaintenanceReturnsFalseWhenNoActiveWindow() {
        manager.schedule(ENV, past(2), past(1), "old");
        assertFalse(manager.isUnderMaintenance(ENV));
    }

    @Test
    void isUnderMaintenanceReturnsFalseForDifferentEnvironment() {
        manager.schedule("staging", past(), future(), "staging maintenance");
        assertFalse(manager.isUnderMaintenance(ENV));
    }

    @Test
    void cancelDeactivatesWindow() {
        MaintenanceWindow w = manager.schedule(ENV, past(), future(), "test");
        assertTrue(manager.isUnderMaintenance(ENV));
        manager.cancel(w.getId());
        assertFalse(manager.isUnderMaintenance(ENV));
    }

    @Test
    void cancelReturnsFalseForUnknownId() {
        assertFalse(manager.cancel("nonexistent-id"));
    }

    @Test
    void assertNotUnderMaintenanceThrowsWhenActive() {
        manager.schedule(ENV, past(), future(), "critical patch");
        MaintenanceWindowException ex = assertThrows(MaintenanceWindowException.class,
                () -> manager.assertNotUnderMaintenance(ENV));
        assertTrue(ex.getMessage().contains(ENV));
    }

    @Test
    void assertNotUnderMaintenancePassesWhenNoActiveWindow() {
        assertDoesNotThrow(() -> manager.assertNotUnderMaintenance(ENV));
    }

    @Test
    void getActiveWindowsReturnsOnlyCurrentlyActiveOnes() {
        manager.schedule(ENV, past(), future(), "active");
        manager.schedule(ENV, past(2), past(1), "expired");
        List<MaintenanceWindow> active = manager.getActiveWindows(ENV);
        assertEquals(1, active.size());
        assertEquals("active", active.get(0).getReason());
    }

    @Test
    void windowConstructorThrowsIfEndBeforeStart() {
        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceWindow("id", ENV, future(), past(), "bad"));
    }

    private Instant past() { return Instant.now().minus(1, ChronoUnit.HOURS); }
    private Instant past(int hours) { return Instant.now().minus(hours, ChronoUnit.HOURS); }
    private Instant future() { return Instant.now().plus(2, ChronoUnit.HOURS); }
}
