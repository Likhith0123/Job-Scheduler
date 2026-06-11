package com.chronoflow.executor.client;

import com.chronoflow.common.dto.JobRunResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class JobServiceClient {

    private final RestClient restClient;

    public JobServiceClient(@Value("${chronoflow.job-service.url}") String jobServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(jobServiceUrl).build();
    }

    public JobRunResponse startRun(UUID jobId, UUID tenantId) {
        return restClient.post()
                .uri("/internal/jobs/{jobId}/runs", jobId)
                .body(new StartRunRequest(tenantId))
                .retrieve()
                .body(JobRunResponse.class);
    }

    public JobRunResponse completeRun(UUID runId, UUID tenantId, String status, String result) {
        return restClient.post()
                .uri("/internal/jobs/runs/{runId}/complete", runId)
                .body(new CompleteRunRequest(tenantId, status, result))
                .retrieve()
                .body(JobRunResponse.class);
    }

    private record StartRunRequest(UUID tenantId) {
    }

    private record CompleteRunRequest(UUID tenantId, String status, String result) {
    }
}
