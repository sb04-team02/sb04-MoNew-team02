package com.sprint.team2.monew.domain.user.exception;

public class ForbiddenUserAuthorityException extends UserException {
    public ForbiddenUserAuthorityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static ForbiddenUserAuthorityException forUpdate() {
        return new ForbiddenUserAuthorityException(UserErrorCode.FORBIDDEN_USER_UPDATE);
    }

    public static ForbiddenUserAuthorityException forDelete() {
        return new ForbiddenUserAuthorityException(UserErrorCode.FORBIDDEN_USER_DELETE);
    }
}
