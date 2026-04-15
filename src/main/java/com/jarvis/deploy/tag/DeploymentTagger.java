package com.jarvis.deploy.tag;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages tagging of deployments with arbitrary key-value metadata.
 * Tags allow operators to annotate deployments with environment info,
 * release notes, ticket references, etc.
 */
public class DeploymentTagger {

    // deploymentId -> (tagKey -> tagValue)
    private final Map<String, Map<String, String>> tagStore = new HashMap<>();

    /**
     * Adds or updates a single tag on a deployment.
     *
     * @param deploymentId the unique deployment identifier
     * @param key          tag key (non-null, non-blank)
     * @param value        tag value (non-null)
     * @throws IllegalArgumentException if key is blank or deploymentId is null
     */
    public void tag(String deploymentId, String key, String value) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(value, "tag value must not be null");
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Tag key must not be blank");
        }
        tagStore.computeIfAbsent(deploymentId, id -> new HashMap<>()).put(key, value);
    }

    /**
     * Bulk-applies a map of tags to a deployment, merging with existing tags.
     */
    public void tagAll(String deploymentId, Map<String, String> tags) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(tags, "tags map must not be null");
        tags.forEach((k, v) -> tag(deploymentId, k, v));
    }

    /**
     * Retrieves a specific tag value for a deployment.
     */
    public Optional<String> getTag(String deploymentId, String key) {
        Map<String, String> tags = tagStore.get(deploymentId);
        if (tags == null) return Optional.empty();
        return Optional.ofNullable(tags.get(key));
    }

    /**
     * Returns an unmodifiable view of all tags for a deployment.
     */
    public Map<String, String> getAllTags(String deploymentId) {
        return Collections.unmodifiableMap(
                tagStore.getOrDefault(deploymentId, Collections.emptyMap()));
    }

    /**
     * Removes a specific tag from a deployment.
     *
     * @return true if the tag existed and was removed
     */
    public boolean removeTag(String deploymentId, String key) {
        Map<String, String> tags = tagStore.get(deploymentId);
        if (tags == null) return false;
        return tags.remove(key) != null;
    }

    /**
     * Clears all tags associated with a deployment.
     */
    public void clearTags(String deploymentId) {
        tagStore.remove(deploymentId);
    }

    /**
     * Convenience method: stamps a deployment with a timestamp tag.
     */
    public void stampTimestamp(String deploymentId) {
        tag(deploymentId, "tagged_at", Instant.now().toString());
    }
}
