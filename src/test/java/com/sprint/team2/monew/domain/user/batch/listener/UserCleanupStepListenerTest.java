package com.sprint.team2.monew.domain.user.batch.listener;

import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCleanupStepListenerTest {

    private MeterRegistry registry;

    private UserCleanupStepListener listener;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 새로운 레지스트리 생성
        registry = new SimpleMeterRegistry();
        listener = new UserCleanupStepListener(registry);
    }

    @Test
    @DisplayName("Step 성공 시 메트릭 상태 테스트")
    void stepSuccessTest() {
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.COMPLETED);

        listener.afterStep(stepExecution);

        // Step이 성공 시 failure는 0
        assertThat(getFailureGauge("USER_NOT_FOUND")).isZero();
        assertThat(getFailureGauge("DB_ERROR")).isZero();
        assertThat(getFailureGauge("UNKNOWN")).isZero();
    }

    @Test
    @DisplayName("UserNotFound 예외 발생 시 메트릭 상태 테스트")
    void stepFailureUserNotFound() {
        UUID userId = UUID.randomUUID();
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.addFailureException(UserNotFoundException.withId(userId));

        listener.afterStep(stepExecution);

        // UserNotFound 예외 발생 시 USER_NOT_FOUND 태그의 값은 1
        assertThat(getFailureGauge("USER_NOT_FOUND")).isEqualTo(1);
    }

    @Test
    @DisplayName("데이터베이스 예외 발생 시 메트릭 상태 테스트")
    void stepFailureDbError() {
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.addFailureException(new DataAccessResourceFailureException("db down"));

        listener.afterStep(stepExecution);

        // DataAccessException 발생 시 DB_ERROR 태그의 값은 1
        assertThat(getFailureGauge("DB_ERROR")).isEqualTo(1);
    }

    @Test
    @DisplayName("알 수 없는 예외 발생 시 메트릭 상태 테스트")
    void stepFailureUnknown() {
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.addFailureException(new RuntimeException("??"));

        listener.afterStep(stepExecution);

        // 그 밖의 예외 발생 시 UNKNOWN 태그의 값은 1
        assertThat(getFailureGauge("UNKNOWN")).isEqualTo(1);
    }

    private double getFailureGauge(String reason) {
        return registry.get("batch.user_cleanup.failure")
                .tag("reason", reason)
                .gauge()
                .value();
    }
}