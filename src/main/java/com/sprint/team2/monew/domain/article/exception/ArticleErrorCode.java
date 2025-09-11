package com.sprint.team2.monew.domain.article.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ArticleErrorCode implements ErrorCode {
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "뉴스 기사 정보가 없습니다."),
    NAVER_API_FAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Naver API 호출 실패"),
    EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Naver API가 빈 응답 반환"),
    ARTICLE_COLLECT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "뉴스 기사 수집 살패"),
    ARTICLE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "뉴스 기사 저장 실패"),
    INVALIED_PARAMETER(HttpStatus.NO_CONTENT.value(), "잘못된 파라미터"),

    // 뉴스 기사 복구
    S3_ACCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S3 백업 파일 접근에 실패했습니다.");

    private final int status;
    private final String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
