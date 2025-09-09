package com.sprint.team2.monew.domain.notification.controller;

import com.sprint.team2.monew.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.sprint.team2.monew.domain.notification.dto.response.NotificationDto;
import com.sprint.team2.monew.domain.notification.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CursorPageResponseNotificationDto> getNotifications(
            @RequestHeader ("MoNew-Request-User-ID") @NotNull UUID userId,
            @RequestParam (value = "cursor", required = false) String nextCursor,
            @RequestParam (value = "after", required = false)LocalDateTime nextAfter,
            @RequestParam (value = "limit") @Positive int size
            ) {
        log.info("[알림] 확인하지 않은 알림 조회 요청 / 요청자 ID={}", userId);
        CursorPageResponseNotificationDto response = notificationService.getAllNotifications(userId,nextAfter,size);
        log.info("[알림] 확인하지 않은 알림 조회 응답 / 요청자 ID={}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping(value = "/{notificationId}")
    public ResponseEntity updateNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("MoNew-Request-User-ID") @NotNull UUID userId
            ) {
        log.info("[알림] 알림 확인 상태 단건 수정 요청 / 요청자 ID={}, 알림 ID={}",userId, notificationId);
        notificationService.confirmNotification(userId,notificationId);
        log.info("[알림] 알림 확인 상태 단건 수정 응답 / 요청자 ID={}, 알림 ID={}\",userId, notificationId");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping
    public ResponseEntity updateNotifications(
            @RequestHeader("MoNew-Request-User-ID") @NotNull UUID userId
    ) {
        log.info("[알림] 알림 확인 상태 일괄 수정 요청 / 요청자 ID={}",userId);
        notificationService.confirmAllNotifications(userId);

    }
}
