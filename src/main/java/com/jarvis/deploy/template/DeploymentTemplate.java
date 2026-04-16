package com.jarvis.deploy.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a reusable deployment template with default configuration
 * values that can be applied across environments.
 */
public class DeploymentTemplate {

    private final String name;
    private final String description;
    private final Map<String, String> defaultProperties;
    private final String baseArtifactPattern;

    public DeploymentTemplate(String name, String description,
                               Map<String, String> defaultProperties,
                               String baseArtifactPattern) {
        Objects.requireNonNull(name, "Template name must not be null");
        Objects.requireNonNull(defaultProperties, "Default properties must not be null");
        this.name = name;
        this.description = description != null ? description : "";
        this.defaultProperties = Collections.unmodifiableMap(new HashMap<>(defaultProperties));
        this.baseArtifactPattern = baseArtifactPattern;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    public String getBaseArtifactPattern() {
        return baseArtifactPattern;
    }

    /**
     * Merges this template's default properties with the provided overrides.
     * Overrides take precedence over template defaults.
     */
    public Map<String, String> mergeWith(Map<String, String> overrides) {
        Map<String, String> merged = new HashMap<>(defaultProperties);
        if (overrides != null) {
            merged.putAll(overrides);
        }
        return Collections.unmodifiableMap(merged);
    }

    @Override
    public String toString() {
        return "DeploymentTemplate{name='" + name + "', description='" + description + "'}";
    }
}
