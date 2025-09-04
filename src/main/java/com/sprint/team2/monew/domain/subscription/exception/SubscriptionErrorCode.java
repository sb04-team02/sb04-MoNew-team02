package com.sprint.team2.monew.domain.subscription.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum SubscriptionErrorCode implements ErrorCode {
    ALREADY_EXISTS_SUBSCRIPTION(HttpStatus.CONFLICT.value(), "해당 관심사를 이미 구독중입니다.", "해당 유저는 해당 관심사를 이미 구독중입니다.");

    @Getter
    private int status;
    @Getter
    private String message;
    @Getter
    private String details;

    SubscriptionErrorCode(int status, String message, String details) {
        this.status = status;
        this.message = message;
        this.details = details;
    }
}
