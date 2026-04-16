package com.jarvis.deploy.dependency;

import java.util.Objects;

/**
 * Represents a single unmet deployment dependency.
 */
public class DependencyViolation {

    private final DeploymentDependency dependency;
    private final String reason;

    public DependencyViolation(DeploymentDependency dependency, String reason) {
        this.dependency = Objects.requireNonNull(dependency, "dependency required");
        this.reason = Objects.requireNonNull(reason, "reason required");
    }

    public DeploymentDependency getDependency() { return dependency; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return String.format("DependencyViolation{app='%s', reason='%s'}",
                dependency.getDependentApp(), reason);
    }
}
