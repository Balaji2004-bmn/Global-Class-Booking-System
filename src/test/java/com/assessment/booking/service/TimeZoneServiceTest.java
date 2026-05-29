package com.assessment.booking.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TimeZoneServiceTest {

    private final TimeZoneService service = new TimeZoneService();

    @Test
    void convertsTeacherLocalTimeToUtc() {
        OffsetDateTime utc = service.toUtc(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                ZoneId.of("America/New_York")
        );

        assertThat(utc).isEqualTo(OffsetDateTime.of(2026, 6, 1, 14, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void convertsUtcToParentTimezone() {
        OffsetDateTime utc = OffsetDateTime.of(2026, 6, 1, 14, 0, 0, 0, ZoneOffset.UTC);

        assertThat(service.toZone(utc, ZoneId.of("Asia/Kolkata")).toString())
                .isEqualTo("2026-06-01T19:30+05:30[Asia/Kolkata]");
    }
}

