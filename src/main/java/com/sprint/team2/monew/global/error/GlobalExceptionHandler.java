package com.sprint.team2.monew.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(BusinessException e) {
    return ResponseEntity.status(e.getErrorCode().getStatus())
        .body(new ErrorResponse(e.getErrorCode()));
  }
}
