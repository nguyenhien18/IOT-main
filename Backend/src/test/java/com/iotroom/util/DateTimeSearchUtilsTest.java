package com.iotroom.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DateTimeSearchUtilsTest {

    @Test
    void parsesTimeToMinutePrecision() {
        DateTimeSearchUtils.DateTimeParts parts = DateTimeSearchUtils.parse("15:31").orElseThrow();

        assertEquals(15, parts.hour());
        assertEquals(31, parts.minute());
        assertEquals(null, parts.second());
        assertEquals(null, parts.day());
    }

    @Test
    void parsesVietnameseDateTimeWithSeconds() {
        DateTimeSearchUtils.DateTimeParts parts = DateTimeSearchUtils
                .parse("06/10/2026 15:31:59")
                .orElseThrow();

        assertEquals(6, parts.day());
        assertEquals(10, parts.month());
        assertEquals(2026, parts.year());
        assertEquals(15, parts.hour());
        assertEquals(31, parts.minute());
        assertEquals(59, parts.second());
    }

    @Test
    void rejectsUnsupportedTimeText() {
        assertTrue(DateTimeSearchUtils.parse("khong-phai-thoi-gian").isEmpty());
    }
}
