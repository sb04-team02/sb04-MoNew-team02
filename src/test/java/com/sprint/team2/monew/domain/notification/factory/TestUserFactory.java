package com.sprint.team2.monew.domain.notification.factory;

import com.sprint.team2.monew.domain.user.entity.User;

public class TestUserFactory {

    public static User createUser() {
        return User.builder()
                .nickname("테스트")
                .build();
    }
}
