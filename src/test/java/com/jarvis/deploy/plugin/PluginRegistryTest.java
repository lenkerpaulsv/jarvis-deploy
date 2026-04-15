package com.jarvis.deploy.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PluginRegistryTest {

    private PluginRegistry registry;

    private DeploymentPlugin makePlugin(String name) {
        return new DeploymentPlugin() {
            @Override public String getName() { return name; }
            @Override public void beforeDeploy(PluginContext ctx) {}
            @Override public void afterDeploy(PluginContext ctx) {}
            @Override public void onFailure(PluginContext ctx, Exception cause) {}
        };
    }

    @BeforeEach
    void setUp() {
        registry = new PluginRegistry();
    }

    @Test
    void registerAndRetrievePlugin() {
        DeploymentPlugin plugin = makePlugin("notify");
        registry.register(plugin);
        assertTrue(registry.isRegistered("notify"));
        assertEquals(1, registry.size());
    }

    @Test
    void registerDuplicateThrows() {
        registry.register(makePlugin("audit"));
        assertThrows(IllegalStateException.class, () -> registry.register(makePlugin("audit")));
    }

    @Test
    void registerNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }

    @Test
    void unregisterPlugin() {
        registry.register(makePlugin("cleanup"));
        assertTrue(registry.unregister("cleanup"));
        assertFalse(registry.isRegistered("cleanup"));
        assertEquals(0, registry.size());
    }

    @Test
    void unregisterNonExistentReturnsFalse() {
        assertFalse(registry.unregister("ghost"));
    }

    @Test
    void pluginsReturnedInRegistrationOrder() {
        registry.register(makePlugin("alpha"));
        registry.register(makePlugin("beta"));
        registry.register(makePlugin("gamma"));
        List<DeploymentPlugin> plugins = registry.getPlugins();
        assertEquals("alpha", plugins.get(0).getName());
        assertEquals("beta", plugins.get(1).getName());
        assertEquals("gamma", plugins.get(2).getName());
    }

    @Test
    void clearRemovesAllPlugins() {
        registry.register(makePlugin("p1"));
        registry.register(makePlugin("p2"));
        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    void getPluginsIsUnmodifiable() {
        registry.register(makePlugin("locked"));
        List<DeploymentPlugin> plugins = registry.getPlugins();
        assertThrows(UnsupportedOperationException.class, () -> plugins.add(makePlugin("extra")));
    }
}
