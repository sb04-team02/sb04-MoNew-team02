package com.sprint.team2.monew.domain.interest.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterestErrorCode implements ErrorCode {
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "관심사를 찾을 수 없습니다.");


    private final int status;
    private final String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
