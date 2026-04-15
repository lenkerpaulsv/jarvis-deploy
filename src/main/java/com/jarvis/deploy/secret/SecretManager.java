package com.jarvis.deploy.secret;

import java.util.*;
import java.util.logging.Logger;

/**
 * High-level manager for deployment secrets. Provides resolution of secret
 * placeholders in deployment configuration values.
 */
public class SecretManager {

    private static final Logger logger = Logger.getLogger(SecretManager.class.getName());
    private static final String PLACEHOLDER_PREFIX = "${secret:";
    private static final String PLACEHOLDER_SUFFIX = "}";

    private final SecretStore secretStore;

    public SecretManager(SecretStore secretStore) {
        this.secretStore = Objects.requireNonNull(secretStore, "SecretStore must not be null");
    }

    public void registerSecret(String environment, String key, String value) {
        SecretEntry entry = new SecretEntry(key, value, environment);
        secretStore.put(entry);
        logger.info("Registered secret '" + key + "' for environment '" + environment + "'");
    }

    public Optional<String> resolveSecret(String environment, String key) {
        return secretStore.get(environment, key).map(SecretEntry::getValue);
    }

    /**
     * Resolves secret placeholders in the form ${secret:KEY} within a given string.
     */
    public String resolvePlaceholders(String environment, String input) {
        if (input == null || !input.contains(PLACEHOLDER_PREFIX)) return input;
        String result = input;
        int start;
        while ((start = result.indexOf(PLACEHOLDER_PREFIX)) != -1) {
            int end = result.indexOf(PLACEHOLDER_SUFFIX, start);
            if (end == -1) break;
            String key = result.substring(start + PLACEHOLDER_PREFIX.length(), end);
            String resolved = resolveSecret(environment, key)
                .orElseThrow(() -> new SecretResolutionException(
                    "No secret found for key '" + key + "' in environment '" + environment + "'"));
            result = result.substring(0, start) + resolved + result.substring(end + 1);
        }
        return result;
    }

    public boolean revokeSecret(String environment, String key) {
        boolean removed = secretStore.remove(environment, key);
        if (removed) logger.info("Revoked secret '" + key + "' from environment '" + environment + "'");
        return removed;
    }

    public Map<String, SecretEntry> listSecrets(String environment) {
        return secretStore.getAllForEnvironment(environment);
    }
}
