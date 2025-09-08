package com.sprint.team2.monew.domain.like.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReactionErrorCode implements ErrorCode {
    REACTION_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "이미 좋아요를 눌렀습니다."),
    REACTION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "좋아요 내역을 찾을 수 없습니다.");

    private final int status;
    private final String message;

    @Override
    public String getErrorCodeName() {return name();}
}
