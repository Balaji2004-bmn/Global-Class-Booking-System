package com.assessment.booking.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateSessionRequest(
        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime
) {

    @AssertTrue(message = "endTime must be after startTime")
    public boolean isEndTimeAfterStartTime() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}

