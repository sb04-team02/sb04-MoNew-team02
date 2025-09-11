package com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class SubscriptionDeleteEvent {
  private final UUID id; // subscription Id
  private final UUID interestId;
  private final UUID userId;
}
