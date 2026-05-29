package com.assessment.booking.service;

import com.assessment.booking.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class TimeZoneService {

    public ZoneId toZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            throw new BadRequestException("Invalid timezone: " + timezone);
        }
    }

    public OffsetDateTime toUtc(LocalDateTime localDateTime, ZoneId sourceZone) {
        return localDateTime
                .atZone(sourceZone)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime();
    }

    public ZonedDateTime toZone(OffsetDateTime utcDateTime, ZoneId targetZone) {
        return utcDateTime.withOffsetSameInstant(ZoneOffset.UTC).atZoneSameInstant(targetZone);
    }
}

