package com.assessment.booking.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long parentId,
        Long offeringId,
        String offeringTitle,
        OffsetDateTime bookedAt,
        List<SessionResponse> sessions
) {
}

