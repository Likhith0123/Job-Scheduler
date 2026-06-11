package com.chronoflow.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank String name,
        @Min(1) int rateLimitPerMinute
) {
}
