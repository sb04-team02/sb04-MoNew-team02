package com.sprint.team2.monew.domain.user.batch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCleanupJobConfigTest {

    @Autowired
    private Job userCleanupJob;

    @Autowired
    private Step userCleanupStep;

    @Test
    @DisplayName("Job Spring Bean 등록 테스트")
    void jobBeanCreationTest() {
        assertThat(userCleanupJob).isNotNull();
        assertThat(userCleanupJob.getName()).isEqualTo("userCleanupJob");
    }

    @Test
    @DisplayName("Step Spring Bean 등록 테스트")
    void stepBeanCreationTest() {
        assertThat(userCleanupStep).isNotNull();
        assertThat(userCleanupStep.getName()).isEqualTo("userCleanupStep");
    }
}