package com.sprint.team2.monew.domain.userActivity.events.userEvent;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserUpdateEvent {
  private final UUID id;
  private final String nickname;
}
