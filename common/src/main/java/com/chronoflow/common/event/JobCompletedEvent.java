package com.chronoflow.common.event;

import java.time.Instant;
import java.util.UUID;

public record JobCompletedEvent(
        UUID jobRunId,
        UUID jobId,
        UUID tenantId,
        String status,
        String result,
        Instant completedAt
) {
}
