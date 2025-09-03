package com.sprint.team2.monew.domain.user.exception;

public class InvalidUserCredentialsException extends UserException {
    public InvalidUserCredentialsException() {
        super(UserErrorCode.INVALID_USER_CREDENTIALS);
    }
    public static InvalidUserCredentialsException wrongPassword() {
        InvalidUserCredentialsException exception = new InvalidUserCredentialsException();
        return exception;
    }
}
