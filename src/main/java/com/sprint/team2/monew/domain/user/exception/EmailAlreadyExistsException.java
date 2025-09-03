package com.sprint.team2.monew.domain.user.exception;

public class EmailAlreadyExistsException extends UserException {
    public EmailAlreadyExistsException() {
        super(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }

    public static EmailAlreadyExistsException emailDuplicated(String email) {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException();
        exception.addDetail("email", email);
        return exception;
    }
}
