package com.sprint.team2.monew.domain.notification.batch.config;

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
public class NotificationCleanupSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job job;

    @InjectMocks
    private NotificationCleanupScheduler notificationCleanupScheduler;

    @Test
    @DisplayName("JobLauncher 호출 여부 테스트")
    public void testJobLauncher() throws Exception {
        //when
        notificationCleanupScheduler.runJob();

        //then
        verify(jobLauncher,times(1))
                .run(eq(job),any(JobParameters.class));
    }
}
