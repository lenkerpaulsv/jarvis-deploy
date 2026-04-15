package com.jarvis.deploy.secret;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single secret key-value entry scoped to an environment.
 */
public class SecretEntry {

    private final String key;
    private final String value;
    private final String environment;
    private final Instant createdAt;

    public SecretEntry(String key, String value, String environment) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Secret key must not be blank");
        if (value == null) throw new IllegalArgumentException("Secret value must not be null");
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("Environment must not be blank");
        this.key = key;
        this.value = value;
        this.environment = environment;
        this.createdAt = Instant.now();
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getEnvironment() { return environment; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecretEntry)) return false;
        SecretEntry that = (SecretEntry) o;
        return Objects.equals(key, that.key) && Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, environment);
    }

    @Override
    public String toString() {
        return "SecretEntry{key='" + key + "', environment='" + environment + "', createdAt=" + createdAt + "}";
    }
}
