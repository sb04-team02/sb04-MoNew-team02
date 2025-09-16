package com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent;

import java.util.List;
import java.util.UUID;

public record SubscriptionKeywordUpdateEvent(
    UUID interestId,
    List<String> keywords
) {

}
