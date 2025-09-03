package com.sprint.team2.monew.domain.user.entity;

import com.sprint.team2.monew.domain.base.DeletableEntity;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends DeletableEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20, nullable = false)
    private String password;

    @Column(length = 20, nullable = false)
    private String nickname;

    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public void update(String newNickname) {
        if (!StringUtils.isBlank(newNickname) && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
        }
    }
}
