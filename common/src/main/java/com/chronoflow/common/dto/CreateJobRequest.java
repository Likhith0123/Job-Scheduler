package com.chronoflow.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateJobRequest(
        @NotBlank String name,
        @NotBlank String cronExpression,
        @NotNull Map<String, Object> payload
) {
}
