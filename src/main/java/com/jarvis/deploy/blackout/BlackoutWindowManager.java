package com.jarvis.deploy.blackout;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages blackout windows that block deployments to specific environments
 * during restricted time periods (e.g. peak traffic, freeze periods).
 */
public class BlackoutWindowManager {

    private final Map<String, BlackoutWindow> windows = new ConcurrentHashMap<>();

    public void register(BlackoutWindow window) {
        Objects.requireNonNull(window, "window must not be null");
        windows.put(window.getId(), window);
    }

    public boolean remove(String windowId) {
        return windows.remove(windowId) != null;
    }

    public Optional<BlackoutWindow> findById(String windowId) {
        return Optional.ofNullable(windows.get(windowId));
    }

    /**
     * Returns all currently active blackout windows for the given environment at the given time.
     */
    public List<BlackoutWindow> getActiveWindows(String environment, LocalDateTime at) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(at, "at must not be null");
        return windows.values().stream()
                .filter(w -> w.getEnvironment().equals(environment) && w.isActive(at))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if deployment to the given environment is blocked at the given time.
     */
    public boolean isBlocked(String environment, LocalDateTime at) {
        return !getActiveWindows(environment, at).isEmpty();
    }

    /**
     * Throws {@link BlackoutViolationException} if the environment is under a blackout at the given time.
     */
    public void assertNotBlocked(String environment, LocalDateTime at) throws BlackoutViolationException {
        List<BlackoutWindow> active = getActiveWindows(environment, at);
        if (!active.isEmpty()) {
            BlackoutWindow first = active.get(0);
            throw new BlackoutViolationException(
                    String.format("Deployment to '%s' is blocked by blackout window '%s' until %s: %s",
                            environment, first.getId(), first.getEnd(), first.getReason()));
        }
    }

    public List<BlackoutWindow> getAllWindows() {
        return Collections.unmodifiableList(new ArrayList<>(windows.values()));
    }

    public int size() {
        return windows.size();
    }
}
