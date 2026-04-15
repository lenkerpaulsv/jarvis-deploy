package com.jarvis.deploy.lock;

import java.time.Instant;

/**
 * Represents an active deployment lock for a given environment.
 */
public class DeploymentLock {

    private final String environment;
    private final String owner;
    private final Instant acquiredAt;
    private final String lockId;

    public DeploymentLock(String lockId, String environment, String owner, Instant acquiredAt) {
        this.lockId = lockId;
        this.environment = environment;
        this.owner = owner;
        this.acquiredAt = acquiredAt;
    }

    public String getLockId() {
        return lockId;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getOwner() {
        return owner;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public boolean isExpired(long ttlSeconds) {
        return Instant.now().isAfter(acquiredAt.plusSeconds(ttlSeconds));
    }

    @Override
    public String toString() {
        return String.format("DeploymentLock[id=%s, env=%s, owner=%s, acquiredAt=%s]",
                lockId, environment, owner, acquiredAt);
    }
}
