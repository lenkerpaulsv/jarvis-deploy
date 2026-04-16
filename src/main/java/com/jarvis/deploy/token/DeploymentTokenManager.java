package com.jarvis.deploy.token;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DeploymentTokenManager {

    private static final long DEFAULT_TTL_SECONDS = 3600;
    private final Map<String, DeploymentToken> tokens = new ConcurrentHashMap<>();

    public DeploymentToken issueToken(String environment, String issuedBy) {
        return issueToken(environment, issuedBy, DEFAULT_TTL_SECONDS);
    }

    public DeploymentToken issueToken(String environment, String issuedBy, long ttlSeconds) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (issuedBy == null || issuedBy.isBlank()) {
            throw new IllegalArgumentException("Issuer must not be blank");
        }
        DeploymentToken token = new DeploymentToken(environment, issuedBy, ttlSeconds);
        tokens.put(token.getTokenId(), token);
        return token;
    }

    public Optional<DeploymentToken> getToken(String tokenId) {
        return Optional.ofNullable(tokens.get(tokenId));
    }

    public boolean validateToken(String tokenId, String environment) {
        return getToken(tokenId)
                .filter(t -> t.getEnvironment().equals(environment))
                .filter(DeploymentToken::isValid)
                .isPresent();
    }

    public boolean revokeToken(String tokenId) {
        DeploymentToken token = tokens.get(tokenId);
        if (token == null) return false;
        token.revoke();
        return true;
    }

    public void purgeExpiredTokens() {
        tokens.entrySet().removeIf(e -> e.getValue().isExpired() || e.getValue().isRevoked());
    }

    public int activeTokenCount() {
        return (int) tokens.values().stream().filter(DeploymentToken::isValid).count();
    }
}
