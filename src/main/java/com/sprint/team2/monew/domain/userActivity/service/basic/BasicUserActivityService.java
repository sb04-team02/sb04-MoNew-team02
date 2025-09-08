package com.sprint.team2.monew.domain.userActivity.service.basic;

import com.sprint.team2.monew.domain.userActivity.dto.response.UserActivityResponseDto;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.exception.UserActivityNotFoundException;
import com.sprint.team2.monew.domain.userActivity.mapper.UserActivityMapper;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import com.sprint.team2.monew.domain.userActivity.service.UserActivityService;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserActivityService implements UserActivityService {

  public final UserActivityRepository userActivityRepository;
  public final UserActivityMapper userActivityMapper;

  public UserActivityResponseDto getUserActivity(UUID userId) {

    log.info("[활동 내역] 조회 시작 - userId={}", userId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));

    log.info("[활동 내역] 조회 완료 - userId={}", userId);

    return userActivityMapper.toUserActivityResponseDto(userActivity);
  }

}
