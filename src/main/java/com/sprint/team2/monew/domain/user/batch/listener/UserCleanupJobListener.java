package com.sprint.team2.monew.domain.user.batch.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UserCleanupJobListener implements JobExecutionListener {

    private final MeterRegistry registry;
    private Timer.Sample sample;
    private final AtomicInteger running = new AtomicInteger(0);
    private final AtomicInteger currentSuccess = new AtomicInteger(0);
    private final AtomicLong currentDeleteCount = new AtomicLong(0L);

    public UserCleanupJobListener(MeterRegistry registry) {
        this.registry = registry;

        Gauge.builder("batch.user_cleanup.running", running, AtomicInteger::get)
                .description("현재 사용자 삭제 배치 처리 작업 진행 여부 (0: 종료, 1: 실행중)")
                .register(registry);

        Gauge.builder("batch.user_cleanup.current_success", currentSuccess, AtomicInteger::get)
                .description("현재 사용자 삭제 배치 처리 작업 성공 여부 (0: 실패, 1: 성공)")
                .register(registry);

        Gauge.builder("batch.user_cleanup.current_deleted_count", currentDeleteCount, AtomicLong::get)
                .description("현재 사용자 삭제 배치 처리 작업에서 삭제된 사용자의 수")
                .register(registry);

        Counter.builder("batch.user_cleanup.total_deleted_count")
                .description("사용자 삭제 배치 처리 작업에서 삭제된 사용자의 총합")
                .register(registry);

        Counter.builder("batch.user_cleanup.total_success")
                .description("사용자 삭제 배치 처리 작업의 총 성공 횟수")
                .register(registry);

        Counter.builder("batch.user_cleanup.total_failure")
                .description("사용자 삭제 배치 처리 작업의 총 실패 횟수")
                .register(registry);
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

        Counter totalDeletedCount = registry.get("batch.user_cleanup.total_deleted_count").counter();
        Counter totalSuccess = registry.get("batch.user_cleanup.total_success").counter();
        Counter totalFailure = registry.get("batch.user_cleanup.total_failure").counter();

        if (jobExecution.getStatus().isUnsuccessful()) {
            totalFailure.increment();
            currentDeleteCount.set(0);
            currentSuccess.set(0);
        } else {
            long deletedCount = jobExecution.getExecutionContext().getLong("deletedCount", 0L);
            totalDeletedCount.increment(deletedCount);
            totalSuccess.increment();
            currentDeleteCount.set(deletedCount);
            currentSuccess.set(1);
        }
    }
}
