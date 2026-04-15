package com.jarvis.deploy.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Carries contextual information passed to deployment plugins.
 */
public class PluginContext {

    private final String environment;
    private final String artifactPath;
    private final String version;
    private final Map<String, String> metadata;

    public PluginContext(String environment, String artifactPath, String version) {
        this.environment = environment;
        this.artifactPath = artifactPath;
        this.version = version;
        this.metadata = new HashMap<>();
    }

    public String getEnvironment() {
        return environment;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public String getVersion() {
        return version;
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return metadata.get(key);
    }

    public Map<String, String> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String toString() {
        return "PluginContext{environment='" + environment + "', version='" + version + "', artifact='" + artifactPath + "'}";
    }
}
