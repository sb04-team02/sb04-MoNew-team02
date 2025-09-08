package com.sprint.team2.monew.domain.notification.factory;

import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import com.sprint.team2.monew.domain.user.entity.User;

public class TestLikeFactory {

    public static Reaction createLike() {
        User user = TestUserFactory.createUser();
        Comment comment = TestCommentFactory.createComment();
        return Reaction.builder()
                .user(user)
                .comment(comment)
                .build();
    }
}
