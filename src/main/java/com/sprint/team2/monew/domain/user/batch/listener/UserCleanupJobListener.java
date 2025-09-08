package com.sprint.team2.monew.domain.user.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UserCleanupJobListener implements JobExecutionListener {

    private final MeterRegistry registry;
    private Timer.Sample sample;
    private final AtomicInteger running = new AtomicInteger(0); // Job 실행 중 상태 코드 - 0: 멈춤, 1: 실행중

    public UserCleanupJobListener(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("batch.user_cleanup.running", running);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        sample = Timer.start(registry);
        running.set(1);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        sample.stop(registry.timer("batch.user_cleanup.duration"));
        running.set(0);

        if (jobExecution.getStatus().isUnsuccessful()) {
            registry.counter("batch.user_cleanup.failure").increment();
        } else {
            long deletedCount = jobExecution.getExecutionContext().getLong("deletedCount", 0L);
            registry.counter("batch.user_cleanup.deleted_total").increment(deletedCount);
            registry.counter("batch.user_cleanup.success").increment();
        }
    }
}
