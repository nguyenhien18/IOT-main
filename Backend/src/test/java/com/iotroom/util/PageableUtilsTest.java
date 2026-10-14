package com.iotroom.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableUtilsTest {

    @Test
    void buildUsesRequestedSortAndLimitsPageSize() {
        Pageable pageable = PageableUtils.build(
                2,
                500,
                "value,asc",
                "createdAt",
                Set.of("createdAt", "value")
        );

        assertEquals(2, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("value").getDirection());
    }

    @Test
    void buildFallsBackToSafeDefaults() {
        Pageable pageable = PageableUtils.build(
                -1,
                0,
                "unknown,asc",
                "createdAt",
                Set.of("createdAt", "value")
        );

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("createdAt").getDirection());
    }
}

