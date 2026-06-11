package com.chronoflow.common.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateApiKeyRequest(
        @NotNull UUID tenantId,
        String label
) {
}
