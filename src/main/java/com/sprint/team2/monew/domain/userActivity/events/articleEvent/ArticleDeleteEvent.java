package com.sprint.team2.monew.domain.userActivity.events.articleEvent;

import java.util.UUID;

public record ArticleDeleteEvent(
    UUID articleId
) {

}
