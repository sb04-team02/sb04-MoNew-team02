package com.sprint.team2.monew.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.sprint.team2.monew.domain.notification.dto.response.NotificationDto;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.exception.NotificationNotFoundException;
import com.sprint.team2.monew.domain.notification.service.NotificationService;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("요청이 유효하다면 알림 목록 조회 성공")
    void getNotifications() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        LocalDateTime nextAfter = LocalDateTime.now().minusDays(1);
        String nextCursor = LocalDateTime.now().minusHours(3).toString();
        int size = 10;
        Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
        List<NotificationDto> notificationDtoList = List.of(
                new NotificationDto(
                        UUID.randomUUID(),
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().minusMinutes(30),
                        false,
                        UUID.randomUUID(),
                        "알림1",
                        ResourceType.COMMENT,
                        UUID.randomUUID()
                ),
                new NotificationDto(
                        UUID.randomUUID(),
                        LocalDateTime.now().minusHours(2),
                        LocalDateTime.now().minusMinutes(20),
                        false,
                        UUID.randomUUID(),
                        "알림2",
                        ResourceType.INTEREST,
                        UUID.randomUUID()
                )
        );

        CursorPageResponseNotificationDto response = new CursorPageResponseNotificationDto(
                notificationDtoList,
                nextCursor,
                nextAfter,
                size,
                2L,
                false
        );
        given(notificationService.getAllNotifications(nextCursor, userId,nextAfter,size)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/notifications")
                        .header("Monew-Request-User-ID",userId)
                        .param("cursor", nextCursor)
                        .param("after", nextAfter.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("limit", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("알림 목록 조회 실패-존재하지 않는 사용자")
    void getNotificationsFailWhenUserNotFound() throws Exception {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        LocalDateTime after = LocalDateTime.now().minusDays(1);
        String nextCursor = LocalDateTime.now().minusHours(3).toString();

        int size = 10;

        given(notificationService.getAllNotifications(nextCursor, nonExistentUserId, after, size))
                .willThrow(UserNotFoundException.withId(nonExistentUserId));

        //when
        ResultActions resultActions = mockMvc.perform(
                get("/api/notifications")
                        .header("Monew-Request-User-ID",nonExistentUserId.toString())
                        .param("cursor", nextCursor)
                        .param("after", after.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("limit", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("요청이 유효하다면 알림 확인 상태 단건 수정 성공")
    void confirmNotification() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        willDoNothing().given(notificationService).confirmNotification(userId, notificationId);

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/api/notifications/{notificationID}", notificationId)
                        .header("Monew-Request-User-ID",userId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("알림 확인 상태 단건 수정 실패 - 존재하지 않는 알림 ID")
    void confirmNotificationFailWhenInvalidNotificationIdFormat() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID nonExistentNotificationId = UUID.randomUUID();

        willThrow(NotificationNotFoundException.withId(nonExistentNotificationId))
        .given(notificationService).confirmNotification(userId, nonExistentNotificationId);

        //when
        ResultActions resultActions = mockMvc.perform(
                patch("/api/notifications/{notificationID}", nonExistentNotificationId.toString())
                        .header("Monew-Request-User-ID",userId)
                        .accept(MediaType.APPLICATION_JSON)
        );
        //then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("알림 확인 상태 일괄 수정 성공")
    void confirmNotifications() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        LocalDateTime after = LocalDateTime.now().minusDays(1);
        int size = 10;

        willDoNothing().given(notificationService).confirmAllNotifications(userId);

        //when
        ResultActions resultActions = mockMvc.perform(
                patch("/api/notifications")
                        .header("Monew-Request-User-ID",userId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("알림 확인 상태 일괄 수정 실패 - 존재하지 않는 사용자")
    void confirmNotificationFailWhenUserNotFound () throws Exception {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        willThrow(UserNotFoundException.withId(nonExistentUserId))
                .given(notificationService).confirmAllNotifications(nonExistentUserId);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                patch("/api/notifications")
                        .header("Monew-Request-User-ID",nonExistentUserId)
                        .accept(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isNotFound());
    }
}
