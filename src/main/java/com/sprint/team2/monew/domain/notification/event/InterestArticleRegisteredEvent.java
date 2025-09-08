package com.sprint.team2.monew.domain.notification.event;

import java.util.UUID;

public record InterestArticleRegisteredEvent (
    UUID interestId,
    UUID articleId,
    UUID receiverId
 ){}
