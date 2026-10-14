package com.iotroom.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.dto.request.ActionHistorySearchCriteria;
import com.iotroom.entity.ActionHistory;
import com.iotroom.exception.AppException;
import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.util.DateTimeSearchUtils;
import com.iotroom.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActionHistoryService {
    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "id");

    private final ActionHistoryRepository actionHistoryRepository;

    @Transactional(readOnly = true)
    public Page<ActionHistory> search(ActionHistorySearchCriteria criteria) {
        Long deviceId = criteria.deviceId();
        String rawKeyword = criteria.keyword();
        LocalDateTime from = criteria.from();
        LocalDateTime to = criteria.to();

        if (from != null && to != null && from.isAfter(to)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "From time must be before to time");
        }

        ActionHistory.Action action = parseAction(criteria.action());
        ActionHistory.Status status = parseStatus(criteria.status());
        Pageable pageable = PageableUtils.build(
                criteria.page(), criteria.size(), criteria.sort(), "createdAt", SORT_FIELDS
        );

        Specification<ActionHistory> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (deviceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("device").get("id"), deviceId));
            }
            if (rawKeyword != null && !rawKeyword.isBlank()) {
                String keyword = rawKeyword.trim().toLowerCase(Locale.ROOT);
                List<Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("device").get("name")),
                        "%" + keyword + "%"
                ));
                keywordPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("user").get("username")),
                        "%" + keyword + "%"
                ));
                keywordPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("user").get("fullName")),
                        "%" + keyword + "%"
                ));
                DateTimeSearchUtils.parse(rawKeyword).ifPresent(parts -> keywordPredicates.add(
                        DateTimeSearchUtils.toPredicate(criteriaBuilder, root.get("createdAt"), parts)
                ));
                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
            }
            if (action != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return actionHistoryRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public ActionHistory getById(Long id) {
        return actionHistoryRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Action history not found"));
    }

    private ActionHistory.Action parseAction(String rawAction) {
        if (rawAction == null || rawAction.isBlank()) {
            return null;
        }
        try {
            return ActionHistory.Action.valueOf(rawAction.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Action must be ON or OFF");
        }
    }

    private ActionHistory.Status parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        try {
            return ActionHistory.Status.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Action status is invalid");
        }
    }
}
