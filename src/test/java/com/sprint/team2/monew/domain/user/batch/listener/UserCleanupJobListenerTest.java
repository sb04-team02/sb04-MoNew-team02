package com.sprint.team2.monew.domain.user.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UserCleanupJobListenerTest {

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private UserCleanupJobListener listener;

    @Test
    @DisplayName("beforeJob running 설정 테스트")
    void beforeJobTest() {
        JobExecution jobExecution = new JobExecution(1L);
        listener.beforeJob(jobExecution);

        // Job 실행 후 정상적으로 beforeJob이 수행되어 running의 값이 1로 변경되는지 확인 (running 0: 종료, 1: 실행중)
        assertThat(registry.get("batch.user_cleanup.running").gauge().value())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Job 성공시 afterJob Metric 설정 테스트")
    void afterJobTestSuccessMetric() {
        JobExecution jobExecution = new JobExecution(1L);
        jobExecution.getExecutionContext().putLong("deletedCount", 3);

        listener.beforeJob(jobExecution);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        listener.afterJob(jobExecution);

        /*
        * running(실행 여부): 0 (0: 종료, 1: 실행중)
        * total_success(이전 배치 모두 포함 총합 성공 횟수): 1
        * total_failure(이전 배치 모두 포함 총합 실패 횟수): 0
        * total_deleted_count(이전 배치 모두 포함 총합 삭제 사용자 수): 3
        * current_success(현재 배치 성공 여부): 1 (0: 실패, 1: 성공)
        * current_deleted_count(현재 배치에서 삭제된 사용자 수): 3
        * */
        assertAll(
                () -> assertEquals(0, registry.get("batch.user_cleanup.running").gauge().value()),

                () -> assertEquals(1, registry.get("batch.user_cleanup.total_success").counter().count()),
                () -> assertEquals(0, registry.get("batch.user_cleanup.total_failure").counter().count()),
                () -> assertEquals(3, registry.get("batch.user_cleanup.total_deleted_count").counter().count()),

                () -> assertEquals(1, registry.get("batch.user_cleanup.current_success").gauge().value()),
                () -> assertEquals(3, registry.get("batch.user_cleanup.current_deleted_count").gauge().value())
        );
    }

    @Test
    @DisplayName("Job 실패시 afterJob Metric 설정 테스트")
    void afterJobTestFailureMetric() {
        JobExecution jobExecution = new JobExecution(1L);

        listener.beforeJob(jobExecution);
        jobExecution.setStatus(BatchStatus.FAILED);
        listener.afterJob(jobExecution);

        /*
         * running(실행 여부): 0 (0: 종료, 1: 실행중)
         * total_success(이전 배치 모두 포함 총합 성공 횟수): 0
         * total_failure(이전 배치 모두 포함 총합 실패 횟수): 1
         * total_deleted_count(이전 배치 모두 포함 총합 삭제 사용자 수): 0
         * current_success(현재 배치 성공 여부): 0 (0: 실패, 1: 성공)
         * current_deleted_count(현재 배치에서 삭제된 사용자 수): 0
         * */
        assertAll(
                () -> assertEquals(0, registry.get("batch.user_cleanup.running").gauge().value()),

                () -> assertEquals(0, registry.get("batch.user_cleanup.total_success").counter().count()),
                () -> assertEquals(1, registry.get("batch.user_cleanup.total_failure").counter().count()),
                () -> assertEquals(0, registry.get("batch.user_cleanup.total_deleted_count").counter().count()),

                () -> assertEquals(0, registry.get("batch.user_cleanup.current_success").gauge().value()),
                () -> assertEquals(0, registry.get("batch.user_cleanup.current_deleted_count").gauge().value())
        );
    }
}