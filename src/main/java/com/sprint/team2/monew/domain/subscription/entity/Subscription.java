package com.sprint.team2.monew.domain.subscription.entity;

import com.sprint.team2.monew.domain.base.BaseEntity;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="subscriptions")
@AllArgsConstructor
@NoArgsConstructor
public class Subscription extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToOne
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;
}
