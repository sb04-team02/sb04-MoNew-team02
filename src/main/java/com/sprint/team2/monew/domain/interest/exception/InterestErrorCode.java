package com.sprint.team2.monew.domain.interest.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InterestErrorCode implements ErrorCode {
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "관심사를 찾을 수 없습니다."),
    INTEREST_ALREADY_EXISTS_SIMILARITY_NAME(HttpStatus.CONFLICT.value(), "비슷한 관심사가 이미 존재합니다.");

    private int status;
    private String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
