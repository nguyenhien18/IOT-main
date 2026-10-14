package com.iotroom.util;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtils {
    private PageableUtils() {
    }

    public static Pageable build(
            Integer page,
            Integer size,
            String sort,
            String defaultSortBy,
            Set<String> allowedSortFields
    ) {
        int resolvedPage = page == null || page < 0 ? 0 : page;
        int resolvedSize = size == null || size <= 0 ? 10 : Math.min(size, 100);

        String[] sortParts = sort == null ? new String[0] : sort.split(",", 2);
        String requestedSortBy = sortParts.length > 0 ? sortParts[0].trim() : "";
        String resolvedSortBy = allowedSortFields.contains(requestedSortBy) ? requestedSortBy : defaultSortBy;
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(direction, resolvedSortBy));
    }
}

