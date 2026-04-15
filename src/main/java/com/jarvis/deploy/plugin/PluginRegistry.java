package com.jarvis.deploy.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for managing registered deployment plugins.
 * Plugins are invoked in registration order.
 */
public class PluginRegistry {

    private final Map<String, DeploymentPlugin> plugins = new LinkedHashMap<>();

    /**
     * Registers a plugin. Throws if a plugin with the same name already exists.
     */
    public void register(DeploymentPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }
        String name = plugin.getName();
        if (plugins.containsKey(name)) {
            throw new IllegalStateException("Plugin already registered: " + name);
        }
        plugins.put(name, plugin);
    }

    /**
     * Unregisters a plugin by name. Returns true if removed.
     */
    public boolean unregister(String name) {
        return plugins.remove(name) != null;
    }

    /**
     * Returns an unmodifiable ordered list of registered plugins.
     */
    public List<DeploymentPlugin> getPlugins() {
        return Collections.unmodifiableList(new ArrayList<>(plugins.values()));
    }

    /**
     * Returns true if a plugin with the given name is registered.
     */
    public boolean isRegistered(String name) {
        return plugins.containsKey(name);
    }

    /**
     * Returns the number of registered plugins.
     */
    public int size() {
        return plugins.size();
    }

    /**
     * Clears all registered plugins.
     */
    public void clear() {
        plugins.clear();
    }
}
