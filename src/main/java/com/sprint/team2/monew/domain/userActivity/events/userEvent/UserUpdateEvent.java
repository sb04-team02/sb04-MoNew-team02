package com.sprint.team2.monew.domain.userActivity.events.userEvent;

import java.util.UUID;

public record UserUpdateEvent (
  String nickname,
  UUID id
){
}
