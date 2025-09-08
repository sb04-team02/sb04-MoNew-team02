package com.sprint.team2.monew.domain.userActivity.controller;

import com.sprint.team2.monew.domain.userActivity.dto.response.UserActivityResponseDto;
import com.sprint.team2.monew.domain.userActivity.service.UserActivityService;
import com.sprint.team2.monew.domain.userActivity.service.basic.BasicUserActivityService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/user-activities")
public class UserActivityController {

  private final BasicUserActivityService userActivityService;

  @GetMapping("{userId}")
  public ResponseEntity<UserActivityResponseDto> find(@PathVariable("userId") UUID userId) {
    log.info("[활동 내역] 활동 내역 정보 요청 수신");
    UserActivityResponseDto userActivityResponseDto = userActivityService.getUserActivity(userId);
    log.info("[활동 내역] 활동 내역 정보 응답 - userId={}", userActivityResponseDto.id());

    return ResponseEntity.status(HttpStatus.OK)
        .body(userActivityResponseDto);
  }

}
