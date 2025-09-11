package com.sprint.team2.monew.domain.userActivity.events.userEvent;

import java.util.UUID;

public record UserDeleteEvent (
  UUID userId
) {
}
