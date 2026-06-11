package com.chronoflow.common.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record JobDueEvent(
        UUID jobId,
        UUID tenantId,
        String jobName,
        Map<String, Object> payload,
        Instant scheduledAt
) {
}
