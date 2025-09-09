package com.sprint.team2.monew.domain.notification.controller;

import com.sprint.team2.monew.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CursorPageResponseNotificationDto> getNotifications(
            @RequestHeader ("MoNew-Request-User-ID") UUID userId,
            @RequestParam (value = "cursor", required = false) String nextCursor,
            @RequestParam (value = "after", required = false)LocalDateTime nextAfter,
            @RequestParam (value = "limit") int size
            ) {
        log.info("[알림] 확인하지 않은 알림 조회 요청 / 요청자 ID={}", userId);
        CursorPageResponseNotificationDto response = notificationService.getAllNotifications(userId,nextAfter,size);
        log.info("[알림] 확인하지 않은 알림 조회 응답 / 요청자 ID={}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
