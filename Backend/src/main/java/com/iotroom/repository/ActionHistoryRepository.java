package com.iotroom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.iotroom.entity.ActionHistory;

@Repository
public interface ActionHistoryRepository extends JpaRepository<ActionHistory, Long>, JpaSpecificationExecutor<ActionHistory> {
    boolean existsByDevice_IdAndStatus(Long deviceId, ActionHistory.Status status);
    List<ActionHistory> findAllByStatus(ActionHistory.Status status);
}
