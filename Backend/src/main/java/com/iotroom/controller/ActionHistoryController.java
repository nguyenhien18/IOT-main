package com.iotroom.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iotroom.dto.request.ActionHistorySearchCriteria;
import com.iotroom.dto.response.ActionHistoryResponse;
import com.iotroom.dto.response.ApiResponse;
import com.iotroom.dto.response.PageResponse;
import com.iotroom.mapper.ActionHistoryMapper;
import com.iotroom.service.ActionHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionHistoryController {
    private final ActionHistoryService actionHistoryService;
    private final ActionHistoryMapper actionHistoryMapper;

    @GetMapping("/{id}")
    public ApiResponse<ActionHistoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(
                actionHistoryMapper.toActionHistoryResponse(actionHistoryService.getById(id))
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<ActionHistoryResponse>> getAll(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        ActionHistorySearchCriteria criteria = new ActionHistorySearchCriteria(
                deviceId, keyword, action, status, from, to, sort, page, size
        );
        return ApiResponse.ok(PageResponse.from(
                actionHistoryService.search(criteria)
                        .map(actionHistoryMapper::toActionHistoryResponse)
        ));
    }
}
