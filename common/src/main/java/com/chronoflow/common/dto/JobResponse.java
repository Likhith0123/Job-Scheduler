package com.chronoflow.common.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record JobResponse(
        UUID id,
        UUID tenantId,
        String name,
        String cronExpression,
        Map<String, Object> payload,
        String status,
        Instant nextRunAt,
        Instant createdAt
) {
}
