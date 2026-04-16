package com.jarvis.deploy.dependency;

import java.util.*;

/**
 * Resolves and validates deployment dependencies for a given environment.
 */
public class DependencyResolver {

    private final Map<String, List<DeploymentDependency>> dependencyMap = new HashMap<>();
    // app -> deployed version
    private final Map<String, String> deployedVersions = new HashMap<>();

    public void registerDependency(DeploymentDependency dependency) {
        Objects.requireNonNull(dependency, "dependency must not be null");
        dependencyMap.computeIfAbsent(dependency.getDependentApp(), k -> new ArrayList<>()).add(dependency);
    }

    public void recordDeployedVersion(String app, String version) {
        if (app == null || app.isBlank()) throw new IllegalArgumentException("app required");
        if (version == null || version.isBlank()) throw new IllegalArgumentException("version required");
        deployedVersions.put(app, version);
    }

    public List<DependencyViolation> resolve(String app) {
        List<DeploymentDependency> deps = dependencyMap.getOrDefault(app, Collections.emptyList());
        List<DependencyViolation> violations = new ArrayList<>();
        for (DeploymentDependency dep : deps) {
            String deployed = deployedVersions.get(dep.getRequiredApp());
            if (deployed == null) {
                violations.add(new DependencyViolation(dep, "Required app '" + dep.getRequiredApp() + "' is not deployed"));
            } else if (!deployed.equals(dep.getRequiredVersion())) {
                violations.add(new DependencyViolation(dep,
                        String.format("Required '%s' version '%s' but found '%s'",
                                dep.getRequiredApp(), dep.getRequiredVersion(), deployed)));
            }
        }
        return violations;
    }

    public boolean isSatisfied(String app) {
        return resolve(app).stream().noneMatch(v -> v.getDependency().isStrict());
    }

    public Map<String, String> getDeployedVersions() {
        return Collections.unmodifiableMap(deployedVersions);
    }

    public List<DeploymentDependency> getDependenciesFor(String app) {
        return Collections.unmodifiableList(dependencyMap.getOrDefault(app, Collections.emptyList()));
    }
}
