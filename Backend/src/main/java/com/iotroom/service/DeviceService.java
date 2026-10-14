package com.iotroom.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.entity.ActionHistory;
import com.iotroom.entity.Device;
import com.iotroom.dto.response.BulkDeviceControlItemResponse;
import com.iotroom.dto.response.BulkDeviceControlResponse;
import com.iotroom.exception.AppException;
import com.iotroom.mqtt.MqttClientService;
import com.iotroom.mqtt.MqttCommandTracker;
import com.iotroom.mqtt.MqttProperties;
import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final ActionHistoryRepository actionHistoryRepository;
    private final ProfileService profileService;
    private final MqttClientService mqttClientService;
    private final MqttCommandTracker commandTracker;
    private final MqttProperties mqttProperties;

    @Transactional(readOnly = true)
    public List<Device> getAll() {
        return deviceRepository.findAll().stream()
                .sorted(Comparator.comparing(Device::getId))
                .toList();
    }

    public ActionHistory control(Long deviceId, String rawAction) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Device not found"));
        ActionHistory.Action action = parseAction(rawAction);

        if (actionHistoryRepository.existsByDevice_IdAndStatus(deviceId, ActionHistory.Status.PROCESSING)) {
            throw new AppException(HttpStatus.CONFLICT, "This device already has a command in progress");
        }

        ActionHistory history = new ActionHistory();
        history.setDevice(device);
        history.setUser(profileService.getCurrentProfile());
        history.setAction(action);
        history.setStatus(ActionHistory.Status.PROCESSING);
        history.setCreatedAt(LocalDateTime.now());
        history = actionHistoryRepository.saveAndFlush(history);

        CompletableFuture<Boolean> feedback = commandTracker.register(history.getId());
        try {
            mqttClientService.publishDeviceCommand(history.getId(), deviceId, action.name());
            boolean success = feedback.get(mqttProperties.getCommandTimeoutMs(), TimeUnit.MILLISECONDS);
            ActionHistory completed = actionHistoryRepository.findById(history.getId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Action history not found"));

            if (!success) {
                throw new AppException(HttpStatus.BAD_GATEWAY, "The ESP8266 reported that the command failed");
            }
            return completed;
        } catch (TimeoutException exception) {
            markFailed(history.getId());
            throw new AppException(HttpStatus.GATEWAY_TIMEOUT, "ESP8266 did not acknowledge the command in time");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            markFailed(history.getId());
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Device command was interrupted");
        } catch (ExecutionException exception) {
            markFailed(history.getId());
            throw new AppException(HttpStatus.BAD_GATEWAY, "Cannot process ESP8266 feedback");
        } catch (AppException exception) {
            markFailed(history.getId());
            throw exception;
        } finally {
            commandTracker.discard(history.getId());
        }
    }

    public BulkDeviceControlResponse controlAll(String rawAction) {
        ActionHistory.Action action = parseAction(rawAction);
        List<Device> devices = getAll();

        List<BulkDeviceControlItemResponse> results = devices.stream()
                .map(device -> controlForBulk(device, action))
                .toList();

        int successCount = (int) results.stream()
                .filter(result -> ActionHistory.Status.SUCCESS.name().equals(result.status()))
                .count();

        return new BulkDeviceControlResponse(
                action.name(),
                results.size(),
                successCount,
                results.size() - successCount,
                results
        );
    }

    private BulkDeviceControlItemResponse controlForBulk(Device device, ActionHistory.Action action) {
        try {
            ActionHistory history = control(device.getId(), action.name());
            Device updatedDevice = deviceRepository.findById(device.getId()).orElse(device);
            return new BulkDeviceControlItemResponse(
                    device.getId(),
                    device.getName(),
                    action.name(),
                    history.getStatus().name(),
                    updatedDevice.getStatus().name(),
                    "success"
            );
        } catch (AppException exception) {
            Device updatedDevice = deviceRepository.findById(device.getId()).orElse(device);
            return new BulkDeviceControlItemResponse(
                    device.getId(),
                    device.getName(),
                    action.name(),
                    ActionHistory.Status.FAILED.name(),
                    updatedDevice.getStatus().name(),
                    exception.getMessage()
            );
        }
    }

    private void markFailed(Long actionId) {
        actionHistoryRepository.findById(actionId).ifPresent(history -> {
            if (history.getStatus() == ActionHistory.Status.PROCESSING) {
                history.setStatus(ActionHistory.Status.FAILED);
                actionHistoryRepository.saveAndFlush(history);
            }
        });
    }

    private ActionHistory.Action parseAction(String rawAction) {
        if (rawAction == null || rawAction.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Action must be ON or OFF");
        }
        try {
            return ActionHistory.Action.valueOf(rawAction.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Action must be ON or OFF");
        }
    }
}
