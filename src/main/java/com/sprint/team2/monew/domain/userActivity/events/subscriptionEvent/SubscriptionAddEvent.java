package com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubscriptionAddEvent {
  private final UUID id; // subscription id
  private final UUID interestId;
  private final String interestName;
  private final List<String> interestKeywords;
  private final long interestSubscriberCount;
  private final LocalDateTime createdAt;
  private final UUID userId;
}
