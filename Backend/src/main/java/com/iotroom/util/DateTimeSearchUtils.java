package com.iotroom.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public final class DateTimeSearchUtils {
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            formatter("dd/MM/uuuu H:mm:ss"),
            formatter("dd/MM/uuuu H:mm")
    );
    private static final DateTimeFormatter DATE_FORMAT = formatter("dd/MM/uuuu");
    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            formatter("H:mm:ss"),
            formatter("H:mm")
    );

    private DateTimeSearchUtils() {
    }

    public static Optional<DateTimeParts> parse(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        String value = rawValue.trim().replaceAll(",\\s*", " ").replaceAll("\\s+", " ");

        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                boolean includesSecond = value.substring(value.indexOf(' ') + 1).split(":").length == 3;
                return Optional.of(new DateTimeParts(
                        dateTime.getDayOfMonth(),
                        dateTime.getMonthValue(),
                        dateTime.getYear(),
                        dateTime.getHour(),
                        dateTime.getMinute(),
                        includesSecond ? dateTime.getSecond() : null
                ));
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }

        try {
            LocalDate date = LocalDate.parse(value, DATE_FORMAT);
            return Optional.of(new DateTimeParts(
                    date.getDayOfMonth(), date.getMonthValue(), date.getYear(), null, null, null
            ));
        } catch (DateTimeParseException ignored) {
            // Try a time-only value.
        }

        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                LocalTime time = LocalTime.parse(value, formatter);
                boolean includesSecond = value.split(":").length == 3;
                return Optional.of(new DateTimeParts(
                        null,
                        null,
                        null,
                        time.getHour(),
                        time.getMinute(),
                        includesSecond ? time.getSecond() : null
                ));
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }

        return Optional.empty();
    }

    public static Predicate toPredicate(
            CriteriaBuilder criteriaBuilder,
            Expression<LocalDateTime> expression,
            DateTimeParts parts
    ) {
        Predicate predicate = criteriaBuilder.conjunction();

        if (parts.year() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
                    criteriaBuilder.function("year", Integer.class, expression), parts.year()
            ));
        }
        if (parts.month() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
                    criteriaBuilder.function("month", Integer.class, expression), parts.month()
            ));
        }
        if (parts.day() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
                    criteriaBuilder.function("day", Integer.class, expression), parts.day()
            ));
        }
        if (parts.hour() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
                    criteriaBuilder.function("hour", Integer.class, expression), parts.hour()
            ));
        }
        if (parts.minute() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
                    criteriaBuilder.function("minute", Integer.class, expression), parts.minute()
            ));
        }
        if (parts.second() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
                    criteriaBuilder.function("second", Integer.class, expression), parts.second()
            ));
        }

        return predicate;
    }

    private static DateTimeFormatter formatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }

    public record DateTimeParts(
            Integer day,
            Integer month,
            Integer year,
            Integer hour,
            Integer minute,
            Integer second
    ) {
    }
}
