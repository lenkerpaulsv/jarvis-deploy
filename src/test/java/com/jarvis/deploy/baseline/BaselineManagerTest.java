package com.jarvis.deploy.baseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BaselineManagerTest {

    private BaselineManager manager;
    private Map<String, String> sampleProps;

    @BeforeEach
    void setUp() {
        manager = new BaselineManager();
        sampleProps = new HashMap<>();
        sampleProps.put("db.host", "localhost");
        sampleProps.put("db.port", "5432");
        sampleProps.put("app.replicas", "2");
    }

    @Test
    void captureCreatesBaselineForEnvironment() {
        DeploymentBaseline baseline = manager.capture("staging", "1.0.0", sampleProps);
        assertNotNull(baseline);
        assertEquals("staging", baseline.getEnvironment());
        assertEquals("1.0.0", baseline.getVersion());
        assertFalse(baseline.isLocked());
    }

    @Test
    void getBaselineReturnsEmptyWhenNotCaptured() {
        Optional<DeploymentBaseline> result = manager.getBaseline("production");
        assertTrue(result.isEmpty());
    }

    @Test
    void getBaselineReturnsCapturedBaseline() {
        manager.capture("production", "2.0.0", sampleProps);
        Optional<DeploymentBaseline> result = manager.getBaseline("production");
        assertTrue(result.isPresent());
        assertEquals("2.0.0", result.get().getVersion());
    }

    @Test
    void lockBaselinePreventsMutation() {
        manager.capture("staging", "1.0.0", sampleProps);
        manager.lockBaseline("staging");
        assertTrue(manager.getBaseline("staging").get().isLocked());
    }

    @Test
    void lockBaselineThrowsWhenNoBaselineExists() {
        assertThrows(BaselineException.class, () -> manager.lockBaseline("unknown-env"));
    }

    @Test
    void checkDriftDetectsChangedProperty() {
        manager.capture("staging", "1.0.0", sampleProps);
        Map<String, String> modified = new HashMap<>(sampleProps);
        modified.put("db.host", "db.prod.internal");

        BaselineDriftReport report = manager.checkDrift("staging", "1.0.0", modified);
        assertTrue(report.isDriftDetected());
        assertTrue(report.getDriftedKeys().containsKey("db.host"));
        assertEquals("localhost", report.getDriftedKeys().get("db.host")[0]);
        assertEquals("db.prod.internal", report.getDriftedKeys().get("db.host")[1]);
    }

    @Test
    void checkDriftDetectsVersionChange() {
        manager.capture("staging", "1.0.0", sampleProps);
        BaselineDriftReport report = manager.checkDrift("staging", "1.1.0", sampleProps);
        assertTrue(report.isVersionChanged());
    }

    @Test
    void checkDriftNoDriftWhenIdentical() {
        manager.capture("staging", "1.0.0", sampleProps);
        BaselineDriftReport report = manager.checkDrift("staging", "1.0.0", new HashMap<>(sampleProps));
        assertFalse(report.isDriftDetected());
        assertTrue(report.getDriftedKeys().isEmpty());
    }

    @Test
    void checkDriftThrowsWhenNoBaselineExists() {
        assertThrows(BaselineException.class, () ->
                manager.checkDrift("production", "1.0.0", sampleProps));
    }

    @Test
    void removeBaselineDeletesEntry() {
        manager.capture("staging", "1.0.0", sampleProps);
        manager.removeBaseline("staging");
        assertFalse(manager.hasBaseline("staging"));
        assertEquals(0, manager.count());
    }

    @Test
    void countReflectsNumberOfBaselines() {
        manager.capture("staging", "1.0.0", sampleProps);
        manager.capture("production", "2.0.0", sampleProps);
        assertEquals(2, manager.count());
    }
}
