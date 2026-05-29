package com.assessment.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingRequest(
        @NotNull
        @Positive
        Long parentId,

        @NotNull
        @Positive
        Long offeringId
) {
}

