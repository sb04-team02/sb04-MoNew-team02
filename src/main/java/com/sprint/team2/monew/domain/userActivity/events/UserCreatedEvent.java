package com.sprint.team2.monew.domain.userActivity.events;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserCreatedEvent {
  private final UUID id; // userid
  private final String email;
  private final String nickname;
  private final LocalDateTime createdAt;
}
