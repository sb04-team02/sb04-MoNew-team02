package com.sprint.team2.monew.domain.user.entity;

import com.sprint.team2.monew.domain.base.DeletableEntity;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User extends DeletableEntity {

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 20, nullable = false)
    private String password;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    public void update(String newNickname) {
        boolean isUpdated = false;
        if (!StringUtils.isBlank(newNickname) && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
            isUpdated = true;
        }
        if (isUpdated) {
            this.setUpdatedAt(LocalDateTime.now());
        }
    }
}
