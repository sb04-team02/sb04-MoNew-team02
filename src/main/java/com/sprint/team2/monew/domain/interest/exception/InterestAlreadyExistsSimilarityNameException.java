package com.sprint.team2.monew.domain.interest.exception;

public class InterestAlreadyExistsSimilarityNameException extends InterestException{
    public InterestAlreadyExistsSimilarityNameException() {
        super(InterestErrorCode.INTEREST_ALREADY_EXISTS_SIMILARITY_NAME);
    }

    public static InterestAlreadyExistsSimilarityNameException alreadyExistsSimilarityName(String name) {
        InterestAlreadyExistsSimilarityNameException exception = new InterestAlreadyExistsSimilarityNameException();
        exception.addDetail("similarName", name);
        return exception;
    }
}
