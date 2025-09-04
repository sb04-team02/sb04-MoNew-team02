package com.sprint.team2.monew.domain.interest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum InterestErrorCode implements ErrorCode {
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "관심사를 찾을 수 없습니다.", "해당 ID를 가진 관심사가 존재하지 않습니다.");

    @Getter
    private int status;
    @Getter
    private String message;
    @Getter
    private String details;

    InterestErrorCode(int status, String message, String details) {
        this.status = status;
        this.message = message;
        this.details = details;
    }
}
