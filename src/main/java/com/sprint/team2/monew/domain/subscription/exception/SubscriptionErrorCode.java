package com.sprint.team2.monew.domain.subscription.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SubscriptionErrorCode implements ErrorCode {
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "해당 관심사를 이미 구독중입니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 유저는 해당 관심사를 구독중이 아닙니다.");

    private int status;
    private String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
