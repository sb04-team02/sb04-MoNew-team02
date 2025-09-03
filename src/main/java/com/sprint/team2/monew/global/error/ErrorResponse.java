package com.sprint.team2.monew.global.error;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

  private String message;
  private String details;
  private int status;
  private LocalDateTime timestamp;

  public ErrorResponse(ErrorCode code) {
    this.message = code.getMessage();
    this.status = code.getStatus();
    this.details = code.getDetails();
    this.timestamp = LocalDateTime.now();
  }

}
