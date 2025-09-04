package com.sprint.team2.monew.domain.interest.exception;

import java.util.UUID;

public class InterestNotFoundException extends InterestException {
    public InterestNotFoundException() {
        super(InterestErrorCode.INTEREST_NOT_FOUND);
    }

    public static InterestNotFoundException notFound(UUID interestId) {
        InterestNotFoundException exception = new InterestNotFoundException();
        exception.addDetail("interestId", interestId);
        return exception;
    }
}
