package com.chronoflow.common.dto;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        UUID tenantId,
        String label,
        String apiKey,
        boolean active,
        Instant createdAt
) {
}
