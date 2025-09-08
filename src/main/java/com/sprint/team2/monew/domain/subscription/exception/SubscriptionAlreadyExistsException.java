package com.sprint.team2.monew.domain.subscription.exception;

import java.util.UUID;

public class SubscriptionAlreadyExistsException extends SubscriptionException {
    public SubscriptionAlreadyExistsException() {
        super(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
    }

    public static SubscriptionAlreadyExistsException alreadyExists(UUID interestId, UUID userId) {
        SubscriptionAlreadyExistsException exception = new SubscriptionAlreadyExistsException();
        exception.addDetail("interestId", interestId);
        exception.addDetail("userId", userId);
        return exception;
    }
}
