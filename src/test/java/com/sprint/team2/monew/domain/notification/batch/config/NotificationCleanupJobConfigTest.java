package com.sprint.team2.monew.domain.notification.batch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class NotificationCleanupJobConfigTest {

    @Autowired
    @Qualifier("notificationCleanupJob")
    private Job notificationCleanupJob;

    @Autowired
    @Qualifier("notificationCleanupStep")
    private Step notificationCleanupStep;

    @Test
    @DisplayName("Job Spring Bean 등록 테스트")
    public void testJobSpringBean() {
        assertThat(notificationCleanupJob).isNotNull();
        assertThat(notificationCleanupJob.getName()).isEqualTo("NotificationCleanupJob");
    }

    @Test
    @DisplayName("Step Spring Bean 등록 테스트")
    public void testStepSpringBean() {
        assertThat(notificationCleanupStep).isNotNull();
        assertThat(notificationCleanupStep.getName()).isEqualTo("NotificationCleanupStep");
    }

}
