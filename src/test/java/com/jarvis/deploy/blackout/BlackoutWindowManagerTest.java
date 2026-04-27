package com.jarvis.deploy.blackout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BlackoutWindowManagerTest {

    private BlackoutWindowManager manager;
    private final LocalDateTime BASE = LocalDateTime.of(2024, 6, 15, 12, 0);

    @BeforeEach
    void setUp() {
        manager = new BlackoutWindowManager();
    }

    private BlackoutWindow window(String id, String env, int startHour, int endHour) {
        return new BlackoutWindow(id, env,
                BASE.withHour(startHour),
                BASE.withHour(endHour),
                "scheduled freeze");
    }

    @Test
    void testRegisterAndFindById() {
        BlackoutWindow w = window("bw-1", "production", 10, 14);
        manager.register(w);
        assertTrue(manager.findById("bw-1").isPresent());
        assertEquals("bw-1", manager.findById("bw-1").get().getId());
    }

    @Test
    void testIsBlockedDuringWindow() {
        manager.register(window("bw-2", "production", 10, 14));
        assertTrue(manager.isBlocked("production", BASE.withHour(11)));
    }

    @Test
    void testIsNotBlockedOutsideWindow() {
        manager.register(window("bw-3", "production", 10, 14));
        assertFalse(manager.isBlocked("production", BASE.withHour(15)));
        assertFalse(manager.isBlocked("production", BASE.withHour(9)));
    }

    @Test
    void testIsNotBlockedDifferentEnvironment() {
        manager.register(window("bw-4", "production", 10, 14));
        assertFalse(manager.isBlocked("staging", BASE.withHour(11)));
    }

    @Test
    void testAssertNotBlockedThrowsWhenBlocked() {
        manager.register(window("bw-5", "production", 10, 14));
        BlackoutViolationException ex = assertThrows(BlackoutViolationException.class,
                () -> manager.assertNotBlocked("production", BASE.withHour(12)));
        assertTrue(ex.getMessage().contains("bw-5"));
        assertTrue(ex.getMessage().contains("production"));
    }

    @Test
    void testAssertNotBlockedPassesWhenClear() {
        manager.register(window("bw-6", "production", 10, 14));
        assertDoesNotThrow(() -> manager.assertNotBlocked("production", BASE.withHour(16)));
    }

    @Test
    void testRemoveWindow() {
        manager.register(window("bw-7", "production", 10, 14));
        assertTrue(manager.remove("bw-7"));
        assertFalse(manager.findById("bw-7").isPresent());
        assertFalse(manager.isBlocked("production", BASE.withHour(11)));
    }

    @Test
    void testRemoveNonExistentReturnsFalse() {
        assertFalse(manager.remove("nonexistent"));
    }

    @Test
    void testMultipleWindowsSameEnvironment() {
        manager.register(window("bw-8", "production", 8, 10));
        manager.register(window("bw-9", "production", 20, 23));
        assertTrue(manager.isBlocked("production", BASE.withHour(9)));
        assertTrue(manager.isBlocked("production", BASE.withHour(21)));
        assertFalse(manager.isBlocked("production", BASE.withHour(12)));
    }

    @Test
    void testInvalidWindowThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new BlackoutWindow("bad", "production", BASE.withHour(14), BASE.withHour(10), "bad"));
    }

    @Test
    void testGetAllWindows() {
        manager.register(window("bw-10", "staging", 10, 12));
        manager.register(window("bw-11", "production", 10, 12));
        assertEquals(2, manager.getAllWindows().size());
    }
}
