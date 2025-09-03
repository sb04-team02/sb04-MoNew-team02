package com.sprint.team2.monew.domain.user.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;

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
