package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the deployment configuration for a single environment.
 */
public class EnvironmentConfig {

    private final String name;
    private final String host;
    private final int port;
    private final String deployPath;
    private final String user;
    private final Map<String, String> envVars;

    public EnvironmentConfig(String name, String host, int port, String deployPath, String user) {
        this.name = Objects.requireNonNull(name, "Environment name must not be null");
        this.host = Objects.requireNonNull(host, "Host must not be null");
        this.port = port;
        this.deployPath = Objects.requireNonNull(deployPath, "Deploy path must not be null");
        this.user = Objects.requireNonNull(user, "User must not be null");
        this.envVars = new HashMap<>();
    }

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDeployPath() { return deployPath; }
    public String getUser() { return user; }
    public Map<String, String> getEnvVars() { return Map.copyOf(envVars); }

    public void addEnvVar(String key, String value) {
        envVars.put(
            Objects.requireNonNull(key, "Env var key must not be null"),
            Objects.requireNonNull(value, "Env var value must not be null")
        );
    }

    @Override
    public String toString() {
        return String.format("EnvironmentConfig{name='%s', host='%s', port=%d, deployPath='%s', user='%s'}",
                name, host, port, deployPath, user);
    }
}
