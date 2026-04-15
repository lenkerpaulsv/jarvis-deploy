package com.jarvis.deploy.environment;

import com.jarvis.deploy.config.ConfigLoader;
import com.jarvis.deploy.config.EnvironmentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentManagerTest {

    private EnvironmentManager manager;
    private EnvironmentConfig devConfig;
    private EnvironmentConfig prodConfig;

    @BeforeEach
    void setUp() {
        ConfigLoader mockLoader = Mockito.mock(ConfigLoader.class);
        manager = new EnvironmentManager(mockLoader);
        devConfig = Mockito.mock(EnvironmentConfig.class);
        prodConfig = Mockito.mock(EnvironmentConfig.class);
    }

    @Test
    void constructorThrowsOnNullLoader() {
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentManager(null));
    }

    @Test
    void registerAndRetrieveEnvironment() {
        manager.registerEnvironment("dev", devConfig);
        Optional<EnvironmentConfig> result = manager.getEnvironment("dev");
        assertTrue(result.isPresent());
        assertSame(devConfig, result.get());
    }

    @Test
    void getEnvironmentCaseInsensitive() {
        manager.registerEnvironment("DEV", devConfig);
        assertTrue(manager.getEnvironment("dev").isPresent());
        assertTrue(manager.getEnvironment("Dev").isPresent());
    }

    @Test
    void registerEnvironmentThrowsOnBlankName() {
        assertThrows(IllegalArgumentException.class, () -> manager.registerEnvironment(" ", devConfig));
    }

    @Test
    void registerEnvironmentThrowsOnNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> manager.registerEnvironment("dev", null));
    }

    @Test
    void setActiveEnvironmentReturnsTrueWhenRegistered() {
        manager.registerEnvironment("prod", prodConfig);
        assertTrue(manager.setActiveEnvironment("prod"));
        assertEquals(Optional.of("prod"), manager.getActiveEnvironmentName());
    }

    @Test
    void setActiveEnvironmentReturnsFalseWhenNotRegistered() {
        assertFalse(manager.setActiveEnvironment("staging"));
        assertTrue(manager.getActiveEnvironmentName().isEmpty());
    }

    @Test
    void getActiveEnvironmentConfigReturnsCorrectConfig() {
        manager.registerEnvironment("prod", prodConfig);
        manager.setActiveEnvironment("prod");
        Optional<EnvironmentConfig> active = manager.getActiveEnvironmentConfig();
        assertTrue(active.isPresent());
        assertSame(prodConfig, active.get());
    }

    @Test
    void listEnvironmentsReturnsAllRegistered() {
        manager.registerEnvironment("dev", devConfig);
        manager.registerEnvironment("prod", prodConfig);
        Set<String> envs = manager.listEnvironments();
        assertTrue(envs.contains("dev"));
        assertTrue(envs.contains("prod"));
        assertEquals(2, envs.size());
    }

    @Test
    void removeEnvironmentClearsActiveIfMatch() {
        manager.registerEnvironment("dev", devConfig);
        manager.setActiveEnvironment("dev");
        manager.removeEnvironment("dev");
        assertFalse(manager.hasEnvironment("dev"));
        assertTrue(manager.getActiveEnvironmentName().isEmpty());
    }

    @Test
    void removeEnvironmentDoesNotClearActiveIfDifferent() {
        manager.registerEnvironment("dev", devConfig);
        manager.registerEnvironment("prod", prodConfig);
        manager.setActiveEnvironment("prod");
        manager.removeEnvironment("dev");
        assertEquals(Optional.of("prod"), manager.getActiveEnvironmentName());
    }
}
