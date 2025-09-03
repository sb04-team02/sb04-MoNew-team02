package com.sprint.team2.monew.domain.user.entity;

import com.sprint.team2.monew.domain.base.DeletableEntity;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends DeletableEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20, nullable = false)
    private String password;

    @Column(length = 20, nullable = false)
    private String nickname;

    public void update(String newNickname) {
        if (!StringUtils.isBlank(newNickname) && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
        }
    }
}
