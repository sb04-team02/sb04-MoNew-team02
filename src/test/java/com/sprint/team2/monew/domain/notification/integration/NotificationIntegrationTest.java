package com.sprint.team2.monew.domain.notification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import com.sprint.team2.monew.domain.notification.service.NotificationService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("알림 목록 조회 API 통합테스트")
    void getAllNotifications() throws Exception {
        //given
        User user = User.builder()
                .email("test@email.com")
                .nickname("Test")
                .password("test123")
                .build();
        userRepository.save(user);

        Notification notification1 = Notification.builder()
                .user(user)
                .content("테스트 알림1")
                .resourceType(ResourceType.COMMENT)
                .resourceId(UUID.randomUUID())
                .confirmed(false)
                .build();

        Notification notification2 = Notification.builder()
                .user(user)
                .content("테스트 알림2")
                .resourceType(ResourceType.INTEREST)
                .resourceId(UUID.randomUUID())
                .confirmed(false)
                .build();

        notificationRepository.saveAll(List.of(notification1, notification2));

        //when &then
        mockMvc.perform(get("/api/notifications")
                        .header("MoNew-Request-User-ID", user.getId())
                        .param("cursor", "")
                        .param("after", LocalDateTime.now().toString())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("알림 목록 조회 API 통합 테스트 - 알림이 존재하지 않을 때 빈 리스트 반환")
    void getNotificationFailureWhenNotificationNotFound() throws Exception {
        // given
        User user = User.builder()
                .email("test@email.com")
                .nickname("Test")
                .password("test123")
                .build();
        userRepository.save(user);

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .header("MoNew-Request-User-ID", user.getId())
                        .param("cursor", "")
                        .param("after", LocalDateTime.now().toString())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("알림 목록 조회 API 통합테스트 - 페이징 동작 확인 테스트")
    void getNotificationsWithPaging() throws Exception {
        // given
        User user = User.builder()
                .email("test@email.com")
                .nickname("Test")
                .password("test123")
                .build();
        userRepository.save(user);

        // 페이징 테스트를 위해 알림 15개 저장
        for (int i = 0; i < 15; i++) {
            notificationRepository.save(Notification.builder()
                    .user(user)
                    .content("알림 " + i)
                    .resourceType(ResourceType.COMMENT)
                    .resourceId(UUID.randomUUID())
                    .confirmed(false)
                    .build());
        }
        //when & then
        // 10개 요청 -> hasNext = true
        mockMvc.perform(get("/api/notifications")
                        .header("MoNew-Request-User-ID", user.getId())
                        .param("cursor", "")
                        .param("after", LocalDateTime.now().toString())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalElements").value(15));

    }

    @Test
    @DisplayName("알림 단건 확인(수정) API 통합 테스트")
    void confirmNotification() throws Exception {
        // given
        User user = User.builder()
                .email("test@email.com")
                .nickname("Test")
                .password("test123")
                .build();
        userRepository.save(user);

        Notification notification = Notification.builder()
                .user(user)
                .content("테스트 알림")
                .resourceType(ResourceType.COMMENT)
                .resourceId(UUID.randomUUID())
                .confirmed(false)
                .build();
        notificationRepository.save(notification);

        //when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", notification.getId())
                        .header("MoNew-Request-User-ID", user.getId()))
                .andExpect(status().isOk());

        //DB에서 상태를 직접 조회
        Notification confirmed = notificationRepository.findById(notification.getId()).orElseThrow();
        assert(confirmed.isConfirmed());
    }

    @Test
    @DisplayName("알림 단건 확인(단건 수정) 실패 API 통합 테스트 - 사용자 정보 없음")
    void confirmNotificationShouldThrowExceptionWhenUserNotFound() throws Exception {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        UUID nonExistentNotificationId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", nonExistentNotificationId)
                    .header("MoNew-Request-User-ID", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("알림 단건 확인(단건 수정) 실패 API 통합 테스트 - 알림 정보 없음")
    void confirmNotificationShouldThrowExceptionWhenNotificationNotFound() throws Exception {
        //given
        User user = User.builder()
                .email("test@email.com")
                .nickname("Test")
                .password("test123")
                .build();
        userRepository.save(user);

        UUID nonExistentUserId = UUID.randomUUID();

        //when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", nonExistentUserId)
                    .header("MoNew-Request-User-ID", user.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTIFICATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("알림 전건 확인(전체 수정) API 통합 테스트")
    void confirmNotifications() throws Exception {
        User user = User.builder()
                .email("test@email.com")
                .nickname("Test")
                .password("test123")
                .build();
        userRepository.save(user);

        Notification notification1 = Notification.builder()
                .user(user)
                .content("테스트 알림1")
                .resourceType(ResourceType.COMMENT)
                .resourceId(UUID.randomUUID())
                .confirmed(false)
                .build();

        Notification notification2 = Notification.builder()
                .user(user)
                .content("테스트 알림2")
                .resourceType(ResourceType.INTEREST)
                .resourceId(UUID.randomUUID())
                .confirmed(false)
                .build();

        notificationRepository.saveAll(List.of(notification1, notification2));

        //when & then
        mockMvc.perform(patch("/api/notifications")
        .header("MoNew-Request-User-ID", user.getId()))
                .andExpect(status().isOk());

        List<Notification> notifications = notificationRepository.findAll();
        assertTrue(notifications.stream()
                .allMatch(Notification ::isConfirmed));
    }

    @Test
    @DisplayName("알림 전건 확인(전체 수정) API 통합 테스트 - 알림 목록이 비어 있어도 정상 응답 반환")
    void confirmNotificationsShouldReturnOkWhenNotificationNotFound() throws Exception {
        User user = User.builder()
                .email("test@email.com")
                .nickname("Test")
                .password("test123")
                .build();
        userRepository.save(user);

        //when & then
        mockMvc.perform(patch("/api/notifications")
        .header("MoNew-Request-User-ID", user.getId()))
                .andExpect(status().isOk());
        List<Notification> notifications = notificationRepository.findAllByUserId(user.getId());
        assertTrue(notifications.isEmpty());
    }
}



