package com.sprint.team2.monew.domain.userActivity.service;

import com.sprint.team2.monew.domain.userActivity.dto.response.UserActivityResponseDto;
import java.util.UUID;

public interface UserActivityService {

  UserActivityResponseDto getUserActivity(UUID userId);

}
