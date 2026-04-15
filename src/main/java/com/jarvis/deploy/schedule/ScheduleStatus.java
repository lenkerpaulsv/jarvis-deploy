package com.jarvis.deploy.schedule;

/**
 * Lifecycle states for a scheduled deployment.
 */
public enum ScheduleStatus {
    /** Waiting to be triggered. */
    PENDING,
    /** Currently being executed. */
    RUNNING,
    /** Completed successfully. */
    COMPLETED,
    /** Execution failed. */
    FAILED,
    /** Cancelled before execution. */
    CANCELLED
}
