package com.sprint.team2.monew.domain.user.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;

import java.util.UUID;

public class ForbiddenUserAuthorityException extends UserException {
    public ForbiddenUserAuthorityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static ForbiddenUserAuthorityException forUpdate(UUID userId, UUID loginUserId) {
        ForbiddenUserAuthorityException updateException = new ForbiddenUserAuthorityException(UserErrorCode.FORBIDDEN_USER_UPDATE);
        updateException.addDetail("userId", userId);
        updateException.addDetail("loginUserId", loginUserId);
        return updateException;
    }

    public static ForbiddenUserAuthorityException forDelete(UUID userId, UUID loginUserId) {
        ForbiddenUserAuthorityException deleteException = new ForbiddenUserAuthorityException(UserErrorCode.FORBIDDEN_USER_DELETE);
        deleteException.addDetail("userId", userId);
        deleteException.addDetail("loginUserId", loginUserId);
        return deleteException;
    }
}
