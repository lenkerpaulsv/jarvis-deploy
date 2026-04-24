package com.jarvis.deploy.cooldown;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-environment deployment cooldown periods to prevent
 * rapid successive deployments that could destabilize services.
 */
public class DeploymentCooldownManager {

    private final Duration defaultCooldown;
    private final Map<String, Duration> environmentCooldowns = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastDeploymentTimes = new ConcurrentHashMap<>();

    public DeploymentCooldownManager(Duration defaultCooldown) {
        if (defaultCooldown == null || defaultCooldown.isNegative()) {
            throw new IllegalArgumentException("Default cooldown must be non-null and non-negative");
        }
        this.defaultCooldown = defaultCooldown;
    }

    public void setEnvironmentCooldown(String environment, Duration cooldown) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (cooldown == null || cooldown.isNegative()) {
            throw new IllegalArgumentException("Cooldown must be non-null and non-negative");
        }
        environmentCooldowns.put(environment, cooldown);
    }

    public boolean isCooledDown(String environment) {
        Instant last = lastDeploymentTimes.get(environment);
        if (last == null) {
            return true;
        }
        Duration cooldown = environmentCooldowns.getOrDefault(environment, defaultCooldown);
        return Instant.now().isAfter(last.plus(cooldown));
    }

    public Optional<Duration> remainingCooldown(String environment) {
        Instant last = lastDeploymentTimes.get(environment);
        if (last == null) {
            return Optional.empty();
        }
        Duration cooldown = environmentCooldowns.getOrDefault(environment, defaultCooldown);
        Instant cooldownEnd = last.plus(cooldown);
        Instant now = Instant.now();
        if (now.isAfter(cooldownEnd)) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(now, cooldownEnd));
    }

    public void recordDeployment(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        lastDeploymentTimes.put(environment, Instant.now());
    }

    public void recordDeployment(String environment, Instant at) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (at == null) {
            throw new IllegalArgumentException("Timestamp must not be null");
        }
        lastDeploymentTimes.put(environment, at);
    }

    public void reset(String environment) {
        lastDeploymentTimes.remove(environment);
    }

    public Duration getEffectiveCooldown(String environment) {
        return environmentCooldowns.getOrDefault(environment, defaultCooldown);
    }
}
