package com.assessment.booking.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record OfferingResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long teacherId,
        String teacherName,
        String title,
        String timezone,
        OffsetDateTime createdAt,
        List<SessionResponse> sessions
) {
}

