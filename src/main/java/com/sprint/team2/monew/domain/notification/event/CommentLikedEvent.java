package com.sprint.team2.monew.domain.notification.event;


import java.util.UUID;


public record CommentLikedEvent (
        UUID commentId,
        UUID receiverId,
        UUID likedUserId
) { }