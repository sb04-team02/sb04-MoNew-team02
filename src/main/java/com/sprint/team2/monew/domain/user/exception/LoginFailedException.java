package com.sprint.team2.monew.domain.user.exception;

public class LoginFailedException extends UserException {
    public LoginFailedException() {
        super(UserErrorCode.LOGIN_FAILED);
    }

    public static LoginFailedException wrongEmailOrPassword() {
        return new LoginFailedException();
    }
}
