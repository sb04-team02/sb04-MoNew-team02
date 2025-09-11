package com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent;

import java.util.UUID;

public record SubscriptionCancelEvent (
  UUID id, // subscription Id
  UUID interestId,
  UUID userId
){

}
