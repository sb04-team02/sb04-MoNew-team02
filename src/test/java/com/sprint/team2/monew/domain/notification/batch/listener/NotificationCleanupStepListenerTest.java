package com.sprint.team2.monew.domain.notification.batch.listener;

import com.sprint.team2.monew.domain.notification.exception.NotificationNotFoundException;
import com.sprint.team2.monew.domain.user.batch.listener.UserCleanupStepListener;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationCleanupStepListenerTest {

    private MeterRegistry registry;
    private NotificationCleanupStepListener listener;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 새로운 레지스트리 생성
        registry = new SimpleMeterRegistry();
        listener = new NotificationCleanupStepListener(registry);
    }

    @Test
    @DisplayName("Step 성공 시 실패 매트릭이 기록되지 않게 하는 테스트")
    void stepSuccessTest() {
        StepExecution execution = new StepExecution("notificationCleanupStep", new JobExecution(1L));
        execution.setExitStatus(ExitStatus.COMPLETED);

        listener.afterStep(execution);

        assertThat(getFailureGauge("NOTIFICATION_NOT_FOUND")).isZero();
        assertThat(getFailureGauge("DB_ERROR")).isZero();
        assertThat(getFailureGauge("UNKNOWN")).isZero();
    }

    @Test
    @DisplayName("알림 삭제 중 NotificationNotFound 예외 발생 시 메트릭 기록 테스트 ")
    void stepFailureNotificationNotFoundTest() {
        StepExecution execution = new StepExecution("notificationCleanupStep", new JobExecution(1L));
        execution.setExitStatus(ExitStatus.FAILED);
        execution.addFailureException(NotificationNotFoundException.withId(UUID.randomUUID()));

        listener.afterStep(execution);

        assertThat(getFailureGauge("NOTIFICATION_NOT_FOUND")).isEqualTo(1);
    }

    @Test
    @DisplayName("알림 삭제 중 DB 예외 발생 시 메트릭 기록 테스트")
    void stepFailureDBErrorTest() {
        StepExecution execution = new StepExecution("notificationCleanupStep", new JobExecution(1L));
        execution.setExitStatus(ExitStatus.FAILED);
        execution.addFailureException(new DataAccessResourceFailureException("DB 문제 발생"));

        listener.afterStep(execution);

        assertThat(getFailureGauge("DB_ERROR")).isEqualTo(1);
    }

    @Test
    @DisplayName("알 수 없는 예외 발생 시 메트릭 기록 테스트")
    void stepFailureUnknownTest() {
        StepExecution execution = new StepExecution("notificationCleanupStep", new JobExecution(1L));
        execution.setExitStatus(ExitStatus.FAILED);
        execution.addFailureException(new RuntimeException("예기치 못한 오류 발생"));

        listener.afterStep(execution);

        assertThat(getFailureGauge("UNKNOWN")).isEqualTo(1);
    }

    private  double getFailureGauge(String reason) {
        return registry.get("batch.notification_cleanup.failure")
                .tag("reason",reason)
                .gauge()
                .value();
    }
}
