package com.jarvis.deploy.lock;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages deployment locks to prevent concurrent deployments to the same environment.
 */
public class DeploymentLockManager {

    private static final long DEFAULT_TTL_SECONDS = 600; // 10 minutes

    private final Map<String, DeploymentLock> locks = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    public DeploymentLockManager() {
        this(DEFAULT_TTL_SECONDS);
    }

    public DeploymentLockManager(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * Attempts to acquire a lock for the given environment.
     *
     * @return the acquired DeploymentLock, or empty if already locked
     */
    public synchronized Optional<DeploymentLock> acquireLock(String environment, String owner) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Owner must not be blank");
        }

        DeploymentLock existing = locks.get(environment);
        if (existing != null && !existing.isExpired(ttlSeconds)) {
            return Optional.empty();
        }

        String lockId = UUID.randomUUID().toString();
        DeploymentLock lock = new DeploymentLock(lockId, environment, owner, Instant.now());
        locks.put(environment, lock);
        return Optional.of(lock);
    }

    /**
     * Releases the lock for the given environment if the lockId matches.
     *
     * @return true if released, false if lock not found or id mismatch
     */
    public synchronized boolean releaseLock(String environment, String lockId) {
        DeploymentLock existing = locks.get(environment);
        if (existing == null || !existing.getLockId().equals(lockId)) {
            return false;
        }
        locks.remove(environment);
        return true;
    }

    /**
     * Returns the current lock for an environment, if one exists and has not expired.
     */
    public Optional<DeploymentLock> getCurrentLock(String environment) {
        DeploymentLock lock = locks.get(environment);
        if (lock == null || lock.isExpired(ttlSeconds)) {
            return Optional.empty();
        }
        return Optional.of(lock);
    }

    public boolean isLocked(String environment) {
        return getCurrentLock(environment).isPresent();
    }
}
