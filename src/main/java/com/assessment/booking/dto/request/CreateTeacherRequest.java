package com.assessment.booking.dto.request;

import com.assessment.booking.config.validation.ValidTimezone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeacherRequest(
        @NotBlank
        @Size(max = 160)
        String name,

        @NotBlank
        @Size(max = 64)
        @ValidTimezone
        String timezone
) {
}

