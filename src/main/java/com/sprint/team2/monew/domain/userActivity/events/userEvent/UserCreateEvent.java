package com.sprint.team2.monew.domain.userActivity.events.userEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserCreateEvent {
  private final UUID id; // userid
  private final String email;
  private final String nickname;
  private final LocalDateTime createdAt;
}
