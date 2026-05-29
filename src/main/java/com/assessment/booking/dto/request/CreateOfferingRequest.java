package com.assessment.booking.dto.request;

import com.assessment.booking.config.validation.ValidTimezone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOfferingRequest(
        @NotNull
        @Positive
        Long courseId,

        @NotNull
        @Positive
        Long teacherId,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 64)
        @ValidTimezone
        String timezone
) {
}

