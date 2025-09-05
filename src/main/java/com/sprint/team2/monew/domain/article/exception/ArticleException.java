package com.sprint.team2.monew.domain.article.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import com.sprint.team2.monew.global.error.BusinessException;

public class ArticleException extends BusinessException {
    public ArticleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
