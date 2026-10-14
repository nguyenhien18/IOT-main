package com.iotroom.mqtt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class MqttCommandTracker {
    private final ConcurrentMap<Long, CompletableFuture<Boolean>> pendingCommands = new ConcurrentHashMap<>();

    public CompletableFuture<Boolean> register(Long actionId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture<Boolean> existing = pendingCommands.putIfAbsent(actionId, future);
        if (existing != null) {
            throw new IllegalStateException("Action is already being tracked: " + actionId);
        }
        return future;
    }

    public void complete(Long actionId, boolean success) {
        CompletableFuture<Boolean> future = pendingCommands.remove(actionId);
        if (future != null) {
            future.complete(success);
        }
    }

    public void discard(Long actionId) {
        pendingCommands.remove(actionId);
    }
}
