package com.assessment.booking.dto.response;

import java.time.ZonedDateTime;

public record SessionResponse(
        Long id,
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        String timezone
) {
}

