package com.jarvis.deploy.broadcast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentBroadcasterTest {

    private DeploymentBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new DeploymentBroadcaster(10);
    }

    @Test
    void constructorRejectsNonPositiveHistorySize() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentBroadcaster(0));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentBroadcaster(-5));
    }

    @Test
    void registerChannelAndBroadcastDeliversMessage() {
        List<BroadcastMessage> received = new ArrayList<>();
        BroadcastChannel channel = received::add;

        broadcaster.registerChannel("prod", channel);
        broadcaster.broadcast("prod", "deploy-001", "Deployment started");

        assertEquals(1, received.size());
        assertEquals("prod", received.get(0).getEnvironment());
        assertEquals("deploy-001", received.get(0).getDeploymentId());
        assertEquals("Deployment started", received.get(0).getMessage());
        assertNotNull(received.get(0).getTimestamp());
    }

    @Test
    void broadcastToUnknownEnvironmentDoesNotThrow() {
        assertDoesNotThrow(() -> broadcaster.broadcast("staging", "deploy-002", "No channels here"));
    }

    @Test
    void broadcastRecordsHistory() {
        broadcaster.broadcast("dev", "deploy-003", "msg1");
        broadcaster.broadcast("dev", "deploy-004", "msg2");

        List<BroadcastMessage> history = broadcaster.getHistory();
        assertEquals(2, history.size());
    }

    @Test
    void historyCapIsRespected() {
        DeploymentBroadcaster smallBroadcaster = new DeploymentBroadcaster(3);
        for (int i = 0; i < 5; i++) {
            smallBroadcaster.broadcast("prod", "deploy-" + i, "msg " + i);
        }
        List<BroadcastMessage> history = smallBroadcaster.getHistory();
        assertEquals(3, history.size());
        assertEquals("msg 2", history.get(0).getMessage());
    }

    @Test
    void getHistoryForEnvironmentFiltersCorrectly() {
        broadcaster.broadcast("prod", "d1", "prod msg");
        broadcaster.broadcast("staging", "d2", "staging msg");
        broadcaster.broadcast("prod", "d3", "prod msg 2");

        List<BroadcastMessage> prodHistory = broadcaster.getHistoryForEnvironment("prod");
        assertEquals(2, prodHistory.size());
        assertTrue(prodHistory.stream().allMatch(m -> m.getEnvironment().equals("prod")));
    }

    @Test
    void unregisterChannelStopsDelivery() {
        List<BroadcastMessage> received = new ArrayList<>();
        BroadcastChannel channel = received::add;

        broadcaster.registerChannel("prod", channel);
        broadcaster.broadcast("prod", "d1", "before unregister");
        broadcaster.unregisterChannel("prod", channel);
        broadcaster.broadcast("prod", "d2", "after unregister");

        assertEquals(1, received.size());
    }

    @Test
    void failingChannelDoesNotPreventOtherChannels() {
        List<BroadcastMessage> received = new ArrayList<>();
        BroadcastChannel faultyChannel = msg -> { throw new RuntimeException("channel error"); };
        BroadcastChannel goodChannel = received::add;

        broadcaster.registerChannel("prod", faultyChannel);
        broadcaster.registerChannel("prod", goodChannel);

        assertDoesNotThrow(() -> broadcaster.broadcast("prod", "d1", "test"));
        assertEquals(1, received.size());
    }

    @Test
    void getChannelCountReturnsCorrectValue() {
        assertEquals(0, broadcaster.getChannelCount("prod"));
        broadcaster.registerChannel("prod", msg -> {});
        broadcaster.registerChannel("prod", msg -> {});
        assertEquals(2, broadcaster.getChannelCount("prod"));
    }

    @Test
    void broadcastRejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> broadcaster.broadcast(null, "d1", "msg"));
        assertThrows(IllegalArgumentException.class, () -> broadcaster.broadcast("prod", null, "msg"));
        assertThrows(IllegalArgumentException.class, () -> broadcaster.broadcast("prod", "d1", null));
    }
}
