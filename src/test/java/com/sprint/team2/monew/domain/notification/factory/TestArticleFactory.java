package com.sprint.team2.monew.domain.notification.factory;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.interest.entity.Interest;

public class TestArticleFactory {

    public static Article createArticle() {
        Interest interest = TestInterestFactory.createInterest();
        return Article.builder()
                .title("아무 제목")
                .source("아무 출처")
                .interest(interest)
                .build();
    }
}
