package com.jarvis.deploy.dependency;

import java.util.Objects;

/**
 * Represents a dependency between two deployments.
 */
public class DeploymentDependency {

    private final String dependentApp;
    private final String requiredApp;
    private final String requiredVersion;
    private final boolean strict;

    public DeploymentDependency(String dependentApp, String requiredApp, String requiredVersion, boolean strict) {
        if (dependentApp == null || dependentApp.isBlank()) throw new IllegalArgumentException("dependentApp required");
        if (requiredApp == null || requiredApp.isBlank()) throw new IllegalArgumentException("requiredApp required");
        if (requiredVersion == null || requiredVersion.isBlank()) throw new IllegalArgumentException("requiredVersion required");
        this.dependentApp = dependentApp;
        this.requiredApp = requiredApp;
        this.requiredVersion = requiredVersion;
        this.strict = strict;
    }

    public String getDependentApp() { return dependentApp; }
    public String getRequiredApp() { return requiredApp; }
    public String getRequiredVersion() { return requiredVersion; }
    public boolean isStrict() { return strict; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentDependency)) return false;
        DeploymentDependency that = (DeploymentDependency) o;
        return strict == that.strict &&
               Objects.equals(dependentApp, that.dependentApp) &&
               Objects.equals(requiredApp, that.requiredApp) &&
               Objects.equals(requiredVersion, that.requiredVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependentApp, requiredApp, requiredVersion, strict);
    }

    @Override
    public String toString() {
        return String.format("DeploymentDependency{dependent='%s', requires='%s@%s', strict=%b}",
                dependentApp, requiredApp, requiredVersion, strict);
    }
}
