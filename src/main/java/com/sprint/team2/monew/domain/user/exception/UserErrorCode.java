package com.sprint.team2.monew.domain.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "사용자 정보가 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "중복된 이메일로 가입할 수 없습니다."),
    INVALID_USER_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "이메일 또는 비밀번호가 일치하지 않습니다."),
    FORBIDDEN_USER_UPDATE(HttpStatus.FORBIDDEN.value(), "해당 사용자에 대한 수정 권한이 없습니다."),
    FORBIDDEN_USER_DELETE(HttpStatus.FORBIDDEN.value(), "해당 사용자에 대한 삭제 권한이 없습니다.");

    private final int status;
    private final String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
