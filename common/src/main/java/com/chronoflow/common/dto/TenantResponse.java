package com.chronoflow.common.dto;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        int rateLimitPerMinute,
        boolean active,
        Instant createdAt
) {
}
