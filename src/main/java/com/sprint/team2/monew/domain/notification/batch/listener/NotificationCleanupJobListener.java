package com.sprint.team2.monew.domain.notification.batch.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class NotificationCleanupJobListener implements JobExecutionListener {

    private final MeterRegistry registry;
    private Timer.Sample timer;
    private final AtomicInteger running = new AtomicInteger(0);
    private final AtomicInteger currentSuccess = new AtomicInteger(0);
    private final AtomicLong currentDeleteCount = new AtomicLong(0);

    public NotificationCleanupJobListener(MeterRegistry registry) {
        this.registry = registry;

        Gauge.builder("batch.notification_cleanup.running", running, AtomicInteger::get)
                .description("현재 알림 삭제 배치 작업 진행 여부 (0: 종료, 1: 실행 중)")
                .register(registry);

        Gauge.builder("batch.notification_cleanup.current_success", currentSuccess, AtomicInteger::get)
                .description("현재 알림 삭제 배치 작업 성공 여부 (0: 실패, 1: 성공)")
                .register(registry);

        Gauge.builder("batch.notification_cleanup.current_deleted_count", currentDeleteCount, AtomicLong::get)
                .description("현재 알림 삭제 배치 작업에서 삭제된 알림 수")
                .register(registry);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[알림] 일림 삭제 배치 작업 시작");
        timer = Timer.start(registry);
        running.set(1);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        timer.stop(registry.timer("batch.notification_cleanup.duration"));
        running.set(0);

        Counter totalDeletedCount = Counter.builder("batch.notification_cleanup.total_deleted_count")
                .description("알림 삭제 배치 작업 중 삭제된 알림의 총합")
                .register(registry);

        Counter totalSuccess = Counter.builder("batch.notification_cleanup.total_success")
                .description("알림 삭제 배치 작업 중 총 성공 횟수")
                .register(registry);

        Counter totalFailure = Counter.builder("batch.notification_cleanup.total_failure")
                .description("알림 삭제 배치 작업의 총 실패 횟수")
                .register(registry);

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.warn("[알림] 알림 삭제 배치 실패");
            totalFailure.increment();
            currentDeleteCount.set(0);
            currentSuccess.set(0);
        } else {
            long deletedCount = jobExecution.getExecutionContext().getLong("deletedCount", 0L);
            log.info("[알림] 배치 작업 완료 - 삭제된 알림 수: {}", deletedCount);
            totalDeletedCount.increment(deletedCount);
            totalSuccess.increment();
            currentDeleteCount.set(deletedCount);
            currentSuccess.set(1);
        }
    }
}
