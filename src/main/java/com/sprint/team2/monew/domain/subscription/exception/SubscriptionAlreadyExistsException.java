package com.sprint.team2.monew.domain.subscription.exception;

import com.sprint.team2.monew.domain.interest.exception.InterestErrorCode;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;

import java.util.UUID;

public class SubscriptionAlreadyExistsException extends SubscriptionException {
    public SubscriptionAlreadyExistsException() {
        super(SubscriptionErrorCode.ALREADY_EXISTS_SUBSCRIPTION);
    }

    public static SubscriptionAlreadyExistsException alreadyExists(UUID interestId, UUID userId) {
        SubscriptionAlreadyExistsException exception = new SubscriptionAlreadyExistsException();
        exception.addDetail("interestId", interestId);
        exception.addDetail("userId", userId);
        return exception;
    }
}
