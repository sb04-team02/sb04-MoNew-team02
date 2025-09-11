package com.sprint.team2.monew.domain.userActivity.events.userEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserCreateEvent (
  UUID id, // userid
  String email,
  String nickname,
  LocalDateTime createdAt
){
}
