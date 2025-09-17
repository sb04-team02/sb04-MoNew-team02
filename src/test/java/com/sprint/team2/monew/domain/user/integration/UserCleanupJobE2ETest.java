package com.sprint.team2.monew.domain.user.integration;

import com.sprint.team2.monew.domain.user.batch.config.UserCleanupJobConfig;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.user.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@SpringBatchTest
@Import(UserCleanupJobConfig.class)
class UserCleanupJobE2ETest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job userCleanupJob;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MeterRegistry registry;

    @TestConfiguration
    public static class TestMetricsConfig {
        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E - UserCleanupJob 실행 후 DB와 Metric 검증")
    void userCleanupJobEndToEnd() throws Exception {
        // given
        // 기본 객체 설정: 서비스로 생성, 저장
        UserRegisterRequest deletingUserRegisterRequest = new UserRegisterRequest("test1@email.com", "toDelete", "test1234");
        UserRegisterRequest keepingUserRegisterRequest = new UserRegisterRequest("test2@email.com", "toKeep", "test1234");

        UserDto deletingUserDto = userService.create(deletingUserRegisterRequest);
        UserDto keepingUserDto = userService.create(keepingUserRegisterRequest);

        User deletingUser = userRepository.findById(deletingUserDto.id())
                .orElseThrow(() -> UserNotFoundException.withId(deletingUserDto.id()));
        User keepingUser = userRepository.findById(keepingUserDto.id())
                .orElseThrow(() -> UserNotFoundException.withId(keepingUserDto.id()));

        // 논리적 삭제 설정
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        deletingUser.setDeletedAt(threshold.minusHours(3)); // 삭제 대상
        keepingUser.setDeletedAt(LocalDateTime.now()); // 유지 대상
        userRepository.save(keepingUser);
        userRepository.save(deletingUser);
        userRepository.flush();

        // when: Job 실행
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.setJob(userCleanupJob);
        jobLauncherTestUtils.launchJob(params);

        // then: Job 성공 상태 확인

        // DB 검증 (삭제 대상만 제거되었는지)
        List<User> remainingUsers = userRepository.findAll();
        assertThat(remainingUsers)
                .extracting(User::getNickname)
                .containsExactly("toKeep");

        // Metric 검증 (Listener에서 기록된 값 확인)
        assertAll(
                () -> assertEquals(0.0, registry.get("batch.user_cleanup.running").gauge().value()),
                () -> assertEquals(1.0, registry.get("batch.user_cleanup.total_success").counter().count()),
                () -> assertEquals(0.0, registry.get("batch.user_cleanup.total_failure").counter().count()),
                () -> assertEquals(1.0, registry.get("batch.user_cleanup.total_deleted_count").counter().count()),
                () -> assertEquals(1.0, registry.get("batch.user_cleanup.current_success").gauge().value()),
                () -> assertEquals(1.0, registry.get("batch.user_cleanup.current_deleted_count").gauge().value())
        );
    }
}
