package com.jarvis.deploy.broadcast;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Broadcasts deployment lifecycle messages to registered channels.
 * Supports per-environment channel targeting and message history.
 */
public class DeploymentBroadcaster {

    private final Map<String, List<BroadcastChannel>> channelsByEnvironment = new ConcurrentHashMap<>();
    private final List<BroadcastMessage> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private final int maxHistorySize;

    public DeploymentBroadcaster(int maxHistorySize) {
        if (maxHistorySize <= 0) {
            throw new IllegalArgumentException("maxHistorySize must be positive");
        }
        this.maxHistorySize = maxHistorySize;
    }

    public void registerChannel(String environment, BroadcastChannel channel) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Channel must not be null");
        }
        channelsByEnvironment
                .computeIfAbsent(environment, e -> Collections.synchronizedList(new ArrayList<>()))
                .add(channel);
    }

    public void unregisterChannel(String environment, BroadcastChannel channel) {
        List<BroadcastChannel> channels = channelsByEnvironment.get(environment);
        if (channels != null) {
            channels.remove(channel);
        }
    }

    public void broadcast(String environment, String deploymentId, String message) {
        if (environment == null || deploymentId == null || message == null) {
            throw new IllegalArgumentException("environment, deploymentId, and message must not be null");
        }

        BroadcastMessage bm = new BroadcastMessage(environment, deploymentId, message, Instant.now());
        recordHistory(bm);

        List<BroadcastChannel> channels = channelsByEnvironment.getOrDefault(environment, Collections.emptyList());
        for (BroadcastChannel channel : channels) {
            try {
                channel.send(bm);
            } catch (Exception e) {
                // Individual channel failures should not interrupt others
                System.err.println("[DeploymentBroadcaster] Channel send failed: " + e.getMessage());
            }
        }
    }

    public List<BroadcastMessage> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(messageHistory));
    }

    public List<BroadcastMessage> getHistoryForEnvironment(String environment) {
        List<BroadcastMessage> result = new ArrayList<>();
        for (BroadcastMessage msg : messageHistory) {
            if (msg.getEnvironment().equals(environment)) {
                result.add(msg);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public int getChannelCount(String environment) {
        List<BroadcastChannel> channels = channelsByEnvironment.get(environment);
        return channels == null ? 0 : channels.size();
    }

    private void recordHistory(BroadcastMessage message) {
        synchronized (messageHistory) {
            if (messageHistory.size() >= maxHistorySize) {
                messageHistory.remove(0);
            }
            messageHistory.add(message);
        }
    }
}
