package com.sprint.team2.monew.domain.user.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCleanupJobListenerTest {

    @Autowired
    private MeterRegistry registry;

    private UserCleanupJobListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserCleanupJobListener(registry);
    }

    @Test
    @DisplayName("beforeJob에서_running이1로설정된다")
    void beforeJobRunningMetricTest() {
        JobExecution jobExecution = new JobExecution(1L);
        listener.beforeJob(jobExecution);

        assertThat(registry.get("batch.user_cleanup.running").gauge().value())
                .isEqualTo(1);
    }

    @Test
    void afterJob_성공시_성공메트릭이갱신된다() {
        JobExecution jobExecution = new JobExecution(1L);
        jobExecution.getExecutionContext().putLong("deletedCount", 3);

        listener.beforeJob(jobExecution);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        listener.afterJob(jobExecution);

        assertThat(registry.get("batch.user_cleanup.current_success").gauge().value())
                .isEqualTo(1);
        assertThat(registry.get("batch.user_cleanup.current_deleted_count").gauge().value())
                .isEqualTo(3);
    }

    @Test
    void afterJob_실패시_실패메트릭이갱신된다() {
        JobExecution jobExecution = new JobExecution(1L);

        listener.beforeJob(jobExecution);
        jobExecution.setStatus(BatchStatus.FAILED);
        listener.afterJob(jobExecution);

        assertThat(registry.get("batch.user_cleanup.current_success").gauge().value())
                .isEqualTo(0);
    }
}