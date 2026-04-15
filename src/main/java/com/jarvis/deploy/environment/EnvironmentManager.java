package com.jarvis.deploy.environment;

import com.jarvis.deploy.config.EnvironmentConfig;
import com.jarvis.deploy.config.ConfigLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages available deployment environments and their active state.
 */
public class EnvironmentManager {

    private final ConfigLoader configLoader;
    private final Map<String, EnvironmentConfig> environments = new HashMap<>();
    private String activeEnvironment;

    public EnvironmentManager(ConfigLoader configLoader) {
        if (configLoader == null) {
            throw new IllegalArgumentException("ConfigLoader must not be null");
        }
        this.configLoader = configLoader;
    }

    public void registerEnvironment(String name, EnvironmentConfig config) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Environment name must not be blank");
        }
        if (config == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }
        environments.put(name.toLowerCase(), config);
    }

    public Optional<EnvironmentConfig> getEnvironment(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(environments.get(name.toLowerCase()));
    }

    public boolean setActiveEnvironment(String name) {
        if (name == null || !environments.containsKey(name.toLowerCase())) {
            return false;
        }
        this.activeEnvironment = name.toLowerCase();
        return true;
    }

    public Optional<String> getActiveEnvironmentName() {
        return Optional.ofNullable(activeEnvironment);
    }

    public Optional<EnvironmentConfig> getActiveEnvironmentConfig() {
        if (activeEnvironment == null) return Optional.empty();
        return Optional.ofNullable(environments.get(activeEnvironment));
    }

    public Set<String> listEnvironments() {
        return Collections.unmodifiableSet(environments.keySet());
    }

    public boolean hasEnvironment(String name) {
        if (name == null) return false;
        return environments.containsKey(name.toLowerCase());
    }

    public void removeEnvironment(String name) {
        if (name == null) return;
        String key = name.toLowerCase();
        environments.remove(key);
        if (key.equals(activeEnvironment)) {
            activeEnvironment = null;
        }
    }
}
