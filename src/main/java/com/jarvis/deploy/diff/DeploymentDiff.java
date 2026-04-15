package com.jarvis.deploy.diff;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents a diff between two deployment configurations or versions,
 * capturing added, removed, and changed properties.
 */
public class DeploymentDiff {

    private final String fromVersion;
    private final String toVersion;
    private final String environment;
    private final Map<String, String> added;
    private final Map<String, String> removed;
    private final Map<String, String[]> changed; // key -> [oldValue, newValue]

    public DeploymentDiff(String fromVersion, String toVersion, String environment) {
        this.fromVersion = Objects.requireNonNull(fromVersion, "fromVersion must not be null");
        this.toVersion = Objects.requireNonNull(toVersion, "toVersion must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.added = new HashMap<>();
        this.removed = new HashMap<>();
        this.changed = new HashMap<>();
    }

    public static DeploymentDiff compute(String fromVersion, String toVersion,
                                         String environment,
                                         Map<String, String> oldProps,
                                         Map<String, String> newProps) {
        DeploymentDiff diff = new DeploymentDiff(fromVersion, toVersion, environment);

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(oldProps.keySet());
        allKeys.addAll(newProps.keySet());

        for (String key : allKeys) {
            boolean inOld = oldProps.containsKey(key);
            boolean inNew = newProps.containsKey(key);

            if (inNew && !inOld) {
                diff.added.put(key, newProps.get(key));
            } else if (inOld && !inNew) {
                diff.removed.put(key, oldProps.get(key));
            } else if (!Objects.equals(oldProps.get(key), newProps.get(key))) {
                diff.changed.put(key, new String[]{oldProps.get(key), newProps.get(key)});
            }
        }
        return diff;
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty() && changed.isEmpty();
    }

    public String getFromVersion() { return fromVersion; }
    public String getToVersion() { return toVersion; }
    public String getEnvironment() { return environment; }
    public Map<String, String> getAdded() { return Collections.unmodifiableMap(added); }
    public Map<String, String> getRemoved() { return Collections.unmodifiableMap(removed); }
    public Map<String, String[]> getChanged() { return Collections.unmodifiableMap(changed); }

    @Override
    public String toString() {
        return String.format("DeploymentDiff[%s -> %s, env=%s, added=%d, removed=%d, changed=%d]",
                fromVersion, toVersion, environment, added.size(), removed.size(), changed.size());
    }
}
