package com.sprint.team2.monew.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    log.error("커스텀 예외 발생: code={}, message={}", e.getErrorCode(), e.getMessage(), e);
    return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(new ErrorResponse(e));
  }
}