package com.sprint.team2.monew.domain.userActivity.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserActivityErrorCode implements ErrorCode {
  USER_ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "활동 내역 정보가 없습니다.");

  private final int status;
  private final String message;

  @Override
  public String getErrorCodeName() {
    return name();
  }

}
