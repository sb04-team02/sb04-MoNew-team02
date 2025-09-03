package com.sprint.team2.monew.global.error;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.Getter;

@Getter
public enum BaseErrorCode implements ErrorCode {
    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String message;

    BaseErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
