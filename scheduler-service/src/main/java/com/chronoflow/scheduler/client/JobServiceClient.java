package com.chronoflow.scheduler.client;

import com.chronoflow.common.dto.JobResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class JobServiceClient {

    private final RestClient restClient;

    public JobServiceClient(@Value("${chronoflow.job-service.url}") String jobServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(jobServiceUrl).build();
    }

    public List<JobResponse> fetchDueJobs() {
        return restClient.get()
                .uri("/internal/jobs/due")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public JobResponse markScheduled(UUID jobId) {
        return restClient.post()
                .uri("/internal/jobs/{jobId}/scheduled", jobId)
                .retrieve()
                .body(JobResponse.class);
    }
}
