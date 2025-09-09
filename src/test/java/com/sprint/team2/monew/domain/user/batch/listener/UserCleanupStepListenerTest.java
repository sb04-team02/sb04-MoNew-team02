package com.sprint.team2.monew.domain.user.batch.listener;

import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCleanupStepListenerTest {

    @Autowired
    private MeterRegistry registry;

    private UserCleanupStepListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserCleanupStepListener(registry);
    }

    @Test
    void 스텝성공시_모든Gauge가0이다() {
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.COMPLETED);

        listener.afterStep(stepExecution);

        assertThat(getFailureGauge("USER_NOT_FOUND")).isZero();
        assertThat(getFailureGauge("DB_ERROR")).isZero();
        assertThat(getFailureGauge("UNKNOWN")).isZero();
    }

    @Test
    void UserNotFoundException발생시_USER_NOT_FOUND가1이다() {
        UUID userId = UUID.randomUUID();
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.addFailureException(UserNotFoundException.withId(userId));

        listener.afterStep(stepExecution);

        assertThat(getFailureGauge("USER_NOT_FOUND")).isEqualTo(1);
    }

    @Test
    void DataAccessException발생시_DB_ERROR가1이다() {
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.addFailureException(new DataAccessResourceFailureException("db down"));

        listener.afterStep(stepExecution);

        assertThat(getFailureGauge("DB_ERROR")).isEqualTo(1);
    }

    @Test
    void 알수없는예외발생시_UNKNOWN이1이다() {
        StepExecution stepExecution = new StepExecution("testStep", new JobExecution(1L));
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.addFailureException(new RuntimeException("??"));

        listener.afterStep(stepExecution);

        assertThat(getFailureGauge("UNKNOWN")).isEqualTo(1);
    }

    private double getFailureGauge(String reason) {
        return registry.get("batch.user_cleanup.failure")
                .tag("reason", reason)
                .gauge()
                .value();
    }
}