package com.sprint.team2.monew.domain.user.batch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCleanupSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job userCleanupJob;

    @InjectMocks
    private UserCleanupScheduler scheduler;

    @Test
    @DisplayName("Job 실행 테스트")
    void runJobTest() throws Exception {
        // when
        scheduler.runJob();

        // then
        verify(jobLauncher, times(1))
                .run(eq(userCleanupJob), any(JobParameters.class));
    }
}