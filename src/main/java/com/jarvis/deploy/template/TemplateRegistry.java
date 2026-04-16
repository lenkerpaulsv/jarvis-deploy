package com.jarvis.deploy.template;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for managing named deployment templates.
 * Templates can be registered, retrieved, and removed.
 */
public class TemplateRegistry {

    private final Map<String, DeploymentTemplate> templates = new LinkedHashMap<>();

    /**
     * Registers a deployment template. Overwrites any existing template with the same name.
     *
     * @param template the template to register
     * @throws IllegalArgumentException if template is null
     */
    public void register(DeploymentTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        templates.put(template.getName(), template);
    }

    /**
     * Retrieves a template by name.
     *
     * @param name the template name
     * @return an Optional containing the template, or empty if not found
     */
    public Optional<DeploymentTemplate> get(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(templates.get(name));
    }

    /**
     * Removes a template by name.
     *
     * @param name the template name
     * @return true if a template was removed, false otherwise
     */
    public boolean remove(String name) {
        if (name == null) {
            return false;
        }
        return templates.remove(name) != null;
    }

    /**
     * Returns all registered templates.
     */
    public Collection<DeploymentTemplate> listAll() {
        return Collections.unmodifiableCollection(templates.values());
    }

    /**
     * Returns the number of registered templates.
     */
    public int size() {
        return templates.size();
    }

    /**
     * Returns true if a template with the given name is registered.
     */
    public boolean contains(String name) {
        return name != null && templates.containsKey(name);
    }
}
