package com.jarvis.deploy.schedule;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages registration and retrieval of scheduled deployments.
 */
public class DeploymentScheduler {

    private final Map<String, DeploymentSchedule> schedules = new ConcurrentHashMap<>();

    /**
     * Registers a new schedule. Throws if a schedule with the same ID already exists.
     */
    public void register(DeploymentSchedule schedule) {
        Objects.requireNonNull(schedule, "schedule must not be null");
        if (schedules.containsKey(schedule.getScheduleId())) {
            throw new IllegalArgumentException("Schedule already registered: " + schedule.getScheduleId());
        }
        schedules.put(schedule.getScheduleId(), schedule);
    }

    /**
     * Returns all schedules that are due at or before the given instant.
     */
    public List<DeploymentSchedule> getDueSchedules(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        List<DeploymentSchedule> due = new ArrayList<>();
        for (DeploymentSchedule s : schedules.values()) {
            if (s.isDue(now)) {
                due.add(s);
            }
        }
        due.sort(Comparator.comparing(DeploymentSchedule::getScheduledAt));
        return Collections.unmodifiableList(due);
    }

    /**
     * Cancels a pending schedule by ID. Returns true if cancelled, false if not found or not cancellable.
     */
    public boolean cancel(String scheduleId) {
        DeploymentSchedule schedule = schedules.get(scheduleId);
        if (schedule == null || schedule.getStatus() != ScheduleStatus.PENDING) {
            return false;
        }
        schedule.setStatus(ScheduleStatus.CANCELLED);
        return true;
    }

    public Optional<DeploymentSchedule> findById(String scheduleId) {
        return Optional.ofNullable(schedules.get(scheduleId));
    }

    public int size() {
        return schedules.size();
    }

    public Collection<DeploymentSchedule> all() {
        return Collections.unmodifiableCollection(schedules.values());
    }
}
