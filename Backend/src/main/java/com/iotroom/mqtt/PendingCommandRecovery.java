package com.iotroom.mqtt;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.entity.ActionHistory;
import com.iotroom.repository.ActionHistoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PendingCommandRecovery {
    private final ActionHistoryRepository actionHistoryRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void failCommandsInterruptedByRestart() {
        for (ActionHistory history : actionHistoryRepository.findAllByStatus(ActionHistory.Status.PROCESSING)) {
            history.setStatus(ActionHistory.Status.FAILED);
        }
    }
}
