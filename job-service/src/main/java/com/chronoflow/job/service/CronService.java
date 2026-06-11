package com.chronoflow.job.service;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
public class CronService {

    public Instant nextRun(String cronExpression, Instant after) {
        CronExpression expression = CronExpression.parse(cronExpression);
        return expression.next(after.atZone(ZoneOffset.UTC).toLocalDateTime())
                .atZone(ZoneOffset.UTC)
                .toInstant();
    }
}
