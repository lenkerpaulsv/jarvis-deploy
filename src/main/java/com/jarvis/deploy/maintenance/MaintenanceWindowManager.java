package com.jarvis.deploy.maintenance;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages maintenance windows and enforces deployment blocks during active windows.
 */
public class MaintenanceWindowManager {

    private final Map<String, MaintenanceWindow> windows = new ConcurrentHashMap<>();

    public MaintenanceWindow schedule(String environment, Instant start, Instant end, String reason) {
        String id = UUID.randomUUID().toString();
        MaintenanceWindow window = new MaintenanceWindow(id, environment, start, end, reason);
        windows.put(id, window);
        return window;
    }

    public boolean cancel(String windowId) {
        MaintenanceWindow window = windows.get(windowId);
        if (window == null) {
            return false;
        }
        window.cancel();
        return true;
    }

    /**
     * Returns true if the given environment is currently under maintenance.
     */
    public boolean isUnderMaintenance(String environment) {
        return isUnderMaintenanceAt(environment, Instant.now());
    }

    public boolean isUnderMaintenanceAt(String environment, Instant moment) {
        return windows.values().stream()
                .filter(w -> w.getEnvironment().equals(environment))
                .anyMatch(w -> w.isActiveAt(moment));
    }

    /**
     * Asserts that no maintenance window is active; throws if one is.
     */
    public void assertNotUnderMaintenance(String environment) throws MaintenanceWindowException {
        List<MaintenanceWindow> active = getActiveWindows(environment);
        if (!active.isEmpty()) {
            MaintenanceWindow w = active.get(0);
            throw new MaintenanceWindowException(
                    String.format("Environment '%s' is under maintenance until %s: %s",
                            environment, w.getEnd(), w.getReason()));
        }
    }

    public List<MaintenanceWindow> getActiveWindows(String environment) {
        Instant now = Instant.now();
        return windows.values().stream()
                .filter(w -> w.getEnvironment().equals(environment) && w.isActiveAt(now))
                .collect(Collectors.toList());
    }

    public List<MaintenanceWindow> getAllWindows() {
        return Collections.unmodifiableList(new ArrayList<>(windows.values()));
    }

    public Optional<MaintenanceWindow> findById(String id) {
        return Optional.ofNullable(windows.get(id));
    }
}
