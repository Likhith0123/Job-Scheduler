package com.chronoflow.job.controller;

import com.chronoflow.common.dto.CreateJobRequest;
import com.chronoflow.common.dto.JobResponse;
import com.chronoflow.common.dto.JobRunResponse;
import com.chronoflow.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public JobResponse createJob(@RequestHeader("X-Tenant-Id") UUID tenantId,
                                   @Valid @RequestBody CreateJobRequest request) {
        return jobService.createJob(tenantId, request);
    }

    @GetMapping
    public List<JobResponse> listJobs(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return jobService.listJobs(tenantId);
    }

    @GetMapping("/{jobId}")
    public JobResponse getJob(@RequestHeader("X-Tenant-Id") UUID tenantId,
                              @PathVariable UUID jobId) {
        return jobService.getJob(tenantId, jobId);
    }

    @PostMapping("/{jobId}/pause")
    public JobResponse pauseJob(@RequestHeader("X-Tenant-Id") UUID tenantId,
                                @PathVariable UUID jobId) {
        return jobService.pauseJob(tenantId, jobId);
    }

    @PostMapping("/{jobId}/resume")
    public JobResponse resumeJob(@RequestHeader("X-Tenant-Id") UUID tenantId,
                                 @PathVariable UUID jobId) {
        return jobService.resumeJob(tenantId, jobId);
    }

    @DeleteMapping("/{jobId}")
    public void deleteJob(@RequestHeader("X-Tenant-Id") UUID tenantId,
                          @PathVariable UUID jobId) {
        jobService.deleteJob(tenantId, jobId);
    }

    @GetMapping("/{jobId}/runs")
    public List<JobRunResponse> listRuns(@RequestHeader("X-Tenant-Id") UUID tenantId,
                                         @PathVariable UUID jobId) {
        return jobService.listRuns(tenantId, jobId);
    }
}
