package com.sprint.team2.monew.domain.user.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(404, "사용자 정보가 없습니다."),
    EMAIL_ALREADY_EXISTS(409, "중복된 이메일로 가입할 수 없습니다."),
    INVALID_USER_CREDENTIALS(401, "이메일 또는 비밀번호가 일치하지 않습니다."),
    FORBIDDEN_USER_UPDATE(403, "해당 사용자에 대한 수정 권한이 없습니다."),
    FORBIDDEN_USER_DELETE(403, "해당 사용자에 대한 삭제 권한이 없습니다.");

    private final int status;
    private final String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
