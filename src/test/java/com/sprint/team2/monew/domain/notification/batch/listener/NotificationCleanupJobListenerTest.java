package com.sprint.team2.monew.domain.notification.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotificationCleanupJobListenerTest {

    private MeterRegistry registry;
    private NotificationCleanupJobListener listener;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        listener = new NotificationCleanupJobListener(registry);
    }

    @Test
    @DisplayName("beforeJob - 알림 배치 실행 상태 메트릭 설정 테스트")
    void beforeJob() {
        JobExecution execution = new JobExecution(1L);
        listener.beforeJob(execution);

        assertThat(registry.get("batch.notification_cleanup.running").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("afterJob - 성공 시 알림 삭제 메트릭 반영 테스트")
    void afterJob() {
        JobExecution execution = new JobExecution(1L);
        execution.getExecutionContext().putLong("deletedCount", 5);

        listener.beforeJob(execution);
        execution.setStatus(BatchStatus.COMPLETED);
        listener.afterJob(execution);

        assertAll(
                () -> assertEquals(0.0, registry.get("batch.notification_cleanup.running").gauge().value()),
                () -> assertEquals(1.0, registry.get("batch.notification_cleanup.total_success").counter().count()),
                () -> assertEquals(0.0, registry.get("batch.notification_cleanup.total_failure").counter().count()),
                () -> assertEquals(5.0, registry.get("batch.notification_cleanup.total_deleted_count").counter().count()),
                () -> assertEquals(1.0, registry.get("batch.notification_cleanup.current_success").gauge().value()),
                () -> assertEquals(5.0, registry.get("batch.notification_cleanup.current_deleted_count").gauge().value())
        );
    }

    @Test
    @DisplayName("afterJob - 실패 시 메트릭 반영 테스트")
    void afterJobFailure() {
        JobExecution execution = new JobExecution(1L);

        listener.beforeJob(execution);
        execution.setStatus(BatchStatus.FAILED);
        listener.afterJob(execution);

        //실패 카운트만 올라가고 나머지는 올라가지 않는지 검증
        assertAll(
                () -> assertEquals(0.0, registry.get("batch.notification_cleanup.running").gauge().value()),
                () -> assertEquals(0.0, registry.get("batch.notification_cleanup.total_success").counter().count()),
                () -> assertEquals(1.0, registry.get("batch.notification_cleanup.total_failure").counter().count()),
                () -> assertEquals(0.0, registry.get("batch.notification_cleanup.total_deleted_count").counter().count()),
                () -> assertEquals(0.0, registry.get("batch.notification_cleanup.current_success").gauge().value()),
                () -> assertEquals(0.0, registry.get("batch.notification_cleanup.current_deleted_count").gauge().value())
        );
    }
}
