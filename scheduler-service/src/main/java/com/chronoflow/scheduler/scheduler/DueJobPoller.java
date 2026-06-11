package com.chronoflow.scheduler.scheduler;

import com.chronoflow.scheduler.service.JobDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DueJobPoller {

    private static final Logger log = LoggerFactory.getLogger(DueJobPoller.class);

    private final JobDispatchService jobDispatchService;

    public DueJobPoller(JobDispatchService jobDispatchService) {
        this.jobDispatchService = jobDispatchService;
    }

    @Scheduled(fixedDelayString = "${chronoflow.scheduler.poll-interval-ms:10000}")
    public void pollDueJobs() {
        int dispatched = jobDispatchService.dispatchDueJobs();
        if (dispatched > 0) {
            log.info("Polling cycle dispatched {} job(s)", dispatched);
        }
    }
}
