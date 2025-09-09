package com.sprint.team2.monew.domain.user.batch.config;

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
    void 잡이_빈으로등록되어야한다() {
        assertThat(userCleanupJob).isNotNull();
        assertThat(userCleanupJob.getName()).isEqualTo("userCleanupJob");
    }

    @Test
    void 스텝이_빈으로등록되어야한다() {
        assertThat(userCleanupStep).isNotNull();
        assertThat(userCleanupStep.getName()).isEqualTo("userCleanupStep");
    }
}