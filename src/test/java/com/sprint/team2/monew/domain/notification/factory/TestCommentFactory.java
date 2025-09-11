package com.sprint.team2.monew.domain.notification.factory;

import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.user.entity.User;

import java.util.UUID;

public class TestCommentFactory {

    public static Comment createComment() {
        User user = TestUserFactory.createUser();
        return Comment.builder()
                .user(user)
                .build();
    }
}
