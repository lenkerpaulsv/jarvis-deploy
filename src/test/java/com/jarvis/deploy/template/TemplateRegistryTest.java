package com.jarvis.deploy.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRegistryTest {

    private TemplateRegistry registry;
    private DeploymentTemplate sampleTemplate;

    @BeforeEach
    void setUp() {
        registry = new TemplateRegistry();
        Map<String, String> props = new HashMap<>();
        props.put("timeout", "30");
        props.put("retries", "3");
        sampleTemplate = new DeploymentTemplate("standard", "Standard deployment", props, "*.jar");
    }

    @Test
    void register_shouldStoreTemplate() {
        registry.register(sampleTemplate);
        assertTrue(registry.contains("standard"));
        assertEquals(1, registry.size());
    }

    @Test
    void register_nullTemplate_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }

    @Test
    void get_existingTemplate_shouldReturnIt() {
        registry.register(sampleTemplate);
        Optional<DeploymentTemplate> result = registry.get("standard");
        assertTrue(result.isPresent());
        assertEquals("standard", result.get().getName());
    }

    @Test
    void get_missingTemplate_shouldReturnEmpty() {
        Optional<DeploymentTemplate> result = registry.get("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void get_nullName_shouldReturnEmpty() {
        assertFalse(registry.get(null).isPresent());
    }

    @Test
    void remove_existingTemplate_shouldReturnTrue() {
        registry.register(sampleTemplate);
        assertTrue(registry.remove("standard"));
        assertFalse(registry.contains("standard"));
    }

    @Test
    void remove_missingTemplate_shouldReturnFalse() {
        assertFalse(registry.remove("ghost"));
    }

    @Test
    void listAll_shouldReturnAllTemplates() {
        Map<String, String> p2 = new HashMap<>();
        p2.put("timeout", "60");
        DeploymentTemplate t2 = new DeploymentTemplate("fast", "Fast deploy", p2, "*.war");
        registry.register(sampleTemplate);
        registry.register(t2);
        Collection<DeploymentTemplate> all = registry.listAll();
        assertEquals(2, all.size());
    }

    @Test
    void mergeWith_shouldOverrideDefaultProperties() {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("timeout", "60");
        overrides.put("env", "production");
        Map<String, String> merged = sampleTemplate.mergeWith(overrides);
        assertEquals("60", merged.get("timeout"));
        assertEquals("3", merged.get("retries"));
        assertEquals("production", merged.get("env"));
    }

    @Test
    void register_duplicateName_shouldOverwrite() {
        registry.register(sampleTemplate);
        Map<String, String> newProps = new HashMap<>();
        newProps.put("timeout", "90");
        DeploymentTemplate updated = new DeploymentTemplate("standard", "Updated", newProps, "*.jar");
        registry.register(updated);
        assertEquals(1, registry.size());
        assertEquals("Updated", registry.get("standard").get().getDescription());
    }
}
