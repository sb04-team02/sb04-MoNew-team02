package com.sprint.team2.monew.domain.subscription.entity;

import com.sprint.team2.monew.domain.base.BaseEntity;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="subscriptions")
public class Subscription extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "interest_id", nullable = false)
    Interest interest;
}
