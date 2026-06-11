package com.chronoflow.job.controller;

import com.chronoflow.common.dto.JobResponse;
import com.chronoflow.common.dto.JobRunResponse;
import com.chronoflow.job.entity.RunStatus;
import com.chronoflow.job.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/jobs")
public class InternalJobController {

    private final JobService jobService;

    public InternalJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/due")
    public List<JobResponse> dueJobs() {
        return jobService.findDueJobs();
    }

    @PostMapping("/{jobId}/scheduled")
    public JobResponse markScheduled(@PathVariable UUID jobId) {
        return jobService.markScheduled(jobId);
    }

    @PostMapping("/{jobId}/runs")
    public JobRunResponse startRun(@PathVariable UUID jobId, @RequestBody StartRunRequest request) {
        return jobService.startRun(jobId, request.tenantId());
    }

    @PostMapping("/runs/{runId}/complete")
    public JobRunResponse completeRun(@PathVariable UUID runId, @RequestBody CompleteRunRequest request) {
        return jobService.completeRun(runId, request.tenantId(), RunStatus.valueOf(request.status()), request.result());
    }

    public record StartRunRequest(UUID tenantId) {
    }

    public record CompleteRunRequest(UUID tenantId, String status, String result) {
    }
}
