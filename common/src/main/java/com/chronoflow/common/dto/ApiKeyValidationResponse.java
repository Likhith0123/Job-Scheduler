package com.chronoflow.common.dto;

import java.util.UUID;

public record ApiKeyValidationResponse(
        boolean valid,
        UUID tenantId,
        int rateLimitPerMinute
) {
    public static ApiKeyValidationResponse invalid() {
        return new ApiKeyValidationResponse(false, null, 0);
    }
}
