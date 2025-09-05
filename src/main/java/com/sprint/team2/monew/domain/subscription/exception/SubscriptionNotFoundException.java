package com.sprint.team2.monew.domain.subscription.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;

import java.util.UUID;

public class SubscriptionNotFoundException extends SubscriptionException{
    public SubscriptionNotFoundException() {
        super(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
    }
    public static SubscriptionNotFoundException notFound(UUID interestId, UUID userId) {
        SubscriptionNotFoundException exception = new SubscriptionNotFoundException();
        exception.addDetail("interestId", interestId);
        exception.addDetail("userId", userId);
        return exception;
    }
}
