package com.jarvis.deploy.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads environment configurations from a .properties file.
 * Expected format: env.<name>.<field>=<value>
 */
public class ConfigLoader {

    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final int DEFAULT_PORT = 22;

    public Map<String, EnvironmentConfig> load(Path configFile) throws IOException {
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(configFile)) {
            props.load(is);
        }
        return parseEnvironments(props);
    }

    private Map<String, EnvironmentConfig> parseEnvironments(Properties props) {
        Map<String, Map<String, String>> envFields = new HashMap<>();

        for (String key : props.stringPropertyNames()) {
            if (!key.startsWith("env.")) continue;
            String[] parts = key.split("\\.", 3);
            if (parts.length < 3) continue;
            String envName = parts[1];
            String field = parts[2];
            envFields.computeIfAbsent(envName, k -> new HashMap<>()).put(field, props.getProperty(key));
        }

        Map<String, EnvironmentConfig> configs = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : envFields.entrySet()) {
            String envName = entry.getKey();
            Map<String, String> fields = entry.getValue();
            try {
                EnvironmentConfig config = buildConfig(envName, fields);
                configs.put(envName, config);
                LOGGER.info("Loaded environment: " + envName);
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Skipping invalid environment '" + envName + "': " + e.getMessage());
            }
        }
        return configs;
    }

    private EnvironmentConfig buildConfig(String name, Map<String, String> fields) {
        String host = require(fields, name, "host");
        String deployPath = require(fields, name, "deployPath");
        String user = require(fields, name, "user");
        int port = fields.containsKey("port") ? Integer.parseInt(fields.get("port")) : DEFAULT_PORT;
        EnvironmentConfig config = new EnvironmentConfig(name, host, port, deployPath, user);
        fields.entrySet().stream()
              .filter(e -> e.getKey().startsWith("var."))
              .forEach(e -> config.addEnvVar(e.getKey().substring(4), e.getValue()));
        return config;
    }

    private String require(Map<String, String> fields, String env, String key) {
        String value = fields.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required field '" + key + "' for env '" + env + "'");
        }
        return value.trim();
    }
}
