package com.jarvis.deploy.promote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PromotionManager {

    private static final List<String> PROMOTION_ORDER = List.of("dev", "staging", "production");
    private final Map<String, PromotionRequest> activePromotions = new ConcurrentHashMap<>();

    public PromotionRequest createRequest(String deploymentId, String source,
                                          String target, String requestedBy) {
        validateEnvironmentOrder(source, target);
        String key = deploymentId + ":" + target;
        if (activePromotions.containsKey(key)) {
            throw new PromotionException("A promotion to '" + target + "' for deployment '" + deploymentId + "' is already active.");
        }
        PromotionRequest request = new PromotionRequest(deploymentId, source, target, requestedBy);
        activePromotions.put(key, request);
        return request;
    }

    public void approve(PromotionRequest request) {
        if (request.getStatus() != PromotionStatus.PENDING) {
            throw new PromotionException("Request is not in PENDING state: " + request.getStatus());
        }
        request.setStatus(PromotionStatus.IN_PROGRESS);
    }

    public void complete(PromotionRequest request, boolean success) {
        request.setStatus(success ? PromotionStatus.SUCCEEDED : PromotionStatus.FAILED);
        String key = request.getDeploymentId() + ":" + request.getTargetEnvironment();
        activePromotions.remove(key);
    }

    public void cancel(PromotionRequest request) {
        if (request.getStatus() == PromotionStatus.IN_PROGRESS) {
            throw new PromotionException("Cannot cancel a promotion that is already in progress.");
        }
        request.setStatus(PromotionStatus.CANCELLED);
        String key = request.getDeploymentId() + ":" + request.getTargetEnvironment();
        activePromotions.remove(key);
    }

    public List<PromotionRequest> getActivePromotions() {
        return Collections.unmodifiableList(new ArrayList<>(activePromotions.values()));
    }

    private void validateEnvironmentOrder(String source, String target) {
        int srcIdx = PROMOTION_ORDER.indexOf(source);
        int tgtIdx = PROMOTION_ORDER.indexOf(target);
        if (srcIdx < 0) throw new PromotionException("Unknown source environment: " + source);
        if (tgtIdx < 0) throw new PromotionException("Unknown target environment: " + target);
        if (tgtIdx <= srcIdx) {
            throw new PromotionException(
                "Cannot promote from '" + source + "' to '" + target + "': target must be higher in the pipeline.");
        }
    }
}
