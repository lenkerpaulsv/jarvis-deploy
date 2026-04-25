package com.jarvis.deploy.dependency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyResolverTest {

    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DependencyResolver();
    }

    @Test
    void resolveReturnNoViolationsWhenDependencyMet() {
        resolver.registerDependency(new DeploymentDependency("app-a", "app-b", "1.2.0", true));
        resolver.recordDeployedVersion("app-b", "1.2.0");
        List<DependencyViolation> violations = resolver.resolve("app-a");
        assertTrue(violations.isEmpty());
    }

    @Test
    void resolveDetectsVersionMismatch() {
        resolver.registerDependency(new DeploymentDependency("app-a", "app-b", "1.2.0", true));
        resolver.recordDeployedVersion("app-b", "1.1.0");
        List<DependencyViolation> violations = resolver.resolve("app-a");
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getReason().contains("1.1.0"));
    }

    @Test
    void resolveDetectsMissingDependency() {
        resolver.registerDependency(new DeploymentDependency("app-a", "app-b", "1.0.0", true));
        List<DependencyViolation> violations = resolver.resolve("app-a");
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).getReason().contains("not deployed"));
    }

    @Test
    void resolveReturnsViolationWithCorrectDependentAndDependency() {
        resolver.registerDependency(new DeploymentDependency("app-a", "app-b", "1.0.0", true));
        List<DependencyViolation> violations = resolver.resolve("app-a");
        assertEquals(1, violations.size());
        DependencyViolation violation = violations.get(0);
        assertEquals("app-a", violation.getDependent());
        assertEquals("app-b", violation.getDependency());
    }

    @Test
    void isSatisfiedReturnsTrueWhenNoStrictViolations() {
        resolver.registerDependency(new DeploymentDependency("app-a", "app-b", "2.0.0", false));
        // app-b not deployed but dependency is non-strict
        assertTrue(resolver.isSatisfied("app-a"));
    }

    @Test
    void isSatisfiedReturnsFalseForStrictViolation() {
        resolver.registerDependency(new DeploymentDependency("app-a", "app-b", "2.0.0", true));
        assertFalse(resolver.isSatisfied("app-a"));
    }

    @Test
    void noDependenciesAlwaysSatisfied() {
        assertTrue(resolver.isSatisfied("unknown-app"));
        assertTrue(resolver.resolve("unknown-app").isEmpty());
    }

    @Test
    void registerDependencyNullThrows() {
        assertThrows(NullPointerException.class, () -> resolver.registerDependency(null));
    }

    @Test
    void recordDeployedVersionBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> resolver.recordDeployedVersion("", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> resolver.recordDeployedVersion("app", ""));
    }

    @Test
    void getDeployedVersionsIsUnmodifiable() {
        resolver.recordDeployedVersion("app-x", "3.0.0");
        assertThrows(UnsupportedOperationException.class,
                () -> resolver.getDeployedVersions().put("app-y", "1.0"));
    }
}
