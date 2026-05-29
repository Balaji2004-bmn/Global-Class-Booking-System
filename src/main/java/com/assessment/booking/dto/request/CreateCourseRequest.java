package com.assessment.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        String description
) {
}

