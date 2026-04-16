package com.jarvis.deploy.filter;

import com.jarvis.deploy.deployment.DeploymentRecord;

import java.util.List;
import java.util.stream.Collectors;

public class DeploymentFilterService {

    public List<DeploymentRecord> apply(List<DeploymentRecord> records, DeploymentFilter filter) {
        if (records == null || records.isEmpty()) return List.of();
        if (filter == null) return records;

        return records.stream()
                .filter(r -> filter.matches(
                        r.getEnvironment(),
                        r.getArtifactId(),
                        r.getStatus(),
                        r.getTimestamp(),
                        r.getTags()
                ))
                .collect(Collectors.toList());
    }

    public long count(List<DeploymentRecord> records, DeploymentFilter filter) {
        return apply(records, filter).size();
    }

    public boolean anyMatch(List<DeploymentRecord> records, DeploymentFilter filter) {
        return !apply(records, filter).isEmpty();
    }
}
