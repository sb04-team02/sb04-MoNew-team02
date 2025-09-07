package com.sprint.team2.monew.domain.notification.event;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.user.entity.User;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InterestArticleRegisteredEvent {
    private final UUID interestId;
    private final UUID articleId;
    private final UUID userId;
    public InterestArticleRegisteredEvent(UUID interestId, UUID articleId, UUID userId) {
        this.interestId = interestId;
        this.articleId = articleId;
        this.userId = userId;
    }
}
