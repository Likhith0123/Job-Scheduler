package com.chronoflow.common.dto;

import java.time.Instant;
import java.util.UUID;

public record JobRunResponse(
        UUID id,
        UUID jobId,
        UUID tenantId,
        String status,
        String result,
        Instant startedAt,
        Instant completedAt
) {
}
